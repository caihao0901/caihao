package com.wsights.areabox.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wsights.areabox.common.constant.StaticConstant;
import com.wsights.areabox.common.constant.ThirdInterface;
import com.wsights.areabox.common.util.RequestContextUtils;
import com.wsights.areabox.mapper.doc.HandlingTotalInfo;
import com.wsights.areabox.mapper.doc.ImportPortPlanMapper;
import com.wsights.areabox.mapper.main.ImportPlanDBMapper;
import com.wsights.areabox.service.ImportPortPlanService;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @Author caihao
 * @Date 2020/4/24 13:20
 * 问题：开发过程中发现sql server的insert语句会报错，大致报错是说最多支持2100个参数
 * 解决：最开始有人提议数据量不太多的情况下，可以多次开关数据库进行处理，最后处于性能考虑，用了下面的切割字符串的方式
 * 备注：2100个参数指的是insert的参数（总数量）
 */
@Service
public class ImportPortPlanServiceImpl implements ImportPortPlanService {
    @Resource
    ImportPortPlanMapper importPortPlanMapper;
    @Resource
    ImportPlanDBMapper importPlanDBMapper;



    @Override
    @Transactional(value = "docTransaction",rollbackFor = Exception.class)
    public HandlingInfoDTO dischargeInsert(HandlingInfoDTO handlingInfoDTO){
        HandlingInfoDTO backDTO = new HandlingInfoDTO();
        //当前数据大于1，删除原有的计划表和明细表的数据
        if(importPortPlanMapper.getTotalCount(handlingInfoDTO).getCheckInfoCount() >= 1){
            importPortPlanMapper.deleteDetail(handlingInfoDTO);
            importPortPlanMapper.deleteTotal(handlingInfoDTO);
//            backDTO.setBackMsg("覆盖已有数据，");
        }
        UserInfoDTO userInfo=(UserInfoDTO) RequestContextUtils.getWebSessionAttribute();
        UUID handlingTotID = UUID.randomUUID();
        HandlingInfoDTO insertTotalSTO = new HandlingInfoDTO();
        List<HandlingInfoDTO> handlingInfoDTOS = new ArrayList<>();
        //获取maindb库的所有卸船数据
        handlingInfoDTOS = importPlanDBMapper.getDischarge(handlingInfoDTO);
        try{
            //sqlserver最大参数是2100 避免绝对值下方取2000
            if(handlingInfoDTOS != null || handlingInfoDTOS.size() !=0){
                for(HandlingInfoDTO resultDTO : handlingInfoDTOS){
                    resultDTO.setCreatName(userInfo.getUsername());
                    resultDTO.setHandlingTotID(handlingTotID.toString());
                    insertTotalSTO = resultDTO;
                }
                insertTotalSTO.setHandlingTotID(handlingTotID.toString());
                importPortPlanMapper.inDoorInsertTotal(insertTotalSTO);
                //一个insert是25个参数需要插入 25*80 = 2000 所以80组数据为一个切割点
                if(handlingInfoDTOS.size() < 80){
                    //数据长度小于80不做切割直接插入数据
                    importPortPlanMapper.inDoorInsert(handlingInfoDTOS);
                }else{
                    //80组数据一次
                    int preInsertDataCount = 80;
                    // 可遍历的插入数据库的次数
                    int insertSqlCount = 0;
                    // 数据长度
                    int totalDataCount=handlingInfoDTOS.size();
                    //总参数量
                    int totalDetailNumber = totalDataCount;
                    if(totalDetailNumber%preInsertDataCount==0){
                        //eg 800/80 分割十组数据
                        insertSqlCount=totalDetailNumber/preInsertDataCount;
                    }else
                    {
                        //eg 801/80 分割11组
                        insertSqlCount=totalDetailNumber/preInsertDataCount+1;
                    }
                    //循环分割数据
                    for (int i = 0; i < insertSqlCount; i++) {
                        int startNumber = 0;
                        if(i == 0){
                            startNumber = 0;
                        }else{
                            startNumber = i*preInsertDataCount+1;
                        }

                        int endUnmber=(i+1)*preInsertDataCount;
                        if(endUnmber>totalDataCount){
                            endUnmber=totalDataCount;
                        }
                        List<HandlingInfoDTO> subListOK = handlingInfoDTOS.subList(startNumber,endUnmber);
                        importPortPlanMapper.inDoorInsert(subListOK);
                    }
                }
            }else{
                backDTO.setBackCode("301");
                backDTO.setBackMsg("没有查到数据");
                return backDTO;
            }

        }catch (Exception e){
            backDTO.setBackCode("300");
            backDTO.setBackMsg("发送数据失败，失败原因"+e.toString());
            return backDTO;
        }
//        if(backDTO.getBackMsg() == null){
            backDTO.setBackMsg("发送数据成功");
//        }else{
//            backDTO.setBackMsg(backDTO.getBackMsg()+"发送数据成功");
//        }
        backDTO.setBackCode("200");
        return backDTO;
    }

  
}

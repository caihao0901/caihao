package com.wsights.areabox.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 蔡浩
 * @version 1.0
 * 问题：主子表进行查询，一对多的关系，子表数据存储一个子表实体对象的list到主表实体，查询结果正常，但是分页出错
 * 效果：ph不能正常分页，比如主子表是1对2的关系，后端sql查找之后自动进行去重操作，导致第一页只有5条数据而不是十条
 * 解决办法：先查找主表数据，再拼接所有主表的主键作为查找子表的in（？）的条件进行查找所有的数据
 *          然后循环进行插入子表数据到主表的list对象
 * 备注：xml文件中的in条件写法
 *         <foreach item="item" index="index" collection="ctnNos" open="and dynaMic.cntr_no in(" separator="," close=")">
 *           #{item}
 *         </foreach>
 *      mapper.java文件中参数的写法 实体对象的话可以不用@Param
 *      List<BoxDynamicDTO> selectBoxDynMicDTO(@Param("ctnNos") List<String> ctnNos);
 */
@Service
public class CaseStateServiceImpl implements CaseStateService {
    @Resource
    CntrCurStatusMapper cntrCurStatusMapper;
    @Override
    public PageInfo<BoxStateDTO> caseStateSearch(BoxStateDTO boxStateDTO) {

        PageHelper.startPage(boxStateDTO.getPageNum(),boxStateDTO.getPageSize());
        //取所有主表数据
        List<BoxStateDTO> resultBoxDTOS = cntrCurStatusMapper.selectBoxStateDTO(boxStateDTO);
        List<String> ctnNos = new ArrayList<String>();
        for (BoxStateDTO resultBoxDTO:resultBoxDTOS) {
            //拼接查找子表数据的表示--mybatis中in条件此处不用拼接单引号
            ctnNos.add(""+resultBoxDTO.getCtnNo()+"");
        }
        //查找动态信息
        List<BoxDynamicDTO> resultDynMicDTOS = getBoxDynMic(ctnNos);
        for (BoxStateDTO resultStatusDTO:resultBoxDTOS) {
            List<BoxDynamicDTO> arrList = new ArrayList<BoxDynamicDTO>();
            for(BoxDynamicDTO resultDynMicDTO:resultDynMicDTOS){
                //存储当前的状态信息
                if(resultStatusDTO.getCtnNo().equals(resultDynMicDTO.getCtnNo())){
                    arrList.add(resultDynMicDTO);
                }
            }
            //List对象赋值
            resultStatusDTO.setBynList(arrList);
        }
//        PageInfo<BoxStateDTO> pageInfo= PageHelper.startPage(boxStateDTO.getPageNum(),boxStateDTO.getPageSize())
//                .doSelectPageInfo(()->cntrCurStatusMapper.selectBoxStateDTO(boxStateDTO));

        PageInfo<BoxStateDTO> pageInfo=new PageInfo<BoxStateDTO>(resultBoxDTOS);
        return pageInfo;
    }
}
















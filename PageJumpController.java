package com.wsights.areabox.controller;

import com.wsights.areabox.common.util.RequestContextUtils;
import com.wsights.areabox.dto.RoleFunction;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

import static com.wsights.areabox.common.util.RequestContextUtils.getWebSessionAttribute;

/*本地项目没问题，打包项目到Linux服务器上面，jar包正常运行，但是无法访问登录页
 * 报错信息：Error resolving template [/thymeleaf/loginPage]
 * 最开始怀疑是application.properties的配置问题
 * 配置如下：
 * server.port=8480
 * spring.mvc.view.prefix=classpath:/templates/thymeleaf
 * spring.mvc.view.suffix=.html
 * spring.mvc.static-path-pattern=/static/**
 * logging.level.com.XX.XX.mapper.XX=debug
 * 再三确认没问题之后，网上找到一篇博客，博主大致说本地没问题，但是服务器有问题，最后发现如果controller层多加“/”导致服务器上面不能找到对应的路径
 * 试着去掉了“/”，成功运行
*/
@Api(value = "页面跳转", tags = "页面跳转Controller")
@Controller
public class PageJumpController {
    //修改前
    //@ApiOperation("跳转到loginPage")
    //@GetMapping(value = {"/loginPage"})
    //public String loginPage(){ return "/thymeleaf/loginPage"; }
    
    @ApiOperation("跳转到loginPage")
    @GetMapping(value = {"/loginPage"})
    public String loginPage(){ return "thymeleaf/loginPage"; }


}


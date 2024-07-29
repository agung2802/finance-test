package com.test.api.controller;

import com.test.api.common.Urls;
import com.test.api.entity.tob.ApplyTobParam;
import com.test.api.service.TobService;
import com.test.api.utils.Response;
import com.test.api.utils.XmlPropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: Faisal Mulya Santosa
 * @create: 2023-05-06 17:47
 * @Description:
 */
@Slf4j
@RestController
public class TobController {
    @Autowired
    private TobService tobService;
    @Autowired
    XmlPropertiesUtil xmlPropertiesUtil;
    @PostMapping(Urls.Upload)
    public Response tobUpload(@RequestBody ApplyTobParam applyInfo){
        log.info("applyInfo:{}",applyInfo);
        return tobService.upload( applyInfo);
    }
}

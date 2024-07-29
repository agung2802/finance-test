package com.test.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.test.api.common.Urls;
import com.test.api.entity.Job;
import com.test.api.service.impl.TriggerJobService;
import com.test.api.utils.Response;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 16:09
* Class description
*/
@RestController
public class JobTriggerController {
	
	@Autowired
	TriggerJobService triggerJobService;

	@GetMapping(Urls.JOB_LIST)
	public Response getJobConfigList(String env) {
		return triggerJobService.getJobDetailiList(env);
	}
	
	@PostMapping(Urls.TRIGGER_JOB)
	public Response triggerJob(@RequestBody Job job) {
		return triggerJobService.triggerJob(job);
	}
}

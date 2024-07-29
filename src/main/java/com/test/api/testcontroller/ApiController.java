package com.test.api.testcontroller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29
* 类说明
*/
@RequestMapping("/api")
@RestController
public class ApiController {
	@RequestMapping("/testapi")
	public String apiTest() {
		return "testapi";
	}
}

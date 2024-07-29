/**
 * 
 */
package com.test.api.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.alibaba.fastjson.JSONObject;
import com.dangdang.ddframe.job.lite.lifecycle.api.JobAPIFactory;
import com.dangdang.ddframe.job.lite.lifecycle.api.JobOperateAPI;
import com.dangdang.ddframe.job.lite.lifecycle.api.JobSettingsAPI;
import com.dangdang.ddframe.job.lite.lifecycle.domain.JobSettings;
import com.google.common.base.Optional;
import com.test.api.entity.Job;
import com.test.api.entity.JobExt;
import com.test.api.utils.Response;
import com.test.api.utils.enums.ResponseCodeTypeEnum;

import lombok.extern.slf4j.Slf4j;

/**  
 * @ClassName: TriggerJob
 * @Description: TODO(描述)
 * @author Faisal Mulya Santosa
 * @date 2024-07-29 
*/
@Slf4j
@Service
public class TriggerJobService {
	
	public static final List<String> commonJOb = Arrays.asList("reportReqJob","calculatorOverdueJob","creditUserTagRuleDelayTriggerJob","paymentJob");
	//测试环境
	private ZooKeeper zooKeeper;
	//测试环境
	private ZooKeeper zooKeeperTest;
	//开发环境
	private ZooKeeper zooKeeperDev;

	public Response getJobDetailiList(String env){
		
		List<JobExt> jobExts = new ArrayList<>();
		List<Job> commonJoblist = new ArrayList<>();

		if(env.equalsIgnoreCase("1")) {
			zooKeeper = zooKeeperDev;
			log.info("开发环境");
		}else if(env.equalsIgnoreCase("0")) {
			zooKeeper = zooKeeperTest;
			log.info("测试环境");
		}
		try {
			
			List<String> children = zooKeeper.getChildren("/", true);
			
//			System.out.println(children);
			log.info("children{}", children);
			children.stream().filter(x->x.contains("job")).forEach(t->System.out.println(t));
			List<String> collect = children.stream().filter(x->x.contains("job")).collect(Collectors.toList());
			System.out.println(collect);
			HashMap<String, Object> namespaceAndJobMap = new HashMap<>();
			HashMap<String, String> jobConfigMap = new HashMap<>();
			collect.forEach(x->{
				try {
					JobExt ext = new JobExt();
					List<Job> joblist= new ArrayList<>();
					List<String> children2 = zooKeeper.getChildren("/"+x, true);
					System.out.println(children2);
					log.info("children2{}", children2);

					namespaceAndJobMap.put(x, children2);
					if(children2.size()>0) {
						log.info("=========");
						children2.forEach(t->{	
							try {
								List<String> children3 = zooKeeper.getChildren("/"+x+"/"+t, true);
								if(children3.contains("config")) {
									byte[] data = zooKeeper.getData("/"+x+"/"+t+"/"+"config", true, new Stat());
									jobConfigMap.put(t, new String(data));
									Job job = JSONObject.parseObject(new String(data), Job.class);
									job.setNameSpace(x);
									joblist.add(job);
									System.out.println(new String(data));
									if(commonJOb.contains(job.getJobName())) {
										commonJoblist.add(job);
									}
								}
							} catch (KeeperException | InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						});
						ext.setNameSpace(x);
						ext.setJoblist(joblist);
						jobExts.add(ext);
					}

				} catch (KeeperException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			});
			
		
			jobExts.add(0, new JobExt("常用定时任务", commonJoblist));
			log.info("=====jobExts=======");
			log.info("job配置{}",JSONObject.toJSONString(jobExts));
		} catch (KeeperException e) {
			new Response("201","连接zk不成功");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			new Response("201","连接zk不成功");
			e.printStackTrace();
		}
		return new Response("0","获取job成功",jobExts);
//		return new Response(jobExts);
	}


	/**
	 * @return
	 * @throws IOException
	 */
	@PostConstruct
	public void connectZk() throws IOException {
		 try {
			 zooKeeperTest = new ZooKeeper("192.168.114.6:2181", 50000, new Watcher() {
				
				@Override
				public void process(WatchedEvent arg0) {
					// TODO Auto-generated method stub
					log.info("测试环境登入成功");
				}
			} );
			 zooKeeperTest.addAuthInfo("digest", "welabsea:sea@123".getBytes());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	@PostConstruct
	public void connectZkDev() throws IOException {
		 try {
			 zooKeeperDev = new ZooKeeper("192.168.114.26:2181", 50000, new Watcher() {
				
				@Override
				public void process(WatchedEvent arg0) {
					// TODO Auto-generated method stub
					log.info("开发环境登入成功");
				}
			} );
			 zooKeeperDev.addAuthInfo("digest", "welabsea:sea@123".getBytes());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public Response triggerJob(Job job) {
		log.info("触发job入参{}",job);
		JobSettingsAPI jobSettingsAPI = createdJobSetting(job);
		JobSettings jobSettings = jobSettingsAPI.getJobSettings(job.getJobName());
		log.info("====jobClass{},JobParameter{}",jobSettings.getJobClass(),jobSettings.getJobParameter());
		jobSettings.setJobParameter(job.getJobParameter());
		log.info("====jobClass{},JobParameter{}",jobSettings.getJobClass(),jobSettings.getJobParameter());
		jobSettingsAPI.updateJobSettings(jobSettings);
		
		JobOperateAPI createJobOperateAPI = JobAPIFactory.createJobOperateAPI("192.168.114.6:2181", 
				job.getNameSpace(), Optional.of("welabsea:sea@123"));
		if(job.getEnv().equalsIgnoreCase("1")) {
			createJobOperateAPI = JobAPIFactory.createJobOperateAPI("192.168.114.26:2181", 
					job.getNameSpace(), Optional.of("welabsea:sea@123"));
		}
		log.info("===createJobOperateAPI{}",createJobOperateAPI);
		
		createJobOperateAPI.trigger(Optional.of(job.getJobName()),Optional.<String>absent());
		return new Response("0","job触发成功");
	}


	/**
	 * @param job
	 * @return
	 */
	public JobSettingsAPI createdJobSetting(Job job) {
		JobSettingsAPI jobSettingsAPI = JobAPIFactory.createJobSettingsAPI("192.168.114.6:2181", 
				job.getNameSpace(), Optional.of("welabsea:sea@123"));
		if(job.getEnv().equalsIgnoreCase("1")) {
			log.info("开发环境dev");
			 jobSettingsAPI = JobAPIFactory.createJobSettingsAPI("192.168.114.26:2181", 
					job.getNameSpace(), Optional.of("welabsea:sea@123"));
		}
		return jobSettingsAPI;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TriggerJobService job = new TriggerJobService();
//		job.getJobDetailiList();
		// {'triggerTime':'2022-02-10'}
		Job job2 = new Job("creditUserTagRuleDelayTriggerJob",
				"", "welab-loan-finance-job");
		System.out.println(job);
		job.triggerJob(job2);
		System.out.println(job2);

	}

}

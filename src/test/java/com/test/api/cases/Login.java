package com.test.api.cases;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

import com.test.api.util.DataUtil;

/**
* @author Faisal Mulya Santosa
* @version Creation time: 2024-07-29 16:09
* 类说明
*/
public class Login extends BaseCase {
	@DataProvider
	public Object[][] datas(ITestContext context){
		System.out.println("我是register@DataProvider");

		Object[][] obj = DataUtil.getProviderData(context,"2");
		
		return obj;
		
	}
	public static void main(String[] args) {
		try {
			ZooKeeper zooKeeper = new ZooKeeper("192.168.114.6:2181", 50000, new Watcher() {
				
				@Override
				public void process(WatchedEvent arg0) {
					// TODO Auto-generated method stub
					System.out.println("登入成功");
				}
			} );
			zooKeeper.addAuthInfo("digest", "welabsea:sea@123".getBytes());
			
			List<String> children = zooKeeper.getChildren("/", true);
			
			System.out.println(children);
			children.stream().filter(x->x.contains("job")).forEach(t->System.out.println(t));
			List<String> collect = children.stream().filter(x->x.contains("job")).collect(Collectors.toList());
			System.out.println(collect);
			HashMap<String, Object> namespaceAndJobMap = new HashMap<>();
			HashMap<String, String> jobConfigMap = new HashMap<>();
			collect.forEach(x->{
				try {
					List<String> children2 = zooKeeper.getChildren("/"+x, true);
					System.out.println(children2);
					namespaceAndJobMap.put(x, children2);
					if(children2.size()>0) {
						System.out.println("=========");
						children2.forEach(t->{	
							try {
								List<String> children3 = zooKeeper.getChildren("/"+x+"/"+t, true);
								if(children3.contains("config")) {
									byte[] data = zooKeeper.getData("/"+x+"/"+t+"/"+"config", true, new Stat());
									jobConfigMap.put(t, new String(data));
									System.out.println(new String(data));
								}
							} catch (KeeperException | InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						});
					}

				} catch (KeeperException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			});
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

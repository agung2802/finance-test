package com.test.api.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.test.api.util.enty.DayaModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.test.api.pojo.Case;
import com.test.api.pojo.WriteCell;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class ExcelUtil {
	public static Map<Integer,String> cellNumAndTitleMap = new HashMap<>();
	public static Map<String,Integer> titleAndCellNumMap = new HashMap<>();
	public static List<WriteCell> writeCellsList = new ArrayList<>();
	public static <T> List<T> readExcel(Class<T> clazz,String path,int index){
		InputStream in;
		Workbook wbook;
		List list = new ArrayList();
		if(StringUtils.isBlank(path)) {
			throw new IllegalArgumentException("文件路径不能为空");
		}else {
			File file = new File(path);
			try {
				in = new FileInputStream(file);
				wbook = WorkbookFactory.create(in);
				Sheet sheet = wbook.getSheetAt(index);
				int lastRowNum = sheet.getLastRowNum();
				Row row = sheet.getRow(0);
				int lastCellNum = row.getLastCellNum();
				System.out.println("行row"+lastRowNum+"   列cell"+lastCellNum);
				for (int i = 0; i <=lastRowNum; i++) {
					Row row2 = sheet.getRow(i);
					Object obj = clazz.newInstance();
					for (int j = 0; j <=lastCellNum-1; j++) {
						if(i==0) {
							Cell cell = row2.getCell(j);
							String title = cell.getStringCellValue();
							String[] split = title.split("\\(");
//							System.err.println("split[0]  "+split[0]);
							cellNumAndTitleMap.put(j, split[0]);
							titleAndCellNumMap.put(split[0], j);
						}else {
							Cell cell = row2.getCell(j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
							cell.setCellType(CellType.STRING);;
							cell.getStringCellValue();
//							System.out.print(cell.getStringCellValue()+"   ");
							String methodName = "set"+cellNumAndTitleMap.get(j);
							Method declaredMethod = clazz.getDeclaredMethod(methodName, String.class);
							declaredMethod.invoke(obj,cell.getStringCellValue());
						}
					}
//					System.out.println();
					if(0!=i) {
						
						list.add(obj);
					}
				}
				log.info("list{}",list);;

				list.forEach(x->{
					System.out.println("x          "+x);
				});
				return  list;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("list"+list);
		log.info("list{}",list);
		log.info("list{}",list);;
		return list;
		
		
	}

	public static void writeToExcel(List<WriteCell> writeCellsList,String excelPath) {
		InputStream in = null;
		Workbook workBook;
		FileOutputStream out = null;
		try {
			in = new FileInputStream(excelPath);
		    workBook = WorkbookFactory.create(in);
		    Sheet sheetAt = workBook.getSheetAt(1);
		    writeCellsList.forEach(x->{
		    	CellStyle createCellStyle = workBook.createCellStyle();
		    	Row row = sheetAt.getRow(Integer.parseInt(x.getNo()));
		    	Integer cellNum = titleAndCellNumMap.get(x.getCellName());
//		    	Cell cell = row.getCell(cellNum);
		    	System.out.println("row"+row+"     cellNum ===     "+cellNum);
		    	Cell cell = row.getCell(cellNum,MissingCellPolicy.CREATE_NULL_AS_BLANK);
		    	cell.setCellType(CellType.STRING);
		    	cell.setCellValue(x.getContext());
		    });
		   out =  new FileOutputStream(excelPath);
			workBook.write(out);
		}  catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
				try {
					if(null!=in) {
					in.close();
					if(null!=out) {
						out.close();
					}
					
				}} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
	}
	public static void addWritecell(String no,String cellName,String context) {
		
		writeCellsList.add(new WriteCell(no,cellName,context));
	}
	
	public  void sendWeChat(int totalCaseNum,int successCaseNum ,int failCaseNum,int abnormalNum) {
		HttpClient client = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost("https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=9228e87a-e3f8-44b0-a2ef-990cb1bfd0f2");
		httpPost.addHeader(new BasicHeader("Content-Type", "application/json; charset=utf-8"));
//		int totalCaseNum = 24;
//		int successCaseNum =4 ;
//		int failCaseNum = 20;
		String params = "{\r\n" + 
				"    \"msgtype\": \"markdown\",\r\n" + 
				"    \"markdown\": {\r\n" + 
				"        \"content\": \"测试用例共<font color=\\\"warning\\\">"+totalCaseNum+"条</font>。\\n\r\n" + 
				"         >类型:<font color=\\\"comment\\\">用例执行进度</font>\r\n" + 
				"         >成功:<font color=\\\"comment\\\">"+successCaseNum+"条</font>\r\n" + 
				"         >失败:<font color=\\\"comment\\\">"+failCaseNum+"条</font>\r\n" + 
				"         >环境异常:<font color=\\\"comment\\\">"+abnormalNum+"条</font>\"\r\n" + 
				"    }\r\n" + 
				"}";
		httpPost.setEntity(new StringEntity(params, "utf-8"));
		try {
			client.execute(httpPost);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			log.info("e.printStackTrace()",e);
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.info("e.printStackTrace()",e);
			e.printStackTrace();
		}
	}
	public static <T> List<T>  easyReadExcel(String filePath,Class<T>  clazz){
		List  list = new ArrayList();
		EasyExcel.read(new File(filePath),clazz,new AnalysisEventListener<Object>(){

			@Override
			public void invoke(Object obj, AnalysisContext analysisContext) {
				log.info("{}",obj);
				list.add(obj);

			}

			@Override
			public void doAfterAllAnalysed(AnalysisContext analysisContext) {

			}
			@Override
			public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context){
//				log.info("表头{}",headMap);
				for(Entry<Integer,String> entry: headMap.entrySet()){
//					log.info("key:{},value:{}",entry.getKey(),entry.getValue());
				}
			}
		}).sheet(0).doRead();
		return list;
	}

	public  static <T> void easyWriteExcel(String filePath, Class<T> clazz, List  list){
		log.info("开始写入到上传文件");
		EasyExcel.write(new File(filePath),clazz).sheet(0).doWrite(list);
		log.info("已经写入到上传文件");


	}
	public static void main(String[] args) {
//		ExcelUtil.readExcel( Api.class, "src\\test\\resources\\case.xlsx",0);
		
		List<Case> readExcel = ExcelUtil.readExcel( Case.class, "src\\main\\resources\\case.xlsx",1);
		System.out.println("readExcel{}"+readExcel.size());
		for(Case cas:readExcel) {
			//前置处理
			log.info("Case{}",cas);
			String beforeExcute = cas.getBeforeExcute();
			log.info("beforeExcute{}"+beforeExcute.length());

				 JSONObject paramMap = JSON.parseObject(beforeExcute);
				if(paramMap.get("parameter")!=null) {
					System.out.println("paramMap.get(\"parameter\")"+paramMap.get("parameter"));
					HashMap<String,Object>  paramtersMap =  JSON.parseObject(paramMap.get("parameter").toString(), HashMap.class);
					String excuteMethod = (String) paramMap.get("execution");
					log.info("====parameter{},disburse{}",paramMap.get("parameter"),paramMap.get("execution"));
					for(Entry<String, Object> entry: paramtersMap.entrySet()) {
						log.info("=====entry{},{}",entry.getKey(),entry.getValue());
					}
				}
			
		}

	}
}

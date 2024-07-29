package com.test.api.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.test.api.entity.tob.DayaModel;
import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: Faisal Mulya Santosa
 * @create: 2023-04-28 16:07
 * @Description:
 */
@Slf4j
public class EasyExcelUtil {
    public static <T> List<T> easyReadExcel(String filePath, Class<T>  clazz){
        List  list = new ArrayList();

//        EasyExcel.read(new File(filePath),clazz,new AnalysisEventListener<Object>(){
        try {
            EasyExcel.read(new ClassPathResource(filePath).getInputStream(),clazz,new AnalysisEventListener<Object>(){

                @Override
                public void invoke(Object obj, AnalysisContext analysisContext) {
                    System.out.println("===");
                    System.out.println(obj);
                    list.add(obj);
                    System.out.println("===");

                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext analysisContext) {

                }
                @Override
                public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context){
    //				log.info("表头{}",headMap);
                    for(Map.Entry<Integer,String> entry: headMap.entrySet()){
    //					log.info("key:{},value:{}",entry.getKey(),entry.getValue());
                    }
                }
            }).sheet(0).doRead();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public  static<T> void easyWriteExcel(String filePath, Class<T> clazz, List  list){
        log.info("filePath:{}",filePath);
        File file = new File(filePath);
        if(!file.isDirectory()){
            file.getParentFile().mkdirs();
        }
        EasyExcel.write(new File(filePath),clazz).sheet(0).doWrite(list);
    }
    public  static<T> void easyWriteExcel(String filePath){
        log.info("清空filePath:{}",filePath);
        EasyExcel.write(new File(filePath)).sheet(0).doWrite(new ArrayList<>());
    }
    public static List<String> readTxt(String filePath){
        List<String> list =null;
        try {
//             list = Files.readAllLines(Paths.get(filePath));
            ClassPathResource classPathResource = new ClassPathResource(filePath);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(classPathResource.getInputStream()));
             list = bufferedReader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return list;

    }
    public static void writeTotxt(String filePath,List list){
        log.info("filePath:{}",filePath);
        File file = new File(filePath);
        if(!file.isDirectory()){
            file.getParentFile().mkdirs();
        }
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (Object  str: list) {
            try {
                fileWriter.write(str.toString()+"\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

package com.test.api;

import com.alibaba.fastjson.JSONPath;
import com.test.api.entity.tob.ApplyTobParam;
import com.test.api.pojo.Api;
import com.test.api.service.TobService;
import com.test.api.util.HttpUtil;
import com.test.api.utils.EasyExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.DateUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.regex.Pattern;

/**
 * @author: Faisal Mulya Santosa
 * @create: 2023-05-04 18:07
 * @Description:
 */
@Slf4j
@SpringBootTest
public class Test2 {
    @Autowired
    TobService tobService;
    @org.junit.jupiter.api.Test
    public void testuploadDaya() throws NoSuchAlgorithmException {
        ApplyTobParam applyTobParam = new ApplyTobParam("87728827624","88880021","DAYA",(byte)107,1000000l,8,"0");
        tobService.upload(applyTobParam);

    }

    @org.junit.jupiter.api.Test
    public void testuploadSmp() throws NoSuchAlgorithmException {
        ApplyTobParam applyTobParam = new ApplyTobParam("876543212","81349558090","SMP",(byte)109,1000000l,7,"1");
        tobService.upload(applyTobParam);

    }
    @org.junit.jupiter.api.Test
    public void testuploadTrio()  {
        ApplyTobParam applyTobParam = new ApplyTobParam("82088289999","88051800026","TRIO",(byte)104,1000000l,5,"0");
        tobService.upload(applyTobParam);
    }

    @org.junit.jupiter.api.Test
    public void testuploadHSO()  {
        ApplyTobParam applyTobParam = new ApplyTobParam("83088889012","83088888044","HSO",(byte)101,1000000l,5,"0");
        tobService.upload(applyTobParam);

    }
//    @Test
    public void testClearExcel(){
        EasyExcelUtil.easyWriteExcel("src/test/resources/file/daya1.xlsx");
    }
    public String excute(StringBuilder str){

        str.append("abc");
        log.info("{}",str);
        return str.toString();
    }
    public int excute1(int a, int  b){
        int c = 0;
        try {
            c = a/b;
        } catch (Exception e) {
            log.error("{}",e);
//            e.printStackTrace();
        }
        return c;
    }
    public static void main(String[] args) throws IOException {
//        Test2 test2 = new Test2();
//        StringBuilder stringBuilder = new StringBuilder();
//        test2.excute(stringBuilder);
//        log.info("{}",stringBuilder.toString());
//        test2.excute1(1,0);
//        log.info("{}",stringBuilder.toString());
//        Date date = new Date("2023-06-10 23:59:59");
        Date date = DateUtil.parse("2023-06-14 23:59:59");
        Instant instant = date.toInstant();
        LocalDate ld = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        long diff =  ChronoUnit.DAYS.between(LocalDate.now(),ld);
        log.info("|||||  {},失效时间{},当前时间{}",diff,ld ,LocalDate.now());
        LinkedList<Integer> integers = new LinkedList<>();
        integers.add(1);
        integers.add(2);
        integers.add(3);
        integers.add(4);
        integers.add(5);
        integers.add(7);

        List<Integer> integers1 = Arrays.asList(1,2,3,4,5,6,7);
        ListIterator<Integer> iterator = integers.listIterator();
        while(iterator.hasNext()){
            Integer next = iterator.next();
            iterator.nextIndex();
            log.info("iterator.nextIndex():{}",iterator.nextIndex());
            if(next.compareTo(7)==0){
                log.info("========");
                integers.subList(0,iterator.nextIndex()).clear();
                integers.forEach(x->log.info("{x:}",x.longValue()));
            }
        }
//        log.info("integers:{},integers1:{}",integers,integers1);

        ListIterator<Integer> listIterator = integers1.listIterator();
//      while (listIterator.hasNext()){
//          int i = listIterator.nextIndex();
//          Integer next = listIterator.next();
//          if(next.compareTo(7)==0){
//              integers1.subList(0,i-1).clear();
//          }
//      }
                log.info("integers:{},integers1:{}",integers1,integers1);
        log.info("integer长度:{},integers1:{}",integers.size());
        ListIterator<Integer> iterator2 = integers.listIterator();
        while(iterator2.hasNext()){
            Integer next = iterator2.next();
            iterator2.nextIndex();
            log.info("next:{}",next);
            log.info("iterator.nextIndex():{}",iterator2.nextIndex());
            if(next.compareTo(7)==0){
                log.info("========");
                integers.subList(0,iterator2.nextIndex()-1).clear();
                integers.forEach(x->log.info("{x:}",x.longValue()));
            }
        }
        log.info("-----------20230717------------");
      //  int[] nums = {0,0,1,1,1,2,2,3,3,4};
        int[] nums1 = {4,9,5};
        int[] nums2 = {9,4,9,8,4,4,8,1};
        HashSet<Object> set = new HashSet<>();
        ArrayList<Integer> array = new ArrayList<>();
        HashMap<Integer, Integer> map = new HashMap<>();
        Map<Integer, Integer> map2 = new TreeMap<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });

        for (int i = 0; i < nums2.length; i++) {
            map.put(nums2[i],map.getOrDefault(nums2[i],0)+1);
            map2.put(nums2[i],map2.getOrDefault(nums2[i],0)+1);
        }
        List<HashMap<Integer,Integer>>  list = new ArrayList<>();
        List<Map.Entry<Integer, Integer>> list1 =  new ArrayList<>();
        map.entrySet().forEach(x->{
            log.info("map:key:{},value:{}",x.getKey(),x.getValue());
            HashMap<Integer, Integer> obj = new HashMap<>();
            obj.put(x.getKey(),x.getValue());
            list.add(obj);
        });
        log.info("list:{}",list);

        map2.entrySet().forEach(x->log.info("map2:key:{},value:{}",x.getKey(),x.getValue()));
        Collections.sort(list, new Comparator<HashMap<Integer, Integer>>() {
            @Override
            public int compare(HashMap<Integer, Integer> o1, HashMap<Integer, Integer> o2) {
                int value1 =0;
                int value1key =0;

                Set<Map.Entry<Integer, Integer>> entries = o1.entrySet();
                for (Map.Entry<Integer, Integer> obj: entries) {
                    value1 = obj.getValue();
                    value1key = obj.getKey();
                }
                int value2 =0;
                int value2key =0;
                Set<Map.Entry<Integer, Integer>> entries2 = o2.entrySet();
                for (Map.Entry<Integer, Integer> obj: entries2) {
                    value2 = obj.getValue();
                    value2key = obj.getKey();
                }
                if(value1==value2){
                   return value2key - value1key>0?1:-1;
                }
                return value2-value1>0?1:-1;
            }
        });



        log.info("----------------");
        map.entrySet().forEach(x->log.info("map:key:{},value:{}",x.getKey(),x.getValue()));
        log.info("list:{}",list);
        System.out.println("array:"+array+"\n\r"+"abc");
        log.info("----------------");

        //Pattern pattern = Pattern.compile("^[0-9]+");
        Pattern pattern = Pattern.compile("^\\d*$");
        System.out.println( pattern.matcher("23080110025647993531354").find());
        UUID uuid = UUID.randomUUID();
        System.out.println(uuid.toString());
        System.out.println(uuid.toString().replace("-",""));
        System.out.println(String.valueOf(0.11));

        System.out.println(Long.parseLong("1000"));
        String  str= "{\n\t\"code\":0,\n\t\"message\":\"Permintaan berhasil\",\n\t\"result\":\"A23081516283492807999849\"\n}";
        String code =  JSONPath.read(str, "$.code").toString();
        while(code.equalsIgnoreCase("10930042")){
            try {
                System.out.println("--------");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
           // confirmResp = HttpUtil.execute(apiConfigTest, "post","/awsom-komodo/api/v3/credit-limit/confirm","{}","applyHeader");
            //code = (String) JSONPath.read(confirmResp, "$.code");
        }

        LinkedList<Object> objects = new LinkedList<>();


    }
}

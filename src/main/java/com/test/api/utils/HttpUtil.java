package com.test.api.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.test.api.entity.test.*;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;


import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.Multicast;

import static org.apache.http.entity.ContentType.MULTIPART_FORM_DATA;

/**
 * @author Faisal Mulya Santosa
 * @version Creation time: 2024-07-29 16:09
 * 类说明
 */
@Slf4j
public class HttpUtil {
    public static CloseableHttpClient client = HttpClients.createDefault();
    public  static   RequestConfig config = RequestConfig.custom().setConnectTimeout(60000).setSocketTimeout(60000).build();
    //	private static Object read;
    public static String execute(ApiConfig apiConfig, Case cas, Api api, String headerType) {
        if("post".equalsIgnoreCase(api.getApiMethod())) {
            return doPost(apiConfig,cas,api,headerType);
        }else if("get".equalsIgnoreCase(api.getApiMethod())){
            return doget(apiConfig, cas, api,headerType);
        }
        return "";
    }
    public static String execute(ApiConfig apiConfig,String requestMethod,String requestUrl,String requestParam,String headerType) {
        if("post".equalsIgnoreCase(requestMethod)) {
            return doPost(apiConfig,requestMethod,requestUrl,requestParam,headerType);
        }else if("get".equalsIgnoreCase(requestMethod)){
            return doget(apiConfig,requestMethod,requestUrl,requestParam,headerType);
        }
        return "请检查请求方式";
    }
    public static String doPost(ApiConfig apiConfig,String requestMethod,String requestUrl,String requestParam,String headerType) {
        HttpPost httpPost = new HttpPost(apiConfig.getRootUrl()+requestUrl);
        //设置超时时间
        httpPost.setConfig(config);
        List<Header> headerList = apiConfig.getHeaderList();
        buildHeader(httpPost, headerList,"json","",headerType);
        log.info("httpPost.getAllHeaders()"+httpPost.getAllHeaders().toString());
        for(org.apache.http.Header header: httpPost.getAllHeaders()) {
            log.info("header.getName() "+header.getName()+"header.getValue()"+header.getValue());
        }
        System.out.println("======Entity1========");
        httpPost.setEntity(new StringEntity(requestParam, "utf-8"));
        System.out.println("======Entity2========");
//		CloseableHttpClient client = HttpClients.createDefault();
        String responseResult = "";
        log.info("请求信息============"+requestParam);
        log.info("请求信息路径requestUrl:"+requestUrl);
        try {
            System.out.println("======Entity3========");
            CloseableHttpResponse response = client.execute(httpPost);
            System.out.println("======Entity4========");
            HttpEntity entity = response.getEntity();
            System.out.println("======Entity5========");
            responseResult = EntityUtils.toString(entity);
            System.out.println("======Entity6========");

            log.info("响应信息responseResult============"+JSON.toJSONString(responseResult));

        } catch (IOException e) {
            System.out.println("发送请求异常");
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return responseResult;

    }
    public static String doPostUpload(ApiConfig apiConfig, String requestMethod, String requestUrl, String requestParam, String headerType
            , List<String> filePath, HashMap<String, String> headMap)  {
        HttpPost httpPost = new HttpPost(apiConfig.getRootUrl()+requestUrl);
        //设置超时时间
        httpPost.setConfig(config);
        List<Header> headerList = apiConfig.getHeaderList();
        buildHeader(httpPost, headerList,"upload","",headerType);
        headMap.entrySet().forEach(x->httpPost.addHeader(x.getKey(),x.getValue()));
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.setCharset(Consts.UTF_8);
        // 在build中添加了contentType   不需要在请求头重添加否则会报错
        builder.setContentType(MULTIPART_FORM_DATA);
        //  通过addBianry   添加上传的文件 以下两种方式都可以

//		builder.addBinaryBody("file",new File(filePath));
//		builder.addBinaryBody("file",new File(filePath)
//				, ContentType.parse("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),"上传发票.xlsx");

        // 通过addPart  添加上传文件
        for (String str:filePath) {
            builder.addPart("file",new FileBody(new File(str)));

        }
//        builder.addPart("file",new FileBody(new File(filePath)));
        log.info("httpPost.getAllHeaders()"+httpPost.getAllHeaders().toString());
//        httpPost.addHeader("x-user-token","buppzvveqvqqd0836532472719081472");
        for(org.apache.http.Header header: httpPost.getAllHeaders()) {
            log.info("header.getName() "+header.getName()+"header.getValue()"+header.getValue());
        }
        System.out.println("======Entity1========");
        httpPost.setEntity(new StringEntity(requestParam, "utf-8"));
        httpPost.setEntity(builder.build());
        System.out.println("======Entity2========");
        String responseResult = "";
        log.info("请求信息============"+requestParam);
        log.info("请求信息路径requestUrl:"+requestUrl);
        try {
            System.out.println("======Entity3========");
            CloseableHttpResponse response = client.execute(httpPost);
            System.out.println("======Entity4========");
            HttpEntity entity = response.getEntity();
            System.out.println("======Entity5========");
            responseResult = EntityUtils.toString(entity);
            System.out.println("======Entity6========");

            log.info("响应信息responseResult============"+JSON.toJSONString(responseResult));

        } catch (IOException e) {
            System.out.println("发送请求异常");
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return responseResult;

    }
    public static String doPost(ApiConfig apiConfig,Case cas,Api api,String headerType) {
        HttpPost httpPost = new HttpPost(apiConfig.getRootUrl()+api.getUrl());
        //设置超时时间
        httpPost.setConfig(config);
        List<Header> headerList = apiConfig.getHeaderList();
        buildHeader(httpPost, headerList,"json",api.getHeader(),headerType);
        log.info("httpPost.getAllHeaders()"+httpPost.getAllHeaders().toString());
        for(org.apache.http.Header header: httpPost.getAllHeaders()) {
            log.info("header.getName() "+header.getName()+"header.getValue()"+header.getValue());
        }
        System.out.println("======Entity1========");
        httpPost.setEntity(new StringEntity(cas.getParams(), "utf-8"));
        System.out.println("======Entity2========");
//		CloseableHttpClient client = HttpClients.createDefault();
        String responseResult = "";
        try {
            CloseableHttpResponse response = client.execute(httpPost);
            HttpEntity entity = response.getEntity();
            responseResult = EntityUtils.toString(entity);
            log.info("cas.getParams()============"+cas.getParams());
            log.info("responseResult============"+responseResult);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return responseResult;

    }
    public static  String  doPost(String url,String params,Map<String,String>  header) {
        HttpPost httpPost = new HttpPost(url);
        //设置超时时间
        httpPost.setConfig(config);
        if(header!=null) {
            for(Entry<String,String> entry:header.entrySet()) {
                httpPost.addHeader(new BasicHeader(entry.getKey(), entry.getValue()));
            }
        }
        try {
            log.info("url:{}",url);
            CloseableHttpResponse response = client.execute(httpPost);
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity, "UTF-8");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }
    public static String doget(ApiConfig apiConfig,Case cas,Api api,String headerType) {
        HttpGet httpGet = new HttpGet(apiConfig.getRootUrl()+api.getUrl()+"?"+cas.getParams());
        //设置超时时间
//		 httpGet.setConfig(config);
        List<Header> headerList = apiConfig.getHeaderList();

        buildHeader(httpGet,headerList,"",api.getHeader(),headerType);
//		CloseableHttpClient client = HttpClients.createDefault();
        String[] assertion = new String[1];
        try {
            CloseableHttpResponse reponse = client.execute(httpGet);
            HttpEntity entity = reponse.getEntity();
            String result = EntityUtils.toString(entity, "UTF-8");
            log.info("result::::"+result);
            apiConfig.getMap().keySet().forEach(x->{
                log.info("key"+x+"    value"+apiConfig.getMap().get(x));
            });
            return result;

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;

    }

    public static String doget(ApiConfig apiConfig,String requestMethod,String requestUrl,String requestParam,String headerType) {
        HttpGet httpGet = null;
        if(StringUtils.isNotBlank(requestParam)&&requestParam.length()>0){
            httpGet = new HttpGet(apiConfig.getRootUrl()+requestUrl+"?"+requestParam);
        }else{
            httpGet = new HttpGet(apiConfig.getRootUrl()+requestUrl);
        }
        //设置超时时间
        httpGet.setConfig(config);
        log.info("请求::::"+apiConfig.getRootUrl()+requestUrl+"?"+requestParam);
        List<Header> headerList = apiConfig.getHeaderList();

        buildHeader(httpGet,headerList,"","",headerType);
//		CloseableHttpClient client = HttpClients.createDefault();
        String[] assertion = new String[1];
        try {
            CloseableHttpResponse reponse = client.execute(httpGet);
            HttpEntity entity = reponse.getEntity();
            String result = EntityUtils.toString(entity, "UTF-8");
            log.info("result::::"+JSON.toJSONString(result));
            log.info("请求信息路径requestUrl:"+requestUrl);
            apiConfig.getMap().keySet().forEach(x->{
                log.info("key"+x+"    value"+apiConfig.getMap().get(x));
            });
            return result;

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;

    }


    private static void buildHeader(HttpRequest httpPost, List<Header> headerList,String type,String apiHeader,String headerType) {
        headerList.forEach(x->{
            if(x.getName().equals("Content-Type")) {
                List<Element> elementList = x.getElementList();
                elementList.forEach(el->{
                    if("json".equals(type)) {
                        if("json".equals(el.getName())) {
                            httpPost.addHeader(new BasicHeader("Content-Type",el.getValue()));
                            log.info("头信息"+"Content-Type: "+el.getValue());
                        }
                    }else if("form".equals(type)) {
                        if("form".equals(el.getName())) {
                            httpPost.addHeader(new BasicHeader("Content-Type: ",el.getValue()));
                            log.info("头信息"+"Content-Type: "+el.getValue());
                        }
                    }else if("upload".equals(type)) {
                        if("upload".equals(el.getName())) {
                            httpPost.addHeader(new BasicHeader("Content-Type",el.getValue()));
                            log.info("头信息"+"Content-Type"+el.getValue());
                        }
                    }
                });

            }else if(x.getName().equalsIgnoreCase("applyHeader")&&headerType.equalsIgnoreCase("applyHeader")) {
                List<Element> elementList = x.getElementList();
                elementList.forEach(ele->{
                    httpPost.addHeader(new BasicHeader(ele.getName(), ele.getValue()));
                    log.info("头信息"+ele.getName()+"  "+ele.getValue());

                });
            }else if(x.getName().equalsIgnoreCase("branchesweb")&&headerType.equalsIgnoreCase("branchesweb")) {
                List<Element> elementList = x.getElementList();
                elementList.forEach(ele->{
                    httpPost.addHeader(new BasicHeader(ele.getName(), ele.getValue()));
                    log.info("头信息"+ele.getName()+"  "+ele.getValue());

                });
            }else if(x.getName().equalsIgnoreCase("collectionweb")&&headerType.equalsIgnoreCase("collectionweb")) {
                List<Element> elementList = x.getElementList();
                elementList.forEach(ele->{
                    httpPost.addHeader(new BasicHeader(ele.getName(), ele.getValue()));
                    log.info("头信息"+ele.getName()+"  "+ele.getValue());

                });
            }else if(x.getName().equalsIgnoreCase(headerType)) {
                List<Element> elementList = x.getElementList();
                elementList.forEach(ele->{
                    httpPost.addHeader(new BasicHeader(ele.getName(), ele.getValue()));
                    log.info("头信息"+ele.getName()+"  "+ele.getValue());

                });
            }
        });
        if (apiHeader.trim().length()>0) {
            if (apiHeader.contains(";")) {
                String[] split = apiHeader.split(";");
                for(String str : split) {
                    String[] split2 = str.split("=");
                    httpPost.addHeader(new BasicHeader(split2[0],split2[1]));
                }
            }else {
                String[] split2 = apiHeader.split("=");
                httpPost.addHeader(new BasicHeader(split2[0],split2[1]));
                log.info("头信息"+split2[0]+"         "+split2[1]);
            }
        }

    }


}

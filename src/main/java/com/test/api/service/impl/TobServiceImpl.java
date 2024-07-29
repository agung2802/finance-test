package com.test.api.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

import com.test.api.entity.TbUser;
import com.test.api.entity.test.ApiConfig;
import com.test.api.entity.test.Element;
import com.test.api.entity.test.Header;
import com.test.api.entity.tob.*;
import com.test.api.entity.UserInfo;
import com.test.api.mapper.TbUserMapper;
import com.test.api.mapper.UserInfoMapper;
import com.test.api.service.ChangeEnv;
import com.test.api.service.TobService;
import com.test.api.utils.*;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author: Faisal Mulya Santosa
 * @create: 2023-04-28 16:26
 * @Description:
 */
@Slf4j
@Service
public class TobServiceImpl implements TobService {
    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    TbUserMapper tbUserMapper;
    @Autowired
    ChangeEnv changeEnv;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Override
    /**
     * trio txt上传文件默认partneruserno为4位，如果<4位会有问题，暂时没有处理加入空格
     * trio HSO上传文件默认partneruserno为10位，如果<10位会有问题，暂时没有处理加入空格
     *
     */
    public Response upload(ApplyTobParam applyInfo) {
        TobEnum anEnum = TobEnum.getEnum(applyInfo.getOrgid());
        applyInfo.setTobType(anEnum.getTobName());
        //登录
        HashMap<String, String> headMap = new HashMap<>();
        ApiConfig apiConfig = XmlPropertiesUtil.apiConfig;
        if("1".equalsIgnoreCase(applyInfo.getEnv())){
            log.info("开发环境：{},{}",applyInfo);
            apiConfig.setRootUrl("https://japi-dev.maucash.id");
        }else{
            log.info("测试环境：{},{}",applyInfo);
            apiConfig.setRootUrl("https://japi-fat.maucash.id");

        }
        login(applyInfo, anEnum,headMap);
        //查询
        QueryWrapper<TbUser> queryTbUser = new QueryWrapper<>();
        queryTbUser.eq("mobile",applyInfo.getSubMobile());
        queryTbUser.eq("partner",applyInfo.getTobType());
       // TbUser tbUser = tbUserMapper.selectOne(queryTbUser);
        TbUser tbUser = getUser(queryTbUser,applyInfo.getEnv());
        //TbUser tbUser = changeEnv.getUserdev(queryTbUser);

        if(tbUser==null){
            log.info("参数错误{}",applyInfo.getSubMobile());
            throw new TestException("300","请检查参数");
        }
        switch (anEnum.getOrgid()){
            case 107:
               return dayaUpload(applyInfo, anEnum, tbUser,headMap);
            case 109:
                return  smpUpload(applyInfo, anEnum, tbUser,headMap);
            case 104:
                return  trioUpload(applyInfo, anEnum, tbUser,headMap);
            case 101:
                return  hsoUpload(applyInfo, anEnum, tbUser,headMap);
        }

        return new Response("0","请求成功",null);
    }

    public Response  hsoUpload(ApplyTobParam applyInfo, TobEnum anEnum, TbUser tbUser,HashMap<String, String> headMap) {
        List<String> textList = EasyExcelUtil.readTxt(anEnum.getTemplatePath().get(0));
        HsoModal hsoModal = EasyExcelUtil.easyReadExcel(anEnum.getTemplatePath().get(1),
                HsoModal.class).get(0);
        StringBuilder stringBuilder = new StringBuilder(textList.get(0));
        stringBuilder.replace(76,86,tbUser.getPartnerUserNo());
        stringBuilder.replace(243,255,"000000000000");
        List<StringBuilder> stringBuilders = new ArrayList<>();
        List<HsoModal> hsoMOdals =  new ArrayList<>();
        for (int i=0;i<applyInfo.getCount();i++){
            StringBuilder stringBuilder1 = new StringBuilder(stringBuilder);
            HsoModal hsoModal1 = new HsoModal();
            BeanUtils.copyProperties(hsoModal,hsoModal1);
            String invoiceNo = generateNo(10);
            stringBuilder1.replace(20,30,invoiceNo);

            long amount = applyInfo.getAmount()+i*1000000l;
            stringBuilder1.replace(stringBuilder1.length()-Long.toString(amount).length()
                    ,stringBuilder1.length(),Long.toString(amount));

            hsoModal1.setInvoiceno(invoiceNo);
            hsoModal1.setBillingNo(generateNo(9));
            hsoModal1.setBillingDate(localTimeString(LocalDateTime.now(),"yyyy/MM/dd"));
            stringBuilders.add(stringBuilder1);
            hsoMOdals.add(hsoModal1);
        }
        //写入前清空数据
//        EasyExcelUtil.writeTotxt(anEnum.getUploadPath().get(0),new ArrayList());
//        EasyExcelUtil.writeTotxt(anEnum.getUploadPath().get(0),stringBuilders);
//        EasyExcelUtil.easyWriteExcel(anEnum.getUploadPath().get(1));
//        EasyExcelUtil.easyWriteExcel(anEnum.getUploadPath().get(1),HsoModal.class,hsoMOdals);
        String uuid = UUID.randomUUID().toString();
        String filePath0 = "/tmp/"+uuid+anEnum.getUploadPath().get(0);
        String filePath1 = "/tmp/"+uuid+anEnum.getUploadPath().get(1);

        EasyExcelUtil.writeTotxt(filePath0,stringBuilders);
        EasyExcelUtil.easyWriteExcel(filePath1,HsoModal.class,hsoMOdals);
        //上传文件
        String uploadResponse =HttpUtil.doPostUpload(XmlPropertiesUtil.apiConfig,"post",
                "/awsom-moon/api/v3/loan/import/"+ anEnum.getTobName()
                ,"", anEnum.getTobName(), Arrays.asList(filePath0,filePath1),headMap);
        String response = null;
        try {
            response = JSONPath.read(uploadResponse, "$.result").toString();
        } catch (Exception e) {
            throw new TestException("300","请重试");
        }
        return  new Response("0","请求成功",response);

    }

    public Response trioUpload(ApplyTobParam applyInfo, TobEnum anEnum, TbUser tbUser,HashMap<String, String> headMap)  {
        List<String> list = EasyExcelUtil.readTxt(anEnum.getTemplatePath().get(0));
        StringBuilder stringBuilder = new StringBuilder(list.get(0));
        stringBuilder.replace(2,6,tbUser.getPartnerUserNo());
        stringBuilder.replace(32,40,localTimeString(LocalDateTime.now(),"MMddyyy"));
        stringBuilder.replace(42,50,localTimeString(LocalDateTime.now(),"MMddyyy"));
        stringBuilder.replace(59,68,"000000000");
        List<StringBuilder> list1 = new ArrayList<>();
        for(int i=0;i<applyInfo.getCount();i++){
            StringBuilder stringBuilder1 = new StringBuilder(stringBuilder);
//            BeanUtils.copyProperties(stringBuilder,stringBuilder1);
            log.info("stringBuilder:{}",stringBuilder);
            log.info("stringBuilder1:{}",stringBuilder1);
            stringBuilder1.replace(8,30,"BBBBBBBBBB"+generateNo(12));
            long amount = applyInfo.getAmount()+i*1000000l;
            stringBuilder1.replace(68-Long.toString(amount).length(),68,Long.toString(amount));
            list1.add(stringBuilder1);
        }
        //先清空再写入
//        EasyExcelUtil.writeTotxt(anEnum.getUploadPath().get(0),new ArrayList());
//        EasyExcelUtil.writeTotxt(anEnum.getUploadPath().get(0),list1);
        String uuid = UUID.randomUUID().toString();
        String filePath = "/tmp/"+uuid+anEnum.getUploadPath().get(0);
        EasyExcelUtil.writeTotxt(filePath,list1);
        //上传文件
        String uploadResponse =HttpUtil.doPostUpload(XmlPropertiesUtil.apiConfig,"post",
                "/awsom-moon/api/v3/loan/import/"+ anEnum.getTobName()
                ,"", anEnum.getTobName(), Arrays.asList(filePath),headMap);
        String response = null;
        try {
            response = JSONPath.read(uploadResponse, "$.result").toString();
        } catch (Exception e) {
            log.info("上传数据异常");
            throw new TestException("300","请重试");
        }
        return  new Response("0","请求成功",response);
    }
    public Response smpUpload(ApplyTobParam applyInfo, TobEnum anEnum, TbUser tbUser,HashMap<String, String> headMap){
        List<SmpModel> smpModels = EasyExcelUtil.easyReadExcel(anEnum.getTemplatePath().get(0), SmpModel.class);
        SmpModel smpModel = smpModels.get(0);
        smpModel.setCmCode(tbUser.getPartnerUserNo());
        smpModel.setInvoiceCcy("IDR");
        smpModel.setAmount(applyInfo.getAmount());
        String timeString = localTimeString(LocalDateTime.now(), "yyyyMMdd");
        smpModel.setInvoiceDate(timeString);
        List list = new ArrayList();
        for (int i=0;i<applyInfo.getCount();i++){
            SmpModel smpModel1 = new SmpModel();
            BeanUtils.copyProperties(smpModel,smpModel1);
            smpModel1.setSellerCode(generateNo(10));
            smpModel1.setInvoiceNo("CMD-07-2023-"+generateNo(8));
            smpModel1.setAmount(smpModel.getAmount()+i*1000000L);
            list.add(smpModel1);
        }
        //先清空再写入
//        EasyExcelUtil.easyWriteExcel(anEnum.getUploadPath().get(0));
//        EasyExcelUtil.easyWriteExcel(anEnum.getUploadPath().get(0),smpModel.getClass(),list);
        String uuid = UUID.randomUUID().toString();
        String filePath = "/tmp/"+uuid+anEnum.getUploadPath().get(0);
        EasyExcelUtil.easyWriteExcel(filePath,smpModel.getClass(),list);
        //上传文件
     String uploadResponse=   HttpUtil.doPostUpload(XmlPropertiesUtil.apiConfig,"post",
                "/awsom-moon/api/v3/loan/import/"+ anEnum.getTobName()
                ,"", anEnum.getTobName(), Arrays.asList(filePath),headMap);
        String response = null;
        try {
            response = JSONPath.read(uploadResponse, "$.result").toString();
        } catch (Exception e) {
            log.info("上传数据异常");
            throw new TestException("300","请重试");
        }
        return  new Response("0","请求成功",response);
    }

    public static String localTimeString(LocalDateTime localDateTime,String pattern ) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return  localDateTime.format(formatter);
    }

    public Response dayaUpload(ApplyTobParam applyInfo, TobEnum anEnum, TbUser tbUser,HashMap<String, String> headMap) {
        //读取模板  修改上传文件
        List<DayaModel> dayaModels =
                EasyExcelUtil.easyReadExcel(anEnum.getTemplatePath().get(0), DayaModel.class);
        DayaModel dayaModel = dayaModels.get(0);
        dayaModel.setCmCode(tbUser.getPartnerUserNo());
        dayaModel.setAmount(applyInfo.getAmount());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime localDateTime = now.plusDays(3);
        String expireDate =localTimeString(localDateTime,"dd/MM/yyyy");
        dayaModel.setInvoiceDate(localTimeString(now,"dd/MM/yyyy"));
        dayaModel.setExpireDate(expireDate);

        List list = new ArrayList();
        for (int i = 0; i< applyInfo.getCount(); i++){
            DayaModel dayaModel1 = new DayaModel();
            BeanUtils.copyProperties(dayaModel,dayaModel1);
            dayaModel1.setInvoiceNo("no"+generateNo(20));
            dayaModel1.setAmount(dayaModel1.getAmount()+i*1000000L);
            list.add(dayaModel1);
        }
        //先清空再写入
//        EasyExcelUtil.easyWriteExcel(anEnum.getUploadPath().get(0));
//        EasyExcelUtil.easyWriteExcel(anEnum.getUploadPath().get(0),dayaModel.getClass(),list);
        String uuid = UUID.randomUUID().toString();
        String filePath = "/tmp/"+uuid+anEnum.getUploadPath().get(0);
        EasyExcelUtil.easyWriteExcel(filePath,dayaModel.getClass(),list);
        //上传文件
        String uploadResponse = HttpUtil.doPostUpload(XmlPropertiesUtil.apiConfig, "post",
                "/awsom-moon/api/v3/loan/import/" + anEnum.getTobName()
                , "", anEnum.getTobName(), Arrays.asList(filePath),headMap);
        String response = null;
        try {
            response = JSONPath.read(uploadResponse, "$.result").toString();
        } catch (Exception e) {
            log.info("上传数据异常");
             throw new TestException("300","请重试");
        }
        return  new Response("0","请求成功",response);
    }

    public  String generateNo(int  length) {
        Random random = new Random();
        log.info("==========");
        StringBuilder stringBuilder = new StringBuilder();
        for (int i=0;i<length;i++){
            int a = random.nextInt(10);
            stringBuilder.append(a);
        }
        log.info("generateNo:{}",stringBuilder);
        return stringBuilder.toString();
    }

    private void login(ApplyTobParam applyInfo, TobEnum anEnum,HashMap<String, String> headMap) {
        QueryWrapper<UserInfo> qw = new QueryWrapper<>();
        UpdateWrapper<UserInfo> uw = new UpdateWrapper<>();
       // qw.eq("mobile", applyInfo.getParentMobile());
        qw.eq("mobile",getAesMobile(applyInfo.getParentMobile()));
        qw.eq("org_id", applyInfo.getOrgid());
//        UserInfo userInfos = userInfoMapper.selectOne(qw);
//        log.info("userInfos:{}",userInfos);
        String md5 = getMd5(applyInfo.getParentMobile());
        log.info("md5:{}",md5);
        UserInfo userInfo = new UserInfo();
        userInfo.setPasswd(md5);


        //将密码修改成和手机号一致
//        int update = userInfoMapper.update(userInfo, qw);

        int update = updateUser(userInfo, qw,applyInfo.getEnv());
//        int update = changeEnv.updateUserdev(userInfo, qw);
        if(update==0){
            log.info("参数错误{}",applyInfo.getParentMobile());
            throw new TestException("300","请检查参数");
        }
        anEnum.getOrgid();
        JSONObject getSmsjson = new JSONObject();
        getSmsjson.put("mobile", applyInfo.getParentMobile());
        //获取验证码
        String smsResponse = HttpUtil.execute(XmlPropertiesUtil.apiConfig,
                "post", "/awsom-user/api/v1/send-sms-code/by-user"
                , getSmsjson.toJSONString(), anEnum.getTobName());
        String smsCodeId = null;
        try {
            smsCodeId = JSONPath.read(smsResponse, "$.result").toString();
        } catch (Exception e) {
            log.info("获取验证码异常：{}",smsResponse);
            throw new TestException("300","请重试");
        }
        //
//        List<Header> headerList = clearToken(anEnum);
        //登录
        JSONObject loginJson=  new JSONObject();
        JSONObject loginSmsJson=  new JSONObject();
        loginJson.put("mobile", applyInfo.getParentMobile());
        loginJson.put("passwd", applyInfo.getParentMobile());
        loginSmsJson.put("smsCodeId",smsCodeId);
        loginSmsJson.put("smsCodeValue","8888");
        loginJson.put("smsCode",loginSmsJson);

        String loginResponse = HttpUtil.execute(XmlPropertiesUtil.apiConfig,
                "post", "/awsom-user/api/v1/user-login"
                , loginJson.toJSONString(), anEnum.getTobName());
        String token = null;
        try {
            token = JSONPath.read(loginResponse, "$.result.token").toString();
        } catch (Exception e) {
            log.info("登录异常：{}",loginResponse);
            throw new TestException("300","请重试");
        }
        headMap.put("x-user-token",token);
        //添加token
//        addToken(anEnum, headerList, token);
    }

    private void addToken(TobEnum anEnum, List<Header> headerList, String token) {
        headerList.forEach(x->{
            if(x.getName().equals(anEnum.getTobName())) {
                x.getElementList().add(new Element("x-user-token", token));
            }
        });
    }

    private static List<Header> clearToken(TobEnum anEnum) {
        List<Header> headerList = XmlPropertiesUtil.apiConfig.getHeaderList();
        for(Header header:headerList) {
            List<Element> elementList = header.getElementList();
            if(header.getName().equalsIgnoreCase(anEnum.getTobName())) {
                ListIterator<Element> listIterator = elementList.listIterator();
                while(listIterator.hasNext()) {
                    Element next = listIterator.next();
                    if(next.getName().equalsIgnoreCase("x-user-token")) {
                        listIterator.remove();
                    }
                }
            }
        }
        XmlPropertiesUtil.apiConfig.setHeaderList(headerList);
        return headerList;
    }

    public String getMd5(String md5String)  {
        String md5Str="";
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5String.getBytes();
            byte[] digest = md5.digest(bytes);
             md5Str = bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return md5Str;
    }
    public static String bytesToHex(byte[] bytes) {
        StringBuffer md5str = new StringBuffer();
        // 把数组每一字节换成16进制连成md5字符串
        int digital;
        for (int i = 0; i < bytes.length; i++) {
            digital = bytes[i];

            if (digital < 0) {
                digital += 256;
            }
            if (digital < 16) {
                md5str.append("0");
            }

            md5str.append(Integer.toHexString(digital));
        }
        return md5str.toString().toLowerCase();
    }
    public String getAesMobile(String mobile){
        String  base64 ="MmJjYzgzOTg4MzNmZDBhNw==";
        String  aesKey = new String(Base64Utils.decodeFromString(base64));
        log.info("加密{}",new String(Base64Utils.decodeFromString(base64)));
        byte[] bytes = aesKey.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec secretKeySpec = new SecretKeySpec(bytes,"AES");
        try {
            Cipher cipher  = Cipher.getInstance( "AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE,secretKeySpec);
            byte[]    bytes1 = cipher.doFinal(mobile.getBytes(StandardCharsets.UTF_8));
            log.info("加密{}",new String(bytes1));
            return Base64Utils.encodeToString(bytes1);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
        catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
    public int updateUser(UserInfo userInfo,QueryWrapper<UserInfo> qw,String env){
        if("1".equalsIgnoreCase(env)){
            log.info("开发环境：{},{}",userInfo,env);
            return  changeEnv.updateUserdev(userInfo,qw);
        }else{
            log.info("测试环境：{},{}",userInfo,env);
            return  changeEnv.updateUserfat(userInfo,qw);
        }
    }
    public TbUser getUser(QueryWrapper<TbUser> queryTbUser, String env ){
        if("1".equalsIgnoreCase(env)){
            log.info("开发环境：{},{}",queryTbUser,env);
            return  changeEnv.getUserdev(queryTbUser);
        }else{
            log.info("测试环境：{},{}",queryTbUser,env);
            return  changeEnv.getUserfat(queryTbUser);
        }

    }
}

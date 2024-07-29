package com.test.api;

import com.alibaba.fastjson.JSONPath;
import com.test.api.mapper.FinanceTradingMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Base64Utils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Date;

/**
 * @author: Faisal Mulya Santosa
 * @create: 2023-10-07 18:10
 * @Description:
 */
@Slf4j
@SpringBootTest
public class Test03 {
    @Autowired
    FinanceTradingMapper financeTradingMapper;
    @org.junit.jupiter.api.Test
    public void testSalary() {
        Date date = new Date();
        Timestamp timeStamp = new Timestamp(date.getTime());
        financeTradingMapper.insertMonthlySalary(BigInteger.valueOf(6220077), 10000000, timeStamp, timeStamp);
    }

    public static void main(String[] args) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
//        String confirmResp ="{\n\t\"code\":10930042,\n\t\"message\":\"Pre-approval in progress\"\n}";
//        String code = JSONPath.read(confirmResp, "$.code").toString();
//        String code2 = (String)JSONPath.read(confirmResp, "$.code");
//
//        log.info("code2.equalsIgnoreCase(\"10930042\"):{},code:{}",code.equalsIgnoreCase("10930042"),code2);


//        String  aesKey = new String (Base64.getDecoder().decode(base64.getBytes(StandardCharsets.UTF_8)));
//        log.info("加密{}",aesKey);
//
//        log.info("加密{}",new String(Base64.getEncoder().encode("2bcc8398833fd0a7".getBytes())));
        String  base64 ="MmJjYzgzOTg4MzNmZDBhNw==";
        String  aesKey = new String(Base64Utils.decodeFromString(base64));
        log.info("加密{}",aesKey);
        byte[] bytes = aesKey.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec secretKeySpec = new SecretKeySpec(bytes,"AES");
        Cipher cipher = Cipher.getInstance( "AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE,secretKeySpec);
        byte[] bytes1 = cipher.doFinal("83668602828".getBytes(StandardCharsets.UTF_8));
        val s = Base64Utils.encodeToString(bytes1);
        log.info("加密{}",s);

    }
}

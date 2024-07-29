package com.test.api.util;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
/**
 * JsonPath解析工具类
 * @author Faisal Mulya Santosa
 *
 */
@Slf4j
public class JsonPathUtil {
    //private static Logger log = LoggerFactory.getLogger(JsonPathUtil.class.getName());
    /**
     * 解析json
     * @param json   json字符串
     * @param jsonPath   jsonPath表达式
     * @return
     */
    public static <T>T extract(String json,String jsonPath){
        try{

            Object document = Configuration.defaultConfiguration()
                    .jsonProvider()
                    .parse(json);

            return JsonPath.read(document,jsonPath);

        }catch(Exception e){
            log.error("jsonpath error,jsonpath="+jsonPath+",json="+json, e);
            return null;
        }
    }



}
package com.test.api.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

public final class SignUtil {
    private SignUtil() {
    }

    public static String getSignature(String json, String secret) {
        if (json != null && json.trim().length() != 0) {
            JSONObject map = JSON.parseObject(json, new Feature[]{Feature.OrderedField});
            String[] keys = new String[map.size()];
            int i = 0;

            Map.Entry entry;
            for (Iterator var6 = map.entrySet().iterator(); var6.hasNext(); keys[i++] = (String) entry.getKey()) {
                entry = (Map.Entry) var6.next();
            }

            Arrays.sort(keys);
            StringBuilder stringBuilder = new StringBuilder();
            String[] var9 = keys;
            int var8 = keys.length;

            String str;
            for (int var7 = 0; var7 < var8; ++var7) {
                str = var9[var7];
                stringBuilder.append(str);
                stringBuilder.append(":");
                stringBuilder.append(map.get(str));
            }

            stringBuilder.append(secret);
            str = stringBuilder.toString();
            str = str.replace("\r", "").replace("\n", "").replace("\t", "").replace(" ", "");
            System.out.println("签名:" + str);
            String encodeStr = DigestUtils.md5Hex(str).toUpperCase();
            return encodeStr;
        } else {
            System.err.println("签名参数为空");
            return null;
        }
    }
}

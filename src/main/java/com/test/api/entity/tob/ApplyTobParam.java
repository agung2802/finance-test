package com.test.api.entity.tob;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Faisal Mulya Santosa
 * @create: 2023-04-28 16:29
 * @Description:
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ApplyTobParam {

    private String parentMobile;
    private String subMobile;
    private String tobType;
    private byte orgid;
    private long  amount;
    private int count;
    private String env;
}

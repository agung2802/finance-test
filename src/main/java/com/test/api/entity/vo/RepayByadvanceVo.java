package com.test.api.entity.vo;

import com.test.api.entity.Dues;
import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

/**
 * @author Faisal Mulya Santosa
 * @create 2022-10-14 17:41
 * @description:
 */
@Data
public class RepayByadvanceVo {
    /**
     * 试算结果
     */
    private HashMap<String, BigDecimal> map;
    /**
     * 未还款dues
     */
    private List<Dues> dues;
}

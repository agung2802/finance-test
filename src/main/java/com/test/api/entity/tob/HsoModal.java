package com.test.api.entity.tob;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Faisal Mulya Santosa
 * @create: 2023-05-06 15:47
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HsoModal {
    @ExcelProperty("SOff.")
    private  String   s0ff;
    @ExcelProperty("Invoice No")
    private  String   invoiceno;
    @ExcelProperty("Item")
    private  String   item;
    @ExcelProperty("Billing No")
    private  String   billingNo;
    @ExcelProperty("Net Value")
    private  String   netValue;
    @ExcelProperty("Curr.")
    private  String   curr1;
    @ExcelProperty("Customer")
    private  String   customer;
    @ExcelProperty("Billing Date")
    private  String   billingDate;
    @ExcelProperty("CGrp")
    private  String   cgrp;
    @ExcelProperty("Tax Amount")
    private  String   taxAmount;
    @ExcelProperty("Curr.")
    private  String   curr2;
    @ExcelProperty("Nett + VAT")
    private  String   nettVat;
    @ExcelProperty("Curr.")
    private  String   curr3;
}


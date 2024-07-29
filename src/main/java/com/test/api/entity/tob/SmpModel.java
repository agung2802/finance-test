package com.test.api.entity.tob;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author: Faisal Mulya Santosa
 * @create: 2023-05-05 18:56
 * @Description:
 */
@Data
public class SmpModel {

        @ExcelProperty("BUYER_CODE")
        private String cmCode;
        @ExcelProperty("SELLER_CODE")
        private  String sellerCode;
        @ExcelProperty("INVOICE_NO")
        private String invoiceNo;
        @ExcelProperty("INVOICE_CCY")
        private String invoiceCcy;
         @ExcelProperty("INVOICE_AMT")
        private long amount;
        @ExcelProperty("INVOICE_DATE")
        private String invoiceDate;
        @ExcelProperty("PO_NO")
        private int poNo;
}

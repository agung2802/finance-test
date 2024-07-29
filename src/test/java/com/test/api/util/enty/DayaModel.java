package com.test.api.util.enty;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author: Faisal Mulya Santosa
 * @create: 2023-04-28 09:58
 * @Description:
 */
@Data
public class DayaModel {
    @ExcelProperty(value = "Kode DAM")
    private String cmCode;
    @ExcelProperty(value = "Nama Customer")
    private String customerName;
    @ExcelProperty(value = "No faktur")
    private String invoiceNo;
    @ExcelProperty(value = "Tgl Faktur")
    private String invoiceDate;
    @ExcelProperty(value = "Tanggal Jatuh Tempo")
    private String expDate;
    @ExcelProperty(value = "Amount")
    private long amount ;
    @ExcelProperty(value = "kategori")
    private String goods;

}

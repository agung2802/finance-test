package com.test.api.entity.tob;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum TobEnum {
    HSO("HSO", Arrays.asList("file/HSOtxtTemplate.txt",
            "file/HSOExcelTemplate.xls"), Arrays.asList("/HSOtxt.txt"
            ,"/HSOExcel.xls"),(byte) 101),
    TRIO("TRIO", Arrays.asList("file/triotemplate.txt"),
            Arrays.asList("/trio.txt"),(byte) 104),
    SMP("SMP", Arrays.asList("file/smptemplate.xls"),
            Arrays.asList("/smp.xls"),(byte) 109),
    DAYA("DAYA", Arrays.asList("file/dayatemplate.xlsx"),
            Arrays.asList("/daya.xlsx"),(byte) 107);
    private String tobName;
    private List<String> templatePath;
//    private List<String> fileSuffix;
    private List<String> uploadPath;
    private byte orgid;

    public static TobEnum getEnum(Byte  value){
        if(value==null){
            return null;
        }
        for (TobEnum tob: TobEnum.values()){
            if(value== tob.getOrgid()){
                return tob;
            }
        }
        return null;
    }

}

package com.test.api.util.preUtils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.test.api.cases.BaseCase;
import com.test.api.entity.LoanTryEPlan;
import com.test.api.entity.vo.LoanEPlanVo;
import com.test.api.entity.vo.TryEPlanVo;
import com.test.api.service.RepaymentService;
import com.test.api.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author: Faisal Mulya Santosa
 * @create: 2022-11-15 11:42
 * @Description:
 */
@Slf4j
@Component
public class EpalnPreProcess {
    @Autowired
    RepaymentService repaymentService;

    /**
     * E计划   8606359366    CL22123014193559239772589    PL22123015562843782119271
     * @param paramtersMap
     */
    public void preApplyEplan(HashMap<String,Object> paramtersMap){

        log.info("app申请E计划");

        HashMap<String, Boolean> trASsertionMap = new HashMap<String, Boolean>();
        String getDuesResp = HttpUtil.execute(BaseCase.apiConfig, "get",
                "/awsom-komodo/api/v3/finances/getDuesByUserId", "", "applyHeader");
        String listStr = JSONPath.read(getDuesResp, "$.result.list").toString();
        //获取期数
        HashMap<String, String> loanNumberAndTenor= new HashMap<>();
        HashMap<String, String> LoanNumberAndProduct = new HashMap<>();
        List<Object> list1= JSONObject.parseObject(listStr, List.class);
        for (Object  obj: list1) {
            HashMap<String,String> map = JSONObject.parseObject(obj.toString(), HashMap.class);
            String loanNumber = map.get("loanNumber").toString();
            loanNumberAndTenor.put(loanNumber,map.get("period").toString());
            LoanNumberAndProduct.put(loanNumber,map.get("secondProdCode").toString());
        }
        String isAllowApplyEPlan = JSONPath.read(getDuesResp, "$.result.isAllowApplyEPlan").toString();
        trASsertionMap.put("isAllowApplyEPlan是否能参加E计划",isAllowApplyEPlan.equalsIgnoreCase("true"));

       String eplanListStr =  HttpUtil.execute(BaseCase.apiConfig,"GET",
                "/awsom-komodo/api/v3/eplan/list","","applyHeader");
        String eplanListResult = JSONPath.read(eplanListStr, "$.result").toString();
        List<Object> list = JSONObject.parseObject(eplanListResult, List.class);
        //eplan  试算/awsom-komodo/api/v3/eplan/try
        List<String> tryEplan = new ArrayList<>();
        //eplan
        List<LoanTryEPlan> ePlanlist = new ArrayList<>();
        ArrayList<String> loanNumberList = new ArrayList<>();
        for (Object ls:list) {
            HashMap<String,Object> map = JSONObject.parseObject(ls.toString(), HashMap.class);
            String loanNumber = map.get("loanNumber").toString();
            tryEplan.add(loanNumber);
            loanNumberList.add(loanNumber);
            LoanTryEPlan loanTryEPlan = new LoanTryEPlan();
            loanTryEPlan.setLoanNumber(loanNumber);
            loanTryEPlan.setTenor(loanNumberAndTenor.get(loanNumber));
            loanTryEPlan.setDiscountLFratio("0.5");
            String secondProdCode= LoanNumberAndProduct.get(loanNumber);
            if(secondProdCode.contains("long")){
                loanTryEPlan.setDisDownPaymentRatio("0.5");
            }else if(secondProdCode.contains("short")){
                loanTryEPlan.setDisDownPaymentRatio("0.25");

            }
            ePlanlist.add(loanTryEPlan);
        }
        TryEPlanVo tryEPlanVo = repaymentService.repayByEplan(ePlanlist);
        //试算 /awsom-komodo/api/v3/eplan/try
        //还算只需验证最低还款金额和  新订单本金，新订单每期还款金额不展示
        String tryEplanResp = HttpUtil.execute(BaseCase.apiConfig, "post",
                "/awsom-komodo/api/v3/eplan/try", JSONObject.toJSONString(tryEplan), "applyHeader");

        String ePlanTryCalculationListStr = JSONPath.read(tryEplanResp, "$.result.ePlanTryCalculationList").toString();
        List<Object> tryResponseList = JSONObject.parseObject(ePlanTryCalculationListStr, List.class);
        for (Object o : tryResponseList) {
            HashMap<String,Object> map = JSONObject.parseObject(o.toString(), HashMap.class);
            HashMap<String,Object> ePlanTryCalculationDetail = JSONObject.parseObject(map.get("ePlanTryCalculationDetail").toString(), HashMap.class);
            for (LoanEPlanVo loanEPlanVo : tryEPlanVo.getLoanEPlanVoList()) {
                if(map.get("oldLoanNumber").equals(loanEPlanVo.getLoanNumber())){
                    trASsertionMap.put(map.get("oldLoanNumber")+"最低还款金额",Long.valueOf(map.get("minRepaymentAmount")
                            .toString()).compareTo(loanEPlanVo.getMinimumPayment())==0);
                    trASsertionMap.put(map.get("oldLoanNumber")+"新订单本金",Long.valueOf(ePlanTryCalculationDetail.get("newLoanTotalPrincipalAmount")
                            .toString()).compareTo(loanEPlanVo.getEPlanOldOrderUnpayPrincipal())==0);
                }
            }

        }
        //校验接口返回   老订单返回的金额  ePlanOldLoanDuesList
        //校验接口返回 最低还款金额
        //校验接口返回 新订单服务费

        //校验接口返回 新订单本金

        //校验接口返回 新订单下一期应还款金额

        //提交订单  /awsom-komodo/api/v3/eplan/save-contact
        HashMap<String, Object> comfirmHashMap = new HashMap<>();
        ArrayList<String> mobileList = new ArrayList<>();
        mobileList.add("8606359366");
        comfirmHashMap.put("contactMobile",mobileList);
        comfirmHashMap.put("loanNumber",loanNumberList);
//        String comfirmString = JSONObject.toJSONString(comfirmHashMap);
        HttpUtil.execute(BaseCase.apiConfig, "post",
                "/awsom-komodo/api/v3/eplan/save-contact", JSONObject.toJSONString(comfirmHashMap), "applyHeader");

    }


    public void submitEplan(HashMap<String,Object> paramtersMap){

        String userId = paramtersMap.get("userId").toString();
        String unsettledResponse = HttpUtil.execute(BaseCase.apiConfig, "get",
                "/awsom-collection/api/v3/auth/user/listUserUnsettledLoan?userId=691285" +
                        "&pageNum=1&pageSize=10", "", "collectionweb");
        String uncloseList = JSONPath.read(unsettledResponse, "$.result.list").toString();
        List<Object> list = JSONObject.parseObject(uncloseList, List.class);
        ArrayList<String> loanNuberList = new ArrayList<>();
        for (Object o : list) {
            HashMap hashMap = JSONObject.parseObject(o.toString(), HashMap.class);
            loanNuberList.add(hashMap.get("loanNumber").toString());
        }
        //   /awsom-collection/api/v3/edit-repayment/tryePlan
    }

    public  void confirmEplan(HashMap<String,Object> paramtersMap){

    }

}

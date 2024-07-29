package com.test.api.util.preUtils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.test.api.cases.BaseCase;
import com.test.api.entity.TryCPlanInfo;
import com.test.api.entity.vo.TryCPlan;
import com.test.api.pojo.Element;
import com.test.api.pojo.Header;
import com.test.api.service.RepaymentService;
import com.test.api.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.Serializers;
import org.eclipse.jetty.util.ajax.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author Faisal Mulya Santosa
 * @ClassName PreProcessByCPlan.java/scala
 * @Description TODO
 * @createTime 2022年11月03日 17:54:00
 */
@Slf4j
@Component
public class PreProcessByCPlan {

    @Autowired
    RepaymentService repaymentService;
/**
  * @Desc
  * @Params   单笔：87800141   多笔：8364244772
  * @Return 
  * @author Faisal Mulya Santosa
  * @Date 2024-07-29
  **/
    public void applyCPlanByApp(HashMap<String,Object> map){
        log.info("开始提交C计划");

        HashMap<String, Boolean> trASsertionMap = new HashMap<String, Boolean>();
        String getDuesResp = HttpUtil.execute(BaseCase.apiConfig, "get",
                "/awsom-komodo/api/v3/finances/getDuesByUserId", "", "applyHeader");
        String isAllowApplyCPlan = JSONPath.read(getDuesResp, "$.result.isAllowApplyCPlan").toString();
        trASsertionMap.put("isAllowApplyCPlan是否能参加C计划",isAllowApplyCPlan.equalsIgnoreCase("true"));
        //试算参数
        TryCPlanInfo tryCPlanInfo = new TryCPlanInfo();
        List<Map<String, Object>> mapsList = new ArrayList<>();
        tryCPlanInfo.setAmount("0");
        tryCPlanInfo.setTryCList(mapsList);
        //获取能参加C计划订单
        String joinC = JSONPath.read(getDuesResp, "$.result.list").toString();
        log.info("joinC:{}",joinC);
        List<String> joinCList = new ArrayList<>();
        List list = JSONObject.parseObject(joinC, List.class);
        for (Object obj:list) {
            HashMap<String, Object> obj1 = JSONObject.parseObject(obj.toString(),HashMap.class);
            joinCList.add(obj1.get("loanNumber").toString());
            //试算参数
            Map<String, Object> tryParam = new HashMap<>();
            tryParam.put("loanNumber",obj1.get("loanNumber").toString());
            tryParam.put("discountLFratio","0.5");
            mapsList.add(tryParam);
        }

        TryCPlan tryCPlan = repaymentService.repayByCplan(tryCPlanInfo);
        //获取C计划自定义金额范围  /awsom-komodo/api/v3/finances/queryCPlanCustomAmount

        HashMap<String, Object> querryParmMap = new HashMap<>();
        querryParmMap.put("loanNumbers",joinCList);
        String  queryCPlanAmoutParam = JSONObject.toJSONString(querryParmMap);
        String cPlanAmountResp = HttpUtil.execute(BaseCase.apiConfig, "post", "/awsom-komodo/api/v3/finances/queryCPlanCustomAmount",
                queryCPlanAmoutParam, "applyHeader");

        //获取每笔订单的C计划最低还款金额
        String minString= JSONPath.read(cPlanAmountResp, "$.result.loanNumberAmountMap").toString();
        HashMap<String,Integer> minList = JSONObject.parseObject(minString,HashMap.class);
        //获取所有订单的最低还款金额和最大还款金额
        Integer minAmount = (Integer)JSONPath.read(cPlanAmountResp, "$.result.minAmount");
        Integer maxAmount = (Integer)JSONPath.read(cPlanAmountResp, "$.result.maxAmount");
        //试算的最小值合最大值
        int totalMinAmount = tryCPlan.getTotalMinAmount().intValue();
        int totalMaxAmount = tryCPlan.getTotalMaxAmount().intValue();
        trASsertionMap.put("最低还款金额totalMinAmount",minAmount.compareTo(totalMinAmount)==0);
        trASsertionMap.put("最大还款金额totalMaxAmount",maxAmount.compareTo(totalMaxAmount)==0);

        //比较每笔订单最低还款金额    所有订单  最低还款金额   最大还款金额


        ///输入自定义金额 试算 welab-komodo/api/v3/finances/tryRepayCPlan
        HashMap<String, Object> tryCPlanMap = new HashMap<>();
        tryCPlanMap.putAll(querryParmMap);
        tryCPlanMap.put("amount",minAmount);
        String tryCPlanParam = JSONObject.toJSONString(tryCPlanMap);
        String tryRepayCPlanResp = HttpUtil.execute(BaseCase.apiConfig, "post", "/awsom-komodo/api/v3/finances/tryRepayCPlan"
                , tryCPlanParam, "applyHeader");

        //提取  C计划试算  下一期应还款金额   总的服务费interest（资金方利息，平台服务费（含税），保证金），进位费，本金
        Integer interest = (Integer)JSONPath.read(tryRepayCPlanResp, "$.result.interest");
        Integer carryingFee = (Integer)JSONPath.read(tryRepayCPlanResp, "$.result.carryingFee");
        Integer principal = (Integer)JSONPath.read(tryRepayCPlanResp, "$.result.principal");
        //下一期总的还款金额
        Integer planAmount = (Integer)JSONPath.read(tryRepayCPlanResp, "$.result.planAmount");
        //下一期还款金额+C计划用户自定义还款金额
        Integer nextTotalAmount = (Integer)JSONPath.read(tryRepayCPlanResp, "$.result.nextTotalAmount");

        //试算的 下期应还款金额
        int totalInterest = tryCPlan.getTotalInterest().intValue();
        int totalCarryingFee = tryCPlan.getTotalCarryingFee().intValue();
        int totalplanAmount = tryCPlan.getTotalplanAmount().intValue();
        int nextTotalAmount1 = tryCPlan.getNextTotalAmount().intValue();
        int totalPrincipal = tryCPlan.getTotalPrincipal().intValue();

        trASsertionMap.put("C计划试算的下一期总的利息interest",interest.compareTo(totalInterest)==0);
        trASsertionMap.put("C计划试算的下一期总的进位费carryingFee",carryingFee.compareTo(totalCarryingFee)==0);
        trASsertionMap.put("C计划试算的下一期总的进位费principal",principal.compareTo(totalPrincipal)==0);
        trASsertionMap.put("C计划试算的下一期总的还款金额planAmount",planAmount.compareTo(nextTotalAmount1)==0);
        trASsertionMap.put("C计划试算的下一期总的还款金额+当前还款金额nextTotalAmount",nextTotalAmount.compareTo(totalplanAmount)==0);


        //提取C计划试算  当期应还款金额
       String  loanTryCPlanListStr= JSONPath.read(tryRepayCPlanResp, "$.result.loanTryCPlanList").toString();
        List loanTryCPlanList = JSONObject.parseObject(loanTryCPlanListStr,List.class);
        //生产合同 /awsom-komodo/api/v3/finances/createCPlanContract
        HttpUtil.execute(BaseCase.apiConfig,"post","/awsom-komodo/api/v3/finances/createCPlanContract"
                ,tryCPlanParam,"applyHeader");

        //提交C计划 /awsom-komodo/api/v3/finances/repayApplyCPlan
        HashMap<String, Object> repayApplyCPlanMap = new HashMap<>();
        repayApplyCPlanMap.putAll(querryParmMap);
        repayApplyCPlanMap.put("reason","2");
        repayApplyCPlanMap.put("customAmount",minAmount);


        String  repayApplyCPlanParam = JSONObject.toJSONString(repayApplyCPlanMap);
        HttpUtil.execute(BaseCase.apiConfig,"post","/awsom-komodo/api/v3/finances/repayApplyCPlan"
                ,repayApplyCPlanParam,"applyHeader");

        log.info("完成提交C计划");

    }

/**
  * @Desc  后台提交C计划
  * @Params
  * @Return
  * @author Faisal Mulya Santosa
  * @Date 2024-07-29
  **/
    public void applyCPlanBackground(HashMap<String,Object> map){

        handleToken();
        //获取能参加C计划的订单  /awsom-komodo/api/v3/finances/getAllowJoinCPlanLoan?userId=686347
        String joinCParam = "userId="+map.get("userId");
        String joinCResp = HttpUtil.execute(BaseCase.apiConfig, "get", "/awsom-komodo/api/v3/finances/getAllowJoinCPlanLoan"
                , joinCParam, "collectionweb");
        String joinCResult = JSONPath.read(joinCResp, "$.result").toString();
        List list = JSONObject.parseObject(joinCResult, List.class);
        ArrayList<Object> tryCPlanDetailParamList = new ArrayList<>();
        list.forEach(x->{
            HashMap<String, String> cPlanMap = new HashMap<>();
            cPlanMap.put("disLateFeeRatio","0.5");
            cPlanMap.put("loanNumber",x.toString());
            tryCPlanDetailParamList.add(cPlanMap);
        });
        //C计划试算 /awsom-komodo/api/v3/finances/tryCPlanForCollection
        HashMap<String, Object> tryCPlanMap = new HashMap<>();
        tryCPlanMap.put("ptpDay",2);
        tryCPlanMap.put("reason",0);
        tryCPlanMap.put("minRepaymentTotalAmount",0);
        tryCPlanMap.put("tryCPlanDetailParamList",tryCPlanDetailParamList);


        String tryCPlanResp = HttpUtil.execute(BaseCase.apiConfig, "post", "/awsom-komodo/api/v3/finances/tryCPlanForCollection"
                , JSONObject.toJSONString(tryCPlanMap), "collectionweb");

        //获取所有订单的最低还款金额和最大还款金额
        Integer minAmount = (Integer)JSONPath.read(tryCPlanResp, "$.result.minRepayAmount");
        Integer maxAmount = (Integer)JSONPath.read(tryCPlanResp, "$.result.maxRepayAmount");
        //试算结果
        String calCPlanDetailAmountListStr = JSONPath.read(tryCPlanResp, "$.result.calCPlanDetailAmountList").toString();
        List list1 = JSONObject.parseObject(calCPlanDetailAmountListStr, List.class);
        for (Object obj : list1) {
            HashMap<String,Object> hashmap = JSONObject.parseObject(obj.toString(),HashMap.class);
            for(Map.Entry  entry: hashmap.entrySet()){
                log.info("{}  ==={}",entry.getKey(),entry.getValue());
            }
        }

        tryCPlanMap.put("minRepaymentTotalAmount",minAmount);
        tryCPlanMap.put("reason",1394);
        tryCPlanMap.put("optName","3rt8d@mail.com");

        String submitCPlanStr = HttpUtil.execute(BaseCase.apiConfig, "post", "/awsom-komodo/api/v3/finances/submitCPlanForCollection"
                , JSONObject.toJSONString(tryCPlanMap), "collectionweb");

        //检查提交后   loan_apply_plans中最小金额 以及后台提交C计划标识 trigger_type




    }
/**
  * @Desc后台提交C 计划后，app确认C计划
  * @Params
  * @Return
  * @author Faisal Mulya Santosa
  * @Date 2024-07-29
  **/
    public void confirmCplanByapp(HashMap<String,Object> map){

        HashMap<String, Boolean> trASsertionMap = new HashMap<String, Boolean>();
        String getDuesResp = HttpUtil.execute(BaseCase.apiConfig, "get",
                "/awsom-komodo/api/v3/finances/getDuesByUserId", "", "applyHeader");
        String isContainCollectionCPlan = JSONPath.read(getDuesResp, "$.result.isContainCollectionCPlan").toString();
        trASsertionMap.put("isContainCollectionCPlan是否后台触发的C计划",isContainCollectionCPlan.equalsIgnoreCase("true"));

        //获取C计划试算结果  /awsom-komodo/api/v3/finances/tryCPlanForUser

        String tryCPlanStr = HttpUtil.execute(BaseCase.apiConfig, "post", "/awsom-komodo/api/v3/finances/tryCPlanForUser"
                , "", "applyHeader");
        String tryCPlanResultStr = JSONPath.read(tryCPlanStr, "$.result").toString();
        log.info("tryCPlanResultStr:{}",tryCPlanResultStr);

        //查看合同入参
        HashMap<String, Object> createdConTractParam = new HashMap<>();
        ArrayList<String> loanNumbers = new ArrayList<>();
        //C计划总的还款金额
        Integer customAmount = (Integer) JSONPath.read(tryCPlanResultStr, "$.totalMinRepaymentAmount");
        createdConTractParam.put("customAmount",customAmount);
        //C计划订单
        String cPlanTryCalculationListStr = JSONPath.read(tryCPlanResultStr, "$.cPlanTryCalculationList").toString();
        List list = JSONObject.parseObject(cPlanTryCalculationListStr, List.class);
        for (Object obj : list) {
            HashMap<String,Object> hashMap = JSONObject.parseObject(obj.toString(), HashMap.class);
            loanNumbers.add(hashMap.get("loanNumber").toString());
        }
        createdConTractParam.put("loanNumbers",loanNumbers);
        //生产合同 /awsom-komodo/api/v3/finances/createCPlanContract
        String createCPlanContractStr = HttpUtil.execute(BaseCase.apiConfig, "post", "/awsom-komodo/api/v3/finances/createCPlanContract"
                , JSONObject.toJSONString(createdConTractParam), "applyHeader");
        //提交C计划
        createdConTractParam.put("reason", 0);
        HttpUtil.execute(BaseCase.apiConfig,"post","/awsom-komodo/api/v3/finances/repayApplyCPlan"
                ,JSONObject.toJSONString(createdConTractParam),"applyHeader");


    }
    public void handleToken() {
        List<Header> headerList = BaseCase.apiConfig.getHeaderList();
        for(Header header:headerList) {
            if(header.getName().equalsIgnoreCase("collectionweb")) {
                List<Element> elementList = header.getElementList();
                Iterator<Element> iterator = elementList.iterator();
                while(iterator.hasNext()) {
                    Element next = iterator.next();
                    if(!next.getName().equalsIgnoreCase("x-user-token")) {
                        //发送短信
                        String smsParam = "{\"mobile\":\"8000000666\"}";
                        String smsResp = HttpUtil.execute(BaseCase.apiConfig, "post", "/awsom-user/api/v1/send-sms-code/by-user"
                                , smsParam, "collectionweb");
                        Integer smsId = (Integer) JSONPath.read(smsResp, "$.result");

                        String loginParam ="{\"mobile\":\"8000000666\",\"passwd\":\"Aa_123456\",\"smsCode\":{\"smsCodeId\":\""+smsId+"\",\"smsCodeValue\":\"8888\"}}";
                        String loginResp = HttpUtil.execute(BaseCase.apiConfig, "post", "/awsom-user/api/v1/user-login"
                                , loginParam, "collectionweb");
                        String token = (String) JSONPath.read(loginResp, "$.result.token");
                        headerList.forEach(x->{
                            if(x.getName().equalsIgnoreCase("collectionweb")) {
                                x.getElementList().add(new Element("x-user-token", token));
                                x.getElementList().add(new Element("x-user-mobile", "8000000666"));
                                x.getElementList().add(new Element("X-Product-Code", "YN-SYSTEM"));

                            }

                        });
                        break;
                    }
                }
            }
        }
    }
}

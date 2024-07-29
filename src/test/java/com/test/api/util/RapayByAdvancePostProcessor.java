package com.test.api.util;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.test.api.entity.Dues;
import com.test.api.entity.FinanceRepaymentTrack;
import com.test.api.entity.LoanDetail;
import com.test.api.entity.Loans;
import com.test.api.entity.vo.LoanVo;
import com.test.api.entity.vo.RepayByadvanceVo;
import com.test.api.mapper.DuesMapper;
import com.test.api.mapper.FinanceRepaymentTrackMapper;
import com.test.api.mapper.LoanDetailMapper;
import com.test.api.mapper.LoansMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Faisal Mulya Santosa
 * @create 2022-10-14 15:45
 * @description:
 */
@Slf4j
@Component
public class RapayByAdvancePostProcessor {
   public static final int diff = 10;
    @Autowired
    DuesMapper duesMapper;
    @Autowired
    FinanceRepaymentTrackMapper repaymentTrackMapper;
    @Autowired
    LoanDetailMapper LoanDetailMapper;
    @Autowired
    LoansMapper loansMapper;
    /**
     * 检查dues
     * @param loanVo
     */
    public void checkDues(LoanVo loanVo){
        HashMap<String, Boolean> assertionMap = loanVo.getAssertionMap();
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("loan_id",loanVo.getLoanNumber());
        List<Dues> list = duesMapper.selectList(queryWrapper);
        Collections.sort(list, new Comparator<Dues>() {
            @Override
            public int compare(Dues o1, Dues o2) {
                if(o1.getDueIndex().compareTo(o2.getDueIndex())==0){
                    return o1.getRepayIndex().compareTo(o2.getRepayIndex());
                }
                return o1.getDueIndex().compareTo(o2.getDueIndex());
            }
        });
        List<Dues> tryAdvanceDues = loanVo.getRepayByadvanceVo().getDues();
        //未结清的dueindexde 起始值
        Optional<Dues> first = tryAdvanceDues.stream().findFirst();
        Integer Index = first.get().getDueIndex();

        //判断订单是否结清
        long countColse = list.stream().filter(x -> x.getIsClose().compareTo(true) == 0).count();
        if(countColse==0){
            assertionMap.put("DuesColseCountAssertion",true);
        }
        //比对已结清dues中的结清金额和提前结清试算的未还款金额
        final List<Dues> closeDues = list.stream().filter(x -> x.getDueIndex() >= Index).collect(Collectors.toList());
        for(Dues dues : closeDues){
            List<Dues> collect = tryAdvanceDues.stream().filter(x -> x.getDueIndex() == dues.getDueIndex()
                    && x.getDueType().equalsIgnoreCase(dues.getDueType())).collect(Collectors.toList());
//            boolean checkSettleAmount = dues.getSettledAmount().compareTo(collect.get(0).getRemainingAmount()) == 0 ? true : false;
            boolean checkSettleAmount = Math.abs(dues.getSettledAmount()-collect.get(0).getRemainingAmount())<=diff? true : false;
            if("Admin_Fee".equalsIgnoreCase(dues.getDueType())){
                assertionMap.put("advance管理费dues",checkSettleAmount);
                log.info("advance管理费 试算应还款金额：{}，还款后的结清金额：{},断言结果:{}",collect.get(0).getRemainingAmount(),dues.getSettledAmount(),checkSettleAmount);
            }else if("Admin_Fee_VAT".equalsIgnoreCase(dues.getDueType())){
                assertionMap.put("advance管理费VATdues",checkSettleAmount);
                log.info("Admin_Fee_VAT 试算应还款金额：{}，还款后的结清金额：{},断言结果:{}",collect.get(0).getRemainingAmount(),dues.getSettledAmount(),checkSettleAmount);
            }else if("Fund_Interest".equalsIgnoreCase(dues.getDueType())){
                assertionMap.put("advance资金方利息dues",checkSettleAmount);
                log.info("资金方利息 试算应还款金额：{}，还款后的结清金额：{},断言结果:{}",collect.get(0).getRemainingAmount(),dues.getSettledAmount(),checkSettleAmount);
            }else if("Service_Fee".equalsIgnoreCase(dues.getDueType())){
                assertionMap.put("advance平台服务费dues",checkSettleAmount);
                log.info("平台服务费 试算应还款金额：{}，还款后的结清金额：{},断言结果:{}",collect.get(0).getRemainingAmount(),dues.getSettledAmount(),checkSettleAmount);
            }else if("Service_Fee_VAT".equalsIgnoreCase(dues.getDueType())){
                assertionMap.put("advance平台服务费vatdues",checkSettleAmount);
                log.info("平台服务费VAT 试算应还款金额：{}，还款后的结清金额：{},断言结果:{}",collect.get(0).getRemainingAmount(),dues.getSettledAmount(),checkSettleAmount);
            }else if("Principal".equalsIgnoreCase(dues.getDueType())){
                assertionMap.put("advance本金dues",checkSettleAmount);
                log.info("本金 试算应还款金额：{}，还款后的结清金额：{},断言结果:{}",collect.get(0).getRemainingAmount(),dues.getSettledAmount(),checkSettleAmount);
            }else if("Carrying_Fee_VAT".equalsIgnoreCase(dues.getDueType())){
                assertionMap.put("advance进位费Vatdues",checkSettleAmount);
                log.info("advance进位费Vat试算应还款金额：{}，还款后的结清金额：{},断言结果:{}",collect.get(0).getRemainingAmount(),dues.getSettledAmount(),checkSettleAmount);
            }else if("Carrying_Fee".equalsIgnoreCase(dues.getDueType())){
                assertionMap.put("advance进位费dues",checkSettleAmount);
                log.info("进位费 试算应还款金额：{}，还款后的结清金额：{},断言结果:{}",collect.get(0).getRemainingAmount(),dues.getSettledAmount(),checkSettleAmount);
            }
        }
    }

    /**
     * 检查还款轨迹中金额 和提前结清标识
     * @param loanVo
     */
    public  void checkRepaymentCheck(LoanVo loanVo){

        HashMap<String, Boolean> assertionMap = loanVo.getAssertionMap();
        //试算结果
        HashMap<String, BigDecimal> map = loanVo.getRepayByadvanceVo().getMap();
        QueryWrapper<FinanceRepaymentTrack> Wrapper = new QueryWrapper<>();
        Wrapper.eq("loan_number",loanVo.getLoanNumber());
        Wrapper.orderByDesc("id").last("limit 1");
        FinanceRepaymentTrack track = repaymentTrackMapper.selectOne(Wrapper);
        int totalAmountAssert = track.getRepaymentAmount().compareTo(map.get("totalAmount").longValue());
        assertionMap.put("总的还款金额RepaymentAmount",totalAmountAssert==0?true:false);
        boolean fundInterest = track.getFundInterestFee().compareTo(map.get("fundInterest").longValue())==0;
        assertionMap.put("还款的资金方利息fundInterest",fundInterest);

        boolean serviceFeeINcludeVat = Math.abs(track.getServiceFee().longValue() - map.get("serviceFeeINcludeVat").longValue()) <= diff;
        assertionMap.put("还款的平台服务费serviceFeeINcludeVat",serviceFeeINcludeVat);

        boolean provisionfee = track.getProvisionFee().compareTo(map.get("provisionfee").longValue()) == 0;
        assertionMap.put("还款的保证金provisionfee",provisionfee);

        boolean lateFee = track.getLateFee().compareTo(map.get("lateFee").longValue()) == 0;
        assertionMap.put("还款的逾期费lateFee",lateFee);
        boolean principalSum = track.getPrincipalFee().compareTo(map.get("principalSum").longValue()) == 0;
        assertionMap.put("还款本金",principalSum);

        boolean carryfee = Math.abs(track.getOverAmount() - map.get("carryFee").longValue()) <= diff;
        assertionMap.put("进位费carryfee",carryfee);
        int advanceTag = track.getIsAdvance().compareTo(true);
        assertionMap.put("提前结清标识",advanceTag==0?true:false);

    }
    /**
     * 检查结清状态loandetail,komodo.loans状态
     * @param loanVo
     */
    public   void  checkLoanDetailStatus(LoanVo loanVo){
        HashMap<String, Boolean> assertionMap = loanVo.getAssertionMap();
        QueryWrapper<LoanDetail> wrapper = new QueryWrapper<>();
        wrapper.eq("loan_number",loanVo.getLoanNumber());
        LoanDetail loanDetail = LoanDetailMapper.selectOne(wrapper);
        int RepayStatus = loanDetail.getRepayStatus().compareTo(3);

        assertionMap.put("loanDetail中的还款状态RepayStatus",RepayStatus==0?true:false);

        QueryWrapper<Loans> wrapper2 = new QueryWrapper<>();
        wrapper2.eq("loan_number",loanVo.getLoanNumber());
        Loans loans = loansMapper.selectOne(wrapper2);
        boolean state = loans.getState().equalsIgnoreCase("closed");
        assertionMap.put("loans中的订单状态state",state);

    }
}

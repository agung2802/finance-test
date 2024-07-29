/**
 * 
 */
package com.test.api.util.preUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.test.api.entity.Dues;
import com.test.api.mapper.DuesMapper;
import com.test.api.util.HttpUtil;

import lombok.extern.slf4j.Slf4j;

/**  
 * @ClassName: RepaymentUtils
 * @Description: TODO(描述)
 * @author Faisal Mulya Santosa
 * @date 2024-07-29 
*/
@Slf4j
@Component
public class RepaymentUtils {
	@Autowired
	DuesMapper duesMapper;
	
	

	public void repaymentByRebook() {
		String loanNumber = "CL22071520331907454173321";
		QueryWrapper  queryDues=new QueryWrapper();
		queryDues.eq("loan_id",loanNumber );
		List<Dues> dueList = duesMapper.selectList(queryDues);
		Collections.sort(dueList, new Comparator<Dues>() {
			@Override
			public int compare(Dues o1, Dues o2) {
				// TODO Auto-generated method stub
				return o1.getDueIndex().compareTo(o2.getDueIndex());
			}
		});
		//还款次数
		 Integer maxDueIndex = dueList.get(dueList.size()-1).getDueIndex();
		int repayNum = ( maxDueIndex%2==0 ? maxDueIndex/2 : maxDueIndex/2+1 ) ;
		log.info("loanNumber:{},还款次数repayNum:{}", loanNumber,repayNum);
		//还款金额  还款期数 虚拟账号  虚拟账号金额（直接去dues里面汇总的金额）
		
	}
	public void repaymentHttp(String  accountNumber,long suggestedAmount) {  
		String  response = HttpUtil.doPost( "http://japi-fat.maucash.id/awsom-pay-gateway/api/receipt?bankCode=BNI&accountNumber="+accountNumber+"&fund=2&amount="+suggestedAmount, "", null);
		log.info("执行还款结果：{}", response);
	}
}

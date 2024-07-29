package com.test.api.service.impl;

import com.test.api.entity.FinanceTrading;
import com.test.api.mapper.FinanceTradingMapper;
import com.test.api.service.IFinanceTradingService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 交易流水表 服务实现类
 * </p>
 *
 * @author Faisal Mulya Santosa
 * @since 2024-07-29
 */
@Service
public class FinanceTradingServiceImpl extends ServiceImpl<FinanceTradingMapper, FinanceTrading> implements IFinanceTradingService {

}

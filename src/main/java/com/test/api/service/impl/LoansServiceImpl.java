package com.test.api.service.impl;

import com.test.api.entity.Loans;
import com.test.api.mapper.LoansMapper;
import com.test.api.service.ILoansService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 借款信息表 服务实现类
 * </p>
 *
 * @author Faisal Mulya Santosa
 * @since 2024-07-29
 */
@Service
public class LoansServiceImpl extends ServiceImpl<LoansMapper, Loans> implements ILoansService {

}

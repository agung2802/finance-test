package com.test.api.service;

import com.test.api.entity.tob.ApplyTobParam;
import com.test.api.utils.Response;

public interface TobService {
    Response upload(ApplyTobParam applyInfo);
}

package com.mall.service;

import com.mall.dto.OssCallbackResult;
import com.mall.dto.OssPolicyResult;

import javax.servlet.http.HttpServletRequest;

/**
 * oss上传管理Service
 */
public interface OssService {
    /**
     * oss上传策略生成
     * @return
     */
    OssPolicyResult policy();

    /**
     * oss上传成功回调
     * @return
     */
    OssCallbackResult callback(HttpServletRequest httpServletRequest);
}

package com.mall.service.impl;

import cn.hutool.json.JSONUtil;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.mall.dto.OssCallbackParam;
import com.mall.dto.OssCallbackResult;
import com.mall.dto.OssPolicyResult;
import com.mall.service.OssService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * oss上传管理Service实现类
 */
@Service
public class OssServiceImpl implements OssService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OssServiceImpl.class);
    @Value("${aliyun.oss.policy.expire}")
    private int ALIYUN_OSS_EXPIRE/*签名有效期*/;
    @Value("${aliyun.oss.maxSize}")
    private int ALIYUN_OSS_MAX_SIZE/*上传文件大小*/;
    @Value("${aliyun.oss.callback}")
    private String ALIYUN_OSS_CALLBACK/*上传成功后的回调地址*/;
    @Value("${aliyun.oss.bucketName}")
    private String ALIYUN_OSS_BUCKET_NAME/*oss存储空间*/;
    @Value("${aliyun.oss.endpoint}")
    private String ALIYUN_OSS_ENDPOINT/*oss对外服务的访问域名*/;
    @Value("${aliyun.oss.dir.prefix}")
    private String ALIYUN_OSS_DIR_PREFIX/*上传文件夹路径前缀*/;

    @Autowired
    private OSSClient ossClient;

    /**
     * 签名生成-oss上传策略
     * @return
     */
    @Override
    public OssPolicyResult policy() {
        OssPolicyResult ossPolicyResult = new OssPolicyResult();
        //存储目录
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        String dir = ALIYUN_OSS_DIR_PREFIX + simpleDateFormat.format(new Date());
        //签名有效期
        long expireEndTime = System.currentTimeMillis() + ALIYUN_OSS_EXPIRE * 1000;
        Date expiration = new Date(expireEndTime);
        //文件大小
        long maxSize = ALIYUN_OSS_MAX_SIZE * 1024 * 1024 ;
        //回调
        OssCallbackParam callbackParam = new OssCallbackParam();
        callbackParam.setCallbackUrl(ALIYUN_OSS_CALLBACK)/*请求的回调地址*/;
        callbackParam.setCallbackBody("filename=${object}&size=${size}&mimeType=${mimeType}&height=${imageInfo.height}&width=${imageInfo.width}")/*回调是传入request中的参数*/;
        callbackParam.setCallbackBodyType("application/x-www-form-urlencoded")/*回调时传入参数的格式，比如表单提交形式*/;
        //提交节点
        String action = "http://" + ALIYUN_OSS_BUCKET_NAME + "." + ALIYUN_OSS_ENDPOINT;
        try {
            PolicyConditions policyConditions = new PolicyConditions();
            policyConditions.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, maxSize);
            policyConditions.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);
            String postPolicy = ossClient.generatePostPolicy(expiration, policyConditions);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String policy = BinaryUtil.toBase64String(binaryData);
            String signature = ossClient.calculatePostSignature(postPolicy);
            String callbackData = BinaryUtil.toBase64String(JSONUtil.parse(callbackParam).toString().getBytes("utf-8"));
            // 返回结果
            ossPolicyResult.setAccessKeyId(ossClient.getCredentialsProvider().getCredentials().getAccessKeyId())/*访问身份验证中用到用户标识*/;
            ossPolicyResult.setPolicy(policy)/*用户表单上传的策略,经过base64编码过的字符串*/;
            ossPolicyResult.setSignature(signature)/*对policy签名后的字符串*/;
            ossPolicyResult.setDir(dir)/*上传文件夹路径前缀*/;
            ossPolicyResult.setCallback(callbackData)/*上传成功后的回调设置*/;
            ossPolicyResult.setHost(action)/*oss对外服务的访问域名*/;
        } catch (Exception e) {
            LOGGER.error("签名生成失败", e);
        }
        return ossPolicyResult;
    }

    /**
     * oss上传成功回调
     * @param httpServletRequest
     * @return
     */
    @Override
    public OssCallbackResult callback(HttpServletRequest httpServletRequest) {
        OssCallbackResult result= new OssCallbackResult();
        String filename = httpServletRequest.getParameter("filename");
        filename = "http://".concat(ALIYUN_OSS_BUCKET_NAME).concat(".").concat(ALIYUN_OSS_ENDPOINT).concat("/").concat(filename);
        result.setFilename(filename);
        result.setSize(httpServletRequest.getParameter("size"));
        result.setMimeType(httpServletRequest.getParameter("mimeType"));
        result.setWidth(httpServletRequest.getParameter("width"));
        result.setHeight(httpServletRequest.getParameter("height"));
        return result;
    }
}

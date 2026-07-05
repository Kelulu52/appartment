package com.atguigu.lease.web.app.service.impl;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.teautil.models.RuntimeOptions;
import com.atguigu.lease.web.app.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SmsServiceImpl implements SmsService {

    @Autowired
    private Client client;

    @Override
    public void sendCode(String phone, String code) {
        SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest()
                .setPhoneNumber(phone)
                .setSignName("速通互联验证码")
                .setTemplateCode("100001")
                .setTemplateParam(String.format("{\"code\":\"%s\",\"min\":\"10\"}", code));

        try {
            SendSmsVerifyCodeResponse response = client.sendSmsVerifyCodeWithOptions(request, new RuntimeOptions());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}

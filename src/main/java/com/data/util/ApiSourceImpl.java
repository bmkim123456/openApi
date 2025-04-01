package com.data.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ApiSourceImpl implements ApiSource {

    @Override
    public String ftcRequestUrl(String requestInfo) {
        return "https://www.ftc.go.kr/www/downloadBizComm.do?atchFileUrl=dataopen&atchFileNm=" + requestInfo;
    };
}

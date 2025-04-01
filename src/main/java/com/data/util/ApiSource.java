package com.data.util;

import org.springframework.http.ResponseEntity;

public interface ApiSource {

    ResponseEntity<byte[]> ftcRequestUrl(String requestInfo);
    String kftcRequestUrl(String requestInfo);
}

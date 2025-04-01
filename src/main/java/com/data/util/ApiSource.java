package com.data.util;

public interface ApiSource {

    String ftcResponse(String city, String district);
    String kftcResponse(String csv);
    String addressApiResponse(String address);
}

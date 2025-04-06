package com.data.util;

import java.util.Optional;

public interface ApiSource {

    String ftcResponse(String city, String district);
    Optional<String> openDataApiResponse(String csv);
    Optional<String> addressApiResponse(String address);
}

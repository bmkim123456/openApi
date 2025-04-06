package com.data.util;

import java.util.Optional;

public interface ExtractData {

    Optional<String> extractEnr(String crn);
    Optional<String> extractAddress(String crn);
    Optional<String> extractDistrictCode(String address);
}

package com.data.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FtcResultDto {

    private String mailOrderNumber;
    private String companyName;
    private String crn;
    private String address;

}

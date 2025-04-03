package com.data.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class DataCompileDto {

    private String mailOrderNumber;
    private String companyName;
    private String crn;
    @Setter
    private String enr;
    @Setter
    private String districtCode;

}

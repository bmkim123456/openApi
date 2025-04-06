package com.data.dto;

import jakarta.annotation.Nullable;
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
    @Nullable
    private String enr;
    @Setter
    @Nullable
    private String districtCode;

}

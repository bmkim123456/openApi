package com.data.entity;

import com.data.dto.DataCompileDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(schema = "public_tmp", name = "api_tmp")
public class ApiDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String mailOrderNumber;
    private String companyName;
    private String crn;
    private String enr;
    private String districtCode;

    public static ApiDataEntity convertDtoToEntity(DataCompileDto dto) {
        return ApiDataEntity.builder()
                .mailOrderNumber(dto.getMailOrderNumber())
                .companyName(dto.getCompanyName())
                .crn(dto.getCrn())
                .enr(dto.getEnr())
                .districtCode(dto.getDistrictCode())
                .build();
    }
}

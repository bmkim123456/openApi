package com.data.entity;

import com.data.dto.DataCompileDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    public static ApiDataEntity apiDataEntity(DataCompileDto dto) {
        return ApiDataEntity.builder()
                .mailOrderNumber(dto.getMailOrderNumber())
                .companyName(dto.getCompanyName())
                .crn(dto.getCrn())
                .enr(dto.getEnr())
                .districtCode(dto.getDistrictCode())
                .build();
    }
}

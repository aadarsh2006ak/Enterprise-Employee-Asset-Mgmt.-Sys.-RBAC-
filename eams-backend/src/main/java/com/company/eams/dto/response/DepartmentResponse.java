package com.company.eams.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Department response payload")
public class DepartmentResponse {

    @Schema(description = "Department unique identifier", example = "1")
    private Long id;

    @Schema(description = "Department name", example = "Engineering")
    private String name;

    @Schema(description = "Manager Employee ID", example = "2")
    private Long managerId;

    @Schema(description = "Manager Full Name", example = "Engineering Manager")
    private String managerName;

    @Schema(description = "Manager Employee Code", example = "EMP-00002")
    private String managerEmployeeCode;

    @Schema(description = "Total number of employees assigned to this department", example = "15")
    private Long employeeCount;
}

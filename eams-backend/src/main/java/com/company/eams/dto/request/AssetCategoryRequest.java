package com.company.eams.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Asset category payload")
public class AssetCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 60, message = "Category name must be between 2 and 60 characters")
    @Schema(description = "Category name", example = "Ergonomic Chair & Desk")
    private String name;
}

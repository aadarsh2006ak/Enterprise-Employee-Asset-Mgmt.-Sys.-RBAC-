package com.company.eams.dto.request;

import com.company.eams.entity.enums.AssetStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Asset return payload")
public class AssetReturnRequest {

    @Schema(description = "Condition notes upon return inspection", example = "Returned in excellent working condition with minor cosmetic scuff on lid")
    private String conditionNotes;

    @Builder.Default
    @Schema(description = "Updated status of the asset after return (defaults to AVAILABLE, or IN_REPAIR, RETIRED)", example = "AVAILABLE")
    private AssetStatus newStatus = AssetStatus.AVAILABLE;
}

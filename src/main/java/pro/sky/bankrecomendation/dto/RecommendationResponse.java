package pro.sky.bankrecomendation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;
import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommendationResponse {
    // Field name EXACTLY as required by the task: user_id
    private UUID user_id;
    private List<RecommendationDto> recommendations;
}

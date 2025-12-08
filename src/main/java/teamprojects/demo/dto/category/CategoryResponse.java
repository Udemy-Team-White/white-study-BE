package teamprojects.demo.dto.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import teamprojects.demo.entity.StudyCategory;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private Integer id;           // 카테고리 ID (예: 1)
    private String name;          // 카테고리 이름 (예: "어학", "코딩")

    // Entity -> DTO 변환을 위한 편의 메서드 (생성자)
    public static CategoryResponse from(StudyCategory entity) {
        return CategoryResponse.builder()
                .id(entity.getId())
                .name(entity.getCategoryName())
                .build();
    }
}
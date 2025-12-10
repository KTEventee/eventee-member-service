package eventee.server.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "내정보 응답 DTO(마이페이지에서 사용됨, 이벤트 목록이랑 같이 제공")
public class MemberResponse {

  @Schema(description = "회원 닉네임", example = "가나디")
  private String nickname;

  @Schema(description = "회원 프로필 이미지 URL")
  private String profileImageUrl;

}

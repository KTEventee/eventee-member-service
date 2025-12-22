package eventee.server.member.converter;

import eventee.server.member.dto.MemberResponse;
import eventee.server.member.model.Member;
import org.springframework.stereotype.Component;

@Component
public class MemberConverter {

  public MemberResponse toResponse(Member member) {
    String profileImageUrl = member.getProfileImageUrl();
    if (profileImageUrl != null && profileImageUrl.isBlank()) {
      profileImageUrl = null;
    }
    return MemberResponse.builder()
        .nickname(member.getNickname())
        .profileImageUrl(profileImageUrl)
        .build();
  }

}

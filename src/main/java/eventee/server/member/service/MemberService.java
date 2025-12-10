package eventee.server.member.service;

import eventee.server.member.dto.MemberResponse;

public interface MemberService {
  String checkAndUpdateNickname(Long memberId, String nickname);
  MemberResponse getMemberInfo(Long memberId);

}

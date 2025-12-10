package eventee.server.member.service;


import eventee.server.member.converter.MemberConverter;
import eventee.server.member.dto.MemberResponse;
import eventee.server.member.exception.MemberHandler;
import eventee.server.member.exception.status.MemberErrorStatus;
import eventee.server.member.model.Member;
import eventee.server.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

  private final MemberRepository memberRepository;
  private final MemberConverter memberConverter;

  // 닉네임 중복 확인 및 변경
  @Override
  @Transactional
  public String checkAndUpdateNickname(Long memberId, String nickname) {
    if (nickname == null || nickname.isBlank()) {
      throw new MemberHandler(MemberErrorStatus.MEMBER_NICKNAME_NULL);
    }

    String trimmed = nickname.trim();
    Member member = verifyMember(memberId);

    // case 1. 닉네임 동일함
    if (trimmed.equals(member.getNickname())) {
      log.info("닉네임 변경 불필요 - 동일한 닉네임 요청 nickname={}, memberId={}", trimmed, member.getId());
      return trimmed;
    }

    // case 2. 닉네임 중복됨
    if (memberRepository.existsByNickname(trimmed)) {
      throw new MemberHandler(MemberErrorStatus.MEMBER_NICKNAME_DUPLICATED);
    }

    member.updateNickname(trimmed);
    memberRepository.save(member);
    return null;
  }

  // 마이페이지 정보 조회 - member 기본 정보만 반환 (이벤트 정보는 반환하지 않음)
  // TODO Event Service 쪽에서 MemberEvent, Event쪽 정보 제공해야됨
  @Override
  @Transactional(readOnly = true)
  public MemberResponse getMemberInfo(Long memberId) {
    Member member = verifyMember(memberId);
    return memberConverter.toResponse(member);
  }

  // Helper Method
  private Member verifyMember(Long memberId){
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new MemberHandler(MemberErrorStatus.MEMBER_NOT_FOUND));
  }

}

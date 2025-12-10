package eventee.server.member.service;

import eventee.server.common.exception.BaseException;
import eventee.server.common.exception.codes.ErrorCode;
import eventee.server.member.dto.MemberAuthContext;
import eventee.server.member.dto.MemberDetails;
import eventee.server.member.model.Member;
import eventee.server.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

/**
 * Spring Security에서 인증된 사용자를 로드하기 위한 서비스.
 * - JWT 인증 과정에서 이메일을 기반으로 MemberDetails를 생성함.
 */
@Service
@RequiredArgsConstructor
public class MemberDetailsService implements UserDetailsService {

  private final MemberRepository memberRepository;

  @Override
  public UserDetails loadUserByUsername(String email) {
    Member member = memberRepository.findMemberByEmail(email)
        .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

    return new MemberDetails(
        MemberAuthContext.of(member),
        member
    );
  }
}

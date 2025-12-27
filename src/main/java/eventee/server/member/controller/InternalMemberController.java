package eventee.server.member.controller;

import eventee.server.member.dto.InternalGoogleLoginRequest;
import eventee.server.member.dto.InternalMemberResponse;
import eventee.server.member.service.MemberService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Hidden
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/members")
public class InternalMemberController {

  private final MemberService memberService;

  @PostMapping("/google")
  public InternalMemberResponse findOrCreateByGoogle(
      @RequestBody InternalGoogleLoginRequest request
  ) {
    return memberService.findOrCreateByGoogle(request);
  }
}

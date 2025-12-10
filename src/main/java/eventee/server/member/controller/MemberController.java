package eventee.server.member.controller;

import eventee.server.common.exception.BaseResponse;
import eventee.server.common.exception.codes.SuccessCode;
import eventee.server.member.dto.MemberResponse;
import eventee.server.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member", description = "회원 관리 및 마이페이지 관련 API")
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/member")
public class MemberController {

  private final MemberService memberService;


  @Operation(
      summary = "닉네임 중복 확인 및 변경",
      description = """
          사용자가 입력한 닉네임의 중복 여부를 확인하고,
          중복이 없으면 해당 닉네임으로 회원 정보를 업데이트합니다.
          """
  )
  @PatchMapping(value = "/nickname", produces = "application/json")
  public BaseResponse<String> checkAndUpdateNickname(
      Long memberId,
      @RequestParam("nickname") @NotBlank String nickname) {

    String updatedNickname = memberService.checkAndUpdateNickname(memberId, nickname);
    return BaseResponse.of(SuccessCode.SUCCESS, updatedNickname);
  }

  //마이페이지
  @Operation(
      summary = "마이페이지 정보 조회",
      description = """
        로그인한 회원의 닉네임, 프로필 이미지를 반환합니다.
        """
  )
  @GetMapping("/mypage")
  public BaseResponse<MemberResponse> getMyPageInfo(Long memberId) {

    MemberResponse response = memberService.getMemberInfo(memberId);
    return BaseResponse.of(SuccessCode.SUCCESS, response);
  }
}

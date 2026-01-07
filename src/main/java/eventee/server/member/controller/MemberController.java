package eventee.server.member.controller;

import eventee.server.common.exception.BaseResponse;
import eventee.server.common.exception.codes.SuccessCode;
import eventee.server.common.jwt.exception.JwtErrorCode;
import eventee.server.common.jwt.exception.JwtHandler;
import eventee.server.member.dto.MemberProfileImageDto;
import eventee.server.member.dto.MemberResponse;
import eventee.server.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member", description = "회원 관리 및 마이페이지 관련 API")
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/members")
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
      HttpServletRequest request,
      @RequestParam("nickname") @NotBlank String nickname,
      Authentication authentication
  ) {
    Long memberId = (Long) authentication.getPrincipal();
    if (memberId == null) {
      throw new JwtHandler(JwtErrorCode.JWT_MISSING_TOKEN);
    }
    String updatedNickname = memberService.checkAndUpdateNickname(memberId, nickname);
    return BaseResponse.of(SuccessCode.SUCCESS, updatedNickname);
  }

  @Operation(
      summary = "마이페이지 정보 조회",
      description = """
          로그인한 회원의 닉네임, 프로필 이미지를 반환합니다.
          """
  )
  @GetMapping("/mypage")
  public BaseResponse<MemberResponse> getMyPageInfo(
          HttpServletRequest request,
          Authentication authentication) {

    Long memberId = (Long) authentication.getPrincipal();
    if (memberId == null) {
      throw new JwtHandler(JwtErrorCode.JWT_MISSING_TOKEN);
    }

    MemberResponse response = memberService.getMemberInfo(memberId);
    return BaseResponse.of(SuccessCode.SUCCESS, response);
  }

  @Operation(
      summary = "프로필 이미지 Presigned URL 발급 (PUT)",
      description = """
          프론트가 S3로 직접 이미지를 업로드할 수 있도록
          Presigned URL을 발급합니다.
          
          1) URL 발급
          2) 프론트에서 S3로 업로드(PUT)
          3) 아래 confirm API 호출 (DB 반영)
          """
  )
  @PostMapping("/presigned-url")
  public BaseResponse<MemberProfileImageDto.PresignedUrlResponse> createPresignedUrl(
      HttpServletRequest request,
      Authentication authentication,
      @Valid @RequestBody MemberProfileImageDto.UploadIntentRequest uploadRequest
  ) {

    Long memberId = (Long) authentication.getPrincipal();
    if (memberId == null) {
      throw new JwtHandler(JwtErrorCode.JWT_MISSING_TOKEN);
    }

    MemberProfileImageDto.PresignedUrlResponse response =
        memberService.createPresignedUrl(memberId, uploadRequest);

    return BaseResponse.of(SuccessCode.SUCCESS, response);
  }

  @Operation(
      summary = "프로필 이미지 업로드 확정",
      description = """
          Presigned URL로 S3 업로드가 완료된 뒤,
          해당 이미지를 회원 프로필에 반영합니다.
          """
  )
  @PostMapping("/confirm")
  public BaseResponse<String> confirmProfileImage(
      HttpServletRequest request,
      Authentication authentication,
      @Valid @RequestBody MemberProfileImageDto.ConfirmUploadRequest confirmRequest
  ) {

    Long memberId = (Long) authentication.getPrincipal();
    if (memberId == null) {
      throw new JwtHandler(JwtErrorCode.JWT_MISSING_TOKEN);
    }

    String imageUrl = memberService.confirmProfileImage(memberId, confirmRequest);
    return BaseResponse.of(SuccessCode.SUCCESS, imageUrl);
  }

  @Operation(
      summary = "프로필 이미지 삭제",
      description = """
          기존 프로필 이미지를 S3에서 삭제하고,
          회원 프로필 정보를 초기화합니다.
          """
  )
  @DeleteMapping
  public BaseResponse<MemberProfileImageDto.DeleteImageResponse> deleteProfileImage(
      HttpServletRequest request,
      Authentication authentication
  ) {

    Long memberId = (Long) authentication.getPrincipal();
    if (memberId == null) {
      throw new JwtHandler(JwtErrorCode.JWT_MISSING_TOKEN);
    }

    MemberProfileImageDto.DeleteImageResponse response =
        memberService.deleteProfileImage(memberId);

    return BaseResponse.of(SuccessCode.SUCCESS, response);
  }
}

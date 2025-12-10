package eventee.server.member.service;

import eventee.server.member.dto.MemberProfileImageDto.ConfirmUploadRequest;
import eventee.server.member.dto.MemberProfileImageDto.DeleteImageResponse;
import eventee.server.member.dto.MemberProfileImageDto.PresignedUrlResponse;
import eventee.server.member.dto.MemberProfileImageDto.UploadIntentRequest;
import eventee.server.member.dto.MemberResponse;
import jakarta.validation.Valid;

public interface MemberService {
  String checkAndUpdateNickname(Long memberId, String nickname);
  MemberResponse getMemberInfo(Long memberId);

  PresignedUrlResponse createPresignedUrl(Long memberId, UploadIntentRequest request);

  String confirmProfileImage(Long memberId, ConfirmUploadRequest request);

  DeleteImageResponse deleteProfileImage(Long memberId);
}

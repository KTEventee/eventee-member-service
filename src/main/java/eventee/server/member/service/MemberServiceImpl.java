package eventee.server.member.service;

import eventee.server.common.aws.S3Props;
import eventee.server.member.converter.MemberConverter;
import eventee.server.member.dto.MemberProfileImageDto.ConfirmUploadRequest;
import eventee.server.member.dto.MemberProfileImageDto.PresignedUrlResponse;
import eventee.server.member.dto.MemberProfileImageDto.UploadIntentRequest;
import eventee.server.member.dto.MemberProfileImageDto.DeleteImageResponse;
import eventee.server.member.dto.MemberResponse;
import eventee.server.member.exception.MemberHandler;
import eventee.server.member.exception.status.MemberErrorStatus;
import eventee.server.member.model.Member;
import eventee.server.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

  private final MemberRepository memberRepository;
  private final MemberConverter memberConverter;
  private final S3Props props;
  private final S3Client s3;
  private final S3Presigner presigner;

  @Override
  @Transactional
  public String checkAndUpdateNickname(Long memberId, String nickname) {
    if (nickname == null || nickname.isBlank()) {
      throw new MemberHandler(MemberErrorStatus.MEMBER_NICKNAME_NULL);
    }

    String trimmed = nickname.trim();
    Member member = verifyMember(memberId);

    if (trimmed.equals(member.getNickname())) {
      return trimmed;
    }

    if (memberRepository.existsByNickname(trimmed)) {
      throw new MemberHandler(MemberErrorStatus.MEMBER_NICKNAME_DUPLICATED);
    }

    member.updateNickname(trimmed);
    return trimmed;
  }

  @Override
  @Transactional(readOnly = true)
  public MemberResponse getMemberInfo(Long memberId) {
    return memberConverter.toResponse(verifyMember(memberId));
  }

  @Override
  @Transactional
  public PresignedUrlResponse createPresignedUrl(Long memberId, UploadIntentRequest request) {

    validateContentType(request.getContentType());
    validateContentLength(request.getContentLength());

    String ext = mapExt(request.getContentType());

    String key = props.getKeyPrefix()
        + "/profiles/" + memberId + "/" + UUID.randomUUID() + ext;

    PresignedPutObjectRequest presigned = presigner.presignPutObject(b -> b
        .signatureDuration(Duration.ofSeconds(300))
        .putObjectRequest(r -> r
            .bucket(props.getBucket())
            .key(key)
            .contentType(request.getContentType()))
    );

    return PresignedUrlResponse.builder()
        .url(presigned.url().toString())
        .key(key)
        .expiresIn(300)
        .build();
  }

  @Override
  @Transactional
  public String confirmProfileImage(Long memberId, ConfirmUploadRequest request) {

    validateContentType(request.getContentType());
    validateContentLength(request.getSize());
    validateExists(request.getKey());

    Member member = verifyMember(memberId);

    String url = buildPublicUrl(request.getKey());
    member.updateProfileImage(request.getKey(), url);

    return url;
  }

  @Override
  @Transactional
  public DeleteImageResponse deleteProfileImage(Long memberId) {

    Member member = verifyMember(memberId);

    if (member.getProfileImageKey() == null) {
      new MemberHandler(MemberErrorStatus.MEMBER_IMAGE_INVALID_KEY);
    }

    String key = member.getProfileImageKey();
    String previousUrl = member.getProfileImageUrl();

    deleteObject(key);
    member.clearProfileImage();

    return DeleteImageResponse.builder()
        .previousUrl(previousUrl)
        .status("deleted")
        .build();
  }

  private Member verifyMember(Long memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new MemberHandler(MemberErrorStatus.MEMBER_NOT_FOUND));
  }

  private void validateExists(String key) {
    try {
      s3.headObject(HeadObjectRequest.builder()
          .bucket(props.getBucket())
          .key(key)
          .build());
    } catch (Exception e) {
      throw new MemberHandler(MemberErrorStatus.MEMBER_IMAGE_NOT_FOUND);
    }
  }

  private void deleteObject(String key) {
    try {
      s3.deleteObject(DeleteObjectRequest.builder()
          .bucket(props.getBucket())
          .key(key)
          .build());
    } catch (Exception ignored) {}
  }

  private void validateContentType(String contentType) {
    boolean allowed = props.getAllowedContentTypes()
        .stream()
        .anyMatch(ct -> ct.equalsIgnoreCase(contentType));

    if (!allowed) {
      throw new MemberHandler(MemberErrorStatus.MEMBER_IMAGE_INVALID_CONTENT_TYPE);
    }
  }

  private void validateContentLength(long size) {
    if (size <= 0 || size > props.getMaxUploadSizeBytes()) {
      throw new MemberHandler(MemberErrorStatus.MEMBER_IMAGE_INVALID_SIZE);
    }
  }

  private String buildPublicUrl(String key) {
    return "https://" + props.getBucket()
        + ".s3." + props.getRegion()
        + ".amazonaws.com/" + key;
  }

  private String mapExt(String contentType) {
    return switch (contentType) {
      case "image/jpeg" -> ".jpg";
      case "image/png" -> ".png";
      case "image/webp" -> ".webp";
      default -> throw new MemberHandler(MemberErrorStatus.MEMBER_IMAGE_INVALID_CONTENT_TYPE);
    };
  }
}

package eventee.server.member.dto;

public record InternalMemberResponse(
    Long memberId,
    boolean isNew
) {}

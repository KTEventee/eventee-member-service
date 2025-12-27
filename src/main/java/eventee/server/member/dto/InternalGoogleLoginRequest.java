package eventee.server.member.dto;


public record InternalGoogleLoginRequest(
    String socialId,
    String email,
    String nickname
) {}

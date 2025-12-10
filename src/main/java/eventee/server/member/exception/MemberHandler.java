package eventee.server.member.exception;

import eventee.server.common.exception.BaseException;
import eventee.server.common.exception.codes.BaseCode;

public class MemberHandler extends BaseException {

  public MemberHandler(BaseCode code) {
    super(code);
  }
}
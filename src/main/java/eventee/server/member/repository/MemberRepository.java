package eventee.server.member.repository;

import eventee.server.member.model.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;



public interface MemberRepository extends JpaRepository<Member,Long>  {
    Optional<Member> findBySocialId(String socialId);
    boolean existsByNickname(String nickname);
}

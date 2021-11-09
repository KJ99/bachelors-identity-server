package pl.kj.bachelors.identity.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.kj.bachelors.identity.domain.model.entity.UserVerification;

import java.util.Optional;

@Repository
public interface UserVerificationRepository extends JpaRepository<UserVerification, String> {

    Optional<UserVerification> findByToken(String token);
    @Query(value = "select * from user_verifications where user_id = :userId order by created_at desc limit 1", nativeQuery = true)
    Optional<UserVerification> findLatestByUserId(@Param("userId") String uid);
}

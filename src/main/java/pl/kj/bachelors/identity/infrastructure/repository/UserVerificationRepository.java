package pl.kj.bachelors.identity.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.kj.bachelors.identity.domain.model.UserVerification;

import java.util.Optional;

@Repository
public interface UserVerificationRepository extends JpaRepository<UserVerification, String> {

    Optional<UserVerification> findByToken(String token);
}

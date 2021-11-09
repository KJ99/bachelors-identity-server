package pl.kj.bachelors.identity.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.kj.bachelors.identity.domain.model.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    @Query(value = "select * from users where username = :name or email = :name", nativeQuery = true)
    Optional<User> findByUserNameOrEmail(@Param("name") String name);

    Optional<User> findByEmail(String email);
}

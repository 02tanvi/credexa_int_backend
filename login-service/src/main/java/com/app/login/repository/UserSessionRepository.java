package com.app.login.repository;

import com.app.login.entity.User;
import com.app.login.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    
    Optional<UserSession> findBySessionToken(String sessionToken);
    
    List<UserSession> findByUserAndActiveTrue(User user);
    
    List<UserSession> findByActiveTrueAndLastActivityBefore(LocalDateTime threshold);
    
    void deleteByUser(User user);
}

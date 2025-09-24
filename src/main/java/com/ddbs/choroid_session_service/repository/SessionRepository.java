package com.ddbs.choroid_session_service.repository;

import com.ddbs.choroid_session_service.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long>{
}

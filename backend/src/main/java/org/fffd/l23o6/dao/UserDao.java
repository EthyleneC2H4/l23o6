package org.fffd.l23o6.dao;

import org.fffd.l23o6.pojo.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserDao extends JpaRepository<UserEntity, Long> {
    UserEntity findByUsername(String username);
}
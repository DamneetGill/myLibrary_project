package com.mylibrary.repository;

import com.mylibrary.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Integer> {
    public User findByUsername(String username);
}

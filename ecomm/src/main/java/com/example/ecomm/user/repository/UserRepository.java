package com.example.ecomm.user.repository;

import com.example.ecomm.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}

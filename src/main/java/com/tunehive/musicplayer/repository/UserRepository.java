package com.tunehive.musicplayer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.tunehive.musicplayer.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {

    User findByMobile(String mobile);
    List<User> findAllByMobile(String mobile);
}
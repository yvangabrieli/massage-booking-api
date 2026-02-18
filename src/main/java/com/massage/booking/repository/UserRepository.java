package com.massage.booking.repository;

import com.massage.booking.entity.User;
import com.massage.booking.entity.valueobject.Phone;
import com.massage.booking.entity.valueobject.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPhone(Phone phone);


    boolean existsByPhone(Phone phone);

    boolean existsByEmail(Email email);


    Optional<User> findByPhoneAndActiveTrue(Phone phone);
}
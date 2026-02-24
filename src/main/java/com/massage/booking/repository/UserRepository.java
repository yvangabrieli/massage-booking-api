package com.massage.booking.repository;

import com.massage.booking.entity.User;
import com.massage.booking.entity.valueobject.Email;
import com.massage.booking.entity.valueobject.Phone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPhone(Phone phone);

    Optional<User> findByEmail(Email email);

    boolean existsByPhone(Phone phone);

    boolean existsByEmail(Email email);

    Optional<User> findByEmailAndActiveTrue(Email email);
}
package org.example.repository;

import org.example.domain.SignUp;

import java.util.List;

public interface SignUpRepo {

    SignUp save(SignUp s);
    List<SignUp> findByClubId(String clubId);
    List<SignUp> findByUserId(String userId);
}

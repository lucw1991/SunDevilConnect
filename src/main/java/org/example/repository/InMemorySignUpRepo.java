package org.example.repository;

import org.example.domain.Registration;
import org.example.domain.SignUp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InMemorySignUpRepo implements SignUpRepo {

    private final Map<String, SignUp> store = new HashMap<>();

    @Override
    public SignUp save(SignUp s) {
        store.put(s.getClubId(), s);
        return s;
    }

    @Override
    public List<SignUp> findByClubId(String clubId) {
        return store.values().stream().filter(r -> r.getClubId()
                        .equals(clubId))
                .collect(Collectors.toList());
    }

    @Override
    public List<SignUp> findByUserId(String userId) {
        return store.values().stream().filter(r -> r.getUserId()
                        .equals(userId))
                .collect(Collectors.toList());
    }
}

package com.example.usersservice.mappers;

import java.util.Objects;

public interface BankMapper<T, S> {
    T map(S dto);
}

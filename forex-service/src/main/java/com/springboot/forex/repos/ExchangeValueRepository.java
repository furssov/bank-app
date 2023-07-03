package com.springboot.forex.repos;

import com.springboot.forex.models.ExchangeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ExchangeValueRepository extends JpaRepository<ExchangeValue, Long> {

    @Query("select ev from ExchangeValue ev where ev.from=?1 and ev.to = ?2")
    ExchangeValue findByFromAndTo(String from, String to);
}

package com.kopacz.TransferService.repository;

import com.kopacz.TransferService.model.Account;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    @Transactional
    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    Optional<Account> findByAccountNumber(Long fromAccount);
}

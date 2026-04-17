package id.ac.ui.cs.advprog.bepromovoucher.repository;

import id.ac.ui.cs.advprog.bepromovoucher.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Voucher v WHERE v.code = :code")
    Optional<Voucher> findByCodeWithLock(String code);

    Optional<Voucher> findByCode(String code);
}
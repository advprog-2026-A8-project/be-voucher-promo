package id.ac.ui.cs.advprog.bepromovoucher.repository;

import id.ac.ui.cs.advprog.bepromovoucher.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
}
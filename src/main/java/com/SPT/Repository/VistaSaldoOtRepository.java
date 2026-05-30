package com.SPT.Repository;

import com.SPT.Model.VistaSaldoOt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VistaSaldoOtRepository extends JpaRepository<VistaSaldoOt, Long> {
}

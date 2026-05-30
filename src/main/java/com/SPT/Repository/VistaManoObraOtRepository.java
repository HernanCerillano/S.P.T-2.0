package com.SPT.Repository;

import com.SPT.Model.VistaManoObraOt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VistaManoObraOtRepository extends JpaRepository<VistaManoObraOt, Long> {
}

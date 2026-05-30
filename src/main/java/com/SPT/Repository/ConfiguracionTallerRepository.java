package com.SPT.Repository;

import com.SPT.Model.ConfiguracionTaller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfiguracionTallerRepository extends JpaRepository<ConfiguracionTaller, Byte> {
}

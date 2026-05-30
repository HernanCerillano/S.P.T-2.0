package com.SPT.Repository;

import com.SPT.Model.ConfiguracionWhatsapp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfiguracionWhatsappRepository extends JpaRepository<ConfiguracionWhatsapp, Byte> {
}

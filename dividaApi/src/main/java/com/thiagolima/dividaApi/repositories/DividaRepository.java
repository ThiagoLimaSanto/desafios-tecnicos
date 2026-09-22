package com.thiagolima.dividaApi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thiagolima.dividaApi.model.Divida;

public interface DividaRepository extends JpaRepository<Divida, Long> {

}

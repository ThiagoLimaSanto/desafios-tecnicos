package com.thiagolima.dividaApi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thiagolima.dividaApi.dto.DividaRequest;
import com.thiagolima.dividaApi.dto.DividaResponse;
import com.thiagolima.dividaApi.service.DividaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/dividas")
@RequiredArgsConstructor
public class DividaController {

    private final DividaService service;

    @PostMapping
    public ResponseEntity<DividaResponse> cadastrarDivida(@Valid @RequestBody DividaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrarDivida(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DividaResponse> consultarDividaPorID(@Valid @PathVariable Long id) {
        return ResponseEntity.ok(service.consultarDividaPorID(id));
    }

    @GetMapping
    public ResponseEntity<List<DividaResponse>> consultarDivida() {
        return ResponseEntity.ok(service.consultarDivida());
    }

    @PutMapping("/{id}")
    public ResponseEntity<DividaResponse> atualizarDivida(@Valid @PathVariable Long id,
            @RequestBody DividaRequest request) {
        return ResponseEntity.ok(service.atualizarDivida(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DividaResponse> deletarDivida(@Valid @PathVariable Long id) {
        return ResponseEntity.ok(service.deletarDivida(id));
    }
}

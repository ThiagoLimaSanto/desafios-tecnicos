package com.thiagolima.dividaApi.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.thiagolima.dividaApi.dto.DividaRequest;
import com.thiagolima.dividaApi.dto.DividaResponse;
import com.thiagolima.dividaApi.exceptions.DividaNaoEncontradaException;
import com.thiagolima.dividaApi.model.Divida;
import com.thiagolima.dividaApi.repositories.DividaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DividaService {

        private final DividaRepository repository;

        public DividaResponse cadastrarDivida(DividaRequest request) {

                Divida divida = new Divida();

                divida.setCpfDevedor(request.cpfDevedor());
                divida.setValorPego(request.valorPego());
                divida.setValorComJuros(request.valorComJuros());
                divida.setValorComDesconto(request.valorComDesconto());

                repository.save(divida);

                return new DividaResponse(divida.getId(), divida.getCpfDevedor(), divida.getValorPego(),
                                divida.getValorComJuros(),
                                divida.getValorComDesconto());
        }

        public DividaResponse consultarDividaPorID(Long id) {
                Divida divida = repository.findById(id)
                                .orElseThrow(() -> new DividaNaoEncontradaException("Divida não encontrada"));

                return new DividaResponse(divida.getId(), divida.getCpfDevedor(), divida.getValorPego(),
                                divida.getValorComJuros(),
                                divida.getValorComDesconto());
        }

        public List<DividaResponse> consultarDivida() {
                return repository.findAll()
                                .stream()
                                .map(divida -> new DividaResponse(divida.getId(), divida.getCpfDevedor(),
                                                divida.getValorPego(),
                                                divida.getValorComJuros(),
                                                divida.getValorComDesconto()))
                                .toList();
        }

        public DividaResponse atualizarDivida(Long id, DividaRequest request) {
                Divida divida = repository.findById(id)
                                .orElseThrow(() -> new DividaNaoEncontradaException("Divida não encontrada"));

                divida.setCpfDevedor(request.cpfDevedor());
                divida.setValorPego(request.valorPego());
                divida.setValorComJuros(request.valorComJuros());
                divida.setValorComDesconto(request.valorComDesconto());

                repository.save(divida);

                return new DividaResponse(divida.getId(), divida.getCpfDevedor(), divida.getValorPego(),
                                divida.getValorComJuros(),
                                divida.getValorComDesconto());
        }

        public DividaResponse deletarDivida(Long id) {
                Divida divida = repository.findById(id)
                                .orElseThrow(() -> new DividaNaoEncontradaException("Divida não encontrada"));

                repository.delete(divida);

                return new DividaResponse(divida.getId(), divida.getCpfDevedor(), divida.getValorPego(),
                                divida.getValorComJuros(),
                                divida.getValorComDesconto());
        }
}

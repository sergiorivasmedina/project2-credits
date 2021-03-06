package com.bootcamp.credit.controllers;

import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.List;

import com.bootcamp.credit.models.Credit;
import com.bootcamp.credit.services.CreditService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = CreditController.class)
public class creditControllerTest {
    @MockBean
    CreditService service;

    @Autowired
    private WebTestClient webclient;
    
    @Test
    public void getAllCredits() {
        Credit credit = new Credit("1","1","soles",100.0,0.0,"tarjeta de credito", 100.0, 0, null,"BCP",null);

        List<Credit> list = new ArrayList<Credit>();
        list.add(credit);
         
        Flux<Credit> transactionTypeFlux = Flux.fromIterable(list);

        Mockito
            .when(service.findAll())
            .thenReturn(transactionTypeFlux);

        webclient.get()
            .uri("/credits")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Credit.class);

            Mockito.verify(service, times(1)).findAll();
    }

    @Test
    public void deleteCredit() {
        Credit credit = new Credit("1","1","soles",100.0,0.0,"tarjeta de credito", 100.0, 0, null,"BCP",null);

        Mockito
            .when(service.findById("1"))
            .thenReturn(Mono.just(credit));

        Mono<Void> voidReturn  = Mono.empty();
        Mockito
            .when(service.delete(credit))
            .thenReturn(voidReturn);

	    webclient.delete()
                .uri("/credit/{creditId}", credit.getIdCredit())
                .exchange()
                .expectStatus().isOk();
    }
}
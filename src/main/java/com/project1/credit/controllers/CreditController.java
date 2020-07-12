package com.project1.credit.controllers;

import com.project1.credit.models.Credit;
import com.project1.credit.repositories.CreditRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT})
public class CreditController {
    
    @Autowired
    private CreditRepository creditRepository;
    
    @GetMapping(value = "/credits")
    public @ResponseBody Flux<Credit> getAllCredits() {
        // list all data in credit collection
        return creditRepository.findAll();
    }

    @PostMapping(value = "/credit/new")
    public Mono<Credit> newCredit(@RequestBody Credit newCredit) {
        // adding a new credit to the collection
        return creditRepository.save(newCredit);
    }

    @PutMapping(value = "/credit/{creditId}")
    public Mono <ResponseEntity<Credit>> updateCredit(@PathVariable(name = "creditId") String creditId, @RequestBody Credit credit) {
        return creditRepository.findById(creditId)
            .flatMap(existingCredit -> {
                return creditRepository.save(credit);
            })
            .map(updateCredit -> new ResponseEntity<>(updateCredit, HttpStatus.OK))
            .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping(value = "/credit/{creditId}")
    public Mono<ResponseEntity<Void>> deleteCreditType(@PathVariable(name = "creditId") String creditId) {
        return creditRepository.findById(creditId)
            .flatMap(existingCredit ->
                creditRepository.delete(existingCredit)
                    .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK))) 
            )
            .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    //Pay credit products
    @PutMapping(value = "/credit/pay/{creditId}/{amount}")
    public Mono<ResponseEntity<Credit>> payProduct(@PathVariable(name = "creditId") String creditId, @PathVariable(name = "amount") Double amount) {
        return creditRepository.findById(creditId)
            .flatMap(existingCredit -> {
                //update available amount and consumed amount
                existingCredit.setAvailableAmount(existingCredit.getAvailableAmount() + amount);
                existingCredit.setConsumedAmount(existingCredit.getConsumedAmount() - amount);
                return creditRepository.save(existingCredit);
            })
            .map(updateCredit -> new ResponseEntity<>(updateCredit, HttpStatus.OK))
            .defaultIfEmpty((new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }

    //Charge credit consumption
    @PutMapping(value = "/credit/charge/{creditId}/{amount}")
    public Mono<ResponseEntity<Credit>> chargeProduct(@PathVariable(name = "creditId") String creditId, @PathVariable(name = "amount") Double amount) {
        return creditRepository.findById(creditId)
            .flatMap(existingCredit -> {
                Double available = existingCredit.getAvailableAmount();
                Double consumed = existingCredit.getConsumedAmount();

                if ((consumed + amount) < existingCredit.getLimit()) {
                    //update available amount and consumed amount
                    existingCredit.setAvailableAmount(available - amount);
                    existingCredit.setConsumedAmount(consumed + amount);
                }
                return creditRepository.save(existingCredit); //debería mandar un mensaje de que el monto no se pudo actualizar
            })
            .map(updateCredit -> new ResponseEntity<>(updateCredit, HttpStatus.OK))
            .defaultIfEmpty((new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }
}
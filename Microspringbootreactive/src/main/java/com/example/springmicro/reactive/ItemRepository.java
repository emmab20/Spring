package com.example.springmicro.reactive;

import reactor.core.publisher.Mono;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ItemRepository extends ReactiveCrudRepository<Item, String> {
    Mono<Item> findByName(String name);
}
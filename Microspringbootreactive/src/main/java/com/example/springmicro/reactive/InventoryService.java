package com.example.springmicro.reactive;

import java.util.stream.Collectors;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.stereotype.Service;


@Service
class InventoryService {

    private ItemRepository itemRepo;
    private CartRepository cartRepo;
    
    public InventoryService(ItemRepository itemRepo, CartRepository cartRepo) {
        this.itemRepo = itemRepo;
        this.cartRepo = cartRepo;
    }

    public Mono<Cart> getCart(String cartId) {
        return this.cartRepo.findById(cartId);
    }

    public Flux<Item> getInventory() {
        return this.itemRepo.findAll();
    }

    Mono<Item> saveItem(Item newItem) {
        return this.itemRepo.save(newItem);
    }

    Mono<Void> deleteItem(String id) {
        return this.itemRepo.deleteById(id);
    }

    Mono<Cart> addItemToCart(String cartId, String itemId) {
        return this.cartRepo.findById(cartId)
            .defaultIfEmpty(new Cart(cartId))
            .flatMap(cart -> cart.getCartItems().stream()
                .filter(cartItem -> cartItem.getItem().getId().equals(itemId))
                .findAny()
                .map(cartItem -> {
                    cartItem.increment();
                    return Mono.just(cart);
                })
                .orElseGet(()->{
                    return this.itemRepo.findById(itemId)
                        .map(item -> new CartItem(item))
                        .map(cartItem -> {
                            cart.getCartItems().add(cartItem);
                            return cart;
                        });
                    }))
                .flatMap(cart -> this.cartRepo.save(cart));
    }

    Mono<Cart> removeOneFromCart(String cartId, String itemId) {
        return this.cartRepo.findById(cartId)
            .defaultIfEmpty(new Cart(cartId))
            .flatMap(cart -> cart.getCartItems().stream()
                .filter(cartItem -> cartItem.getItem().getId().equals(itemId))
                .findAny()
                .map(cartItem -> {
                    cartItem.decrement();
                    return Mono.just(cart);
                }) 
                .orElse(Mono.empty()))
            .map(cart -> new Cart(cart.getId(), cart.getCartItems().stream()
                .filter(cartItem -> cartItem.getQuantity() > 0)
                .collect(Collectors.toList())))
            .flatMap(cart -> this.cartRepo.save(cart));
    }

}

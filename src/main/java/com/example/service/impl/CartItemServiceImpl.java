package com.example.service.impl;

import com.example.exception.ProductNotFoundException;
import com.example.model.CartItem;
import com.example.model.Product;
import com.example.model.Cart;
import com.example.repository.ProductRepository;
import com.example.repository.CartRepository;
import com.example.service.CartItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {
    private final CartRepository sessionRepository;
    private final com.example.repository.CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Override
    public CartItem add(int userId, int productId) {
        Cart cart = sessionRepository.findByUserId(userId).orElseThrow(
                () -> new RuntimeException(String.format("User with id %d not found", userId)));
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new ProductNotFoundException(productId));
        Optional<CartItem> cartItemOpt = cartItemRepository.findByCartAndProductId(cart, productId);

        if (cartItemOpt.isEmpty()) // if product is not in cart yet save new cart item, otherwise increment quantity
            return cartItemRepository.save(new CartItem(cart, product, 1));

        CartItem cartItem = cartItemOpt.get();
        cartItem.setQuantity(cartItem.getQuantity() + 1);
        return cartItemRepository.save(cartItem);
    }

    @Override
    public void remove(int userId, int productId) {
        Cart cart = sessionRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException(
                        "Could not find shopping session with userId=" + userId));
        CartItem cartItem = cartItemRepository.findByCartAndProductId(cart, productId)
                .orElseThrow(ProductNotFoundException::new);

        cartItemRepository.delete(cartItem);
    }
}

package com.nsss.pizzamsmsaorderbackend.service;

import com.nsss.pizzamsmsaorderbackend.model.Order;
import com.nsss.pizzamsmsaorderbackend.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.stream.Stream;

@Service
public class OrderDetailsService {
    @Autowired
    private OrderRepository orderRepository;

    public Order getFile(String id) {
        return orderRepository.findById(id).get();
    }

    public Stream<Order> getAllFiles() {
        return orderRepository.findAll().stream();
    }
}
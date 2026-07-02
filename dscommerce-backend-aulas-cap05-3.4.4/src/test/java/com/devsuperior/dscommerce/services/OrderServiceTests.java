package com.devsuperior.dscommerce.services;


import com.devsuperior.dscommerce.dto.OrderDTO;
import com.devsuperior.dscommerce.entities.Order;
import com.devsuperior.dscommerce.entities.User;
import com.devsuperior.dscommerce.repositories.OrderRepository;
import com.devsuperior.dscommerce.tests.OrderFactory;
import com.devsuperior.dscommerce.tests.UserFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

@ExtendWith(SpringExtension.class)
public class OrderServiceTests {
    //Faz a injeção da classe ProductService para testar
    @InjectMocks
    private OrderService service;

    @Mock
    private OrderRepository repository;

    @Mock
    private AuthService authService;

    private Long existingOrderId, nonExistingId;

    private Order order;
    private OrderDTO orderDTO;
    private User admin, client;

    void setUp(){
        existingOrderId = 1L;
        nonExistingId = 2L;

        admin = UserFactory.createCustomAdminUser(1L,"Jef");
        client = UserFactory.createCustomClientUser(2L,"Bob");

        order = OrderFactory.createOrder(client);

        orderDTO = new OrderDTO(order);

        //Simular os comportamentos
        Mockito.when(repository.findById(existingOrderId)).thenReturn(Optional.of(order));
        Mockito.when(repository.findById(nonExistingId)).thenReturn(Optional.empty());
    }

}

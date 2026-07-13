package com.devsuperior.dscommerce.controllers.it;

import com.devsuperior.dscommerce.dto.OrderDTO;
import com.devsuperior.dscommerce.entities.*;
import com.devsuperior.dscommerce.tests.ProductFactory;
import com.devsuperior.dscommerce.tests.TokenUtil;
import com.devsuperior.dscommerce.tests.UserFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OrderControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenUtil tokenUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private String clientUsername, clientPassword, adminUsername, adminPassword;

    private String clientToken, adminToken, invalidToken;

    private Long existingOrderId, nonExistingOrderId;

    private Order order;

    private OrderDTO orderDTO;

    private User user;

    @BeforeEach
    void setUp() throws Exception {
        clientUsername = "maria@gmail.com";
        clientPassword = "123456";
        adminUsername = "alex@gmail.com";
        adminPassword = "123456";

        existingOrderId = 1L;
        nonExistingOrderId = 100L;

        clientToken = tokenUtil.obtainAccessToken(mockMvc, clientUsername, clientPassword);
        adminToken = tokenUtil.obtainAccessToken(mockMvc, adminUsername, adminPassword);
        invalidToken = adminToken + "xpto"; // Simulates wrong password

        user = UserFactory.createClientUser();
        order = new Order(null, Instant.now(), OrderStatus.WAITING_PAYMENT, user, null);

        Product product = ProductFactory.createProduct();
        OrderItem orderItem = new OrderItem(order, product, 2, 10.0);
        order.getItems().add(orderItem);
    }

    /*Problema 4: Consultar pedido por id

    Implemente os testes de API usando MockMvc para consulta de pedidos por id (método GET do OrderController), considerando os seguintes cenários. Lembre-se de inserir o token no cabeçalho da requisição.
    1.	Busca de pedido por id retorna pedido existente quando logado como admin
    2.	Busca de pedido por id retorna pedido existente quando logado como cliente e o pedido pertence ao usuário
    3.	Busca de pedido por id retorna 403 quando pedido não pertence ao usuário (com perfil de cliente)
    4.	Busca de pedido por id retorna 404 para pedido inexistente quando logado como admin
    5.	Busca de pedido por id retorna 404 para pedido inexistente quando logado como cliente
    6.	Busca de pedido por id retorna 401 quando não logado como admin ou cliente

    * */

    //1.	Busca de pedido por id retorna pedido existente quando logado como admin
    @Test
    public void findByIdShouldReturnOrderDTOWhenIdExistAndAdminLogged() throws Exception {
        ResultActions result = mockMvc
                //No perform vai testar o metódo get do controller e sua url
                .perform(get("/orders/{id}", existingOrderId)
                        //Vai passar o Bearer Token
                        .header("Authorization", "Bearer " + adminToken)
                        //O Tipo da response do método
                        .accept(MediaType.APPLICATION_JSON))
                //Ajuda na verificação quando faz o debbug
                .andDo(MockMvcResultHandlers.print());

        //Verificar o status
        result.andExpect(status().isOk());
        //Verifica o id do pedido cadastrado
        result.andExpect(jsonPath("$.id").value(existingOrderId));
        //Verifica o moment
        result.andExpect(jsonPath("$.moment").value("2022-07-25T13:00:00Z"));
        //Verifica o status do Pedido
        result.andExpect(jsonPath("$.status").value("PAID"));
        //Verifica se o client existe
        result.andExpect(jsonPath("$.client").exists());
        //Verifica se o nome do cliente é Maria Brown
        result.andExpect(jsonPath("$.client.name").value("Maria Brown"));
        //Verifica se o payment existe
        result.andExpect(jsonPath("$.payment").exists());
        //Verifica se o items existe
        result.andExpect(jsonPath("$.items").exists());
        result.andExpect(jsonPath("$.items[1].name").value("Macbook Pro"));
        //Verifica se o total existe
        result.andExpect(jsonPath("$.total").exists());
    }

    //2.	Busca de pedido por id retorna pedido existente quando logado como cliente e o pedido pertence ao usuário
    @Test
    public void findByIdShouldReturnOrderDTOWhenIdExistAndClientLogged() throws Exception {
        ResultActions result = mockMvc
                //No perform vai testar o metódo get do controller e sua url
                .perform(get("/orders/{id}", existingOrderId)
                        //Vai passar o Bearer Token
                        .header("Authorization", "Bearer " + clientToken)
                        //O Tipo da response do método
                        .accept(MediaType.APPLICATION_JSON));

        //Verificar o status
        result.andExpect(status().isOk());
        //Verifica o id do pedido cadastrado
        result.andExpect(jsonPath("$.id").value(existingOrderId));
        //Verifica o moment
        result.andExpect(jsonPath("$.moment").value("2022-07-25T13:00:00Z"));
        //Verifica o status do Pedido
        result.andExpect(jsonPath("$.status").value("PAID"));
        //Verifica se o client existe
        result.andExpect(jsonPath("$.client").exists());
        //Verifica se o nome do cliente é Maria Brown
        result.andExpect(jsonPath("$.client.name").value("Maria Brown"));
        //Verifica se o payment existe
        result.andExpect(jsonPath("$.payment").exists());
        //Verifica se o items existe
        result.andExpect(jsonPath("$.items").exists());
        result.andExpect(jsonPath("$.items[1].name").value("Macbook Pro"));
        //Verifica se o total existe
        result.andExpect(jsonPath("$.total").exists());
    }

    //3.	Busca de pedido por id retorna 403 quando pedido não pertence ao usuário (com perfil de cliente)
    @Test
    public void findByIdShouldReturnForbiddenWhenIdExistAndClientLoggedAndOrderDoesNotBelongUser() throws Exception {
        Long otherOrderId = 2L;
        ResultActions result = mockMvc
                //No perform vai testar o metódo get do controller e sua url
                .perform(get("/orders/{id}", otherOrderId)
                        //Vai passar o Bearer Token
                        .header("Authorization", "Bearer " + clientToken)
                        //O Tipo da response do método
                        .accept(MediaType.APPLICATION_JSON));

        //Verificar o status
        result.andExpect(status().isForbidden());
    }

    //4.	Busca de pedido por id retorna 404 para pedido inexistente quando logado como admin
    @Test
    public void findByIdShouldReturnNotFoundWhenIdDoesNotExistAndAdminLogged() throws Exception{
        ResultActions result = mockMvc
                //No perform vai testar o metódo get do controller e sua url
                .perform(get("/orders/{id}", nonExistingOrderId)
                        //Vai passar o Bearer Token
                        .header("Authorization", "Bearer " + adminToken)
                        //O Tipo da response do método
                        .accept(MediaType.APPLICATION_JSON));

        //Verificar o status
        result.andExpect(status().isNotFound());
    }
    //5.	Busca de pedido por id retorna 404 para pedido inexistente quando logado como cliente
    @Test
    public void findByIdShouldReturnNotFoundWhenIdDoesNotExistAndClientLogged() throws Exception{
        ResultActions result = mockMvc
                //No perform vai testar o metódo get do controller e sua url
                .perform(get("/orders/{id}", nonExistingOrderId)
                        //Vai passar o Bearer Token
                        .header("Authorization", "Bearer " + clientToken)
                        //O Tipo da response do método
                        .accept(MediaType.APPLICATION_JSON));

        //Verificar o status
        result.andExpect(status().isNotFound());
    }

    //6.	Busca de pedido por id retorna 401 quando não logado como admin ou cliente
    @Test
    public void findByIdShouldReturnUnauthorizedWhenIdExistAndInvalidToken() throws Exception{
        ResultActions result = mockMvc
                //No perform vai testar o metódo get do controller e sua url
                .perform(get("/orders/{id}", existingOrderId)
                        //Vai passar o Bearer Token
                        .header("Authorization", "Bearer " + invalidToken)
                        //O Tipo da response do método
                        .accept(MediaType.APPLICATION_JSON));

        //Verificar o status
        result.andExpect(status().isUnauthorized());
    }
}

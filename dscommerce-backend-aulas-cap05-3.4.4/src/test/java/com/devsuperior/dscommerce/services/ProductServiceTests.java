package com.devsuperior.dscommerce.services;

import com.devsuperior.dscommerce.dto.ProductDTO;
import com.devsuperior.dscommerce.dto.ProductMinDTO;
import com.devsuperior.dscommerce.entities.Product;
import com.devsuperior.dscommerce.repositories.ProductRepository;
import com.devsuperior.dscommerce.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscommerce.tests.ProductFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

//Essa anotação faz para não carregar o contexto da aplicação
@ExtendWith(SpringExtension.class)
public class ProductServiceTests {

    //Faz a injeção da classe ProductService para testar
    @InjectMocks
    private ProductService service;

    @Mock
    private ProductRepository repository;

    private long existingProductId, nonExistingProductId;
    private String productName;
    private Product product;
    private PageImpl<Product> page;

    @BeforeEach
    void setUp() throws Exception {
        existingProductId = 1L;
        nonExistingProductId = 2L;

        productName = "Playstation 5";

        product = ProductFactory.createProduct(productName);
        page = new PageImpl<>(List.of(product));


        //Simulando o comportamento do Repository, quando testamos os métodos abaixo:
        Mockito.when(repository.findById(existingProductId)).thenReturn(Optional.of(product));
        /*Problema 1: Consultar produto por id

        Implemente o mock para simular o comportamento do método findById do ProductRepository, de forma que ao chamar ProductRepository.findById passando como argumento um id de produto não existente, deve retornar uma instância do Optional vazia (empty). Na figura 1 (abaixo), é apresentado o teste unitário do serviço ProductService para o findById que retorna uma exceção ResourceNotFoundException quando o id do produto não existir.
        @Test
        public void findByIdShouldReturnResourceNotFoundExceptionWhenIdDoesNotExist() {

            //Espera receber a exceção ResourceNotFoundException
            Assertions.assertThrows(ResourceNotFoundException.class, () -> {
                //Busca o método classe ProductService
                service.findById(nonExistingProductId);
            });
        }

        Figura 1: Exemplo do método findById que retorna resource not found quando id não existir
        * */
        Mockito.when(repository.findById(nonExistingProductId)).thenReturn(Optional.empty());
        /*Problema 2: Consultar produto por nome

        Implemente o mock para o método searchByName do ProductRepository. O método pode receber com argumento qualquer cadeia de caracteres representando um nome e um Pageable e deve retornar um page de Products. Na figura 2 (abaixo), é apresentado o teste unitário findAll, que deve retornar um page de ProductMinDTO no ProductService.


        Figura 2: Método findAll deve retornar page de ProductMinDTO
        @Test
        public void findAllShouldReturnPagedProductMinDTO() {

            Pageable pageable = PageRequest.of(0, 12);
            String name = "Playstation 5";

            Page<ProductMinDTO> result = service.findAll(name, pageable);

            Assertions.assertNotNull(result);
            Assertions.assertEquals(result.getSize(), 1);
            Assertions.assertEquals(result.iterator().next().getName(), productName);
        }
        * */

        Mockito.when(repository.searchByName(any(), (Pageable) any())).thenReturn(page);
    }

    @Test
    public void findByIdShouldReturnProductDTOWhenIdExists() {
        ProductDTO result = service.findById(existingProductId);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getId(), existingProductId);
        Assertions.assertEquals(result.getName(), product.getName());
    }

    @Test
    public void findByIdShouldReturnResourceNotFoundExceptionWhenIdDoesNotExist() {

        //Espera receber a exceção ResourceNotFoundException
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            //Busca o método classe ProductService
            service.findById(nonExistingProductId);
        });
    }

    @Test
    public void findAllShouldReturnPagedProductMinDTO() {

        Pageable pageable = PageRequest.of(0, 12);
        String name = "Playstation 5";

        Page<ProductMinDTO> result = service.findAll(name, pageable);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getSize(), 1);
        Assertions.assertEquals(result.iterator().next().getName(), productName);
    }
}

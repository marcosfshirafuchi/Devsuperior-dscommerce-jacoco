package com.devsuperior.dscommerce.services;

import com.devsuperior.dscommerce.dto.ProductDTO;
import com.devsuperior.dscommerce.dto.ProductMinDTO;
import com.devsuperior.dscommerce.entities.Product;
import com.devsuperior.dscommerce.repositories.ProductRepository;
import com.devsuperior.dscommerce.services.exceptions.DatabaseException;
import com.devsuperior.dscommerce.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscommerce.tests.ProductFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
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

    private long existingProductId, nonExistingProductId, dependentProductId;
    private String productName;
    private Product product;
    private ProductDTO productDTO;
    private PageImpl<Product> page;

    @BeforeEach
    void setUp() throws Exception {
        existingProductId = 1L;
        nonExistingProductId = 2L;
        dependentProductId = 3L;

        productName = "Playstation 5";

        product = ProductFactory.createProduct(productName);
        productDTO = new ProductDTO(product);
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

        //O método save do ProductRepository recebe qualquer coisa(any()) e retorna um Product(product)
        Mockito.when(repository.save(any())).thenReturn(product);

        /*Problema 3: Atualizar produto

        Implemente os testes unitários do método ProductService.update, cobrindo os seguintes cenários. Lembre-se de mockar os métodos do ProductRepository que são usados pelo ProductService.update.
        1.	Atualização de produto atualiza produto para id existente
        2.	Atualização de produto lança exceção ResourceNotFoundException para produto inexistente

        * */
        Mockito.when(repository.getReferenceById(existingProductId)).thenReturn(product);
        Mockito.when(repository.getReferenceById(nonExistingProductId)).thenThrow(new EntityNotFoundException());

        /*Problema 4: Deletar produto

        Implemente os testes unitários para o método ProductSevice.delete, cobrindo os seguintes cenários. Lembre-se de mockar os métodos do ProductRepository que são usados pelo ProductService.delete.
        1.	Deleção de produto deleta produto para id existente
        2.	Deleção de produto lança exceção ResourceNotFoundException para id inexistente
        3.	Deleção de produto lança exceção DatabaseException para id dependente (quando o produto participa de um pedido).

        * */
        Mockito.when(repository.existsById(existingProductId)).thenReturn(true);
        Mockito.when(repository.existsById(dependentProductId)).thenReturn(true);
        Mockito.when(repository.existsById(nonExistingProductId)).thenReturn(false);

        //Faz mocks
        Mockito.doNothing().when(repository).deleteById(existingProductId);
        Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentProductId);
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

    @Test
    public void insertShouldReturnProductDTO() {
        ProductDTO result = service.insert(new ProductDTO(product));

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getId(), product.getId());
    }

    @Test
    public void updateShouldReturnProductDTOWhenIdExists() {
        ProductDTO result = service.update(existingProductId, productDTO);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getId(), existingProductId);
        Assertions.assertEquals(result.getName(), productDTO.getName());
    }

    @Test
    public void updateShouldReturnResourceNotFoundExceptionWhenIdDoesNotExist() {
        //Espera receber a exceção ResourceNotFoundException
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            //Busca o método classe ProductService
            service.update(nonExistingProductId, productDTO);
        });
    }

    @Test
    public void deleteShouldDoNothingWhenIdExists() {
        Assertions.assertDoesNotThrow(()-> {
            service.delete(existingProductId);
        });
    }

    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        //Espera receber a exceção ResourceNotFoundException
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            //Busca o método classe ProductService
            service.delete(nonExistingProductId);
        });
    }

    @Test
    public void deleteShouldThrowDatabaseExceptionWhenIdDoesNotExist() {
        //Espera receber a exceção DatabaseException
        Assertions.assertThrows(DatabaseException.class, () -> {
            //Busca o método classe ProductService
            service.delete(dependentProductId);
        });
    }
}

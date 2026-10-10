package com.gallegos.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import com.gallegos.dto.ProductRequest;
import com.gallegos.entity.Product;
import com.gallegos.exception.ProductNotFoundException;
import com.gallegos.mapper.ProductMapper;
import com.gallegos.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class ProductServiceTest {

    private ProductRepository repository;
    private ProductMapper mapper;
    private ProductService service;

    @BeforeEach
    void setUp() {
        repository = mock(ProductRepository.class);
        mapper = mock(ProductMapper.class);
        service = new ProductService(repository, mapper);
    }

    // INV-01 - Crear producto
    @Test
    void addProductShouldSaveProduct() {

        // DADO
        ProductRequest request = new ProductRequest();
        Product product = new Product();

        when(mapper.toEntity(request)).thenReturn(product);
        when(repository.save(product)).thenReturn(product);

        // CUANDO
        Product result = service.addProduct(request);

        // ENTONCES
        assertEquals(product, result);
        assertNotNull(product.getTimeRegister());

        verify(mapper).toEntity(request);
        verify(repository).save(product);
    }
    

    // INV-02 - Consultar producto existente
    @Test
    void getProductByIdShouldReturnProductWhenExists() {

        // DADO
        Integer id = 7;
        Product product = new Product();

        when(repository.findById(id)).thenReturn(Optional.of(product));

        // CUANDO
        Product result = service.getProductById(id);

        // ENTONCES
        assertEquals(product, result);

        verify(repository).findById(id);
    }

    // INV-03 - Consultar producto inexistente
    @Test
    void getProductByIdShouldThrowWhenProductDoesNotExist() {

        // DADO
        Integer id = 99;

        when(repository.findById(id)).thenReturn(Optional.empty());

        // CUANDO / ENTONCES
        assertThrows(
                ProductNotFoundException.class,
                () -> service.getProductById(id)
        );

        verify(repository).findById(id);
    }

    // INV-04 / INV-05 - Stock suficiente / insuficiente
    @ParameterizedTest
    @CsvSource({"1, true", "0, false"})
    void decreaseStockReportsWhetherStockWasUpdated(int rows, boolean expected) {

        // DADO
        when(repository.decreaseStock(7, 3)).thenReturn(rows);

        // CUANDO
        Boolean result = service.decreaseStock(7, 3);

        // ENTONCES
        assertEquals(expected, result);

        verify(repository).decreaseStock(7, 3);
        verifyNoMoreInteractions(repository);
    }

    // INV-06 - Restaurar stock
    @ParameterizedTest
    @CsvSource({"1, true", "0, false"})
    void increaseStockReportsWhetherStockWasRestored(int rows, boolean expected) {

        // DADO
        when(repository.increaseStock(7, 3)).thenReturn(rows);

        // CUANDO
        Boolean result = service.increaseStock(7, 3);

        // ENTONCES
        assertEquals(expected, result);

        verify(repository).increaseStock(7, 3);
        verifyNoMoreInteractions(repository);
    }

    // INV-07 - Rechazar cantidad inválida
    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {0, -1})
    void invalidQuantitiesNeverModifyStock(Integer quantity) {

        // CUANDO
        Boolean decreaseResult = service.decreaseStock(7, quantity);
        Boolean increaseResult = service.increaseStock(7, quantity);

        // ENTONCES
        assertFalse(decreaseResult);
        assertFalse(increaseResult);

        verifyNoInteractions(repository);
    }
}
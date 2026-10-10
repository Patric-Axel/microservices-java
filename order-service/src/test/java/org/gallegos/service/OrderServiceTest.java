package org.gallegos.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.gallegos.client.InventoryClient;
import org.gallegos.dto.OrderRequest;
import org.gallegos.dto.ProductDTO;
import org.gallegos.entity.Order;
import org.gallegos.entity.OrderHistory;
import org.gallegos.enums.OrderStatus;
import org.gallegos.exception.InvalidOrderStatusException;
import org.gallegos.exception.OrderNotFoundException;
import org.gallegos.exception.StockInsufficienteException;
import org.gallegos.mapper.OrderHistoryMapper;
import org.gallegos.mapper.OrderMapper;
import org.gallegos.repository.OrderHistoryRepository;
import org.gallegos.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;

class OrderServiceTest {
    private OrderRepository orders;
    private OrderHistoryRepository histories;
    private InventoryClient inventory;
    private OrderMapper mapper;
    private OrderHistoryMapper historyMapper;
    private OrderService service;

    @BeforeEach
    void setUp() {
        orders = mock(OrderRepository.class);
        histories = mock(OrderHistoryRepository.class);
        inventory = mock(InventoryClient.class);
        mapper = mock(OrderMapper.class);
        historyMapper = mock(OrderHistoryMapper.class);
        service = new OrderService(orders, histories, inventory, mapper, historyMapper);
    }

    
    // ORD-01 - Crear pedido con stock disponible
    @Test
    void creationConfirmsOrderAfterSavingPendingAndItsHistory() {
        Order order = order(OrderStatus.PENDING);
        OrderRequest request = request();
        prepareCreation(request, order);
        List<OrderStatus> savedStates = captureSavedStates();
        OrderHistory pending = history(order, OrderStatus.PENDING);
        OrderHistory confirmed = history(order, OrderStatus.CONFIRMED);
        when(inventory.decreaseStock(7, 3)).thenReturn(true);
        LocalDateTime before = LocalDateTime.now();

        assertSame(order, service.registerOrder(request));

        assertEquals(37.5, order.getTotal());
        assertTrue(order.getNumOrder().matches("ORD-[0-9A-F]{8}"));
        assertNotNull(order.getTimeRegister());
        assertFalse(order.getTimeRegister().isBefore(before));
        assertFalse(order.getTimeRegister().isAfter(LocalDateTime.now()));
        assertEquals(List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED), savedStates);
        assertEquals(OrderStatus.CONFIRMED, order.getOrderStatus());
        InOrder flow = inOrder(orders, histories, inventory);
        flow.verify(inventory).getProductById(7);
        flow.verify(orders).save(order);
        flow.verify(histories).save(pending);
        flow.verify(inventory).decreaseStock(7, 3);
        flow.verify(orders).save(order);
        flow.verify(histories).save(confirmed);
        flow.verifyNoMoreInteractions();
    }

    // ORD-02 - Crear pedido sin stock disponible
    @Test
    void insufficientStockCancelsOrderAndPreservesBothHistoryEntries() {
        Order order = order(OrderStatus.PENDING);
        OrderRequest request = request();
        prepareCreation(request, order);
        List<OrderStatus> savedStates = captureSavedStates();
        OrderHistory pending = history(order, OrderStatus.PENDING);
        OrderHistory cancelled = history(order, OrderStatus.CANCELLED);
        when(inventory.decreaseStock(7, 3)).thenReturn(false);

        assertThrows(StockInsufficienteException.class, () -> service.registerOrder(request));

        assertEquals(List.of(OrderStatus.PENDING, OrderStatus.CANCELLED), savedStates);
        assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());
        InOrder flow = inOrder(orders, histories, inventory);
        flow.verify(inventory).getProductById(7);
        flow.verify(orders).save(order);
        flow.verify(histories).save(pending);
        flow.verify(inventory).decreaseStock(7, 3);
        flow.verify(orders).save(order);
        flow.verify(histories).save(cancelled);
        flow.verifyNoMoreInteractions();
        verify(historyMapper, never()).toEntity(any(), eq(OrderStatus.CONFIRMED));
    }
    
    //ORD-03 - Consultar pedido existente
    @Test
    void getOrderByIdShouldReturnOrderWhenExists() {
  
        Order order = order(OrderStatus.CONFIRMED);
        when(orders.findById(11))
                .thenReturn(Optional.of(order));

        Order result = service.getOrderById(11);

        assertSame(order, result);
        verify(orders).findById(11);
        verifyNoMoreInteractions(orders);
    }
    
    //ORD-04 - Consultar pedido inexistente
    @Test
    void getOrderByIdShouldThrowWhenOrderDoesNotExist() {

        when(orders.findById(99))
                .thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> service.getOrderById(99)
        );

        verify(orders).findById(99);
        verifyNoMoreInteractions(orders);
    }

    // ORD-05 - Cancelar pedido confirmado
    @Test
    void cancellationRestoresStockBeforeSavingCancelledOrderAndHistory() {
        Order order = order(OrderStatus.CONFIRMED);
        when(orders.findById(11)).thenReturn(Optional.of(order));
        when(inventory.increaseStock(7, 3)).thenAnswer(invocation -> {
            assertEquals(OrderStatus.CONFIRMED, order.getOrderStatus());
            return true;
        });
        List<OrderStatus> savedStates = captureSavedStates();
        OrderHistory cancelled = history(order, OrderStatus.CANCELLED);

        assertSame(order, service.cancelOrder(11));

        assertEquals(List.of(OrderStatus.CANCELLED), savedStates);
        assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());
        InOrder flow = inOrder(orders, inventory, histories);
        flow.verify(orders).findById(11);
        flow.verify(inventory).increaseStock(7, 3);
        flow.verify(orders).save(order);
        flow.verify(histories).save(cancelled);
        flow.verifyNoMoreInteractions();
    }

    // ORD-06 - Rechazar segunda cancelación
    @Test
    void cancelledOrderCannotBeCancelledAgain() {
        Order order = order(OrderStatus.CANCELLED);
        when(orders.findById(11)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStatusException.class, () -> service.cancelOrder(11));

        assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());
        verify(orders, never()).save(any());
        verifyNoInteractions(inventory, histories, historyMapper);
    }

    // ORD-07 - Consultar historial de una orden
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void historyRequiresAnExistingOrder(boolean exists) {
        when(orders.existsById(11)).thenReturn(exists);
        if (exists) {
            OrderHistory entry = new OrderHistory();
            List<OrderHistory> expected = List.of(entry);
            when(histories.findByOrderIdOrder(11)).thenReturn(expected);

            assertSame(expected, service.getHistoryByOrder(11));
            verify(histories).findByOrderIdOrder(11);
        } else {
            assertThrows(OrderNotFoundException.class, () -> service.getHistoryByOrder(11));
            verifyNoInteractions(histories);
        }
    }
    
    // EXTRA - Fallo al restaurar stock
    @Test
    void failedRestorationLeavesOrderConfirmedWithoutCancellationHistory() {
        Order order = order(OrderStatus.CONFIRMED);
        when(orders.findById(11)).thenReturn(Optional.of(order));
        when(inventory.increaseStock(7, 3)).thenReturn(false);

        assertThrows(InvalidOrderStatusException.class, () -> service.cancelOrder(11));

        assertEquals(OrderStatus.CONFIRMED, order.getOrderStatus());
        verify(inventory).increaseStock(7, 3);
        verify(orders, never()).save(any());
        verifyNoInteractions(histories, historyMapper);
    }
    
    

    private void prepareCreation(OrderRequest request, Order order) {
        ProductDTO product = new ProductDTO();
        product.setPrice(12.5);
        when(inventory.getProductById(7)).thenReturn(product);
        when(mapper.toEntity(request, product)).thenReturn(order);
    }

    private List<OrderStatus> captureSavedStates() {
        List<OrderStatus> states = new ArrayList<>();
        when(orders.save(any(Order.class))).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            states.add(saved.getOrderStatus());
            return saved;
        });
        return states;
    }

    private OrderHistory history(Order order, OrderStatus status) {
        OrderHistory entry = new OrderHistory();
        entry.setOrder(order);
        entry.setOrderStatus(status);
        when(historyMapper.toEntity(order, status)).thenReturn(entry);
        return entry;
    }

    private Order order(OrderStatus status) {
        Order order = new Order();
        order.setIdOrder(11);
        order.setIdProduct(7);
        order.setQuantity(3);
        order.setOrderStatus(status);
        return order;
    }

    private OrderRequest request() {
        OrderRequest request = new OrderRequest();
        request.setIdProduct(7);
        request.setQuantity(3);
        request.setNameClient("Cliente");
        return request;
    }
}

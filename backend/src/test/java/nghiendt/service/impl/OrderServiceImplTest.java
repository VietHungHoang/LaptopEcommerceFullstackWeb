package nghiendt.service.impl;

import nghiendt.entity.Order;
import nghiendt.entity.User; // Cần import User vì Order có quan hệ với User
import nghiendt.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepositoryMock;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock // Mock đối tượng User để liên kết với Order
    private User mockUser1;
    
    @Mock
    private User mockUser2;

    private Order order1;
    private Order order2;

    @BeforeEach
    void setUp() {
        // Tạo data mẫu
        order1 = Order.builder()
                .id(1)
                .user(mockUser1)
                .createdat(new Date(System.currentTimeMillis() - 100000)) // Order 1 tạo trước order 2
                .updatedat(new Date(System.currentTimeMillis() - 50000))
                // Không cần tạo order details 
                .build();

        order2 = Order.builder()
                .id(2)
                .user(mockUser2) 
                .createdat(new Date()) // Thời gian tạo mới hơn
                .updatedat(new Date())
                .build();
    }

    @Test
    @DisplayName("findAll - Should return all orders")
    void findAll_shouldReturnAllOrders() {
        // Arrange
        List<Order> expectedOrders = Arrays.asList(order1, order2);
        when(orderRepositoryMock.findAll()).thenReturn(expectedOrders);

        // Act
        List<Order> actualOrders = orderService.findAll();

        // Assert
        assertThat(actualOrders).isNotNull();
        assertThat(actualOrders).hasSize(2);
        assertThat(actualOrders).containsExactly(order1, order2);

        // Verify
        verify(orderRepositoryMock).findAll();
    }

    @Test
    @DisplayName("findAll - Should return empty list when no orders")
    void findAll_shouldReturnEmptyListWhenNoOrders() {
        // Arrange
        when(orderRepositoryMock.findAll()).thenReturn(List.of());

        // Act
        List<Order> actualOrders = orderService.findAll();

        // Assert
        assertThat(actualOrders).isNotNull();
        assertThat(actualOrders).isEmpty();

        // Verify
        verify(orderRepositoryMock).findAll();
    }

    @Test
    @DisplayName("findById - Should return order when ID exists")
    void findById_whenIdExists_shouldReturnOrder() {
        // Arrange
        int existingId = 1;
        when(orderRepositoryMock.findById(existingId)).thenReturn(Optional.of(order1));

        // Act
        Order actualOrder = orderService.findById(existingId);

        // Assert
        assertThat(actualOrder).isNotNull();
        assertThat(actualOrder.getId()).isEqualTo(existingId);
        assertThat(actualOrder.getUser()).isEqualTo(mockUser1);
        assertThat(actualOrder.getCreatedat()).isEqualTo(order1.getCreatedat());
        assertThat(actualOrder).isEqualTo(order1);

        // Verify
        verify(orderRepositoryMock).findById(existingId);
    }

    @Test
    @DisplayName("findById - Should throw NoSuchElementException when ID does not exist (due to .get())")
    void findById_whenIdDoesNotExist_shouldThrowException() {
        // Arrange
        int nonExistentId = 99;
        when(orderRepositoryMock.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> orderService.findById(nonExistentId))
                .withMessageContaining("No value present");

        // Verify
        verify(orderRepositoryMock).findById(nonExistentId);
    }

    @Test
    @DisplayName("create - Should save and return the new order")
    void create_shouldSaveAndReturnNewOrder() {
        // Arrange
        Order newOrder = Order.builder()
                .user(mockUser1) // Gán user
                .build();

        Order savedOrder = Order.builder() // Giả lập đối tượng trả về sau khi lưu
                .id(3) // Có ID mới
                .user(mockUser1)
                .createdat(new Date()) // Giả sử được gán khi lưu
                .updatedat(new Date())
                .build();

        when(orderRepositoryMock.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        Order createdOrder = orderService.create(newOrder);

        // Assert
        assertThat(createdOrder).isNotNull();
        assertThat(createdOrder.getId()).isEqualTo(3);
        assertThat(createdOrder.getUser()).isEqualTo(mockUser1);
        assertThat(createdOrder.getCreatedat()).isNotNull();
        assertThat(createdOrder).isEqualTo(savedOrder);

        // Verify
        verify(orderRepositoryMock).save(newOrder);
    }

    @Test
    @DisplayName("update - Should save and return the updated order")
    void update_shouldSaveAndReturnUpdatedOrder() {
        // Arrange: order1 đã có ID và ngày tạo
        Date originalCreationDate = order1.getCreatedat();
        Date newUpdateDate = new Date();

        Order orderToUpdate = Order.builder()
                .id(1)
                .user(order1.getUser())
                .createdat(originalCreationDate)
                .updatedat(newUpdateDate)
                .build();


        // Mock save trả về chính đối tượng đã update
        when(orderRepositoryMock.save(orderToUpdate)).thenReturn(orderToUpdate);

        // Act
        Order updatedOrder = orderService.update(orderToUpdate);

        // Assert
        assertThat(updatedOrder).isNotNull();
        assertThat(updatedOrder.getId()).isEqualTo(1);
        assertThat(updatedOrder.getUser()).isEqualTo(mockUser1);
        assertThat(updatedOrder.getCreatedat()).isEqualTo(originalCreationDate);
        assertThat(updatedOrder.getUpdatedat()).isEqualTo(newUpdateDate);
        assertThat(updatedOrder).isEqualTo(orderToUpdate);

        // Verify
        verify(orderRepositoryMock).save(orderToUpdate);
    }

    @Test
    @DisplayName("delete - Should call repository deleteById")
    void delete_shouldCallRepositoryDeleteById() {
        // Arrange
        int idToDelete = 1;
        doNothing().when(orderRepositoryMock).deleteById(idToDelete);

        // Act
        orderService.delete(idToDelete);

        // Assert: Không có gì để assert

        // Verify
        verify(orderRepositoryMock, times(1)).deleteById(idToDelete);
    }
}
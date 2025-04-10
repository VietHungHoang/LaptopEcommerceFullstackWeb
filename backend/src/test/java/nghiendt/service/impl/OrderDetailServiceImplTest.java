package nghiendt.service.impl;

import nghiendt.entity.Order;
import nghiendt.entity.OrderDetail;
import nghiendt.entity.Product;
import nghiendt.repository.OrderDetailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class) // Bật Mockito
class OrderDetailServiceImplTest {

    @Mock // Tạo mock cho dependency
    private OrderDetailRepository orderDetailRepositoryMock;

    @InjectMocks // Inject mock vào class cần test
    private OrderDetailServiceImpl orderDetailService;

    private OrderDetail orderDetail1;
    private OrderDetail orderDetail2;

    @BeforeEach
    void setUp() {
        // Tạo dữ liệu test mẫu
        Order mockOrder = mock(Order.class);
        Product mockProduct1 = mock(Product.class);
        Product mockProduct2 = mock(Product.class);

        orderDetail1 = new OrderDetail();
        orderDetail1.setId(1);
        orderDetail1.setOrder(mockOrder);
        orderDetail1.setProduct(mockProduct1);
        orderDetail1.setPrice(100.0);
        orderDetail1.setQuantity(2);

        orderDetail2 = new OrderDetail();
        orderDetail2.setId(2);
        orderDetail2.setOrder(mockOrder);
        orderDetail2.setProduct(mockProduct2);
        orderDetail2.setPrice(50.0);
        orderDetail2.setQuantity(5);
    }

    @Test
    @DisplayName("findAll - Should return all order details")
    void findAll_shouldReturnAllOrderDetails() {
        // Arrange
        List<OrderDetail> expectedDetails = Arrays.asList(orderDetail1, orderDetail2);
        when(orderDetailRepositoryMock.findAll()).thenReturn(expectedDetails);

        // Act
        List<OrderDetail> actualDetails = orderDetailService.findAll();

        // Assert
        assertThat(actualDetails).isNotNull();
        assertThat(actualDetails).hasSize(2);
        assertThat(actualDetails).containsExactly(orderDetail1, orderDetail2);

        // Verify
        verify(orderDetailRepositoryMock).findAll();
    }

    @Test
    @DisplayName("findAll - Should return empty list when no order details")
    void findAll_shouldReturnEmptyListWhenNoDetails() {
        // Arrange
        when(orderDetailRepositoryMock.findAll()).thenReturn(List.of()); // Trả về list rỗng

        // Act
        List<OrderDetail> actualDetails = orderDetailService.findAll();

        // Assert
        assertThat(actualDetails).isNotNull();
        assertThat(actualDetails).isEmpty();

        // Verify
        verify(orderDetailRepositoryMock).findAll();
    }

    @Test
    @DisplayName("findById - Should return order detail when ID exists")
    void findById_whenIdExists_shouldReturnOrderDetail() {
        // Arrange
        int existingId = 1;
        // Repository trả về Optional
        when(orderDetailRepositoryMock.findById(existingId)).thenReturn(Optional.of(orderDetail1));

        // Act
        OrderDetail actualDetail = orderDetailService.findById(existingId);

        // Assert
        assertThat(actualDetail).isNotNull();
        assertThat(actualDetail.getId()).isEqualTo(existingId);
        assertThat(actualDetail).isEqualTo(orderDetail1);

        // Verify
        verify(orderDetailRepositoryMock).findById(existingId);
    }

    @Test
    @DisplayName("findById - Should throw NoSuchElementException when ID does not exist (due to .get())")
    void findById_whenIdDoesNotExist_shouldThrowException() {
        // Arrange
        int nonExistentId = 99;
        // Mock repository trả về Optional rỗng
        when(orderDetailRepositoryMock.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert: Kiểm tra exception do service gọi .get()
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> orderDetailService.findById(nonExistentId))
                .withMessageContaining("No value present"); 

        // Verify: Repository vẫn được gọi
        verify(orderDetailRepositoryMock).findById(nonExistentId);
    }

    @Test
    @DisplayName("create - Should save and return the new order detail")
    void create_shouldSaveAndReturnNewOrderDetail() {
        // Arrange
        OrderDetail newDetail = new OrderDetail();
        newDetail.setPrice(200.0);
        newDetail.setQuantity(1);
        // Giả sử newDetail chưa có ID

        OrderDetail savedDetail = new OrderDetail(); // Giả lập đối tượng trả về sau khi lưu
        savedDetail.setId(3); // Có ID mới
        savedDetail.setPrice(200.0);
        savedDetail.setQuantity(1);

        // Mock phương thức save
        when(orderDetailRepositoryMock.save(any(OrderDetail.class))).thenReturn(savedDetail);

        // Act
        OrderDetail createdDetail = orderDetailService.create(newDetail);

        // Assert
        assertThat(createdDetail).isNotNull();
        assertThat(createdDetail.getId()).isEqualTo(3); // Kiểm tra ID được gán
        assertThat(createdDetail.getPrice()).isEqualTo(200.0);
        assertThat(createdDetail).isEqualTo(savedDetail);

        // Verify
        verify(orderDetailRepositoryMock).save(newDetail);
    }

    @Test
    @DisplayName("update - Should save and return the updated order detail")
    void update_shouldSaveAndReturnUpdatedOrderDetail() {
        // Arrange: orderDetail1 đã có ID
        OrderDetail detailToUpdate = new OrderDetail();
        detailToUpdate.setId(1); // ID của orderDetail1
        detailToUpdate.setPrice(120.0); // Giá mới
        detailToUpdate.setQuantity(3);   // Số lượng mới

        // Mock save trả về chính đối tượng đã update
        when(orderDetailRepositoryMock.save(detailToUpdate)).thenReturn(detailToUpdate);

        // Act
        OrderDetail updatedDetail = orderDetailService.update(detailToUpdate);

        // Assert
        assertThat(updatedDetail).isNotNull();
        assertThat(updatedDetail.getId()).isEqualTo(1);
        assertThat(updatedDetail.getPrice()).isEqualTo(120.0);
        assertThat(updatedDetail.getQuantity()).isEqualTo(3);
        assertThat(updatedDetail).isEqualTo(detailToUpdate);

        // Verify
        verify(orderDetailRepositoryMock).save(detailToUpdate);
    }

    @Test
    @DisplayName("delete - Should call repository deleteById")
    void delete_shouldCallRepositoryDeleteById() {
        // Arrange
        int idToDelete = 1;
        doNothing().when(orderDetailRepositoryMock).deleteById(idToDelete);

        // Act
        orderDetailService.delete(idToDelete);

        // Assert: Không có gì để assert

        // Verify: deleteById được gọi đúng 1 lần với ID chính xác
        verify(orderDetailRepositoryMock, times(1)).deleteById(idToDelete);
    }
}
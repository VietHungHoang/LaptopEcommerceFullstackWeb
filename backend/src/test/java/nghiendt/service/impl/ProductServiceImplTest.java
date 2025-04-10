package nghiendt.service.impl;

import nghiendt.entity.Category;
import nghiendt.entity.Company;
import nghiendt.entity.Product;
import nghiendt.entity.User;
import nghiendt.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManager; 
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock // Mock product repository
    private ProductRepository productRepositoryMock;

    @Mock // Mock EntityManager 
    private EntityManager entityManagerMock;

    @InjectMocks // Inject các mock vào service cần test
    private ProductServiceImpl productService;

    // Mock các entity liên quan để tạo Product mẫu
    @Mock private User mockUser;
    @Mock private Category mockCategory;
    @Mock private Company mockCompany;

    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        // Tạo dữ liệu test mẫu cho Product
        product1 = Product.builder()
                .id(1)
                .name("Laptop Pro X")
                .price(1200.00)
                .quantity(50)
                .discount(10)
                .available(true)
                .description("High-end laptop")
                .image("laptop_pro_x.jpg")
                .createdAt(new Date(System.currentTimeMillis() - 200000))
                .updatedAt(new Date(System.currentTimeMillis() - 100000))
                .user(mockUser)
                .category(mockCategory)
                .company(mockCompany)
                .build();

        product2 = Product.builder()
                .id(2)
                .name("Wireless Mouse G")
                .price(25.50)
                .quantity(200)
                .discount(0)
                .available(true)
                .description("Ergonomic wireless mouse")
                .image("mouse_g.png")
                .createdAt(new Date())
                .updatedAt(new Date())
                .user(mockUser)      // Có thể dùng user khác nếu logic yêu cầu
                .category(mockCategory) // Có thể dùng category khác
                .company(mockCompany)  // Có thể dùng company khác
                .build();
    }

    @Test
    @DisplayName("findAll - Should return all products")
    void findAll_shouldReturnAllProducts() {
        // Arrange
        List<Product> expectedProducts = Arrays.asList(product1, product2);
        when(productRepositoryMock.findAll()).thenReturn(expectedProducts);

        // Act
        List<Product> actualProducts = productService.findAll();

        // Assert
        assertThat(actualProducts).isNotNull();
        assertThat(actualProducts).hasSize(2);
        assertThat(actualProducts).containsExactly(product1, product2);

        // Verify
        verify(productRepositoryMock).findAll();
    }

    @Test
    @DisplayName("findAll - Should return empty list when no products")
    void findAll_shouldReturnEmptyListWhenNoProducts() {
        // Arrange
        when(productRepositoryMock.findAll()).thenReturn(List.of()); // Trả về list rỗng

        // Act
        List<Product> actualProducts = productService.findAll();

        // Assert
        assertThat(actualProducts).isNotNull();
        assertThat(actualProducts).isEmpty();

        // Verify
        verify(productRepositoryMock).findAll();
    }

    @Test
    @DisplayName("findById - Should return product when ID exists")
    void findById_whenIdExists_shouldReturnProduct() {
        // Arrange
        int existingId = 1;
        // Repository trả về Optional
        when(productRepositoryMock.findById(existingId)).thenReturn(Optional.of(product1));

        // Act
        Product actualProduct = productService.findById(existingId);

        // Assert
        assertThat(actualProduct).isNotNull();
        assertThat(actualProduct.getId()).isEqualTo(existingId);
        assertThat(actualProduct.getName()).isEqualTo("Laptop Pro X");
        assertThat(actualProduct).isEqualTo(product1);

        // Verify
        verify(productRepositoryMock).findById(existingId);
    }

    @Test
    @DisplayName("findById - Should throw NoSuchElementException when ID does not exist (due to .get())")
    void findById_whenIdDoesNotExist_shouldThrowException() {
        // Arrange
        int nonExistentId = 99;
        // Mock repository trả về Optional rỗng
        when(productRepositoryMock.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> productService.findById(nonExistentId))
                .withMessageContaining("No value present");

        // Verify
        verify(productRepositoryMock).findById(nonExistentId);
    }

    @Test
    @DisplayName("create - Should save and return the new product")
    void create_shouldSaveAndReturnNewProduct() {
        // Arrange
        Product newProduct = Product.builder()
                .name("Webcam HD 1080p")
                .price(45.00)
                .quantity(100)
                .available(true)
                .user(mockUser)
                .category(mockCategory)
                .company(mockCompany)
                .build();

        Product savedProduct = Product.builder() // Giả lập đối tượng trả về sau khi lưu
                .id(3) // Có ID mới
                .name("Webcam HD 1080p")
                .price(45.00)
                .quantity(100)
                .available(true)
                .user(mockUser)
                .category(mockCategory)
                .company(mockCompany)
                .createdAt(new Date()) // Giả sử được gán khi lưu
                .updatedAt(new Date())
                .build();

        when(productRepositoryMock.save(any(Product.class))).thenReturn(savedProduct);

        // Act
        Product createdProduct = productService.create(newProduct);

        // Assert
        assertThat(createdProduct).isNotNull();
        assertThat(createdProduct.getId()).isEqualTo(3); // Kiểm tra ID được gán
        assertThat(createdProduct.getName()).isEqualTo("Webcam HD 1080p");
        assertThat(createdProduct.getCreatedAt()).isNotNull(); // Kiểm tra ngày tạo
        assertThat(createdProduct).isEqualTo(savedProduct);

        // Verify
        verify(productRepositoryMock).save(newProduct);
    }

    @Test
    @DisplayName("update - Should save and return the updated product")
    void update_shouldSaveAndReturnUpdatedProduct() {
        // Arrange
        Date originalCreationDate = product1.getCreatedAt();
        Date newUpdateDate = new Date();

        Product productToUpdate = Product.builder()
                .id(1) // ID của product1
                .name("Laptop Pro X - Gen 2") // Tên mới
                .price(1250.00) // Giá mới
                .quantity(45) // Số lượng mới
                .available(product1.getAvailable()) // Giữ nguyên
                .description("Updated high-end laptop") // Mô tả mới
                .image(product1.getImage())
                .createdAt(originalCreationDate) // Ngày tạo không đổi
                .updatedAt(newUpdateDate) // Ngày cập nhật mới
                .user(product1.getUser())
                .category(product1.getCategory())
                .company(product1.getCompany())
                .build();


        // Mock save trả về chính đối tượng đã update
        when(productRepositoryMock.save(productToUpdate)).thenReturn(productToUpdate);

        // Act
        Product updatedProduct = productService.update(productToUpdate);

        // Assert
        assertThat(updatedProduct).isNotNull();
        assertThat(updatedProduct.getId()).isEqualTo(1);
        assertThat(updatedProduct.getName()).isEqualTo("Laptop Pro X - Gen 2");
        assertThat(updatedProduct.getPrice()).isEqualTo(1250.00);
        assertThat(updatedProduct.getUpdatedAt()).isEqualTo(newUpdateDate);
        assertThat(updatedProduct.getCreatedAt()).isEqualTo(originalCreationDate);
        assertThat(updatedProduct).isEqualTo(productToUpdate);

        // Verify
        verify(productRepositoryMock).save(productToUpdate);
    }

    @Test
    @DisplayName("delete - Should call repository deleteById")
    void delete_shouldCallRepositoryDeleteById() {
        // Arrange
        int idToDelete = 1;
        // Mock phương thức void deleteById
        doNothing().when(productRepositoryMock).deleteById(idToDelete);

        // Act
        productService.delete(idToDelete);

        // Assert: Không có gì để assert

        // Verify: deleteById được gọi đúng 1 lần với ID chính xác
        verify(productRepositoryMock, times(1)).deleteById(idToDelete);
    }
}
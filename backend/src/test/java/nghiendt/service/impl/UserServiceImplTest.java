package nghiendt.service.impl;

import nghiendt.entity.User;
import nghiendt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Date;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepositoryMock;

    @InjectMocks
    private UserServiceImpl userService;

    private User user1;
    private User user2;

    @BeforeEach 
    void setUp() {
        // Khởi tạo các biến 
        user1 = User.builder()
                .id(1)
                .username("userone")
                .email("userone@example.com")
                .fullname("User One")
                .token("token1")
                .createdat(new Date())
                .build();

        user2 = User.builder()
                .id(2)
                .username("usertwo")
                .email("usertwo@example.com")
                .fullname("User Two")
                .token("token2")
                .createdat(new Date())
                .build();
    }

    @Test
    @DisplayName("findAll - Should return list of all users")
    void findAll_shouldReturnAllUsers() {
        // Arrange
        List<User> users = Arrays.asList(user1, user2);
        when(userRepositoryMock.findAll()).thenReturn(users);

        // Act
        List<User> result = userService.findAll();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(user1, user2); // Kiểm tra nội dung

        // Verify
        verify(userRepositoryMock, times(1)).findAll();
    }

    @Test
    @DisplayName("findAll - Should return empty list when no users")
    void findAll_shouldReturnEmptyListWhenNoUsers() {
        // Arrange
        when(userRepositoryMock.findAll()).thenReturn(Arrays.asList());

        // Act
        List<User> result = userService.findAll();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        // Verify
        verify(userRepositoryMock, times(1) ).findAll();
    }

    @Test
    @DisplayName("findById - Should return user when ID exists")
    void findById_whenIdExists_shouldReturnUser() {
        // Arrange
        int userId = 1;
        when(userRepositoryMock.findById(userId)).thenReturn(Optional.of(user1));

        // Act
        User result = userService.findById(userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getUsername()).isEqualTo("userone");
        assertThat(result).isEqualTo(user1);

        // Verify
        verify(userRepositoryMock).findById(userId);
    }

    @Test
    @DisplayName("findById - Should throw exception when ID does not exist (due to .get())")
    void findById_whenIdDoesNotExist_shouldThrowException() {
        // Arrange
        int nonExistentId = 99;

        when(userRepositoryMock.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatExceptionOfType(java.util.NoSuchElementException.class)
                .isThrownBy(() -> userService.findById(nonExistentId))
                .withMessageContaining("No value present");

        // Verify
        verify(userRepositoryMock).findById(nonExistentId);
    }

    @Test
    @DisplayName("create - Should save and return the user")
    void create_shouldSaveAndReturnUser() {
        // Arrange
        User newUser = User.builder().username("newuser").email("new@example.com").build();
        User savedUser = User.builder().id(3).username("newuser").email("new@example.com").createdat(new Date()).build(); // Simulate ID generation

        when(userRepositoryMock.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.create(newUser);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3);
        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result).isEqualTo(savedUser);

        // Verify
        verify(userRepositoryMock).save(newUser);
    }

    @Test
    @DisplayName("update - Should save and return the updated user")
    void update_shouldSaveAndReturnUpdatedUser() {
        // Arrange
        // User1 đã tồn tại (trong setup)
        User userToUpdate = User.builder()
                               .id(1) 
                               .username("userone_updated") 
                               .email("userone@example.com")
                               .fullname("User One Updated")
                               .token("token1")
                               .createdat(user1.getCreatedat()) 
                               .updatedat(new Date())
                               .build();

        when(userRepositoryMock.save(userToUpdate)).thenReturn(userToUpdate);

        // Act
        User result = userService.update(userToUpdate);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getUsername()).isEqualTo("userone_updated");
        assertThat(result.getFullname()).isEqualTo("User One Updated");
        assertThat(result).isEqualTo(userToUpdate);

        // Verify
        verify(userRepositoryMock).save(userToUpdate);
    }

    @Test
    @DisplayName("delete - Should call repository deleteById")
    void delete_shouldCallRepositoryDeleteById() {
        // Arrange
        int userIdToDelete = 1;

        // Act
        userService.delete(userIdToDelete);

        // Assert: Không có gì để assert

        // Verify: hàm Delete gọi đúng 1 lần
        verify(userRepositoryMock, times(1)).deleteById(userIdToDelete);
    }

    @Test
    @DisplayName("findUsernameByEmail - Should return user when email exists")
    void findUsernameByEmail_whenEmailExists_shouldReturnUser() {
        // Arrange
        String email = "userone@example.com";
        when(userRepositoryMock.findUsernameByEmail(email)).thenReturn(user1);

        // Act
        User result = userService.findUsernameByEmail(email);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(user1);
        assertThat(result.getEmail()).isEqualTo(email);

        // Verify
        verify(userRepositoryMock).findUsernameByEmail(email);
    }

    @Test
    @DisplayName("findUsernameByEmail - Should return null when email does not exist")
    void findUsernameByEmail_whenEmailDoesNotExist_shouldReturnNull() {
        // Arrange
        String nonExistentEmail = "nouser@example.com";
        when(userRepositoryMock.findUsernameByEmail(nonExistentEmail)).thenReturn(null); // Mock returning null

        // Act
        User result = userService.findUsernameByEmail(nonExistentEmail);

        // Assert
        assertThat(result).isNull();

        // Verify
        verify(userRepositoryMock).findUsernameByEmail(nonExistentEmail);
    }

    @Test
    @DisplayName("findByEmail - Should return user when email exists")
    void findByEmail_whenEmailExists_shouldReturnUser() {
        // Arrange
        String email = "userone@example.com";
        when(userRepositoryMock.findByEmail(email)).thenReturn(user1);

        // Act
        User result = userService.findByEmail(email);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(user1);

        // Verify
        verify(userRepositoryMock).findByEmail(email);
    }

    @Test
    @DisplayName("findByEmail - Should return null when email does not exist")
    void findByEmail_whenEmailDoesNotExist_shouldReturnNull() {
        // Arrange
        String nonExistentEmail = "nouser@example.com";
        when(userRepositoryMock.findByEmail(nonExistentEmail)).thenReturn(null);

        // Act
        User result = userService.findByEmail(nonExistentEmail);

        // Assert
        assertThat(result).isNull();

        // Verify
        verify(userRepositoryMock).findByEmail(nonExistentEmail);
    }

    @Test
    @DisplayName("findByToken - Should return user when token exists")
    void findByToken_whenTokenExists_shouldReturnUser() {
        // Arrange
        String token = "token1";
        when(userRepositoryMock.findByToken(token)).thenReturn(user1);

        // Act
        User result = userService.findByToken(token);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(user1);

        // Verify
        verify(userRepositoryMock).findByToken(token);
    }

    @Test
    @DisplayName("findByToken - Should return null when token does not exist")
    void findByToken_whenTokenDoesNotExist_shouldReturnNull() {
        // Arrange
        String nonExistentToken = "invalidtoken";
        when(userRepositoryMock.findByToken(nonExistentToken)).thenReturn(null);

        // Act
        User result = userService.findByToken(nonExistentToken);

        // Assert
        assertThat(result).isNull();

        // Verify
        verify(userRepositoryMock).findByToken(nonExistentToken);
    }
}

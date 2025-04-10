package nghiendt.service.impl;

import nghiendt.dto.UserDTO;
import nghiendt.entity.User;
import nghiendt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;

// --- Static imports ---
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class) // Bật Mockito
class UserDetailsImplTest {

    @Mock 
    private UserRepository userRepositoryMock;

    @Mock
    private PasswordEncoder passwordEncoderMock;

    @InjectMocks 
    private UserDetailsImpl userDetailsImpl;

    @Captor // Bắt đối tượng User được truyền vào userRepository.save()
    private ArgumentCaptor<User> userCaptor;

    private User sampleUser;
    private UserDTO sampleUserDto;
    private String existingUsername;
    private String rawPassword;
    private String encodedPassword;

    @BeforeEach
    void setUp() {
        existingUsername = "testuser";
        rawPassword = "password123";
        encodedPassword = "encodedPassword123"; // Giả lập mật khẩu đã mã hóa

        // Tạo User entity mẫu (kết quả từ repository)
        sampleUser = User.builder()
                .id(1)
                .username(existingUsername)
                .password(encodedPassword)
                .email("test@example.com")
                .fullname("Test User Full")
                .phone("123456789")
                .address("123 Test St")
                .birthday(new Date())
                .createdat(new Date())
                .updatedat(new Date())
                .image("test.jpg")
                .token("testtoken")
                .build();

        // Tạo UserDTO mẫu (dữ liệu đầu vào cho save)
        sampleUserDto = new UserDTO();
        sampleUserDto.setUsername("newuser");
        sampleUserDto.setPassword(rawPassword); // DTO chứa mật khẩu gốc
        sampleUserDto.setEmail("new@example.com");
        sampleUserDto.setFullname("New User Full");
        sampleUserDto.setPhone("987654321");
        sampleUserDto.setAddress("456 New Ave");
        sampleUserDto.setBirthday(new Date());
        sampleUserDto.setCreatedAt(new Date()); // Có thể null nếu DB tự tạo
        sampleUserDto.setUpdatedAt(new Date()); // Có thể null nếu DB tự tạo
        sampleUserDto.setImage("new.png");
        sampleUserDto.setToken("newtoken");
    }

    @Test
    @DisplayName("loadUserByUsername - Should return UserDetails when username exists")
    void loadUserByUsername_whenUsernameExists_shouldReturnUserDetails() {
        // Arrange: Mock repository trả về sampleUser khi tìm theo existingUsername
        when(userRepositoryMock.findByUsername(existingUsername)).thenReturn(sampleUser);

        // Act
        UserDetails userDetails = userDetailsImpl.loadUserByUsername(existingUsername);

        // Assert: Kiểm tra UserDetails trả về
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(existingUsername);
        // UserDetails chứa mật khẩu đã mã hóa từ DB
        assertThat(userDetails.getPassword()).isEqualTo(encodedPassword);
        // Kiểm tra quyền (authorities) - theo implementation là rỗng
        assertThat(userDetails.getAuthorities()).isNotNull();
        assertThat(userDetails.getAuthorities()).isEmpty();

        // Verify: Đảm bảo repository được gọi đúng 1 lần
        verify(userRepositoryMock).findByUsername(existingUsername);
    }

    @Test
    @DisplayName("loadUserByUsername - Should throw UsernameNotFoundException when username does not exist")
    void loadUserByUsername_whenUsernameDoesNotExist_shouldThrowUsernameNotFoundException() {
        // Arrange: Mock repository trả về null
        String nonExistentUsername = "unknownuser";
        when(userRepositoryMock.findByUsername(nonExistentUsername)).thenReturn(null);

        // Act & Assert: Kiểm tra exception được ném ra
        assertThatThrownBy(() -> userDetailsImpl.loadUserByUsername(nonExistentUsername))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found with username: " + nonExistentUsername);

        // Verify
        verify(userRepositoryMock).findByUsername(nonExistentUsername);
    }

    @Test
    @DisplayName("save - Should encode password, save user, and return saved user")
    void save_shouldEncodePasswordSaveAndReturnUser() {
        // Arrange
        // Mock password encoder trả về mật khẩu đã mã hóa khi nhận mật khẩu gốc từ DTO
        when(passwordEncoderMock.encode(rawPassword)).thenReturn(encodedPassword);

        // Tạo đối tượng User giả lập sẽ được trả về sau khi lưu (có thể có ID)
        User savedUser = User.builder()
                .id(5) // Giả lập ID mới
                .username(sampleUserDto.getUsername())
                .password(encodedPassword) // Mật khẩu trả về phải là mã hóa
                .email(sampleUserDto.getEmail())
                .fullname(sampleUserDto.getFullname())
                .phone(sampleUserDto.getPhone())
                .address(sampleUserDto.getAddress())
                .birthday(sampleUserDto.getBirthday())
                .createdat(sampleUserDto.getCreatedAt() != null ? sampleUserDto.getCreatedAt() : new Date()) // Xử lý null
                .updatedat(sampleUserDto.getUpdatedAt() != null ? sampleUserDto.getUpdatedAt() : new Date()) // Xử lý null
                .image(sampleUserDto.getImage())
                .token(sampleUserDto.getToken())
                .build();
        // Mock repository save trả về đối tượng savedUser khi nhận bất kỳ User nào
        when(userRepositoryMock.save(any(User.class))).thenReturn(savedUser);

        // Act: Gọi phương thức save với DTO
        User result = userDetailsImpl.save(sampleUserDto);

        // Assert: Kiểm tra kết quả trả về
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(5); // Kiểm tra ID (nếu giả lập)
        assertThat(result.getPassword()).isEqualTo(encodedPassword); // Mật khẩu phải là mã hóa
        assertThat(result.getUsername()).isEqualTo(sampleUserDto.getUsername());
        assertThat(result).isEqualTo(savedUser);

        // Verify:
        // PasswordEncoder.encode đã được gọi với mật khẩu gốc từ DTO
        verify(passwordEncoderMock).encode(rawPassword);
        // UserRepository.save đã được gọi, bắt đối tượng User được truyền vào
        verify(userRepositoryMock).save(userCaptor.capture());
        User userPassedToSave = userCaptor.getValue();

        // Kiểm tra chi tiết đối tượng User trước khi lưu:
        assertThat(userPassedToSave).isNotNull();
        assertThat(userPassedToSave.getId()).isNull(); // ID phải là null trước khi lưu
        assertThat(userPassedToSave.getUsername()).isEqualTo(sampleUserDto.getUsername());
        assertThat(userPassedToSave.getPassword()).isEqualTo(encodedPassword); // Mật khẩu đã được mã hóa
        assertThat(userPassedToSave.getEmail()).isEqualTo(sampleUserDto.getEmail());
        assertThat(userPassedToSave.getFullname()).isEqualTo(sampleUserDto.getFullname());
        assertThat(userPassedToSave.getBirthday()).isEqualTo(sampleUserDto.getBirthday());
        assertThat(userPassedToSave.getAddress()).isEqualTo(sampleUserDto.getAddress());
        assertThat(userPassedToSave.getImage()).isEqualTo(sampleUserDto.getImage());
        assertThat(userPassedToSave.getToken()).isEqualTo(sampleUserDto.getToken());

    }
}
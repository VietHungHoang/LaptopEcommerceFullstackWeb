package nghiendt.service.impl;

import nghiendt.common.Link;
import nghiendt.dto.MailDTO;
import nghiendt.entity.User;
import nghiendt.service.MailerService;
import nghiendt.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor; 
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Date;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private UserService userServiceMock;

    @Mock
    private MailerService mailerServiceMock;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Captor // Tạo một ArgumentCaptor để bắt đối tượng User khi gọi userService.update
    private ArgumentCaptor<User> userCaptor;

    @Captor // Tạo ArgumentCaptor để bắt đối tượng MailDTO khi gọi mailer.queue
    private ArgumentCaptor<MailDTO> mailCaptor;

    private User sampleUser;
    private String existingToken;
    private String existingEmail;
    private int existingUserId;

    @BeforeEach
    void setUp() {
        existingToken = "valid-token-123";
        existingEmail = "test@example.com";
        existingUserId = 1;

        sampleUser = User.builder()
                .id(existingUserId)
                .username("testuser")
                .email(existingEmail)
                .password("oldPassword") // Mật khẩu cũ
                .token(existingToken) // Token hiện tại
                .createdat(new Date())
                .build();
    }

    @Test
    @DisplayName("findByToken - Should return simplified User when token exists")
    void findByToken_whenTokenExists_shouldReturnSimplifiedUser() {
        // Arrange
        when(userServiceMock.findByToken(existingToken)).thenReturn(sampleUser);

        // Act
        User result = authenticationService.findByToken(existingToken);

        // Assert
        assertThat(result).isNotNull();
        // Service chỉ trả về User chỉ có id và token
        assertThat(result.getId()).isEqualTo(existingUserId);
        assertThat(result.getToken()).isEqualTo(existingToken);
        assertThat(result.getUsername()).isNull(); // Các trường khác phải là null
        assertThat(result.getEmail()).isNull();

        // Verify
        verify(userServiceMock).findByToken(existingToken);
    }

    @Test
    @DisplayName("findByToken - Should return null when token does not exist")
    void findByToken_whenTokenDoesNotExist_shouldReturnNull() {
        // Arrange
        String nonExistentToken = "invalid-token";
        when(userServiceMock.findByToken(nonExistentToken)).thenReturn(null);

        // Act
        User result = authenticationService.findByToken(nonExistentToken);

        // Assert
        assertThat(result).isNull();

        // Verify
        verify(userServiceMock).findByToken(nonExistentToken);
    }

    @Test
    @DisplayName("changePassword - Should update password and clear token when user exists")
    void changePassword_whenUserExists_shouldUpdatePasswordAndClearToken() {
        // Arrange
        User userWithNewPassword = User.builder()
                .id(existingUserId)
                .password("newSecurePassword") // Mật khẩu mới cần đặt
                .build();

        // Mock findById để trả về user hiện tại trong "DB" (có token và mật khẩu cũ)
        when(userServiceMock.findById(existingUserId)).thenReturn(sampleUser);

        // Mock update để mô phỏng việc lưu thành công
        // Dùng ArgumentCaptor để kiểm tra user được truyền vào update
        User updatedUserMockReturn = User.builder() // Đối tượng giả lập được trả về sau khi update
                .id(existingUserId)
                .username(sampleUser.getUsername())
                .email(sampleUser.getEmail())
                .password("newSecurePassword") // Mật khẩu đã đổi
                .token(null)                   // Token đã bị xóa
                .createdat(sampleUser.getCreatedat())
                .updatedat(new Date())
                .build();
        when(userServiceMock.update(any(User.class))).thenReturn(updatedUserMockReturn);


        // Act
        boolean result = authenticationService.changePassword(userWithNewPassword);

        // Assert
        assertThat(result).isTrue(); // Mong đợi trả về true khi thành công

        // Verify
        verify(userServiceMock).findById(existingUserId);

        // Verify
        verify(userServiceMock).update(userCaptor.capture());
        User userPassedToUpdate = userCaptor.getValue();

        // Kiểm tra user được truyền vào update có đúng là đã đổi mật khẩu và xóa token chưa
        assertThat(userPassedToUpdate).isNotNull();
        assertThat(userPassedToUpdate.getId()).isEqualTo(existingUserId);
        assertThat(userPassedToUpdate.getPassword()).isEqualTo("newSecurePassword"); // Mật khẩu mới
        assertThat(userPassedToUpdate.getToken()).isNull(); // Token phải là null
        assertThat(userPassedToUpdate.getUsername()).isEqualTo(sampleUser.getUsername());

    }

    @Test
    @DisplayName("changePassword - Should return false when user update fails")
    void changePassword_whenUserUpdateFails_shouldReturnFalse() {
         // Arrange
         User userWithNewPassword = User.builder().id(existingUserId).password("newPassword").build();

         // Mock findById thành công
         when(userServiceMock.findById(existingUserId)).thenReturn(sampleUser);

         // Mock update thất bại (trả về null)
         when(userServiceMock.update(any(User.class))).thenReturn(null);

         // Act
         boolean result = authenticationService.changePassword(userWithNewPassword);

         // Assert
         assertThat(result).isFalse();

         // Verify findById và update đều được gọi
         verify(userServiceMock).findById(existingUserId);
         verify(userServiceMock).update(any(User.class));
    }

    @Test
    @DisplayName("changePassword - Should throw exception if user not found by findById")
    void changePassword_whenUserNotFound_shouldThrowException() {
        // Arrange
        int nonExistentUserId = 99;
        User userWithNewPassword = User.builder().id(nonExistentUserId).password("newPassword").build();

        // Mock findById ném lỗi
        when(userServiceMock.findById(nonExistentUserId)).thenThrow(new NoSuchElementException("Simulated user not found"));

        // Act & Assert
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> authenticationService.changePassword(userWithNewPassword))
                .withMessageContaining("Simulated user not found");

        // Verify findById được gọi, nhưng update thì không
        verify(userServiceMock).findById(nonExistentUserId);
        verify(userServiceMock, never()).update(any(User.class));
    }


    @Test
    @DisplayName("sendResetMail - Should generate token, update user, queue mail when email exists")
    void sendResetMail_whenEmailExists_shouldGenerateTokenUpdateUserAndQueueMail() {
        // Arrange
        // Mock findByEmail để trả về user
        when(userServiceMock.findByEmail(existingEmail)).thenReturn(sampleUser);
        // Mock findUsernameByEmail (dùng để lấy username cho mail body)
        when(userServiceMock.findUsernameByEmail(existingEmail)).thenReturn(sampleUser);
        // Mock update user
        when(userServiceMock.update(any(User.class))).thenReturn(sampleUser); // Giả sử trả về user đã update
        doNothing().when(mailerServiceMock).queue(any(MailDTO.class));

        // Act
        boolean result = authenticationService.sendResetMail(existingEmail);

        // Assert
        assertThat(result).isTrue();

        // Verify 
        verify(userServiceMock).findByEmail(existingEmail);
        verify(userServiceMock).findUsernameByEmail(existingEmail);

        verify(userServiceMock).update(userCaptor.capture());
        User userPassedToUpdate = userCaptor.getValue();
        assertThat(userPassedToUpdate).isNotNull();
        assertThat(userPassedToUpdate.getId()).isEqualTo(existingUserId);
        assertThat(userPassedToUpdate.getToken()).isNotNull().isNotEmpty(); // Token phải được tạo
        String generatedToken = userPassedToUpdate.getToken(); // Lấy token đã tạo để kiểm tra trong mail

        // bắt mail và kiểm tra nội dung
        verify(mailerServiceMock).queue(mailCaptor.capture());
        MailDTO mailSent = mailCaptor.getValue();
        assertThat(mailSent).isNotNull();
        assertThat(mailSent.getTo()).isEqualTo(existingEmail); // Đúng người nhận
        assertThat(mailSent.getSubject()).isEqualTo("Yêu cầu đặt lại mật khẩu"); // Đúng tiêu đề
        assertThat(mailSent.getBody()).isNotNull().isNotEmpty();
        assertThat(mailSent.getBody()).contains(sampleUser.getUsername()); // Chứa username
        assertThat(mailSent.getBody()).contains(Link.ClientLink.RESET_PASSWORD_URL + "?token=" + generatedToken); // Chứa đúng link reset với token đã tạo

    }

    @Test
    @DisplayName("sendResetMail - Should return false and not send mail when email does not exist")
    void sendResetMail_whenEmailDoesNotExist_shouldReturnFalse() {
        // Arrange
        String nonExistentEmail = "notfound@example.com";
        // Mock findByEmail trả về null
        when(userServiceMock.findByEmail(nonExistentEmail)).thenReturn(null);

        // Act
        boolean result = authenticationService.sendResetMail(nonExistentEmail);

        // Assert
        assertThat(result).isFalse();

        // Verify findByEmail được gọi
        verify(userServiceMock).findByEmail(nonExistentEmail);
        // Verify các phương thức khác không được gọi
        verify(userServiceMock, never()).findUsernameByEmail(anyString());
        verify(userServiceMock, never()).update(any(User.class));
        verify(mailerServiceMock, never()).queue(any(MailDTO.class));
    }
}
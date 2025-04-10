package nghiendt.payload;

import io.jsonwebtoken.ExpiredJwtException;
import nghiendt.service.impl.UserDetailsImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class) // Bật Mockito
class JwtRequestFilterTest {

    @Mock
    private UserDetailsImpl userDetailsImplMock;

    @Mock
    private JwtTokenUtil jwtTokenUtilMock;

    @Mock
    private FilterChain filterChainMock;

    @InjectMocks 
    private JwtRequestFilter jwtRequestFilter;

    // Sử dụng MockHttpServletRequest/Response từ spring-test
    private MockHttpServletRequest mockRequest;
    private MockHttpServletResponse mockResponse;

    private final String TEST_USERNAME = "testuser";
    private final String TEST_TOKEN = "validJwtToken";
    private UserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        mockRequest = new MockHttpServletRequest();
        mockResponse = new MockHttpServletResponse();
        // Tạo mock UserDetails
        mockUserDetails = new User(TEST_USERNAME, "password", new ArrayList<>());
        // Xóa Security Context trước mỗi test
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        // Đảm bảo context được xóa sau mỗi test
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("doFilterInternal - Should set Authentication in SecurityContext when token is valid and context is empty")
    void doFilterInternal_whenTokenIsValidAndContextIsEmpty_shouldSetAuthentication() throws ServletException, IOException {
        // Arrange
        mockRequest.addHeader("Authorization", "Bearer " + TEST_TOKEN);
        when(jwtTokenUtilMock.getUsernameFromToken(TEST_TOKEN)).thenReturn(TEST_USERNAME);
        when(userDetailsImplMock.loadUserByUsername(TEST_USERNAME)).thenReturn(mockUserDetails);
        when(jwtTokenUtilMock.validateToken(TEST_TOKEN, mockUserDetails)).thenReturn(true);

        // Act
        jwtRequestFilter.doFilterInternal(mockRequest, mockResponse, filterChainMock);

        // Assert
        // Kiểm tra SecurityContextHolder có Authentication không
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        // Kiểm tra principal (UserDetails) trong Authentication
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(mockUserDetails);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getCredentials()).isNull(); // Credentials nên là null
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities()).isEqualTo(mockUserDetails.getAuthorities());

        // Verify
        verify(jwtTokenUtilMock).getUsernameFromToken(TEST_TOKEN);
        verify(userDetailsImplMock).loadUserByUsername(TEST_USERNAME);
        verify(jwtTokenUtilMock).validateToken(TEST_TOKEN, mockUserDetails);
        // chain.doFilter luôn được gọi
        verify(filterChainMock).doFilter(mockRequest, mockResponse);
    }

    @Test
    @DisplayName("doFilterInternal - Should NOT set Authentication when token is invalid (validateToken returns false)")
    void doFilterInternal_whenTokenIsInvalid_shouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        mockRequest.addHeader("Authorization", "Bearer " + TEST_TOKEN);
        when(jwtTokenUtilMock.getUsernameFromToken(TEST_TOKEN)).thenReturn(TEST_USERNAME);
        when(userDetailsImplMock.loadUserByUsername(TEST_USERNAME)).thenReturn(mockUserDetails);
        // Giả lập token không hợp lệ
        when(jwtTokenUtilMock.validateToken(TEST_TOKEN, mockUserDetails)).thenReturn(false);

        // Act
        jwtRequestFilter.doFilterInternal(mockRequest, mockResponse, filterChainMock);

        // Assert
        // SecurityContextHolder trống
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        // Verify
        verify(jwtTokenUtilMock).getUsernameFromToken(TEST_TOKEN);
        verify(userDetailsImplMock).loadUserByUsername(TEST_USERNAME);
        verify(jwtTokenUtilMock).validateToken(TEST_TOKEN, mockUserDetails);
        verify(filterChainMock).doFilter(mockRequest, mockResponse); // chain.doFilter được gọi
    }

    @Test
    @DisplayName("doFilterInternal - Should NOT set Authentication when username from token is null (e.g., token expired)")
    void doFilterInternal_whenUsernameFromTokenIsNull_shouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        mockRequest.addHeader("Authorization", "Bearer " + TEST_TOKEN);
        // Giả lập token hết hạn hoặc lỗi khi lấy username
        when(jwtTokenUtilMock.getUsernameFromToken(TEST_TOKEN)).thenThrow(new ExpiredJwtException(null, null, "Expired"));

        // Act
        jwtRequestFilter.doFilterInternal(mockRequest, mockResponse, filterChainMock);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        // Verify
        verify(jwtTokenUtilMock).getUsernameFromToken(TEST_TOKEN);
        // Các bước sau không được gọi
        verify(userDetailsImplMock, never()).loadUserByUsername(anyString());
        verify(jwtTokenUtilMock, never()).validateToken(anyString(), any(UserDetails.class));
        verify(filterChainMock).doFilter(mockRequest, mockResponse);
    }

    @Test
    @DisplayName("doFilterInternal - Should NOT set Authentication when getUsernameFromToken throws IllegalArgumentException")
    void doFilterInternal_whenIllegalArgumentException_shouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        mockRequest.addHeader("Authorization", "Bearer " + TEST_TOKEN);
        when(jwtTokenUtilMock.getUsernameFromToken(TEST_TOKEN)).thenThrow(new IllegalArgumentException("Invalid Token"));

        // Act
        jwtRequestFilter.doFilterInternal(mockRequest, mockResponse, filterChainMock);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        // Verify
        verify(jwtTokenUtilMock).getUsernameFromToken(TEST_TOKEN);
        verify(userDetailsImplMock, never()).loadUserByUsername(anyString());
        verify(jwtTokenUtilMock, never()).validateToken(anyString(), any(UserDetails.class));
        verify(filterChainMock).doFilter(mockRequest, mockResponse);
    }


    @Test
    @DisplayName("doFilterInternal - Should NOT set Authentication when Authorization header is missing")
    void doFilterInternal_whenAuthorizationHeaderIsMissing_shouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange: Không add header "Authorization"

        // Act
        jwtRequestFilter.doFilterInternal(mockRequest, mockResponse, filterChainMock);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        // Verify: Không có tương tác nào với jwtTokenUtil hoặc userDetailsImpl
        verifyNoInteractions(jwtTokenUtilMock);
        verifyNoInteractions(userDetailsImplMock);
        verify(filterChainMock).doFilter(mockRequest, mockResponse); // chain.doFilter vẫn được gọi
    }

    @Test
    @DisplayName("doFilterInternal - Should NOT set Authentication when Authorization header does not start with Bearer")
    void doFilterInternal_whenHeaderDoesNotStartWithBearer_shouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        mockRequest.addHeader("Authorization", "InvalidPrefix " + TEST_TOKEN);

        // Act
        jwtRequestFilter.doFilterInternal(mockRequest, mockResponse, filterChainMock);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        // Verify: Không có tương tác nào với jwtTokenUtil hoặc userDetailsImpl
        verifyNoInteractions(jwtTokenUtilMock);
        verifyNoInteractions(userDetailsImplMock);
        verify(filterChainMock).doFilter(mockRequest, mockResponse);
    }

    @Test
    @DisplayName("doFilterInternal - Should NOT set Authentication when SecurityContext already has Authentication")
    void doFilterInternal_whenContextAlreadyHasAuthentication_shouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        mockRequest.addHeader("Authorization", "Bearer " + TEST_TOKEN);
        when(jwtTokenUtilMock.getUsernameFromToken(TEST_TOKEN)).thenReturn(TEST_USERNAME);
        // Thiết lập Authentication sẵn trong context
        UserDetails existingUserDetails = new User("existingUser", "pass", new ArrayList<>());
        UsernamePasswordAuthenticationToken existingAuth = new UsernamePasswordAuthenticationToken(existingUserDetails, null, existingUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        // Act
        jwtRequestFilter.doFilterInternal(mockRequest, mockResponse, filterChainMock);

        // Assert
        // Authentication trong context phải là cái đã thiết lập ban đầu, không bị ghi đè
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(existingAuth);

        // Verify: chỉ getUsernameFromToken được gọi
        verify(jwtTokenUtilMock).getUsernameFromToken(TEST_TOKEN);
        verify(userDetailsImplMock, never()).loadUserByUsername(anyString());
        verify(jwtTokenUtilMock, never()).validateToken(anyString(), any(UserDetails.class));
        verify(filterChainMock).doFilter(mockRequest, mockResponse);
    }
}
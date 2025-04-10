package nghiendt.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class) // Bật Mockito
class UploadServiceImplTest {

    @Mock
    private ServletContext servletContextMock;

    @Mock
    private MultipartFile multipartFileMock;

    @InjectMocks
    private UploadServiceImpl uploadService;

    @Captor // Bắt đối tượng File được truyền vào transferTo
    private ArgumentCaptor<File> fileCaptor;

    private final String FOLDER_NAME = "images";
    private final String ORIGINAL_FILENAME = "test-image.jpg";
    // Giả lập đường dẫn trả về từ getRealPath
    private final String MOCKED_REAL_PATH = "/var/webapp/assets/" + FOLDER_NAME;
    // Đường dẫn đầy đủ của thư mục mock
    private final String MOCKED_DIR_PATH = MOCKED_REAL_PATH;


    @BeforeEach
    void setUp() {
        // Giả lập getRealPath trả về đường dẫn mong muốn
        when(servletContextMock.getRealPath("/assets/" + FOLDER_NAME)).thenReturn(MOCKED_DIR_PATH);
        // Giả lập getOriginalFilename
        when(multipartFileMock.getOriginalFilename()).thenReturn(ORIGINAL_FILENAME);
    }

    @Test
    @DisplayName("save - Should create directory if not exists, generate unique name, transfer file, and return saved file")
    void save_shouldSaveFileAndReturnFileObject() throws IOException { // Thêm throws IOException vì transferTo có thể ném
        // Arrange
        // Giả lập transferTo thành công (không làm gì cả)
        doNothing().when(multipartFileMock).transferTo(any(File.class));

        // Do không thể mock new File(), dir.exists(), dir.mkdirs() => không kiểm tra trực tiếp việc tạo thư mục

        // Act
        File savedFile = uploadService.save(multipartFileMock, FOLDER_NAME);

        // Assert
        assertThat(savedFile).isNotNull();
        // Kiểm tra đường dẫn của thư mục cha có đúng không
        assertThat(savedFile.getParent()).isEqualTo(MOCKED_DIR_PATH);
        // Kiểm tra tên file: phải khác tên gốc và có đuôi file đúng
        assertThat(savedFile.getName()).isNotEqualTo(ORIGINAL_FILENAME);
        assertThat(savedFile.getName()).endsWith(".jpg");

        // Verify
        // etRealPath đã được gọi
        verify(servletContextMock).getRealPath("/assets/" + FOLDER_NAME);
        // getOriginalFilename đã được gọi (ít nhất 1 lần để tạo tên)
        verify(multipartFileMock, atLeastOnce()).getOriginalFilename();
        // transferTo đã được gọi 1 lần với đối tượng File đã bắt được
        verify(multipartFileMock, times(1)).transferTo(fileCaptor.capture());

        // Kiểm tra thêm về File đã bắt được (đảm bảo nó khớp với file trả về)
        File capturedFile = fileCaptor.getValue();
        assertThat(capturedFile).isNotNull();
        assertThat(capturedFile.getAbsolutePath()).isEqualTo(savedFile.getAbsolutePath());
        assertThat(capturedFile.getName()).endsWith(".jpg");
    }

    @Test
    @DisplayName("save - Should handle filename with no extension")
    void save_filenameWithNoExtension_shouldGenerateNameWithoutExtension() throws IOException {
        // Arrange
        String filenameNoExt = "image-no-extension";
        when(multipartFileMock.getOriginalFilename()).thenReturn(filenameNoExt);
        doNothing().when(multipartFileMock).transferTo(any(File.class));

        // Act
        File savedFile = uploadService.save(multipartFileMock, FOLDER_NAME);

        // Assert
        assertThat(savedFile).isNotNull();
        assertThat(savedFile.getParent()).isEqualTo(MOCKED_DIR_PATH);
        // Tên file không nên chứa dấu "." nếu tên gốc không có
        assertThat(savedFile.getName()).doesNotContain(".");
        assertThat(savedFile.getName()).isNotEqualTo(filenameNoExt);

        // Verify
        verify(servletContextMock).getRealPath("/assets/" + FOLDER_NAME);
        verify(multipartFileMock, atLeastOnce()).getOriginalFilename();
        verify(multipartFileMock).transferTo(fileCaptor.capture());
        assertThat(fileCaptor.getValue().getName()).doesNotContain(".");
    }

     @Test
     @DisplayName("save - Should handle filename starting with dot (hidden file)")
     void save_filenameStartingWithDot_shouldGenerateNameWithExtension() throws IOException {
         // Arrange
         String hiddenFilename = ".configfile.txt";
         when(multipartFileMock.getOriginalFilename()).thenReturn(hiddenFilename);
         doNothing().when(multipartFileMock).transferTo(any(File.class));

         // Act
         File savedFile = uploadService.save(multipartFileMock, FOLDER_NAME);

         // Assert
         assertThat(savedFile).isNotNull();
         assertThat(savedFile.getParent()).isEqualTo(MOCKED_DIR_PATH);
         assertThat(savedFile.getName()).isNotEqualTo(hiddenFilename);
         // Vẫn phải giữ lại đuôi file .txt
         assertThat(savedFile.getName()).endsWith(".txt");

         // Verify
         verify(servletContextMock).getRealPath("/assets/" + FOLDER_NAME);
         verify(multipartFileMock, atLeastOnce()).getOriginalFilename();
         verify(multipartFileMock).transferTo(fileCaptor.capture());
         assertThat(fileCaptor.getValue().getName()).endsWith(".txt");
     }

    @Test
    @DisplayName("save - Should throw RuntimeException when transferTo fails")
    void save_whenTransferToFails_shouldThrowRuntimeException() throws IOException {
        // Arrange
        // Giả lập transferTo ném ra IOException
        IOException ioException = new IOException("Disk full simulation");
        doThrow(ioException).when(multipartFileMock).transferTo(any(File.class));

        // Act & Assert
        // Kiểm tra xem RuntimeException có được ném ra không
        assertThatThrownBy(() -> uploadService.save(multipartFileMock, FOLDER_NAME))
                .isInstanceOf(RuntimeException.class)
                .hasCause(ioException); // Kiểm tra nguyên nhân gốc là IOException đã giả lập

        // Verify
        // getRealPath và getOriginalFilename vẫn được gọi
        verify(servletContextMock).getRealPath("/assets/" + FOLDER_NAME);
        verify(multipartFileMock, atLeastOnce()).getOriginalFilename();
        // transferTo được gọi (và đã ném exception)
        verify(multipartFileMock).transferTo(any(File.class));
    }
}
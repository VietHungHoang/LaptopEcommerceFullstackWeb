package nghiendt.service.impl;

import nghiendt.dto.MailDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import javax.mail.internet.MimeMessage;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MailerServiceImplTest {

    @Mock
    private JavaMailSender javaMailSenderMock; 

    // Sử dụng @Spy thay vì @InjectMocks để có thể kiểm tra/mock các lời gọi nội bộ và vẫn sử dụng được implementation thật của các phương thức khác.
    @Spy
    @InjectMocks // Vẫn inject javaMailSenderMock vào spy
    private MailerServiceImpl mailerService;

    @Captor // Bắt đối số MimeMessagePreparator khi gọi javaMailSenderMock.send()
    private ArgumentCaptor<MimeMessagePreparator> mmpCaptor;

    private MailDTO sampleMail;

    @BeforeEach
    void setUp() {
        sampleMail = new MailDTO("test@example.com", "Test Subject", "Test Body");
        // Xóa queue trước mỗi test để đảm bảo độc lập
        mailerService.queue.clear();
    }

    @Test
    @DisplayName("send(MailDTO) - Should prepare MimeMessage and call JavaMailSender.send")
    void send_mailDto_shouldPrepareAndCallSenderSend() throws Exception { // Thêm throws Exception vì mmp.prepare có thể ném lỗi
        // Arrange
        // Mock MimeMessage để tránh NullPointerException bên trong lambda của MimeMessagePreparator
        MimeMessage mimeMessageMock = mock(MimeMessage.class);

        // Act
        mailerService.send(sampleMail);

        // Assert & Verify
        // javaMailSenderMock.send() được gọi đúng 1 lần với một MimeMessagePreparator
        verify(javaMailSenderMock, times(1)).send(mmpCaptor.capture());

        // Kiểm tra logic bên trong MimeMessagePreparator lambda
        //  Lấy MimeMessagePreparator đã bắt được
        MimeMessagePreparator capturedMmp = mmpCaptor.getValue();
        //  Thực thi phương thức prepare của nó với mimeMessageMock
        try {
            capturedMmp.prepare(mimeMessageMock);
        } catch (Exception e) {
            fail("MimeMessagePreparator lambda failed with exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("send(String...) - Should create MailDTO and call send(MailDTO)")
    void send_stringParams_shouldCreateDtoAndCallSendMailDto() {
        // Arrange
        String to = "string@example.com";
        String subject = "String Subject";
        String body = "String Body";

        doNothing().when(mailerService).send(any(MailDTO.class));

        // Act
        mailerService.send(to, subject, body);

        // Assert & Verify
        ArgumentCaptor<MailDTO> mailDtoCaptor = ArgumentCaptor.forClass(MailDTO.class);
        verify(mailerService, times(1)).send(mailDtoCaptor.capture());

        // Kiểm tra xem MailDTO được tạo ra có đúng thông tin không
        MailDTO capturedDto = mailDtoCaptor.getValue();
        assertThat(capturedDto).isNotNull();
        assertThat(capturedDto.getTo()).isEqualTo(to);
        assertThat(capturedDto.getSubject()).isEqualTo(subject);
        assertThat(capturedDto.getBody()).isEqualTo(body);
    }

    @Test
    @DisplayName("queue(MailDTO) - Should add mail to the internal queue")
    void queue_mailDto_shouldAddMailToQueue() {
        // Arrange
        assertThat(mailerService.queue).isEmpty();

        // Act
        mailerService.queue(sampleMail);

        // Assert
        assertThat(mailerService.queue).hasSize(1);
        assertThat(mailerService.queue.peek()).isEqualTo(sampleMail); // Kiểm tra phần tử đầu tiên
    }

    @Test
    @DisplayName("queue(String...) - Should create MailDTO and add to queue")
    void queue_stringParams_shouldCreateDtoAndAddToQueue() {
        // Arrange
        String to = "queue.string@example.com";
        String subject = "Queue Subject";
        String body = "Queue Body";
        assertThat(mailerService.queue).isEmpty();

        // Act
        mailerService.queue(to, subject, body);

        // Assert
        assertThat(mailerService.queue).hasSize(1);
        MailDTO queuedMail = mailerService.queue.peek();
        assertThat(queuedMail).isNotNull();
        assertThat(queuedMail.getTo()).isEqualTo(to);
        assertThat(queuedMail.getSubject()).isEqualTo(subject);
        assertThat(queuedMail.getBody()).isEqualTo(body);
    }

    @Test
    @DisplayName("run - Should send all mails from queue and empty it")
    void run_whenQueueHasItems_shouldSendAllMailsAndEmptyQueue() {
        // Arrange
        MailDTO mail1 = new MailDTO("q1@example.com", "Q1", "B1");
        MailDTO mail2 = new MailDTO("q2@example.com", "Q2", "B2");

        // Thêm mail vào queue
        mailerService.queue(mail1);
        mailerService.queue(mail2);
        assertThat(mailerService.queue).hasSize(2);

        // Mock phương thức send(MailDTO) nội bộ để tránh gửi mail thật
        doNothing().when(mailerService).send(any(MailDTO.class));

        // Act
        mailerService.run(); 

        // Assert & Verify
        // Send(MailDTO) nội bộ được gọi đúng 2 lần
        verify(mailerService, times(2)).send(any(MailDTO.class));

        // Queue đã trống sau khi chạy
        assertThat(mailerService.queue).isEmpty();

        // javaMailSenderMock.send() không được gọi TRỰC TIẾP từ run()
        verify(javaMailSenderMock, never()).send(any(MimeMessagePreparator.class));
    }

    @Test
    @DisplayName("run - Should do nothing when queue is empty")
    void run_whenQueueIsEmpty_shouldDoNothing() {
        // Arrange
        assertThat(mailerService.queue).isEmpty(); // Đảm bảo queue trống

        // Mock lời gọi nội bộ
        doNothing().when(mailerService).send(any(MailDTO.class));

        // Act
        mailerService.run();

        // Assert & Verify
        // send(MailDTO) nội bộ không bao giờ được gọi
        verify(mailerService, never()).send(any(MailDTO.class));
        // javaMailSenderMock.send() không bao giờ được gọi
        verify(javaMailSenderMock, never()).send(any(MimeMessagePreparator.class));
        // Queue vẫn trống
        assertThat(mailerService.queue).isEmpty();
    }
}
package nghiendt.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;

class DateUtilsTest {

    private DateUtils dateUtils;

    @BeforeEach
    void setUp() {
        dateUtils = new DateUtils();
    }

    private Date createSpecificDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        // Reset hết các trường về 0 trước khi set để đảm bảo chỉ có ngày tháng năm đúng
        cal.clear();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1); // Calendar.MONTH bắt đầu từ 0
        cal.set(Calendar.DAY_OF_MONTH, day);
        return cal.getTime();
    }

    // Không test parseDate và parseTimestamp vì là private

    @Test
    @DisplayName("formatDate - Should format Date to yyyy-MM-dd string")
    void formatDate_validDate_shouldReturnCorrectString() {
        // Arrange
        Date dateToFormat = createSpecificDate(2024, 2, 29);
        String expectedFormat = "2024-02-29";

        // Act
        String actualFormat = DateUtils.formatDate(dateToFormat); // Gọi trực tiếp static method

        // Assert
        assertThat(actualFormat).isEqualTo(expectedFormat);
    }

    @Test
    @DisplayName("formatDate - Should format Date with single digit month/day correctly")
    void formatDate_singleDigitMonthDay_shouldReturnPaddedString() {
        Date dateToFormat = createSpecificDate(2023, 5, 7);
        String expectedFormat = "2023-05-07"; // Kiểm tra padding số 0

        String actualFormat = DateUtils.formatDate(dateToFormat);

        assertThat(actualFormat).isEqualTo(expectedFormat);
    }

    @Test
    @DisplayName("formatDate - Should throw NullPointerException for null input")
    void formatDate_nullDate_shouldThrowNullPointerException() {
        // Arrange
        Date nullDate = null;

        // Act & Assert
        assertThatThrownBy(() -> DateUtils.formatDate(nullDate))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("formatDateTwo - Should format Date to dd-MM-yyyy string")
    void formatDateTwo_validDate_shouldReturnCorrectString() {
        // Arrange
        Date dateToFormat = createSpecificDate(2023, 12, 5);
        String expectedFormat = "05-12-2023"; // Kiểm tra padding và thứ tự

        // Act
        String actualFormat = DateUtils.formatDateTwo(dateToFormat);

        // Assert
        assertThat(actualFormat).isEqualTo(expectedFormat);
    }

     @Test
    @DisplayName("formatDateTwo - Should format Date with single digit month/day correctly")
    void formatDateTwo_singleDigitMonthDay_shouldReturnPaddedString() {
        Date dateToFormat = createSpecificDate(2024, 1, 9);
        String expectedFormat = "09-01-2024";

        String actualFormat = DateUtils.formatDateTwo(dateToFormat);

        assertThat(actualFormat).isEqualTo(expectedFormat);
    }

    @Test
    @DisplayName("formatDateTwo - Should throw NullPointerException for null input")
    void formatDateTwo_nullDate_shouldThrowNullPointerException() {
        // Arrange
        Date nullDate = null;

        // Act & Assert
        assertThatThrownBy(() -> DateUtils.formatDateTwo(nullDate))
                .isInstanceOf(NullPointerException.class);
    }
}
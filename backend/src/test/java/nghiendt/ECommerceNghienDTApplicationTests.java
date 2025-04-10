package nghiendt;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ECommerceNghienDTApplicationTests {

    @Test
    void contextLoads() {
    }

    /*
        Không test Controller ở Unit Test vì Unit test thuần không kiểm tra được các cơ chế của Spring MVC như request mapping (@GetMapping, @PostMapping), binding dữ liệu (@RequestBody, @PathVariable), chuyển đổi JSON, xử lý exception, ...
        => Giải pháp: Sử dụng @WebMvcTest ở Intergration Test
     */ 

     /*
        Không test Model vì model chỉ có getter/setter đơn giản, không có phương thức nào đặc biệt, không ghi đè equals() và hashCode(). Validation Logic sẽ test gián tiếp khi test Controller.
     */ 

     /*
        Không test Repository do nó thường là các interface. Mock một interface không thể kiểm tra được query. 
        => Giải pháp: Sử dụng @DataJpaTest ở Intergration Test
     */ 

}

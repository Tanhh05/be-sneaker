package com.sneakerstore.config;

import com.sneakerstore.repository.SalesOrderRepository;
import com.sneakerstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        try {
            List<String> defaultUsers = List.of("admin", "manager", "staff", "staff2", "locked_user");
            for (String username : defaultUsers) {
                userRepository.findByUsername(username).ifPresent(user -> {
                    if (!passwordEncoder.matches("123456", user.getPasswordHash())) {
                        user.setPasswordHash(passwordEncoder.encode("123456"));
                        userRepository.save(user);
                        log.info("Đã đồng bộ mật khẩu mặc định (123456) cho tài khoản: {}", username);
                    }
                });
            }

            // Đồng bộ ngày của các đơn hàng mẫu HD0001..HD0004 theo giờ hệ thống hiện tại để Dashboard hiển thị chuẩn Doanh thu hôm nay
            LocalDateTime now = LocalDateTime.now();
            jdbcTemplate.update("UPDATE SALES_ORDERS SET ORDER_DATE = ? WHERE ORDER_CODE IN ('HD0001', 'HD0002')",
                    Timestamp.valueOf(now.minusHours(1)));
            jdbcTemplate.update("UPDATE SALES_ORDERS SET ORDER_DATE = ? WHERE ORDER_CODE IN ('HD0003', 'HD0004')",
                    Timestamp.valueOf(now.minusMinutes(25)));
            jdbcTemplate.update("UPDATE SALES_ORDERS SET ORDER_DATE = ? WHERE ORDER_CODE = 'HD0005'",
                    Timestamp.valueOf(now.minusDays(2)));
            jdbcTemplate.update("UPDATE SALES_ORDERS SET ORDER_DATE = ? WHERE ORDER_CODE = 'HD0006'",
                    Timestamp.valueOf(now.minusDays(5)));
            jdbcTemplate.update("UPDATE SALES_ORDERS SET ORDER_DATE = ? WHERE ORDER_CODE = 'HD0007'",
                    Timestamp.valueOf(now.minusDays(3)));
            jdbcTemplate.update("UPDATE SALES_ORDERS SET ORDER_DATE = ? WHERE ORDER_CODE = 'HD0008'",
                    Timestamp.valueOf(now.minusMonths(1)));
        } catch (Exception ex) {
            log.warn("Không thể đồng bộ dữ liệu mẫu khi khởi động: {}", ex.getMessage());
        }
    }
}

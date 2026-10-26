package com.eusleats.eusleats.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Creates the notifications table the first time the back end starts.
 * It only ADDS the table when it is missing; existing tables and data are
 * never touched. (The same SQL is in /database/notifications.sql.)
 */
@Component
public class NotificationSchemaInitializer implements CommandLineRunner {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationSchemaInitializer.class);

    private static final String CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS notifications (
                notification_id INT AUTO_INCREMENT PRIMARY KEY,
                recipient_type  VARCHAR(20)  NOT NULL,
                recipient_id    INT          NOT NULL,
                event_type      VARCHAR(40)  NOT NULL,
                message         VARCHAR(500) NOT NULL,
                order_id        INT          NULL,
                is_read         TINYINT(1)   NOT NULL DEFAULT 0,
                created_at      DATETIME     NOT NULL,
                INDEX idx_notifications_recipient (recipient_type, recipient_id, is_read),
                UNIQUE KEY uq_notifications_event
                    (order_id, recipient_type, recipient_id, event_type)
            )
            """;

    private final DataSource dataSource;

    public NotificationSchemaInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute(CREATE_TABLE);

        } catch (Exception e) {
            log.error("Could not create the notifications table: {}. "
                    + "Run database/notifications.sql manually.", e.getMessage());
        }
    }
}

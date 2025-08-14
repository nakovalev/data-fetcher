package io.github.kovalev.datafetcher;

import io.github.kovalev.datafetcher.configuration.TestConfig;
import io.github.kovalev.datafetcher.domain.Comment;
import io.github.kovalev.datafetcher.domain.ComparableEntity;
import io.github.kovalev.datafetcher.domain.OrderItem;
import io.github.kovalev.datafetcher.domain.Post;
import io.github.kovalev.datafetcher.domain.User;
import io.github.kovalev.datafetcher.services.DataFetcher;
import io.github.kovalev.datafetcher.testutils.CommentGenerator;
import io.github.kovalev.datafetcher.testutils.OrderGenerator;
import io.github.kovalev.datafetcher.testutils.OrderItemGenerator;
import io.github.kovalev.datafetcher.testutils.PostGenerator;
import io.github.kovalev.datafetcher.testutils.ProductGenerator;
import io.github.kovalev.datafetcher.testutils.TransactionalExecutor;
import io.github.kovalev.datafetcher.testutils.UserGenerator;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.UUID;

@Slf4j
@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({TestApplication.class, TestConfig.class})
@TestPropertySource("classpath:application.properties")
public abstract class DatabaseTest {

    private static final String[] tableNames = {
            "users",
            "comments",
            "posts",
            "comparable_entity",
            "temporal_entity",
            "orders",
            "products",
            "order_items"
    };
    @ServiceConnection
    protected static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.2");
    @Autowired
    protected JdbcTemplate jdbcTemplate;
    @Autowired
    protected EntityManager entityManager;
    @Autowired
    protected DataFetcher<User, UUID> userDataFetcher;
    @Autowired
    protected DataFetcher<Post, UUID> postDataFetcher;
    @Autowired
    protected DataFetcher<Comment, UUID> commentDataFetcher;
    @Autowired
    protected DataFetcher<OrderItem, Long> orderItemFetcher;
    @Autowired
    protected DataFetcher<ComparableEntity, Long> comparableDataFetcher;
    @Autowired
    protected UserGenerator userGenerator;
    @Autowired
    protected PostGenerator postGenerator;
    @Autowired
    protected CommentGenerator commentGenerator;
    @Autowired
    protected ProductGenerator productGenerator;
    @Autowired
    protected OrderGenerator orderGenerator;
    @Autowired
    protected OrderItemGenerator orderItemGenerator;
    @Autowired
    protected TransactionalExecutor transactionalExecutor;

    @BeforeEach
    public void clearDb() {
        log.info("Starting database cleanup...");
        transactionalExecutor.executeWithInNewTransaction(
                () -> {
                    jdbcTemplate.execute("SET session_replication_role = 'replica'");
                    jdbcTemplate.execute("TRUNCATE TABLE " + String.join(", ", tableNames) + " CASCADE");
                    jdbcTemplate.execute("SET session_replication_role = 'origin'");
                }
        );
        log.info("Database cleanup completed successfully!");
    }
}

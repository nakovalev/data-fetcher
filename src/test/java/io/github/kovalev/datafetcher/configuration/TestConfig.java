package io.github.kovalev.datafetcher.configuration;

import io.github.kovalev.datafetcher.domain.Comment;
import io.github.kovalev.datafetcher.domain.OrderItem;
import io.github.kovalev.datafetcher.domain.Post;
import io.github.kovalev.datafetcher.domain.User;
import io.github.kovalev.datafetcher.services.DataFetcher;
import io.github.kovalev.datafetcher.services.EntityGraphFactory;
import io.github.kovalev.datafetcher.testutils.OrderGenerator;
import io.github.kovalev.datafetcher.testutils.OrderItemGenerator;
import io.github.kovalev.datafetcher.testutils.PostGenerator;
import io.github.kovalev.datafetcher.testutils.ProductGenerator;
import io.github.kovalev.datafetcher.testutils.TransactionalExecutor;
import io.github.kovalev.datafetcher.testutils.UserGenerator;
import jakarta.persistence.EntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Random;
import java.util.UUID;

@TestConfiguration
public class TestConfig {

    @Bean
    TransactionalExecutor transactionalExecutor() {
        return new TransactionalExecutor();
    }

    @Bean
    EntityGraphFactory entityGraphBuilder(EntityManager entityManager) {
        return new EntityGraphFactory(entityManager);
    }

    @Bean
    DataFetcher<User, UUID> userDataFetcher(EntityManager entityManager,
                                            EntityGraphFactory entityGraphFactory) {
        return new DataFetcher<>(entityManager, User.class, User::getId, entityGraphFactory);
    }

    @Bean
    DataFetcher<Post, UUID> postDataFetcher(EntityManager entityManager,
                                            EntityGraphFactory entityGraphFactory) {
        return new DataFetcher<>(entityManager, Post.class, Post::getId, entityGraphFactory);
    }

    @Bean
    DataFetcher<Comment, UUID> commentsDataFetcher(EntityManager entityManager,
                                                   EntityGraphFactory entityGraphFactory) {
        return new DataFetcher<>(entityManager, Comment.class, Comment::getId, entityGraphFactory);
    }

    @Bean
    public DataFetcher<OrderItem, Long> orderItemFetcher(EntityManager entityManager,
                                                         EntityGraphFactory entityGraphFactory) {
        return new DataFetcher<>(entityManager, OrderItem.class, OrderItem::getId, entityGraphFactory);
    }

    @Bean
    Random random() {
        return new Random();
    }

    @Bean
    UserGenerator userGenerator(Random random) {
        return new UserGenerator(random);
    }

    @Bean
    PostGenerator postGenerator(Random random) {
        return new PostGenerator(random);
    }

    @Bean
    ProductGenerator productGenerator(Random random) {
        return new ProductGenerator(random);
    }

    @Bean
    OrderGenerator orderGenerator(Random random) {
        return new OrderGenerator(random);
    }

    @Bean
    OrderItemGenerator orderItemGenerator(Random random) {
        return new OrderItemGenerator(random);
    }
}

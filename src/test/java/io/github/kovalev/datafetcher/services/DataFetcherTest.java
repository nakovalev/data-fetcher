package io.github.kovalev.datafetcher.services;

import io.github.kovalev.datafetcher.DatabaseTest;
import io.github.kovalev.datafetcher.domain.Order;
import io.github.kovalev.datafetcher.domain.OrderItem;
import io.github.kovalev.datafetcher.domain.OrderItem_;
import io.github.kovalev.datafetcher.domain.Post;
import io.github.kovalev.datafetcher.domain.Product;
import io.github.kovalev.datafetcher.domain.Product_;
import io.github.kovalev.datafetcher.domain.User;
import io.github.kovalev.datafetcher.domain.User_;
import io.github.kovalev.datafetcher.specifications.And;
import io.github.kovalev.datafetcher.specifications.Empty;
import io.github.kovalev.datafetcher.specifications.Equal;
import io.github.kovalev.datafetcher.specifications.Include;
import io.github.kovalev.datafetcher.utils.FunctionParams;
import io.github.kovalev.datafetcher.utils.GroupParam;
import io.github.kovalev.datafetcher.utils.AttributeNode;
import org.hibernate.LazyInitializationException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DataFetcherTest extends DatabaseTest {

    @Test
    void oneById() {
        User user = userGenerator.one();
        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(user));

        Optional<User> result = userDataFetcher.one(user.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(user.getId());

        Optional<User> emptyResult = userDataFetcher.one(UUID.randomUUID());
        assertThat(emptyResult).isEmpty();
    }

    @Test
    void oneByIdWithEntityGraphName() {
        User user = userGenerator.one();
        transactionalExecutor.executeWithInNewTransaction(() -> {
            entityManager.persist(user);
            entityManager.persist(postGenerator.one(user));
        });

        Optional<User> result = userDataFetcher.one(user.getId(), User.USER_WITH_POSTS);

        assertThat(result).isPresent();
        User fetchedUser = result.get();
        assertThat(fetchedUser.getId()).isEqualTo(user.getId());

        assertThat(fetchedUser.getPosts()).isNotNull();

        assertThatNoException().isThrownBy(() -> {
            List<Post> posts = fetchedUser.getPosts();
            assertThat(posts).isNotEmpty();
            Post firstPost = posts.getFirst();
            assertThat(firstPost.getTitle()).isNotNull();
        });
    }

    @Test
    void oneByIdWithoutEntityGraphShouldThrowLazyInit() {
        User user = userGenerator.one();
        transactionalExecutor.executeWithInNewTransaction(() -> {
            entityManager.persist(user);
            entityManager.persist(postGenerator.one(user));
        });

        Optional<User> result = userDataFetcher.one(user.getId(), "");

        assertThat(result).isPresent();
        User fetchedUser = result.get();
        assertThat(fetchedUser.getId()).isEqualTo(user.getId());

        List<Post> posts = fetchedUser.getPosts();
        assertThatThrownBy(posts::size)
                .isInstanceOf(LazyInitializationException.class)
                .hasMessageContaining("could not initialize proxy");
    }

    @Test
    void oneByIdWithEntityGraphAsListFields() {
        User user = userGenerator.one();
        transactionalExecutor.executeWithInNewTransaction(() -> {
            entityManager.persist(user);
            entityManager.persist(postGenerator.one(user));
        });

        Optional<User> result = userDataFetcher.one(user.getId(), List.of(
                new AttributeNode(User_.POSTS, null)
        ));

        assertThat(result).isPresent();
        User fetchedUser = result.get();
        assertThat(fetchedUser.getId()).isEqualTo(user.getId());

        assertThat(fetchedUser.getPosts()).isNotNull();

        assertThatNoException().isThrownBy(() -> {
            List<Post> posts = fetchedUser.getPosts();
            assertThat(posts).isNotEmpty();
            Post firstPost = posts.getFirst();
            assertThat(firstPost.getTitle()).isNotNull();
        });
    }

    @Test
    void oneByIdWithEntityGraphAsInvalidListFields() {
        User user = userGenerator.one();
        transactionalExecutor.executeWithInNewTransaction(() -> {
            entityManager.persist(user);
            entityManager.persist(postGenerator.one(user));
        });

        Optional<User> result = userDataFetcher.one(user.getId(), List.of(
                new AttributeNode("Invalid field", null)
        ));

        assertThat(result).isPresent();
        User fetchedUser = result.get();
        assertThat(fetchedUser.getId()).isEqualTo(user.getId());

        List<Post> posts = fetchedUser.getPosts();
        assertThatThrownBy(posts::size)
                .isInstanceOf(LazyInitializationException.class)
                .hasMessageContaining("could not initialize proxy");
    }

    @Test
    void oneBySpecification() {
        List<User> users = transactionalExecutor.executeWithInNewTransaction(() ->
                userGenerator.list(4).stream()
                        .peek(entityManager::persist)
                        .toList());

        User user = users.getFirst();

        Optional<User> result = userDataFetcher.one(new And<>(
                new Equal<>(user.getId(), User_.ID),
                new Equal<>(user.getUsername(), User_.USERNAME),
                new Equal<>(user.getEmail(), User_.EMAIL)
        ));

        assertThat(result).contains(user);
    }

    @Test
    void oneBySpecificationWithEntityGraph() {
        List<User> users = transactionalExecutor.executeWithInNewTransaction(() ->
                userGenerator.list(4).stream()
                        .peek(entityManager::persist)
                        .toList());

        User user = users.getFirst();

        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(postGenerator.one(user)));

        Optional<User> result = userDataFetcher.one(new And<>(
                new Equal<>(user.getId(), User_.ID),
                new Equal<>(user.getUsername(), User_.USERNAME),
                new Equal<>(user.getEmail(), User_.EMAIL)
        ), User.USER_WITH_POSTS);

        assertThat(result).contains(user);

        User fetchedUser = result.get();
        assertThat(fetchedUser.getPosts()).isNotNull();

        assertThatNoException().isThrownBy(() -> {
            List<Post> posts = fetchedUser.getPosts();
            assertThat(posts).isNotEmpty();
            Post firstPost = posts.getFirst();
            assertThat(firstPost.getTitle()).isNotNull();
        });
    }

    @Test
    void oneBySpecificationWithoutEntityGraph() {
        List<User> users = transactionalExecutor.executeWithInNewTransaction(() ->
                userGenerator.list(4).stream()
                        .peek(entityManager::persist)
                        .toList());

        User user = users.getFirst();

        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(postGenerator.one(user)));

        Optional<User> result = userDataFetcher.one(new And<>(
                new Equal<>(user.getId(), User_.ID),
                new Equal<>(user.getUsername(), User_.USERNAME),
                new Equal<>(user.getEmail(), User_.EMAIL)
        ), "");

        assertThat(result).contains(user);
        List<Post> posts = result.get().getPosts();
        assertThatThrownBy(posts::size)
                .isInstanceOf(LazyInitializationException.class)
                .hasMessageContaining("could not initialize proxy");
    }

    @Test
    void oneBySpecificationWithEntityGraphAsListFields() {
        List<User> users = transactionalExecutor.executeWithInNewTransaction(() ->
                userGenerator.list(4).stream()
                        .peek(entityManager::persist)
                        .toList());

        User user = users.getFirst();

        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(postGenerator.one(user)));

        Optional<User> result = userDataFetcher.one(new And<>(
                new Equal<>(user.getId(), User_.ID),
                new Equal<>(user.getUsername(), User_.USERNAME),
                new Equal<>(user.getEmail(), User_.EMAIL)
        ), List.of(
                new AttributeNode(User_.POSTS, null)
        ));

        assertThat(result).contains(user);

        User fetchedUser = result.get();
        assertThat(fetchedUser.getPosts()).isNotNull();

        assertThatNoException().isThrownBy(() -> {
            List<Post> posts = fetchedUser.getPosts();
            assertThat(posts).isNotEmpty();
            Post firstPost = posts.getFirst();
            assertThat(firstPost.getTitle()).isNotNull();
        });
    }

    @Test
    void oneBySpecificationWithoutEntityGraphAsListWithInvalidFields() {
        List<User> users = transactionalExecutor.executeWithInNewTransaction(() ->
                userGenerator.list(4).stream()
                        .peek(entityManager::persist)
                        .toList());

        User user = users.getFirst();

        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(postGenerator.one(user)));

        Optional<User> result = userDataFetcher.one(new And<>(
                new Equal<>(user.getId(), User_.ID),
                new Equal<>(user.getUsername(), User_.USERNAME),
                new Equal<>(user.getEmail(), User_.EMAIL)
        ), List.of(
                new AttributeNode("Invalid field", null)
        ));

        assertThat(result).contains(user);
        List<Post> posts = result.get().getPosts();
        assertThatThrownBy(posts::size)
                .isInstanceOf(LazyInitializationException.class)
                .hasMessageContaining("could not initialize proxy");
    }

    @Test
    void fetchAllIdsWithSpecification() {
        List<UUID> userIds = transactionalExecutor.executeWithInNewTransaction(() ->
                userGenerator.list(10).stream()
                        .peek(entityManager::persist)
                        .map(User::getId)
                        .toList()
                        .subList(0, 5));

        List<UUID> allIds = userDataFetcher.fetchAllIds(new Include<>(userIds, User_.ID));

        assertThat(allIds).containsExactlyInAnyOrderElementsOf(userIds);
    }

    @Test
    void fetchAllIdsWithoutSpecification() {
        List<UUID> userIds = transactionalExecutor.executeWithInNewTransaction(() ->
                userGenerator.list(10).stream()
                        .peek(entityManager::persist)
                        .map(User::getId)
                        .toList());

        List<UUID> allIds = userDataFetcher.fetchAllIds(null);

        assertThat(allIds).containsExactlyInAnyOrderElementsOf(userIds);
    }

    @Test
    void fetchAllIdsWithSpecificationAndPageable() {
        List<UUID> userIds = transactionalExecutor.executeWithInNewTransaction(() ->
                userGenerator.list(10).stream()
                        .peek(entityManager::persist)
                        .map(User::getId)
                        .sorted(Comparator.comparing(UUID::toString))
                        .toList());

        Sort sort = Sort.by(Sort.Direction.ASC, User_.ID);
        PageRequest firstPageRequest = PageRequest.of(0, 5, sort);
        PageRequest secondPageRequest = PageRequest.of(1, 5, sort);

        List<UUID> firstPage = userDataFetcher.fetchAllIds(null, firstPageRequest);
        List<UUID> secondPage = userDataFetcher.fetchAllIds(null, secondPageRequest);

        assertThat(firstPage).containsExactlyInAnyOrderElementsOf(userIds.subList(0, 5));
        assertThat(secondPage).containsExactlyInAnyOrderElementsOf(userIds.subList(5, 10));
    }

    @Test
    void fetchAllByIds() {
        List<User> users = transactionalExecutor.executeWithInNewTransaction(() ->
                userGenerator.list(10).stream()
                        .peek(entityManager::persist)
                        .toList()
                        .subList(0, 3));

        List<UUID> userIds = users.stream()
                .map(User::getId)
                .toList();

        List<User> result = userDataFetcher.fetchAllByIds(userIds);

        assertThat(result).containsExactlyInAnyOrderElementsOf(users);
    }

    @Test
    void fetchAllByIdsWithEntityGraphName() {
        User user = transactionalExecutor.executeWithInNewTransaction(() ->
                userGenerator.list(5).stream()
                        .peek(entityManager::persist)
                        .toList()
                        .getFirst());

        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(postGenerator.one(user)));

        List<User> users = userDataFetcher.fetchAllByIds(List.of(user.getId()), User.USER_WITH_POSTS);
        assertThat(users).hasSize(1);

        User result = users.getFirst();
        assertThat(result).isEqualTo(user);
        assertThat(result.getPosts()).isNotNull();
        assertThatNoException().isThrownBy(() -> {
            List<Post> posts = result.getPosts();
            assertThat(posts).isNotEmpty();
            Post firstPost = posts.getFirst();
            assertThat(firstPost.getTitle()).isNotNull();
        });
    }

    @Test
    void fetchAllByIdsWithoutEntityGraphName() {
        User user = transactionalExecutor.executeWithInNewTransaction(() ->
                userGenerator.list(5).stream()
                        .peek(entityManager::persist)
                        .toList()
                        .getFirst());

        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(postGenerator.one(user)));


        List<User> users = userDataFetcher.fetchAllByIds(List.of(user.getId()), "");
        assertThat(users).hasSize(1);

        User result = users.getFirst();
        assertThat(result).isEqualTo(user);
        List<Post> posts = result.getPosts();
        assertThatThrownBy(posts::size)
                .isInstanceOf(LazyInitializationException.class)
                .hasMessageContaining("could not initialize proxy");
    }

    @Test
    void fetchAllByIdsWithEntityGraphAsListFields() {
        User user = transactionalExecutor.executeWithInNewTransaction(() ->
                userGenerator.list(5).stream()
                        .peek(entityManager::persist)
                        .toList()
                        .getFirst());

        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(postGenerator.one(user)));

        List<User> users = userDataFetcher.fetchAllByIds(
                List.of(user.getId()),
                List.of(new AttributeNode(User_.POSTS, null))
        );
        assertThat(users).hasSize(1);

        User result = users.getFirst();
        assertThat(result).isEqualTo(user);
        assertThat(result.getPosts()).isNotNull();
        assertThatNoException().isThrownBy(() -> {
            List<Post> posts = result.getPosts();
            assertThat(posts).isNotEmpty();
            Post firstPost = posts.getFirst();
            assertThat(firstPost.getTitle()).isNotNull();
        });
    }

    @Test
    void fetchAllByIdsWithoutEntityGraphAsListInvalidFields() {
        User user = transactionalExecutor.executeWithInNewTransaction(() ->
                userGenerator.list(5).stream()
                        .peek(entityManager::persist)
                        .toList()
                        .getFirst());

        transactionalExecutor.executeWithInNewTransaction(() -> entityManager.persist(postGenerator.one(user)));

        List<User> users = userDataFetcher.fetchAllByIds(
                List.of(user.getId()),
                List.of(new AttributeNode("Invalid field", null))
        );
        assertThat(users).hasSize(1);

        User result = users.getFirst();
        assertThat(result).isEqualTo(user);
        List<Post> posts = result.getPosts();
        assertThatThrownBy(posts::size)
                .isInstanceOf(LazyInitializationException.class)
                .hasMessageContaining("could not initialize proxy");
    }

    @Test
    void groups() {
        record OrderSummaryDto(Long orderId,
                               BigDecimal totalSum,
                               Long itemCount,
                               BigDecimal maxPrice,
                               BigDecimal minPrice) {
        }

        transactionalExecutor.executeWithInNewTransaction(() -> {
            List<Product> products = productGenerator.list(5);
            products.forEach(entityManager::persist);

            List<Order> orders = orderGenerator.list(3);
            orders.forEach(entityManager::persist);

            List<OrderItem> items = orderItemGenerator.list(15, orders, products);
            items.forEach(entityManager::persist);
        });


        // === 2. Настройка параметров группировки и агрегации ===
        List<GroupParam> groupParams = List.of(new GroupParam("order.id").alias("orderId"));

        List<FunctionParams> functionParams = getFunctionParams();

        // === 3. Выполнение группировки ===
        Function<Map<String, ?>, OrderSummaryDto> mapOrderSummaryDtoFunction =
                map -> new OrderSummaryDto(
                        (Long) map.get("orderId"),
                        (BigDecimal) map.get("totalSum"),
                        (Long) map.get("itemCount"),
                        (BigDecimal) map.get("maxPrice"),
                        (BigDecimal) map.get("minPrice")
                );

        List<OrderSummaryDto> result = orderItemFetcher.groups(
                new Empty<>(),
                Pageable.ofSize(Integer.MAX_VALUE),
                groupParams,
                mapOrderSummaryDtoFunction,
                functionParams
        );

        // === 4. Проверка результата ===
        assertFalse(result.isEmpty());
        result.forEach(dto -> {
            System.out.printf("Order #%d → Total: %s | Count: %d | Max: %s | Min: %s%n",
                    dto.orderId(),
                    dto.totalSum(),
                    dto.itemCount(),
                    dto.maxPrice(),
                    dto.minPrice()
            );

            assertNotNull(dto.totalSum());
            assertNotNull(dto.itemCount());
            assertNotNull(dto.maxPrice());
            assertNotNull(dto.minPrice());
        });
    }

    private @NotNull List<FunctionParams> getFunctionParams() {
        UnaryOperator<Object> objectUnaryOperator = value -> Optional.ofNullable((BigDecimal) value)
                .map(v -> v.setScale(2, RoundingMode.HALF_UP))
                .orElse(BigDecimal.ZERO);

        return List.of(
                new FunctionParams("totalSum", "sum", BigDecimal.class,
                        objectUnaryOperator,
                        // или просто "product.price"
                        OrderItem_.PRODUCT, Product_.PRICE),

                new FunctionParams("itemCount", "count", Long.class,
                        value -> value != null ? ((Number) value).longValue() : 0L,
                        OrderItem_.ID),

                new FunctionParams("maxPrice", "max", BigDecimal.class,
                        objectUnaryOperator,
                        OrderItem_.PRODUCT, Product_.PRICE),

                new FunctionParams("minPrice", "min", BigDecimal.class,
                        objectUnaryOperator,
                        OrderItem_.PRODUCT, Product_.PRICE)
        );
    }
}
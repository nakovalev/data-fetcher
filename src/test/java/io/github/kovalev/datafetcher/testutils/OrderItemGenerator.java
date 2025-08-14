package io.github.kovalev.datafetcher.testutils;

import io.github.kovalev.datafetcher.domain.Order;
import io.github.kovalev.datafetcher.domain.OrderItem;
import io.github.kovalev.datafetcher.domain.Product;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class OrderItemGenerator {

    private final Random random;

    public OrderItemGenerator(Random random) {
        this.random = random;
    }

    public List<OrderItem> list(int count, List<Order> orders, List<Product> products) {
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    OrderItem item = new OrderItem();
                    item.setOrder(orders.get(random.nextInt(orders.size())));
                    item.setProduct(products.get(random.nextInt(products.size())));
                    item.setQuantity(1 + random.nextInt(5));
                    return item;
                })
                .toList();
    }

    public OrderItem one(Order order, Product product) {
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(1 + random.nextInt(5));
        return item;
    }
}

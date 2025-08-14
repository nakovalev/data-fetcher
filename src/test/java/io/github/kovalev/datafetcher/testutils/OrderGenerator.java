package io.github.kovalev.datafetcher.testutils;

import io.github.kovalev.datafetcher.domain.Order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class OrderGenerator {
    private final Random random;

    private final String[] firstnames =
            {"john", "emma", "michael", "sophia", "william", "olivia", "james", "ava", "robert", "mia"};
    private final String[] lastnames =
            {"smith", "johnson", "williams", "brown", "jones", "miller", "davis", "garcia", "rodriguez", "wilson"};

    public OrderGenerator(Random random) {
        this.random = random;
    }

    public List<Order> list(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    Order order = new Order();
                    String firstName = firstnames[random.nextInt(firstnames.length)];
                    String lastName = lastnames[random.nextInt(lastnames.length)];
                    order.setCustomer(firstName + "_" + lastName);
                    order.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(60)));
                    return order;
                })
                .toList();
    }

    public Order one() {
        return list(1).getFirst();
    }
}
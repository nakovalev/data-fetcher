package io.github.kovalev.datafetcher.testutils;

import io.github.kovalev.datafetcher.domain.Product;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class ProductGenerator {

    private final Random random;
    private final String[] names = {
            "Wireless Mouse", "Bluetooth Headphones", "Gaming Keyboard",
            "Smartphone Stand", "USB-C Charger", "Webcam 1080p",
            "Portable SSD", "Noise Cancelling Earbuds", "Laptop Cooling Pad",
            "Mechanical Keyboard"
    };

    public ProductGenerator(Random random) {
        this.random = random;
    }

    public List<Product> list(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    Product product = new Product();
                    product.setName(names[random.nextInt(names.length)]);
                    product.setPrice(BigDecimal.valueOf(10 + random.nextDouble() * 990).setScale(2, RoundingMode.HALF_UP));
                    return product;
                })
                .toList();
    }

    public Product one() {
        return list(1).getFirst();
    }
}
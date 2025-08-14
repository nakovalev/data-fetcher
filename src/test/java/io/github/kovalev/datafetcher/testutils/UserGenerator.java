package io.github.kovalev.datafetcher.testutils;

import io.github.kovalev.datafetcher.domain.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class UserGenerator {

    private final Random random;
    private final String[] firstnames =
            {"john", "emma", "michael", "sophia", "william", "olivia", "james", "ava", "robert", "mia"};
    private final String[] lastnames =
            {"smith", "johnson", "williams", "brown", "jones", "miller", "davis", "garcia", "rodriguez", "wilson"};

    public UserGenerator(Random random) {
        this.random = random;
    }

    public List<User> list(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    User user = new User();
                    String firstName = firstnames[random.nextInt(firstnames.length)];
                    String lastName = lastnames[random.nextInt(lastnames.length)];
                    user.setUsername(firstName + "_" + lastName);
                    user.setEmail(user.getUsername() + "@gmail.com");
                    user.setCreatedAt(LocalDateTime.now());
                    return user;
                })
                .toList();
    }

    public User one() {
        return list(1).getFirst();
    }
}

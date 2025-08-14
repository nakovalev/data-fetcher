package io.github.kovalev.datafetcher.testutils;

import io.github.kovalev.datafetcher.domain.Post;
import io.github.kovalev.datafetcher.domain.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class PostGenerator {
    private final Random random;
    private final String[] titles = {
            "Introduction to Spring Boot",
            "Advanced Java Techniques",
            "Microservices Architecture",
            "Database Design Patterns",
            "Cloud Computing Basics",
            "React vs Angular",
            "Machine Learning Fundamentals",
            "DevOps Best Practices",
            "Security in Web Applications",
            "The Future of AI"
    };

    private final String[] contents = {
            "This is a comprehensive guide about the topic.",
            "In this post, we'll explore various aspects of the subject.",
            "Learn the key concepts and practical applications.",
            "A deep dive into the technical details you need to know.",
            "Practical examples and code snippets included.",
            "Comparing different approaches to solve common problems.",
            "Tips and tricks from industry experts.",
            "How to avoid common pitfalls in this area.",
            "Step-by-step tutorial for beginners.",
            "Case studies and real-world applications."
    };

    public PostGenerator(Random random) {
        this.random = random;
    }

    public List<Post> list(int count, User author) {
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    Post post = new Post();
                    post.setTitle(titles[random.nextInt(titles.length)]);
                    post.setContent(contents[random.nextInt(contents.length)]);
                    post.setCreatedAt(LocalDateTime.now());
                    post.setAuthor(author);
                    post.setComments(List.of());
                    return post;
                })
                .toList();
    }

    public Post one(User author) {
        return list(1, author).getFirst();
    }
}

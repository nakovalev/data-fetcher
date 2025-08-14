package io.github.kovalev.datafetcher.testutils;

import io.github.kovalev.datafetcher.domain.Comment;
import io.github.kovalev.datafetcher.domain.Post;
import io.github.kovalev.datafetcher.domain.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class CommentGenerator {
    private final Random random;
    private final String[] comments = {
            "Great post! Very informative.",
            "I disagree with some points, but overall good.",
            "This helped me solve my problem, thanks!",
            "Could you elaborate more on section 3?",
            "I've been looking for this information for ages!",
            "Not convinced by the arguments presented.",
            "Excellent explanation, clear and concise.",
            "What about the edge cases you mentioned?",
            "This is exactly what I needed!",
            "Looking forward to more content like this."
    };

    public CommentGenerator(Random random) {
        this.random = random;
    }

    public List<Comment> list(int count, User author, Post post) {
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    Comment comment = new Comment();
                    comment.setText(comments[random.nextInt(comments.length)]);
                    comment.setAuthor(author);
                    comment.setPost(post);
                    comment.setCreatedAt(LocalDateTime.now());
                    return comment;
                })
                .toList();
    }

    public Comment one(User author, Post post) {
        return list(1, author, post).getFirst();
    }
}

package io.github.kovalev.datafetcher.utils;

import io.github.kovalev.datafetcher.domain.Comment;
import io.github.kovalev.datafetcher.domain.Post;
import io.github.kovalev.datafetcher.domain.User;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.SingularAttribute;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PathCalculatorTest {

    @Mock
    private Root<Post> root;

    @Mock
    private Join<Post, User> join;

    @Mock
    private Path<Post> path;

    @Test
    void testPathWithSingleField() {
        // select p from Post p where p.title ...
        String title = "title";
        when(root.get(title)).thenAnswer(inv -> path);

        Path<Object> result = new PathCalculator<>(root, title).path();

        assertThat(result).isEqualTo(path);
        // Проверяем получение атрибута "title"
        verify(root).get(title);
        verifyNoMoreInteractions(root);
    }

    @Test
    void testPathWithMultipleFields() {
        // select p from Post p where p.author.username ...
        String[] attributePath = "author.username".split("\\.");
        String author = attributePath[0];
        String username = attributePath[1];

        when(root.getJoins()).thenReturn(Set.of());
        when(root.getFetches()).thenReturn(Set.of());
        when(root.join(author, JoinType.LEFT)).thenAnswer(inv -> join);
        when(join.get(username)).thenAnswer(inv -> path);

        Path<Object> result = new PathCalculator<>(root, author, username).path();

        assertThat(result).isEqualTo(path);
        verify(root).join(author, JoinType.LEFT);
        verify(join).get(username);
    }

    @Test
    void testPathWithExistingLeftJoin() {
        // select p from Post p where p.author.username ...
        String[] attributePath = "author.username".split("\\.");
        String author = attributePath[0];
        String username = attributePath[1];

        // Создаем параметризованный атрибут
        SingularAttribute<Post, User> authorAttribute = mock();
        when(authorAttribute.getName()).thenReturn(author);

        // Настраиваем моки с учетом типов
        when(join.getAttribute()).thenAnswer(inv -> authorAttribute);
        when(join.getJoinType()).thenReturn(JoinType.LEFT);

        // Настраиваем коллекции joins и fetches
        when(root.getJoins()).thenReturn(Set.of(join));
        when(root.getFetches()).thenReturn(Set.of());

        // Настраиваем получение пути для username
        when(join.get(username)).thenAnswer(inv -> path);

        Path<Object> result = new PathCalculator<>(root, author, username).path();

        assertThat(result).isEqualTo(path);
        // Проверяем, что новый join не создавался
        verify(root, never()).join(any(SingularAttribute.class), any(JoinType.class));
        // Проверяем получение атрибута "username"
        verify(join).get(username);
    }


    @Test
    void pathWithExistingFetch() {
        // select p from Post p where p.author.username ...
        String[] attributePath = "author.username".split("\\.");
        String author = attributePath[0];
        String username = attributePath[1];

        SingularAttribute<Post, User> attribute = mock();
        when(attribute.getName()).thenReturn(author);

        // создаем left join на автора как EAGER
        Fetch<Post, User> fetch = mock();
        when(fetch.getAttribute()).thenAnswer(inv -> attribute);
        when(fetch.getJoinType()).thenReturn(JoinType.LEFT);

        /*
        добавляем созданный join в коллекцию fetches,
        которая содержит связи загружаемые как EAGER
         */
        root = mock();
        when(root.getFetches()).thenReturn(Collections.singleton(fetch));

        // Настраиваем, чтобы создался left join с названием "author"
        join = mock();
        when(root.join(author, JoinType.LEFT)).thenAnswer(inv -> join);

        // Настраиваем, чтобы при вызове join.get(username) вернулся path
        path = mock();
        when(join.get(username)).thenAnswer(inv -> path);

        Path<Object> result = new PathCalculator<>(root, author, username).path();

        assertSame(path, result);
        // Проверяем создание left join с названием "author"
        verify(root).join(author, JoinType.LEFT);
        // Проверяем получение атрибута "username"
        verify(join).get(username);
    }

    @Test
    void testPathWithThreeFields() {
        // select p from Post p where p.comments.author.username ...
        String[] attributePath = "comments.author.username".split("\\.");
        String comments = attributePath[0];
        String author = attributePath[1];
        String username = attributePath[2];

        root = mock();
        when(root.getJoins()).thenReturn(Collections.emptySet());
        when(root.getFetches()).thenReturn(Collections.emptySet());

        // Настраиваем, чтобы создался left join с названием "comments"
        Join<Post, Comment> commentJoin = mock();
        when(root.join(comments, JoinType.LEFT)).thenAnswer(inv -> commentJoin);

        // Настраиваем, чтобы создался left join с названием "author"
        Join<Post, User> userJoin = mock();
        when(commentJoin.join(author, JoinType.LEFT)).thenAnswer(inv -> userJoin);

        // Настраиваем, чтобы при вызове join.get(username) вернулся path
        path = mock();
        when(userJoin.get(username)).thenAnswer(inv -> path);

        Path<Object> result = new PathCalculator<>(root, comments, author, username).path();

        assertSame(path, result);
        // Проверяем создание left join с названием "comments"
        verify(root).join(comments, JoinType.LEFT);
        // Проверяем создание left join с названием "author"
        verify(commentJoin).join(author, JoinType.LEFT);
        // Проверяем получение атрибута "username"
        verify(userJoin).get(username);
    }
}
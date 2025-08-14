package io.github.kovalev.datafetcher.services;

import io.github.kovalev.datafetcher.domain.Post;
import io.github.kovalev.datafetcher.domain.Post_;
import io.github.kovalev.datafetcher.domain.User;
import io.github.kovalev.datafetcher.domain.User_;
import io.github.kovalev.datafetcher.utils.QueryField;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Subgraph;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntityGraphFactoryTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private EntityGraph<User> userEntityGraph;

    @Mock
    private Subgraph<Post> postsSubgraph;

    @InjectMocks
    private EntityGraphFactory entityGraphFactory;

    @Test
    void graphByName_ShouldReturnEntityGraph_WhenNameIsValid() {
        String graphName = "User.withPostsAndComments";
        when(entityManager.getEntityGraph(graphName)).thenAnswer(inv -> userEntityGraph);

        EntityGraph<?> result = entityGraphFactory.graphByName(graphName);

        assertThat(result).isNotNull();
        assertThat(userEntityGraph).isEqualTo(result);
        verify(entityManager).getEntityGraph(graphName);
    }

    @Test
    void graphByFields_ShouldCreateEntityGraph_WithAssociationsOnly() {
        List<QueryField> fields = List.of(
                new QueryField(User_.POSTS, null),
                new QueryField(User_.COMMENTS, null)
        );

        when(entityManager.createEntityGraph(User.class)).thenReturn(userEntityGraph);

        EntityGraph<?> result = entityGraphFactory.graphByFields(fields, User.class);

        assertThat(result).isNotNull();
        verify(userEntityGraph).addAttributeNodes(User_.POSTS);
        verify(userEntityGraph).addAttributeNodes(User_.COMMENTS);
    }

    @Test
    void graphByFields_ShouldHandleNestedSubgraphs() {
        List<QueryField> fields = List.of(
                new QueryField(User_.POSTS, List.of(
                        new QueryField(Post_.COMMENTS, null)
                ))
        );

        when(entityManager.createEntityGraph(User.class)).thenReturn(userEntityGraph);
        when(userEntityGraph.addSubgraph(User_.POSTS)).thenAnswer(inv -> postsSubgraph);

        EntityGraph<?> result = entityGraphFactory.graphByFields(fields, User.class);

        assertThat(result).isNotNull();
        verify(userEntityGraph).addAttributeNodes(User_.POSTS);
        verify(postsSubgraph).addAttributeNodes(Post_.COMMENTS);
    }

    @Test
    void graphByFields_ShouldLogWarning_WhenFieldIsNotAnAssociation() {
        List<QueryField> fields = List.of(
                new QueryField(User_.USERNAME, null)
        );

        when(entityManager.createEntityGraph(User.class)).thenReturn(userEntityGraph);
        doThrow(IllegalArgumentException.class)
                .when(userEntityGraph).addAttributeNodes(User_.USERNAME);

        EntityGraph<?> result = entityGraphFactory.graphByFields(fields, User.class);

        assertThat(result).isNotNull();
    }

    @Test
    void graphByFields_ShouldSkipInvalidAssociations() {
        List<QueryField> fields = List.of(
                new QueryField(User_.POSTS, List.of(
                        new QueryField("invalidField", null)
                ))
        );

        when(entityManager.createEntityGraph(User.class)).thenReturn(userEntityGraph);
        when(userEntityGraph.addSubgraph(User_.POSTS)).thenAnswer(inv -> postsSubgraph);
        doThrow(IllegalArgumentException.class)
                .when(postsSubgraph).addAttributeNodes("invalidField");

        EntityGraph<?> result = entityGraphFactory.graphByFields(fields, User.class);

        assertThat(result).isNotNull();
    }
}
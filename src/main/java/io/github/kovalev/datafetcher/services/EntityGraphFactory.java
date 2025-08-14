package io.github.kovalev.datafetcher.services;

import io.github.kovalev.datafetcher.utils.QueryField;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Subgraph;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hibernate.graph.CannotContainSubGraphException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class EntityGraphFactory {

    private final EntityManager entityManager;

    public EntityGraph<?> graphByName(String graphName) {
        if (StringUtils.hasText(graphName)) {
            return entityManager.getEntityGraph(graphName);
        }

        return null;
    }

    public EntityGraph<?> graphByFields(List<QueryField> fields, Class<?> entityClass) {
        EntityGraph<?> graph = entityManager.createEntityGraph(entityClass);
        fillSubgraphs(fields, graph);
        return graph;
    }

    void fillSubgraphs(List<QueryField> fields, EntityGraph<?> graph) {
        if (CollectionUtils.isEmpty(fields)) {
            return;
        }
        for (QueryField field : fields) {
            String fieldName = field.getName();
            runGraphBuilder(() -> graph.addAttributeNodes(fieldName), fieldName);
            if (!CollectionUtils.isEmpty(field.getChildFields())) {
                runGraphBuilder(
                        () -> addSubgraph(field.getChildFields(), graph.addSubgraph(fieldName)), fieldName
                );
            }
        }
    }

    void runGraphBuilder(Runnable runnable, String fieldName) {
        try {
            runnable.run();
        } catch (CannotContainSubGraphException | IllegalArgumentException e) {
            log.warn("Ошибка создания подграфа: {}", fieldName);
        } catch (Exception e) {
            log.error("Ошибка создания графа", e);
        }
    }

    void addSubgraph(List<QueryField> fields, Subgraph<Object> subgraph) {
        if (CollectionUtils.isEmpty(fields)) {
            return;
        }
        for (var field : fields) {
            val fieldName = field.getName();
            subgraph.addAttributeNodes(fieldName);
            if (!CollectionUtils.isEmpty(field.getChildFields())) {
                addSubgraph(field.getChildFields(), subgraph.addSubgraph(fieldName));
            }
        }
    }
}

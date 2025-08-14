package io.github.kovalev.datafetcher.services;

import io.github.kovalev.datafetcher.utils.AttributeNode;
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

    public EntityGraph<?> graphByAttributeNodes(List<AttributeNode> attributeNodes, Class<?> entityClass) {
        EntityGraph<?> graph = entityManager.createEntityGraph(entityClass);
        fillSubgraphs(attributeNodes, graph);
        return graph;
    }

    void fillSubgraphs(List<AttributeNode> attributeNodes, EntityGraph<?> graph) {
        if (CollectionUtils.isEmpty(attributeNodes)) {
            return;
        }
        for (AttributeNode attributeNode : attributeNodes) {
            String attribute = attributeNode.getAttribute();
            runGraphBuilder(() -> graph.addAttributeNodes(attribute), attribute);
            if (!CollectionUtils.isEmpty(attributeNode.getSubGraph())) {
                runGraphBuilder(
                        () -> addSubgraph(attributeNode.getSubGraph(), graph.addSubgraph(attribute)), attribute
                );
            }
        }
    }

    void runGraphBuilder(Runnable runnable, String attribute) {
        try {
            runnable.run();
        } catch (CannotContainSubGraphException | IllegalArgumentException e) {
            log.warn("Ошибка создания подграфа: {}", attribute);
        } catch (Exception e) {
            log.error("Ошибка создания графа", e);
        }
    }

    void addSubgraph(List<AttributeNode> attributeNodes, Subgraph<Object> subgraph) {
        if (CollectionUtils.isEmpty(attributeNodes)) {
            return;
        }
        for (var attributeNode : attributeNodes) {
            val attribute = attributeNode.getAttribute();
            subgraph.addAttributeNodes(attribute);
            if (!CollectionUtils.isEmpty(attributeNode.getSubGraph())) {
                addSubgraph(attributeNode.getSubGraph(), subgraph.addSubgraph(attribute));
            }
        }
    }
}

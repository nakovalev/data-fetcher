package io.github.kovalev.datafetcher.services;

import io.github.kovalev.datafetcher.utils.AttributeNode;
import org.springframework.lang.NonNull;

import java.util.Arrays;
import java.util.List;

public class AttributeNodeFactory {

    public AttributeNode attributeNode(@NonNull String attribute) {
        return new AttributeNode(attribute, null);
    }

    public List<AttributeNode> attributeNodes(@NonNull String... attributes) {
        return Arrays.stream(attributes)
                .map(this::attributeNode)
                .toList();
    }

    public AttributeNode deepAttributeNode(@NonNull String... fields) {
        AttributeNode firstField = attributeNode(fields[0]);
        AttributeNode attributeNode = firstField;
        if (fields.length > 1) {
            for (int i = 1; i < fields.length; i++) {
                AttributeNode nextField = attributeNode(fields[i]);
                attributeNode.setSubGraph(List.of(nextField));
                attributeNode = nextField;
            }
        }
        return firstField;
    }
}

package io.github.kovalev.datafetcher.services;

import io.github.kovalev.datafetcher.utils.AttributeNode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AttributeNodeFactoryTest {

    AttributeNodeFactory attributeNodeFactory = new AttributeNodeFactory();

    @Test
    void attributeNode() {
        String field = "field";
        AttributeNode attributeNode = attributeNodeFactory.attributeNode(field);
        assertThat(attributeNode).isNotNull();
        assertThat(attributeNode.getAttribute()).isEqualTo(field);
        assertThat(attributeNode.getSubGraph()).isNull();
    }

    @Test
    void deepAttributeNode() {
        assertThatThrownBy(() -> attributeNodeFactory.deepAttributeNode())
                .isInstanceOf(IndexOutOfBoundsException.class);

        String[] fields = {"parent", "childLvl1", "childLvl2", "childLvl3"};

        AttributeNode parent = attributeNodeFactory.deepAttributeNode(fields);
        assertThat(parent).isNotNull();
        assertThat(parent.getAttribute()).isEqualTo(fields[0]);
        assertThat(parent.getSubGraph()).hasSize(1);

        AttributeNode childLvl1 = parent.getSubGraph().getFirst();
        assertThat(childLvl1).isNotNull();
        assertThat(childLvl1.getAttribute()).isEqualTo(fields[1]);
        assertThat(childLvl1.getSubGraph()).hasSize(1);


        AttributeNode childLvl2 = childLvl1.getSubGraph().getFirst();
        assertThat(childLvl2).isNotNull();
        assertThat(childLvl2.getAttribute()).isEqualTo(fields[2]);
        assertThat(childLvl2.getSubGraph()).hasSize(1);

        AttributeNode childLvl3 = childLvl2.getSubGraph().getFirst();
        assertThat(childLvl3).isNotNull();
        assertThat(childLvl3.getAttribute()).isEqualTo(fields[3]);
        assertThat(childLvl3.getSubGraph()).isNull();
    }
}
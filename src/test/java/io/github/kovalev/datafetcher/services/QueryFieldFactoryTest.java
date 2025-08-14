package io.github.kovalev.datafetcher.services;

import io.github.kovalev.datafetcher.utils.QueryField;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QueryFieldFactoryTest {

    QueryFieldFactory queryFieldFactory = new QueryFieldFactory();

    @Test
    void field() {
        String field = "field";
        QueryField queryField = queryFieldFactory.field(field);
        assertThat(queryField).isNotNull();
        assertThat(queryField.getName()).isEqualTo(field);
        assertThat(queryField.getChildFields()).isNull();
    }

    @Test
    void fields() {
        String field = "field";
        List<QueryField> fields = queryFieldFactory.fields(field);
        assertThat(fields).hasSize(1);
        assertThat(fields.getFirst()).isNotNull();
        assertThat(fields.getFirst().getName()).isEqualTo(field);
        assertThat(fields.getFirst().getChildFields()).isNull();
    }

    @Test
    void deepField() {
        assertThatThrownBy(() -> queryFieldFactory.deepField())
                .isInstanceOf(IndexOutOfBoundsException.class);

        String[] fields = {"parent", "childLvl1", "childLvl2", "childLvl3"};

        QueryField parent = queryFieldFactory.deepField(fields);
        assertThat(parent).isNotNull();
        assertThat(parent.getName()).isEqualTo(fields[0]);
        assertThat(parent.getChildFields()).hasSize(1);

        QueryField childLvl1 = parent.getChildFields().getFirst();
        assertThat(childLvl1).isNotNull();
        assertThat(childLvl1.getName()).isEqualTo(fields[1]);
        assertThat(childLvl1.getChildFields()).hasSize(1);


        QueryField childLvl2 = childLvl1.getChildFields().getFirst();
        assertThat(childLvl2).isNotNull();
        assertThat(childLvl2.getName()).isEqualTo(fields[2]);
        assertThat(childLvl2.getChildFields()).hasSize(1);

        QueryField childLvl3 = childLvl2.getChildFields().getFirst();
        assertThat(childLvl3).isNotNull();
        assertThat(childLvl3.getName()).isEqualTo(fields[3]);
        assertThat(childLvl3.getChildFields()).isNull();
    }

    @Test
    void deepFields() {
        String[] fields = {"parent", "childLvl1", "childLvl2", "childLvl3"};

        List<QueryField> queryFields = queryFieldFactory.deepFields(fields);

        assertThat(queryFields).hasSize(1);

        QueryField parent = queryFields.getFirst();
        assertThat(parent).isNotNull();
        assertThat(parent.getName()).isEqualTo(fields[0]);
        assertThat(parent.getChildFields()).hasSize(1);

        QueryField childLvl1 = parent.getChildFields().getFirst();
        assertThat(childLvl1).isNotNull();
        assertThat(childLvl1.getName()).isEqualTo(fields[1]);
        assertThat(childLvl1.getChildFields()).hasSize(1);


        QueryField childLvl2 = childLvl1.getChildFields().getFirst();
        assertThat(childLvl2).isNotNull();
        assertThat(childLvl2.getName()).isEqualTo(fields[2]);
        assertThat(childLvl2.getChildFields()).hasSize(1);

        QueryField childLvl3 = childLvl2.getChildFields().getFirst();
        assertThat(childLvl3).isNotNull();
        assertThat(childLvl3.getName()).isEqualTo(fields[3]);
        assertThat(childLvl3.getChildFields()).isNull();
    }
}
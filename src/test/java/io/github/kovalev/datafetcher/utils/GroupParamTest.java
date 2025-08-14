package io.github.kovalev.datafetcher.utils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupParamTest {

    @Mock
    private Root<?> root;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Path<Object> path;

    @Mock
    private Expression<?> expression;

    @Mock
    private ExpressionFunction expressionFunction;

    @Test
    void constructorWithFields_shouldSetFieldsAndAlias() {
        GroupParam param = new GroupParam("field1", "field2");

        assertThat(param.getFields())
                .containsExactly("field1", "field2")
                .hasSize(2);

        assertThat(param.getAlias()).isEqualTo("field1");
        assertThat(param.getExpressionFunction()).isNull();
    }

    @Test
    void constructorWithSingleDotField_shouldSplitFields() {
        GroupParam param = new GroupParam("parent.child");

        assertThat(param.getFields())
                .containsExactly("parent", "child")
                .hasSize(2);

        assertThat(param.getAlias()).isEqualTo("parent.child");
    }

    @Test
    void constructorWithEmptyFields_shouldThrowException() {
        assertThatThrownBy(() -> new GroupParam(new String[0]))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("fields is null or empty");
    }

    @Test
    void constructorWithAliasAndExpressionFunction_shouldSetFields() {
        GroupParam param = new GroupParam("testAlias", expressionFunction);

        assertThat(param.getFields()).isNull();
        assertThat(param.getAlias()).isEqualTo("testAlias");
        assertThat(param.getExpressionFunction()).isSameAs(expressionFunction);
    }

    @Test
    void expression_withFields_shouldCreatePathExpression() {
        when(root.get("field1")).thenReturn(path);

        GroupParam param = new GroupParam("field1");

        Expression<?> result = param.expression(root, cb);

        assertThat(result).isSameAs(path);
        verify(path).alias("field1");
    }

    @Test
    void expression_withExpressionFunction_shouldUseProvidedFunction() {
        when(expressionFunction.apply(root, cb)).thenAnswer(inv -> expression);

        GroupParam param = new GroupParam("testAlias", expressionFunction);

        Expression<?> result = param.expression(root, cb);

        assertThat(result)
                .isSameAs(expression)
                .isEqualTo(expression); // Альтернативный вариант проверки
        verify(expression).alias("testAlias");
    }

    @Test
    void alias_shouldSetNewAliasAndReturnThis() {
        GroupParam param = new GroupParam("field1");

        GroupParam result = param.alias("newAlias");

        assertThat(result)
                .isSameAs(param)
                .extracting(GroupParam::getAlias)
                .isEqualTo("newAlias");
    }

    @Test
    void getFields_shouldReturnOriginalFieldsWhenNotSplit() {
        GroupParam param = new GroupParam("field1", "field2");

        assertThat(param.getFields())
                .containsExactly("field1", "field2")
                .hasSize(2);
    }

    @Test
    void getFields_shouldReturnSplitFieldsForDotNotation() {
        GroupParam param = new GroupParam("parent.child");

        assertThat(param.getFields())
                .containsExactly("parent", "child")
                .hasSize(2);
    }
}
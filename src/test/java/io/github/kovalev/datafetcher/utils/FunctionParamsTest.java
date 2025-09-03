package io.github.kovalev.datafetcher.utils;

import io.github.kovalev.specificationhelper.utils.PathCalculator;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Function;
import java.util.function.UnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FunctionParamsTest {

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
    void constructorWithAllParams_shouldSetFieldsCorrectly() {
        UnaryOperator<Object> mapper = UnaryOperator.identity();
        String[] fields = {"field1", "field2"};

        FunctionParams params = new FunctionParams(
                "testAlias",
                "testFunction",
                String.class,
                mapper,
                fields
        );

        assertThat(params)
                .extracting(
                        FunctionParams::getAlias,
                        FunctionParams::getFunctionName,
                        FunctionParams::getReturnType,
                        FunctionParams::getMapper,
                        FunctionParams::getFields,
                        FunctionParams::getExpressionFunction
                )
                .containsExactly(
                        "testAlias",
                        "testFunction",
                        String.class,
                        mapper,
                        fields,
                        null
                );
    }

    @Test
    void constructorWithAliasAndExpressionFunction_shouldSetFieldsCorrectly() {
        FunctionParams params = new FunctionParams("testAlias", expressionFunction);

        assertThat(params)
                .extracting(
                        FunctionParams::getAlias,
                        FunctionParams::getFunctionName,
                        FunctionParams::getReturnType,
                        FunctionParams::getFields,
                        FunctionParams::getExpressionFunction
                )
                .containsExactly(
                        "testAlias",
                        null,
                        null,
                        null,
                        expressionFunction
                );

        assertThat(params.getMapper())
                .isNotNull()
                .isEqualTo(Function.identity());
    }

    @Test
    void constructorWithAliasMapperAndExpressionFunction_shouldSetFieldsCorrectly() {
        UnaryOperator<Object> mapper = UnaryOperator.identity();

        FunctionParams params = new FunctionParams("testAlias", mapper, expressionFunction);

        assertThat(params)
                .extracting(
                        FunctionParams::getAlias,
                        FunctionParams::getFunctionName,
                        FunctionParams::getReturnType,
                        FunctionParams::getMapper,
                        FunctionParams::getFields,
                        FunctionParams::getExpressionFunction
                )
                .containsExactly(
                        "testAlias",
                        null,
                        null,
                        mapper,
                        null,
                        expressionFunction
                );
    }

    @Test
    void expression_withFunctionName_shouldCreateFunctionExpression() {
        when(new PathCalculator<>(root, new String[]{"field1"}).path()).thenReturn(path);
        when(cb.function("testFunction", String.class, path)).thenAnswer(inv -> expression);

        FunctionParams params = new FunctionParams(
                "testAlias",
                "testFunction",
                String.class,
                UnaryOperator.identity(),
                "field1"
        );

        Expression<?> result = params.expression(root, cb);

        assertThat(result)
                .isSameAs(expression)
                .isEqualTo(expression);

        verify(expression).alias("testAlias");
    }

    @Test
    void expression_withExpressionFunction_shouldUseProvidedFunction() {
        when(expressionFunction.apply(root, cb)).thenAnswer(inv -> expression);

        FunctionParams params = new FunctionParams("testAlias", expressionFunction);

        Expression<?> result = params.expression(root, cb);

        assertThat(result)
                .isSameAs(expression)
                .isEqualTo(expression);

        verify(expression).alias("testAlias");
        verifyNoMoreInteractions(cb);
    }

    @Test
    void alias_shouldSetNewAliasAndReturnThis() {
        FunctionParams params = new FunctionParams("oldAlias", expressionFunction);

        FunctionParams result = params.alias("newAlias");

        assertThat(result)
                .isSameAs(params)
                .extracting(FunctionParams::getAlias)
                .isEqualTo("newAlias");
    }

    @Test
    void getMapper_shouldReturnIdentityFunctionWhenNotSpecified() {
        FunctionParams params = new FunctionParams("alias", expressionFunction);

        Object input = new Object();
        Object output = params.getMapper().apply(input);

        assertThat(output)
                .isSameAs(input)
                .isEqualTo(input);
    }

    @Test
    void getMapper_shouldReturnCustomMapperWhenSpecified() {
        UnaryOperator<Object> mapper = o -> "transformed";
        FunctionParams params = new FunctionParams("alias", mapper, expressionFunction);

        Object result = params.getMapper().apply(new Object());

        assertThat(result)
                .isEqualTo("transformed")
                .isInstanceOf(String.class);
    }
}
package io.github.kovalev.datafetcher.specifications;

import io.github.kovalev.datafetcher.enums.NullPolicy;
import io.github.kovalev.datafetcher.utils.CheckFields;
import io.github.kovalev.datafetcher.utils.Expressions;
import io.github.kovalev.datafetcher.utils.PathCalculator;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Создает спецификацию для сравнения полей.
 *
 * <p><b>Особенности работы с временными типами:</b><br>
 * При сравнении типов {@link LocalTime}, {@link LocalDateTime}, {@link Instant},
 * {@link ZonedDateTime}, {@link OffsetDateTime} возможны расхождения из-за разной
 * точности представления времени в Java (наносекунды) и БД (обычно микро/миллисекунды).
 * Рекомендуется заранее выравнивать точность с помощью {@code truncatedTo()}.
 *
 * <p><b>Пример:</b><br>
 * {@code LocalDateTime truncated = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);}
 */
public abstract class BaseComparisonSpecification<E> implements CustomSpecification<E> {

    protected static final boolean DEFAULT_IGNORE_CASE = false;

    protected final transient Object value;
    protected final String[] fields;
    protected final NullPolicy nullPolicy;
    protected final boolean ignoreCase;
    protected final transient Expressions expressions;

    protected BaseComparisonSpecification(Object value, @NonNull NullPolicy nullPolicy,
                                          boolean ignoreCase, String... fields) {
        this.value = value;
        this.nullPolicy = Objects.requireNonNull(nullPolicy);
        this.ignoreCase = ignoreCase;
        this.fields = fields;
        this.expressions = new Expressions();
    }

    @Override
    public Specification<E> specification() {
        if (new CheckFields(fields).nonNull()) {
            return (root, query, cb) -> {
                Path<Object> path = new PathCalculator<>(root, fields).path();

                if (value == null) {
                    return handleNull(cb, path);
                }

                if (value instanceof CharSequence str) {
                    return resolveCase(cb, path, expressions, String.valueOf(str));
                }

                Expression<?> expression = expressions.get(cb, path, value);
                return createPredicate(cb, expression, value);
            };
        }

        return new Empty<>();
    }

    protected abstract Predicate createPredicate(CriteriaBuilder cb, Expression<?> expression, Object value);

    protected abstract Predicate handleNull(CriteriaBuilder criteriaBuilder, Path<Object> path);

    protected abstract Predicate resolveCase(CriteriaBuilder criteriaBuilder, Path<Object> path,
                                             Expressions expressions, String str);
}
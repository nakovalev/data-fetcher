package io.github.kovalev.datafetcher.specifications;

import io.github.kovalev.datafetcher.enums.NullPolicy;
import io.github.kovalev.datafetcher.utils.Expressions;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.lang.NonNull;

/**
 * Спецификация для проверки несоответствия значений полей.
 *
 * <p>Наследует все особенности сравнения из {@link BaseComparisonSpecification},
 * включая ограничения точности для временных типов.</p>
 *
 * <p><b>Особенности реализации:</b></p>
 * <ul>
 *   <li>Генерирует SQL-условие с оператором {@code <>}</li>
 *   <li>Поддерживает регистронезависимое сравнение строк (при {@code ignoreCase = true})</li>
 *   <li>Обработка NULL значений согласно политике {@link NullPolicy}:
 *     <ul>
 *       <li>{@code IGNORE} - исключает NULL-значения из условия</li>
 *       <li>{@code ADD_AS_NULL} - добавляет условие {@code IS NOT NULL}</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * @param <E> Тип сущности
 */
public class NotEqual<E> extends BaseComparisonSpecification<E> {

    public NotEqual(Object value, String... fields) {
        super(value, NullPolicy.IGNORE, DEFAULT_IGNORE_CASE, fields);
    }

    public NotEqual(Object value, boolean ignoreCase, String... fields) {
        super(value, NullPolicy.IGNORE, ignoreCase, fields);
    }

    public NotEqual(Object value, @NonNull NullPolicy nullPolicy, String... fields) {
        super(value, nullPolicy, DEFAULT_IGNORE_CASE, fields);
    }

    public NotEqual(Object value, @NonNull NullPolicy nullPolicy, boolean ignoreCase, String... fields) {
        super(value, nullPolicy, ignoreCase, fields);
    }

    @Override
    protected Predicate createPredicate(CriteriaBuilder cb, Expression<?> expression, Object value) {
        return cb.notEqual(expression, value);
    }

    @Override
    protected Predicate resolveCase(CriteriaBuilder cb, Path<Object> path, Expressions expressions, String str) {
        return ignoreCase
                ? cb.notEqual(expressions.toLower(cb, path), str.toLowerCase())
                : cb.notEqual(path, str);
    }

    @Override
    protected Predicate handleNull(CriteriaBuilder cb, Path<Object> path) {
        return switch (nullPolicy) {
            case IGNORE -> null;
            case ADD_AS_NULL -> cb.isNotNull(path);
        };
    }
}

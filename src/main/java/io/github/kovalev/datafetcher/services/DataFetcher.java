package io.github.kovalev.datafetcher.services;

import io.github.kovalev.datafetcher.utils.AttributeNode;
import io.github.kovalev.datafetcher.utils.FetchParams;
import io.github.kovalev.datafetcher.utils.FunctionParams;
import io.github.kovalev.datafetcher.utils.GroupParam;
import io.github.kovalev.specificationhelper.specifications.Equal;
import io.github.kovalev.specificationhelper.specifications.In;
import io.github.kovalev.specificationhelper.utils.PathCalculator;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Selection;
import lombok.val;
import org.hibernate.graph.GraphSemantic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.data.domain.Sort.Direction.ASC;

@Transactional(readOnly = true, noRollbackFor = Exception.class)
public class DataFetcher<E, I> {

    private static final PageRequest DEFAULT_PAGE = PageRequest.ofSize(Integer.MAX_VALUE);

    private final String idFieldName;
    private final PageRequest defaultPagerequest;
    private final EntityManager entityManager;
    private final Class<E> entityClass;
    private final Function<E, I> idFunction;
    private final EntityGraphFactory entityGraphFactory;

    public DataFetcher(EntityManager entityManager, Class<E> entityClass, Function<E, I> idFunction,
                       EntityGraphFactory entityGraphFactory) {
        this.entityManager = entityManager;
        this.entityClass = entityClass;
        this.idFunction = idFunction;
        this.idFieldName = Stream.of(entityClass.getFields())
                .filter(field -> field.getAnnotation(Id.class) != null)
                .findFirst()
                .map(Field::getName)
                .orElse("id");
        this.defaultPagerequest = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(ASC, idFieldName));
        this.entityGraphFactory = entityGraphFactory;
    }

    /**
     * Получает все идентификаторы сущностей по спецификации.
     *
     * @param specification спецификация
     * @return список идентификаторов
     */
    public List<I> fetchAllIds(@Nullable Specification<E> specification) {
        return fetchAllIds(specification, defaultPagerequest);
    }

    /**
     * Получает идентификаторы сущностей по спецификации и параметрам пагинации.
     *
     * @param specification спецификация
     * @param pageable      параметры пагинации и сортировки
     * @return список идентификаторов
     */
    public List<I> fetchAllIds(Specification<E> specification, @NonNull Pageable pageable) {
        return fetchFields(
                new FetchParams<>(
                        specification,
                        pageable,
                        list -> (I) list.getFirst(),
                        List.of(List.of(idFieldName))
                )
        );
    }

    /**
     * Получает поля из базы данных на основе предоставленных параметров.
     *
     * @param params параметры для получения полей
     * @param <D>    тип полей, которые нужно получить
     * @return список полученных полей
     */
    public <D> List<D> fetchFields(@NonNull FetchParams<E, D> params) {
        val cb = entityManager.getCriteriaBuilder();
        val query = cb.createTupleQuery();
        val root = query.from(entityClass);
        if (params.specification() != null) {
            val predicate = params.specification().toPredicate(root, query, cb);
            if (predicate != null) {
                query.where(predicate);
            }
        }
        val orders = new ArrayList<Order>();
        val selectPaths = new LinkedHashSet<Path<?>>();
        val fields = params.fields();
        for (List<String> path : fields) {
            Path<Object> objectPath = root.get(path.getFirst());
            if (path.size() > 1) {
                for (int i = 1; i < path.size(); i++) {
                    objectPath = objectPath.get(path.get(i));
                }
            }
            selectPaths.add(objectPath);
        }
        val pageable = params.pageable();
        for (Sort.Order order : pageable.getSort()) {
            var path = new PathCalculator<>(root, order.getProperty().split("\\.")).path();
            orders.add(ASC == order.getDirection() ? cb.asc(path) : cb.desc(path));
            selectPaths.add(path);
        }
        query.select(cb.tuple(selectPaths.toArray(new Selection<?>[]{})));
        query.distinct(true);
        query.orderBy(orders);

        val result = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList()
                .stream()
                .map(tuple -> {
                    List<Object> list = new ArrayList<>();
                    for (int i = 0; i < fields.size(); i++) {
                        list.add(tuple.get(i));
                    }
                    return list;
                })
                .toList();

        return result.stream().map(params::map).toList();
    }

    /**
     * Получает одну сущность по идентификатору.
     *
     * @param id идентификатор сущности
     * @return опциональное значение сущности
     */
    public Optional<E> one(I id) {
        return one(id, List.of());
    }

    /**
     * Получает одну сущность по идентификатору и имени графа сущности.
     *
     * @param id              идентификатор сущности
     * @param entityGraphName имя графа сущности
     * @return опциональное значение сущности
     */
    public Optional<E> one(I id, String entityGraphName) {
        return one(new Equal<>(id, idFieldName), entityGraphName);
    }

    /**
     * Получает одну сущность по идентификатору и списку полей.
     *
     * @param id     идентификатор сущности
     * @param attributeNodes список полей, которые нужно получить
     * @return опциональное значение сущности
     */
    public Optional<E> one(I id, List<AttributeNode> attributeNodes) {
        return one(new Equal<>(id, idFieldName), entityGraphFactory.graphByAttributeNodes(attributeNodes, entityClass));
    }


    /**
     * Получает одну сущность по спецификации и списку полей.
     *
     * @param specification спецификация для выборки сущности
     * @param attributeNodes        список полей, которые нужно получить
     * @return опциональное значение сущности
     */
    public Optional<E> one(@NonNull Specification<E> specification, List<AttributeNode> attributeNodes) {
        return one(specification, entityGraphFactory.graphByAttributeNodes(attributeNodes, entityClass));
    }

    /**
     * Получает одну сущность по спецификации и имени графа сущности
     *
     * @param specification   спецификация
     * @param entityGraphName имя графа сущности
     * @return полученная сущность
     */
    public Optional<E> one(@NonNull Specification<E> specification, String entityGraphName) {
        return one(specification, entityGraphFactory.graphByName(entityGraphName));
    }

    /**
     * Получение сущности по спецификации.
     *
     * @param specification спецификация
     * @return полученная сущность
     */
    public Optional<E> one(@NonNull Specification<E> specification) {
        return one(specification, List.of());
    }

    /**
     * Получает список сущностей по идентификаторам.
     *
     * @param ids коллекция идентификаторов
     * @return список сущностей
     */
    public List<E> fetchAllByIds(Collection<I> ids) {
        return fetchAllByIds(ids, List.of());
    }

    /**
     * Получает список сущностей по идентификаторам и имени графа сущности.
     *
     * @param ids коллекция идентификаторов
     * @return список сущностей
     */
    public List<E> fetchAllByIds(Collection<I> ids, String entityGraphName) {
        return fetchAllByIds(ids, entityGraphFactory.graphByName(entityGraphName));
    }

    /**
     * Получает список сущностей по идентификаторам и графу сущности.
     *
     * @param ids коллекция идентификаторов
     * @return список сущностей
     */
    public List<E> fetchAllByIds(Collection<I> ids, List<AttributeNode> attributeNodes) {
        return fetchAllByIds(ids, entityGraphFactory.graphByAttributeNodes(attributeNodes, entityClass));
    }

    /**
     * Получает список сущностей по спецификации, сортировке и имени графа сущности.
     *
     * @param specification   спецификация для выборки сущностей
     * @param sort            сортировка для выборки сущностей
     * @param entityGraphName имя графа сущности
     * @return список сущностей
     */
    public List<E> list(@NonNull Specification<E> specification, @NonNull Sort sort, String entityGraphName) {
        return fetchEntities(specification, sort, entityGraphFactory.graphByName(entityGraphName));
    }

    /**
     * Получает список сущностей по спецификации, сортировке и списку полей.
     *
     * @param specification спецификация для выборки сущностей
     * @param sort          сортировка для выборки сущностей
     * @param attributeNodes        список полей, которые нужно получить
     * @return список сущностей
     */
    public List<E> list(@NonNull Specification<E> specification, @NonNull Sort sort, List<AttributeNode> attributeNodes) {
        return fetchEntities(specification, sort, entityGraphFactory.graphByAttributeNodes(attributeNodes, entityClass));
    }

    /**
     * Получает список сущностей по спецификации и имени графа сущности.
     *
     * @param specification   спецификация для выборки сущностей
     * @param entityGraphName имя графа сущности
     * @return список сущностей
     */
    public List<E> list(@NonNull Specification<E> specification, String entityGraphName) {
        return fetchEntities(specification, Sort.unsorted(), entityGraphFactory.graphByName(entityGraphName));
    }

    /**
     * Получает список сущностей по спецификации и списку полей.
     *
     * @param specification спецификация для выборки сущностей
     * @param attributeNodes        список полей, которые нужно получить
     * @return список сущностей
     */
    public List<E> list(@NonNull Specification<E> specification, List<AttributeNode> attributeNodes) {
        return fetchEntities(specification, Sort.unsorted(), entityGraphFactory.graphByAttributeNodes(attributeNodes, entityClass));
    }

    /**
     * Получает список сущностей по спецификации.
     *
     * @param specification спецификация для выборки сущностей
     * @return список сущностей
     */
    public List<E> list(@NonNull Specification<E> specification) {
        return fetchEntities(specification, Sort.unsorted(), null);
    }

    /**
     * Получает страницу сущностей по спецификации и параметрам пагинации.
     *
     * @param specification спецификация для выборки сущностей
     * @param pageable      параметры пагинации
     * @return страница сущностей
     */
    public Page<E> page(Specification<E> specification, @NonNull Pageable pageable) {
        return page(specification, pageable, List.of());
    }

    /**
     * Получает страницу сущностей по спецификации, параметрам пагинации и списку полей.
     *
     * @param specification спецификация для выборки сущностей
     * @param pageable      параметры пагинации
     * @param attributeNodes        список полей, которые нужно получить
     * @return страница сущностей
     */
    public Page<E> page(Specification<E> specification, @NonNull Pageable pageable, List<AttributeNode> attributeNodes) {
        return page(specification, pageable, entityGraphFactory.graphByAttributeNodes(attributeNodes, entityClass));
    }

    /**
     * Получает страницу сущностей по спецификации, имени графа сущности и параметрам пагинации.
     *
     * @param specification   спецификация для выборки сущностей
     * @param entityGraphName имя графа сущности
     * @param pageable        параметры пагинации
     * @return страница сущностей
     */
    public Page<E> page(Specification<E> specification, @NonNull Pageable pageable, String entityGraphName) {
        return page(specification, pageable, entityGraphFactory.graphByName(entityGraphName));
    }

    /**
     * Получает срез сущностей по спецификации и параметрам пагинации.
     *
     * @param specification спецификация для выборки сущностей
     * @param pageable      параметры пагинации
     * @return срез сущностей
     */
    public Slice<E> slice(Specification<E> specification, @NonNull Pageable pageable) {
        return slice(specification, pageable, List.of());
    }

    /**
     * Получает срез сущностей по спецификации, параметрам пагинации и списку полей.
     *
     * @param specification спецификация для выборки сущностей
     * @param pageable      параметры пагинации
     * @param attributeNodes        список полей, которые нужно получить
     * @return срез сущностей
     */
    public Slice<E> slice(Specification<E> specification, @NonNull Pageable pageable, List<AttributeNode> attributeNodes) {
        return slice(specification, pageable, entityGraphFactory.graphByAttributeNodes(attributeNodes, entityClass));
    }

    /**
     * Получает срез сущностей по спецификации, имени графа сущности и параметрам пагинации.
     *
     * @param specification   спецификация для выборки сущностей
     * @param entityGraphName имя графа сущности
     * @param pageable        параметры пагинации
     * @return срез сущностей
     */
    public Slice<E> slice(Specification<E> specification, @NonNull Pageable pageable, String entityGraphName) {
        return slice(specification, pageable, entityGraphFactory.graphByName(entityGraphName));
    }

    /**
     * Получение групп данных.
     *
     * @param specification спецификация
     * @param groupParams   поля для группировки
     * @param mapper        функция для преобразования данных
     * @param <D>           тип данных
     * @return список данных
     */
    public <D> List<D> groups(Specification<E> specification,
                              List<GroupParam> groupParams,
                              Function<Map<String, ?>, D> mapper) {
        return groups(specification, DEFAULT_PAGE, groupParams, mapper, List.of());
    }

    /**
     * Получение групп данных.
     *
     * @param specification  спецификация
     * @param groupParams    поля для группировки
     * @param mapper         функция для преобразования данных
     * @param functionParams параметры функции
     * @param <D>            тип данных
     * @return список данных
     */
    public <D> List<D> groups(Specification<E> specification,
                              List<GroupParam> groupParams,
                              Function<Map<String, ?>, D> mapper,
                              FunctionParams functionParams) {
        return groups(specification, DEFAULT_PAGE, groupParams, mapper, List.of(functionParams));
    }

    /**
     * Получение групп данных.
     *
     * @param specification  спецификация
     * @param groupParams    поля для группировки
     * @param mapper         функция для преобразования данных
     * @param functionParams параметры функции
     * @param <D>            тип данных
     * @return список данных
     */
    public <D> List<D> groups(Specification<E> specification,
                              List<GroupParam> groupParams,
                              Function<Map<String, ?>, D> mapper,
                              List<FunctionParams> functionParams) {
        return groups(specification, DEFAULT_PAGE, groupParams, mapper, functionParams);
    }

    /**
     * Получение групп данных.
     *
     * @param specification  спецификация
     * @param pageable       параметры пагинации и сортировки
     * @param groupParams    поля для группировки
     * @param mapper         функция для преобразования данных
     * @param functionParams параметры функции
     * @param <D>            тип данных
     * @return список данных
     */
    public <D> List<D> groups(Specification<E> specification,
                              Pageable pageable,
                              List<GroupParam> groupParams,
                              Function<Map<String, ?>, D> mapper,
                              FunctionParams functionParams) {
        return groups(specification, pageable, groupParams, mapper, List.of(functionParams));
    }

    /**
     * Получение групп данных.
     *
     * @param specification  спецификация
     * @param pageable       параметры пагинации и сортировки
     * @param groupParams    выражения или поля для группировки
     * @param mapper         функция для преобразования данных
     * @param functionParams параметры функции
     * @param <D>            тип данных
     * @return список данных
     */
    public <D> List<D> groups(Specification<E> specification,
                              Pageable pageable,
                              List<GroupParam> groupParams,
                              Function<Map<String, ?>, D> mapper,
                              List<FunctionParams> functionParams) {
        val cb = entityManager.getCriteriaBuilder();
        val query = cb.createTupleQuery();
        val root = query.from(entityClass);
        // Формируем условия выборки
        if (specification != null) {
            val predicate = specification.toPredicate(root, query, cb);
            if (predicate != null) {
                query.where(predicate);
            }
        }
        // Формируем поля для выборки и группировки
        List<Selection<?>> selectPaths = new ArrayList<>();
        List<Expression<?>> groupByPaths = new ArrayList<>();
        for (var groupParam : groupParams) {
            Expression<?> expression = groupParam.expression(root, cb);
            selectPaths.add(expression);
            groupByPaths.add(expression);
        }
        if (selectPaths.isEmpty()) {
            selectPaths.add(cb.literal("Без группировки"));
        }

        // Формируем порядок сортировки
        val orders = new ArrayList<Order>();
        for (var order : pageable.getSort()) {
            String[] properties = order.getProperty().split("\\.");
            var path = new PathCalculator<>(root, properties).path();
            orders.add(ASC == order.getDirection() ? cb.asc(path) : cb.desc(path));
            if (!selectPaths.contains(path)) {
                selectPaths.add(path);
            }
        }
        // Формируем функцию аггрегации
        functionParams.stream()
                .map(functionParam -> functionParam.expression(root, cb))
                .forEach(selectPaths::add);

        // Формируем запрос
        query.multiselect(selectPaths)
                .groupBy(groupByPaths)
                .orderBy(orders);
        return entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList()
                .stream()
                .map(tuple -> {
                    Map<String, Object> map = new HashMap<>();
                    for (var groupParam : groupParams) {
                        map.put(groupParam.getAlias(), tuple.get(groupParam.getAlias()));
                    }

                    // Конвертируем результат аггрегации в ожидаемый тип
                    for (var functionParam : functionParams) {
                        val object = functionParam.getMapper().apply(tuple.get(functionParam.getAlias()));
                        map.put(functionParam.getAlias(), object);
                    }
                    return map;
                })
                .map(mapper)
                .toList();
    }

    private Page<E> page(Specification<E> specification, Pageable pageable, EntityGraph<?> graph) {
        val total = total(specification);

        if (total == 0) {
            return emptyResult(pageable, total);
        }

        List<I> ids = fetchAllIds(specification, pageable);

        if (ids.isEmpty()) {
            return emptyResult(pageable, total);
        }

        Map<I, E> idMap = fetchAllByIds(ids, graph).stream()
                .collect(Collectors.toMap(idFunction, Function.identity()));

        List<E> data = ids.stream().map(idMap::get).toList();

        return new PageImpl<>(data, pageable, total);
    }

    private Slice<E> slice(Specification<E> specification, Pageable pageable, EntityGraph<?> graph) {
        List<I> ids = fetchAllIds(specification, PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize() + 1,
                pageable.getSort()
        ));

        boolean hasNext = ids.size() > pageable.getPageSize();

        List<I> pageIds = hasNext ? ids.subList(0, pageable.getPageSize()) : ids;

        if (pageIds.isEmpty()) {
            return new SliceImpl<>(List.of(), pageable, hasNext);
        }

        Map<I, E> idMap = fetchAllByIds(pageIds, graph).stream()
                .collect(Collectors.toMap(idFunction, Function.identity()));

        List<E> content = pageIds.stream().map(idMap::get).toList();

        return new SliceImpl<>(content, pageable, hasNext);
    }

    private List<E> fetchAllByIds(Collection<I> ids, EntityGraph<?> graph) {
        return fetchEntities(new In<>(ids, idFieldName), Sort.unsorted(), graph);
    }

    private List<E> fetchEntities(Specification<E> spec, Sort sort, EntityGraph<?> graph) {
        val cb = entityManager.getCriteriaBuilder();
        val query = cb.createQuery(entityClass);
        val root = query.from(entityClass);

        Predicate predicate = spec.toPredicate(root, query, cb);

        if (predicate != null) {
            query.where(predicate);
        }

        List<Order> orders = sort.stream()
                .map(order -> {
                    Path<Object> path = new PathCalculator<>(root, order.getProperty().split("\\.")).path();
                    return order.isAscending() ? cb.asc(path) : cb.desc(path);
                })
                .toList();
        query.orderBy(orders);

        val typedQuery = entityManager.createQuery(query);

        if (graph != null) {
            typedQuery.setHint(GraphSemantic.FETCH.getJakartaHintName(), graph);
        }

        List<E> resultList = typedQuery.getResultList();

        resultList.forEach(entityManager::detach);

        return resultList;
    }

    private long total(Specification<E> specification) {
        val cb = entityManager.getCriteriaBuilder();
        val query = cb.createQuery(Long.class);
        val root = query.from(entityClass);
        val predicate = specification.toPredicate(root, query, cb);

        query.distinct(true);
        query.select(cb.countDistinct(root));

        if (predicate != null) {
            query.where(predicate);
        }

        return entityManager.createQuery(query).getSingleResult();
    }

    private Optional<E> one(Specification<E> specification, EntityGraph<?> entityGraph) {
        val cb = entityManager.getCriteriaBuilder();
        val query = cb.createQuery(entityClass);
        val root = query.from(entityClass);

        val predicate = specification.toPredicate(root, query, cb);

        if (predicate != null) {
            query.where(predicate);
        }

        val typedQuery = entityManager.createQuery(query);

        if (entityGraph != null) {
            typedQuery.setHint(GraphSemantic.FETCH.getJakartaHintName(), entityGraph);
        }

        Optional<E> optional = typedQuery.getResultStream().findFirst();

        optional.ifPresent(entityManager::detach);

        return optional;
    }

    private PageImpl<E> emptyResult(Pageable pageable, long total) {
        return new PageImpl<>(List.of(), PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()), total);
    }
}

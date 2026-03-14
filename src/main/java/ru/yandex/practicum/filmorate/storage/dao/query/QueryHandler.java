package ru.yandex.practicum.filmorate.storage.dao.query;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
@PropertySource("classpath:sql_queries.properties")
public class QueryHandler {

    private final Environment env;
    private final Map<Query, String> queries = new EnumMap<>(Query.class);

    public QueryHandler(Environment env) {
        this.env = env;
    }

    @PostConstruct
    private void init() {
        for (Query query : Query.values()) {
            String sql = env.getProperty(query.name());
            if (sql == null) {
                throw new IllegalStateException("Не найден SQL запрос для: " + query.name());
            }
            queries.put(query, sql);
        }
    }

    public String get(Query query) {
        return queries.get(query);
    }
}

package it.r.ports.utils.beans;

import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public class BeanBuilder<T> {

    private final Class<T> type;

    private final Map<String, Object> map = new HashMap<>();

    public final BeanBuilder<T> with(String key, Optional<?> value) {
        value.ifPresent(v -> map.put(key, v));
        return this;
    }

    public final BeanBuilder<T> with(String key, Object value) {
        map.put(key, value);
        return this;
    }

    public T build() throws MappingException {
        return BeanUtils.newInstance(type, map);
    }

}

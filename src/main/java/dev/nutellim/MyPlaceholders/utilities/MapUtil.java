package dev.nutellim.MyPlaceholders.utilities;

import lombok.experimental.UtilityClass;

import java.util.*;

@UtilityClass
public class MapUtil {

    @SafeVarargs
    public <K, V> LinkedHashMap<K, V> ofOrderMap(Map.Entry<K, V>... entries) {
        LinkedHashMap<K, V> map = new LinkedHashMap<>();

        for (Map.Entry<K, V> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }

        return map;
    }

    public <K, V> Map.Entry<K, V> entry(K k, V v) {
        return new AbstractMap.SimpleImmutableEntry<>(k, v);
    }

    public <K, V> Map<K, V> of(Object... entries) {
        if (entries.length % 2 != 0) {
            throw new IllegalArgumentException("Invalid number of arguments provided");
        }

        Map<K, V> map = new HashMap<>();

        for (int i = 0; i < entries.length; i += 2) {
            K key = (K) entries[i];
            V value = (V) entries[i + 1];
            map.put(key, value);
        }

        return Collections.unmodifiableMap(map);
    }
}

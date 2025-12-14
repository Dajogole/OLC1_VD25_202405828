package utils;

import java.util.HashMap;
import java.util.Map;

public class CaseInsensitiveMap<V> extends HashMap<String, V> {
    
    @Override
    public V put(String key, V value) {
        return super.put(key.toLowerCase(), value);
    }
    
    @Override
    public V get(Object key) {
        if (key instanceof String) {
            return super.get(((String) key).toLowerCase());
        }
        return super.get(key);
    }
    
    @Override
    public boolean containsKey(Object key) {
        if (key instanceof String) {
            return super.containsKey(((String) key).toLowerCase());
        }
        return super.containsKey(key);
    }
    
    @Override
    public V remove(Object key) {
        if (key instanceof String) {
            return super.remove(((String) key).toLowerCase());
        }
        return super.remove(key);
    }
}
package httpserver.itf.impl;

import java.util.HashMap;
import java.util.Map;
import httpserver.itf.HttpSession;

public class Session implements HttpSession {
    
    private String m_id;
    private Map<String, Object> m_data;
    private long m_lastAccess;
    
    public Session(String id) {
        m_id = id;
        m_data = new HashMap<>();
        m_lastAccess = System.currentTimeMillis();
    }
    
    @Override
    public String getId() {
        return m_id;
    }
    
    @Override
    public Object getValue(String name) {
        return m_data.get(name);
    }
    
    @Override
    public void setValue(String name, Object value) {
        m_data.put(name, value);
    }
    
    // @Override
    // public void removeValue(String name) {
    //     m_data.remove(name);
    // }
    
    public void updateAccess() {
        m_lastAccess = System.currentTimeMillis();
    }
    
    public long getLastAccess() {
        return m_lastAccess;
    }
}
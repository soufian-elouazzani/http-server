package httpserver.itf.impl;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import httpserver.itf.HttpRicmlet;

public class Application {
    
    private String m_name;
    private URLClassLoader m_classLoader;
    private Map<String, HttpRicmlet> m_ricmlets;
    
    public Application(String name, File jarFile, ClassLoader parent) throws Exception {
        m_name = name;
        m_ricmlets = new HashMap<>();
        
        // Create classloader for this application
        m_classLoader = new URLClassLoader(
            new URL[] { jarFile.toURI().toURL() },
            parent
        );
    }
    
    public HttpRicmlet getInstance(String className) throws Exception {
        // Check if we already have an instance (singleton per application)
        if (m_ricmlets.containsKey(className)) {
            return m_ricmlets.get(className);
        }
        
        // Load class using application's classloader
        Class<?> c = m_classLoader.loadClass(className);
        HttpRicmlet ricmlet = (HttpRicmlet) c.getDeclaredConstructor().newInstance();
        m_ricmlets.put(className, ricmlet);
        return ricmlet;
    }
    
    public String getName() {
        return m_name;
    }
}
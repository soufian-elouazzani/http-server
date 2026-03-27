package httpserver.itf.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import httpserver.itf.HttpRicmlet;
import httpserver.itf.HttpRicmletRequest;
import httpserver.itf.HttpSession;

public class HttpRicmletRequestImpl extends HttpRicmletRequest {

    private Map<String, String> m_args = new HashMap<>();
    private Map<String, String> m_cookies = new HashMap<>();
    private HttpSession m_session;
    private String m_appName;      
    private String m_className;    

    public HttpRicmletRequestImpl(HttpServer hs, String method, String ressname, BufferedReader br) throws IOException {
        super(hs, method, ressname, br);

        // Parser les arguments ?name=Bob&surname=Marley
        if (ressname.contains("?")) {
            String queryString = ressname.split("\\?")[1];
            for (String param : queryString.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2) {
                    m_args.put(pair[0], pair[1]);
                }
            }
        }

        // Parse Cookies From headers
        String line;
        while ((line = br.readLine()) != null && !line.isEmpty()) {
            if (line.startsWith("Cookie:")) {
                String cookieLine = line.substring(7); // Remove "Cookie: "
                String[] cookies = cookieLine.split(";");
                for (String cookie : cookies) {
                    String[] pair = cookie.trim().split("=");
                    if (pair.length == 2) {
                        m_cookies.put(pair[0], pair[1]);
                    }
                }
            }
        }

        // STEP 5: Parse application URL
        // URL format: /ricmlets/<appName>/<className>
        String path = ressname;
        if (path.startsWith("/ricmlets/")) {
            path = path.substring("/ricmlets/".length());
            String[] parts = path.split("/", 2);
            if (parts.length == 2) {
                m_appName = parts[0];
                m_className = parts[1].replace("/", ".");

                String className = parts[1].replace("/", ".");
                if (!className.contains(".")) {
                    m_className = "examples." + className;
                } else {
                    m_className = className;
                }
            }
        }

        // STEP 4: Get or create session 
        String sessionId = getCookie("SESSIONID");
        if (sessionId != null) {
            m_session = m_hs.getSession(sessionId);
        }
        
        if (m_session == null) {
            m_session = m_hs.createSession();
        }
    }

    public String getAppName() {
        return m_appName;
    }
    
    public String getClassName() {
        return m_className;
    }

    @Override
    public String getArg(String name) {
        return m_args.get(name);
    }

    @Override
    public String getCookie(String name) {
        return m_cookies.get(name); // Step 3
    }

    @Override
    public HttpSession getSession() {
        return m_session; // Step 4
    }

    @Override
    public void process(httpserver.itf.HttpResponse resp) throws Exception {
        // Extraire le nom de la classe (sans les arguments)
        String ressname = m_ressname;
        if (ressname.contains("?")) {
            ressname = ressname.split("\\?")[0];
        }

        // URL : /ricmlets/examples/HelloRicmlet
        // → classe : examples.HelloRicmlet
        String className = ressname.substring("/ricmlets/".length()).replace("/", ".");

        if (m_appName == null || m_className == null) {
            resp.setReplyError(404, "Invalid ricmlet URL format");
            return;
        }

        try {
             // STEP 5: Get application and ricmlet instance
            Application app = m_hs.getApplication(m_appName);
            HttpRicmlet ricmlet = app.getInstance(m_className);
            
            // HttpRicmlet ricmlet = m_hs.getInstance(className);
            HttpRicmletResponseImpl ricmletResp = new HttpRicmletResponseImpl(m_hs, this, 
                ((HttpResponseImpl) resp).m_ps);
            ricmlet.doGet(this, ricmletResp);

        } catch (ClassNotFoundException e) {
            resp.setReplyError(404, "Ricmlet not found");
        }
    }
}
package httpserver.itf.impl;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import httpserver.itf.HttpRicmletResponse;
import httpserver.itf.HttpSession;
import httpserver.itf.HttpRequest;

public class HttpRicmletResponseImpl extends HttpResponseImpl implements HttpRicmletResponse {

    private List<String> m_cookies;
    private List<String> m_headers;
    private boolean headersSent;
    private HttpRicmletRequestImpl m_req;

    public HttpRicmletResponseImpl(HttpServer hs, HttpRicmletRequestImpl req, PrintStream ps) {
        super(hs, req, ps);
        m_req = req;
        m_cookies = new ArrayList<>();
        m_headers = new ArrayList<>();
        headersSent = false;

         HttpSession session = m_req.getSession();
        if (session != null) {
            setCookie("SESSIONID", session.getId());
        }
    }

    @Override
    public void setCookie(String name, String value) {
        m_cookies.add("Set-Cookie: " + name + "=" + value);
    }

    @Override
    public void setReplyOk() {
        m_headers.add("HTTP/1.0 200 OK");
        m_headers.add("Date: " + new java.util.Date());
        m_headers.add("Server: ricm-http 1.0");
    }

    @Override
    public void setContentType(String type) {
        m_headers.add("Content-type: " + type);
    }

    @Override
    public PrintStream beginBody() {
        if (!headersSent) {
            // Write all headers
            for (String header : m_headers) {
                m_ps.println(header);
            }
            // Write all cookies
            for (String cookie : m_cookies) {
                m_ps.println(cookie);
            }
            headersSent = true;
        }
        return super.beginBody();
    }
}
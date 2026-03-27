package examples;

import java.io.IOException;
import java.io.PrintStream;
import httpserver.itf.HttpRicmlet;
import httpserver.itf.HttpRicmletRequest;
import httpserver.itf.HttpRicmletResponse;

public class MyFirstCookieRicmlet implements HttpRicmlet {

    @Override
    public void doGet(HttpRicmletRequest req, HttpRicmletResponse resp) throws IOException {
        
        String myFirstCookie = req.getCookie("MyFirstCookie");
        
        if (myFirstCookie == null) {
            resp.setCookie("MyFirstCookie", "1");
        } else {
            int n = Integer.valueOf(myFirstCookie);
            resp.setCookie("MyFirstCookie", new Integer(n + 1).toString());
        }
        
        resp.setReplyOk();
        resp.setContentType("text/html");
        PrintStream ps = resp.beginBody();
        
        ps.println("<HTML><HEAD><TITLE>Ricmlet processing</TITLE></HEAD>");
        ps.println("<BODY><H4>MyFirstCookie: " + req.getCookie("MyFirstCookie") + "</H4>");
        ps.println("</BODY></HTML>");
    }
}
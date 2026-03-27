package httpserver.itf.impl;

import java.io.File;
import java.io.IOException;


import httpserver.itf.HttpRequest;
import httpserver.itf.HttpResponse;

/*
 * This class allows to build an object representing an HTTP static request
 */
public class HttpStaticRequest extends HttpRequest {
	static final String DEFAULT_FILE = "index.html";
	
	public HttpStaticRequest(HttpServer hs, String method, String ressname) throws IOException {
		super(hs, method, ressname);
	}
	
	public void process(HttpResponse resp) throws Exception {
		String ressname = m_ressname;
		if (ressname.endsWith("/")) {
	        ressname = ressname + DEFAULT_FILE;
	    }
		File file = new File(m_hs.getFolder(), ressname);
		if (!file.exists() || !file.isFile()) {
	        resp.setReplyError(404, "File not found");
	        return;
	    }
		byte[] content = java.nio.file.Files.readAllBytes(file.toPath());
		resp.setReplyOk();
	    resp.setContentLength(content.length);
	    resp.setContentType(HttpRequest.getContentType(ressname));
	    java.io.PrintStream ps = resp.beginBody();
	    ps.write(content);
	    ps.flush();
	    
	}

}

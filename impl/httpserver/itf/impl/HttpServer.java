package httpserver.itf.impl;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

import httpserver.itf.HttpRequest;
import httpserver.itf.HttpResponse;
import httpserver.itf.HttpRicmlet;


/**
 * Basic HTTP Server Implementation 
 * 
 * Only manages static requests
 * The url for a static ressource is of the form: "http//host:port/<path>/<ressource name>"
 * For example, try accessing the following urls from your brower:
 *    http://localhost:<port>/
 *    http://localhost:<port>/voile.jpg
 *    ...
 */
public class HttpServer {

	private int m_port;
	private File m_folder;  // default folder for accessing static resources (files)
	private ServerSocket m_ssoc;
	private Map<String, HttpRicmlet> m_ricmlets = new HashMap<>();

	private Map<String, Application> m_applications = new HashMap<>();
    private File m_appFolder;


	//*******************Session handelling************************//
	private Map<String, Session> m_sessions = new HashMap<>();
    private static final long SESSION_TIMEOUT = 300000; // 5 minutes


	public Session getSession(String sessionId) {
        if (sessionId != null && m_sessions.containsKey(sessionId)) {
            Session session = m_sessions.get(sessionId);
            session.updateAccess();
            return session;
        }
        return null;
    }
    
    public Session createSession() {
        String sessionId = UUID.randomUUID().toString();
        Session session = new Session(sessionId);
        m_sessions.put(sessionId, session);
        return session;
    }
    
    public void cleanExpiredSessions() {
        long now = System.currentTimeMillis();
        m_sessions.entrySet().removeIf(entry -> 
            (now - entry.getValue().getLastAccess()) > SESSION_TIMEOUT);
    }
	//********************End Session handling***********************//

	protected HttpServer(int port, String folderName, String appFolderName) {
		m_port = port;

		// Static files folder
		if (!folderName.endsWith(File.separator)) 
			folderName = folderName + File.separator;
		m_folder = new File(folderName);

		// Applications folder
        if (!appFolderName.endsWith(File.separator)) 
            appFolderName = appFolderName + File.separator;
        m_appFolder = new File(appFolderName);
		
		try {
			m_ssoc=new ServerSocket(m_port);
			System.out.println("HttpServer started on port " + m_port);
			System.out.println("Static files: " + m_folder.getAbsolutePath());
            System.out.println("Applications: " + m_appFolder.getAbsolutePath());
		} catch (IOException e) {
			System.out.println("HttpServer Exception:" + e );
			System.exit(1);
		}
	}


	// Get or create application
    public Application getApplication(String appName) throws Exception {
        if (m_applications.containsKey(appName)) {
            return m_applications.get(appName);
        }
        
        // Look for jar file: apps/appName.jar
        File jarFile = new File(m_appFolder, appName + ".jar");
        if (!jarFile.exists()) {
            throw new Exception("Application not found: " + appName);
        }
        
        Application app = new Application(appName, jarFile, this.getClass().getClassLoader());
        m_applications.put(appName, app);
        return app;
    }


	
	public File getFolder() {
		return m_folder;
	}
	
	

	public HttpRicmlet getInstance(String clsname)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, MalformedURLException, 
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if (m_ricmlets.containsKey(clsname)) {
	        return m_ricmlets.get(clsname);
	    }
	    // Sinon créer une nouvelle instance
	    Class<?> c = Class.forName(clsname);
	    HttpRicmlet ricmlet = (HttpRicmlet) c.getDeclaredConstructor().newInstance();
	    m_ricmlets.put(clsname, ricmlet);
	    return ricmlet;
	}




	/*
	 * Reads a request on the given input stream and returns the corresponding HttpRequest object
	 */
	public HttpRequest getRequest(BufferedReader br) throws IOException {
		HttpRequest request = null;
		
		String startline = br.readLine();
		StringTokenizer parseline = new StringTokenizer(startline);
		String method = parseline.nextToken().toUpperCase(); 
		String ressname = parseline.nextToken();
		if (method.equals("GET")) {
			if (ressname.startsWith("/ricmlets/")) {
	            request = new HttpRicmletRequestImpl(this, method, ressname, br);
	        } else {
	            request = new HttpStaticRequest(this, method, ressname);
	        }
		} else 
			request = new UnknownRequest(this, method, ressname);
		return request;
	}


	/*
	 * Returns an HttpResponse object associated to the given HttpRequest object
	 */
	public HttpResponse getResponse(HttpRequest req, PrintStream ps) {
		return new HttpResponseImpl(this, req, ps);
	}


	/*
	 * Server main loop
	 */
	protected void loop() {
		try {
			while (true) {
				Socket soc = m_ssoc.accept();
				(new HttpWorker(this, soc)).start();
			}
		} catch (IOException e) {
			System.out.println("HttpServer Exception, skipping request");
			e.printStackTrace();
		}
	}

	
	
	public static void main(String[] args) {
		int port = 0;
		if (args.length != 3) {
			System.out.println("Usage: java Server <port-number> <static folder> <app folder>");
		} else {
			port = Integer.parseInt(args[0]);
			String foldername = args[1];
			String appFolder = args[2];
			HttpServer hs = new HttpServer(port, foldername, appFolder);
			hs.loop();
		}
	}

}


/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.lib.collab.util;

import java.net.*;
import java.io.*;
import java.util.*;

/**
 * the worlds simplist and most ineffecient web server
 *
 *
 *
 * @author Rahul Shah
 *
 */
public class Http extends Thread  {

    /**
     * active client socket
     */
    Socket s;

    /**
     * default file
     */
    final static String index = "index.html";

    /**
     * root directory to serv from or file if only one file is to be served
     */
    private File rootFile;

    private static Hashtable _servers = new Hashtable();

    /**
     * create a client connection
     * @param file The root directory to serv from or file if only one file is to be served
     * @param s
     */
    public Http(File file, Socket s) {
        this.s = s;
        rootFile = file;
    }

    /**
     * start the http server as application
     *
     * @param args
     */
    public static void main(String[] args) {

        String port = "9980";
        String root = ".";

        for (int x = 0; x < args.length; x++) {
            if (args[x].equals("-port")) {
                port = args[x++];
            }
            if (args[x].equals("-root")) {
                root = args[x++];
            }
        }
	HostPort hp = new HostPort(port, 9980);
        try {
            startServ(root,hp);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * start the http server
     *
     * @param filepath - root directory to serv from or the file path if only one file is to be served
     * @param port - port to listne on
     * @param i - address to bind server to
     * @throws IOException if Unable to start the server
     */
    public static void startServ(String filepath, HostPort hp) throws IOException
    {
        HTTPAcceptor acceptor = (HTTPAcceptor)_servers.get(hp);
        if (acceptor != null) {
            //System.out.println("HTTP port alreary enabled");
            return;
        }
        File file = new File(filepath);
        if (!file.exists()) {
            //System.out.println("Invalid HTTP doc root: " + file.getAbsolutePath() + " does not exists");
            return;
        }
        acceptor = new HTTPAcceptor(file, hp);
        acceptor.startServ();
	Thread t = new Thread(acceptor);
        _servers.put(hp, acceptor);
        t.setDaemon(true);
	t.start();
    }

    static class HTTPAcceptor implements Runnable
    {
	private HostPort _hp;
        private boolean running;
        private ServerSocket serv;
        private File file;
        
	HTTPAcceptor(File f, HostPort hp) {
	    _hp = hp;
            file = f;
	}
        
        public void startServ() throws IOException {
            int port = _hp.getPort();
            InetAddress i = _hp.getHost();
            serv = new ServerSocket(port, 10, i);
            running = true;
            if (port == 0) {
                _hp.setPort(serv.getLocalPort());
            }
            //System.out.println("HTTP port accepting connections: " + serv.getLocalPort() +
            //     " : " + file.getAbsolutePath());
        }
        
	public void run() {
	    try {
		while (running) {
		    Http n = new Http(file, serv.accept());
                    n.setDaemon(true);
		    n.start();
		}
	    }
	    catch (IOException e) {
		//if (running == true) System.out.println("HTTP listener error: " + e);
	    }
	    stopServ();
	}
        
        boolean isRunning() {
            return running;
        }
        
        /**
         * stop the http server
         */
        void stopServ() {
            if (running == false) return;
            running = false;
            if (serv != null) {
                try { serv.close(); } catch (Exception e) { }
            }
            serv = null;
            //System.out.println("Stopping HTTP listener on port " + _hp.getPort());
        }
    }

    /**
     * stop the http server
     */
    public static void stopServ(HostPort hp) {
        HTTPAcceptor acceptor = (HTTPAcceptor)_servers.get(hp);
        if (acceptor != null) {
            acceptor.stopServ();
        }
    }

    /**
     * server content to a client
     */
    public void run() {
        String version = "";

        try {
            PrintStream os = new PrintStream(new BufferedOutputStream(s.getOutputStream()));
            BufferedReader is = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String line = is.readLine();
	    if (line == null) return;
            StringTokenizer st = new StringTokenizer(line);
            String method = st.nextToken();
            String fileName = st.nextToken();
            if (st.hasMoreTokens()) {
                version = st.nextToken();
            }
            //read header but ignore
            while ((line = is.readLine()) != null) {
                if (line.trim().equals("")) break;
            }

	    //System.out.println("HTTP request = " + method + " " + fileName);

            //we only do gets
            if (method.equals("GET") || method.equals("HEAD")) {
                File file;
                FileInputStream fs = null;
		boolean headOnly = false;
		if (method.equals("HEAD")) {
		    headOnly = true;
		}
                try {
                    if (fileName.endsWith("/")) fileName += index;
                    fileName = fileName.substring(1,fileName.length());
                    if (rootFile.isFile()) {
                        if (rootFile.getName().equals(fileName)) {
                            file = rootFile;
                            fs = new FileInputStream(file);
                        } else {
                            //send an unauthorized message
                            file = null;
                        }
                    } else if (rootFile.isDirectory()) {
                        file = new File(rootFile, fileName);
                        if (file.isDirectory()) file = new File(file, index);
                        fs = new FileInputStream(file);
                    } else {
                        file = null;
                    }
                }
                catch (IOException e) {
                    //could not open file
                    file = null;
                    //not a get so output error
                    if (version.startsWith("HTTP/")) { //send headers
                        String header = "HTTP/1.0 404 File Not Found\r\n";
                        header += standardHeaders();
                        header += "Content-type: text/html\r\n\r\n";
                        os.print(header);
                    }
                    os.println("<HTML><HEAD><TITLE>File Not Found</TITLE><HEAD>");
                    os.println("<BODY><H1>HTTP Error 404: File Not Found</H1></BODY></HTML>");
                    os.close();
                    //System.out.println( "Requested HTTP File: >" + fileName + "< not found");
                }
                if (file != null) {
                    if (version.startsWith("HTTP/")) { //send headers
                        String header = "HTTP/1.0 200 OK\r\n";
                        header += standardHeaders();
                        header += "Content-length: " + file.length() + "\r\n";
                        header += "Content-type: " + contentType(fileName) + "\r\n\r\n";
                        os.print(header);
                    }
		    if (!headOnly) {
			byte[] data = new byte[1024];
			int r;
			while ((r = fs.read(data)) > 0) {
			    os.write(data,0,r);
			}
		    }
                    os.flush();
                    os.close();
                    fs.close();
		    s.close();
                    //System.out.println("HTTP File: >" + fileName + "<  size: " + file.length() + " bytes - Content-type: " + contentType(fileName));
                }
            } else {
                //not a get/head so output error
                if (version.startsWith("HTTP/")) { //send headers
                    String header = "HTTP/1.0 501 Not Implemented\r\n";
                    header += standardHeaders();
                    header += "Content-type: text/html\r\n\r\n";
                    os.print(header);
                }
                os.println("<HTML><HEAD><TITLE>Not Implemented</TITLE><HEAD>");
                os.println("<BODY><H1>HTTP Error 501: Not Implemented</H1></BODY></HTML>");
                os.close();
                //System.out.println( "HTTP only supports GET or HEAD" );
            }
        } catch (IOException e) {
            //file io or socket error so nothing to do
        }
        try {
            s.close();
        }
        catch (IOException e) {
            //must have already been closed or error
        }
    }

    /**
     * send boring standard http headers
     */
    private String standardHeaders() {
        //Date now = LazyDate.getDate();
        Date now = new Date();
        return "Date: " + now + "\r\nServer: iim http 1.0\r\n";
    }

    /**
     * get the content type for request
     *
     * @param filename
     */
    private String contentType(String filename) {
        String [] a = {
            ".html",    "text/html",
            ".htm",     "text/html",
            ".txt",     "text/plain",
            ".jar",     "application/octet-stream",
            ".class",   "application/octet-stream",
            ".gif",     "image/gif",
            ".jpg",     "image/jpeg",
            ".jpeg",    "image/jpeg",
            ".jnlp",    "application/x-java-jnlp-file",
            ".nlc",    "application/x-x509-ca-cert"
        };
        for (int x = 0; x < a.length -1; x += 2) {
            if (filename.endsWith(a[x])) return a[x + 1];
        }
        return "application/octet-stream";
    }
}









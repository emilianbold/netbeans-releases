/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.uihandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.netbeans.junit.NbTestCase;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jaroslav Tulach
 */
public class ServerTest extends NbTestCase {
    public ServerTest(String s) {
        super(s);
    }
    
    
    public static int startServer(final Queue<String> replies, final Queue<String> queries) throws IOException {
        final ServerSocket ss = new ServerSocket(0);
        
        class Run implements Runnable {
            private void doRun(String reply) throws IOException {
                Socket s = ss.accept();
                s.setSoTimeout(500);
                InputStream is = s.getInputStream();
                StringBuffer sb = new StringBuffer();
                try {
                    for (;;) {
                        int ch = is.read();
                        if (ch == -1) {
                            break;
                        }
                        sb.append((char)ch);
                    }
                } catch (SocketTimeoutException ex) {
                    // ok
                }
                
                queries.add(sb.toString());
                
                OutputStream os = s.getOutputStream();
                os.write(reply.getBytes());
                os.close();
                
                is.close();
                s.close();
            }
            
            public void run() {
                for (;;) {
                    String reply = replies.poll();
                    if (reply == null) {
                        break;
                    }
                    try {
                        doRun(reply);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        Run r = new Run();
        RequestProcessor.getDefault().post(r);
        
        
        return ss.getLocalPort();
    }
    
    public void testRedirectsLogs() throws Exception {
        LinkedList<String> query = new LinkedList<String>();
        query.add("<meta http-equiv=\"Refresh\" conteNT='URL=http://www.netbeans.org'>");
        LinkedList<String> reply = new LinkedList<String>();
        int port = startServer(query, reply);
        
        URL u = new URL("http://localhost:" + port);
        
        List<LogRecord> recs = new ArrayList<LogRecord>();
        recs.add(new LogRecord(Level.WARNING, "MSG_MISTAKE"));
        URL redir = Installer.uploadLogs(u, null, Collections.<String,String>emptyMap(), recs);

        assertTrue("one query has been sent: " + query, query.isEmpty());
        assertEquals("One reply received", 1, reply.size());
        assertEquals("Redirected to nb.org", new URL("http://www.netbeans.org"), redir);
    }


    public void testRedirectsLogsWithTime() throws Exception {
        LinkedList<String> query = new LinkedList<String>();
        query.add("<meta http-equiv='Refresh' content='3; URL=http://logger.netbeans.org/welcome/use.html'>");
        LinkedList<String> reply = new LinkedList<String>();
        int port = startServer(query, reply);
        
        URL u = new URL("http://localhost:" + port);
        
        List<LogRecord> recs = new ArrayList<LogRecord>();
        recs.add(new LogRecord(Level.WARNING, "MSG_MISTAKE"));
        URL redir = Installer.uploadLogs(u, null, Collections.<String,String>emptyMap(), recs);

        assertTrue("one query has been sent: " + query, query.isEmpty());
        assertEquals("One reply received", 1, reply.size());
        assertEquals("Redirected to nb.org", new URL("http://logger.netbeans.org/welcome/use.html"), redir);
    }

    



}



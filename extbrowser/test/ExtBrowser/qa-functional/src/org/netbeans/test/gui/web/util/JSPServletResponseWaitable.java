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

package org.netbeans.test.gui.web.util;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.net.Socket;
import java.net.ServerSocket;


import org.netbeans.jemmy.Waitable;
import org.netbeans.core.NbTopManager;

public class JSPServletResponseWaitable implements Waitable {
    private String id = null;
    private String real = null;
    private String userAgent = null;
    private String answer = null;
    private int port = -1;
    private boolean started = false;
    public JSPServletResponseWaitable(String id, String answer, int port) {
	this.id = id;
	this.answer = answer;
	this.port = port;

	(new Thread () {
		public void run() {
		    try {
			String answer = getDefaultAnswer();
			int port = getDefaultPort();
			ServerSocket ss = new ServerSocket(port);
			System.out.println("Listen on " + port);
			started = true;
			Socket s = ss.accept();
			InputStream is = s.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line = br.readLine();
			while(!(line == null)) {
			    System.out.println("READ: \"" + line + "\"");
			    if(line.startsWith("ID")) {
				setId(line);
			    }
			    if(line.startsWith("UserAgent")) {
				setUserAgent(line);
			    }
			    line = br.readLine();
			    if(line.equals("")) {
				line = null;
			    }
			}
			OutputStream os = s.getOutputStream();
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
			bw.write(answer,0,answer.length());
			System.out.println("CL is " + answer.length());
			bw.flush();
			s.close();
			ss.close();
		    }catch(Exception e) {
			real = "Exception at READ/WRITE http opeartions";
			System.out.println("ERROR!");
			e.printStackTrace();
		    }
		}
	    }).start();
	while(!started);
    }
    
    public Object actionProduced(Object o) {
	if((real!=null)&&(real.indexOf(id)!=-1)) {
	    return Boolean.TRUE;
	}
	if((real!=null)&&(real.indexOf(id)==-1)) {
	    System.out.println("ID is " + real + " instead of " + id);
	}
	return null;
    }

    public String getDescription() {
	return "Waiter for ID: " + id;
    }

    private int getDefaultPort() {
	return port;
    }
    private String getDefaultAnswer() {
	return answer;
    }
    private void setId(String id) {
	real = id;
    }

    private void setUserAgent(String agent) {
	userAgent = agent;
    }
    private String getUserAgent() {
	return userAgent;
    }
    public String getId() {
	return real;
    }
}


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

package org.netbeans.lib.collab.sample;

import org.netbeans.lib.collab.*;
import java.io.*;
import java.util.*;


/**
 * sample program which listens for notifications
 *
 */
public class Alerter extends Thread implements CollaborationSessionListener, NotificationServiceListener
{

    // sessions
    private NotificationService _service;

    // contains all open subscriptions and news channels
    private Hashtable _subscriptions = new Hashtable();

    // keeps track of the latest answer to a yes or no question
    private boolean _shutdown = false;
    
    private String _user;
    
    //create a connection using the command line
    public Alerter(CollaborationSessionFactory fac, 
		       String server, String user, String password) throws Exception 
    {
	_user = user;
	CollaborationSession s = fac.getSession(server, user, password, this);
	_service = s.getNotificationService();
	_service.initialize(this);
    }
    
    public void onError(org.netbeans.lib.collab.CollaborationException e) {
	e.printStackTrace();
    }
    
    public void run()
    {
	
	while (!_shutdown) {
	    
	    System.out.println("Listening for notifications");

	    try {		
		String s = _reader.readLine();
		if (s == null) break;
		s = s.trim();
		if (s.equals("")) continue;
		
		// command
		StringTokenizer st = new StringTokenizer(s);
		String cmd = st.nextToken();
		String args[] = new String [ st.countTokens() ];
		int i = 0;
		while (st.hasMoreTokens()) {
		    args[i++] = st.nextToken();
		}
		
		if (cmd.equalsIgnoreCase("quit") ||
			   cmd.equalsIgnoreCase("exit") ||
			   cmd.equalsIgnoreCase(".")) {
		    _shutdown = true;
		}

	    } catch (Exception e) {
		System.out.println("Error " + e.toString());
		e.printStackTrace();
	    }
	    
	}
    
    }

    public synchronized void onMessage(Message m) 
    {
	try {
	    printMessage(m);
            String s = prompt("Hit Enter when you have read the message... ", false);

	    // send read status
	    m.sendStatus(MessageStatus.READ);

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }


    //
    // STATIC CODE
    //
    
    // source to read commands from
    private static BufferedReader _reader;
    

    //login to the server    
    public static void main(String args[]) 
    {

	_reader = new BufferedReader(new InputStreamReader(System.in));

        String user, password, server;
        
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("login host username password");
            return;
        }

	// prompt server        
        if (args.length >= 1) {
            server = args[0];
        } else {
            server = prompt("Hostname [localhost:9909]\t: ",
			     false, "localhost:9909");
	}
        
        if (args.length >= 2) {
            user = args[1];
        } else {
	    String username = System.getProperty("user.name");
	    user = prompt("User name [" + username + "]\t: ",
			  false, username);
        }
        
        if (args.length >= 3) {
            password = args[2];
        } else {            
            password = prompt("Password [iplanet]\t: ", false, "iplanet");
        }
	
	try {
	    
	    CollaborationSessionFactory fac = new CollaborationSessionFactory();
	    Alerter thr = new Alerter(fac, server, user, password);
	    thr.start();
	    
	    thr.join();
	    
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public static void printMessage(Message message) throws Exception
    {
        // display the message
	//        System.out.println("Message Received -- " + message.getMessageId());
        System.out.println("    Subject: " + message.getHeader("subject"));
        System.out.println("    Content-type: " + message.getContentType());
	String from = message.getOriginator();
	System.out.println("    From: " + from);
        String[] rcpts = message.getRecipients();
        for (int i = 0 ; i < rcpts.length ; i++) {
	    System.out.println("    To: " + rcpts[i]);
        }

        MessagePart[] parts = message.getParts();
        for (int i = 0 ; i < parts.length ; i++) {
            //System.out.println("    Part " + i);
            //System.out.println("        Content-type: " + parts[i].getContentType());
            try {
		BufferedReader br=new BufferedReader(new InputStreamReader(parts[i].getInputStream()));
		String str;
		while((str=br.readLine()) != null) System.out.println(str);
            } catch (Exception e) { 
                e.printStackTrace();
            }
        }
    }

    private static String prompt(String ask, boolean newline) {
	return prompt(ask, newline, "");
    }
          
    private static String prompt(String ask, boolean newline, String defaultValue) 
    {
        String ret = defaultValue;
        if (newline) {
	    System.out.println(ask);
	} else {
	    System.out.print(ask);
	}

	try {	      
	    String s = _reader.readLine();
	    if (s.length() > 0) ret = s;
	} catch (Exception e) {
	    e.printStackTrace();
	}

        return ret;
    }

}            

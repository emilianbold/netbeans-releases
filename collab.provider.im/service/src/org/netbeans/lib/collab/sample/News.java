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
 * Sample Command line client
 * to read messages from news channels and
 * post new messages to news channels
 *
 */
class NewsChannelListenerImpl implements NewsChannelListener
{

    NewsChannel _nc;
    boolean _subscribed;
    private Hashtable _messages = new Hashtable();

    public NewsChannelListenerImpl(NewsService s, String name, boolean create, boolean subscribe) throws Exception
    {
	_subscribed = subscribe;
	if (create) {
	    _nc = s.newNewsChannel(name, this, Conference.MANAGE);
	} else {
	    _nc = s.getNewsChannel(name, this);
	}
	if (_nc == null) throw new Exception("Failed to create or access news channel");
    }
        
    //add a message to the current conference
    public void addMessage(String subject, String body) throws Exception
    {
	Message newMsg = _nc.createMessage();
	newMsg.setContent(body);
	newMsg.setHeader("subject", subject);
	_nc.addMessage(newMsg);
	_messages.put(newMsg.getMessageId(), newMsg);
    }

    /**
     * invoked when a message has been posted to the bulletin board
     *
     * @param message the message
     */
    public void onMessageModified(String messageId, Message message) 
    {
    }

    /**
     * invoked when a message has been posted to the bulletin board
     *
     * @param message the message
     */
    public void onMessageAdded(Message message) 
    {
	if (_subscribed) {
	    System.out.println("[" + _nc.getDestination() + "] Message added");
	    News.printMessage(message);
	    System.out.print("News> ");
	}
	_messages.put(message.getMessageId(), message);
    }
        

    /**
     * invoked when a message has been posted to the bulletin board
     *
     * @param message the message
     */
    public void onMessageRemoved(String messageId) 
    {
	if (_subscribed) {
	    System.out.println("[" + _nc.getDestination() + "] Message deleted: " + messageId);
	    System.out.print("News> ");
	}
	_messages.remove(messageId);
    }
        
    /**
     * invoked when an unexpected failure has happed.
     * The listener is no longer
     * valid after this method has been called.
     * @param e describes the nature of the problem
     */
    public void onError(org.netbeans.lib.collab.CollaborationException e)
    {
	e.printStackTrace();
	System.out.print("News> ");
    }
            
    public void remove()
    {
    }

    public void removeMessage(String messageId) throws Exception 
    {
	_nc.removeMessage(messageId);
	_messages.remove(messageId);
    }

    public void onEvent(String event)
    {
    }

    public void listMessages() throws Exception 
    {
	if (_messages.size() == 0) {
	    // give it some time to receive messages
	    Thread.sleep(200);
	} 
	java.util.Collection c = _messages.values() ;
        System.out.println("[" + _nc.getDestination() + "] begin message list");
        for (java.util.Iterator i = c.iterator() ; i.hasNext() ;) {
            org.netbeans.lib.collab.Message m = (org.netbeans.lib.collab.Message)i.next();
	    News.printMessage(m);
        }
        System.out.println("[" + _nc.getDestination() + "] end message list");
	System.out.print("News> ");

    }

    /** invoked when a moderated message is received from another user participating
     * in this session.
     * @param message the message
     */
    public void onModeratedMessageAdded(Message message) {
    }
    
    /** invoked when there is a change in the status of the moderated message
     * @param message the message
     * @param status The status of the message as defined in Conference.
     */
    public void onModeratedMessageStatus(Message message, int status, String reason) {
    }
    
}


public class News extends Thread implements CollaborationSessionListener
{

    // sessions
    private NewsService _service;
    
    // contains all open conferences and news channels
    private Hashtable _channels = new Hashtable();
    
    //create a connection using the command line
    public News(CollaborationSessionFactory fac, 
		String server, String user, String password) throws Exception 
    {
	CollaborationSession s = fac.getSession(server, user, password, this);
	_service = s.getNewsService();
	//_service.initialize(this);
    }
    
    public void onError(org.netbeans.lib.collab.CollaborationException e) {
	e.printStackTrace();
    }
    
    public void run()
    {
	
	for (;;) {
	    
	    try {
		System.out.print("News> ");
		
		String s = _reader.readLine();
		if (s == null) break;
		s = s.trim();
		if (s.equals("")) continue;
		
		if (s.startsWith("?") || s.equalsIgnoreCase("help")) {
		    System.out.println("read <news-channel>      // read messages from a news channel");
		    System.out.println("post <news-channel>      // post a new message");
		    System.out.println("delete <news-channel>    // remove a news channel message");
		    System.out.println("create <news-channel>    // create a new news channel");
		} else {
		    StringTokenizer st = new StringTokenizer(s);
		    String cmd = st.nextToken();		    
		    String channel = null;
		    try {
			channel = st.nextToken();
		    } catch (Exception e) {
			System.out.println("Missing news channel name");
			continue;
		    }

		    if (s.startsWith("read ")) {
			read(channel);

		    } else if (s.startsWith("post ")) {
			postMessage(channel);
			
		    } else if (s.startsWith("delete ")) {
			removeMessage(channel);
			
		    } else if (s.startsWith("create ")) {
			createChannel(channel);
			
		    } else if (s.startsWith("remove ")) {
			removeChannel(channel);

		    } else {
			System.out.println("unrecognized command: " + cmd);
		    }

		}
                
	    } catch (Exception e) {
		System.out.println("Error " + e.toString());
		e.printStackTrace();
	    }
	    
	}
    
    }

    private NewsChannelListenerImpl getChannel(String name) throws Exception
    {
	NewsChannelListenerImpl c = (NewsChannelListenerImpl)_channels.get(name);
	if (c == null) {
	    c = new NewsChannelListenerImpl(_service, name, false, false);
	}
	return c;
    }

    private void read(String name) throws Exception
    {
	NewsChannelListenerImpl c = getChannel(name);
	if (c != null) {
	    c.listMessages();
	} else {
	    System.out.println("No such news channel: " + name);   
	}
    }

    private void removeChannel(String name) throws Exception
    {
	NewsChannelListenerImpl c = getChannel(name);
	if (c != null) {
	    c.remove();
	} else {
	    System.out.println("No such news channel: " + name);   
	}
    }

    private void createChannel(String name) throws Exception
    {
	NewsChannelListenerImpl c = null;
	try {
	    c = getChannel(name);
	} catch (Exception e) {
	    // ignore news channel not found
	}

	if (c == null) {
	    c = new NewsChannelListenerImpl(_service, name, true, false);
	} else {
	    System.out.println("There is already a news channel called " + name);   
	}
    }

    private void postMessage(String name) throws Exception
    {
	NewsChannelListenerImpl c = getChannel(name);
	if (c != null) {
            String subject = prompt("Subject: ", false, "");
            String body = prompt("Content: ", true, "");
	    c.addMessage(subject, body);
	} else {
	    System.out.println("No such news channel: " + name);   
	}
    }

    private void removeMessage(String name) throws Exception
    {
	NewsChannelListenerImpl c = getChannel(name);
	if (c != null) {
	    c.listMessages();
            String id = prompt("ID of message to remove: ", false, "");
	    c.removeMessage(id);
	} else {
	    System.out.println("No such news channel: " + name);   
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
            System.out.println("Talk host username password");
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
	    News talk = new News(fac, server, user, password);
	    talk.start();
	    
	    talk.join();
	    
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public static void printMessage(Message message) 
    {
        // display the message
        System.out.println("    Message-Id: " + message.getMessageId());
        System.out.println("    From: " + message.getOriginator());
        System.out.println("    Subject: " + message.getHeader("subject"));
        System.out.println("    Content-type: " + message.getContentType());
        MessagePart[] parts = message.getParts();
        for (int i = 0 ; i < parts.length ; i++) {
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

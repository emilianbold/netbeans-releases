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
 *
 */
class TalkConference implements ConferenceListener, InviteMessageStatusListener
{

    String _name, _user;
    Conference _conf;

    /** Creates new iIMCLIConference (create) */
    public TalkConference(String user, ConferenceService s) throws Exception
    {
	_user = user;
        _conf = s.setupConference(this, Conference.MANAGE);
        _name = _conf.getDestination();
        _conf.join(this);
    }

    /** Creates new iIMCLIConference (join) */
    public TalkConference(String user, ConferenceService s, Conference c) throws Exception 
    {
	_user = user;
        _conf = c;
        _name = _conf.getDestination();
        c.join(this);
    }
    
    //add a message to the current conference
    public void addMessage(String msg) throws Exception
    {
	Message newMsg = _conf.createMessage();
	MessagePart part = newMsg.newPart();
	part.setContentType("text/plain");
	part.setContent(msg);
	newMsg.addPart(part);
	newMsg.setOriginator("yoyo@yoyo.com");
	_conf.addMessage(newMsg);
    }

    /**
     * invoked when a message has been posted to the bulletin board
     *
     * @param message the message
     */
    public void onMessageAdded(Message message) 
    {
	if (!message.getOriginator().startsWith(_user + "@")) {
	    Talk.printMessage(message);
	}
    }
    
    /**
     * invoked when the system is sending information to the user.  This may
     * include<ul>
     * <li>indication that another member has left</li>
     * <li>indication that your access level has changed</li>
     * <li>etc...</li>
     * @param event event 
 */
    public void onEvent(String event) {
	try {
	    ConferenceEventHelper ceh = new ConferenceEventHelper(event);
	    for (Iterator i = ceh.getTuples().iterator(); i.hasNext(); ) {
		ConferenceEventTuple cet = (ConferenceEventTuple)i.next();
		if (cet.status.equals("on")) {
		    System.out.println("*** " + cet.destination + " has joined " + _name);
		} else {
		    System.out.println("*** " + cet.destination + " has left " + _name);
		}
	    }	
	} catch(Exception e) {
	}    	
    }
    
    public void leave() {
        _conf.leave();
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
    }
    
    public String getDestination()
    {
        return _name;
    }
    
    public Message createInviteMessage() throws Exception
    {
        return _conf.createInviteMessage();
    }
    
    public void invite(Message m) throws Exception
    {
	_conf.invite(Conference.MANAGE, m, this);
    }
    
    public void onRsvp(String destination, Message message, boolean accepted)
    {
	onRsvp(destination, accepted);
	Talk.printMessage(message);
    }
    
    public void onRsvp(String destination, boolean accepted)
    {
	if (accepted) {
	    System.out.println(destination + " accepted your invitation.");
	} else {
	    System.out.println(destination + " declined your invitation.");
	}
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


public class Talk extends Thread implements SecureSessionListener, ConferenceServiceListener
{

    // sessions
    private ConferenceService _service;
    
    // contains all open conferences and news channels
    private Hashtable _conferences = new Hashtable();
    
    // keeps track of the latest answer to a yes or no question
    private boolean bYesOrNo, _shutdown = false, invitePending = false;
    
    private TalkConference _currentConference = null;

    private Object mutex = new Object();
    private String _user;
    
    //create a connection using the command line
    public Talk(CollaborationSessionFactory fac, 
		String server, String user, String password) throws Exception 
    {
	_user = user;
	CollaborationSession s = fac.getSession(server, user, password, this);
	_service = s.getConferenceService();
	_service.initialize(this);
    }
    
    private boolean promptYesOrNo(String ask) throws Exception
    {
	boolean answer = false;
        System.out.print(ask);
        // take the latest answer
	synchronized(mutex) {
	    mutex.wait();
	    answer = bYesOrNo;
	}
        return answer;
    }

    public void onError(org.netbeans.lib.collab.CollaborationException e) {
	e.printStackTrace();
    }
public boolean onX509Certificate(java.security.cert.X509Certificate[] chain)
{
return true;
}
    
    public void run()
    {
	
	while (!_shutdown) {
	    
	    try {
		System.out.print(_user + "> ");
		
		String s = _reader.readLine();
		if (s == null) break;
		s = s.trim();
		if (s.equals("")) continue;
		
		if (s.startsWith("+")) {
		    // command
		    StringTokenizer st = new StringTokenizer(s);
		    String cmd = st.nextToken();
		    String args[] = new String [ st.countTokens() ];
		    int i = 0;
		    while (st.hasMoreTokens()) {
			args[i++] = st.nextToken();
		    }
		    
		    if (cmd.equalsIgnoreCase("+invite")) {
			invite(args);
		    } else if (cmd.equalsIgnoreCase("+leave")) {
			    leave(args);
		    } else if (cmd.equalsIgnoreCase("+list")) {
			list();
		    } else if (cmd.equalsIgnoreCase("+active")) {
			active(args);
		    }

                } else if (invitePending) {
		    if ((s.equalsIgnoreCase("y") ||
			 s.equalsIgnoreCase("yes"))) {
			synchronized(mutex) {
			    bYesOrNo = true;
			    mutex.notify();
			}
		    } else {
			synchronized(mutex) {
			    bYesOrNo = false;
			    mutex.notify();
			}
		    }

		} else {
		    // send message to current conference
		    if (_currentConference != null) {
			_currentConference.addMessage(s);
		    } else {
			System.out.println("No active conference!");
			System.out.println("you first need to select one using the +active command.");			
			list();
		    }
		}
                
	    } catch (Exception e) {
		System.out.println("Error " + e.toString());
		e.printStackTrace();
	    }
	    
	}
    
    }

    public synchronized void onInvite(Conference conference, InviteMessage message) 
    {
        System.out.println(message.getOriginator() + " has invited to a conference ");
        System.out.println("conference address: " + conference.getDestination());
        System.out.print("Invite Message: ");
	printMessage(message);

	if (_conferences.get(conference.getDestination()) != null) {
	    System.out.println("already invited");
	}

        try {
	    invitePending = true;
	    if (promptYesOrNo("Do you want to join this conference? ")) {
		TalkConference c = new TalkConference(_user, _service, conference);
		_conferences.put(c.getDestination(), c);
		if (_currentConference == null) _currentConference = c;
		System.out.println("Conference " + c.getDestination() + " now active");
	    } else {
		System.out.println("Invitation declined");
	    }
        } catch (Exception e) {
            e.printStackTrace();
	    System.out.println("Failed to join.");
        } finally {
	    invitePending = false;
	}
    }
 
    private synchronized void active(String args[]) 
    {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("+active [ conference ]");
            return;
        }

	TalkConference c;
        String confId = null;
        if (args.length > 0) {
            confId = args[0];
        } else {
	    confId = prompt("New active conference: ", false);
	}
	c = (TalkConference)_conferences.get(confId);
	if (c == null) {
	    System.out.println("Conference " + confId + " not found");
	    return;
	}

	_currentConference = c;
	System.out.println("The new active conference is " + confId);

    }

    //invite a user to a room
    private void invite(String args[]) throws Exception {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("+invite [ conference ]");
            return;
        }

	TalkConference c;
        String confId = null;
        if (args.length > 0) {
	    // find conference
            confId = args[0];
	    c = (TalkConference)_conferences.get(confId);
	    if (c == null) {
		System.out.println("Conference " + confId + " not found");
		return;
	    }

        } else {
	    // create new conference
	    c = new TalkConference(_user, _service);
	    _conferences.put(c.getDestination(), c);
	}
        
	_currentConference = c;

	// create invite message
        Message newMsg = c.createInviteMessage();
	newMsg.setOriginator("yoyo@yoyo.com");

	for (;;) {
	    String user = prompt("invite (enter one address, return if done): ", false);
	    if (user.length() > 0) {
		newMsg.addRecipient(user);
	    } else { 
		break;
	    }
	}

	// body
        String msg = prompt("Invite message: ", true);
        MessagePart part = newMsg.newPart();
        part.setContent(msg);
	newMsg.addPart(part);

	_currentConference.invite(newMsg);
	
    }
        
    //leave a room
    public void leave(String args[]) throws Exception {
        if (args.length > 0 && args[0].equals("?")) {
            System.out.println("+leave [ conference ]");
            return;
        }
        String confId;
	TalkConference c;
        if (args.length >= 1) {
            confId = args[0];
	    c = (TalkConference)_conferences.get(confId);
	    if (c == null) {
		System.out.println("Conference not found");
		return;
	    }
        } else {
            c = _currentConference;
	    _currentConference = null;
        }

	c.leave();
    }
    
    //leave a room
    public void list() throws Exception {
        String confId;
	TalkConference c;
        for (Enumeration keys = _conferences.keys();
	     keys.hasMoreElements(); ) {
	    System.out.println((String)keys.nextElement());
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
	
System.setProperty(CollaborationSessionFactory.systemProperty,
 "com.iplanet.im.client.api.iIMSecureSessionFactory");
	try {
	    
	    CollaborationSessionFactory fac = new CollaborationSessionFactory();
	    Talk talk = new Talk(fac, server, user, password);
	    talk.start();
	    
	    talk.join();
	    
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public static void printMessage(Message message) {
        // display the message
	//        System.out.println("Message Received -- " + message.getMessageId());
        //System.out.println("    Subject: " + message.getHeader("subject"));
        //System.out.println("    Content-type: " + message.getContentType());
	String from = message.getOriginator();
	System.out.print(from.substring(0, from.indexOf("@")) + "> ");
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

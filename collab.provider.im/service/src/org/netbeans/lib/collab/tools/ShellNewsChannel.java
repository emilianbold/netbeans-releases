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

package org.netbeans.lib.collab.tools;

import org.netbeans.lib.collab.*;

import java.util.* ;

/**
 *
 *
 * @author Jacques Belissent
 * @author Rahul Shah
 * @author Vijayakumar Palaniappan
 *
 */
public class ShellNewsChannel extends ShellConference implements org.netbeans.lib.collab.NewsChannelListener
{

    // Map simple numeric message ids to their longer form (as in getMessageId())
    // makes deletion easier to specify in a news channel
    int _messageIdCount = 0;
    java.util.Map _messageIdToNumber = new java.util.HashMap();
    java.util.Map _numberToMessageId = new java.util.HashMap();
    java.util.Map _messages;
    
    /** retrieve an existing bulletin board */
    public ShellNewsChannel(String uid, NewsService s, String name, boolean subscribe) throws Exception {
        super(uid, name);
	if (subscribe) {
	    _conf = s.getNewsChannel(name, this);
	} else {
	    _conf = s.getNewsChannel(name, null);
	} 
        if (_conf == null) {
            throw new NoSuchElementException("news channel " + name + " does not exist");
        }
        _messages = new java.util.HashMap();
        System.out.println("Got news channel " + _conf.getDestination());
    }
    
    /** Create a new bulletin board */
     public ShellNewsChannel(String uid, NewsService s, String name, int accessLevel) throws Exception {
        super(uid, name);
        _conf = s.newNewsChannel(name, this, accessLevel);
        _messages = new java.util.HashMap();
        System.out.println("Created news channel " + _conf.getDestination());
    }
    
    /**
     * invoked when a message has been posted to the bulletin board
     *
     * @param message the message
     */
    public synchronized void onMessageAdded(Message message) {
        //System.out.println("onMessage called for " + message);
	System.out.println("[" + _uid + ":" + _conf.getDestination() + "] message added -- " + message.getHeader("subject"));
        Shell.printMessage(message);

	String msgId = message.getMessageId();
        _messages.put(msgId, message);
        //System.out.println("Message id is " + msgId);
	// Make a simple numeric id to identify a message for purposes of deletion

	if (_messageIdToNumber.get(msgId) == null) {
	    _messageIdToNumber.put(msgId, Integer.toString(_messageIdCount));
	    _numberToMessageId.put(Integer.toString(_messageIdCount), msgId);
            //System.out.println("onMessage called for " + _uid + " msgId " + _messageIdCount);
	_messageIdCount++;
	} else {
           //System.out.println("MessageIdToNumer " + _messageIdToNumber.get(msgId));              
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
    public void onEvent(ConferenceEvent event) {

        ConferenceEventHelper ch = null;
        try {
              ch = new ConferenceEventHelper(event.toString());
        } catch (Exception e ) {
             e.printStackTrace();
             System.exit(1);
        }

        ArrayList tuples = (ArrayList)ch.getTuples();
        for(int i = 0; i < tuples.size() ; i++ ) {
            ConferenceEventTuple t = (ConferenceEventTuple)tuples.get(i);
            System.out.println("Destination  " + t.destination + " status " + t.
status);
        }
    }
    
    /**
     * @param messageId id of the removed message
     */
    public void onMessageRemoved(String messageId) {

	// Removed messageId from this print statement for use with Shell
	// where the time dependent part
	// of a messageId would mean that output could not be reproduced identically in
	// different runs
        System.out.println("[" +_uid + ":" + _conf.getDestination() + "] message removed -- ");
        _messages.remove(messageId);
    }
    
    /**
     * @param messageId message id of the modified item
     * @param message modified message.
     */
    public void onMessageModified(String messageId, Message message) {
	// Removed messageId from this print statement for use with 
	// Shell where the time dependent part
	// of a messageId would mean that output could not be reproduced identically on different runs.
        System.out.println("[" +_uid + ":" + _conf.getDestination() + "] message modified -- ");
        _messages.put(messageId, message);
    }
    
    public void listMessages() throws Exception 
    {
	// This wait must be long enough for it to receive messages...
	//  When listMessages is first called, in turn it calls getNewsChannel() 
	// which causes all messages to be sent from the server (and printed out
	// via onMessageAdded). If they don't all arrive in the _messages hash table
	// before it's enumeration is complete, they won't all be listed (a second time)

	if (_messages.size() == 0) {
	    // Give it some time to receive messages
	    Thread.sleep(500);
	} 
	
	java.util.Collection c = _messages.values() ;
        System.out.println("*** Start message list for news channel " + _conf.getDestination());
        for (java.util.Iterator i = c.iterator() ; i.hasNext() ;) {
            org.netbeans.lib.collab.Message m = (org.netbeans.lib.collab.Message)i.next();
	//    System.out.println("[" + _conf.getDestination() + "] -- " + m.getMessageId() + " --");
        //    System.out.println("    Subject: " + m.getHeader("subject"));
	    System.out.println("id: " + _messageIdToNumber.get(m.getMessageId()));
            Shell.printMessage(m);
        }
        System.out.println("*** End message list for news channel " + _conf.getDestination());
    }
}

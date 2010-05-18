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

import java.util.*;

/**
 *
 *
 * @author Jacques Belissent
 * @author Rahul Shah
 * @author Vijayakumar Palaniappan
 *
 */
public class ShellConference implements org.netbeans.lib.collab.ConferenceListener
{
    String _uid = null;

    // local name used by the user to reference this
    // conference in the shell
    String _name;

    Conference _conf;
    ConferenceService _confService;
    Shell _shell;

    /** Creates new ShellConference (public conference)
     */
    public ShellConference(String uid, ConferenceService s, String name, boolean create, boolean join) throws Exception 
    {
	_uid = uid;
        _confService = s;
	_name = name;
	if (create) {
	    _conf = s.setupPublicConference(name, this, Conference.PUBLISH);
	} else if (join) {
	    _conf = s.joinPublicConference(name, this);
	} else {
            _conf = s.getPublicConference(name);
        }
        if (_conf == null) {
            throw new NoSuchElementException("Conference " + name + " does not exist");
        }
    }
    
    /**
     * Creates new ShellConference
     * private conference
     */
    public ShellConference(String uid, ConferenceService s, String localName) throws Exception {
	_uid = uid;
        _confService = s;
        _name = localName;
        _conf = s.setupConference(this, Conference.MANAGE);
    }
    
    public ShellConference(String uid, ConferenceService s,
			   Conference c) throws Exception
    {
	this(uid, s, c, c.getDestination());		       
    }

    /** Creates new ShellConference (join) */
    public ShellConference(String uid, ConferenceService s,
			   Conference c, String localName) throws Exception
    {
	_uid = uid;
        _confService = s;
        _conf = c;
        _name = localName;
        c.join(this);
    }
    
    /** Creates new ShellConference */
    public ShellConference(String uid, String name) throws Exception {
	_uid = uid;
        _name = name;
    }

    public void setShell(Shell s) {
        _shell = s;
    }
    
    /**
     * invoked when a message has been posted to the bulletin board
     *
     * @param message the message
     */
    public void onMessageAdded(Message message) {
	System.out.println("[" + _uid + ":" + _name + "] message added -- " + message.getHeader("subject"));
        Shell.printMessage(message);
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
        ConferenceEventHelper ch = null;
        try {
	    ch = new ConferenceEventHelper(event);
        } catch (Exception e ) {
	    e.printStackTrace();
	    System.exit(1);
        }

        ArrayList tuples = (ArrayList)ch.getTuples();
	//System.out.println("DEBUG CONFERENCE EVENT: " + tuples.size() + " tuples");
        for (int i = 0; i < tuples.size() ; i++ ) {
            ConferenceEventTuple t = (ConferenceEventTuple)tuples.get(i);
            try {
		// The leading newline is important when this is being used with Shell
		// for functional testing. Conference events are asynchronous and if received
		// late they can appear on a line with command input, screwing up the
		// test output. Making sure they are on a line of their avoids this.
		System.out.println("\n[ConferenceEvent Received] Conference=<" + _name + "> Recipient=<"+ _uid + "> Destination=<" + t.destination + "> status=<" + t.status + ">");
            } catch(Exception e ) {
		System.out.println("Exception: " + e);
		break ;
            }
        }

    }
    
    /**
     * invoked when an unexpected failure has happed.  The listener is no longer
     * valid after this method has been called.
     * @param e describes the nature of the problem
     */
    public void onError(org.netbeans.lib.collab.CollaborationException e) {
    }
    
    public Conference getConference() {
        return _conf;
    }
    
    /** invoked when a moderated message is received from another user participating
     * in this session.
     * @param message the message
     */
    public void onModeratedMessageAdded(Message message) {
        System.out.println("[" + _uid + ":" + _name + "] moderated message added -- " + message.getHeader("subject"));
        Shell.printMessage(message);
    }
    
    public void onModeratedMessageStatus(Message message, int status, String reason) {
        System.out.println("[" + _uid + ":" + _name + "] moderated message status -- " + message.getHeader("subject"));
        System.out.println("Status: " + status + " Reason: " + reason);
        Shell.printMessage(message);
        try {
            _shell.handleModeratedMessage(_conf,message,status);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
}

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
import org.netbeans.lib.collab.util.*;
import java.util.*;
import java.io.*;
import java.net.*;

import org.apache.log4j.*;


/**
 *
 * @author Jacques Belissent
 * @author Rahul Shah
 * @author Vijayakumar Palaniappan
 * @author Rebecca Ramer
 *
 */

class LoadConferenceCloser implements Runnable, ConferenceListener
{
    Timer timer;
    Conference conf;

    LoadConferenceCloser(Timer t, Conference conf)
    {
        timer = t;
        this.conf = conf;
    }

    public void onError(CollaborationException e) {
        e.printStackTrace();
    }

    public void onEvent(String event) {

        Generate.debuglog("Received event: " + event);
    }

    public void onModeratedMessageAdded(Message message) {
    }

    public void onModeratedMessageStatus(Message message, int status, String reason) {
    }  

    public void onMessageAdded(Message message) {
    }

    public void run() {


        try {

            Collection participants = conf.getParticipants();
            if (participants.size() > 1) {

                TimerTask task = new WorkerTimerTask(this);
                timer.schedule(task,5000);
            } else {
                Generate.debuglog("Closing conference: " + conf.getDestination());
                conf.close();
            }
        } catch (CollaborationException e) {
            e.printStackTrace();
        }

    }
}
        

class LoadConferenceParticipant implements Runnable, ConferenceListener 
{
    Timer timer;
    int num_messages_sent = 0;
    long numMessagesToSend = 0;
    int num_message_intervals = 0;
    long numMessageIntervals = 0;
    Conference conf;
    long minWaitTime, maxWaitTime;
    String uid;
    String type;
    //Max message time
    static int max = 0;
    //Min message time
    static int min = Integer.MAX_VALUE;
    //Total message time
    static long total = 0;
    //Count of messages
    static int count = 0;

    final private static String RESOURCE_NAME = "conference_participant";
    static {
	LoadStatistics.createResource(RESOURCE_NAME);
    }

    LoadConferenceParticipant(String ctype,
                              String uid,
			      Timer t,
			      long numMessagesToSend,
			      long minWaitTime,
			      long maxWaitTime,
			      Conference conf) {
        this.type = ctype;
	this.uid = uid;
	timer = t;
	this.minWaitTime = minWaitTime;
	this.maxWaitTime = maxWaitTime;
	this.numMessagesToSend = numMessagesToSend;
	this.conf = conf;
	LoadStatistics.incrementResourceOrder(RESOURCE_NAME);
    }

    LoadConferenceParticipant(String uid,
                              Timer t,
                              Conference conf) {
        this.uid = uid;
        timer = t;
        this.conf = conf;

        LoadStatistics.incrementResourceOrder(RESOURCE_NAME);
    }
    
    void setConference(Conference conf)
    {
	this.conf = conf;
    }

    String getConferenceType()
    {
        return this.type;
    }

    public void onError(CollaborationException e) {
	e.printStackTrace();
    }

    public void run() {

        if ((type.equals("private")) || (type.equals("public"))) {
		
	    if (numMessagesToSend > num_messages_sent) {

	    Object trans = 
		LoadStatistics.startTransaction(Generate.MESSAGE_SERVICE_NAME);

	    try {

		Message msg = conf.createMessage();
		MessagePart part = msg.newPart();
		part.setContent("This is the message from " + msg.getOriginator() + " to " + conf.getDestination());
		msg.addPart(part);
		
		conf.addMessage(msg);
		num_messages_sent++;
		
		Generate.debuglog("Added Message conf=" + conf.getDestination() + " uid=" + uid + " sent=" + num_messages_sent + "/" + numMessagesToSend);

		// schedule next message
		long sleep = Generate.getRand(minWaitTime, maxWaitTime);

		Generate.debuglog("participant waiting for: " + sleep);

		TimerTask task = new WorkerTimerTask(this);
		timer.schedule(task,sleep);
		
	        } catch(CollaborationException e) {
		    e.printStackTrace();
		    LoadStatistics.decrementResourceOrder(RESOURCE_NAME);
		    return;
	        }

	        LoadStatistics.endTransaction(trans);

	    } else {
	        LoadStatistics.decrementResourceOrder(RESOURCE_NAME);
	        Generate.debuglog("Leaving conference:  " + conf.getDestination() + " uid=" + uid);

                conf.leave();
                if (LoadConference.isInitiator(uid,conf)) {

                    Generate.debuglog("Removing conference: " + conf.getDestination() + " uid=" + uid);

                    LoadConference.removeConference(uid);
                    if (type.equals("public")) {
                       Generate.debuglog("Conference is public: " + conf.getDestination());

                       closeConference();
                    }
                }

	    }

        }
        else if (type.equals("big"))  {

            numMessageIntervals = minWaitTime;
            //hack to get a refreshed propertyy every time
            int occupants = Integer.parseInt(
                    ((org.netbeans.lib.collab.xmpp.XMPPConference)conf).
                                getProperty("muc#roominfo_occupants",true));
            if(occupants < Generate.occThrForMessFiring) {
                System.out.println("Occupants " + occupants);
                TimerTask task = new WorkerTimerTask(this);
	        timer.schedule(task, 2000);
                return;
            } 

            if (numMessageIntervals > num_message_intervals) { 

                try {
                   
	            Object trans = 
	            LoadStatistics.startTransaction(Generate.MESSAGE_SERVICE_NAME);

		    Message msg = conf.createMessage();
		    MessagePart part = msg.newPart();
		    part.setContent(System.currentTimeMillis()+ ": " +
                            "This is the message from " + msg.getOriginator() + 
                            " to " + conf.getDestination());
		    msg.addPart(part);
	    
	            conf.addMessage(msg);
                    
	            Generate.debuglog("Added Message conf=" + conf.getDestination() + " uid=" + uid + " interval=" + num_message_intervals + "/" + numMessageIntervals);
    
	    	
	            LoadStatistics.endTransaction(trans);


                } catch(CollaborationException e) {
	            e.printStackTrace();
		    LoadStatistics.decrementResourceOrder(RESOURCE_NAME);
		    return;
	        }


                num_message_intervals++;

                Generate.debuglog("Completed an interval" + conf.getDestination() + " interval=" + num_message_intervals + "/" + numMessageIntervals);

	        // schedule next interval
	        TimerTask task = new WorkerTimerTask(this);
	        timer.schedule(task, minWaitTime);
	    	

            } else {
	        LoadStatistics.decrementResourceOrder(RESOURCE_NAME);
	        Generate.printlog("Leaving conference:  " + conf.getDestination() + " uid=" + uid);
	        conf.leave();
                LoadConference.removeConference(uid);
	        return;
            }
        }

    }

    /**
     * invoked when the system is sending information to the user.  This may
     * include<ul>
     * <li>indication that another member has left</li>
     * <li>indication that your access level has changed</li>
     * <li>etc...</li>
     * </ul>
     * object instantiated by the application allowing the provider to
     * return data to the application asynchronously.
     * <p>The Conference Event format is defined in
     * <a href="../../../icapi-dtd.html">
     * iCAPI's pseudo-DTD</a>.
     *
     * @param event XML representation of an event
     */
    public void onEvent(String event) {

        Generate.debuglog("Received event: " + event);
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

    /**
     * invoked when a message is received from another
     * user participating
     * in this session.
     * @param message the message
     */
    
    public void onMessageAdded(Message message) {

        try {
        if (Generate.MSG_LISTEN) Generate.printMessage(message);
        if (Generate.BIG_CONF){
            //Should calculate the diff.
            String content = message.getContent();
            long start = Long.parseLong(content.substring(0,content.indexOf(":")));
            int time = (int)(System.currentTimeMillis() - start);
            synchronized(LoadConferenceParticipant.class) {
                setMaxTime(time);
                setMinTime(time);
                incrementMessageCount();
                updateTotalTime(time);
                int users = Generate.numUsers;
                if(getMessageCount() == users) {
                    System.err.println("Max : " + max + " Min : " + min + 
                    " Average time for " + count + " messages : " + (total/users));
                    max = 0;
                    min = Integer.MAX_VALUE;
                    total = 0;
                    count = 0;
                }
            }
            
            Generate.printlog ("Received message from= " + message.getOriginator() + " to= " + (message.getRecipients())[0] + " conf= " + conf.getDestination());
        }

        } catch(CollaborationException e) {
            e.printStackTrace();
        }
    }

    public long getNumMessagesToSend() {
        return numMessagesToSend;
    }

    public long getMinWaitTime() {
        return minWaitTime;
    }

    public long getMaxWaitTime() {
        return maxWaitTime;
    }
    
    static void setMaxTime(int max) {
        if(max > LoadConferenceParticipant.max) {
            LoadConferenceParticipant.max = max;
        }
    }
    
    static void setMinTime(int min) {
        if(min < LoadConferenceParticipant.min) {
            LoadConferenceParticipant.min = min;
        }
    }
    
    static void incrementMessageCount() {
        count++;
    }
    
    static void updateTotalTime(int time) {
        total += time;
    }
    
    static int getMessageCount() {
        return count;
    }

   private void closeConference()
   {
       //hang out and wait for last participant
       TimerTask task = new WorkerTimerTask(new LoadConferenceCloser(timer, conf));
       timer.schedule(task,0);

       Generate.debuglog("scheduled Conference Closer");
   }
}






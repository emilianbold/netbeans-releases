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

public class LoadConference implements InviteMessageStatusListener 
{
    static Hashtable conferenceMap = new Hashtable();
    String initiator;
    Timer timer;
    ConferenceService service;

    final private static String RESOURCE_NAME = "conference";
    final private static String SERVICE_NAME = "conference:setup";
    static {
	LoadStatistics.createResource(RESOURCE_NAME);
	LoadStatistics.createService(SERVICE_NAME);
    }

    LoadConference(String type,
                   CollaborationSession session,
                   String conferenceName,
		   long numMessagesToSend,
		   long minWaitTime,
		   long maxWaitTime,
		   Set participants,
		   Timer t)
                   
    {
        Conference conf = null;

        timer = t;

	try {
	    initiator = session.getPrincipal().getUID();

	    this.service = session.getConferenceService();

	    Generate.debuglog("New Conference: minWait=" + minWaitTime + " maxWait=" + maxWaitTime + " numMessagesToSend=" + numMessagesToSend + " numParticipants=" + (participants.size()+1) + " initiator=" + initiator);

	    LoadConferenceParticipant participant =
		new LoadConferenceParticipant(type, initiator, timer, numMessagesToSend, minWaitTime, maxWaitTime, null);
	    

	    if (type.equals("private")) {

                conf = service.setupConference(participant, Conference.MANAGE);
                Generate.printlog(initiator + " created private conference room " + conf.getDestination());

            } else if (type.equals("public")) { 

               conf = service.getPublicConference(conferenceName);

               if (conf == null) {
                   conf = service.setupPublicConference(conferenceName, participant, Conference.MANAGE);
               }
               Generate.printlog(initiator + " created public conference: " + conf.getDestination());

             } else if (type.equals("big")) {

              // check to see if big conf exists
               conf = service.getPublicConference(conferenceName);

             if (conf == null) { //create it
                   conf = service.setupPublicConference(conferenceName, participant, Conference.MANAGE);
                   Generate.printlog(initiator + " created public conference: " + conf.getDestination());

                 // mask presence broadcast
                Generate.printlog("disabling presence broadcasts");
                conf.setEventMask(Conference.LISTEN | Conference.PUBLISH);
                conf.save();

             } else { // join it
                 service.joinPublicConference(conf.getDestination(), participant); 
	         Generate.debuglog("Joined Public Conference: " + conf.getDestination() + " minWait=" + minWaitTime + " maxWait=" + maxWaitTime + " numMessagesToSend=" + numMessagesToSend + " uid=" + session.getPrincipal().getUID());

             }
          }

	  participant.setConference(conf);
          conferenceMap.put(StringUtility.removeResource(initiator), participant);

	  // start initiator
	  startParticipant(participant);

          if (!Generate.NO_INVITES) {

             Message inviteMsg = conf.createInviteMessage();
             MessagePart part = inviteMsg.newPart();
             part.setContent("Invited to join conference by " + session.getPrincipal().getUID());
             inviteMsg.addPart(part);

	     for (Iterator i = participants.iterator(); i.hasNext(); ) {
	        String id = (String)i.next();
	        Generate.debuglog("Inviting uid=" + id + " from=" + initiator + " conf=" + conf.getDestination());
	        inviteMsg.addRecipient(id);
	     }

             conf.invite(Conference.MANAGE, inviteMsg, this);
          }  

	} catch (Exception e) {
            e.printStackTrace();
	}

    } 

    LoadConference(String type,
                   CollaborationSession session, 
                   LoadConferenceParticipant initiator, 
		   Conference conf,
		   Timer t) 
    {

        timer = t;

        try {
	    this.service = session.getConferenceService();

	    LoadConferenceParticipant participant = 
                new LoadConferenceParticipant(type, session.getPrincipal().getUID(),
					      timer,
					      initiator.getNumMessagesToSend(),
					      initiator.getMinWaitTime(),
					      initiator.getMaxWaitTime(),
					      conf);
	   
            if (conf.isPublic()) {

                service.joinPublicConference(conf.getDestination(), participant); 
	        Generate.debuglog("Joined Public Conference: " + conf.getDestination() + " minWait=" + initiator.getMinWaitTime() + " maxWait=" + initiator.getMaxWaitTime() + " numMessagesToSend=" + initiator.getNumMessagesToSend() + " uid=" + session.getPrincipal().getUID());

            } else {

                conf.join(participant);

	        Generate.debuglog("Joined Private Conference: " + conf.getDestination() + " minWait=" + initiator.getMinWaitTime() + " maxWait=" + initiator.getMaxWaitTime() + " numMessagesToSend=" + initiator.getNumMessagesToSend() + " uid=" + session.getPrincipal().getUID());

            }
            
	    // start participant activity
	        startParticipant(participant);
            
        } catch(CollaborationException e) {
            e.printStackTrace();
        }
    }

    LoadConference(CollaborationSession session,
                   Conference conf,
                   Timer t)
    {

        timer = t;

        try {
            this.service = session.getConferenceService();

            LoadConferenceParticipant participant =
               new LoadConferenceParticipant(session.getPrincipal().getUID(),
                                              timer,
                                              conf);


            service.joinPublicConference(conf.getDestination(), participant);

            Generate.printlog("Joined Big Conference: " + conf.getDestination()
+ " uid=" + session.getPrincipal().getUID());

        } catch(CollaborationException e) {
            e.printStackTrace();
        }
    }

    public static LoadConferenceParticipant findInitiator(String uid)
    {
        return (LoadConferenceParticipant)conferenceMap.get(uid);
    }

    public static void removeConference (String uid)
    {

       LoadConferenceParticipant p = findInitiator(StringUtility.removeResource(uid));
    
       if (p != null) {
           Generate.debuglog("Found initiator: " + uid + " removed conference");
           conferenceMap.remove(StringUtility.removeResource(uid));
       }
    }

    public static boolean isInitiator(String uid)
    {
        Generate.debuglog("Does " + uid + " own any active conference?");
        LoadConferenceParticipant p = findInitiator(StringUtility.removeResource(uid));
        if (p == null) return false;
        return true;
    }

    public static boolean isInitiator(String uid, Conference conf)
    {

        Generate.debuglog("Does " + uid + " own conference " + conf.getDestination() + "?");

        LoadConferenceParticipant p = findInitiator(StringUtility.removeResource(uid));
        if (p == null)  return false;

        if (p.conf.getDestination().equals(conf.getDestination())) return true;

        return false;

    }

    private void startParticipant(LoadConferenceParticipant participant)
    {
	// start participant activity
	TimerTask task = new WorkerTimerTask(participant);
	timer.schedule(task, 0);
	
	Generate.debuglog("scheduled new Confererence Participant");
    }
   
    /**
     * invoked when an unexpected failure has happened.
     * The listener is no longer
     * valid after this method has been called.
     * @param e describes the nature of the problem
     */
    public void onError(CollaborationException e) {
    }
    
    /**
     * sent when the invitation is either accepted or denied.
     * @param message in response to the invitation.  This message is not posted to the
     * conference.  It typically contains a justification of declinal of
     * the invitation.
     * @param accepted whether the invitation has been accepted or not.
     * @param destination
     */
    public void onRsvp(String destination, Message message, boolean accepted) {
        if(accepted) Generate.debuglog("Invitation accepted by " + message.getOriginator());
        else Generate.printlog("Invitation rejected by " + message.getOriginator());
    }
  
    /**
     * sent when the invitation is either accepted or denied.
     * No reason is given as to why the oinvitation has been accepted or
     * declined.
     * @param message in response to the invitation.  This message is not posted to the
     * conference.  It typically contains a justification of declinal of
     * the invitation.
     * @param accepted whether the invitation has been accepted or not.
     */
    public void onRsvp(String destination, boolean accepted) {
        if(accepted) Generate.debuglog("Invitation accepted ");
        else Generate.printlog("Invitation rejected ");
    }
    
}




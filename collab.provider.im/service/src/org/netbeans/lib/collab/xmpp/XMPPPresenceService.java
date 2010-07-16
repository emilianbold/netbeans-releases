
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

package org.netbeans.lib.collab.xmpp;

import org.netbeans.lib.collab.PresenceService;
import org.netbeans.lib.collab.CollaborationException;
import org.netbeans.lib.collab.PresenceTuple;
import org.netbeans.lib.collab.PresenceServiceListener;
import org.netbeans.lib.collab.Presence;
import org.netbeans.lib.collab.ApplicationInfo;

import org.netbeans.lib.collab.util.StringUtility;

import org.jabberstudio.jso.JID;
import org.jabberstudio.jso.JIDFormatException;
import org.jabberstudio.jso.StreamDataFactory;
import org.jabberstudio.jso.StreamException;
import org.jabberstudio.jso.StreamElement;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Collections;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Vijayakumar Palaniappan
 * 
 */
public class XMPPPresenceService implements PresenceService{

    private List _presenceServiceListeners = Collections.synchronizedList(new ArrayList());
    XMPPSession __session;

    private Hashtable _subscriptions = new Hashtable();
    private Hashtable _cache = new Hashtable();

    private ArrayList _acl;

    final int MAX_PRIORITY = 256;
    final String AWAY = "away";
    final String DND = "dnd";
    final String XA = "xa";
    final String CHAT = "chat";

    /** Creates a new instance of XMPPPresenceService */
    public XMPPPresenceService(XMPPSession session) {
        __session = session;
    }

    ///////////////
    /////Presence Service Impl
    /////////////////////
    /**
     * Subscribes to the presentity.
     * @param presentity presentity url
     *
     */
    public void subscribe(String presentity) throws CollaborationException {
        sendPresence(org.jabberstudio.jso.Presence.SUBSCRIBE, __session.getDataFactory().createJID(presentity));
    }

    /**
     * Subscribes to the multiple presentity.
     * @param presentity[] a list of presentity urls
     *
     */
    public void subscribe(String[] presentity) throws CollaborationException {
        for(int i = 0; i < presentity.length; i++) {
            subscribe(presentity[i]);
        }
    }

    /**
     * call Roster.unsubscribe
     */
    public void unsubscribe(String str) throws CollaborationException {
        sendPresence(org.jabberstudio.jso.Presence.UNSUBSCRIBE, __session.getDataFactory().createJID(str));
    }

    /**
     * call Roster.unsubscribe
     */
    public void unsubscribe(String[] str) throws CollaborationException {
        for(int i = 0; i < str.length; i++) {
            unsubscribe(str[i]);
        }
    }

    /**
     * Send a unit of presence information to a particular user
     * @param presence Presence information
     * @param rcpt The recipeint to whom the presence is directed.
     */
    public void publish(Presence presence, String rcpt) throws CollaborationException {
        try {
            org.jabberstudio.jso.Presence p = setPresence(presence, rcpt);

            // set caps
            XMPPApplicationInfo appInfo = 
                (XMPPApplicationInfo)__session.getApplicationInfo();
            if (appInfo != null) {
                StreamElement caps = appInfo.getCapabilities(true);
                if (caps != null) {
                    p.add(caps);
                }
            }

            __session.getConnection().send(p);

        } catch(Exception e) {
            throw new CollaborationException(e.toString());
        }
    }

    /**
     * Update a unit of presence information in the relevant presence
     * stores.
     * @param presence Presence information
     */
    public void publish(org.netbeans.lib.collab.Presence presence) 
        throws CollaborationException 
    {
        publish(presence,null);
    }

    /**
     * get from roster and convert to PIDF?
     */
    public org.netbeans.lib.collab.Presence fetchPresence(String uid)
        throws CollaborationException
    {
        uid = StringUtility.appendDomainToAddress(uid, __session.getPrincipal().getDomainName());
        StreamDataFactory sdf = __session.getDataFactory();
        JID jid = sdf.createJID(uid);
        org.netbeans.lib.collab.Presence p =
            (org.netbeans.lib.collab.Presence)_cache.get(jid);
        XMPPSessionProvider.debug("getting cached presence for " + jid);
        if (p == null) {
            //either the presence is not present in cache or user
            //has requested presence based on bare jid
            Enumeration e = _cache.keys();
            while (e.hasMoreElements()) {
                JID j = (JID)e.nextElement();
                if (j.toBareJID().equals(jid)) {
                    XMPPSessionProvider.debug("getting cached presence for " + j);
                    p = (org.netbeans.lib.collab.Presence)_cache.get(j);
                }
            }
        }

        // check subscriptions
        if (p == null) {
            p = (org.netbeans.lib.collab.Presence)_subscriptions.get(jid);
        }

        if (p == null || p.getTuples().size() == 0 ||
            ((PresenceTuple)p.getTuples().iterator().next()).getStatus() == null) {
            //The cache does not have presence so send a probe to server.
            try {
                org.jabberstudio.jso.Presence pp =
                                     (org.jabberstudio.jso.Presence)sdf.createPacketNode(
                                XMPPSession.PRESENCE_NAME, org.jabberstudio.jso.Presence.class);
                pp.setType(org.jabberstudio.jso.Presence.PROBE);
                pp.setTo(sdf.createJID(uid));
                                String id = __session.nextID("presence");
                                pp.setID(id);
                pp = (org.jabberstudio.jso.Presence)__session.sendAndWatch(pp,
                                                            __session.getShortRequestTimeout());
                if (pp == null ||
                    org.jabberstudio.jso.Presence.ERROR.equals(pp.getType())) {
                    throw new CollaborationException("Failed to get presence for " + uid + " error=" + pp);
                }
                return getPresence(pp);
           } catch(StreamException se) {
               throw new CollaborationException(se);
           }
        }

        return p;
    }

    /**
     * get it from roster if cached, otherwise do the subscription
     */
    public org.netbeans.lib.collab.Presence[] fetchPresence(String[] uids)
                                         throws CollaborationException
    {
        org.netbeans.lib.collab.Presence[] ret = new org.netbeans.lib.collab.Presence[uids.length];
        for(int i = 0; i < uids.length; i++) {
            ret[i] = fetchPresence(uids[i]);
        }
        return ret;
    }

    public void cancel(String presentity) throws CollaborationException
    {
        JID jid;
        try {
            jid = new JID(presentity);
        } catch(JIDFormatException e) {
            throw new CollaborationException(e);
        }
        sendPresence(org.jabberstudio.jso.Presence.UNSUBSCRIBED,jid);
    }

    public void authorize(String presentity) throws CollaborationException {
        JID jid;
        try {
            jid = new JID(presentity);
        } catch(JIDFormatException e) {
            throw new CollaborationException(e);
        }
        sendPresence(org.jabberstudio.jso.Presence.SUBSCRIBED,jid);
    }

    public void initialize(PresenceServiceListener listener)
        throws CollaborationException {
        addPresenceServiceListener(listener);
    }

    //////////////
    ////Package methods
    ///////////////
    void addSubscriptions(JID jid, org.netbeans.lib.collab.Presence p) {
        _subscriptions.put(jid,p);
        _firePresenceServiceListener(p, null);
    }

    void removeSubscriptions(JID jid) {
        _subscriptions.remove(jid);
        _subscriptions.remove(jid.toBareJID());
    }

    org.netbeans.lib.collab.Presence getSubscriptions(JID jid) {
        return (org.netbeans.lib.collab.Presence)_subscriptions.get(jid);
    }

    org.netbeans.lib.collab.Presence processPresence(org.jabberstudio.jso.Presence p)
                                        throws CollaborationException
    {
        org.netbeans.lib.collab.Presence imPresence = getPresence(p);
        // todo : do not do thus if the application is acting on behalf of
        // more than one user, as this can eventually lead to using
        // a lot more memory than appropriate.

        JID from = getFromJID(p);

        XMPPSessionProvider.debug("caching presence for " + from);
        _cache.put(from, imPresence);

        _firePresenceServiceListener(imPresence,p);
         return imPresence;
    }

    private boolean isPending(org.netbeans.lib.collab.Presence p)
    {
        Iterator tuples = p.getTuples().iterator();
        if (tuples.hasNext()) {
            PresenceTuple t = (PresenceTuple)tuples.next();
            return (PresenceService.STATUS_PENDING.equals(t.getStatus()));
        }
        return false;
    }

    org.netbeans.lib.collab.Presence createPresence(String uid,
                                                       String status,
                                                       String note,
                                                       float priority)
    {
        PresenceTuple pt = new PresenceTuple();
        pt.setStatus(status);
        pt.setPriority(priority);
        pt.addNote(note);
        pt.setContact(uid);
        return new org.netbeans.lib.collab.Presence(pt);
    }

    //////////////////
    /////Private methods
    ////////
    private void sendPresence(org.jabberstudio.jso.Presence.Type type, JID to)
                                throws CollaborationException
    {
        try {

            org.jabberstudio.jso.Presence p =
                        (org.jabberstudio.jso.Presence)__session.getDataFactory().createPacketNode(
                                                    XMPPSession.PRESENCE_NAME,
                                                    org.jabberstudio.jso.Presence.class);
            p.setType(type);
            p.setTo(to);
            
            XMPPSessionProvider.debug("Sending the subscribed presence to " + to);
            __session.getConnection().send(p);
        } catch(Exception e) {
            throw new CollaborationException(e.toString());
        }
    }

    private org.netbeans.lib.collab.Presence getPresence(org.jabberstudio.jso.Presence p) {
        String uid;
        String status = null;
        String note;
        float priority;
        if (p.hasPriority()) priority = ((float)(p.getPriority() + 128))/(float)MAX_PRIORITY;
        else priority = 0.5f;
        note = p.getStatus();
        if (p.getType() == null) {
            String show = p.getShow();
            if (show.equalsIgnoreCase(CHAT)) {
                status = STATUS_CHAT;
            } else if (show.equalsIgnoreCase(AWAY)) {
                status = STATUS_IDLE;
            } else if (show.equalsIgnoreCase(XA)) {
                status =  STATUS_AWAY;
            } else if (show.equalsIgnoreCase(DND)) {
                status = STATUS_BUSY;
            } else {
                status = STATUS_OPEN;
            }
        } else if (p.getType().equals(org.jabberstudio.jso.Presence.UNAVAILABLE)) {
            //For now the forwarded detection is done based on the status message
            //Change the code to use the extensions.
            if (note.equalsIgnoreCase("FORWARDED")) {
                status = STATUS_FORWARDED;
            } else {
                status = STATUS_CLOSED;
            }
        }

        uid = getFromJID(p).toString();

        return createPresence(uid, status, note, priority);
    }

    private org.jabberstudio.jso.Presence setPresence(org.netbeans.lib.collab.Presence presence) 
        throws CollaborationException {
        return setPresence(presence, null);
    }

    private org.jabberstudio.jso.Presence setPresence(org.netbeans.lib.collab.Presence presence,
            String rcpt) throws CollaborationException {
        org.jabberstudio.jso.Presence p =
                        (org.jabberstudio.jso.Presence)__session.getDataFactory().createPacketNode(
                                            XMPPSession.PRESENCE_NAME,
                                            org.jabberstudio.jso.Presence.class);

        Object o[] = presence.getTuples().toArray();
        PresenceTuple pt = (PresenceTuple)o[0];
        float priority = pt.getPriority();
        if (priority != 0) p.setPriority((int) (priority * MAX_PRIORITY - 128));
        p.setStatus(pt.getNote());
        String status = pt.getStatus();
        if ((status.equalsIgnoreCase(STATUS_CLOSED)) ||
        (status.equalsIgnoreCase(STATUS_FORWARDED))) {
            p.setType(org.jabberstudio.jso.Presence.UNAVAILABLE);
        } else if (status.equalsIgnoreCase(STATUS_CHAT)) {
            p.setShow(CHAT);
        } else if (status.equalsIgnoreCase(STATUS_IDLE)) {
            p.setShow(AWAY);
        } else if (status.equalsIgnoreCase(STATUS_AWAY)) {
            p.setShow(XA);
        } else if (status.equalsIgnoreCase(STATUS_BUSY)) {
            p.setShow(DND);
        }
                
        if (rcpt != null) p.setTo(new JID(rcpt));
        return p;
    }

    /////////
    ////Inner classes
    /////////
    private class PresenceNotifier implements Runnable {
        org.netbeans.lib.collab.Presence presence;
        org.jabberstudio.jso.Presence xmppPresence;

        PresenceNotifier(Presence p, org.jabberstudio.jso.Presence x)
        {
            this.presence = p;
            this.xmppPresence = x;
        }

        public void run() {
            synchronized(_presenceServiceListeners) {
                for(Iterator itr = _presenceServiceListeners.iterator();
                    itr.hasNext();) {
                    PresenceServiceListener serviceListener = (PresenceServiceListener)itr.next();
                    if (serviceListener == null) continue;
                    try {
                        //Check for the type of presence packet and call the corresponding listener method
                        if (xmppPresence == null ||
                            xmppPresence.getType() == null) {
                            serviceListener.onPresence(presence);
                            if (xmppPresence != null) {
                                removeSubscriptions(getFromJID(xmppPresence));
                            }
                        } else {
                            JID from = getFromJID(xmppPresence);
                            if (org.jabberstudio.jso.Presence.SUBSCRIBE == xmppPresence.getType()) {
                                if (__session.isGatewayEntry(from)) {
                                    sendPresence(org.jabberstudio.jso.Presence.SUBSCRIBED,from);
                                } else {
                                    serviceListener.onSubscribeRequest(presence);
                                }
                            } else if (org.jabberstudio.jso.Presence.UNSUBSCRIBE.equals(xmppPresence.getType())) {
                                serviceListener.onUnsubscribe(presence);
                            } else if (org.jabberstudio.jso.Presence.SUBSCRIBED.equals(xmppPresence.getType())) {
                                serviceListener.onSubscribed(presence);
                            } else if (org.jabberstudio.jso.Presence.UNSUBSCRIBED.equals(xmppPresence.getType())) {
                                serviceListener.onUnsubscribed(presence);
                                removeSubscriptions(from);
                                XMPPSessionProvider.debug("uncaching presence for " + from);
                                _cache.remove(from);
                            } else if (org.jabberstudio.jso.Presence.UNAVAILABLE.equals(xmppPresence.getType())) {
                                serviceListener.onPresence(presence);
                                removeSubscriptions(from);
                                XMPPSessionProvider.debug("uncaching presence for " + from);
                                _cache.remove(from);
                            } else {
                                XMPPSessionProvider.debug("Unknown packet type " + xmppPresence.getType());
                            }
                        }
                    } catch(Exception e) {
                        XMPPSessionProvider.error(e.toString(),e);
                    }
                }
            }
        }
    }

    private JID getFromJID(org.jabberstudio.jso.Presence p) {
        //As per section 9.1.2  of RFC 3920
        //if from is null then assume it to be the current user
        JID from = p.getFrom();
        if(from == null) {
            try {
                from = ((XMPPPrincipal)__session.getPrincipal()).getJID();
            } catch(CollaborationException e) {
                //should not happen
                XMPPSessionProvider.error("Error occured on getPrincipal() " + e);
            }
        }
        return from;
    }

    private void _firePresenceServiceListener(org.netbeans.lib.collab.Presence imPresence, org.jabberstudio.jso.Presence jsoPresence) {
        __session.addWorkerRunnable(new PresenceNotifier(imPresence, jsoPresence));
    }
    
    public void addPresenceServiceListener(PresenceServiceListener listener) {
        if (!_presenceServiceListeners.contains(listener))
            _presenceServiceListeners.add(listener);
    }
    
    public void removePresenceServiceListener(PresenceServiceListener listener) {
        _presenceServiceListeners.remove(listener);
    }
}

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

import java.util.Iterator;
import java.util.ListIterator;
import java.util.List;
import java.util.Set;
import java.util.LinkedList;
import java.util.Map;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import org.netbeans.lib.collab.*;

//imports from jso library

import org.jabberstudio.jso.*;
import org.jabberstudio.jso.x.disco.*;
import org.jabberstudio.jso.x.xdata.*;

import org.netbeans.lib.collab.xmpp.jso.iface.x.muc.*;

import org.netbeans.lib.collab.util.StringUtility;

/**
 *
 * 
 * @author Rahul Shah
 * 
 */


public class XMPPConference implements org.netbeans.lib.collab.Conference {
    
    protected JID _jid; // need a better abstraction than this, representing the conference and NewsChannel
    
    /** Creates a new instance of XMPPConference */
    protected XMPPSession _session;
    private ConferenceListener _listener;
    private int _access;
    private static final int _initialChatMsgCacheSize = 10;
    private ArrayList _initialChatMessages = new ArrayList(_initialChatMsgCacheSize);    
    boolean _joined = false;
    boolean isPrivate = false;
    //nick name of the user in this room
    private String _nick = null;
    Hashtable inviteListeners = new Hashtable();
    //Set _participants = new HashSet();
    Hashtable _participants = new Hashtable();
    int currentUserPrivilege = -1;
    Affiliation.Type currentUserAffiliation = null;
    //final static String MODERATE = "moderate";
    //final static String PRIVATE = "private";
    //final static String LEFT_MESSAGE = "left_message";
    String _recipient = null;
    boolean isGroupChat = false;
    String _thread = null;
    boolean isJEP0085Supported = false;
    boolean isFirstMessage = true;

    // room configuration.  This information is accessible 
    // to all users.  it includes name, description, 
    // occupancy, current subject, etc...
    Map _cachedMetadata;

    // room features.  This information is accessible 
    // to all users.  it includes room features such
    // as open, moderated, hidden ....
    Set _cachedFeatures = null;

    // room configuration.  This information is only accessible 
    // to room admnistrators
    XDataForm _configForm;
    // This is used to keep track of when the configuration is
    // locally modified, so that it is not refreshed before being
    // saved.
    boolean _configDirtyFlag = false;

    // room display name.  set it using roomconfig_roomname.
    // get it from disco#info
    String _displayName = null;

    //nick name of the moderator
    String moderator = null;
    public static final String CHATSTATE_NAMESPACE = "http://jabber.org/protocol/chatstates";
    public static final NSI NSI_ACTIVE = new NSI("active", CHATSTATE_NAMESPACE);
    public static final NSI NSI_INACTIVE = new NSI("inactive", CHATSTATE_NAMESPACE);
    public static final NSI NSI_COMPOSING = new NSI("composing", CHATSTATE_NAMESPACE);
    public static final NSI NSI_PAUSE = new NSI("pause", CHATSTATE_NAMESPACE);
    public static final NSI NSI_GONE = new NSI("gone", CHATSTATE_NAMESPACE);
    
    public static final String MODERATION_SUBMIT = "submit";
    public static final String MODERATION_ACCEPTED = "accepted";
    public static final String MODERATION_PENDING = "pending";
    public static final String MODERATION_MODIFIED = "modified";
    public static final String MODERATION_REJECTED = "rejected";
    public static final String ATTR_TYPE = "type";
    public static final String ATTR_FROM = "from";
    public static final String MODERATION_NAMESPACE = "sun:xmpp:moderation";
    public static final NSI NSI_START = new NSI("start", MODERATION_NAMESPACE);
    public static final NSI NSI_STOP = new NSI("stop", MODERATION_NAMESPACE);
    public static final NSI NSI_MESSAGE = new NSI("x", MODERATION_NAMESPACE);
    public static final NSI NSI_ACTION = new NSI("action", null);
    public static final NSI NSI_REASON = new NSI("reason", null);
    public static final NSI NSI_ORIGINATOR = new NSI("originator", null);
    
    //will be true only between conversion of one to one chat to muc room
    private boolean _isConvertedToMuc = false;
    
    private Object _historyLock = new Object();
    private LinkedList _historyMessages = null;
    private boolean _useThread = true;

    public XMPPConference() {
    }
    
    public XMPPConference(XMPPSession session,
                          ConferenceListener listener,
                          int access)
        throws CollaborationException 
    {
       createRandomRoom(session,listener,access,null);
    }
    public XMPPConference(XMPPSession session,
                          ConferenceListener listener,
                          int access,JID component)
       throws CollaborationException 
    {
       createRandomRoom(session,listener,access,component); 
    }
    
    public XMPPConference(XMPPSession session,
                          String destination,
                          ConferenceListener listener,
                          int access)
        throws CollaborationException 
    {
        initializeNewRoom(session,destination,listener,access,null);
    }
    public XMPPConference(XMPPSession session,
                          String destination,
                          ConferenceListener listener,
                          int access,JID component)
           throws CollaborationException               
    {
        initializeNewRoom(session,destination,listener,access,component);
        
    }
    
    private void createRandomRoom(XMPPSession session,
                                  ConferenceListener listener,
                                  int access,
                                  JID component)
                                  throws CollaborationException
    {
        _session = session;
        _listener = listener;
        addParticipant(_session.getCurrentUserJID());
        JID service = ( component == null ) ? getService() : component;
        _jid = makeConferenceJID(service.toString());
        _access = access;
        //userStatusChange(_session.getCurrentUserJID().toBareJID().toString(),
        userStatusChange(_session.getCurrentUserJID().toString(),
                         ConferenceEvent.ETYPE_USER_JOINED);
        isPrivate = true;
	currentUserPrivilege = PUBLISH | LISTEN;
    }
    
    private void initializeNewRoom(XMPPSession session,
                          String destination,
                          ConferenceListener listener,
                          int access,JID component) throws CollaborationException
    {
        _session = session;
        _listener = listener;
        createNewRoom(destination,component);
        try {
            setDefaultPrivilege(access);
        } catch(CollaborationException ce) {
            leave();
            throw ce;
        }
        _access = access;
        isGroupChat = true;
        //if listener is null leave the room once it is created
        if (_listener == null) leave();
        //_join();
    }
        
    public XMPPConference(XMPPSession session) {
        _session = session;
    }
    
    public XMPPConference(XMPPSession session, String name, JID jid) 
           throws CollaborationException 
    {
        _session = session;
        _jid = jid;
        if (_jid == null) {            
            JID service = getService();
            String n = JIDUtil.encodedString(name);
            if (service != null) {
                _jid = new JID(StringUtility.appendDomainToAddress(n,
                                                    service.toString()));
            } else {
                _jid = new JID(n);
            } 
            isPrivate = true;
        } else isGroupChat = true;
    }
    
    private JID makeConferenceJID(String domain) {
        String node = 
            Long.toString(((new java.util.Date()).getTime())) +
            Long.toString(Math.round((Math.random() * 10000)));

        return new JID(node, domain, null);
    }
    
    private void createNewRoom(String destination,JID component) throws CollaborationException {
        if (!StringUtility.hasDomain(destination)) {
            if(component == null) {
                JID service = getService();
                if (service == null) throw new ServiceUnavailableException();
                _jid = new JID(destination, service.getDomain(), null);
            }else
                _jid = new JID(destination,component.getDomain(),null);
        } else {
            _jid = new JID(destination);
        }
        _session.addConference(this);
        _join(null, null);
    }
    
    public void addMessage(org.netbeans.lib.collab.Message message) 
                throws CollaborationException 
    {
        XMPPSessionProvider.debug("_recipient is " + _recipient);
        org.jabberstudio.jso.Message m = 
                (org.jabberstudio.jso.Message)((XMPPMessage)message).getXMPPMessage();
        //add the active chat state if this is a first message
        if (isFirstMessage) {
            StreamElement activeElement = _session.getDataFactory().createElementNode(NSI_ACTIVE);
            m.add(activeElement);
            isFirstMessage = false;
        }
        if (!isGroupChat) {
            if (_recipient != null) {
                m.setType(org.jabberstudio.jso.Message.CHAT);
		if (_useThread) m.setThread(getName());
                m.setTo(new JID(_recipient));
                _session.sendAllMessageParts((XMPPMessage)message);
            }
            //call the conference listener and change the to address
            // Not too sure what impact the below code will have with multipart messages
            //
            m.setTo(m.getFrom());
            messageAdded((XMPPMessage)message);
            return;
        }
        m.setType(org.jabberstudio.jso.Message.GROUPCHAT);
        m.setTo(_jid);
        m.setThread(null);
        _session.sendAllMessageParts((XMPPMessage)message);
    }
    
    public void close() throws org.netbeans.lib.collab.CollaborationException 
    {
        InfoQuery iq = (InfoQuery)_session.getDataFactory().createPacketNode(_session.getDataFactory().createNSI("iq", _session.getConnection().getDefaultNamespace()), InfoQuery.class);
        //MUCOwnerQuery owner = new MUCOwnerQueryNode(_session.getDataFactory());
        StreamDataFactory sdf = _session.getDataFactory();
        MUCOwnerQuery owner = (org.netbeans.lib.collab.xmpp.jso.iface.x.muc.MUCOwnerQuery)sdf.createElementNode(MUCOwnerQuery.NAME, MUCOwnerQuery.class);
        //Destroy destroy = new DestroyNode(_session.getDataFactory(),owner.NAMESPACE);
        Destroy destroy = (org.netbeans.lib.collab.xmpp.jso.iface.x.muc.Destroy)sdf.createElementNode(new NSI(Destroy.NAME, owner.NAMESPACE));
        destroy.setJID(_session.getCurrentUserJID());
        owner.add(destroy);
        iq.addExtension(owner);
        iq.setType(InfoQuery.SET);
        iq.setFrom(_session.getCurrentUserJID());
        iq.setTo(_jid);
        iq.setID(_session.nextID("destroy"));
        try {
            iq = (InfoQuery)_session.sendAndWatch(iq, _session.getRequestTimeout());
            //_session.getConnection().send(iq);
        } catch(StreamException se) {
            se.printStackTrace();
        }
        if ((iq == null) || (iq.getType() != InfoQuery.RESULT))
            throw new CollaborationException("Cannot close the room!");
    }
    
    public org.netbeans.lib.collab.InviteMessage createInviteMessage() throws org.netbeans.lib.collab.CollaborationException {
        XMPPMessage m = new XMPPMessage(_session,_session.getCurrentUserJID());
        //m.getXMPPMessage().setType(org.jabberstudio.jso.Message.GROUPCHAT);
        return m;
    }
    
    public org.netbeans.lib.collab.Message createMessage() throws org.netbeans.lib.collab.CollaborationException {
        XMPPMessage m;
        if (!isGroupChat) {
            if (_recipient == null) {
                //throw new CollaborationException("First invite a user");
                XMPPSessionProvider.warning("Recipient is null. Invite a user to room");
            }
            m = new XMPPMessage(_session,
                                (_recipient != null)? new JID(_recipient):null,
                                _session.getCurrentUserJID());
            m.getXMPPMessage().setType(org.jabberstudio.jso.Message.CHAT);
	    if (_useThread) {
                ((org.jabberstudio.jso.Message)m.getXMPPMessage()).setThread(getName());
	    } else {
                ((org.jabberstudio.jso.Message)m.getXMPPMessage()).setThread(null);
	    }
            
        } else {
            m = new XMPPMessage(_session, _jid, _session.getCurrentUserJID());
            m.getXMPPMessage().setType(org.jabberstudio.jso.Message.GROUPCHAT);
        }
        return m;
    }
    
    public int getDefaultPrivilege() throws CollaborationException {
        int access = PUBLISH | LISTEN;
        if (getBooleanConfigValue("muc#roomconfig_membersonly",
                                  false, false)) {
            access = NONE;
        } else if (getBooleanConfigValue("muc#roomconfig_moderatedroom",
                                  false, false)) {
            access = LISTEN;
        }
        // if user can access config, then user has MANAGE access
        // why is this needed here?   todo
        currentUserPrivilege = MANAGE | LISTEN | PUBLISH | INVITE;
        return access;
    }
    
    public String getName() {
        return _jid.getNode();
    }
    
    public String getDestination() {
        assert _jid != null;
        return _jid.toString();
    }
    
    public JID getNode() {
        assert _jid != null;
        return _jid;
    }

    protected JID getJID() {
        assert _jid != null;
        return _jid;
    }
    
    public int getPrivilege() throws CollaborationException {
        XMPPSessionProvider.debug("Current user privilege " + currentUserPrivilege);
        if (currentUserPrivilege == -1) {
            //try the default privilege if getDefaultPrivilege was 
            // successful then current user has Manage privilege.
            int priv = getDefaultPrivilege();
        }
        if (currentUserPrivilege != -1) return currentUserPrivilege;
        throw new CollaborationException("Cannot get the privilege");
    }
    
    public int getPrivilege(String str) throws CollaborationException {
        Map map = listPrivileges();
        Integer i = (Integer)map.get(str);
        if (i != null) return i.intValue();
        //return -1;
                return getDefaultPrivilege();
        //throw new CollaborationException("Not Implemeneted");
    }

    public void setPrivileges(Map m) throws CollaborationException 
    {
        InfoQuery iq = (InfoQuery)_session.getDataFactory().createPacketNode(_session.IQ_NAME, InfoQuery.class);
        StreamDataFactory sdf = _session.getDataFactory();

        MUCOwnerQuery admin = 
            (MUCOwnerQuery)sdf.createElementNode(MUCOwnerQuery.NAME,
                                                 MUCOwnerQuery.class);

        for (Iterator i = m.keySet().iterator(); i.hasNext();) {
            String uid = (String)i.next();
            MUCItem item = (MUCItem)sdf.createElementNode(new NSI(MUCItem.NAME, admin.NAMESPACE), MUCItem.class);
            item.setJID(new JID(uid));
            item.setAffiliation(getAffiliation(((Integer)m.get(uid)).intValue()));
            if (item.getAffiliation()  == Affiliation.NONE) {
                item.setRole(MUCRole.VISITOR);
            }
            admin.add(item);
        }
        iq.addExtension(admin);
        iq.setType(InfoQuery.SET);
        iq.setFrom(_session.getCurrentUserJID());
        iq.setTo(_jid);
        iq.setID(_session.nextID("affiliation"));
        try {
            iq = (InfoQuery)_session.sendAndWatch(iq);
        } catch(StreamException se) {
            se.printStackTrace();
        }
        if ((iq == null) || (iq.getType() != InfoQuery.RESULT))
            throw new CollaborationException("Cannot set the list");
    }

    public Map listPrivileges() throws CollaborationException {
        Map users = new Hashtable();
        //get the users for all the affiliations
        users = addUserstoList(users, getUserPrivileges(Affiliation.ADMIN),
                               getAccess(Affiliation.ADMIN));
        users = addUserstoList(users, getUserPrivileges(Affiliation.MEMBER),
                               getAccess(Affiliation.MEMBER));
        users = addUserstoList(users, getUserPrivileges(Affiliation.NONE),
                               getAccess(Affiliation.NONE));
        users = addUserstoList(users, getUserPrivileges(Affiliation.OUTCAST),
                               getAccess(Affiliation.OUTCAST));
        users = addUserstoList(users, getUserPrivileges(Affiliation.OWNER),
                               getAccess(Affiliation.OWNER));
        return users;
    }

    private Map addUserstoList(Map m, List l, int access) {
        for(Iterator i = l.iterator(); i.hasNext();) {
            m.put(i.next(), new Integer(access));
        }
        return m;
    }
    
    private List getUserPrivileges(Affiliation.Type type) throws CollaborationException {
        List ret = new java.util.ArrayList();
        InfoQuery iq = (InfoQuery)_session.getDataFactory().createPacketNode(_session.IQ_NAME, InfoQuery.class);
        StreamDataFactory sdf = _session.getDataFactory();
        MUCOwnerQuery admin = (org.netbeans.lib.collab.xmpp.jso.iface.x.muc.MUCOwnerQuery)sdf.createElementNode(MUCOwnerQuery.NAME, MUCOwnerQuery.class);
        MUCItem item = (MUCItem)sdf.createElementNode(new NSI(MUCItem.NAME,admin.NAMESPACE), MUCItem.class);
        if (type != null) item.setAffiliation(type);
        admin.add(item);
        iq.addExtension(admin);
        iq.setType(InfoQuery.GET);
        iq.setFrom(_session.getCurrentUserJID());
        iq.setTo(_jid);
        iq.setID(_session.nextID("affiliation"));
        try {
            iq = (InfoQuery)_session.sendAndWatch(iq);
        } catch(StreamException se) {
            se.printStackTrace();
        }
        if ((iq == null) || (iq.getType() != InfoQuery.RESULT))
            throw new CollaborationException("Cannot get the list");
        admin = (MUCOwnerQuery) iq.listExtensions(admin.NAMESPACE).get(0);
        Iterator itr = admin.listElements().iterator();
        while (itr.hasNext()) {
            Object o = itr.next();
            if (o instanceof MUCItem) {
                ret.add(((MUCItem)o).getJID().toString());
            }
        }
        return ret;
    }
    
    private int getAccess(Affiliation.Type type) {
        if (type == null) return -1;
        if ((type == Affiliation.ADMIN) || (type == Affiliation.OWNER)) {
            return (MANAGE | PUBLISH | LISTEN | INVITE);
        }
        if (type == Affiliation.MEMBER) {
            return (PUBLISH | LISTEN);
        }
        if (type == Affiliation.NONE) {
            return LISTEN;
        }
        return NONE;
    }
    
    private Affiliation.Type getAffiliation(int access) {

        if (access >= MANAGE) return Affiliation.OWNER;
        else if (access >= PUBLISH) return Affiliation.MEMBER;
        else if (access >= LISTEN) return Affiliation.NONE;
        else return Affiliation.OUTCAST;
    }
    
    public String getProperty(String str) 
    {
        return getProperty(str,false);
    }
    
    //This is only for testing 
    public String getProperty(String str, boolean refresh) 
    {
        // check meta data
        String val = getMetaDataValue(str, refresh);

        // try configuration
        if (val == null) {
            try {
                val = getConfigValue(str, refresh);
            } catch (Exception e) {
            }
        }
        return val;
    }
        
    boolean getFeature(String name, boolean refresh, boolean defaultVal)
    {
        boolean b = defaultVal;
        try {
            Set features = getFeatures(refresh);
            if (features != null) {
                b = features.contains(name);
            }
        } catch (Exception e) {
        }
        return b;
    }

    String getMetaDataValue(String name, boolean refresh) {
        String val = null;
        try {
            getFeatures(refresh);
            if (_cachedMetadata != null) {
                val = (String)_cachedMetadata.get(name);
            }
        } catch (Exception e) {
        }
        return val;
    }

    boolean getBooleanConfigValue(String name, boolean defaultValue,
                                 boolean refresh)
        throws CollaborationException
    {
        boolean val = defaultValue;
        XDataForm form = getConfigForm(refresh);
        XDataField f = form.getField(name);
        if (f != null && XDataField.BOOLEAN.equals(f.getType())) {
            val = f.getBooleanValue().booleanValue();
        } else {
            throw new ServiceUnavailableException(name + " is not configurable or not boolean");
        }
        return val;
    }

    String getConfigValue(String name, boolean refresh)
        throws CollaborationException
    {
        String val = null;
        XDataForm form = getConfigForm(refresh);
        XDataField f = form.getField(name);
        if (f != null) {
            val = f.getValue();
        } else {
            throw new ServiceUnavailableException(name + " is not configurable");
        }
        return val;
    }

    void setConfigValue(String name, String val, boolean refresh) 
        throws CollaborationException
    {
        XDataForm form = getConfigForm(refresh);
        XDataField f = form.getField(name);
        if (f != null) {
            f.setValue(val);
        } else {
            throw new ServiceUnavailableException(name + " is not configurable");
        }
        _configDirtyFlag = true;
    }

    void setBooleanConfigValue(String name, boolean val, boolean refresh) 
        throws CollaborationException
    {
        XDataForm form = getConfigForm(refresh);
        XDataField f = form.getField(name);
        if (f != null && XDataField.BOOLEAN.equals(f.getType())) {
            f.setBooleanValue(new Boolean(val));
        } else {
            throw new ServiceUnavailableException("Presence broadcast is not configurable or not boolean");
        }
        _configDirtyFlag = true;
    }


    public boolean isPublic() 
    {
        return (!isTemporary() && isPersistent());
    }

    public boolean isTemporary() 
    {
        if (!isGroupChat) return true;
        else return getFeature("muc_temporary", false, true);
    }

    public boolean isPersistent() 
    {
        if (!isGroupChat) return false;
        else return getFeature("muc_persistent", false, true);
    }


    public void invite(int param, 
                       org.netbeans.lib.collab.Message message, 
                       InviteMessageStatusListener inviteListener) 
                       throws CollaborationException 
    {
        org.jabberstudio.jso.Message m = 
                    (org.jabberstudio.jso.Message)((XMPPMessage)message).getXMPPMessage();
        StreamElement activeElement = _session.getDataFactory().createElementNode(NSI_ACTIVE);
        m.add(activeElement);
        String rcpt[] = message.getRecipients();
        if ((!isGroupChat) && ((_participants.size() == 1) && (rcpt.length == 1))) {                
            m.setFrom(_session.getCurrentUserJID());
            m.setTo(new JID(rcpt[0]));
            m.setID(null);
            m.setType(org.jabberstudio.jso.Message.CHAT);
            if (_useThread) m.setThread(getName());            
            _session.sendAllMessageParts((XMPPMessage)message);
            if (inviteListener != null) {        
                inviteListeners.put(rcpt[0],inviteListener);
            }
            _participants.put(rcpt[0], new JID(rcpt[0]));
            //userStatusChange(rcpt[0],ConferenceEvent.ETYPE_USER_JOINED);
            _recipient = rcpt[0];
            //call the conference listener and change the to address
            /*
            m.setTo(m.getFrom());
            messageAdded((XMPPMessage)message);                
             */
            return;
        }
        MessagePart[] parts = message.getParts();
        String content = null;
        if (parts != null) {
            content = parts[0].getContent();
        }
        StreamDataFactory sdf = _session.getDataFactory();
        MUCUserQuery user = (MUCUserQuery)sdf.createElementNode(MUCUserQuery.NAME, MUCUserQuery.class);
        Invite invite = (Invite)sdf.createElementNode(Invite.NAME, Invite.class);            
        user.add(invite);            
        m.add(user);
        m.setTo(_jid);
        m.setFrom(null);
        m.setID(null);
        m.setType(null);
        m.setBody(null);
        m.setThread(null);
        if ((!isGroupChat) && 
            ((_participants.size() == 2) || ((_participants.size() < 2) && (rcpt.length > 1))))
        {
            if (_recipient != null) {
                //converting one to one chat to muc
                _isConvertedToMuc = true;
            }
            createNewRoom(getName(),null);
            //_join(null);
            if (_recipient != null) {
                invite.setTo(new JID(_recipient));
                invite.setContinue();
                _session.sendAllMessageParts((XMPPMessage)message);
            } 
            _recipient = null;
            invite.removeContinue();
        }
        invite.setReason(content);
        for(int i = 0; i < rcpt.length; i++) {
            invite.setTo(new JID(rcpt[i]));
            _session.sendAllMessageParts((XMPPMessage)message);
            if (inviteListener != null) {        
                inviteListeners.put(rcpt[i],inviteListener);
            }
        }
    }
    
    public void handleInviteReply(org.jabberstudio.jso.Message m) {
        XMPPSessionProvider.debug("Handle Invite");
        //try {
        MUCUserQuery user = (MUCUserQuery)m.getExtension(MUCUserQuery.NAMESPACE);
        String rcpt = null;
        if (user == null) return;
        Iterator itr = user.listElements().iterator();
        while (itr.hasNext()) {
            Object o = itr.next();
            if (o instanceof Decline) {
                rcpt = ((Decline)o).getFrom().toString();
                InviteMessageStatusListener inviteListener = (InviteMessageStatusListener) inviteListeners.get(rcpt);
                if (inviteListener != null) {
                    XMPPSessionProvider.debug("invite listener not null- decline");
                    inviteListener.onRsvp(rcpt,false);
                    inviteListeners.remove(rcpt);
                    //inviteListener.onRsvp(((Decline)o).getFrom(),null,false);
                } else XMPPSessionProvider.debug("invite listener null- decline");
                return;
            } else if (o instanceof Invite) {
                rcpt = ((Invite)o).getFrom().toString();
                if (((Invite)o).hasContinue()) {
                    //the message is an invite message sent when CHAT was converted to GROUPCHAT
                    try {
                        _isConvertedToMuc = true;
                        _join(null, null);
                    } catch(CollaborationException e) {
                        e.printStackTrace();
                    }
                    _recipient = null;
                    return;
                }
                InviteMessageStatusListener inviteListener = (InviteMessageStatusListener) inviteListeners.get(rcpt);
                if (inviteListener != null) {
                    XMPPSessionProvider.debug("invite listener not null- invite");
                    inviteListener.onRsvp(rcpt,true);
                    inviteListeners.remove(rcpt);
                    //inviteListener.onRsvp(((Decline)o).getFrom(),null,false);
                } else XMPPSessionProvider.debug("invite listener null- invite");
            }
        }
    }
    
    public void join(ConferenceListener conferenceListener) throws CollaborationException {
        join(null, null, conferenceListener);
    }
    
    public void join(String nick, ConferenceHistory history, ConferenceListener conferenceListener) throws CollaborationException {
        _session.addConference(this);
        if (isGroupChat || (_participants.size() > 2)) {
            _listener = conferenceListener;
            _join(nick, history);
        } else if ((!isGroupChat) && (_listener == null) &&(_recipient != null)) {
            
            synchronized (_initialChatMessages) {
                if (_initialChatMessages.size() > 0) {
                    for (Iterator i = _initialChatMessages.iterator(); i.hasNext();) {
                        org.netbeans.lib.collab.Message msg = (org.netbeans.lib.collab.Message)i.next();
                        conferenceListener.onMessageAdded(msg);
                    }
                }
                _initialChatMessages.clear();
                _listener = conferenceListener;
            }           
            
            //send a join notification
            XMPPSessionProvider.debug("Sending join notification " + _recipient);
            //userStatusChange(_session.getCurrentUserJID().toBareJID().toString(), ConferenceEvent.ETYPE_USER_JOINED);
            userStatusChange(_session.getCurrentUserJID().toString(), 
                    ConferenceEvent.ETYPE_USER_JOINED);
            userStatusChange(_recipient, ConferenceEvent.ETYPE_USER_JOINED);
        }
    }
    
    public void leave() {
        try {
            if (isGroupChat) {
                StreamDataFactory sdf = _session.getDataFactory();
                
                org.jabberstudio.jso.Presence p = (org.jabberstudio.jso.Presence)sdf.createPacketNode(
                                                            XMPPSession.PRESENCE_NAME, org.jabberstudio.jso.Presence.class);                
                p.setTo(new JID(_jid.getNode(), _jid.getDomain(),_nick));
                p.setFrom(_session.getCurrentUserJID());
                p.setType(org.jabberstudio.jso.Presence.UNAVAILABLE);
                if ((moderator != null) && (_session.isCurrentUser((JID)_participants.get(moderator)))) {
                    p.add(sdf.createElementNode(NSI_STOP));
                    moderator = null;
                }
                _session.getConnection().send(p);
                synchronized(this) {
                    if (_joined) {
                        _listener = null;
                        _joined = false;
                    }
                }
                _session.removeConference(_jid.toString());
            } else {
                if (isJEP0085Supported) {
                    _listener = null;
                    StreamElement element = _session.getDataFactory().createElementNode(NSI_GONE);
                    sendStatusMessage(element);
                }
                /*if (_recipient != null) {
                    //send a leave message
                    XMPPMessage message = (XMPPMessage)createMessage();
                    org.jabberstudio.jso.Message m = 
                        (org.jabberstudio.jso.Message)(message).getXMPPMessage();
                    m.setFrom(((XMPPPrincipal)_session.getPrincipal()).getJID());
                    m.setTo(new JID(_recipient));
                    m.setID(null);
                    m.setType(org.jabberstudio.jso.Message.CHAT);
                    if (_useThread) m.setThread(getName());
                    m.setBody(msg);
                    message.setHeader(LEFT_MESSAGE, "true");
                    //_session.getConnection().send(m);
                    _session.sendAllMessageParts(message);
                }*/
                _session.removeConference(getName());
            }
            _participants.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void setDefaultPrivilege(int access) throws CollaborationException 
    {
        setConfigValue("muc#roomconfig_persistentroom", "1", false);
        boolean moderated = false;
        boolean restricted = false;
        if (access >= PUBLISH) {
            // no change
        } else if (access >= LISTEN) {
            moderated = true;
        } else {
            restricted = true;
        }
        setBooleanConfigValue("muc#roomconfig_moderatedroom",
                              moderated, false);
        setBooleanConfigValue("muc#roomconfig_membersonly",
                              restricted, false);

        saveConfiguration();
    }
    
    private Map getProperties() throws CollaborationException {
        Map prop = new HashMap();
        XDataForm form = getConfigForm(false);
        if (form != null) {
            extractXData(form, prop);
        }
        return prop;
    }

    /**
     * @deprecated config info should be maintained in xdata form
     */
    private void extractXData(XDataForm form, Map map) 
    {
        List fields = form.listFields();
        for (Iterator fieldItr = form.listFields().iterator();
             fieldItr.hasNext(); ) {
            XDataField field = (XDataField)fieldItr.next();
            if (XDataField.LIST_MULTI.equals(field.getType()) ||
                XDataField.JID_MULTI.equals(field.getType())) {
                map.put(field.getVar(), field.listValues());
            } else {
                map.put(field.getVar(), field.getValue());
            }
        }
    }

    private XDataForm getConfigForm(boolean refresh) 
        throws CollaborationException
    {
        if (refresh || _configForm == null) {
            InfoQuery iq = (InfoQuery)_session.getDataFactory().createPacketNode(_session.IQ_NAME, InfoQuery.class);
            iq.setType(InfoQuery.GET);
            iq.setTo(_jid);        
            // JEP-0045 says that you send muc#owner iq using your jid
            //iq.setFrom(new JID(_jid.toString() + "/" + ((XMPPPrincipal)_session.getPrincipal()).getJID().getNode()));
            iq.setFrom(_session.getCurrentUserJID());
            iq.setID(_session.nextID("room"));
            //MUCOwnerQuery owner = new MUCOwnerQueryNode(_session.getDataFactory());
            StreamDataFactory sdf = _session.getDataFactory();
            MUCOwnerQuery owner = (MUCOwnerQuery)sdf.createElementNode(MUCOwnerQuery.NAME, MUCOwnerQuery.class);
            
            iq.add(owner);
            
            try {
                InfoQuery response = (InfoQuery)_session.sendAndWatch(iq);
                if (response == null) {
                    throw new TimeoutException("Timeout while getting the configuration");
                } else if (response.getType() != InfoQuery.RESULT) {
                    // todo throw proper exception subclass
                    throw new CollaborationException("Cannot get the configuration");
                }
                
                StreamElement q = response.getFirstElement(MUCOwnerQuery.NAME);
                _configForm = (XDataForm)q.getFirstElement(XDataForm.NAME);
                _configForm.detach();

            } catch(StreamException e) {
                e.printStackTrace();
                throw new CollaborationException(e);
            }
        }
        return _configForm;
    }


    private void saveConfiguration() throws CollaborationException 
    {
        if (_configForm == null) return;

        InfoQuery iq = (InfoQuery)_session.getDataFactory().createPacketNode(_session.IQ_NAME, InfoQuery.class);
        StreamDataFactory sdf = _session.getDataFactory();
        MUCOwnerQuery owner = (MUCOwnerQuery)sdf.createElementNode(MUCOwnerQuery.NAME, MUCOwnerQuery.class);
        iq.add(owner);

        owner.add(_configForm);
        iq.setType(InfoQuery.SET);
        iq.setID(_session.nextID("room"));
        iq.setFrom(_session.getCurrentUserJID());
        iq.setTo(_jid);                        
        try {
            InfoQuery response = (InfoQuery)_session.sendAndWatch(iq, _session.getRequestTimeout());
            if (response == null) {
                throw new TimeoutException("Timeout while changing the default privilege");
            } else if (response.getType() != InfoQuery.RESULT) {
                throw new CollaborationException("Cannot set the default privilege");
            }

            _configDirtyFlag = false;

        } catch(StreamException e) {
            e.printStackTrace();
            throw new CollaborationException(e);
        }

    }
    
    public void setPrivilege(String uid, int param) 
        throws CollaborationException 
    {
        Map m = listPrivileges();
        m.put(uid, new Integer(param));
        setPrivileges(m);
    }
    
    public void setProperty(String str, String str1) throws CollaborationException {
        // assume it is a configuration prop
        setConfigValue(str, str1, false);
    }
    
    public boolean hasPrivilege(int accessLevel) throws CollaborationException {
        if (currentUserPrivilege == -1) { 
            try {
                //get the default privilege
                getDefaultPrivilege();
            } catch(CollaborationException ce) {
                //will happen when the user does not have manage privilege
                XMPPSessionProvider.error("Cannot get the default privlege");
                return false;
            }
        }
        return (currentUserPrivilege >= accessLevel);
    }
    
    public void moderate(boolean start) throws CollaborationException 
    {
        if (!isPublic()) 
            throw new CollaborationException("Moderation is supported only for public conferences");
        if (start && (moderator != null)) 
            throw new CollaborationException("Only one moderator is allowed per room");
        if (!start) {
            if (moderator == null)
                throw new CollaborationException("No one is moderating the room");
            if (!_session.isCurrentUser((JID)_participants.get(moderator))) 
                throw new CollaborationException("Unauthorized to stop moderation");
        }
        try {
            StreamDataFactory sdf = _session.getDataFactory();
            org.jabberstudio.jso.Presence p = (org.jabberstudio.jso.Presence)sdf.createPacketNode(
                                                XMPPSession.PRESENCE_NAME, org.jabberstudio.jso.Presence.class);
            
            p.setTo(_jid);
            p.setFrom(_session.getCurrentUserJID());
            if (start) {
                p.add(sdf.createElementNode(NSI_START));
            } else {
                p.add(sdf.createElementNode(NSI_STOP));
            }
            _session.getConnection().send(p);
        } catch (StreamException e) {
            XMPPSessionProvider.error(e.toString(), e);
            throw new CollaborationException("Error while moderating the room");
        }
    }
    
    protected synchronized void _join(String nick, ConferenceHistory history) throws CollaborationException {
        if (!_joined && XMPPConferenceService.HISTORY_TIME > 0) {
	    synchronized(_historyLock) {
		_historyMessages = new LinkedList();
		Timer t = new Timer(true);
		t.schedule(new TimerTask() {
		   public void run() {
		     clearQueue();
		   }
		},XMPPConferenceService.HISTORY_TIME);
	    }
        }
        try {
            StreamDataFactory sdf = _session.getDataFactory();
            org.jabberstudio.jso.Presence p = 
                (org.jabberstudio.jso.Presence)sdf.createPacketNode(
                                        XMPPSession.PRESENCE_NAME,
                                        org.jabberstudio.jso.Presence.class);
           
            
            if (nick == null) {
		JID j = _session.getCurrentUserJID();
                nick = j.getNode() + "-" + j.getResource();
            }
            p.setTo(new JID(_jid.getNode(),_jid.getDomain(), nick));
            p.setFrom(_session.getCurrentUserJID());
            //p.setID(_session.nextID("conference"));
            MUCQuery mucQuery = (MUCQuery)sdf.createElementNode(MUCQuery.NAME, MUCQuery.class);
            p.add(mucQuery);
            if (history != null) {
                History h = (History)sdf.createElementNode(History.NAME, History.class);
                /*int maxChars = history.getMaxChars();
                if (maxChars != -1) h.setMaxChars(maxChars);*/
                int maxMessages = history.getMaxMessages();
                if (maxMessages != -1) h.setMaxStanzas(maxMessages);
                int sec = history.getSeconds();
                if (sec != -1) h.setSeconds(sec);
                java.util.Date d = history.getSince();
                if (d != null) h.setSince(d);
                mucQuery.add(h);
            }
            //check if the room is password protected
            if (getFeature("muc_passwordprotected", false, false) && 
                _listener instanceof ConferencePasswordListener) 
            {
                String pass = ((ConferencePasswordListener)_listener).getPassword();
                if (pass != null) mucQuery.setPassword(pass);
            }
            //the user should get the reply packet with the privileges before doing 
            //any other operation
            p = (org.jabberstudio.jso.Presence)_session.sendAndWatch(p, _session.getRequestTimeout());
            if (p == null) {
                throw new TimeoutException("");
            } else if (p.getType() == org.jabberstudio.jso.Presence.ERROR) {
                throw XMPPSession.getCollaborationException(p.getError(), null);
                /*PacketError error = p.getError();
                if (error.getFirstElement(PacketError.CONFLICT_CONDITION) != null)
                {
                    throw new ConflictException("Nick name conflict");
                }
                throw new CollaborationException("Cannot join the room");*/
            }
            _nick = nick;
            _joined = true;
            isGroupChat = true;
            //_participants = new Hashtable();

            // force config to be reloaded next time
            if (!_configDirtyFlag) _configForm = null;

        } catch (StreamException e) {
            e.printStackTrace();
            throw new CollaborationException("Cannot create the room");
        }
    }
    
    public void userStatusChange(org.jabberstudio.jso.Presence p) {
        JID jid = null;
        JID roomJID = p.getFrom();
        MUCUserQuery user = 
            (MUCUserQuery)p.getExtension(MUCUserQuery.NAMESPACE);
        Iterator itr = user.listElements().iterator();
        while (itr.hasNext()) {
            Object o = itr.next();
            if (o instanceof MUCItem) {
                MUCItem item = (MUCItem)o;
                jid = item.getJID();
		int oldPrivilege = currentUserPrivilege;
                if (_session.isCurrentUser(jid)) {
                    MUCRole.Type role = item.getRole();
                    if (role == MUCRole.MODERATOR)
                        currentUserPrivilege = MANAGE | INVITE | LISTEN | PUBLISH;
                    else if (role == MUCRole.PARTICIPANT)
                        currentUserPrivilege = INVITE | LISTEN | PUBLISH;
                    else if (role == MUCRole.VISITOR)
                        currentUserPrivilege = LISTEN;
                    else
                        currentUserPrivilege = NONE;
                    currentUserAffiliation = item.getAffiliation();
                    XMPPSessionProvider.debug("Current user privilege "+ currentUserPrivilege);
                }
                if ((_listener == null) || (jid == null) || (roomJID == null)) continue;
                ConferenceEventTuple cet = new ConferenceEventTuple(roomJID.toString());
                cet.id = jid.toString();
                if (p.getType() == null) {
                    cet.status = Integer.toString(ConferenceEvent.ETYPE_USER_JOINED);
                    //if the user was already present in the room then dont send the join remove the older entry from _participants
                    //if (!_participants.add(jid)) return;
                    String nick = getNickFromJID(jid);
                    if (nick != null) {
			if (oldPrivilege != currentUserPrivilege) {
			    cet.status = Integer.toString(ConferenceEvent.ETYPE_ACCESS_MODIFIED);
			}
                        _participants.remove(nick);
                        _participants.put(p.getFrom().getResource(),jid);
                        //do not send the event when the room is being converted from 
                        //one to one chat to muc room.
                        if (!_isConvertedToMuc) {
                            fireEventListener(cet);
                        }
                    } else {
                        _participants.put(p.getFrom().getResource(),jid);
                        //send the active chat state whenever someone joins
                        isFirstMessage = true;
                        //if this is new participant then most likely the conversion
                        //is complete so reset the variable
                        _isConvertedToMuc = false;
                        fireEventListener(cet);
                    }
                } else if (p.getType().equals(org.jabberstudio.jso.Presence.UNAVAILABLE)) {
		    Destroy destroy = (Destroy)item.getFirstElement(Destroy.NAME);
                    if (destroy != null) {
                        cet.status = Integer.toString(ConferenceEvent.ETYPE_CLOSE);
                        cet.destination = destroy.getJID().toString();
                        _joined = false;
                        _session.removeConference(_jid.toString());
                        _participants.clear();
                    } else {
                        cet.status = Integer.toString(ConferenceEvent.ETYPE_USER_LEFT);
                        //_participants.remove(jid);
                        _participants.remove(p.getFrom().getResource());
                    }
                    fireEventListener(cet);
                }
                if (p.getFirstElement(NSI_START) != null) {
                    //TODO: Handle the error for moderation
                    if (p.getType() == Packet.ERROR) return;
                    cet.status = Integer.toString(ConferenceEvent.ETYPE_MODERATION_STARTED);
                    moderator = p.getFrom().getResource();
                    fireEventListener(cet);
                } else if (p.getFirstElement(NSI_STOP) != null) {
                    //TODO: Handle the error for moderation
                    if (p.getType() == Packet.ERROR) return;
                    cet.status = Integer.toString(ConferenceEvent.ETYPE_MODERATION_STOPED);
                    moderator = null;
                    fireEventListener(cet);
                }
            }
        }
    }
    
    public void userStatusChange(String jid, int status) {
        //if there was a invite message status listener then notify the listener
        //instead of Conference listener.
        if ((!isGroupChat) && (status == ConferenceEvent.ETYPE_USER_LEFT)) 
        {            
            String rcpt = StringUtility.removeResource(jid);
            //remove this user from the participants list
            if (jid.equals(_recipient)) {
                _participants.remove(jid);
                _recipient = null;
            } else if (rcpt.equals(_recipient)) {
                _participants.remove(rcpt);
                _recipient = null;
            }

            InviteMessageStatusListener inviteListener = (InviteMessageStatusListener) inviteListeners.remove(jid);
            if(inviteListener == null) {
                inviteListener = (InviteMessageStatusListener) inviteListeners.remove(rcpt);
            }
            if (inviteListener != null) {
                XMPPSessionProvider.debug("Invite listener not null - " + rcpt);
                inviteListener.onRsvp(rcpt,false);                
                return;
            }
            isJEP0085Supported = false;
        }
        if (_listener == null) return;
        ConferenceEventTuple cet = new ConferenceEventTuple(jid);
        cet.id = (isGroupChat) ? getParticipant(StringUtility.getResource(jid)).toString() : jid;
        if (status == MessageStatus.TYPING_ON) {
            cet.status = Integer.toString(ConferenceEvent.ETYPE_USER_INPUT_STARTED);
        } else if (status == MessageStatus.TYPING_OFF) {
            cet.status = Integer.toString(ConferenceEvent.ETYPE_USER_INPUT_STOPED);
        } else {
            cet.status = Integer.toString(status);
        }
        fireEventListener(cet);       
    }
        
    private void fireEventListener(ConferenceEventTuple cet) 
    {
        // force config to be reloaded next time
        if (!_configDirtyFlag) _configForm = null;
        
        if (_listener instanceof ConferenceEventListener) {
            ((ConferenceEventListener)_listener).onEvent(cet);
        } else {
            ConferenceEventHelper ceh = new ConferenceEventHelper();
            ceh.addTuple(cet);
            _listener.onEvent(ceh.toString());
        }        
    }
    
    public void messageAdded(XMPPMessage m) {
        if (isGroupChat) {
                if (_listener != null) {
                org.jabberstudio.jso.Message msg = 
                    (org.jabberstudio.jso.Message)m.getXMPPMessage();
                String nick = msg.getFrom().getResource();
                JID jid = (JID)_participants.get(nick);
                String uid = (jid == null) ? nick:jid.toString();
                try {
                    m.setOriginator(uid);
                } catch(CollaborationException e) {
                    e.printStackTrace();
                }
                StreamElement element = msg.getFirstElement(NSI_MESSAGE);
                if (element != null) {
                    if (org.jabberstudio.jso.Message.GROUPCHAT.equals(msg.getType())) {
                        StreamElement orig = element.getFirstElement(NSI_ORIGINATOR);
                        String from = orig.getAttributeValue(ATTR_FROM);
                        try {
                            m.setOriginator(from);
                        } catch(CollaborationException e) {
                            e.printStackTrace();
                        }
                        _listener.onModeratedMessageAdded(m);
                    } else {
                        StreamElement action = element.getFirstElement(NSI_ACTION);
                        String type = action.getAttributeValue(ATTR_TYPE);
                        StreamElement reasonElem = action.getFirstElement(NSI_REASON);
                        String reason = (reasonElem == null)? null: reasonElem.normalizeTrimText();
                        if (MODERATION_SUBMIT.equals(type)) {
                            _listener.onModeratedMessageStatus(m, STATUS_SUBMIT, reason);
                        } else if (MODERATION_ACCEPTED.equals(type)) {
                            _listener.onModeratedMessageStatus(m, STATUS_APPROVED, reason);
                        } else if (MODERATION_MODIFIED.equals(type)) {
                            _listener.onModeratedMessageStatus(m, STATUS_MODIFIED, reason);
                        } else if (MODERATION_REJECTED.equals(type)) {
                            _listener.onModeratedMessageStatus(m, STATUS_REJECTED, reason);
                        } else if (MODERATION_PENDING.equals(type)) {
                            _listener.onModeratedMessageStatus(m, STATUS_PENDING, reason);
                        } 
                    }
                } else {
		    if (XMPPConferenceService.HISTORY_TIME > 0) {
			queue(m);
		    } else {
                        _listener.onMessageAdded(m);
		    }
                }
            } else {
                // listener is null
                XMPPSessionProvider.debug("listener is null");
            }
                                          
        } else {
            // it is 1 to 1 private chat
            synchronized(_initialChatMessages) {
                if (_listener == null) {
                    // buffer the initial chat messages
                    if (_initialChatMessages.size() < _initialChatMsgCacheSize) {
                        _initialChatMessages.add(m);
                    }
                } else {
                    _listener.onMessageAdded(m);
                }
            }            
        }
    }

    private void queue(XMPPMessage m) {
        synchronized (_historyLock) {
            if (_historyMessages == null) {
                _listener.onMessageAdded(m);
                return;
            }
            boolean append = true, prepend = false;
            for (ListIterator i = _historyMessages.listIterator() ; i.hasNext(); ) {
                XMPPMessage hm = (XMPPMessage)i.next();
                if (hm.getTime() > m.getTime()) {
                    if (i.hasPrevious()) {
                        i.previous();
                        i.add(m);
                    } else {
                        prepend = true;
                    }
                    append = false;
                    break;
                }
            }
            if (append) _historyMessages.add(m);
            if (prepend) _historyMessages.add(0, m);
        }
    }

    private void clearQueue() {
	synchronized(_historyLock) {
	    for(Iterator itr = _historyMessages.iterator(); itr.hasNext();) {
                _listener.onMessageAdded((XMPPMessage)itr.next());
            }
            _historyMessages = null;
	}
    }
    
    public void addParticipant(JID jid) {
        if ((!_session.isCurrentUser(jid)) && (_participants.size() < 2)) {
            _recipient = jid.toString();
            XMPPSessionProvider.debug("_recipient = " + _recipient);
        }
        XMPPSessionProvider.debug("Adding participant = " + jid.toString());
        //_participants.add(jid.toString());
        _participants.put(jid.toString(), jid);
    }
    
    /*public void setPrivate(String str) {
        if (str == null) return;
        isPrivate = (new Boolean(str)).booleanValue();
    }*/
    
    public JID getParticipant(String jid) {
        return (JID)_participants.get(jid);
    }
    
    public void setGroupChat(boolean b) {
        isGroupChat = b;
    }
    
    public boolean sendStatus(int msgStatus) throws CollaborationException {
        if (!isJEP0085Supported) return false;
        StreamElement element;
        StreamDataFactory sdf = _session.getDataFactory();
        switch(msgStatus) {
            case MessageStatus.TYPING_ON:
                element = sdf.createElementNode(NSI_COMPOSING);
                break;
            case MessageStatus.TYPING_OFF:
                element = sdf.createElementNode(NSI_PAUSE);
                break;
            default:
                return false;
        }
        return sendStatusMessage(element);
    }
    
    public boolean sendStatusMessage(StreamElement element) throws CollaborationException {
        //create a message and add this element and send it.
        Stream connection = _session.getConnection();
        // create an xmpp message with originator and recipient swapped
        StreamDataFactory sdf = _session.getDataFactory();
        org.jabberstudio.jso.Message msg  = (org.jabberstudio.jso.Message)sdf.createPacketNode(
                                                       XMPPSession.MESSAGE_NAME, 
                                                       org.jabberstudio.jso.Message.class);        
        msg.setFrom(_session.getCurrentUserJID());
        msg.add(element);
        if (!isGroupChat) {
            if (_recipient != null) msg.setTo(new JID(_recipient));
            msg.setType(org.jabberstudio.jso.Message.CHAT);
            if (_useThread) msg.setThread(getName());
        } else {
            msg.setTo(_jid);
            msg.setType(org.jabberstudio.jso.Message.GROUPCHAT);
        }
        try {
            if (msg.getTo() != null) connection.send(msg);
            //if the message type is chat echo the event to self
            if (!isGroupChat) {
                msg.setTo(msg.getFrom());
                _session.processMessage((Packet)msg);
            }
        } catch (StreamException se) {
           throw new CollaborationException(se);
        }
        return true;
    }
    
    public void processChatStates(org.jabberstudio.jso.Message in) throws CollaborationException {
        
        
        if(isFirstMessage) {
            StreamElement activeElement = in.getFirstElement(NSI_ACTIVE);
            if ((activeElement != null) &&
                (!in.getFrom().equals(_session.getCurrentUserJID()))) 
            {

                String rcpt = in.getFrom().toString();
                String whichListener = rcpt;
                InviteMessageStatusListener inviteListener = (InviteMessageStatusListener) inviteListeners.get(rcpt);
                if (null == inviteListener){
                    whichListener = StringUtility.removeResource(rcpt);
                    inviteListener = (InviteMessageStatusListener) inviteListeners
                            .get(whichListener);
                }
                if ((in.getType() == org.jabberstudio.jso.Message.CHAT) &&
                    (inviteListener != null)) 
                {
                    XMPPSessionProvider.debug("[Process Chat States] Invite listener not null - " + rcpt);
                    inviteListener.onRsvp(rcpt,true);                    
                    userStatusChange(rcpt,ConferenceEvent.ETYPE_USER_JOINED);
                    // in case this was not removed, remove it here
                    inviteListeners.remove(whichListener);
                }
            }
        } else {
            String uid = in.getFrom().toString();
            StreamElement element = in.getFirstElement(NSI_COMPOSING);
            if (element != null) {
                userStatusChange(uid, MessageStatus.TYPING_ON);
            }
            element = in.getFirstElement(NSI_PAUSE);
            if (element != null) {
                userStatusChange(uid, MessageStatus.TYPING_OFF);
            }
            element = in.getFirstElement(NSI_INACTIVE);
            if (element != null) {
                isJEP0085Supported = false;
            }
        }
    }
    
    public void addModeratedMessage(org.netbeans.lib.collab.Message msg, int status, String reason) 
                throws CollaborationException 
    {
        if (!isPublic()) 
            throw new CollaborationException("Moderation is supported only for public conferences");
        //throw new CollaborationException("Not Implemented");
        if (moderator == null)
            throw new CollaborationException("No moderator for this room");
        XMPPMessage message = ((XMPPMessage)msg).copy();
        org.jabberstudio.jso.Message m = 
                (org.jabberstudio.jso.Message)message.getXMPPMessage();
        String nick;
        StreamElement element = m.getFirstElement(NSI_MESSAGE);
        if (element == null) {
            element = _session.getDataFactory().createElementNode(NSI_MESSAGE);
            m.add(element);
        }
        if (status == STATUS_SUBMIT) {
            if (currentUserPrivilege < LISTEN) {
                throw new CollaborationException("Users with LISTEN privilege can post to moderator");
            }
            nick = moderator;
        } else {
            if (!_session.isCurrentUser((JID)_participants.get(moderator))) 
                throw new CollaborationException("Not Authorized");
            nick = getNickFromJID(m.getFrom());
            m.setFrom(_session.getCurrentUserJID());
            StreamElement action = element.getFirstElement(NSI_ACTION);
            if (action == null) {
                action = _session.getDataFactory().createElementNode(NSI_ACTION);
                element.add(action);
            }
            switch(status) {
                case STATUS_APPROVED :
                    action.setAttributeValue(ATTR_TYPE,MODERATION_ACCEPTED);
                    break;
                case STATUS_MODIFIED :
                    action.setAttributeValue(ATTR_TYPE,MODERATION_MODIFIED);
                    break;
                case STATUS_REJECTED :
                    action.setAttributeValue(ATTR_TYPE,MODERATION_REJECTED);
                    break;
                case STATUS_PENDING :
                    action.setAttributeValue(ATTR_TYPE,MODERATION_PENDING);
                    break;
                default:
                    throw new CollaborationException("Illegal status " + status);
            }
            StreamElement reasonElem = action.getFirstElement(NSI_REASON);
            if (reasonElem == null) {
                reasonElem = _session.getDataFactory().createElementNode(NSI_REASON);
                action.add(reasonElem);
            }
            reasonElem.clearText();
            reasonElem.addText(reason);
        }
        m.setTo(new JID(_jid.getNode(), _jid.getDomain(), nick));
        m.setType(org.jabberstudio.jso.Message.CHAT);
        m.setThread(null);
        _session.sendAllMessageParts(message);
    }
    
    private Set getFeatures(boolean refresh) throws CollaborationException
    {
        if (refresh || _cachedFeatures == null) {
            InfoQuery iq = (InfoQuery)_session.getDataFactory()
                .createPacketNode(_session.IQ_NAME, InfoQuery.class);
            iq.setType(InfoQuery.GET);
            iq.setTo(_jid);
            iq.setFrom(_session.getCurrentUserJID());
            iq.setID(_session.nextID("room"));
            StreamDataFactory sdf = _session.getDataFactory();
            DiscoInfoQuery disco = 
                (DiscoInfoQuery)sdf.createElementNode(DiscoInfoQuery.NAME);
            iq.add(disco);
            try {
                InfoQuery response = (InfoQuery)_session.sendAndWatch(iq, _session.getShortRequestTimeout());
                if (response == null) {
                    throw new CollaborationException("No disco#info response received");
                } else if (response.getType() == Packet.ERROR) {
                    throw XMPPSession.getCollaborationException(response.getError(), null);
                    //throw new CollaborationException("disco#info query failed");
                } else {
                    disco = (DiscoInfoQuery)response
                        .getFirstElement(DiscoInfoQuery.NAME);
                    updateConference(disco);
                }
            } catch(StreamException e) {
                throw new CollaborationException(e);
            }
        }
        return _cachedFeatures;
    }
    
    
    void updateConference(DiscoInfoQuery disco){
        if (disco != null) {
            _cachedFeatures = disco.getFeatures();
            DiscoIdentity did =
                disco.getIdentity("conference", "text");
            if (did != null) _displayName = did.getName();
        }
        //The xdataform is inside the diso info.
        //get by Namespace does not work
        XDataForm form = (XDataForm)disco.getFirstElement(XDataForm.class);
        if (form != null) {
            HashMap prop = new HashMap();
            extractXData(form, prop);
            _cachedMetadata = prop;
        }
    }

    public Collection getParticipants() throws CollaborationException {
	if (!isGroupChat) return _participants.keySet();
        List list = new java.util.ArrayList();
        InfoQuery iq = (InfoQuery)_session.getDataFactory().createPacketNode(_session.IQ_NAME, InfoQuery.class);
        iq.setType(InfoQuery.GET);
        iq.setTo(_jid);
        iq.setFrom(_session.getCurrentUserJID());
        iq.setID(_session.nextID("room"));
        //MUCOwnerQuery owner = new MUCOwnerQueryNode(_session.getDataFactory());
        StreamDataFactory sdf = _session.getDataFactory();
        DiscoItemsQuery disco = (DiscoItemsQuery)sdf.createElementNode(DiscoItemsQuery.NAME, DiscoItemsQuery.class);
        
        iq.add(disco);
        try {
            InfoQuery response = (InfoQuery)_session.sendAndWatch(iq, _session.getShortRequestTimeout());
            if (response == null) {
                throw new CollaborationException("Cannot get the participants list");

            } else if (response.getType() == Packet.ERROR) {
                throw XMPPSession.getCollaborationException(response.getError(), "Cannot get the participants list");
                //throw new CollaborationException("Admin privilege required");
                //throw new CollaborationException("Cannot get the participants list");
            }
            disco = (DiscoItemsQuery)response.listExtensions(DiscoItemsQuery.NAMESPACE).get(0);
            Iterator itr = disco.listItems().iterator();
            while (itr.hasNext()) {
                DiscoItem item = (DiscoItem)itr.next();
                JID jid = item.getJID();
                if (jid != null)
                    list.add(jid.toString());
            }
        } catch(StreamException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    private String getNickFromJID(JID jid) 
    {
        for(Enumeration en = _participants.keys(); en.hasMoreElements();) {
            String key = (String)en.nextElement();
            JID value = (JID)_participants.get(key);
            //XMPPSessionProvider.debug("key = " + key + " value = " + value + " jid = " + jid);
            if (jid.equals(value) //|| jid.equals(value.toBareJID())
            ) {
                return key;
            }
        }
        return null;
    }
    
    private JID getService() {
       try {
                XMPPConferenceService s = (XMPPConferenceService)_session.getConferenceService();
                return s.getService();
        } catch(CollaborationException e) {
            e.printStackTrace();
            return null;
        }
    }
     
    public boolean isPresentInRoom() {
        if (_recipient != null) return true;
        if (isGroupChat && _joined) return true;
        return false;
    }
    
    public void sendChatInviteReply(org.netbeans.lib.collab.Message message, boolean invite) 
                throws CollaborationException
    {
        if (!isJEP0085Supported) return;
        XMPPMessage m = (XMPPMessage)message;
        if (m == null) {
            m = new XMPPMessage(_session,
                                    (_recipient != null)? new JID(_recipient):null,
                                    _session.getCurrentUserJID());
                
        }
        org.jabberstudio.jso.Message xmppMsg = 
                            (org.jabberstudio.jso.Message)m.getXMPPMessage();
        if (invite) {
            if (isFirstMessage) {
                StreamElement activeElement = _session.getDataFactory().createElementNode(NSI_ACTIVE);
                xmppMsg.add(activeElement);
                isFirstMessage = false;
            } else {
                return;
            }
        } else {
            _listener = null;
            StreamElement goneElement = _session.getDataFactory().createElementNode(NSI_GONE);
            xmppMsg.add(goneElement);
            _session.removeConference(getName());
        }
        xmppMsg.setType(org.jabberstudio.jso.Message.CHAT);
        if (_useThread) xmppMsg.setThread(getName());
        _session.sendAllMessageParts(m);
    }
    
    public boolean isParticipant(String jid) {        
        return _participants.containsKey(jid) ? true : false;
    }
    
    public boolean isOne2OnePrivateChat() {
        //return _recipient != null;
        return !isGroupChat && !isPublic();
    }
    
    public void setNode(JID jid) {
        _jid = jid;
    }




    public String getDisplayName()
    {
        try {
            getFeatures((_displayName == null));
        } catch(Exception e) {
            e.printStackTrace(); // DEBUG
            // just return null
        }
	if (_displayName == null || "".equals(_displayName.trim())) {
	    _displayName = getName();
	}
        return _displayName;
    }
     
    public void setDisplayName(String name) 
        throws ServiceUnavailableException
    {
        try {
            XDataForm form = getConfigForm(false);
            XDataField f = form.getField("muc#roomconfig_roomname");
            if (f == null) {
                throw new ServiceUnavailableException("Conference display name is not configurable");
            } else {
                f.setValue(name);
                _displayName = null;
            }
        } catch(Exception e) {
            
        }
        _configDirtyFlag = true;
    }

    public void save() throws CollaborationException
    {
        saveConfiguration();
    }


    public void setEventMask(int mask) 
        throws ServiceUnavailableException
    {
        try {
            XDataForm form = getConfigForm(false);
            XDataField f = form.getField("muc#roomconfig_presencebroadcast");
            if (f != null) {
                f.clearValues();
                if ((mask & LISTEN) == 0) f.addValue("visitor");
                if ((mask & PUBLISH) == 0) f.addValue("participant");
                if ((mask & MANAGE) == 0) f.addValue("moderator");
            } else {
                throw new ServiceUnavailableException("Presence broadcast is not configurable");
            }
            // do not commit yet / call save later
        } catch(Exception e) {
            e.printStackTrace();
        }
        _configDirtyFlag = true;
    }

    public int getEventMask() throws ServiceUnavailableException
    {
        int mask = LISTEN | MANAGE | PUBLISH;
        try {
            XDataForm form = getConfigForm(false);
            XDataField f = form.getField("muc#roomconfig_presencebroadcast");
            if (f != null) {
                List roles = f.listValues();
                if (roles != null && !roles.isEmpty()) {
                    for (Iterator i = roles.iterator(); i.hasNext(); ) {
                        String role = (String)i.next();
                        if ("visitor".equals(role)) {
                            mask &= (MANAGE | PUBLISH);
                        }
                        if ("participant".equals(role)) {
                            mask &= (MANAGE | LISTEN);
                        }
                        if ("moderator".equals(role)) {
                            mask &= (LISTEN | PUBLISH);
                        }
                    }
                }
            } else {
                throw new ServiceUnavailableException("Presence broadcast is not configurable");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return mask;
    }

    public void useThreads(boolean b) {
	_useThread = b;
    }
    
    public void useChatStates(boolean b) {
        isJEP0085Supported = b;
    }

}

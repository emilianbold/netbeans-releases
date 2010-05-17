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

import java.util.*;

import org.netbeans.lib.collab.*;

//imports from the jso libary

import org.jabberstudio.jso.* ;
import org.jabberstudio.jso.x.disco.*;
import org.jabberstudio.jso.x.xdata.*;

import org.netbeans.lib.collab.xmpp.jso.iface.x.pubsub.PubSubQuery;
import org.netbeans.lib.collab.xmpp.jso.iface.x.pubsub.PubSubElement;
import org.netbeans.lib.collab.xmpp.jso.iface.x.pubsub.EntityContainer;
import org.netbeans.lib.collab.xmpp.jso.iface.x.pubsub.PubSubEntityElement;

import org.netbeans.lib.collab.xmpp.jso.impl.x.pubsub.PubSubQueryNode;
import org.netbeans.lib.collab.util.StringUtility;

/**
 *
 * 
 */
public class XMPPNewsChannel extends XMPPConference implements NewsChannel {
    
    private NewsChannelListener _listener;
    private XMPPNewsService _newsService;
    // private Map _user2SubStatus = new HashMap();
    
    private String _pepNode = null;

    XDataForm _metaDataForm;
    XDataForm _metaData;

    // channel display name. set it using meta-data.
    // get it from disco#info
    String _displayName = null;

    public static final String NAMESPACE_METADATA = "http://jabber.org/protocol/pubsub#meta-data";
    public static final NSI NSI_METADATA = new NSI("query", NAMESPACE_METADATA);
    
    /** Creates a new instance of XMPPNewsChannel */
    XMPPNewsChannel() {      
    }
    
    
    XMPPNewsChannel(XMPPNewsService service, XMPPSession s,String node)
            throws CollaborationException {
        super(s);
        _newsService = service;
        init(s, node, null);        
    }
    
    /**
     * retreive the existing newschannel and subscribe  to it
     */
    XMPPNewsChannel(XMPPNewsService service, XMPPSession s, String destination,
            NewsChannelListener listener) 
           throws CollaborationException 
    {
        super(s);
        _newsService = service;
        init(s, destination, listener);        
        if (listener != null) {
            subscribe(listener);
        } 
    }
    
    /**
     * create a new newschannel and subscribe to it
     */
    XMPPNewsChannel(XMPPNewsService service, XMPPSession s, String destination,
            NewsChannelListener listener, int defaultAccess)
            throws CollaborationException
    {
        super(s);
        XMPPSessionProvider.debug("Creating the new newschannel ");
        _newsService = service;
        init(s, destination, listener);
        //_listener = listener;
        //_newsService = (XMPPNewsService)s.getNewsService();
        // Assume that the domain of the newschannel will be same as users domain       
        
        /*
        if (_jid.getNode() == null || _jid.getNode().equals("")) {
            XMPPSessionProvider.debug("DOMAIN is not there");
            _jid = null;
            _jid = JIDUtil.encodedJID(destination, getServiceJID().toString(), null);            
        } 
         */        
        createNewChannel(s, destination);
        setDefaultPrivilege(defaultAccess);
        subscribe(listener);
        
    }
    
    private JID getServiceJID() throws CollaborationException {
        return (_newsService != null) ? _newsService.getService() : null;
    }
    
    private void init(XMPPSession s, String destination, NewsChannelListener listener)
                          throws CollaborationException {
        _listener = listener;
        if (s.getApplicationInfo().hasFeature(destination)) {
            _pepNode = destination;
        } else {
            String fqDest = null;
            fqDest = StringUtility.appendDomainToAddress(destination, getServiceJID().toString());
            destination = fqDest;
        
            if (destination != null) _jid = new JID(destination);
        }
    }
    
    private void createNewChannel(XMPPSession s, String destination)
      throws CollaborationException {
        //try {
        // to do, remove duplication of creating of infoquery object code through out this class and encapsulate it in a method.
        StreamDataFactory sdf = _session.getDataFactory();
        Stream connection = _session.getConnection();
        InfoQuery iq = (InfoQuery)sdf.createPacketNode(XMPPSession.IQ_NAME, InfoQuery.class);
        PubSubQuery pubsubquery = (PubSubQuery)sdf.createExtensionNode(PubSubQuery.NAME,PubSubQuery.class);
        PubSubElement createElem = pubsubquery.createCreateElement(JIDUtil.encodedString(destination));
        pubsubquery.add(createElem);
        
        iq.addExtension(pubsubquery);
        iq.setType(InfoQuery.SET);
        iq.setTo(getServiceJID());
        iq.setFrom(((XMPPPrincipal)_session.getPrincipal()).getJID());
        iq.setID(_session.nextID("pubsub"));
        // use packetWatcher instead to send the message
        try {
            iq = (InfoQuery)_session.sendAndWatch(iq,_session.getShortRequestTimeout());
        } catch (StreamException se) {
            throw new CollaborationException(se.toString());
        }
        
        if (iq == null) {
            throw new TimeoutException("Request timed out");
        } else if (iq.getType() == InfoQuery.RESULT) {
            return;
        } else if (iq.getType() == InfoQuery.ERROR) {
            PacketError error = iq.getError();
            if (error != null) {
                String errorCond = error.getDefinedCondition();
                if (PacketError.FEATURE_NOT_IMPLEMENTED_CONDITION.equals(errorCond)
                || (PacketError.SERVICE_UNAVAILABLE_CONDITION.equals(errorCond))) {
                    throw new ServiceUnavailableException(error.getText());
                } else if (PacketError.CONFLICT_CONDITION.equals(errorCond)) {
                    throw new ConflictException(error.getText());
                } else if (PacketError.REGISTRATION_REQUIRED_CONDITION.equals(errorCond)) {
                    throw new CollaborationException(error.getText());
                } else {
                    throw new CollaborationException(error.getText());
                }
            }
        }
    }
    
    public org.netbeans.lib.collab.Message createMessage() {
        try {
            if (_pepNode != null) {
                return new XMPPNewsMessage(_session, _pepNode,
                        ((XMPPPrincipal)_session.getPrincipal()).getJID(),
                        null);
            } else {
                return new XMPPNewsMessage(_session, _jid.toString(),
                        ((XMPPPrincipal)_session.getPrincipal()).getJID(),
                        getServiceJID());
            }
        } catch(CollaborationException ce) {
ce.printStackTrace();
            return null;
        }
    }
    
    /**
     * override the addMessage method from the XMPPConference
     */
    
    public void addMessage(org.netbeans.lib.collab.Message message) throws CollaborationException {
        //_session.sendAllMessageParts((XMPPMessage)message);
        //try {
            InfoQuery iq = (InfoQuery)((XMPPMessage)message).getXMPPMessage();
            Stream connection = _session.getConnection();
            try {
                iq = (InfoQuery)_session.sendAndWatch(iq,_session.getRequestTimeout());
            } catch (StreamException se) {
                throw new CollaborationException(se.getMessage());
            }                       
            if (iq == null) {
                throw new TimeoutException("Request timed out");
            } else if (iq.getType() == InfoQuery.RESULT) {
                return;
            } else if (iq.getType() == InfoQuery.ERROR) {
                PacketError error = iq.getError();
                if (error != null) {
                    String errorCond = error.getDefinedCondition();
                    if (PacketError.FEATURE_NOT_IMPLEMENTED_CONDITION.equals(errorCond)
                    || (PacketError.SERVICE_UNAVAILABLE_CONDITION.equals(errorCond))) {
                        throw new ServiceUnavailableException(error.getText());
                    } else if (PacketError.CONFLICT_CONDITION.equals(errorCond)) {
                        throw new ConflictException(error.getText());
                    } else if (PacketError.REGISTRATION_REQUIRED_CONDITION.equals(errorCond)) {
                        throw new CollaborationException(error.getText());
                    } else {
                        throw new CollaborationException(error.getText());
                    }
                }
            }                
    }
    
    public void modifyMessage(String msgId, org.netbeans.lib.collab.Message message) throws org.netbeans.lib.collab.CollaborationException {
        //Assume that this message will have a pubsub item payload
        InfoQuery xmppMessage = (InfoQuery)((XMPPMessage)message).getXMPPMessage();
        // modify the id of the message
        // need to write a method which will get elements nested
        // at any level, the jso library does not have such method
        
        List l = xmppMessage.listElements(PubSubQuery.NAME,PubSubQuery.class);
        try {
            if (l.iterator().hasNext()) {
                StreamElement elem = (StreamElement)l.iterator().next();
                l = elem.listElements(PubSubQueryNode.PubSubPublishElement.NAME, PubSubQueryNode.PubSubPublishElement.class);
            }
            PubSubQueryNode.PubSubPublishElement publishElem = null;
            if (l.iterator().hasNext()) {
                publishElem = (PubSubQueryNode.PubSubPublishElement)l.iterator().next();
            }
            PubSubQueryNode.PubSubItemElement item = (PubSubQueryNode.PubSubItemElement)publishElem.getFirstElement(PubSubQueryNode.PubSubItemElement.NAME, PubSubQueryNode.PubSubItemElement.class);
            item.setID(msgId);
            
            _session.getConnection().send(xmppMessage);
        } catch(Exception e) {
            
        }
    }
    
    public void removeMessage(String msgId) throws org.netbeans.lib.collab.CollaborationException {
        
        try {            
            StreamDataFactory sdf = _session.getDataFactory();
            Stream connection = _session.getConnection();
            InfoQuery iq = (InfoQuery)sdf.createPacketNode(XMPPSession.IQ_NAME, InfoQuery.class);
            PubSubQuery pubsubquery = (PubSubQuery)sdf.createExtensionNode(PubSubQuery.NAME,PubSubQuery.class);
            PubSubElement retractElem = (PubSubElement)pubsubquery.createRetractElement(_jid.getNode(),msgId);
            pubsubquery.add(retractElem);
            iq.addExtension(pubsubquery);
            iq.setType(InfoQuery.SET);
            iq.setTo(getServiceJID());
            iq.setFrom(((XMPPPrincipal)_session.getPrincipal()).getJID());
            iq.setID(_session.nextID("pubsub"));            
            connection.send(iq);
        } catch(Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public void subscribe(org.netbeans.lib.collab.NewsChannelListener newsChannelListener)
    throws org.netbeans.lib.collab.CollaborationException {      
        _listener = newsChannelListener;
        
            if (_listener != null) {                
                StreamDataFactory sdf = _session.getDataFactory();
                Stream connection = _session.getConnection();
                InfoQuery iq = (InfoQuery)sdf.createPacketNode(XMPPSession.IQ_NAME, InfoQuery.class);
                
                PubSubQuery pubsubquery = 
                    (PubSubQuery)sdf.createExtensionNode(PubSubQuery.NAME, PubSubQuery.class);
                // Subscribing to bare jid - required behaviour ?
                PubSubElement subElem = 
                    pubsubquery.createSubscribeElement(XMPPNewsService.encode(_jid.toString(),
                                                    getServiceJID().toString()),
                           ((XMPPPrincipal)_session.getPrincipal()).getJID().toBareJID());
                
                pubsubquery.add(subElem);
                iq.add(pubsubquery);
                iq.setType(InfoQuery.SET);
                iq.setTo(getServiceJID());
                iq.setFrom(((XMPPPrincipal)_session.getPrincipal()).getJID());
                iq.setID(_session.nextID("pubsub"));
                
                try {
                    iq = (InfoQuery)_session.sendAndWatch(iq,
                                                               _session.getShortRequestTimeout());
                } catch (StreamException se) {
                    throw new CollaborationException(se.getMessage());
                }                
                                                 
                if (iq == null) {
                    throw new TimeoutException("Request timed out");
                } else if (iq.getType() == InfoQuery.RESULT) {
                    _session.addNewsChannel(this);
                    return;
                } else if (iq.getType() == InfoQuery.ERROR) {
                    PacketError error = iq.getError();
                    if (error != null) {
                        String errorCond = error.getDefinedCondition();
                        if (PacketError.FEATURE_NOT_IMPLEMENTED_CONDITION.equals(errorCond)
                        || (PacketError.SERVICE_UNAVAILABLE_CONDITION.equals(errorCond))) {
                            throw new ServiceUnavailableException(error.getText());
                        } else if (PacketError.CONFLICT_CONDITION.equals(errorCond)) {
                            throw new ConflictException(error.getText());
                        } else if (PacketError.REGISTRATION_REQUIRED_CONDITION.equals(errorCond)) {
                            throw new CollaborationException(error.getText());
                        } else {
                            throw new CollaborationException(error.getText());
                        }
                    }
                }
            }                
    }
    
    public StreamElement getConfiguration() throws CollaborationException {
        XMPPSessionProvider.debug("Getting configuration .......");
        StreamDataFactory sdf = _session.getDataFactory();
        Stream connection = _session.getConnection();
        InfoQuery iq = (InfoQuery)sdf.createPacketNode(XMPPSession.IQ_NAME, InfoQuery.class);
        PubSubQuery pubsubquery = (PubSubQuery)sdf.createExtensionNode(new NSI("pubsub",PubSubQuery.NAMESPACE_OWNER));            
        PubSubElement configureElem = pubsubquery.createConfigureElement(XMPPNewsService.encode(_jid.toString(),
                                                            getServiceJID().toString()));
        pubsubquery.add(configureElem);
        iq.add(pubsubquery);
        iq.setType(InfoQuery.GET);               
        iq.setTo(getServiceJID());
        iq.setFrom(((XMPPPrincipal)_session.getPrincipal()).getJID());
        iq.setID(_session.nextID("pubsub"));
        try {           
            iq = (InfoQuery)_session.sendAndWatch(iq,_session.getShortRequestTimeout());
            
            if (iq == null || iq.getType() != InfoQuery.RESULT) {
                throw new CollaborationException("NewsChannel " + _jid.toString() + " does not exist");
            }
        } catch(StreamException se) {
            se.printStackTrace();
            throw new CollaborationException(se.toString());
        }
        pubsubquery = (PubSubQuery)iq.getExtension(PubSubQuery.NAMESPACE_OWNER);
        configureElem = (PubSubElement)pubsubquery.getFirstElement("configure");
        return configureElem;
    }
    
    
    // override this method in XMPPconference
    // unsubscribe the newsChannel
    
    public void leave() {
        try {
            StreamDataFactory sdf = _session.getDataFactory();
            Stream connection = _session.getConnection();
            InfoQuery iq = (InfoQuery)sdf.createPacketNode(XMPPSession.IQ_NAME, InfoQuery.class);
            
            PubSubQuery pubsubquery = (PubSubQuery)sdf.createExtensionNode(PubSubQuery.NAME, PubSubQueryNode.class);
            // Subscribing to bare jid - required behaviour ?
            PubSubElement unsubElem = pubsubquery.createUnSubscribeElement(
                    XMPPNewsService.encode(
                        _jid.toString(), getServiceJID().toString()),
                    ((XMPPPrincipal)_session.getPrincipal()).getJID().toBareJID());
            pubsubquery.add(unsubElem);
            iq.setType(InfoQuery.SET);            
            iq.setTo(getServiceJID());
            iq.setFrom(((XMPPPrincipal)_session.getPrincipal()).getJID());
            iq.setID(_session.nextID("pubsub"));
            iq.addExtension(pubsubquery);
            connection.send(iq);
        } catch(Exception e) {
            // throw new CollaborationException(e.toString());
        }
        
    }
    
    public void close() throws CollaborationException {
        StreamDataFactory sdf =        _session.getDataFactory();
        Stream connection = _session.getConnection();
        try {
            InfoQuery iq = (InfoQuery)sdf.createPacketNode(XMPPSession.IQ_NAME, InfoQuery.class);
            
            PubSubQuery pubsubquery = (PubSubQuery)sdf.createExtensionNode(PubSubQuery.NAME, PubSubQueryNode.class);
            PubSubElement deleteElem = pubsubquery.createDeleteElement(XMPPNewsService.encode(_jid.toString(),
                                                        getServiceJID().toString()));
            pubsubquery.add(deleteElem);
            iq.addExtension(pubsubquery);
            iq.setType(InfoQuery.SET);           
            iq.setTo(getServiceJID());
            iq.setFrom(((XMPPPrincipal)_session.getPrincipal()).getJID());
            iq.setID(_session.nextID("pubsub"));
            connection.send(iq);
        } catch(org.jabberstudio.jso.StreamException se) {
            throw new CollaborationException(se.toString());
        }
                _session.removeNewsChannel(this);
    }
    
    private class PubSubModel {
        String publishModel;
        String subscriptionModel;
    }
    
    public void setDefaultPrivilege(int accesslevel) throws CollaborationException {
        
        StreamDataFactory sdf =        _session.getDataFactory();
        Stream connection = _session.getConnection();        
        try {
            InfoQuery iq = (InfoQuery)sdf.createPacketNode(XMPPSession.IQ_NAME, InfoQuery.class);
            
            PubSubQuery pubsubquery = (PubSubQuery)sdf.createExtensionNode(PubSubQuery.NAME_OWNER);
            StreamElement config = getConfiguration();
            XDataForm xform = (XDataForm)config.getFirstElement(XDataForm.NAME);
            xform.setType(XDataForm.SUBMIT);
            XDataField pubField =  xform.getField("pubsub#publish_model");
            XDataField subField =  xform.getField("pubsub#subscription_model");
            PubSubModel pubsubModel = defaultAccess2PubSubModel(accesslevel);
            
            subField.setValue(pubsubModel.subscriptionModel);
            pubField.setValue(pubsubModel.publishModel);
            subField.clearOptions();
            pubField.clearOptions();
            
            pubsubquery.add(config);
            iq.addExtension(pubsubquery);            
            iq.setType(InfoQuery.SET);        
            iq.setTo(getServiceJID());
            iq.setFrom(((XMPPPrincipal)_session.getPrincipal()).getJID());
            iq.setID(_session.nextID("pubsub"));
            connection.send(iq);            
        } catch(org.jabberstudio.jso.StreamException se) {
            se.printStackTrace();
            throw new CollaborationException(se.toString());
            
        }        
    }
    
    public int getPrivilege() throws CollaborationException {
        XMPPPrincipal principal = (XMPPPrincipal)_newsService.__session.getPrincipal();
        return getPrivilege(JIDUtil.getBareJIDString(principal.getUID()));
    }
    
    public int getPrivilege(String uid) throws CollaborationException {       
        Map m = listPrivileges();
        Integer accessInteger = (Integer)m.get(uid);
        int access = -1;
        if (accessInteger != null) {
            access = accessInteger.intValue();            
        } else {
            access = getDefaultPrivilege();
        }
        return access;        
    }
    
    public void setPrivilege(String uid, int accesslevel) throws org.netbeans.lib.collab.CollaborationException {
        Map m = new HashMap();
        m.put(uid, new Integer(accesslevel));
        setPrivileges(m);        
    }
    
    public boolean hasPrivilege(int accessLevel) throws CollaborationException {
        return (getPrivilege() >= accessLevel);
    }
    
    public int getDefaultPrivilege() throws CollaborationException {
        StreamElement config = getConfiguration();
        XDataForm xform = (XDataForm)config.getFirstElement(XDataForm.NAME);
        XDataField pubField =  xform.getField("pubsub#publish_model");
        XDataField subField =  xform.getField("pubsub#subscription_model"); 
        // What if these two fields are not there
        String publishModel = null;
        String subscriptionModel = null;
        if (pubField != null) {
            publishModel = pubField.getValue();
        } 
        if (subField != null) {
            subscriptionModel = subField.getValue();           
        } 
        return pubSubModel2DefaultAccess(publishModel,subscriptionModel);        
    }
        
    public Map listPrivileges() throws CollaborationException {
        Map user2Priv = new HashMap();
        
        EntityContainer entitiesElem;
        
        try {
            entitiesElem = getEntitiesElementResponse();
        } catch(org.jabberstudio.jso.StreamException se) {
            throw new CollaborationException(se.toString());
        }
                        
        for (Iterator i = entitiesElem.listPubSubEntities().iterator(); i.hasNext();) {
            PubSubEntityElement entityElem = (PubSubEntityElement)i.next();
            if (_newsService.isIncludedInACL(entityElem)) {
                String affiliation = entityElem.getAffiliation().toString();
                int access = _session.affiliation2Access(affiliation);
                String userID = entityElem.getSubscriberJID().toString();
                user2Priv.put(userID,new Integer(access));
            }
        }    
        return user2Priv;
    }
    
    private Map listUserSubscriptionStatus() throws CollaborationException {
        Map user2Status = new HashMap();
        EntityContainer entitiesElem = null;
        try {
            entitiesElem = getEntitiesElementResponse();            
        } catch(org.jabberstudio.jso.StreamException se) {
            throw new CollaborationException(se.toString());
        } 
        
        for (Iterator i = entitiesElem.listPubSubEntities().iterator(); i.hasNext();) {
            PubSubEntityElement entityElem = (PubSubEntityElement)i.next();          
            String userID = entityElem.getSubscriberJID().toString();
            user2Status.put(userID,entityElem.getSubscriptionStatus());
        }
        return user2Status;       
    }
    
    private EntityContainer getEntitiesElementResponse() throws StreamException, CollaborationException {
        StreamDataFactory sdf =        _session.getDataFactory();
        Stream connection = _session.getConnection();
        EntityContainer entitiesElem;
        InfoQuery iq = (InfoQuery)sdf.createPacketNode(XMPPSession.IQ_NAME, InfoQuery.class);
        
        PubSubQuery pubsubquery = (PubSubQuery)sdf.createExtensionNode(PubSubQuery.NAME,PubSubQuery.class);
        entitiesElem = pubsubquery.createEntitiesElement(XMPPNewsService.encode
                                        (_jid.toString(), 
                                        getServiceJID().toString()));
        pubsubquery.add(entitiesElem);
        iq.addExtension(pubsubquery);
        iq.setType(InfoQuery.GET);
        iq.setTo(getServiceJID());
        iq.setFrom(((XMPPPrincipal)_session.getPrincipal()).getJID());
        iq.setID(_session.nextID("pubsub"));
        iq = (InfoQuery)_session.sendAndWatch(iq, _session.getShortRequestTimeout());
        
        if ((iq == null) || (iq.getType() != InfoQuery.RESULT)) {
            throw new CollaborationException("Could not get affiliations of the newschannel");
        }
        
        pubsubquery = (PubSubQuery)iq.getExtension(PubSubQuery.NAMESPACE);
        entitiesElem = (EntityContainer)pubsubquery.getFirstElement("entities");
        return entitiesElem;
    }
    
    public void setPrivileges(Map map) throws CollaborationException {
        StreamDataFactory sdf =        _session.getDataFactory();
        Stream connection = _session.getConnection();
        try {
            EntityContainer entitiesElement = getEntitiesElementResponse();
            Map user2Entity = new HashMap();
            for (Iterator i = entitiesElement.listPubSubEntities().iterator(); i.hasNext();) {
                PubSubEntityElement entityElem = (PubSubEntityElement)i.next();
                String userID = entityElem.getSubscriberJID().toString();
                user2Entity.put(userID, entityElem);
            }
            //Map user2SubStatus = listUserSubscriptionStatus();
            InfoQuery iq = (InfoQuery)sdf.createPacketNode(XMPPSession.IQ_NAME,
            InfoQuery.class);
            
            PubSubQuery pubsubquery = (PubSubQuery)sdf.createExtensionNode(PubSubQuery.NAME,PubSubQuery.class);
            EntityContainer entitiesElem = pubsubquery.createEntitiesElement(XMPPNewsService.encode(_jid.toString(), _jid.getDomain()));
            pubsubquery.add(entitiesElem);
            iq.addExtension(pubsubquery);
            
            String userID,affiliation;
            Integer access;
            for (Iterator i = map.keySet().iterator(); i.hasNext();) {
                userID = (String)i.next();
                access = (Integer)map.get(userID);
                affiliation = _session.access2Affiliation(access.intValue());
                PubSubEntityElement entityElement = (PubSubEntityElement)user2Entity.get(userID);
                PubSubEntityElement.SubscriptionStatus subStatus;
                String subscriptionStatus;
                if (entityElement != null) {
                    subStatus = entityElement.getSubscriptionStatus();
                    //PubSubEntityElement.SubscriptionStatus subStatus = (PubSubEntityElement.SubscriptionStatus)user2SubStatus.get(userID);                    
                    if (subStatus != null) {
                        subscriptionStatus = subStatus.toString();
                    } else {
                        subscriptionStatus = PubSubEntityElement.NONE.toString();
                    }
                    /*
                    if (!_newsService.isIncludedInACL(entityElement)) {
                        _newsService.removeDefaultElementExtension(entityElement);
                    }
                     */
                    entityElement.setAttributeValue("affiliation", affiliation);
                    entitiesElem.addPubSubEntity(entityElement);
                } else {
                    subscriptionStatus = PubSubEntityElement.NONE.toString();
                    PubSubEntityElement entityElem = (PubSubEntityElement)pubsubquery.createEntityElement(null,new JID(userID),affiliation,subscriptionStatus);
                    entitiesElem.addPubSubEntity(entityElem);                    
                }                                          
            }
            iq.setType(InfoQuery.SET);
            iq.setTo(getServiceJID());
            iq.setFrom(((XMPPPrincipal)_session.getPrincipal()).getJID());
            iq.setID(_session.nextID("pubsub"));
            connection.send(iq);                
        } catch(StreamException se) {
            throw new CollaborationException(se.toString());
        }
    }

 
    public void messageAdded(XMPPNewsMessage message) {
        if (_listener != null) {
            _listener.onMessageAdded(message);
        }
    }
    
    public void messageRemoved(XMPPNewsMessage message) {
        if (_listener != null) {            
            _listener.onMessageRemoved(message.getMessageId());
        } 
    }
       
    
    private PubSubModel defaultAccess2PubSubModel(int accesslevel)   {
        PubSubModel pubsubModel = new PubSubModel();
        if (accesslevel == Conference.NONE) {
            pubsubModel.publishModel = "publishers";
            pubsubModel.subscriptionModel = "whitelist";            
        } else if (accesslevel >= Conference.MANAGE) {
            pubsubModel.publishModel = "open";
            pubsubModel.subscriptionModel = "open";
        } else if (accesslevel >= Conference.PUBLISH) {
            pubsubModel.publishModel = "open";
            pubsubModel.subscriptionModel = "open";
        } else if (accesslevel >= Conference.LISTEN) {
            pubsubModel.publishModel = "publishers";
            pubsubModel.subscriptionModel = "open";
        }
        return pubsubModel;
    }
    
    private int pubSubModel2DefaultAccess(String publishModel, String subscriptionModel) {
        /**
         * publish_model = "open" --------> Default access = Conference.PUBLISH
         * publish_model = "subscribers" and subscription_model = "open" ---> Default access = Conference.LISTEN
         * publish_model = "publishers" and subscription_model = "open" ----> Default access = Conference.LISTEN
         * publish_model = "publishers" and subscription_model = "authorize" --> Default access = Conference.NONE
         * publish_model = "publishers" and subscription_model = "whitelist" --> Default access = Conference.NONE
         */
        int defaultAccess = Conference.NONE;
        
        if (publishModel.equalsIgnoreCase("open")) {
            defaultAccess = Conference.PUBLISH | Conference.LISTEN;
            return defaultAccess;
        }
        if (publishModel.equalsIgnoreCase("subscribers") && subscriptionModel.equalsIgnoreCase("open")) {
            defaultAccess = Conference.LISTEN; // safe default access
        }
        
        if (publishModel.equalsIgnoreCase("publishers") && subscriptionModel.equalsIgnoreCase("open")) {
            defaultAccess = Conference.LISTEN;
        }
        
        if (publishModel.equalsIgnoreCase("publishers") && subscriptionModel.equalsIgnoreCase("whitelist")) {
            defaultAccess = Conference.NONE;
        }
        
        if (publishModel.equalsIgnoreCase("publishers") && subscriptionModel.equalsIgnoreCase("authorize")) {
            defaultAccess = Conference.NONE;
        }
        return defaultAccess;
    }
    
    public boolean equals(Object o) {
        return (o != null && o instanceof NewsChannel && 
                ((NewsChannel)o).getDestination().equals(getDestination()));
    }
    
    public void setListener(NewsChannelListener listener) throws CollaborationException {
        _listener = listener;
        _session.addNewsChannel(this);
    }
    
    public void getMessages() throws CollaborationException {
/*
        PubSubQuery pubsubquery = (PubSubQuery)sdf.createExtensionNode(PubSubQuery.NAME,PubSubQuery.class);
            EntityContainer entitiesElem = pubsubquery.createPubSubI(XMPPNewsService.encode(_jid.toString(), _jid.getDomain()));
            pubsubquery.add(entitiesElem);
            iq.addExtension(pubsubquery);
*/
        
    }
    
    /*
    private static String encode(JID newsChannelName) {
        // encode it as domain/nodename
       JID newsJID = new JID(null, newsChannelName.getDomain(),
                                newsChannelName.getNode());
       return newsJID.toString();
       
    }
     */


    public String getDisplayName()
    {
        try {
            if (_metaData == null) getMetaData();
            XDataField f = _metaData.getField("pubsub#title");
            if (f != null) {
                _displayName = f.getValue();
            }
        } catch (Exception e) {
            e.printStackTrace(); // DEBUG
        }
        if (_displayName == null || "".equals(_displayName.trim())) {
            _displayName = _jid.getNode();
        }
        return _displayName;
    }
     
    public void setDisplayName(String name) throws ServiceUnavailableException
    {
        try {
            getMetaDataForm();
            XDataField f = _metaDataForm.getField("pubsub#title");
            if (f == null) {
                throw new ServiceUnavailableException("News Channel display name is not configurable");
            } else {
                f.setValue(name);
            }
        } catch (Exception e) {
        }
    }

    private void getMetaData() throws CollaborationException
    {
        InfoQuery iq = (InfoQuery)_session.getDataFactory().createPacketNode(_session.IQ_NAME, InfoQuery.class);
        iq.setType(InfoQuery.GET);
        iq.setTo(getServiceJID());
        iq.setFrom(_session.getCurrentUserJID());
        iq.setID(_session.nextID("meta"));
        StreamDataFactory sdf = _session.getDataFactory();
        DiscoInfoQuery disco = (DiscoInfoQuery)sdf.createElementNode(DiscoInfoQuery.NAME);
        disco.setAttributeValue("node", XMPPNewsService.encode(_jid.toString(),
                                        getServiceJID().toString()));
        iq.add(disco);
        try {
            InfoQuery response = (InfoQuery)_session.sendAndWatch(iq, _session.getShortRequestTimeout());
            if (response == null) {
                throw new CollaborationException("No disco#info response received");
            } else if (response.getType() == Packet.ERROR) {
                throw XMPPSession.getCollaborationException(response.getError(), null);
            } else {
                StreamElement q = response.getFirstElement(DiscoInfoQuery.NAME);
                _metaData = (XDataForm)q.getFirstElement(XDataForm.NAME);
            }
        } catch (StreamException e) {
            throw new CollaborationException(e);
        }
    }


    private void getMetaDataForm() throws CollaborationException
    {
        InfoQuery iq = (InfoQuery)_session.getDataFactory().createPacketNode(_session.IQ_NAME, InfoQuery.class);
        iq.setType(InfoQuery.GET);
        iq.setTo(getServiceJID());
        iq.setFrom(_session.getCurrentUserJID());
        iq.setID(_session.nextID("meta"));

        StreamDataFactory sdf = _session.getDataFactory();
        StreamElement query = sdf.createElementNode(NSI_METADATA);
        query.setAttributeValue("node", XMPPNewsService.encode(_jid.toString(),
                                        getServiceJID().toString()));
        iq.add(query);
        try {
            InfoQuery response = (InfoQuery)_session.sendAndWatch(iq, _session.getRequestTimeout());
            if (response == null) {
                throw new TimeoutException("Timeout while getting the meta data");
            } else if (response.getType() != InfoQuery.RESULT) {
                throw new CollaborationException("Cannot get the meta data");
            }
            StreamElement q = response.getFirstElement(NSI_METADATA);
            _metaDataForm = (XDataForm)q.getFirstElement(XDataForm.NAME);
            _metaDataForm.detach();
        } catch (StreamException e) {
            e.printStackTrace();
            throw new CollaborationException(e);
        }
    }

    private void saveMetaData() throws CollaborationException
    {
        if (_metaDataForm == null) return;

        InfoQuery iq = (InfoQuery)_session.getDataFactory().createPacketNode(_session.IQ_NAME, InfoQuery.class);
        StreamDataFactory sdf = _session.getDataFactory();
        StreamElement query = sdf.createElementNode(NSI_METADATA);
        query.setAttributeValue("node", XMPPNewsService.encode(_jid.toString(),
                                        getServiceJID().toString()));
        _metaDataForm.setType(XDataForm.SUBMIT);
        query.add(_metaDataForm);
        iq.setType(InfoQuery.SET);
        iq.setID(_session.nextID("meta"));
        iq.setFrom(_session.getCurrentUserJID());
        iq.setTo(getServiceJID());
        iq.add(query);
        try {
            InfoQuery response = (InfoQuery)_session.sendAndWatch(iq, _session.getRequestTimeout());
            if (response == null) {
                throw new TimeoutException("Timeout while saving the meta data");
            } else if (response.getType() != InfoQuery.RESULT) {
                throw new CollaborationException("Cannot set the meta data");
            }
        } catch (StreamException e) {
            e.printStackTrace();
            throw new CollaborationException(e);
        }
    }

            

    public void save() throws CollaborationException
    {
        saveMetaData();
        getMetaData();
    }

    /**
     * override the getParticipants method from the XMPPConference
     */
    public Collection getParticipants() throws CollaborationException {
        List list = new java.util.ArrayList();
        EntityContainer entitiesElem;
        try {
            entitiesElem = getEntitiesElementResponse();
        } catch (org.jabberstudio.jso.StreamException se) {
            throw new CollaborationException(se.toString());
        }
        for (Iterator i = entitiesElem.listPubSubEntities().iterator(); i.hasNext();) {
            PubSubEntityElement entityElem = (PubSubEntityElement)i.next();
            if (entityElem.hasSubscriptionStatus(PubSubEntityElement.SUBSCRIBED)) {
                list.add(entityElem.getSubscriberJID().toString());
            }
        }
        return list;
    }

}


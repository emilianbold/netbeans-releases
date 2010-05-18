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

import org.netbeans.lib.collab.CollaborationException;
import org.netbeans.lib.collab.ServiceUnavailableException;
import org.netbeans.lib.collab.NewsChannel;
import org.netbeans.lib.collab.NewsChannelListener;
import org.netbeans.lib.collab.NewsService;
import org.netbeans.lib.collab.TimeoutException;
import org.netbeans.lib.collab.PersonalStoreEntry;
import org.netbeans.lib.collab.PersonalContact;

import org.netbeans.lib.collab.util.StringUtility;

import org.jabberstudio.jso.InfoQuery;
import org.jabberstudio.jso.StreamException;
import org.jabberstudio.jso.StreamElement;
import org.jabberstudio.jso.StreamDataFactory;
import org.jabberstudio.jso.JID;
import org.jabberstudio.jso.Packet;
import org.jabberstudio.jso.NSI;
import org.jabberstudio.jso.x.disco.DiscoItem;
import org.jabberstudio.jso.x.disco.DiscoItemsQuery;
import org.netbeans.lib.collab.xmpp.jso.iface.x.pubsub.PubSubQuery;
import org.netbeans.lib.collab.xmpp.jso.iface.x.pubsub.PubSubItems;
import org.netbeans.lib.collab.xmpp.jso.iface.x.pubsub.PubSubEvent;
import org.netbeans.lib.collab.xmpp.jso.iface.x.pubsub.EntityContainer;
import org.netbeans.lib.collab.xmpp.jso.iface.x.pubsub.PubSubEntityElement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 *
 * 
 */
public class XMPPNewsService implements NewsService {
    
    public final static String NEWS_SERVICEPREFIX = "pubsub.";
    
    private Map _newsChannels = new HashMap();
    XMPPSession __session;
    
    private HashSet _remoteServices = new HashSet();
    
    public static final NSI NEWS_EXTENSION = new NSI("sunnews", "sun:xmpp:news");
    public static final String DEFAULT_ACCESS_ELEMENT = "defaultaccess";
    /** Creates a new instance of XMPPNewsService */
    public XMPPNewsService(XMPPSession session) {
        __session = session;
    }

    protected void processNewsMessage(org.jabberstudio.jso.Message in) 
                 throws CollaborationException 
    {
        String domain = in.getFrom().toString();
        
        // if the from is a roster entry, this is a pep notification
        PersonalStoreEntry contact = __session.getPersonalStoreService().
                getEntry(PersonalStoreEntry.CONTACT, domain);
        if (contact instanceof XMPPPersonalContact) {
            ((XMPPPersonalContact)contact).handlePersonalEvent(in);
            return;
        }
        
        String newsService = getService().toString();
        PubSubEvent event = (PubSubEvent)in.getExtension(PubSubEvent.NAMESPACE);
        if (event != null) {
            
            if (event.hasPubSubItemsElement()) {                
                PubSubItems items = event.getPubSubItemsElement();
                if (items.hasPubSubRetractItem()) {
                    String node = items.getNodeIdentifier();                    
                    String decodedNode = decode(node, newsService);
                                        
                    XMPPNewsChannel nc = (XMPPNewsChannel)_newsChannels.get(decodedNode);
                    if (nc != null) {                                                 
                        nc.messageRemoved(new XMPPNewsMessage(__session,in));
                       
                        /*XMPPNewsMessage msg = 
                            (XMPPNewsMessage)assembleMessages(new XMPPNewsMessage(this,in));
                        nc.messageRemoved(msg);*/
                    }
                } else {
                    String node = items.getNodeIdentifier();                    
                    String decodedNode = decode(node, newsService);
                    String fullyDecodedNode = decodedNode;
                    XMPPNewsChannel nc = (XMPPNewsChannel)_newsChannels.get(fullyDecodedNode);
                    
                    if (nc != null) {
                        
                        PubSubItems itemsElement = event.getPubSubItemsElement();                        

                        for (Iterator iter = itemsElement.listPubSubItems().iterator(); 
                                                                        iter.hasNext();) {
                            Packet message = __session.getDataFactory().createPacketNode(new NSI("message",null),org.jabberstudio.jso.Message.class);
                            PubSubEvent pubsubEvent = (PubSubEvent)__session.getDataFactory().createExtensionNode(PubSubEvent.NAME,PubSubEvent.class);
                            PubSubItems pubSubItems = (PubSubItems)pubsubEvent.createPubSubItemsElement(itemsElement.getNodeIdentifier());
                            pubsubEvent.add(pubSubItems);
                            message.add(pubsubEvent);
                            StreamElement item = (StreamElement)iter.next();
                            pubSubItems.add(item);
                            message.setFrom(new JID(decodedNode));
                            message.setTo(in.getTo());
                            nc.messageAdded(new XMPPNewsMessage(__session, message));
                        }
                        // nc.messageAdded(new XMPPNewsMessage(__session,in));
                        /*XMPPNewsMessage msg = 
                            (XMPPNewsMessage)assembleMessages(new XMPPNewsMessage(this,in));
                        nc.messageAdded(msg);*/
                    } else {
                        XMPPSessionProvider.info("No NewsChannel " + decode(node, newsService));
                    }
                }
            } else if (event.hasDeleteElement()) {
                // node deletion event;
                XMPPSessionProvider.info("Node deletion Event: " + in.toString());
            }
        } else {
            XMPPSessionProvider.info("[XMPPSession#processNewsMessage]: no message event extension");
        }
    }
    
    protected Map getNewsAffiliations() throws CollaborationException {
        Map news2Priv = new HashMap();
        
        //__session.loadJabberServices();        
        EntityContainer affiliationsElem;
        
        affiliationsElem = getAffiliationsElementResponse();
                   
        for (Iterator i = affiliationsElem.listPubSubEntities().iterator(); i.hasNext();) {
            PubSubEntityElement entityElem = (PubSubEntityElement)i.next();
            int access = -1;            
            String affiliation = entityElem.getAffiliation().toString();
            access = __session.affiliation2Access(affiliation);
            String nodeId = decode(entityElem.getNodeIdentifier(),
                                   getService().toString());
                         
            news2Priv.put(nodeId,new Integer(access));
            //_user2SubStatus.put(userID,entityElem.getSubscriptionStatus());
        }
        return news2Priv;
    }
    
    protected void addNewsChannel(XMPPNewsChannel nc) {
       _newsChannels.put(nc.getDestination(), nc);
    }
    
    protected void removeNewsChannel(XMPPNewsChannel nc) {
       _newsChannels.remove(nc.getDestination());
    }
    
    JID getService() throws CollaborationException {
	JID _newsService = __session.getPubSubService();
        if (_newsService == null) {
            __session.waitForServiceInitialization(this);
	    _newsService = __session.getPubSubService();
        }
        if (_newsService == null) throw new ServiceUnavailableException("News Service was not initialized successfully");
        return _newsService;
    }

    void addRemoteService(JID jid) {
	_remoteServices.add(jid);
    }
    Set getRemoteServices() { return _remoteServices; }
    

    //////////////////
    //// static functions
    ////////////////////
    /**
     * encode jid of the form node@domain into domain/node jid form
     */
    public static String encode(String jid, String defaultDomain) {
        JID newsJID = null;
        try {
            newsJID = new JID(jid);
        } catch(Throwable e) {
            return jid; // leave alone - this may be a pep node
        }

        JID encodedNewsChannel = null;
        if (newsJID.getDomain() != null && newsJID.getNode() != null) {
            encodedNewsChannel = new JID(null, newsJID.getDomain(), newsJID.getNode());
        } else {
            encodedNewsChannel = new JID(null, defaultDomain, jid);
        }
        return encodedNewsChannel.toString();       
    }
    /**
     * decode jid of form domain/node into node@domain
     */
    public static String decode(String jid, String defaultDomain) {
        JID newsJID = null;
        try {
            newsJID = new JID(jid);
        } catch(Throwable e) {
            return jid;
        }

        JID decodedNewsChannel = null;
        if (newsJID.getDomain() != null && newsJID.getResource() != null) {
            decodedNewsChannel = new JID(newsJID.getResource(), newsJID.getDomain(), null);
        } else {
            decodedNewsChannel = new JID(jid, defaultDomain, null);
        }
        return decodedNewsChannel.toString();
    }
    
    /////////////////////////////////////////
    //////NewsService Impl
    ////////////////////////////////////////
    public NewsChannel getNewsChannel(String destination, 
                                      NewsChannelListener newsChannelListener) 
                                      throws CollaborationException 
    {   
        String fqDestination = StringUtility.appendDomainToAddress(
                                        destination, getService().toString());
        NewsChannel newsChannelFound = null;
        newsChannelFound = (NewsChannel)_newsChannels.get(fqDestination);         
        if (newsChannelFound != null) {
            if (newsChannelListener != null) {
                newsChannelFound.subscribe(newsChannelListener);
            }
            return newsChannelFound;                   
        } else if (__session.supportsPersonalEvents() &&
                   __session.getApplicationInfo() != null && 
                   __session.getApplicationInfo().hasFeature(destination)) {
            newsChannelFound = new XMPPNewsChannel(this, __session, destination);
            _newsChannels.put(destination, newsChannelFound);
            return newsChannelFound;
        }

        try {
            Collection l = listNewsChannels();
            if (l == null) return null;
            destination = StringUtility.appendDomainToAddress(fqDestination, getService().toString());
            for (Iterator i = l.iterator(); i.hasNext();) {
                NewsChannel nc = (NewsChannel)i.next();
                String fqName = nc.getDestination();
                //String name = StringUtility.getLocalPartFromAddress(fqName);
                if (fqName.equals(destination)) {
                    newsChannelFound = nc;
                    _newsChannels.put(nc.getDestination(), nc);
                    break;
                }
            }
        } catch(Exception e) {
            return null;
        }
                
        
        if (newsChannelFound != null && newsChannelListener != null) {
            newsChannelFound.subscribe(newsChannelListener);
            //newsChannelFound.setListener(newsChannelListener);
        }
        return newsChannelFound;            
    }
    
    public Collection listNewsChannels() throws CollaborationException {
                
        List newsChannelsList = new ArrayList();
        
        /*
        discoQuery = (DiscoItemsQuery)iq.getFirstElement(DiscoItemsQuery.NAME);
         */
        
        //__session.loadJabberServices();
        DiscoItemsQuery discoQuery = __session.sendItemsQuery(getService(), null);
        
        if (discoQuery != null) {
            // List l = dis.listElements(DiscoItem.NAME,ItemNode.class);
            for (Iterator i = discoQuery.listItems().iterator();i.hasNext();) {
                DiscoItem item = (DiscoItem)i.next();
                XMPPSessionProvider.debug("NewsChannel: " + item.getNode());
                String node = decode(item.getNode(), getService().toString());
                NewsChannel nc = new XMPPNewsChannel(this, __session, node);
                newsChannelsList.add(nc);
                /*if (_newsChannels.get(nc.getDestination()) == null) {
                    _newsChannels.put(nc.getDestination(),nc);
                }*/
            }
        }
        return newsChannelsList;
    }
     
    public Collection listNewsChannels(int access) 
                      throws CollaborationException 
    {
        // do a service discovery and get all the newschannels
        StreamDataFactory sdf = __session.getDataFactory();
        //__session.loadJabberServices();
        
        EntityContainer affiliationsElem = getAffiliationsElementResponse();                
        List newsChannelList = new ArrayList();
        String affiliation = __session.access2Affiliation(access);
        //PubSubEntityElement.Affiliation affil = new PubSubEntityElement.Affiliation(affiliation);
        
        for (Iterator i = affiliationsElem.listPubSubEntities().iterator(); i.hasNext();) {
            PubSubEntityElement entityElem = (PubSubEntityElement)i.next();
            //if (PubSubEntityElement.OWNER.equals(affiliation)) {
            if (entityElem.getAffiliation().toString().equals(affiliation)) { 
                // XMPPSessionProvider.debug("NODE-IDENTIFIER " + entityElem.getNodeIdentifier());
                NewsChannel nc = new XMPPNewsChannel(this, __session, 
                        decode(entityElem.getNodeIdentifier(),
                               getService().toString()));
                newsChannelList.add(nc);
                /*if (_newsChannels.get(nc.getDestination()) == null) {
                    _newsChannels.put(nc.getDestination(),nc);
                }*/
            }
        }
        return newsChannelList;
    }
    
     public Collection getSubscribedNewsChannels() 
                      throws CollaborationException 
    {
        // do a service discovery and get all the newschannels
        //__session.loadJabberServices();
        EntityContainer affiliationsElem = getAffiliationsElementResponse(); 
        List newsChannelList = new ArrayList();
        //String affiliation = access2Affiliation(access);
        //PubSubEntityElement.Affiliation affil = new PubSubEntityElement.Affiliation(affiliation);
        
        for (Iterator i = affiliationsElem.listPubSubEntities().iterator(); i.hasNext();) {
            PubSubEntityElement entityElem = (PubSubEntityElement)i.next();
            if (entityElem.hasSubscriptionStatus(PubSubEntityElement.SUBSCRIBED)) {
                XMPPNewsChannel nc = new XMPPNewsChannel(this, __session,
                                         decode(entityElem.getNodeIdentifier(),
                                                getService().toString())); 
                newsChannelList.add(nc);
                if (_newsChannels.get(nc.getDestination()) == null) {           
                    _newsChannels.put(nc.getDestination(),nc);                  
                }                                                               
            }
        }        
        return newsChannelList;
    }
     
    public NewsChannel newNewsChannel(String destination, 
                                      NewsChannelListener newsChannelListener, 
                                      int accesslevel) 
                                      throws CollaborationException 
    {
        XMPPNewsChannel nc = new XMPPNewsChannel(this, __session, 
                                                 destination, 
                                                 newsChannelListener, 
                                                 accesslevel);
        _newsChannels.put(nc.getDestination(),nc);
        return nc;
    }
    
    public boolean isIncludedInACL(StreamElement affiliation) {
        StreamDataFactory sdf = __session.getDataFactory();
        if (affiliation == null) return false;
        StreamElement newsExt = affiliation.getFirstElement(NEWS_EXTENSION);
        if (newsExt != null) {
            StreamElement defaultAccessElem = newsExt.getFirstElement(DEFAULT_ACCESS_ELEMENT);
            return defaultAccessElem == null;
        } else {
            return true;
        }
    }
    
    public void addDefaultElementExtension(StreamElement affiliation) {
        StreamDataFactory sdf = __session.getDataFactory();
        StreamElement newsExt = sdf.createPacketNode(NEWS_EXTENSION);
        StreamElement defaultElement = sdf.createElementNode(new NSI(DEFAULT_ACCESS_ELEMENT, null));
        if (newsExt != null) {
            newsExt.add(defaultElement);
        }
        affiliation.add(newsExt);
    }

    EntityContainer getAffiliationsElementResponse() throws CollaborationException {
        StreamDataFactory sdf = __session.getDataFactory();
        InfoQuery iq = (InfoQuery)sdf.createPacketNode(__session.IQ_NAME,InfoQuery.class);
        PubSubQuery pubsubquery =
        (PubSubQuery)sdf.createExtensionNode(PubSubQuery.NAME,PubSubQuery.class);
        EntityContainer affiliationsElem = pubsubquery.createAffiliationsElement();
        pubsubquery.add(affiliationsElem);
        iq.addExtension(pubsubquery);
        iq.setTo(getService());
        iq.setFrom(((XMPPPrincipal)__session.getPrincipal()).getJID());
        iq.setID(__session.nextID("pubsub"));
        iq.setType(InfoQuery.GET);
        
        try {
            iq = (InfoQuery)__session.sendAndWatch(iq,__session.getRequestTimeout());
        } catch(StreamException se) {
            XMPPSessionProvider.error(se.toString(),se);
        }
                
        if (iq == null) {
            throw new TimeoutException("Request timed out");
        } else if (iq.getType() != InfoQuery.RESULT) {
            throw new CollaborationException("Could not get newschannels from the server");
        }
        pubsubquery = (PubSubQuery)iq.getExtension(PubSubQuery.NAMESPACE);
        affiliationsElem = (EntityContainer)pubsubquery.getFirstElement("affiliations");
        return affiliationsElem;
        
    }
}


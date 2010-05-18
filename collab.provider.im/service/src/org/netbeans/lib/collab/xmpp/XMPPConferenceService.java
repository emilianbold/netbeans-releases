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

import java.util.LinkedList;
import org.jabberstudio.jso.x.xdata.XDataField;
import org.jabberstudio.jso.x.xdata.XDataForm;
import org.netbeans.lib.collab.ConferenceService;
import org.netbeans.lib.collab.ExtendedConferenceService;
import org.netbeans.lib.collab.ConferenceHistory;
import org.netbeans.lib.collab.PersonalStoreService;
import org.netbeans.lib.collab.ConferenceServiceListener;
import org.netbeans.lib.collab.CollaborationException;
import org.netbeans.lib.collab.ServiceUnavailableException;
import org.netbeans.lib.collab.Conference;
import org.netbeans.lib.collab.ConferenceEvent;
import org.netbeans.lib.collab.ConferenceListener;
import org.netbeans.lib.collab.CollaborationSessionFactory;
import org.netbeans.lib.collab.InviteMessage;
import org.netbeans.lib.collab.PersonalStoreEntry;        

import org.netbeans.lib.collab.util.StringUtility;
import org.netbeans.lib.collab.xmpp.*;

import org.jabberstudio.jso.JID;
import org.jabberstudio.jso.Message;
import org.jabberstudio.jso.StreamElement;
import org.jabberstudio.jso.x.disco.DiscoItem;
import org.jabberstudio.jso.x.disco.DiscoItemsQuery;
import org.jabberstudio.jso.*;
import org.jabberstudio.jso.x.disco.DiscoIdentity;
import org.jabberstudio.jso.x.disco.DiscoInfoQuery;



import org.netbeans.lib.collab.xmpp.jso.iface.x.muc.MUCUserQuery;
import org.netbeans.lib.collab.xmpp.jso.iface.x.muc.Invite;
import org.netbeans.lib.collab.xmpp.jso.iface.x.muc.Decline;
import org.netbeans.lib.collab.xmpp.jso.iface.x.event.MessageEventExtension;



import java.util.Hashtable;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * 
 * @author Rahul Shah
 * 
 */
public class XMPPConferenceService implements ConferenceService,ExtendedConferenceService{

    private List _conferenceServiceListeners = Collections.synchronizedList(new ArrayList());
    XMPPSession __session;

    private Set _remoteServices = Collections.synchronizedSet(new HashSet());

    private Hashtable _conferences = new Hashtable();

    public static int HISTORY_TIME = Integer.getInteger("org.netbeans.lib.collab.xmpp.history_queuetime",0).intValue();
    
    private static final String SUN_CONF_SEARCH_EXT = "sun:muc:search:ext";

    /** Creates a new instance of XMPPConferenceService */
    public XMPPConferenceService(XMPPSession session) {
        __session = session;
    }

    protected void processInvite(Message in) throws CollaborationException
    {
        MUCUserQuery user = (MUCUserQuery)in.getExtension(MUCUserQuery.NAMESPACE);
        XMPPSessionProvider.debug("[" + __session.getCurrentUserJID() + "] processInvite: " + user);
        if (user != null) {
            Iterator itr = user.listElements().iterator();
            while (itr.hasNext()) {
                Object o = itr.next();
                XMPPConference c = getConference(in.getFrom().toString());
                if (c == null) {
                    //if the room was a 1-1 chat room and was in the cache then the key will
                    // can be only the node part(thread) of the destination name.
                    c = (XMPPConference)removeConference(in.getFrom().getNode());
                    if (c == null) {
                        c = new XMPPConference(__session,in.getFrom().getNode(), in.getFrom());
                    } else {
                        c.setNode(in.getFrom());
                    }
                    c.useChatStates(in.getFirstElement(XMPPConference.NSI_ACTIVE) != null);
                    //_conferences.put(in.getFrom().toString(),c);
                    addConference(c);
                }
                if (c.isPresentInRoom() &&
                    ((o instanceof Invite) || (o instanceof Decline)))
                {
                    XMPPSessionProvider.debug("[" + __session.getCurrentUserJID() + "] Received a invite reply");
                    //most probably it is a invite reply
                    c.handleInviteReply(in);
                } else if (o instanceof Invite) {
                    Invite invite = (Invite)o;
                    //org.netbeans.lib.collab.InviteMessage m = new XMPPMessage(this,in);
                    InviteMessage m = __session.assembleMessages(new XMPPMessage(__session,in));
                    ((XMPPMessage)m).setAssociatedConferenceJID(in.getFrom().toBareJID());
                    try {
                        m.setOriginator(invite.getFrom().toString());
                        m.setContent(invite.getReason());
                    } catch(CollaborationException e) {
                        e.printStackTrace();
                    }
                    //c.setPrivate(m.getHeader(XMPPConference.PRIVATE));
                    c.setGroupChat(true);
                    _fireConferenceServiceListener(c,m);
                    return;
                }
            }
        }
    }

    protected void processGroupChat(Message in)
                 throws CollaborationException
    {
        if ((in.getExtension(MUCUserQuery.NAMESPACE) != null) &&
            (in.getType() == null))
        {
            processInvite(in);
            return;
        }
        String id = in.getFrom().toBareJID().toString();
        XMPPConference c = getConference(id);
        if (in.hasBody() || in.hasSubject()) {
            c.messageAdded(__session.assembleMessages(new XMPPMessage(__session,in)));
        }
        MessageEventExtension xNode =
            (MessageEventExtension)in.getExtension(MessageEventExtension.NAMESPACE);
        if ((xNode != null) && (xNode.hasMessageID())) {
            c.userStatusChange((c.getParticipant(in.getFrom().getResource())).toString(),
                               XMPPNotificationService.getApiStatus(xNode));
        }
        c.processChatStates(in);
    }

    protected void processChat(Message in)
                 throws CollaborationException
    {
        String thread = getConversationThread(in);
        XMPPConference c = getConference(thread);
        XMPPMessage message = __session.assembleMessages(new XMPPMessage(__session,in));

        //if the message is a leave message the send leave event
        //otherwise call message added.
        /*String leftMsg = message.getHeader(XMPPConference.LEFT_MESSAGE);
        if ((leftMsg != null) && (new Boolean(leftMsg)).booleanValue()) {
            if (c == null) return;
            c.userStatusChange(message.getOriginator(),ConferenceEvent.ETYPE_USER_LEFT);
            return;
        }*/
        StreamElement element = in.getFirstElement(XMPPConference.NSI_GONE);
        if (element != null) {
            if (c == null) return;
            c.userStatusChange(message.getOriginator(), ConferenceEvent.ETYPE_USER_LEFT);
            return;
        }
        //if the room is not present and there is no data in the message then
        // the message should not be processed.
        if ((c == null) && (!in.hasBody())) return;
        if (c == null) {
            c = new XMPPConference(__session,thread,null);
            addConference(c);
            InviteMessage m = __session.assembleMessages(new XMPPMessage(__session,in));
            //m.setHeader(XMPPMessage.ID_ORIGINATOR,m.getOriginator());
            c.addParticipant(in.getFrom());
            c.addParticipant(in.getTo());            
            c.useThreads(in.getThread() != null && !"".equals(in.getThread()));
            c.useChatStates(in.getFirstElement(XMPPConference.NSI_ACTIVE) != null);            
            c.processChatStates(in);
            _fireConferenceServiceListener(c,m);
        } else {
	    if(in.getFirstElement(XMPPConference.NSI_ACTIVE) != null)
            	c.useChatStates(true);
	    c.useThreads(in.getThread() != null && !"".equals(in.getThread()));
            c.processChatStates(in);
            if (in.hasBody()) {
                c.messageAdded(message);
            }            
	    
        }
        
        MessageEventExtension xNode =
            (MessageEventExtension)in.getExtension(MessageEventExtension.NAMESPACE);
        if ((xNode != null) && (xNode.hasMessageID())) {
            c.userStatusChange(in.getFrom().toString(),
                               XMPPNotificationService.getApiStatus(xNode));
        }        
    }

    protected JID getService() throws CollaborationException {
	JID _confService = __session.getMUCService();
        if (_confService == null) {
            __session.waitForServiceInitialization(this);
	    _confService = __session.getMUCService();
        }
        if (_confService == null) throw new ServiceUnavailableException("[" + __session.getCurrentUserJID() + "] Conference service is not initialized");
        return _confService;
    }

    void addRemoteService(JID jid) {
	_remoteServices.add(jid);
    }
    Set getRemoteServices() { return _remoteServices; }

    public void addConference(XMPPConference c) {
        _conferences.put(c.getDestination(), c);
    }
     
    public XMPPConference removeConference(String str) {
        XMPPConference ret = null;
        if (str == null) return null;
	XMPPConference c = getConference(str);
	if (c != null) {
	    if ((ret =(XMPPConference)_conferences.remove(c.getDestination())) == null) {
		ret = (XMPPConference)_conferences.remove(c.getName());
	    }
	}
        return ret;
    }

    public XMPPConference getConference(String str) {
        XMPPConference c = null;
        try{
            c = getConference(str,null);
        }catch(Exception e){
            
        }
        finally{
            return c;
        }
    }
    
    public XMPPConference getConference(String str,JID component){

       XMPPConference c = (XMPPConference)_conferences.get(str);
	if (c == null) {
	    // check if jid normalization changed name and this is called
	    // with unnormalized name, so renormalize and try the 
	    // normalized version
	    try { 
		JID service = (component == null) ? getService() : component;
		String name = 
		    StringUtility.appendDomainToAddress(str, 
							service.toString());
		JID jid = new JID(name);
		c = (XMPPConference)_conferences.get(jid.toString());
		if (c == null) {
		    c = (XMPPConference)_conferences.get(jid.getNode());
		}
	    } catch (Exception e) {
		// not a valid jid.
		//e.printStackTrace();  // debug
	    }
	}
	return c; 
    }

    private void _fireConferenceServiceListener(Conference c, InviteMessage m){
        __session.addWorkerRunnable(new InviteNotifier(c,m));
    }

    public void addConferenceServiceListener(ConferenceServiceListener listener) {
        if (!_conferenceServiceListeners.contains(listener))
            _conferenceServiceListeners.add(listener);
    }
   
    public void removeConferenceServiceListener(ConferenceServiceListener listener) {
        _conferenceServiceListeners.remove(listener);
    }

    ///////////////////////////////////////////////
    ////ConferenceService impl
    ///////////////////////////////////////////////
    /**
     * use MUC spec
     */
    public Conference getPublicConference(String str) throws CollaborationException
    {
        return getPublicConference(str,this.getService().getDomain());
    }
    
    public Conference getPublicConference(String str, String component) throws CollaborationException{
        if(str == null || str.equals("")) throw new CollaborationException("invalid conference name");
        if(StringUtility.hasDomain(str)){
            JID tmp = new JID(str);
            str = tmp.getNode();
            if(component != null && !component.equalsIgnoreCase(tmp.getDomain()))
                throw new CollaborationException("improper inputs conference name " + str + " component  " + component);
            else
                component = tmp.getDomain();
        }
        
        Conference c = getConference(str,null != component ? new JID(component) : getService());
        Conference[] a = null;
        {
            Set<Conference> confs = searchConference(PersonalStoreService.SEARCHTYPE_EQUALS, 
                    str,component);
            
            if (null == confs) return null;
            a = (Conference[])confs.toArray(new Conference[confs.size()]);

        }
        JID confJid = new JID(str,null != component ? component : getService().getDomain(),"");
        XMPPSessionProvider.debug(" Searching for pattern " + str +", confJid : " + confJid);
        if (a != null) {
            for(int i = 0;i < a.length;i++){
                if(((XMPPConference)a[i]).getJID().toBareJID().equals(confJid)){
                    if(c != null) {
                        if(((XMPPConference)a[i]).getJID().equals(((XMPPConference)c).getJID()))
                            return c;
                    }else {
                        return a[i];
                    }
                }
            }
        }
        //there is no room with this name
        if (c != null) {
            //a stale room exists in cache so remove it
            removeConference(c.getDestination());
        }
        return null;
    }

    public Conference[] listConference(int access) throws CollaborationException {
            return listConference(access,null);
    }
    
    public Conference[] listConference(int access,String component) throws CollaborationException {
        Set<Conference> retval = null;
	if (access >= Conference.MANAGE) {
            retval = searchConference(PersonalStoreService.SEARCHTYPE_EQUALS, "(affiliation=owner)*",component);
	} else  if (access >= Conference.PUBLISH) {
	    retval = searchConference(PersonalStoreService.SEARCHTYPE_EQUALS, "(affiliation=member)*",component); 
	} else  if (access >= Conference.LISTEN) {
	    retval = searchConference(PersonalStoreService.SEARCHTYPE_EQUALS, "(role=visitor)*",component); 
	} else  if (access >= Conference.NONE) {
	    retval = searchConference(PersonalStoreService.SEARCHTYPE_EQUALS, "(affiliation=outcast)*",component); 
	}
        
        if (null != retval){
            return retval.toArray(new Conference[retval.size()]);
        }
	return null;
    }

    private Hashtable<JID, Boolean> extSearchSupport = new Hashtable<JID, Boolean>();
    void updateExtendedSearchSupport(JID jid, DiscoInfoQuery diq) {
        Boolean val = Boolean.FALSE;
        if (diq.getFeatures().contains(SUN_CONF_SEARCH_EXT)){
            val = Boolean.TRUE;
        }
        extSearchSupport.put(jid, val);
    }
    
    private boolean supportsExtendedSearch(JID jid){
        Boolean val = extSearchSupport.get(jid);
        
        if (null != val){
            return val;
        }
        
        try{
            updateExtendedSearchSupport(jid, __session.sendInfoQuery(jid, null));
            return extSearchSupport.get(jid);
        } catch(Exception ex){} // ignore for now
        return false;
    }
    
    public Conference[] searchConferences(int access, int searchType, 
            String filter, String component) throws CollaborationException{
        
        Set<Conference> vals = null;
        if (null != component) {
            JID componentJid = null;
            try {
                componentJid = new JID("", component, "");
            } catch(JIDFormatException jfEx){
                throw new CollaborationException("Invalid component specified : " + component);
            }
            vals = searchConferencesImpl(access, searchType, filter, componentJid);
        }
        else{
            vals = new HashSet<Conference>();
            // Search in all conference components.
            Set<String> components = getMUCProviders(__session._client.getDomainName());
            if (null == components) 
                throw new CollaborationException("Unable to list components for user domain : " + 
                        __session._client.getDomainName());
            for (String comp : components){
                vals.addAll(searchConferencesImpl(access, searchType, filter, new JID("", comp, "")));
            }
        }
        if (null == vals) throw new CollaborationException("search failed");
        return (Conference[]) vals.toArray(new Conference[vals.size()]);
    }
    
    private Set<Conference> searchConferencesImpl(int access, int searchType, 
            String filter, JID componentJid) throws CollaborationException{
        // Does this component support extended search.
        boolean supportsExt = supportsExtendedSearch(componentJid);
        
        if (supportsExt){
            InfoQuery iq = (InfoQuery)__session._sdf.createPacketNode(__session.IQ_NAME, 
                    InfoQuery.class);
            iq.setType(InfoQuery.GET);
            iq.setFrom(__session._client.getJID());
            iq.setTo(componentJid);
            iq.setID(__session.nextID("query"));
            NSI searchNsi = __session._sdf.createNSI("query", SUN_CONF_SEARCH_EXT);
            iq.add(__session._sdf.createElementNode(searchNsi));
            
            try{
                Packet packet = __session.sendAndWatch(iq);
                StreamElement elem = null;
                XDataForm form = null;
                if (null == packet || 
                        InfoQuery.RESULT != ((InfoQuery)packet).getType() ||
                        null == (elem = packet.getFirstElement(searchNsi)) ||
                        null == (form = (XDataForm)elem.getFirstElement(XDataForm.NAME))){
                    throw new CollaborationException("Unable to perform search");
                }
                form = (XDataForm)form.copy();
                // We always expect these ...
                if (0 != access){
                    XDataField f = form.getField("access");
                    String val = null;
                    // Copy pasted this ... might want to validate this
                    if (access >= Conference.MANAGE) {
                        val = "affiliation>member";
                    } else  if (access >= Conference.PUBLISH) {
                        val = "affiliation>visitor";
                    } else  if (access >= Conference.LISTEN) {
                        val = "affiliation>outcast";
                    } else  if (access >= Conference.NONE) {
                        // same as not specifying ?
                        val = "";
                    }
                    f.setValue(val);
                }

                if (null != filter){
                    XDataField f = form.getField("filter");
                    f.setValue(XMPPPersonalStoreService.getWildCard(searchType, filter));
                }

                XDataField f = form.getField("result");
                f.setValue("config");
                
                iq = (InfoQuery)__session._sdf.createPacketNode(__session.IQ_NAME, 
                        InfoQuery.class);
                iq.setType(InfoQuery.SET);
                iq.setFrom(__session._client.getJID());
                iq.setTo(componentJid);
                iq.setID(__session.nextID("query"));
                elem = __session._sdf.createElementNode(searchNsi);
                elem.add(form);
                iq.add(elem);

                packet = __session.sendAndWatch(iq, __session.getRequestTimeout());
                if (null == packet || InfoQuery.RESULT != packet.getType() ||
                        null == (elem = packet.getFirstElement(searchNsi))) {
                    throw new CollaborationException("Error while doing the search");
                }

                List list = elem.listElements(DiscoInfoQuery.NAME);
                Iterator iter = list.iterator();
                Set<Conference> retval = new HashSet();
                while (iter.hasNext()){
                    DiscoInfoQuery dis = (DiscoInfoQuery)iter.next();
                    String jidstr = dis.getAttributeValue("jid");
                    if (null == jidstr){
                        XMPPSessionProvider.debug("Invalid conference search result : " + dis);
                        continue;
                    }
                    XMPPConference conf = getConference(jidstr, componentJid);
                    if (null == conf){
                        conf = new XMPPConference(__session,
                            null, new JID(jidstr)); 
                    }
                    // always doing this since this will be updated info
                    conf.updateConference(dis);
                    retval.add(conf);
                }
                return retval;
            }catch (Exception ex){
                throw new CollaborationException("Unable to perform search", ex);
            }
        }
        else {
            // For now, we will continue with our earlier proprietory behavior - fix this later.
            String sfilter = "";
            if (0 != access){
                String val = "";
                // Copy pasted this ... might want to validate this
                if (access >= Conference.MANAGE) {
                    val = "affiliation>member";
                } else  if (access >= Conference.PUBLISH) {
                    val = "affiliation>visitor";
                } else  if (access >= Conference.LISTEN) {
                    val = "affiliation>outcast";
                } else  if (access >= Conference.NONE) {
                    // same as not specifying ?
                    val = "";
                }
                sfilter += "(" + val + ")";
            }
            if (null == filter) filter = "*";
            sfilter += filter;
            return searchConference(PersonalStoreService.SEARCHTYPE_EQUALS, sfilter, 
                    componentJid.toString());
        }
    }
    
    private Set<Conference> searchConference(int searchType, String pattern,String service) throws CollaborationException {
         int i = 0;
         List l = searchConferenceItems(searchType, pattern,service);
         if (l == null || l.size() == 0 ) {
             return null;
         } else {
             
             //XMPPConference[] result =  new XMPPConference[l.size()];
             Set<Conference> retval = new HashSet<Conference>();
             for (Iterator itr = l.iterator(); itr.hasNext(); ) {
                 DiscoItem item = (DiscoItem)itr.next();
                 XMPPSessionProvider.debug(" conference recieved " + item.getJID().toString());
                 XMPPConference conf = this.getConference(item.getJID().toString());
                 if (null == conf) {
                     conf = new XMPPConference(__session,
                             item.getName(),
                             item.getJID());
                 }
                 retval.add(conf);
                 i++;
             }
             return retval;
         }
     }
    
    private List searchConferenceItems(int searchType, String pattern,String service) throws CollaborationException {
        String wildcard = XMPPPersonalStoreService.getWildCard(searchType, pattern);
        
        XMPPSessionProvider.debug("[XMPPConferenceService] search for wildCard " + wildcard + " in component " + service);
        
        DiscoItemsQuery sq  = null;
        sq = (service == null) ?__session.sendItemsQuery(getService(),wildcard): this.__session.sendItemsQuery(new JID(service),wildcard);
        List p = sq.listItems();
        if(service == null){    //This is to support old behaviour.
            for (Iterator iter = this.getRemoteServices().iterator();
            iter.hasNext(); ) {
                JID serviceJID = (JID)iter.next();
                try {
                    sq = __session.sendItemsQuery(serviceJID, wildcard);
                    p.addAll(sq.listItems());
                } catch (CollaborationException e) {
                }
            }
        }
        
        return p;
     }
  

    public Conference joinPublicConference(String conf,ConferenceListener listener) throws CollaborationException {
        return joinPublicConference(null, null, conf, listener,null);
    }
    public Conference joinPublicConference(String nick,ConferenceHistory history,String conf,ConferenceListener listener) throws CollaborationException {
         return joinPublicConference(nick,history,conf,listener,null);
    }
    public Conference joinPublicConference(String conf,ConferenceListener listener,String component) throws CollaborationException {
        return joinPublicConference(null, null, conf, listener,component);
    }
    public Conference joinPublicConference(String nick,
                                           ConferenceHistory history,
                                           String conf,
                                           ConferenceListener listener,
                                           String component)
                                           throws CollaborationException
    {
        Conference c = getPublicConference(conf,component);
        if (c == null)
            throw new CollaborationException("[" + __session.getCurrentUserJID() + "] Conference room not found");
        c.join(nick, history, listener);
        return c;
    }
    
   public Conference setupConference(ConferenceListener conferenceListener, int access)
                      throws CollaborationException
    {
        //XMPPSessionProvider.debug("Adding " + c.getNode().toString() + " to hashtable");
        return setupConference(conferenceListener,access,this.getService().getDomain());
    }
   public Conference setupConference(ConferenceListener conferenceListener, int access,String component) throws CollaborationException
   {
       component = getCorrectComponent(component);
       XMPPConference c = new XMPPConference(__session,conferenceListener,access,new JID(component));
       addConference(c);
       return c;
   }

    public Conference setupPublicConference(String str, ConferenceListener conferenceListener, int param) throws CollaborationException {
        return (XMPPConference)setupPublicConference(str,conferenceListener,param,getService().getDomain());
    }
    public Conference setupPublicConference(String destination, ConferenceListener listener, int accessLevel, String component) throws CollaborationException {
        component = getCorrectComponent(component);
        XMPPConference c = new XMPPConference(__session,destination,listener,accessLevel,new JID(component));
        addConference(c);
        return c;
    }
    
    public void initialize(ConferenceServiceListener listener)
                                                throws CollaborationException {
        addConferenceServiceListener(listener);
    }
    /*
     *  Returns the Set(servicesSet) of Strings , where each String is MUC Provider JID.
     *  basically jid.toString();
     *
     */ 
    public Set<String> getMUCProviders(String domain) throws CollaborationException {
        Set serverSet = new HashSet();
        Set<String> servicesSet = new HashSet<String>();
        JID searchJID = null;
        boolean searchAll = false;
        
        if(domain != null){
            if(!JID.isValidDomain(domain)) throw new CollaborationException("malformed domain id");
            searchJID = new JID(domain);
        }else {
            searchAll = true;
            searchJID = __session._server;
        }
        DiscoItemsQuery dq = this.__session.sendItemsQuery(searchJID,null);
        Iterator itr = dq.listItems().iterator();
        while(itr.hasNext()){
            JID jid = ((DiscoItem)itr.next()).getJID();
            DiscoInfoQuery infoquery = null;
	    try{
	    	infoquery = __session.sendInfoQuery(jid,null);
	    }catch(CollaborationException cex){
		    XMPPSessionProvider.error("[XMPPConferenceService:getMUCProviders] cant find identities for component " + jid + " error: " + cex.getMessage());
		    continue;
	    }
            for(Iterator iter = infoquery.listIdentities().iterator();iter.hasNext(); ) {
                DiscoIdentity discoIdentity = (DiscoIdentity)iter.next();
                String category = discoIdentity.getCategory();
                if("server".equals(category) && searchAll){
                    serverSet.add(jid);
                }else if(PersonalStoreEntry.CONFERENCE.equals(category)){
                    servicesSet.add(jid.toString()); //Need to return List of strings to user
                    //As the MUC services aren't from local services so add them to remoteServices
                    if(!jid.equals(__session.getMUCService()))
                        this._remoteServices.add(jid);
                }
            }
        }
        
        if (searchAll){
            servicesSet.addAll(getRemoteServices(serverSet));
        }
        
        return servicesSet;
    }
    /* 
     * Returns the list of services for the serverSet provided.
     * Also adds the new servers discovered , to XMPPSession.addRemoteServices()
     * and adds the new discovered list to remoteServices list maintained locally.
     */
    private Set<String> getRemoteServices(Set serverSet) throws CollaborationException{
        Set<String> providerSet = new HashSet<String>();
        try{
        if(null == serverSet || serverSet.isEmpty()) return providerSet;
        else this.__session.addRemoteServices(serverSet);
        
        Iterator listIterator = serverSet.iterator();
        
        while(listIterator.hasNext()){
            DiscoItemsQuery dq = this.__session.sendItemsQuery((JID)listIterator.next(),null);
            Iterator itr = dq.listItems().iterator();
            while(itr.hasNext()){
                JID componentJID = ((DiscoItem)itr.next()).getJID();
                DiscoInfoQuery infoquery = __session.sendInfoQuery(componentJID,null);
                for(Iterator iter = infoquery.listIdentities().iterator();iter.hasNext(); ) {
                    DiscoIdentity discoIdentity = (DiscoIdentity)iter.next();
                    String category = discoIdentity.getCategory();

                    if(PersonalStoreEntry.CONFERENCE.equals(category)){
                        XMPPSessionProvider.debug(" discovered conferenceService for  " + componentJID );
                        providerSet.add(componentJID.toString()); //Need to return List of strings to user
                        this._remoteServices.add(componentJID);
                    }else
                        continue;
                }
            }
        }
        
        }catch(CollaborationException ce){
            throw ce;
        }
        return providerSet;
    }
    
   
    public void setDefaultMUCProvider(String component) throws CollaborationException{
        component = getCorrectComponent(component);
        //return immediatly if component is default MUCService
        if(component.equalsIgnoreCase(getService().getDomain())) return; 
        
        Set l = getMUCProviders(__session._client.getDomainName());
        Iterator itr = l.iterator();
        while(itr.hasNext()){
            if(((String)itr.next()).equalsIgnoreCase(component)){
               this.__session.setMUCService(new JID(component));
               return;
            }
        }
       throw new CollaborationException("component could not be reached");
   }
    
    
    private String getCorrectComponent(String component) throws CollaborationException{
        if(component == null)
             component = getService().getDomain();
         if(!JID.isValidDomain(component)) throw new CollaborationException("malformed component name");
        
        return new JID(component).getDomain();
    }
    /////////////////////////////
    ///Inner classes
    //////////////////////

    class InviteNotifier implements Runnable {
        Conference c;
        InviteMessage m;
        InviteNotifier(Conference conference, InviteMessage message) {
            this.m = message;
            this.c = conference;
        }

        public void run() {
            synchronized(_conferenceServiceListeners) {
                for(Iterator itr = _conferenceServiceListeners.iterator(); itr.hasNext();) {
                    try {
                        ConferenceServiceListener l = (ConferenceServiceListener)itr.next();
                        if (l == null) continue;
                        l.onInvite(c,m);
                    } catch(Exception e) {
                        XMPPSessionProvider.error(e.toString(),e);
                    }
                }
            }
        }
    }
    
    
    public String getConversationThread(org.jabberstudio.jso.Message in) throws CollaborationException {
        String thread = in.getThread();
        //String id = StringUtility.appendDomainToAddress(thread,getService().toString());
        if ((thread == null) || (thread.equals(""))) {
            String confName = null;
            XMPPConference conf = null;
            String sender = StringUtility.removeResource(in.getFrom().toString());

            // check if the sender is already involved in a private chat
            for (Enumeration en = _conferences.keys(); en.hasMoreElements();) {
                confName = (String)en.nextElement();
                conf = (XMPPConference)_conferences.get(confName);
                if (conf.isOne2OnePrivateChat() && conf.isParticipant(sender)) {
                    return conf.getName();
                }
            }
	    String from = in.getFrom().getNode();
	    String to = in.getTo().getNode();
	    //the thread name in sorted order will help in archiving on the server
            thread = (from.compareTo(to) > 0) ? from + "-" + to: to + "-" + from;
        }
        return thread;
    }
}



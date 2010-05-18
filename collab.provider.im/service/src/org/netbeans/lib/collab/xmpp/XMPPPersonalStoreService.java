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

import org.netbeans.lib.collab.PersonalStoreService;
import org.netbeans.lib.collab.PersonalStoreServiceListener;
import org.netbeans.lib.collab.PersonalStoreEvent;
import org.netbeans.lib.collab.PersonalStoreEntry;
import org.netbeans.lib.collab.PersonalContact;
import org.netbeans.lib.collab.PersonalStoreFolder;
import org.netbeans.lib.collab.PersonalGateway;
import org.netbeans.lib.collab.PersonalProfile;
import org.netbeans.lib.collab.PersonalConference;
import org.netbeans.lib.collab.Conference;
import org.netbeans.lib.collab.CollaborationPrincipal;
import org.netbeans.lib.collab.CollaborationException;
import org.netbeans.lib.collab.ServiceUnavailableException;
import org.netbeans.lib.collab.util.*;

import org.jabberstudio.jso.StreamDataFactory;
import org.jabberstudio.jso.InfoQuery;
import org.jabberstudio.jso.NSI;
import org.jabberstudio.jso.JID;
import org.jabberstudio.jso.StreamException;
import org.jabberstudio.jso.StreamElement;
import org.jabberstudio.jso.Extension;
import org.jabberstudio.jso.x.core.RosterQuery;
import org.jabberstudio.jso.x.core.RosterItem;
import org.jabberstudio.jso.x.disco.DiscoItem;
import org.jabberstudio.jso.x.disco.DiscoItemsQuery;
import org.jabberstudio.jso.x.disco.DiscoIdentity;
import org.jabberstudio.jso.x.disco.DiscoInfoQuery;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * 
 * @author Vijayakumar Palaniappan
 * 
 */
public class XMPPPersonalStoreService implements PersonalStoreService{

    private List _personalStoreServiceListeners = Collections.synchronizedList(new ArrayList());
    XMPPSession __session;

    private XMPPPersonalProfile _personalProfile = null;
    private Hashtable _profiles = new Hashtable();

    private HashMap _folders = new HashMap();

    //Important note: The key in this map should be encoded string
    private Map _subscribedConferenceItems = new HashMap();
    private Map _watchers = new HashMap();
    private Map _personalGroups = new HashMap();

    private HashSet _remoteServices = new HashSet();


    private boolean _asyncRosterProcessed;
    private Exception _asyncRosterException;

    private boolean _asyncBookmarkProcessed;
    private Exception _asyncBookmarkException;
    private Object _bookmarkLock = new Object();

    private boolean _asyncLDAPGroupProcessed;
    private Exception _asyncLDAPGroupException;

    /** Creates a new instance of XMPPPersonalStoreService */
    public XMPPPersonalStoreService(XMPPSession session) {
        __session = session;
    }


    ///////////////////////
    ////////PersonalStore Service Impl
    ///////////
    public PersonalStoreEntry getEntry(String entryType, String entryId)
                              throws CollaborationException
    {
        if (entryId == null) return null;
        if ((PersonalStoreEntry.CONTACT_FOLDER.equals(entryType)) ||
            (PersonalStoreEntry.FOLDER.equals(entryType)))
        {
            return (PersonalStoreEntry)_getFolders().get(entryId);
        } else if ((PersonalStoreEntry.CONTACT.equals(entryType)) ||
                   (PersonalStoreEntry.GATEWAY.equals(entryType)))
        {
            for (Iterator i = _getFolders().values().iterator(); i.hasNext();) {
                PersonalStoreFolder f = (PersonalStoreFolder)i.next();
                PersonalStoreEntry e = f.getEntry(entryId);
                if ((e != null) && (e.getType().equals(entryType))) return e;
            }
            return null;
        } else if ((PersonalStoreEntry.GROUP.equals(entryType))) {
            for (Iterator i = _getFolders().values().iterator(); i.hasNext();) {
                PersonalStoreFolder f = (PersonalStoreFolder)i.next();
                PersonalStoreEntry e = f.getEntry((org.netbeans.lib.collab.xmpp.JIDUtil.encodedJID(entryId)).toString());
                if ((e != null) && (e.getType().equals(entryType))) return e;
                // work around for now for our client
                // try not to enode and also decode to see if there is entry
                e = f.getEntry(entryId);
                if ((e != null) && (e.getType().equals(entryType))) return e;
                e = f.getEntry((org.netbeans.lib.collab.xmpp.JIDUtil.decodedJID(entryId)));
                if ((e != null) && (e.getType().equals(entryType))) return e;
            }
            return null;
        } else if (PersonalStoreEntry.CONFERENCE.equals(entryType)) {
            return (PersonalConference)_getSubscribedConferenceItems().get((new JID(entryId)).toString());
        }
        return null;
    }

    public PersonalStoreEntry getEntry(CollaborationPrincipal collaborationPrincipal,
                                       String entryType,
                                       String entryId)
                                       throws CollaborationException
    {
        if (collaborationPrincipal == null) {
            return getEntry(entryType, entryId);
        } else {
            throw new CollaborationException("Not Implemented");
        }
    }

    public Collection getEntries(String entryType) throws CollaborationException {
        if ((PersonalStoreEntry.CONTACT_FOLDER.equals(entryType)) ||
            (PersonalStoreEntry.FOLDER.equals(entryType)))
        {
            return _getFolders().values();
        } else if (PersonalStoreEntry.CONTACT.equals(entryType)) {
            List contacts = new ArrayList();
            for (Iterator i = _getFolders().values().iterator(); i.hasNext();) {
                PersonalStoreFolder f = (PersonalStoreFolder)i.next();
                contacts.addAll(f.getEntries(PersonalStoreEntry.CONTACT));
            }
            return contacts;
        } else if (PersonalStoreEntry.GROUP.equals(entryType)) {
            List groups = new ArrayList();
            for (Iterator i = _getFolders().values().iterator(); i.hasNext();) {
                PersonalStoreFolder f = (PersonalStoreFolder)i.next();
                groups.addAll(f.getEntries(PersonalStoreEntry.GROUP));
            }
            return groups;
        } else if (PersonalStoreEntry.CONFERENCE.equals(entryType)) {
            return _getSubscribedConferenceItems().values();
        } else if (PersonalStoreEntry.GATEWAY.equals(entryType)){
            List gateways = new ArrayList();
            for (Iterator i = _getFolders().values().iterator(); i.hasNext();) {
                PersonalStoreFolder f = (PersonalStoreFolder)i.next();
                gateways.addAll(f.getEntries(PersonalStoreEntry.GATEWAY));
            }
            return gateways;
            // return _gateWays.values();
        } else if (PersonalStoreEntry.WATCHER.equals(entryType)) {
            return _watchers.values();
        } else {
            throw new CollaborationException("Not Implemented");
        }
    }

    public Collection getFolders(String entryType) throws CollaborationException {
        return _getFolders().values();
    }

    /**
     * enumerate roster and build folder->item relationship
     */
    public Collection getFolders(CollaborationPrincipal collaborationPrincipal,
                                 String entryType)
                                 throws CollaborationException
    {
        if (collaborationPrincipal == null) {
            return getFolders(entryType);
        } else {
            throw new CollaborationException("Not Implemented");
        }
    }

    public PersonalStoreEntry createEntry(String entryType, String displayName)
                              throws org.netbeans.lib.collab.CollaborationException
    {
        return createEntry(entryType, displayName, null);
    }

    public PersonalStoreEntry createEntry(CollaborationPrincipal collaborationPrincipal,
                                          String entryType,
                                          String displayName)
                                          throws CollaborationException
    {
        if (collaborationPrincipal == null) {
            return createEntry(entryType, displayName);
        } else {
            // Resource is required for group's , while should be removed for other uid's
             return createEntry(entryType, displayName,
                    (collaborationPrincipal instanceof XMPPGroup) ?
                        collaborationPrincipal.getUID() :
                        JIDUtil.getBareJIDString(collaborationPrincipal.getUID()));
       }
    }

    private PersonalStoreEntry createEntry(String entryType,
                                           String displayName,
                                           String jid)
                                           throws CollaborationException
    {
        PersonalStoreEntry e = getEntry(entryType, jid);
        if ( e != null) return e;
        if (PersonalStoreEntry.CONTACT.equals(entryType)) {
            return new XMPPPersonalContact(__session, displayName, jid);
        } else if (PersonalStoreEntry.GROUP.equals(entryType)) {
            if (jid == null) return new XMPPPersonalGroup(__session, displayName, jid);
            else return new XMPPPersonalGroup(__session, displayName, org.netbeans.lib.collab.xmpp.JIDUtil.encodedJID(jid).toString());
        } else if ((PersonalStoreEntry.CONTACT_FOLDER.equals(entryType)) ||
                   (PersonalStoreEntry.FOLDER.equals(entryType))) {
            Map map = _getFolders();
            if (map.get(displayName) != null) {
                throw new CollaborationException("Folder name " + displayName + " already present!");
            }
            XMPPPersonalFolder folder = new XMPPPersonalFolder(__session, displayName);
            map.put(displayName, folder);
            return(folder);
        } else if (PersonalStoreEntry.CONFERENCE.equals(entryType)) {
            return new XMPPPersonalConference(__session, displayName, jid);
        } else if (entryType.equals(PersonalStoreEntry.GATEWAY)) {
            // return new XMPPPersonalGateway(this, displayName, jid);
            throw new CollaborationException("PersonalStoreEntry of type PersonalStoreEntry.GATEWAY  cannot be created");
        } else if (entryType.equals(PersonalStoreEntry.WATCHER)) {
            throw new CollaborationException("PersonalStoreEntry of type PersonalStoreEntry.WATCHER  cannot be created");
        } else {
            throw new CollaborationException("Unsupported entry type: " + entryType);
        }
    }

    /**
     * search directory for entry
     * @return a array of principals from the search result
     */
    public CollaborationPrincipal[] searchPrincipals(int searchType,
                                                     String pattern)
        throws CollaborationException
    {
        if (searchType == SEARCHTYPE_EQUALS) {
            return searchPrincipals(searchType, pattern, UID_ATTRIBUTE);
        }
        return searchPrincipals(searchType, pattern, NAME_ATTRIBUTE);
    }

    /**
     * search directory for entry
     * @return a array of principals from the search result
     */
    public CollaborationPrincipal[] searchPrincipals(int searchType,
                                                     String pattern,
                                                     int attribute)
                                    throws CollaborationException
    {
        if (((attribute == UID_ATTRIBUTE) ||
             (attribute == MAIL_ATTRIBUTE)) &&
            (searchType != SEARCHTYPE_EQUALS))
        {
            throw new CollaborationException("Not Supported");
        }
        String wildcard = pattern;
        InfoQuery iqSearch;
        StreamElement sq;
        ArrayList p = new ArrayList();
        String domain = null;
        String searchField;
        JID judJID = getService();
        switch (attribute) {
            case UID_ATTRIBUTE:
                searchField = "nick";
                domain = StringUtility.getDomainFromAddress(pattern, null);
                judJID = getService(domain);

                // there is no advertized search service for this domain
                if (judJID == null) {
                    throw new ServiceUnavailableException("No search service for " + domain);
                }

                break;
            case MAIL_ATTRIBUTE:
                searchField = "email";
                break;
            case NAME_ATTRIBUTE:
            default:
                searchField = "first";
                break;
        }
        wildcard = getWildCard(searchType, pattern);

        StreamDataFactory sdf = __session.getDataFactory();
        iqSearch = (InfoQuery)sdf.createPacketNode(__session.IQ_NAME, InfoQuery.class);
        iqSearch.setType(InfoQuery.SET);
        XMPPPrincipal client = (XMPPPrincipal)__session.getPrincipal();
        iqSearch.setFrom(client.getJID());
        iqSearch.setTo(judJID);
        iqSearch.setID(__session.nextID("search"));

        sq = sdf.createElementNode(__session.SEARCH_QUERY_NAME, null);
        sq.addElement(new NSI(searchField, null)).addText(wildcard);
        iqSearch.add(sq);

        try {
            iqSearch = (InfoQuery)__session.sendAndWatch(iqSearch, __session.getRequestTimeout());
        } catch (StreamException se) {
            throw new CollaborationException(se);
        }

        if ((iqSearch == null) ||(iqSearch.getType() != InfoQuery.RESULT)) {
            throw new CollaborationException("Error while doing the search");
        }

        sq = (StreamElement)iqSearch.listExtensions("jabber:iq:search").get(0);

        Iterator itr = sq.listElements().iterator();
        while (itr.hasNext()) {
            StreamElement n = (StreamElement)itr.next();
            JID jid = new JID(n.getAttributeValue("jid"));
            String displayName = __session.getItemValue(n, "first");
            XMPPPrincipal principal;

            if ("".equals(jid.getNode())) {
                principal = new XMPPGroup(jid, displayName);
            } else {
                principal = new XMPPPrincipal(jid, displayName);
            }

            StreamElement x = (StreamElement)n.getFirstElement(__session.SUN_PRIVATE_NAME);
            if (x != null) {
                for (Iterator i = x.listElements("property").iterator(); i.hasNext();) {
                    StreamElement prop = (StreamElement)i.next();
                    String name = prop.getAttributeValue("name");
                    StreamElement val = (StreamElement)prop.getFirstElement("value");
                    if (val != null)
                        principal.setProperty(name, val.normalizeTrimText());
                }
            }
            p.add(principal);
        }

        if (p.size() > 0) {
            XMPPPrincipal[] result =  new XMPPPrincipal[p.size()];
            for (int i = 0; i < p.size(); i++) {
                result[i] = (XMPPPrincipal)p.get(i);
            }
            return result;
        }
        return null;
    }

    public PersonalStoreEntry[] search(int searchType, String pattern, String entryType)
                                throws CollaborationException
    {
         if (searchType == SEARCHTYPE_EQUALS) {
             return search(searchType, pattern, entryType, UID_ATTRIBUTE);
         }
         return search(searchType, pattern, entryType, NAME_ATTRIBUTE);
    }

    public PersonalStoreEntry[] search(int searchType, String pattern, String entryType, int attribute)
                                throws CollaborationException
    {
        if (PersonalStoreEntry.CONFERENCE.equals(entryType)) {
            return searchPersonalConference(searchType, pattern);

        } else if (entryType.equals(PersonalStoreEntry.CONTACT) ||
                   entryType.equals(PersonalStoreEntry.GROUP)) {
            CollaborationPrincipal[] p = searchPrincipals(searchType, pattern, attribute);
            if (p == null) return null;
            PersonalStoreEntry[] pse = new PersonalStoreEntry[p.length];
            // convert to PersonalStoreEntry
            for (int i = 0; i < p.length; i++) {
                JID jid = ((XMPPPrincipal)p[i]).getJID();
                // can use getEntryType to do a disco#info to check
                // if it is a group or not.
                //String type = getEntryType(jid);
                if (p[i] instanceof XMPPGroup) {
                    pse[i] = new XMPPPersonalGroup(__session,
                                                   p[i].getDisplayName(),
                                                   ((XMPPPrincipal)p[i]).getJid());
                } else {
                    pse[i] = new XMPPPersonalContact(__session,
                                                     p[i].getDisplayName(),
                                                     JIDUtil.getBareJIDString(p[i].getUID()));
                }
            }
            return pse;
        } else if (entryType.equals(PersonalStoreEntry.GATEWAY)) {
            return searchGateways(searchType, pattern);
        }

        return null;
    }


    public PersonalProfile getProfile() throws CollaborationException {
        if (_personalProfile != null) return _personalProfile;
        else {
            _personalProfile = getPrivateQuery(null);
            _profiles.put(JIDUtil.getBareJIDString(__session.getPrincipal().getUID()), _personalProfile);
            return _personalProfile;
        }
    }

    public PersonalProfile getProfile(CollaborationPrincipal collaborationPrincipal)
                           throws CollaborationException
    {
        if (collaborationPrincipal == null) {
            return getProfile();
        } else {
            PersonalProfile profile = null;
            profile = (PersonalProfile)_profiles.get(
                    JIDUtil.getBareJIDString(collaborationPrincipal.getUID()));
            if (profile == null) {
                profile =
                    (PersonalProfile)getPrivateQuery(((XMPPPrincipal)collaborationPrincipal).getJID());
                _profiles.put(
                        JIDUtil.getBareJIDString(collaborationPrincipal.getUID()), 
                        profile);
            }
            return profile;
        }
    }

    public void save() throws CollaborationException {
        // it should be better to call the individual entry's save() method
        for (Iterator i = _getFolders().values().iterator(); i.hasNext();) {
            PersonalStoreFolder f = (PersonalStoreFolder)i.next();
            saveFolder(f);
        }
        saveProfile();
    }

    public void initialize(PersonalStoreServiceListener listener)
                           throws CollaborationException {
        addPersonalStoreServiceListener(listener);
        sendLDAPGroupRequest();
        sendRosterRequest();
        sendBookmarkRequest();
        // discover the services in background
        // ignore the failure of loading of the
        // services in case it fails
        try {
        __session.loadJabberServices();
        } catch (CollaborationException ce) {}

    }

    /////////////////
    /////////Protected Methods
    ////////////
    protected Collection expandGroup(org.netbeans.lib.collab.PersonalGroup group) {
        Hashtable p = new Hashtable();
        InfoQuery iq;
        DiscoItemsQuery dq;

        try {
            //__session.loadJabberServices();
            dq = __session.sendItemsQuery(getService(), group.getEntryId());

            Iterator itr = dq.listItems().iterator();
            while (itr.hasNext()) {
                DiscoItem item = (DiscoItem)itr.next();
                p.put(item.getJID(),new XMPPPrincipal(item.getJID(), item.getName()));
            }

            Collection c = getEntries(PersonalStoreEntry.CONTACT);
            itr = c.iterator();
            while (itr.hasNext()) {
                PersonalContact pc = (PersonalContact)itr.next();
                JID jid = new JID(pc.getEntryId());
                if(p.containsKey(jid)) {
                    p.put(jid, pc.getPrincipal());
                }
            }
        } catch (Exception e) {
        }

        return p.values();
    }

    protected void saveEntry(PersonalStoreEntry entry) throws CollaborationException {
        setRosterQuery(entry);
    }

    protected void removeEntry(PersonalStoreEntry entry) throws CollaborationException {
        removeRosterQuery(entry);
    }

    protected static void saveFolder(PersonalStoreFolder folder) throws CollaborationException {
        Collection entries = folder.getEntries();
        for (Iterator i = entries.iterator(); i.hasNext();) {
            PersonalStoreEntry pse = (PersonalStoreEntry)i.next();
            pse.save();
        }
    }

    protected void removeFolder(PersonalStoreFolder folder) throws CollaborationException {
        Collection entries = folder.getEntries();
        for (Iterator i = entries.iterator(); i.hasNext();) {
            PersonalStoreEntry pse = (PersonalStoreEntry)i.next();
            pse.removeFromFolder(folder);
            pse.save();
        }
        _getFolders().remove(folder.getDisplayName());
        XMPPSessionProvider.debug("removeFolder folders: " + _folders.values());
    }

    protected void removeProfile() throws CollaborationException {
        if (_personalProfile == null) return;
        _personalProfile.clear(true);
        _personalProfile.save();
    }

    protected void setRosterQuery(PersonalStoreEntry entry)
                   throws CollaborationException {
        InfoQuery iqRoster;
        RosterQuery rq;
        StreamDataFactory sdf = __session.getDataFactory();
        iqRoster = (InfoQuery)sdf.createPacketNode(__session.IQ_NAME, InfoQuery.class);
        iqRoster.setType(InfoQuery.SET);
        iqRoster.setID(__session.nextID("roster"));

        rq = (RosterQuery)sdf.createExtensionNode(RosterQuery.NAME, RosterQuery.class);
        iqRoster.add(rq);
        XMPPSessionProvider.info("Saving Roster Entry : " + entry.getEntryId());
        JID jid = new JID(entry.getEntryId());
        org.jabberstudio.jso.x.core.RosterItem item = rq.createItem(jid);
        item.setDisplayName(entry.getDisplayName());
        Collection groups = ((XMPPPersonalStoreEntry)entry).getFolders();
        for (Iterator i = groups.iterator(); i.hasNext(); ) {
            String group = ((PersonalStoreFolder)i.next()).getDisplayName();
            item.addGroup(group);
        }

        rq.add(item);

        try {
            iqRoster = (InfoQuery)__session.sendAndWatch(iqRoster,
                                                             __session.getRequestTimeout());
        } catch (StreamException se) {
            throw new CollaborationException(se);
        }
    }


    protected void removeRosterQuery(PersonalStoreEntry entry)
                   throws CollaborationException
    {
        InfoQuery iqRoster;
        RosterQuery rq;
        StreamDataFactory sdf = __session.getDataFactory();
        iqRoster = (InfoQuery)sdf.createPacketNode(__session.IQ_NAME, InfoQuery.class);
        iqRoster.setType(InfoQuery.SET);
        iqRoster.setID(__session.nextID("roster"));

        rq = (RosterQuery)sdf.createExtensionNode(RosterQuery.NAME, RosterQuery.class);
        iqRoster.add(rq);

        org.jabberstudio.jso.x.core.RosterItem item = rq.createItem(new JID(entry.getEntryId()));
        item.setSubscription(org.jabberstudio.jso.x.core.RosterItem.REMOVE);

        rq.add(item);

        try {
            //iqRoster = (InfoQuery)__session.sendAndWatch(iqRoster, __session.REQUEST_TIMEOUT);
            __session.getConnection().send(iqRoster);
        } catch (StreamException se) {
            throw new CollaborationException(se);
        }
    }

    protected void setPrivateStorage(PersonalStoreEntry entry) throws CollaborationException {
        if (entry instanceof org.netbeans.lib.collab.PersonalConference) {
            String entryId = entry.getEntryId();
            JID jid = JIDUtil.encodedJID(entryId);
            XMPPSessionProvider.info("Saving Conference Bookmark Storage Entry : " + entryId);
            XMPPConference c = (XMPPConference)__session.getConference(jid.toString());
            if (c == null) {
                c = new XMPPConference(__session, StringUtility.getLocalPartFromAddress(entryId), jid);
            }
            __session.addConference(c);
            _subscribedConferenceItems.put(jid.toString(), entry);
            setConferenceBookmarkQuery();
        } else if (entry instanceof org.netbeans.lib.collab.PersonalGroup) {
            XMPPSessionProvider.info("Saving LDAP Group Storage Entry : " + entry.getEntryId());
            JID jid = new JID(entry.getEntryId());
            _personalGroups.put(entry.getEntryId(), entry);
            try {
                setLDAPGroupQuery();
            } catch(CollaborationException e) {
                //i don't know to which it was added or removed
                //need to be taken care by caller
                throw e;
            }
            _firePersonalStoreServiceListeners(new PersonalStoreEvent(PersonalStoreEvent.TYPE_ADDED,entry));
        }
    }

    protected void removePrivateStorage(PersonalStoreEntry entry) throws CollaborationException {
        if (entry instanceof org.netbeans.lib.collab.PersonalConference) {
            String encodedEntryId = JIDUtil.encodedJID(entry.getEntryId()).toString();
            XMPPSessionProvider.info("Deleting Conference Bookmark Entry : " + encodedEntryId);
            __session.removeConference(encodedEntryId);
            _subscribedConferenceItems.remove(encodedEntryId);
            setConferenceBookmarkQuery();
        } else if (entry instanceof org.netbeans.lib.collab.PersonalGroup) {
            XMPPSessionProvider.info("Deleting LDAP Group Storage Entry : " + entry.getEntryId());
            // just remove the entry for now, deal with subscription later
            _personalGroups.remove(entry.getEntryId());
            try {
                setLDAPGroupQuery();
            } catch(CollaborationException e) {
                //i don't know from which it was removed
                throw e;
            }
            _firePersonalStoreServiceListeners(new PersonalStoreEvent(PersonalStoreEvent.TYPE_REMOVED,entry));
        }
    }

    //////////////////
    ////////Package methods
    ////////////////////
    private List searchAllConferenceItems(int searchType, String pattern) throws CollaborationException
    {
        /*if (StringUtility.hasDomain(pattern)) {
            pattern = JIDUtil.encodedJID(pattern).toString();
        } else {
            pattern = JIDUtil.encodedString(pattern);
        }*/

        String wildcard = getWildCard(searchType, pattern);

        //__session.loadJabberServices();
        XMPPConferenceService cs =
            (XMPPConferenceService)__session.getConferenceService();
        DiscoItemsQuery sq  = 
            __session.sendItemsQuery(cs.getService(),wildcard);
        List p = sq.listItems();

        for (Iterator iter = cs.getRemoteServices().iterator();
             iter.hasNext(); ) {
            JID serviceJID = (JID)iter.next();
            try {
                sq = __session.sendItemsQuery(serviceJID, wildcard);
                p.addAll(sq.listItems());
            } catch (CollaborationException e) {
            }
        }
        return p;
    }
    
    List searchPublicConferences(int searchType, String pattern) throws CollaborationException {
        return searchAllConferenceItems(searchType, pattern);
    }

    private PersonalConference[] searchPersonalConference(int searchType, String pattern) throws CollaborationException
   {
       int i = 0;
       List l = searchPublicConferences(searchType, pattern);
       if (l == null || l.size() == 0 ) {
           return null;
       } else {
           XMPPPersonalConference[] result =  new XMPPPersonalConference[l.size()];
           for (Iterator itr = l.iterator(); itr.hasNext(); ) {
               DiscoItem item = (DiscoItem)itr.next();
               result[i++] = new XMPPPersonalConference(__session,
                                                        item.getName(),
                                                        item.getJID().toString());
           }
           return result;
       }
   }

    void getRosterQuery() throws CollaborationException {
        InfoQuery iqRoster;
        RosterQuery rq;
        org.jabberstudio.jso.x.core.RosterItem item;
        StreamDataFactory sdf = __session.getDataFactory();
        // Request roster
        iqRoster = (InfoQuery)sdf.createPacketNode(__session.IQ_NAME, InfoQuery.class);
        iqRoster.setType(InfoQuery.GET);
        iqRoster.setID(__session.nextID("roster"));

        rq = (RosterQuery)sdf.createExtensionNode(RosterQuery.NAME, RosterQuery.class);
        iqRoster.add(rq);

        try {
            iqRoster = (InfoQuery)__session.sendAndWatch(iqRoster,
                                                            __session.getRequestTimeout());
        } catch (StreamException se) {
            XMPPSessionProvider.error(se.toString(),se);
        }

        if ((iqRoster == null) || (iqRoster.getType() != InfoQuery.RESULT)) {
            throw new CollaborationException("Failed to get roster " + iqRoster);
        }


        List rqList = iqRoster.listExtensions(RosterQuery.NAMESPACE);
        if (rqList.size() > 0) {
            processRosterQuery((RosterQuery)rqList.get(0));
        }
    }

    //This is assumed to be used only once during startup, if this
    //is relaxed then this impl has to be revisited
    void sendRosterRequest() throws CollaborationException {
        InfoQuery iqRoster;
        RosterQuery rq;
        StreamDataFactory sdf = __session.getDataFactory();
        // Request roster
        iqRoster = (InfoQuery)sdf.createPacketNode(__session.IQ_NAME, InfoQuery.class);
        iqRoster.setType(InfoQuery.GET);
        iqRoster.setID(__session.nextID("roster"));

        rq = (RosterQuery)sdf.createExtensionNode(RosterQuery.NAME, RosterQuery.class);
        iqRoster.add(rq);

        try {
            __session._connection.send(iqRoster);
        } catch (StreamException se) {
            XMPPSessionProvider.error(se.toString(),se);
            throw new CollaborationException(se); } }

    void processRosterQuery(RosterQuery rosterQuery) throws CollaborationException {
        Iterator itr = rosterQuery.listElements().iterator();

        while (itr.hasNext()) {
            RosterItem item = (RosterItem)itr.next();
            //XMPPSessionProvider.debug("Item " + item.getDisplayName() + " : " + item.getJID());
            XMPPSessionProvider.debug("processRosterQuery called subscription: " + item.getSubscription());
            XMPPSessionProvider.debug("processRosterQuery item: " + item);
            List groups = item.listGroups();
            JID jid = item.getJID();
            String displayName = item.getDisplayName();
            if ((displayName == null) || (displayName.trim().length() == 0)) {
                displayName = jid.toString();
            }
            String uid = jid.getNode();
            String host = jid.getDomain();
            String resource = jid.getResource();

            boolean remove = false;
            if ((item.getSubscription() != null) &&
               (RosterItem.REMOVE.equals(item.getSubscription()))) {
                remove = true;
            } else if (groups.size() == 0) {
                groups.add("");
            }

            if (uid.equals("")) {
                if (isGatewayEntry(jid)) {
                    XMPPSessionProvider.debug("Got a gateway entry " + jid);
                    XMPPPersonalGateway gtw = null;
                    int eventType;
                    if(_asyncRosterProcessed) {
                        gtw = (XMPPPersonalGateway)getEntry(PersonalStoreEntry.GATEWAY,jid.toString());
                    }

                    if(gtw == null) {
                       gtw = new XMPPPersonalGateway(__session, displayName, jid.toString());
                       eventType = PersonalStoreEvent.TYPE_ADDED;
                    } else {
                        eventType = PersonalStoreEvent.TYPE_MODIFIED;
                    }

                    //XMPPPersonalGateway contact = new XMPPPersonalGateway(__session, displayName, jid.toString());
                    if (remove) {
                        removeContactsFromFolders(gtw);
                        eventType = PersonalStoreEvent.TYPE_REMOVED;
                    } else {
                        gtw.setDisplayName(displayName);
                        Collection c = gtw.getFolders();
                        Iterator ltr = c.iterator();
                        while(ltr.hasNext()){
                            XMPPPersonalFolder fol = (XMPPPersonalFolder)ltr.next();
                            fol.removeEntry(gtw);
                        }
                        gtw.clearFolders();
                        addContactsToFolders(gtw, groups);
                    }
                    if(_asyncRosterProcessed) {
                        _firePersonalStoreServiceListeners(new PersonalStoreEvent(eventType,gtw));
                    }
                }
            } else {
                XMPPPersonalContact entry = null;
                XMPPPresenceService ps = (XMPPPresenceService)__session.getPresenceService();

                entry = new XMPPPersonalContact(__session, displayName, jid.toString());
                int eventType;
                if(_asyncRosterProcessed)
                    entry = (XMPPPersonalContact)getEntry(PersonalStoreEntry.CONTACT,jid.toString());
                if(entry == null) {
                    entry = new XMPPPersonalContact(__session, displayName, jid.toString());
                    eventType = PersonalStoreEvent.TYPE_ADDED;
                } else {
                    eventType = PersonalStoreEvent.TYPE_MODIFIED;
                }
                
                if (remove) {
                    removeContactsFromFolders(entry);
                    ps.removeSubscriptions(jid);
                    eventType = PersonalStoreEvent.TYPE_REMOVED;
                } else {
                    RosterItem.SubscriptionType subsType = item.getSubscription();
                    
                    if(subsType.isSubscribedFrom()){
                        entry.setOutboundSubscriptionStatus(PersonalContact.SUBSCRIPTION_STATUS_OPEN);
                    } else {
                        entry.setOutboundSubscriptionStatus(PersonalContact.SUBSCRIPTION_STATUS_CLOSED);
                    }
                    
                    if(subsType.isSubscribedTo()){
                        entry.setInboundSubscriptionStatus(PersonalContact.SUBSCRIPTION_STATUS_OPEN);
                    } else {
                        entry.setInboundSubscriptionStatus(PersonalContact.SUBSCRIPTION_STATUS_CLOSED);
                    }
                    
                    entry.setDisplayName(displayName);
                    Collection c = entry.getFolders();
                    Iterator ltr = c.iterator();
                    while(ltr.hasNext()){
                        XMPPPersonalFolder fol = (XMPPPersonalFolder)ltr.next();
                        fol.removeEntry(entry);
                    }
                    entry.clearFolders();
                    addContactsToFolders(entry, groups);
                    if (item.isAskSubscribe()) {
                        //create a presence with pending status
                        entry.setInboundSubscriptionStatus(PersonalContact.SUBSCRIPTION_STATUS_PENDING);
                        org.netbeans.lib.collab.Presence p =
                            ps.createPresence(jid.toString(),
                                              org.netbeans.lib.collab.PresenceService.STATUS_PENDING,
                                              "",
                                              0);
                        ps.addSubscriptions(jid,p);
                    }
                }
                //_watchers.remove(jid.toString());
                //fire the events here
                if(_asyncRosterProcessed) {
                    _firePersonalStoreServiceListeners(new PersonalStoreEvent(eventType,entry));
                }
            }
        }
    }

    void processAsyncRosterQuery(RosterQuery rosterQuery) {
        //XMPPSessionProvider.debug("processAsyncRosterQuery");
        try {
            processRosterQuery(rosterQuery);

        } catch (Exception e) {
            //additionally we should handle connection lost
            //and parsing exceptions to be done.
            _asyncRosterException = e;
        } finally {
            synchronized(this) {
                _asyncRosterProcessed = true;
                notifyAll();
                //XMPPSessionProvider.debug("Notification");
            }
        }
    }

    // same here, assuming to be used only once during startup
    void sendBookmarkRequest() throws CollaborationException {
        StreamDataFactory sdf = __session.getDataFactory();
        InfoQuery iqPrivate = (InfoQuery)sdf.createPacketNode(XMPPSession.IQ_NAME, InfoQuery.class);

        iqPrivate.setType(InfoQuery.GET);
        iqPrivate.setID(__session.nextID("private"));

        StreamElement pq = sdf.createElementNode(new NSI("query", "jabber:iq:private"), null);

        Extension n = sdf.createExtensionNode(__session.STORAGE_BOOKMARK_NAME);
        pq.add(n);
        iqPrivate.add(pq);

        try {
            __session._connection.send(iqPrivate);
        } catch (StreamException se) {
            XMPPSessionProvider.error(se.toString(), se);
            throw new CollaborationException(se);
        }
    }

    void processBookmarkQuery(StreamElement bookmarks) throws CollaborationException {
        List confs = bookmarks.listElements("conference");
        for (Iterator i = confs.iterator(); i.hasNext();) {
            StreamElement conf = (StreamElement)i.next();
            String name = conf.getAttributeValue("name");
            String jid = conf.getAttributeValue("jid");
            String nick = null;
            StreamElement nickElem = (StreamElement)conf.getFirstElement("nick");
            if (nickElem != null) {
                nick = nickElem.normalizeTrimText();
            }
            XMPPPersonalStoreEntry entry = new XMPPPersonalConference(__session, name, jid);
            XMPPConference c = (XMPPConference)__session.getConference(jid);
            if (c == null) {
                c = new XMPPConference(__session, name, new JID(jid));
            }
            _subscribedConferenceItems.put(jid, entry);
            __session.addConference(c);
        }

    }

    void processAsyncBookmarkQuery(StreamElement bookmarks) {
        try {
            processBookmarkQuery(bookmarks);
        } catch (Exception e) {
            e.printStackTrace();
            _asyncBookmarkException = e;
        } finally {
            synchronized(_bookmarkLock) {
                _asyncBookmarkProcessed = true;
                _bookmarkLock.notifyAll();
            }
        }
    }

    // same here, assuming to be used only once during startup
    void sendLDAPGroupRequest() throws CollaborationException {
        StreamDataFactory sdf = __session.getDataFactory();
        InfoQuery iqPrivate = (InfoQuery)sdf.createPacketNode(XMPPSession.IQ_NAME, InfoQuery.class);

        iqPrivate.setType(InfoQuery.GET);
        iqPrivate.setID(__session.nextID("private"));

        StreamElement pq = sdf.createElementNode(new NSI("query", "jabber:iq:private"), null);

        Extension n = sdf.createExtensionNode(__session.SUN_PRIVATE_LDAPGROUP_NAME);
        pq.add(n);
        iqPrivate.add(pq);

        try {
            __session._connection.send(iqPrivate);
        } catch (StreamException se) {
            XMPPSessionProvider.error(se.toString(), se);
            throw new CollaborationException(se);
        }
    }

    void processLDAPGroupQuery(StreamElement ldapGroups) throws CollaborationException {
        List groups = ldapGroups.listElements("ldapgroup");
        for (Iterator i = groups.iterator(); i.hasNext();) {
            StreamElement group = (StreamElement)i.next();
            String name = group.getAttributeValue("name");
            String jid = group.getAttributeValue("jid");
            XMPPPersonalGroup contact = new XMPPPersonalGroup(__session, name, jid);
            List g = group.listElements("group");
            for (Iterator itr = g.iterator(); itr.hasNext();) {
                StreamElement elem = (StreamElement)itr.next();
                String folderName = elem.normalizeTrimText();
                XMPPPersonalFolder folder = null;
                if (!_folders.containsKey(folderName)) {
                    folder = new XMPPPersonalFolder(__session, folderName);
                    _folders.put(folderName, folder);
                } else {
                    folder = (XMPPPersonalFolder)_folders.get(folderName);
                }
                contact.addToFolder(folder);
            }
            _personalGroups.put(jid, (PersonalStoreEntry)contact);
        }
    }

    void processAsyncLDAPGroupQuery(StreamElement bookmarks) {
        try {
            processLDAPGroupQuery(bookmarks);
        } catch (Exception e) {
            _asyncLDAPGroupException = e;
        } finally {
            synchronized(this) {
                _asyncLDAPGroupProcessed = true;
                notifyAll();
            }
        }
    }


    JID getService() throws CollaborationException {
        JID _pstoreService = __session.getJUDService();
        if (_pstoreService == null) {
            __session.waitForServiceInitialization(this);
            _pstoreService = __session.getJUDService();
        }
        if (_pstoreService == null) throw new ServiceUnavailableException("personal store service was not initialized successfully");
        return _pstoreService;
    }

    /**
     * return the service associated with the given domain.
     * default to the local jud service.
     * @param domain domain name.
     */
    JID getService(String domain) throws CollaborationException
    {
        if (domain == null ||
            __session.getCurrentUserJID().getDomain().equalsIgnoreCase(domain)) {
            return getService();
        }

        if (domain != null && domain.trim().length() > 0) {
            domain = domain.toLowerCase();
            for (Iterator iter = _remoteServices.iterator();
                 iter.hasNext(); ) {
                JID rsjid = (JID)iter.next();
                if (rsjid.getDomain().toLowerCase().indexOf(domain) >= 0) {
                    return rsjid;
                }
            }
        }

        // no jud service for this domain
        return null;
    }

    void addRemoteService(JID jid) {
         _remoteServices.add(jid);
    }
    Set getRemoteServices() { return _remoteServices; }

    /*void addGatewayEntries(String jid, PersonalStoreEntry pse)  throws CollaborationException {
        _gateWays.put(jid, pse);
    }*/

    PersonalGateway getGatewayEntry(String jid) throws CollaborationException {
        return (PersonalGateway)__session.getGateways().get(jid);
    }

    void saveProfile() throws CollaborationException {
        setPrivateQuery(_personalProfile);
    }

    void saveProfile(XMPPPersonalProfile pp) throws CollaborationException {
        setPrivateQuery(pp);
    }
    ///////////////////
    ///Private methods
    //////////////////

    private String getEntryType(JID jid) {
        InfoQuery iq;
        DiscoInfoQuery dq;

        try {
            __session.loadJabberServices();
            JID judJID = getService(jid.getDomain());
            if (judJID == null) return null;
            dq = __session.sendInfoQuery(judJID, jid.toString());

            for (Iterator iter = dq.listIdentities().iterator(); iter.hasNext(); ) {
                DiscoIdentity discoIdentity = (DiscoIdentity)iter.next();
                String type = discoIdentity.getType();
                if (PersonalStoreEntry.CONTACT.equals(type)) return PersonalStoreEntry.CONTACT;
                else if (PersonalStoreEntry.GROUP.equals(type)) return PersonalStoreEntry.GROUP;
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

    private void checkAsyncRequest() throws CollaborationException{
        //XMPPSessionProvider.debug("In checkAsync " + Thread.currentThread().getName());
        if(_asyncRosterProcessed && _asyncLDAPGroupProcessed) {
            if(_asyncRosterException == null && _asyncLDAPGroupException == null) {
                return;
            } else {
                throw new CollaborationException(_asyncRosterException);
            }
        } else {
            synchronized(this) {
                int waitTime = 5000;
                while(!_asyncRosterProcessed || !_asyncLDAPGroupProcessed) {
                    long start = System.currentTimeMillis();
                    try {
                        //XMPPSessionProvider.debug("Wait happenning......................");
                        wait(waitTime);
                        //XMPPSessionProvider.debug("Getting out of wait");
                    } catch (InterruptedException e) {
                        throw new CollaborationException("Wait for RosterQuery and LDAPGroup retreival interrupted");
                    }

                    if(!_asyncRosterProcessed || !_asyncLDAPGroupProcessed) {
                        int slept = (int)(System.currentTimeMillis() - start);
                        if(slept >= waitTime) {
                            throw new CollaborationException("Wait for RosterQuery and LDAPGroup retreival timed out");
                        } else {
                            waitTime -= slept;
                        }
                    }
                }

                if(_asyncRosterException == null && _asyncLDAPGroupException == null) {
                    //XMPPSessionProvider.debug("Returning sucessfully");
                    return;
                } else {
                    throw new CollaborationException(_asyncRosterException);
                }
            }
        }
    }


    private void checkAsyncBookmarkRequest() throws CollaborationException{
        //XMPPSessionProvider.debug("In checkAsyncBookmark " + Thread.currentThread().getName());
        if (_asyncBookmarkProcessed) {
            if(_asyncBookmarkException == null) {
                return;
            } else {
                throw new CollaborationException(_asyncBookmarkException);
            }
        } else {
            synchronized(_bookmarkLock) {
                int waitTime = 5000;
                while(!_asyncBookmarkProcessed) {
                    long start = System.currentTimeMillis();
                    try {
                        //XMPPSessionProvider.debug("Wait happenning......................");
                        _bookmarkLock.wait(waitTime);
                        //XMPPSessionProvider.debug("Getting out of wait");
                    } catch (InterruptedException e) {
                        throw new CollaborationException("Wait for BookmarkQuery retreival interrupted");
                    }

                    if(!_asyncBookmarkProcessed ) {
                        int slept = (int)(System.currentTimeMillis() - start);
                        if(slept >= waitTime) {
                            throw new CollaborationException("Wait for BookmarkQuery retreival timed out");
                        } else {
                            waitTime -= slept;
                        }
                    }
                }

                if(_asyncBookmarkException == null) {
                    //XMPPSessionProvider.debug("Returning sucessfully");
                    return;
                } else {
                    throw new CollaborationException(_asyncBookmarkException);
                }
            }
        }
    }


    //after discussion with rahul came to follwoing conclusion
    //only folders need to be guarded
    //other information like conferences should be retreived from folders
    //probably subscription info should should be associated with the entries

    private Map _getFolders() throws CollaborationException{
        checkAsyncRequest();
        return _folders;
    }

    private Map _getSubscribedConferenceItems() throws CollaborationException {
        checkAsyncBookmarkRequest();
        return _subscribedConferenceItems;
    }

    private Properties getUserAttributes(String uid) {
        try {
            String localUid = org.netbeans.lib.collab.util.StringUtility.getLocalPartFromAddress(uid);
            CollaborationPrincipal[] searchResults =
                    searchPrincipals(PersonalStoreService.SEARCHTYPE_EQUALS, localUid);
            if (searchResults == null) return null;
            boolean found = false;
            CollaborationPrincipal cp = null;
            for (int i = 0; i < searchResults.length; i++) {
                cp = searchResults[i];

                if (JIDUtil.getBareJIDString(cp.getUID()).equalsIgnoreCase(uid)) {
                    found = true;
                    break;
                }
            }
            if (found) return ((XMPPPrincipal)cp).getAttributes();
        } catch(CollaborationException e) {
            XMPPSessionProvider.error(e.toString(),e);
        }

        return null;
    }

    private XMPPPersonalProfile getPrivateQuery(JID jid) throws CollaborationException {
        //InfoQuery iqPrivate;
        boolean forOther = false;

        Properties privateSettings = sendPrivateGetQuery(jid, XMPPSession.SUN_PRIVATE_SUNMSGR_NAME);
        Properties ISServicePolicyAttrs = null;
        if (jid == null) {
            ISServicePolicyAttrs = sendPrivateGetQuery(jid, XMPPSession.SUN_PRIVATE_POLICY_NAME);
        }

        /*
        iqPrivate = (InfoQuery)_sdf.createPacketNode(XMPPSession.IQ_NAME, InfoQuery.class);

        iqPrivate.setType(InfoQuery.GET);
        iqPrivate.setID(__session.nextID("private"));
        if (jid != null) {
            iqPrivate.setFrom(jid);
        }

        pq = _sdf.createElementNode(new NSI("query", "jabber:iq:private"), null);

        Extension n = _sdf.createExtensionNode(__session.SUN_PRIVATE_SUNMSGR_NAME);
        pq.add(n);
        iqPrivate.add(pq);

        try {
            iqPrivate = (InfoQuery)__session.sendAndWatch(iqPrivate,
                                                              __session.REQUEST_TIMEOUT);
        } catch (StreamException se) {
            throw new CollaborationException(se);
        }

        if ((iqPrivate == null) || (iqPrivate.getType() != InfoQuery.RESULT)) {
            throw new CollaborationException("Could not authenticate to  server!");
        }

        Properties p = new Properties();

        List privateNodes = iqPrivate.listElements("query");
        pq = (StreamElement)privateNodes.get(0);
        StreamElement properties = (StreamElement)pq.listElements().get(0);

        for (Iterator i = properties.listElements().iterator(); i.hasNext();) {
            StreamElement e = (StreamElement)i.next();
            String val = null;
            Iterator j = e.listElements().iterator();
            if (j.hasNext()) {
                val = ((StreamElement)j.next()).normalizeTrimText();
            }
            p.put(e.getAttributeValue("name"), val);
        }
         */

        String displayName = null;
        if (jid == null) {
            XMPPPrincipal client = (XMPPPrincipal)__session.getPrincipal();
            Properties a = getUserAttributes(JIDUtil.getBareJIDString(client.getUID()));
            if (a != null) displayName = a.getProperty("cn");
            _personalProfile = new XMPPPersonalProfile(__session, 
                    JIDUtil.getBareJIDString(client.getUID()), displayName,
                    privateSettings, a, ISServicePolicyAttrs);
            return _personalProfile;
        } else {
            Properties a = getUserAttributes(jid.toBareJID().toString());
            if (a != null) displayName = a.getProperty("cn");
            XMPPPersonalProfile xp = new XMPPPersonalProfile(__session, jid.toString(), displayName,
                                            privateSettings, a, ISServicePolicyAttrs);
            return xp;
        }
    }

    private Properties sendPrivateGetQuery(JID jid, NSI privateExt) throws CollaborationException {
        StreamDataFactory sdf = __session.getDataFactory();
        InfoQuery iqPrivate = (InfoQuery)sdf.createPacketNode(XMPPSession.IQ_NAME, InfoQuery.class);

        iqPrivate.setType(InfoQuery.GET);
        iqPrivate.setID(__session.nextID("private"));
        if (jid != null) {
            iqPrivate.setFrom(jid);
        }

        StreamElement pq = sdf.createElementNode(new NSI("query", "jabber:iq:private"), null);

        Extension n = sdf.createExtensionNode(privateExt);
        pq.add(n);
        iqPrivate.add(pq);

        try {
            iqPrivate = (InfoQuery)__session.sendAndWatch(iqPrivate,
                    __session.getRequestTimeout());
        } catch (StreamException se) {
            throw new CollaborationException(se);
        }

        if ((iqPrivate == null) || (iqPrivate.getType() != InfoQuery.RESULT)) {
            throw new CollaborationException("Could not get private data from server!");
        }

        Properties p = new Properties();

        List privateNodes = iqPrivate.listElements("query");
        pq = (StreamElement)privateNodes.get(0);
        StreamElement properties = (StreamElement)pq.listElements().get(0);

        for (Iterator i = properties.listElements().iterator(); i.hasNext();) {
            StreamElement e = (StreamElement)i.next();
            String propName = e.getAttributeValue("name");
            String val = null;
            List valueList = e.listElements();
            if (valueList.size() == 0) {
                val = "";
                p.put(propName, val);
            } else if (valueList.size() == 1) {
                val = ((StreamElement)valueList.get(0)).normalizeTrimText();
                p.put(propName, val);
            } else {
                Set valueSet = new HashSet();
                for (Iterator j = valueList.iterator(); j.hasNext(); ) {
                        val = ((StreamElement)j.next()).normalizeTrimText();
                        valueSet.add(val);
                }
                p.put(propName, valueSet);
            }
        }
        return p;
    }


    private void setPrivateQuery(XMPPPersonalProfile p) throws CollaborationException {
        InfoQuery iqPrivate;
        StreamElement pq;
        StreamDataFactory sdf = __session.getDataFactory();
        iqPrivate = (InfoQuery)sdf.createPacketNode(__session.IQ_NAME, InfoQuery.class);

        iqPrivate.setType(InfoQuery.SET);
        iqPrivate.setID(__session.nextID("private"));

        pq = sdf.createElementNode(new NSI("query", "jabber:iq:private"), null);

        Extension n = sdf.createExtensionNode(__session.SUN_PRIVATE_SUNMSGR_NAME);

        Map m = p.getProperties(false);
        if (m instanceof Hashtable) {
            m = (Map)((Hashtable)m).clone();
        } else if (m instanceof HashMap) {
            m = (Map)((HashMap)m).clone();
        }
        Set propNames = m.keySet();
        for (Iterator i = propNames.iterator(); i.hasNext();) {
            String prop = (String)i.next();
            String val = p.getProperty(prop, "");
            StreamElement e = sdf.createElementNode(new NSI("property", null), null);
            e.setAttributeValue("name", prop);
            e.addElement("value").addText(val);
            n.add(e);
        }

        pq.add(n);
        iqPrivate.add(pq);

        try {
            iqPrivate = (InfoQuery)__session.sendAndWatch(iqPrivate,
                                                              __session.getRequestTimeout());
        } catch (StreamException se) {
            throw new CollaborationException(se);
        }

        if ((iqPrivate == null) || (iqPrivate.getType() != InfoQuery.RESULT)) {
            throw new CollaborationException("Could not set the private data to the server!");
        }
    }

    private void setConferenceBookmarkQuery() throws CollaborationException {
        InfoQuery iqPrivate;
        StreamElement pq;
        StreamDataFactory sdf = __session.getDataFactory();
        iqPrivate = (InfoQuery)sdf.createPacketNode(__session.IQ_NAME, InfoQuery.class);

        iqPrivate.setType(InfoQuery.SET);
        iqPrivate.setID(__session.nextID("private"));

        pq = sdf.createElementNode(new NSI("query", "jabber:iq:private"), null);

        Extension n = sdf.createExtensionNode(__session.STORAGE_BOOKMARK_NAME);

        Collection confs = _subscribedConferenceItems.values();
        for(Iterator i = confs.iterator(); i.hasNext(); ) {
            PersonalStoreEntry entry = (PersonalStoreEntry)i.next();
            String id = entry.getEntryId();
            XMPPConference c =
                (XMPPConference)__session.getConference(id);
            // shouldn't happen
            if (c == null) {
                c = new XMPPConference(__session, StringUtility.getLocalPartFromAddress(id), new JID(id));
            }

            StreamElement e = sdf.createElementNode(new NSI("conference", null), null);
            String display = c.getDisplayName();
            if (display == null) display =  c.getDestination();
            e.setAttributeValue("name", display);
            e.setAttributeValue("jid", c.getDestination());
            XMPPPrincipal client = (XMPPPrincipal)__session.getPrincipal();
            e.addElement("nick").addText(client.getName());
            n.add(e);
        }

        pq.add(n);
        iqPrivate.add(pq);

        try {
            iqPrivate = (InfoQuery)__session.sendAndWatch(iqPrivate,
                                                              __session.getRequestTimeout());
        } catch (StreamException se) {
            throw new CollaborationException(se);
        }

        if ((iqPrivate == null) || (iqPrivate.getType() != InfoQuery.RESULT)) {
            throw new CollaborationException("Could not save conference subscription to the server!");
        }
    }

    private void setLDAPGroupQuery() throws CollaborationException {
        InfoQuery iqPrivate;
        StreamElement pq;
        StreamDataFactory sdf = __session.getDataFactory();
        iqPrivate = (InfoQuery)sdf.createPacketNode(__session.IQ_NAME, InfoQuery.class);

        iqPrivate.setType(InfoQuery.SET);
        iqPrivate.setID(__session.nextID("private"));

        pq = sdf.createElementNode(new NSI("query", "jabber:iq:private"), null);

        Extension n = sdf.createExtensionNode(__session.SUN_PRIVATE_LDAPGROUP_NAME);

        Collection ldapGroups = _personalGroups.values();
        for (Iterator i = ldapGroups.iterator(); i.hasNext();) {
            PersonalStoreEntry entry = (PersonalStoreEntry)i.next();
            StreamElement e = sdf.createElementNode(new NSI("ldapgroup", null), null);
            e.setAttributeValue("name",  entry.getDisplayName());
            e.setAttributeValue("jid", entry.getEntryId());
            Collection groups = ((XMPPPersonalStoreEntry)entry).getFolders();
            for (Iterator itr = groups.iterator(); itr.hasNext();) {
                String group = ((PersonalStoreFolder)itr.next()).getDisplayName();
                e.addElement("group").addText(group);
            }
            n.add(e);
        }

        pq.add(n);
        iqPrivate.add(pq);

        try {
            iqPrivate = (InfoQuery)__session.sendAndWatch(iqPrivate,
                                                              __session.getRequestTimeout());
        } catch (StreamException se) {
            throw new CollaborationException(se);
        }

        if ((iqPrivate == null) || (iqPrivate.getType() != InfoQuery.RESULT)) {
            throw new CollaborationException("Could not set ldap group to the server!");
        }
    }


    private void addContactsToFolders(PersonalStoreEntry contact, List groups) throws CollaborationException {
        for (Iterator i = groups.iterator(); i.hasNext();) {
            String folderName = (String)i.next();
            XMPPPersonalFolder folder = null;

            if (!_folders.containsKey(folderName)) {
                folder = new XMPPPersonalFolder(__session, folderName);
                _folders.put(folderName, folder);
            } else {
                folder = (XMPPPersonalFolder)_folders.get(folderName);
            }
            contact.addToFolder(folder);
        }
    }

    private void removeContactsFromFolders(PersonalStoreEntry contact) throws CollaborationException {

        if (_folders != null && !_folders.isEmpty()) {
            for (Iterator i = _folders.values().iterator(); i.hasNext();) {
                XMPPPersonalFolder folder = (XMPPPersonalFolder)i.next();
                contact.removeFromFolder(folder);
            }
        }
    }

    public static String getWildCard(int searchType, String pattern)  {
        String wildcard = pattern;
        switch (searchType) {
            case PersonalStoreService.SEARCHTYPE_EQUALS:
                wildcard = pattern;
                break;
            case PersonalStoreService.SEARCHTYPE_CONTAINS:
                wildcard = "*" + pattern + "*";
                break;
            case PersonalStoreService.SEARCHTYPE_ENDSWITH:
                wildcard = "*" + pattern;
                break;
            case PersonalStoreService.SEARCHTYPE_STARTSWITH:
                wildcard = pattern + "*";
                break;
        }
        return wildcard;
    }

    private PersonalStoreEntry[] searchGateways(int searchType, String pattern) throws CollaborationException {
        __session.loadJabberServices();
        Map _gateWays = __session.getGateways();
        if (searchType == PersonalStoreService.SEARCHTYPE_EQUALS) {
            PersonalStoreEntry gw = (PersonalStoreEntry)_gateWays.get(pattern);
            if (gw == null) return null;
            return new PersonalStoreEntry[] { gw };
        }

        PersonalStoreEntry[] pse = new PersonalStoreEntry[_gateWays.size()];
        int i = 0;
        for (Iterator iter = _gateWays.values().iterator(); iter.hasNext();) {
            pse[i++] = (PersonalStoreEntry)iter.next();
        }
        return pse;
    }

    boolean isGatewayEntry(JID jid) throws CollaborationException{
        __session.loadJabberServices();
        return __session.getGateways().get(jid.toString()) != null;
    }

    protected void renameFolder(XMPPPersonalFolder folder, String newName)
    {
        String oldName = folder.getDisplayName();
        if (oldName.length() <= 0) return;

        if (_folders.containsKey(newName)) return;

        // change the name in local cache
        folder.setDisplayName(newName);
        _folders.put(newName, folder);
        _folders.remove(oldName);
    }

    void _firePersonalStoreServiceListeners(PersonalStoreEvent event) {
        __session.addWorkerRunnable(new PersonalStoreServiceNotifier(event));
    }

    public void addPersonalStoreServiceListener(PersonalStoreServiceListener listener) {
        if (!_personalStoreServiceListeners.contains(listener))
            _personalStoreServiceListeners.add(listener);
    }
    
    public void removePersonalStoreServiceListener(PersonalStoreServiceListener listener) {
        _personalStoreServiceListeners.remove(listener);
    }
    
    private class PersonalStoreServiceNotifier implements Runnable {
        PersonalStoreEvent event;
        PersonalStoreServiceNotifier(PersonalStoreEvent e) {
            event = e;
        }
        
        public void run() {
            synchronized(_personalStoreServiceListeners) {
                for(Iterator itr = _personalStoreServiceListeners.iterator(); itr.hasNext();) {
                    try {
                        PersonalStoreServiceListener l = (PersonalStoreServiceListener)itr.next();
                        if (l == null) continue;
                        l.onEvent(event);
                    } catch(Exception e) {
                        XMPPSessionProvider.error(e.toString(),e);
                    }
                }
            }
        }
    }
}


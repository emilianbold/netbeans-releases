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
package org.netbeans.modules.collab.provider.im;

import com.sun.collablet.CollabException;
import com.sun.collablet.CollabPrincipal;
import com.sun.collablet.CollabSession;
import com.sun.collablet.ContactGroup;

import org.openide.*;
import org.openide.util.*;

import java.beans.*;

import java.io.*;

import java.util.*;

import org.netbeans.lib.collab.*;

//import org.netbeans.lib.collab.Watcher;
import org.netbeans.modules.collab.core.Debug;


/**
 *
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class IMContactList extends Object {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    public static final String DEFAULT_CONTACT_LIST = NbBundle.getMessage(
            IMContactList.class, "IMContactList_DefaultContactList"
        ); // NOI18N
    public static final String WATCHER_FOLDER = NbBundle.getMessage(IMContactList.class, "IMContactList_WatcherFolder"); // NOI18N

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private IMCollabSession session;
    private Map contactGroups = new TreeMap();
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    /**
     *
     *
     */
    public IMContactList(IMCollabSession session) {
        super();
        this.session = session;
    }

    /**
     *
     *
     */
    public CollabSession getCollabSession() {
        return session;
    }

    /**
     *
     *
     */
    private IMCollabSession _getSession() {
        return session;
    }

    /**
     *
     *
     */
    public ContactGroup[] getContactGroups() {
        Collection values = contactGroups.values();

        return (ContactGroup[]) values.toArray(new ContactGroup[values.size()]);
    }

    /**
     *
     *
     */
    public ContactGroup getContactGroup(String name) {
        return (ContactGroup) contactGroups.get(name);
    }

    /**
     *
     *
     */
    public void addContactGroup(ContactGroup group) throws CollabException {
        if ((group == null) || contactGroups.containsKey(group.getName())) {
            return;
        }

        try {
            PersonalStoreEntry folder = _getSession().getPersonalStoreService().createEntry(
                    PersonalStoreEntry.CONTACT_FOLDER, group.getName()
                );
            folder.save();
            contactGroups.put(group.getName(), group);
            changeSupport.firePropertyChange(CollabSession.PROP_CONTACT_GROUPS, null, null);
        } catch (CollaborationException e) {
            throw new CollabException(e);
        }
    }

    /**
     *
     *
     */
    public void removeContactGroup(ContactGroup group)
    throws CollabException {
        if (group == null) {
            return;
        }

        try {
            PersonalStoreEntry pse = _getSession().getPersonalStoreService().getEntry(
                    PersonalStoreEntry.CONTACT_FOLDER, group.getName()
                );

            if (pse != null) {
                /* Hack. IM does not provide an explicit callback for
                 * subscription denial event, it's a generic onUnsubscribed(),
                 * we have to manipulate contact status, so that we can depend
                 * on contact status = 'PENDING' to display subscription denial
                 * notification
                 */
                for (int i = 0; i < group.getContacts().length; i++) {
                    group.getContacts()[i].setStatus(CollabPrincipal.STATUS_OFFLINE);
                }

                pse.remove();
            }

            contactGroups.remove(group.getName());
            changeSupport.firePropertyChange(CollabSession.PROP_CONTACT_GROUPS, null, null);
        } catch (CollaborationException ce) {
            throw new CollabException(ce);
        }
    }

    /**
     *
     *
     */
    protected void load() throws CollabException, CollaborationException {
        boolean hasContacts = false;

        Collection folders = _getSession().getPersonalStoreService().getFolders(PersonalStoreFolder.CONTACT_FOLDER);
        folders = new ArrayList(folders); // clone first
	
        for (Iterator i = folders.iterator(); i.hasNext();) {
            PersonalStoreFolder folder = (PersonalStoreFolder) i.next();
            String folderName = folder.getDisplayName();
            PersonalStoreFolder newFolder = null;

            IMContactGroup group = null;

            // If the current folder name is blank, remove the entry from old 
            // folder and add it to new (default) one
            if (folderName.equals("")) {
                //folderName=WATCHER_FOLDER;

                /*
                group = new IMContactGroup(this, WATCHER_FOLDER);
                Collection entries=folder.getEntries();
                for (Iterator j = entries.iterator(); j.hasNext(); )
                {
                        PersonalStoreEntry entry=(PersonalStoreEntry)j.next();
                        if (entry.getType()==PersonalStoreEntry.CONTACT)
                        {
                                PersonalContact contact=(PersonalContact)entry;
                                String id=contact.getAddress(PersonalContact.IM);
                                Debug.out.println(" watcher id : " + id);

                                // Convert the preferred address to a principal
                                // TODO: Does this really work?  Alternative is to call
                                // contact.getPrincipal(), but that may not take into
                                // account priority address.
                                CollabPrincipal principal=_getSession().getPrincipal(id);
                                principal.setStatus(CollabPrincipal.STATUS_WATCHED);
                                group.addContact(principal, false);
                        }
                }
                contactGroups.put(group.getName(), group);
                continue;
                 */
                newFolder = (PersonalStoreFolder) _getSession().getPersonalStoreService().getEntry(
                        PersonalStoreEntry.CONTACT_FOLDER, DEFAULT_CONTACT_LIST
                    );

                // Create the default folder if it doesn't exist
                if (newFolder == null) {
                    newFolder = (PersonalStoreFolder) _getSession().getPersonalStoreService().createEntry(
                            PersonalStoreEntry.CONTACT_FOLDER, DEFAULT_CONTACT_LIST
                        );
                }

                folderName = DEFAULT_CONTACT_LIST;
            }

            group = (IMContactGroup) contactGroups.get(folderName);

            if (group == null) {
                // Create the local group
                group = new IMContactGroup(this, folderName);

                //addContactGroup(group);
                contactGroups.put(group.getName(), group);
            }

            // Collect the users from the folder
            Collection entries = folder.getEntries();

            for (Iterator j = entries.iterator(); j.hasNext();) {
                PersonalStoreEntry entry = (PersonalStoreEntry) j.next();

                if (entry.getType() == PersonalStoreEntry.CONTACT) {
                    PersonalContact contact = (PersonalContact) entry;

                    //					String id=contact.getAddress(PersonalContact.IM);
                    // Convert the preferred address to a principal
                    // TODO: Does this really work?  Alternative is to call
                    // contact.getPrincipal(), but that may not take into 
                    // account priority address.
                    //					CollabPrincipal principal=_getSession().getPrincipal(
                    //							StringUtility.removeResource(id));
                    CollaborationPrincipal cp = contact.getPrincipal();
                    CollabPrincipal principal = _getSession().getPrincipal(cp.getUID());

                    if (principal != null) {
                        // TODO: workaround for IM presence bug
                        if (contact.getInboundSubscriptionStatus() == PersonalContact.SUBSCRIPTION_STATUS_OPEN) {
                            // fetch presence and set principal's initial status
                            String identifier = cp.getUID();

                            try {
                                Presence presence = _getSession().getPresenceService().fetchPresence(identifier);
                                Object[] tuples = presence.getTuples().toArray();

                                PresenceTuple tuple = (PresenceTuple) tuples[0];
                                _getSession().setPrincipalStatus(principal, tuple.getStatus());
                            } catch (CollaborationException ce) {
                                // do nothing, the contact will remain in user's list
                                // with status 'unknown'
                            }
                        }

                        group.addContact(principal, false);
                    }

                    //					if (principal!=null)
                    //						group.addContact(principal, false);
                    hasContacts = true;
                }

                /*
                else if ((entry.getType()==PersonalStoreEntry.WATCHER))
                {
                        Watcher watcher = (Watcher)entry;
                        String id = watcher.getAddress();
                        CollabPrincipal principal=_getSession().getPrincipal(id);
                        principal.setStatus(CollabPrincipal.STATUS_WATCHED);
                        group.addContact(principal, false);
                }
                else if(entry.getType()==PersonalStoreEntry.GROUP)
                {   // not supported in bow
                        PersonalGroup contact=(PersonalGroup)entry;
                        Debug.out.println(
                                "WARNING: Found PersonalGroup contact; ignoring");
                        Debug.out.println("     - "+contact.getDisplayName()+
                                " <"+contact.getEntryId()+">");
                }
                */
                if (newFolder != null) {
                    entry.addToFolder(newFolder);
                }
            }

            // Complete move of folder
            if (newFolder != null) {
                newFolder.save();
                folder.remove();
            }
        }

        if (!hasContacts) {
            IMContactGroup group = new IMContactGroup(this, DEFAULT_CONTACT_LIST);
            addContactGroup(group);
        }

        refreshWatcherGroup();
    }

    /**
     *
     *
     */
    protected void save() throws CollabException, CollaborationException {
    }

    /**
     *
     *
     */
    public void renameContactGroup(ContactGroup group, String name)
    throws CollabException {
        try {
            String groupName = group.getName();
            PersonalStoreFolder personalStoreFolder = (PersonalStoreFolder) _getSession().getPersonalStoreService()
                                                                                .getEntry(
                    PersonalStoreEntry.CONTACT_FOLDER, groupName
                );
            personalStoreFolder.rename(name);
            personalStoreFolder.save();
            contactGroups.remove(group.getName());
            contactGroups.put(name, group);
        } catch (CollaborationException ce) {
            throw new CollabException(ce);
        }
    }

    /*
     *
     *
     */
    public void refreshWatcherGroup() { /*
               Debug.out.println(" refreshing watchers");
               contactGroups.remove(WATCHER_FOLDER);
               contactGroups.put(WATCHER_FOLDER, getWatchers());

               changeSupport.firePropertyChange(CollabSession.PROP_CONTACT_GROUPS,
                                                                                       null,null);
         */
    }

    /*
     *
     *
     */
    public IMContactGroup getWatchers() {
        // will switch to IM's Watcher API when it's available
        IMContactGroup watcherGroup = new IMContactGroup(this, WATCHER_FOLDER);
        Debug.out.println(" get watchers");

        try {
            Collection watchers = _getSession().getPersonalStoreService().getEntries(PersonalStoreEntry.WATCHER);
            Iterator iterator = watchers.iterator();
            Debug.out.println("watcher size: " + watchers.size());

            while (iterator.hasNext()) {
                // FIXME [pnejedly] no Watcher class in new service API

                /*                                Watcher watcher = (Watcher)iterator.next();
                                                String uid = watcher.getAddress();
                                                Debug.out.println(" wather id: " + uid);
                                                CollabPrincipal principal = _getSession().getPrincipal(
                                                                StringUtility.removeResource(uid));
                                                if (principal!=null)
                                                {
                                                        principal.setStatus(CollabPrincipal.STATUS_WATCHED);
                                                        watcherGroup.addContact(principal, false);
                                                }
                */
            }
        } catch (CollaborationException e) {
            Debug.errorManager.notify(e);
        }

        //		catch (CollabException ce)
        //		{
        //			Debug.errorManager.notify(ce);
        //		}
        return watcherGroup;

        // temp, 

        /*
        IMContactGroup watcherGroup = new IMContactGroup(this, WATCHER_FOLDER);
        Collection folders = _getSession().getPersonalStoreSession().getFolders(
                PersonalStoreFolder.CONTACT_FOLDER);
        for (Iterator i=folders.iterator(); i.hasNext(); )
        {
                PersonalStoreFolder folder=(PersonalStoreFolder)i.next();
                String folderName=folder.getDisplayName();

                if (!folderName.equals(""))
                {
                        continue;
                }
                else
                {
                        Collection entries=folder.getEntries();
                        for (Iterator j = entries.iterator(); j.hasNext(); )
                        {
                                PersonalStoreEntry entry=(PersonalStoreEntry)j.next();
                                if (entry.getType()==PersonalStoreEntry.CONTACT)
                                {
                                        PersonalContact contact=(PersonalContact)entry;
                                        String id=contact.getAddress(PersonalContact.IM);
                                        Debug.out.println(" watcher id : " + id);

                                        CollabPrincipal principal=_getSession().getPrincipal(id);
                                        principal.setStatus(CollabPrincipal.STATUS_WATCHED);
                                        watcherGroup.addContact(principal, false);
                                }
                        }
                        contactGroups.put(watcherGroup.getName(), watcherGroup);
                }
        }
        return watcherGroup;
         */
    }

    ////////////////////////////////////////////////////////////////////////////
    // Property change methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     *
     *
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }
}

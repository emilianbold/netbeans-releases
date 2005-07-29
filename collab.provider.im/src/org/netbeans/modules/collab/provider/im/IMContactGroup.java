/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.provider.im;

import com.sun.collablet.CollabException;
import com.sun.collablet.CollabPrincipal;
import com.sun.collablet.ContactGroup;

import org.openide.*;
import org.openide.util.*;

import java.beans.*;

import java.io.*;

import java.util.*;

import org.netbeans.lib.collab.*;

import org.netbeans.modules.collab.core.Debug;


/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class IMContactGroup extends Object implements ContactGroup {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private IMContactList contactList;
    private String name;
    private List contacts = new ArrayList();
    private Map contactsIndex = new HashMap();
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    /**
     *
     *
     */
    public IMContactGroup(IMContactList contactList, String name) {
        super();
        this.contactList = contactList;
        this.name = name;
    }

    /**
     *
     *
     */
    public IMContactList getContactList() // TAF: May change!
     {
        return contactList;
    }

    /**
     *
     *
     */
    public String getName() {
        return name;
    }

    /**
     * Removes this group and all its contacts from the persistent contact
     * list
     *
     */
    public void delete() throws CollabException {
        getContactList().removeContactGroup(this);
    }

    /**
     *
     *
     */
    public synchronized CollabPrincipal[] getContacts() {
        int size = contacts.size();

        return (CollabPrincipal[]) contacts.toArray(new CollabPrincipal[size]);
    }

    /*
     * (Inherits docs)
     *
     */
    public synchronized CollabPrincipal getContact(String identifier) {
        CollabPrincipal result = (CollabPrincipal) contactsIndex.get(identifier);

        return result;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Internal contact management methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public synchronized void addContact(CollabPrincipal contact)
    throws CollabException {
        try {
            IMCollabSession collabSession = (IMCollabSession) contactList.getCollabSession();
            PersonalStoreService personalStoreService = collabSession.getPersonalStoreService();
            PersonalStoreFolder personalStoreFolder = (PersonalStoreFolder) personalStoreService.getEntry(
                    PersonalStoreEntry.CONTACT_FOLDER, getName()
                );

            if (personalStoreFolder == null) {
                personalStoreFolder = (PersonalStoreFolder) personalStoreService.createEntry(
                        PersonalStoreEntry.CONTACT_FOLDER, getName()
                    );
            }

            PersonalContact entry = (PersonalContact) personalStoreService.getEntry(
                    PersonalStoreEntry.CONTACT, contact.getIdentifier()
                );

            if (entry == null) {
                Debug.out.println(" adding new entry to folder");
                entry = (PersonalContact) personalStoreService.createEntry(
                        PersonalStoreEntry.CONTACT, contact.getDisplayName()
                    );

                if (!contact.equals(collabSession.getUserPrincipal())) {
                    contact.subscribe();
                    contact.setStatus(CollabPrincipal.STATUS_PENDING);
                }
            }

            ((PersonalContact) entry).addAddress(PersonalContact.IM, contact.getIdentifier(), 0);
            entry.addToFolder(personalStoreFolder);
            entry.save();

            //			if (!contact.equals(collabSession.getUserPrincipal()))
            //			{
            //				contact.subscribe();
            //				contact.setStatus(CollabPrincipal.STATUS_PENDING);
            //			}
            contacts.add(contact);
            contactsIndex.put(contact.getIdentifier(), contact);
            changeSupport.firePropertyChange(PROP_CONTACTS, null, null);
        } catch (CollaborationException collbE) {
            throw new CollabException(collbE);
        }
    }

    public synchronized void addContact(CollabPrincipal contact, boolean flag)
    throws CollabException {
        if (!flag) // exists in server, only add to the contacts list
         {
            contacts.add(contact);
            contactsIndex.put(contact.getIdentifier(), contact);
            changeSupport.firePropertyChange(PROP_CONTACTS, null, null);
        } else // new contact
         {
            addContact(contact);
        }
    }

    /**
     *
     *
     */
    public synchronized void removeContact(CollabPrincipal contact)
    throws CollabException {
        try {
            IMCollabSession session = (IMCollabSession) contactList.getCollabSession();
            PersonalStoreService personalStoreService = session.getPersonalStoreService();
            PersonalStoreFolder personalStoreFolder = (PersonalStoreFolder) personalStoreService.getEntry(
                    PersonalStoreEntry.CONTACT_FOLDER, getName()
                );

            //			Debug.out.println(" folder name: " + personalStoreFolder.getDisplayName());
            PersonalStoreEntry entry = personalStoreService.getEntry(
                    PersonalStoreEntry.CONTACT, contact.getIdentifier()
                );

            if (entry != null) {
                Debug.out.println(" removing from folder");
                entry.removeFromFolder(personalStoreFolder);
                entry.save();

                /* Instruction from IM team, if this entry appears in only one
                 * folder and we just want to remove contact from one side, use
                 * unsubscribe(), otherwise server will remove current user from
                 * the other user's contact list as well per XMPP, two-way
                 * removal
                 */
                PersonalStoreEntry pse = personalStoreService.getEntry(
                        PersonalStoreEntry.CONTACT, contact.getIdentifier()
                    );

                if (pse == null) {
                    if (
                        (contact != session.getUserPrincipal()) &&
                            (contact.getStatus() != CollabPrincipal.STATUS_PENDING)
                    ) {
                        contact.unsubscribe();
                    }
                }
            }

            contacts.remove(contact);
            contactsIndex.remove(contact.getIdentifier());
            changeSupport.firePropertyChange(PROP_CONTACTS, null, null);
        } catch (CollaborationException collabE) {
            throw new CollabException(collabE);
        }
    }

    /**
     *
     *
     */
    protected void setName(String newName) {
        name = newName;
    }

    /**
     *
     *
     */
    public void rename(String newName) throws CollabException {
        getContactList().renameContactGroup(this, newName);
        setName(newName);
        changeSupport.firePropertyChange(PROP_CONTACTS, null, null);
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

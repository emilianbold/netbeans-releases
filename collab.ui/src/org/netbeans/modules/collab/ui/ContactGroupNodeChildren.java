/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.ui;

import java.beans.*;
import java.util.*;

import org.openide.nodes.*;
import org.openide.util.*;

import com.sun.collablet.CollabPrincipal;
import com.sun.collablet.CollabSession;
import com.sun.collablet.ContactGroup;
import org.netbeans.modules.collab.core.Debug;

/**
 *
 *
 * @author        Todd Fast, todd.fast@sun.com
 */
public class ContactGroupNodeChildren extends Children.Keys implements NodeListener, PropertyChangeListener {
    /**/

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private Collection keys;
    private CollabSession session;
    private ContactGroup group;

    /**
     *
     *
     */
    public ContactGroupNodeChildren(CollabSession session, ContactGroup group) {
        super();
        this.session = session;
        this.group = group;
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
    public ContactGroup getContactGroup() {
        return group;
    }

    /**
     *
     *
     */
    protected void addNotify() {
        //		getCollabSession().addPropertyChangeListener(this);
        getContactGroup().addPropertyChangeListener(this);
        refreshChildren();
    }

    /**
     *
     *
     */
    protected void removeNotify() {
        getContactGroup().removePropertyChangeListener(this);
    }

    /**
     *
     *
     */
    protected Node[] createNodes(Object key) {
        Node[] result = null;

        try {
            if (key instanceof Node) {
                result = new Node[] { (Node) key };
            } else {
                result = new Node[] { new ContactNode(getCollabSession(), getContactGroup(), (CollabPrincipal) key) };
            }
        } catch (Exception e) {
            Debug.debugNotify(e);
        }

        return result;
    }

    /**
     *
     *
     */
    public Collection getKeys() {
        return keys;
    }

    /**
     *
     *
     */
    public void _setKeys(Collection value) {
        keys = value;
        super.setKeys(value);
    }

    /**
     *
     *
     */
    public void refreshChildren() {
        List keys = new ArrayList();

        try {
            CollabPrincipal[] contacts = getContactGroup().getContacts();

            if ((contacts == null) || (contacts.length == 0)) {
                keys.add(
                    new MessageNode(
                        NbBundle.getMessage(ContactsNodeChildren.class, "LBL_ContactsNodeChildren_NoContacts")
                    )
                );
            } else {
                Arrays.sort(contacts, new ContactsComparator());
                keys.addAll(Arrays.asList(contacts));
            }

            _setKeys(keys);
        } catch (Exception e) {
            Debug.errorManager.notify(e);
        }
    }

    /**
     *
     *
     */
    public void propertyChange(PropertyChangeEvent event) {
        //Debug.out.println("Received property change event in group node children: "+event.getPropertyName());
        if (event.getSource() instanceof CollabSession) {
            if (CollabSession.PROP_VALID.equals(event.getPropertyName())) {
                // TODO: Is there any reason to listen to valid=true?
                // I don't think there is.
                if (event.getNewValue().equals(Boolean.FALSE)) {
                    _setKeys(Collections.EMPTY_SET);
                    getCollabSession().removePropertyChangeListener(this);
                    getContactGroup().removePropertyChangeListener(this);
                }
            }

            //			else
            //			if (CollabSession.PROP_CONTACT_GROUPS.equals(
            //				event.getPropertyName()))
            //			{
            //				refreshChildren();
            //			}
        } else if (event.getSource() instanceof ContactGroup) {
            if (ContactGroup.PROP_CONTACTS.equals(event.getPropertyName())) {
                refreshChildren();
            }
        }
    }

    /**
     *
     *
     */
    public void childrenAdded(NodeMemberEvent ev) {
        // Ignore
    }

    /**
     *
     *
     */
    public void childrenRemoved(NodeMemberEvent ev) {
        // Ignore
    }

    /**
     *
     *
     */
    public void childrenReordered(NodeReorderEvent ev) {
        // Ignore
    }

    /**
     *
     *
     */
    public void nodeDestroyed(NodeEvent ev) {
        refreshChildren();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    protected static class ContactsComparator extends Object implements Comparator {
        /**
         *
         *
         */
        public ContactsComparator() {
            super();
        }

        /**
         *
         *
         */
        public int compare(Object o1, Object o2) {
            if (o1 == o2) {
                return 0;
            }

            if (o1 == null) {
                return -1;
            }

            if (o2 == null) {
                return 1;
            }

            String s1 = ((CollabPrincipal) o1).getDisplayName();
            String s2 = ((CollabPrincipal) o2).getDisplayName();

            if (s1 == null) {
                s1 = "";
            }

            if (s2 == null) {
                s2 = "";
            }

            return s1.compareTo(s2);
        }
    }
}

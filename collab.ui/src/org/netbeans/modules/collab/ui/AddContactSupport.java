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
package org.netbeans.modules.collab.ui;

import com.sun.collablet.CollabSession;
import com.sun.collablet.ContactGroup;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;

import java.io.IOException;

import org.netbeans.modules.collab.*;


public class AddContactSupport implements AddContactCookie {
    CollabSession _session;
    ContactGroup _group = null;

    public AddContactSupport(CollabSession session) {
        _session = session;
    }

    public AddContactSupport(CollabSession session, ContactGroup group) {
        _session = session;
        _group = group;
    }

    public void addContact() {
        AddContactForm form = new AddContactForm(_session, _group);
        form.addContacts();
    }

    public void addContactGroup() {
        AddContactGroupForm form = new AddContactGroupForm(_session);
        form.addContactGroup();
    }
}

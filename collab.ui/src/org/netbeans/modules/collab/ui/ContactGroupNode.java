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

import com.sun.collablet.CollabException;
import com.sun.collablet.CollabManager;
import com.sun.collablet.CollabPrincipal;
import com.sun.collablet.CollabSession;
import com.sun.collablet.ContactGroup;

import org.openide.*;
import org.openide.actions.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.util.datatransfer.*;
import org.openide.util.datatransfer.NewType;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.DnDConstants;
import java.awt.event.*;

import java.beans.*;

import java.io.*;

import java.lang.reflect.*;

import java.util.*;
import java.util.List;

import javax.swing.*;

import org.netbeans.modules.collab.*;
import org.netbeans.modules.collab.core.Debug;
import org.netbeans.modules.collab.ui.CollabSessionCookie;
import org.netbeans.modules.collab.ui.ContactCookie;
import org.netbeans.modules.collab.ui.ContactGroupCookie;
import org.netbeans.modules.collab.ui.actions.*;


public class ContactGroupNode extends AbstractNode implements CollabSessionCookie, ContactGroupCookie {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    public static final String ICON_BASE = "org/netbeans/modules/collab/ui/resources/group_png"; // NOI18N
    private static final SystemAction[] DEFAULT_ACTIONS = new SystemAction[] {
            SystemAction.get(CreateConversationAction.class), null, SystemAction.get(AddContactAction.class),
            SystemAction.get(RenameAction.class), null, SystemAction.get(PasteAction.class),
            SystemAction.get(DeleteAction.class)
        };

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private CollabSession session;
    private ContactGroup group;
    private Image icon;

    /**
     *
     *
     */
    public ContactGroupNode(CollabSession session, ContactGroup group) {
        super(createChildren(session, group));
        this.session = session;
        this.group = group;
        setName(group.getName());

        //		setIconBase(ICON_BASE);
        systemActions = DEFAULT_ACTIONS;

        // Add cookies
        getCookieSet().add(this);
    }

    /**
     *
     *
     */
    protected static ContactGroupNodeChildren createChildren(CollabSession session, ContactGroup group) {
        return new ContactGroupNodeChildren(session, group);
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
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ContactGroupNode.class);
    }

    /**
     *
     *
     */
    public boolean canCut() {
        return false;
    }

    /**
     *
     *
     */
    public boolean canCopy() {
        return false;
    }

    /**
     *
     *
     */
    public boolean canDestroy() {
        return true;
    }

    /**
     *
     *
     */
    public boolean canRename() {
        return true;
    }

    /**
     *
     *
     */
    public void destroy() throws IOException {
        super.destroy();

        try {
            if (sessionExists()) {
                getContactGroup().delete();
            }
        } catch (CollabException ce) {
            Debug.debugNotify(ce);
        }
    }

    /**
     *
     *
     */
    private boolean sessionExists() {
        CollabSession[] sessions = CollabManager.getDefault().getSessions();

        for (int i = 0; i < sessions.length; i++) {
            if (sessions[i].equals(getSession())) {
                return true;
            }
        }

        return false;
    }

    /**
     *
     *
     */
    public Image getIcon(int type) {
        if (icon == null) {
            Image image = (Image) UIManager.get("Nb.Explorer.Folder.icon"); // NOI18N

            if (image != null) {
                icon = image;
            } else {
                setIconBase(ICON_BASE);
                icon = super.getIcon(type);
            }
        }

        return icon;
    }

    /**
     *
     *
     */
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    /**
     *
     *
     */
    public void setName(String name) {
        if (!group.getName().equals(name)) {
            if (session.getContactGroup(name) != null) {
                // Notify user that group already exists
                String message = NbBundle.getMessage(
                        ContactGroupNode.class, "MSG_AddContactGroupForm_GroupAlreadyExists", // NOI18N
                        name
                    );
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message));
            } else {
                try {
                    group.rename(name);
                    super.setName(name);
                } catch (CollabException e) {
                    Debug.debugNotify(e);
                }
            }
        } else {
            super.setName(name);
        }
    }

    /**
     *
     *
     */
    protected void createPasteTypes(Transferable t, List list) {
        boolean cut = false;
        Node[] ns = NodeTransfer.nodes(t, NodeTransfer.CLIPBOARD_COPY);

        if (ns == null) {
            ns = NodeTransfer.nodes(t, NodeTransfer.DND_COPY);
        }

        if (ns == null) {
            ns = NodeTransfer.nodes(t, NodeTransfer.DND_MOVE);
            cut = (ns == null) ? false : true;
        }

        if (ns == null) {
            ns = NodeTransfer.nodes(t, NodeTransfer.CLIPBOARD_CUT);
            cut = (ns == null) ? false : true;
        }

        final Node[] nodes = ns;
        final boolean clipboardCut = cut;

        if (nodes != null) {
            list.add(
                new PasteType() {
                    public Transferable paste() throws IOException {
                        Node[] node = new Node[nodes.length];

                        for (int i = 0; i < node.length; i++) {
                            node[i] = nodes[i].cloneNode();
                        }

                        ContactCookie contactCookie = null;

                        for (int i = 0; i < node.length; i++) {
                            contactCookie = (ContactCookie) node[i].getCookie(ContactCookie.class);

                            if (contactCookie == null) {
                                continue;
                            }

                            CollabPrincipal contact = contactCookie.getContact();

                            try {
                                if (ContactGroupNode.this.getContactGroup().getContact(contact.getIdentifier()) == null) {
                                    ContactGroupNode.this.getContactGroup().addContact(contact);

                                    if (clipboardCut) {
                                        // TODO: to remove node from the original group
                                        node[i].destroy();
                                    }
                                }
                            } catch (CollabException ce) {
                                // TODO
                            }
                        }

                        return null;
                    }
                }
            );
        }

        // Also try superclass, but give it lower priority:
        super.createPasteTypes(t, list);
    }

    /**
     *
     *
     */
    public ContactGroupNodeChildren getContactGroupNodeChildren() {
        return (ContactGroupNodeChildren) getChildren();
    }

    public CollabSession getSession() {
        return session;
    }
}

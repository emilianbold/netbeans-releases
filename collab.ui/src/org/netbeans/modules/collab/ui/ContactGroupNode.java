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
package org.netbeans.modules.collab.ui;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.List;
import javax.swing.*;

import org.openide.*;
import org.openide.actions.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.PasteType;

import com.sun.collablet.*;
import org.netbeans.modules.collab.core.Debug;
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

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
import java.beans.*;
import java.io.IOException;

import org.openide.actions.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;

import com.sun.collablet.*;
import org.netbeans.modules.collab.core.Debug;
import org.netbeans.modules.collab.ui.actions.*;

/**
 *
 *
 * @author        Todd Fast, todd.fast@sun.com
 */
public class ContactNode extends AbstractNode implements CollabSessionCookie, ContactCookie, PropertyChangeListener {
    //	/**
    //	 *
    //	 *
    //	 */
    //	public Sheet createSheet()
    //	{
    //		Sheet sheet=Sheet.createDefault();
    //		Sheet.Set propertiesSet=sheet.get(Sheet.PROPERTIES);
    //
    //		propertiesSet.put(new EnableCollabServerProperty());
    //
    //		return sheet;
    //	}
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    public static final String USER_ICON = "org/netbeans/modules/collab/ui/resources/empty.gif"; // NOI18N

    //		"org/netbeans/modules/collab/ui/resources/user_png.gif"; // NOI18N
    public static final String OPEN_ICON = "org/netbeans/modules/collab/ui/resources/online_png.gif"; // NOI18N
    public static final String CLOSED_ICON = "org/netbeans/modules/collab/ui/resources/offline_png.gif"; // NOI18N
    public static final String AWAY_ICON = "org/netbeans/modules/collab/ui/resources/away_png.gif"; // NOI18N
    public static final String CHAT_ICON = "org/netbeans/modules/collab/ui/resources/chat_png.gif"; // NOI18N
    public static final String BUSY_ICON = "org/netbeans/modules/collab/ui/resources/busy_png.gif"; // NOI18N
    public static final String PENDING_ICON = "org/netbeans/modules/collab/ui/resources/pending_png.gif"; // NOI18N
    public static final String IDLE_ICON = "org/netbeans/modules/collab/ui/resources/idle_png.gif"; // NOI18N
    public static final String WATCHED_ICON = "org/netbeans/modules/collab/ui/resources/watched_png.gif"; // NOI18N
    private static final SystemAction[] DEFAULT_ACTIONS = new SystemAction[] {
            SystemAction.get(CreateConversationAction.class), null, SystemAction.get(SubscribeAction.class), 
            //	SystemAction.get(UnsubscribeAction.class),
            null, SystemAction.get(CutAction.class), SystemAction.get(CopyAction.class),
            SystemAction.get(DeleteAction.class)
        };

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private CollabSession session;
    private CollabPrincipal contact;
    private ContactGroup group;
    private Image currentIcon = null;
    private Image baseIcon = ImageUtilities.loadImage(USER_ICON);
    private Image openIcon = ImageUtilities.loadImage(OPEN_ICON);
    private Image closedIcon = ImageUtilities.loadImage(CLOSED_ICON);
    private Image awayIcon = ImageUtilities.loadImage(AWAY_ICON);
    private Image chatIcon = ImageUtilities.loadImage(CHAT_ICON);
    private Image busyIcon = ImageUtilities.loadImage(BUSY_ICON);
    private Image pendingIcon = ImageUtilities.loadImage(PENDING_ICON);
    private Image idleIcon = ImageUtilities.loadImage(IDLE_ICON);
    private Image watchedIcon = ImageUtilities.loadImage(WATCHED_ICON);

    /**
     *
     *
     */
    public ContactNode(CollabSession session, ContactGroup group, CollabPrincipal contact)
    throws IntrospectionException {
        super(Children.LEAF);
        this.session = session;
        this.group = group;
        this.contact = contact;

        setName(contact.getDisplayName());
        updateStatus();

        systemActions = DEFAULT_ACTIONS;
        setDefaultAction(DEFAULT_ACTIONS[0]);

        // Add self as cookie
        getCookieSet().add(this);

        contact.addPropertyChangeListener(this);
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
    public CollabPrincipal getContact() {
        return contact;
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
        return new HelpCtx(ContactNode.class);
    }

    /**
     *
     *
     */
    public boolean canCut() {
        return true;
    }

    /**
     *
     *
     */
    public boolean canCopy() {
        return true;
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
        return false;
    }

    /**
     *
     *
     */
    public void destroy() throws IOException {
        super.destroy();

        //getContactGroup().removeContact(getContact());
        try {
            if (sessionExists()) {
                getContactGroup().removeContact(getContact());
            }
        } catch (CollabException e) {
            // TODO: Nice error message
            Debug.errorManager.notify(e);
        }
    }

    /**
     *
     *
     */
    private boolean sessionExists() {
        CollabSession[] sessions = CollabManager.getDefault().getSessions();

        for (int i = 0; i < sessions.length; i++) {
            if (sessions[i].equals(getCollabSession())) {
                return true;
            }
        }

        return false;
    }

    /**
     *
     *
     */
    public Image getIcon(int kind) {
        return currentIcon;
    }

    /**
     *
     *
     */
    protected void updateStatus() {
        int status = getContact().getStatus();
        Image statusIcon = null;
        String description = null;

        switch (status) {
        case CollabPrincipal.STATUS_AWAY:
            statusIcon = awayIcon;
            description = NbBundle.getMessage(ContactNode.class, "LBL_ContactNode_StatusAway"); // NOI18N

            break;

        case CollabPrincipal.STATUS_BUSY:
            statusIcon = busyIcon;
            description = NbBundle.getMessage(ContactNode.class, "LBL_ContactNode_StatusBusy"); // NOI18N

            break;

        case CollabPrincipal.STATUS_CHAT:
            statusIcon = chatIcon;
            description = NbBundle.getMessage(ContactNode.class, "LBL_ContactNode_StatusChat"); // NOI18N

            break;

        case CollabPrincipal.STATUS_IDLE:
            statusIcon = idleIcon;
            description = NbBundle.getMessage(ContactNode.class, "LBL_ContactNode_StatusIdle"); // NOI18N

            break;

        case CollabPrincipal.STATUS_OFFLINE:
            statusIcon = closedIcon;
            description = NbBundle.getMessage(ContactNode.class, "LBL_ContactNode_StatusOffline"); // NOI18N

            break;

        case CollabPrincipal.STATUS_ONLINE:
            statusIcon = openIcon;
            description = NbBundle.getMessage(ContactNode.class, "LBL_ContactNode_StatusOnline"); // NOI18N

            break;

        case CollabPrincipal.STATUS_PENDING:
            statusIcon = pendingIcon;
            description = NbBundle.getMessage(ContactNode.class, "LBL_ContactNode_StatusPending"); // NOI18N

            break;

        case CollabPrincipal.STATUS_WATCHED:
            statusIcon = watchedIcon;
            description = NbBundle.getMessage(ContactNode.class, "LBL_ContactNode_StatusWatched"); // NOI18N

            break;

        default:
            statusIcon = closedIcon;
            description = NbBundle.getMessage(ContactNode.class, "LBL_ContactNode_StatusUnknown"); // NOI18N
        }

        setShortDescription(description);

        //		currentIcon=Utilities.mergeImages(statusIcon,baseIcon,16,0);
        currentIcon = ImageUtilities.mergeImages(baseIcon, statusIcon, 0, 0);
        fireIconChange();
    }

    /**
     *
     *
     */
    public void notifyStatusChange() {
        int status = getContact().getStatus();
        int userStatus = -1;

        switch (status) {
        case CollabPrincipal.STATUS_ONLINE:
            userStatus = UserInterface.NOTIFY_USER_STATUS_ONLINE;

            break;

        case CollabPrincipal.STATUS_OFFLINE:
            userStatus = UserInterface.NOTIFY_USER_STATUS_OFFLINE;

            break;

        case CollabPrincipal.STATUS_AWAY:
            userStatus = UserInterface.NOTIFY_USER_STATUS_AWAY;

            break;

        case CollabPrincipal.STATUS_IDLE:
            userStatus = UserInterface.NOTIFY_USER_STATUS_IDLE;

            break;

        case CollabPrincipal.STATUS_WATCHED:
            userStatus = UserInterface.NOTIFY_USER_STATUS_WATCHED;

            break;

        case CollabPrincipal.STATUS_PENDING:
            userStatus = UserInterface.NOTIFY_USER_STATUS_PENDING;

            break;

        case CollabPrincipal.STATUS_CHAT:
            userStatus = UserInterface.NOTIFY_USER_STATUS_CHAT;

            break;

        case CollabPrincipal.STATUS_BUSY:
            userStatus = UserInterface.NOTIFY_USER_STATUS_BUSY;

            break;
        }

        getCollabSession().getManager().getUserInterface().notifyStatusChange(
            getCollabSession(), getContact(), userStatus
        );
    }

    /**
     *
     *
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getSource() instanceof CollabPrincipal) {
            if (event.getPropertyName().equals(CollabPrincipal.PROP_STATUS)) {
                if (!event.getNewValue().equals(event.getOldValue())) {
                    updateStatus();

                    if (((Integer) event.getOldValue()).intValue() != CollabPrincipal.STATUS_UNKNOWN) {
                        notifyStatusChange();
                    }
                }
            }
        }
    }
}

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
import com.sun.collablet.UserInterface;

import org.openide.*;
import org.openide.actions.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.util.datatransfer.NewType;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;

import java.beans.*;

import java.io.*;

import java.lang.reflect.*;

import java.util.*;

import javax.swing.*;

import org.netbeans.modules.collab.*;
import org.netbeans.modules.collab.core.Debug;
import org.netbeans.modules.collab.ui.CollabSessionCookie;
import org.netbeans.modules.collab.ui.ContactCookie;
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
    private Image baseIcon = Utilities.loadImage(USER_ICON);
    private Image openIcon = Utilities.loadImage(OPEN_ICON);
    private Image closedIcon = Utilities.loadImage(CLOSED_ICON);
    private Image awayIcon = Utilities.loadImage(AWAY_ICON);
    private Image chatIcon = Utilities.loadImage(CHAT_ICON);
    private Image busyIcon = Utilities.loadImage(BUSY_ICON);
    private Image pendingIcon = Utilities.loadImage(PENDING_ICON);
    private Image idleIcon = Utilities.loadImage(IDLE_ICON);
    private Image watchedIcon = Utilities.loadImage(WATCHED_ICON);

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
        currentIcon = Utilities.mergeImages(baseIcon, statusIcon, 0, 0);
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

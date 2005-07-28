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

import com.sun.collablet.CollabPrincipal;
import com.sun.collablet.Conversation;

import org.openide.*;
import org.openide.actions.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.util.datatransfer.NewType;

import java.awt.*;
import java.awt.event.*;

import java.beans.*;

import java.io.*;

import java.lang.reflect.*;

import java.util.*;

import javax.swing.*;

import org.netbeans.modules.collab.*;
import org.netbeans.modules.collab.ui.ContactCookie;
import org.netbeans.modules.collab.ui.actions.*;


/**
 *
 *
 * @author        Todd Fast, todd.fast@sun.com
 */
public class ParticipantNode extends AbstractNode implements ContactCookie, PropertyChangeListener {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    public static final String EMPTY_ICON = "org/netbeans/modules/collab/ui/resources/empty8.gif"; // NOI18N
    public static final String USER_ICON = 
        //		"org/netbeans/modules/collab/ui/resources/user_png.gif"; // NOI18N
        EMPTY_ICON; // NOI18N
    public static final String OPEN_ICON = "org/netbeans/modules/collab/ui/resources/online_png.gif"; // NOI18N
    public static final String CLOSED_ICON = "org/netbeans/modules/collab/ui/resources/offline_png.gif"; // NOI18N
    public static final String AWAY_ICON = "org/netbeans/modules/collab/ui/resources/away_png.gif"; // NOI18N
    public static final String CHAT_ICON = "org/netbeans/modules/collab/ui/resources/chat_png.gif"; // NOI18N
    public static final String BUSY_ICON = "org/netbeans/modules/collab/ui/resources/busy_png.gif"; // NOI18N
    public static final String PENDING_ICON = "org/netbeans/modules/collab/ui/resources/pending_png.gif"; // NOI18N
    public static final String IDLE_ICON = "org/netbeans/modules/collab/ui/resources/idle_png.gif"; // NOI18N
    public static final String WATCHED_ICON = "org/netbeans/modules/collab/ui/resources/watched_png.gif"; // NOI18N
    public static final String TYPING_ON_ICON = "org/netbeans/modules/collab/ui/resources/typing_on_png.gif"; // NOI18N
    public static final String TYPING_OFF_ICON = "org/netbeans/modules/collab/ui/resources/empty8.gif"; // NOI18N
    private static final SystemAction[] DEFAULT_ACTIONS = new SystemAction[] {  };

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private CollabPrincipal contact;
    private Conversation conversation;
    private Image currentIcon;
    private Image currentAnnotatedIcon;
    private Image baseIcon = Utilities.loadImage(USER_ICON);
    private Image emptyIcon = Utilities.loadImage(EMPTY_ICON);
    private Image openIcon = Utilities.loadImage(OPEN_ICON);
    private Image closedIcon = Utilities.loadImage(CLOSED_ICON);
    private Image awayIcon = Utilities.loadImage(AWAY_ICON);
    private Image chatIcon = Utilities.loadImage(CHAT_ICON);
    private Image busyIcon = Utilities.loadImage(BUSY_ICON);
    private Image pendingIcon = Utilities.loadImage(PENDING_ICON);
    private Image idleIcon = Utilities.loadImage(IDLE_ICON);
    private Image watchedIcon = Utilities.loadImage(WATCHED_ICON);
    private Image typingOnIcon = Utilities.loadImage(TYPING_ON_ICON);
    private Image typingOffIcon = Utilities.loadImage(TYPING_OFF_ICON);

    /**
     *
     *
     */
    public ParticipantNode(Conversation conversation, CollabPrincipal contact) {
        super(Children.LEAF);
        this.conversation = conversation;
        this.contact = contact;

        setName(contact.getDisplayName());
        setShortDescription(contact.getIdentifier());

        updateStatus();

        systemActions = DEFAULT_ACTIONS;

        // Add self as cookie
        getCookieSet().add(this);

        contact.addPropertyChangeListener(this);
        conversation.addPropertyChangeListener(this);
    }

    /**
     *
     *
     */
    public Conversation getConversation() {
        return conversation;
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
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ContactNode.class);
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
        return false;
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
    public Image getIcon(int kind) {
        if (currentAnnotatedIcon != null) {
            return currentAnnotatedIcon;
        } else {
            return currentIcon;
        }
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

        //		currentIcon=Utilities.mergeImages(baseIcon,statusIcon,16,0);
        currentIcon = Utilities.mergeImages(baseIcon, statusIcon, 0, 0);
        currentIcon = Utilities.mergeImages(currentIcon, typingOffIcon, 8, 0);
        fireIconChange();
    }

    /**
     *
     *
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getSource() instanceof CollabPrincipal) {
            if (event.getPropertyName().equals(CollabPrincipal.PROP_STATUS)) {
                updateStatus();
            }
        } else if (event.getSource() instanceof Conversation) {
            if (event.getPropertyName().equals(Conversation.USER_TYPING_ON)) {
                CollabPrincipal principal = (CollabPrincipal) event.getNewValue();
                updateTypingStatus(principal, true);
            } else if (event.getPropertyName().equals(Conversation.USER_TYPING_OFF)) {
                CollabPrincipal principal = (CollabPrincipal) event.getNewValue();
                updateTypingStatus(principal, false);
            }
        }
    }

    /**
     *
     *
     */
    private void updateTypingStatus(CollabPrincipal principal, boolean status) {
        if (principal != getContact()) {
            return;
        }

        if (status) {
            currentAnnotatedIcon = Utilities.mergeImages(currentIcon, typingOnIcon, 11, 0);
        } else {
            currentAnnotatedIcon = null;
        }

        fireIconChange();
    }
}

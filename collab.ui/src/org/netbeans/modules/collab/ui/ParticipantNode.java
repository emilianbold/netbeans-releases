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
import java.beans.*;

import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;

import com.sun.collablet.CollabPrincipal;
import com.sun.collablet.Conversation;

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

        contact.addPropertyChangeListener(WeakListeners.propertyChange(this, contact));
        conversation.addPropertyChangeListener(WeakListeners.propertyChange(this, conversation));
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

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
import java.io.*;

import org.openide.actions.OpenAction;
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
public class PublicConversationNode extends AbstractNode implements CollabSessionCookie, ConversationCookie,
    PropertyChangeListener {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    public static final String PUBLIC_CONVERSATION_ICON = "org/netbeans/modules/collab/ui/resources/public_conversation_png"; // NOI18N
    public static final String PUBLIC_CONVERSATION_ACTIVE_ICON = "org/netbeans/modules/collab/ui/resources/public_conversation_active_png"; // NOI18N
    public static final String NOTIFY_ICON_BASE = "org/netbeans/modules/collab/ui/resources/conversation_notify_png"; // NOI18N
    private static final SystemAction[] DEFAULT_ACTIONS = new SystemAction[] {
            SystemAction.get(OpenAction.class), SystemAction.get(LeaveConversationAction.class),
            SystemAction.get(UnsubscribePublicConversationAction.class), null,
            SystemAction.get(ManagePublicConversationAction.class), null,
            SystemAction.get(DeletePublicConversationAction.class),
        };

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private Conversation conversation;
    private CollabSession session;
    private String name;
    private String defaultIconResource;

    /**
     *
     *
     */
    public PublicConversationNode(CollabSession session, String name) {
        super(Children.LEAF);
        this.session = session;
        this.name = name;

        String displayName = name.substring(0, name.indexOf("@"));
        setDisplayName(displayName);

        //setName(displayName);
        setName(name);
        defaultIconResource = PUBLIC_CONVERSATION_ICON;
        setIconBase(defaultIconResource);

        systemActions = DEFAULT_ACTIONS;
        setDefaultAction(DEFAULT_ACTIONS[0]);

        session.addPropertyChangeListener(WeakListeners.propertyChange(this, session));

        getCookieSet().add(this);
        getCookieSet().add(new PublicConversationOpenSupport(this, session, name));
    }

    /**
     *
     *
     */
    public CollabSession getCollabSession() {
        return this.session;
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
    public String getConversationName() {
        return name;
    }

    /**
     *
     *
     */
    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
        setIconBase(PUBLIC_CONVERSATION_ACTIVE_ICON);
        conversation.addPropertyChangeListener(WeakListeners.propertyChange(this, conversation));

        // Register this node as the UI object for the conversation
        CollabManager.getDefault().getUserInterface().registerConversationUI(conversation, this);
    }

    /**
     *
     *
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ConversationNode.class);
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
        return false;
    }

    /**
     *
     *
     */
    public void destroy() throws IOException {
        super.destroy();

        try {
            getCollabSession().unsubscribePublicConversation(name);
        } catch (CollabException e) {
            Debug.errorManager.notify(e);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // PropertyChangeListener methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getSource() instanceof Conversation) {
            if (event.getPropertyName().equals(Conversation.PROP_VALID)) {
                if (event.getNewValue().equals(Boolean.FALSE)) {
                    setIconBase(PUBLIC_CONVERSATION_ICON);
                } else {
                    setIconBase(PUBLIC_CONVERSATION_ACTIVE_ICON);
                }
            }
        } else if (event.getSource() instanceof CollabSession) {
            if (event.getPropertyName().equals(CollabSession.PROP_PUBLIC_CONVERSATIONS)) {
                Conversation conv = (Conversation) event.getNewValue();

                if ((conv != null) && conv.getIdentifier().equals(getConversationName())) {
                    setConversation((Conversation) event.getNewValue());
                }
            }
        }
    }

    /**
     *
     *
     */
    public void notify(boolean value) {
        if (value) {
            setIconBase(NOTIFY_ICON_BASE);
        } else {
            setIconBase(PUBLIC_CONVERSATION_ACTIVE_ICON);
        }
    }
}

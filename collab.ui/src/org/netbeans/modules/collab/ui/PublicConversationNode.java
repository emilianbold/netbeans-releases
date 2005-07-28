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
import com.sun.collablet.CollabSession;
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

import org.netbeans.modules.collab.*;
import org.netbeans.modules.collab.core.Debug;
import org.netbeans.modules.collab.ui.*;
import org.netbeans.modules.collab.ui.CollabSessionCookie;
import org.netbeans.modules.collab.ui.ConversationCookie;
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

        session.addPropertyChangeListener(this);

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
        conversation.addPropertyChangeListener(this);

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

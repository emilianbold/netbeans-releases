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
import java.io.IOException;

import com.sun.collablet.CollabManager;
import com.sun.collablet.CollabSession;
import com.sun.collablet.Conversation;
import org.openide.actions.OpenAction;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.collab.ui.actions.LeaveConversationAction;

/**
 *
 *
 * @author        Todd Fast, todd.fast@sun.com
 */
public class ConversationNode extends AbstractNode implements CollabSessionCookie, ConversationCookie,
    PropertyChangeListener {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    //	public static final String ICON_BASE=
    //		"org/netbeans/modules/collab/ui/resources/conversation_png"; // NOI18N
    public static final String NOTIFY_ICON_BASE = "org/netbeans/modules/collab/ui/resources/conversation_notify_png"; // NOI18N
    public static final String PRIVATE_CONVERSATION_ICON = "org/netbeans/modules/collab/ui/resources/chat_png"; // NOI18N
    public static final String PUBLIC_CONVERSATION_ACTIVE_ICON = "org/netbeans/modules/collab/ui/resources/public_conversation_active_png"; // NOI18N
    private static final SystemAction[] DEFAULT_ACTIONS = new SystemAction[] {
            SystemAction.get(OpenAction.class), SystemAction.get(LeaveConversationAction.class)
        //SystemAction.get(UnsubscribeConversationAction.class)
        //SystemAction.get(DeleteAction.class)
        };

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private Conversation conversation;
    private String defaultIconResource;

    /**
     *
     *
     */
    public ConversationNode(Conversation conversation) {
        super(Children.LEAF);
        this.conversation = conversation;

        setName(conversation.getDisplayName());

        defaultIconResource = PRIVATE_CONVERSATION_ICON;

        if (conversation.isPublic()) {
            defaultIconResource = PUBLIC_CONVERSATION_ACTIVE_ICON;
        } else {
            defaultIconResource = PRIVATE_CONVERSATION_ICON;
        }

        setIconBase(defaultIconResource);

        systemActions = DEFAULT_ACTIONS;
        setDefaultAction(DEFAULT_ACTIONS[0]);

        // Add the open cookie
        getCookieSet().add(this);
        getCookieSet().add(new ConversationOpenSupport(this, conversation));

        conversation.addPropertyChangeListener(this);

        // Register this node as the UI object for the conversation
        CollabManager.getDefault().getUserInterface().registerConversationUI(conversation, this);
    }

    /**
     *
     *
     */
    public CollabSession getCollabSession() {
        return getConversation().getCollabSession();
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
        getConversation().leave();
    }

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

    /**
     *
     *
     */
    public void notify(boolean value) {
        if (value) {
            setIconBase(NOTIFY_ICON_BASE);
        } else {
            setIconBase(defaultIconResource);
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
            if (event.getPropertyName().equals(Conversation.PROP_PARTICIPANTS)) {
                //				// Update the name of the conversation based on participants
                //				StringBuffer name=new StringBuffer();
                //
                //				CollabPrincipal self=
                //					getConversation().getCollabSession().getUserPrincipal();
                //				CollabPrincipal[] participants=
                //					getConversation().getParticipants();
                //				int numAdded=0;
                //				for (int i=0; i<participants.length; i++)
                //				{
                //					// Don't include self in the list of participants
                //					if (participants[i].equals(self))
                //						continue;
                //
                //					if (numAdded++>0)
                //						name.append(", "); // NOI18N
                //
                //					name.append(participants[i].getDisplayName());
                //				}
                //
                //				setName(name.toString());
                setName(getConversation().getDisplayName());
            }
        }
    }
}

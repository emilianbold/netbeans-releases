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

import org.openide.cookies.OpenCookie;
import org.openide.util.Mutex;
import org.openide.windows.*;

import com.sun.collablet.*;
import org.netbeans.modules.collab.core.Debug;

/**
 *
 * @author  todd
 */
public class PublicConversationOpenSupport extends CloneableOpenSupport implements OpenCookie {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private PublicConversationNode node;
    private Conversation conversation = null;
    private CollabSession session;
    private String name;
    private ConversationComponent component;

    /**
     * Creates an instance with a default Env
     *
     */
    public PublicConversationOpenSupport(PublicConversationNode node, CollabSession session, String name) {
        this(node, session, name, new PublicConversationOpenSupportEnv());

        // Register this instance with the Env
        ((PublicConversationOpenSupportEnv) env).registerCloneableOpenSupport(this);
    }

    /**
     * Creates an instance with the specified Env.  The Env must be implemented
     * to return this instance from its <code>findCloneableOpenSupport()</code>
     * method.
     *
     */
    protected PublicConversationOpenSupport(
        PublicConversationNode node, CollabSession session, String name, CloneableOpenSupport.Env env
    ) {
        super(env);
        this.node = node;
        this.session = session;
        this.name = name;
    }

    /**
     *
     *
     */
    public PublicConversationNode getNode() {
        return node;
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
    public CollabSession getCollabSession() {
        return session;
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
    public void open() {
        if ((getConversation() != null) && getConversation().isValid() && (component != null)) {
            // Reset the component's reference, which was unset when
            // the component was closed
            component.setReference(allEditors);

            // This code was effectively copied from CloneableOpenSupport
            // Bugfix #10688 open() is now run in AWT thread
            Mutex.EVENT.writeAccess(
                new Runnable() {
                    public void run() {
                        // Open it and request active
                        component.open();
                        component.requestActive();
                    }
                }
            );
        } else {
            component = null;

            try {
                String[] conv = getCollabSession().findPublicConversations(
                        CollabSession.SEARCHTYPE_EQUALS, getConversationName()
                    );

                if ((conv != null) && (conv.length > 0)) {
                    super.open();
                } else {
                    getCollabSession().unsubscribePublicConversation(getConversationName());
                    getCollabSession().getManager().getUserInterface().notifyPublicConversationDeleted(
                        getConversationName()
                    );
                }
            } catch (CollabException ce) {
                // do nothing
            }
        }
    }

    /**
     *
     *
     */
    protected CloneableTopComponent createCloneableTopComponent() {
        // join public conversation and initialize channel
        if (getConversation() == null) {
            try {
                conversation = getCollabSession().createPublicConversation(getConversationName());

                conversation.addPropertyChangeListener(((PublicConversationOpenSupportEnv) env));

                getNode().setConversation(conversation);
            } catch (CollabException e) {
                Debug.errorManager.notify(e);
            }
        }

        component = new ConversationComponent(getNode(), getConversation());

        return component;
    }

    /**
     *
     *
     */
    protected String messageOpened() {
        return "";
    }

    /**
     *
     *
     */
    protected String messageOpening() {
        return "";
    }
}

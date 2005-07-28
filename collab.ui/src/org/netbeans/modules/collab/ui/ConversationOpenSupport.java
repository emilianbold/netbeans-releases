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

import com.sun.collablet.Conversation;

import org.openide.*;
import org.openide.cookies.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.windows.*;

import java.beans.*;

import java.io.*;

import java.util.*;

import org.netbeans.modules.collab.*;


/**
 *
 * @author  todd
 */
public class ConversationOpenSupport extends CloneableOpenSupport implements OpenCookie {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private Node node;
    private Conversation conversation;
    private ConversationComponent component;

    /**
     * Creates an instance with a default Env
     *
     */
    public ConversationOpenSupport(Node node, Conversation conversation) {
        this(node, conversation, new ConversationOpenSupportEnv(conversation));

        // Register this instance with the Env
        ((ConversationOpenSupportEnv) env).registerCloneableOpenSupport(this);
    }

    /**
     * Creates an instance with the specified Env.  The Env must be implemented
     * to return this instance from its <code>findCloneableOpenSupport()</code>
     * method.
     *
     */
    protected ConversationOpenSupport(Node node, Conversation conversation, CloneableOpenSupport.Env env) {
        super(env);
        this.node = node;
        this.conversation = conversation;
    }

    /**
     *
     *
     */
    public Node getNode() {
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
    public void open() {
        if (conversation.isValid() && (component != null)) {
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
            super.open();
        }
    }

    /**
     *
     *
     */
    protected CloneableTopComponent createCloneableTopComponent() {
        //		// join public conversation and initialize channel again
        //		if (getConversation().isPublic() && 
        //			(!getConversation().isValid() || 
        //			getConversation().getChannels().length ==0))
        //		{
        //			try {
        //				conversation.join();
        //			}catch (CollabException e)
        //			{
        //				Debug.errorManager.notify(e);
        //			}
        //		}
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

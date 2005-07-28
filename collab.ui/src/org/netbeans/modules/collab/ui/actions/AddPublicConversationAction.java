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
package org.netbeans.modules.collab.ui.actions;

import com.sun.collablet.CollabManager;
import com.sun.collablet.CollabSession;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;

import java.awt.event.*;

import java.io.*;

import java.util.*;

import org.netbeans.modules.collab.*;
import org.netbeans.modules.collab.ui.*;
import org.netbeans.modules.collab.ui.CollabSessionCookie;


/**
 *
 *
 */
public class AddPublicConversationAction extends CookieAction {
    /**
     *
     *
     */
    public String getName() {
        return NbBundle.getMessage(CreateConversationAction.class, "LBL_AddPublicConversationAction_Name"); // NOI18N
    }

    /**
     *
     *
     */
    protected String iconResource() {
        return "org/netbeans/modules/collab/ui/resources/" + // NOI18N
        "public_conversation_active_png.gif"; // NOI18N
    }

    /**
     *
     *
     */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     *
     *
     */
    protected Class[] cookieClasses() {
        return new Class[] { CollabSessionCookie.class };
    }

    /**
     *
     *
     */
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    /**
     *
     *
     */
    protected boolean asynchronous() {
        return true;
    }

    /**
     *
     *
     */
    protected boolean enable(Node[] nodes) {
        if (nodes.length == 0) {
            return false;
        }

        // Sanity check to make sure the collab manager is present
        CollabManager manager = CollabManager.getDefault();

        if (manager == null) {
            return false;
        }

        // Disable if cookie not present
        if (!super.enable(nodes)) {
            return false;
        }

        // The selected nodes can either be the Conversations group node
        // or one or more contact nodes.  We need to discover which mode we're
        // operating in.
        return true;
    }

    /**
     *
     *
     */
    protected void performAction(Node[] nodes) {
        // The selected nodes can either be the Conversations group node
        // or one or more contact nodes.  We need to discover which mode we're
        // operating in.
        CollabSessionCookie sessionCookie = (CollabSessionCookie) nodes[0].getCookie(CollabSessionCookie.class);
        assert sessionCookie != null : "CollabSessionCookie was null despite enable check";

        CollabSession session = sessionCookie.getCollabSession();
        AddConversationForm form = new AddConversationForm(session);
        form.addConversation();
    }
}

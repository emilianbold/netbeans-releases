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
public class ManagePublicConversationAction extends CookieAction {
    /**
     *
     *
     */
    public String getName() {
        return NbBundle.getMessage(CreateConversationAction.class, "LBL_ManagePublicConversationAction_Name"); // NOI18N
    }

    /**
     *
     *
     */
    protected String iconResource() {
        return "org/netbeans/modules/collab/ui/resources/chat_png.gif"; // NOI18N
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
        return new Class[] { CollabSessionCookie.class, PublicConversationNode.class };
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

        // Disable if cookies not present
        if (!super.enable(nodes)) {
            return false;
        }

        assert nodes.length == 1 : "Only one node should be available when this method is called";

        PublicConversationNode node = (PublicConversationNode) nodes[0].getCookie(PublicConversationNode.class);

        if (node == null) {
            return false;
        }

        CollabSession session = node.getCollabSession();
        String name = node.getConversationName();

        //Fix for bug#6239787, disable manage if conv is in progress  
        //		Conversation conversation=node.getConversation(); 
        //		Debug.out.println("ManagePublic Conversation: "); 
        //		if(conversation!=null) 
        //		{ 
        //			CollabPrincipal[] convUsers = conversation.getParticipants(); 
        //			Debug.out.println("ManagePublic Conversation: "+ convUsers.length); 
        //			if((convUsers!=null && convUsers.length>0))//conv users are enough 
        //			{             
        //				return false; 
        //			} 
        //		}		
        return session.canManagePublicConversation(name);
    }

    /**
     *
     *
     */
    protected void performAction(Node[] nodes) {
        PublicConversationNode node = (PublicConversationNode) nodes[0].getCookie(PublicConversationNode.class);
        assert node != null : "PublicConversationNode was null despite enable check"; // NOI18N

        CollabSession session = node.getCollabSession();
        String name = node.getConversationName();

        CollabManager.getDefault().getUserInterface().managePublicConversation(session, name);
    }
}

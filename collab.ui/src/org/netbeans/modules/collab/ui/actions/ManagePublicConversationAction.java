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
package org.netbeans.modules.collab.ui.actions;

import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.CookieAction;

import com.sun.collablet.CollabManager;
import com.sun.collablet.CollabSession;
import org.netbeans.modules.collab.ui.*;

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

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
import org.openide.awt.*;
import org.openide.filesystems.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.windows.*;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;

import org.netbeans.modules.collab.*;
import org.netbeans.modules.collab.core.Debug;
import org.netbeans.modules.collab.ui.*;
import org.netbeans.modules.collab.ui.options.AccountNode;


/**
 * TAF 10-22-2004: This class does not currently function.
 *
 */
public class ToggleAutoLoginAccountAction extends CookieAction {
    //	/**
    //	 *
    //	 *
    //	 */
    //	protected boolean enable(Node[] nodes) 
    //	{
    //		if (nodes.length!=1)
    //		{
    //	Debug.out.println("Nodes.length <> 1");
    //			return false;
    //		}
    //		// Sanity check to make sure the collab manager is present
    //		CollabManager manager=CollabManager.getDefault();
    //		if (manager==null)
    //		{
    //	Debug.out.println("Cookie was null");
    //			return false;
    //		}
    //else
    //	Debug.out.println("Manager not null");
    //
    //		AccountNode node=(AccountNode)
    //			nodes[0].getCookie(AccountNode.class);
    //
    //Debug.out.println("Enabled was "+(node!=null));
    //
    //		return node!=null;
    //	}

    /**
     *
     *
     */
    public String getName() {
        return NbBundle.getMessage(ToggleAutoLoginAccountAction.class, "LBL_ToggleAutoLoginAccountAction_Name");
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
    protected boolean isAutoLogin() {
        CollabSession session = null;

        if (getActivatedNodes().length > 0) {
            AccountNode cookie = (AccountNode) getActivatedNodes()[0].getCookie(AccountNode.class);

            if (cookie != null) {
                CollabManager manager = CollabManager.getDefault();

                if (manager != null) {
                    Debug.out.println("Result = " + manager.getUserInterface().isAutoLoginAccount(cookie.getAccount()));

                    return cookie.isAutoLogin();
                }
            } else {
                Debug.out.println("Cookie was null");
                new Exception().printStackTrace(Debug.out);
            }
        } else {
            Debug.out.println("No active nodes");
        }

        return false;
    }

    //	/**
    //	 *
    //	 *
    //	 */
    //	public JMenuItem getPopupPresenter()
    //	{
    //		JMenuItem result=
    //			new JCheckBoxMenuItem()
    //			{
    //				public boolean getSelected()
    //				{
    //					return isAutoLogin();
    //				}
    //			};
    //
    //		Actions.connect(result,this);
    //		return result;
    //
    //		
    //
    //	}

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
    protected void performAction(Node[] nodes) {
        // Sanity check to make sure the collab manager is present
        CollabManager manager = CollabManager.getDefault();
        assert manager != null : "CollabManager was null; action should not have been enabled"; // NOI18N

        // Flip the auto-login bit
        AccountNode node = (AccountNode) nodes[0].getCookie(AccountNode.class);

        if (node != null) {
            manager.getUserInterface().setAutoLoginAccount(
                node.getAccount(), !manager.getUserInterface().isAutoLoginAccount(node.getAccount())
            );
        }
    }

    protected Class[] cookieClasses() {
        return new Class[] { AccountNode.class };
    }

    protected int mode() {
        return MODE_EXACTLY_ONE;
    }
}

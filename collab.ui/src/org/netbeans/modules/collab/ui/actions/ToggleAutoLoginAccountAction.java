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

import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.actions.CookieAction;

import com.sun.collablet.CollabManager;
import com.sun.collablet.CollabSession;
import org.netbeans.modules.collab.core.Debug;
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

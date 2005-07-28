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
package org.netbeans.modules.collab.ui.options;

import com.sun.collablet.Account;
import com.sun.collablet.AccountManager;
import com.sun.collablet.CollabManager;

import org.openide.*;
import org.openide.actions.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.util.datatransfer.*;

import java.awt.datatransfer.*;

import java.beans.*;

import java.io.*;

import java.util.*;

import org.netbeans.modules.collab.*;
import org.netbeans.modules.collab.ui.actions.*;


/**
 *
 *
 * @author        Todd Fast, todd.fast@sun.com
 */
public class AccountsNode extends AbstractNode {
    ////////////////////////////////////////////////////////////////////////////
    // Class fields
    ////////////////////////////////////////////////////////////////////////////
    public static final String ICON_BASE = 
        //		"org/netbeans/modules/collab/ui/resources/group_png"; // NOI18N
        "org/netbeans/modules/collab/core/resources/account_png"; // NOI18N
    private static final SystemAction[] DEFAULT_ACTIONS = new SystemAction[] {
            SystemAction.get(PasteAction.class), null, SystemAction.get(NewAction.class), null,
            SystemAction.get(PropertiesAction.class),
        };

    /**
     *
     *
     */
    public AccountsNode() {
        super(createChildren());
        setName(NbBundle.getBundle(AccountsNode.class).getString("LBL_AccountsNode_Name")); // NOI18N
        setIconBase(ICON_BASE);
        systemActions = DEFAULT_ACTIONS;
    }

    /**
     *
     *
     */
    protected static AccountsNodeChildren createChildren() {
        return new AccountsNodeChildren();
    }

    /**
     *
     *
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(AccountsNode.class);
    }

    /**
     *
     *
     */
    public NewType[] getNewTypes() {
        return new NewType[] { new NewAccount() };
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
        return false;
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
    public AccountsNodeChildren getAccountsNodeChildren() {
        return (AccountsNodeChildren) getChildren();
    }

    /**
     *
     *
     */
    protected void createPasteTypes(Transferable transferrable, List list) {
        final Node[] nodes = NodeTransfer.nodes(transferrable, NodeTransfer.COPY);

        if ((nodes != null) && (nodes.length > 0) && nodes[0] instanceof AccountNode) {
            list.add(
                new PasteType() {
                    public Transferable paste() throws IOException {
                        for (int i = 0; i < nodes.length; i++) {
                            Account account = ((AccountNode) nodes[i]).getAccount();
                            Account clone = (Account) account.clone();

                            // Add the clone as an account.  Note, this
                            // should cause a property change event to 
                            // fire, which should cause the accounts node
                            // to reflect the new account.  Therefore, we
                            // don't need to manually add a node to our
                            // children.
                            AccountManager.getDefault().addAccount(clone);
                        }

                        return null;
                    }
                }
            );
        }

        // Also try superclass, but give it lower priority
        super.createPasteTypes(transferrable, list);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public class NewAccount extends NewType {
        /**
         *
         *
         */
        public String getName() {
            return NbBundle.getBundle(AccountsNode.class).getString("LBL_AccountsNode_NewAccount_Name"); // NOI18N
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
        public void create() throws IOException {
            CollabManager manager = CollabManager.getDefault();

            if (manager != null) {
                manager.getUserInterface().createNewAccount(null, null);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance fields
    ////////////////////////////////////////////////////////////////////////////
}

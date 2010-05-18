/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.collab.ui.options;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.List;

import org.openide.actions.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.*;

import com.sun.collablet.Account;
import com.sun.collablet.AccountManager;
import com.sun.collablet.CollabManager;




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

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
package org.netbeans.modules.collab.ui.options;

import java.beans.*;
import java.util.*;

import org.openide.nodes.*;
import org.openide.util.NbBundle;

import com.sun.collablet.Account;
import com.sun.collablet.AccountManager;
import org.netbeans.modules.collab.core.Debug;
import org.netbeans.modules.collab.ui.*;

/**
 *
 *
 * @author        Todd Fast, todd.fast@sun.com
 */
public class AccountsNodeChildren extends Children.Keys implements NodeListener, PropertyChangeListener {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private Collection keys;

    /**
     *
     *
     */
    public AccountsNodeChildren() {
        super();
    }

    /**
     *
     *
     */
    protected void addNotify() {
        AccountManager.getDefault().addPropertyChangeListener(this);
        refreshChildren();
    }

    /**
     *
     *
     */
    protected void removeNotify() {
        _setKeys(Collections.EMPTY_SET);
        AccountManager.getDefault().removePropertyChangeListener(this);
    }

    /**
     *
     *
     */
    protected Node[] createNodes(Object key) {
        Node[] result = null;

        try {
            if (key instanceof Node) {
                result = new Node[] { (Node) key };
            } else {
                result = new Node[] { new AccountNode((Account) key) };
            }
        } catch (Exception e) {
            Debug.debugNotify(e);
        }

        return result;
    }

    /**
     *
     *
     */
    public Collection getKeys() {
        return keys;
    }

    /**
     *
     *
     */
    public void _setKeys(Collection value) {
        keys = value;
        super.setKeys(value);
    }

    /**
     *
     *
     */
    public void refreshChildren() {
        List keys = new ArrayList();

        try {
            Account[] accounts = AccountManager.getDefault().getAccounts();

            if ((accounts == null) || (accounts.length == 0)) {
                keys.add(
                    new MessageNode(
                        NbBundle.getMessage(AccountsNodeChildren.class, "LBL_AccountsNodeChildren_NoAccounts")
                    )
                );
            } else {
                Arrays.sort(accounts, new AccountComparator());
                keys.addAll(Arrays.asList(accounts));
            }

            _setKeys(keys);
        } catch (Exception e) {
            Debug.errorManager.notify(e);
        }
    }

    /**
     *
     *
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getSource() instanceof AccountManager) {
            if (AccountManager.PROP_ACCOUNTS.equals(event.getPropertyName())) {
                refreshChildren();
            }
        }
    }

    /**
     *
     *
     */
    public void childrenAdded(NodeMemberEvent ev) {
        // Ignore
    }

    /**
     *
     *
     */
    public void childrenRemoved(NodeMemberEvent ev) {
        // Ignore
    }

    /**
     *
     *
     */
    public void childrenReordered(NodeReorderEvent ev) {
        // Ignore
    }

    /**
     *
     *
     */
    public void nodeDestroyed(NodeEvent ev) {
        // Ignore
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    protected static class AccountComparator extends Object implements Comparator {
        /**
         *
         *
         */
        public int compare(Object o1, Object o2) {
            if (o1 == o2) {
                return 0;
            }

            if (o1 == null) {
                return -1;
            }

            if (o2 == null) {
                return 1;
            }

            Account a1 = (Account) o1;
            Account a2 = (Account) o2;

            String s1 = a1.getDisplayName();

            if (s1 == null) {
                s1 = "";
            }

            String s2 = a2.getDisplayName();

            if (s2 == null) {
                s2 = "";
            }

            return s1.compareTo(s2);
        }
    }
}

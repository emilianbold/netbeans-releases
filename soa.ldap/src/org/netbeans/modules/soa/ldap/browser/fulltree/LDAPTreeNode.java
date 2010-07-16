/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.soa.ldap.browser.fulltree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.swing.Icon;
import javax.swing.tree.TreeNode;
import org.netbeans.modules.soa.ldap.EntryType;
import org.netbeans.modules.soa.ldap.browser.IconPool;
import org.netbeans.modules.soa.ldap.browser.Iconed;
import org.netbeans.modules.soa.ldap.browser.Named;
import org.netbeans.modules.soa.ldap.browser.UserAndGroupNameHolder;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author anjeleevich
 */
public class LDAPTreeNode extends AbstractLDAPTreeNode implements
        Comparable<LDAPTreeNode>, Named, Iconed, UserAndGroupNameHolder
{
    private Rdn[] rdns;
 
    private String[] objectClasses;
    private String userNameAttributeValue;
    private String groupNameAttributeValue;

    private boolean notLoaded = true;
    private RequestProcessor.Task loadingTask = null;

    private EntryType entryType = null;
    private Icon icon;

    public LDAPTreeNode(LDAPTreeModel ldapTreeModel, Rdn rdn,
            String[] objectClasses,
            String userNameAttributeValue,
            String groupNameAttrbuteValue)
    {
        this(ldapTreeModel, (rdn == null) ? null : new Rdn[] { rdn },
                objectClasses, userNameAttributeValue,
                groupNameAttrbuteValue, true);
    }

    public LDAPTreeNode(LDAPTreeModel ldapTreeModel, Rdn[] rdns,
            String[] objectClasses,
            String userNameAttributeValue,
            String groupNameAttributeValue)
    {
        this(ldapTreeModel, rdns, objectClasses, userNameAttributeValue,
                groupNameAttributeValue, true);
    }

    public LDAPTreeNode(LDAPTreeModel ldapTreeModel, Rdn rdn,
            String[] objectClasses,
            String userNameAttributeValue,
            String groupNameAttributeValue,
            boolean lazy)
    {
        this(ldapTreeModel, (rdn == null) ? null : new Rdn[] { rdn },
                objectClasses, userNameAttributeValue,
                groupNameAttributeValue, lazy);
    }

    public LDAPTreeNode(LDAPTreeModel ldapTreeModel, Rdn[] rdns,
            String[] objectClasses, 
            String userNameAttributeValue,
            String groupNameAttributeValue,
            boolean lazy)
    {
        super(ldapTreeModel);
        
        this.rdns = rdns;
        this.objectClasses = objectClasses;
        this.userNameAttributeValue = userNameAttributeValue;
        this.groupNameAttributeValue = groupNameAttributeValue;

        if (rdns != null) {
            if (rdns.length == 1) {
                setUserObject(rdns[0]);
            } else {
                LdapName ldapName = new LdapName(Arrays.asList(rdns));
                setUserObject(ldapName);
            }
        } else {
            setUserObject(NbBundle.getMessage(LDAPTreeNode.class, 
                    "BROWSE")); // NOI18N
        }

        if (!lazy) {
            notLoaded = false;
        }
    }

    @Override
    public void abortBackgroundTasks() {
        if (loadingTask != null) {
            loadingTask.cancel();
        }

        if (notLoaded) {
            return;
        }

        super.abortBackgroundTasks();
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    public Icon getIcon() {
        if (icon == null) {
            icon = IconPool.getIcon(getObjectClasses());
        }

        return (icon == IconPool.DEFAULT_ICON) ? null : icon;
    }

    public EntryType getEntryType() {
        if (entryType == null) {
            entryType = EntryType.getEntryType(objectClasses);
        }
        return entryType;
    }

    @Override
    public int getChildCount() {
        if (notLoaded) {
            notLoaded = false;
            loadingTask = getLDAPTreeModel().getRequestProcessor()
                    .post(new LoadChildrenRunnable(this));
            add(new LDAPLoadingNode(getLDAPTreeModel()));
        }

        return super.getChildCount();
    }

    @Override
    public Rdn getRdn() {
        return rdns[rdns.length - 1];
    }

    public Rdn[] getRdns() {
        return rdns;
    }

    public LdapName getLDAPName() {
        List<Rdn> builderRdns = new ArrayList<Rdn>();

        AbstractLDAPTreeNode node = this;
        while (node != null) {
            Rdn[] nodeRdns = node.getRdns();
            if (nodeRdns != null) {
                if (nodeRdns.length == 1) {
                    builderRdns.add(0, nodeRdns[0]);
                } else {
                    builderRdns.addAll(0, Arrays.asList(nodeRdns));
                }
            }
            node = (AbstractLDAPTreeNode) node.getParent();
        }

        return new LdapName(builderRdns);
    }

    public LDAPLoadingNode getLDAPLoadingNode() {
        int lastIndex = getChildCount() - 1;
        if (lastIndex >= 0) {
            TreeNode lastNode = getChildAt(lastIndex);
            if (lastNode instanceof LDAPLoadingNode) {
                return (LDAPLoadingNode) lastNode;
            }
        }
        return null;
    }

    void setLoadProgress(int progress) {
        LDAPLoadingNode loadingNode = getLDAPLoadingNode();
        if (loadingNode != null) {
            loadingNode.setProgress(progress);
        }
    }

    void setLoadOrderering() {
        LDAPLoadingNode loadingNode = getLDAPLoadingNode();
        if (loadingNode != null) {
            loadingNode.setOrdering();
        }
    }

    void setLoadResults(List<AbstractLDAPTreeNode> result) {
        int lastIndex = getChildCount() - 1;
        if (lastIndex >= 0) {
            TreeNode lastNode = getChildAt(lastIndex);
            if (lastNode instanceof LDAPLoadingNode) {
                remove(lastIndex);
                getLDAPTreeModel().nodesWereRemoved(this,
                        new int[] { lastIndex },
                        new Object[] { lastNode });
            }
        }

        int i0 = getChildCount();

        int size = result.size();

        int[] indeces = new int[size];

        for (int i = 0; i < size; i++) {
            indeces[i] = i0 + i;
            add(result.get(i));
        }

        getLDAPTreeModel().nodesWereInserted(this, indeces);

        // feed GC with loadingTask
        loadingTask = null;
    }

    public int compareTo(LDAPTreeNode o) {
        return getRdn().compareTo(o.getRdn());
    }

    public int compareTo(Rdn rdn) {
        return getRdn().compareTo(rdn);
    }

    public String[] getObjectClasses() {
        return objectClasses;
    }

    public String getUserNameAttributeValue() {
        return (getEntryType() != EntryType.USER) ? null
                : userNameAttributeValue;
    }

    public String getGroupNameAttributeValue() {
        return (getEntryType() != EntryType.GROUP) ? null
                : groupNameAttributeValue;
    }
}

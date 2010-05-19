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

package org.netbeans.modules.soa.ldap.browser.searchtree;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import org.netbeans.modules.soa.ldap.EntryType;
import org.netbeans.modules.soa.ldap.browser.IconPool;
import org.netbeans.modules.soa.ldap.browser.Iconed;
import org.netbeans.modules.soa.ldap.browser.Named;
import org.netbeans.modules.soa.ldap.browser.UserAndGroupNameHolder;

/**
 *
 * @author anjeleevich
 */
public class LDAPSearchTreeNode implements TreeNode, Named, Iconed,
        Comparable<LDAPSearchTreeNode>, UserAndGroupNameHolder
{
    private String[] objectClasses;
    private String userNameAttributeValue;
    private String groupNameAttributeValue;

    private final LDAPSearchTreeNode parent;
    private final Rdn rdn;
    private final LDAPSearchTreeModel model;

    private Icon icon = null;
    private EntryType entryType = null;
    
    /*
     * Search request is processed in RequestProcessor thread, new nodes
     * for found entries also are instantiated in this thread and collected in
     * childrenToAddList.
     *
     * Than special runnable moves nodes from childrenToAddList
     * to chilrenList in AWT Event Dispatching Thread (AWT EDT).
     * The last list is list of children which are visible in JTree
     *
     * Both lists are keeped sorted.
     */
    private LDAPSearchTreeNodeList childrenList 
            = new LDAPSearchTreeNodeList();
    
    private LDAPSearchTreeNodeList childrenToAddList
            = new LDAPSearchTreeNodeList();

    private boolean searchResultFlag = false;

    private boolean setSearchResultFlag = false;
    private String[] setObjectClasses = null;
    private String setUserNameAttributeValue = null;
    private String setGroupNameAttributeValue = null;
    
    /*
     * This attribute accumulate change count which shoud be performed
     * in AWT EDT in this node and all descendants. For example
     * "to add one child", "to change node property" and so on
     *
     * changeCount attribute of root node show how many changes should
     * be made in whole tree
     */
    private int changeCount = 0;

    /*
     * Constructor for root node
     */
    LDAPSearchTreeNode(LDAPSearchTreeModel model) {
        assert (model != null);

        this.model = model;
        this.parent = null;
        this.rdn = null;
    }

    LDAPSearchTreeNode(LDAPSearchTreeModel model, 
            LDAPSearchTreeNode parent, Rdn rdn)
    {
        assert (model != null);
        assert (parent != null);
        assert (rdn != null);

        this.model = model;
        this.parent = parent;
        this.rdn = rdn;
    }

    public LDAPSearchTreeModel getLDAPSearchTreeModel() {
        return model;
    }

    public Rdn getRdn() {
        return rdn;
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

    public boolean isSearchResult() {
        return searchResultFlag;
    }

    void sync(boolean fireEvents) {
        assert SwingUtilities.isEventDispatchThread();
        assert model.isLockedByCurrentThread();

        if (changeCount > 0) {
            // insert new children
            int[] indeces = null;

            if (!childrenToAddList.isEmpty()) {
                indeces = childrenList.addAll(childrenToAddList);
            }

            for (int i = childrenList.size() - 1; i >= 0; i--) {
                LDAPSearchTreeNode childNode = childrenList.get(i);
                childNode.sync(fireEvents
                        && !childrenToAddList.contains(childNode));
            }

            if (!childrenToAddList.isEmpty() && fireEvents) {
                model.nodesWereInserted(this, indeces);
            } 

            childrenToAddList.clean();

            boolean fireNodeChanged = false;

            // update search result flag
            if (setSearchResultFlag && !searchResultFlag) {
                searchResultFlag = true;

                if (fireEvents) {
                    fireNodeChanged = true;
                }
            }

            if (setObjectClasses != null) {
                this.objectClasses = setObjectClasses;
                setObjectClasses = null;
                entryType = null;
                
                if (icon != null && fireEvents) {
                    fireNodeChanged = true;
                }
            }

            if (setUserNameAttributeValue != null) {
                this.userNameAttributeValue = setUserNameAttributeValue;
                setUserNameAttributeValue = null;
            }

            if (setGroupNameAttributeValue != null) {
                this.groupNameAttributeValue = setGroupNameAttributeValue;
                setGroupNameAttributeValue = null;
            }

            if (fireNodeChanged) {
                model.nodeChanged(this);
            }

            // reset change counter
            changeCount = 0;
        }
    }

    /*
     * Searchs for child in both children lists.
     * Creates new child if nothing was found.
     */
    LDAPSearchTreeNode getChild(Rdn childRDN, AddEntryResult result) {
        assert !SwingUtilities.isEventDispatchThread();
        assert model.isLockedByCurrentThread();

        LDAPSearchTreeNode node = childrenList.get(childRDN);
        if (node != null) {
            return node;
        }

        node = childrenToAddList.get(childRDN);
        if (node != null) {
            return node;
        }

        LDAPSearchTreeNode newChild = new LDAPSearchTreeNode(model, this,
                childRDN);

        result.addNode(newChild);

        childrenToAddList.add(newChild);

        increaseChangeCounter();

        return newChild;
    }

    void setSearchResult() {
        assert !SwingUtilities.isEventDispatchThread();
        assert model.isLockedByCurrentThread();
        
        if (!searchResultFlag && !setSearchResultFlag) {
            setSearchResultFlag = true;
            increaseChangeCounter();
        }
    }

    public void setAttributes(String[] objectClasses,
            String userNameAttributeValue,
            String groupNameAttributeValue)
    {
        assert !SwingUtilities.isEventDispatchThread();
        assert model.isLockedByCurrentThread();

        boolean increaseChangeCounterFlag = false;

        if (objectClasses != null) {
            this.setObjectClasses = objectClasses;
            increaseChangeCounterFlag = true;
        }

        if (userNameAttributeValue != null) {
            setUserNameAttributeValue = userNameAttributeValue;
            increaseChangeCounterFlag = true;
        }

        if (groupNameAttributeValue != null) {
            setGroupNameAttributeValue = groupNameAttributeValue;
            increaseChangeCounterFlag = true;
        }
        
        if (increaseChangeCounterFlag) {
            increaseChangeCounter();
        }
    }

    private void increaseChangeCounter() {
        changeCount++;
        
        if (parent != null) {
            parent.increaseChangeCounter();
        }
    }

    // Named interface impl
    public LdapName getLDAPName() {
        List<Rdn> rdns = new ArrayList<Rdn>();

        LDAPSearchTreeNode node = this;
        while (node != null) {
            Rdn nodeRdn = node.getRdn();
            if (nodeRdn != null) {
                rdns.add(0, nodeRdn);
            }
            node = node.getParent();
        }

        return new LdapName(rdns);
    }

    // TreeNode interface impl
    public LDAPSearchTreeNode getParent() {
        return parent;
    }

    public int getChildCount() {
        return childrenList.size();
    }

    public LDAPSearchTreeNode getChildAt(int i) {
        return childrenList.get(i);
    }
    
    public int getIndex(TreeNode node) {
        return (node instanceof LDAPSearchTreeNode)
                ? childrenList.indexOf((LDAPSearchTreeNode) node) : -1;
    }

    public boolean getAllowsChildren() {
        return true;
    }

    public boolean isLeaf() {
        if (parent == null) {
            return false;
        }
        
        return childrenList.isEmpty();
    }

    public Enumeration<TreeNode> children() {
        if (childrenList.isEmpty()) {
            return DefaultMutableTreeNode.EMPTY_ENUMERATION;
        }

        return new Enumeration<TreeNode>() {
            int index = 0;

            public boolean hasMoreElements() {
                return index < childrenList.size();
            }

            public TreeNode nextElement() {
                return childrenList.get(index++);
            }
        };
    }

    // Comparable interface impl
    public int compareTo(LDAPSearchTreeNode node) {
        return rdn.compareTo(node.rdn);
    }

    @Override
    public String toString() {
        if (rdn != null) {
            return rdn.toString();
        }

        return model.getTitle();
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

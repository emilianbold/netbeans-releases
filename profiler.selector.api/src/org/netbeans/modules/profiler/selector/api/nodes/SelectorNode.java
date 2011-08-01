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

package org.netbeans.modules.profiler.selector.api.nodes;

import org.netbeans.lib.profiler.ui.components.tree.CheckTreeNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import javax.swing.Icon;
import javax.swing.tree.TreeNode;
import org.netbeans.lib.profiler.client.ClientUtils.SourceCodeSelection;
import org.openide.util.Lookup;


/**
 * A subclass of {@linkplain CheckTreeNode} used in the {@linkplain org.netbeans.modules.profiler.selector.ui.RootSelectorTree}
 *
 * @author Jaroslav Bachorik
 */
abstract public class SelectorNode extends CheckTreeNode implements Lookup.Provider {
    private SelectorChildren children;
    private String nodeName;
    private String displayName;
    private boolean valid = true;
    private Lookup lookup = null;

    /** Creates a new instance of SelectorNode */
    public SelectorNode(String name, String displayName, Icon icon, SelectorChildren children) {
        super(displayName, icon);
        init(displayName, name, children);
    }

    public SelectorNode(String name, String displayName, Icon icon, SelectorChildren children, Lookup lookup) {
        super(displayName, icon);
        init(displayName, name, children);
        this.lookup = lookup;
    }

    public SelectorNode(String name, String displayName, Icon icon, SelectorChildren children, ContainerNode parent) {
        super(displayName, icon);
        init(displayName, name, children);
        
        setParent(parent);
    }

    public SelectorNode(String name, String displayName, Icon icon, SelectorChildren children, ContainerNode parent, Lookup lookup) {
        this(name, displayName, icon, children, parent);
        this.lookup = lookup;
    }

    private void init(String displayName, String name, SelectorChildren children) {
        this.nodeName = name;
        this.children = children;
        this.displayName = displayName;
        if (this.children != null) {
            this.children.setParent(this);
        }
    }

    final public Lookup getLookup() {
        if (lookup == null) {
            if (getParent() != null) {
                return getParent().getLookup();
            } else {
                return Lookup.EMPTY;
            }
        }
        return lookup;
    }

    @Override
    final public TreeNode getChildAt(int childIndex) {
        int size = children.getNodes().size();

        return (TreeNode) (((childIndex <= size) && (childIndex >= 0)) ? children.getNodes().get(childIndex) : null);
    }

    @Override
    public int getChildCount() {
        return children.getNodeCount();
    }

    public int getChildCount(boolean forceRefresh) {
        return children.getNodeCount(forceRefresh);
    }

    @Override
    final public int getIndex(TreeNode node) {
        return children.getNodes().indexOf(node);
    }

    @Override
    public boolean isLeaf() {
        return getChildCount() == 0;
    }

    /**
     * This method calculates all root-methods presented by this {@linkplain SelectorNode}
     * @param all A flag to indicate that we are interested in all root-methods regardless
     *            of the "checked" status.
     * @return Will return node's root-methods if the node is checked or "all" is true. 
     *         Otherwise it will return an empty collection
     */
    public Collection<SourceCodeSelection> getRootMethods(boolean all) {
        Collection<SourceCodeSelection> roots = new ArrayList<SourceCodeSelection>();

        if (all || isFullyChecked()) {
            SourceCodeSelection signature = getSignature();

            if (signature != null) {
                roots.add(signature);
            }
        }

        return roots;
    }

    /**
     * A shortcut to {@linkplain SelectorNode#getRootMethods(boolean)} with FALSE
     * @return Will return node's root-methods if the node is checked.
     *         Otherwise it will return an empty collection
     */
    final public Collection<SourceCodeSelection> getRootMethods() {
        return getRootMethods(false);
    }

    /**
     * Node-name property
     * @return Returns a node internal name
     */
    final public String getNodeName() {
        return nodeName;
    }

    /**
     * Display name property
     * @return Returns a human readable node name
     */
    final public String getDisplayName() {
        return displayName;
    }

    @Override
    final public ContainerNode getParent() {
        TreeNode parent = super.getParent();

        if ((parent == null) || (!(parent instanceof ContainerNode))) {
            return null;
        }

        return (ContainerNode) parent;
    }

    /**
     * This method gives access to the assigned VM signature
     * @return Returns a VM signature representing this node or NULL
     */
    abstract public SourceCodeSelection getSignature();

    /**
     * The validity flag
     * @return Returns TRUE if the node is safe to be displayed, FALSE otherwise
     */
    final public boolean isValid() {
        return valid;
    }

    @Override
    final public Enumeration children() {
        return Collections.enumeration(children.getNodes());
    }

    /**
     * Will detach the node from its parent
     */
    final public void detach() {
        this.parent = null;
    }

    @Override
    public boolean equals(Object anotherNode) {
        if (anotherNode == null) {
            return false;
        }

        if (!(anotherNode instanceof SelectorNode)) {
            return false;
        }

        if ((((SelectorNode) anotherNode).getSignature() != null) && (getSignature() != null)) {
            return (((SelectorNode) anotherNode).getSignature().equals(getSignature()));
        }

        return ((SelectorNode) anotherNode).getNodeName().equals(getNodeName());
    }

    @Override
    public int hashCode() {
        return getNodeName().hashCode() + ((getSignature() != null) ? getSignature().hashCode() : 0);
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    /**
     * Call this method to programatically set the node's children
     * @param children The {@linkplain SelectorChildren} instance to be used
     */
    final protected void setChildren(SelectorChildren children) {
        this.children = children;
        this.children.setParent(this);
    }

    /**
     * Sets the validity flag
     * @param value The validity flag; when set to FALSE the node will not be displayed
     */
    final protected void setValid(boolean value) {
        valid = value;
    }
    
    /**
     * Used to update the displayName property after the node has been initialized
     * @param displayName The new display name
     */
    final protected void updateDisplayName(String displayName) {
        this.displayName = displayName;
    }
}

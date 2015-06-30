/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.mixeddev.java.jni.ui;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.TreeNode;

/**
 *
 * @author Petr Kudryavtsev <petrk@netbeans.org>
 */
public final class JPFCTreeNode<D> implements TreeNode {
    
    private final JPFCTreeNodeProviders providers;
    
    private final JPFCTreeNodeProvider<D> provider;
    
    private final JPFCTreeNodeFilter filter;
    
    private final JPFCTreeNode parent;
    
    private final D data;
    
    private List<JPFCTreeNode<?>> children;

    public JPFCTreeNode(JPFCTreeNodeProviders providers, JPFCTreeNodeFilter filter, JPFCTreeNode parent, D data) {
        this.providers = providers;
        this.filter = filter;
        this.parent = parent;
        this.data = data;
        this.provider = providers.getProvider(data);
    }
    
    public JPFCTreeNodeProviders getProviders() {
        return providers;
    }

    public D getData() {
        return data;
    }
    
    @Override
    public TreeNode getParent() {
        return parent;
    }
    
    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public TreeNode getChildAt(int childIndex) {
        ensureInitialized();
        return children.get(childIndex);
    }

    @Override
    public int getChildCount() {
        ensureInitialized();
        return children.size();
    }

    @Override
    public int getIndex(TreeNode node) {
        ensureInitialized();
        return children.indexOf(node);
    }

    @Override
    public boolean isLeaf() {
        ensureInitialized();
        return !children.isEmpty();
    }

    @Override
    public Enumeration children() {
        ensureInitialized();
        return Collections.enumeration(children);
    }

    @Override
    public String toString() {
        return provider.getName(this);
    }
    
    private void ensureInitialized() {
        if (children == null) {
            children = provider.getChildren(this, filter);
        }
        if (children == null) {
            children = Collections.emptyList();
        }
    }
}

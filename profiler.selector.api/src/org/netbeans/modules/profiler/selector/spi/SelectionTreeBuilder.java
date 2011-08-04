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

package org.netbeans.modules.profiler.selector.spi;

import org.netbeans.modules.profiler.selector.api.nodes.SelectorNode;
import java.util.Collections;
import java.util.List;
import org.openide.util.Lookup;


/**
 * {@linkplain SelectionTreeBuilder} takes care of building a tree representing
 * the project logical model. The tree is composed of {@linkplain SelectorNode}
 * nodes.
 * @author Jaroslav Bachorik
 */
public abstract class SelectionTreeBuilder {
    public static final SelectionTreeBuilder NULL = new SelectionTreeBuilder() {
        @Override
        public List<SelectorNode> buildSelectionTree() {
            return Collections.emptyList();
        }

        @Override
        public int estimatedNodeCount() {
            return 0;
        }
    };

    public static class Type {
        final public String id;
        final public String displayName;

        public Type(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Type other = (Type) obj;
            if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
                return false;
            }
            if ((this.displayName == null) ? (other.displayName != null) : !this.displayName.equals(other.displayName)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 11 * hash + (this.id != null ? this.id.hashCode() : 0);
            hash = 11 * hash + (this.displayName != null ? this.displayName.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
    
    private Type type = new Type("NULL", "YOU SHOULD NOT SEE THIS"); // NOI18N
    private boolean preferredFlag;
    private Lookup context;

    public SelectionTreeBuilder() {
        this(new Type("NULL", "YOU SHOULD NOT SEE THIS"), false); // NOI18N
    }
    public SelectionTreeBuilder(Type builderType, boolean isPreferred) {
        this.type = builderType;
        this.preferredFlag = isPreferred;
    }

    final public boolean isPreferred() {
        return preferredFlag;
    }

    final public void setPreferred(boolean isPreferred) {
        preferredFlag = isPreferred;
    }

    /**
     * Each builder *MUST* have a human readable name
     * @return Returns the human readable builder name
     */
    final public String getDisplayName() {
        return type.displayName;
    }

    /**
     * Each builder *MUST* have a unique ID
     * @return Returns an ID string
     */
    final public String getID() {
        return type.id;
    }

    final public Type getType() {
        return type;
    }

    /**
     * Builds the tree composed of {@linkplain SelectorNode} nodes
     * @return Returns a list of top-level nodes
     */
    abstract public List<? extends SelectorNode> buildSelectionTree();
    /**
     * 
     * @return Returns the estimated number of top level
     *         nodes that will be created when calling
     *         {@linkplain SelectionTreeBuilder#buildSelectionTree()} method.
     *         Return -1 if the builder would generate an empty tree
     */
    abstract public int estimatedNodeCount();

    @Override
    /**
     * By default the {@linkplain SelectionTreeBuilder#displayName} is used
     */
    public String toString() {
        return type.displayName;
    }
    
    /**
     * Sets an additional builder context
     * @param lkp The builder context
     */
    final public void setContext(Lookup lkp) {
        this.context = lkp;
    }
    
    /**
     * Gives access to the builder's additional context
     * @return Returns a {@linkplain Lookup} context
     */
    final protected Lookup getContext() {
        return context;
    }
}

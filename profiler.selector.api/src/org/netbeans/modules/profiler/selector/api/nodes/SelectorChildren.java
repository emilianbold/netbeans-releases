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

import java.util.ArrayList;
import java.util.List;


/**
 * Support for lazily loaded tree node children
 * @author Jaroslav Bachorik
 */
public abstract class SelectorChildren<T extends SelectorNode> {
    public static final SelectorChildren<SelectorNode> LEAF = new SelectorChildren() {
        private final List<SelectorNode> LEAFLIST = new ArrayList<SelectorNode>();

        protected List<? extends SelectorNode> prepareChildren(SelectorNode parent) {
            return LEAFLIST;
        }
    };


    private List<?extends SelectorNode> children;
    private T nodeParent;

    /** Creates a new instance of SelectorChildren with no parent*/
    public SelectorChildren() {
    }

    /**
     * Creates a new instance of {@linkplain SelectorChildren} with the given parent
     * @param parent The parent to attach the children to
     */
    public SelectorChildren(T parent) {
        nodeParent = parent;
    }

    /**
     * Retrieves the number of child nodes
     * @return Returns the actual number of child nodes if they have been
     *         already calculated; -1 otherwise
     */
    public int getNodeCount() {
        return getNodeCount(false);
    }

    /**
     * Retrieves the number of child nodes
     * @param forceRefresh A flag indicating whether to force child nodes calculation
     * @return Returns the actual number of child nodes if they have been
     *         already calculated; -1 otherwise
     */
    public int getNodeCount(boolean forceRefresh) {
        if (forceRefresh) {
            getNodes();
        }

        if (children == null) {
            return -1;
        }

        return children.size();
    }

    /**
     * Calculates the child nodes - can take a while
     * @return Returns the list of child nodes
     */
    public List<?extends SelectorNode> getNodes() {
        if (children == null) {
            children = prepareChildren(nodeParent);
        }

        return children;
    }

    /**
     * Will attach the children to the given parent
     * @param parent The parent to attach the children to
     */
    public void setParent(T parent) {
        nodeParent = parent;
    }

    /**
     * Cleans up the calculated child nodes -
     * it should be used to force reloading of modified children
     */
    public void reset() {
        children = null;
    }

    /**
     * This method is called when the child nodes are to be calculated
     * @param parent The parent node
     * @return Returns the list of child nodes
     */
    protected abstract List<?extends SelectorNode> prepareChildren(T parent);
}

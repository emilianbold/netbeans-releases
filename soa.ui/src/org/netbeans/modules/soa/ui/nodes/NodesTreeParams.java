/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.soa.ui.nodes;

import org.openide.nodes.Node;

/**
 * Contains subsidiary methods to optimize tree view execution.
 *
 * @author nk160297
 */
public class NodesTreeParams {
    
    private Class<? extends Node>[] targetNodeClasses;
    private Class<? extends Node>[] leafNodeClasses;
    private boolean highlightTargetNodes = false;
    
    /**
     * This method is intended to be used by Tree Node Choosers.
     * A Node chooser is intended to choose a node. But not any node can be chosen.
     * Usually the Chooser is designed to choose node of the particular type.
     * This method allows to specify one or more classes of nodes.
     * If the method returns null then it means that any nodes are allowed.
     * Method has to return not empty array or null!
     */
    public Class<? extends Node>[] getTargetNodeClasses() {
        return targetNodeClasses;
    }
    
    /**
     * Specifies the set of node's classes which will be considered as leaf nodes.
     * It's a kind of optimization, so this method can return null and it will not
     * change result view if corresponding filter is assign.
     * But if the method returns an array then nodes of the specifed types
     * will not try to load their children. This method is important for cases
     * when a node type can represent leaf as well as not leaf in different cases.
     * <p>
     * If the method returns null then it means that all nodes should try to load
     * children to dicide if they are leaf or not.
     * Method has to return not empty array or null!
     */
    public Class<? extends Node>[] getLeaftNodeClasses() {
        return leafNodeClasses;
    }
    
    public void setTargetNodeClasses(Class<? extends Node>... types) {
        if (types == null || types.length == 0) {
            targetNodeClasses = null;
        } else {
            targetNodeClasses = types;
        }
    }
    
    public void setLeafNodeClasses(Class<? extends Node>... types) {
        if (types == null || types.length == 0) {
            leafNodeClasses = null;
        } else {
            leafNodeClasses = types;
        }
    }
    
    /**
     * Check if the specified class is the target node class as 
     * it specified with the setTargetNodeClasses method.
     */
    public boolean isTargetNodeClass(Class<? extends Node> nodeClass) {
        Class<? extends Node>[] classArr = getTargetNodeClasses();
        boolean isTargetNodeClass = false;
        if (classArr != null) {
            for (Class<? extends Node> targNodeClass : classArr) {
                if (targNodeClass.isAssignableFrom(nodeClass)) {
                    isTargetNodeClass = true;
                    break;
                }
            }
        }
        //
        return isTargetNodeClass;
    }
    
    public void setHighlightTargetNodes(boolean newValue) {
        highlightTargetNodes = newValue;
    }
    
    public boolean isHighlightTargetNodes() {
        return highlightTargetNodes;
    }
    
}

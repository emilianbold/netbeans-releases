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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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

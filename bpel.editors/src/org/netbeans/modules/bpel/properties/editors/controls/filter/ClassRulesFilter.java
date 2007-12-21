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
package org.netbeans.modules.bpel.properties.editors.controls.filter;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.bpel.properties.editors.controls.ActionProxyNode;
import org.openide.nodes.Node;

/**
 * This node filter make a decision based on the class of parent an child.
 *
 * By default all class pairs are prohibitted.
 * To allow the pair of classes you have to explicitly call addAllowRule.
 *
 * @author nk160293
 */
public class ClassRulesFilter implements NodeChildFilter {

    private List<ClassPair> allowedPairs;
    
    public ClassRulesFilter() {
        allowedPairs = new ArrayList<ClassPair>();
    }
    
    public boolean isPairAllowed(Node parentNode, Node childNode) {
        //
        Class parentNodeClass = null;
        if (parentNode instanceof ActionProxyNode) {
            parentNodeClass = ((ActionProxyNode)parentNode).getOriginalNodeClass();
        } else {
            parentNodeClass = parentNode.getClass();
        }
        //
        Class childNodeClass = null;
        if (childNode instanceof ActionProxyNode) {
            childNodeClass = ((ActionProxyNode)childNode).getOriginalNodeClass();
        } else {
            childNodeClass = childNode.getClass();
        }
        //
        return isPairAllowed(parentNodeClass, childNodeClass);
    }
    
    public boolean isPairAllowed(Class parentClass, Class childClass) {
        for (ClassPair cPair : allowedPairs) {
            if (cPair.parentClass.isAssignableFrom(parentClass) && 
                    cPair.childClass.isAssignableFrom(childClass)) {
                return true;
            }
        }
        return false;
    }
    
    public void addAllowRule(Class parentClass, Class childClass) {
        allowedPairs.add(new ClassPair(parentClass, childClass));
    }
    
    private class ClassPair {
        public Class parentClass;
        public Class childClass;
        
        public ClassPair(Class pClass, Class cClass) {
            parentClass = pClass;
            childClass = cClass;
        }
    }
}

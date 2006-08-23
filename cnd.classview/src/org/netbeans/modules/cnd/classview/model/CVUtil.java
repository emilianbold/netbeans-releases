/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.classview.model;

import java.util.*;
import org.netbeans.modules.cnd.api.model.*;
import java.awt.*;
import org.netbeans.modules.cnd.api.model.util.CsmSortUtilities;
import org.openide.nodes.*;
//import org.openide.util.NbBundle;
import org.netbeans.modules.cnd.classview.resources.I18n;

/**
 * Misc static utilitiy functions
 * @author Vladimir Kvasihn
 */
public class CVUtil {

    public static String getSignature(CsmFunction fun) {
        StringBuffer sb = new StringBuffer(fun.getName());
        sb.append('(');
        boolean addComma = false;
        for( Iterator iter = fun.getParameters().iterator(); iter.hasNext(); ) {
            CsmParameter par = (CsmParameter) iter.next();
            if( addComma ) {
                sb.append(", ");
            }
            else {
                addComma = true;
            }
            //sb.append(par.getText());
            CsmType type = par.getType();
	    if( type != null ) {
		sb.append(type.getText());
		sb.append(' ');
	    }
	    sb.append(par.getName());
        }
        
        sb.append(')');
        return sb.toString();
    }

    public static Node createLoadingRoot() {
        Children.Array children = new Children.SortedArray();
        children.add(new Node[] { createLoadingNode() });
        AbstractNode root = new AbstractNode(children);
        return root;
    }
    
    public static Node createLoadingNode() {
        BaseNode node = new LoadingNode();
	return node;
    }
    
    public static class BaseNodeComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            if( (o1 instanceof Node) && (o2 instanceof Node) ) {
                return ((Node) o1).getDisplayName().compareTo(((Node) o2).getDisplayName());
            }
            else {
                return 0;
            }
        }
    }
    
    public static class NamespaceNodesComparator extends BaseNodeComparator {
        
        public int compare(Object o1, Object o2) {
            
            if( (o1 instanceof NamespaceNode) ||  (o2 instanceof NamespaceNode) ) {
                if( ! (o2 instanceof NamespaceNode) ) {
                    return -1;
                } 
                else if( ! (o1 instanceof NamespaceNode) ) {
                    return +1;
                }
                else {
                    return super.compare(o1, o2);
                }
            }
            else if( (o1 instanceof ObjectNode) && (o2 instanceof  ObjectNode) ) {
                return CsmSortUtilities.NATURAL_NAMESPACE_MEMBER_COMPARATOR.compare(
                            ((ObjectNode) o1).getObject(), ((ObjectNode) o2).getObject());
            }
            return super.compare(o1, o2);
        }
    }    
    
    public static class MemberNodesComparator extends BaseNodeComparator {
        
        public int compare(Object o1, Object o2) {
            
            if( (o1 instanceof MemberNode) && (o2 instanceof  MemberNode) ) {
                return CsmSortUtilities.NATURAL_MEMBER_NAME_COMPARATOR.compare(
                            ((ObjectNode) o1).getObject(), ((ObjectNode) o2).getObject());
            }
            return super.compare(o1, o2);
        }
    }
}

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
package org.netbeans.modules.bpel.debugger.ui.plinks;

import java.util.HashSet;
import org.netbeans.modules.bpel.debugger.ui.plinks.models.EndpointWrapper;
import org.netbeans.modules.bpel.debugger.ui.plinks.models.PartnerLinkWrapper;
import org.netbeans.spi.viewmodel.TreeExpansionModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.w3c.dom.Node;

/**
 * 
 * 
 * @author Kirill Sorokin
 */
public class PLinksTreeExpansionModel implements TreeExpansionModel {
    
    private HashSet myExpandedNodes = new HashSet();
    
    /**{@inheritDoc}*/
    public synchronized boolean isExpanded(
            final Object object) throws UnknownTypeException {
        
        final Object key = getKey(object);
        
        if (key != null) {
            return myExpandedNodes.contains(key);
        } else {
            return false;
        }
    }
    
    /**{@inheritDoc}*/
    public synchronized void nodeExpanded(
            final Object object) {
        
        final Object key = getKey(object);
        
        if (key != null) {
            myExpandedNodes.add(key);
        }
    }
    
    /**{@inheritDoc}*/
    public synchronized void nodeCollapsed(
            final Object object) {
        
        final Object key = getKey(object);
        
        if (key != null) {
            myExpandedNodes.remove(key);
        }
    }
    
    private Object getKey(
            final Object object) {
        
        if (object instanceof PartnerLinkWrapper) {
            return ((PartnerLinkWrapper) object).getName();
        }
        
        if (object instanceof EndpointWrapper) {
            return ((EndpointWrapper) object).getSerializedValue();
        }
        
        if (object instanceof Node) {
            final Node node = (Node) object;
            
            return node.getNodeName() + ":" + node.getNodeValue(); // NOI18N
        }
        
        return object;
    }
}

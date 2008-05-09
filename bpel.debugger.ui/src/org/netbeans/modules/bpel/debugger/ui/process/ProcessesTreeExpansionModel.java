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
package org.netbeans.modules.bpel.debugger.ui.process;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.BpelProcess;
import org.netbeans.modules.bpel.debugger.api.CorrelationSet;
import org.netbeans.modules.bpel.debugger.api.ProcessInstance;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.TreeExpansionModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 * 
 * 
 * @author Kirill Sorokin
 */
public class ProcessesTreeExpansionModel implements TreeExpansionModel {
    
    private BpelDebugger myDebugger;
    private Set<Object> myExpandedNodes = new HashSet<Object>();
    private Set<Object> myCollapsedNodes = new HashSet<Object>();
    
    public ProcessesTreeExpansionModel(
            final ContextProvider lookupProvider) {
        
        myDebugger = lookupProvider.lookupFirst(null, BpelDebugger.class);
    }
    
    /**{@inheritDoc}*/
    public synchronized boolean isExpanded(
            final Object object) throws UnknownTypeException {
        final Object key = getKey(object);
        
        final ProcessInstance currentInstance =
                myDebugger.getCurrentProcessInstance();
        
        if ((object instanceof BpelProcess) && 
                !myCollapsedNodes.contains(key) &&
                (currentInstance != null) &&
                currentInstance.getProcess().equals(object)) {
            return true;
        }
        
        return myExpandedNodes.contains(key) && 
                !myCollapsedNodes.contains(key);
    }
    
    /**{@inheritDoc}*/
    public synchronized void nodeExpanded(
            final Object object) {
        final Object key = getKey(object);
        
        myExpandedNodes.add(key);
        myCollapsedNodes.remove(key);
    }
    
    /**{@inheritDoc}*/
    public synchronized void nodeCollapsed(
            final Object object) {
        final Object key = getKey(object);
        
        myExpandedNodes.remove(key);
        myCollapsedNodes.add(key);
    }
    
    private Object getKey(Object node) {
        if (node instanceof CorrelationSet) {
            return ((CorrelationSet) node).getId();
        }
        
        return node;
    }
}

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

import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.debugger.ui.util.HtmlUtil;
import org.openide.util.NbBundle;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.BpelProcess;
import org.netbeans.modules.bpel.debugger.api.CorrelationSet;
import org.netbeans.modules.bpel.debugger.api.Fault;
import org.netbeans.modules.bpel.debugger.api.ProcessInstance;
import org.netbeans.modules.bpel.debugger.api.WaitingCorrelatedMessage;
import org.netbeans.modules.bpel.debugger.ui.process.ProcessesTreeModel.FaultsWrapper;
import org.netbeans.modules.bpel.debugger.ui.util.VariablesUtil;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2005.10.24
 */
public class ProcessesNodeModel implements NodeModel {
    
    private BpelDebugger myDebugger;
    
    private VariablesUtil myVariablesUtil;
    
    public ProcessesNodeModel() {
        // Does nothing
    }
    
    /**{@inheritDoc}*/
    public ProcessesNodeModel(
            final ContextProvider contextProvider) {
        
        myDebugger = contextProvider.lookupFirst(null, BpelDebugger.class);
        myVariablesUtil = new VariablesUtil(myDebugger);
    }
    
    /**{@inheritDoc}*/
    public String getDisplayName(
            final Object object) throws UnknownTypeException {
        
        if (object.equals(TreeModel.ROOT)) {
            return NbBundle.getMessage(
                    ProcessesNodeModel.class, 
                    "CTL_Process_Column_Name"); // NOI18N
        }
        
        if (object instanceof BpelProcess) {
            final BpelProcess process = (BpelProcess) object;
            
            return process.getName();
        }
        
        if (object instanceof ProcessInstance) {
            final ProcessInstance instance = (ProcessInstance) object;
            final String name = instance.getName();
            
            return isProcessInstanceCurrent(instance) ?
                HtmlUtil.toBold(name) : name;
        }
        
        if (object instanceof ProcessesTreeModel.CorrelationSetsWrapper) {
            return NbBundle.getMessage(
                    ProcessesNodeModel.class, 
                    "LBL_Correlation_Sets"); // NOI18N
        }
        
        if (object instanceof CorrelationSet) {
            return ((CorrelationSet) object).getName();
        }
        
        if (object instanceof CorrelationSet.Property) {
            final QName name = ((CorrelationSet.Property) object).getName();
            final String prefix = name.getPrefix();
            
            if ((prefix == null) || prefix.equals("")) {
                return name.getLocalPart();
            } else {
                return name.getPrefix() + ":" + name.getLocalPart();
            }
        }
        
        if (object instanceof ProcessesTreeModel.NoPropertiesDataMarker) {
            return NbBundle.getMessage(
                    ProcessesNodeModel.class, 
                    "LBL_No_Properties_Data_Name"); // NOI18N
        }
        
        if (object instanceof ProcessesTreeModel.WaitingMessagesWrapper) {
            return NbBundle.getMessage(
                    ProcessesNodeModel.class, 
                    "LBL_Waiting_Messages"); // NOI18N
        }
        
        if (object instanceof WaitingCorrelatedMessage) {
            return ((WaitingCorrelatedMessage) object).getName();
        }
        
        if (object instanceof FaultsWrapper) {
            return NbBundle.getMessage(
                    ProcessesNodeModel.class, 
                    "LBL_Faults"); // NOI18N
        }
        
        if (object instanceof Fault) {
            final QName name = ((Fault) object).getQName();
            final String prefix = name.getPrefix();
            
            if ((prefix == null) || prefix.equals("")) {
                return name.getLocalPart();
            } else {
                return name.getPrefix() + ":" + name.getLocalPart();
            }
        }
        
        return myVariablesUtil.getDisplayName(object);
    }
    
    /**{@inheritDoc}*/
    public String getShortDescription(
            final Object object) throws UnknownTypeException {
        
        if (object.equals(TreeModel.ROOT)) {
            return NbBundle.getMessage(
                    ProcessesNodeModel.class, 
                    "CTL_Process_Column_Name_Tooltip"); // NOI18N
        }
        
        if (object instanceof CorrelationSet.Property) {
            final QName name = ((CorrelationSet.Property) object).getName();
            
            if (name.getNamespaceURI().equals("")) {
                return name.getLocalPart();
            } else {
                return "{" + name.getNamespaceURI() + "} " + 
                        name.getLocalPart();
            }
        }
        
        if (object instanceof ProcessesTreeModel.NoPropertiesDataMarker) {
            return NbBundle.getMessage(
                    ProcessesNodeModel.class, 
                    "LBL_No_Properties_Data_Tooltip"); // NOI18N
        }
        
        if (object instanceof Fault) {
            final QName name = ((Fault) object).getQName();
            
            if (name.getNamespaceURI().equals("")) {
                return name.getLocalPart();
            } else {
                return "{" + name.getNamespaceURI() + "} " + 
                        name.getLocalPart();
            }
        }
        
        return getDisplayName(object);
    }
    
    /**{@inheritDoc}*/
    public String getIconBase(
            final Object object) throws UnknownTypeException {
        
        if (object.equals(TreeModel.ROOT)) {
            return CURRENT_INSTANCE_ICON;
        }
        
        if (object instanceof BpelProcess) {
            return PROCESS_ICON;
        }
        
        if (object instanceof ProcessInstance) {
            final ProcessInstance instance = (ProcessInstance) object;
            final int state = instance.getState();
            
            if (isProcessInstanceCurrent(instance)) {
                return CURRENT_INSTANCE_ICON;
            }
            
            if (state == ProcessInstance.STATE_SUSPENDED) {
                return SUSPENDED_INSTANCE_ICON;
            }
            
            return RUNNING_INSTANCE_ICON;
        }
        
        if (object instanceof ProcessesTreeModel.CorrelationSetsWrapper) {
            return PROCESS_ICON;
        }
        
        if (object instanceof CorrelationSet) {
            return PROCESS_ICON;
        }
        
        if (object instanceof CorrelationSet.Property) {
            return PROCESS_ICON;
        }
        
        if (object instanceof ProcessesTreeModel.NoPropertiesDataMarker) {
            return PROCESS_ICON;
        }
        
        if (object instanceof ProcessesTreeModel.WaitingMessagesWrapper) {
            return PROCESS_ICON;
        }
        
        if (object instanceof WaitingCorrelatedMessage) {
            return PROCESS_ICON;
        }
        
        if (object instanceof FaultsWrapper) {
            return PROCESS_ICON;
        }
        
        if (object instanceof Fault) {
            return PROCESS_ICON;
        }
        
        return myVariablesUtil.getIconBase(object);
    }
    
    /**{@inheritDoc}*/
    public void addModelListener(
            final ModelListener listener) {
        // Does nothing
    }

    /**{@inheritDoc}*/
    public void removeModelListener(
            final ModelListener listener) {
        // Does nothing
    }
    
    // Private /////////////////////////////////////////////////////////////////
    private boolean isProcessInstanceCurrent(
            final ProcessInstance instance) {
        
        if (myDebugger == null) {
            return false;
        } else {
            return instance.equals(myDebugger.getCurrentProcessInstance());
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Constants
    private static final String ICONS_BASE =
            "org/netbeans/modules/bpel/debugger/ui/resources/image/"; // NOI18N
    
    private static final String PROCESS_ICON =
            ICONS_BASE + "process"; // NOI18N
    
    private static final String CURRENT_INSTANCE_ICON =
            ICONS_BASE + "current"; // NOI18N
    
    private static final String RUNNING_INSTANCE_ICON =
            ICONS_BASE + "running"; // NOI18N
    
    private static final String SUSPENDED_INSTANCE_ICON =
            ICONS_BASE + "suspended"; // NOI18N
}

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

import javax.swing.JToolTip;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.BpelProcess;
import org.netbeans.modules.bpel.debugger.api.CorrelationSet;
import org.netbeans.modules.bpel.debugger.api.Fault;
import org.netbeans.modules.bpel.debugger.api.ProcessInstance;
import org.netbeans.modules.bpel.debugger.api.WaitingCorrelatedMessage;
import org.netbeans.modules.bpel.debugger.ui.process.ProcessesTreeModel.FaultsWrapper;
import org.netbeans.modules.bpel.debugger.ui.util.VariablesUtil;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;

/**
 * @author Vladimir Yaroslavskiy
 * @author Kirill Sorokin
 * 
 * @version 2005.10.24
 */
public class ProcessesTableModel implements TableModel {
    
    private BpelDebugger myDebugger;
    private VariablesUtil myVariablesUtil;
    
    /**
     * Creates a new instance of ProcessesTableModel.
     *
     * @param lookupProvider debugger context
     */
    public ProcessesTableModel(
            final ContextProvider lookupProvider) {
        
        myDebugger = lookupProvider.lookupFirst(null, BpelDebugger.class);
        myVariablesUtil = new VariablesUtil(myDebugger);
    }
    
    /**{@inheritDoc}*/
    public Object getValueAt(
            final Object stuff, 
            final String column) throws UnknownTypeException {
        
        Object object = stuff;
        boolean isTooltip = false;
        
        if (stuff instanceof JToolTip) {
            isTooltip = true;
            object = ((JToolTip) stuff).
                        getClientProperty("getShortDescription");
        }
        
        if (column.equals(ProcessesColumnModel_State.COLUMN_ID)) {
            if (object.equals(TreeModel.ROOT)) {
                return "";
            }
            
            if (object instanceof BpelProcess) {
                return "";
            }
            
            if (object instanceof ProcessInstance) {
                switch (((ProcessInstance) object).getState()) {
                    case ProcessInstance.STATE_UNKNOWN:
                        return NbBundle.getMessage(
                                ProcessesTableModel.class,
                                "CTL_Process_State_Unknown"); // NOI18N
                        
                    case ProcessInstance.STATE_RUNNING:
                        return NbBundle.getMessage(
                                ProcessesTableModel.class,
                                "CTL_Process_State_Running"); // NOI18N
                        
                    case ProcessInstance.STATE_COMPLETED:
                        return NbBundle.getMessage(
                                ProcessesTableModel.class,
                                "CTL_Process_State_Completed"); // NOI18N
                        
                    case ProcessInstance.STATE_FAILED:
                        return NbBundle.getMessage(
                                ProcessesTableModel.class,
                                "CTL_Process_State_Failed"); // NOI18N
                        
                    case ProcessInstance.STATE_SUSPENDED:
                        return NbBundle.getMessage(
                                ProcessesTableModel.class,
                                "CTL_Process_State_Suspended"); // NOI18N
                        
                    case ProcessInstance.STATE_TERMINATED:
                        return NbBundle.getMessage(
                                ProcessesTableModel.class,
                                "CTL_Process_State_Terminated"); // NOI18N
                        
                    default:
                        throw new UnknownTypeException(object);
                }
            }
            
            if (object instanceof ProcessesTreeModel.CorrelationSetsWrapper) {
                return "";
            }
            
            if (object instanceof CorrelationSet) {
                return "";
            }
            
            if (object instanceof CorrelationSet.Property) {
                return "";
            }
            
            if (object instanceof ProcessesTreeModel.WaitingMessagesWrapper) {
                return "";
            }
            
            if (object instanceof WaitingCorrelatedMessage) {
                return "";
            }
            
            if (object instanceof FaultsWrapper) {
                return "";
            }
            
            if (object instanceof Fault) {
                return "";
            }
            
            return ""; // We return an empty string for everything, to be
                       // able to list all possible types which occur in the
                       // variable-based children of a fault. Pure lazyness.
        }
        
        if (column.equals(ProcessesColumnModel_Type.COLUMN_ID)) {
            if (object.equals(TreeModel.ROOT)) {
                return "";
            }
            
            if (object instanceof BpelProcess) {
                return "";
            }
            
            if (object instanceof ProcessInstance) {
                return "";
            }
            
            if (object instanceof ProcessesTreeModel.CorrelationSetsWrapper) {
                return "";
            }
            
            if (object instanceof CorrelationSet) {
                return "";
            }
            
            if (object instanceof CorrelationSet.Property) {
                final QName qName = 
                        ((CorrelationSet.Property) object).getType();
                
                if (isTooltip) {
                    final String namespaceUri = qName.getNamespaceURI();
                    
                    if ((namespaceUri == null) || namespaceUri.equals("")) {
                        return qName.getLocalPart();
                    } else {
                        return "{" + namespaceUri + "} " + qName.getLocalPart();
                    }
                } else {
                    final String prefix = qName.getPrefix();
                    
                    if ((prefix == null) || prefix.equals("")) {
                        return qName.getLocalPart();
                    } else {
                        return prefix + ":" + qName.getLocalPart();
                    }
                }
            }
            
            if (object instanceof ProcessesTreeModel.WaitingMessagesWrapper) {
                return "";
            }
            
            if (object instanceof WaitingCorrelatedMessage) {
                return "";
            }
            
            if (object instanceof FaultsWrapper) {
                return "";
            }
            
            if (object instanceof Fault) {
                return "";
            }
            
            final String type = myVariablesUtil.getType(object);
            
            return type == null ? "" : type;
        }
        
        if (column.equals(ProcessesColumnModel_Value.COLUMN_ID)) {
            if (object.equals(TreeModel.ROOT)) {
                return "";
            }
            
            if (object instanceof BpelProcess) {
                return "";
            }
            
            if (object instanceof ProcessInstance) {
                return "";
            }
            
            if (object instanceof ProcessesTreeModel.CorrelationSetsWrapper) {
                return "";
            }
            
            if (object instanceof CorrelationSet) {
                final String value = ((CorrelationSet) object).getValue();
                
                if (value == null) {
                    return NbBundle.getMessage(
                            ProcessesTableModel.class, 
                            "ERR_Corr_Set_Uninitialized", 
                            ((CorrelationSet) object).getName()); // NOI18N
                } else {
                    return value;
                }
            }
            
            if (object instanceof CorrelationSet.Property) {
                return ((CorrelationSet.Property) object).getValue();
            }
            
            if (object instanceof ProcessesTreeModel.WaitingMessagesWrapper) {
                return "";
            }
            
            if (object instanceof WaitingCorrelatedMessage) {
                return "";
            }
            
            if (object instanceof FaultsWrapper) {
                return "";
            }
            
            if (object instanceof Fault) {
                return "";
            }
            
            return object;
        }
        
        throw new UnknownTypeException(object);
    }
    
    /**{@inheritDoc}*/
    public void setValueAt(
            final Object object, 
            final String column, 
            final Object value) throws UnknownTypeException {
        
        return;
    }
    
    /**{@inheritDoc}*/
    public boolean isReadOnly(
            final Object object, 
            final String column) throws UnknownTypeException {
        
        return true;
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
}

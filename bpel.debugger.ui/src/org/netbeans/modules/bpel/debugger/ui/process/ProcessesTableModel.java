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

import org.netbeans.modules.bpel.debugger.api.BpelProcess;
import org.netbeans.modules.bpel.debugger.api.ProcessInstance;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2005.10.24
 */
public class ProcessesTableModel implements TableModel {
    /**{@inheritDoc}*/
    public Object getValueAt(
            final Object object, 
            final String column) throws UnknownTypeException {
        
        if (object.equals(TreeModel.ROOT)) {
            return "";
        }
        
        if (object instanceof BpelProcess) {
            return "";
        }
        
        if (object instanceof ProcessInstance) {
            final ProcessInstance process = (ProcessInstance) object;
            
            if (column.equals(ProcessesColumnModel_State.COLUMN_ID)) {
                switch (process.getState()) {
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
        }
        
        throw new UnknownTypeException(object);
    }
    
    /**{@inheritDoc}*/
    public void setValueAt(
            final Object object, 
            final String column, 
            final Object value) throws UnknownTypeException {
        
        if (object.equals(TreeModel.ROOT)) {
            return;
        }
        
        if (object instanceof BpelProcess) {
            return;
        }
        
        if (object instanceof ProcessInstance) {
            return;
        }
        
        throw new UnknownTypeException(object);
    }
    
    /**{@inheritDoc}*/
    public boolean isReadOnly(
            final Object object, 
            final String column) throws UnknownTypeException {
        
        if (object.equals(TreeModel.ROOT)) {
            return true;
        }
        
        if (object instanceof BpelProcess) {
            return true;
        }
        
        if (object instanceof ProcessInstance) {
            return true;
        }
        
        throw new UnknownTypeException(object);
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

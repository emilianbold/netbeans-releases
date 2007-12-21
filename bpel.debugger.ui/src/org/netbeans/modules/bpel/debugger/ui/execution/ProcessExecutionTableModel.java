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

package org.netbeans.modules.bpel.debugger.ui.execution;


import org.netbeans.modules.bpel.debugger.api.pem.PemEntity;
import org.netbeans.modules.bpel.debugger.api.psm.PsmEntity;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import static org.netbeans.modules.bpel.debugger.ui.execution.Constants.*;

/**
 * Model describing the table structure in the Process Execution View. Since 
 * the view contains only the tree column, this is an empty stub, which only 
 * performs some basic type verification.
 * 
 * @author Alexander Zgursky
 * @author Kirill Sorokin
 */
public class ProcessExecutionTableModel implements TableModel {
    private ContextProvider myContextProvider;

    /**{@inheritDoc}*/
    public ProcessExecutionTableModel(
            final ContextProvider contextProvider) {
        myContextProvider = contextProvider;
    }
    
    /**{@inheritDoc}*/
    public Object getValueAt(
            final Object object, 
            final String column) throws UnknownTypeException {
        if (object instanceof PsmEntity) {
            if (column.equals(ProcessExecutionColumnModel_Thread.COLUMN_ID)) {
                return "";
            }
        }
        
        if (object instanceof PemEntity) {
            if (column.equals(ProcessExecutionColumnModel_Thread.COLUMN_ID)) {
                final String branchId = ((PemEntity) object).getBranchId();
                
                return branchId == null ? "" : branchId;
            }
        }
        
        throw new UnknownTypeException(object);
    }
    
    
    /**{@inheritDoc}*/
    public void setValueAt(
            final Object object, 
            final String column, 
            final Object value) throws UnknownTypeException {
        if (object instanceof PsmEntity) {
            if (column.equals(ProcessExecutionColumnModel_Thread.COLUMN_ID)) {
                return;
            }
        } 
        
        if (object instanceof PemEntity) {
            if (column.equals(ProcessExecutionColumnModel_Thread.COLUMN_ID)) {
                return;
            }
        }
        
        throw new UnknownTypeException(object);
    }
    
    /**{@inheritDoc}*/
    public boolean isReadOnly(
            final Object object, 
            final String column) throws UnknownTypeException {
        if (object instanceof PsmEntity) {
            if (column.equals(ProcessExecutionColumnModel_Thread.COLUMN_ID)) {
                return true;
            }
        }
        
        if (object instanceof PemEntity) {
            if (column.equals(ProcessExecutionColumnModel_Thread.COLUMN_ID)) {
                return true;
            }
        }
            
        throw new UnknownTypeException(object);
    }
    
    /**{@inheritDoc}*/
    public void addModelListener(final ModelListener listener) {
        // des nothing
    }

    /**{@inheritDoc}*/
    public void removeModelListener(final ModelListener listener) {
        // does nothing
    }
}

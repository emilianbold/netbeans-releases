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

package org.netbeans.modules.bpel.debugger.ui.threads;

import java.util.Vector;
import javax.swing.JToolTip;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.pem.ProcessExecutionModel.Branch;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 * Table model supporting the BPEL Partner Links view.
 * 
 * @author Kirill Sorokin
 */
public class ThreadsTableModel implements TableModel, Constants {
    
    private BpelDebugger myDebugger;
    
    private Vector<ModelListener> myListeners = new Vector<ModelListener>();
    
    /**{@inheritDoc}*/
    public ThreadsTableModel(
            final ContextProvider contextProvider) {
        
        myDebugger = contextProvider.lookupFirst(null, BpelDebugger.class);
    }
    
    /**{@inheritDoc}*/
    public Object getValueAt(
            final Object object, 
            final String column) throws UnknownTypeException {
        
        if (object == TreeModel.ROOT) {
            return "";
        }
        
        if (object instanceof ThreadsTreeModel.Dummy) {
            return "";
        }
        
        if (object instanceof JToolTip) {
            final Object realObject = ((JToolTip) object).
                    getClientProperty("getShortDescription"); // NOI18N
                    
            return getValueAt(realObject, column);
        }
        
        if (column.equals(ThreadsColumnModel_State.COLUMN_ID)) {
            if (object instanceof Branch) {
                return ((Branch) object).getState();
            }
        }
        
        throw new UnknownTypeException(object);
    }
    
    
    /**{@inheritDoc}*/
    public void setValueAt(
            final Object object, 
            final String column, 
            final Object value) throws UnknownTypeException {
        
        if (column.equals(ThreadsColumnModel_State.COLUMN_ID)) {
            if (object instanceof Branch) {
                return;
            }
        }
        
        throw new UnknownTypeException(object);
    }
    
    /**{@inheritDoc}*/
    public boolean isReadOnly(
            final Object object, 
            final String column) throws UnknownTypeException {
        
        if (object instanceof ThreadsTreeModel.Dummy) {
            return true;
        }
        
        if (column.equals(ThreadsColumnModel_State.COLUMN_ID)) {
            if (object instanceof Branch) {
                return true;
            }
        }
        
        throw new UnknownTypeException(object);
    }
    
    /**{@inheritDoc}*/
    public void addModelListener(
            final ModelListener listener) {
        
        myListeners.add(listener);
    }
    
    /**{@inheritDoc}*/
    public void removeModelListener(
            final ModelListener listener) {
        
        myListeners.remove(listener);
    }
}

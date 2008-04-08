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

package org.netbeans.modules.bpel.debugger.ui.variable;

import java.util.Vector;
import javax.swing.JToolTip;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.ui.util.VariablesUtil;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 * Table model supporting the Local Variable view.
 * 
 * @author Alexander Zgursky
 * @author Kirill Sorokin
 */
public class LocalsTableModel implements TableModel, Constants {
    
    private BpelDebugger myDebugger;
    private VariablesUtil myHelper;
    
    private Vector<ModelListener> myListeners = new Vector<ModelListener>();
    
    /**{@inheritDoc}*/
    public LocalsTableModel(
            final ContextProvider contextProvider) {
        
        myDebugger = contextProvider.lookupFirst(null, BpelDebugger.class);
        myHelper = new VariablesUtil(myDebugger);
    }
    
    /**{@inheritDoc}*/
    public Object getValueAt(
            final Object object, 
            final String column) throws UnknownTypeException {
        
        if (object == TreeModel.ROOT) {
            return new LocalsTreeModel.Dummy();
        }
        
        if (object instanceof LocalsTreeModel.Dummy) {
            return object;
        }
        
        if (column.equals(LOCALS_VALUE_COLUMN_ID)) {
            if (object instanceof JToolTip) {
                final Object realObject = ((JToolTip) object).
                        getClientProperty("getShortDescription"); // NOI18N
                
                return myHelper.getValueTooltip(realObject);
            }
            
            return new Pair(object, myHelper.getValue(object));
        }
        
        if (column.equals(LOCALS_TYPE_COLUMN_ID)) {
            if (object instanceof JToolTip) {
                final Object realObject = ((JToolTip) object).
                        getClientProperty("getShortDescription"); // NOI18N
                
                return myHelper.getTypeTooltip(realObject);
            }
            
            return object;
        }
        
        if (column.equals(LOCALS_TO_STRING_COLUMN_ID)) {
            return object.toString();
        }
        
        throw new UnknownTypeException(object);
    }
    
    
    /**{@inheritDoc}*/
    public void setValueAt(
            final Object object, 
            final String column, 
            final Object value) throws UnknownTypeException {
        
        if (object instanceof LocalsTreeModel.Dummy) {
            return;
        }
        
        if (column.equals(LOCALS_VALUE_COLUMN_ID)) {
            // All the necessary type checking will be done in the 
            // VariablesUtils, thus we don't need it here (132133)
            myHelper.setValue(object, ((Pair) value).getValue());
            
            fireTableValueChanged(object, LOCALS_VALUE_COLUMN_ID);
            return;
        }
        
        throw new UnknownTypeException(object);
    }
    
    /**{@inheritDoc}*/
    public boolean isReadOnly(
            final Object object, 
            final String column) throws UnknownTypeException {
        
        if (object instanceof LocalsTreeModel.Dummy) {
            return true;
        }
        
        if (column.equals(LOCALS_VALUE_COLUMN_ID)) {
            return myHelper.isValueReadOnly(object);
        }
        
        if (column.equals(LOCALS_TYPE_COLUMN_ID)) {
            return true;
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
    
    // Private /////////////////////////////////////////////////////////////////
    private void fireTableValueChanged(
            final Object node, 
            final String columnId) {
        
        final Vector clone = (Vector) myListeners.clone();
        final ModelEvent.TableValueChanged event = 
                new ModelEvent.TableValueChanged(this, node, columnId);
        
        for (int i = 0; i < clone.size(); i++) {
            ((ModelListener) clone.get(i)).modelChanged(event);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class Pair {
        
        private Object key;
        private String value;
        
        public Pair(
                final Object key, 
                final String value) {
            this.key = key;
            this.value = value;
        }
        
        public Object getKey() {
            return key;
        }
        
        public String getValue() {
            return value;
        }
    }
}

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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.debug.variablesfilterring;

import org.netbeans.modules.web.debug.variablesfilterring.JSPVariablesFilter.AttributeMap;
import org.netbeans.spi.viewmodel.TableModel;

import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 *
 * @author Libor Kotouc
 */
public class JSPVariablesTableModelFilter implements TableModelFilter {

    public JSPVariablesTableModelFilter() {
    }

    /**
     * Returns filterred value to be displayed in column <code>columnID</code>
     * and row <code>node</code>. Column ID is defined in by 
     * {@link ColumnModel#getID}, and rows are defined by values returned from 
     * {@TreeModel#getChildren}. You should not throw UnknownTypeException
     * directly from this method!
     *
     * @param   original the original table model
     * @param   node a object returned from {@TreeModel#getChildren} for this row
     * @param   columnID a id of column defined by {@link ColumnModel#getID}
     * @throws  ComputingException if the value is not known yet and will 
     *          be computed later
     * @throws  UnknownTypeException this exception can be thrown from 
     *          <code>original.getValueAt (...)</code> method call only!
     *
     * @return value of variable representing given position in tree table.
     */
    public Object getValueAt(TableModel original, Object node, String columnID)
    throws UnknownTypeException
    {
        
        Object colValue = "";
        if (node instanceof JSPVariablesFilter.AttributeMap.Attribute)
            colValue = original.getValueAt(((AttributeMap.Attribute)node).getValue(), columnID);
        else if (node instanceof JSPVariablesFilter.AttributeMap ||
                 node instanceof JSPVariablesFilter.ImplicitLocals)
            colValue = "";
        else
            colValue = original.getValueAt(node, columnID);
        
        return colValue;
    }
    
    /**
     * Changes a value displayed in column <code>columnID</code>
     * and row <code>node</code>. Column ID is defined in by 
     * {@link ColumnModel#getID}, and rows are defined by values returned from 
     * {@TreeModel#getChildren}. You should not throw UnknownTypeException
     * directly from this method!
     *
     * @param  original the original table model
     * @param  node a object returned from {@TreeModel#getChildren} for this row
     * @param  columnID a id of column defined by {@link ColumnModel#getID}
     * @param  value a new value of variable on given position
     * @throws  UnknownTypeException this exception can be thrown from 
     *          <code>original.setValueAt (...)</code> method call only!
     */
    public void setValueAt(TableModel original, Object node, String columnID, Object value)
    throws UnknownTypeException
    {
            original.setValueAt(node, columnID, value);
    }

    /**
     * Filters original isReadOnly value from given table model. You should 
     * not throw UnknownTypeException
     * directly from this method!
     *
     * @param  original the original table model
     * @param  node a object returned from {@TreeModel#getChildren} for this row
     * @param  columnID a id of column defined by {@link ColumnModel#getID}
     * @throws  UnknownTypeException this exception can be thrown from 
     *          <code>original.isReadOnly (...)</code> method call only!
     *
     * @return true if variable on given position is read only
     */
    public boolean isReadOnly(TableModel original, Object node, String columnID)
    throws UnknownTypeException
    {
        boolean ro = true;
        if (node instanceof JSPVariablesFilter.AttributeMap ||
                 node instanceof JSPVariablesFilter.ImplicitLocals ||
                 node instanceof JSPVariablesFilter.AttributeMap.Attribute)
            ro = true;
        else
            ro = original.isReadOnly(node, columnID);
        
        return ro;
    }

    public void removeModelListener(org.netbeans.spi.viewmodel.ModelListener l) {
    }

    public void addModelListener(org.netbeans.spi.viewmodel.ModelListener l) {
    }

}

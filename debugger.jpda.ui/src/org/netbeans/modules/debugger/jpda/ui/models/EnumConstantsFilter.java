/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.ui.models;

import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.spi.debugger.jpda.VariablesFilterAdapter;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;


/**
 * Filter for instances of enumetaion types. Instead of #20 it displays the name of the constant.
 *
 * @author Maros Sandor
 */
public class EnumConstantsFilter extends VariablesFilterAdapter {

    public String[] getSupportedTypes () {
        return new String[] {
        };
    }
    
    public String[] getSupportedAncestors () {
        return new String[] {
            "java.lang.Enum"
        };
    }
    
    /**
     * Returns true.
     * 
     * @param   original the original tree model
     * @param   variable an enumeraion constant
     * @return  true
     */
    public boolean isLeaf (TreeModel original, Variable variable) throws UnknownTypeException {
        return true;
    }
    
    public Object getValueAt (
        TableModel original, 
        Variable variable, 
        String columnID
    ) throws UnknownTypeException {
        
        ObjectVariable ov = (ObjectVariable) variable;
        if ( columnID == Constants.LOCALS_VALUE_COLUMN_ID || 
             columnID == Constants.WATCH_VALUE_COLUMN_ID
        ) {
            try {
                return ov.getToStringValue ();
            } catch (InvalidExpressionException ex) {
                return ex.getLocalizedMessage ();
            }
        } else if (columnID == Constants.LOCALS_TYPE_COLUMN_ID || columnID == Constants.WATCH_TYPE_COLUMN_ID) {
            String typeName = ov.getType();
            int idx = typeName.lastIndexOf("$");
            if (idx != -1) {
                return typeName.substring(idx + 1);
            }
            idx = typeName.lastIndexOf(".");
            if (idx != -1) {
                return typeName.substring(idx + 1);
            }
        }
        return original.getValueAt (variable, columnID);
    }
}

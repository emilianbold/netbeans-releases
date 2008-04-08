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

package org.netbeans.modules.bpel.debugger.ui.watch;


import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.variables.NamedValueHost;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.w3c.dom.Node;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2005.10.28
 */
public class WatchesTableModel implements TableModel, Constants {
    
    private ContextProvider myContextProvider;
    private BpelDebugger myDebugger;
    private Util myHelper;
    
    /**{@inheritDoc}*/
    public WatchesTableModel(
            final ContextProvider contextProvider) {
        
        myContextProvider = contextProvider;
        myDebugger = contextProvider.lookupFirst(null, BpelDebugger.class);
        myHelper = new Util(myDebugger);
    }
    
    /**{@inheritDoc}*/
    public Object getValueAt(
            final Object object, 
            final String column) throws UnknownTypeException {
        
        if (column.equals(WATCH_VALUE_COLUMN_ID)) {
            if (object instanceof BpelWatch) {
                final BpelWatch bpelWatch = (BpelWatch) object;
                
                if (bpelWatch.getValue() != null) {
                    return myHelper.toString(bpelWatch.getValue());
                }
                
                // If we did not get the value in an ordinary way, it could be 
                // a variable expression. Check if it starts with a "$", 
                // prepend if it not and try to fetch the value
                String expression = bpelWatch.getExpression();
                if (!expression.startsWith("$")) {
                    expression = "$" + expression;
                }
                
                final Object value = 
                        myHelper.getValue(expression);
                if (value != null) {
                    if (value instanceof Node) {
                        return myHelper.toString((Node) value);
                    } else {
                        return value;
                    }
                }
                
                if (bpelWatch.getException() != null) {
                    return " > " + 
                            bpelWatch.getException().getMessage() + " < ";
                }
                
                return "";
            }
            
            if (object instanceof NamedValueHost) {
                return myHelper.getVariablesUtil().getValue(object);
            }
            
            if (object instanceof Node) {
                return myHelper.getVariablesUtil().getValue(object);
            }
        }
        
        throw new UnknownTypeException(object);
    }
    
    /**{@inheritDoc}*/
    public void setValueAt(
            final Object object, 
            final String column, 
            final Object value) throws UnknownTypeException {
        // does nothing
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
        // does nothing
    }

    /**{@inheritDoc}*/
    public void removeModelListener(
            final ModelListener listener) {
        // does nothing
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String WATCH =
        "org/netbeans/modules/debugger/resources/watchesView/Watch"; // NOI18N
}

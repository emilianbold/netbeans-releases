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

import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAWatch;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;


/**
 * @author   Jan Jancura
 */
public class WatchesNodeModel extends VariablesNodeModel { 

    public static final String WATCH =
        "org/netbeans/modules/debugger/resources/watchesView/Watch";


    public WatchesNodeModel (ContextProvider lookupProvider) {
        super (lookupProvider);
    }
    
    public String getDisplayName (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT)
            return NbBundle.getBundle (WatchesNodeModel.class).
                getString ("CTL_WatchesModel_Column_Name_Name");
        if (o instanceof JPDAWatch)
            return ((JPDAWatch) o).getExpression ();
        return super.getDisplayName (o);
    }
    
    public String getShortDescription (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT)
            return TreeModel.ROOT;
        if (o instanceof JPDAWatch) {
            JPDAWatch w = (JPDAWatch) o;
            boolean evaluated;
            synchronized (VariablesTreeModelFilter.evaluatedNodes) {
                evaluated = VariablesTreeModelFilter.evaluatedNodes.contains(o);
            }
            if (!evaluated) {
                return w.getExpression ();
            }
            String e = w.getExceptionDescription ();
            if (e != null)
                return w.getExpression () + " = >" + e + "<";
            String t = w.getType ();
            if (t == null)
                return w.getExpression () + " = " + w.getValue ();
            else
                try {
                    return w.getExpression () + " = (" + w.getType () + ") " + 
                        w.getToStringValue ();
                } catch (InvalidExpressionException ex) {
                    return ex.getLocalizedMessage ();
                }
        }
        return super.getShortDescription (o);
    }
    
    public String getIconBase (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT)
            return WATCH;
        if (o instanceof JPDAWatch)
            return WATCH;
        return super.getIconBase (o);
    }

    public void addModelListener (ModelListener l) {
    }

    public void removeModelListener (ModelListener l) {
    }
}

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

import org.netbeans.api.debugger.LookupProvider;
import org.netbeans.api.debugger.jpda.JPDAWatch;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.modules.debugger.jpda.ui.FixedWatch;


/**
 * @author   Jan Jancura
 */
public class WatchesNodeModel extends VariablesNodeModel { 

    public static final String WATCH =
        "org/netbeans/modules/debugger/resources/watchesView/Watch";
    public static final String FIXED_WATCH =
        "org/netbeans/modules/debugger/resources/watchesView/FixedWatch";


    public WatchesNodeModel (LookupProvider lookupProvider) {
        super (lookupProvider);
    }
    
    public String getDisplayName (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT)
            return "Name";
        if (o instanceof JPDAWatch)
            return ((JPDAWatch) o).getExpression ();
        if (o instanceof FixedWatch)
            return ((FixedWatch) o).getName();
        return super.getDisplayName (o);
    }
    
    public String getShortDescription (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT)
            return TreeModel.ROOT;
        if (o instanceof JPDAWatch) {
            JPDAWatch w = (JPDAWatch) o;
            String t = w.getType ();
            String e = w.getExceptionDescription ();
            if (e != null)
                return w.getExpression () + " = >" + e + "<";
            if (t == null)
                return w.getExpression () + " = " + w.getValue ();
            else
                return w.getExpression () + " = (" + w.getType () + ") " + 
                    w.getToStringValue ();
        }
        if (o instanceof FixedWatch) {
            FixedWatch fw = (FixedWatch) o;
            return fw.getName() + " = (" + fw.getType() + ") " + fw.getValue();
        }
        return super.getShortDescription (o);
    }
    
    public String getIconBase (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT)
            return WATCH;
        if (o instanceof JPDAWatch)
            return WATCH;
        if (o instanceof FixedWatch)
            return FIXED_WATCH;
        return super.getIconBase (o);
    }

    public void addTreeModelListener (TreeModelListener l) {
    }

    public void removeTreeModelListener (TreeModelListener l) {
    }
}

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

package org.netbeans.modules.debugger.ui.models;

import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 * @author   Jan Jancura
 */
public class BreakpointsNodeModel implements NodeModel {

    public static final String BREAKPOINT_GROUP =
        "org/netbeans/modules/debugger/resources/breakpointsView/Breakpoint";

    
    public String getDisplayName (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return "Name";
        } else
        if (o instanceof String) {
            return (String) o;
        } else
        throw new UnknownTypeException (o);
    }
    
    public String getShortDescription (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return TreeModel.ROOT;
        } else
        if (o instanceof String) {
            return "Group of Breakpoints";
        } else
        throw new UnknownTypeException (o);
    }
    
    public String getIconBase (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return BREAKPOINT_GROUP;
        } else
        if (o instanceof String) {
            return BREAKPOINT_GROUP;
        } else
        throw new UnknownTypeException (o);
    }

    public void addTreeModelListener (TreeModelListener l) {
    }

    public void removeTreeModelListener (TreeModelListener l) {
    }
}

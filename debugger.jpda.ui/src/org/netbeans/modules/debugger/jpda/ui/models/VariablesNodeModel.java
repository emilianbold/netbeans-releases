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

import java.util.Vector;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.LookupProvider;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.LocalVariable;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Super;
import org.netbeans.api.debugger.jpda.This;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;


/**
 * @author   Jan Jancura
 */
public class VariablesNodeModel implements NodeModel { 

    public static final String FIELD =
        "org/netbeans/modules/debugger/resources/watchesView/Field";
    public static final String LOCAL =
        "org/netbeans/modules/debugger/resources/localsView/LocalVariable";
    public static final String FIXED_WATCH =
        "org/netbeans/modules/debugger/resources/watchesView/FixedWatch";
    public static final String STATIC_FIELD =
        "org/netbeans/modules/debugger/resources/watchesView/StaticField";
    public static final String SUPER =
        "org/netbeans/modules/debugger/resources/watchesView/SuperVariable";

    
    private JPDADebugger debugger;
    
    
    public VariablesNodeModel (LookupProvider lookupProvider) {
        debugger = (JPDADebugger) lookupProvider.
            lookupFirst (JPDADebugger.class);
    }
    
    
    public String getDisplayName (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT)
            return "Name";
        if (o instanceof Field)
            return ((Field) o).getName ();
        if (o instanceof LocalVariable)
            return ((LocalVariable) o).getName ();
        if (o instanceof Super)
            return "super";
        if (o instanceof This)
            return "this";
        throw new UnknownTypeException (o);
    }
    
    public String getShortDescription (Object o) 
    throws UnknownTypeException {
        if (o == TreeModel.ROOT)
            return "Locals Root";
        if (o instanceof Field) {
            if (o instanceof ObjectVariable) {
                String type = ((ObjectVariable) o).getType ();
                String declaredType = ((Field) o).getDeclaredType ();
                if (type.equals (declaredType))
                    return "(" + type + ") " + 
                        ((ObjectVariable) o).getToStringValue ();
                else
                    return "(" + declaredType + ") " + "(" + type + ") " + 
                        ((ObjectVariable) o).getToStringValue ();
            } else
                return "(" + ((Field) o).getDeclaredType () + ") " + 
                    ((Field) o).getValue ();
        }
        if (o instanceof LocalVariable) {
            if (o instanceof ObjectVariable) {
                String type = ((ObjectVariable) o).getType ();
                String declaredType = ((LocalVariable) o).getDeclaredType ();
                if (type.equals (declaredType))
                    return "(" + type + ") " + 
                        ((ObjectVariable) o).getToStringValue ();
                else
                    return "(" + declaredType + ") " + "(" + type + ") " + 
                        ((ObjectVariable) o).getToStringValue ();
            } else
                return "(" + ((LocalVariable) o).getDeclaredType () + ") " + 
                    ((LocalVariable) o).getValue ();
        }
        if (o instanceof Super)
            return ((Super) o).getType ();
        if (o instanceof This)
            return "(" + ((This) o).getType () + ") " + 
                ((This) o).getToStringValue ();
        throw new UnknownTypeException (o);
    }
    
    public String getIconBase (Object o) throws UnknownTypeException {
        if (o instanceof String)
            return FIELD;
        if (o instanceof Field)
            return FIELD;
        if (o instanceof LocalVariable)
            return LOCAL;
        if (o instanceof Super)
            return SUPER;
        if (o instanceof This)
            return FIELD;
        throw new UnknownTypeException (o);
    }

    public void addTreeModelListener (TreeModelListener l) {
    }

    public void removeTreeModelListener (TreeModelListener l) {
    }
}

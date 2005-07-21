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
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.LocalVariable;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Super;
import org.netbeans.api.debugger.jpda.This;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;


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
    
    
    public VariablesNodeModel (ContextProvider lookupProvider) {
        debugger = (JPDADebugger) lookupProvider.
            lookupFirst (null, JPDADebugger.class);
    }
    
    
    public String getDisplayName (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT)
            return NbBundle.getBundle (VariablesNodeModel.class).getString 
                ("CTL_LocalsModel_Column_Name_Name");
        if (o instanceof Field)
            return ((Field) o).getName ();
        if (o instanceof LocalVariable)
            return ((LocalVariable) o).getName ();
        if (o instanceof Super)
            return "super"; // NOI18N
        if (o instanceof This)
            return "this"; // NOI18N
        if (o == "NoInfo") // NOI18N
            return NbBundle.getMessage(VariablesNodeModel.class, "CTL_No_Info");
        if (o == "No current thread") { // NOI18N
            return NbBundle.getMessage(VariablesNodeModel.class, "NoCurrentThreadVar");
        }
        String str = o.toString();
        if (str.startsWith("SubArray")) { // NOI18N
            int index = str.indexOf('-');
            //int from = Integer.parseInt(str.substring(8, index));
            //int to = Integer.parseInt(str.substring(index + 1));
            return NbBundle.getMessage (VariablesNodeModel.class,
                    "CTL_LocalsModel_Column_Name_SubArray",
                    str.substring(8, index), str.substring(index + 1));
        }
        throw new UnknownTypeException (o);
    }
    
    public String getShortDescription (Object o) 
    throws UnknownTypeException {
        if (o == TreeModel.ROOT)
            return NbBundle.getBundle(VariablesNodeModel.class).getString("CTL_LocalsModel_Column_Name_Desc");
        if (o instanceof Field) {
            if (o instanceof ObjectVariable) {
                String type = ((ObjectVariable) o).getType ();
                String declaredType = ((Field) o).getDeclaredType ();
                if (type.equals (declaredType))
                    try {
                        return "(" + type + ") " + 
                            ((ObjectVariable) o).getToStringValue ();
                    } catch (InvalidExpressionException ex) {
                        return ex.getLocalizedMessage ();
                    }
                else
                    try {
                        return "(" + declaredType + ") " + "(" + type + ") " + 
                            ((ObjectVariable) o).getToStringValue ();
                    } catch (InvalidExpressionException ex) {
                        return ex.getLocalizedMessage ();
                    }
            } else
                return "(" + ((Field) o).getDeclaredType () + ") " + 
                    ((Field) o).getValue ();
        }
        if (o instanceof LocalVariable) {
            if (o instanceof ObjectVariable) {
                String type = ((ObjectVariable) o).getType ();
                String declaredType = ((LocalVariable) o).getDeclaredType ();
                if (type.equals (declaredType))
                    try {
                        return "(" + type + ") " + 
                            ((ObjectVariable) o).getToStringValue ();
                    } catch (InvalidExpressionException ex) {
                        return ex.getLocalizedMessage ();
                    }
                else
                    try {
                        return "(" + declaredType + ") " + "(" + type + ") " + 
                            ((ObjectVariable) o).getToStringValue ();
                    } catch (InvalidExpressionException ex) {
                        return ex.getLocalizedMessage ();
                    }
            } else
                return "(" + ((LocalVariable) o).getDeclaredType () + ") " + 
                    ((LocalVariable) o).getValue ();
        }
        if (o instanceof Super)
            return ((Super) o).getType ();
        if (o instanceof This)
            try {
                return "(" + ((This) o).getType () + ") " + 
                    ((This) o).getToStringValue ();
            } catch (InvalidExpressionException ex) {
                return ex.getLocalizedMessage ();
            }
        String str = o.toString();
        if (str.startsWith("SubArray")) { // NOI18N
            int index = str.indexOf('-');
            return NbBundle.getMessage (VariablesNodeModel.class,
                    "CTL_LocalsModel_Column_Descr_SubArray",
                    str.substring(8, index), str.substring(index + 1));
        }
        if (o == "NoInfo") // NOI18N
            return NbBundle.getMessage(VariablesNodeModel.class, "CTL_No_Info_descr");
        if (o == "No current thread") { // NOI18N
            return NbBundle.getMessage(VariablesNodeModel.class, "NoCurrentThreadVar");
        }
        throw new UnknownTypeException (o);
    }
    
    public String getIconBase (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT)
            return FIELD;
        if (o instanceof Field) {
            if (((Field) o).isStatic ())
                return STATIC_FIELD;
            else
                return FIELD;
        }
        if (o instanceof LocalVariable)
            return LOCAL;
        if (o instanceof Super)
            return SUPER;
        if (o instanceof This)
            return FIELD;
        if (o.toString().startsWith("SubArray")) // NOI18N
            return LOCAL;
        if (o == "NoInfo" || o == "No current thread") // NOI18N
            return null;
        throw new UnknownTypeException (o);
    }

    public void addModelListener (ModelListener l) {
    }

    public void removeModelListener (ModelListener l) {
    }
}

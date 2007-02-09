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

package org.netbeans.modules.debugger.jpda.ui.models;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.jpda.ClassVariable;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.ReturnVariable;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.LocalVariable;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Super;
import org.netbeans.api.debugger.jpda.This;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


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
    public static final String STATIC =
        "org/netbeans/modules/debugger/resources/watchesView/SuperVariable";
    public static final String RETURN =
        "org/netbeans/modules/debugger/jpda/resources/Filter";

    
    private JPDADebugger debugger;
    
    private RequestProcessor evaluationRP = new RequestProcessor();
    private final Collection modelListeners = new HashSet();
    
    
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
        if (o instanceof JPDAClassType) {
            return NbBundle.getMessage(VariablesNodeModel.class, "MSG_VariablesFilter_StaticNode");    // NOI18N
        }
        if (o instanceof ClassVariable) {
            return "class";
        }
        if (o instanceof ReturnVariable) {
            return "return "+((ReturnVariable) o).methodName()+"()";
        }
        if (o == "lastOperations") { // NOI18N
            return NbBundle.getMessage(VariablesNodeModel.class, "lastOperationsNode");
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
    
    private Map shortDescriptionMap = new HashMap();
    
    public String getShortDescription (final Object o) throws UnknownTypeException {
        synchronized (shortDescriptionMap) {
            Object shortDescription = shortDescriptionMap.remove(o);
            if (shortDescription instanceof String) {
                return (String) shortDescription;
            } else if (shortDescription instanceof UnknownTypeException) {
                throw (UnknownTypeException) shortDescription;
            }
        }
        testKnown(o);
        // Called from AWT - we need to postpone the work...
        evaluationRP.post(new Runnable() {
            public void run() {
                Object shortDescription = getShortDescriptionSynch(o);
                if (shortDescription != null && !"".equals(shortDescription)) {
                    synchronized (shortDescriptionMap) {
                        shortDescriptionMap.put(o, shortDescription);
                    }
                    fireModelChange(new ModelEvent.NodeChanged(VariablesNodeModel.this,
                        o, ModelEvent.NodeChanged.SHORT_DESCRIPTION_MASK));
                }
            }
        });
        return "";
    }
    
    private String getShortDescriptionSynch (Object o) {
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
        if (o instanceof JPDAClassType) {
            return NbBundle.getMessage(VariablesNodeModel.class, "MSG_VariablesFilter_StaticNode_descr");    // NOI18N
        }
        if (o instanceof ClassVariable) {
            return NbBundle.getMessage(VariablesNodeModel.class, "MSG_VariablesFilter_Class_descr");    // NOI18N
        }
        if (o instanceof ReturnVariable) {
            return NbBundle.getMessage(VariablesNodeModel.class, "MSG_VariablesFilter_Return_descr", ((ReturnVariable) o).methodName()+"()");    // NOI18N
        }
        if (o == "lastOperations") { // NOI18N
            return NbBundle.getMessage(VariablesNodeModel.class, "MSG_LastOperations_descr");
        }
        return null;
        //throw new UnknownTypeException (o);
    }
    
    private void testKnown(Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) return ;
        if (o instanceof Field) return ;
        if (o instanceof LocalVariable) return ;
        if (o instanceof Super) return ;
        if (o instanceof This) return ;
        String str = o.toString();
        if (str.startsWith("SubArray")) return ; // NOI18N
        if (o == "NoInfo") return ; // NOI18N
        if (o == "No current thread") return ; // NOI18N
        if (o == "lastOperations") return ; // NOI18N
        if (o instanceof JPDAClassType) return ;
        if (o instanceof ClassVariable) return ;
        if (o instanceof ReturnVariable) return ;
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
        if (o instanceof JPDAClassType) {
            return STATIC;
        }
        if (o instanceof ClassVariable) {
            return STATIC;
        }
        if (o instanceof ReturnVariable || o == "lastOperations") {
            return RETURN;
        }
        if (o.toString().startsWith("SubArray")) // NOI18N
            return LOCAL;
        if (o == "NoInfo" || o == "No current thread") // NOI18N
            return null;
        throw new UnknownTypeException (o);
    }

    public void addModelListener (ModelListener l) {
        synchronized (modelListeners) {
            modelListeners.add(l);
        }
    }

    public void removeModelListener (ModelListener l) {
        synchronized (modelListeners) {
            modelListeners.remove(l);
        }
    }
    
    private void fireModelChange(ModelEvent me) {
        Object[] listeners;
        synchronized (modelListeners) {
            listeners = modelListeners.toArray();
        }
        for (int i = 0; i < listeners.length; i++) {
            ((ModelListener) listeners[i]).modelChanged(me);
        }
    }
}

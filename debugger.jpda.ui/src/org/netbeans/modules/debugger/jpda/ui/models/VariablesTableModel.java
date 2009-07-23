/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.debugger.jpda.ui.models;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.security.auth.Refreshable;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAWatch;
import org.netbeans.api.debugger.jpda.LocalVariable;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Super;
import org.netbeans.api.debugger.jpda.This;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.ui.views.VariablesViewButtons;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.jpda.EditorContext.Operation;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;


/**
 *
 * @author   Jan Jancura
 */
public class VariablesTableModel implements TableModel, Constants {
    
    private JPDADebugger debugger;
    private Set<ModelListener> listeners = new HashSet<ModelListener>();
    private Preferences preferences = NbPreferences.forModule(VariablesViewButtons.class).node(VariablesViewButtons.PREFERENCES_NAME);

    public VariablesTableModel(ContextProvider contextProvider) {
        debugger = contextProvider.lookupFirst(null, JPDADebugger.class);
    }
    
    public Object getValueAt (Object row, String columnID) throws 
    UnknownTypeException {
        
        if ( LOCALS_TO_STRING_COLUMN_ID.equals (columnID) ||
             WATCH_TO_STRING_COLUMN_ID.equals (columnID)
        ) {
            if (row instanceof Super)
                return "";
            else

            if (row instanceof ObjectVariable)
                try {
                    return ((ObjectVariable) row).getToStringValue ();
                } catch (InvalidExpressionException ex) {
                    return getMessage (ex);
                } finally {
                    fireChildrenChange(row);
                }
            else
            if (row instanceof Variable) {
                try {
                    return ((Variable) row).getValue ();
                } finally {
                    fireChildrenChange(row);
                }
            }
            if (row instanceof Operation ||
                row == "lastOperations" || // NOI18N
                row instanceof String && ((String) row).startsWith("operationArguments ")) { // NOI18N
                
                return ""; // NOI18N
            }
        } else
        if ( LOCALS_TYPE_COLUMN_ID.equals (columnID) ||
             WATCH_TYPE_COLUMN_ID.equals (columnID)
        ) {
            if (row instanceof Variable)
                return getShort (((Variable) row).getType ());
            if (row instanceof javax.swing.JToolTip) {
                row = ((javax.swing.JToolTip) row).getClientProperty("getShortDescription");
                if (row instanceof Variable) {
                    if (row instanceof Refreshable && !((Refreshable) row).isCurrent()) {
                        return "";
                    }
                    return ((Variable) row).getType();
                }
            }
        } else
        if ( LOCALS_VALUE_COLUMN_ID.equals (columnID) ||
             WATCH_VALUE_COLUMN_ID.equals (columnID)
        ) {
            if (VariablesViewButtons.isShowValuesAsString()) {
                return getValueAt(row, LOCALS_TO_STRING_COLUMN_ID);
            }
            if (row instanceof JPDAWatch) {
                JPDAWatch w = (JPDAWatch) row;
                String e = w.getExceptionDescription ();
                if (e != null)
                    return BoldVariablesTableModelFilterFirst.toHTML(">" + e + "<", false, false, Color.RED);
                try {
                        return w.getValue ();
                } finally {
                    fireChildrenChange(row);
                }
            } else 
            if (row instanceof Variable) {
                try {
                    return ((Variable) row).getValue ();
                } finally {
                    fireChildrenChange(row);
                }
            }
        }
        if (row instanceof JPDAClassType) {
            return ""; // NOI18N
        }
        if (row.toString().startsWith("SubArray")) { // NOI18N
            return ""; // NOI18N
        }
        if (row instanceof Operation ||
            row == "lastOperations" || // NOI18N
            row instanceof String && ((String) row).startsWith("operationArguments ")) { // NOI18N

            return ""; // NOI18N
        }
        if (row == "noDebugInfoWarning") { // NOI18N
            return ""; // NOI18N
        }
        throw new UnknownTypeException (row);
    }
    
    public boolean isReadOnly (Object row, String columnID) throws 
    UnknownTypeException {
        if (row instanceof Variable) {
            if ( LOCALS_TO_STRING_COLUMN_ID.equals (columnID) ||
                 WATCH_TO_STRING_COLUMN_ID.equals (columnID) ||
                 LOCALS_TYPE_COLUMN_ID.equals (columnID) ||
                 WATCH_TYPE_COLUMN_ID.equals (columnID)
            ) return true;
            if ( LOCALS_VALUE_COLUMN_ID.equals (columnID) ||
                 WATCH_VALUE_COLUMN_ID.equals (columnID) 
            ) {
                if (row instanceof This)
                    return true;
                else {
                    if (row instanceof JPDAWatch && row instanceof Refreshable) {
                        if (!((Refreshable) row).isCurrent()) {
                            return true;
                        }
                        try {
                            // Retrieve the evaluated watch so that we can test if it's an object variable or not.
                            java.lang.reflect.Method getEvaluatedWatchMethod = row.getClass().getDeclaredMethod("getEvaluatedWatch");
                            getEvaluatedWatchMethod.setAccessible(true);
                            row = (JPDAWatch) getEvaluatedWatchMethod.invoke(row);
                        } catch (Exception ex) {
                            // Log it only
                            ex.printStackTrace();
                        }
                    }
                    if (row instanceof ObjectVariable) {
                        String declaredType;
                        if (row instanceof LocalVariable) {
                            declaredType = ((LocalVariable) row).getDeclaredType();
                        } else if (row instanceof Field) {
                            declaredType = ((Field) row).getDeclaredType();
                        } else {
                            declaredType = ((ObjectVariable) row).getType();
                        }
                        // Allow to edit Strings
                        if (!"java.lang.String".equals(declaredType)) { // NOI18N
                            return true;
                        }
                    }
                    if ( row instanceof LocalVariable ||
                         row instanceof Field ||
                         row instanceof JPDAWatch
                    ) {
                        if (WatchesNodeModel.isEmptyWatch(row)) {
                            return true;
                        } else {
                            return !debugger.canBeModified();
                        }
                    } else {
                        return true;
                    }
                }
            }
        }
        if (row instanceof JPDAClassType) {
            return true;
        }
        if (row.toString().startsWith("SubArray")) {
            return true;
        }
        if (row instanceof Operation) {
            return true;
        }
        if (row == "noDebugInfoWarning") {
            return true;
        }
        throw new UnknownTypeException (row);
    }
    
    public void setValueAt (Object row, String columnID, Object value) 
    throws UnknownTypeException {
        if (row instanceof LocalVariable) {
            if (LOCALS_VALUE_COLUMN_ID.equals (columnID)) {
                try {
                    ((LocalVariable) row).setValue ((String) value);
                } catch (InvalidExpressionException e) {
                    NotifyDescriptor.Message descriptor = 
                        new NotifyDescriptor.Message (
                            e.getLocalizedMessage (), 
                            NotifyDescriptor.WARNING_MESSAGE
                        );
                    DialogDisplayer.getDefault ().notify (descriptor);
                }
                return;
            }
        }
        if (row instanceof Field) {
            if ( LOCALS_VALUE_COLUMN_ID.equals (columnID) ||
                 WATCH_VALUE_COLUMN_ID.equals (columnID)
            ) {
                try {
                    ((Field) row).setValue ((String) value);
                } catch (InvalidExpressionException e) {
                    NotifyDescriptor.Message descriptor = 
                        new NotifyDescriptor.Message (
                            e.getLocalizedMessage (), 
                            NotifyDescriptor.WARNING_MESSAGE
                        );
                    DialogDisplayer.getDefault ().notify (descriptor);
                }
                return;
            }
        }
        if (row instanceof JPDAWatch) {
            if ( LOCALS_VALUE_COLUMN_ID.equals (columnID) ||
                 WATCH_VALUE_COLUMN_ID.equals (columnID)
            ) {
                try {
                    ((JPDAWatch) row).setValue ((String) value);
                } catch (InvalidExpressionException e) {
                    NotifyDescriptor.Message descriptor = 
                        new NotifyDescriptor.Message (
                            e.getLocalizedMessage (), 
                            NotifyDescriptor.WARNING_MESSAGE
                        );
                    DialogDisplayer.getDefault ().notify (descriptor);
                }
                return;
            }
        }
        throw new UnknownTypeException (row);
    }
    
    private void fireChildrenChange(Object row) {
        Set<ModelListener> ls;
        synchronized(listeners) {
            ls = new HashSet(listeners);
        }
        for (ModelListener ml : ls)
            ml.modelChanged (
                new ModelEvent.NodeChanged(this, row, ModelEvent.NodeChanged.CHILDREN_MASK)
            );
    }
    
    /** 
     * Registers given listener.
     * 
     * @param l the listener to add
     */
    public void addModelListener (ModelListener l) {
        synchronized(listeners) {
            listeners.add(l);
        }
    }

    /** 
     * Unregisters given listener.
     *
     * @param l the listener to remove
     */
    public void removeModelListener (ModelListener l) {
        synchronized(listeners) {
            listeners.remove(l);
        }
    }
    
    static String getShort (String c) {
        int i = c.lastIndexOf ('.');
        if (i < 0) return c;
        return c.substring (i + 1);
    }
    
    static String getMessage (InvalidExpressionException e) {
        String m = e.getLocalizedMessage ();
        if (m == null) {
            m = e.getMessage ();
        }
        if (m == null) {
            m = NbBundle.getMessage(VariablesTableModel.class, "MSG_NA");
        }
        Throwable t = e.getTargetException();
        if (t != null && t instanceof org.omg.CORBA.portable.ApplicationException) {
            java.io.StringWriter s = new java.io.StringWriter();
            java.io.PrintWriter p = new java.io.PrintWriter(s);
            t.printStackTrace(p);
            p.close();
            m += " \n"+s.toString();
        }
        return BoldVariablesTableModelFilterFirst.toHTML(">" + m + "<", false, false, Color.RED);
    }
}

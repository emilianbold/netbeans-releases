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

package org.netbeans.modules.ruby.debugger.model;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.modules.ruby.debugger.ContextProviderWrapper;
import org.netbeans.modules.ruby.debugger.RubySession;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;
import org.rubyforge.debugcommons.model.RubyValue;
import org.rubyforge.debugcommons.model.RubyVariable;
import static org.netbeans.spi.debugger.ui.Constants.LOCALS_TO_STRING_COLUMN_ID;
import static org.netbeans.spi.debugger.ui.Constants.LOCALS_TYPE_COLUMN_ID;
import static org.netbeans.spi.debugger.ui.Constants.LOCALS_VALUE_COLUMN_ID;

/**
 * @author Martin Krauskopf
 */
public class VariablesModel implements TreeModel, ExtendedNodeModel, TableModel {
    
    private static final String GLOBAL = "Global Variables"; // NOI18N
    public static final String LOCAL =
            "org/netbeans/modules/debugger/resources/localsView/local_variable_16.png"; // NOI18N
    public static final String CLASS =
            "org/netbeans/modules/debugger/resources/watchesView/SuperVariable.gif"; // NOI18N
    
    protected final RubySession rubySession;
    protected final List<ModelListener> listeners = new CopyOnWriteArrayList<ModelListener>();
    
    public VariablesModel(ContextProvider contextProvider) {
        this.rubySession = new ContextProviderWrapper(contextProvider).getRubySession();
    }
    
    // TreeModel implementation ................................................
    
    public Object getRoot() {
        return ROOT;
    }
    
    public Object[] getChildren(Object parent, int from, int to)
            throws UnknownTypeException {
        // TODO: why this is called when #getChildrenCount() return 0?
        if (!rubySession.isSessionSuspended()) {
            return new Object[0];
        }
        if (parent == ROOT) {
            RubyVariable[] frameVars = rubySession.getVariables();
            Object[] vars = new Object[frameVars.length + 1]; // 1 - Global Variables node
            vars[0] = GLOBAL;
            System.arraycopy(frameVars, 0, vars, 1, frameVars.length);
            return vars;
        } else if (parent == GLOBAL) {
            return rubySession.getGlobalVariables();
        } else if (parent instanceof RubyVariable) {
            return rubySession.getChildren((RubyVariable) parent);
        } else {
            throw new UnknownTypeException(parent);
        }
    }
    
    public boolean isLeaf(Object node) throws UnknownTypeException {
        if (node == ROOT || node == GLOBAL) {
            return false;
        } else if (node instanceof RubyVariable) {
            RubyValue val = ((RubyVariable) node).getValue();
            return val == null || !val.hasVariables();
        } else {
            throw new UnknownTypeException(node);
        }
    }
    
    public int getChildrenCount(Object parent) throws UnknownTypeException {
        if (!rubySession.isSessionSuspended()) {
            return 0;
        }
        if (parent == ROOT) {
            return rubySession.getVariables().length + 1; // 1 - Global Variables node
        } else if (parent == GLOBAL) {
            return rubySession.getGlobalVariables().length;
        } else if (parent instanceof RubyVariable) {
            return rubySession.getChildren((RubyVariable) parent).length;
        } else {
            throw new UnknownTypeException(parent);
        }
    }
    
    public void addModelListener(ModelListener l) {
        listeners.add(l);
    }
    
    public void removeModelListener(ModelListener l) {
        listeners.remove(l);
    }
    
    public void fireChanges() {
        for (ModelListener listener : listeners) {
            listener.modelChanged(new ModelEvent.TreeChanged(this));
        }
    }
    
    // NodeModel implementation ................................................
    
    public String getDisplayName(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return getMessage("CTL_VariablesModel.Column.Name.Name");
        } else if (node == GLOBAL) {
            return getMessage("CTL_VariablesModel.Global.Variables");
        } else if (node instanceof RubyVariable) {
            String name = ((RubyVariable) node).getName();
            assert name != null : "null name for the RubyVariable: " + node;
            return name;
        } else {
            assert node != null : "null node passed to VariablesModel.getDisplayName()";
            throw new UnknownTypeException(node);
        }
    }

    public String getIconBase(Object node) throws UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public String getIconBaseWithExtension(Object node) throws UnknownTypeException {
        assert node != ROOT;
        // TODO use different icons
        if (node == GLOBAL) {
            return CLASS;
        } else if (node instanceof RubyVariable) {
            if (((RubyVariable) node).isClass()) {
                return CLASS;
            } else {
                return LOCAL;
            }
        } else {
            throw new UnknownTypeException(node);
        }
    }

    public String getShortDescription(Object node)
            throws UnknownTypeException {
        if (node == GLOBAL) {
            return getMessage("CTL_VariablesModel.Global.Variables.Short.Description");
        } else if (node == ROOT) {
            return getMessage("CTL_VariablesModel.Column.Name.Desc");
        } else if (node instanceof RubyVariable) {
            RubyValue value = ((RubyVariable) node).getValue();
            return '(' + value.getReferenceTypeName() + ") " +  value.getValueString(); // NOI18N
        } else {
            throw new UnknownTypeException(node);
        }
    }
    
    
    // TableModel implementation ...............................................
    
    public Object getValueAt(Object node, String columnID) throws
            UnknownTypeException {
        if (node == GLOBAL) {
            return "";
        } else if (node instanceof RubyVariable) {
            RubyVariable var = (RubyVariable) node;
            if (var.getValue() == null) {
                return "<nil>"; // NOI18N
            } else if (LOCALS_VALUE_COLUMN_ID.equals(columnID) || LOCALS_TO_STRING_COLUMN_ID.equals(columnID)) {
                return var.getValue().getValueString();
            } else if (LOCALS_TYPE_COLUMN_ID.equals(columnID)) {
                return var.getValue().getReferenceTypeName();
            }
        }
        throw new UnknownTypeException(node);
    }
    
    public boolean isReadOnly(Object node, String columnID) throws
            UnknownTypeException {
        return true;
    }
    
    public void setValueAt(Object node, String columnID, Object value)
            throws UnknownTypeException {
        throw new UnknownTypeException(node);
    }

    public boolean canRename(Object node) throws UnknownTypeException {
        return false;
    }

    public boolean canCopy(Object node) throws UnknownTypeException {
        return false;
    }

    public boolean canCut(Object node) throws UnknownTypeException {
        return false;
    }

    public Transferable clipboardCopy(Object node) throws IOException, UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public Transferable clipboardCut(Object node) throws IOException, UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public PasteType[] getPasteTypes(Object node, Transferable t) throws UnknownTypeException {
        return null;
    }

    public void setName(Object node, String name) throws UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    private static String getMessage(final String key) {
        return NbBundle.getMessage(VariablesModel.class, key);
    }
}

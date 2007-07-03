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

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Watch;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.rubyforge.debugcommons.model.RubyVariable;
import static org.netbeans.spi.debugger.ui.Constants.LOCALS_TO_STRING_COLUMN_ID;
import static org.netbeans.spi.debugger.ui.Constants.LOCALS_TYPE_COLUMN_ID;
import static org.netbeans.spi.debugger.ui.Constants.LOCALS_VALUE_COLUMN_ID;
import static org.netbeans.spi.debugger.ui.Constants.WATCH_TO_STRING_COLUMN_ID;
import static org.netbeans.spi.debugger.ui.Constants.WATCH_TYPE_COLUMN_ID;
import static org.netbeans.spi.debugger.ui.Constants.WATCH_VALUE_COLUMN_ID;

/**
 * @author Martin Krauskopf
 */
public final class WatchesModel extends VariablesModel {
    
    public static final String WATCH =
            "org/netbeans/modules/debugger/resources/watchesView/Watch"; // NOI18N
    
    public WatchesModel(final ContextProvider contextProvider) {
        super(contextProvider);
    }
    
    // TreeModel implementation ................................................
    
    public Object[] getChildren(Object parent, int from, int to)
            throws UnknownTypeException {
        if (parent == ROOT) {
            return DebuggerManager.getDebuggerManager().getWatches();
        } else if (parent instanceof Watch) {
            RubyVariable var = resolveVariable((Watch) parent);
            return var == null ? new Object[0] : super.getChildren(var, from, to);
        } else {
            return super.getChildren(parent, from, to);
        }
    }
    
    public boolean isLeaf(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return false;
        } else if (node instanceof Watch) {
            RubyVariable var = resolveVariable((Watch) node);
            return var == null ? true : super.isLeaf(var);
        } else {
            return super.isLeaf(node);
        }
    }
    
    public int getChildrenCount(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return DebuggerManager.getDebuggerManager().getWatches().length;
        } else if (node instanceof Watch) {
            RubyVariable var = resolveVariable((Watch) node);
            return var == null ? 0 : super.getChildrenCount(var);
        } else {
            return super.getChildrenCount(node);
        }
    }
    
    // NodeModel implementation ................................................
    
    public String getDisplayName(Object node) throws UnknownTypeException {
        if (node instanceof Watch) {
            return ((Watch) node).getExpression();
        } else {
            return super.getDisplayName(node);
        }
    }
    
    public String getIconBase(Object node) throws UnknownTypeException {
        if (node == ROOT || node instanceof Watch) {
            return WATCH;
        } else {
            return super.getIconBase(node);
        }
    }
    
    public String getShortDescription(Object node)
            throws UnknownTypeException {
        if (node instanceof Watch) {
            return null; // XXX
        } else {
            return super.getShortDescription(node);
        }
    }
    
    
    // TableModel implementation ...............................................
    
    public Object getValueAt(Object node, String columnID) throws
            UnknownTypeException {
        if(node instanceof Watch) {
            RubyVariable var = resolveVariable((Watch) node);
            if (var == null) {
                return "<Unkown in the current context>";
            }
            if (WATCH_VALUE_COLUMN_ID.equals(columnID)) {
                return super.getValueAt(var, LOCALS_VALUE_COLUMN_ID);
            } else if(WATCH_TYPE_COLUMN_ID.equals(columnID)) {
                return super.getValueAt(var, LOCALS_TYPE_COLUMN_ID);
            }
        } else {
            if (WATCH_VALUE_COLUMN_ID.equals(columnID)) {
                return super.getValueAt(node, LOCALS_VALUE_COLUMN_ID);
            } else if(WATCH_TYPE_COLUMN_ID.equals(columnID)) {
                return super.getValueAt(node, LOCALS_TYPE_COLUMN_ID);
            } else if(WATCH_TO_STRING_COLUMN_ID.equals(columnID)) {
                return super.getValueAt(node, LOCALS_TO_STRING_COLUMN_ID);
            }
        }
        throw new UnknownTypeException(node);
    }
    
    public boolean isReadOnly(Object node, String columnID) throws UnknownTypeException {
        return true;
    }
    
    public void setValueAt(Object node, String columnID, Object value)
            throws UnknownTypeException {
        throw new UnknownTypeException(node);
    }
    
    private RubyVariable resolveVariable(final Watch watch) {
        String expr = watch.getExpression();
        return rubySession.inspectExpression(expr);
    }
    
}

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

package org.netbeans.modules.ruby.debugger.breakpoints;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import static org.netbeans.spi.debugger.ui.Constants.BREAKPOINT_ENABLED_COLUMN_ID;

/**
 * @author Martin Krauskopf
 */
public final class BreakpointModel implements NodeModel, TableModel {
    
    public static final String LINE_BREAKPOINT =
            "org/netbeans/modules/debugger/resources/editor/Breakpoint";
    public static final String LINE_BREAKPOINT_PC =
            "org/netbeans/modules/debugger/resources/editor/Breakpoint+PC";
    public static final String DISABLED_LINE_BREAKPOINT =
            "org/netbeans/modules/debugger/resources/editor/DisabledBreakpoint";
    
    private List<ModelListener> listeners = new CopyOnWriteArrayList<ModelListener>();
    
    // NodeModel implementation ................................................
    
    public String getDisplayName(Object node) throws UnknownTypeException {
        if (node instanceof RubyBreakpoint) {
            RubyBreakpoint breakpoint = (RubyBreakpoint) node;
            return breakpoint.getFileObject().getNameExt() + ':' +
                    breakpoint.getLineNumber();
        }
        throw new UnknownTypeException(node);
    }
    
    public String getIconBase(Object node) throws UnknownTypeException {
        if (node instanceof RubyBreakpoint) {
            if (!((RubyBreakpoint) node).isEnabled()) {
                return DISABLED_LINE_BREAKPOINT;
            }
            //            RubyBreakpoint breakpoint = (RubyBreakpoint) node;
            //            RubyDebugger debugger = getDebugger();
            //            if(debugger != null && Utils.contains(debugger.getCurrentLine(), breakpoint.getLine())) {
            //                return LINE_BREAKPOINT_PC;
            //            }
            return LINE_BREAKPOINT;
        }
        throw new UnknownTypeException(node);
    }
    
    public String getShortDescription(Object node)
            throws UnknownTypeException {
        if (node instanceof RubyBreakpoint) {
            RubyBreakpoint breakpoint = (RubyBreakpoint) node;
            return breakpoint.getLine().getDisplayName();
        }
        throw new UnknownTypeException(node);
    }
    
    public void addModelListener(ModelListener l) {
        listeners.add(l);
    }
    
    public void removeModelListener(ModelListener l) {
        listeners.remove(l);
    }
    
    // TableModel implementation ......................................
    
    public Object getValueAt(Object node, String columnID) throws
            UnknownTypeException {
        if (node instanceof RubyBreakpoint && BREAKPOINT_ENABLED_COLUMN_ID.equals(columnID)) {
            return Boolean.valueOf(((RubyBreakpoint) node).isEnabled());
        }
        throw new UnknownTypeException(node);
    }
    
    public boolean isReadOnly(Object node, String columnID) throws
            UnknownTypeException {
        if (node instanceof RubyBreakpoint && BREAKPOINT_ENABLED_COLUMN_ID.equals(columnID)) {
            return false;
        }
        throw new UnknownTypeException(node);
    }
    
    public void setValueAt(Object node, String columnID, Object value)
            throws UnknownTypeException {
        if (node instanceof RubyBreakpoint && BREAKPOINT_ENABLED_COLUMN_ID.equals(columnID)) {
            if (((Boolean) value)) {
                ((RubyBreakpoint) node).enable();
            } else {
                ((RubyBreakpoint) node).disable();
            }
        } else {
            throw new UnknownTypeException(node);
        }
    }
    
    // TableModel implementation ......................................
    
    public void fireChanges() {
        for (ModelListener ml : listeners) {
            ml.modelChanged(new ModelEvent.TreeChanged(this));
        }
    }
    
}

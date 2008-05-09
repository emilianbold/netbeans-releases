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

package org.netbeans.modules.bpel.debugger.ui.breakpoint;

import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.modules.bpel.debugger.api.breakpoints.BpelBreakpoint;

/**
 * @author Vladimir Yaroslavskiy
 * @author Alexander Zgursky
 */
abstract class BpelBreakpointView implements NodeModel, TableModel, Constants {
    
    public static final String BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/NonLineBreakpoint";
    
    public static final String LINE_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/Breakpoint";
    
    public static final String LINE_BREAKPOINT_HIT =
        "org/netbeans/modules/debugger/resources/breakpointsView/BreakpointHit";
    
    public static final String BROKEN_BREAKPOINT = 
        "org/netbeans/modules/debugger/resources/breakpointsView/Breakpoint_broken";
    
    // NodeModel ----------------------------------------------------
    
    public String getDisplayName(Object object) throws UnknownTypeException {
        if ( !(object instanceof BpelBreakpoint)) {
            throw new UnknownTypeException(object);
        }
        return getName((BpelBreakpoint) object);
    }
    
    protected abstract String getName(BpelBreakpoint breakpoint)
    throws UnknownTypeException;
    
    public String getShortDescription(Object object) throws UnknownTypeException {
        return getDisplayName(object);
    }
    
    // --------------------------------------------------------------
    
    public void addModelListener(ModelListener listener) {}
    public void removeModelListener(ModelListener listener) {}
    
    // TableModel ---------------------------------------------------
    
    public Object getValueAt(Object object, String column)
    throws UnknownTypeException {
        if ( !(object instanceof BpelBreakpoint)) {
            throw new UnknownTypeException(object);
        }
        BpelBreakpoint breakpoint = (BpelBreakpoint) object;
        
        if (column.equals(BREAKPOINT_ENABLED_COLUMN_ID)) {
            return Boolean.valueOf(breakpoint.isEnabled());
        }
        throw new UnknownTypeException(object);
    }
    
    public void setValueAt(Object object, String column, Object value)
    throws UnknownTypeException {
        if ( !(object instanceof BpelBreakpoint)) {
            throw new UnknownTypeException(object);
        }
        BpelBreakpoint breakpoint = (BpelBreakpoint) object;
        
        if (column.equals(BREAKPOINT_ENABLED_COLUMN_ID)) {
            if (((Boolean) value).equals(Boolean.TRUE)) {
                breakpoint.enable();
            } else {
                breakpoint.disable();
            }
        }
        throw new UnknownTypeException(object);
    }
    
    public boolean isReadOnly(Object object, String column)
            throws UnknownTypeException
    {
        if ( !(object instanceof BpelBreakpoint)) {
            throw new UnknownTypeException(object);
        }
        if (column.equals(BREAKPOINT_ENABLED_COLUMN_ID)) {
            return false;
        }
        throw new UnknownTypeException(object);
    }
}

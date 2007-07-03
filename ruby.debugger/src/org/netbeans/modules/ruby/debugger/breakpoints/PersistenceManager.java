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

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Properties;
import org.netbeans.modules.ruby.debugger.Util;

/**
 * @author Martin Krauskopf
 */
public final class PersistenceManager extends DebuggerManagerAdapter {
    
    private static final String RUBY_PROPERTY = "ruby"; // NOI18N
    private static final String DEBUGGER_PROPERTY = "debugger"; // NOI18N
    
    @Override
    public Breakpoint[] initBreakpoints() {
        Properties p = loadBreakpointProperties();
        Breakpoint[] breakpoints = (Breakpoint[]) p.getArray(RUBY_PROPERTY, new Breakpoint[0]);
        List<Breakpoint> validBreakpoints = new ArrayList<Breakpoint>();
        for (Breakpoint breakpoint : breakpoints) {
            if (breakpoint != null) {
                breakpoint.addPropertyChangeListener(this);
                validBreakpoints.add(breakpoint);
            } else {
                Util.warning("null stored in the array obtained from \"" + RUBY_PROPERTY + "\" property"); // TODO: why?
            }
        }
        return validBreakpoints.toArray(new Breakpoint[validBreakpoints.size()]);
    }
    
    @Override
    public String[] getProperties() {
        return new String [] {
            DebuggerManager.PROP_BREAKPOINTS_INIT,
            DebuggerManager.PROP_BREAKPOINTS,
        };
    }
    
    @Override
    public void breakpointAdded(final Breakpoint breakpoint) {
        if (breakpoint instanceof RubyBreakpoint) {
            storeBreakpoints();
            breakpoint.addPropertyChangeListener(this);
        }
    }
    
    @Override
    public void breakpointRemoved(final Breakpoint breakpoint) {
        if (breakpoint instanceof RubyBreakpoint) {
            storeBreakpoints();
            breakpoint.removePropertyChangeListener(this);
        }
    }
    
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (evt.getSource() instanceof RubyBreakpoint) {
            storeBreakpoints();
        }
    }
    
    private void storeBreakpoints() {
        loadBreakpointProperties().setArray(RUBY_PROPERTY, RubyBreakpoint.getBreakpoints());
    }
    
    private Properties loadBreakpointProperties() {
        return Properties.getDefault().getProperties(DEBUGGER_PROPERTY).
                getProperties(DebuggerManager.PROP_BREAKPOINTS);
    }
    
}


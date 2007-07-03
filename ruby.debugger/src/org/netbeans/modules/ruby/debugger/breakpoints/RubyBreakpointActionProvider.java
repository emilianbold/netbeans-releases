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
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.modules.ruby.debugger.EditorUtil;
import org.netbeans.modules.ruby.debugger.Util;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.openide.text.Line;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;
import org.rubyforge.debugcommons.RubyDebuggerException;

/**
 * Provides actions for adding and removing Ruby breakpoints.
 *
 * @author Martin Krauskopf
 */
public final class RubyBreakpointActionProvider extends ActionsProviderSupport
        implements PropertyChangeListener {
    
    private final static Set<Object> ACTIONS =
            Collections.singleton(ActionsManager.ACTION_TOGGLE_BREAKPOINT);
    
    public RubyBreakpointActionProvider() {
        setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, true);
        TopComponent.getRegistry().addPropertyChangeListener(
                WeakListeners.propertyChange(this, TopComponent.getRegistry()));
    }
    
    @Override
    public Set<Object> getActions() {
        return ACTIONS;
    }
    
    @Override
    public void doAction(Object action) {
        Line line = EditorUtil.getCurrentLine();
        if (line == null) {
            return;
        }
        
        boolean removed = false;
        for (RubyBreakpoint breakpoint : RubyBreakpoint.getBreakpoints()) {
            if (breakpoint.getLine().equals(line)) {
                // breakpoint is already there, remove it (toggle)
                RubyBreakpoint.removeBreakpoint(breakpoint);
                removed = true;
                break;
            }
        }
        
        if (!removed) { // new breakpoint
            try {
                RubyBreakpoint.addBreakpoint(line);
            } catch (RubyDebuggerException e) {
                Util.LOGGER.log(Level.WARNING, "Unable to add breakpoint.", e);
            }
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        boolean enabled = EditorUtil.getCurrentLine() != null;
        setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, enabled);
    }
    
}

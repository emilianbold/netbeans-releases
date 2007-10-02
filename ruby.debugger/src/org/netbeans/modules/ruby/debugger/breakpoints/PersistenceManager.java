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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
        loadBreakpointProperties().setArray(RUBY_PROPERTY, RubyBreakpointManager.getBreakpoints());
    }
    
    private Properties loadBreakpointProperties() {
        return Properties.getDefault().getProperties(DEBUGGER_PROPERTY).
                getProperties(DebuggerManager.PROP_BREAKPOINTS);
    }
    
}


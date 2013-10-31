/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.js.breakpoints;

import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.modules.debugger.jpda.js.Context;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.text.AnnotationProvider;
import org.openide.text.Line;
import org.openide.util.Lookup;

/**
 *
 * @author Martin
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.text.AnnotationProvider.class)
@DebuggerServiceRegistration(types=LazyDebuggerManagerListener.class)
public class JSBreakpointAnnotationListener extends DebuggerManagerAdapter
                                            implements AnnotationProvider {
    
    private final Map<JSBreakpoint, Object> breakpointAnnotations = new HashMap<>();

    @Override
    public String[] getProperties() {
        return new String[] { DebuggerManager.PROP_BREAKPOINTS, DebuggerManager.PROP_DEBUGGER_ENGINES }; 
    }

    @Override
    public void breakpointAdded(Breakpoint breakpoint) {
        if (! (breakpoint instanceof JSBreakpoint)) {
            return;
        }
        addAnnotation((JSBreakpoint) breakpoint);
    }

    @Override
    public void breakpointRemoved(Breakpoint breakpoint) {
        if (! (breakpoint instanceof JSBreakpoint)) {
            return;
        }
        removeAnnotation((JSBreakpoint) breakpoint);
    }
    
    @Override
    public void annotate(Line.Set set, Lookup context) {
        // Annotate all JS breakpoints
        DebuggerManager.getDebuggerManager().getBreakpoints();
    }

    private void addAnnotation(JSBreakpoint breakpoint) {
        Object annotation = Context.annotate(((JSBreakpoint) breakpoint));
        if (annotation != null) {
            synchronized (breakpointAnnotations) {
                breakpointAnnotations.put(breakpoint, annotation);
            }
        }
    }

    private void removeAnnotation(JSBreakpoint breakpoint) {
        Object annotation;
        synchronized (breakpointAnnotations) {
            annotation = breakpointAnnotations.remove(breakpoint);
        }
        if (annotation != null) {
            Context.removeAnnotation(annotation);
        }
    }
    
}

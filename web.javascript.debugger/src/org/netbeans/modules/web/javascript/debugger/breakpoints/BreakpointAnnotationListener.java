/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.web.javascript.debugger.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.modules.web.javascript.debugger.annotation.LineBreakpointAnnotation;
import org.netbeans.modules.web.webkit.debugging.api.Debugger;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.text.Annotation;
import org.openide.text.AnnotationProvider;
import org.openide.text.Line;
import org.openide.text.Line.Set;
import org.openide.util.Lookup;


/**
 * Listens on {@org.netbeans.api.debugger.DebuggerManager} on
 * {@link org.netbeans.api.debugger.DebuggerManager#PROP_BREAKPOINTS}
 * property and annotates Debugger line breakpoints in NetBeans editor.
 *
 * @author ads
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.text.AnnotationProvider.class)
@DebuggerServiceRegistration(types=LazyDebuggerManagerListener.class)
public class BreakpointAnnotationListener extends DebuggerManagerAdapter
    implements PropertyChangeListener, AnnotationProvider
{
    
    private boolean active = true;

    @Override
    public String[] getProperties() {
        return new String[] { DebuggerManager.PROP_BREAKPOINTS, DebuggerManager.PROP_DEBUGGER_ENGINES }; 
    }

    @Override
    public void breakpointAdded(Breakpoint breakpoint) {
        if (! (breakpoint instanceof LineBreakpoint)) {
            return;
        }
        addAnnotation(breakpoint);
    }

    @Override
    public void breakpointRemoved(Breakpoint breakpoint) {
        if (! (breakpoint instanceof LineBreakpoint)) {
            return;
        }
        removeAnnotation(breakpoint);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if (Breakpoint.PROP_ENABLED.equals(propertyName) ||
            LineBreakpoint.PROP_LINE.equals(propertyName)) {
            
            Breakpoint b = (Breakpoint) evt.getSource();
            removeAnnotation(b);
            addAnnotation(b);
        } else if (Debugger.PROP_BREAKPOINTS_ACTIVE.equals(propertyName)) {
            boolean a = (Boolean) evt.getNewValue();
            if (a != active) {
                active = a;
                refreshAnnotations();
            }
        }
    }

    private void addAnnotation(Breakpoint breakpoint) {
        Line line = ((LineBreakpoint) breakpoint).getLine();
        Annotation annotation = new LineBreakpointAnnotation(line, (LineBreakpoint) breakpoint, active);
        synchronized (myAnnotations) {
            myAnnotations.put( breakpoint, annotation );
        }
        breakpoint.addPropertyChangeListener(this);
    }

    private void removeAnnotation(Breakpoint breakpoint) {
        Annotation annotation;
        synchronized (myAnnotations) {
            annotation = myAnnotations.remove(breakpoint);
        }
        
        if (annotation == null) {
            return;
        }
        
        annotation.detach();
        breakpoint.removePropertyChangeListener(this);
    }

    @Override
    public void engineAdded(DebuggerEngine engine) {
        Debugger debugger = engine.lookupFirst(null, Debugger.class);
        if (debugger != null) {
            debugger.addPropertyChangeListener(this);
            boolean a = debugger.areBreakpointsActive();
            if (a != active) {
                active = a;
                refreshAnnotations();
            }
        }
    }

    @Override
    public void engineRemoved(DebuggerEngine engine) {
        Debugger debugger = engine.lookupFirst(null, Debugger.class);
        if (debugger != null) {
            debugger.removePropertyChangeListener(this);
            if (true != active) {
                active = true;
                refreshAnnotations();
            }
        }
    }
    
    private void refreshAnnotations() {
        java.util.Set<Breakpoint> annotatedBreakpoints;
        synchronized (myAnnotations) {
            annotatedBreakpoints = new HashSet<Breakpoint>(myAnnotations.keySet());
        }
        for (Breakpoint b : annotatedBreakpoints) {
            removeAnnotation(b);
            addAnnotation(b);
        }
    }
    
    private final Map<Breakpoint, Annotation> myAnnotations 
        = new HashMap<Breakpoint, Annotation>();

    @Override
    public void annotate(Set set, Lookup context) {
        DebuggerManager.getDebuggerManager().getBreakpoints();
    }
}

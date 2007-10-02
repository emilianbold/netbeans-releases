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
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.modules.ruby.debugger.DebuggerAnnotation;
import org.openide.text.Annotation;

/**
 * @author Martin Krauskopf
 */
public final class BreakpointAnnotationListener extends DebuggerManagerAdapter
        implements PropertyChangeListener {
    
    private Map<Breakpoint, Annotation> breakpointToAnnotation
            = new HashMap<Breakpoint, Annotation>();
    
    @Override
    public String[] getProperties() {
        return new String[] { DebuggerManager.PROP_BREAKPOINTS };
    }
    
    @Override
    public void breakpointAdded(final Breakpoint b) {
        if (!(b instanceof RubyBreakpoint)) return;
        addAnnotation(b);
    }
    
    @Override
    public void breakpointRemoved(final Breakpoint b) {
        if (!(b instanceof RubyBreakpoint)) return;
        removeAnnotation(b);
    }
    
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if (propName == Breakpoint.PROP_ENABLED || propName == RubyBreakpoint.PROP_UPDATED) {
            removeAnnotation((Breakpoint) evt.getSource());
            addAnnotation((Breakpoint) evt.getSource());
        }
    }
    
    private void addAnnotation(final Breakpoint b) {
        Annotation debugAnnotation = new DebuggerAnnotation(
                b.isEnabled() ? DebuggerAnnotation.BREAKPOINT_ANNOTATION_TYPE : DebuggerAnnotation.DISABLED_BREAKPOINT_ANNOTATION_TYPE,
                ((RubyBreakpoint) b).getLine());
        breakpointToAnnotation.put(b, debugAnnotation);
        b.addPropertyChangeListener(this);
    }
    
    private void removeAnnotation(Breakpoint b) {
        Annotation annotation = breakpointToAnnotation.remove(b);
        if (annotation == null) return;
        annotation.detach();
        b.removePropertyChangeListener(this);
    }
    
}

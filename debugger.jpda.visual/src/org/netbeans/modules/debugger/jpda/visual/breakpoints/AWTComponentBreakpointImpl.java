/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.visual.breakpoints;

import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;
import java.util.List;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.FieldBreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;

/**
 *
 * @author martin
 */
public class AWTComponentBreakpointImpl extends BaseComponentBreakpointImpl {
    public AWTComponentBreakpointImpl(ComponentBreakpoint cb, JPDADebugger debugger) {
        super(cb, debugger);
    }

    @Override
    protected void initServiceBreakpoints() {
        //MethodBreakpoint mb = MethodBreakpoint.create("", "");
        ObjectReference component = cb.getComponent().getComponent(debugger);
        Variable variableComponent = ((JPDADebuggerImpl) debugger).getVariable(component);
        //mb.setInstanceFilters(debugger, new ObjectVariable[] { (ObjectVariable) variableComponent });
        
        int type = cb.getType();
        if (((type & AWTComponentBreakpoint.TYPE_ADD) != 0) || ((type & AWTComponentBreakpoint.TYPE_REMOVE) != 0)) {
            FieldBreakpoint fb = FieldBreakpoint.create("java.awt.Component", "parent", FieldBreakpoint.TYPE_MODIFICATION);
            fb.setHidden(true);
            fb.setInstanceFilters(debugger, new ObjectVariable[] { (ObjectVariable) variableComponent });
            fb.addJPDABreakpointListener(new JPDABreakpointListener() {
                @Override
                public void breakpointReached(JPDABreakpointEvent event) {
                    navigateToCustomCode(event.getThread());
                }
            });
            DebuggerManager.getDebuggerManager().addBreakpoint(fb);
            serviceBreakpoints.add(fb);
        }
        if (((type & AWTComponentBreakpoint.TYPE_SHOW) != 0) || ((type & AWTComponentBreakpoint.TYPE_HIDE) != 0)) {
            MethodBreakpoint mbShow = MethodBreakpoint.create("java.awt.Component", "show");
            mbShow.setMethodSignature("()V");
            addMethodBreakpoint(mbShow, (ObjectVariable) variableComponent);
            MethodBreakpoint mbHide = MethodBreakpoint.create("java.awt.Component", "hide");
            mbHide.setMethodSignature("()V");
            addMethodBreakpoint(mbHide, (ObjectVariable) variableComponent);
        }
        if (((type & AWTComponentBreakpoint.TYPE_REPAINT) != 0)) {
            String componentClassName = null;
            if (variableComponent instanceof JDIVariable) {
                Value value = ((JDIVariable) variableComponent).getJDIValue();
                if (value instanceof ObjectReference) {
                    componentClassName = findClassWithMethod(((ObjectReference) value).referenceType(), "repaint", "(JIIII)V");
                }
            }
            if (componentClassName == null) {
                componentClassName = "java.awt.Component";  // NOI18N
            }
            MethodBreakpoint mbShow = MethodBreakpoint.create(componentClassName, "repaint");
            // void repaint(long tm, int x, int y, int width, int height)
            mbShow.setMethodSignature("(JIIII)V");
            mbShow.setHidden(true);
            mbShow.setInstanceFilters(debugger, new ObjectVariable[] { (ObjectVariable) variableComponent });
            mbShow.addJPDABreakpointListener(new JPDABreakpointListener() {
                @Override
                public void breakpointReached(JPDABreakpointEvent event) {
                    navigateToCustomCode(event.getThread());
                }
            });
            DebuggerManager.getDebuggerManager().addBreakpoint(mbShow);
            serviceBreakpoints.add(mbShow);
        }
    }

    private String findClassWithMethod(ReferenceType referenceType, String name, String signature) {
        List<Method> methods = referenceType.methodsByName(name, signature);
        if (!methods.isEmpty()) {
            return methods.get(0).declaringType().name();
        }
        return null;
    }
}

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

package org.netbeans.modules.debugger.jpda.js.breakpoints.models;

import org.netbeans.modules.debugger.jpda.js.JSUtils;
import org.netbeans.modules.debugger.jpda.js.breakpoints.JSBreakpoint;
import org.netbeans.modules.debugger.jpda.js.breakpoints.JSLineBreakpoint;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin
 */
@DebuggerServiceRegistration(path="BreakpointsView", types=NodeModel.class)
public class JSBreakpointsNodeModel implements NodeModel {

    public static final String FUNCTION_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/NonLineBreakpoint";
    public static final String LINE_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/Breakpoint";
    public static final String CURRENT_FUNCTION_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/NonLineBreakpointHit";
    public static final String CURRENT_LINE_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/BreakpointHit";
    public static final String DISABLED_FUNCTION_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/DisabledNonLineBreakpoint";
    public static final String DISABLED_LINE_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/DisabledBreakpoint";
    public static final String DISABLED_CURRENT_FUNCTION_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/DisabledNonLineBreakpointHit";
    public static final String DISABLED_CURRENT_LINE_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/DisabledBreakpointHit";
    public static final String LINE_CONDITIONAL_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/ConditionalBreakpoint";
    public static final String CURRENT_LINE_CONDITIONAL_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/ConditionalBreakpointHit";
    public static final String DISABLED_LINE_CONDITIONAL_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/DisabledConditionalBreakpoint";

    @NbBundle.Messages({"# {0} - File name", "# {1} - Line number", "CTL_JS_Line_Breakpoint=Line {0}:{1}"})
    @Override
    public String getDisplayName(Object node) throws UnknownTypeException {
        if (!(node instanceof JSBreakpoint)) {
            throw new UnknownTypeException(node);
        }
        JSBreakpoint b = (JSBreakpoint) node;
        String name = JSUtils.getFileName(b);
        if (b instanceof JSLineBreakpoint) {
            int lineNumber = ((JSLineBreakpoint) b).getLineNumber();
            return Bundle.CTL_JS_Line_Breakpoint(name, lineNumber);
        } else {
            throw new UnknownTypeException(b);
        }
    }

    @Override
    @NbBundle.Messages({"# {0} - File name", "# {1} - Line number", "TT_JS_Line_Breakpoint=JavaScript breakpoint in {0} at line {1}"})
    public String getShortDescription(Object node) throws UnknownTypeException {
        if (!(node instanceof JSBreakpoint)) {
            throw new UnknownTypeException(node);
        }
        JSBreakpoint b = (JSBreakpoint) node;
        String name = JSUtils.getFileName(b);
        if (b instanceof JSLineBreakpoint) {
            int lineNumber = ((JSLineBreakpoint) b).getLineNumber();
            return Bundle.TT_JS_Line_Breakpoint(name, lineNumber);
        } else {
            throw new UnknownTypeException(b);
        }
    }

    @Override
    public String getIconBase(Object node) throws UnknownTypeException {
        String iconBase = null;
        if (node instanceof JSLineBreakpoint) {
            JSLineBreakpoint lb = (JSLineBreakpoint) node;
            boolean isEnabled = lb.isEnabled();
            boolean hasCondition = (lb.getCondition() != null);
            if (isEnabled) {
                if (hasCondition) {
                    iconBase = LINE_CONDITIONAL_BREAKPOINT;
                } else {
                    iconBase = LINE_BREAKPOINT;
                }
            } else {
                if (hasCondition) {
                    iconBase = DISABLED_LINE_CONDITIONAL_BREAKPOINT;
                } else {
                    iconBase = DISABLED_LINE_BREAKPOINT;
                }
            }
        }
        if (iconBase == null) {
            throw new UnknownTypeException(node);
        } else {
            return iconBase;
        }
    }

    @Override
    public void addModelListener(ModelListener l) {
    }

    @Override
    public void removeModelListener(ModelListener l) {
    }
    
}

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

package org.netbeans.modules.javascript2.debug.breakpoints.models;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.modules.javascript2.debug.JSUtils;
import org.netbeans.modules.javascript2.debug.breakpoints.JSBreakpointStatus;
import org.netbeans.modules.javascript2.debug.breakpoints.JSBreakpointsInfoManager;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin
 */
@DebuggerServiceRegistration(path="BreakpointsView", types=NodeModel.class)
public class BreakpointNodeModel implements NodeModel {
    
    public static final String LINE_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/Breakpoint";                   // NOI18N
    public static final String CURRENT_LINE_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/BreakpointHit";                // NOI18N
    public static final String DISABLED_LINE_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/DisabledBreakpoint";           // NOI18N
    public static final String DISABLED_CURRENT_LINE_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/DisabledBreakpointHit";        // NOI18N
    public static final String LINE_CONDITIONAL_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/ConditionalBreakpoint";        // NOI18N
    public static final String CURRENT_LINE_CONDITIONAL_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/ConditionalBreakpointHit";     // NOI18N
    public static final String DISABLED_LINE_CONDITIONAL_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/DisabledConditionalBreakpoint";// NOI18N
    public static final String DEACTIVATED_LINE_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/editor/Breakpoint_stroke.png";                 // NOI18N
    public static final String DEACTIVATED_DISABLED_LINE_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/editor/DisabledBreakpoint_stroke.png";         // NOI18N

    public BreakpointNodeModel() {
    }
    
    @NbBundle.Messages({
        "# {0} - The file name and line number",
        "LBL_LineBreakpoint_on=Line {0}"
    })
    @Override
    public String getDisplayName(Object node) throws UnknownTypeException {
        if (node instanceof JSLineBreakpoint) {
            JSLineBreakpoint breakpoint = (JSLineBreakpoint) node;
            String fileName = JSUtils.getFileName(breakpoint);
            int lineNumber = breakpoint.getLineNumber();
            return Bundle.LBL_LineBreakpoint_on(fileName + ":" + lineNumber);
        }
        throw new UnknownTypeException(node);
    }
    
    @Override
    public String getIconBase(Object node) throws UnknownTypeException {
        if (!(node instanceof JSLineBreakpoint)) {
            throw new UnknownTypeException(node);
        }
        JSLineBreakpoint b = (JSLineBreakpoint) node;
        boolean disabled = !b.isEnabled();
        boolean invalid = b.getValidity() == Breakpoint.VALIDITY.INVALID;
        boolean active = JSBreakpointsInfoManager.getDefault().areBreakpointsActivated();
        String iconBase;
        if (disabled) {
            if (active) {
                iconBase = DISABLED_LINE_BREAKPOINT;
            } else {
                iconBase = DEACTIVATED_DISABLED_LINE_BREAKPOINT;
            }
        } else {
            if (b == JSBreakpointStatus.getActive()) {
                iconBase = CURRENT_LINE_BREAKPOINT;
            } else {
                if (active) {
                    iconBase = LINE_BREAKPOINT;
                } else {
                    iconBase = DEACTIVATED_LINE_BREAKPOINT;
                }
            }
        }
        if (invalid && !disabled) {
            iconBase += "_broken";
        }
        return iconBase;
    }
    
    @NbBundle.Messages({
        "CTL_APPEND_BP_Valid=[Active]",
        "CTL_APPEND_BP_Invalid=[Invalid]",
        "# {0} - message describing why is the breakpoint invalid.",
        "CTL_APPEND_BP_Invalid_with_reason=[Invalid, reason: {0}]"
    })
    @Override
    public String getShortDescription(Object node) throws UnknownTypeException {
        if (!(node instanceof JSLineBreakpoint)) {
            throw new UnknownTypeException(node);
        }
        JSLineBreakpoint b = (JSLineBreakpoint) node;
        String appendMsg = null;
        Breakpoint.VALIDITY validity = b.getValidity();
        boolean valid = validity == Breakpoint.VALIDITY.VALID;
        boolean invalid = validity == Breakpoint.VALIDITY.INVALID;
        String message = b.getValidityMessage();
        if (valid) {
            appendMsg = Bundle.CTL_APPEND_BP_Valid();
        }
        if (invalid) {
            if (message != null) {
                appendMsg = Bundle.CTL_APPEND_BP_Invalid_with_reason(message);
            } else {
                appendMsg = Bundle.CTL_APPEND_BP_Invalid();
            }
        }
        String description = b.getLine().getDisplayName();
        if (appendMsg != null) {
            description = description + " " + appendMsg;
        }
        return description;
    }

    @Override
    public void addModelListener(ModelListener l) {
    }

    @Override
    public void removeModelListener(ModelListener l) {
    }

}

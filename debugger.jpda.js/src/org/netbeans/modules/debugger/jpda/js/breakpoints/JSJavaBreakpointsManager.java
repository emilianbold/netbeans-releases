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

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.modules.debugger.jpda.js.JSUtils;
import org.netbeans.modules.debugger.jpda.js.source.SourceURLMapper;
import org.netbeans.modules.javascript2.debug.breakpoints.FutureLine;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.filesystems.FileObject;
import org.openide.text.Line;

/**
 * Manages creation/removal of Java breakpoints corresponding to JS breakpoints.
 * 
 * @author Martin
 */
@DebuggerServiceRegistration(types=LazyDebuggerManagerListener.class)
public class JSJavaBreakpointsManager extends DebuggerManagerAdapter {
    
    private final Map<JSLineBreakpoint, LineBreakpoint> breakpoints = new HashMap<>();

    @Override
    public Breakpoint[] initBreakpoints() {
        return super.initBreakpoints(); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public String[] getProperties() {
        return new String[] { DebuggerManager.PROP_BREAKPOINTS }; 
    }

    @Override
    public void breakpointAdded(Breakpoint breakpoint) {
        if (!(breakpoint instanceof JSLineBreakpoint)) {
            return ;
        }
        Line line = ((JSLineBreakpoint) breakpoint).getLine();
        LineBreakpoint lb = createJavaLB(line);
        synchronized (breakpoints) {
            breakpoints.put((JSLineBreakpoint) breakpoint, lb);
        }
        DebuggerManager.getDebuggerManager().addBreakpoint(lb);
    }

    @Override
    public void breakpointRemoved(Breakpoint breakpoint) {
        if (!(breakpoint instanceof JSLineBreakpoint)) {
            return ;
        }
        LineBreakpoint lb;
        synchronized (breakpoints) {
            lb = breakpoints.remove((JSLineBreakpoint) breakpoint);
        }
        DebuggerManager.getDebuggerManager().removeBreakpoint(lb);
    }
    
    private LineBreakpoint createJavaLB(Line line) {
        FileObject fo = line.getLookup().lookup(FileObject.class);
        String url;
        String name;
        if (fo != null) {
            url = fo.toURL().toExternalForm();
            name = fo.getName();
        } else {
            url = ((FutureLine) line).getURL().toExternalForm();
            int i1 = url.lastIndexOf('/');
            if (i1 < 0) {
                i1 = 0;
            } else {
                i1++;
            }
            int i2 = url.lastIndexOf('.');
            if (i2 < i1) {
                i2 = url.length();
            }
            name = url.substring(i1, i2);
            name = SourceURLMapper.percentDecode(name);
        }
        if ("<eval>".equals(name)) {        // NOI18N
            name = "\\^eval\\_";            // NOI18N
        }
        int lineNo = line.getLineNumber() + 1;
        LineBreakpoint lb = LineBreakpoint.create("", lineNo);
        lb.setHidden(true);
        lb.setPreferredClassName(JSUtils.NASHORN_SCRIPT + name);
        lb.setSuspend(JPDABreakpoint.SUSPEND_EVENT_THREAD);
        return lb;
    }

}

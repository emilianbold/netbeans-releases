/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.ruby.debugger.EditorUtil;
import org.openide.filesystems.FileObject;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.rubyforge.debugcommons.RubyDebuggerException;
import org.rubyforge.debugcommons.RubyDebuggerProxy;
import org.rubyforge.debugcommons.model.IRubyBreakpoint;

public final class RubyBreakpointManager {
    
    private static final Map<RubyBreakpoint, BreakpointLineUpdater> BLUS = new HashMap<RubyBreakpoint, BreakpointLineUpdater>();

    private RubyBreakpointManager() {};

    static RubyBreakpoint createBreakpoint(final Line line) {
        RubyBreakpoint breakpoint = new RubyBreakpoint(line);
        BreakpointLineUpdater blu = new BreakpointLineUpdater(breakpoint);
        try {
            blu.attach();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        BLUS.put(breakpoint, blu);
        return breakpoint;
    }
    
    public static RubyBreakpoint addBreakpoint(final Line line) throws RubyDebuggerException {
        RubyBreakpoint breakpoint = createBreakpoint(line);
        DebuggerManager.getDebuggerManager().addBreakpoint(breakpoint);
        for (RubyDebuggerProxy proxy : RubyDebuggerProxy.PROXIES) {
            proxy.addBreakpoint(breakpoint);
        }
        return breakpoint;
    }

    public static void removeBreakpoint(final RubyBreakpoint breakpoint) {
        BreakpointLineUpdater blu = BLUS.remove(breakpoint);
        assert blu != null : "No BreakpointLineUpdater for RubyBreakpoint:" + breakpoint;
        if (blu != null) {
            blu.detach();
        }
        DebuggerManager.getDebuggerManager().removeBreakpoint(breakpoint);
        for (RubyDebuggerProxy proxy : RubyDebuggerProxy.PROXIES) {
            proxy.removeBreakpoint(breakpoint);
        }
    }

    /**
     * Uses {@link DebuggerManager#getBreakpoints()} filtering out all non-Ruby
     * breakpoints.
     */
    public static RubyBreakpoint[] getBreakpoints() {
        Breakpoint[] bps = DebuggerManager.getDebuggerManager().getBreakpoints();
        List<RubyBreakpoint> rubyBPs = new ArrayList<RubyBreakpoint>();
        for (Breakpoint bp : bps) {
            if (bp instanceof RubyBreakpoint) {
                rubyBPs.add((RubyBreakpoint) bp);
            }
        }
        return rubyBPs.toArray(new RubyBreakpoint[rubyBPs.size()]);
    }

    /**
     * Uses {@link DebuggerManager#getBreakpoints()} filtering out all non-Ruby
     * breakpoints. Returns only breakpoints associated with the given script.
     */
    static IRubyBreakpoint[] getBreakpoints(final FileObject script) {
        assert script != null;
        List<RubyBreakpoint> scriptBPs = new ArrayList<RubyBreakpoint>();
        for (RubyBreakpoint bp : getBreakpoints()) {
            FileObject fo = bp.getFileObject();
            if (script.equals(fo)) {
                scriptBPs.add(bp);
            }
        }
        return scriptBPs.toArray(new RubyBreakpoint[scriptBPs.size()]);
    }

    public static boolean isBreakpointOnLine(final FileObject file, final int line) {
        for (RubyBreakpoint bp : getBreakpoints()) {
            if (file.equals(bp.getFileObject()) && line == bp.getLineNumber()) {
                return true;
            }
        }
        return false;
    }

    static RubyBreakpoint getCurrentLineBreakpoint() {
        Line line = EditorUtil.getCurrentLine();
        if (line == null) {
            return null;
        }
        
        for (RubyBreakpoint breakpoint : RubyBreakpointManager.getBreakpoints()) {
            if (breakpoint.getLine().equals(line)) {
                return breakpoint;
            }
        }
        return null;
    }
}

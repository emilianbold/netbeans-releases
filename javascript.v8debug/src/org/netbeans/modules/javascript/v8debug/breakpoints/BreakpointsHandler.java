/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.v8debug.breakpoints;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.lib.v8debug.V8Arguments;
import org.netbeans.lib.v8debug.V8Breakpoint;
import org.netbeans.lib.v8debug.V8Command;
import org.netbeans.lib.v8debug.V8Request;
import org.netbeans.lib.v8debug.V8Response;
import org.netbeans.lib.v8debug.commands.ChangeBreakpoint;
import org.netbeans.lib.v8debug.commands.ClearBreakpoint;
import org.netbeans.lib.v8debug.commands.SetBreakpoint;
import org.netbeans.lib.v8debug.events.BreakEventBody;
import org.netbeans.modules.javascript.v8debug.ScriptsHandler;
import org.netbeans.modules.javascript.v8debug.V8Debugger;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Entlicher
 */
public class BreakpointsHandler {
    
    private static final Logger LOG = Logger.getLogger(BreakpointsHandler.class.getName());
    
    private final V8Debugger dbg;
    private final V8Debugger.CommandResponseCallback breakpointsCommandsCallback = new BreakpointsCommandsCallback();
    private final Map<V8Arguments, JSLineBreakpoint> submittingBreakpoints = new HashMap<>();
    private final Map<JSLineBreakpoint, SubmittedBreakpoint> submittedBreakpoints = new HashMap<>();
    private final Map<Long, SubmittedBreakpoint> breakpointsById = new HashMap<>();
    
    public BreakpointsHandler(V8Debugger dbg) { // TODO pass in initially submitted breakpoints in the debuggee
        this.dbg = dbg;
    }
    
    public boolean add(JSLineBreakpoint lb) {
        SetBreakpoint.Arguments srargs = createSetRequestArguments(lb);
        LOG.log(Level.FINE, "Adding {0}, args = {1}", new Object[]{lb, srargs});
        if (srargs == null) {
            return false;
        }
        synchronized (submittingBreakpoints) {
            submittingBreakpoints.put(srargs, lb);
        }
        V8Request request = dbg.sendCommandRequest(V8Command.Setbreakpoint, srargs, breakpointsCommandsCallback);
        LOG.log(Level.FINE, "  request = {0}", request);
        if (request == null) {
            // Failed
            synchronized (submittingBreakpoints) {
                submittingBreakpoints.remove(srargs);
            }
            return false;
        } else {
            return true;
        }
    }
    
    public boolean remove(JSLineBreakpoint lb) {
        SubmittedBreakpoint sb;
        synchronized (submittedBreakpoints) {
            sb = submittedBreakpoints.get(lb);
        }
        if (sb == null) {
            return false;
        }
        ClearBreakpoint.Arguments cbargs = new ClearBreakpoint.Arguments(sb.getId());
        V8Request request = dbg.sendCommandRequest(V8Command.Clearbreakpoint, cbargs);
        LOG.log(Level.FINE, "Removing {0}, request = {1}", new Object[]{lb, request});
        return request != null;
    }
    
    @CheckForNull
    private SetBreakpoint.Arguments createSetRequestArguments(JSLineBreakpoint b) {
        FileObject fo = b.getFileObject();
        if (fo == null) {
            return null;
        }
        File file = FileUtil.toFile(fo);
        if (file == null) {
            return null;
        }
        String localPath = file.getAbsolutePath();
        String serverPath;
        try {
            serverPath = dbg.getScriptsHandler().getServerPath(localPath);
        } catch (ScriptsHandler.OutOfScope oos) {
            return null;
        }
        String condition = (b.isConditional()) ? b.getCondition() : null;
        Long groupId = null; // TODO ?
        return new SetBreakpoint.Arguments(V8Breakpoint.Type.scriptName, serverPath,
                                           (long) b.getLineNumber() - 1, null, b.isEnabled(),
                                           condition, null, groupId);
    }
    
    static ChangeBreakpoint.Arguments createChangeRequestArguments(SubmittedBreakpoint sb) {
        JSLineBreakpoint b = sb.getBreakpoint();
        String condition = (b.isConditional()) ? b.getCondition() : null;
        return new ChangeBreakpoint.Arguments(sb.getId(), b.isEnabled(), condition, null);
    }
    
    private SubmittedBreakpoint submitted(JSLineBreakpoint b, SetBreakpoint.ResponseBody rb,
                                         V8Debugger dbg) {
        return new SubmittedBreakpoint(b, rb.getBreakpoint(), dbg);
    }

    public void event(BreakEventBody beb) {
        // TODO
        beb.getBreakpoints();
    }
    
    private final class BreakpointsCommandsCallback implements V8Debugger.CommandResponseCallback {

        @Override
        public void notifyResponse(V8Request request, V8Response response) {
            JSLineBreakpoint lb;
            synchronized (submittingBreakpoints) {
                lb = submittingBreakpoints.remove(request.getArguments());
            }
            if (lb == null) {
                LOG.log(Level.INFO, "Did not find a submitting breakpoint for response {0}, request was {1}",
                        new Object[]{response, request});
                return ;
            }
            if (response != null) {
                long id = ((SetBreakpoint.ResponseBody) response.getBody()).getBreakpoint();
                SubmittedBreakpoint sb = new SubmittedBreakpoint(lb, id, dbg);
            } else {
                // TODO invalidate lb
            }
        }
        
    }
    
}

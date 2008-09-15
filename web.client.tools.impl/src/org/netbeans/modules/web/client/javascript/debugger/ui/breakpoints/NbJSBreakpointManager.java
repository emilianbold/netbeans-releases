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

package org.netbeans.modules.web.client.javascript.debugger.ui.breakpoints;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.logging.Level;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.web.client.javascript.debugger.filesystem.URLFileObject;
import org.netbeans.modules.web.client.javascript.debugger.ui.NbJSEditorUtil;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSURILocation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.util.Exceptions;

public final class NbJSBreakpointManager {

    private static final Map<NbJSBreakpoint, NbJSBreakpointLineUpdater> BLUS = new HashMap<NbJSBreakpoint, NbJSBreakpointLineUpdater>();

    private NbJSBreakpointManager() {};


    /**
     * Creates a breakpoint but does not add it to the Debugger.
     *
     * @param line Line on which to add the breakpoint.
     * @return returns the created breakpoint.
     */

    protected final static NbJSBreakpoint createBreakpoint(final Line line) {
        if(line == null || line.getLookup() == null)
                return null;
    	DataObject dataObject = (DataObject)line.getLookup().lookup (DataObject.class);
        if(dataObject == null)
            return null;
        FileObject fileObject = dataObject.getPrimaryFile();
        NbJSBreakpoint breakpoint = null;
        if (fileObject instanceof URLFileObject) {
            try {
                URL url = fileObject.getURL();
                breakpoint = new NbJSURIBreakpoint(url.toString(), line.getLineNumber() + 1);
                ((NbJSURIBreakpoint)breakpoint).setOwnerLine(line);
            } catch (FileStateInvalidException e) {
                Log.getLogger().log(Level.INFO, "Exception creating URI breakpoint", e);
                return null;
            }
        } else {
            breakpoint = new NbJSFileObjectBreakpoint(line);
        }

        attachLineUpdater(breakpoint);
        return breakpoint;
    }

    protected final static NbJSURIBreakpoint createURIBreakpoint(String strURL, int lineNum) {
        NbJSURIBreakpoint breakpoint = new NbJSURIBreakpoint(strURL, lineNum + 1);
        attachLineUpdater(breakpoint);
        return breakpoint;
    }

    private static final void attachLineUpdater(NbJSBreakpoint breakpoint){
        NbJSBreakpointLineUpdater blu = new NbJSBreakpointLineUpdater(breakpoint);
        try {
            blu.attach();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        BLUS.put(breakpoint, blu);
    }

    public static NbJSBreakpoint addURIBreakpoint(final String strURL, final int lineNum){
        NbJSURIBreakpoint uriBp = createURIBreakpoint(strURL, lineNum);
        DebuggerManager.getDebuggerManager().addBreakpoint(uriBp);
        return uriBp;
    }
    /**
     * Creates a breakpoint and adds it to the debugger.
     * @param line Line on which to add the breakpoint.
     * @return breakpoint that is created and added.
     */
    public static NbJSBreakpoint addBreakpoint(final Line line)  {

    	NbJSBreakpoint breakpoint = createBreakpoint(line);
        DebuggerManager.getDebuggerManager().addBreakpoint(breakpoint);
        return breakpoint;
    }

    /**
     * Removes a breakpoint from the following.
     *   1- Current list of breakpoints ever created.
     *   2- The DebuggerManager
     *   3- From the LineUpdaterListener
     * @param breakpoint to be removed.
     */
    public static void removeBreakpoint(final NbJSBreakpoint breakpoint) {
        NbJSBreakpointLineUpdater blu = BLUS.remove(breakpoint);
        if (blu != null) {
            blu.detach();
        }
        DebuggerManager.getDebuggerManager().removeBreakpoint(breakpoint);
    }

    /**
     * Uses {@link DebuggerManager#getBreakpoints()} filtering out all non-JS
     * breakpoints.
     */
    public static NbJSBreakpoint[] getBreakpoints() {
        Breakpoint[] bps = DebuggerManager.getDebuggerManager().getBreakpoints();
        List<NbJSBreakpoint> jsBPs = new ArrayList<NbJSBreakpoint>();
        for (Breakpoint bp : bps) {
            if (bp instanceof NbJSBreakpoint) {
                jsBPs.add((NbJSBreakpoint) bp);
            }
        }
        return jsBPs.toArray(new NbJSBreakpoint[jsBPs.size()]);
    }

    /**
     * Uses {@link DebuggerManager#getBreakpoints()} filtering out all non-JS
     * breakpoints. Returns only breakpoints associated with the given script.
     */
    static NbJSBreakpoint[] getBreakpoints(final FileObject script) {
        assert script != null;
        List<NbJSBreakpoint> scriptBPs = new ArrayList<NbJSBreakpoint>();
        for (NbJSBreakpoint bp : getBreakpoints()) {
            FileObject fo = bp.getFileObject();
            if (script.equals(fo)) {
                scriptBPs.add(bp);
            }
        }
        return scriptBPs.toArray(new NbJSBreakpoint[scriptBPs.size()]);
    }

    /**
     * XXX Unused code - see <code>getCurrentLineBreakpoint</code> implementation to fix
     * 
     * Determines if a breakpoint exists on that line.
     * @param file The fileObject of the relevant file.
     * @param line This value starts from 1 rather than 0.
     * @return true if a breakpoint exists on that line in that file.  Otherwise false.
     */
    public static boolean isBreakpointOnLine(final FileObject file, final int line) {
        for (NbJSBreakpoint bp : getBreakpoints()) {
            if (file.equals(bp.getFileObject()) && line == bp.getLineNumber() ) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the currently selected line (the line with the cursor) has a
     * breakpoint and returns that breakpoint.
     *
     * @return the breakpoint on the current line.
     */
    public static NbJSBreakpoint getCurrentLineBreakpoint() {
        Line line = NbJSEditorUtil.getCurrentLine();
        if (line == null) {
            return null;
        }

        FileObject fo = ((DataObject)line.getLookup().lookup(DataObject.class)).getPrimaryFile();
        boolean isURI = fo instanceof URLFileObject;
        String path = null;
        if (isURI) {
            JSURILocation tmpLocation = new JSURILocation(fo.getPath(), 1, -1);
            path = tmpLocation.getURI().toString();
        }
        
        for (NbJSBreakpoint breakpoint : NbJSBreakpointManager.getBreakpoints()) {
            if (isURI && path != null && breakpoint instanceof NbJSURIBreakpoint) {
                JSURILocation location = ((NbJSURIBreakpoint)breakpoint).getLocation();
                String bpPath = location.getURI().toString();
                if ( (bpPath.equals(path) || bpPath.equals(path + "/")) &&
                        breakpoint.getLineNumber() == (line.getLineNumber() + 1)) {
                    return breakpoint;
                }
            } else if ( fo.equals(breakpoint.getFileObject())	&&
            		breakpoint.getLineNumber() == (line.getLineNumber() + 1) ) {
                return breakpoint;
            }
        }
        return null;
    }


}

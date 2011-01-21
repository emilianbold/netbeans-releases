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

package org.netbeans.modules.cnd.debugger.gdb2;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.debugger.common2.debugger.DebuggerSettings;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.Signals;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.Pathmap;

import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;


import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.DebuggerSettingsBridge;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerInfo;

import org.netbeans.modules.cnd.debugger.common2.debugger.debugtarget.DebugTarget;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.DbgProfile;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.HostInfo.OSFamily;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;

public final class GdbDebuggerSettingsBridge extends DebuggerSettingsBridge {
    
    private final GdbDebuggerImpl gdbDebugger;

    private Pathmap.Item[] shadowPathmap = null;

    public GdbDebuggerSettingsBridge(NativeDebugger debugger) {
	super(debugger, new GdbDebuggerSettings());
	gdbDebugger = (GdbDebuggerImpl) debugger;
    }

    @Override
    protected DebuggerSettings createSettingsFromTarget(DebugTarget dt) {
        RunProfile newRunProfile = dt.getRunProfile();
        DbgProfile newDbgProfile = dt.getDbgProfile();
        return GdbDebuggerSettings.create(newRunProfile, newDbgProfile);
    }
    
    /**
     * Set tentative profile as the current profile.
     */
    @Override
    protected void setTentativeSettings(NativeDebuggerInfo info) {
        RunProfile newRunProfile = info.getProfile();
        DbgProfile newDbgProfile = info.getDbgProfile();
        assert newRunProfile != null;

        String exename = info.getTarget();
        assignTentativeSettings(GdbDebuggerSettings.create(newRunProfile, newDbgProfile), exename);
    }

    protected void applyPathmap(Pathmap o, Pathmap n) {
	if (o == null) {
	    shadowPathmap = new Pathmap.Item[0];
	    applyPathmap(shadowPathmap, n.getPathmap());
	} else {
	    applyPathmap(o.getPathmap(), n.getPathmap());
	}
    }

    private void applyPathmap(Pathmap.Item[] oldMap, Pathmap.Item[] newMap) {
        // Wipe out elements in the old map that aren't in the new map,
        // and then set the new map

        // Clear old elements
        if (oldMap != null) {
            for (int i = 0; i < oldMap.length; i++) {
                // If this is in the new map, we don't have to do anything...
                if (newMap != null) {
                    int j = 0;
                    for (; j < newMap.length; j++) {
                        if (newMap[j].from().equals(oldMap[i].from() ) &&
                            IpeUtils.sameString(newMap[j].to(), oldMap[i].to())) {
                            break;
                        }
                    }
                    if (j < newMap.length) { // Found: no need to delete
                        continue;
                    }
                }
                gdbDebugger.pathmap("unset substitute-path " + oldMap[i].from()); // NOI18N
            }
        }

	if (newMap != null) {
            for (int i = 0; i < newMap.length; i++) {
                // If this is in the old map, we don't have to do anything...
                if (oldMap != null) {
                    int j = 0;
                    for (; j < oldMap.length; j++) {
                        if (newMap[i].from().equals(oldMap[j].from()) &&
                            IpeUtils.sameString(newMap[i].to(), oldMap[j].to()) ) {
                            break;
                        }
                    }
                    if (j < oldMap.length) { // Found: no need to add
                        continue;
                    }
                }

		String pathmap = null;
		pathmap = "set substitute-path " + newMap[i].from(); // NOI18N
                if (newMap[i].to() != null)
		    pathmap =  pathmap + " " + newMap[i].to(); // NOI18N
		gdbDebugger.pathmap(pathmap);
            }
        }
    }

    protected void applyRunargs() {
	String runargs = getArgsFlatEx();
	if (runargs == null)
	    runargs = "";
	gdbDebugger.runArgs(runargs + ioRedirect());
    }

    protected void applyRunDirectory() {
        RunProfile mainRunProfile = getMainSettings().runProfile();
        if (mainRunProfile.getRunDirectory() != null) {
            gdbDebugger.runDir(mainRunProfile.getRunDirectory());
        }
    }

    protected void applyClasspath() {
	// System.out.println("GdbDebuggerSettingsBridge.applyClasspath(): NOT IMPLEMENTED");
    }

    protected void applyEnvvars() {
	// Iterate over the environment variable list
        RunProfile mainRunProfile = getMainSettings().runProfile();
	String [] envvars = mainRunProfile.getEnvironment().getenv();
	if (envvars == null) {
	    return;
        }
	for (String envVar : envvars) {
	    gdbDebugger.setEnv(envVar);
	}
    }

    protected void applySignals(Signals o, Signals n) {
	// System.out.println("GdbDebuggerSettingsBridge.applySignals(): NOT IMPLEMENTED");
    }

    protected void applyInterceptList() {
	// System.out.println("GdbDebuggerSettingsBridge.applyRunargs(): NOT IMPLEMENTED");
    }

    private String ioRedirect() {
        String[] files = gdbDebugger.getIOPack().getIOFiles();
        if (files == null) {
            return "";
        }
        OSFamily osFamily = OSFamily.UNKNOWN;
        try {
            HostInfo hostInfo = HostInfoUtils.getHostInfo(gdbDebugger.getExecutionEnvironment());
            osFamily = hostInfo.getOSFamily();
        } catch (CancellationException ex) {
        } catch (IOException ex) {
        }

        String inRedir = "";
        String inFile = files[0];
        String outFile = files[1];
        if (osFamily == OSFamily.WINDOWS) {
            inFile = gdbDebugger.fmap().worldToEngine(inFile);
            outFile = gdbDebugger.fmap().worldToEngine(outFile);
        }
        // fix for the issue 149736 (2>&1 redirection does not work in gdb MI on mac)
        if (osFamily == OSFamily.MACOSX) {
            inRedir = " < " + inFile + " > " + outFile + " 2> " + outFile; // NOI18N
        } else {
            // csh (tcsh also) does not support 2>&1 stream redirection, see issue 147872
            String shell = HostInfoProvider.getEnv(gdbDebugger.getExecutionEnvironment()).get("SHELL"); // NOI18N
            if (shell != null && shell.endsWith("csh")) { // NOI18N
                inRedir = " < " + inFile + " >& " + outFile; // NOI18N
            } else {
                inRedir = " < " + inFile + " > " + outFile + " 2>&1"; // NOI18N
            }
        }
        return inRedir;
    }
}

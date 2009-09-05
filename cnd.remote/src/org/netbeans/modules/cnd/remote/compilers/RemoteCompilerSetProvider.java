/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.compilers;

import java.util.List;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSetProvider;
import org.netbeans.modules.cnd.api.compilers.PlatformTypes;
import org.netbeans.modules.cnd.remote.fs.RemoteFileSystem;
import org.netbeans.modules.cnd.remote.fs.RemoteFileSystemManager;
import org.netbeans.modules.cnd.remote.fs.RemoteFileSystemsProvider;
import org.netbeans.modules.cnd.remote.support.RemoteCommandSupport;
import org.netbeans.modules.cnd.remote.support.RemoteUtil;
import org.netbeans.modules.cnd.remote.support.SystemIncludesUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 * @author gordonp
 */
public class RemoteCompilerSetProvider implements CompilerSetProvider {
    
    private CompilerSetScriptManager manager;
    private final ExecutionEnvironment env;

    /*package-local*/ RemoteCompilerSetProvider(ExecutionEnvironment env) {
        if (env == null) {
            throw new IllegalArgumentException("ExecutionEnvironment should not be null"); //NOI18N
        }
        this.env = env;
    }

    @Override
    public void init() {
        manager = new CompilerSetScriptManager(env);
        manager.runScript();
    }
    
    public int getPlatform() {
        String platform = manager.getPlatform();
        if (platform == null || platform.length() == 0) {
            RemoteUtil.LOGGER.warning("RCSP.getPlatform: Got null response on platform"); //NOI18N
            platform = ""; //NOI18N
        }
        if (platform.startsWith("Windows")) { // NOI18N
            return PlatformTypes.PLATFORM_WINDOWS;
        } else if (platform.startsWith("Linux")) { // NOI18N
            return PlatformTypes.PLATFORM_LINUX;
        } else if (platform.startsWith("SunOS")) { // NOI18N
            return platform.contains("86") ? PlatformTypes.PLATFORM_SOLARIS_INTEL : PlatformTypes.PLATFORM_SOLARIS_SPARC; // NOI18N
        } else if (platform.toLowerCase().startsWith("mac")) { // NOI18N
            return PlatformTypes.PLATFORM_MACOSX;
        } else {
            return PlatformTypes.PLATFORM_GENERIC;
        }
    }

    public boolean hasMoreCompilerSets() {
        return manager.hasMoreCompilerSets();
    }

    public String getNextCompilerSetData() {
        return manager.getNextCompilerSetData();
    }

    public Runnable createCompilerSetDataLoader(List<CompilerSet> sets) {
        if (RemoteFileSystemsProvider.USE_REMOTE_FS) {
            RemoteFileSystem fs = RemoteFileSystemManager.getInstance().get(env);
            return new Runnable() {
                public void run() {
                }
            };
        } else {
            return SystemIncludesUtils.createLoader(env, sets);
        }
    }

    public String[] getCompilerSetData(String path) {
        RemoteCommandSupport rcs = new RemoteCommandSupport(env,
                CompilerSetScriptManager.SCRIPT + " " + path); //NOI18N
        if (rcs.run() == 0) {
            return rcs.getOutput().split("\n"); // NOI18N
        }
        return null;
    }

}

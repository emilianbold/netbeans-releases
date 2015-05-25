/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.support;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.ui.ToolsCacheManager;
import org.netbeans.modules.cnd.remote.server.RemoteServerRecord;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Misc. utiliy finctions
 * @author Vladimir Kvashin
 */
public class RemoteUtil {

    public static final Logger LOGGER = RemoteLogger.getInstance();

    public static class PrefixedLogger {

        private final String prefix;

        public PrefixedLogger(String prefix) {
            this.prefix = prefix;
        }

        public void log(Level level, String format, Object... args) {
            if (LOGGER.isLoggable(level)) {
                String text = String.format(format, args);
                text = prefix + ": " + text; // NOI18N
                LOGGER.log(level, text);
            }
        }

        public Level getLevel() {
            return LOGGER.getLevel();
        }

        public boolean isLoggable(Level level) {
            return LOGGER.isLoggable(level);
        }        
    }

    private RemoteUtil() {}

    /**
     * FIXUP: * Need this hack for Cloud stuff:
     * we have to distinguish "normal", i.e. CND environments
     * from "foreign", i.e. "cloud" ones.
     */
    public static boolean isForeign(ExecutionEnvironment execEnv ) {
        if (execEnv == null) {
            return false;
        }
        String id = ExecutionEnvironmentFactory.toUniqueID(execEnv);
        String protocolSeparator = "://"; //NOI18N
        if (id.indexOf(protocolSeparator) < 0) {
            // old-style environment - no protocol
            return false; 
        } else {
            // there is a protocol and it equals to ssh
            return ! id.startsWith("ssh" + protocolSeparator); //NOI18N
        }
    }
    
    public static String getDisplayName(ExecutionEnvironment execEnv) {
        ServerRecord rec = ServerList.get(execEnv);
        if (rec == null) {
            return execEnv.getDisplayName();
        } else {
            return rec.getDisplayName();
        }
    }

    public static void checkSetupAfterConnection(ExecutionEnvironment env) {
        RemoteServerRecord record = (RemoteServerRecord) ServerList.get(env);
        checkSetupAfterConnection(record);
    }

    public static void checkSetupAfterConnection(RemoteServerRecord record) {
        if (!record.isOnline()) {
            record.resetOfflineState();
            record.init(null);
            if (record.isOnline()) {
                ToolsCacheManager cacheManager = ToolsCacheManager.createInstance(true);
                CompilerSetManager csm = cacheManager.getCompilerSetManagerCopy(record.getExecutionEnvironment(), false);
                csm.initialize(false, true, null);
                cacheManager.applyChanges();
            }
        }
    }
    
    public static boolean isWindows(ExecutionEnvironment env) {
        return env.isLocal() && Utilities.isWindows();
    }
    
    public static String getMessage(IOException e) {
        String result;
        String reason = e.getMessage();
        if (e instanceof UnknownHostException) {
            result = NbBundle.getMessage(RemoteConnectionSupport.class, "REASON_UnknownHost", e.getMessage());
        } else if (reason.startsWith("Auth fail")) { // NOI18N
            result = NbBundle.getMessage(RemoteConnectionSupport.class, "REASON_AuthFailed");
        } else {
            result = reason;
        }
        return result;
    }    
}

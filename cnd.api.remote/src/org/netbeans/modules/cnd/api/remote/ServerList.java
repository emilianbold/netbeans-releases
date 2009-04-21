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

package org.netbeans.modules.cnd.api.remote;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.spi.remote.ServerListImplementation;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.util.Lookup;

/**
 * This is a place holder for a RemoteServerList which will be implemented in cnd.remote.
 * 
 * @author gordonp
 */
public class ServerList {

    private ServerList() {
    }

    private static ServerListImplementation DEFAULT;

    private static ServerListImplementation getDefault() {
        ServerListImplementation result = DEFAULT;
        if (result == null) {
            result = Lookup.getDefault().lookup(ServerListImplementation.class);
//            assert result != null;
            // FIXUP

            final ExecutionEnvironment execEnv = ExecutionEnvironmentFactory.getLocal();

            final ServerRecord record = new ServerRecord() {

                public ExecutionEnvironment getExecutionEnvironment() {
                    return execEnv;
                }

                public String getName() {
                    return execEnv.getDisplayName();
                }

                public String getServerName() {
                    return execEnv.getHost();
                }

                public String getUserName() {
                    return execEnv.getUser();
                }

                public boolean isDeleted() {
                    return false;
                }

                public boolean isOffline() {
                    return false;
                }

                public boolean isOnline() {
                    return true;
                }

                public boolean isRemote() {
                    return execEnv.isRemote();
                }

                public void validate(boolean force) {
                }

            };

            result = new  ServerListImplementation() {

                public int getDefaultIndex() {
                    return 0;
                }

                public Collection<? extends ServerRecord> getRecords() {
                    return Collections.singletonList(record);
                }

                public void setDefaultIndex(int defaultIndex) {}

                public List<ExecutionEnvironment> getEnvironments() {
                    return Collections.singletonList(execEnv);
                }

                public ServerRecord get(ExecutionEnvironment env) {
                    return env.isLocal() ? record : null;
                }

                public ServerRecord getDefaultRecord() {
                    return record;
                }

                public void clear() {
                }

                public ServerRecord addServer(ExecutionEnvironment env, boolean asDefault, boolean connect) {
                    return null;
                }

                public boolean isValidExecutable(ExecutionEnvironment env, String path) {
                    return true;
                }
            };

            DEFAULT = result;
        }
        return result;
    };

    /** The index of the default development server */
    public static int getDefaultIndex() {
        return getDefault().getDefaultIndex();
    }

    public static Collection<? extends ServerRecord> getRecords() {
        return getDefault().getRecords();
    }
    
    /** Set the index of the default development server */
    public static void setDefaultIndex(int defaultIndex) {
        getDefault().setDefaultIndex(defaultIndex);
    }
    
    public static List<ExecutionEnvironment> getEnvironments() {
        return getDefault().getEnvironments();
    };

    public static ServerRecord get(ExecutionEnvironment env) {
        return getDefault().get(env);
    }
    
    public static ServerRecord getDefaultRecord() {
        return getDefault().getDefaultRecord();
    }
    
    public static void clear() {
        getDefault().clear();
    }

    public static ServerRecord addServer(ExecutionEnvironment env, boolean asDefault, boolean connect) {
        return getDefault().addServer(env, asDefault, connect);
    }

    public static boolean isValidExecutable(ExecutionEnvironment env, String path) {
        return getDefault().isValidExecutable(env, path);
    }
}

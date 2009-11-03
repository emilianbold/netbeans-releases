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

package org.netbeans.modules.cnd.remote.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.remote.SetupProvider;
import org.netbeans.modules.cnd.remote.support.RemoteCommandSupport;
import org.netbeans.modules.cnd.remote.support.RemoteCopySupport;
import org.netbeans.modules.cnd.remote.support.RemoteUtil;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * NB: the class is not thread safe!
 * @author gordonp
 */
public class RemoteServerSetup {
    
    private final Map<String, String> binarySetupMap;
    private final Map<ExecutionEnvironment, List<String>> updateMap;
    private final ExecutionEnvironment executionEnvironment;
    private final Set<String> checkedDirs = new HashSet<String>();
    private boolean cancelled;
    private boolean failed;
    private String reason;
    private String libDir;
    
    /*package*/ RemoteServerSetup(ExecutionEnvironment executionEnvironment) {
        this.executionEnvironment = executionEnvironment;
        Lookup.Result<SetupProvider> results = Lookup.getDefault().lookup(new Lookup.Template<SetupProvider>(SetupProvider.class));
        Collection<? extends SetupProvider> list = results.allInstances();
        SetupProvider[] providers = list.toArray(new SetupProvider[list.size()]);
        libDir = HostInfoProvider.getLibDir(executionEnvironment); //NB: should contain trailing '/'
        if (!libDir.endsWith("/")) { // NOI18N
            libDir += "/"; // NOI18N
        }
        // Binary setup map
        binarySetupMap = new HashMap<String, String>();
        for (SetupProvider provider : providers) {
            Map<String, String> map = provider.getBinaryFiles(executionEnvironment);
            if (map != null) {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    binarySetupMap.put(libDir + entry.getKey(), entry.getValue());
                }
            }
        }
        
        updateMap = new HashMap<ExecutionEnvironment, List<String>>();
    }

    /*package*/ boolean needsSetupOrUpdate() {
        List<String> updateList = new ArrayList<String>();        
        updateMap.clear();
        if (!isFailedOrCanceled()) {
            updateList = getBinaryUpdates(updateList);
        }
        if (isFailedOrCanceled()) {
            return false;
        }
        
        if (!updateList.isEmpty()) {
            updateMap.put(executionEnvironment, updateList);
            return true;
        } else {
            return false;
        }
    }
    
    protected  void setup() {
        List<String> list = updateMap.remove(executionEnvironment);
        for (String path : list) {
            RemoteUtil.LOGGER.fine("RSS.setup: Updating \"" + path + "\" on " + executionEnvironment); //NO18N
            if (binarySetupMap.containsKey(path)) {
                String localFileName = binarySetupMap.get(path);
                File file = InstalledFileLocator.getDefault().locate(localFileName, null, false);
                //String remotePath = REMOTE_LIB_DIR + file.getName();
                String remotePath = path;
                if (file == null
                        || !file.exists()
                        || !copyTo(file, remotePath)) {
                    setFailed(NbBundle.getMessage(RemoteServerSetup.class, "ERR_UpdateSetupFailure", executionEnvironment, path)); //NOI18N
                }
            }
        }
    }

    private boolean copyTo(File file, String remoteFilePath) {
        int slashPos = remoteFilePath.lastIndexOf('/'); //NOI18N
        if (slashPos >= 0) {
            String remoteDir = remoteFilePath.substring(0, slashPos);
            if (!checkedDirs.contains(remoteDir)) {
                checkedDirs.add(remoteDir);
                String cmd = String.format("sh -c \"if [ ! -d %s ]; then mkdir -p %s; fi\"", remoteDir, remoteDir); // NOI18N
                RemoteCommandSupport.run(executionEnvironment, cmd);
            }
        }        
        return RemoteCopySupport.copyTo(executionEnvironment, file.getAbsolutePath(), remoteFilePath);
    }
        
    private List<String> getBinaryUpdates(List<String> list) {

        StringBuilder sb = new StringBuilder("sh -c \""); // NOI18N
        for (String path : binarySetupMap.keySet()) {
            sb.append(String.format("if [ ! -x %s ]; then echo %s; fi;", path, path)); // NOI18N
        }
        sb.append("\""); // NOI18N

        RemoteCommandSupport support = new RemoteCommandSupport(executionEnvironment, sb.toString());
        support.run();
        if (!support.isFailed()) {
            RemoteUtil.LOGGER.fine("RSS.getBinaryUpdates: GET_LIB_INFO returned " + support.getExitStatus());
            if (support.isCancelled()) {
                cancelled = true;
            } else {
                String val = support.getOutput();
                for (String line : val.split("\n")) { // NOI18N
                    if (line.length() > 0) {
                        list.add(line);
                    }
                }
            }
        } else {
            // failed
            setFailed(support.getFailureReason());
        }
        return list;
    }
    
    /**
     * Map the reason to a more human readable form. The original reason is currently
     * always in English. This method would need changing were that to change.
     * 
     * @return The reason, possibly localized and more readable
     */
    public String getReason() {
        return reason;
    }
    
    protected boolean isCancelled() {
        return cancelled;
    }

    private void setFailed(String reason) {
        this.failed = true;
        this.reason = reason;
    }

    protected boolean isFailed() {
        return failed;
    }

    private boolean isFailedOrCanceled() {
        return failed || cancelled;
    }    
}

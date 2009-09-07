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
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.remote.SetupProvider;
import org.netbeans.modules.cnd.remote.support.RemoteCommandSupport;
import org.netbeans.modules.cnd.remote.support.RemoteCopySupport;
import org.netbeans.modules.cnd.remote.support.RemoteUtil;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author gordonp
 */
public class RemoteServerSetup {
    
    private static final String REMOTE_SCRIPT_DIR = ".netbeans/6.7/cnd2/scripts/"; // NOI18N
    private static final String LOCAL_SCRIPT_DIR = "src/scripts/"; // NOI18N

    // Anyhow all REMOTE_SCRIPT_DIR contents should have execution permission.
    // So it's faster to just invoke "chmod a+x" than first to invoke another command,
    // then analyze results, and call the same "chmod a+x" if necessary
    //
    // private static final String GET_SCRIPT_INFO = "grep VERSION= " + REMOTE_SCRIPT_DIR + "* /dev/null 2> /dev/null"; // NOI18N
    private static final String GET_SCRIPT_INFO = "sh -c \"chmod a+x " + REMOTE_SCRIPT_DIR + "* && grep VERSION= " + REMOTE_SCRIPT_DIR + "* 2> /dev/null \""; // NOI18N
    
    private static final String DOS2UNIX_CMD = "dos2unix " + REMOTE_SCRIPT_DIR; // NOI18N
    public static final String REMOTE_LIB_DIR = ".netbeans/6.7/cnd2/lib/"; // NOI18N
    
    private final Map<String, Double> scriptSetupMap;
    private final Map<String, String> binarySetupMap;
    private final Map<ExecutionEnvironment, List<String>> updateMap;
    private final ExecutionEnvironment executionEnvironment;
    private boolean cancelled;
    private boolean failed;
    private String reason;
    
    protected RemoteServerSetup(ExecutionEnvironment executionEnvironment) {
        this.executionEnvironment = executionEnvironment;
        Lookup.Result<SetupProvider> results = Lookup.getDefault().lookup(new Lookup.Template<SetupProvider>(SetupProvider.class));
        Collection<? extends SetupProvider> list = results.allInstances();
        SetupProvider[] providers = list.toArray(new SetupProvider[list.size()]);
        
        // Script setup map
        scriptSetupMap = new HashMap<String, Double>();
        scriptSetupMap.put("getCompilerSets.bash", Double.valueOf(0.96)); // NOI18N
        for (SetupProvider provider : providers) {
            Map<String, Double> map = provider.getScriptFiles();
            if (map != null) {
                for (Map.Entry<String, Double> entry : map.entrySet()) {
                    scriptSetupMap.put(entry.getKey(), entry.getValue());
                }
            }
        }
        
        // Binary setup map
        binarySetupMap = new HashMap<String, String>();
        for (SetupProvider provider : providers) {
            Map<String, String> map = provider.getBinaryFiles();
            if (map != null) {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    binarySetupMap.put(REMOTE_LIB_DIR + entry.getKey(), entry.getValue());
                }
            }
        }
        
        updateMap = new HashMap<ExecutionEnvironment, List<String>>();
    }

    protected boolean needsSetupOrUpdate() {
        List<String> updateList = new ArrayList<String>();
        
        updateMap.clear(); // remote entries if run for other remote systems
        updateList = getScriptUpdates(updateList);
        if (!isFailedOrCanceled()) {
            updateList = getBinaryUpdates(updateList);
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
        boolean needChmod = false;
        
        for (String path : list) {
            if (path.equals(REMOTE_SCRIPT_DIR)) {
                RemoteUtil.LOGGER.fine("RSS.setup: Creating ~/" + REMOTE_SCRIPT_DIR); //NO18N
                int exit_status = RemoteCommandSupport.run(executionEnvironment,
                        "mkdir -p " + REMOTE_SCRIPT_DIR); // NOI18N
                if (exit_status == 0) {
                    needChmod = true;
                    for (String key : scriptSetupMap.keySet()) {
                        RemoteUtil.LOGGER.fine("RSS.setup: Copying " + path + " to " + executionEnvironment); //NO18N
                        File file = InstalledFileLocator.getDefault().locate(LOCAL_SCRIPT_DIR + key, null, false);
                        if (file == null
                                || !file.exists()
                                || !RemoteCopySupport.copyTo(executionEnvironment, file.getAbsolutePath(), REMOTE_SCRIPT_DIR + file.getName())
                                || RemoteCommandSupport.run(executionEnvironment, DOS2UNIX_CMD + key + ' ' + REMOTE_SCRIPT_DIR + key) != 0) { //NO18N
                            reason = NbBundle.getMessage(RemoteServerSetup.class, "ERR_UpdateSetupFailure", //NO18N
                                    executionEnvironment.toString(), key);
                        }
                    }
                } else {
                    reason = NbBundle.getMessage(RemoteServerSetup.class, "ERR_DirectorySetupFailure", //NO18N
                            executionEnvironment.toString(), exit_status);
                }
            } else if (path.equals(REMOTE_LIB_DIR)) {
                RemoteUtil.LOGGER.fine("RSS.setup: Creating ~/" + REMOTE_LIB_DIR); //NO18N
                int exit_status = RemoteCommandSupport.run(executionEnvironment,
                        "mkdir -p " + REMOTE_LIB_DIR); // NOI18N
                if (exit_status == 0) {
                    needChmod = true;
                    for (String remoteFileName : binarySetupMap.keySet()) {
                        String localFileName = binarySetupMap.get(remoteFileName);
                        RemoteUtil.LOGGER.fine("RSS.setup: Copying" + localFileName + " to " + executionEnvironment); //NO18N
                        File file = InstalledFileLocator.getDefault().locate(localFileName, null, false);
                        if (file == null
                                || !file.exists()
                                || !RemoteCopySupport.copyTo(executionEnvironment, file.getAbsolutePath(), remoteFileName)) {
                            reason = NbBundle.getMessage(RemoteServerSetup.class, "ERR_UpdateSetupFailure", //NOI18N
                                    executionEnvironment.toString(), localFileName);
                        }
                    }
                } else {
                    reason = NbBundle.getMessage(RemoteServerSetup.class, "ERR_DirectorySetupFailure", //NO18N
                            executionEnvironment.toString(), exit_status);
                }
            } else {
                RemoteUtil.LOGGER.fine("RSS.setup: Updating \"" + path + "\" on " + executionEnvironment); //NO18N
                if (binarySetupMap.containsKey(path)) {
                    needChmod = true;
                    String loc = binarySetupMap.get(path);
                    File file = InstalledFileLocator.getDefault().locate(loc, null, false);
                    if (file == null
                            || !file.exists()
                            || !RemoteCopySupport.copyTo(executionEnvironment, file.getAbsolutePath(), REMOTE_LIB_DIR + file.getName())) {
                        reason = NbBundle.getMessage(RemoteServerSetup.class, "ERR_UpdateSetupFailure", executionEnvironment, path); //NOI18N
                    }
                } else {
                    File file = InstalledFileLocator.getDefault().locate(LOCAL_SCRIPT_DIR + path, null, false);
                    if (file == null
                            || !file.exists()
                            || !RemoteCopySupport.copyTo(executionEnvironment, file.getAbsolutePath(), REMOTE_SCRIPT_DIR + file.getName())
                            || RemoteCommandSupport.run(executionEnvironment, DOS2UNIX_CMD + path + ' ' + REMOTE_SCRIPT_DIR + path) != 0) { //NOI18N
                        reason = NbBundle.getMessage(RemoteServerSetup.class, "ERR_UpdateSetupFailure", executionEnvironment.toString(), path); //NOI18N
                    }
                }
            }
        }
        if (needChmod) {
            RemoteCommandSupport.run(executionEnvironment, "chmod 755 " + REMOTE_SCRIPT_DIR + "*.bash " + REMOTE_LIB_DIR + "*.so"); //NOI18N
        }
    }
    
    private List<String> getScriptUpdates(List<String> list) {
        RemoteCommandSupport support = new RemoteCommandSupport(executionEnvironment, GET_SCRIPT_INFO);
        support.run();
        if (!support.isFailed()) {
            RemoteUtil.LOGGER.fine("RSS.needsSetupOrUpdate: GET_SCRIPT_INFO returned " + support.getExitStatus());
            if (support.getExitStatus() == 0) {
                String val = support.getOutput();
                for (String line : val.split("\n")) { // NOI18N
                    try {
                        int pos = line.indexOf(':');
                        if (pos > 0 && line.length() > 0) {
                            String script = line.substring(REMOTE_SCRIPT_DIR.length(), pos);
                            Double installedVersion = Double.valueOf(line.substring(pos + 9));
                            Double expectedVersion = scriptSetupMap.get(script);
                            if (expectedVersion != null && expectedVersion > installedVersion) {
                                RemoteUtil.LOGGER.fine("RSS.getScriptUpdates: Need to update " + script);
                                list.add(script);
                            }
                        } else {
                            RemoteUtil.LOGGER.warning("RSS.getScriptUpdates: Grep returned [" + line + "]");
                        }
                    } catch (NumberFormatException nfe) {
                        RemoteUtil.LOGGER.warning("RSS.getScriptUpdates: Bad response from remote grep comand (NFE parsing version)");
                    } catch (Exception ex) {
                        RemoteUtil.LOGGER.warning("RSS.getScriptUpdates: Bad response from remote grep comand: " + ex.getClass().getName());
                    }
                }
            } else {
                if (!support.isCancelled()) {
                    RemoteUtil.LOGGER.fine("RSS.getScriptUpdates: Need to create ~/" + REMOTE_SCRIPT_DIR);
                    list.add(REMOTE_SCRIPT_DIR);
                } else if (support.isCancelled()) {
                        cancelled = true;
                } else {
                    RemoteUtil.LOGGER.warning("RSS.getScriptUpdates: Unexpected  exit code [" + support.getExitStatus() + "]");
                }
            }
        } else {
            // failed
            failed = true;
            reason = support.getFailureReason();
        }
        return list;
    }
    
    private List<String> getBinaryUpdates(List<String> list) {

        // Parsing ls output doesn't work, since it differs in diferent OSes
        // (not to mention localization):
        // For example, Ubuntu says:
        // ls: cannot access .netbeans/6.7/cnd2/lib/rfs_preload-SunOS-x86.so: No such file or directory
        // while Solaris says
        // .netbeans/6.7/cnd2/lib/rfs_preload-SunOS-x86.so: No such file or directory

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
                int count = 0;
                for (String line : val.split("\n")) { // NOI18N
                    if (line.length() > 0) {
                        if (count++ == 0) {
                            list.add(REMOTE_LIB_DIR);
                        }
                        list.add(line);
                    }
                }
            }
        } else {
            // failed
            failed = true;
            reason = support.getFailureReason();
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
    
    protected boolean isFailed() {
        return failed;
    }

    private boolean isFailedOrCanceled() {
        return failed || cancelled;
    }    
}

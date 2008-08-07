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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.remote.support.RemoteCommandSupport;
import org.netbeans.modules.cnd.remote.support.RemoteCopySupport;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;

/**
 *
 * @author gordonp
 */
public class RemoteServerSetup {
    
    private static Logger log = Logger.getLogger("cnd.remote.logger"); // NOI18N
    private static final String REMOTE_SCRIPT_DIR = ".netbeans/6.5/cnd2/scripts/"; // NOI18N
    private static final String LOCAL_SCRIPT_DIR = "src/scripts/"; // NOI18N
    private static final String GET_SCRIPT_INFO = "PATH=/bin:/usr/bin:$PATH  grep VERSION= " + REMOTE_SCRIPT_DIR + "* /dev/null 2> /dev/null"; // NOI18N
    private static final String DOS2UNIX_CMD = "PATH=/bin:/usr/bin:$PATH  dos2unix " + REMOTE_SCRIPT_DIR; // NOI18N
    private static final String GET_LIB_INFO = "PATH=/bin:/usr/bin:$PATH  ls -1 2>&1 "; // NOI18N
    private static final String REMOTE_LIB_DIR = ".netbeans/6.5/cnd2/lib/"; // NOI18N
    
    private final Map<String, Double> scriptSetupMap;
    private final Map<String, String> binarySetupMap;
    private final Map<String, List<String>> updateMap;
    private final String hkey;
    private boolean cancelled;
    private boolean failed;
    private String reason;
    
    protected RemoteServerSetup(String hkey) {
        this.hkey = hkey;
        
        // Script setup map
        scriptSetupMap = new HashMap<String, Double>();
        scriptSetupMap.put("getCompilerSets.bash", Double.valueOf(0.6)); // NOI18N
        
        // Binary setup map
        binarySetupMap = new HashMap<String, String>();
        binarySetupMap.put(".netbeans/6.5/cnd2/lib/GdbHelper-Linux-x86.so", "bin/GdbHelper-Linux-x86.so"); // NOI18N
        binarySetupMap.put(".netbeans/6.5/cnd2/lib/GdbHelper-SunOS-x86.so", "bin/GdbHelper-SunOS-x86.so"); // NOI18N
        binarySetupMap.put(".netbeans/6.5/cnd2/lib/GdbHelper-SunOS-sparc.so", "bin/GdbHelper-SunOS-sparc.so"); // NOI18N
        
        updateMap = new HashMap<String, List<String>>();
    }

    protected boolean needsSetupOrUpdate() {
        List<String> updateList = new ArrayList<String>();
        
        updateMap.clear(); // remote entries if run for other remote systems
        updateList = getScriptUpdates(updateList);
        if (!isFailedOrCanceled()) {
            updateList = getBinaryUpdates(updateList);
        }
        
        if (!updateList.isEmpty()) {
            updateMap.put(hkey, updateList);
            return true;
        } else {
            return false;
        }
    }
    
    protected  void setup() {
        List<String> list = updateMap.remove(hkey);
        boolean bupdate = false;
        
        for (String path : list) {
            if (path.equals(REMOTE_SCRIPT_DIR)) {
                log.fine("RSS.setup: Creating ~/" + REMOTE_SCRIPT_DIR);
                int exit_status = RemoteCommandSupport.run(hkey,
                        "PATH=/bin:/usr/bin:$PATH mkdir -p " + REMOTE_SCRIPT_DIR); // NOI18N
                if (exit_status == 0) {
                    for (String key : scriptSetupMap.keySet()) {
                        log.fine("RSS.setup: Copying" + path + " to " + hkey);
                        File file = InstalledFileLocator.getDefault().locate(LOCAL_SCRIPT_DIR + key, null, false);
                        RemoteCopySupport.copyTo(hkey, file.getAbsolutePath(), REMOTE_SCRIPT_DIR);
                        RemoteCommandSupport.run(hkey, DOS2UNIX_CMD + key + ' ' + REMOTE_SCRIPT_DIR + key);
                    }
                } else {
                    reason = NbBundle.getMessage(RemoteServerSetup.class, "ERR_DirectorySetupFailure", hkey, exit_status);
                }
            } else if (path.equals(REMOTE_LIB_DIR)) {
                log.fine("RSS.setup: Creating ~/" + REMOTE_LIB_DIR);
                int exit_status = RemoteCommandSupport.run(hkey,
                        "PATH=/bin:/usr/bin:$PATH mkdir -p " + REMOTE_LIB_DIR); // NOI18N
                if (exit_status == 0) {
                    bupdate = true;
                    for (String key : binarySetupMap.keySet()) {
                        String loc = binarySetupMap.get(key);
                        log.fine("RSS.setup: Copying" + loc + " to " + hkey);
                        File file = InstalledFileLocator.getDefault().locate(loc, null, false);
                        if (file != null && file.exists()) {
                            RemoteCopySupport.copyTo(hkey, file.getAbsolutePath(), REMOTE_LIB_DIR);
                        } else {
                            reason = NbBundle.getMessage(RemoteServerSetup.class, "ERR_UpdateSetupFailure", hkey, key);
                        }
                    }
                } else {
                    reason = NbBundle.getMessage(RemoteServerSetup.class, "ERR_DirectorySetupFailure", hkey, exit_status);
                }
            } else {
                log.fine("RSS.setup: Updating \"" + path + "\" on " + hkey);
                if (binarySetupMap.containsKey(path)) {
                    bupdate = true;
                    String loc = binarySetupMap.get(path);
                    File file = InstalledFileLocator.getDefault().locate(loc, null, false);
                    boolean brc = RemoteCopySupport.copyTo(hkey, file.getAbsolutePath(), REMOTE_LIB_DIR);
                    if (brc) {
                        reason = NbBundle.getMessage(RemoteServerSetup.class, "ERR_UpdateSetupFailure", hkey, path);
                    }
                } else {
                    File file = InstalledFileLocator.getDefault().locate(LOCAL_SCRIPT_DIR + path, null, false);
                    boolean brc = RemoteCopySupport.copyTo(hkey, file.getAbsolutePath(), REMOTE_SCRIPT_DIR);
                    int rc = RemoteCommandSupport.run(hkey, DOS2UNIX_CMD + path + ' ' + REMOTE_SCRIPT_DIR + path);
                    if (brc || rc != 0) {
                        reason = NbBundle.getMessage(RemoteServerSetup.class, "ERR_UpdateSetupFailure", hkey, path);
                    }
                }
            }
        }
        if (bupdate) {
            RemoteCommandSupport.run(hkey, "PATH=/bin:/usr/bin:$PATH chmod 755 " + REMOTE_LIB_DIR + "/*.so");
        }
    }
    
    private List<String> getScriptUpdates(List<String> list) {
        RemoteCommandSupport support = new RemoteCommandSupport(hkey, GET_SCRIPT_INFO);
        if (!support.isFailed()) {
            log.fine("RSS.needsSetupOrUpdate: GET_SCRIPT_INFO returned " + support.getExitStatus());
            if (support.getExitStatus() == 0) {
                String val = support.toString();
                for (String line : val.split("\n")) { // NOI18N
                    try {
                        int pos = line.indexOf(':');
                        if (pos > 0 && line.length() > 0) {
                            String script = line.substring(REMOTE_SCRIPT_DIR.length(), pos);
                            Double installedVersion = Double.valueOf(line.substring(pos + 9));
                            Double expectedVersion = scriptSetupMap.get(script);
                            if (expectedVersion != null && expectedVersion > installedVersion) {
                                log.fine("RSS.getScriptUpdates: Need to update " + script);
                                list.add(script);
                            }
                        } else {
                            log.warning("RSS.getScriptUpdates: Grep returned [" + line + "]");
                        }
                    } catch (NumberFormatException nfe) {
                        log.warning("RSS.getScriptUpdates: Bad response from remote grep comand (NFE parsing version)");
                    } catch (Exception ex) {
                        log.warning("RSS.getScriptUpdates: Bad response from remote grep comand: " + ex.getClass().getName());
                    }
                }
            } else {
                if (!support.isCancelled()) {
                    log.fine("RSS.getScriptUpdates: Need to create ~/" + REMOTE_SCRIPT_DIR);
                    list.add(REMOTE_SCRIPT_DIR);
                } else if (support.isCancelled()) {
                        cancelled = true;
                } else {
                    log.warning("RSS.getScriptUpdates: Unexpected  exit code [" + support.getExitStatus() + "]");
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
        RemoteCommandSupport support = new RemoteCommandSupport(hkey, GET_LIB_INFO + getBinarySetupFiles());
        if (!support.isFailed()) {
            log.fine("RSS.getBinaryUpdates: GET_LIB_INFO returned " + support.getExitStatus());
            if (support.isCancelled()) {
                cancelled = true;
            } else {
                String val = support.toString();
                int count = 0;
                for (String line : val.split("\n")) { // NOI18N
                    int pos = line.indexOf(':');
                    if (pos > 0) {
                        if (count++ == 0) {
                            list.add(REMOTE_LIB_DIR);
                        }
                        list.add(line.substring(0, pos));
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
        String msg;
        
        if (reason.contains("UnknownHostException")) { // NOI18N
            int pos = reason.lastIndexOf(' ');
            String host = reason.substring(pos + 1);
            msg = NbBundle.getMessage(RemoteServerSetup.class, "REASON_UnknownHost", host);
        } else if (reason.equals("Auth failed")) { // NOI18N
            msg = NbBundle.getMessage(RemoteServerSetup.class, "REASON_AuthFailed");
        } else {
            msg = reason;
        }
        return msg;
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
    
    private String getBinarySetupFiles() {
        StringBuilder sb = new StringBuilder();
        
        for (String path : binarySetupMap.keySet()) {
            sb.append(path);
            sb.append(' ');
        }
        return sb.toString();
    }
}

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
package org.netbeans.modules.cnd.remote.mapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.cnd.remote.support.RemoteCommandSupport;
import org.netbeans.modules.cnd.remote.ui.EditPathMapDialog;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.NbPreferences;

/**
 * An implementation of PathMap which returns remote path information.
 * 
 * @author gordonp
 */
public class RemotePathMap extends PathMap {

    private final static Map<ExecutionEnvironment, RemotePathMap> pmtable =
            new HashMap<ExecutionEnvironment, RemotePathMap>();

    public static RemotePathMap getPathMap(ExecutionEnvironment env) {
        RemotePathMap pathmap = pmtable.get(env);
        if (pathmap == null) {
            synchronized (pmtable) {
                pathmap = new RemotePathMap(env);
                pmtable.put(env, pathmap);
            }
        }
        return pathmap;
    }

    public static boolean isReady(ExecutionEnvironment execEnv) {
        return pmtable.get(execEnv) != null;
    }

    //

    private final HashMap<String, String> map = new HashMap<String, String>();
    private final ExecutionEnvironment execEnv;

    private RemotePathMap(ExecutionEnvironment execEnv) {
        this.execEnv = execEnv;
        init();
    }

    /** 
     *
     * Initialization the path map here:
     */
    public void init() {
        synchronized ( map ) {
            String list = getPreferences(execEnv);

            if (list == null) {
                // 1. Developers entry point
                String pmap = System.getProperty("cnd.remote.pmap");
                if (pmap != null) {
                    String line;
                    File file = new File(pmap);

                    if (file.exists() && file.canRead()) {
                        try {
                            BufferedReader in = new BufferedReader(new FileReader(file));
                            try {
                                while ((line = in.readLine()) != null) {
                                    int pos = line.indexOf(' ');
                                    if (pos > 0) {
                                        map.put(line.substring(0, pos), line.substring(pos + 1).trim());
                                    }
                                }
                            } finally {
                                in.close();
                            }
                        } catch (IOException ioe) {
                        }
                    }
                } else {
                    // 2. Automated mappings gathering entry point
                    HostMappingsAnalyzer ham = new HostMappingsAnalyzer(execEnv);
                    map.putAll(ham.getMappings());
                    // TODO: what about consequent runs. User may share something, we need to check it
                }
            } else {
                // 3. Deserialization
                String[] paths = list.split(DELIMITER);
                for (int i = 0; i < paths.length; i+=2) {
                    if (i+1 < paths.length) { //TODO: only during development
                        map.put(paths[i], paths[i+1]);
                    } else {
                        System.err.println("mapping serialization flaw. Was found: " + list);
                    }
                }
            }
        }
    }

    // PathMap
    public String getRemotePath(String lpath, boolean useDefault) {
        String ulpath = unifySeparators(lpath);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = unifySeparators(entry.getKey());
            if (ulpath.startsWith(key)) {
                String mpoint = entry.getValue();
                return mpoint + lpath.substring(key.length()).replace('\\', '/');
            }
        }
        return useDefault ? lpath : null;
    }

    public String getLocalPath(String rpath, boolean useDefault) {
        String urpath = unifySeparators(rpath);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String value = unifySeparators(entry.getValue());
            if (urpath.startsWith(value)) {
                String mpoint = entry.getKey();
                return mpoint + rpath.substring(value.length());
            }
        }
        return useDefault ? rpath : null;
    }

    /**
     * See if a path is local or remote. The main use of this call is to verify a project's
     * Development Host setting. If the project's sources are local then you should not be
     * able to set a remote development host.
     * 
     * @param lpath The local path to check
     * @return true if path is remote, false otherwise
     */
    public boolean checkRemotePath(String lpath, boolean fixMissingPaths) {
        String ulpath = unifySeparators(lpath);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String mpoint = unifySeparators(entry.getValue());
            if (ulpath.startsWith(mpoint)) {
                return true;
            }
        }

        for (String mpoint : map.keySet()) {
            if (ulpath.startsWith(unifySeparators(mpoint))) {
                return true;
            }
        }

        try {
            // check if local path is mirrored by remote path
            if (validateMapping(execEnv, lpath, new File(lpath))) {
                synchronized (map) {
                    map.put(lpath, lpath);
                }
                return true;
            }
        } catch (InterruptedException ex) {
            return false;
        }

        if (fixMissingPaths) {
            return EditPathMapDialog.showMe(execEnv, lpath) && checkRemotePath(lpath, false);
        } else {
            return false;
        }

    }

    public void addMapping(String localParent, String remoteParent) {
        synchronized( map ) {
            Map<String, String> clone = new LinkedHashMap<String, String>(map);
            clone.put(localParent,remoteParent);
            updatePathMap(clone);
        }
    }


    // Utility
    public void updatePathMap(Map<String, String> newPathMap) {
        synchronized( map ) {
            map.clear();
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : newPathMap.entrySet()) {
                String remotePath = fixEnding(entry.getValue());
                String path = fixEnding(entry.getKey());
                map.put(path, remotePath);
                sb.append( fixEnding(path) );
                sb.append(DELIMITER);
                sb.append( remotePath );
                sb.append(DELIMITER);
            }
            setPreferences(sb.toString());
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getMap() {
        return (Map<String, String>)map.clone();
    }

    private static String fixEnding(String path) {
        //TODO: system dependent separator?
        if (path.charAt(path.length()-1)!='/' && path.charAt(path.length()-1)!='\\') {
            return path + "/"; //NOI18N
        } else {
            return path;
        }
    }
    // inside path mapper we use only / and lowercase 
    // TODO: lowercase should be only windows issue -- possible flaw
    private static String unifySeparators(String path) {
        return path.replace('\\', '/').toLowerCase();
    }

    public static boolean isSubPath(String path, String pathToValidate) {
        return unifySeparators(pathToValidate).startsWith(unifySeparators(path));
    }

    private static final String REMOTE_PATH_MAP = "remote-path-map"; // NOI18N
    private static final String DELIMITER = "\n"; // NOI18N

    private static String getPreferences(ExecutionEnvironment execEnv) {
        return NbPreferences.forModule(RemotePathMap.class).get(
                REMOTE_PATH_MAP + ExecutionEnvironmentFactory.toUniqueID(execEnv), null);
    }

    private void setPreferences(String newValue) {
        NbPreferences.forModule(RemotePathMap.class).put(
                REMOTE_PATH_MAP + ExecutionEnvironmentFactory.toUniqueID(execEnv), newValue);
    }

    private static boolean validateMapping(ExecutionEnvironment execEnv,
            String rpath, File lpath) throws InterruptedException {
        if (!PlatformInfo.getDefault(execEnv).isWindows() && !PlatformInfo.getDefault(ExecutionEnvironmentFactory.getLocal()).isWindows()) {
            return isTheSame(execEnv, rpath, lpath);
        }
        return false;
    }
    
    /**
     * Determines whether local and remote directories coincide,
     * i.e. map to the same physical directory
     * @param execEnv remote environment
     * @param localDir local path
     * @param remoteDir remote path
     * @return
     */
    // TODO: move to a more appropriate place
    public static boolean isTheSame(ExecutionEnvironment execEnv, String rpath, File path) throws InterruptedException {
        if (path.exists() && path.isDirectory()) {
            File validationFile = null;
            try {
                // create file
                validationFile = File.createTempFile("cnd", "tmp", path); // NOI18N
                if (validationFile.exists()) {
                    BufferedWriter out = new BufferedWriter(new FileWriter(validationFile));
                    String validationLine = Double.toString(Math.random());
                    out.write(validationLine);
                    out.close();

                    RemoteCommandSupport rcs = new RemoteCommandSupport(
                            execEnv, "grep", null, // NOI18N
                            validationLine,
                            rpath + "/" + validationFile.getName()); // NOI18N

                    if (rcs.run() == 0) {
                        return true;
                    }
                    if (rcs.isCancelled() || rcs.isInterrupted()) {
                        throw new InterruptedException();
                    }
                }
            } catch (IOException ex) {
                // directory is write protected
            } finally {
                if (validationFile != null && validationFile.exists()) {
                    validationFile.delete();
                }
            }
        }
        return false;
    }
}

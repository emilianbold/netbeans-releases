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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.api.remote.PathMap;

/**
 * An implementation of PathMap which returns remote path information.
 * 
 * @author gordonp
 */
public class RemotePathMap extends HashMap<String, String> implements PathMap {
    
    private static HashMap<String, RemotePathMap> pmtable = new HashMap<String, RemotePathMap>();
    
    private String hkey;

    public static RemotePathMap getMapper(String hkey) {
        RemotePathMap pathmap = pmtable.get(hkey);
        
        if (pathmap == null) {
            pathmap = new RemotePathMap(hkey);
            pmtable.put(hkey, pathmap);
        }
        return pathmap;
    }
    
    private RemotePathMap(String hkey) {
        this.hkey = hkey;
        init();
    }
    
    /** 
     * Initialization the path map here:
     * Windows Algorythm:
     *    1. Get the drive letter
     *    2. See if there is an NFS mount point in the Windows registry
     *    3. Run a RemotePathMapSupport(host, user, [mount point host], [mount point path])
     * 
     * Unix Algorythm:
     *    1. TBD 
     */
    private void init() {
        if (Boolean.getBoolean("cnd.remote.enable")) { // Debug
            if (hkey.startsWith("gordonp@")) { // Debug
                put("z:/", "/net/pucci/export/pucci1/"); // Debug
                put("x:/", "/net/pucci/export/pucci2/"); // Debug
                put("/net/pucci/", "/net/pucci/"); // Debug
            } else if (hkey.startsWith("sg155630@")) { // Debug
                put("z:/", "/home/sg155630/"); // Debug
            }
        }
        
        String pmap = System.getProperty("cnd.remote.pmap");
        if (pmap != null) {
            String line;
            File file = new File(pmap);
            
            if (file.exists() && file.canRead()) {
                try {
                    BufferedReader in = new BufferedReader(new FileReader(file));
                    while ((line = in.readLine()) != null) {
                        int pos = line.indexOf(' ');
                        if (pos > 0) {
                            put(line.substring(0, pos), line.substring(pos + 1).trim());
                        }
                    }
                } catch (IOException ioe) {
                }
            }
        }
    }
    
    public String getRemotePath(String lpath) {
        for (String mpoint : keySet()) {
            if (lpath.startsWith(mpoint)) {
                return mpoint + lpath.substring(mpoint.length());
            }
        }
        return lpath;
    }
    
    public String getLocalPath(String rpath) {
        for (Map.Entry<String, String> entry : entrySet()) {
            String value = entry.getValue();
            if (rpath.startsWith(value)) {
                String mpoint = entry.getKey();
                return mpoint + rpath.substring(value.length());
            }
        }
        return rpath;
    }
    
    /**
     * See if a path is local or remote. The main use of this call is to verify a project's
     * Development Host setting. If the project's sources are local then you should not be
     * able to set a remote development host.
     * 
     * @param lpath The local path to check
     * @return true if path is remote, false otherwise
     */
    public boolean isRemote(String lpath) {
        for (String mpoint : keySet()) {
            if (lpath.startsWith(mpoint)) {
                return true;
            }
        }
        return false;
    }
}

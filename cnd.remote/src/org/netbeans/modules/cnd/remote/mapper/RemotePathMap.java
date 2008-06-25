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

import java.util.HashMap;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.openide.util.Utilities;

/**
 * An implementation of PathMap which returns remote path information.
 * 
 * @author gordonp
 */
public class RemotePathMap extends HashMap<String, String> implements PathMap {
    
    private static HashMap<String, RemotePathMap> pmtable = new HashMap<String, RemotePathMap>();
    
    private String user;
    private String host;

    public static RemotePathMap getMapper(String user, String host) {
        RemotePathMap pathmap = pmtable.get(makeKey(user, host));
        
        if (pathmap == null) {
            pathmap = new RemotePathMap(user, host);
            pmtable.put(makeKey(user, host), pathmap);
        }
        return pathmap;
    }
    
    private static String makeKey(String user, String host) {
        return user + ' ' + host;
    }
    
    private RemotePathMap(String user, String host) {
        this.user = user;
        this.host = host;
    }
    
    public String getRemotePath(String lpath) {
        String rpath = get(lpath);
        
        if (rpath == null) {
            rpath = initializePath(lpath);
        }
        return rpath;
    }
    
    public String getLocalPath(String rpath) {
//        String rpath = get(lpath);
//        
//        if (rpath == null) {
//            rpath = initializePath(lpath);
//        }
        return rpath;
    }
    
    /**
     * See if a path is local or remote. The main use of this call is to verify a project's
     * Development Host setting. If the project's sources are local then you should not be
     * able to set a remote development host.
     * 
     * @param path The path to check
     * @return true if path is remote, false otherwise
     */
    public boolean isRemote(String path) {
        if (Boolean.getBoolean("cnd.remote.enable")) { // Debug
            String ch = path.substring(0, 2).toLowerCase();
            if (user.equals("gordonp")) { // Debug
                if (ch.equals("z:") || ch.equals("x:")) { // Debug
                    return true; // Debug
                } else if (ch.equals("c:") || ch.equals("d:")) { // Debug
                    return false; // Debug
                } else if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) {
                    return true; // for me, all relevant Solaris paths are remotely visible...
                }
            } else if (user.equals("sg155630")) { // Debug
                // fill in your debugging pathmaps if you want...
            }
        }
        return false;
    }
    
    /** 
     * Implement the path initialization here:
     * Windows Algorythm:
     *    1. Get the drive letter
     *    2. See if there is an NFS mount point in the Windows registry
     *    3. Run a RemotePathMapSupport(host, user, [mount point host], [mount point path])
     * 
     * Unix Algorythm:
     *    1. TBD 
     */
    private String initializePath(String lpath) {
        String rpath = null;
        
        if (Boolean.getBoolean("cnd.remote.enable")) { // Debug
            if (user.equals("gordonp")) { // Debug
                if (lpath.toLowerCase().startsWith("z:")) { // Debug
                    rpath = "/net/pucci/export/pucci1/" + lpath.substring(2); // Debug
                } else if (lpath.toLowerCase().startsWith("x:")) { // Debug
                    rpath = "/net/pucci/export/pucci2/" + lpath.substring(2); // Debug
                } else if (lpath.startsWith("/export/")) { // Debug
                    rpath = "/net/" + host + lpath; // Debug
                } else {
                    rpath = lpath;
                }
            } else if (user.equals("sg155630")) { // Debug
                // fill in your debugging pathmaps if you want...
            }
        }
        
        if (rpath != null) {
            put(lpath, rpath);
        } else {
            rpath = "/tmp"; // Debug
        }
        
        return rpath;
    }
}

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

/**
 *
 * @author gordonp
 */
public class RemotePathMap extends HashMap<String, String> {
    
    private static HashMap<String, RemotePathMap> pmtable = new HashMap<String, RemotePathMap>();
    
    private String host;
    private String user;

    public static RemotePathMap getMapper(String host, String user) {
        RemotePathMap pathmap = pmtable.get(makeKey(host, user));
        
        if (pathmap == null) {
            pathmap = new RemotePathMap(host, user);
            pmtable.put(makeKey(host, user), pathmap);
        }
        return pathmap;
    }
    
    private static String makeKey(String host, String user) {
        return host + " " + user;
    }
    
    private RemotePathMap(String host, String user) {
        this.host = host;
        this.user = user;
    }
    
    public String getPath(String lpath) {
        String rpath = get(lpath);
        
        if (rpath == null) {
            rpath = initializePath(lpath);
        }
        return rpath;
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
        
        if (Boolean.getBoolean("cnd.remote.enabled")) { // this means we're still debugging...
            if (user.equals("gordonp")) {
                if (lpath.toLowerCase().startsWith("z:")) {
                    rpath = "/net/pucci/export/pucci1/" + lpath.substring(2);
                } else if (lpath.toLowerCase().startsWith("x:")) {
                    rpath = "/net/pucci/export/pucci2/" + lpath.substring(2);
                } else if (lpath.startsWith("/export/")) {
                    rpath = "/net/" + host + lpath;
                } else /*if (lpath.startsWith("/net/"))*/ {
                    rpath = lpath;
                }
            } else if (user.equals("sg155630")) {
                // fill in your debugging pathmaps if you want...
            }
        }
        
        if (rpath != null) {
            put(lpath, rpath);
        } else {
            rpath = "/tmp";
        }
        
        return rpath;
    }
}

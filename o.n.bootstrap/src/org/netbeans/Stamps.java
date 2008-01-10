/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Support for optimal checking of time stamps of certain files in
 * NetBeans directory structure. 
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 * @since 2.9
 */
public final class Stamps {
    private static AtomicLong moduleJARs;

    private Stamps() {
    }
    
    /** This class can be executed from command line to perform various checks
     * on installed NetBeans, however outside of running NetBeans.
     * 
     */
    public static void main(String... args) {
        if (args.length == 1 && "reset".equals(args[0])) { // NOI18N
            moduleJARs = null;
            stamp(false);
            return;
        }
    }
    
    /** Computes and returns timestamp for all files that affect
     * module classloading and related caches.
     * @return
     */
    public static long moduleJARs() {
        AtomicLong local = moduleJARs;
        if (local == null) {
            local = moduleJARs = stamp(true);
        }
        return local.longValue();
    }
    

    
    //
    // Implementation. As less dependecies on other NetBeans clases, as possible, please.
    // This will be called externally from a launcher.
    //
    

    private static AtomicLong stamp(boolean checkStampFile) {
        AtomicLong result = new AtomicLong();
        
        Set<File> processedDirs = new HashSet<File>();
        String home = System.getProperty ("netbeans.home"); // NOI18N
        if (home != null) {
            stampForCluster (new File (home), result, processedDirs, checkStampFile, true);
        }
        String nbdirs = System.getProperty("netbeans.dirs"); // NOI18N
        if (nbdirs != null) {
            StringTokenizer tok = new StringTokenizer(nbdirs, File.pathSeparator);
            while (tok.hasMoreTokens()) {
                stampForCluster(new File(tok.nextToken()), result, processedDirs, checkStampFile, true);
            }
        }
        String user = System.getProperty ("netbeans.user"); // NOI18N
        if (user != null) {
            stampForCluster (new File (user), result, new HashSet<File> (), false, false);
        }
        
        return result;
    }
    
    private static void stampForCluster(
        File cluster, AtomicLong result, Set<File> hashSet, 
        boolean checkStampFile, boolean createStampFile
    ) {
        File stamp = new File(cluster, ".lastModified"); // NOI18N
        long time;
        if (checkStampFile && (time = stamp.lastModified()) > 0) {
            if (time > result.longValue()) {
                result.set(time);
            }
            return;
        }
        String user = System.getProperty ("netbeans.user"); // NOI18N
        if (user != null) {
            File userDir = new File(user);
            stamp = new File(new File(new File(new File(userDir, "var"), "cache"), "lastModified"), cluster.getName());
            if (checkStampFile && (time = stamp.lastModified()) > 0) {
                if (time > result.longValue()) {
                    result.set(time);
                }
                return;
            }
        } else {
            createStampFile = false;
        }

    
        File configDir = new File(new File(cluster, "config"), "Modules"); // NOI18N
        File modulesDir = new File(cluster, "modules"); // NOI18N
        
        highestStampForDir(configDir, result);
        highestStampForDir(modulesDir, result);
    
        if (createStampFile) {
            try {
                stamp.getParentFile().mkdirs();
                stamp.createNewFile();
                stamp.setLastModified(result.longValue());
            } catch (IOException ex) {
                System.err.println("Cannot write timestamp to " + stamp); // NOI18N
            }
        }
    }

    private static void highestStampForDir(File file, AtomicLong result) {
        File[] children = file.listFiles();
        if (children == null) {
            long time = file.lastModified();
            if (time > result.longValue()) {
                result.set(time);
            }
            return;
        }
        
        for (File f : children) {
            highestStampForDir(f, result);
        }

    }

}

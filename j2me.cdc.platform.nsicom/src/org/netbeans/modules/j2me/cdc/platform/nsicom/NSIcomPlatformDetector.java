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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.j2me.cdc.platform.nsicom;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import org.netbeans.modules.j2me.cdc.platform.CDCDevice;
import org.netbeans.modules.j2me.cdc.platform.CDCPlatform;
import org.netbeans.modules.j2me.cdc.platform.spi.CDCPlatformDetector;
import org.netbeans.modules.j2me.cdc.platform.spi.CDCPlatformUtil;
import org.netbeans.modules.j2me.cdc.platform.spi.StreamReader;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Utilities;

/**
 *
 * @author suchys
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.j2me.cdc.platform.spi.CDCPlatformDetector.class)
public class NSIcomPlatformDetector extends CDCPlatformDetector {
    
    private static Set propertiesToFix = new HashSet ();
    
    //Properties used by IDE which should be fixed not to use resolved symlink
    static {
        propertiesToFix.add ("sun.boot.class.path");    //NOI18N
        propertiesToFix.add ("sun.boot.library.path");  //NOI18N
        propertiesToFix.add ("java.library.path");      //NOI18N
        propertiesToFix.add ("java.ext.dirs");          //NOI18N
        propertiesToFix.add ("java.home");              //NOI18N       
    }

    /** Creates a new instance of CDCPlatform */
    public NSIcomPlatformDetector() {
    }

    public String getPlatformName() {
        return "NSIcom"; //NOI18N
    }

    public String getPlatformType() {
        return "nsicom";
    }    

    public boolean accept(FileObject dir) {
        FileObject tool = CDCPlatformUtil.findTool("bin","pJSCP", Collections.singleton(dir));  //NOI18N
        return tool != null;
    }

    public CDCPlatform detectPlatform(FileObject dir) throws IOException {
        assert dir != null;
        FileObject java = CDCPlatformUtil.findTool("bin","pJSCP", Collections.singleton(dir)); //NOI18N
        if (java == null){
            throw new IOException("pJSCP.exe can not be found in desired location!"); //NOI18N
        }
        File javaFile = FileUtil.toFile (java);
        if (javaFile == null)
            throw new IOException("pJSCP.exe can not be found in desired location!"); //NOI18N
        String javapath = javaFile.getAbsolutePath();
            
        FileObject bin = dir.getFileObject("bin"); //NOI18N        

        String filePath = File.createTempFile("nb-cdcplatformdetect", "properties").getAbsolutePath();
        getSDKProperties(javapath, filePath);
        File f = new File(filePath);
        Properties p = new Properties();
        InputStream is = new FileInputStream(f);
        p.load(is);
        Map m = new HashMap(p.size());
        for (Enumeration en = p.keys(); en.hasMoreElements(); ) {
            String k = (String)en.nextElement();
            String v = (String) p.getProperty(k);
            v = fixSymLinks (dir,k,v);
            m.put(k, v);
        }   
        is.close();
        f.delete();
             
        String name = (String) m.get("java.vm.version");
        assert name != null : "Platform name is null";
        String bcp = (String) m.get("sun.boot.class.path");
        assert bcp != null : "Boot classpath is null";

        Map modes = new HashMap();
        modes.put(CDCPlatform.PROP_EXEC_MAIN, null);
        modes.put(CDCPlatform.PROP_EXEC_APPLET, null);
        //todo - names
        CDCDevice.CDCProfile profile = new CDCDevice.CDCProfile("PP-1.0", "NSIcom CDC-1.0 PP-1.0", "1.0", modes, bcp, null, true);
        CDCDevice device = new CDCDevice();
        device.setProfiles(new CDCDevice.CDCProfile[] {profile});
        
        return new CDCPlatform(name, name, getPlatformType(), "1.3",       //NOI18N
                Collections.singletonList(dir.getURL()), 
                Collections.EMPTY_LIST, 
                Collections.EMPTY_LIST, 
                new CDCDevice[]{device}, true);        
    }

    public int getVersion() {
        return 1;
    }
    
    /**
     * Fixes sun.boot.class.path property if it contains resolved
     * symbolic link. On Suse the jdk is symlinked and during update
     * the link is changed
     *
     */
    private String fixSymLinks (FileObject dir, String key, String value) {
        if (Utilities.isUnix() && propertiesToFix.contains (key)) {
            try {
                String[] pathElements = value.split(File.pathSeparator);
                boolean changed = false;
                File f = FileUtil.toFile ((FileObject) dir);
                if (f != null) {
                    String path = f.getAbsolutePath();
                    String canonicalPath = f.getCanonicalPath();
                    if (!path.equals(canonicalPath)) {
                        for (int i=0; i<pathElements.length; i++) {
                            if (pathElements[i].startsWith(canonicalPath)) {
                                pathElements[i] = path + pathElements[i].substring(canonicalPath.length());
                                changed = true;
                            }
                        }
                    }
                }
                if (changed) {
                    StringBuffer sb = new StringBuffer ();
                    for (int i = 0; i<pathElements.length; i++) {
                        if (i > 0) {
                            sb.append(File.pathSeparatorChar);
                        }
                        sb.append(pathElements[i]);                
                    }
                    return sb.toString();
                }
            } catch (IOException ioe) {
                //Return the original value
            }
        }
        return value;
    }

    private void getSDKProperties(String javaPath, String path) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        try {
            String[] command = new String[5];
            command[0] = javaPath;
            command[1] = "-classpath";    //NOI18N
            command[2] = InstalledFileLocator.getDefault().locate("modules/ext/org-netbeans-modules-j2me-cdc-platform-nsicom-probe.jar", "org.netbeans.modules.java.j2seplatform", false).getAbsolutePath(); // NOI18N
            command[3] = "org.netbeans.modules.j2me.cdc.platform.nsicom.wizard.SDKProbe";
            command[4] = path;
            final Process process = runtime.exec(command);
            // PENDING -- this may be better done by using ExecEngine, since
            // it produces a cancellable task.
            process.waitFor();
            int exitValue = process.exitValue();
            if (exitValue != 0)
                throw new IOException();
        } catch (InterruptedException ex) {
            IOException e = new IOException();
            ErrorManager.getDefault().annotate(e,ex);
            throw e;
        }
    }    
}

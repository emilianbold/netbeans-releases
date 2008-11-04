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

package org.netbeans.modules.j2me.cdc.platform.nokias80;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.netbeans.modules.j2me.cdc.platform.CDCDevice;
import org.netbeans.modules.j2me.cdc.platform.CDCPlatform;
import org.netbeans.modules.j2me.cdc.platform.spi.CDCPlatformDetector;
import org.netbeans.modules.j2me.cdc.platform.spi.CDCPlatformUtil;
import org.netbeans.modules.j2me.cdc.platform.spi.StreamReader;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author suchys
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.j2me.cdc.platform.spi.CDCPlatformDetector.class)
public class NokiaPlatformDetector extends CDCPlatformDetector {
    
    /** Creates a new instance of CDCPlatform */
    public NokiaPlatformDetector() {
    }

    public String getPlatformName() {
        return "Nokia S80 CDC"; //NOI18N
    }

    public String getPlatformType() {
        return "nokiaS80";
    }    
    
    public boolean accept(FileObject dir) {
        FileObject tool = CDCPlatformUtil.findTool("epoc32/release/wins/udeb","epoc", Collections.singleton(dir));  //NOI18N
        FileObject tool2 = CDCPlatformUtil.findTool("bin","emulator", Collections.singleton(dir));  //NOI18N
        return (tool != null && tool2 != null); 
    }

    public CDCPlatform detectPlatform(FileObject dir) throws IOException {        
        assert dir != null;
        FileObject java = CDCPlatformUtil.findTool("bin","emulator", Collections.singleton(dir)); //NOI18N
        if (java == null){
            throw new IOException("emulator.exe can not be found in desired location!"); //NOI18N
        }
        File javaFile = FileUtil.toFile (java);
        if (javaFile == null)
            throw new IOException("emulator.exe can not be found in desired location!"); //NOI18N
        String javapath = javaFile.getAbsolutePath();
            
        FileObject bin = dir.getFileObject("bin"); //NOI18N
        
        List jdocs = new ArrayList();
        File base = FileUtil.toFile(dir);
        if (base != null){
            for (int i = 0; i < NOKIA_JAVADOC_FOLDERS.length; i++){
                File f = new File (base, NOKIA_JAVADOC_FOLDERS[i]); //NOI18N
                if (f.isDirectory() && f.canRead()) {
                    jdocs.add(FileUtil.toFileObject(f));
                }                        
            }
        }
        File path = FileUtil.toFile(bin);
        
        StringBuffer sb = new StringBuffer();
        List env = new ArrayList();        
        String absPath = FileUtil.toFile(dir).getAbsolutePath();
        //do not depend on user setting, prepend this path before others, can broke by Nokia installation of S80 otherwise!
        env.add("SDKDRIVE=" + String.valueOf(absPath.charAt(0))); //NOI18N
        env.add("EPOCROOT=" + absPath.substring(2) + "\\"); //NOI18N
        env.add("Path=" + absPath + "\\epoc32\\gcc\\bin;" +  //NOI18N
                absPath + "\\epoc32\\tools;" +  //NOI18N
                absPath + "\\epoc32\\include;" + //NOI18N
                System.getProperty("java.library.path")); //NOI18N

        try {
            String[] command = new String[2];
            command[0] = javapath;
            command[1] = "-version"; //NOI18N
            final Process process = Runtime.getRuntime().exec(command, 
                    env != null ? (String[]) env.toArray(new String[0]) : null, path);
            StreamReader ior = new StreamReader(process.getInputStream(), sb);
            StringBuffer err = new StringBuffer();
            StreamReader irr = new StreamReader(process.getErrorStream(), err);

            // PENDING -- this may be better done by using ExecEngine, since
            // it produces a cancellable task.
            ior.join();
            irr.join();
            process.waitFor();
	    process.getOutputStream().close();
            int exitValue = process.exitValue();
            System.out.println(err);
            if (exitValue != 0)
                throw new IOException();
        } catch (InterruptedException ex) {
            IOException e = new IOException();
            ErrorManager.getDefault().annotate(e,ex);
            throw e;
        }
        String name = sb.toString();
        int i = name.indexOf('\n');
        if (i != -1){
            name = name.substring(0, i);
        }

        sb = new StringBuffer();
        try {
            String[] command = new String[2];
            command[0] = javapath;
            command[1] = "-Xquery"; //NOI18N
            final Process process = Runtime.getRuntime().exec(command, 
                    env != null ? (String[]) env.toArray(new String[0]) : null, path);
            StreamReader ior = new StreamReader(process.getInputStream(), sb);
            StringBuffer err = new StringBuffer();
            StreamReader irr = new StreamReader(process.getErrorStream(), err);

            // PENDING -- this may be better done by using ExecEngine, since
            // it produces a cancellable task.
            ior.join();
            irr.join();
            process.waitFor();
	    process.getOutputStream().close();
            int exitValue = process.exitValue();
            System.out.println(err);
            if (exitValue != 0)
                throw new IOException();
        } catch (InterruptedException ex) {
            IOException e = new IOException();
            ErrorManager.getDefault().annotate(e,ex);
            throw e;
        }
        BufferedReader br = new BufferedReader( new StringReader(sb.toString()));
        String line;
        String devices = null;
        StringBuffer newCp = new StringBuffer();
        String width = null, height = null, bitDepth = null, isColor = null;
        while ((line = br.readLine()) != null){
            if (line.startsWith("device.list")){
                devices = line.substring(line.indexOf(':') + 1).trim(); //do expect only one
            }
            else if (line.startsWith(devices + ".bootclasspath")){
                String bcp = line.substring(line.indexOf(':') + 1).trim();
                StringTokenizer st = new StringTokenizer(bcp, ",");
                while( st.hasMoreTokens() ){
                    newCp.append(st.nextToken().trim());
                    if (st.hasMoreTokens()){
                        newCp.append(';');
                    }
                }                    
            }
            else if (line.startsWith(devices + ".screen.width")){
                width = line.substring(line.indexOf(':') + 1).trim();                
            }
            else if (line.startsWith(devices + ".screen.height")){
                height = line.substring(line.indexOf(':') + 1).trim();
            }
            else if (line.startsWith(devices + ".screen.isColor")){
                isColor = line.substring(line.indexOf(':') + 1).trim();
            }
            else if (line.startsWith(devices + ".screen.bitDepth")){
                bitDepth = line.substring(line.indexOf(':') + 1).trim();
            }
            
        }
        Map modes = new HashMap();
        modes.put(CDCPlatform.PROP_EXEC_MAIN, null);
        CDCDevice.CDCProfile profile = new CDCDevice.CDCProfile("PP-1.0", "PP-1.0", "1.0", modes, newCp.toString(), null, true);
        CDCDevice.Screen screen = new CDCDevice.Screen(width, height, bitDepth, isColor, "false", "true"); //NOI18N
        CDCDevice device = new CDCDevice(name, name, new CDCDevice.CDCProfile[] {profile}, new CDCDevice.Screen[]{screen});

        return new CDCPlatform(getPlatformName() + " " + name,
                getPlatformName() + " " + name,
                getPlatformType(), //NOI18N
                "1.4",       //NOI18N
                Collections.singletonList(dir.getURL()), 
                Collections.EMPTY_LIST, 
                jdocs, 
                new CDCDevice[] {device}, true);
    }
    
    public int getVersion() {
        return 1;
    }

    private static String[] NOKIA_JAVADOC_FOLDERS = {
        "PersonalProfileDoc/CDC1.0",
        "PersonalProfileDoc/FOUNDATIONPROFILE",
        "PersonalProfileDoc/JDBC",
        "PersonalProfileDoc/PERSONALPROFILE"
    };
}

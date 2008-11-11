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

package org.netbeans.modules.j2me.cdc.platform.semc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import org.netbeans.modules.j2me.cdc.platform.CDCDevice;
import org.netbeans.modules.j2me.cdc.platform.CDCPlatform;
import org.netbeans.modules.j2me.cdc.platform.spi.CDCPlatformConfigurator;
import org.netbeans.modules.j2me.cdc.platform.spi.CDCPlatformDetector;
import org.netbeans.modules.j2me.cdc.platform.spi.CDCPlatformUtil;
import org.netbeans.modules.j2me.cdc.platform.spi.StreamReader;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author suchys
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.j2me.cdc.platform.spi.CDCPlatformDetector.class)
public class SEMCPlatformDetector extends CDCPlatformDetector {
    
    /** Creates a new instance of CDCPlatform */
    public SEMCPlatformDetector() {
    }

    public String getPlatformName() {
        return "Sony Ericsson CDC Platform 1"; //NOI18N
    }

    public String getPlatformType() {
        return "semc";
    }    
    public boolean accept(FileObject dir) {
        FileObject tool = CDCPlatformUtil.findTool("epoc32/release/winscw/udeb","epoc", Collections.singleton(dir));  //NOI18N
        return tool != null;
    }

    public CDCPlatform detectPlatform(FileObject dir) throws IOException {
        assert dir != null;
        FileObject java = CDCPlatformUtil.findTool("epoc32/tools","epoc", Collections.singleton(dir)); //NOI18N
        if (java == null){
            throw new IOException("epoc.bat can not be found in desired location!"); //NOI18N
        }
        File javaFile = FileUtil.toFile (java);
        if (javaFile == null)
            throw new IOException("epoc.bat can not be found in desired location!"); //NOI18N
        String javapath = javaFile.getAbsolutePath();
            
        FileObject bin = dir.getFileObject("epoc32/tools"); //NOI18N        
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
                
        File path = FileUtil.toFile(bin);
        try {
            String[] command = new String[2];
            command[0] = "perl";
            command[1] = "-version"; //NOI18N
            final Process process = Runtime.getRuntime().exec(command, 
                    env != null ? (String[]) env.toArray(new String[0]) : null, path);
            StringBuffer sou = new StringBuffer();
            StreamReader ior = new StreamReader (process.getInputStream(), sou);
            StringBuffer err = new StringBuffer();
            StreamReader irr = new StreamReader (process.getErrorStream(), err);

            // PENDING -- this may be better done by using ExecEngine, since
            // it produces a cancellable task.
            ior.join();
            irr.join();
            process.waitFor();
            process.getOutputStream().close();
            process.getInputStream().close();
            int exitValue = process.exitValue();
            if (exitValue != 0)
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(NbBundle.getMessage(SEMCPlatformDetector.class, "ERR_NoPerl"), NotifyDescriptor.ERROR_MESSAGE));                
        } catch (InterruptedException ex) {
            IOException e = new IOException(NbBundle.getMessage(SEMCPlatformDetector.class, "ERR_NoPerl"));
            ErrorManager.getDefault().annotate(e,ex);
            throw e;
           
        } catch (IOException ioEx){
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(NbBundle.getMessage(SEMCPlatformDetector.class, "ERR_NoPerl"), NotifyDescriptor.ERROR_MESSAGE));                
            throw ioEx;                
        }
        
        try {
            String[] command = new String[2];
            command[0] = javapath;
            command[1] = "-version"; //NOI18N
            final Process process = Runtime.getRuntime().exec(command, 
                    env != null ? (String[]) env.toArray(new String[0]) : null, path);
            StreamReader ior = new StreamReader (process.getInputStream(), sb);
            StringBuffer err = new StringBuffer();
            StreamReader irr = new StreamReader (process.getErrorStream(), err);

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
            //search for libraries
            FileObject libBin = dir.getFileObject("epoc32/release/winscw/udeb/Z/Resource/ive/lib"); //NOI18N            

            StringBuffer bcp  = new StringBuffer();
            for (Enumeration children = libBin.getChildren(true); children.hasMoreElements();) {
                FileObject elem = (FileObject) children.nextElement();
                String ext = elem.getExt();
                if ("jar".equalsIgnoreCase(ext) || "zip".equalsIgnoreCase(ext)){
                    bcp.append(FileUtil.toFile(elem).getAbsolutePath());
                    if (children.hasMoreElements()){
                        bcp.append(';');
                    }
                }
            }
            //add screen also later
            CDCDevice.CDCProfile profile = new CDCDevice.CDCProfile("PP-1.0", "Sony Ericsson CDC-1.0 PP-1.0", "1.0", null, bcp.toString(), null, true);
            CDCDevice device = new CDCDevice();
            device.setProfiles(new CDCDevice.CDCProfile[] {profile});
            return new CDCPlatform("SEMC " + sb.toString(), "SEMC " + sb.toString(), "semc", "1.2", 
                Collections.singletonList(dir.getURL()), 
                Collections.EMPTY_LIST, 
                Collections.EMPTY_LIST, 
                new CDCDevice[] {device}, true);            
            
        } catch (InterruptedException ex) {
            IOException e = new IOException();
            ErrorManager.getDefault().annotate(e,ex);
            throw e;
        }
    }


    public int getVersion() {
        return 1;
    }
    
    public CDCPlatformConfigurator getConfigurator(final FileObject installedFolder) {
        assert installedFolder != null;
        return new CDCPlatformConfigurator(){
            public boolean isConfigured(){
                return installedFolder != null && CDCPlatformUtil.findTool("epoc32/tools/ppro-custom-launcher", "custom-app", Collections.singleton(installedFolder)) != null; //NOI18N
            }
            
            public JPanel getConfigurationTools(){
                return new SDKConfigPanel(installedFolder);
            }
            
            public String getInfo(){
                if (installedFolder != null && CDCPlatformUtil.findTool("epoc32/tools/ppro-custom-launcher", "custom-app", Collections.singleton(installedFolder)) == null){ //NOI18N
                    return NbBundle.getMessage(SEMCPlatformDetector.class, "ERR_MissingTools"); //NOI18N
                }
                return ""; //NOI18N
            }
        };  
    }
}

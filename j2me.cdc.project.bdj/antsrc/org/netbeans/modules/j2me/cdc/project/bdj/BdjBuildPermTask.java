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

package org.netbeans.modules.j2me.cdc.project.bdj;

// IMPORTANT! You need to compile this class against ant.jar.
// The easiest way to do this is to add ${ant.core.lib} to your project's classpath.
// For example, for a plain Java project with no other dependencies, set in project.properties:
// javac.classpath=${ant.core.lib}

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * @author suchys
 */
public class BdjBuildPermTask extends Task {
    
    private File jarFile;
    private String xletClass;
    private String orgId;
    private String appId;
    private boolean fileAccess;
    private boolean appLifecycle;
    private boolean serviceSelect;
    private boolean prefRead;
    private boolean prefWrite;
    private String networkPerm;


    public @Override void execute() throws BuildException {
        //crete perm file content
        StringWriter sw = new StringWriter();
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(sw);
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.println("<n:permissionrequestfile xmlns:n=\"urn:BDA:bdmv;PRF\" orgid=\"" + orgId + "\" appid=\"" + appId + "\">");
            pw.println("    <file value=\"" + fileAccess + "\"></file>");
            pw.println("    <applifecyclecontrol value=\"" + appLifecycle + "\"></applifecyclecontrol>");
            pw.println("    <servicesel value=\"" + serviceSelect + "\"></servicesel>");
            pw.println("    <userpreferences read=\"" + prefRead + "\" write=\"" + prefWrite + "\"></userpreferences>");
            try {
                if (networkPerm != null && networkPerm.length() != 0){
                    pw.println("    <network>");
                    StringTokenizer st = new StringTokenizer(networkPerm, ";");
                    while(st.hasMoreTokens()){
                        String perm = st.nextToken();
                        int i = perm.indexOf('=');
                        if (i != -1){
                            String action = perm.substring(0, i-1);
                            String target = perm.substring(i+1);
                            pw.println("        <host action=\"" + action + "\">" + target + "</host>");
                        }
                    }
                    pw.println("    </network>");
                }
            } catch (Exception exception) {
            }
            pw.println("</n:permissionrequestfile>");
        } finally {
            pw.close();
        }
        
        //copy jar content into temp file
        File tmpJarFile = null;
        JarInputStream jis = null;
        JarOutputStream jos = null;
        byte[] data = new byte[1024];
        try {
            tmpJarFile = File.createTempFile(orgId, null);
            tmpJarFile.deleteOnExit();
            jis = new JarInputStream(new FileInputStream(jarFile));
            Manifest mf = jis.getManifest();
            jos = new JarOutputStream(new FileOutputStream(tmpJarFile), mf);
            JarEntry je;
            while((je = jis.getNextJarEntry()) != null){
                if (je.getName().endsWith(".perm")){ //filter out perm
                    continue;
                }
                jos.putNextEntry(new JarEntry(je.getName()));
                if (!je.isDirectory()){
                    int cnt;
                    while((cnt = jis.read(data)) != -1){
                        jos.write(data, 0, cnt);                        
                    }   
                }
                jis.closeEntry();
                jos.closeEntry();
            }
            //add perm file
            jos.putNextEntry(new JarEntry(getXletPermFile()));
            jos.write(sw.toString().getBytes());
            jos.closeEntry();
        } catch (IOException ex) {
            throw new BuildException(ex);
        } finally {
            try {
                jos.close();
            } catch (IOException ex) {
                throw new BuildException(ex);
            }
            try {
                jis.close();
            } catch (IOException ex) {
                throw new BuildException(ex);
            }
        }
        //copy back
        DataOutputStream dos = null;
        DataInputStream dis = null;
        try {
            dos = new DataOutputStream(new FileOutputStream(jarFile));
            dis = new DataInputStream(new FileInputStream(tmpJarFile));
            int cnt;
            while((cnt = dis.read(data)) != -1){
                dos.write(data, 0, cnt);                        
            }   
        } catch (IOException ioEx){
            throw new BuildException(ioEx);
        } finally {
            try {
                dos.close();
            } catch (IOException ex) {
                throw new BuildException(ex);
            }
            try {
                dis.close();
            } catch (IOException ex) {
                throw new BuildException(ex);
            }
        }
    }
    
    private String getXletPermFile() {
        String subpath = "";
        String className = xletClass;
        int dotIndex = xletClass.lastIndexOf('.');
        if (dotIndex != -1) {
            subpath = xletClass.substring(0, dotIndex).replace('.', File.separatorChar);
            className = xletClass.substring(dotIndex + 1);
        }
        return subpath + File.separator + "bluray." + className + ".perm";
    }

    public File getJarFile() {
        return jarFile;
    }

    public void setJarFile(File jarFile) {
        this.jarFile = jarFile;
    }
    
    public String getXletClass() {
        return xletClass;
    }

    public void setXletClass(String xletClass) {
        this.xletClass = xletClass;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public boolean isAppLifecycle() {
        return appLifecycle;
    }

    public void setAppLifecycle(boolean appLifecycle) {
        this.appLifecycle = appLifecycle;
    }

    public boolean isFileAccess() {
        return fileAccess;
    }

    public void setFileAccess(boolean fileAccess) {
        this.fileAccess = fileAccess;
    }

    public String getNetworkPerm() {
        return networkPerm;
    }

    public void setNetworkPerm(String networkPerm) {
        this.networkPerm = networkPerm;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public boolean isPrefRead() {
        return prefRead;
    }

    public void setPrefRead(boolean prefRead) {
        this.prefRead = prefRead;
    }

    public boolean isPrefWrite() {
        return prefWrite;
    }

    public void setPrefWrite(boolean prefWrite) {
        this.prefWrite = prefWrite;
    }

    public boolean isServiceSelect() {
        return serviceSelect;
    }

    public void setServiceSelect(boolean serviceSelect) {
        this.serviceSelect = serviceSelect;
    }
}

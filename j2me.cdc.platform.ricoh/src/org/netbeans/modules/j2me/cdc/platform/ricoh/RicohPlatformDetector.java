/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
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

package org.netbeans.modules.j2me.cdc.platform.ricoh;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.netbeans.modules.j2me.cdc.platform.CDCDevice;
import org.netbeans.modules.j2me.cdc.platform.CDCPlatform;
import org.netbeans.modules.j2me.cdc.platform.spi.CDCPlatformDetector;
import org.netbeans.modules.j2me.cdc.platform.spi.CDCPlatformUtil;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.JarFileSystem;

/**
 *
 * @author suchys
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.j2me.cdc.platform.spi.CDCPlatformDetector.class)
public class RicohPlatformDetector extends CDCPlatformDetector{
    
    /** Creates a new instance of CDCPlatform */
    public RicohPlatformDetector() {
    }

    public String getPlatformName() {
        return "Ricoh"; //NOI18N
    }

    public String getPlatformType() {
        return "ricoh";
    }    
    
    public boolean accept(FileObject dir) {
        FileObject tool = CDCPlatformUtil.findTool("","startemulator", Collections.singleton(dir));  //NOI18N
        return tool != null;
    }

    public CDCPlatform detectPlatform(FileObject dir) throws IOException {
        assert dir != null;
        FileObject java = CDCPlatformUtil.findTool("","startemulator", Collections.singleton(dir)); //NOI18N
        if (java == null){
            throw new IOException("startemulator can not be found in desired location!"); //NOI18N
        }
        File javaFile = FileUtil.toFile (java);
        if (javaFile == null)
            throw new IOException("startemulator can not be found in desired location!"); //NOI18N
        String javapath = javaFile.getAbsolutePath();
            
        FileObject bin = dir.getFileObject(""); //NOI18N

        List jdocs = new ArrayList();
        FileObject base = dir.getFileObject("javadoc"); //NOI18N
        if (base != null){
            findJavaDoc(base, jdocs);
        }
        
        String version = null;
        InputStream is = null;
        try {
            FileObject versionFolder = dir.getFileObject("resource/version"); //NOI18N
            FileObject versionFo = versionFolder.getFileObject("version", "txt"); //NOI18N
            if (versionFo != null){
                Properties props = new Properties();
                is = versionFo.getInputStream();
                props.load(is);
                version = props.getProperty("Version"); //NOI18N
            }
        } catch (IOException ioEx){
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioEx);
        } finally {
            if (is != null)
                is.close();
        }
        String plafName = getPlatformName() + (version != null ? " " + version : "");
        return new CDCPlatform(plafName,
                plafName,
                getPlatformType(), //NOI18N
                "1.3",       //NOI18N
                Collections.singletonList(dir.getURL()), 
                Collections.EMPTY_LIST, 
                jdocs, 
                getRicohSDKDevice(javapath, FileUtil.toFile(bin), dir), false);
    }

    private static void findJavaDoc(FileObject folder, List folders){
        if (folder == null)
            return;
        FileObject[] fo = folder.getChildren();
        for (int i = 0; i < fo.length; i++) {
            if (fo[i].isData() && "index".equals(fo[i].getName())){
                folders.add(fo[i].getParent());
            }
        }
        for (int i = 0; i < fo.length; i++) {
            if (fo[i].isFolder() && !folders.contains(fo[i].getParent())){
                findJavaDoc(fo[i], folders);
            }
        }
    }
    
    private CDCDevice[] getRicohSDKDevice(String javaPath, File path, FileObject dir) throws IOException {
        StringBuffer bcp = new StringBuffer();
        FileObject[] children = dir.getChildren();
        for (int i = 0; i < children.length; i++) {
            if ("dsdk.jar".equals(children[i].getNameExt())){ //NOI18N
                JarFileSystem jfs = new JarFileSystem();
                try {
                    jfs.setJarFile(FileUtil.toFile(children[i]));
                    FileObject fo = jfs.findResource("jp/co/ricoh/dsdk/xlet/Xlet.class"); //NOI18N
                    if (fo == null)
                        throw new IOException("Invalid bootclasspath"); //NOI18N
                    bcp.append(FileUtil.toFile(children[i]).getAbsolutePath() + ";"); //NOI18N
                } catch (PropertyVetoException ex) {
                } catch (IOException ex) {
                }
            } else if ("foundation.jar".equals(children[i].getNameExt())){
                bcp.append(FileUtil.toFile(children[i]).getAbsolutePath() + ";"); //NOI18N
            } else if ("btclasses.zip".equals(children[i].getNameExt())){
                bcp.append(FileUtil.toFile(children[i]).getAbsolutePath() + ";"); //NOI18N
            }
        }
        Map modes = new HashMap();
        modes.put(CDCPlatform.PROP_EXEC_XLET, "jp.co.ricoh.dsdk.xlet.Xlet"); //NOI18N
        CDCDevice.CDCProfile profile = new CDCDevice.CDCProfile("Foundation Profile", "Foundation Profile with Ricoh extensions", "1.0", modes, bcp.toString(), null, true);
        CDCDevice device = new CDCDevice();
        device.setProfiles(new CDCDevice.CDCProfile[] {profile});
        return new CDCDevice[] {device};
    }

    public int getVersion() {
        return 1;
    }    
}

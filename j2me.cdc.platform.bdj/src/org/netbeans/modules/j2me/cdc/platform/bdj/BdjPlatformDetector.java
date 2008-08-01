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

package org.netbeans.modules.j2me.cdc.platform.bdj;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.modules.j2me.cdc.platform.CDCDevice;
import org.netbeans.modules.j2me.cdc.platform.CDCPlatform;
import org.netbeans.modules.j2me.cdc.platform.spi.CDCPlatformDetector;
import org.netbeans.modules.j2me.cdc.platform.spi.CDCPlatformUtil;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class BdjPlatformDetector extends CDCPlatformDetector {

    private static String PLATFORM_TYPE = "bdj"; //NOI18N

    private Detector[] detectors = new Detector[]{
        new SunBdjLauncherDetector(),
        new WinDVDDetector(),
        new DigitalTheatreDetector(),
        new PowerDVDDetector()};

    private int lastDetected = -1;
    
    public String getPlatformName() {
        return "BD-J platform"; //NOI18N
    }

    public String getPlatformType() {
        return PLATFORM_TYPE;
    }

    public boolean accept(FileObject dir) {
        for (int i = 0; i < detectors.length; i++) {
            if (detectors[i].accept(dir)){
                lastDetected = i;
                return true;
            }
        }
        return false;
    }

    public CDCPlatform detectPlatform(FileObject dir) throws IOException {
        assert lastDetected != -1;
        return detectors[lastDetected].detectPlatform(dir);
    }
    
    static interface Detector {
        public boolean accept(FileObject dir);
        public CDCPlatform detectPlatform(FileObject dir) throws IOException;
    }
    
//#### Win DVD section
    
    private class WinDVDDetector implements Detector {

        public boolean accept(FileObject dir) {
            FileObject tool = CDCPlatformUtil.findTool("", "WinDVD", Collections.singleton(dir)); //NOI18N
            FileObject tool2 = CDCPlatformUtil.findTool("BDJ/jre/bin", "cvmi.dll", Collections.singleton(dir)); //NOI18N
            return null != tool && null != tool2;
        }

        public CDCPlatform detectPlatform(FileObject dir) throws IOException {
            assert dir != null;
            FileObject java = CDCPlatformUtil.findTool("","WinDVD", Collections.singleton(dir)); //NOI18N
            if (java == null){
                throw new IOException("WinDVD can not be found in desired location!"); //NOI18N
            }
            File javaFile = FileUtil.toFile (java);
            if (javaFile == null)
                throw new IOException("WinDVD can not be found in desired location!"); //NOI18N
            String javapath = javaFile.getAbsolutePath();

            FileObject stubs = dir.getFileObject("BDJ/jre/lib/bdj_stubs.zip"); //NOI18N
            //toto try to install here the stubs?
            if (stubs == null){
                FileObject stubsFolder = dir.getFileObject("BDJ/jre/lib"); //NOI18N
                stubs = installStubs(stubsFolder);
            }

            FileObject bin = dir.getFileObject(""); //NOI18N

            List jdocs = new ArrayList();
    //        FileObject base = dir.getFileObject("javadoc"); //NOI18N
    //        if (base != null){
    //            findJavaDoc(base, jdocs);
    //        }

            String version = null;
            InputStream is = null;
            try {
                FileObject versionFo = dir.getFileObject("WinDVD.exe", "manifest"); //NOI18N
                if (versionFo != null){
                    is = versionFo.getInputStream();
                    ManifestParser mp = new ManifestParser();
                    mp.parse(is);
                    version = mp.getVersion(); //NOI18N
                }
            } catch (IOException ioEx){
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioEx);
            } finally {
                if (is != null)
                    is.close();
            }
            String plafName = "Corel WinDVD BD-J player " + (version != null ? " " + version : ""); //NOI18N
            return new CDCPlatform(plafName,
                    plafName,
                    PLATFORM_TYPE,
                    "1.3",       //NOI18N 
                    Collections.singletonList(dir.getURL()), 
                    Collections.EMPTY_LIST, 
                    jdocs, 
                    getBdjSDKDevice(javapath, FileUtil.toFile(bin), dir, FileUtil.toFile(stubs).getAbsolutePath()), false);        
        }    

        private void findJavaDoc(FileObject folder, List folders){
            if (folder == null)
                return;
            FileObject[] fo = folder.getChildren();
            for (int i = 0; i < fo.length; i++) {
                if (fo[i].isData() && "index".equals(fo[i].getName())){ //NOI18N
                    folders.add(fo[i].getParent());
                }
            }
            for (int i = 0; i < fo.length; i++) {
                if (fo[i].isFolder() && !folders.contains(fo[i].getParent())){
                    findJavaDoc(fo[i], folders);
                }
            }
        }

        private CDCDevice[] getBdjSDKDevice(String javaPath, File path, FileObject dir, String stubsPath) throws IOException {
            StringBuffer bcp = new StringBuffer();
            bcp.append(stubsPath);
    //        Enumeration<FileObject> children = (Enumeration<FileObject>) dir.getChildren(true);
    //        while (children.hasMoreElements()) {
    //            FileObject fo = children.nextElement();
    //            if (fo.isData() && fo.getNameExt().endsWith("jar")){
    //                bcp.append(FileUtil.toFile(fo).getAbsolutePath() + ";"); //NOI18N
    //            }
    //        }
            Map<String, String> modes = new HashMap<String, String>();
            modes.put(CDCPlatform.PROP_EXEC_XLET, "javax.tv.xlet.Xlet"); //NOI18N
            CDCDevice.CDCProfile profile = new CDCDevice.CDCProfile("Personal Basis Profile", "Personal Basis Profile with BD-J extensions", "1.0", modes, bcp.toString(), null, true); //NOI18N
            CDCDevice device = new CDCDevice();
            device.setProfiles(new CDCDevice.CDCProfile[] {profile});
            return new CDCDevice[] {device};
        }

        private class ManifestParser extends DefaultHandler {

            private String version = "N/A"; //NOI18N

            public String getVersion() {
                return version;
            }

            public void parse( InputStream is ) {
                SAXParserFactory spf = SAXParserFactory.newInstance();
                try {
                    spf.setNamespaceAware( true );
                    spf.setValidating( false );

                    SAXParser parser = spf.newSAXParser();

                    parser.parse( is, this );
                } catch( SAXException e ) {
                    e.printStackTrace();
                } catch( ParserConfigurationException e ) {
                    e.printStackTrace();
                } catch( IOException e ) {
                    e.printStackTrace();
                }        
            }

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                if (localName.equals("assemblyIdentity")){ //NOI18N
                    if ("WinDVD.exe".equals(attributes.getValue("name"))){ //NOI18N
                        version = attributes.getValue("version"); //NOI18N
                    }
                }
            }        
        }        
    } 
//#### end Win DVD section

//#### DigitalTheatre section
    
    private class DigitalTheatreDetector implements Detector {

        public boolean accept(FileObject dir) {
            FileObject tool = CDCPlatformUtil.findTool("", "uDigital Theatre", Collections.singleton(dir)); //NOI18N
            FileObject tool2 = CDCPlatformUtil.findTool("BDJ/cvm", "cvmi.dll", Collections.singleton(dir)); //NOI18N
            return null != tool && null != tool2;
        }

        public CDCPlatform detectPlatform(FileObject dir) throws IOException {
            assert dir != null;
            FileObject java = CDCPlatformUtil.findTool("","uDigital Theatre", Collections.singleton(dir)); //NOI18N
            if (java == null){
                throw new IOException("uDigital Theatre can not be found in desired location!"); //NOI18N
            }
            File javaFile = FileUtil.toFile (java);
            if (javaFile == null)
                throw new IOException("uDigital Theatre can not be found in desired location!"); //NOI18N
            String javapath = javaFile.getAbsolutePath();

            FileObject stubs = dir.getFileObject("BDJ/cvm/lib/bdj_stubs.zip"); //NOI18N
            //toto try to install here the stubs?
            if (stubs == null){
                FileObject stubsFolder = dir.getFileObject("BDJ/cvm/lib"); //NOI18N
                stubs = installStubs(stubsFolder);
            }

            FileObject bin = dir.getFileObject(""); //NOI18N

            List jdocs = new ArrayList();

            String version = "1.0";

            String plafName = "ArcSoft Digital Theatre BD-J player " + (version != null ? " " + version : ""); //NOI18N
            return new CDCPlatform(plafName,
                    plafName,
                    PLATFORM_TYPE,
                    "1.3",       //NOI18N 
                    Collections.singletonList(dir.getURL()), 
                    Collections.EMPTY_LIST, 
                    jdocs, 
                    getBdjSDKDevice(javapath, FileUtil.toFile(bin), dir, FileUtil.toFile(stubs).getAbsolutePath()), false);        
        }    

        private CDCDevice[] getBdjSDKDevice(String javaPath, File path, FileObject dir, String stubsPath) throws IOException {
            StringBuffer bcp = new StringBuffer();
            bcp.append(stubsPath);
            Map<String, String> modes = new HashMap<String, String>();
            modes.put(CDCPlatform.PROP_EXEC_XLET, "javax.tv.xlet.Xlet"); //NOI18N
            CDCDevice.CDCProfile profile = new CDCDevice.CDCProfile("Personal Basis Profile", "Personal Basis Profile with BD-J extensions", "1.0", modes, bcp.toString(), null, true); //NOI18N
            CDCDevice device = new CDCDevice();
            device.setProfiles(new CDCDevice.CDCProfile[] {profile});
            return new CDCDevice[] {device};
        }
    } 
//#### end DigitalTheatre section
    
//#### PowerDVD section
    
    private class PowerDVDDetector implements Detector {

        public boolean accept(FileObject dir) {
            FileObject tool = CDCPlatformUtil.findTool("", "PowerDVD", Collections.singleton(dir)); //NOI18N
            FileObject tool2 = CDCPlatformUtil.findTool("NavFilter/bin", "cvmi.dll", Collections.singleton(dir)); //NOI18N
            return null != tool && null != tool2;
        }

        public CDCPlatform detectPlatform(FileObject dir) throws IOException {
            assert dir != null;
            FileObject java = CDCPlatformUtil.findTool("","PowerDVD", Collections.singleton(dir)); //NOI18N
            if (java == null){
                throw new IOException("PowerDVD can not be found in desired location!"); //NOI18N
            }
            File javaFile = FileUtil.toFile (java);
            if (javaFile == null)
                throw new IOException("PowerDVD can not be found in desired location!"); //NOI18N
            String javapath = javaFile.getAbsolutePath();

//            FileObject stubs1 = dir.getFileObject("NavFilter/btclasses.zip"); //NOI18N
//            //This platform has stubs available
//            if (stubs1 == null){
//                throw new IOException("Can not find base java platform classes");
//            }
//
//            FileObject stubs2 = dir.getFileObject("NavFilter/BDJ.jar"); //NOI18N
//            //This platform has stubs available
//            if (stubs2 == null){
//                throw new IOException("Can not find BDJ platform classes");
//            }
            FileObject stubs = dir.getFileObject("NavFilter/bdj_stubs.zip"); //NOI18N
            //toto try to install here the stubs?
            if (stubs == null){
                FileObject stubsFolder = dir.getFileObject("NavFilter"); //NOI18N
                stubs = installStubs(stubsFolder);
            }

            FileObject bin = dir.getFileObject(""); //NOI18N

            List jdocs = new ArrayList();

            String version = "1.0";

            String plafName = "CyberLink PowerDVD BD-J player " + (version != null ? " " + version : ""); //NOI18N
            return new CDCPlatform(plafName,
                    plafName,
                    PLATFORM_TYPE,
                    "1.3",       //NOI18N 
                    Collections.singletonList(dir.getURL()), 
                    Collections.EMPTY_LIST, 
                    jdocs, 
                    getBdjSDKDevice(javapath, FileUtil.toFile(bin), dir, FileUtil.toFile(stubs).getAbsolutePath()), false); //NOI18N
        }    


        private CDCDevice[] getBdjSDKDevice(String javaPath, File path, FileObject dir, String stubsPath) throws IOException {
            StringBuffer bcp = new StringBuffer();
            bcp.append(stubsPath);
            Map<String, String> modes = new HashMap<String, String>();
            modes.put(CDCPlatform.PROP_EXEC_XLET, "javax.tv.xlet.Xlet"); //NOI18N
            CDCDevice.CDCProfile profile = new CDCDevice.CDCProfile("Personal Basis Profile", "Personal Basis Profile with BD-J extensions", "1.0", modes, bcp.toString(), null, true); //NOI18N
            CDCDevice device = new CDCDevice();
            device.setProfiles(new CDCDevice.CDCProfile[] {profile});
            return new CDCDevice[] {device};
        }
    } 
//#### end PowerDVD section

//#### PowerDVD section
    
    private class SunBdjLauncherDetector implements Detector {

        public boolean accept(FileObject dir) {
            FileObject tool = CDCPlatformUtil.findTool("lib", "bd-j__bdj-emulator.jar", Collections.singleton(dir)); //NOI18N
            FileObject tool2 = CDCPlatformUtil.findTool("lib", "bd-j__blu-ray-generator.jar", Collections.singleton(dir)); //NOI18N
            return null != tool && null != tool2;
        }

        public CDCPlatform detectPlatform(FileObject dir) throws IOException {
            assert dir != null;
            FileObject java = CDCPlatformUtil.findTool("lib","bd-j__bdj-emulator.jar", Collections.singleton(dir)); //NOI18N
            if (java == null){
                throw new IOException("Sun BD-J Launcher can not be found in desired location!"); //NOI18N
            }
            File javaFile = FileUtil.toFile (java);
            if (javaFile == null)
                throw new IOException("Sun BD-J Launcher can not be found in desired location!"); //NOI18N
            String javapath = javaFile.getAbsolutePath();

//            FileObject stubs1 = dir.getFileObject("NavFilter/btclasses.zip"); //NOI18N
//            //This platform has stubs available
//            if (stubs1 == null){
//                throw new IOException("Can not find base java platform classes");
//            }
//
//            FileObject stubs2 = dir.getFileObject("NavFilter/BDJ.jar"); //NOI18N
//            //This platform has stubs available
//            if (stubs2 == null){
//                throw new IOException("Can not find BDJ platform classes");
//            }
            FileObject stubs = dir.getFileObject("lib/bdj_stubs.zip"); //NOI18N
            //toto try to install here the stubs?
            if (stubs == null){
                FileObject stubsFolder = dir.getFileObject("lib"); //NOI18N
                stubs = installStubs(stubsFolder);
            }

            FileObject bin = dir.getFileObject(""); //NOI18N

            List jdocs = new ArrayList();

            String version = "0.1";

            String plafName = "Sun BD-J Launcher" + (version != null ? " " + version : ""); //NOI18N
            return new CDCPlatform(plafName,
                    plafName,
                    PLATFORM_TYPE,
                    "1.3",       //NOI18N 
                    Collections.singletonList(dir.getURL()), 
                    Collections.EMPTY_LIST, 
                    jdocs, 
                    getBdjSDKDevice(javapath, FileUtil.toFile(bin), dir, FileUtil.toFile(stubs).getAbsolutePath()), false); //NOI18N
        }    


        private CDCDevice[] getBdjSDKDevice(String javaPath, File path, FileObject dir, String stubsPath) throws IOException {
            StringBuffer bcp = new StringBuffer();
            bcp.append(stubsPath);
            Map<String, String> modes = new HashMap<String, String>();
            modes.put(CDCPlatform.PROP_EXEC_XLET, "javax.tv.xlet.Xlet"); //NOI18N
            CDCDevice.CDCProfile profile = new CDCDevice.CDCProfile("Personal Basis Profile", "Personal Basis Profile with BD-J extensions", "1.0", modes, bcp.toString(), null, true); //NOI18N
            CDCDevice device = new CDCDevice();
            device.setProfiles(new CDCDevice.CDCProfile[] {profile});
            return new CDCDevice[] {device};
        }
    }
//#### end PowerDVD section
    
    private static FileObject installStubs(FileObject stubsFolder) throws IOException {
        File stubsFile = InstalledFileLocator.getDefault().locate("external/bdj/bdj_stubs.zip", "org.netbeans.modules.j2me.cdc.platform.bdj", false); //NOI18N
        if (stubsFile == null || !stubsFile.isFile()){
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(BdjPlatformDetector.class, "ERR_StubsMissing", NotifyDescriptor.WARNING_MESSAGE)));
            throw new IOException("Missing stubs"); //NOI18N
        }
        FileObject stubs = stubsFolder.createData("bdj_stubs", "zip"); //NOI18N
        FileLock lock = null;
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        try {
            lock = stubs.lock();
            bis = new BufferedInputStream(new FileInputStream(stubsFile));
            byte[] data = new byte[bis.available()];
            bos = new BufferedOutputStream(stubs.getOutputStream(lock));
            int i;
            while ((i = bis.read(data)) != -1) {
                bos.write(data, 0, i);
            }
            bis.close();
            bos.close();
        } finally {
            lock.releaseLock();
        }
        return stubs;
    }
}
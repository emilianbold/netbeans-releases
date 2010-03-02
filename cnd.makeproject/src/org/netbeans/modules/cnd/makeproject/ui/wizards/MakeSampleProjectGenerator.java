/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.makeproject.ui.wizards;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.platform.Platforms;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.w3c.dom.Element;

/**
 * Create a sample web project by unzipping a template into some directory
 */
public class MakeSampleProjectGenerator {
    
    private static final String PROJECT_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/make-project/1"; // NOI18N
    private static final String PROJECT_CONFIGURATION_FILE = "nbproject/configurations.xml"; // NOI18N
    
    private MakeSampleProjectGenerator() {}
    
    public static Set<DataObject> createProjectFromTemplate(final FileObject template, File projectLocation, final String name) throws IOException {
        String mainProject = (String)template.getAttribute("mainProjectLocation"); // NOI18N
        String subProjects = (String)template.getAttribute("subProjectLocations"); // NOI18N
        if (mainProject != null) {
            File mainProjectLocation = new File(projectLocation.getPath(), mainProject);
            File[] subProjectLocations = null;
            if (subProjects != null) {
                List<File> subProjectsFiles = new ArrayList<File>();
                StringTokenizer st = new StringTokenizer(subProjects, ","); // NOI18N
                while (st.hasMoreTokens()) {
                    subProjectsFiles.add(new File(projectLocation.getPath() + File.separator + st.nextToken()));
                }
                subProjectLocations = subProjectsFiles.toArray(new File[subProjectsFiles.size()]);
            }
            return createProjectFromTemplate(template.getInputStream(), projectLocation, mainProjectLocation, subProjectLocations, name);
        } else {
            return createProjectFromTemplate(template.getInputStream(), projectLocation, name);
        }
    }
    
    public static Set<DataObject> createProjectFromTemplate(final URL template, File projectLocation, final String name) throws IOException {
        return createProjectFromTemplate(template.openStream(), projectLocation, name);
    }
    
    private static void postProcessProject(FileObject prjLoc, String name) throws IOException {
        // update project.xml
        try {
            // Change project name in 'project.xml'
            prjLoc.getFileSystem().refresh(false); // IZ124952
            FileObject fo = prjLoc.getFileObject(AntProjectHelper.PROJECT_XML_PATH);
            File projXml = FileUtil.toFile(fo);
            Document doc = XMLUtil.parse(new InputSource(projXml.toURI().toString()), false, true, null, null);
            if (name != null) {
                changeXmlFileByNameNS(doc, PROJECT_CONFIGURATION_NAMESPACE, "name", name, null); // NOI18N
            }
            saveXml(doc, prjLoc, AntProjectHelper.PROJECT_XML_PATH);
            
            // Change working dir and default conf in 'projectDescriptor.xml'
            //String workingDir = projectLocation.getPath();
            String systemOs = getCurrentSystemOs();
            projXml = FileUtil.toFile(prjLoc.getFileObject(PROJECT_CONFIGURATION_FILE));
            doc = XMLUtil.parse(new InputSource(projXml.toURI().toString()), false, true, null, null);
            //changeXmlFileByTagName(doc, "buildCommandWorkingDir", workingDir, "X-PROJECTDIR-X"); // NOI18N
            //changeXmlFileByTagName(doc, "cleanCommandWorkingDir", workingDir, "X-PROJECTDIR-X"); // NOI18N
            //changeXmlFileByTagName(doc, "executablePath", workingDir, "X-PROJECTDIR-X"); // NOI18N
            //changeXmlFileByTagName(doc, "folderPath", workingDir, "X-PROJECTDIR-X"); // NOI18N
            changeXmlFileByTagName(doc, "defaultConf", systemOs, "X-DEFAULTCONF-X"); // NOI18N

            ExecutionEnvironment env = ServerList.getDefaultRecord().getExecutionEnvironment();
            CompilerSetManager compilerSetManager = CompilerSetManager.get(env);
            int platform = compilerSetManager.getPlatform();
            CompilerSet compilerSet = compilerSetManager.getDefaultCompilerSet();
            String variant = null;
            if (compilerSet != null) {
                variant = MakeConfiguration.getVariant(compilerSet, platform);
            }
            if (platform == PlatformTypes.PLATFORM_WINDOWS) { // Utilities.isWindows()) {
                changeXmlFileByTagName(doc, "output", "lib", "X-LIBPREFIX-X"); // NOI18N
                changeXmlFileByTagName(doc, "output", "dll", "X-LIBSUFFIX-X"); // NOI18N
                changeXmlFileByTagAttrName(doc, "makeArtifact", "OP", "lib", "X-LIBPREFIX-X"); // NOI18N
                changeXmlFileByTagAttrName(doc, "makeArtifact", "OP", "dll", "X-LIBSUFFIX-X"); // NOI18N
                if (variant != null) {
                    changeXmlFileByTagAttrName(doc, "makeArtifact", "OP", variant, "X-PLATFORM-X"); // NOI18N
                }
            }
            if (platform == PlatformTypes.PLATFORM_MACOSX) { //Utilities.getOperatingSystem() == Utilities.OS_MAC) {
                changeXmlFileByTagName(doc, "output", "lib", "X-LIBPREFIX-X"); // NOI18N
                changeXmlFileByTagName(doc, "output", "dylib", "X-LIBSUFFIX-X"); // NOI18N
                changeXmlFileByTagAttrName(doc, "makeArtifact", "OP", "lib", "X-LIBPREFIX-X"); // NOI18N
                changeXmlFileByTagAttrName(doc, "makeArtifact", "OP", "dylib", "X-LIBSUFFIX-X"); // NOI18N
                if (variant != null) {
                    changeXmlFileByTagAttrName(doc, "makeArtifact", "OP", variant, "X-PLATFORM-X"); // NOI18N
                }
            } else {
                changeXmlFileByTagName(doc, "output", "lib", "X-LIBPREFIX-X"); // NOI18N
                changeXmlFileByTagName(doc, "output", "so", "X-LIBSUFFIX-X"); // NOI18N
                changeXmlFileByTagAttrName(doc, "makeArtifact", "OP", "lib", "X-LIBPREFIX-X"); // NOI18N
                changeXmlFileByTagAttrName(doc, "makeArtifact", "OP", "so", "X-LIBSUFFIX-X"); // NOI18N
                if (variant != null) {
                    changeXmlFileByTagAttrName(doc, "makeArtifact", "OP", variant, "X-PLATFORM-X"); // NOI18N
                }
            }
            //saveXml(doc, prjLoc, "nbproject/projectDescriptor.xml"); // NOI18N
            saveXml(doc, prjLoc, PROJECT_CONFIGURATION_FILE);
            recordCreateSampleProject(env);
        } catch (Exception e) {
            IOException ex = new IOException();
            ex.initCause(e);
            throw ex;
        }
    }
    
    // http://wiki.netbeans.org/UsageLoggingSpecification
    private static void recordCreateSampleProject(ExecutionEnvironment env) {
        final Logger logger = Logger.getLogger(ConfigurationDescriptorProvider.USG_LOGGER_NAME);
        if (!logger.isLoggable(Level.INFO)) {
            return;
        }
        CompilerSetManager compilerSetManager = CompilerSetManager.get(env);
        CompilerSet compilerSet = compilerSetManager.getDefaultCompilerSet();
        LogRecord logRecord = new LogRecord(Level.INFO, ConfigurationDescriptorProvider.USG_PROJECT_CREATE_CND);
        logRecord.setLoggerName(logger.getName());
        String host;
        if (env.isLocal()) {
            host = "LOCAL"; // NOI18N
        } else {
            host = "REMOTE"; // NOI18N
        }
        String platform;
        if (compilerSet != null && Platforms.getPlatform(compilerSetManager.getPlatform()) != null) {
            platform = Platforms.getPlatform(compilerSetManager.getPlatform()).getName();
        } else {
            platform = "UNKNOWN_PLATFORM"; // NOI18N
        }
        String flavor;
        String[] families;
        if (compilerSet != null) {
            families = compilerSet.getCompilerFlavor().getToolchainDescriptor().getFamily();
            flavor = compilerSet.getCompilerFlavor().toString();
        } else {
            families = new String[0];
            flavor = "UKNOWN"; // NOI18N
        }
        String family;
        if (families.length == 0) {
            family = "UKNOWN"; // NOI18N
        } else {
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < families.length; i++) {
                buffer.append(families[i]);
                if (i < families.length - 1) {
                    buffer.append(","); // NOI18N
                }
            }
            family = buffer.toString();
        }
        logRecord.setParameters(new Object[]{"APPLICATION", flavor, family, host, platform, "SAMPLE_PROJECT"}); // NOI18N
        logger.log(logRecord);
    }

    public static Set<DataObject> createProjectFromTemplate(InputStream inputStream, File projectLocation, final String name) throws IOException {
        FileObject prjLoc;
        unzip(inputStream, projectLocation);
        prjLoc = FileUtil.toFileObject(projectLocation);
        
        postProcessProject(prjLoc, name);
        
        prjLoc.refresh(false);
        
        return Collections.singleton(DataObject.find(prjLoc));
    }
    
    private static void addToSet(List<DataObject> set, File projectFile) throws IOException {
        try {
            FileObject prjLoc = null;
            prjLoc = FileUtil.toFileObject(projectFile);
            postProcessProject(prjLoc, null);
            prjLoc.refresh(false);
            set.add(DataObject.find(prjLoc));
        } catch (Exception e) {
            IOException ex = new IOException();
            ex.initCause(e);
            throw ex;
        }
    }
    
    public static Set<DataObject> createProjectFromTemplate(InputStream inputStream, File projectLocation, File mainProjectLocation, File[] subProjectLocations, String name) throws IOException {
        List<DataObject> set = new ArrayList<DataObject>();
        unzip(inputStream, projectLocation);
        addToSet(set, mainProjectLocation);
        if (subProjectLocations != null) {
            for (int i = 0; i < subProjectLocations.length; i++) {
                addToSet(set, subProjectLocations[i]);
            }
        }
        return new LinkedHashSet<DataObject>(set);
    }
    
    private static void changeXmlFileByNameNS(Document doc, String tagNameNS, String tagName, String newText, String regex) throws IOException {
        NodeList nlist = doc.getElementsByTagNameNS(tagNameNS, tagName); // NOI18N
        changeXmlFileByNodeList(nlist, newText, regex);
    }
    
    private static void changeXmlFileByTagName(Document doc, String tagName, String newText, String regex) throws IOException {
        NodeList nlist = doc.getElementsByTagName(tagName); // NOI18N
        changeXmlFileByNodeList(nlist, newText, regex);
    }
    
    private static void changeXmlFileByTagAttrName(Document doc, String tagName, String attrName, String newText, String regex) throws IOException {
        NodeList nlist = doc.getElementsByTagName(tagName); // NOI18N
        changeXmlFileByAttrList(nlist, attrName, newText, regex);
    }
    
    private static void changeXmlFileByNodeList(NodeList nlist, String newText, String regex) throws IOException {
        if (nlist != null) {
            for (int i=0; i < nlist.getLength(); i++) {
                Node n = nlist.item(i);
                if (n.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element e = (Element)n;
                replaceText(e, newText, regex);
            }
        }
    }
    private static void changeXmlFileByAttrList(NodeList nlist, String attrName, String newText, String regex) throws IOException {
        if (nlist != null) {
            for (int i=0; i < nlist.getLength(); i++) {
                Node n = nlist.item(i);
                if (n.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element e = (Element)n;
                Attr attr = e.getAttributeNode(attrName);
                if (attr != null) {
                    attr.setValue(attr.getValue().replaceAll(regex, newText));
                }
            }
        }
    }
    
    private static void unzip(InputStream source, File targetFolder) throws IOException {
        //installation
        ZipInputStream zip=new ZipInputStream(source);
        try {
            ZipEntry ent;
            while ((ent = zip.getNextEntry()) != null) {
                File f = new File(targetFolder, ent.getName());
                if (ent.isDirectory()) {
                    FileUtil.createFolder(f);//f.mkdirs();
                } else {
                    FileUtil.createFolder(f.getParentFile()); //f.getParentFile().mkdirs();
                    FileOutputStream out = new FileOutputStream(f);
                    try {
                        copy(zip, out);
                    } finally {
                        out.close();
                    }
                }
            }
        } finally {
            zip.close();
        }
    }

    /**
     * Replacement for FileUtil.copy(). The problem with FU.c is that on Windows it terminates lines with
     * <CRLF> rather than <LF>. Now that we do remote development, this means that if a remote project is
     * created on Windows to be built by Sun Studio's dmake, then the <CRLF> breaks the build (this is
     * probably true with Solaris "make" as well).
     *
     * @param is The InputStream
     * @param os The Output Stream
     * @throws java.io.IOException
     */
    private static void copy(InputStream is, OutputStream os) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
        String line;

        while ((line = br.readLine()) != null) {
            bw.write(line + "\n"); // NOI18N
        }
        bw.flush();
    }
    
    private static void replaceText(Element parent, String name, String regex) {
        NodeList l = parent.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            if (l.item(i).getNodeType() == Node.TEXT_NODE) {
                Text text = (Text)l.item(i);
                if (regex != null) {
                    String s = text.getNodeValue();
                    text.setNodeValue(s.replaceAll(regex, name));
                } else {
                    text.setNodeValue(name);
                }
                return;
            }
        }
    }
    
    /**
     * Save an XML config file to a named path.
     * If the file does not yet exist, it is created.
     */
    private static void saveXml(Document doc, FileObject dir, String path) throws IOException {
        FileObject xml = FileUtil.createData(dir, path);
        FileLock lock = xml.lock();
        try {
            OutputStream os = xml.getOutputStream(lock);
            try {
                XMLUtil.write(doc, os, "UTF-8"); // NOI18N
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
    }
    
    /*
     * "0" = solaris/sparc
     * "1" = solaris/x86
     * "2" = Linux
     */
    private static String getCurrentSystemOs() {
        // FIXUP: needs improvement...
        String osname = System.getProperty("os.name"); // NOI18N
        String osarch = System.getProperty("os.arch"); // NOI18N
        
        if (osname.toLowerCase().indexOf("linux") >= 0) { // NOI18N
            return "2"; // NOI18N
        } else if (osarch.indexOf("86") >= 0) { // NOI18N
            return "1"; // NOI18N
        } else {
            return "0"; // NOI18N
        }
    }
}

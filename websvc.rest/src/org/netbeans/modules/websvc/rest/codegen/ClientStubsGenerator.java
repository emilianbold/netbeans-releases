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

package org.netbeans.modules.websvc.rest.codegen;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.websvc.rest.codegen.model.ClientStubModel;
import org.netbeans.modules.websvc.rest.codegen.model.ClientStubModel.*;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.modules.websvc.rest.support.Inflector;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.netbeans.modules.websvc.rest.wizard.Util;

/**
 * Code generator for plain REST resource class.
 * The generator takes as paramenters:
 *  - target directory
 *  - REST resource bean meta model.
 *
 * @author Ayub Khan
 */
public class ClientStubsGenerator extends AbstractGenerator {
    
    public static final String RESOURCES = "resources"; //NOI18N
    public static final String TEMPLATES = "templates"; //NOI18N
    public static final String DOJO = "dojo"; //NOI18N
    public static final String REST = "rest"; //NOI18N
    public static final String RDJ = "rdj"; //NOI18N
    public static final String DATA = "data"; //NOI18N
    public static final String WIDGET = "widget"; //NOI18N
    public static final String RJS = "rjs"; //NOI18N
    public static final String TABLE = "table"; //NOI18N
    public static final String STORE = "Store"; //NOI18N
    public static final String CSS = "css"; //NOI18N
    public static final String JS = "js"; //NOI18N
    public static final String HTML = "html"; //NOI18N
    public static final String HTM = "htm"; //NOI18N
    public static final String TXT = "txt"; //NOI18N
    public static final String JSON = "json"; //NOI18N
    public static final String GIF = "gif"; //NOI18N
    public static final String JSP = "jsp"; //NOI18N
    public static final String PHP = "php"; //NOI18N
    public static final String RHTML = "rhtml"; //NOI18N
    public static final String EJS = "ejs"; //NOI18N
    public static final String IMAGES = "images"; //NOI18N
    public static final String BUNDLE = "Bundle"; //NOI18N
    public static final String PROPERTIES = "properties"; //NOI18N
    public static final String LIBS = "libs"; //NOI18N
    public static final String DJD43 = "djd43"; //NOI18N
    public static final String JMAKI_DOJO = "jmaki-dojo"; //NOI18n
    public static final String JMAKI_COMP_LIB = "jmakicomplib"; //NOI18n
    
    public static final String JS_SUPPORT = "Support"; //NOI18N
    public static final String JS_TESTSTUBS = "TestStubs"; //NOI18N
    public static final String JS_README = "Readme"; //NOI18N
    public static final String JS_TESTSTUBS_TEMPLATE = "Templates/WebServices/JsTestStubs.html"; //NOI18N
    public static final String JS_STUBSUPPORT_TEMPLATE = "Templates/WebServices/JsStubSupport.js"; //NOI18N
    public static final String JS_PROJECTSTUB_TEMPLATE = "Templates/WebServices/JsProjectStub.js"; //NOI18N
    public static final String JS_CONTAINERSTUB_TEMPLATE = "Templates/WebServices/JsContainerStub.js"; //NOI18N
    public static final String JS_CONTAINERITEMSTUB_TEMPLATE = "Templates/WebServices/JsContainerItemStub.js"; //NOI18N
    public static final String JS_GENERICSTUB_TEMPLATE = "Templates/WebServices/JsGenericStub.js"; //NOI18N
    public static final String JS_README_TEMPLATE = "Templates/WebServices/JsReadme.html"; //NOI18N
    
    public static final String TTL_DojoResources_Stubs = "TTL_DojoResources_Stubs";
    public static final String MSG_Readme = "MSG_Readme";
    public static final String MSG_TestPage = "MSG_TestPage";
    public static final String TTL_RestClient_Stubs = "TTL_RestClient_Stubs";
    public static final String TTL_JMakiWidget_Stubs = "TTL_JMakiWidget_Stubs";
    public static final String MSG_SelectResource = "MSG_SelectResource";
    public static final String MSG_JS_Readme_Content = "MSG_JS_Readme_Content";
    public static final String MSG_JMaki_Readme_Content = "MSG_JMaki_Readme_Content";
     
    //Dojo templates
    public static final String DOJO_RESTSTORE = "RestStore";//NOI18N
    public static final String DOJO_COLLECTIONSTORE = "CollectionStore";//NOI18N
    public static final String DOJO_RESOURCESTABLE = "ResourcesTable";//NOI18N
    public static final String DOJO_TESTRESOURCESTABLE = "TestResourcesTable"; //NOI18N
    public static final String DOJO_SUPPORT = "Support"; //NOI18N
    public static final String DOJO_RESTSTORE_TEMPLATE = "Templates/WebServices/DojoRestStore.js"; //NOI18N
    public static final String DOJO_COLLECTIONSTORE_TEMPLATE = "Templates/WebServices/DojoCollectionStore.js"; //NOI18N
    public static final String DOJO_RESOURCESTABLE_TEMPLATE = "Templates/WebServices/DojoResourcesTable.js"; //NOI18N
    public static final String DOJO_SUPPORT_TEMPLATE = "Templates/WebServices/DojoSupport.js"; //NOI18N
    public static final String DOJO_TESTRESOURCESTABLE_TEMPLATE = "Templates/WebServices/DojoTestResourcesTable.html"; //NOI18N
    
    //jMaki templates
    public static final String JMAKI_README = "Readme"; //NOI18N
    public static final String JMAKI_COMPONENT = "component"; //NOI18N
    public static final String JMAKI_TESTRESOURCESTABLE = "TestResourcesTable"; //NOI18N
    public static final String JMAKI_RESOURCESTABLE_SRC = "JmakiResourcesTable"; //NOI18N
    public static final String JMAKI_RESOURCESTABLEUP_SRC = "JmakiResourcesTableUp"; //NOI18N
    public static final String JMAKI_RESOURCESTABLEDOWN_SRC = "JmakiResourcesTableDown"; //NOI18N
    public static final String JMAKI_RESOURCESTABLE_DEST = "rtable"; //NOI18N
    public static final String JMAKI_RESOURCESTABLEUP_DEST = "rtableUp"; //NOI18N
    public static final String JMAKI_RESOURCESTABLEDOWN_DEST = "rtableDown"; //NOI18N
    public static final String JMAKI_README_TEMPLATE = "Templates/WebServices/JmakiReadme.html"; //NOI18N
    public static final String JMAKI_COMPONENTCSS_TEMPLATE = "Templates/WebServices/JmakiComponent.css"; //NOI18N
    public static final String JMAKI_COMPONENTHTM_TEMPLATE = "Templates/WebServices/JmakiComponent.htm"; //NOI18N
    public static final String JMAKI_COMPONENTJS_TEMPLATE = "Templates/WebServices/JmakiComponent.js"; //NOI18N
    public static final String JMAKI_RESTBUNDLE_TEMPLATE = "Templates/WebServices/JmakiRestBundle.properties"; //NOI18N
    public static final String JMAKI_TEMPLATESBUNDLE_TEMPLATE = "Templates/WebServices/JmakiTemplatesBundle.properties"; //NOI18N
    public static final String JMAKI_TEMPLATESEJS_TEMPLATE = "Templates/WebServices/JmakiTemplates.ejs"; //NOI18N
    public static final String JMAKI_TEMPLATESJSP_TEMPLATE = "Templates/WebServices/JmakiTemplates.jsp"; //NOI18N
    public static final String JMAKI_TEMPLATESPHP_TEMPLATE = "Templates/WebServices/JmakiTemplates.php"; //NOI18N
    public static final String JMAKI_TEMPLATESRHTML_TEMPLATE = "Templates/WebServices/JmakiTemplates.rhtml"; //NOI18N
    public static final String JMAKI_TESTRESOURCESTABLE_TEMPLATE = "Templates/WebServices/JmakiTestResourcesTable.jsp"; //NOI18N
    public static final String JMAKI_WIDGETJSON_TEMPLATE = "Templates/WebServices/JmakiWidget.json"; //NOI18N

    public static final String DEFAULT_PROTOCOL = "http";
    public static final String DEFAULT_HOST = "localhost";
    public static final String DEFAULT_PORT = "8080";
    
    private FileObject rootFolder;
    private Project p;
    private boolean createJmaki;
    private boolean overwrite;
    private String projectName;
    private ClientStubModel model;
    private FileObject resourcesDir;
    private FileObject dojoDir;
    private FileObject restDir;
    private FileObject rdjDir;
    private FileObject dataDir;
    private FileObject rjsDir;
    private FileObject templatesDir;
    private String includeJs = "";
    private String libsJs = "";
    private String resourcesDojo = "";
    private String requireDojo = "";
    protected String dojoResSelList = "";
    protected String jmakiResSelList = "";
    protected String jmakiResTagList = "";
    private static final int READ_BUF_SIZE = 65536;
    private static final int WRITE_BUF_SIZE = 65536;
    private FileObject wadlFile;
    private String folderName;
    private String baseUrl;
    
    public ClientStubsGenerator(FileObject root, Project p, boolean createJmaki, boolean overwrite) throws IOException {
        assert root != null;
        assert p != null;
        this.rootFolder = root;
        this.p = p;
        this.createJmaki = createJmaki;
        this.overwrite = overwrite;
        this.projectName = ProjectUtils.getInformation(getProject()).getName();
    }
    
    public ClientStubsGenerator(FileObject root, String folderName, Project p, 
            boolean createJmaki, boolean overwrite) throws IOException {
        assert root != null;
        assert p != null;
        this.rootFolder = root;
        this.folderName = folderName;
        this.p = p;
        this.createJmaki = createJmaki;
        this.overwrite = overwrite;
        this.projectName = ProjectUtils.getInformation(getProject()).getName();
    }
    
    public ClientStubsGenerator(FileObject root, String folderName, FileObject wadlFile, 
            boolean createJmaki, boolean overwrite) throws IOException {
        assert root != null;
        assert wadlFile != null;
        this.rootFolder = root;
        this.folderName = folderName;
        this.wadlFile = wadlFile;
        this.createJmaki = createJmaki;
        this.overwrite = overwrite;
        this.projectName = "NewProject";
    }
    
    public FileObject getRootFolder() {
        return rootFolder;
    }
    
    public String getFolderName() {
        return folderName;
    }
    
    public Project getProject() {
        return p;
    }
    
    public boolean canOverwrite() {
        return overwrite;
    }
    
    public boolean createJmaki() {
        return createJmaki;
    }
    
    public String getProjectName() {
        return projectName;
    }
    
    public ClientStubModel getModel() {
        return model;
    }
    
    private String getApplicationNameFromUrl(String url) {
        String appName = url.replaceAll(DEFAULT_PROTOCOL+"://", "");
        if(appName.endsWith("/"))
            appName = appName.substring(0, appName.length()-1);
        String[] paths = appName.split("/");
        if(paths != null && paths.length > 0) {
            for(int i=0;i<paths.length;i++) {
                String path = paths[i];
                if(path != null && path.startsWith(DEFAULT_HOST) &&
                        i+1 < paths.length && paths[i+1] != null &&
                        paths[i+1].trim().length() > 0) {
                    return paths[i+1];
                }
            }
        }
        return ClientStubModel.normailizeName(appName);
    }
    
    public Set<FileObject> generate(ProgressHandle pHandle) throws IOException {
        if(pHandle != null)
            initProgressReporting(pHandle, false);
        
        this.model = new ClientStubModel();
        baseUrl = DEFAULT_PROTOCOL+"://"+DEFAULT_HOST+":"+DEFAULT_PORT+"/";
        if(p != null) {
            this.model.buildModel(p);
            baseUrl += getProjectName() + "/resources";
        } else if(wadlFile != null) {
            String url = this.model.buildModel(wadlFile);
            if(url != null) {
                baseUrl = url;
                this.projectName = getApplicationNameFromUrl(baseUrl);
            }
        }
        List<Resource> resourceList = model.getResources();
        
        includeJs = "    "+RDJ+".includeJS('../"+RJS+"/"+getProjectName().toLowerCase()+"/"+getProjectName() + "." + JS+"');\n";
        libsJs = "                   '../"+RJS+"/"+getProjectName().toLowerCase()+"/"+getProjectName() + "." + JS+"',\n";;
        resourcesDojo = "";
        requireDojo = "";
        //Prepare include list
        for (Resource r : resourceList) {
            includeJs += "    "+RDJ+".includeJS('../"+RJS+"/"+getProjectName().toLowerCase()+"/"+r.getName() + "." + JS+"');\n";
            libsJs += "                   '../"+RJS+"/"+getProjectName().toLowerCase()+"/"+r.getName() + "." + JS+"',\n";
            if(r.isContainer()) {
                resourcesDojo += "                   '../"+RDJ+"/"+DATA+"/"+r.getName()+STORE+"."+JS+"',\n";
                requireDojo += DJD43+".require(\""+RDJ+"."+DATA+"."+r.getName()+STORE+"\");\n";
            }
        }
        
        if (createJmaki()) {
            resourcesDir = createFolder(getRootFolder(), RESOURCES);
            dojoDir = createFolder(resourcesDir, DOJO);
            restDir = createFolder(dojoDir, REST);
            rjsDir = createFolder(restDir, RJS);
            rdjDir = createFolder(restDir, RDJ);
            templatesDir = createFolder(getRootFolder(), TEMPLATES);
            initJs(p);
            initDojo(p, resourceList);
            initJmaki(p, resourceList);
        } else {
            rjsDir = createFolder(getRootFolder(), getFolderName());
            initJs(p);
        }
        
        FileObject prjStubDir = createFolder(rjsDir, getProjectName().toLowerCase());
        createDataObjectFromTemplate(JS_PROJECTSTUB_TEMPLATE, prjStubDir, getProjectName(), JS, canOverwrite());
        updateProjectStub(prjStubDir.getFileObject(getProjectName(), JS), getProjectName(), "", baseUrl);
        for (Resource r : resourceList) {
            if(pHandle != null)
                reportProgress(NbBundle.getMessage(ClientStubsGenerator.class,
                    "MSG_GeneratingClass", r.getName(), JS));
            ResourceJavaScript js = null;
            RepresentationNode root = r.getRepresentation().getRoot();
            if(r.isContainer() && root != null && root.getChildren().size() > 0)
                js = new ContainerJavaScript(r, prjStubDir);
            else if(root != null){
                js = new ContainerItemJavaScript(r, prjStubDir);
            } else {
                js = new GenericResourceJavaScript(r, prjStubDir);
            }
            js.generate();
            
            //Generate the resource dojo and jmaki script
            if (createJmaki()) {
                new ResourceDojoComponents(r, rdjDir).generate();
                new ResourceJmakiComponent(r, restDir).generate();
                File dir = new File(FileUtil.toFile(templatesDir), DOJO+File.separator+REST);
                FileUtil.createFolder(dir);
                new ResourceJmakiTemplate(r, FileUtil.toFileObject(dir)).generate();
            }
        }
        updateRestStub(rjsDir.getFileObject(JS_TESTSTUBS, HTML), resourceList, "");
  
        Set<FileObject> files = new HashSet<FileObject>();
        if (createJmaki()) {  
            //copy dojo libs
            if(pHandle != null)
                reportProgress(NbBundle.getMessage(ClientStubsGenerator.class,
                    "MSG_CopyLibs", DJD43, JS));//NoI18n
            copyDojoLibs();

            // Create the ZIP file
            if(pHandle != null)
                reportProgress(NbBundle.getMessage(ClientStubsGenerator.class,
                    "MSG_GeneratingZip", getProjectName(), "zip"));            
            File projectDir = FileUtil.toFile(resourcesDir.getParent().getParent());
            File dojoLibs = FileUtil.toFile(dojoDir.getFileObject(RESOURCES));
            File zipFile = new File(projectDir, getProjectName() + ".zip");
            String[] sources = {
                FileUtil.toFile(restDir).getAbsolutePath(),
                FileUtil.toFile(templatesDir).getAbsolutePath(),
                FileUtil.toFile(resourcesDir.getParent()).getAbsolutePath() + 
                         File.separator + BUNDLE + "." + PROPERTIES,
                dojoLibs.getAbsolutePath()
            };
            String[] paths = {
                File.separator+RESOURCES+File.separator+DOJO,
                "",
                "",
                File.separator+RESOURCES+File.separator+DOJO
            };
            zip(zipFile, sources, paths);

            FileObject testFile = restDir.getFileObject(JMAKI_TESTRESOURCESTABLE, JSP);
            if (testFile != null) {
                files.add(testFile);
            }
            FileObject readme = restDir.getFileObject(JMAKI_README, TXT);
            if(readme != null)
                files.add(readme);
        } else {
            FileObject rjsTest = rjsDir.getFileObject(JS_TESTSTUBS, HTML);
            if(rjsTest != null)
                files.add(rjsTest);
            FileObject readme = rjsDir.getFileObject(JS_README, TXT);
            if(readme != null)
                files.add(readme);
        }

        

        return files;
    }
    
    private FileObject createDataObjectFromTemplate(final String template, final FileObject dir, 
            final String fileName, final String ext, final boolean overwrite) throws IOException {
        FileObject rF0 = dir.getFileObject(fileName, ext);
        if(rF0 != null) {
            if(overwrite) {
                DataObject d = DataObject.find(rF0);
                if(d != null)
                    d.delete();
            } else {
                return rF0;
            }
        }
        DataObject d0 = RestUtils.createDataObjectFromTemplate(template, dir, fileName);
        return d0.getPrimaryFile();
    }
    
    private void copyDojoLibs() throws FileNotFoundException, IOException {
        String userPath = System.getProperty("netbeans.user");
        if(userPath == null || userPath.length() == 0) {
            throw new RuntimeException("Cannot locate netbeans user folder.");
        }
        
        File userDir = new File(userPath);
        if(!userDir.exists()) {
            throw new RuntimeException("NetBeans user folder (" + userDir.getPath() + ") does not exist.");
        }
        
        File jmakiCompDir = new File(userDir, JMAKI_COMP_LIB);
        if(!jmakiCompDir.exists()) {
            throw new RuntimeException("Cannot find jMaki component folder (" + jmakiCompDir + ").");
        }
        
        File dojoLib = findDojoLibrary(jmakiCompDir);
        if(dojoLib != null) {
            unzip(new FileInputStream(dojoLib), resourcesDir.getParent(), canOverwrite());
        } else {
            File src = new File(jmakiCompDir, RESOURCES+File.separator+DOJO+File.separator+RESOURCES);
            File dst = FileUtil.toFile(dojoDir);
            FileSystem fs = FileUtil.toFileObject(dst).getFileSystem();
            copyDirectory(fs, src, dst);
        }
        if(dojoDir.getFileObject(RESOURCES) == null)
            throw new IOException("Copying dojo libs from :"+jmakiCompDir.getAbsolutePath()+" to "+resourcesDir.getParent()+" failed.");
    }
    
    private File findDojoLibrary(File jmakiCompDir) {
        File dojoLib = null;
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if(name != null && name.startsWith("jmaki-dojo") && name.endsWith(".zip"))
                    return true;
                else
                    return false;
            }
        };
        File[] dojoLibs = jmakiCompDir.listFiles(filter);
        if(dojoLibs != null && dojoLibs.length > 0)
            dojoLib = dojoLibs[0];
        return dojoLib;
    }
    
    public void copyDirectory(final FileSystem fs, final File src, final File dst)
            throws IOException {
        if (src.isDirectory()) {
            if (!dst.exists()) {
                dst.mkdir();
            }
            String files[] = src.list();
            for (int i = 0; i < files.length; i++) {
                copyDirectory(fs, new File(src, files[i]),
                        new File(dst, files[i]));
            }
        } else {
            if (!src.exists()) {
                throw new IOException("File or directory does not exist.");
            } else {
                fs.runAtomicAction(new FileSystem.AtomicAction() {
                    public void run() throws IOException {
                        InputStream in = new FileInputStream(src);
                        OutputStream out = new FileOutputStream(dst);
                        try {
                            byte[] buf = new byte[1024];
                            int len;
                            while ((len = in.read(buf)) > 0) {
                                out.write(buf, 0, len);
                            }
                        } finally {
                            in.close();
                            out.close();
                        }
                    }
                });
            }
        }
    }
    
    private void initJs(Project p) throws IOException {
        TokenReplacer tr = new TokenReplacer();
        tr.addToken(TTL_RestClient_Stubs, NbBundle.getMessage(ClientStubsGenerator.class, TTL_RestClient_Stubs));
        tr.addToken(MSG_Readme, NbBundle.getMessage(ClientStubsGenerator.class, MSG_Readme));
        tr.addToken(MSG_TestPage, NbBundle.getMessage(ClientStubsGenerator.class, MSG_TestPage));
        tr.addToken(MSG_JS_Readme_Content, NbBundle.getMessage(ClientStubsGenerator.class, MSG_JS_Readme_Content));
        
        FileObject fo = createDataObjectFromTemplate(JS_TESTSTUBS_TEMPLATE, rjsDir, JS_TESTSTUBS, HTML, false);
        tr.replaceTokens(fo);
        
        createDataObjectFromTemplate(JS_STUBSUPPORT_TEMPLATE, rjsDir, JS_SUPPORT, JS, false);
        
        fo = createDataObjectFromTemplate(JS_README_TEMPLATE, rjsDir, JS_README, HTML, false);
        tr.replaceTokens(fo);
        
        File cssDir = new File(FileUtil.toFile(rjsDir), "css");
        cssDir.mkdirs();
        copySupportFiles(cssDir);
    }

    private void initDojo(Project p, List<Resource> resourceList) throws IOException {
        TokenReplacer tr = new TokenReplacer();
        tr.addToken(TTL_DojoResources_Stubs, NbBundle.getMessage(ClientStubsGenerator.class, TTL_DojoResources_Stubs));
        tr.addToken(MSG_Readme, NbBundle.getMessage(ClientStubsGenerator.class, MSG_Readme));
        tr.addToken(MSG_TestPage, NbBundle.getMessage(ClientStubsGenerator.class, MSG_TestPage));
        tr.addToken(MSG_SelectResource, NbBundle.getMessage(ClientStubsGenerator.class, MSG_SelectResource));
        
        dataDir = createFolder(rdjDir, DATA);//NoI18n
        FileObject widgetDir = createFolder(rdjDir, WIDGET);//NoI18n
        Resource c = null;
        for (Resource r : resourceList) {
            if(r.isContainer()) {
                c = r;
                break;
            }
        }
        
        dojoResSelList = createDojoResourceSelectList(resourceList);
        jmakiResSelList = createJmakiResourceSelectList(resourceList);
        jmakiResTagList = createJmakiResourceTagList(resourceList);
        
        createDataObjectFromTemplate(DOJO_RESOURCESTABLE_TEMPLATE, widgetDir, DOJO_RESOURCESTABLE, JS, canOverwrite());
        FileObject fo = createDataObjectFromTemplate(DOJO_SUPPORT_TEMPLATE, rdjDir, DOJO_SUPPORT, JS, false);
        if(c != null)
            new ResourceDojoComponents(c, rdjDir).replaceTokens(fo);
        
        fo = createDataObjectFromTemplate(DOJO_TESTRESOURCESTABLE_TEMPLATE, rdjDir, DOJO_TESTRESOURCESTABLE, HTML, false);
        tr.replaceTokens(fo);
        if(c != null)
            new ResourceDojoComponents(c, rdjDir).replaceTokens(fo);
    }
    
    private void initJmaki(Project p, List<Resource> resourceList) throws IOException {
        TokenReplacer tr = new TokenReplacer();
        tr.addToken(TTL_JMakiWidget_Stubs, NbBundle.getMessage(ClientStubsGenerator.class, TTL_JMakiWidget_Stubs));
        tr.addToken(MSG_Readme, NbBundle.getMessage(ClientStubsGenerator.class, MSG_Readme));
        tr.addToken(MSG_TestPage, NbBundle.getMessage(ClientStubsGenerator.class, MSG_TestPage));
        tr.addToken(MSG_JMaki_Readme_Content, NbBundle.getMessage(ClientStubsGenerator.class, MSG_JMaki_Readme_Content));
        
        FileObject fo = createDataObjectFromTemplate(JMAKI_README_TEMPLATE, restDir, JMAKI_README, HTML, canOverwrite());
        tr.replaceTokens(fo);
        
        createDataObjectFromTemplate(JMAKI_RESTBUNDLE_TEMPLATE, getRootFolder(), BUNDLE, PROPERTIES, canOverwrite());
                
        //find first container 
        Resource c = null;
        for (Resource r : resourceList) {
            if(r.isContainer()) {
                c = r;
                break;
            }
        }
        fo = createDataObjectFromTemplate(JMAKI_TESTRESOURCESTABLE_TEMPLATE, restDir, JMAKI_TESTRESOURCESTABLE, JSP, false);
        tr.replaceTokens(fo);
        if(c != null)
            new ResourceDojoComponents(c, restDir).replaceTokens(fo);
    }
    
    private void copySupportFiles(File cssDir) throws IOException {
        String[] fileNames = { 
            "clientstubs.css", 
            "css_master-all.css",
            "images/background_border_bottom.gif",
            "images/pbsel.png",
            "images/bg_gradient.gif",
            "images/pname-clientstubs.png",
            "images/level1_selected-1lvl.jpg",
            "images/primary-enabled.gif",
            "images/masthead.png",
            "images/primary-roll.gif",
            "images/pbdis.png",
            "images/secondary-enabled.gif",
            "images/pbena.png",
            "images/tbsel.png",
            "images/pbmou.png",
            "images/tbuns.png"
        };
        File imagesDir = new File(cssDir, "images");
        imagesDir.mkdirs();
        for(String file: fileNames) {
            RestSupport.copyFile(cssDir, file);
        }
    }

    protected String createDojoResourceSelectList(List<Resource> resourceList) {
        String str = "";
        for (Resource r : resourceList) {
            if(r.isContainer()) {
                str += "            <option value='http://localhost:8080/"+getProjectName()+"/resources/"+r.getRepresentation().getRoot().getName()+"/;" + r.getName() + "'>" + r.getName() + "</option>\n";
            }
        }
        return str;
    }
    
    protected String createJmakiResourceSelectList(List<Resource> resourceList) {
        String str = "";
        for (Resource r : resourceList) {
            if(r.isContainer()) {
                str += "                <option value='" + r.getName() + "' <%=p.equals(\"" + r.getName() + "\")?\"selected\":\"\"%>>" + r.getName() + "</option>\n";
            }
        }
        return str;
    }
    
    protected String createJmakiResourceTagList(List<Resource> resourceList) {
        String str = "";
        int count = 0;
        for (Resource r : resourceList) {
            if(r.isContainer()) {
                String name = r.getName();
                String pathName = r.getRepresentation().getRoot().getName();
                if(count++ == 0) {
                    str += "         <% if(p.equals(\"" + name + "\")) {%>\n"+
                        "            <a:widget name=\"dojo.rest." + pathName + "table\" service=\"http://localhost:8080/" + getProjectName() + "/resources/" + pathName + "/\" />\n";
                } else {
                    str += "         <% } else if(p.equals(\"" + name + "\")) {%>\n"+
                        "            <a:widget name=\"dojo.rest." + pathName + "table\" service=\"http://localhost:8080/" + getProjectName() + "/resources/" + pathName + "/\" />\n";
                }
            }
        }
        str += "<% }%>";
        return str;
    }
        
    private void copyFile(String resourceName, File destFile) throws IOException {
        String path = "resources/"+resourceName;
        if(!destFile.exists()) {
            InputStream is = null;
            OutputStream os = null;
            try {
                is = ClientStubsGenerator.class.getResourceAsStream(path);
                os = new FileOutputStream(destFile);
                int c;
                while ((c = is.read()) != -1) {
                    os.write(c);
                }
            } finally {
                if(os != null) {
                    os.flush();
                    os.close();
                }
                if(is != null)
                    is.close();            
            }
        }
    }
            
    private FileObject createFolder(FileObject parent, String folderName) throws IOException {
        FileObject folder = parent.getFileObject(folderName);
        if(folder == null)
            folder = parent.createFolder(folderName);
        return folder;
    }
    
    private static void zip(File zipFile, String[] sources, String[] paths) {
        try {

            FileOutputStream dest = new FileOutputStream(zipFile);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            for (int i = 0; i < sources.length; i++) {
                File f = new File(sources[i]);
                addEntry(f, paths[i], out);
            }
            out.close();
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    private static void addEntry(File file, String path, ZipOutputStream out) throws FileNotFoundException, IOException {
        if (file.isDirectory()) {
            String[] files = file.list();
            for (int i = 0; i < files.length; i++) {
                File f = new File(file + File.separator + files[i]);
                addEntry(f, path + File.separator + file.getName(), out);
            }
        } else {
            byte[] data = new byte[WRITE_BUF_SIZE];
            BufferedInputStream origin = null;
            //System.out.println("Adding: " + file);
            FileInputStream fi = new FileInputStream(file);
            origin = new BufferedInputStream(fi, READ_BUF_SIZE);
            ZipEntry entry = new ZipEntry(path + File.separator + file.getName());
            out.putNextEntry(entry);
            int count;
            while ((count = origin.read(data, 0, WRITE_BUF_SIZE)) != -1) {
                out.write(data, 0, count);
            }
            origin.close();
        }
    }

    private static boolean unzip(final InputStream source,
            final FileObject targetFolderFO, boolean overwrite) throws IOException {
        boolean result = true;
        FileSystem targetFS = targetFolderFO.getFileSystem();
        File targetFolder = FileUtil.toFile(targetFolderFO);
        ZipInputStream zip = null;
        try {
            final byte [] buffer = new byte [WRITE_BUF_SIZE];
            zip = new ZipInputStream(new BufferedInputStream(source, READ_BUF_SIZE));
            final InputStream in = zip;
            ZipEntry entry;
            while((entry = zip.getNextEntry()) != null) {
                if(!(entry.getName().startsWith(RESOURCES+"/"+DOJO+"/"+RESOURCES+"/"+LIBS) ||
                        entry.getName().startsWith(RESOURCES+File.separator+DOJO+File.separator+RESOURCES+File.separator+LIBS))) {
                    continue;
                }
                final File entryFile = new File(targetFolder, entry.getName());
                if(entry.isDirectory()) {
                    if(!entryFile.exists()) {
                        try {
                            FileObject fObj = FileUtil.createFolder(entryFile);
                        } catch(IOException iox) {
                            throw new RuntimeException("Failed to create folder: " +
                                    entryFile.getName() + ".  Terminating archive installation.");
                        }
                    }
                } else {
                    if(entryFile.exists() && overwrite) {
                        if (!entryFile.delete()) {
                            throw new RuntimeException("Failed to delete file: " +
                                    entryFile.getName() + ".  Terminating archive installation.");
                        }
                    }
                    File parentFile = entryFile.getParentFile();
                    if(!parentFile.exists()) {
                        try {
                            FileObject fObj = FileUtil.createFolder(parentFile);
                        } catch(IOException iox) {
                            throw new RuntimeException("Failed to create folder: " +
                                parentFile.getName() + ".  Terminating archive installation.");
                        }
                    }
                    targetFS.runAtomicAction(new FileSystem.AtomicAction() {
                        public void run() throws IOException {
                            FileOutputStream os = null;
                            try {
                                os = new FileOutputStream(entryFile);
                                int len;
                                while((len = in.read(buffer)) >= 0) {
                                    os.write(buffer, 0, len);
                                }
                            } finally {
                                if(os != null) {
                                    try {
                                        os.close();
                                    } catch(IOException ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                }
                            }
                        }
                    });
                }
            }
        } finally {
            if(zip != null) {
                try {
                    zip.close();
                } catch(IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        
        return result;
    }

    private void updateProjectStub(FileObject projectStub, String prjName, String pkg, String baseUrl) throws IOException {
        FileLock lock = projectStub.lock();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(FileUtil.toFile(projectStub)));
            String line;
            StringBuffer sb = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                if (line.contains("__BASE_URL__")) {
                    sb.append(line.replaceAll("__BASE_URL__", baseUrl));
                } else if (line.contains("__PROJECT_NAME__")) {
                    sb.append(line.replaceAll("__PROJECT_NAME__", prjName));
                } else if (line.contains("__PROJECT_INIT_BODY__")) {
                    String initBody = "";
                    int count = 0;
                    List<Resource> resourceList = model.getResources();
                    for (Resource r : resourceList) {
                        if (r.isContainer()) {
                            initBody += "      this.resources[" + count++ + "] = new " + pkg + r.getName() + "(this.uri+'" + r.getPath() + "');\n";
                        }
                    }
                    sb.append(initBody);
                } else {
                    sb.append(line);
                }
                sb.append("\n");
            }
            OutputStreamWriter writer = new OutputStreamWriter(projectStub.getOutputStream(lock), "UTF-8");
            try {
                writer.write(sb.toString());
            } finally {
                writer.close();
            }
        } finally {
            lock.releaseLock();
        }
    }
    
    private void updateRestStub(FileObject restStub, List<Resource> resourceList, String pkg) throws IOException {
        String prjName = getProjectName();
        String prjStubDir = prjName.toLowerCase();
        StringBuffer sb1 = new StringBuffer();
        sb1.append("\t<script type='text/javascript' src='./" + prjStubDir + "/" + prjName + "." + JS + "'></script>\n");
        for (Resource r : resourceList) {
            sb1.append("\t<script type='text/javascript' src='./" + prjStubDir + "/" + r.getName() + "." + JS + "'></script>\n");
        }
        StringBuffer sb2 = new StringBuffer();
        sb2.append("\n\t<!-- Using JavaScript files for project " + prjName + "-->\n");
        sb2.append("\t<script language='Javascript'>\n");
        sb2.append("\t\tvar str = '';\n");
        sb2.append("\t\t//Example test code for " + prjName + "\n");
        sb2.append("\t\tstr = '<h2>Resources for " + prjName + ":</h2><br><table border=\"1\">';\n");
        sb2.append("\t\tvar app = new " + pkg+prjName + "('"+baseUrl+"');\n");
        sb2.append("\t\tvar resources = app.getResources();\n");
        sb2.append("\t\tfor(i=0;i<resources.length;i++) {\n");
        sb2.append("\t\t  var resource = resources[i];\n");
        sb2.append("\t\t  var uri = resource.getUri();\n");
        sb2.append("\t\t  str += '<tr><td valign=\"top\"><a href=\"'+uri+'\" target=\"_blank\">'+uri+'</a></td><td>';\n");
        sb2.append("\t\t  var items  = resource.getItems();\n");
        sb2.append("\t\t  if(items != undefined && items.length > 0) {\n");
        sb2.append("\t\t    for(j=0;j<items.length;j++) {\n");
        sb2.append("\t\t        var item = items[j];\n");
        sb2.append("\t\t        var uri2 = item.getUri();\n");
        sb2.append("\t\t        str += '<a href=\"'+uri2+'\" target=\"_blank\">'+uri2+'</a><br/>';\n");
        sb2.append("\t\t        str += '&nbsp;&nbsp;<font size=\"-3\">'+item.toString()+'</font><br/>';\n");
        sb2.append("\t\t    }\n");
        sb2.append("\t\t  } else {\n");
        sb2.append("\t\t    str += 'No items, please check the url: <a href=\"'+uri+'\" target=\"_blank\">'+uri+'</a>';\n");
        sb2.append("\t\t  }\n");
        sb2.append("\t\t  str += '</td></tr>';\n");
        sb2.append("\t\t}\n");
        sb2.append("\t\tstr += '</table><br>';\n");
        sb2.append("\t\tvar n = document.getElementById('containerContent');\n");
        sb2.append("\t\tn.innerHTML = n.innerHTML + str;\n\n");
        sb2.append("\t</script>\n");
        FileLock lock = restStub.lock();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(FileUtil.toFile(restStub)));
            String line;
            StringBuffer sb = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                if (line.contains("JS_DECLARE_END")) {
                    sb.append(sb1.toString());
                } else if (line.contains("JS_USAGE_END")) {
                    sb.append(sb2.toString());
                }
                sb.append(line);
                sb.append("\n");
            }
            OutputStreamWriter writer = new OutputStreamWriter(restStub.getOutputStream(lock), "UTF-8");
            try {
                writer.write(sb.toString());
            } finally {
                writer.close();
            }
        } finally {
            lock.releaseLock();
        }
    }
    
    public class TokenReplacer {

        private Map<String, String> tokens = new HashMap<String, String>();

        public Map<String, String> getTokens() {
            return Collections.unmodifiableMap(tokens);
        }
        
        public void addToken(String name, String value) {
            tokens.put(name, value);
        }
        
        public void setTokens(Map<String, String> tokens) {
            this.tokens = tokens;
        }

        public void replaceTokens(FileObject fo) throws IOException {
            replaceTokens(fo, getTokens());
        }
        
        public void replaceTokens(FileObject fo, Map<String, String> tokenMap) throws IOException {
            FileLock lock = fo.lock();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(FileUtil.toFile(fo)));
                String line;
                StringBuffer sb = new StringBuffer();
                while ((line = reader.readLine()) != null) {
                    line = replaceTokens(line, "", "", tokenMap);
                    sb.append(line);
                    sb.append("\n");
                }
                OutputStreamWriter writer = new OutputStreamWriter(fo.getOutputStream(lock), "UTF-8");
                try {
                    writer.write(sb.toString());
                } finally {
                    writer.close();
                }
            } finally {
                lock.releaseLock();
            }
        }

        private String replaceTokens(String line, String object, String pkg, Map<String, String> tokenMap) {
            String replacedLine = line;
            for(Map.Entry e:tokenMap.entrySet()) {
                String key = (String) e.getKey();
                String value = (String) e.getValue();
                if(key != null && value != null)
                    replacedLine = replacedLine.replaceAll(key, value);
            }
            return replacedLine;
        }
    }
    
    public abstract class ResourceJavaScript extends TokenReplacer {
        final String RJSSUPPORT = "rjsSupport";
        
        protected Resource r;
        protected FileObject jsFolder;
        protected RepresentationNode root;
        protected String pkg;
        protected String object;
        protected String stubJSToken;
        private Map<String, String> tokens;
        
        public ResourceJavaScript(Resource r, FileObject jsFolder) {
            super();
            this.r = r;
            this.jsFolder = jsFolder;
            pkg = "";
            object = "";
            root = r.getRepresentation().getRoot();
            stubJSToken = createStubJSMethods(r, object, pkg);
        }
        
        public FileObject getFolder() {
            return jsFolder;
        }
        
        public abstract FileObject generate() throws IOException;

        protected String createStubJSMethods(Resource r, String object, String pkg) {
            StringBuffer sb = new StringBuffer();
            for (Method m : r.getMethods()) {
                sb.append("   ,\n"+createMethod(m, RJSSUPPORT+".", pkg)+"\n");
            }
            return sb.toString();
        }
         
        private String createMethod(Method m, final String object, String pkg) {
            if (m.getType() == MethodType.GET) {
                return createGetMethod(m, object);
            } else if (m.getType() == MethodType.POST) {
                return createPostMethod(m, object);
            } else if (m.getType() == MethodType.PUT) {
                return createPutMethod(m, object);
            } else if (m.getType() == MethodType.DELETE) {
                return createDeleteMethod(m, object);
            } else if (m instanceof NavigationMethod) {
                return createNavigationMethod((NavigationMethod) m, pkg);
            } else {
                return "";
            }
        }
        
        private String createMethodName(Method m, String mimeType, int length) {
            if(length > 1) {
                for(Constants.MimeType mime:Constants.MimeType.values())
                    if(mime.value().equals(mimeType))
                        return m.getName() + mime.suffix();
            }
            return m.getName();
        }
        
        private String createGetMethod(Method m, String object) {
            StringBuffer sb = new StringBuffer();
            int length = m.getResponse().getRepresentation().size();
            for(Representation rep:m.getResponse().getRepresentation()) {
                String mimeType = rep.getMime();
                mimeType = mimeType.replaceAll("\"", "").trim();
                sb.append("   " + createMethodName(m, mimeType, length) + " : function() {\n" +
                        "      return "+object+"get(this.uri, '" +mimeType+ "');\n" +
                        "   },\n\n");
            }
            String s = sb.toString();
            if(s.length() > 3)
                return s.substring(0, s.length()-3);
            else
                return s;
        }
        
        private String createPostMethod(Method m, String object) {
            StringBuffer sb = new StringBuffer();
            int length = m.getRequest().getRepresentation().size();
            for(Representation rep:m.getRequest().getRepresentation()) {
                String mimeType = rep.getMime();
                mimeType = mimeType.replaceAll("\"", "").trim();
                sb.append("   " + createMethodName(m, mimeType, length) + " : function(content) {\n" +
                        "      return "+object+"post(this.uri, '" + mimeType + "', content);\n" +
                        "   },\n\n");
            }
            String s = sb.toString();
            if(s.length() > 3)
                return s.substring(0, s.length()-3);
            else
                return s;
        }
        
        private String createPutMethod(Method m, String object) {
            StringBuffer sb = new StringBuffer();
            int length = m.getRequest().getRepresentation().size();
            for(Representation rep:m.getRequest().getRepresentation()) {
                String mimeType = rep.getMime();
                mimeType = mimeType.replaceAll("\"", "").trim();
                sb.append("   " + createMethodName(m, mimeType, length) + " : function(content) {\n" +
                        "      return "+object+"put(this.uri, '" + mimeType + "', content);\n" +
                        "   },\n\n");
            }
            String s = sb.toString();
            if(s.length() > 3)
                return s.substring(0, s.length()-3);
            else
                return s;
        }
        
        private String createDeleteMethod(Method m, String object) {
            return "   " + RestUtils.escapeJSReserved(m.getName()) + " : function() {\n" +
                    "      return "+object+"delete_(this.uri);\n" +
                    "   }";
        }
        
        private String createNavigationMethod(NavigationMethod m, String pkg) {
            String s = "";
            String fs = "";
            if(m.getNavigationUri().contains(",")) {
                String[] ss = m.getNavigationUri().split(",");
                for(String s1:ss) {
                    if(s1.startsWith("{"))
                        s1 = s1.substring(1);
                    else if(s1.endsWith("}"))
                        s1 = s1.substring(0, s1.length()-1);
                    s += s1+"+','+";
                    fs += s1+",";
                }
                s = s.substring(0, s.length()-5);
                fs = fs.substring(0, fs.length()-1);
            } else {
                s = m.getNavigationUri();
                fs = s;
            }
            return "   " + m.getName() + " : function(" + fs + ") {\n" +
                    "      var link = new " + pkg+m.getLinkName() + "(this.uri+'/'+" + s + ");\n" +
                    "      return link;\n" +
                    "   }";
        }
        
    }
    
    public class ContainerJavaScript extends ResourceJavaScript {
        
        public ContainerJavaScript(Resource r, FileObject jsFolder) {
            super(r, jsFolder);
            
            if (root != null && root.getChildren().size() > 0) {
                String containerName = r.getName();
                String containerRepName = root.getName();
                //TODO
                String containerItemRepName = root.getChildren().get(0).getName();
                String containerItemName = containerItemRepName.substring(0, 1).toUpperCase() + containerItemRepName.substring(1);
                Map<String, String> containerStubTokens = new HashMap<String, String>();
                containerStubTokens.put("__CONTAINER_NAME__", containerName);
                containerStubTokens.put("__CONTAINER_PATH_NAME__", containerRepName);
                containerStubTokens.put("__CONTAINER_ITEM_NAME__", containerItemName);
                containerStubTokens.put("__CONTAINER_ITEM_PATH_NAME__", containerItemRepName);
                containerStubTokens.put("__STUB_METHODS__", createStubJSMethods(r, object, pkg));
                containerStubTokens.put("__PROJECT_NAME__", getProjectName());
                containerStubTokens.put("<!-- __DOJO_RESOURCE_SELECT_LIST__ -->", dojoResSelList + "\n<!-- __DOJO_RESOURCE_SELECT_LIST__ -->");
                containerStubTokens.put("<!-- __JMAKI_RESOURCE_SELECT_LIST__ -->", jmakiResSelList + "\n<!-- __JMAKI_RESOURCE_SELECT_LIST__ -->");
                containerStubTokens.put("<!-- __JMAKI_RESOURCE_TAG_LIST__ -->", jmakiResTagList + "\n<!-- __JMAKI_RESOURCE_TAG_LIST__ -->");
                setTokens(containerStubTokens);
            }
        }
        
        @Override
        public FileObject generate() throws IOException {
            String fileName = r.getName();
            String fileNameExt = r.getName() + "." + JS;
            FileObject fo = jsFolder.getFileObject(fileNameExt);
            if (fo != null) {
                if(canOverwrite()) {
                    fo.delete();
                } else {
                    Logger.getLogger(this.getClass().getName()).log(
                        Level.INFO, NbBundle.getMessage(ClientStubsGenerator.class,
                            "MSG_SkippingStubGeneration", jsFolder.getPath()+
                                    File.separator+fileNameExt));
                }
            }
            createDataObjectFromTemplate(JS_CONTAINERSTUB_TEMPLATE, jsFolder, fileName, JS, canOverwrite());
            fo = jsFolder.getFileObject(fileNameExt);
            replaceTokens(fo);
            return fo;
        }
    }
    
    public class ContainerItemJavaScript extends ResourceJavaScript {
        
        public ContainerItemJavaScript(Resource r, FileObject jsFolder) {
            super(r, jsFolder);
            
            if(root != null){
                String resourceName = r.getName();
                String resourceRepName = root.getName();
                Map<String, String> genericStubTokens = new HashMap<String, String>();
                genericStubTokens.put("__GENERIC_NAME__", resourceName);
                genericStubTokens.put("__GENERIC_PATH_NAME__", resourceRepName);
                genericStubTokens.put("__FIELDS_DEFINITION__", createFieldsDefinition(root, true));
                genericStubTokens.put("__GETTER_SETTER_METHODS__", createGetterSetterMethods(root, true));
                genericStubTokens.put("__FIELDS_INIT__", createFieldsInitBody(root, true, pkg));
                genericStubTokens.put("__SUB_RESOURCE_NAME__", "");
                genericStubTokens.put("__SUB_RESOURCE_PATH_NAME__", "");
                genericStubTokens.put("__FIELDS_TOSTRING__", createFieldsToStringBody(root, true));
                genericStubTokens.put("__FIELD_NAMES_TOSTRING__", createFieldNamesBody(root, true));
                genericStubTokens.put("__STUB_METHODS__", stubJSToken);
                setTokens(genericStubTokens);
            }
        }
        
        @Override
        public FileObject generate() throws IOException {
            String fileName = r.getName();
            String fileNameExt = r.getName() + "." + JS;
            FileObject fo = jsFolder.getFileObject(fileNameExt);
            if (fo != null) {
                if(canOverwrite()) {
                    fo.delete();
                } else {
                    Logger.getLogger(this.getClass().getName()).log(
                        Level.INFO, NbBundle.getMessage(ClientStubsGenerator.class,
                            "MSG_SkippingStubGeneration", jsFolder.getPath()+
                                    File.separator+fileNameExt));
                }
            }
            createDataObjectFromTemplate(JS_CONTAINERITEMSTUB_TEMPLATE, jsFolder, fileName, JS, canOverwrite());
            fo = jsFolder.getFileObject(fileNameExt);
            replaceTokens(fo);
            return fo;
        }
        
        private String createGetterSetterMethods(RepresentationNode root, boolean skipUri) {
            StringBuffer sb = new StringBuffer();
            
            //create getter and setter for attributes
            sb.append(createGetterSetterMethods(root.getAttributes(), skipUri));
            
            //create getter and setter for elements
            sb.append(createGetterSetterMethods(root.getChildren(), skipUri));
            
            return sb.toString();
        }
        
        private String createGetterSetterMethods(List<RepresentationNode> nodes, boolean skipUri) {
            StringBuffer sb = new StringBuffer();
            for(RepresentationNode child:nodes) {
                String childName = child.getName();
                if(!(skipUri && childName.equals("uri"))) {
                    sb.append(createGetterMethod(child)+",\n\n");
                    sb.append(createSetterMethod(child)+",\n\n");
                }
            }
            return sb.toString();
        }
        
        private String createFieldsDefinition(RepresentationNode root, boolean skipUri) {
            StringBuffer sb = new StringBuffer();
            for(RepresentationNode child:root.getAttributes()) {
                String childName = child.getName();
                if(!(skipUri && childName.equals("uri")))
                    sb.append("    this."+childName+" = '';\n");
            }
            for(RepresentationNode child:root.getChildren()) {
                String name = child.getName();
                if(child.isContainer()) {
                    sb.append("    this."+name+" = new Array();\n");
                } else {
                    sb.append("    this."+name+" = '';\n");
                }
            }
            return sb.toString();
        }
        
        private String createFieldsInitBody(RepresentationNode root, boolean skipUri, String pkg) {
            String repName = root.getName();
            StringBuffer sb = new StringBuffer();
            for(RepresentationNode child:root.getAttributes()) {
                String childName = child.getName();
                if(!(skipUri && childName.equals("uri")))
                    sb.append("         this."+childName+" = "+repName+"['@"+childName+"'];\n");
            }
            for(RepresentationNode child:root.getChildren()) {
                String childName = child.getName();
                if(child.isRoot() || child.isReference()) {
                    String refName = child.getId();//child.isContainer()?pluralize(childName):child.getId();
                    sb.append("         this."+childName+" = new "+pkg+
                            findResourceName(childName)+"("+repName+"['"+refName+"']['@uri']);\n");
                } else {
                    //this.vehiclePK = this.findValue(this.vehiclePK , vehicle['vehiclePK']);
                    sb.append("         this."+childName+" = this.findValue(this."+childName+", "+repName+"['"+childName+"']);\n");
                }
            }
            return sb.toString();
        }
        
        private String pluralize(String word) {
            String plural = Util.pluralize(word);
            if(plural.endsWith("ss"))
                plural = plural.substring(0, plural.length()-2)+"Collection";
            return plural;
        }
        
        private String createFieldsToStringBody(RepresentationNode root, boolean skipUri) {
            StringBuffer sb = new StringBuffer();
            for(RepresentationNode child:root.getAttributes()) {
                String childName = child.getName();
                if(!(skipUri && childName.equals("uri")))
                    sb.append("         ', \"@"+childName+"\":\"'+this."+childName+"+'\"'+\n");
            }
            for(RepresentationNode child:root.getChildren()) {
                String childName = child.getName();
                if(child.isRoot() || child.isReference()) {
                    sb.append("         ', \""+childName+"\":{\"@uri\":\"'+this."+childName+".getUri()+'\"}'+\n");
                }else
                    sb.append("         ', \""+childName+"\":\"'+this."+childName+"+'\"'+\n");
            }
            return sb.toString();
        }
        
        private String createFieldNamesBody(RepresentationNode root, boolean skipUri) {
            StringBuffer sb = new StringBuffer();
            for(RepresentationNode child:root.getAttributes()) {
                String childName = child.getName();
                if(!(skipUri && childName.equals("uri")))
                    sb.append("         fields.push('"+childName+"');\n");//      fields.push('customerId');
            }
            for(RepresentationNode child:root.getChildren()) {
                String childName = child.getName();
                if(!(child.isReference() || child.isRoot())) {
                    sb.append("         fields.push('"+childName+"');\n");//      fields.push('customerId');
                }
            }
            return sb.toString();
        }
        
        private String findResourceName(String repName) {
            return repName.substring(0,1).toUpperCase()+repName.substring(1);
        }

        private String createGetterMethod(RepresentationNode n) {
            String mName = RestUtils.createGetterMethodName(n);
            String fieldName = n.getName();
            return "   "+mName + " : function() {\n" +
                    "      if(!this.initialized)\n" +
                    "         this.init();\n" +
                    "      return this." + fieldName + ";\n" +
                    "   }";
        }
        
        private String createSetterMethod(RepresentationNode n) {
            String mName = createSetterMethodName(n);
            String fieldName = n.getName();
            return "   " + mName + " : function(" + fieldName + "_) {\n" +
                    "      this." + fieldName + " = " + fieldName + "_;\n" +
                    "   }";
        }
        
        private String createSetterMethodName(RepresentationNode n) {
            String mName = "set";
            if(n.getLink() != null) {
                mName = RestUtils.escapeJSReserved(n.getLink().getName().toString());
                mName = "set"+mName.substring(3);
            } else {
                mName = n.getName();
                mName = "set"+mName.substring(0, 1).toUpperCase()+mName.substring(1);
            }
            return mName;
        }
    }
    
    public class GenericResourceJavaScript extends ResourceJavaScript {
        
        public GenericResourceJavaScript(Resource r, FileObject jsFolder) {
            super(r, jsFolder);
            Map<String, String> stubOnlyTokens = new HashMap<String, String>();
            stubOnlyTokens.put("__RESOURCE_NAME__", r.getName());
            stubOnlyTokens.put("__STUB_METHODS__", stubJSToken);
            setTokens(stubOnlyTokens);
        }
        
        @Override
        public FileObject generate() throws IOException {
            String fileName = r.getName();
            String fileNameExt = r.getName() + "." + JS;
            FileObject fo = jsFolder.getFileObject(fileNameExt);
            if (fo != null) {
                if(canOverwrite()) {
                    fo.delete();
                } else {
                    Logger.getLogger(this.getClass().getName()).log(
                        Level.INFO, NbBundle.getMessage(ClientStubsGenerator.class,
                            "MSG_SkippingStubGeneration", jsFolder.getPath()+
                                    File.separator+fileNameExt));
                }
            }
            createDataObjectFromTemplate(JS_GENERICSTUB_TEMPLATE, jsFolder, fileName, JS, canOverwrite());
            fo = jsFolder.getFileObject(fileNameExt);
            replaceTokens(fo);
            return fo;
        } 
     }
    
    public class ResourceDojoComponents extends ResourceJavaScript {
        
        public ResourceDojoComponents(Resource r, FileObject rdjDir) {
            super(r, rdjDir);
            
            if (r.isContainer() && root != null && root.getChildren().size() > 0) {
                String containerName = r.getName();
                String containerRepName = root.getName();
                //TODO
                String containerItemRepName = root.getChildren().get(0).getName();
                String containerItemName = containerItemRepName.substring(0, 1).toUpperCase() + containerItemRepName.substring(1);
                Map<String, String> containerStubTokens = new HashMap<String, String>();
                containerStubTokens.put("//__INCLUDE_JS_SCRIPTS__", includeJs + "\n//__INCLUDE_JS_SCRIPTS__");
                containerStubTokens.put("//__LIBS_JS_SCRIPTS__", libsJs + "\n//__LIBS_JS_SCRIPTS__");
                containerStubTokens.put("//__RESOURCES_DOJO_SCRIPTS__", resourcesDojo + "\n//__RESOURCES_DOJO_SCRIPTS__");
                containerStubTokens.put("//__REQUIRE_DOJO_SCRIPTS__", requireDojo + "\n//__REQUIRE_DOJO_SCRIPTS__");
                containerStubTokens.put("__CONTAINER_NAME__", containerName);
                containerStubTokens.put("__CONTAINER_PATH_NAME__", containerRepName);
                containerStubTokens.put("__CONTAINER_ITEM_NAME__", containerItemName);
                containerStubTokens.put("__CONTAINER_ITEM_PATH_NAME__", containerItemRepName);
                containerStubTokens.put("__STUB_METHODS__", createStubJSMethods(r, object, pkg));
                containerStubTokens.put("__PROJECT_NAME__", getProjectName());
                containerStubTokens.put("<!-- __DOJO_RESOURCE_SELECT_LIST__ -->", dojoResSelList + "\n<!-- __DOJO_RESOURCE_SELECT_LIST__ -->");
                containerStubTokens.put("<!-- __JMAKI_RESOURCE_SELECT_LIST__ -->", jmakiResSelList + "\n<!-- __JMAKI_RESOURCE_SELECT_LIST__ -->");
                containerStubTokens.put("<!-- __JMAKI_RESOURCE_TAG_LIST__ -->", jmakiResTagList + "\n<!-- __JMAKI_RESOURCE_TAG_LIST__ -->");
                containerStubTokens.put("TTL_JMakiWidget_Stubs", NbBundle.getMessage(ClientStubsGenerator.class, "TTL_JMakiWidget_Stubs"));
                setTokens(containerStubTokens);
            }
        }
        
        @Override
        public FileObject generate() throws IOException {
            if (r.isContainer()) {
                String name = r.getName();
                FileObject dataDir = getFolder().getFileObject(DATA);
                FileObject fo = createDataObjectFromTemplate(DOJO_COLLECTIONSTORE_TEMPLATE, dataDir, name+STORE, JS, canOverwrite());
                replaceTokens(fo);
            } else {
                return null;
            }
            return rdjDir;
        }

        
    }
    
    public class ResourceJmakiComponent extends ResourceDojoComponents {
        
        public ResourceJmakiComponent(Resource r, FileObject restDir) {
            super(r, restDir);
        }
        
        @Override
        public FileObject generate() throws IOException {  
            if (r.isContainer()) {
                String name = r.getRepresentation().getRoot().getName();
                FileObject compDir = createFolder(getFolder(), name+TABLE);
                createDataObjectFromTemplate(JMAKI_COMPONENTCSS_TEMPLATE, compDir, JMAKI_COMPONENT, CSS, canOverwrite());
                createDataObjectFromTemplate(JMAKI_COMPONENTHTM_TEMPLATE, compDir, JMAKI_COMPONENT, HTM, canOverwrite());
                FileObject fo = createDataObjectFromTemplate(JMAKI_COMPONENTJS_TEMPLATE, compDir, JMAKI_COMPONENT, JS, canOverwrite());
                replaceTokens(fo);
                fo = createDataObjectFromTemplate(JMAKI_WIDGETJSON_TEMPLATE, compDir, WIDGET, JSON, canOverwrite());
                replaceTokens(fo);

                FileObject imagesDir = createFolder(compDir, IMAGES);//NoI18n
                File imgDir = FileUtil.toFile(imagesDir);
                copyFile(JMAKI_RESOURCESTABLE_SRC + "." + GIF, new File(imgDir, JMAKI_RESOURCESTABLE_DEST + "." + GIF));
                copyFile(JMAKI_RESOURCESTABLEUP_SRC + "." + GIF, new File(imgDir, JMAKI_RESOURCESTABLEUP_DEST + "." + GIF));
                copyFile(JMAKI_RESOURCESTABLEDOWN_SRC + "." + GIF, new File(imgDir, JMAKI_RESOURCESTABLEDOWN_DEST + "." + GIF));
            } else {
                return null;
            }
            return getFolder();
        }
    }
    
    public class ResourceJmakiTemplate extends ResourceDojoComponents {
        
        public ResourceJmakiTemplate(Resource r, FileObject tRestDir) {
            super(r, tRestDir);
        }
        
        @Override
        public FileObject generate() throws IOException {  
            if (r.isContainer()) {
                String templateName = r.getRepresentation().getRoot().getName()+TABLE;
                FileObject tDir = createFolder(getFolder(), templateName);
                FileObject fo = createDataObjectFromTemplate(JMAKI_TEMPLATESBUNDLE_TEMPLATE, tDir, BUNDLE, PROPERTIES, canOverwrite());
                replaceTokens(fo);
                fo = createDataObjectFromTemplate(JMAKI_TEMPLATESJSP_TEMPLATE, tDir, templateName, JSP, canOverwrite());
                replaceTokens(fo);
                fo = createDataObjectFromTemplate(JMAKI_TEMPLATESPHP_TEMPLATE, tDir, templateName, PHP, canOverwrite());
                replaceTokens(fo);
                fo = createDataObjectFromTemplate(JMAKI_TEMPLATESEJS_TEMPLATE, tDir, templateName, EJS, canOverwrite());
                replaceTokens(fo);
                fo = createDataObjectFromTemplate(JMAKI_TEMPLATESRHTML_TEMPLATE, tDir, templateName, RHTML, canOverwrite());
                replaceTokens(fo);
            } else {
                return null;
            }
            return getFolder();
        }
    }
}

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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;

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
    public static final String RTABLE = "rtable"; //NOI18N
    public static final String CSS = "css"; //NOI18N
    public static final String JS = "js"; //NOI18N
    public static final String HTML = "html"; //NOI18N
    public static final String HTM = "htm"; //NOI18N
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
    
    public static final String JS_SUPPORT = "Support"; //NOI18N
    public static final String JS_TESTSTUBS = "TestStubs"; //NOI18N
    public static final String JS_TESTSTUBS_TEMPLATE = "Templates/WebServices/JsTestStubs.html"; //NOI18N
    public static final String JS_STUBSUPPORT_TEMPLATE = "Templates/WebServices/JsStubSupport.js"; //NOI18N
    public static final String JS_PROJECTSTUB_TEMPLATE = "Templates/WebServices/JsProjectStub.js"; //NOI18N
    public static final String JS_CONTAINERSTUB_TEMPLATE = "Templates/WebServices/JsContainerStub.js"; //NOI18N
    public static final String JS_CONTAINERITEMSTUB_TEMPLATE = "Templates/WebServices/JsContainerItemStub.js"; //NOI18N
    public static final String JS_GENERICSTUB_TEMPLATE = "Templates/WebServices/JsGenericStub.js"; //NOI18N
     
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
    
    //JMaki templates
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

    private FileObject root;
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
    private FileObject rtableDir;
    private FileObject templatesDir;
    private final int BUFFER = 2048;
    private static final int READ_BUF_SIZE = 65536;
    private static final int WRITE_BUF_SIZE = 65536;
    
    public ClientStubsGenerator(FileObject root, Project p, boolean createJmaki, boolean overwrite) throws IOException {
        assert root != null;
        assert p != null;
        this.root = root;
        this.p = p;
        this.createJmaki = createJmaki;
        this.overwrite = overwrite;
        this.projectName = p.getProjectDirectory().getName();
    }
    
    public FileObject getRootDir() {
        return root;
    }
    
    public Project getProject() {
        return p;
    }
    
    public boolean isOverwrite() {
        return overwrite;
    }
    
    public String getProjectName() {
        return projectName;
    }
    
    public ClientStubModel getModel() {
        return model;
    }

    public Set<FileObject> generate(ProgressHandle pHandle) throws IOException {
        initProgressReporting(pHandle, false);
        
        init(p);
        
        String prjName = ProjectUtils.getInformation(getProject()).getName();
        FileObject prjStubDir = createFolder(rjsDir, prjName.toLowerCase());
        if(prjStubDir.getFileObject(prjName, JS) == null)
            RestUtils.createDataObjectFromTemplate(JS_PROJECTSTUB_TEMPLATE, prjStubDir, prjName);
        updateProjectStub(prjStubDir.getFileObject(prjName, JS), prjName, "");
        
        Set<FileObject> files = new HashSet<FileObject>();
        List<Resource> resourceList = model.getResources();
        
        for (Resource r : resourceList) {
            reportProgress(NbBundle.getMessage(ClientStubsGenerator.class,
                    "MSG_GeneratingClass", r.getName(), JS));            
            FileObject fo = new ResourceJavaScript(r, prjStubDir).generate();
            
            //Generate the resource dojo script
            new ResourceDojoStore(r, dataDir).generate();
        }
        updateRestStub(rjsDir.getFileObject(JS_TESTSTUBS, HTML), resourceList, "");
        
        //copy dojo libs
        copyDojoLibs();
        
        // Create the ZIP file
        File projectDir = FileUtil.toFile(resourcesDir.getParent().getParent());
        File zipFile = new File(projectDir, prjName + ".zip");
        zip(zipFile, new String[]{
            FileUtil.toFile(resourcesDir).getAbsolutePath(), 
            FileUtil.toFile(templatesDir).getAbsolutePath(),
            FileUtil.toFile(resourcesDir.getParent()).getAbsolutePath()+
                    File.separator+BUNDLE+"."+PROPERTIES});
            
        FileObject testFile = restDir.getFileObject(JMAKI_TESTRESOURCESTABLE, JSP);
        if(testFile != null)
            files.add(testFile);
        FileObject readme = restDir.getFileObject(JMAKI_README, HTML);
        if(readme != null)
            files.add(readme);
        return files;
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
        
        File jmakiCompDir = new File(userDir, "jmakicomplib");
        if(!jmakiCompDir.exists() && !jmakiCompDir.mkdir()) {
            throw new RuntimeException("Cannot create jMaki component folder (" + jmakiCompDir + ").");
        }
        
        File dojoLib = new File(jmakiCompDir, "jmaki-dojo-1.0.zip");
        unzip(new FileInputStream(dojoLib), resourcesDir.getParent());
    }
    
    private void init(Project p) throws IOException {
        this.model = new ClientStubModel();
        this.model.buildModel(p);
        
        resourcesDir =  getRootDir();
        dojoDir = createFolder(resourcesDir, DOJO);
        restDir = createFolder(dojoDir, REST);
        rdjDir = createFolder(restDir, RDJ);
        rjsDir = createFolder(restDir, RJS);
        rtableDir = createFolder(restDir, RTABLE);
            
        initJs(p);
        
        initDojo(p);
        
        initJmaki(p);
    }
    
    private void initJs(Project p) throws IOException {
        if(rjsDir.getFileObject(JS_TESTSTUBS, HTML) == null)
            RestUtils.createDataObjectFromTemplate(JS_TESTSTUBS_TEMPLATE, rjsDir, JS_TESTSTUBS);
        if(rjsDir.getFileObject(JS_SUPPORT, JS) == null)
            RestUtils.createDataObjectFromTemplate(JS_STUBSUPPORT_TEMPLATE, rjsDir, JS_SUPPORT);  
    }

    private void initDojo(Project p) throws IOException {
        dataDir = createFolder(rdjDir, DATA);//NoI18n
        FileObject widgetDir = createFolder(rdjDir, WIDGET);//NoI18n
        if(dataDir.getFileObject(DOJO_RESTSTORE, JS) == null)
            RestUtils.createDataObjectFromTemplate(DOJO_RESTSTORE_TEMPLATE, dataDir, DOJO_RESTSTORE);
        if(widgetDir.getFileObject(DOJO_RESOURCESTABLE, JS) == null)
            RestUtils.createDataObjectFromTemplate(DOJO_RESOURCESTABLE_TEMPLATE, widgetDir, DOJO_RESOURCESTABLE);
        if(rdjDir.getFileObject(DOJO_SUPPORT, JS) == null)
            RestUtils.createDataObjectFromTemplate(DOJO_SUPPORT_TEMPLATE, rdjDir, DOJO_SUPPORT);
        if(rdjDir.getFileObject(DOJO_TESTRESOURCESTABLE, HTML) == null)
            RestUtils.createDataObjectFromTemplate(DOJO_TESTRESOURCESTABLE_TEMPLATE, rdjDir, DOJO_TESTRESOURCESTABLE);
    }
    
    private void initJmaki(Project p) throws IOException {
        if(restDir.getFileObject(JMAKI_README, HTML) == null)
            RestUtils.createDataObjectFromTemplate(JMAKI_README_TEMPLATE, restDir, JMAKI_README);
        if(restDir.getFileObject(JMAKI_TESTRESOURCESTABLE, HTML) == null)
            RestUtils.createDataObjectFromTemplate(JMAKI_TESTRESOURCESTABLE_TEMPLATE, restDir, JMAKI_TESTRESOURCESTABLE);
        
        if(getRootDir().getParent().getFileObject(BUNDLE, PROPERTIES) == null)
            RestUtils.createDataObjectFromTemplate(JMAKI_RESTBUNDLE_TEMPLATE, getRootDir().getParent(), BUNDLE);
        if(rtableDir.getFileObject(JMAKI_COMPONENT, CSS) == null)
            RestUtils.createDataObjectFromTemplate(JMAKI_COMPONENTCSS_TEMPLATE, rtableDir, JMAKI_COMPONENT);
        if(rtableDir.getFileObject(JMAKI_COMPONENT, HTM) == null)
            RestUtils.createDataObjectFromTemplate(JMAKI_COMPONENTHTM_TEMPLATE, rtableDir, JMAKI_COMPONENT);
        if(rtableDir.getFileObject(JMAKI_COMPONENT, JS) == null)
            RestUtils.createDataObjectFromTemplate(JMAKI_COMPONENTJS_TEMPLATE, rtableDir, JMAKI_COMPONENT);
        if(rtableDir.getFileObject(WIDGET, JSON) == null)
            RestUtils.createDataObjectFromTemplate(JMAKI_WIDGETJSON_TEMPLATE, rtableDir, WIDGET);
        
        FileObject imagesDir = createFolder(rtableDir, IMAGES);//NoI18n
        File imgDir = FileUtil.toFile(imagesDir);
        copyFile(JMAKI_RESOURCESTABLE_SRC + "." + GIF, new File(imgDir, JMAKI_RESOURCESTABLE_DEST + "." + GIF));
        copyFile(JMAKI_RESOURCESTABLEUP_SRC + "." + GIF, new File(imgDir, JMAKI_RESOURCESTABLEUP_DEST + "." + GIF));
        copyFile(JMAKI_RESOURCESTABLEDOWN_SRC + "." + GIF, new File(imgDir, JMAKI_RESOURCESTABLEDOWN_DEST + "." + GIF));
        
        //Jmaki templates dir
        templatesDir = createFolder(getRootDir().getParent(), TEMPLATES);
        File t_rtableDir1 = new File(FileUtil.toFile(templatesDir), DOJO+"/"+REST+"/"+RTABLE);
        t_rtableDir1.mkdirs();
        FileObject t_rtableDir = FileUtil.toFileObject(t_rtableDir1);
        if(t_rtableDir.getFileObject(BUNDLE, PROPERTIES) == null)
            RestUtils.createDataObjectFromTemplate(JMAKI_TEMPLATESBUNDLE_TEMPLATE, t_rtableDir, BUNDLE);
        replaceBundleTokens(t_rtableDir.getFileObject(BUNDLE, PROPERTIES), getProjectName());
        if(t_rtableDir.getFileObject(RTABLE, JSP) == null)
            RestUtils.createDataObjectFromTemplate(JMAKI_TEMPLATESJSP_TEMPLATE, t_rtableDir, RTABLE);
        if(t_rtableDir.getFileObject(RTABLE, PHP) == null)
            RestUtils.createDataObjectFromTemplate(JMAKI_TEMPLATESPHP_TEMPLATE, t_rtableDir, RTABLE);
        if(t_rtableDir.getFileObject(RTABLE, EJS) == null)
            RestUtils.createDataObjectFromTemplate(JMAKI_TEMPLATESEJS_TEMPLATE, t_rtableDir, RTABLE);
        if(t_rtableDir.getFileObject(RTABLE, RHTML) == null)
            RestUtils.createDataObjectFromTemplate(JMAKI_TEMPLATESRHTML_TEMPLATE, t_rtableDir, RTABLE);
    }
    
    /*
     * Copy File only
     */    
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
    
    private void replaceBundleTokens(FileObject fo, String containerName) throws IOException {
        FileLock lock = fo.lock();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(FileUtil.toFile(fo)));
            String line;
            StringBuffer sb = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                line = line.replaceAll("__CONTAINER_NAME__", containerName);
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
            
    private FileObject createFolder(FileObject parent, String folderName) throws IOException {
        FileObject folder = parent.getFileObject(folderName);
        if(folder == null)
            folder = parent.createFolder(folderName);
        return folder;
    }

    private void zip(File zipFile, final String[] source) throws FileNotFoundException, IOException {
        FileOutputStream dest = new FileOutputStream(zipFile);
        final ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
        FileSystem targetFS = FileUtil.toFileObject(zipFile).getFileSystem();
        targetFS.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                FileOutputStream os = null;
                try {
                    for (int i = 0; i < source.length; i++) {
                        File f = new File(source[i]);
                        addEntry(f, "", out);
                    }
                } finally {
                    if (os != null) {
                        try {
                            out.close();
                        } catch (IOException ex) {
                        }
                    }
                }
            }
        });
    }
        
    private static boolean unzip(final InputStream source,
            final FileObject targetFolderFO) throws IOException {
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
                System.out.println("entry: "+entry.getName());
                if(!entry.getName().startsWith(RESOURCES+File.separator+DOJO+File.separator+RESOURCES+File.separator+LIBS)) {
                    System.out.println("skipping entry: "+entry.getName());
                    continue;
                }
                final File entryFile = new File(targetFolder, entry.getName());
                if(entryFile.exists()) {
                    // !PW FIXME entry already exists, offer overwrite option...
                    throw new RuntimeException("Target " + entryFile.getPath() +
                            " already exists.  Terminating archive installation.");
                } else if(entry.isDirectory()) {
                    if(!entryFile.mkdirs()) {
                        throw new RuntimeException("Failed to create folder: " +
                                entryFile.getName() + ".  Terminating archive installation.");
                    }
                } else {
                    File parentFile = entryFile.getParentFile();
                    if(!parentFile.exists() && !parentFile.mkdirs()) {
                        throw new RuntimeException("Failed to create folder: " +
                                parentFile.getName() + ".  Terminating archive installation.");
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
                }
            }
        }
        
        return result;
    }

    private void addEntry(File file, String path, ZipOutputStream out) throws FileNotFoundException, IOException {
        if (file.isDirectory()) {
            String[] files = file.list();
            for (int i = 0; i < files.length; i++) {
                File f = new File(file + File.separator + files[i]);
                addEntry(f, path + File.separator + file.getName(), out);
            }
        } else {
            byte[] data = new byte[BUFFER];
            BufferedInputStream origin = null;
            try {
                System.out.println("Adding: " + file);
                FileInputStream fi = new FileInputStream(file);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(path + File.separator + file.getName());
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
            } finally {
                origin.close();
            }
        }
    }

    private void updateProjectStub(FileObject projectStub, String prjName, String pkg) throws IOException {
        FileLock lock = projectStub.lock();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(FileUtil.toFile(projectStub)));
            String line;
            StringBuffer sb = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                if (line.contains("__BASE_URL__")) {
                    sb.append(line.replaceAll("__BASE_URL__", "http://localhost:8080/" + prjName + "/resources"));
                } else if (line.contains("__PROJECT_NAME__")) {
                    sb.append(line.replaceAll("__PROJECT_NAME__", prjName));
                } else if (line.contains("__PROJECT_INIT_BODY__")) {
                    String initBody = "";
                    List<Resource> resourceList = model.getResources();
                    for (int i = 0; i < resourceList.size(); i++) {
                        Resource r = resourceList.get(i);
                        if (r.isContainer()) {
                            initBody += "      this.resources[" + i + "] = new " + pkg + r.getName() + "(this.uri+'" + r.getPath() + "');\n";
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
        sb2.append("\t\tvar app = new " + pkg+prjName + "();\n");
        sb2.append("\t\tvar resources = app.getResources();\n");
        sb2.append("\t\tfor(i=0;i<resources.length;i++) {\n");
        sb2.append("\t\t  var resource = resources[i];\n");
        sb2.append("\t\t  var uri = resource.getUri();\n");
        sb2.append("\t\t  str += '<tr><td valign=\"top\"><a href=\"'+uri+'\" target=\"_blank\">'+uri+'</a></td><td>';\n");
        sb2.append("\t\t  var items  = resource.getItems();\n");
        sb2.append("\t\t  if(items != undefined) {\n");
        sb2.append("\t\t    for(j=0;j<items.length;j++) {\n");
        sb2.append("\t\t        var item = items[j];\n");
        sb2.append("\t\t        var uri2 = item.getUri();\n");
        sb2.append("\t\t        str += '<a href=\"'+uri2+'\" target=\"_blank\">'+uri2+'</a><br/>';\n");
        sb2.append("\t\t        str += '&nbsp;&nbsp;<font size=\"-3\">'+item.toString()+'</font><br/>';\n");
        sb2.append("\t\t    }\n");
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
    
    public class ResourceJavaScript {
        
        protected Resource r;
        protected FileObject jsFolder;
        
        public ResourceJavaScript(Resource r, FileObject jsFolder) {
            this.r = r;
            this.jsFolder = jsFolder;
        }
        
        public FileObject generate() throws IOException {
            String fileName = r.getName();
            String fileNameExt = r.getName() + "." + JS;
            FileObject fo = jsFolder.getFileObject(fileNameExt);
            if (fo != null) {
                if(isOverwrite())
                    fo.delete();
                else
                    throw new IOException("File: "+jsFolder.getPath()+"/"+fileNameExt+" already exists.");
            }
            
            if(r.isContainer())
                RestUtils.createDataObjectFromTemplate(JS_CONTAINERSTUB_TEMPLATE, jsFolder, fileName);
            else if(r.getRepresentation().getRoot() != null)
                RestUtils.createDataObjectFromTemplate(JS_CONTAINERITEMSTUB_TEMPLATE, jsFolder, fileName);
            else //generate only stub for no representation
                RestUtils.createDataObjectFromTemplate(JS_GENERICSTUB_TEMPLATE, jsFolder, fileName);
            fo = jsFolder.getFileObject(fileNameExt);
            replaceTokens(fo);
            return fo;
        }
        
        private void replaceTokens(FileObject fo) throws IOException {
            FileLock lock = fo.lock();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(FileUtil.toFile(fo)));
                String line;
                StringBuffer sb = new StringBuffer();
                while ((line = reader.readLine()) != null) {
                    line = replaceTokens(r, line, "", "");
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
        
        protected String replaceTokens(Resource r, String line, String object, String pkg) {
            RepresentationNode root = r.getRepresentation().getRoot();
            String replacedLine = line;
            String[] containerStubTokens = {
                "__CONTAINER_NAME__",
                "__CONTAINER_PATH_NAME__",
                "__CONTAINER_ITEM_NAME__",
                "__CONTAINER_ITEM_PATH_NAME__",
                "__STUB_METHODS__"
            };
            String[] genericStubTokens = {
                "__GENERIC_NAME__",
                "__GENERIC_PATH_NAME__",
                "__FIELDS_DEFINITION__",
                "__GETTER_SETTER_METHODS__",
                "__FIELDS_INIT__",
                "__SUB_RESOURCE_NAME__",
                "__SUB_RESOURCE_PATH_NAME__",
                "__FIELDS_TOSTRING__",
                "__STUB_METHODS__"
            };
            String[] stubOnlyTokens = {
                "__RESOURCE_NAME__",
                "__STUB_METHODS__"
            };
            if(r.isContainer() && root != null) {
                String containerName = r.getName();
                String containerRepName = root.getName();
                //TODO
                String containerItemRepName = root.getChildren().get(0).getName();
                if(containerItemRepName.indexOf("Ref") != -1)
                    containerItemRepName = containerItemRepName.substring(0, containerItemRepName.indexOf("Ref"));
                String containerItemName = containerItemRepName.substring(0, 1).toUpperCase() + containerItemRepName.substring(1);
                for(String token: containerStubTokens) {
                    if("__CONTAINER_NAME__".equals(token))
                        replacedLine = replacedLine.replaceAll("__CONTAINER_NAME__", containerName);
                    else if("__CONTAINER_PATH_NAME__".equals(token))
                        replacedLine = replacedLine.replaceAll("__CONTAINER_PATH_NAME__", containerRepName);
                    else if("__CONTAINER_ITEM_NAME__".equals(token))
                        replacedLine = replacedLine.replaceAll("__CONTAINER_ITEM_NAME__", containerItemName);
                    else if("__CONTAINER_ITEM_PATH_NAME__".equals(token))
                        replacedLine = replacedLine.replaceAll("__CONTAINER_ITEM_PATH_NAME__", containerItemRepName);
                    else if("__STUB_METHODS__".equals(token))
                        replacedLine = replacedLine.replace("__STUB_METHODS__", createStubJSMethods(r, object, pkg));
                }
            } else if(root != null){
                String resourceName = r.getName();
                String resourceRepName = root.getName();
                for(String token: genericStubTokens) {
                    if("__GENERIC_NAME__".equals(token)) {
                        replacedLine = replacedLine.replaceAll("__GENERIC_NAME__", resourceName);
                    } else if("__GENERIC_PATH_NAME__".equals(token)) {
                        replacedLine = replacedLine.replaceAll("__GENERIC_PATH_NAME__", resourceRepName);
                    } else if("__FIELDS_DEFINITION__".equals(token)) {
                        replacedLine = replacedLine.replaceAll("__FIELDS_DEFINITION__", createFieldsDefinition(root, true));
                    } else if("__GETTER_SETTER_METHODS__".equals(token)) {
                        replacedLine = replacedLine.replace("__GETTER_SETTER_METHODS__", createGetterSetterMethods(root, true));
                    } else if("__FIELDS_INIT__".equals(token)) {
                        replacedLine = replacedLine.replace("__FIELDS_INIT__", createFieldsInitBody(root, true, pkg));
                    } else if("__SUB_RESOURCE_NAME__".equals(token)) {
                        replacedLine = replacedLine.replaceAll("__SUB_RESOURCE_NAME__", "");
                    } else if("__SUB_RESOURCE_PATH_NAME__".equals(token)) {
                        replacedLine = replacedLine.replaceAll("__SUB_RESOURCE_PATH_NAME__", "");
                    } else if("__FIELDS_TOSTRING__".equals(token)) {
                        String fieldsToString = createFieldsToStringBody(root, true);
                        if(fieldsToString.endsWith(",'+\n"))
                            fieldsToString = fieldsToString.substring(0, fieldsToString.length()-4)+"'+\n";
                        replacedLine = replacedLine.replace("__FIELDS_TOSTRING__", fieldsToString);
                    } else if("__STUB_METHODS__".equals(token)) {
                        replacedLine = replacedLine.replace("__STUB_METHODS__", createStubJSMethods(r, object, pkg));
                    }
                }
            } else {
                String resourceName = r.getName();
                for(String token: stubOnlyTokens) {
                    if("__RESOURCE_NAME__".equals(token))
                        replacedLine = replacedLine.replaceAll("__RESOURCE_NAME__", resourceName);
                    else if("__STUB_METHODS__".equals(token))
                        replacedLine = replacedLine.replace("__STUB_METHODS__", createStubJSMethods(r, object, pkg));
                }
            }
            return replacedLine;
        }
        
        protected String createStubJSMethods(Resource r, String object, String pkg) {
            StringBuffer sb = new StringBuffer();
            for (Method m : r.getMethods()) {
                sb.append(createMethod(m, object, pkg)+",\n\n");
            }
            String s = sb.toString();
            if(s.length() > 3)
                return s.substring(0, s.length()-3)+"\n";
            else
                return s;
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
                if(child.isRoot()) {
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
                if(child.isReference() || child.isRoot()) {
                    String childRepName = findRepresentationName(childName);
                    sb.append("         this."+childName+" = new "+pkg+findResourceName(childName)+"("+repName+"['"+childName+"']['@uri']);\n");
                } else {
                    sb.append("         this."+childName+" = "+repName+"['"+childName+"']['$'];\n");
                }
            }
            return sb.toString();
        }
        
        private String createFieldsToStringBody(RepresentationNode root, boolean skipUri) {
            StringBuffer sb = new StringBuffer();
            for(RepresentationNode child:root.getAttributes()) {
                String childName = child.getName();
                if(!(skipUri && childName.equals("uri")))
                    sb.append("         '\"@"+childName+"\":\"'+this."+childName+"+'\",'+\n");
            }
            for(RepresentationNode child:root.getChildren()) {
                String childName = child.getName();
                if(child.isReference()) {
                    String childRepName = findRepresentationName(childName);
                    sb.append("         '\""+childName+"\":{\"@uri\":\"'+this."+childName+".getUri()+'\", \""+childRepName+"\":{\"$\":\"'+this."+childName+".get"+findResourceName(childRepName)+"()+'\"}},'+\n");
                }else if(child.isRoot()) {
                    sb.append("         this."+childName+".toString()+\n");
                }else
                    sb.append("         '\""+childName+"\":{\"$\":\"'+this."+childName+"+'\"},'+\n");
            }
            return sb.toString();
        }
        
        private String findResourceName(String repName) {
            if(repName.contains("Ref"))
                repName = repName.substring(0, repName.indexOf("Ref"));
            return repName.substring(0,1).toUpperCase()+repName.substring(1);
        }
        
        private String findRepresentationName(String repName) {
            if(repName.contains("Ref"))
                return repName.substring(0, repName.indexOf("Ref"));
            return repName;
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
        
        private String createMethod(Method m, String object, String pkg) {
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
            String mimeTypes[] = m.getResponse().getRepresentation().getMime().split(",");
            int length = mimeTypes.length;
            for(String mimeType:mimeTypes) {
                mimeType = mimeType.replaceAll("\"", "").trim();
                sb.append("   " + createMethodName(m, mimeType, length) + " : function() {\n" +
                        "      return "+object+"get_(this.uri, '" +mimeType+ "');\n" +
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
            String mimeTypes[] = m.getRequest().getRepresentation().getMime().split(",");
            int length = mimeTypes.length;
            for(String mimeType:mimeTypes) {
                mimeType = mimeType.replaceAll("\"", "").trim();
                sb.append("   " + createMethodName(m, mimeType, length) + " : function(content) {\n" +
                        "      return "+object+"post_(this.uri, '" + mimeType + "', content);\n" +
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
            String mimeTypes[] = m.getRequest().getRepresentation().getMime().split(",");
            int length = mimeTypes.length;
            for(String mimeType:mimeTypes) {
                mimeType = mimeType.replaceAll("\"", "").trim();
                sb.append("   " + createMethodName(m, mimeType, length) + " : function(content) {\n" +
                        "      return "+object+"put_(this.uri, '" + mimeType + "', content);\n" +
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
                    "      return "+object+"delete__(this.uri);\n" +
                    "   }";
        }
        
        private String createNavigationMethod(NavigationMethod m, String pkg) {
            return "   " + m.getName() + " : function(" + m.getNavigationUri() + ") {\n" +
                    "      var link = new " + pkg+m.getLinkName() + "(this.uri+'/'+" + m.getNavigationUri() + ")()\n" +
                    "      return link;\n" +
                    "   }";
        }
        
    }
    
    public class ResourceDojoStore extends ResourceJavaScript {
        
        public ResourceDojoStore(Resource r, FileObject jsFolder) {
            super(r, jsFolder);
        }
        
        public FileObject generate() throws IOException {  
            String fileName = r.getName()+"Store";
            String fileNameExt = fileName + "." + JS;
            FileObject fo = jsFolder.getFileObject(fileNameExt);
            if (fo != null) {
                if(isOverwrite())
                    fo.delete();
                else
                    throw new IOException("File: "+jsFolder.getPath()+"/"+fileNameExt+" already exists.");
            }
            
            if(r.isContainer())
                RestUtils.createDataObjectFromTemplate(DOJO_COLLECTIONSTORE_TEMPLATE, jsFolder, fileName);
            else
                return null;
            return fo;
        }        
    }
}

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
import java.util.HashSet;
import java.util.List;
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
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
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
    public static final String JS_README_TEMPLATE = "Templates/WebServices/JsReadme.txt"; //NOI18N
     
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
    public static final String JMAKI_README_TEMPLATE = "Templates/WebServices/JmakiReadme.txt"; //NOI18N
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
    
    public ClientStubsGenerator(FileObject root, Project p, boolean createJmaki, boolean overwrite) throws IOException {
        assert root != null;
        assert p != null;
        this.root = root;
        this.p = p;
        this.createJmaki = createJmaki;
        this.overwrite = overwrite;
        this.projectName = ProjectUtils.getInformation(getProject()).getName();
    }
    
    public ClientStubsGenerator(FileObject root, String folderName, Project p, 
            boolean createJmaki, boolean overwrite) throws IOException {
        assert root != null;
        assert p != null;
        this.root = root.getFileObject(folderName);
        if(this.root == null)
            this.root = root.createFolder(folderName);
        this.p = p;
        this.createJmaki = createJmaki;
        this.overwrite = overwrite;
        this.projectName = ProjectUtils.getInformation(getProject()).getName();
    }
    
    public ClientStubsGenerator(FileObject root, String folderName, FileObject wadlFile, 
            boolean createJmaki, boolean overwrite) throws IOException {
        assert root != null;
        assert wadlFile != null;
        this.root = root.getFileObject(folderName);
        if(this.root == null)
            this.root = root.createFolder(folderName);
        this.wadlFile = wadlFile;
        this.createJmaki = createJmaki;
        this.overwrite = overwrite;
        this.projectName = "NewProject";
    }
    
    public FileObject getRootDir() {
        return root;
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

    public Set<FileObject> generate(ProgressHandle pHandle) throws IOException {
        if(pHandle != null)
            initProgressReporting(pHandle, false);
        
        this.model = new ClientStubModel();
        if(p != null)
            this.model.buildModel(p);
        else if(wadlFile != null) {
            String appName = this.model.buildModel(wadlFile);
            if(appName != null)
                this.projectName = appName;
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
            resourcesDir = getRootDir();
            dojoDir = createFolder(resourcesDir, DOJO);
            restDir = createFolder(dojoDir, REST);
            rjsDir = createFolder(restDir, RJS);
            rdjDir = createFolder(restDir, RDJ);
            templatesDir = createFolder(getRootDir().getParent(), TEMPLATES);
            initJs(p);
            initDojo(p, resourceList);
            initJmaki(p, resourceList);
        } else {
            rjsDir = createFolder(getRootDir().getParent(), REST);
            initJs(p);
        }
        
        FileObject prjStubDir = createFolder(rjsDir, getProjectName().toLowerCase());
        createDataObjectFromTemplate(JS_PROJECTSTUB_TEMPLATE, prjStubDir, getProjectName(), JS, canOverwrite());
        updateProjectStub(prjStubDir.getFileObject(getProjectName(), JS), getProjectName(), "");
            
        for (Resource r : resourceList) {
            if(pHandle != null)
                reportProgress(NbBundle.getMessage(ClientStubsGenerator.class,
                    "MSG_GeneratingClass", r.getName(), JS));            
            FileObject fo = new ResourceJavaScript(r, prjStubDir).generate();
            
            //Generate the resource dojo and jmaki script
            if (createJmaki()) {
                new ResourceDojoComponents(r, rdjDir).generate();
                new ResourceJmakiComponent(r, restDir).generate();
                File dir = new File(FileUtil.toFile(templatesDir), DOJO+File.separator+REST);
                dir.mkdirs();
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
    
    private FileObject rF0 = null;
    private FileObject createDataObjectFromTemplate(final String template, final FileObject dir, 
            final String fileName, final String ext, final boolean overwrite) throws IOException {
        FileSystem targetFS = dir.getFileSystem();
        targetFS.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                try {
                    rF0 = dir.getFileObject(fileName, ext);
                    if (rF0 != null) {
                        if (overwrite) {
                            rF0.delete();
                        } else {
                            return;
                        }
                    }
                    DataObject d0 = RestUtils.createDataObjectFromTemplate(template, dir, fileName);
                    rF0 = d0.getPrimaryFile();
                } finally {
                }
            }
        });
        return rF0;
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
        createDataObjectFromTemplate(JS_TESTSTUBS_TEMPLATE, rjsDir, JS_TESTSTUBS, HTML, false);
        createDataObjectFromTemplate(JS_STUBSUPPORT_TEMPLATE, rjsDir, JS_SUPPORT, JS, canOverwrite());  
        createDataObjectFromTemplate(JS_README_TEMPLATE, rjsDir, JS_README, TXT, canOverwrite());
    }

    private void initDojo(Project p, List<Resource> resourceList) throws IOException {
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
        if(c != null)
            new ResourceDojoComponents(c, rdjDir).replaceTokens(fo);
    }
    
    private void initJmaki(Project p, List<Resource> resourceList) throws IOException {
        createDataObjectFromTemplate(JMAKI_README_TEMPLATE, restDir, JMAKI_README, TXT, canOverwrite());
        createDataObjectFromTemplate(JMAKI_RESTBUNDLE_TEMPLATE, getRootDir().getParent(), BUNDLE, PROPERTIES, canOverwrite());
                
        //find first container 
        Resource c = null;
        for (Resource r : resourceList) {
            if(r.isContainer()) {
                c = r;
                break;
            }
        }
        FileObject fo = createDataObjectFromTemplate(JMAKI_TESTRESOURCESTABLE_TEMPLATE, restDir, JMAKI_TESTRESOURCESTABLE, JSP, false);
        if(c != null)
            new ResourceDojoComponents(c, restDir).replaceTokens(fo);
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
            
            for(int i=0;i<sources.length;i++) {
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
                    if(!entryFile.exists() && !entryFile.mkdirs()) {
                        throw new RuntimeException("Failed to create folder: " +
                                entryFile.getName() + ".  Terminating archive installation.");
                    }
                } else {
                    if(entryFile.exists() && overwrite) {
                        if (!entryFile.delete()) {
                            throw new RuntimeException("Failed to delete file: " +
                                    entryFile.getName() + ".  Terminating archive installation.");
                        }
                    }
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
        
        public FileObject getFolder() {
            return jsFolder;
        }
        
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
            
            if(r.isContainer())
                createDataObjectFromTemplate(JS_CONTAINERSTUB_TEMPLATE, jsFolder, fileName, JS, canOverwrite());
            else if(r.getRepresentation().getRoot() != null)
                createDataObjectFromTemplate(JS_CONTAINERITEMSTUB_TEMPLATE, jsFolder, fileName, JS, canOverwrite());
            else //generate only stub for no representation
                createDataObjectFromTemplate(JS_GENERICSTUB_TEMPLATE, jsFolder, fileName, JS, canOverwrite());
            fo = jsFolder.getFileObject(fileNameExt);
            replaceTokens(fo);
            return fo;
        }
        
        protected void replaceTokens(FileObject fo) throws IOException {
            FileLock lock = fo.lock();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(FileUtil.toFile(fo)));
                String line;
                StringBuffer sb = new StringBuffer();
                while ((line = reader.readLine()) != null) {
                    line = replaceTokens(line, "", "");
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
        
        protected String replaceTokens(String line, String object, String pkg) {
            RepresentationNode root = r.getRepresentation().getRoot();
            String replacedLine = line;
            String[] containerStubTokens = {
                "__CONTAINER_NAME__",
                "__CONTAINER_PATH_NAME__",
                "__CONTAINER_ITEM_NAME__",
                "__CONTAINER_ITEM_PATH_NAME__",
                "__STUB_METHODS__",
                "__PROJECT_NAME__",
                "<!-- __DOJO_RESOURCE_SELECT_LIST__ -->",
                "<!-- __JMAKI_RESOURCE_SELECT_LIST__ -->",
                "<!-- __JMAKI_RESOURCE_TAG_LIST__ -->"
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
                "__FIELD_NAMES_TOSTRING__",
                "__STUB_METHODS__"
            };
            String[] stubOnlyTokens = {
                "__RESOURCE_NAME__",
                "__STUB_METHODS__"
            };
            if(r.isContainer() && root != null && root.getChildren().size() > 0) {
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
                    else if("__PROJECT_NAME__".equals(token))
                        replacedLine = replacedLine.replaceAll("__PROJECT_NAME__", getProjectName());
                    else if("<!-- __DOJO_RESOURCE_SELECT_LIST__ -->".equals(token))
                        replacedLine = replacedLine.replaceAll("<!-- __DOJO_RESOURCE_SELECT_LIST__ -->", dojoResSelList+"\n<!-- __DOJO_RESOURCE_SELECT_LIST__ -->");
                    else if("<!-- __JMAKI_RESOURCE_SELECT_LIST__ -->".equals(token))
                        replacedLine = replacedLine.replaceAll("<!-- __JMAKI_RESOURCE_SELECT_LIST__ -->", jmakiResSelList+"\n<!-- __JMAKI_RESOURCE_SELECT_LIST__ -->");
                     else if("<!-- __JMAKI_RESOURCE_TAG_LIST__ -->".equals(token))
                        replacedLine = replacedLine.replaceAll("<!-- __JMAKI_RESOURCE_TAG_LIST__ -->", jmakiResTagList+"\n<!-- __JMAKI_RESOURCE_TAG_LIST__ -->");
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
                    } else if("__FIELD_NAMES_TOSTRING__".equals(token)) {
                        String fieldsToString = createFieldNamesBody(root, true);
                        replacedLine = replacedLine.replace("__FIELD_NAMES_TOSTRING__", fieldsToString);
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
        
        public static final String RJSSUPPORT = "rjsSupport";
        protected String createStubJSMethods(Resource r, String object, String pkg) {
            StringBuffer sb = new StringBuffer();
            Method getMethod = null;
            for (Method m : r.getMethods()) {
                if (m.getType() == MethodType.GET) {
                    getMethod = m;
                }
            }
            if(getMethod != null){
                String defaultGetMethod = createDefaultGetMethod(getMethod, RJSSUPPORT+".");
                if(defaultGetMethod != null)
                    sb.append(defaultGetMethod+",\n\n");
            }
            for (Method m : r.getMethods()) {
                sb.append(createMethod(m, RJSSUPPORT+".", pkg)+",\n\n");
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
                    //this.vehiclePK = this.findValue(this.vehiclePK , vehicle['vehiclePK']);
                    sb.append("         this."+childName+" = this.findValue(this."+childName+", "+repName+"['"+childName+"']);\n");
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
                    if(child.isEntity()) //child is a Entity and has a non-generic converter
                        sb.append("         '\""+childName+"\":{\"@uri\":\"'+" +
                            "this."+childName+".getUri()+'\", \""+childRepName+"\":" +
                            ":\"'+eval(\"this."+childName+".get\"+this."+childName+".getFields()[0].substring(0,1).toUpperCase()+this."+childName+".getFields()[0].substring(1)+\"()\")+'\"},'+\n");
                    else
                        sb.append("         '\""+childName+"\":{\"@uri\":\"'+this."+childName+".getUri()+'\"},'+\n");
                }else if(child.isRoot()) {
                    sb.append("         this."+childName+".toString()+','+\n");
                }else
                    sb.append("         '\""+childName+"\":\"'+this."+childName+"+'\",'+\n");
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
        
        private String createDefaultGetMethod(Method m, String object) {
            StringBuffer sb = new StringBuffer();
            String jsonMethod = null;
            for(Representation rep:m.getResponse().getRepresentation()) {
                String mimeType = rep.getMime();
                mimeType = mimeType.replaceAll("\"", "").trim();
                if(mimeType.equals(Constants.MimeType.JSON.value()))
                    jsonMethod = mimeType;
            }
            //Add a default getJson() method used by Container/Containee init() methods
            if(jsonMethod != null) {
                sb.append("/* Default getJson() method used by Container/Containee init() methods. Do not remove. */\n");
                sb.append("   getJson : function() {\n" +
                    "      return "+object+"get(this.uri, '" +jsonMethod+ "');\n" +
                    "   }");
            }
            if(sb.length() > 0)
                return sb.toString();
            else
                return null;
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
                    "      var link = new " + pkg+m.getLinkName() + "(this.uri+'/'+" + s + ")()\n" +
                    "      return link;\n" +
                    "   }";
        }
        
    }
    
    public class ResourceDojoComponents extends ResourceJavaScript {
        
        public ResourceDojoComponents(Resource r, FileObject rdjDir) {
            super(r, rdjDir);
        }
        
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
        
        protected String replaceTokens(String line, String object, String pkg) {
            String replacedLine = line;
            replacedLine = replacedLine.replaceAll("//__INCLUDE_JS_SCRIPTS__", includeJs+"\n//__INCLUDE_JS_SCRIPTS__");
            replacedLine = replacedLine.replaceAll("//__LIBS_JS_SCRIPTS__", libsJs+"\n//__LIBS_JS_SCRIPTS__");
            replacedLine = replacedLine.replaceAll("//__RESOURCES_DOJO_SCRIPTS__", resourcesDojo+"\n//__RESOURCES_DOJO_SCRIPTS__");
            replacedLine = replacedLine.replaceAll("//__REQUIRE_DOJO_SCRIPTS__", requireDojo+"\n//__REQUIRE_DOJO_SCRIPTS__");
            return super.replaceTokens(replacedLine, object, pkg);
        }
    }
    
    public class ResourceJmakiComponent extends ResourceDojoComponents {
        
        public ResourceJmakiComponent(Resource r, FileObject restDir) {
            super(r, restDir);
        }
        
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
    
    public class ResourceJmakiTemplate extends ResourceJavaScript {
        
        public ResourceJmakiTemplate(Resource r, FileObject tRestDir) {
            super(r, tRestDir);
        }
        
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

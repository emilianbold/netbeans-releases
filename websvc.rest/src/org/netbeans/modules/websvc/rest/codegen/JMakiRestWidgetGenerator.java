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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.websvc.rest.codegen.model.ClientStubModel.*;
import org.netbeans.modules.websvc.rest.support.ZipUtil;
import org.netbeans.modules.websvc.rest.support.ZipUtil.UnZipFilter;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;

/**
 * jMaki Rest widget generator for REST resource class.
 * The generator takes as paramenters:
 *  - target directory
 *  - REST resource bean meta model.
 *
 * @author Ayub Khan
 */
public class JMakiRestWidgetGenerator extends ClientStubsGenerator {
    
    public static final String RESOURCES = "resources"; //NOI18N
    public static final String TEMPLATES = "templates"; //NOI18N
    public static final String DOJO = "dojo"; //NOI18N
    public static final String RDJ = "rdj"; //NOI18N
    public static final String DATA = "data"; //NOI18N
    public static final String WIDGET = "widget"; //NOI18N
    public static final String TABLE = "table"; //NOI18N
    public static final String STORE = "Store"; //NOI18N
    public static final String JSP = "jsp"; //NOI18N
    public static final String PHP = "php"; //NOI18N
    public static final String RHTML = "rhtml"; //NOI18N
    public static final String EJS = "ejs"; //NOI18N
    public static final String LIBS = "libs"; //NOI18N
    public static final String DJD43 = "djd43"; //NOI18N
    public static final String JMAKI_DOJO = "jmaki-dojo"; //NOI18n
    public static final String JMAKI_COMP_LIB = "jmakicomplib"; //NOI18n
     
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
    
    private FileObject resourcesDir;
    private FileObject dojoDir;
    private FileObject rdjDir;
    private FileObject rjsDir;
    private FileObject templatesDir;
    private String includeJs = "";
    private String libsJs = "";
    private String resourcesDojo = "";
    private String requireDojo = "";
    protected String dojoResSelList = "";
    protected String jmakiResSelList = "";
    protected String jmakiResTagList = "";
    private FileObject restDir;
    
    public JMakiRestWidgetGenerator(FileObject rootFolder, String folderName, Project p, 
            boolean createJmaki, boolean overwrite) throws IOException {
        super(rootFolder, folderName, p, overwrite);
        resourcesDir = createFolder(getRootFolder(), RESOURCES);
        dojoDir = createFolder(resourcesDir, DOJO);
        restDir = createFolder(dojoDir, REST);
        rdjDir = createFolder(restDir, RDJ);
        templatesDir = createFolder(getRootFolder(), TEMPLATES);
        setStubFolder(createFolder(restDir, RJS));
    }
 
    @Override
    public Set<FileObject> generate(ProgressHandle pHandle) throws IOException {
        if(getProject() == null) {
            throw new IOException("Project parameter cannot be null for jMaki REST widget generator.");
        }
        super.generate(pHandle);

        List<Resource> resourceList = getModel().getResources();
        includeJs = "    "+RDJ+".includeJS('../"+RJS+"/"+getProjectName().toLowerCase()+"/"+getProjectName() + "." + JS+"');\n";
        libsJs = "                   '../"+RJS+"/"+getProjectName().toLowerCase()+"/"+getProjectName() + "." + JS+"',\n";
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

        initDojo(getProject(), resourceList);
        initJmaki(getProject(), resourceList);

        for (Resource r : resourceList) {
            if(pHandle != null)
                reportProgress(NbBundle.getMessage(JMakiRestWidgetGenerator.class,
                    "MSG_GeneratingClass", r.getName(), JS));
            
            //Generate the resource dojo and jmaki script
                new ResourceDojoComponents(r, rdjDir).generate();
                new ResourceJmakiComponent(r, restDir).generate();
                File dir = new File(FileUtil.toFile(templatesDir), DOJO+File.separator+REST);
                FileUtil.createFolder(dir);
                new ResourceJmakiTemplate(r, FileUtil.toFileObject(dir)).generate();
        }

        Set<FileObject> files = new HashSet<FileObject>(); 
        //copy dojo libs
        if(pHandle != null)
            reportProgress(NbBundle.getMessage(JMakiRestWidgetGenerator.class,
                "MSG_CopyLibs", DJD43, JS));//NoI18n
        copyDojoLibs();

        // Create the ZIP file
        if(pHandle != null)
            reportProgress(NbBundle.getMessage(JMakiRestWidgetGenerator.class,
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
        ZipUtil zipUtil = new ZipUtil();
        zipUtil.zip(zipFile, sources, paths);

        FileObject testFile = restDir.getFileObject(JMAKI_TESTRESOURCESTABLE, JSP);
        if (testFile != null) {
            files.add(testFile);
        }
        FileObject readme = restDir.getFileObject(JMAKI_README, TXT);
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
        
        File jmakiCompDir = new File(userDir, JMAKI_COMP_LIB);
        if(!jmakiCompDir.exists()) {
            throw new RuntimeException("Cannot find jMaki component folder (" + jmakiCompDir + ").");
        }
        
        File dojoLib = findDojoLibrary(jmakiCompDir);
        if(dojoLib != null) {
            ZipUtil zipUtil = new ZipUtil();
            zipUtil.addFilter(new UnZipFilter() {
                public boolean allow(ZipEntry entry) {
                    return (entry.getName().startsWith(RESOURCES+"/"+DOJO+"/"+RESOURCES+"/"+LIBS) ||
                        entry.getName().startsWith(RESOURCES+File.separator+DOJO+File.separator+RESOURCES+File.separator+LIBS));
                }
            });
            zipUtil.unzip(new FileInputStream(dojoLib), resourcesDir.getParent(), canOverwrite());
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

    private void initDojo(Project p, List<Resource> resourceList) throws IOException {
        TokenReplacer tr = new TokenReplacer();
        tr.addToken(TTL_DojoResources_Stubs, NbBundle.getMessage(JMakiRestWidgetGenerator.class, TTL_DojoResources_Stubs));
        tr.addToken(MSG_Readme, NbBundle.getMessage(JMakiRestWidgetGenerator.class, MSG_Readme));
        tr.addToken(MSG_TestPage, NbBundle.getMessage(JMakiRestWidgetGenerator.class, MSG_TestPage));
        tr.addToken(MSG_SelectResource, NbBundle.getMessage(JMakiRestWidgetGenerator.class, MSG_SelectResource));
        tr.addToken(BASE_URL_TOKEN, getBaseUrl());
        tr.addToken(FILE_ENCODING_TOKEN, getBaseEncoding().name());
        
        createFolder(rdjDir, DATA);//NoI18n
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
        tr.addToken(TTL_JMakiWidget_Stubs, NbBundle.getMessage(JMakiRestWidgetGenerator.class, TTL_JMakiWidget_Stubs));
        tr.addToken(MSG_Readme, NbBundle.getMessage(JMakiRestWidgetGenerator.class, MSG_Readme));
        tr.addToken(MSG_TestPage, NbBundle.getMessage(JMakiRestWidgetGenerator.class, MSG_TestPage));
        tr.addToken(MSG_JMaki_Readme_Content, NbBundle.getMessage(JMakiRestWidgetGenerator.class, MSG_JMaki_Readme_Content));
        tr.addToken(FILE_ENCODING_TOKEN, getBaseEncoding().name());
        
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

    protected String createDojoResourceSelectList(List<Resource> resourceList) {
        String str = "";
        for (Resource r : resourceList) {
            if(r.isContainer()) {
                str += "            <option value='"+getBaseUrl()+"/"+r.getRepresentation().getRoot().getName()+"/;" + r.getName() + "'>" + r.getName() + "</option>\n";
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
        String proxyComment = "<!-- If using cross-domain proxy uncomment tag below and comment above line -->\n";
        for (Resource r : resourceList) {
            if(r.isContainer()) {
                String name = r.getName();
                String pathName = r.getRepresentation().getRoot().getName();
                if(count++ == 0) {
                    str += "         <% if(p.equals(\"" + name + "\")) {%>\n"+
                        "            <a:widget name=\"dojo.rest." + pathName + "table\" service=\""+getBaseUrl()+"/" + pathName + "/\" />\n"+
                        "            "+proxyComment+"<!-- &lt;a:widget name=\"dojo.rest." + pathName + "table\" service=\""+getBaseUrl()+"/" + pathName + "/\" args=\"proxy="+getProxyUrl()+"\"/>-->\n";
                } else {
                    str += "         <% } else if(p.equals(\"" + name + "\")) {%>\n"+
                        "            <a:widget name=\"dojo.rest." + pathName + "table\" service=\""+getBaseUrl()+"/" + pathName + "/\" />\n"+
                        "            "+proxyComment+"<!-- &lt;a:widget name=\"dojo.rest." + pathName + "table\" service=\""+getBaseUrl()+"/" + pathName + "/\" args=\"proxy="+getProxyUrl()+"\"/>-->\n";
                }
            }
        }
        str += "<% }%>";
        return str;
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
                containerStubTokens.put("__PROJECT_NAME__", getProjectName());
                containerStubTokens.put("<!-- __DOJO_RESOURCE_SELECT_LIST__ -->", dojoResSelList + "\n<!-- __DOJO_RESOURCE_SELECT_LIST__ -->");
                containerStubTokens.put("<!-- __JMAKI_RESOURCE_SELECT_LIST__ -->", jmakiResSelList + "\n<!-- __JMAKI_RESOURCE_SELECT_LIST__ -->");
                containerStubTokens.put("<!-- __JMAKI_RESOURCE_TAG_LIST__ -->", jmakiResTagList + "\n<!-- __JMAKI_RESOURCE_TAG_LIST__ -->");
                containerStubTokens.put("TTL_JMakiWidget_Stubs", NbBundle.getMessage(JMakiRestWidgetGenerator.class, "TTL_JMakiWidget_Stubs"));
                containerStubTokens.put(BASE_URL_TOKEN, getBaseUrl());
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

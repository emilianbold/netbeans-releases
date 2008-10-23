/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.websvc.rest.codegen.model.ClientStubModel;
import org.netbeans.modules.websvc.rest.codegen.model.ClientStubModel.*;
import org.netbeans.modules.websvc.rest.projects.WebProjectRestSupport;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.netbeans.modules.websvc.rest.wizard.Util;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Code generator for plain REST resource class.
 * The generator takes as paramenters:
 *  - target directory
 *  - REST resource bean meta getModel().
 *
 * @author Ayub Khan
 */
public class ClientStubsGenerator extends AbstractGenerator {
    
    public static final String REST = "rest"; //NOI18N
    public static final String RJS = "rjs"; //NOI18N
    public static final String CSS = "css"; //NOI18N
    public static final String JS = "js"; //NOI18N
    public static final String HTML = "html"; //NOI18N
    public static final String HTM = "htm"; //NOI18N
    public static final String TXT = "txt"; //NOI18N
    public static final String JSON = "json"; //NOI18N
    public static final String GIF = "gif"; //NOI18N
    public static final String IMAGES = "images"; //NOI18N
    public static final String BUNDLE = "Bundle"; //NOI18N
    public static final String PROPERTIES = "properties"; //NOI18N
    
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
    
    public static final String PROXY = "RestProxyServlet"; //NOI18N
    public static final String PROXY_URL = "/restproxy";
    public static final String PROXY_TEMPLATE = "Templates/WebServices/RestProxyServlet.txt"; //NOI18N
    
    public static final String TTL_DojoResources_Stubs = "TTL_DojoResources_Stubs";
    public static final String MSG_Readme = "MSG_Readme";
    public static final String MSG_TestPage = "MSG_TestPage";
    public static final String TTL_RestClient_Stubs = "TTL_RestClient_Stubs";
    public static final String TTL_JMakiWidget_Stubs = "TTL_JMakiWidget_Stubs";
    public static final String MSG_SelectResource = "MSG_SelectResource";
    public static final String MSG_JS_Readme_Content = "MSG_JS_Readme_Content";
    public static final String MSG_JMaki_Readme_Content = "MSG_JMaki_Readme_Content";

    public static final String DEFAULT_PROTOCOL = "http";
    public static final String DEFAULT_HOST = "localhost";
    public static final String DEFAULT_PORT = "8080";
    public static final String DEFAULT_BASE_URL = DEFAULT_PROTOCOL+"://"+DEFAULT_HOST+":"+DEFAULT_PORT;
    public static final String BASE_URL_TOKEN = "__BASE_URL__";
    public static final String FILE_ENCODING_TOKEN = "__FILE_ENCODING__";
    
    private FileObject stubFolder;
    private Project p;
    private boolean overwrite;
    private String projectName;
    private ResourceModel model;
    private FileObject rjsDir;
    private InputStream wis;
    private String folderName;
    private String baseUrl;
    private String proxyUrl;
    private Charset baseEncoding;
    private FileObject rootFolder;
    
    public ClientStubsGenerator(FileObject rootFolder, String folderName, Project p, 
            boolean overwrite) throws IOException {
        assert p != null;
        this.rootFolder = rootFolder;
        this.folderName = folderName;
        this.p = p;
        this.overwrite = overwrite;
        this.projectName = ProjectUtils.getInformation(getProject()).getName();
        this.baseEncoding = FileEncodingQuery.getEncoding(rootFolder);
    }
    
    public ClientStubsGenerator(FileObject rootFolder, String folderName, InputStream wis, 
            boolean overwrite) throws IOException {
        this.rootFolder = rootFolder;
        this.folderName = folderName;
        this.wis = wis;
        this.overwrite = overwrite;
        this.projectName = "NewProject";
        this.baseEncoding = FileEncodingQuery.getEncoding(rootFolder);
    }

    public FileObject getRootFolder() {
        return rootFolder;
    }

    public FileObject getStubFolder() {
        if(stubFolder == null) {
            try {
                stubFolder = createFolder(getRootFolder(), getFolderName());
            } catch (IOException ex) {
            }
        }
        return stubFolder;
    }
    
    public void setStubFolder(FileObject stubFolder) {
        this.stubFolder = stubFolder;
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
    
    public String getProjectName() {
        return projectName;
    }
    
    public ResourceModel getModel() {
        return model;
    }
    
    public String getDefaultBaseUrl() {
        return DEFAULT_BASE_URL+"/";
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public String getProxyUrl() {
        return proxyUrl;
    }
    
    public void setProxyUrl(String proxyUrl) {
        this.proxyUrl = proxyUrl;
    }
    
    public Charset getBaseEncoding() {
        return baseEncoding;
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
                    return ClientStubModel.normalizeName(paths[i+1]);
                }
            }
        }
        return ClientStubModel.normalizeName(appName);
    }
    
    private String findBaseUrl(Project p) {
        String url = null;
        FileObject privProp = p.getProjectDirectory().getFileObject("nbproject/private/private.properties");
        if(privProp != null) {
            String asProp = getProperty(privProp, "deploy.ant.properties.file");
            FileObject asPropFile = FileUtil.toFileObject(new File(asProp));
            url = getProperty(asPropFile, "sjsas.url");
            if(url == null)
                url = getProperty(asPropFile, "tomcat.url");
            if(url != null)
                url = url.replace("\\", "");
            }
        return url;
    }
    
    private String findAppContext(Project p) throws IOException {
        String appContext = null;
        FileObject sunWebData = p.getProjectDirectory().getFileObject("web/WEB-INF/sun-web.xml");
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(FileUtil.toFile(sunWebData));

            //Context nodes
            NodeList contextNodes = RestUtils.getNodeList(doc, "//sun-web-app/context-root");
            if (contextNodes != null && contextNodes.getLength() > 0) {
                Node contextNode = contextNodes.item(0);
                if(contextNode.getFirstChild() instanceof Text)
                    appContext = contextNode.getFirstChild().getNodeValue().trim();
            }
        } catch (Exception ex) {//If parseer fails, try directly reading the context
            appContext = getXmlData(sunWebData, "context-root");
        }
        if(appContext != null) {
            if(appContext.length() > 1 && appContext.startsWith("/"))
                appContext = appContext.substring(1);
        } else {
            appContext = ProjectUtils.getInformation(p).getName();
        }
        return appContext;
    }
    
    private String getProperty(FileObject fo, String name) {
        if(fo == null)
            return null;
        FileLock lock = null;
        try {
            lock = fo.lock();
            BufferedReader reader = new BufferedReader(new FileReader(FileUtil.toFile(fo)));
            String line;
            StringBuffer sb = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                if(line.trim().startsWith(name+"="))
                    return line.trim().split("=")[1];
            }
        } catch(IOException iox) {
        } finally {
            if(lock != null)
                lock.releaseLock();
        }
        return null;
    }

    private String getXmlData(FileObject fo, String name) {
        if(fo == null)
            return null;
        FileLock lock = null;
        try {
            lock = fo.lock();
            BufferedReader reader = new BufferedReader(new FileReader(FileUtil.toFile(fo)));
            String line;
            StringBuffer sb = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                if(line.contains("<"+name+">"))
                    return line.substring(line.indexOf(">")+1, line.indexOf("</"));
            }
        } catch(IOException iox) {  
        } finally {
            if(lock != null)
                lock.releaseLock();
        }
        return null;
    }

    public Set<FileObject> generate(ProgressHandle pHandle) throws IOException {
        if(pHandle != null)
            initProgressReporting(pHandle, false);
        Project targetPrj = FileOwnerQuery.getOwner(getRootFolder());
        if(p != null) {
            this.model = new ClientStubModel().createModel(p);
            this.model.build();
            String url = findBaseUrl(p);
            if(url == null)
                url = getDefaultBaseUrl();
            String proxyUrl2 = findBaseUrl(targetPrj);
            if(proxyUrl2 == null)
                proxyUrl2 = url;
            ServletMapping servletMap = WebProjectRestSupport.getRestServletMapping(p);
            String path = "/resources";
            if(servletMap != null)
                path = servletMap.getUrlPattern();
            if(path.endsWith("/*"))
                path = path.substring(0, path.length()-2);
            setBaseUrl((url.endsWith("/")?url:url+"/") + findAppContext(getProject()) + (path.startsWith("/")?path:"/"+path));
            setProxyUrl((proxyUrl2.endsWith("/")?proxyUrl2:proxyUrl2+"/") + findAppContext(targetPrj) + PROXY_URL);
        } else if(wis != null) {
            this.model = new ClientStubModel().createModel(wis);
            this.model.build();
            String url = ((WadlModeler)this.model).getBaseUrl();
            if(url == null)
                url = getDefaultBaseUrl();
            setBaseUrl(url);
            setProxyUrl(url+".."+PROXY_URL);
            this.projectName = getApplicationNameFromUrl(url);
        }
        List<Resource> resourceList = getModel().getResources();
        
        rjsDir = getStubFolder();
        initJs(p);
        
        FileObject prjStubDir = createFolder(rjsDir, getProjectName().toLowerCase());
        createDataObjectFromTemplate(JS_PROJECTSTUB_TEMPLATE, prjStubDir, getProjectName(), JS, canOverwrite());
        updateProjectStub(prjStubDir.getFileObject(getProjectName(), JS), getProjectName(), "");
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
        }
        updateRestStub(rjsDir.getFileObject(JS_TESTSTUBS, HTML), resourceList, "");
  
        Set<FileObject> files = new HashSet<FileObject>();
        FileObject rjsTest = rjsDir.getFileObject(JS_TESTSTUBS, HTML);
        if(rjsTest != null)
            files.add(rjsTest);
        FileObject readme = rjsDir.getFileObject(JS_README, TXT);
        if(readme != null)
            files.add(readme);
        return files;
    }
    
    protected FileObject createDataObjectFromTemplate(final String template, final FileObject dir, 
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

    protected void copyDirectory(final FileSystem fs, final File src, final File dst)
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
        tr.addToken(FILE_ENCODING_TOKEN, getBaseEncoding().name());
        
        FileObject fo = createDataObjectFromTemplate(JS_TESTSTUBS_TEMPLATE, rjsDir, JS_TESTSTUBS, HTML, false);
        tr.replaceTokens(fo);
        
        createDataObjectFromTemplate(JS_STUBSUPPORT_TEMPLATE, rjsDir, JS_SUPPORT, JS, false);
        
        fo = createDataObjectFromTemplate(JS_README_TEMPLATE, rjsDir, JS_README, HTML, false);
        tr.replaceTokens(fo);
        
        fo = createDataObjectFromTemplate(PROXY_TEMPLATE, rjsDir, PROXY, TXT, false);
        
        File cssDir = new File(FileUtil.toFile(rjsDir), "css");
        cssDir.mkdirs();
        copySupportFiles(cssDir);
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
 
    protected void copyFile(String resourceName, File destFile) throws IOException {
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
            
    protected FileObject createFolder(FileObject parent, String folderName) throws IOException {
        FileObject folder = parent.getFileObject(folderName);
        if(folder == null)
            folder = parent.createFolder(folderName);
        return folder;
    }
    
    private void updateProjectStub(FileObject projectStub, String prjName, String pkg) throws IOException {
        FileLock lock = projectStub.lock();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(FileUtil.toFile(projectStub)));
            String line;
            StringBuffer sb = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                if (line.contains(BASE_URL_TOKEN)) {
                    sb.append(line.replaceAll(BASE_URL_TOKEN, getBaseUrl()));
                } else if (line.contains("__PROJECT_NAME__")) {
                    sb.append(line.replaceAll("__PROJECT_NAME__", prjName));
                } else if (line.contains("__PROJECT_INIT_BODY__")) {
                    String initBody = "";
                    int count = 0;
                    List<Resource> resourceList = getModel().getResources();
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
            OutputStreamWriter writer = new OutputStreamWriter(projectStub.getOutputStream(lock), getBaseEncoding());
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
        sb2.append("\t\tvar app = new " + pkg+prjName + "('"+getBaseUrl()+"');\n");
        sb2.append("\t\t//Uncomment below if using proxy for javascript cross-domain.\n");
        sb2.append("\t\t//app.setProxy(\""+getProxyUrl()+"\");\n");
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
        sb2.append("\t\t    str += 'No items, please check the url: <a href=\"'+uri+'\" target=\"_blank\">'+uri+'</a>.<br/>" +
                "Set proxy if RESTful web service is not running on the same domain as this application.';\n");
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
            OutputStreamWriter writer = new OutputStreamWriter(restStub.getOutputStream(lock), getBaseEncoding());
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
                OutputStreamWriter writer = new OutputStreamWriter(fo.getOutputStream(lock), getBaseEncoding());
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
                sb.append(createMethod(m, RJSSUPPORT+".", pkg)+",\n\n");
            }
            String s = sb.toString();
            if(s.length() > 3)
                return s.substring(0, s.length()-3);
            else
                return s;
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
                containerStubTokens.put("__STUB_METHODS__", stubJSToken.equals("")?stubJSToken:"   ,\n"+stubJSToken);
                containerStubTokens.put("__PROJECT_NAME__", getProjectName());
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
                genericStubTokens.put("__STUB_METHODS__", stubJSToken.equals("")?stubJSToken:"   ,\n"+stubJSToken);
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
                plural = plural.substring(0, plural.length()-2)+Constants.COLLECTION;
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
}

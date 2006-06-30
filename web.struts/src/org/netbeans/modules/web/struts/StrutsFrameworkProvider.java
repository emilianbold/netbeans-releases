/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.struts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.dd.api.common.CreateCapability;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.JspConfig;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.Taglib;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WelcomeFileList;
import org.netbeans.modules.web.struts.config.model.MessageResources;
import org.netbeans.modules.web.struts.config.model.StrutsConfig;
import org.openide.ErrorManager;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.Repository;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;

import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.FrameworkConfigurationPanel;

import org.netbeans.modules.web.struts.ui.StrutsConfigurationPanel;

import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;


/**
 *
 * @author petr
 */
public class StrutsFrameworkProvider extends WebFrameworkProvider {
    
    
    private static final String STRUTS_CONFIG ="nbres:/org/netbeans/modules/web/struts/resources/struts-config.xml";
    
    private StrutsConfigurationPanel panel;
    private static String defaultAppResource ="com.myapp.struts.ApplicationResource";  //NOI18N
    
    public StrutsFrameworkProvider(){
        super (
                NbBundle.getMessage(StrutsFrameworkProvider.class, "Sruts_Name"),               //NOI18N
                NbBundle.getMessage(StrutsFrameworkProvider.class, "Sruts_Description"));       //NOI18N
    }

    public Set extend (WebModule wm) {
        FileObject fo = wm.getDocumentBase();
        Project project = FileOwnerQuery.getOwner(fo);
        
        Library lib = LibraryManager.getDefault().getLibrary("struts");                         //NOI18N
        if (lib != null) {
            ProjectClassPathExtender cpExtender = (ProjectClassPathExtender) project.getLookup().lookup(ProjectClassPathExtender.class);
            if (cpExtender != null) {
                try {
                    cpExtender.addLibrary(lib);
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ioe);

                }
            } else {
                ErrorManager.getDefault().log ("WebProjectClassPathExtender not found in the project lookup of project: "+project.getProjectDirectory().getPath());    //NOI18N
            }

            try {
                FileSystem fs = wm.getWebInf().getFileSystem();
                fs.runAtomicAction(new CreateStrutsConfig(wm));
              
            } catch (FileNotFoundException exc) {
                ErrorManager.getDefault().notify(exc);
                return null;
            } catch (IOException exc) {
                ErrorManager.getDefault().notify(exc);
                return null;
            }
        }
        FileObject welcomePage = wm.getDocumentBase().getFileObject("welcome.jsp");
        return null;
    }
    
    private static String readResource(InputStream is, String encoding) throws IOException {
        // read the config from resource first
        StringBuffer sb = new StringBuffer();
        String lineSep = System.getProperty("line.separator");//NOI18N
        BufferedReader br = new BufferedReader(new InputStreamReader(is, encoding));
        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            sb.append(lineSep);
            line = br.readLine();
        }
        br.close();
        return sb.toString();
    }

    public java.io.File[] getConfigurationFiles(org.netbeans.modules.web.api.webmodule.WebModule wm) {
        FileObject webinf = wm.getWebInf();
        List files = new ArrayList();
        FileObject[] configs = StrutsConfigUtilities.getConfigFilesFO(wm.getDeploymentDescriptor());
        for (int i = 0; i < configs.length; i ++){
            files.add(FileUtil.toFile(configs[i]));
        }
        FileObject fo = webinf.getFileObject("tiles-defs.xml");  //NOI18N
        if (fo != null) files.add(FileUtil.toFile(fo));
        fo = webinf.getFileObject("validation.xml");            //NOI18N
        if (fo != null) files.add(FileUtil.toFile(fo));
        fo = webinf.getFileObject("validator-rules.xml");       //NOI18N
        if (fo != null) files.add(FileUtil.toFile(fo));
        
        File [] rFiles = new File [files.size()];
        files.toArray(rFiles);
        return rFiles;
    }

    public boolean isInWebModule(org.netbeans.modules.web.api.webmodule.WebModule wm) {
        return StrutsConfigUtilities.getActionServlet(wm.getDeploymentDescriptor()) == null ? false : true;
    }
    
    public FrameworkConfigurationPanel getConfigurationPanel(WebModule wm) {
        boolean defaultValue = (wm == null || !isInWebModule(wm));
        panel = new StrutsConfigurationPanel(!defaultValue);
        if (defaultValue){
            // get configuration panel with default value
            panel.setAppResource(defaultAppResource);
        }
        else {
            // get configuration panel with values from the wm
            Servlet servlet = StrutsConfigUtilities.getActionServlet(wm.getDeploymentDescriptor());
            panel.setServletName(servlet.getServletName());
            panel.setURLPattern(StrutsConfigUtilities.getActionServletMapping(wm.getDeploymentDescriptor()));
            MessageResources resource = StrutsConfigUtilities.getDefatulMessageResource(wm.getDeploymentDescriptor());
            if (resource != null){
                String name = resource.getAttributeValue("parameter");
                name = name.replaceAll("/", ".");
                panel.setAppResource(name);
            }
        }
        
        return panel;
    }
    
    private class  CreateStrutsConfig implements FileSystem.AtomicAction{
        WebModule wm;
        public CreateStrutsConfig (WebModule wm){
            this.wm = wm;
        }
        
        private void createFile(FileObject target, String content, String encoding) throws IOException{            
            FileLock lock = target.lock();
            try {
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(target.getOutputStream(lock), encoding));
                bw.write(content);
                bw.close();

            }
            finally {
                lock.releaseLock();
            }
        }
        
        public void run() throws IOException {
            // copy struts-config.xml
            String content = readResource (Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-modules-web-struts/struts-config.xml").getInputStream (), "UTF-8"); //NOI18N
            content = content.replaceFirst("____ACTION_MAPPING___",  //NOI18N
                    StrutsConfigUtilities.getActionAsResource(panel.getURLPattern(), "/Welcome"));
            content = content.replaceFirst("_____MESSAGE_RESOURCE____",  //NOI18N
                    panel.getAppResource().replace('.', '/'));
            FileObject target = FileUtil.createData(wm.getWebInf(), "struts-config.xml");//NOI18N
            createFile(target, content, "UTF-8"); //NOI18N
            //copy tiles-defs.xml
            content = readResource (Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-modules-web-struts/tiles-defs.xml").getInputStream (), "UTF-8"); //NOI18N
            target = FileUtil.createData(wm.getWebInf(), "tiles-defs.xml");//NOI18N
            createFile(target, content, "UTF-8"); //NOI18N
            //copy validation.xml
            content = readResource (Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-modules-web-struts/validation.xml").getInputStream (), "UTF-8"); //NOI18N
            target = FileUtil.createData(wm.getWebInf(), "validation.xml");//NOI18N
            createFile(target, content, "UTF-8"); //NOI18N
            //copy validator-rules.xml
            content = readResource (Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-modules-web-struts/validator-rules.xml").getInputStream (), "UTF-8"); //NOI18N
            target = FileUtil.createData(wm.getWebInf(), "validator-rules.xml");//NOI18N
            createFile(target, content, "UTF-8"); //NOI18N
            //copy Welcome.jsp
            content = readResource (Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-modules-web-struts/welcome.jsp").getInputStream (), "UTF-8"); //NOI18N
            target = FileUtil.createData(wm.getDocumentBase(), "welcomeStruts.jsp");//NOI18N
            createFile(target, content, "UTF-8"); //NOI18N
            //MessageResource.properties
            content = readResource (Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-modules-web-struts/MessageResources.properties").getInputStream (), "UTF-8"); //NOI18N
            Project project = FileOwnerQuery.getOwner(wm.getDocumentBase());
            SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            String sresource = panel.getAppResource();
            String path = sresource.substring(0, sresource.lastIndexOf("."));   //NOI18N
            String name = sresource.substring(sresource.lastIndexOf(".")+1);    //NOI18N
            name = name + ".properties";   //NOI18N
            FileObject targetFolder = sourceGroups[0].getRootFolder();
            String folders[] = path.split("\\.");
            for (int i = 0; i < folders.length; i++){
                if (targetFolder.getFileObject(folders[i])== null)
                    targetFolder = targetFolder.createFolder(folders[i]);
                else
                    targetFolder = targetFolder.getFileObject(folders[i]);
            }
            target = FileUtil.createData(targetFolder, name);//NOI18N
            createFile(target, content, "UTF-8"); //NOI18N
            
            if (panel.addTLDs()){
                //copy struts-bean.tld
                content = readResource (Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-modules-web-struts/struts-bean.tld").getInputStream (), "UTF-8"); //NOI18N
                target = FileUtil.createData(wm.getWebInf(), "struts-bean.tld");//NOI18N
                createFile(target, content, "UTF-8"); //NOI18N
                //copy struts-html.tld
                content = readResource (Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-modules-web-struts/struts-html.tld").getInputStream (), "UTF-8"); //NOI18N
                target = FileUtil.createData(wm.getWebInf(), "struts-html.tld");//NOI18N
                createFile(target, content, "UTF-8"); //NOI18N
                //copy struts-logic.tld
                content = readResource (Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-modules-web-struts/struts-logic.tld").getInputStream (), "UTF-8"); //NOI18N
                target = FileUtil.createData(wm.getWebInf(), "struts-logic.tld");//NOI18N
                createFile(target, content, "UTF-8"); //NOI18N
                //copy struts-nested.tld
                content = readResource (Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-modules-web-struts/struts-nested.tld").getInputStream (), "UTF-8"); //NOI18N
                target = FileUtil.createData(wm.getWebInf(), "struts-nested.tld");//NOI18N
                createFile(target, content, "UTF-8"); //NOI18N
                //copy struts-tiles.tld
                content = readResource (Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-modules-web-struts/struts-tiles.tld").getInputStream (), "UTF-8"); //NOI18N
                target = FileUtil.createData(wm.getWebInf(), "struts-tiles.tld");//NOI18N
                createFile(target, content, "UTF-8"); //NOI18N
            }
            
            // Enter servlet into the deployment descriptor
            FileObject dd = wm.getDeploymentDescriptor();
            WebApp ddRoot = DDProvider.getDefault().getDDRootCopy(dd);
            if (ddRoot != null){
                try{
                    Servlet servlet = (Servlet)ddRoot.createBean("Servlet"); //NOI18N
                    servlet.setServletName("action"); //NOI18N
                    servlet.setServletClass("org.apache.struts.action.ActionServlet"); //NOI18N    

                    ddRoot.addServlet(servlet);

                    InitParam param = (InitParam)servlet.createBean("InitParam"); //NOI18N
                    param.setParamName("config");//NOI18N
                    param.setParamValue("/WEB-INF/struts-config.xml");//NOI18N
                    servlet.addInitParam(param);
                    param = (InitParam)servlet.createBean("InitParam"); //NOI18N
                    param.setParamName("debug");//NOI18N
                    param.setParamValue("2");//NOI18N
                    servlet.addInitParam(param);
                    param = (InitParam)servlet.createBean("InitParam"); //NOI18N
                    param.setParamName("detail");//NOI18N
                    param.setParamValue("2");//NOI18N
                    servlet.addInitParam(param);
                    servlet.setLoadOnStartup(new BigInteger("2"));//NOI18N


                    ServletMapping mapping = (ServletMapping)ddRoot.createBean("ServletMapping"); //NOI18N
                    mapping.setServletName(panel.getServletName());//NOI18N
                    mapping.setUrlPattern(panel.getURLPattern());//NOI18N

                    ddRoot.addServletMapping(mapping);
                    
                    if (panel.addTLDs()){
                        try{
                            CreateCapability create;
                            if (ddRoot.getVersion().equals(ddRoot.VERSION_2_3)){
                                create  = ddRoot;
                                ddRoot.addTaglib(createTaglib(create, "/WEB-INF/struts-bean.tld", "/WEB-INF/struts-bean.tld"));     //NOI18N
                                ddRoot.addTaglib(createTaglib(create, "/WEB-INF/struts-html.tld", "/WEB-INF/struts-html.tld"));     //NOI18N
                                ddRoot.addTaglib(createTaglib(create, "/WEB-INF/struts-logic.tld", "/WEB-INF/struts-logic.tld"));   //NOI18N
                                ddRoot.addTaglib(createTaglib(create, "/WEB-INF/struts-nested.tld", "/WEB-INF/struts-nested.tld")); //NOI18N
                                ddRoot.addTaglib(createTaglib(create, "/WEB-INF/struts-tiles.tld", "/WEB-INF/struts-tiles.tld"));   //NOI18N
                            }
                            else {
                                JspConfig jspConfig = ddRoot.getSingleJspConfig();
                                if (jspConfig==null){
                                    jspConfig = (JspConfig)ddRoot.createBean("JspConfig");
                                    ddRoot.setJspConfig(jspConfig);
                                }
                                create = jspConfig;
                                jspConfig.addTaglib(createTaglib(create, "/WEB-INF/struts-bean.tld", "/WEB-INF/struts-bean.tld"));  //NOI18N
                                jspConfig.addTaglib(createTaglib(create, "/WEB-INF/struts-html.tld", "/WEB-INF/struts-html.tld"));  //NOI18N
                                jspConfig.addTaglib(createTaglib(create, "/WEB-INF/struts-logic.tld", "/WEB-INF/struts-logic.tld"));    //NOI18N
                                jspConfig.addTaglib(createTaglib(create, "/WEB-INF/struts-nested.tld", "/WEB-INF/struts-nested.tld"));  //NOI18N
                                jspConfig.addTaglib(createTaglib(create, "/WEB-INF/struts-tiles.tld", "/WEB-INF/struts-tiles.tld"));    //NOI18N
                            }
                            
                        }
                        catch (VersionNotSupportedException e){
                            ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
                        }
                    }
                    ddRoot.write(dd);
                    
                    
                }
                catch (ClassNotFoundException cnfe){
                    ErrorManager.getDefault().notify(cnfe);
                }
            }
            
            // changing index.jsp
            FileObject documentBase = wm.getDocumentBase();
            FileObject indexjsp = documentBase.getFileObject("index.jsp"); //NOI18N
            if (indexjsp != null){
                changeIndexJSP(indexjsp);
            }
        }
        
        private Taglib createTaglib(CreateCapability createObject, String location, String uri) throws ClassNotFoundException {
            Taglib taglib = (Taglib)createObject.createBean("Taglib"); //NOI18N
            taglib.setTaglibLocation(location);
            taglib.setTaglibUri(uri);
            return taglib;
        }
        
        /** Changes the index.jsp file. Only when there is <h1>JSP Page</h1> string.
         */
        private void changeIndexJSP(FileObject indexjsp) throws IOException {
            String content = readResource(indexjsp.getInputStream(), "UTF-8"); //NOI18N
            // what find
            String find = "<h1>JSP Page</h1>"; // NOI18N
            String endLine = System.getProperty("line.separator"); //NOI18N
            if ( content.indexOf(find) > 0){
                StringBuffer replace = new StringBuffer();
                replace.append(find);
                replace.append(endLine);
                replace.append("    <br/>");                        //NOI18N
                replace.append(endLine);
                replace.append("    <a href=\".");                  //NOI18N
                replace.append(StrutsConfigUtilities.getActionAsResource(panel.getURLPattern(), "/Welcome")); //NOI18N
                replace.append("\">");                              //NOI18N
                replace.append(NbBundle.getMessage(StrutsFrameworkProvider.class,"LBL_STRUTS_WELCOME_PAGE"));
                replace.append("</a>");                             //NOI18N
                content = content.replaceFirst(find, replace.toString());
                createFile(indexjsp, content, "UTF-8"); //NOI18N
            }
        }
    }
    
    
}

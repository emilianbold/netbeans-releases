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

package org.netbeans.modules.web.jsf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.*;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.wizards.JSFConfigurationPanel;
import org.openide.DialogDescriptor;
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
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.FrameworkConfigurationPanel;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
public class JSFFrameworkProvider extends WebFrameworkProvider {
    
    private JSFConfigurationPanel panel;
    /** Creates a new instance of JSFFrameworkProvider */
    public JSFFrameworkProvider() {
        super(
                NbBundle.getMessage(JSFFrameworkProvider.class, "JSF_Name"),               // NOI18N
                NbBundle.getMessage(JSFFrameworkProvider.class, "JSF_Description"));       //NOI18N
    }
    
    public Set extend(WebModule webModule) {
        FileObject fileObject = webModule.getDocumentBase();Project project = FileOwnerQuery.getOwner(fileObject);
        
        try {
            FileObject dd = webModule.getDeploymentDescriptor();
            WebApp ddRoot = DDProvider.getDefault().getDDRoot(dd);
            ClassPath cp = ClassPath.getClassPath(fileObject, ClassPath.COMPILE);
            if (ddRoot != null){
                if (!WebApp.VERSION_2_5.equals(ddRoot.getVersion())) {
                    Library jsfLibrary = LibraryManager.getDefault().getLibrary("jsf");
                    if (jsfLibrary != null) {
                        if (cp.findResource("javax/faces/FacesException.class") == null) {  //NOI18N
                            ProjectClassPathExtender cpExtender = (ProjectClassPathExtender) project.getLookup().lookup(ProjectClassPathExtender.class);
                            if (cpExtender != null) {
                                try {
                                    cpExtender.addLibrary(jsfLibrary);
                                    Library jstlLibrary = LibraryManager.getDefault().getLibrary("jstl11");
                                    if (jstlLibrary != null){
                                        cpExtender.addLibrary(jstlLibrary);
                                    }
                                } catch (IOException ioe) {
                                    //                    ErrorManager.getDefault().notify(ioe);
                                }
                            }
                        } else {
                            //                ErrorManager.getDefault().log ("WebProjectClassPathExtender not found in the project lookup of project: "+project.getProjectDirectory().getPath());    //NOI18N
                        }
                    }
                }
            }
            boolean isMyFaces = cp.findResource("org/apache/myfaces/webapp/StartupServletContextListener.class") != null; //NOI18N
            FileSystem fileSystem = webModule.getWebInf().getFileSystem();
            fileSystem.runAtomicAction(new CreateFacesConfig(webModule, isMyFaces));
        } catch (FileNotFoundException exc) {
            ErrorManager.getDefault().notify(exc);
        } catch (IOException exc) {
            ErrorManager.getDefault().notify(exc);
        }
        return null;
    }
    
    public static String readResource(InputStream is, String encoding) throws IOException {
        // read the config from resource first
        StringBuffer sbuffer = new StringBuffer();
        String lineSep = System.getProperty("line.separator");//NOI18N
        BufferedReader br = new BufferedReader(new InputStreamReader(is, encoding));
        String line = br.readLine();
        while (line != null) {
            sbuffer.append(line);
            sbuffer.append(lineSep);
            line = br.readLine();
        }
        br.close();
        return sbuffer.toString();
    }
    
    public java.io.File[] getConfigurationFiles(org.netbeans.modules.web.api.webmodule.WebModule wm) {
        // The JavaEE 5 introduce web modules without deployment descriptor. In such wm can not be jsf used.
        FileObject dd = wm.getDeploymentDescriptor();
        if (dd != null){
            FileObject[] filesFO = ConfigurationUtils.getFacesConfigFiles(wm);
            File[] files = new File[filesFO.length];
            for (int i = 0; i < filesFO.length; i++)
                files[i] = FileUtil.toFile(filesFO[i]);
            if (files.length > 0)
                return files;
        }
        return null;
    }
    
    public FrameworkConfigurationPanel getConfigurationPanel(WebModule webModule) {
        boolean defaultValue = (webModule == null || !isInWebModule(webModule));
        panel = new JSFConfigurationPanel(!defaultValue);
        if (!defaultValue){
            // get configuration panel with values from the wm
            Servlet servlet = ConfigurationUtils.getFacesServlet(webModule);
            panel.setServletName(servlet.getServletName());
            panel.setURLPattern(ConfigurationUtils.getFacesServletMapping(webModule));
            panel.setValidateXML(JSFConfigUtilities.validateXML(webModule.getDeploymentDescriptor()));
            panel.setVerifyObjects(JSFConfigUtilities.verifyObjects(webModule.getDeploymentDescriptor()));
        }
        
        return panel;
    }
    
    public boolean isInWebModule(org.netbeans.modules.web.api.webmodule.WebModule webModule) {
        // The JavaEE 5 introduce web modules without deployment descriptor. In such wm can not be jsf used.
        FileObject dd = webModule.getDeploymentDescriptor();
        return (dd != null && ConfigurationUtils.getFacesServlet(webModule) != null);
    }
    
    public String getServletPath(FileObject file){
        String url = null;
        if (file == null) return url;
        
        WebModule wm = WebModule.getWebModule(file);
        if (wm != null){
            url = FileUtil.getRelativePath(wm.getDocumentBase(), file);
            if (url.charAt(0)!='/')
                url = "/" + url;
            String mapping = ConfigurationUtils.getFacesServletMapping(wm);
            if (mapping != null && !"".equals(mapping)){
                if (mapping.endsWith("/*")){
                    mapping = mapping.substring(0, mapping.length()-2);
                    url = mapping + url;
                }
            }
        }
        return url;
    }
    
    public static void createFile(FileObject target, String content, String encoding) throws IOException{
        FileLock lock = target.lock();
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(target.getOutputStream(lock), encoding));
            bw.write(content);
            bw.close();
            
        } finally {
            lock.releaseLock();
        }
    }
    
    private class  CreateFacesConfig implements FileSystem.AtomicAction{
        WebModule webModule;
        boolean isMyFaces;
        
        public CreateFacesConfig(WebModule webModule, boolean isMyFaces){
            this.webModule = webModule;
            this.isMyFaces = isMyFaces;
        }
        
        public void run() throws IOException {
            // Enter servlet into the deployment descriptor
            FileObject dd = webModule.getDeploymentDescriptor();
            WebApp ddRoot = DDProvider.getDefault().getDDRoot(dd);
            if (ddRoot != null){
                try{
                    Servlet servlet = (Servlet)ddRoot.createBean("Servlet"); //NOI18N
                    String servletName = panel == null ? "Faces Servlet" : panel.getServletName();
                    servlet.setServletName(servletName); //NOI18N
                    servlet.setServletClass("javax.faces.webapp.FacesServlet"); //NOI18N
                    ddRoot.addServlet(servlet);
                    
                    servlet.setLoadOnStartup(new BigInteger("1"));//NOI18N
                    
                    
                    ServletMapping mapping = (ServletMapping)ddRoot.createBean("ServletMapping"); //NOI18N
                    mapping.setServletName(servletName);//NOI18N
                    mapping.setUrlPattern(panel == null ? "/faces/*" : panel.getURLPattern());//NOI18N
                    
                    ddRoot.addServletMapping(mapping);
                    
                    InitParam contextParam = (InitParam)ddRoot.createBean("InitParam"); //NOI18N
                    contextParam.setParamName("com.sun.faces.verifyObjects");  //NOI18N
                    if (panel != null && panel.verifyObjects())
                        contextParam.setParamValue("true");  //NOI18N
                    else
                        contextParam.setParamValue("false");  //NOI18N
                    ddRoot.addContextParam(contextParam);
                    
                    contextParam = (InitParam)ddRoot.createBean("InitParam"); //NOI18N
                    contextParam.setParamName("com.sun.faces.validateXml"); //NOI18N
                    if(panel == null || panel.validateXML())
                        contextParam.setParamValue("true"); //NOI18N
                    else
                        contextParam.setParamValue("false"); //NOI18N
                    ddRoot.addContextParam(contextParam);
                    
                    contextParam = (InitParam)ddRoot.createBean("InitParam"); //NOI18N
                    contextParam.setParamName("javax.faces.STATE_SAVING_METHOD"); //NOI18N
                    contextParam.setParamValue("client"); //NOI18N
                    ddRoot.addContextParam(contextParam);
                    
                    if (isMyFaces) {
                        Listener facesListener = (Listener) ddRoot.createBean("Listener");
                        facesListener.setListenerClass("org.apache.myfaces.webapp.StartupServletContextListener");
                        ddRoot.addListener(facesListener);
                    }
                    ddRoot.write(dd);
                    
                    
                } catch (ClassNotFoundException cnfe){
                    ErrorManager.getDefault().notify(cnfe);
                }
            }
            
            // copy faces-config.xml
            if (canCreateNewFile(webModule.getWebInf(),"faces-config.xml")){
                String facesConfigTemplate = "faces-config.xml"; //NOI18N
                if (ddRoot != null) {
                    if (WebApp.VERSION_2_5.equals(ddRoot.getVersion())) {
                        facesConfigTemplate = "faces-config_1_2.xml"; //NOI18N
                    }
                }
                String content = readResource(Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-modules-web-jsf/" + facesConfigTemplate).getInputStream(), "UTF-8"); //NOI18N
                FileObject target = FileUtil.createData(webModule.getWebInf(), "faces-config.xml");//NOI18N
                createFile(target, content, "UTF-8"); //NOI18N
            }
            
            //copy Welcome.jsp
            if (canCreateNewFile(webModule.getDocumentBase(), "welcomeJSF.jsp")){
                String content = readResource(Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-modules-web-jsf/welcomeJSF.jsp").getInputStream(), "UTF-8"); //NOI18N
                FileObject target = FileUtil.createData(webModule.getDocumentBase(), "welcomeJSF.jsp");//NOI18N
                createFile(target, content, "UTF-8");  //NOI18N
                
                FileObject documentBase = webModule.getDocumentBase();
                FileObject indexjsp = documentBase.getFileObject("index.jsp"); //NOI18N
                if (indexjsp != null){
                    changeIndexJSP(indexjsp);
                }
            }
            
        }
        
        private boolean canCreateNewFile(FileObject parent, String name){
            File fileToBe = new File(FileUtil.toFile(parent), name);
            boolean create = true;
            if (fileToBe.exists()){
                DialogDescriptor dialog = new DialogDescriptor(
                        NbBundle.getMessage(JSFFrameworkProvider.class, "MSG_OverwriteFile", fileToBe.getAbsolutePath()),
                        NbBundle.getMessage(JSFFrameworkProvider.class, "TTL_OverwriteFile"),
                        true, DialogDescriptor.YES_NO_OPTION, DialogDescriptor.NO_OPTION, null);
                java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
                d.setVisible(true);
                create = (dialog.getValue() == org.openide.DialogDescriptor.NO_OPTION);
            }
            return create;
        }
        
        /** Changes the index.jsp file. Only when there is <h1>JSP Page</h1> string.
         */
        private void changeIndexJSP(FileObject indexjsp) throws IOException {
            
            String content = readResource(indexjsp.getInputStream(), "UTF-8"); //NO18N
            
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
                replace.append(ConfigurationUtils.translateURI(panel == null ? "/faces/*" : panel.getURLPattern(),"/welcomeJSF.jsp")); //NOI18N
                replace.append("\">");                              //NOI18N
                replace.append(NbBundle.getMessage(JSFFrameworkProvider.class,"LBL_JSF_WELCOME_PAGE"));
                replace.append("</a>");                             //NOI18N
                content = content.replaceFirst(find, new String(replace.toString().getBytes("UTF8"), "UTF-8")); //NOI18N
                createFile(indexjsp, content, "UTF-8"); //NOI18N
            }
        }
    }
    
}

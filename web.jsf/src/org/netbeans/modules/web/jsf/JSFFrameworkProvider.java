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
import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;

import org.netbeans.modules.j2ee.dd.api.web.*;
import org.netbeans.modules.web.jsf.wizards.JSFConfigurationPanel;
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

    public Set extend (WebModule wm) {
        FileObject fo = wm.getDocumentBase();;
        Project project = FileOwnerQuery.getOwner(fo);
        
        Library jsfLibrary = LibraryManager.getDefault().getLibrary("jsf");
        if (jsfLibrary != null) {
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
            } else {
//                ErrorManager.getDefault().log ("WebProjectClassPathExtender not found in the project lookup of project: "+project.getProjectDirectory().getPath());    //NOI18N
            }

            try {
                FileSystem fs = wm.getWebInf().getFileSystem();
                fs.runAtomicAction(new CreateFacesConfig(wm));
            } catch (FileNotFoundException exc) {
                return null;
            } catch (IOException exc) {
                return null;
            }
        }
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
        
        FileObject[] filesFO = JSFConfigUtilities.getConfiFilesFO(wm.getDeploymentDescriptor());
        File[] files = new File[filesFO.length];
        for (int i = 0; i < filesFO.length; i++)
            files[i] = FileUtil.toFile(filesFO[i]);
        if (files.length > 0)
            return files;
        return null;
    }

    public FrameworkConfigurationPanel getConfigurationPanel(WebModule wm) {
        boolean defaultValue = (wm == null || !isInWebModule(wm));
        panel = new JSFConfigurationPanel(!defaultValue);
        if (!defaultValue){
            // get configuration panel with values from the wm
            Servlet servlet = JSFConfigUtilities.getActionServlet(wm.getDeploymentDescriptor());
            panel.setServletName(servlet.getServletName());
            panel.setURLPattern(JSFConfigUtilities.getActionServletMapping(wm.getDeploymentDescriptor()));
            panel.setValidateXML(JSFConfigUtilities.validateXML(wm.getDeploymentDescriptor()));
            panel.setVerifyObjects(JSFConfigUtilities.verifyObjects(wm.getDeploymentDescriptor()));
        }
        
        return panel;
    }

    public boolean isInWebModule(org.netbeans.modules.web.api.webmodule.WebModule wm) {
        return JSFConfigUtilities.getActionServlet(wm.getDeploymentDescriptor()) == null ? false : true;
    }
    
    
    private class  CreateFacesConfig implements FileSystem.AtomicAction{
        WebModule wm;
        public CreateFacesConfig (WebModule wm){
            this.wm = wm;
        }
        
        public void run() throws IOException {
            
            // copy struts-config.xml
            String content = readResource (Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-modules-web-jsf/faces-config.xml").getInputStream (), "UTF-8"); //NOI18N
            FileObject target = FileUtil.createData(wm.getWebInf(), "faces-config.xml");//NOI18N
            createFile(target, content, "UTF-8"); //NOI18N
            //copy Welcome.jsp
            content = readResource (Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-modules-web-jsf/welcomeJSF.jsp").getInputStream (), "UTF-8"); //NOI18N
            target = FileUtil.createData(wm.getDocumentBase(), "welcomeJSF.jsp");//NOI18N
            createFile(target, content, "UTF-8");  //NOI18N
            
            // Enter servlet into the deployment descriptor
            FileObject dd = wm.getDeploymentDescriptor();
            WebApp ddRoot = DDProvider.getDefault().getDDRootCopy(dd);
            if (ddRoot != null){
                try{
                    Servlet servlet = (Servlet)ddRoot.createBean("Servlet"); //NOI18N
                    servlet.setServletName(panel.getServletName()); //NOI18N
                    servlet.setServletClass("javax.faces.webapp.FacesServlet"); //NOI18N    
                    ddRoot.addServlet(servlet);

                    servlet.setLoadOnStartup(new BigInteger("1"));//NOI18N


                    ServletMapping mapping = (ServletMapping)ddRoot.createBean("ServletMapping"); //NOI18N
                    mapping.setServletName(panel.getServletName());//NOI18N
                    mapping.setUrlPattern(panel.getURLPattern());//NOI18N

                    ddRoot.addServletMapping(mapping);

                    InitParam contextParam = (InitParam)ddRoot.createBean("InitParam"); //NOI18N
                    contextParam.setParamName("com.sun.faces.verifyObjects");  //NOI18N
                    if (panel.verifyObjects())
                        contextParam.setParamValue("true");  //NOI18N
                    else
                        contextParam.setParamValue("false");  //NOI18N
                    ddRoot.addContextParam(contextParam);

                    contextParam = (InitParam)ddRoot.createBean("InitParam"); //NOI18N
                    contextParam.setParamName("com.sun.faces.validateXml"); //NOI18N
                    if(panel.validateXML())
                        contextParam.setParamValue("true"); //NOI18N
                    else
                        contextParam.setParamValue("false"); //NOI18N
                    ddRoot.addContextParam(contextParam);
                    
                    // adding configuration file deffinition
                    contextParam = (InitParam)ddRoot.createBean("InitParam"); //NOI18N
                    contextParam.setParamName("javax.faces.CONFIG_FILES"); //NOI18N
                    contextParam.setParamValue("/WEB-INF/faces-config.xml"); //NOI18N
                    ddRoot.addContextParam(contextParam);
                    
                    contextParam = (InitParam)ddRoot.createBean("InitParam"); //NOI18N
                    contextParam.setParamName("javax.faces.STATE_SAVING_METHOD"); //NOI18N
                    contextParam.setParamValue("client"); //NOI18N
                    ddRoot.addContextParam(contextParam);
                    
                    ddRoot.write(dd);
                    
                    
                }
                catch (ClassNotFoundException cnfe){
                    ErrorManager.getDefault().notify(cnfe);
                }
            }
            
            FileObject documentBase = wm.getDocumentBase();
            FileObject indexjsp = documentBase.getFileObject("index.jsp"); //NOI18N
            if (indexjsp != null){
                changeIndexJSP(indexjsp);
            }

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
                replace.append(JSFConfigUtilities.getActionAsResource(panel.getURLPattern(),"/welcomeJSF.jsp")); //NOI18N
                replace.append("\">");                              //NOI18N
                replace.append(NbBundle.getMessage(JSFFrameworkProvider.class,"LBL_JSF_WELCOME_PAGE"));
                replace.append("</a>");                             //NOI18N
                content = content.replaceFirst(find, new String (replace.toString().getBytes("UTF8"), "UTF-8")); //NOI18N
                createFile(indexjsp, content, "UTF-8"); //NOI18N
            }
        }
    }

}

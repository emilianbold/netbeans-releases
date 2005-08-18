/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.jsf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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

/**
 *
 * @author Petr Pisl
 */
public class JSFFrameworkProvider extends WebFrameworkProvider {
    
    private JSFConfigurationPanel panel;
    /** Creates a new instance of JSFFrameworkProvider */
    public JSFFrameworkProvider() {
        super("Java Server Faces", "Java Server Faces");   //NOI18N
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
    
    private static String readResource(InputStream is) throws IOException {
        // read the config from resource first
        StringBuffer sb = new StringBuffer();
        String lineSep = System.getProperty("line.separator");//NOI18N
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
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
        //dummy implementation, it MUST be reimplemented
        return wm.getDocumentBase().getFileObject("welcomeJSF.jsp") == null ? false : true;
    }
    
    
    private class  CreateFacesConfig implements FileSystem.AtomicAction{
        WebModule wm;
        public CreateFacesConfig (WebModule wm){
            this.wm = wm;
        }
        
        public void run() throws IOException {
            
            // copy struts-config.xml
            String content = readResource (Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-modules-web-jsf/faces-config.xml").getInputStream ()); //NOI18N
            FileObject target = FileUtil.createData(wm.getWebInf(), "faces-config.xml");//NOI18N
            createFile(target, content);
            //copy Welcome.jsp
            content = readResource (Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-modules-web-jsf/welcomeJSF.jsp").getInputStream ()); //NOI18N
            target = FileUtil.createData(wm.getDocumentBase(), "welcomeJSF.jsp");//NOI18N
            createFile(target, content);
            
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

                    InitParam contextParam = (InitParam)servlet.createBean("InitParam"); //NOI18N
                    contextParam.setParamName("com.sun.faces.verifyObjects");  //NOI18N
                    if (panel.verifyObjects())
                        contextParam.setParamValue("true");  //NOI18N
                    else
                        contextParam.setParamValue("false");  //NOI18N
                    ddRoot.addContextParam(contextParam);

                    contextParam = (InitParam)servlet.createBean("InitParam"); //NOI18N
                    contextParam.setParamName("com.sun.faces.validateXml"); //NOI18N
                    if(panel.validateXML())
                        contextParam.setParamValue("true"); //NOI18N
                    else
                        contextParam.setParamValue("false"); //NOI18N
                    ddRoot.addContextParam(contextParam);

                    WelcomeFileList wfl = ddRoot.getSingleWelcomeFileList();
                    wfl.addWelcomeFile("welcomeJSF.jsp");  //NOI18N
                    for (int i = wfl.sizeWelcomeFile()-1;  i > 0; i-- ){
                        wfl.setWelcomeFile(i, wfl.getWelcomeFile(i-1));
                    }
                    wfl.setWelcomeFile(0, "jsf/welcomeJSF.jsp");
                    ddRoot.write(dd);
                }
                catch (ClassNotFoundException cnfe){
                    ErrorManager.getDefault().notify(cnfe);
                }
            }

        }
        
        private void createFile(FileObject target, String content) throws IOException{            
            FileLock lock = target.lock();
            try {
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(target.getOutputStream(lock)));
                bw.write(content);
                bw.close();

            }
            finally {
                lock.releaseLock();
            }
        }
    }

}

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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.web.jsf.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModelFactory;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class ConfigurationUtils {
    
    /**
     * This methods returns the model source for the faces config file.
     * @param confFile - the faces config file
     * @param editable - if the source will be editable. Clients should use true. 
     * @return The ModelSource for the configuration file. If the file is not faces config file
     * or a version which is not handled, then returns null. 
     */
    public static JSFConfigModel getConfigModel(FileObject confFile, boolean editable){
        try     {
            ModelSource modelSource = Utilities.createModelSource(confFile,editable);
            JSFConfigModel configModel = JSFConfigModelFactory.getInstance().getModel(modelSource);
            
            return configModel;
        } catch (CatalogModelException ex) {
            java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                    ex.getMessage(), ex);
        }
        return null;
    }
    
    /**
     * The methods finds the definition of the Faces Servlet in the deployment descriptor
     * of the given web module.
     * @param webModule the given web module, where the Faces Servlet is.
     * @return Faces Servlet definition or null if the Faces Servlet definition is not 
     * found in the given web module.
     */
    public static Servlet getFacesServlet(WebModule webModule) {
        FileObject deploymentDescriptor = webModule.getDeploymentDescriptor();
        if (deploymentDescriptor == null) {
            return null;
        }
        try {
            WebApp webApp = DDProvider.getDefault().getDDRoot(deploymentDescriptor);
            
            // Try to find according the servlet class name. The javax.faces.webapp.FacesServlet is final, so
            // it can not be extended.
            return (Servlet) webApp
                    .findBeanByName("Servlet", "ServletClass", "javax.faces.webapp.FacesServlet"); //NOI18N;
        } catch (java.io.IOException e) {
            return null;
        }
    }
    
    /** Returns the mapping for the Faces Servlet.
     * @param webModule web module, where the JSF framework should be defined
     * @return The maping for the faces servlet. Null if the web module doesn't
     * contains definition of faces servlet.
     */
    public static String getFacesServletMapping(WebModule webModule){
        FileObject deploymentDescriptor = webModule.getDeploymentDescriptor();
        Servlet servlet = getFacesServlet(webModule);
        if (servlet != null){
            try{
                WebApp webApp = DDProvider.getDefault().getDDRoot(deploymentDescriptor);
                ServletMapping[] mappings = webApp.getServletMapping();
                for (int i = 0; i < mappings.length; i++){
                    if (mappings[i].getServletName().equals(servlet.getServletName()))
                        return mappings[i].getUrlPattern();
                }
            } catch (java.io.IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        return null;
    }
    
    /**
     * The method returns all faces configuration files in the web module
     * @param webModule - the web module, where you want to find the faces
     * configuration files
     * @return array of all faces configuration files. If there are not any 
     * configuration file, then empty array is returned.
     **/
    
    public static FileObject[] getFacesConfigFiles(WebModule webModule){
        String[] sFiles = JSFConfigUtilities.getConfigFiles(webModule.getDeploymentDescriptor());
        if (sFiles.length > 0){
            FileObject documentBase = webModule.getDocumentBase();
            ArrayList files = new ArrayList();
            FileObject file;
            for (int i = 0; i < sFiles.length; i++){
                file = documentBase.getFileObject(sFiles[i]);
                if (file != null)
                    files.add(file);
            }
            return (FileObject[])files.toArray(new FileObject[files.size()]);
        }
        return new FileObject [0];
    }
    
    /**
     * Translates an URI to be executed with faces serlvet with the given mapping.
     * For example, the servlet has mapping <i>*.jsf</i> then uri <i>/hello.jps</i> will be
     * translated to <i>/hello.jsf</i>. In the case where the mapping is <i>/faces/*</i>
     * will be translated to <i>/faces/hello.jsp<i>.
     *
     * @param mapping The servlet mapping
     * @param uri The original URI
     * @return The translated URI
     */
    public static String translateURI(String mapping, String uri){
        String resource = "";
        if (mapping != null && mapping.length()>0){
            if (mapping.startsWith("*.")){
                if (uri.indexOf('.') > 0)
                    resource = uri.substring(0, uri.lastIndexOf('.'))+mapping.substring(1);
                else
                    resource = uri + mapping.substring(1);
            } else
                if (mapping.endsWith("/*"))
                    resource = mapping.substring(0,mapping.length()-2) + uri;
        }
        return resource;
    }
    
    /**
     * Helper method which finds the faces configuration file, where is the managed bean
     * defined.
     * @param webModule the web module, wher the managed bean is defined.
     * @param name Name of the managed bean. 
     * @return faces configuration file, where the managed bean is defined. Null, if a bean
     * with the given name is not defined in the web module.
     */
    public static FileObject findFacesConfigForManagedBean(WebModule webModule, String name){
        FileObject[] configs = ConfigurationUtils.getFacesConfigFiles(webModule);
        
        
        for (int i = 0; i < configs.length; i++) {
            //DataObject dObject = DataObject.find(configs[i]);
            FacesConfig facesConfig = getConfigModel(configs[i], true).getRootComponent();
            Collection<ManagedBean>beans = facesConfig.getManagedBeans();
            for (Iterator<ManagedBean> it = beans.iterator(); it.hasNext();) {
                ManagedBean managedBean = it.next();
                if(name.equals(managedBean.getManagedBeanName()))
                    return configs[i];
            }
            
        }
        return null;
    }
    
        
}

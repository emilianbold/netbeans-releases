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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.jsf.config.model.*;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author petr
 */
public class JSFConfigUtilities {
    
    public static List getAllNavigationRules(JSFConfigDataObject data){
        ArrayList list = new ArrayList();
        try{
            FacesConfig config = data.getFacesConfig();
            NavigationRule [] rules = config.getNavigationRule();
            if (rules != null)
                for (int i = 0; i < rules.length; i++)
                    list.add(rules[i]);
        } catch (java.io.IOException e){
            ErrorManager.getDefault().notify(e);
        }
        return list;
    }
    
    public static List getAllManagedBeans(JSFConfigDataObject data){
        ArrayList list = new ArrayList();
        try{
            FacesConfig config = data.getFacesConfig();
            ManagedBean [] beans = config.getManagedBean();
            for (int i = 0; i < beans.length; i++)
                list.add(beans[i]);
        } catch (java.io.IOException e){
            ErrorManager.getDefault().notify(e);
        }
        return list;
    }
    
    public static NavigationRule findNavigationRule(JSFConfigDataObject data, String fromView){
        try {
            return findNavigationRule(data.getFacesConfig(), fromView);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return null;
    }
    
    /** Returns the navigation rule, where the FromViewID is the parameter. If the rule doesn't exist
     * then returns null.
     */
    public static NavigationRule findNavigationRule(FacesConfig config, String fromView){
        if (fromView != null){
            NavigationRule [] rules = config.getNavigationRule();
            for (int i = 0; i < rules.length; i++)
                if (fromView.equals(rules[i].getFromViewId()))
                    return rules[i];
        }
        return null;
    }
    
    /** Returns WebPages for the project, where the fo is located.
     */
    public static SourceGroup[] getDocBaseGroups(FileObject fo) throws java.io.IOException {
        Project proj = FileOwnerQuery.getOwner(fo);
        if (proj==null) return new SourceGroup[]{};
        Sources sources = ProjectUtils.getSources(proj);
        return sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
    }
    
    public static String getResourcePath(SourceGroup[] groups, FileObject fo, char separator, boolean withExt) {
        for (int i=0;i<groups.length;i++) {
            FileObject root = groups[i].getRootFolder();
            if (FileUtil.isParentOf(root,fo)) {
                String relativePath = FileUtil.getRelativePath(root,fo);
                if (relativePath!=null) {
                    if (separator!='/') relativePath = relativePath.replace('/',separator);
                    if (!withExt) {
                        int index = relativePath.lastIndexOf((int)'.');
                        if (index>0) relativePath = relativePath.substring(0,index);
                    }
                    return relativePath;
                } else {
                    return "";
                }
            }
        }
        return "";
    }
    
    public static Servlet getActionServlet(FileObject dd) {
        if (dd == null) {
            return null;
        }
        try {
            WebApp webApp = DDProvider.getDefault().getDDRoot(dd);
            
            // Try to find according the servlet class name. The javax.faces.webapp.FacesServlet is final, so
            // it can not be extended.
            return (Servlet) webApp
                    .findBeanByName("Servlet", "ServletClass", "javax.faces.webapp.FacesServlet"); //NOI18N;
        } catch (java.io.IOException e) {
            return null;
        }
    }
    
    /** Returns the mapping for the Faces Servlet.
     */
    public static String getActionServletMapping(FileObject dd){
        Servlet servlet = getActionServlet(dd);
        if (servlet != null){
            try{
                WebApp webApp = DDProvider.getDefault().getDDRoot(dd);
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
    
    public static boolean validateXML(FileObject dd){
        boolean value = false;  // the default value of the com.sun.faces.validateXml
        if (dd != null){
            try{
                WebApp webApp = DDProvider.getDefault().getDDRoot(dd);
                InitParam param = null;
                if (webApp != null)
                    param = (InitParam)webApp.findBeanByName("InitParam", "ParamName", "com.sun.faces.validateXml"); //NOI18N
                if (param != null)
                    value =   "true".equals(param.getParamValue().trim()); //NOI18N
            } catch (java.io.IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        return value;
    }
    
    public static boolean verifyObjects(FileObject dd){
        boolean value = false; // the default value of the com.sun.faces.verifyObjects
        if (dd != null){
            try{
                WebApp webApp = DDProvider.getDefault().getDDRoot(dd);
                InitParam param = null;
                if (webApp != null)
                    param = (InitParam)webApp.findBeanByName("InitParam", "ParamName", "com.sun.faces.verifyObjects"); //NOI18N
                if (param != null)
                    value = "true".equals(param.getParamValue().trim());
            } catch (java.io.IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        return value;
    }
    
    /** Returns relative path for all jsf configuration files in the web module. If there is no
     *  configuration file, then returns String array with lenght = 0.
     */
    public static String[] getConfigFiles(FileObject dd){
        if (dd != null){
            InitParam param = null;
            try{
                WebApp webApp = DDProvider.getDefault().getDDRoot(dd);
                if (webApp != null)
                    param = (InitParam)webApp.findBeanByName("InitParam", "ParamName", "javax.faces.CONFIG_FILES"); //NOI18N
            } catch (java.io.IOException e) {
                ErrorManager.getDefault().notify(e);
            }
            
            if (param != null){
                // the configuration files are defined
                String value = param.getParamValue().trim();
                if (value != null){
                    String[] files = value.split(",");
                    for (int i = 0; i < files.length; i++)
                        files[i] = files[i].trim();
                    return  files;
                }
            } else{
                // the configguration files are not defined -> looking for WEB-INF/faces-config.xml
                WebModule wm = WebModule.getWebModule(dd);
                FileObject baseDir = wm.getDocumentBase();
                FileObject fo = baseDir.getFileObject("WEB-INF/faces-config.xml");
                if (fo != null)
                    return new String[]{"WEB-INF/faces-config.xml"};
            }
        }
        return new String[]{};
    }
    
    public static FileObject[] getConfiFilesFO(FileObject dd){
        String[] sFiles = getConfigFiles(dd);
        if (sFiles.length > 0){
            WebModule wm = WebModule.getWebModule(dd);
            FileObject documentBase = wm.getDocumentBase();
            FileObject config;
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
     * will be translated to <i>/faces/hello.js<i>.
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
            }
            else
                if (mapping.endsWith("/*"))
                    resource = mapping.substring(0,mapping.length()-2) + uri;
        }
        return resource;
    }
    
    public static FileObject findFacesConfigForManagedBean(WebModule wm, String name){
        FileObject[] configs = getConfiFilesFO(wm.getDeploymentDescriptor());
        
        try {
            for (int i = 0; i < configs.length; i++) {
                DataObject dObject = DataObject.find(configs[i]);
                if (dObject instanceof JSFConfigDataObject){
                    ManagedBean [] beans = ((JSFConfigDataObject)dObject).getFacesConfig().getManagedBean();
                    for (int j = 0; j < beans.length; j++) {
                        if (beans[j].getManagedBeanName().equals(name))
                            return configs[i];
                    }
                }
            }
        } catch (DataObjectNotFoundException e) {
            ErrorManager.getDefault().notify(e);
        } catch (java.io.IOException e){
            ErrorManager.getDefault().notify(e);
        }
        return null;
    }
}

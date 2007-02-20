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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;

/**
 *
 * @author petr
 */
public class JSFConfigUtilities {
    
    
    
    
    public static NavigationRule findNavigationRule(JSFConfigDataObject data, String fromView){
        NavigationRule navigationRule = null;
        FacesConfig config = ConfigurationUtils.getConfigModel(data.getPrimaryFile(), true).getRootComponent();
        Collection<NavigationRule> rules = config.getNavigationRules();
        for (Iterator<NavigationRule> it = rules.iterator(); it.hasNext();) {
            NavigationRule nRule = it.next();
            if (fromView.equals(nRule.getFromViewId())){
                navigationRule = nRule;
                continue;
            }
        }
        return navigationRule;
    }
    
    /** Returns the navigation rule, where the FromViewID is the parameter. If the rule doesn't exist
     * then returns null.
     */
    //    public static NavigationRule findNavigationRule(FacesConfig config, String fromView){
    //        if (fromView != null){
    //            FacesConfig config = getConfigModel(data.getPrimaryFile(), true).getRootComponent();
    //            NavigationRule [] rules = config.getNavigationRule();
    //            for (int i = 0; i < rules.length; i++)
    //                if (fromView.equals(rules[i].getFromViewId()))
    //                    return rules[i];
    //        }
    //        return null;
    //    }
    
    /** Returns WebPages for the project, where the fo is located.
     */
    public static SourceGroup[] getDocBaseGroups(FileObject fileObject) throws java.io.IOException {
        Project proj = FileOwnerQuery.getOwner(fileObject);
        if (proj==null) return new SourceGroup[]{};
        Sources sources = ProjectUtils.getSources(proj);
        return sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
    }
    
    public static String getResourcePath(SourceGroup[] groups,FileObject fileObject, char separator, boolean withExt) {
        for (int i=0;i<groups.length;i++) {
            FileObject root = groups[i].getRootFolder();
            if (FileUtil.isParentOf(root,fileObject)) {
                String relativePath = FileUtil.getRelativePath(root,fileObject);
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
    
    
    
    
    
    public static boolean validateXML(FileObject deploymentDesc){
        boolean value = false;  // the default value of the com.sun.faces.validateXml
        if (deploymentDesc != null){
            try{
                WebApp webApp = DDProvider.getDefault().getDDRoot(deploymentDesc);
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
    
    public static boolean verifyObjects(FileObject deploymentDesc){
        boolean value = false; // the default value of the com.sun.faces.verifyObjects
        if (deploymentDesc != null){
            try{
                WebApp webApp = DDProvider.getDefault().getDDRoot(deploymentDesc);
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
    public static String[] getConfigFiles(FileObject deploymentDesc){
        ArrayList<String> files = new ArrayList();
        String[]  filesURI;
        if (deploymentDesc != null){
            InitParam param = null;
            try{
                WebApp webApp = DDProvider.getDefault().getDDRoot(deploymentDesc);
                if (webApp != null)
                    param = (InitParam)webApp.findBeanByName("InitParam", "ParamName", "javax.faces.CONFIG_FILES"); //NOI18N
            } catch (java.io.IOException e) {
                ErrorManager.getDefault().notify(e);
            }
            
            if (param != null){
                // the configuration files are defined
                String value = param.getParamValue().trim();
                if (value != null){
                    filesURI = value.split(",");
                    for (int i = 0; i < filesURI.length; i++)
                        files.add(filesURI[i].trim());
                }
            } 
            // looking for WEB-INF/faces-config.xml
            WebModule webModule = WebModule.getWebModule(deploymentDesc);
            FileObject baseDir = webModule.getDocumentBase();
            FileObject fileObject = baseDir.getFileObject("WEB-INF/faces-config.xml");
            if (fileObject != null)
                files.add("WEB-INF/faces-config.xml");
        }
        filesURI = new String[files.size()];
        return files.toArray(filesURI);
    }
}

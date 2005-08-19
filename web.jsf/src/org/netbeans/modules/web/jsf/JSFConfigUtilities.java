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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.jsf.config.model.*;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;

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
            for (int i = 0; i < rules.length; i++)
                list.add(rules[i]);
        }
        catch (java.io.IOException e){
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
        }
        catch (java.io.IOException e){
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
          NavigationRule [] rules = config.getNavigationRule();
            for (int i = 0; i < rules.length; i++)
                if (rules[i].getFromViewId().equals(fromView))
                    return rules[i];
        return null;
    }
    
    /** Returns WebPages for the project, where the fo is located.
     */
    public static SourceGroup[] getDocBaseGroups(FileObject fo) throws java.io.IOException {
        Project proj = FileOwnerQuery.getOwner(fo);
        if (proj==null) return new SourceGroup[]{};
        Sources sources = (Sources)proj.getLookup().lookup(Sources.class);
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
    
    public static Servlet getActionServlet(FileObject dd){
        // PENDING - must be more declarative.
        try{
            WebApp webApp = DDProvider.getDefault().getDDRoot(dd);
            return (Servlet)webApp.findBeanByName("Servlet","ServletName","Faces Servlet"); //NOI18N
        } catch (java.io.IOException e) {
            return null;
        }
    }
    
    /** Returns the mapping for the Struts Action Servlet.
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
                
            }
        }
        return null;
    }
    
    public static boolean validateXML (FileObject dd){
        try{
            WebApp webApp = DDProvider.getDefault().getDDRoot(dd);
            InitParam param = (InitParam)webApp.findBeanByName("InitParam", "ParamName", "com.sun.faces.validateXml"); //NOI18N
            return  "true".equals(param.getParamValue().trim());
        } catch (java.io.IOException e) {

        }
        return false;
    }
    
    public static boolean verifyObjects (FileObject dd){
        try{
            WebApp webApp = DDProvider.getDefault().getDDRoot(dd);
            InitParam param = (InitParam)webApp.findBeanByName("InitParam", "ParamName", "com.sun.faces.verifyObjects"); //NOI18N
            return  "true".equals(param.getParamValue().trim());
        } catch (java.io.IOException e) {

        }
        return false;
    }
}

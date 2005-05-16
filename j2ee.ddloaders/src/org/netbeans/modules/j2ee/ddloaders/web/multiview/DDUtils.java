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

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import org.netbeans.modules.j2ee.dd.api.web.*;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.api.project.*;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author mkuchtiak
 */
public class DDUtils {
    
    public static String[] getUrlPatterns(WebApp webApp, Servlet servlet) {
        if (servlet.getServletName()==null) return new String[]{};
        ServletMapping[] mapping = webApp.getServletMapping();
        java.util.List maps = new java.util.ArrayList();
        for (int i=0;i<mapping.length;i++) {
            if (servlet.getServletName().equals(mapping[i].getServletName())) {
            String urlPattern = mapping[i].getUrlPattern();
            if (urlPattern!=null) maps.add(urlPattern);
            }
        }
        String[] urlPatterns = new String[maps.size()];
        maps.toArray(urlPatterns);
        return urlPatterns;
    }
    
    public static String[] getUrlPatterns(WebApp webApp, Filter filter) {
        if (filter.getFilterName()==null) return new String[]{};
        FilterMapping[] mapping = webApp.getFilterMapping();
        java.util.List maps = new java.util.ArrayList();
        for (int i=0;i<mapping.length;i++) {
            if (filter.getFilterName().equals(mapping[i].getFilterName())) {
                String urlPattern = mapping[i].getUrlPattern();
                if (urlPattern!=null) maps.add(urlPattern);
                else {
                    String servletName = mapping[i].getServletName();
                    if (servletName!=null) maps.add(servletName);
                }
            }
        }
        String[] urlPatterns = new String[maps.size()];
        maps.toArray(urlPatterns);
        return urlPatterns;
    }

    public static String[] getStringArray(String text) {
        java.util.StringTokenizer tok = new java.util.StringTokenizer(text,",");
        java.util.Set set = new java.util.HashSet();
        while (tok.hasMoreTokens()) {
            String token = tok.nextToken().trim();
            if (token.length()>0) set.add(token);
        }
        String[] stringArray = new String[set.size()];
        set.toArray(stringArray);
        return stringArray;
    }
    
    public static boolean isServletMapping(WebApp webApp, String urlPattern) {
        return webApp.findBeanByName("ServletMapping","UrlPattern",urlPattern)!=null;
    }
    
    public static boolean isServletMapping(WebApp webApp, Servlet servlet, String urlPattern) {
        ServletMapping[] maps = webApp.getServletMapping();
        String servletName = servlet.getServletName();
        if (servletName!=null) {
            for (int i=0;i<maps.length;i++) {
                if (urlPattern.equals(maps[i].getUrlPattern()) && !servletName.equals(maps[i].getServletName()) ) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static String urlPatternList(String[] urlPatterns) {
        if (urlPatterns==null) return "";
        StringBuffer buf = new StringBuffer();
        for (int i=0;i<urlPatterns.length;i++) {
            if (i>0) buf.append(", "); //NOI18N
            buf.append(urlPatterns[i]);
        }
        return buf.toString();
    }
    
    public static void addServletMappings(WebApp webApp, Servlet servlet, String[] urlPatterns) {
        String servletName = servlet.getServletName();
        try {
            for (int i=0;i<urlPatterns.length;i++) {
                ServletMapping map = (ServletMapping)webApp.createBean("ServletMapping"); //NOI18
                map.setServletName(servletName);
                map.setUrlPattern(urlPatterns[i]);
                webApp.addServletMapping(map);
            }
        } catch (ClassNotFoundException ex){}
    }
    
    public static void setServletMappings(WebApp webApp, Servlet servlet, String[] urlPatterns) {
        String servletName = servlet.getServletName();
        java.util.List oldMaps = getServletMappingList(webApp,servlet);
        java.util.List newPatterns = new java.util.ArrayList();
        // looking for old mappings
        for (int i=0;i<urlPatterns.length;i++) {
            boolean found =false;
            for (int j=0;j<oldMaps.size();j++) {
                ServletMapping oldMap = (ServletMapping)oldMaps.get(j);
                if (urlPatterns[i].equals(oldMap.getUrlPattern())) {
                    oldMaps.remove(oldMap);
                    found=true;
                    break;
                }
            }
            if (!found) newPatterns.add(urlPatterns[i]);
        }
        int min = java.lang.Math.min(oldMaps.size(),newPatterns.size());
        // replace old mappings
        for (int i=0;i<min;i++) {
            ServletMapping oldMap = (ServletMapping)oldMaps.get(i);
            oldMap.setUrlPattern((String)newPatterns.get(i));
        }
        // add new mappings
        try {
            for (int i=min;i<newPatterns.size();i++) {
                ServletMapping map = (ServletMapping)webApp.createBean("ServletMapping"); //NOI18
                map.setServletName(servletName);
                map.setUrlPattern((String)newPatterns.get(i));
                webApp.addServletMapping(map);
            }
        } catch (ClassNotFoundException ex){}
        // removing old mappings
        for (int i=min;i<oldMaps.size();i++) {
            webApp.removeServletMapping((ServletMapping)oldMaps.get(i));
        }     
    }
    
    public static ServletMapping[] getServletMappings(WebApp webApp, Servlet servlet) {
        java.util.List maps = getServletMappingList(webApp,servlet);
        ServletMapping[] newMappings = new ServletMapping[maps.size()];
        maps.toArray(newMappings);
        return newMappings;
    }
    
    public static FilterMapping[] getFilterMappings(WebApp webApp, Filter filter) {
        java.util.List maps = getFilterMappingList(webApp,filter);
        FilterMapping[] newMappings = new FilterMapping[maps.size()];
        maps.toArray(newMappings);
        return newMappings;
    }
    
    private static java.util.List getServletMappingList(WebApp webApp, Servlet servlet) {
        String servletName = servlet.getServletName();
        java.util.List maps = new java.util.ArrayList();
        if (servletName==null) return maps;
        ServletMapping[] mapping = webApp.getServletMapping();
        for (int i=0;i<mapping.length;i++) {
            if (servlet.getServletName().equals(mapping[i].getServletName())) {
                maps.add(mapping[i]);
            }
        }
        return maps;
    }
    
    private static java.util.List getFilterMappingList(WebApp webApp, Filter filter) {
        String filterName = filter.getFilterName();
        java.util.List maps = new java.util.ArrayList();
        if (filterName==null) return maps;
        FilterMapping[] mapping = webApp.getFilterMapping();
        for (int i=0;i<mapping.length;i++) {
            if (filter.getFilterName().equals(mapping[i].getFilterName())) {
                maps.add(mapping[i]);
            }
        }
        return maps;
    }
    
    public static void openEditorFor(DDDataObject dObj, String className) {
        if (className==null || className.length()==0) return;
        try {
            SourceGroup[] sourceGroups =  getJavaSourceGroups(dObj);
            String resource = className.trim().replace('.','/');
            for (int i=0;i<sourceGroups.length;i++) {
                FileObject fo = sourceGroups[i].getRootFolder();
                FileObject target = fo.getFileObject(resource+".java"); //NOI18N
                if (target!=null) {
                    DataObject javaDo = DataObject.find(target);
                    org.openide.cookies.OpenCookie cookie = 
                        (org.openide.cookies.OpenCookie)javaDo.getCookie(org.openide.cookies.OpenCookie.class);
                    if (cookie !=null) {
                        cookie.open();
                        return;
                    }
                }
            }
        } catch (java.io.IOException ex) {}
        org.openide.DialogDisplayer.getDefault().notify(new org.openide.NotifyDescriptor.Message(
            org.openide.util.NbBundle.getMessage(DDUtils.class,"MSG_sourceNotFound")));
    }
    
    public static void openEditorForSingleFile(DDDataObject dObj, String fileName) {
        if (fileName==null || fileName.length()==0) return;
        FileObject docBase = null;
        try {
            docBase = getDocumentBase(dObj);
        } catch (java.io.IOException ex) {return;}
        if (docBase!=null) {
            FileObject target = docBase.getFileObject(fileName.trim());
            if (target!=null) {
                try {
                    DataObject javaDo = DataObject.find(target);
                    org.openide.cookies.OpenCookie cookie = 
                        (org.openide.cookies.OpenCookie)javaDo.getCookie(org.openide.cookies.OpenCookie.class);
                    if (cookie !=null) {
                        cookie.open();
                        return;
                    }
                } catch (org.openide.loaders.DataObjectNotFoundException ex) {}
            }
        }
        org.openide.DialogDisplayer.getDefault().notify(new org.openide.NotifyDescriptor.Message(
            org.openide.util.NbBundle.getMessage(DDUtils.class,"MSG_sourceNotFound")));
    }

    public static void openEditorForFiles(DDDataObject dObj, java.util.StringTokenizer tok) {
        FileObject docBase = null;
        try {
            docBase = getDocumentBase(dObj);
        } catch (java.io.IOException ex) {return;}
        if (!tok.hasMoreTokens()) return;
        boolean found=false;
        if (docBase!=null)
            while (tok.hasMoreTokens()) {
                String resource = tok.nextToken().trim();
                if (resource.length()>0) {
                    FileObject target = docBase.getFileObject(resource);
                    if (target!=null) {
                        try {
                            DataObject javaDo = DataObject.find(target);
                            org.openide.cookies.OpenCookie cookie = 
                                (org.openide.cookies.OpenCookie)javaDo.getCookie(org.openide.cookies.OpenCookie.class);
                            if (cookie !=null) {
                                cookie.open();
                                found=true;
                            }
                        } catch (org.openide.loaders.DataObjectNotFoundException ex) {}
                    }
                }
            }
        if (!found) {
            org.openide.DialogDisplayer.getDefault().notify(new org.openide.NotifyDescriptor.Message(
            org.openide.util.NbBundle.getMessage(DDUtils.class,"MSG_sourcesNotFound")));
        }
    }
    
    public static SourceGroup[] getJavaSourceGroups(DDDataObject dObj) throws java.io.IOException {
        Project proj = FileOwnerQuery.getOwner(dObj.getPrimaryFile());
        if (proj==null) return new SourceGroup[]{};
        Sources sources = (Sources)proj.getLookup().lookup(Sources.class);
        return sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
    }
    
    public static SourceGroup[] getDocBaseGroups(DDDataObject dObj) throws java.io.IOException {
        Project proj = FileOwnerQuery.getOwner(dObj.getPrimaryFile());
        if (proj==null) return new SourceGroup[]{};
        Sources sources = (Sources)proj.getLookup().lookup(Sources.class);
        return sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
    }
    
    public static FileObject getDocumentBase(DDDataObject dObj) throws java.io.IOException {
        WebModule wm = WebModule.getWebModule(dObj.getPrimaryFile());
        if (wm==null) return null;
        return wm.getDocumentBase();
    }

    public static String getResourcePath(SourceGroup[] groups, FileObject fo) {
        return getResourcePath(groups, fo, '.', false);
    }
    
    public static String getResourcePath(SourceGroup[] groups, FileObject fo, char separator) {
        return getResourcePath(groups, fo, separator, false);
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
    
    public static void removeServletMappings(WebApp webApp, String servletName) {
        if (servletName==null) return;
        ServletMapping[] oldMaps = webApp.getServletMapping();
        for (int i=0;i<oldMaps.length;i++) {
            if (servletName.equals(oldMaps[i].getServletName())) {
                webApp.removeServletMapping(oldMaps[i]);
            }
        }
    }
    /** removes all filter mappings for filterName
     * @return Stack of deleetd rows
     */
    public static java.util.Stack removeFilterMappings(WebApp webApp, String filterName) {
        java.util.Stack deletedRows = new java.util.Stack();
        if (filterName==null) return deletedRows;
        FilterMapping[] oldMaps = webApp.getFilterMapping();
        for (int i=0;i<oldMaps.length;i++) {
            if (filterName.equals(oldMaps[i].getFilterName())) {
                webApp.removeFilterMapping(oldMaps[i]);
                deletedRows.push(new Integer(i));
            }
        }
        return deletedRows;
    }
    
    public static String addItem(String text, String newItem, boolean asFirst) {
        String[] stringArray = getStringArray(text);
        java.util.List list = new java.util.ArrayList();
        if (asFirst) {
            list.add(newItem);
            for (int i=0;i<stringArray.length;i++) {
                if (!newItem.equals(stringArray[i])) list.add(stringArray[i]);
            }
        } else {
            for (int i=0;i<stringArray.length;i++) {
                if (!newItem.equals(stringArray[i])) list.add(stringArray[i]);
            }
            list.add(newItem);
        }
        return getAsString(list);
    }
    
    private static String getAsString(java.util.List list) {
        StringBuffer buf = new StringBuffer();
        for (int i=0;i<list.size();i++) {
            if (i>0) buf.append(", "); //NOI18N
            buf.append((String)list.get(i));
        }
        return buf.toString();
    }
    
    public static String[] getServletNames(WebApp webApp) {
        Servlet[] allServlets = webApp.getServlet();
        java.util.List list = new java.util.ArrayList();
        for (int i=0;i<allServlets.length;i++) {
            String servletName = allServlets[i].getServletName();
            if (servletName!=null && !list.contains(allServlets[i])) list.add(servletName);
        }
        String[] names = new String[list.size()];
        list.toArray(names);
        return names;
    }
    
    public static String[] getFilterNames(WebApp webApp) {
        Filter[] filters = webApp.getFilter();
        java.util.List list = new java.util.ArrayList();
        for (int i=0;i<filters.length;i++) {
            String filterName = filters[i].getFilterName();
            if (filterName!=null && !list.contains(filters[i])) list.add(filterName);
        }
        String[] names = new String[list.size()];
        list.toArray(names);
        return names;
    }
    
}

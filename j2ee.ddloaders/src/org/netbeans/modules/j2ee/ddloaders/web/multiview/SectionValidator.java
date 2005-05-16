/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import org.netbeans.modules.xml.multiview.Error;
import org.netbeans.modules.j2ee.dd.api.web.*;
import org.openide.util.NbBundle;

/** SectionValidator.java
 *
 * Created on February 9, 2005, 4:57 PM
 * @author mkuchtiak
 */
public class SectionValidator {

    static Error validateServlets(WebApp webApp) {
        Servlet[] servlets = webApp.getServlet();
        Error error=null;
        for (int i=0;i<servlets.length;i++) {
            // valiadation for missing servlet-name
            String name = servlets[i].getServletName();
            if (name==null || name.length()==0) {
                Error.ErrorLocation loc = new Error.ErrorLocation(servlets[i],"ServletName"); //NOI18N
                error = new Error(Error.MISSING_VALUE_MESSAGE, "Servlet Name", loc);
                break;
            }
            // valiadation for missing servlet-class/jsp-file
            name = servlets[i].getServletClass();
            if (name==null) name = servlets[i].getJspFile();
            if (name==null || name.length()==0) {
                Error.ErrorLocation loc = new Error.ErrorLocation(servlets[i],"ServletClass"); //NOI18N
                error = new Error(Error.MISSING_VALUE_MESSAGE, "Servlet Class", loc);
                break;
            }
            // validation for duplicite url-patterns
            String[] urlPatterns = DDUtils.getUrlPatterns(webApp,servlets[i]);
            for (int j=0;j<urlPatterns.length;j++) {
                if (DDUtils.isServletMapping(webApp, servlets[i], urlPatterns[j])) {
                    Error.ErrorLocation loc = new Error.ErrorLocation(servlets[i],"ServletMapping"); //NOI18N
                    error = new Error(Error.DUPLICATE_VALUE_MESSAGE, urlPatterns[j] , loc);
                    break;
                }
            }
        }
        return error;
    }
    
    static Error validateFilters(WebApp webApp) {
        Filter[] filters = webApp.getFilter();
        Error error=null;
        for (int i=0;i<filters.length;i++) {
            // valiadation for missing filter-name
            String name = filters[i].getFilterName();
            if (name==null || name.length()==0) {
                Error.ErrorLocation loc = new Error.ErrorLocation(filters[i],"FilterName"); //NOI18N
                error = new Error(Error.MISSING_VALUE_MESSAGE, "Filter Name", loc);
                break;
            }
            // valiadation for missing filter-class
            name = filters[i].getFilterClass();
            if (name==null || name.length()==0) {
                Error.ErrorLocation loc = new Error.ErrorLocation(filters[i],"FilterClass"); //NOI18N
                error = new Error(Error.MISSING_VALUE_MESSAGE, "Filter Class", loc);
                break;
            }
        }
        return error;
    }
    
    static Error validatePages(WebApp webApp) {
        Error error=null;
        JspConfig jspConfig=null;
        JspPropertyGroup[] groups=null;
        try {
            jspConfig = webApp.getSingleJspConfig();
        } catch (org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException ex) {
            return null;
        }
        if (jspConfig==null) {
            return null;
        } else groups = jspConfig.getJspPropertyGroup();
        if (groups==null) return null;
        
        // validation for missing url-patern(s) 
        for (int i=0;i<groups.length;i++) {
            if (groups[i].sizeUrlPattern()==0) {
                Error.ErrorLocation loc = new Error.ErrorLocation(groups[i],"url_patterns"); //NOI18N
                error = new Error(Error.MISSING_VALUE_MESSAGE, "URL Pattern", loc);
                break;
            }
        }
        return error;
    }
    
    static String validateNewServlet(WebApp webApp, String servletName, String servletClass, String jspFile, String urlPatterns) {
        if (servletName.length()==0) {
            return NbBundle.getMessage(SectionValidator.class,"TXT_EmptyServletName");
        } else {
            Servlet[] servlets = webApp.getServlet();
            boolean exists=false;
            for (int i=0;i<servlets.length;i++) {
                if (servletName.equals(servlets[i].getServletName())) {
                    exists=true;
                    break;
                }
            }
            if (exists) {
                return NbBundle.getMessage(SectionValidator.class,"TXT_ServletNameExists",servletName);
            }
        }
        if (servletClass.length()==0 && jspFile.length()==0) {
            return NbBundle.getMessage(SectionValidator.class,"TXT_Servlet_BothMissing");
        }
        if (servletClass.length()>0 && jspFile.length()>0) {
            return NbBundle.getMessage(SectionValidator.class,"TXT_Servlet_BothSpecified");
        }
        String[] patterns = DDUtils.getStringArray(urlPatterns);
        if (patterns.length>0) {
            for (int i=0;i<patterns.length;i++) {
                if (DDUtils.isServletMapping(webApp,patterns[i])) {
                    return NbBundle.getMessage(SectionValidator.class,"TXT_UrlPatternExists",patterns[i]);
                }
            }
        }
        return null;
    }
    
    static String validateNewFilter(WebApp webApp, String filterName, String filterClass) {
        if (filterName.length()==0) {
            return NbBundle.getMessage(SectionValidator.class,"TXT_EmptyFilterName");
        } else {
            Filter[] filters = webApp.getFilter();
            boolean exists=false;
            for (int i=0;i<filters.length;i++) {
                if (filterName.equals(filters[i].getFilterName())) {
                    exists=true;
                    break;
                }
            }
            if (exists) {
                return NbBundle.getMessage(SectionValidator.class,"TXT_FilterNameExists",filterName);
            }
        }
        if (filterClass.length()==0) {
            return NbBundle.getMessage(SectionValidator.class,"TXT_EmptyFilterClass");
        }
        return null;
    }
    
    static String validateNewJspPropertyGroup(String urlPatterns) {
        String[] patterns = DDUtils.getStringArray(urlPatterns);
        if (patterns.length==0) {
            return NbBundle.getMessage(SectionValidator.class,"TXT_EmptyUrls");
        }
        return null;
    }
}

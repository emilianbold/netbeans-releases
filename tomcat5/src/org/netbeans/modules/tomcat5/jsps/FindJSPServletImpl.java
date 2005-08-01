/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tomcat5.jsps;

import java.io.File;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.api.FindJSPServlet;
import org.netbeans.modules.tomcat5.TomcatManager;
import org.netbeans.modules.tomcat5.util.TomcatProperties;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author Petr Jiricka
 */
public class FindJSPServletImpl implements FindJSPServlet {

    private static final String WEB_INF_TAGS = "WEB-INF/tags/";
    private static final String META_INF_TAGS = "META-INF/tags/";
    
    private TomcatManager tm;
    
    /** Creates a new instance of FindJSPServletImpl */
    public FindJSPServletImpl(DeploymentManager manager) {
        tm = (TomcatManager)manager;
    }
    
    
    public File getServletTempDirectory(String moduleContextPath) {
        File baseDir = tm.getTomcatProperties().getCatalinaDir();
        if ((baseDir == null) || !baseDir.exists()) {
            return null;
        }
        File hostBase = new File(baseDir, "work/Catalina/localhost".replace('/', File.separatorChar));
        File workDir = new File(hostBase, getContextRootString(moduleContextPath));
        //System.out.println("returning servlet root " + workDir);
        return workDir;
    }
    
    private String getContextRootString(String moduleContextPath) {
        String contextRootPath = moduleContextPath;
        if (contextRootPath.startsWith("/")) {
            contextRootPath = contextRootPath.substring(1);
        }
        if (contextRootPath.equals("")) {
            return "_";
        }
        else {
            return contextRootPath;
        }
    }
    
    public String getServletResourcePath(String moduleContextPath, String jspResourcePath) {
        //String path = module.getWebURL();
        
        //we expect .tag file; in other case, we expect .jsp file
        String path = getTagHandlerClassName(jspResourcePath);
        if (path != null) //.tag
            path = path.replace('.', '/') + ".java";
        else //.jsp
            path = getServletPackageName(jspResourcePath).replace('.', '/') + '/' +
                   getServletClassName(jspResourcePath) + ".java";
            
        return path;
        
        //int lastDot = jspResourcePath.lastIndexOf('.');
        //return jspResourcePath.substring(0, lastDot) + "$jsp.java"; // NOI18N
    }

    // copied from org.apache.jasper.JspCompilationContext
    public String getServletPackageName(String jspUri) {
        String dPackageName = getDerivedPackageName(jspUri);
        if (dPackageName.length() == 0) {
            return JspNameUtil.JSP_PACKAGE_NAME;
        }
        return JspNameUtil.JSP_PACKAGE_NAME + '.' + getDerivedPackageName(jspUri);
    }
    
    // copied from org.apache.jasper.JspCompilationContext
    private String getDerivedPackageName(String jspUri) {
        int iSep = jspUri.lastIndexOf('/');
        return (iSep > 0) ? JspNameUtil.makeJavaPackage(jspUri.substring(0,iSep)) : "";
    }
    
    // copied from org.apache.jasper.JspCompilationContext
    public String getServletClassName(String jspUri) {
        int iSep = jspUri.lastIndexOf('/') + 1;
        return JspNameUtil.makeJavaIdentifier(jspUri.substring(iSep));
    }
    
    public String getServletEncoding(String moduleContextPath, String jspResourcePath) {
        return "UTF8"; // NOI18N
    }

    /**
     * Copied (and slightly modified) from org.apache.jasper.compiler.JspUtil
     *
     * Gets the fully-qualified class name of the tag handler corresponding to
     * the given tag file path.
     *
     * @param path Tag file path
     *
     * @return Fully-qualified class name of the tag handler corresponding to 
     * the given tag file path
     */
    private String getTagHandlerClassName(String path) {

        String className = null;
        int begin = 0;
        int index;
        
        index = path.lastIndexOf(".tag");
        if (index == -1) {
            return null;
        }

        index = path.indexOf(WEB_INF_TAGS);
        if (index != -1) {
            className = "org.apache.jsp.tag.web.";
            begin = index + WEB_INF_TAGS.length();
        } else {
	    index = path.indexOf(META_INF_TAGS);
	    if (index != -1) {
		className = "org.apache.jsp.tag.meta.";
		begin = index + META_INF_TAGS.length();
	    } else {
		return null;
	    }
	}

        className += JspNameUtil.makeJavaPackage(path.substring(begin));
  
        return className;
    }
    
}

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
import javax.enterprise.deploy.spi.TargetModuleID;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet;
import org.netbeans.modules.tomcat5.TomcatManager;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author Petr Jiricka
 */
public class FindJSPServletImpl implements FindJSPServlet {
    
    private TomcatManager tm;
    
    /** Creates a new instance of FindJSPServletImpl */
    public FindJSPServletImpl() {
    }
    
    public File getServletTempDirectory(String moduleContextPath) {
        File baseDir = findBaseDir();
        if ((baseDir == null) || !baseDir.exists()) {
            return null;
        }
        File hostBase = new File(baseDir, "work/Catalina/localhost".replace('/', File.separatorChar));
        File workDir = new File(hostBase, getContextRootString(moduleContextPath));
        //System.out.println("returning servlet root " + workDir);
        return workDir;
    }
    
    private File findBaseDir() {
        String home = tm.getCatalinaHome();
        String base = tm.getCatalinaBase();
        if (home == null) {
            // not supported
            return null;
        }
        
        if (base == null) {
            base = home;
        }
        
        InstalledFileLocator ifl = InstalledFileLocator.getDefault();
        File baseDir = new File(base);
        if (!baseDir.isAbsolute()) {
            baseDir = ifl.locate(base, null, false);
            if (baseDir == null) {
                return null;
            }
        }
        return baseDir;
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
        return getServletPackageName(jspResourcePath).replace('.', '/') + '/' +
            getServletClassName(jspResourcePath) + ".java";
        
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
        return (iSep > 0) ? JspNameUtil.makeJavaPackage(jspUri.substring(1,iSep)) : "";
    }
    
    // copied from org.apache.jasper.JspCompilationContext
    public String getServletClassName(String jspUri) {
        int iSep = jspUri.lastIndexOf('/') + 1;
        return JspNameUtil.makeJavaIdentifier(jspUri.substring(iSep));
    }
    
    public String getServletEncoding(String moduleContextPath, String jspResourcePath) {
        return "UTF8"; // NOI18N
    }
    
    public void setDeploymentManager(DeploymentManager manager) {
        tm = (TomcatManager)manager;
    }
    
}

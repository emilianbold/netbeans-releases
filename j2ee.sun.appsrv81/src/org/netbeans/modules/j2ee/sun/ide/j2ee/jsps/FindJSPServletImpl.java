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

package org.netbeans.modules.j2ee.sun.ide.j2ee.jsps;

import java.io.File;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.api.FindJSPServlet;

import org.netbeans.modules.j2ee.sun.ide.j2ee.DeploymentManagerProperties;
import org.netbeans.modules.j2ee.sun.ide.j2ee.PluginProperties;

/**
 *
 * @author Petr Jiricka, adapted by Ludo for 8PE (using tomcat5)
 */
public class FindJSPServletImpl implements FindJSPServlet {
    
    private DeploymentManager tm;
    
    /** Creates a new instance of FindJSPServletImpl */
    public FindJSPServletImpl(DeploymentManager dm) {
        tm =dm;
    }
    
    public File getServletTempDirectory(String moduleContextPath) {
        File baseDir = findBaseDir();
        if ((baseDir == null) || !baseDir.exists()) {
            return null;
        }
        DeploymentManagerProperties dmProps = new DeploymentManagerProperties(tm);
        String domain = dmProps.getDomainName();
        if (domain==null){
            domain="domain1";
            dmProps.setDomainName(domain);
        }
        File hostBase = new File(baseDir, "domains/"+domain+"/generated/jsp/j2ee-modules".replace('/', File.separatorChar));
        File workDir = new File(hostBase, getContextRootString(moduleContextPath));
      //  System.out.println("returning servlet root " + workDir);
        return workDir;
    }
    
    private File findBaseDir() {
        String installRoot = PluginProperties.getDefault().getInstallRoot().getAbsolutePath(); //System.getProperty("com.sun.aas.installRoot"); 

 
        if (installRoot == null) {
            ///System.out.println("cannot start please specify com.sun.aas.installRoot");
            return null;
        } 
        return new File(installRoot);
    }
    
    private String getContextRootString(String moduleContextPath) {
        String contextRootPath = moduleContextPath;
        if (contextRootPath.startsWith("/")) {
            contextRootPath = contextRootPath.substring(1);
        }

            return contextRootPath;

    }
    
    public String getServletResourcePath(String moduleContextPath, String jspResourcePath) {
        //String path = module.getWebURL();
        String s= getServletPackageName(jspResourcePath).replace('.', '/') + '/' +
            getServletClassName(jspResourcePath) + ".java";
    //    System.out.println("in jsp  "+s);
        return s;
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
    
    public void setDeploymentManager(DeploymentManager manager) {
        tm = manager;
    }
    
}

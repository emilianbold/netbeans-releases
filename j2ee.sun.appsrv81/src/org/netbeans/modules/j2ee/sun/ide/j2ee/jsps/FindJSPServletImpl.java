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

package org.netbeans.modules.j2ee.sun.ide.j2ee.jsps;

import java.io.File;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.api.FindJSPServlet;

import org.netbeans.modules.j2ee.sun.ide.j2ee.DeploymentManagerProperties;

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

        DeploymentManagerProperties dmProps = new DeploymentManagerProperties(tm);
        String domain = dmProps.getDomainName();
        if (domain==null){
            domain="domain1";
            dmProps.setDomainName(domain);
        }
	String domainDir = dmProps.getLocation();
        File hostBase = new File(domainDir, "/"+domain+"/generated/jsp/j2ee-modules".replace('/', File.separatorChar));
        File workDir = new File(hostBase, getContextRootString(moduleContextPath));
      //  System.out.println("returning servlet root " + workDir);
        return workDir;
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

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core.jsploader;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;

import org.netbeans.modules.j2ee.server.ServerInstance;
import org.netbeans.modules.web.context.WebContextObject;
import org.openide.ErrorManager;

import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/** Data related to compilation attached to one JSP page.
 *  Basically a copy of the data retrieved from the compilation plugin.
 *  This data will change during the compilation process.
 *  This class is also responsible for translating File-view of compiled data to
 *  FileObject-view, including the creation of the necessary filesystems.
 *
 * @author  Petr Jiricka
 * @version 
 */
public class CompileData {

    private JspDataObject jspPage;
    private FileObject docRoot;
    private ServerInstance serverInstance;
    private String servletEncoding;
    
    private final static boolean debug = false;
    
    private File servletJavaRoot;
    private String servletResourceName;
     

    /** Creates new CompileData */
    public CompileData(JspDataObject jspPage) {
        this.jspPage = jspPage;
        this.docRoot = JspCompileUtil.getContextRoot(jspPage.getPrimaryFile());
        //FileObject jspFileObject = jspPage.getPrimaryFile();
        serverInstance = JspCompileUtil.getCurrentServerInstance(jspPage);
        servletJavaRoot = getServletJavaRootFromServer();
        servletResourceName = getServletResourceNameFromServer();
        servletEncoding = getServletEncodingFromServer();
    }
    
    public FileObject getServletJavaRoot() {
        if ((servletJavaRoot != null) && servletJavaRoot.exists()) {
            return JspCompileUtil.getAsRootOfFileSystem(servletJavaRoot);
        }
        else {
            return null;
        }
    }
    
    public String getServletResourceName() {
        return servletResourceName;
    }
    
    private File getServletFile() {
        if (servletJavaRoot == null) {
            return null;
        }
        URI rootURI = servletJavaRoot.toURI();
        URI servletURI = rootURI.resolve(servletResourceName);
        return new File(servletURI);
    }
    
    public FileObject getServletFileObject() {
        FileObject root = getServletJavaRoot();
        if (root == null) {
            return null;
        }
        File servlet = getServletFile();
        if ((servlet == null) || !servlet.exists()) {
            return null;
        }
        FileObject fo[] = FileUtil.fromFile(servlet);
        // get a fileobject from the same FS as the root
        try {
            FileSystem rootFs = root.getFileSystem();
            for (int i = 0; i < fo.length; i++) {
                if (fo[i].getFileSystem() == rootFs) {
                    return fo[i];
                }
            }
            // not found, needs refresh
            root.getFileSystem().refresh(false);
            return JspCompileUtil.findRelativeResource(root, getServletResourceName());
        }
        catch (FileStateInvalidException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        return null;
    }
    
    /** Returns the Java resource root from the server.
     * For now just hardcoded for Tomcat 5, but needs 
     * to be changed to talk to the plugin through the server integration API.
     */
    private File getServletJavaRootFromServer() {
        // PENDING - Tomcat specific
        File catalinaBase = getCatalinaBase();
        if (catalinaBase == null) {
            return null;
        }
        File hostBase = new File(catalinaBase, "work/Tomcat-Internal/localhost".replace('/', File.separatorChar));
        File workDir = new File(hostBase, getContextRootString());
        //System.out.println("returning servlet root " + workDir);
        return workDir;
    }
    
    /** PENDING - remove this, as this is Tomcat-specific.
     */
    private File getCatalinaBase() {
        Class siClass = serverInstance.getClass();
        if (siClass.getName().equals("org.netbeans.modules.tomcat.tomcat40.Tomcat40Instance")) {
            try {
                // this is Tomcat
                java.lang.reflect.Method getInst = siClass.getMethod("getInstallation", new Class[0]);
                Object inst = getInst.invoke(serverInstance, new Object[0]);
                Class instClass = inst.getClass();
                java.lang.reflect.Method baseMethod = instClass.getMethod("getBaseDirectory", new Class[0]);
                File baseDirFile = (File)baseMethod.invoke(inst, new Object[0]);
                if (baseDirFile != null) {
                    return baseDirFile;
                }
                java.lang.reflect.Method homeMethod = instClass.getMethod("getHomeDirectory", new Class[0]);
                baseDirFile = (File)homeMethod.invoke(inst, new Object[0]);
                return baseDirFile;
            }
            catch (NoSuchMethodException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            catch (IllegalAccessException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            catch (java.lang.reflect.InvocationTargetException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return null;
    }
        
    /** Finds the context path used by this JSP during deployment.
     */
    private String getContextPath() {
        try {
            FileObject contextRoot = JspCompileUtil.getContextRoot(jspPage.getPrimaryFile());
            DataObject dobj = DataObject.find(contextRoot);
            if (dobj instanceof WebContextObject) {
                WebContextObject wco = (WebContextObject)dobj;
                return wco.getURIParameter();
            }
        }
        catch (DataObjectNotFoundException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        // an ugly fallback
        return "";
    }
    
    /** PENDING - remove this, as this is Tomcat-specific.
     */
    private String getContextRootString() {
        String contextRootPath = getContextPath();
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
    
    /** Returns the resource name of the servlet relative to the base Java root from the server.
     * For now just hardcoded for Tomcat 5, but needs
     * to be changed to talk to the plugin through the server integration API.
     */
    private String getServletResourceNameFromServer() {
        // PENDING - Tomcat specific
        String jspPath = JspCompileUtil.findRelativePath(docRoot, jspPage.getPrimaryFile());
        int lastDot = jspPath.lastIndexOf('.');
        return jspPath.substring(0, lastDot) + "$jsp.java";
    }

    /** Returns the encoding of the servlet from the server.
     * For now just hardcoded for Tomcat 5, but needs
     * to be changed to talk to the plugin through the server integration API.
     */
    private String getServletEncodingFromServer() {
        // PENDING - Tomcat specific
        return "UTF8"; // NOI18N
    }
    
    /** Returns server instance for which this CompileData was created. */
    public ServerInstance getServerInstance() {
        return serverInstance;
    }
    
    /** Returns encoding for the servlet generated from the JSP. */
    public String getServletEncoding() {
        return servletEncoding;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("--COMPILE DATA--"); // NOI18N
        sb.append("\n"); // NOI18N
        sb.append("server          : " + serverInstance); // NOI18N
        sb.append("\n"); // NOI18N
        sb.append("JSP page        : " + jspPage.getPrimaryFile().getPackageNameExt('/','.')); // NOI18N
        sb.append("\n"); // NOI18N
        sb.append("servletJavaRoot : " + servletJavaRoot + ", exists= " +  // NOI18N
            ((servletJavaRoot == null) ? "false" : "" + servletJavaRoot.exists())); // NOI18N
        sb.append("\n"); // NOI18N
        sb.append("servletResource : " + servletResourceName + ", fileobject exists= " +  // NOI18N
            (getServletFileObject() != null)); // NOI18N
        sb.append("\n"); // NOI18N
        sb.append("servletFile : " + getServletFile().getAbsolutePath() + ", exists= " +  // NOI18N
            getServletFile().exists()); // NOI18N
        sb.append("\n"); // NOI18N
        sb.append("--end COMPILE DATA--"); // NOI18N
        return sb.toString();
    }
    
}

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
import java.util.*;

import org.netbeans.modules.j2ee.server.datamodel.WebStandardData;
import org.netbeans.modules.j2ee.server.web.FfjJspCompileContext;
import org.netbeans.modules.j2ee.server.ServerInstance;

import org.openide.filesystems.*;

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
    private ServerInstance serverInstance;
    //private FileObject classesDirectory
    private FileObject servletDirectory;
    //private String futureServletClassName, currentServletClassName;
    //private String targetClassFileName;
    //private String packageName;
    private boolean outDated;
    private String servletEncoding;
    private Vector additionalClassPath;
    private String servletClassName;
    private String realServletClassName;
    private String servletFileName;
    private String servletFileNameWithoutPackage;
    
    private final static boolean debug = false;
     

    /** Creates new CompileData */
    public CompileData(JspDataObject jspPage) {
        this.jspPage = jspPage;
        FileObject jspFileObject = jspPage.getPrimaryFile();
        WebStandardData.WebResource res = JspCompileUtil.getResourceData(jspFileObject);
        if (res == null) return; // IZ: 36034
        WebStandardData.WebJsp jspData = (WebStandardData.WebJsp)res;  // this should be ok
        serverInstance = JspCompileUtil.getCurrentServerInstance(jspPage);
        FfjJspCompileContext comp = JspCompileUtil.getCurrentCompileContext(jspPage);
        if (comp != null) {
            // the plugin supports compilation
            FfjJspCompileContext.DevelopmentCompilation dev = 
                comp.getDevelopmentCompilation(jspData);

            // now fill the data
            outDated = dev.isOutDated();
            servletEncoding = dev.getServletEncoding();
            servletClassName = comp.getServletClassName(jspData);
            if (comp instanceof ExCompileContext) {
                realServletClassName = ((ExCompileContext)comp).getRealClassName(jspData);
            }
            else {
                realServletClassName = servletClassName;
            }
            servletFileName = dev.getServletFileName();
            additionalClassPath = computeAdditionalClassPath(dev.getAdditionalClassPath());
            if (servletFileName != null) {
                try {
                    computeServletData();
                }
                catch (IOException e) {
                    // pending
                    e.printStackTrace();
                }
            }
        }
    }
    
    /** Returns server instance for which this CompileData was created. */
    public ServerInstance getServerInstance() {
        return serverInstance;
    }
    
    /** Returns servlet directory for the servlet (including any subpackage directories). */
    public FileObject getServletDirectory() {
        return servletDirectory;
    }

    /** Returns whether the JSP page is outdated */
    public boolean isOutDated() {
        return outDated;
    }
    
    /** Returns additional classpath for JSP compilation as an enumeration of Files. */
    public Vector getAdditionalClassPath() {
        return additionalClassPath;
    }

    /** Returns a name of the servlet with extension without package.
    * Null if does not exist.
    */
    public String getCurrentServletFileName() {
        return servletFileNameWithoutPackage;
    }
    
    public String getCurrentServletClassName() {
        return servletClassName;
    }
    
    public String getRealServletClassName() {
        return realServletClassName;
    }
    
    /** Returns encoding for the servlet generated from the JSP. */
    public String getServletEncoding() {
        return servletEncoding;
    }
    
    private Vector computeAdditionalClassPath(String classpath) {
        if (debug)
            System.out.println("---additional CP---"); // NOI18N
        Vector v = new Vector();
        StringTokenizer st = new StringTokenizer(classpath, "" + File.pathSeparatorChar); // NOI18N
        for (;st.hasMoreTokens();) {
            File f = new File(st.nextToken());
            if (debug)
                System.out.println("plugin returned " + f); // NOI18N
            v.add(f);
        }
        return v;
    }
    
    /** Computes the servlet location in IDE terms, e.g. servlet FileObject. */
    private void computeServletData() throws IOException {
        // first determine the root of the servlet package hierarchy
        // from the servlet file and its class name
        File serv = new File(servletFileName).getAbsoluteFile();
        String servName = serv.getAbsolutePath();
        // compute the servletFileNameWithoutPackage
        int lastSep = servName.lastIndexOf(File.separatorChar);
        if (lastSep != -1) {
            servletFileNameWithoutPackage = servName.substring(lastSep + 1);
        }
        else {
            servletFileNameWithoutPackage = servName;
        }
        // compare the file name with the package name
        int lastDot = servName.lastIndexOf('.');
        if (lastDot != -1) {
            servName = servName.substring(0, lastDot);
        }
        String classNameFS = servletClassName.replace('.', File.separatorChar);
        // now servName should end with classNameFs
        if (servName.endsWith(classNameFS)) {
            servName = servName.substring(0, servName.length() - classNameFS.length());
            // now servName contains the package directory root
            FileObject rootFO = JspCompileUtil.getAsRootOfFileSystem(new File(servName));
            // find the package name
	    if(debug) {
		System.err.println("servletClassName"); // NOI18N
		System.err.println(servletClassName);
	    }
	    
            lastDot = servletClassName.lastIndexOf('.');
	    if(debug) System.err.println(lastDot);
            if (lastDot != -1) {
                String packageNameSl = servletClassName.substring(0, lastDot).replace('.','/');
                servletDirectory = FileUtil.createFolder(rootFO, packageNameSl);
            }
            else {
                servletDirectory = rootFO;
            }
        }
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("--COMPILE DATA--"); // NOI18N
        sb.append("\n"); // NOI18N
        sb.append("server          : " + serverInstance); // NOI18N
        sb.append("\n"); // NOI18N
        sb.append("JSP page        : " + jspPage.getPrimaryFile().getPackageNameExt('/','.')); // NOI18N
        sb.append("\n"); // NOI18N
        sb.append("servletFile     : " + servletFileName + ", exists= " +  // NOI18N
            ((servletFileName == null) ? "false" : "" + new File(servletFileName).exists())); // NOI18N
        sb.append("\n"); // NOI18N
        sb.append("servletClass    : " + servletClassName); // NOI18N
        sb.append("\n"); // NOI18N
        sb.append("realServletClass: " + realServletClassName); // NOI18N
        sb.append("\n"); // NOI18N
        sb.append("encoding        : " + servletEncoding); // NOI18N
        sb.append("\n"); // NOI18N
        sb.append("servletDir (FO) : " +  // NOI18N
            ((servletDirectory == null) ? "null" : servletDirectory.getPackageName('/'))); // NOI18N
        sb.append("\n"); // NOI18N
        sb.append("servlet W/O Pkg : " + servletFileNameWithoutPackage); // NOI18N
        sb.append("\n"); // NOI18N
        sb.append("outdated        : " + outDated); // NOI18N
        sb.append("\n"); // NOI18N
        sb.append("--end COMPILE DATA--"); // NOI18N
        return sb.toString();
    }
    
}

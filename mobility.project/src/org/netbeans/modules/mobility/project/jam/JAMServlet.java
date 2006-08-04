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

/*
 * JAMServlet.java
 *
 * Created on February 8, 2002, 1:41 PM
 *
 *
 */
package org.netbeans.modules.mobility.project.jam;

import java.io.*;
import java.net.URLDecoder;
import javax.servlet.*;
import javax.servlet.http.*;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.mobility.project.J2MEProject;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author  mr97946
 * @version
 */
public class JAMServlet extends HttpServlet {
    
    private static final long serialVersionUID = 5518842704648404246L;
       
    /** Destroys the servlet.
     */
    public void destroy() {
        
    }
    
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
    throws java.io.IOException {
        processRequest(request, response);
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
    throws java.io.IOException {
        processRequest(request, response);
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return NbBundle.getMessage(JAMServlet.class, "LAB_ServletInfo"); // NOI18N
    }
    
    /** Processes the request for both HTTP GET and POST methods
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(final HttpServletRequest request, final HttpServletResponse response)
    throws java.io.IOException {
        handleRepositoryRequest(request, response);
    }
    
    /** Handles a request to a repository item. Returns true if the request was handled, i.e.
     * if the URL corresponds to a repository FileObject. According to whether this file object is a
     * folder, either outputs the file with appropriate content type or outputs a directory and file
     * listing for this folder.
     */
    @SuppressWarnings("deprecation")
	protected boolean handleRepositoryRequest(final HttpServletRequest request, final HttpServletResponse response)
    throws IOException {
        String pathI = request.getPathInfo();
        if (pathI == null) {
            return false;
        }
        if ("status".equals(pathI)) {//NOI18N
            return false;
        }
        try {
            pathI = URLDecoder.decode(pathI, "UTF-8"); // NOI18N
        } catch (UnsupportedEncodingException e) {
            pathI = URLDecoder.decode(pathI);
        }
        final File f = new File(pathI);
        if (!f.isFile()) return false;
        final FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(f));
        if (fo == null) return false;
        final Project p = FileOwnerQuery.getOwner(fo);
        if (p instanceof J2MEProject) {
            return sendFile(response,f);
        }
        return false;
        
    }
    
    private boolean sendFile(final HttpServletResponse response, final File file)
    throws IOException {
        String encoding = "content/unknown"; //NOI18N
        if (file.getName().endsWith(".jad")) {//NOI18N
            encoding = "text/vnd.sun.j2me.app-descriptor";//NOI18N
        } else if (file.getName().endsWith(".jar")) { //NOI18N
            encoding = "application/java-archive";//NOI18N
        } else return false;
        response.setContentType(encoding);
        final int len = (int)file.length();
        response.setContentLength(len);
        response.setDateHeader("Last-Modified", file.lastModified()); // NOI18N
        final InputStream in = new FileInputStream(file);
        try {
            final ServletOutputStream os = response.getOutputStream();
            try {
                copyStream(in, os);
            } finally {
                os.close();
            }
        } finally {
            in.close();
        }
        return true;
    }
    
    /** Copy Stream in to Stream until EOF or exception
     */
    public static void copyStream(final InputStream in, final OutputStream out)
    throws IOException {
        final int bufferSize = 8000;
        try {
            final byte buffer[] = new byte[bufferSize];
            int len=bufferSize;
            while (true) {
                len = in.read(buffer, 0, bufferSize);
                if (len == -1) break;
                out.write(buffer, 0, len);
            }
        } finally {
            out.flush();
        }
    }
}

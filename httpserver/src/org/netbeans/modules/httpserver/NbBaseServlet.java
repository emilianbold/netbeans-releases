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

package org.netbeans.modules.httpserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.TreeSet;
import java.text.DateFormat;
import java.net.InetAddress;
import javax.servlet.*;
import javax.servlet.http.*;

import org.openide.util.NbBundle;
import org.openide.util.enum.AlterEnumeration;
import org.openide.util.enum.EmptyEnumeration;
import org.openide.util.enum.RemoveDuplicatesEnumeration;
import org.openide.util.enum.SequenceEnumeration;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;
import org.openide.util.SharedClassObject;

/** Base servlet for servlets which access NetBeans Open APIs
*
* @author Petr Jiricka
* @version 0.11 May 5, 1999
*/
public abstract class NbBaseServlet extends HttpServlet {

    /** Initializes the servlet. */
    public void init() throws ServletException {
    }

    /** Processes the request for both HTTP GET and POST methods
    * @param request servlet request
    * @param response servlet response
    */
    protected abstract void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException;

    /** Performs the HTTP GET operation.
    * @param request servlet request
    * @param response servlet response
    */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException {
        processRequest(request, response);
    }

    /** Performs the HTTP POST operation.
    * @param request servlet request
    * @param response servlet response
    */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException {
        processRequest(request, response);
    }

    /**
    * Returns a short description of the servlet.
    */
    public String getServletInfo() {
        return NbBundle.getBundle(NbBaseServlet.class).getString("MSG_BaseServletDescr");
    }

    /** Checks whether access should be permitted according to HTTP Server module access settings
    * (localhost/anyhost, granted addesses)
    *  @return true if access is granted
    */
    protected boolean checkAccess(HttpServletRequest request) throws IOException {

        HttpServerSettings settings = (HttpServerSettings)SharedClassObject.findObject (HttpServerSettings.class);
        if (settings == null)
            return false;

        if (settings.getHostProperty ().getHost ().equals(HttpServerSettings.ANYHOST))
            return true;

        HashSet hs = settings.getGrantedAddressesSet();

        if (hs.contains(request.getRemoteAddr().trim()))
            return true;

        String pathI = request.getPathInfo();
        if (pathI == null)
            pathI = "";      // NOI18N
        // ask user
        try {
            String address = request.getRemoteAddr().trim();
            if (settings.allowAccess(InetAddress.getByName(address), pathI)) return true;
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
            return false;
        }

        return false;
    }

    protected boolean handleClasspathRequest(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
        String pathI = request.getPathInfo();
        if (pathI == null) {
            return false;
        }
        if (pathI.length() == 0) return false;
        if (pathI.charAt(0) == '/') pathI = pathI.substring(1);
        if (pathI.length() == 0) return false;
	ClassLoader cl = (ClassLoader)Lookup.getDefault().lookup(ClassLoader.class);
        InputStream is = cl.getResourceAsStream(pathI);
        if (is == null) return false;
        
        try {
            String encoding = null;
            int ind = pathI.lastIndexOf("."); // NOI18N
            if (ind != -1) {
                String ext = pathI.substring(ind + 1);
                encoding = FileUtil.getMIMEType(ext);
            }
            // resources like com/ or org/ are in our classpath but have 
            // zero lenght - use repository to get directory listing of items 
            // tha are mounted
            if (encoding == null) {
                encoding = "content/unknown"; // NOI18N
            }
            response.setContentType(encoding);
            // don't know content length
            ServletOutputStream os = response.getOutputStream();
            try {
                copyStream(is, os);
    	    }
    	    finally {
    	        os.close();
    	    }
    	}
        finally {
            is.close();
        }
        return true;
    }

    /** Handles a request to a repository item. Returns true if the request was handled, i.e.
    * if the URL corresponds to a repository FileObject. According to whether this file object is a 
    * folder, either outputs the file with appropriate content type or outputs a directory and file 
    * listing for this folder.
    */
    protected boolean handleRepositoryRequest(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
        String pathI = request.getPathInfo();
        if (pathI == null) {
            return false;
        }
        FileObject fo = Repository.getDefault().findResource(pathI);
        if (fo == null) {
            // try with the trailing /
            if ((pathI.length() > 0) && (pathI.charAt(pathI.length() - 1) != '/'))
                fo = Repository.getDefault ().findResource(pathI + '/');
        }
        if (fo == null) return false;

        // handle the request
        if (fo.isFolder()) sendDirectory(request, response, fo);
        else               sendFile(request,response,fo);

        return true;
    }

    private void sendFile(HttpServletRequest request, HttpServletResponse response, FileObject file)
    throws IOException {
        String encoding = file.getMIMEType();
        response.setContentType(encoding);
        int len = (int)file.getSize();
        response.setContentLength(len);
        response.setDateHeader("Last-Modified", file.lastModified().getTime()); // NOI18N
        InputStream in = file.getInputStream();
        try {
            ServletOutputStream os = response.getOutputStream();
            try {
                copyStream(in, os);
            }
            finally {
                os.close();
            }
        }
        finally {
            in.close();
        }
    }

    private void sendDirectory(HttpServletRequest request, HttpServletResponse response, FileObject file)
    throws IOException {
        response.setContentType("text/html"); // NOI18N
        String title = file.getPath ();
        if (title.length() == 0)
            title = NbBundle.getBundle(NbBaseServlet.class).getString("LAB_REPOSITORY_ROOT");

        PrintWriter out=response.getWriter();

        out.print("<HTML><HEAD><TITLE>"); // NOI18N
        out.print(title);
        out.print("</TITLE></HEAD><BODY>\n<H1>"); // NOI18N
        out.print(title);
        out.print("</H1>"); // NOI18N

        FileObject parent = file.getParent();
        if (parent != null) {
            out.print ("<A HREF=\"../\">" + // NOI18N
                       NbBundle.getBundle(NbBaseServlet.class).getString("CTL_PARENT_DIR") +
                       "</A><BR><BR>\n"); // NOI18N
        }

        DateFormat dfmt=DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

        Iterator it = sortEnumeration (children (file.getPath ()),
                                       new Comparator() {

                                           /** Implements ordering of FileObjects: directories first, alphabetically */
                                           public int compare(Object o1, Object o2) {
                                               FileObject f1 = (FileObject)o1;
                                               FileObject f2 = (FileObject)o2;
                                               if (f1.isFolder() && !f2.isFolder()) return -1;
                                               if (!f1.isFolder() && f2.isFolder()) return 1;
                                               int res = String.CASE_INSENSITIVE_ORDER.compare(f1.getName(), f2.getName());
                                               if (res == 0) res = String.CASE_INSENSITIVE_ORDER.compare(f1.getExt(), f2.getExt());
                                               return res;
                                           }

                                           /** Default implementation */
                                           public boolean equals(Object obj) {
                                               return false;
                                           }

                                       });

        HashMap all = new HashMap();
        while (it.hasNext()) {
            FileObject fo = (FileObject)it.next();
            String name;
            name = fo.getNameExt ();

            if (all.get (name) == null) {
                all.put (name, name);
		StringBuffer sb = new StringBuffer ("<A HREF=\"").append (name);
		if (fo.isFolder ()) {
		    sb.append ("/\"><B>").append (name).append ("</B></A><BR>"); // NOI18N
		}
		else {
		    sb.append ("\">").append (name).append ("</A><BR>"); // NOI18N
		}
                out.print (sb.toString ());
            }
        }
        out.flush();
    }

    /** Sorts enumeration elements by the given comparator and returns them as an iterator */
    private static Iterator sortEnumeration(Enumeration en, Comparator c) {
        TreeSet ts = new TreeSet(c);
        for (;en.hasMoreElements();)
            ts.add(en.nextElement());
        return ts.iterator();
    }

    /** List of all children of folders with given name
    * @param n name of folder to find
    * @return enumeration of children (FileObjects)
    */
    private static Enumeration children (final String name) {
        Enumeration en = Repository.getDefault ().getFileSystems ();
        // only not hidden filesystems containing the folder are counted
        // creates enumeration of enumerations of FileObjects
        en = new AlterEnumeration (en) {
                 public Object alter (Object o) {
                     FileSystem fs = (FileSystem)o;
                     if (fs.isHidden ()) return EmptyEnumeration.EMPTY;

                     FileObject fo = fs.findResource (name);
                     if (fo == null || !fo.isFolder ()) return EmptyEnumeration.EMPTY;
                     return fo.getChildren (false);
                 }
             };

        // composes enumerations into one
        return new SequenceEnumeration (en);
    }

    /** Copy Stream in to Stream until EOF or exception
     */
    public static void copyStream(InputStream in, OutputStream out)
    throws IOException {
        int bufferSize = 8000;
        try {
            byte buffer[] = new byte[bufferSize];
            int len=bufferSize;
            while (true) {
                len = in.read(buffer, 0, bufferSize);
                if (len == -1) break;
                out.write(buffer, 0, len);
            }
        }
        finally {
            out.flush();
        }
    }

}

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.httpserver;
                            
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
import org.openide.TopManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileSystem;

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
    return "Base servlet for servlets which access NetBeans Open APIs";
  }

  /** Checks whether access should be permitted according to HTTP Server module access settings 
  * (localhost/anyhost, granted addesses)
  *  @return true if access is granted
  */
  protected boolean checkAccess(HttpServletRequest request) throws IOException {
  
    HttpServerSettings settings = HttpServerSettings.OPTIONS;
    
    if (settings.getHost().equals(HttpServerSettings.ANYHOST))
      return true;

    HashSet hs = HttpServerSettings.OPTIONS.getGrantedAddressesSet();
    
    if (hs.contains(request.getRemoteAddr().trim()))
      return true;
    
    // ask user
    try {
      String address = request.getRemoteAddr().trim();
      if (HttpServerSettings.OPTIONS.allowAccess(InetAddress.getByName(address))) return true;
    } catch (Exception ex) {
      TopManager.getDefault().notifyException(ex);
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
    InputStream is = TopManager.getDefault().systemClassLoader().getResourceAsStream(pathI);
    if (is == null) return false;

    int ind = pathI.lastIndexOf(".");
    String ext = pathI.substring(ind + 1);
    String encoding = FileUtil.getMIMEType(ext);
    // PENDING - URL com/ behaves incorrectly
    if (encoding == null)
      encoding = "thisisabug/inclassloader";
    response.setContentType(encoding);
    // don't know content length
    ServletOutputStream os = response.getOutputStream();
    copyStream(is, os);
    os.close();
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
    FileObject fo = TopManager.getDefault().getRepository().findResource(pathI);
    if (fo == null) {
      // try with the trailing /
      if ((pathI.length() > 0) && (pathI.charAt(pathI.length() - 1) != '/'))
        fo = TopManager.getDefault().getRepository().findResource(pathI + '/');
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
    response.setDateHeader("Last-Modified", file.lastModified().getTime());
    InputStream in = file.getInputStream();
    ServletOutputStream os = response.getOutputStream();
    copyStream(in, os);
    os.close();
  }

  private void sendDirectory(HttpServletRequest request, HttpServletResponse response, FileObject file)
  throws IOException {
    String base = file.getPackageName('/');

    response.setContentType("text/html");
    String title = base;
    if (title.length() == 0)
      title = NbBundle.getBundle(NbBaseServlet.class).getString("LAB_REPOSITORY_ROOT");

    PrintWriter out=response.getWriter();

    out.print("<HTML><HEAD><TITLE>");
    out.print(title);
    out.print("</TITLE></HEAD><BODY>\n<H1>");
    out.print(title);
    out.print("</H1>");
	
    FileObject parent = file.getParent();
    if (parent != null) {
      out.print ("<A HREF=\"../\">" + 
        NbBundle.getBundle(NbBaseServlet.class).getString("CTL_PARENT_DIR") + 
        "</A><BR><BR>\n");
    }                      
	
    DateFormat dfmt=DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

    Iterator it = sortEnumeration (children (file.getPackageNameExt ('/', '.')), 
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
      String form1;
      String form2;
      if (fo.isFolder ()) {
        //name  = fo.getName ();
        form1 = "<B>";
        form2 = "</B>";
      } 
      else {
        //name = fo.getName () + "." + fo.getExt ();
        form1 = "";
        form2 = "";
      }            
      if (fo.getExt().length() == 0) name = fo.getName ();
      else                           name = fo.getName () + "." + fo.getExt ();
          
      if (all.get (name) == null) {
        all.put (name, name);
        out.print ("<A HREF=\"" /*+ baseRelativeURL*/);
        out.print (name/*fo.getPackageNameExt ('/', '.')*/);
        if (fo.isFolder ()) out.println("/");
        out.print ("\">");
        out.print (form1 + name + form2);
        out.print ("</A>");
        out.println ("<BR>");
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
    Enumeration en = TopManager.getDefault ().getRepository ().getFileSystems ();
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

/*
 * Log
 *  3    Gandalf   1.2         10/7/99  Petr Jiricka    Removed debug println
 *  2    Gandalf   1.1         10/4/99  Petr Jiricka    
 *  1    Gandalf   1.0         9/30/99  Petr Jiricka    
 * $
 */

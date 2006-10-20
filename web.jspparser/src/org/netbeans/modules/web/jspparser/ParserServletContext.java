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

package org.netbeans.modules.web.jspparser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;
import java.net.URL;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;

import org.openide.filesystems.FileObject;

import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;

import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.openide.text.CloneableEditorSupport;

/**
 * Simple <code>ServletContext</code> implementation without
 * HTTP-specific methods.
 *
 * @author Peter Rossbach (pr@webapp.de)
 */

public class ParserServletContext implements ServletContext {


    // ----------------------------------------------------- Instance Variables


    /**
     * Servlet context attributes.
     */
    protected Hashtable<String, Object> myAttributes;


    /**
     * The base FileObject (document root) for this context.
     */
    protected FileObject wmRoot;
    
    
    protected JspParserAPI.WebModule myWm;
    
    /** If true, takes the data from the editor; otherwise 
     * from the disk.
     */
    protected boolean useEditorVersion;
    
    
    // ----------------------------------------------------------- Constructors


    /**
     * Create a new instance of this ServletContext implementation.
     *
     * @param wmRoot Resource base FileObject
     * @param wm JspParserAPI.WebModule in which we are parsing the file - this is used to 
     *    find the editor for objects which are open in the editor
     */
    public ParserServletContext(FileObject wmRoot, JspParserAPI.WebModule wm, boolean useEditor) {

        myAttributes = new Hashtable<String, Object>();
        this.wmRoot = wmRoot;
        this.myWm = wm;
        this.useEditorVersion = useEditor;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Return the specified context attribute, if any.
     *
     * @param name Name of the requested attribute
     */
    public Object getAttribute(String name) {
        return myAttributes.get(name);
    }


    /**
     * Return an enumeration of context attribute names.
     */
    public Enumeration<String> getAttributeNames() {

        return myAttributes.keys();

    }


    /**
     * Return the servlet context for the specified path.
     *
     * @param uripath Server-relative path starting with '/'
     */
    public ServletContext getContext(String uripath) {

        return (null);

    }


    /**
     * Return the specified context initialization parameter.
     *
     * @param name Name of the requested parameter
     */
    public String getInitParameter(String name) {

        return (null);

    }


    /**
     * Return an enumeration of the names of context initialization
     * parameters.
     */
    public Enumeration getInitParameterNames() {

        return (new Vector().elements());

    }


    /**
     * Return the Servlet API major version number.
     */
    public int getMajorVersion() {

        return (2);

    }


    /**
     * Return the MIME type for the specified filename.
     *
     * @param file Filename whose MIME type is requested
     */
    public String getMimeType(String file) {

        return (null);

    }


    /**
     * Return the Servlet API minor version number.
     */
    public int getMinorVersion() {

        return (3);

    }


    /**
     * Return a request dispatcher for the specified servlet name.
     *
     * @param name Name of the requested servlet
     */
    public RequestDispatcher getNamedDispatcher(String name) {

        return (null);

    }

    /** Returns a FileObject representation of the specified context-relative 
     * virtual path.
     */
    protected FileObject getResourceAsObject(String path) {
        return ContextUtil.findRelativeFileObject(wmRoot, path);
    }
    

    /**
     * Return the real path for the specified context-relative
     * virtual path.
     *
     * @param path The context-relative virtual path to resolve
     */
    public String getRealPath(String path) {
        
        if (!path.startsWith("/")) {
            return (null);
        }
        FileObject fo = getResourceAsObject(path);
        if (fo != null) {
            File ff = FileUtil.toFile(fo);
            if (ff != null) {
                return ff.getAbsolutePath();
            }
        }
        
        return null;
    }
            
            
    /**
     * Return a request dispatcher for the specified context-relative path.
     *
     * @param path Context-relative path for which to acquire a dispatcher
     */
    public RequestDispatcher getRequestDispatcher(String path) {

        return (null);

    }


    /**
     * Return a URL object of a resource that is mapped to the
     * specified context-relative path.
     *
     * @param path Context-relative path of the desired resource
     *
     * @exception MalformedURLException if the resource path is
     *  not properly formed
     */
    public URL getResource(String path) throws MalformedURLException {

        if (!path.startsWith("/"))
            throw new MalformedURLException(NbBundle.getMessage(ParserServletContext.class,
                "EXC_PathMustStartWithSlash", path));
        
        FileObject fo = getResourceAsObject(path);
        if (fo == null) {
            return null;
        }
        return URLMapper.findURL(fo, URLMapper.EXTERNAL);

    }


    /**
     * Return an InputStream allowing access to the resource at the
     * specified context-relative path.
     *
     * @param path Context-relative path of the desired resource
     */
    public InputStream getResourceAsStream(String path) {

        // first try from the opened editor - if fails read from file
        if (myWm != null) {
            FileObject fo = getResourceAsObject(path);
            if ((fo != null) && (useEditorVersion)) {
                // reading from the editor
                InputStream result = myWm.getEditorInputStream (fo);
                if (result != null) {
                    return result;
                }
            }
        }
        
        // read from the file by default
        try {
            URL url = getResource(path);
            if (url == null) {
                return null;
            }
            else {
                return url.openStream();
            }
        } catch (Throwable t) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, t);
            return (null);
        }

    }
    

    /**
     * Return the set of resource paths for the "directory" at the
     * specified context path.
     *
     * @param path Context-relative base path
     */
    public Set<String> getResourcePaths(String path) {

        Set<String> thePaths = new HashSet<String>();
        if (!path.endsWith("/"))
            path += "/";
        String basePath = getRealPath(path);
        if (basePath == null)
            return (thePaths);
        File theBaseDir = new File(basePath);
        if (!theBaseDir.exists() || !theBaseDir.isDirectory())
            return (thePaths);
        String theFiles[] = theBaseDir.list();
        for (int i = 0; i < theFiles.length; i++) {
            File testFile = new File(basePath + File.separator + theFiles[i]);
            if (testFile.isFile())
                thePaths.add(path + theFiles[i]);
            else if (testFile.isDirectory())
                thePaths.add(path + theFiles[i] + "/");
        }
        return thePaths;

    }


    /**
     * Return descriptive information about this server.
     */
    public String getServerInfo() {

        return ("NB.ParserServletContext/1.0");

    }


    /**
     * Return a null reference for the specified servlet name.
     *
     * @param name Name of the requested servlet
     *
     * @deprecated This method has been deprecated with no replacement
     */
    public Servlet getServlet(String name) throws ServletException {

        return (null);

    }


    /**
     * Return the name of this servlet context.
     */
    public String getServletContextName() {

        return (getServerInfo());

    }


    /**
     * Return an empty enumeration of servlet names.
     *
     * @deprecated This method has been deprecated with no replacement
     */
    public Enumeration getServletNames() {

        return (new Vector().elements());

    }


    /**
     * Return an empty enumeration of servlets.
     *
     * @deprecated This method has been deprecated with no replacement
     */
    public Enumeration getServlets() {

        return (new Vector().elements());

    }


    /**
     * Log the specified message.
     *
     * @param message The message to be logged
     */
    public void log(String message) {

        ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, message);

    }


    /**
     * Log the specified message and exception.
     *
     * @param exception The exception to be logged
     * @param message The message to be logged
     *
     * @deprecated Use log(String,Throwable) instead
     */
    public void log(Exception exception, String message) {

        log(message, exception);

    }


    /**
     * Log the specified message and exception.
     *
     * @param message The message to be logged
     * @param exception The exception to be logged
     */
    public void log(String message, Throwable exception) {

        ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, message);
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exception);
        
    }


    /**
     * Remove the specified context attribute.
     *
     * @param name Name of the attribute to remove
     */
    public void removeAttribute(String name) {

        myAttributes.remove(name);

    }


    /**
     * Set or replace the specified context attribute.
     *
     * @param name Name of the context attribute to set
     * @param value Corresponding attribute value
     */
    public void setAttribute(String name, Object value) {

        myAttributes.put(name, value);

    }


    public String getContextPath(){
        return "";
    }

}

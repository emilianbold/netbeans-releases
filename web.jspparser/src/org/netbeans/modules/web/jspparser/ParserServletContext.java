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

package org.netbeans.modules.web.jspparser;


import java.io.File;
import java.io.InputStream;
import java.io.IOException;
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

import org.openide.TopManager;
import org.openide.ErrorManager;

import org.netbeans.modules.web.core.jsploader.ContextDescriptor;

/**
 * Simple <code>ServletContext</code> implementation without
 * HTTP-specific methods.
 *
 * @author Peter Rossbach (pr@webapp.de)
 * @author Petr Jiricka
 */

public class ParserServletContext implements ServletContext {


    // ----------------------------------------------------- Instance Variables


    /**
     * Servlet context attributes.
     */
    protected Hashtable myAttributes;


    /**
     * The log writer we will write log messages to.
     */
    protected PrintWriter myLogWriter;


    /**
     * The descriptor of the base context.
     */
    protected ContextDescriptor myContextDescriptor;


    // ----------------------------------------------------------- Constructors


    /**
     * Create a new instance of this ServletContext implementation.
     *
     * @param aLogWriter PrintWriter which is used for <code>log()</code> calls. May be null.
     * @param aResourceBaseURL Resource base URL
     */
    public ParserServletContext(PrintWriter aLogWriter, ContextDescriptor aContextDescriptor) {

        myAttributes = new Hashtable();
        myLogWriter = aLogWriter;
        myContextDescriptor = aContextDescriptor;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Return the specified context attribute, if any.
     *
     * @param name Name of the requested attribute
     */
    public Object getAttribute(String name) {

        return (myAttributes.get(name));

    }


    /**
     * Return an enumeration of context attribute names.
     */
    public Enumeration getAttributeNames() {

        return (myAttributes.keys());

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


    /**
     * Return the real path for the specified context-relative
     * virtual path.
     *
     * @param path The context-relative virtual path to resolve
     */
    public String getRealPath(String path) {
        return myContextDescriptor.getRealPath(path);
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
        return myContextDescriptor.getResource(path);
    }


    /**
     * Return an InputStream allowing access to the resource at the
     * specified context-relative path.
     *
     * @param path Context-relative path of the desired resource
     */
    public InputStream getResourceAsStream(String path) {
        return myContextDescriptor.getResourceAsStream(path);
    }


    /**
     * Return the set of resource paths for the "directory" at the
     * specified context path.
     *
     * @param path Context-relative base path
     */
    public Set getResourcePaths(String path) {

        Set thePaths = new HashSet();
        if (!path.endsWith("/")) // NOI18N
            path += "/"; // NOI18N
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
                thePaths.add(path + theFiles[i] + "/"); // NOI18N
        }
        return (thePaths);

    }


    /**
     * Return descriptive information about this server.
     */
    public String getServerInfo() {

        return ("ParserServletContext/1.0"); // NOI18N

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
        
        if (myLogWriter == null)
            return;

        myLogWriter.println(message);

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
        
        TopManager.getDefault().getErrorManager().notify(ErrorManager.INFORMATIONAL, exception);

        if (myLogWriter == null)
            return;
        
        log(message, exception);

    }


    /**
     * Log the specified message and exception.
     *
     * @param message The message to be logged
     * @param exception The exception to be logged
     */
    public void log(String message, Throwable exception) {

        TopManager.getDefault().getErrorManager().notify(ErrorManager.INFORMATIONAL, exception);

        if (myLogWriter == null)
            return;
        
        myLogWriter.println(message);
        exception.printStackTrace(myLogWriter);

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



}

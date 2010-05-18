/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.visualweb.jsfsupport.container;

import java.io.InputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.openide.modules.InstalledFileLocator;

/**
 * <p>Provides a <code>ServletContext</code> (v 2.3) object for design-time use</p>
 *
 * @author Robert Brewin
 * @version 1.0
 */
public class RaveServletContext implements ServletContext {

    /**
     * Storage for attributes
     */
    private Hashtable attributes = new Hashtable();

    /**
     * Storage for parameters
     */
    private Hashtable parameters = new Hashtable();

    /**
     * Default constructor for a RaveServletContext object
     */
    public RaveServletContext() {
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Add an initialization parameter
     *
     * @param name - a <code>String</code> specifying the name of the parameter
     * @param value - a <code>String</code> representing the value for this parameter
     */
    public void addInitParameter(String name, String value) {
        parameters.put(name, value);
    }

    // ------------------------------------------------- ServletContext Methods

    /**
     * Returns the servlet container attribute with the given name, or null if
     * there is no attribute by that name.  An attribute allows a servlet container
     * to give the  servlet additional information not  already provided by this
     * interface. See your  server documentation for information about its attributes.
     * A list of supported attributes can be retrieved using {@link getAttributeNames}.
     *
     * The attribute is returned as a {@link java.lang.Object} or some subclass.
     * Attribute names should follow the same convention as package names. The
     * Java Servlet API specification reserves names matching java.*, javax.*, and sun.*.
     *
     * @param name - a String specifying the name of the attribute
     *
     * @return an Object containing the value of the attribute, or null
     * if no attribute exists matching the given name
     */
    public Object getAttribute(String name) {
        return (attributes.get(name));
    }

    /**
     * Binds an object to a given attribute name in this servlet context. If  the name specified
     * is already used for an attribute, this  method will replace the attribute with the new to
     * the new attribute.
     *
     * @param name - a <code>String</code> specifying the name of the attribute
     * @param object - an <code>Object</code> representing the attribute to be bound
     */
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    /**
     * Returns an Enumeration containing the attribute names available within
     * this servlet context. Use the {@link getAttribute(java.lang.String)} method
     * with an attribute name to get the value of an attribute.
     *
     * @return an <code>Enumeration</code> of attribute names
     */
    public Enumeration getAttributeNames() {
        return attributes.keys();
    }

    /**
     * Removes the attribute with the given name from the servlet context
     *
     * @param name - a <code>String</code> which contains the name of the attribute to remove
     */
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    /**
     * Returns a <code>ServletContext</code> object that corresponds to a
     * specified URL on the server.  In the <em>Mock</em> environment, this
     * method throws an <code>UnsupportedOperationException</code> as there
     * is only one context available
     *
     * @param uripath - a <code>String</code> specifying the context path of
     * another web application in the container.
     */
    public ServletContext getContext(String uripath) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a <code>String</code> containing the value of the named context-wide
     * initialization parameter, or <code>null</code> if the parameter does not exist.
     *
     * @param name - a String containing the name of the parameter whose value is
     * requested
     *
     * @return a <code>String</code> containing at least the servlet container name
     * and version number
     */
    public String getInitParameter(String name) {
        return (String)parameters.get(name);
    }

    /**
     * Returns the names of the context's initialization parameters as an
     * <code>Enumeration</code> of <code>String</code> objects, or an empty
     * <code>Enumeration</code> if the context has no initialization parameters.
     *
     * @return <code>Enumeration</code> of <code>String</code> objects containing
     * the names of the context's initialization parameters
     */
    public Enumeration getInitParameterNames() {
        return parameters.keys();
    }

    /**
     * Returns the major version of the Java Servlet API that this servlet container
     * supports
     *
     * @return <em>2</em>
     */
    public int getMajorVersion() {
        return 2;
    }

    /**
     * Returns the minor version of the Java Servlet API that this servlet container
     * supports
     *
     * @return <em>3</em>
     */
    public int getMinorVersion() {
        return 3;
    }

    /**
     * Returns the MIME type of the specified file, or null if the MIME type is not known.
     *
     * TODO: Return a valid MIME type... at present, this method throws an UnsupportedOperationException
     *
     * @param path - a <code>String</code> specifying the name of a file
     *
     * @return a <code>String</code> specifying the file's MIME type
     */
    public String getMimeType(String path) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a <code>RequestDispatcher</code> object that acts as a wrapper for the
     * named servlet.  This method is <em>unsupported</em> in the Mock environment and
     * returns an <code>UnsupportedOperationException</code>
     */
    public RequestDispatcher getNamedDispatcher(String name) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a <code>RequestDispatcher</code> object that acts as a wrapper for the
     * named servlet.  This method is <em>unsupported</em> in the Mock environment and
     * returns an <code>UnsupportedOperationException</code>
     */
    public RequestDispatcher getRequestDispatcher(String path) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a <code>String</code> containing the real path for a given virtual path.
     * This method will attempt to find the resource using the current thread's
     * context class loader. This method will work only when the path specified
     * is a file path.
     */
    public String getRealPath(String path) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        if (url == null)
            return null;
        String str = url.getPath();
        if (str.startsWith("file:")) {
            int i = 5;
            while (str.charAt(i) == '/')
                i++;
            str = str.substring(i);
            
        }
        return str;
    }

    /**
     * Returns a URL to the resource that is mapped to a specified path.
     * This method returns urls for specific resources available in the Mock jar
     */
    public URL getResource(String resource) throws MalformedURLException {
        URL url = null;
        ClassLoader loader = getClass().getClassLoader();
        if (!resource.endsWith(".jar")) {
            String fqpath = "com/sun/rave/jsfsupp/container" + resource;
            url = loader.getResource(fqpath);
        }
        else {
            // TODO: BOB
            // If the location of where we install component libraries changes (likely),
            // the hard-coded relative path below needs to change
            File file =
                InstalledFileLocator.getDefault().locate("/modules/autoload/ext/" + resource, null, false);
            url = file.toURL();
        }
        return url;
    }

    /**
     * Returns a resource as an <code>InputStream</code> that is mapped to a specified path.
     * This method returns streams for specific resources available in the Mock jar
     */
    public InputStream getResourceAsStream(String path) {
        ClassLoader loader = getClass().getClassLoader();
        String fqpath = "com/sun/rave/jsfsupp/container" + path;
        return loader.getResourceAsStream(fqpath);
    }

    /**
     * Returns a directory-like listing of all the paths to resources.
     * This method is <em>unsupported</em> in the Mock environment and returns null
     */
    public Set getResourcePaths(String path) {
        // TODO: BOB
        // For TP, there is likely only one library, and it's hard-coded below
        // The Set returned from this method should be populated by some resource
        // set which has the list of added component libraries post-TP
        Set set = new java.util.HashSet();
        set.add("jsfcl.jar");
        return set;
    }

    /**
     * Returns the name of this web application associated with this context
     */
    public String getServletContextName() {
        return "RaveServletContext";
    }

    /**
     * Returns the name and version of the servlet container on which the servlet is
     * running.
     */
    public String getServerInfo() {
        return "RaveServletContext";
    }

    /**
     * Deprecated in Servlet APIs, included for compliation purposes only.
     * This method is <em>unsupported</em> in the Mock environment and returns an
     * <code>UnsupportedOperationException</code>
     */
    public Servlet getServlet(String name) throws ServletException {
        throw new UnsupportedOperationException();
        //!CQ could return the one servlet that we could know about
    }

    /**
     * Deprecated in Servlet APIs, included for compliation purposes only.
     * This method is <em>unsupported</em> in the Mock environment and returns an
     * <code>UnsupportedOperationException</code>
     */
    public Enumeration getServlets() {
        throw new UnsupportedOperationException();
        //!CQ could return the one servlet that we could know about
    }

    /**
     * Deprecated in Servlet APIs, included for compliation purposes only.
     * This method is <em>unsupported</em> in the Mock environment and returns an
     * <code>UnsupportedOperationException</code>
     */
    public Enumeration getServletNames() {
        throw new UnsupportedOperationException();
        //!CQ could return the one servlet that we could know about
    }

    /**
     * Writes the specified message to a servlet log file.  Currently,
     * his method is <em>unsupported</em> in the Mock environment and returns an
     * <code>UnsupportedOperationException</code>
     */
    public void log(String message) {
        throw new UnsupportedOperationException();
    }

    /**
     * Deprecated in Servlet APIs, included for compliation purposes only.
     * This method is <em>unsupported</em> in the Mock environment and returns an
     * <code>UnsupportedOperationException</code>
     */
    public void log(Exception exception, String message) {
        throw new UnsupportedOperationException();
    }

    /**
     * Writes the specified message and stack trace to a servlet log file.  Currently,
     * his method is <em>unsupported</em> in the Mock environment and returns an
     * <code>UnsupportedOperationException</code>
     */
    public void log(String message, Throwable exception) {
        throw new UnsupportedOperationException();
    }

    public String getContextPath() {
        return "";
    }
}

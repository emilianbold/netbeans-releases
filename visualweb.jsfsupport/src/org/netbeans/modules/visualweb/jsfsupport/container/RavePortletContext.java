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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.jsfsupport.container;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

//import javax.portlet.PortletContext;
//import javax.portlet.PortletRequestDispatcher;

import org.openide.modules.InstalledFileLocator;


/**
 * A design-time portlet context for web apps rendered at designtime in Creator.
 *
 * Based heavily on RaveServletContext. I could not subclass RaveServletContext or
 * make it implement both interfaces, because various client code, such as the Braveheart
 * PageRenderer, checks "!(context instanceof ServletContext)" to see if it's a portlet,
 * so a context which implements both will confuse code using that technique.
 *
 * @author Tor Norbye
 */
public class RavePortletContext { //implements PortletContext {
    /**
     * Storage for attributes
     */
    private Hashtable attributes = new Hashtable();

    /**
     * Storage for parameters
     */
    private Hashtable parameters = new Hashtable();

    /** Creates a new instance of RavePortletContext */
    public RavePortletContext() {
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public Enumeration getAttributeNames() {
        return attributes.keys();
    }

    public String getInitParameter(String name) {
        return (String)parameters.get(name);
    }

    public java.util.Enumeration getInitParameterNames() {
        return parameters.keys();
    }

    public int getMajorVersion() {
        return 1;
    }

    public int getMinorVersion() {
        return 0;
    }

    public String getMimeType(String file) {
        throw new UnsupportedOperationException();
    }

    public String getRealPath(String path) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(path);

        if (url == null) {
            return null;
        }

        String str = url.getPath();

        if (str.startsWith("file:")) {
            int i = 5;

            while (str.charAt(i) == '/')
                i++;

            str = str.substring(i);
        }

        return str;
    }

    public java.net.URL getResource(String resource) throws MalformedURLException {
        URL url = null;
        ClassLoader loader = getClass().getClassLoader();

        if (!resource.endsWith(".jar")) {
            String fqpath = "com/sun/rave/jsfsupp/container" + resource;
            url = loader.getResource(fqpath);
        } else {
            // TODO: BOB
            // If the location of where we install component libraries changes (likely),
            // the hard-coded relative path below needs to change
            File file =
                InstalledFileLocator.getDefault().locate("/modules/autoload/ext/" + resource, null,
                    false);
            url = file.toURL();
        }

        return url;
    }

    public InputStream getResourceAsStream(String path) {
        ClassLoader loader = getClass().getClassLoader();
        String fqpath = "com/sun/rave/jsfsupp/container" + path;

        return loader.getResourceAsStream(fqpath);
    }

    public String getPortletContextName() {
        return "RavePortletContext";
    }

    public String getServerInfo() {
        return "RavePortletContext";
    }

//    public PortletRequestDispatcher getNamedDispatcher(String name) {
//        throw new UnsupportedOperationException();
//    }
//
//    public PortletRequestDispatcher getRequestDispatcher(String path) {
//        throw new UnsupportedOperationException();
//    }

    public java.util.Set getResourcePaths(String path) {
        throw new UnsupportedOperationException();
    }

    public void log(String msg) {
        throw new UnsupportedOperationException();
    }

    public void log(String message, Throwable throwable) {
        throw new UnsupportedOperationException();
    }
}

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

import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * <p>Provides a <code>ServletConfig</code> (v 2.3) object for design-time use</p>
 *
 * @author Robert Brewin
 * @version 1.0
 */
public class RaveServletConfig implements ServletConfig {

    /**
     * Hashtable to store configuration parameters
     */
    private Hashtable parameters = new Hashtable();

    /**
     * The ServletContext associated with this configuration
     */
    private ServletContext context;

    /**
     * Public constructor for the <em>RaveServletConfig</em> class, initialized
     * with a <em>ServletContext</em> object
     *
     * @param context The <em>ServletContext</em> object used to initialize this configuration object
    */
    public RaveServletConfig(ServletContext context) {
        setServletContext(context);
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Add an initialization parameter
     *
     * @param name The name associated with this parameter
     * @param value The value for this parameter
     */
    public void addInitParameter(String name, String value) {
        parameters.put(name, value);
    }

    // -------------------------------------------------- ServletConfig Methods

    /**
     * Get the initialization parameter
     *
     * @param name The name of the parameter to return
     * @return the parameter value
     */
    public String getInitParameter(String name) {
        return ((String) parameters.get(name));
    }

    /**
     * Get the parameter names
     *
     * @return An Enumeration of the parameter names
     */
    public Enumeration getInitParameterNames() {
        return (parameters.keys());
    }

    /**
     * Set the servlet context
     *
     * @param context The servlet context
     */
    public void setServletContext(ServletContext context) {
        this.context = context;
    }

    /**
     * Get the servlet context for this configuration
     *
     * @return the <em>ServletContext</em> for this configuration
     */
    public ServletContext getServletContext() {
        return (this.context);
    }

    /**
     * Get the Servlet name
     *
     * @return A <em>String</em>containing the servlet name
     */
    public String getServletName() {
        return ("MockServlet");
    }
}

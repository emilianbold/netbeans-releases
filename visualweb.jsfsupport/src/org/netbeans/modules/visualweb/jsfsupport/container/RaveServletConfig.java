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

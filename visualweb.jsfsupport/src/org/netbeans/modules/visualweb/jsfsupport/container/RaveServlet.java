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

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * <p>Rave <em>Servlet</em> for hosting JSF design</p>
 *
 * @author Robert Brewin
 * @version 1.0
 */
public class RaveServlet implements Servlet {

    /**
     * Holds the ServletConfig object for this Servlet
     */
    private ServletConfig servletConfig;

    /**
     * Get the configuration object
     *
     * @return the <em>ServletConfig</em> object
     */
    public ServletConfig getServletConfig() {
        return this.servletConfig;
    }

    /**
     * Set the <em>ServletConfig</em> object for this <em>RaveServlet</em>
     *
     * @param config The new <em>ServletConfig</em> object
     */
    public void setServletConfig(ServletConfig config) {
        this.servletConfig = config;
    }

    /**
     * Get information about this servlet
     *
     * @return the ServletInfo for this mock Servlet
     */
    public String getServletInfo() {
        return "MockServlet";
    }

    /**
     * Construct the basic mock Servlet
     */
    public RaveServlet() {
    }

    /**
     * Construct a mock Servlet initialized with a specific ServletConfig object
     */
    public RaveServlet(ServletConfig config) throws ServletException {
        init(config);
    }

    /**
     * Destroy this mock Servlet
     */
    public void destroy() {
    }

    /**
     * Initialize this servlet with a specific configuration object
     *
     * @param config The <em>ServletConfig</em> object to use
     *
     * @exception javax.servlet.ServletException on an error during initialization
     */
    public void init(ServletConfig config) throws ServletException {
        this.servletConfig = config;
    }

    /**
     * Process a given request.  At present, this is a <em>no-op</em> for this
     * mock environment and will throw an <em>UnsupportedOperationException</em>
     *
     * @param request The <em>ServletRequest</em> object
     * @param response The <em>ServletResponse</em> object
     *
     * @exception java.io.IOException required by the Servlet interface
     * @exception javax.servlet.ServletException required by the Servlet interface
     * @exception java.lang.UnsupportedOperationException thrown if invoked at present
     */
    public void service(ServletRequest request, ServletResponse response)
        throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }
}

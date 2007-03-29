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

package org.netbeans.modules.j2ee.deployment.plugins.spi;

import java.io.File;

/** This interface allows plugins to specify the location of servlets generated
 * for JSPs.
 *
 * @author Petr Jiricka
 */
public interface FindJSPServlet {

    /** Returns the temporary directory where the server writes servlets generated
     * from JSPs. The servlets placed in this directory must honor the Java
     * directory naming conventions, i.e. the servlet must be placed in subdirectories
     * of this directory corresponding to the servlet package name.
     * @param moduleContextPath web module for which the temporary directory is requested.
     * @return the root temp directory containing servlets generated from JSPs for this module.
     */
    public File getServletTempDirectory(String moduleContextPath);
    
    /** Returns the resource path of the servlet generated for a particular JSP, relatively
     * to the main temporary directory.
     * @param moduleContextPath context path of web module in which the JSP is located.
     * @param jspResourcePath the path of the JSP for which the servlet is requested, e.g.
     *  "pages/login.jsp". Never starts with a '/'.
     * @return the resource name of the servlet generated for the JSP in the module, e.g.
     *  "org/apache/jsps/pages/login$jsp.java". Must never start with a '/'.
     *  The servlet file itself does not need to exist at this point - 
     *  if this particular page was not compiled yet.
     */
    public String getServletResourcePath(String moduleContextPath, String jspResourcePath);
    
    /** Returns the encoding of the generated servlet file.
     * @param moduleContextPath context path of web module in which the JSP is located.
     * @param jspResourcePath the path of the JSP for which the servlet is requested, e.g.
     *  "pages/login.jsp". Never starts with a '/'.
     * @return the encoding of the servlet generated for the JSP in the module, 
     *  e.g. "UTF8".
     *  The servlet file itself does not need to exist at this point -
     *  if this particular page was not compiled yet.
     */
    public String getServletEncoding(String moduleContextPath, String jspResourcePath);
    
}

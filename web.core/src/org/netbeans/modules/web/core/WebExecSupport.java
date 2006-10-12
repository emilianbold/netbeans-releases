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

package org.netbeans.modules.web.core;

import java.io.IOException;
import java.util.List;
import org.netbeans.modules.web.api.webmodule.WebFrameworkSupport;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.core.jsploader.JspCompileUtil;
import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;

import org.openide.filesystems.*;
import org.netbeans.modules.web.spi.webmodule.RequestParametersProvider;

/** Static methods for execution parameters.
*
* @author Petr Jiricka
*/
public class WebExecSupport implements RequestParametersProvider {

    public static final String EA_REQPARAMS = "NetBeansAttrReqParams"; // NOI18N

    /* Sets execution query string for the associated entry.
    * @param qStr the query string
    * @exception IOException if arguments cannot be set
    */
    public static void setQueryString(FileObject fo, String qStr) throws IOException {
        fo.setAttribute (EA_REQPARAMS, qStr);
    }

    /* Getter for query string associated with given file.
    * @return the query string or empty string if no quesy string associated
    */
    public static String getQueryString(FileObject fo) {
        try {
            String qStr = (String)fo.getAttribute (EA_REQPARAMS);
            if (qStr != null) {
                if ((qStr.length() > 0) && (!qStr.startsWith("?"))) // NOI18N
                    qStr = "?" + qStr; // NOI18N
                return qStr;
            }
        } catch (Exception ex) {
            // null pointer or IOException
        }
        return ""; // NOI18N
    }

    public String getFileAndParameters(FileObject f) {
        
        List <WebFrameworkProvider> frameworkProviders = WebFrameworkSupport.getFrameworkProviders(); 
        String url = null;
        WebModule wm = WebModule.getWebModule(f);
        if (wm != null && frameworkProviders.size() > 0){
            for ( WebFrameworkProvider frameworkProvider : frameworkProviders) {
                if (frameworkProvider.isInWebModule(wm)){
                    url = frameworkProvider.getServletPath(f);
                    if (url != null)
                        break;
                }
            }
        }
        if (url == null)
            url = JspCompileUtil.findRelativeContextPath(WebModule.getWebModule (f).getDocumentBase (), f);
        url = url + getQueryString(f);
        url = org.openide.util.Utilities.replaceString(url, " ", "%20");
        return url;
    }
}


/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core;

import java.io.IOException;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.core.jsploader.JspCompileUtil;

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
        String url = JspCompileUtil.findRelativeContextPath(WebModule.getWebModule (f).getDocumentBase (), f);
        url = url + getQueryString(f);
        url = org.openide.util.Utilities.replaceString(url, " ", "%20");
        return url;
    }
}


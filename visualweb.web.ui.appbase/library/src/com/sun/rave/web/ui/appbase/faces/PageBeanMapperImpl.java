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


package com.sun.rave.web.ui.appbase.faces;

import java.util.HashSet;
import java.util.Set;

/**
 * <p>Default implementation of {@link PageBeanMapper} that corresponds to
 * the mapping performed by the IDE at design time.  This implementation
 * uses the following rules:</p>
 * <ul>
 * <li>Strip the leading '/' character (if any).</li>
 * <li>String the trailing extension (if any).</li>
 * <li>Replace any '/' characters with '$' characters</li>
 * <li>If the resulting string matches the name of one of the implicit
 *     variables recognized by JSF EL expressions, prefix it with '_'.
 * </ul>
 */
public class PageBeanMapperImpl implements PageBeanMapper {


    // ------------------------------------------------------- Static Variables


    /**
     * <p>The set of reserved identifiers recognized by the JavaServer Faces
     * expression language machinery.</p>
     */
    private static Set reserved = new HashSet();

    static {
        reserved.add("applicationScope"); //NOI18N
        reserved.add("cookies");          //NOI18N
        reserved.add("facesContext");     //NOI18N
        reserved.add("header");           //NOI18N
        reserved.add("headerValues");     //NOI18N
        reserved.add("initParam");        //NOI18N
        reserved.add("param");            //NOI18N
        reserved.add("paramValues");      //NOI18N
        reserved.add("requestScope");     //NOI18N
        reserved.add("sessionScope");     //NOI18N
        reserved.add("view");             //NOI18N
    };

    // ------------------------------------------------- PageBeanMapper Methods


    /**
     * <p>Map the specified view identifier (which will be the context relative
     * path of a JSP page) to the managed bean name of the corresponding
     * page bean (which must extend {@link AbstractPageBean}).</p>
     *
     * @param viewId View identifier of the JSP page to be mapped
     */
    public String mapViewId(String viewId) {

        if ((viewId == null) || (viewId.length() < 1)) {
            return viewId;
        }

        // Strip any leading '/' character
        if (viewId.charAt(0) == '/') { //NOI18N
            viewId = viewId.substring(1);
        }

        // Strip any trailing extension
        int slash = viewId.lastIndexOf('/'); //NOI18N
        int period = viewId.lastIndexOf('.'); //NOI18N
        if (period >= 0) {
            if ((slash < 0) || (period > slash)) {
                viewId = viewId.substring(0, period);
            }
        }

        // Replace remaining '/' characters with '$'
        viewId = viewId.replace('/', '$'); //NOI18N

        // Prefix with '_' if necessary, and return
        if (reserved.contains(viewId)) {
            return "_" + viewId;
        } else {
            return viewId;
        }

    }


}

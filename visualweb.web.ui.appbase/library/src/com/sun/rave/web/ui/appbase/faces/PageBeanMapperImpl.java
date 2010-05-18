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

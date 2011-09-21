/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.core.syntax.folding;

import org.netbeans.api.editor.fold.FoldType;

/**
 * This class defines @see org.netbeans.api.editor.fold.FoldType 
 * instancies used in JSP code folding.
 *
 * @author  mf100882
 */

public class JspFoldTypes {
    
    private static final String FOLD_TYPE_PREFIX = "jsp-";//NOI18N
    
    /** JSP directive fold type */
    public static final FoldType DIRECTIVE = new FoldType(FOLD_TYPE_PREFIX + "directives"); // NOI18N
   
    /** JSP tag fold type */
    public static final FoldType TAG = new FoldType(FOLD_TYPE_PREFIX + "tag"); // NOI18N
    
    /** JSP comment fold type */
    public static final FoldType COMMENT = new FoldType(FOLD_TYPE_PREFIX + "comment"); // NOI18N

    /** JSP declaration fold type */
    public static final FoldType DECLARATION = new FoldType(FOLD_TYPE_PREFIX + "declaration"); // NOI18N
    
    /** JSP scriptlet fold type */
    public static final FoldType SCRIPTLET = new FoldType(FOLD_TYPE_PREFIX + "scriptlet"); // NOI18N
    
    //there isn't any fold type for JSP expression
    
    /** JSP directive fold type description */
    public  static final String DIRECTIVE_DESCRIPTION = "<%@...%>"; // NOI18N

    /** JSP tag fold type description */
    public static final String TAG_DESCRIPTION = "<jsp:...>"; // NOI18N

    /** JSP comment fold type description */
    public static final String COMMENT_DESCRIPTION = "<%--...--%>"; // NOI18N
    
    /** JSP declaration fold type description */
    public static final String DECLARATION_DESCRIPTION = "<%!...%>"; // NOI18N
    
    /** JSP scriptlet fold type description */
    public static final String SCRIPTLET_DESCRIPTION = "<%...%>"; // NOI18N
    
    //html fold types
    
    private static final String HTML_FOLD_TYPE_PREFIX = "html-";//NOI18N
    
    /** HTML comment fold type */
    public static final FoldType HTML_COMMENT = new FoldType(HTML_FOLD_TYPE_PREFIX + "comment"); // NOI18N
    
    /** HTML tag fold type */
    public static final FoldType HTML_TAG = new FoldType(HTML_FOLD_TYPE_PREFIX + "tag"); // NOI18N
    
    /** HTML comment fold type default description */
    public static final String HTML_COMMENT_DESCRIPTION = "<!--...-->"; // NOI18N
    
    /** HTML tag fold type default description */
    public static final String HTML_TAG_DESCRIPTION = "<...>"; // NOI18N
}

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
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

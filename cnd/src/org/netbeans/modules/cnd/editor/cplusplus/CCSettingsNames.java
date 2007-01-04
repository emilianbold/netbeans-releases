/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.editor.cplusplus;

import org.netbeans.editor.ext.ExtSettingsNames;

/**
* Names of the cc editor settings.
*
*/

public class CCSettingsNames extends ExtSettingsNames {

    /** Whether insert extra space before the parenthesis or not.
    * Values: java.lang.Boolean instances
    * Effect: function(a)
    *           becomes (when set to true)
    *         function (a)
    */
    public static final String FORMAT_SPACE_BEFORE_PARENTHESIS
    = "cc-add-space-before-parenthesis"; //NOI18N

    /** Whether insert space after the comma inside the parameter list.
    * Values: java.lang.Boolean instances
    * Effect: function(a,b)
    *           becomes (when set to true)
    *         function(a, b)
    *
    */
    public static final String FORMAT_SPACE_AFTER_COMMA
    = "cc-add-space-after-comma"; //NOI18N

    /** Whether insert extra new-line before the compound bracket or not.
    * Values: java.lang.Boolean instances
    * Effect: if (test) {
    *           function();
    *         }
    *           becomes (when set to true)
    *         if (test)
    *         {
    *           function();
    *         }
    */
    public static final String FORMAT_NEWLINE_BEFORE_BRACE
    = "cc-add-newline-before-brace"; //NOI18N
    
    /** Whether to indent preprocessors positioned at start of line.
     * Those not starting at column 0 of the line will automatically be indented.
     * This setting is to prevent C/C++ code that is compiled with compilers that
     * require the processors to have '#' in column 0.
     * <B>Note:</B>This will not convert formatted preprocessors back to column 0.
     **/
    public static final String FORMAT_PREPROCESSOR_AT_LINE_START
    = "cc-keep-preprocessor-at-line-start"; //NOI18N    

    /** String to use to construct a URL to fetch documentation pages from:
    */
    public static final String DOCUMENTATION_URLBASE =
	"cc-doc-urlbase"; //NOI18N
    
    /** Whether the code completion window should be lower case
    *  Values: java.lang.Boolean
    */
    public static final String COMPLETION_LOWER_CASE = "completion-lower-case"; // NOI18N
    
    public static final String CODE_FOLDING_UPDATE_TIMEOUT = "code-folding-update-interval"; //NOI18N
    
    /** Completion of { } [ ] " " ' '  */
    public static final String PAIR_CHARACTERS_COMPLETION = "pair-characters-completion"; // NOI18N
    
}

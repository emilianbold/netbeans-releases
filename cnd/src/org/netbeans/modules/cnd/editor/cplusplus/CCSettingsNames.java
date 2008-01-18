/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.cnd.editor.cplusplus;

import org.netbeans.editor.ext.ExtSettingsNames;

/** Names of the cc editor settings */
public class CCSettingsNames extends ExtSettingsNames {

    /**
     * Whether insert extra space before the parenthesis or not.
     * Values: java.lang.Boolean instances
     * Effect: function(a)
     *           becomes (when set to true)
     *         function (a)
     */
    public static final String CC_FORMAT_SPACE_BEFORE_PARENTHESIS  = 
            "cc-add-space-before-parenthesis"; //NOI18N

    /**
     * Whether insert space after the comma inside the parameter list.
     * Values: java.lang.Boolean instances
     * Effect: function(a,b)
     *           becomes (when set to true)
     *         function(a, b)
     */
    public static final String CC_FORMAT_SPACE_AFTER_COMMA =
            "cc-add-space-after-comma"; //NOI18N

    /**
     * Whether insert extra new-line before the compound bracket or not.
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
    public static final String CC_FORMAT_NEWLINE_BEFORE_BRACE =
            "cc-add-newline-before-brace"; //NOI18N
    
    /**
     * Whether insert extra new-line before the declaration or not.
     * Values: java.lang.Boolean instances
     * Effect: int foo() {
     *           function();
     *         }
     *           becomes (when set to true)
     *         int foo(test)
     *         {
     *           function();
     *         }
     */
    public static final String CC_FORMAT_NEWLINE_BEFORE_BRACE_DECLARATION =
            "cc-add-newline-before-brace-declaratin"; //NOI18N

    /**
     * Whether to indent preprocessors positioned at start of line.
     * Those not starting at column 0 of the line will automatically be indented.
     * This setting is to prevent C/C++ code that is compiled with compilers that
     * require the processors to have '#' in column 0.
     * <B>Note:</B>This will not convert formatted preprocessors back to column 0.
     */
    public static final String CC_FORMAT_PREPROCESSOR_AT_LINE_START =
            "cc-keep-preprocessor-at-line-start"; //NOI18N

    /**
     * Add one more space to the begining of each line
     * in the multi-line comment if it's not already there.
     * Values: java.lang.Boolean
     * Effect: For example in java:
     *
     *        /* this is
     *        *  multiline comment
     *        *\/
     *
     *            becomes (when set to true)
     *
     *         /* this is
     *          * multiline comment
     *          *\/
     */
    public static final String CC_FORMAT_LEADING_SPACE_IN_COMMENT
            = "cc-format-leading-space-in-comment"; // NOI18N

    /** Whether the '*' should be added at the new line * in comment */
    public static final String CC_FORMAT_LEADING_STAR_IN_COMMENT
            = "cc-format-leading-star-in-comment"; // NOI18N
    
    /**
     * How many spaces should be added to the statement that continues
     * on the next line.
     */
    public static final String CC_FORMAT_STATEMENT_CONTINUATION_INDENT
            = "cc-format-statement-continuation-indent"; // NOI18N 

    /**
     * Whether the code completion window should be lower case
     *  Values: java.lang.Boolean
     */
    public static final String COMPLETION_LOWER_CASE = "cc-completion-lower-case"; // NOI18N
    
    public static final String CODE_FOLDING_UPDATE_TIMEOUT = "cc-code-folding-update-interval"; //NOI18N
    
}

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

package org.netbeans.editor.ext.java;

import org.netbeans.editor.ext.ExtSettingsNames;

/**
* Names of the java editor settings.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class JavaSettingsNames extends ExtSettingsNames {

    /** Whether insert extra space before the parenthesis or not.
    * Values: java.lang.Boolean instances
    * Effect: function(a)
    *           becomes (when set to true)
    *         function (a)
    */
    public static final String JAVA_FORMAT_SPACE_BEFORE_PARENTHESIS
    = "java-format-space-before-parenthesis"; // NOI18N

    /** Whether insert space after the comma inside the parameter list.
    * Values: java.lang.Boolean instances
    * Effect: function(a,b)
    *           becomes (when set to true)
    *         function(a, b)
    *
    */
    public static final String JAVA_FORMAT_SPACE_AFTER_COMMA
    = "java-format-space-after-comma"; // NOI18N

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
    public static final String JAVA_FORMAT_NEWLINE_BEFORE_BRACE
    = "java-format-newline-before-brace"; // NOI18N

    /** Add one more space to the begining of each line
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
    public static final String JAVA_FORMAT_LEADING_SPACE_IN_COMMENT
    = "java-format-leading-space-in-comment"; // NOI18N

    /** Whether the '*' should be added at the new line
     * in comment.
     */
    public static final String JAVA_FORMAT_LEADING_STAR_IN_COMMENT
    = "java-format-leading-star-in-comment"; // NOI18N
    
    /**
     * How many spaces should be added to the statement that continues
     * on the next line.
     */
    public static final String JAVA_FORMAT_STATEMENT_CONTINUATION_INDENT
        = "java-format-statement-continuation-indent"; // NOI18N

    /** Completion of { } [ ] " " ' '  */
    public static final String PAIR_CHARACTERS_COMPLETION 
      = "pair-characters-completion"; // NOI18N

    /** Collapse methods by default */
    public static final String CODE_FOLDING_COLLAPSE_METHOD = "code-folding-collapse-method"; //NOI18N
    
    /** Collapse inner classes by default */
    public static final String CODE_FOLDING_COLLAPSE_INNERCLASS = "code-folding-collapse-innerclass"; //NOI18N
    
    /** Collapse import section default */
    public static final String CODE_FOLDING_COLLAPSE_IMPORT = "code-folding-collapse-import"; //NOI18N
    
    /** Collapse javadoc comment by default */
    public static final String CODE_FOLDING_COLLAPSE_JAVADOC = "code-folding-collapse-javadoc"; //NOI18N

    /** Collapse initial comment by default */
    public static final String CODE_FOLDING_COLLAPSE_INITIAL_COMMENT = "code-folding-collapse-initial-comment"; //NOI18N

    /** Whether go to class is case sensitive */
    public static final String GOTO_CLASS_CASE_SENSITIVE = "goto-class-case-sensitive"; //NOI18N

    /** Whether go to class shows inner classes */
    public static final String GOTO_CLASS_SHOW_INNER_CLASSES = "goto-class-show-inner-classes"; //NOI18N

    /** Whether go to class shows library classes */
    public static final String GOTO_CLASS_SHOW_LIBRARY_CLASSES = "goto-class-show-library-classes"; //NOI18N

}

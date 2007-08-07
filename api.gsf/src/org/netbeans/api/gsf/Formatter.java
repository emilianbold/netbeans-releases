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
package org.netbeans.api.gsf;

import javax.swing.text.Caret;
import javax.swing.text.Document;

import org.netbeans.api.gsf.ParserResult;


/**
 * Implementations of this interface can be registered such that the formatter
 * helps indent or reformat source code, or even determine where the caret should
 * be placed on a newly created line.
 *
 * @author Tor Norbye
 */
public interface Formatter {
    /**
     * Reformat the given portion of source code from startOffset to endOffset in the document.
     * You may use the provided parse tree information, if available, to guide formatting decisions.
     * The caret (if any) should be updated to the corresponding position that it was at before formatting.     * 
     */
    void reformat(Document doc, int startOffset, int endOffset, ParserResult result, FormattingPreferences preferences);

    /**
     * Reindent the source code. Adjusts indentation and strips trailing whitespace but
     * does not otherwise change the code. The caret (if any) should be updated to the corresponding
     * position that it was at before formatting.
     */
    void reindent(Document doc, int startOffset, int endOffset, ParserResult result,
        FormattingPreferences preferences);

    /**
     * Return the preferred size in characters of each indentation level for this language.
     * This is not necessarily going to mean spaces since the IDE may use tabs to perform
     * part of the indentation, but the number should reflect the number of spaces it would
     * visually correspond to. For example, the Sun JDK Java style guidelines would return
     * "4" here, and Ruby would return "2".
     *
     * @return The size in characters of each indentation level.
     */
    int indentSize();
    
    /**
     * Return the preferred "hanging indent" size, the amount of space to indent a continued
     * line such as the second line here:
     * <pre>
     *   foo = bar +
     *       baz
     * </pre>
     * The hanging indent is the indentation level difference between "baz" and "foo"
     */
    int hangingIndentSize();
}

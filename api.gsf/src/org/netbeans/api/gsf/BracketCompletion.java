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

import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.api.gsf.annotations.NonNull;


/**
 * Interface that a plugin can implement to assist with bracket completion during
 * editing.
 *
 * @todo Rename Pair Completion? Or perhaps PairMatcher?
 *
 * @author Tor Norbye
 */
public interface BracketCompletion {
    /**
     * (Based on BracketCompletion class in NetBeans' java editor support)
     *
     * A hook method called after a character was inserted into the
     * document. The function checks for special characters for
     * completion ()[]'"{} and other conditions and optionally performs
     * changes to the doc and or caret (complets braces, moves caret,
     * etc.)
     *
     * Return true if the character was already inserted (and the IDE
     * should not further insert anything)
     *
     * XXX Fix javadoc.
     */
    boolean beforeCharInserted(Document doc, int caretOffset, Caret caret, char ch)
        throws BadLocationException;

    /** @todo Rip out the boolean return value? What does it mean? */
    boolean afterCharInserted(Document doc, int caretOffset, Caret caret, char ch)
        throws BadLocationException;

    /**
     * (Based on BracketCompletion class in NetBeans' java editor support)
     *
     * Hook called after a character *ch* was backspace-deleted from
     * *doc*. The function possibly removes bracket or quote pair if
     * appropriate.
     * @todo Document why both caretOffset and caret is passed in!
     * Return the new offset, or -1
     */

    /** @todo Split into before and after? */
    public boolean charBackspaced(Document doc, int caretOffset, Caret caret, char ch)
        throws BadLocationException;

    /**
     * A line break is being called. Return -1 to do nothing.
     * If you want to modify the document first, you can do that, and then
     * return the new offset to assign the caret to AFTER the newline has been
     * inserted.
     *
     * @todo rip out return value
     * @todo Document why both caretOffset and caret is passed in!
     */
    int beforeBreak(Document doc, int caretOffset, Caret caret)
        throws BadLocationException;

    /**
     * Compute a range matching the caret position. If no eligible range
     * is found, return {@link OffsetRange.NONE}.
     */
    @NonNull
    OffsetRange findMatching(Document doc, int caretOffset);
    
    /**
     * Compute set of selection ranges for the given parse tree (around the given offset),
     * in leaf-to-root order.
     */
    @NonNull
    List<OffsetRange> findLogicalRanges(CompilationInfo info, int caretOffset);
}

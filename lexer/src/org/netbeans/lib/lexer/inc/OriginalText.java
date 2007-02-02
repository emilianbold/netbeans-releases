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

package org.netbeans.lib.lexer.inc;

import javax.swing.BoundedRangeModel;
import org.netbeans.lib.editor.util.AbstractCharSequence;
import org.netbeans.lib.editor.util.CharSubSequence;

/**
 * Character sequence emulating state of a mutable input source
 * before the last modification.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class OriginalText extends AbstractCharSequence.StringLike {

    private final CharSequence currentText;

    private final int offset;

    private final int insertedTextLength;

    private final CharSequence removedText;

    private final int origLength;

    public OriginalText(CharSequence currentText, int offset, CharSequence removedText, int insertedTextLength) {
        this.currentText = currentText;
        this.offset = offset;
        this.removedText = (removedText != null) ? removedText : ""; // always non-null
        this.insertedTextLength = insertedTextLength;

        this.origLength = currentText.length() - insertedTextLength + this.removedText.length();
    }

    public int length() {
        return origLength;
    }

    public char charAt(int index) {
        if (index < offset) {
            return currentText.charAt(index);
        }
        index -= offset;
        if (index < removedText.length()) {
            return removedText.charAt(index);
        }
        return currentText.charAt(offset + index - removedText.length() + insertedTextLength);
    }

    public char[] toCharArray(int start, int end) {
        char[] chars = new char[end - start];
        int charsIndex = 0;
        if (start < offset) {
            int bound = (end < offset) ? end : offset;
            while (start < bound) {
                chars[charsIndex++] = currentText.charAt(start++);
            }
            if (end == bound) {
                return chars;
            }
        }
        start -= offset;
        end -= offset;
        int bound = removedText.length();
        if (start < bound) {
            if (end < bound) {
                bound = end;
            }
            while (start < bound) {
                chars[charsIndex++] = removedText.charAt(start++);
            }
            if (end == bound) {
                return chars;
            }
        }
        bound = offset - removedText.length() + insertedTextLength;
        start += bound;
        bound += end;
        while (start < bound) {
            chars[charsIndex++] = currentText.charAt(start++);
        }
        return chars;
    }

}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.util;

/**
 *
 * @author Marian Petras
 */
public class TextUtils {

    private TextUtils() {}

    /**
     * Shortens the given text to the given number of characters and adds
     * an ellipsis if appropriate. If the given string is too long to fit the
     * limit, it is shortened and an ellipsis is appended to signal that it
     * was shortened. Length of the resulting {@code String} thus can by longer
     * than the given limit because of the ellipsis and possible an extra space
     * before the ellipsis.
     *
     * @param  text  text to be shortened to fit into the given number
     *               of characters
     * @param  minWords  try to output at least the given number of words,
     *                   even if the last word should be truncated
     * @param  limit  maximum number of characters of the trimmed message
     *
     * @return
     */
    public static String shortenText(String text,
                                     final int minWords,
                                     final int limit) {
        if (text == null) {
            throw new IllegalArgumentException("text must be non-null");//NOI18N
        }
        if (minWords < 1) {
            throw new IllegalArgumentException(
                    "minimum number of words must be positive");        //NOI18N
        }
        if (limit < 1) {
            throw new IllegalArgumentException(
                    "limit must be positive - was: " + limit);          //NOI18N
        }

        text = trimSpecial(text);

        int length = text.length();
        if (length <= limit) {
            return text;
        }

        int wordCount = 0;
        int lastWordEndIndex = -1;
        boolean lastWasSpace = false;
        for (int i = 1; i < limit; i++) {
            boolean isSpace = isSpace(text.charAt(i));
            if (isSpace && !lastWasSpace) {
                lastWordEndIndex = i;
                wordCount++;
            }
            lastWasSpace = isSpace;
        }

        int endIndex;
        boolean wholeWords;

        if (wordCount >= minWords) {
            endIndex = lastWordEndIndex;
            wholeWords = true;
        } else if (lastWasSpace) {
            /* the for-cycle ended in a space between the first two words */
            endIndex = lastWordEndIndex;
            wholeWords = true;
        } else {
            endIndex = limit;
            if (isSpace(text.charAt(limit))) {
                /* the for-cycle ended just after the second word */
                wholeWords = true;
            } else {
                /* the for-cycle ended in the middle of the second word */
                wholeWords = false;
            }
        }

        StringBuilder buf = new StringBuilder(endIndex + 4);
        buf.append(text.substring(0, endIndex));
        if (wholeWords) {
            buf.append(' ');
        }
        buf.append("...");                                              //NOI18N
        return buf.toString();
    }

    /**
     * Trims the given text by removing all leading and trailing <em>space
     * characters</em>. Unlike the known method {@link java.lang.String#trim},
     * this method removes also tabs and all characters for which method
     * {@link java.lang.Character#isSpaceChar(char)} returns {@code true}.
     * @param  str  string to be trimmed
     * @return  the trimmed string
     *          (may be the original string if no trimming was needed)
     */
    public static String trimSpecial(String str) {
        if (str == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }

        int length = str.length();

        int beginIndex, endIndex;

        int index;

        index = 0;
        while ((index < length) && isSpace(str.charAt(index))) {
            index++;
        }

        if (index == length) {
            /* there were just space characters in the string */
            return "";                                                  //NOI18N
        }

        beginIndex = index;

        index = length - 1;
        while (isSpace(str.charAt(index))) {
            index--;
        }

        endIndex = index + 1;

        return str.substring(beginIndex, endIndex);
    }

    private static boolean isSpace(char ch) {
        return (ch == '\t') || Character.isSpaceChar(ch);
    }

}

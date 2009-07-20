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

package org.netbeans.modules.php.api.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.openide.util.Parameters;

/**
 * Miscellaneous string utilities.
 * @author Tomas Mysik
 */
public final class StringUtils {

    private StringUtils() {
    }

    /**
     * Return <code>true</code> if the String is not <code>null</code>
     * and has any character after trimming.
     * @param input input <tt>String</tt>, can be <code>null</code>.
     * @return <code>true</code> if the String is not <code>null</code>
     *         and has any character after trimming.
     */
    public static boolean hasText(String input) {
        return input != null && input.trim().length() > 0;
    }

    /**
     * Implode list of strings to one string using delimiter.
     * @param items string to be imploded
     * @param delimiter delimiter to be used
     * @return one string of imploded strings using delimiter, never <code>null</code>
     * @see #explode(String, String)
     */
    public static String implode(List<String> items, String delimiter) {
        Parameters.notNull("items", items);
        Parameters.notNull("delimiter", delimiter);

        if (items.isEmpty()) {
            return ""; // NOI18N
        }

        StringBuilder buffer = new StringBuilder(200);
        boolean first = true;
        for (String s : items) {
            if (!first) {
                buffer.append(delimiter);
            }
            buffer.append(s);
            first = false;
        }
        return buffer.toString();
    }

    /**
     * Explode the string using the delimiter.
     * @param string string to be exploded, can be <code>null</code>
     * @param delimiter delimiter to be used
     * @return list of exploded strings using delimiter
     * @see #implode(List, String)
     */
    public static List<String> explode(String string, String delimiter) {
        Parameters.notNull("delimiter", delimiter); // NOI18N

        if (!hasText(string)) {
            return Collections.<String>emptyList();
        }
        return Arrays.asList(string.split(Pattern.quote(delimiter)));
    }

    /**
     * Get the case-insensitive {@link Pattern pattern} for the given <tt>String</tt>
     * or <code>null</code> if it does not contain any "?" or "*" characters.
     * <p>
     * This pattern is "unbounded", it means that the <tt>text</tt> can be anywhere
     * in the matching string. See {@link #getExactPattern(String)} for pattern matching the whole string.
     * @param text the text to get {@link Pattern pattern} for
     * @return the case-insensitive {@link Pattern pattern} or <code>null</code>
     *         if the <tt>text</tt> does not contain any "?" or "*" characters
     * @since 1.6
     * @see #getExactPattern(String)
     */
    public static Pattern getPattern(String text) {
        Parameters.notNull("text", text); // NOI18N

        return getPattern0(text, ".*", ".*"); // NOI18N
    }

    /**
     * Get the case-insensitive {@link Pattern pattern} for the given <tt>String</tt>
     * or <code>null</code> if it does not contain any "?" or "*" characters.
     * <p>
     * This pattern exactly matches the string, it means that the <tt>text</tt> must be fully matched in the
     * matching string. See {@link #getPattern(String)} for pattern matching any substring in the matching string.
     * @param text the text to get {@link Pattern pattern} for
     * @return the case-insensitive {@link Pattern pattern} or <code>null</code>
     *         if the <tt>text</tt> does not contain any "?" or "*" characters
     * @since 1.6
     * @see #getPattern(String)
     */
    public static Pattern getExactPattern(String text) {
        Parameters.notNull("text", text); // NOI18N

        return getPattern0(text, "^", "$"); // NOI18N
    }

    private static Pattern getPattern0(String text, String prefix, String suffix) {
        assert text != null;
        assert prefix != null;
        assert suffix != null;

        if (text.contains("?") || text.contains("*")) { // NOI18N
            String pattern = text.replace("\\", "") // remove regexp escapes first // NOI18N
                    .replace(".", "\\.") // NOI18N
                    .replace("-", "\\-") // NOI18N
                    .replace("(", "\\(") // NOI18N
                    .replace(")", "\\)") // NOI18N
                    .replace("[", "\\[") // NOI18N
                    .replace("]", "\\]") // NOI18N
                    .replace("?", ".") // NOI18N
                    .replace("*", ".*"); // NOI18N
            return Pattern.compile(prefix + pattern + suffix, Pattern.CASE_INSENSITIVE); // NOI18N
        }
        return null;
    }
}

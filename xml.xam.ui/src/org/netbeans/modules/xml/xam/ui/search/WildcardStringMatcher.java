/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.xml.xam.ui.search;

/**
 * A string matcher that supports a small set of wildcards (e.g. * and ?).
 * See the documentation for the <code>match()</code> method for details.
 *
 * @see #match(String, String)
 * @author Nathan Fiedler
 */
public class WildcardStringMatcher {

    /**
     * Creates a new instance of WildcardStringMatcher.
     */
    private WildcardStringMatcher() {
    }

    /**
     * Determines if the given query string contains wildcards that this
     * string matcher understands.
     *
     * @param  query  query string, which may or may not contain wildcards.
     * @return  true if query has recognized wildcards, false otherwise.
     */
    public static boolean containsWildcards(String query) {
        return query.contains("*") || query.contains("?");
    }

    /**
     * Scans the given text for a pattern, indicating if the text matched
     * the pattern or not. The pattern wildcards are as follows:
     *
     * <ul>
     *   <li>? matches any single character.</li>
     *   <li>* matches zero or more characters.</li>
     * </ul>
     *
     * <p>The text must match the entire pattern, or it is not considered
     * a match. That is, if "floss*" is the pattern, then it will only
     * match text that begins with "floss", while "floss?" will match
     * "flossy" but not "floss" since it expects an additional character.</p>
     *
     * <p>Note that * is greedy, such that "*foo" will match "foofoofoo".</p>
     *
     * @param  text   the text in which to look for the pattern.
     * @param  query  the pattern to match, may contain wildcards.
     * @return  true if matches, false otherwise.
     */
    public static boolean match(String text, String query) {
        int ti;
        int qi;
        int tl = text.length();
        int ql = query.length();
        boolean star = false;

        for (ti = 0, qi = 0; ti < tl; ti++, qi++) {
            // This line allows this algorithm to be greedy, such that
            // "*foo" will match "foofoofoo", and "*a" matches "aaa".
            char qc = qi < ql ? query.charAt(qi) : 0;
            switch (qc) {
                case '?':
                    // We allow question marks to match anything.
                    break;
                case '*':
                    star = true;
                    // Skip over consecutive asterisks.
                    do {
                        qi++;
                    } while (qi < ql && query.charAt(qi) == '*');
                    if (qi == ql) {
                        // Query ended with an asterisk, that makes a match.
                        return true;
                    }
                    // Simulate recursion on both substrings.
                    text = text.substring(ti);
                    query = query.substring(qi);
                    tl = text.length();
                    ql = query.length();
                    ti = -1;
                    qi = -1;
                    break;
                default:
                    char tc = text.charAt(ti);
                    if (tc != qc) {
                        if (!star) {
                            // No asterisk and not a match, exit immediately.
                            return false;
                        }
                        // Simulate recursion on the text substring.
                        text = text.substring(1);
                        tl--;
                        ti = -1;
                        qi = -1;
                    }
                    break;
            }
        }
        // Consume any trailing asterisks in query.
        while (qi < ql && query.charAt(qi) == '*') {
            qi++;
        }
        // It is a match only if we reached the ends of both strings.
        return ti >= tl && qi >= ql;
    }
}

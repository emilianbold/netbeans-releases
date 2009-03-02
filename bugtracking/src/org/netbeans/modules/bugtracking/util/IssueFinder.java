/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 *
 * @author Tomas Stupka
 * @author Marian Petras
 */
public class IssueFinder {

    private static final int[] EMPTY_INT_ARR = new int[0];

    private static final String NUM_RE = "\\#?[ \\t]*(\\d++)";          //NOI18N

    private static final String BUGWORDS = "bug,issue";                 //NOI18N

    private static final String BUGWORDS_RE = "(?:"                     //NOI18N
                                              + BUGWORDS.replace(',', '|') //NOI18N
                                              + ')'; //NOI18N

    private static final String BUG_RE = BUGWORDS_RE
                                         + "[ \\t]*(?:[\\r\\n]+[ \\t]*\\*?[ \\t]*)*" //NOI18N
                                         + NUM_RE;

    private static final String BUG_LINK_RE = "\\b" + BUG_RE;           //NOI18N

    private static final Pattern bugPattern = Pattern.compile(BUG_LINK_RE, CASE_INSENSITIVE);

    //private static final String COMMENTWORD_RE = "comment";
    //private static final String COMMENT_RE = COMMENTWORD_RE + "\\s*" + NUM_RE; //NOI18N
    //private static final String BUG_OR_COMMENT_LINK_RE
    //                            = "\\b"                                 //NOI18N
    //                                  + BUG_RE + "(?:\\s*,?\\s*" + COMMENT_RE + ")?" //NOI18N
    //                                  + "|"                             //NOI18N
    //                                  + COMMENT_RE;
    //private static final Pattern advancedPattern = Pattern.compile(BUG_OR_COMMENT_LINK_RE, CASE_INSENSITIVE);

    public static int[] getIssueSpans(String text) {
        Matcher matcher = bugPattern.matcher(text);

        if (!matcher.find()) {
            return EMPTY_INT_ARR;
        }

        int start = matcher.start();
        int end = matcher.end();
        if (!matcher.find()) {
            return new int[] {start, end};
        }

        List<Integer> bounds = new ArrayList<Integer>(6);
        do {
            start = matcher.start();
            end = matcher.end();
            bounds.add(Integer.valueOf(start));
            bounds.add(Integer.valueOf(end));
        } while (matcher.find());

        return toIntArray(bounds);
    }

    public static String getIssueNumber(String issueHyperlinkText) {
        Matcher matcher = bugPattern.matcher(issueHyperlinkText);

        if (!matcher.matches()) {
            throw new IllegalArgumentException();
        }

        return matcher.group(1);
    }

    private static int[] toIntArray(List<Integer> list) {
        if (list.isEmpty()) {
            return EMPTY_INT_ARR;
        }

        final int size = list.size();
        int[] result = new int[size];
        for (int i = 0; i < size; i++) {
            result[i] = list.get(i);        //auto-unboxing
        }
        return result;
    }

}

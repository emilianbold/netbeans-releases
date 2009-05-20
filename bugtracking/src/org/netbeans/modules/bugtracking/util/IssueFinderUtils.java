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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.modules.bugtracking.spi.IssueFinder;
import org.openide.util.Lookup;

/**
 *
 * @author Marian Petras
 */
public class IssueFinderUtils {

    private IssueFinderUtils() {}

    public static int[] getIssueSpans(String text) {
        Collection<? extends IssueFinder> issueFinders
                = Lookup.getDefault().lookupAll(IssueFinder.class);

        if (issueFinders.isEmpty()) {
            return new int[0];
        }

        int[] result = null;

        int issueFindersWithResults = 0;
        for (IssueFinder issueFinder : issueFinders) {
            int[] subresult = issueFinder.getIssueSpans(text);
            boolean resultsValid = checkIssueSpans(subresult,
                                                   text.length(),
                                                   issueFinder);
            if (!resultsValid || (subresult.length == 0)) {
                continue;
            }

            if (++issueFindersWithResults == 1) {
                result = subresult;
            } else {
                assert result != null;
                result = append(result, subresult);
            }
        }
        //PENDING: check for overlaping intervals if issueFindersWithResults > 1
        return (result != null) ? result : new int[0];
    }

    public static List<HyperlinkSpanInfo> getIssueSpansExt(String text) {
        Collection<? extends IssueFinder> issueFinders
                = Lookup.getDefault().lookupAll(IssueFinder.class);

        if (issueFinders.isEmpty()) {
            return Collections.emptyList();
        }

        List<HyperlinkSpanInfo> result = new ArrayList<HyperlinkSpanInfo>(4);

        int issueFindersWithResults = 0;
        for (IssueFinder issueFinder : issueFinders) {
            int[] subresult = issueFinder.getIssueSpans(text);
            boolean resultsValid = checkIssueSpans(subresult,
                                                   text.length(),
                                                   issueFinder);
            if (!resultsValid || (subresult.length == 0)) {
                continue;
            }

            issueFindersWithResults++;

            for (int i = 0; i < subresult.length; i += 2) {
                result.add(new HyperlinkSpanInfo(issueFinder, subresult[i],
                                                              subresult[i + 1]));
            }
        }
        //PENDING: check for overlaping intervals if issueFindersWithResults > 1
        return result;

    }

    public static String getIssueNumber(String text, int startOffset,
                                                     int endOffset) {
        IssueFinder issueFinder = determineIssueFinder(text, startOffset, endOffset);
        if (issueFinder == null) {
            return null;
        }

        return issueFinder.getIssueId(text.substring(endOffset, endOffset));
    }

    public static IssueFinder determineIssueFinder(String text, int startOffset,
                                                                int endOffset) {
        Collection<? extends IssueFinder> issueFinders
                = Lookup.getDefault().lookupAll(IssueFinder.class);

        for (IssueFinder issueFinder : issueFinders) {
            int[] spans = issueFinder.getIssueSpans(text);
            if (checkSpansContainsPair(spans, startOffset, endOffset)) {
                return issueFinder;
            }
        }
        return null;
    }

    /**
     * Checks whether the given set of spans contains a given span.
     * @param  spans  set of span boundaries to be checked
     * @param  startOffset  start offset of the given span
     * @param  endOffset  end offset of the given span
     * @return   {@code true} if the given span was found,
     *           {@code false} otherwise
     */
    private static boolean checkSpansContainsPair(int[] spans, int startOffset,
                                                               int endOffset) {
        for (int i = 0; i < spans.length; i += 2) {
            if ((spans[i] == startOffset) && (spans[i + 1] == endOffset)) {
                return true;
            }
        }
        return false;
    }

    private static int[] append(int[] arr1, int[] arr2) {
        int[] result = new int[arr1.length + arr2.length];

        System.arraycopy(arr1, 0, result,           0, arr1.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);

        return result;
    }

    private static boolean checkIssueSpans(int[] spans,
                                           int textLength,
                                           IssueFinder issueFinder) {
        if (spans == null) {
            Logger.global.warning(
                    "Issue finder "                                     //NOI18N
                    + issueFinder.getClass().getName()
                    + " returned <null> from getIssueSpans(...).");     //NOI18N
            return false;
        }
        if ((spans.length % 2) != 0) {
            Logger.global.warning(
                    "Issue finder "                                     //NOI18N
                    + issueFinder.getClass().getName()
                    + " returned array containing odd number of "       //NOI18N
                    + " elements from method getIssueSpans().");        //NOI18N
            return false;
        }
        for (int index = 0; index < spans.length; ) {
            int low = spans[index++];
            int high = spans[index++];
            if ((low < 0) || (high < 0)) {
                Logger.global.warning(
                        "Issue finder "                                 //NOI18N
                        + issueFinder.getClass().getName()
                        + " returned invalid data from method"          //NOI18N
                        + " getIssueSpans() (negative index).");        //NOI18N
                return false;
            }
            if (low >= high) {
                Logger.global.warning(
                        "Issue finder "                                 //NOI18N
                        + issueFinder.getClass().getName()
                        + " returned invalid data from method"          //NOI18N
                        + " getIssueSpans() (start >= end).");          //NOI18N
                return false;
            }
            if (high > textLength) {
                Logger.global.warning(
                        "Issue finder "                                 //NOI18N
                        + issueFinder.getClass().getName()
                        + " returned invalid data from method"          //NOI18N
                        + " getIssueSpans() (index > text length).");   //NOI18N
                return false;
            }
            //PENDING - check for overlaping intervals
        }
        return true;
    }

    public static class HyperlinkSpanInfo {
        public final IssueFinder issueFinder;
        public final int startOffset;
        public final int endOffset;
        public HyperlinkSpanInfo(IssueFinder finder, int start, int end) {
            this.issueFinder = finder;
            this.startOffset = start;
            this.endOffset = end;
        }
    }

}

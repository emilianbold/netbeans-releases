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

package org.netbeans.modules.jira.issue;

import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Marian Petras
 */
public class JiraIssueFinderTest {

    private JiraIssueFinder issueFinder;

    public JiraIssueFinderTest() { }

    @After
    public void tearDown() {
        issueFinder = null;
    }

    @Test
    public void testGetIssueSpans() {
        issueFinder = JiraIssueFinder.getTestInstance();

        checkNoIssueSpansFound("");
        checkNoIssueSpansFound("bug");
        checkNoIssueSpansFound("bug ");
        checkNoIssueSpansFound("bug #");
        checkNoIssueSpansFound("bug#");

        checkNoIssueSpansFound("bugA");
        checkNoIssueSpansFound("bug A");
        checkNoIssueSpansFound("bug AB");
        checkNoIssueSpansFound("bug ABC");
        checkNoIssueSpansFound("bug ABC-");
        checkNoIssueSpansFound("bug -");
        checkNoIssueSpansFound("bug -1");
        checkNoIssueSpansFound("bug -12");
        checkNoIssueSpansFound("bug -123");
        checkNoIssueSpansFound("bug A-1");
        checkNoIssueSpansFound("bug A-12");
        checkNoIssueSpansFound("bug A-123");
        checkNoIssueSpansFound("bug AB-");
        checkNoIssueSpansFound("bug A-B");
        checkNoIssueSpansFound("bug AB-C");
        checkNoIssueSpansFound("bug AB-1C");
        checkNoIssueSpansFound("bug AB-12C");
        checkNoIssueSpansFound("bug AB-123C");

        checkNoIssueSpansFound("bug#A");
        checkNoIssueSpansFound("bug #A");
        checkNoIssueSpansFound("bug #AB");
        checkNoIssueSpansFound("bug #ABC");
        checkNoIssueSpansFound("bug #ABC-");
        checkNoIssueSpansFound("bug #-");
        checkNoIssueSpansFound("bug #-1");
        checkNoIssueSpansFound("bug #-12");
        checkNoIssueSpansFound("bug #-123");
        checkNoIssueSpansFound("bug #A-1");
        checkNoIssueSpansFound("bug #A-12");
        checkNoIssueSpansFound("bug #A-123");
        checkNoIssueSpansFound("bug #AB-");
        checkNoIssueSpansFound("bug #A-B");
        checkNoIssueSpansFound("bug #AB-C");
        checkNoIssueSpansFound("bug #AB-1C");
        checkNoIssueSpansFound("bug #AB-12C");
        checkNoIssueSpansFound("bug #AB-123C");

        checkNoIssueSpansFound("bug##ABC-123");
        checkNoIssueSpansFound("bug ##ABC-123");
        checkNoIssueSpansFound("bug###ABC-123");

        checkNoIssueSpansFound("sbug#ABC-123");

        checkNoIssueSpansFound("bugABC-123");
        checkNoIssueSpansFound("bug#ABC-123");

        checkIssueSpans("ABC-123", "ABC-123");
        checkIssueSpans(" ABC-123", "ABC-123");
        checkIssueSpans("  ABC-123", "ABC-123");
        checkIssueSpans("   ABC-123", "ABC-123");
        checkIssueSpans("bug# ABC-123", "ABC-123");
        checkIssueSpans("bug#  ABC-123", "ABC-123");
        checkIssueSpans("sbug ABC-123", "ABC-123");
        checkIssueSpans("bug## ABC-123", "ABC-123");

        checkIssueSpans("#ABC-123", "#ABC-123");
        checkIssueSpans("# ABC-123", "# ABC-123");
        checkIssueSpans(" #ABC-123", "#ABC-123");
        checkIssueSpans("  #ABC-123", "#ABC-123");
        checkIssueSpans(" # ABC-123", "# ABC-123");
        checkIssueSpans("#  ABC-123", "#  ABC-123");
        checkIssueSpans("bug ABC-123", "bug ABC-123");
        checkIssueSpans("bug  ABC-123", "bug  ABC-123");
        checkIssueSpans("bug #ABC-123", "bug #ABC-123");
        checkIssueSpans("bug   ABC-123", "bug   ABC-123");
        checkIssueSpans("bug  #ABC-123", "bug  #ABC-123");
        checkIssueSpans("bug # ABC-123", "bug # ABC-123");

        checkIssueSpans("Bug ABC-123", "Bug ABC-123");
        checkIssueSpans("BUG ABC-123", "BUG ABC-123");
        checkIssueSpans("Issue ABC-123", "Issue ABC-123");
        checkIssueSpans("ISSUE ABC-123", "ISSUE ABC-123");

        checkIssueSpans("Bug #ABC-123", "Bug #ABC-123");
        checkIssueSpans("BUG #ABC-123", "BUG #ABC-123");
        checkIssueSpans("Issue #ABC-123", "Issue #ABC-123");
        checkIssueSpans("ISSUE #ABC-123", "ISSUE #ABC-123");

        checkIssueSpans("bug# #ABC-123", "#ABC-123");
        checkIssueSpans("sbug #ABC-123", "#ABC-123");

        checkIssueSpans("#ABC-67888 and #XY-73573", "#ABC-67888", "#XY-73573");
        checkIssueSpans("bugs #ABC-67888 and #XY-73573", "#ABC-67888", "#XY-73573");
        checkIssueSpans("issues #ABC-67888 and #XY-73573", "#ABC-67888", "#XY-73573");
        checkIssueSpans("bugs #ABC-67888, #KL-12345 and #XY-73573", "#ABC-67888", "#KL-12345", "#XY-73573");

        checkIssueSpans("bug\nABC-123", "bug\nABC-123");
        checkIssueSpans("* bug\nABC-123", "bug\nABC-123");
        checkIssueSpans("* bug\n ABC-123", "bug\n ABC-123");
        checkIssueSpans("* bug\n* ABC-123", "bug\n* ABC-123");
        checkIssueSpans("* bug\n * ABC-123", "bug\n * ABC-123");
        checkIssueSpans("* bug \n * ABC-123", "bug \n * ABC-123");
        checkIssueSpans("bug\n#ABC-123", "bug\n#ABC-123");
        checkIssueSpans("* bug\n#ABC-123", "bug\n#ABC-123");
        checkIssueSpans("* bug\n #ABC-123", "bug\n #ABC-123");
        checkIssueSpans("* bug\n* #ABC-123", "bug\n* #ABC-123");
        checkIssueSpans("* bug\n * #ABC-123", "bug\n * #ABC-123");

        checkIssueSpans("bug\n\nABC-123", "bug\n\nABC-123");
        checkIssueSpans("* bug\n\nABC-123", "bug\n\nABC-123");
        checkIssueSpans("* bug\n\n ABC-123", "bug\n\n ABC-123");
        checkIssueSpans("* bug\n\n* ABC-123", "bug\n\n* ABC-123");
        checkIssueSpans("* bug\n\n * ABC-123", "bug\n\n * ABC-123");
        checkIssueSpans("* bug \n\n * ABC-123", "bug \n\n * ABC-123");
        checkIssueSpans("bug\n\n#ABC-123", "bug\n\n#ABC-123");
        checkIssueSpans("* bug\n\n#ABC-123", "bug\n\n#ABC-123");
        checkIssueSpans("* bug\n\n #ABC-123", "bug\n\n #ABC-123");
        checkIssueSpans("* bug\n\n* #ABC-123", "bug\n\n* #ABC-123");
        checkIssueSpans("* bug\n\n * #ABC-123", "bug\n\n * #ABC-123");

        checkIssueSpans("bug\n* \nABC-123", "bug\n* \nABC-123");
        checkIssueSpans("bug\n * \nABC-123", "bug\n * \nABC-123");

        checkNoIssueSpansFound("* bug\n *ABC-123");
        checkNoIssueSpansFound("* bug\n *#ABC-123");

        checkIssueSpans("* bug\n *# ABC-123", "ABC-123");
        checkIssueSpans("* bug\n *#  ABC-123", "ABC-123");
        checkIssueSpans("bug\n ** \nABC-123", "ABC-123");

        checkIssueSpans("bug #ABC-123\n", "bug #ABC-123");
    }

    @Test
    public void testGetIssueNumber() {
        issueFinder = JiraIssueFinder.getTestInstance();

        testGetIssueNumber("#ABC-123", "ABC-123");
        testGetIssueNumber("# ABC-123", "ABC-123");
        testGetIssueNumber(" #ABC-123", "ABC-123");
        testGetIssueNumber("  #ABC-123", "ABC-123");
        testGetIssueNumber(" # ABC-123", "ABC-123");
        testGetIssueNumber("#  ABC-123", "ABC-123");
        testGetIssueNumber("bug ABC-123", "ABC-123");
        testGetIssueNumber("bug  ABC-123", "ABC-123");
        testGetIssueNumber("bug #ABC-123", "ABC-123");
        testGetIssueNumber("bug   ABC-123", "ABC-123");
        testGetIssueNumber("bug  #ABC-123", "ABC-123");
        testGetIssueNumber("bug # ABC-123", "ABC-123");

        testGetIssueNumber("bug# #ABC-123", "ABC-123");
        testGetIssueNumber("sbug #ABC-123", "ABC-123");

        checkNoIssueSpansFound("bug #ABC-abcdef");
        checkNoIssueSpansFound("bug #ABC-ABCDEF");

        checkNoIssueSpansFound("bug # ABC-abcdef");
        checkNoIssueSpansFound("# ABC-abcdef");

        testGetIssueNumber("Bug ABC-123", "ABC-123");
        testGetIssueNumber("BUG ABC-123", "ABC-123");
        testGetIssueNumber("Issue ABC-123", "ABC-123");
        testGetIssueNumber("ISSUE ABC-123", "ABC-123");
        testGetIssueNumber("Bug #ABC-123", "ABC-123");
        testGetIssueNumber("BUG #ABC-123", "ABC-123");
        testGetIssueNumber("Issue #ABC-123", "ABC-123");
        testGetIssueNumber("ISSUE #ABC-123", "ABC-123");

    }

    private void checkIssueSpans(String str, String... substr) {
        checkTestValidity(str != null);
        checkTestValidity(substr != null);

        int fromIndex = 0;

        int[] expBounds = new int[substr.length * 2];
        for (int i = 0; i < substr.length; i++) {
            int lowBound = str.indexOf(substr[i], fromIndex);
            checkTestValidity(lowBound != -1);
            int highBound = lowBound + substr[i].length();
            expBounds[2 * i] = lowBound;
            expBounds[2 * i + 1] = highBound;
            fromIndex = highBound;
        }
        checkIssueSpans(str, expBounds);
    }

    private void checkIssueSpans(String str, int... expectedBounds) {
        if ((expectedBounds == null) || (expectedBounds.length == 0)) {
            checkNoIssueSpansFound(str);
            return;
        }

        checkTestValidity(expectedBounds.length % 2 == 0);

        int[] spans = issueFinder.getIssueSpans(str);
        assertNotNull(spans);
        assertTrue("incorrect bounds detected: "
                       + "expected: " + printArray(expectedBounds)
                       + ", real: " + (spans.length == 0 ? "none" : printArray(spans)),
                   equals(expectedBounds, spans));
    }

    private void checkNoIssueSpansFound(String str) {
        checkTestValidity(str != null);
        int[] spans = issueFinder.getIssueSpans(str);
        assertNotNull(spans);
        assertTrue("incorrect bounds detected for \"" + str + "\": "
                       + "no spans expected but got: " + printArray(spans),
                   spans.length == 0);
    }

    private static boolean equals(int[] expected, int[] real) {
        if ((expected == null) && (real == null)) {
            return true;
        }

        if ((expected == null) || (real == null)) {
            return false;
        }

        if (expected.length != real.length) {
            return false;
        }

        for (int i = 0; i < real.length; i++) {
            if (real[i] != expected[i]) {
                return false;
            }
        }

        return true;
    }

    private static String printArray(int... arr) {
        if (arr == null) {
            return "<null>";
        }

        if (arr.length == 0) {
            return "[]";
        }

        StringBuilder buf = new StringBuilder(arr.length * 4 + 5);
        buf.append('[');
        buf.append(arr[0]);
        for (int i = 1; i < arr.length; i++) {
            buf.append(',').append(' ').append(arr[i]);
        }
        buf.append(']');
        return buf.toString();
    }

    private void testGetIssueNumber(String hyperlinkText, String issueNumber) {
        assertEquals(issueNumber, issueFinder.getIssueId(hyperlinkText));
    }

    private static void checkTestValidity(boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException();
        }
    }
}
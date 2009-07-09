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

package org.netbeans.modules.bugzilla.issue;

import org.junit.After;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import static org.junit.Assert.*;

/**
 *
 * @author  Marian Petras
 */
public class BugzillaIssueFinderTest extends NbTestCase {

    private BugzillaIssueFinder issueFinder;

    public BugzillaIssueFinderTest(String name) {
        super(name);
    }

    @After
    @Override
    public void tearDown() {
        issueFinder = null;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
    }



    @Test
    public void testGetIssueSpans() {
        issueFinder = BugzillaIssueFinder.getTestInstance();

        checkNoIssueSpansFound("");
        checkNoIssueSpansFound("bug");
        checkNoIssueSpansFound("bug ");
        checkNoIssueSpansFound("bug #");
        checkNoIssueSpansFound("bug#");
        checkNoIssueSpansFound("bug# ");

        checkNoIssueSpansFound("bug##123456");
        checkNoIssueSpansFound("bug ##123456");
        checkNoIssueSpansFound("bug## 123456");
        checkNoIssueSpansFound("bug###123456");

        checkNoIssueSpansFound("sbug 123456");
        checkNoIssueSpansFound("sbug#123456");

        checkNoIssueSpansFound("123456");
        checkNoIssueSpansFound(" 123456");
        checkNoIssueSpansFound("  123456");
        checkNoIssueSpansFound("   123456");
        checkNoIssueSpansFound("bug123456");
        checkNoIssueSpansFound("bug#123456");
        checkNoIssueSpansFound("bug# 123456");
        checkNoIssueSpansFound("bug#  123456");

        checkIssueSpans("#123456", "#123456");
        checkIssueSpans("# 123456", "# 123456");
        checkIssueSpans(" #123456", "#123456");
        checkIssueSpans("  #123456", "#123456");
        checkIssueSpans(" # 123456", "# 123456");
        checkIssueSpans("#  123456", "#  123456");
        checkIssueSpans("bug 123456", "bug 123456");
        checkIssueSpans("bug  123456", "bug  123456");
        checkIssueSpans("bug #123456", "bug #123456");
        checkIssueSpans("bug   123456", "bug   123456");
        checkIssueSpans("bug  #123456", "bug  #123456");
        checkIssueSpans("bug # 123456", "bug # 123456");

        checkIssueSpans("Bug 123456", "Bug 123456");
        checkIssueSpans("BUG 123456", "BUG 123456");
        checkIssueSpans("Issue 123456", "Issue 123456");
        checkIssueSpans("ISSUE 123456", "ISSUE 123456");

        checkIssueSpans("Bug #123456", "Bug #123456");
        checkIssueSpans("BUG #123456", "BUG #123456");
        checkIssueSpans("Issue #123456", "Issue #123456");
        checkIssueSpans("ISSUE #123456", "ISSUE #123456");

        checkIssueSpans("bug# #123456", "#123456");
        checkIssueSpans("sbug #123456", "#123456");

        checkIssueSpans("#67888 and #73573", "#67888", "#73573");
        checkIssueSpans("bugs #67888 and #73573", "#67888", "#73573");
        checkIssueSpans("issues #67888 and #73573", "#67888", "#73573");
        checkIssueSpans("bugs #67888, #12345 and #73573", "#67888", "#12345", "#73573");

        checkNoIssueSpansFound("#123cdE");
        checkNoIssueSpansFound("#123CDe");

        checkNoIssueSpansFound("#123cd G");
        checkNoIssueSpansFound("#123cd g");
        checkNoIssueSpansFound("#123cdG");
        checkNoIssueSpansFound("#123cdg");

        checkIssueSpans("bug\n123456", "bug\n123456");
        checkIssueSpans("* bug\n123456", "bug\n123456");
        checkIssueSpans("* bug\n 123456", "bug\n 123456");
        checkIssueSpans("* bug\n* 123456", "bug\n* 123456");
        checkIssueSpans("* bug\n * 123456", "bug\n * 123456");
        checkIssueSpans("* bug \n * 123456", "bug \n * 123456");
        checkIssueSpans("bug\n#123456", "bug\n#123456");
        checkIssueSpans("* bug\n#123456", "bug\n#123456");
        checkIssueSpans("* bug\n #123456", "bug\n #123456");
        checkIssueSpans("* bug\n* #123456", "bug\n* #123456");
        checkIssueSpans("* bug\n * #123456", "bug\n * #123456");

        checkIssueSpans("bug\n\n123456", "bug\n\n123456");
        checkIssueSpans("* bug\n\n123456", "bug\n\n123456");
        checkIssueSpans("* bug\n\n 123456", "bug\n\n 123456");
        checkIssueSpans("* bug\n\n* 123456", "bug\n\n* 123456");
        checkIssueSpans("* bug\n\n * 123456", "bug\n\n * 123456");
        checkIssueSpans("* bug \n\n * 123456", "bug \n\n * 123456");
        checkIssueSpans("bug\n\n#123456", "bug\n\n#123456");
        checkIssueSpans("* bug\n\n#123456", "bug\n\n#123456");
        checkIssueSpans("* bug\n\n #123456", "bug\n\n #123456");
        checkIssueSpans("* bug\n\n* #123456", "bug\n\n* #123456");
        checkIssueSpans("* bug\n\n * #123456", "bug\n\n * #123456");

        checkIssueSpans("bug\n* \n123456", "bug\n* \n123456");
        checkIssueSpans("bug\n * \n123456", "bug\n * \n123456");

        checkNoIssueSpansFound("* bug\n *123456");
        checkNoIssueSpansFound("* bug\n *#123456");
        checkNoIssueSpansFound("* bug\n *# 123456");
        checkNoIssueSpansFound("* bug\n *#  123456");

        checkNoIssueSpansFound("bug\n ** \n123456");

        checkIssueSpans("bug #123456\n", "bug #123456");
    }

    @Test
    public void testGetIssueNumber() {
        issueFinder = BugzillaIssueFinder.getTestInstance();

        testGetIssueNumber("#123456", "123456");
        testGetIssueNumber("# 123456", "123456");
        testGetIssueNumber(" #123456", "123456");
        testGetIssueNumber("  #123456", "123456");
        testGetIssueNumber(" # 123456", "123456");
        testGetIssueNumber("#  123456", "123456");
        testGetIssueNumber("bug 123456", "123456");
        testGetIssueNumber("bug  123456", "123456");
        testGetIssueNumber("bug #123456", "123456");
        testGetIssueNumber("bug   123456", "123456");
        testGetIssueNumber("bug  #123456", "123456");
        testGetIssueNumber("bug # 123456", "123456");

        testGetIssueNumber("bug# #123456", "123456");
        testGetIssueNumber("sbug #123456", "123456");

        checkNoIssueSpansFound("bug #abcdef");
        checkNoIssueSpansFound("bug #ABCDEF");

        checkNoIssueSpansFound("bug # abcdef");
        checkNoIssueSpansFound("# abcdef");

        testGetIssueNumber("Bug 123456", "123456");
        testGetIssueNumber("BUG 123456", "123456");
        testGetIssueNumber("Issue 123456", "123456");
        testGetIssueNumber("ISSUE 123456", "123456");
        testGetIssueNumber("Bug #123456", "123456");
        testGetIssueNumber("BUG #123456", "123456");
        testGetIssueNumber("Issue #123456", "123456");
        testGetIssueNumber("ISSUE #123456", "123456");

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
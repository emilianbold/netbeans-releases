/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugzilla.issue;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Marian Petras
 */
public class AttachmentHyperlinkSupportTest {

    public AttachmentHyperlinkSupportTest() {
    }

    @Test
    public void test() {
        checkBoundaries("", null, null);
        checkBoundaries("C", null, null);
        checkBoundaries("(id=123)", null, null);
        checkBoundaries("Created an attachment", null, null);
        checkBoundaries("Created an attachment (id=", null, null);
        checkBoundaries("Created an attachment (id=1", null, null);
        checkBoundaries("Created an attachment (id=12", null, null);
        checkBoundaries("Created an attachment (id=123", null, null);
        checkBoundaries("Created an attachment (id=)", null, null);
        checkBoundaries("Created an attachment (id=1)", "1", "1");
        checkBoundaries("Created an attachment (id=12)", "12", "12");
        checkBoundaries("Created an attachment (id=123)", "123", "123");
        checkBoundaries("Created an atmachment (id=123)", null, null);
        checkBoundaries("Created an attachment (id=1a5)", null, null);
        checkBoundaries("Created an attachment (id=123) [details]", "123", "123");
        checkBoundaries("Created an attachment (id=123)  [details]", "123", "123");
        checkBoundaries("Created an attachment (id=123)\t[details]", "123", "123");
        checkBoundaries("Created an attachment (id=123)\t\t[details]", "123", "123");
        checkBoundaries("Created an attachment (id=123)\t [details]", "123", "123");
        checkBoundaries("Created an attachment (id=123) \t[details]", "123", "123");
        checkBoundaries("Created an attachment (id=123) [details] ", "123", "123");
        checkBoundaries("Created an attachment (id=123) [details]  ", "123", "123");
        checkBoundaries("Created an attachment (id=123) [details]\t", "123", "123");
        checkBoundaries("Created an attachment (id=123) [details]\t\t", "123", "123");
        checkBoundaries("Created an attachment (id=123) [details]\t ", "123", "123");
        checkBoundaries("Created an attachment (id=123) [details] \t", "123", "123");
        checkBoundaries("Created an attachment (id=123) [details]\n", "123", "123");
        checkBoundaries("Created an attachment (id=123) [details] \n", "123", "123");
        checkBoundaries("Created an attachment (id=123) [details]  \n", "123", "123");
        checkBoundaries("Created an attachment (id=123) [details]\t\n", "123", "123");
        checkBoundaries("Created an attachment (id=123) [details]\t\t\n", "123", "123");
        checkBoundaries("Created an attachment (id=123) [details]\t \n", "123", "123");
        checkBoundaries("Created an attachment (id=123) [details] \t\n", "123", "123");
        checkBoundaries("Created an attachment (id=123) [details] \n ", "123", "123");
        checkBoundaries("Created an attachment (id=123) [details]\t\n ", "123", "123");
        checkBoundaries("Created an attachment (id=123)\nfoo", "foo", "123");
        checkBoundaries("Created an attachment (id=123)\n\tfoo", "foo", "123");
        checkBoundaries("Created an attachment (id=123)\n \tfoo", "foo", "123");
        checkBoundaries("Created an attachment (id=123)\n\t foo", "foo", "123");
        checkBoundaries("Created an attachment (id=123)\t\nfoo", "foo", "123");
        checkBoundaries("Created an attachment (id=123)\t\t\n\t\tfoo", "foo", "123");
        checkBoundaries("Created an attachment (id=123)\t\t\n\t\tfoo\tbar", "foo\tbar", "123");
        checkBoundaries("Created an attachment (id=123)\t  \n  \tfoo\tbar", "foo\tbar", "123");
        checkBoundaries("Created an attachment (id=123)\t  \n  \tfoo\tbar baz", "foo\tbar baz", "123");
        checkBoundaries("Created an attachment (id=123)\t  \n  \tfoo\tbar baz", "foo\tbar baz", "123");
        checkBoundaries("Created an attachment (id=123)\t  \n  \tfoo bar\nbaz", "foo bar", "123");
        checkBoundaries("Created an attachment (id=123) [details]\nfoo", "foo", "123");
        checkBoundaries("Created an attachment (id=123) [details]\n\tfoo", "foo", "123");
        checkBoundaries("Created an attachment (id=123) [details]\n \tfoo", "foo", "123");
        checkBoundaries("Created an attachment (id=123) [details]\n\t foo", "foo", "123");
        checkBoundaries("Created an attachment (id=123) [details]\t\nfoo", "foo", "123");
        checkBoundaries("Created an attachment (id=123) [details]\t\t\n\t\tfoo", "foo", "123");
        checkBoundaries("Created an attachment (id=123) [details]\t\t\n\t\tfoo\tbar", "foo\tbar", "123");
        checkBoundaries("Created an attachment (id=123) [details]\t  \n  \tfoo\tbar", "foo\tbar", "123");
        checkBoundaries("Created an attachment (id=123) [details]\t  \n  \tfoo\tbar baz", "foo\tbar baz", "123");
        checkBoundaries("Created an attachment (id=123) [details]\t  \n  \tfoo\tbar baz", "foo\tbar baz", "123");
        checkBoundaries("Created an attachment (id=123) [details]\t  \n  \tfoo bar\nbaz", "foo bar", "123");

        checkBoundaries("Created an attachment (id=123)\nScreenshot", "Screenshot", "123");
        checkBoundaries("Created an attachment (id=123)\n\nScreenshot", "123", "123");
        checkBoundaries("Created an attachment (id=123) [details]\nScreenshot", "Screenshot", "123");
        checkBoundaries("Created an attachment (id=123) [details]\n\nScreenshot", "123", "123");

        checkBoundaries("Created an attachment (id=92562)\n"
                            + "Screenshot\n"
                            + '\n'
                            + "I used NetBeans without connection to internet and when I tried to generate javadoc for openide.util project, strange dialog appeared. I suspect it is warning from Kenai about inability to connect to network.\n"
                            + '\n'
                            + "The dialog is shown when I right-click a node. This is not the right time to display dialogs (from UI point of view) nor to check internet connectivity (from performance point of view).\n"
                            + '\n'
                            + "Please eliminate such checks at this time.",
                        "Screenshot",
                        "92562");
    }

    private void checkBoundaries(String stringToParse,
                                 String expectedHyperlinkText,
                                 String expectedId) {
        int[] expected;
        if (expectedHyperlinkText == null) {
            expected = null;
        } else {
            int index = stringToParse.indexOf(expectedHyperlinkText);
            assert index != -1;
            expected = new int[] {index,
                                         index + expectedHyperlinkText.length()};
        }

        int[] actual = AttachmentHyperlinkSupport.findBoundaries(stringToParse);

        assertArrayEquals(expected, actual);
        if (expected != null) {
            assertEquals(expectedId, AttachmentHyperlinkSupport.getAttachmentId(stringToParse));
        }
    }

}
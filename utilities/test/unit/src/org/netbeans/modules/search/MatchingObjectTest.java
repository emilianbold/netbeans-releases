/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.search;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.search.LineReader.LineSeparator;
import static org.netbeans.modules.search.LineReader.LineSeparator.CR;
import static org.netbeans.modules.search.LineReader.LineSeparator.LF;
import static org.netbeans.modules.search.LineReader.LineSeparator.CRLF;

/**
 *
 * @author  Marian Petras
 */
public class MatchingObjectTest extends NbTestCase {

    private static final LineSeparator[] NO_SEPARATOR = new LineSeparator[0];

    public MatchingObjectTest() {
        super("MatchingObjectTest");
    }

    public void testMakeStringToWrite() {
        checkText("", NO_SEPARATOR, "");
        checkText("\n", list(CR), "\r");
        checkText("\n", list(LF), "\n");
        checkText("\n", list(CRLF), "\r\n");
        checkText("a\n", list(CR), "a\r");
        checkText("a\n", list(LF), "a\n");
        checkText("a\n", list(CRLF), "a\r\n");
        checkText("\nb", list(CR), "\rb");
        checkText("\nb", list(LF), "\nb");
        checkText("\nb", list(CRLF), "\r\nb");
        checkText("a\nb", list(CR), "a\rb");
        checkText("a\nb", list(LF), "a\nb");
        checkText("a\nb", list(CRLF), "a\r\nb");
        checkText("\na\n", list(CR, CR), "\ra\r");
        checkText("\na\n", list(CR, LF), "\ra\n");
        checkText("\na\n", list(LF, CR), "\na\r");
        checkText("\na\n", list(LF, LF), "\na\n");

        checkText("\n\n", list(CR, CR), "\r\r");
        checkText("\n\n", list(CR, LF), "\r\n");
        checkText("\n\n", list(LF, CR), "\n\r");
        checkText("\n\n", list(LF, LF), "\n\n");

        checkText("\n\n\n", list(CR, CR, CR), "\r\r\r");
        checkText("\n\n\n", list(CR, CR, LF), "\r\r\n");
        checkText("\n\n\n", list(CR, LF, CR), "\r\n\r");
        checkText("\n\n\n", list(CR, LF, LF), "\r\n\n");
        checkText("\n\n\n", list(LF, CR, CR), "\n\r\r");
        checkText("\n\n\n", list(LF, CR, LF), "\n\r\n");
        checkText("\n\n\n", list(LF, LF, CR), "\n\n\r");
        checkText("\n\n\n", list(LF, LF, LF), "\n\n\n");

        checkText("alpha", NO_SEPARATOR, "alpha");
        checkText("alpha\n", list(CR), "alpha\r");
        checkText("alpha\n", list(LF), "alpha\n");
        checkText("alpha\n", list(CRLF), "alpha\r\n");
        checkText("\nalpha", list(CR), "\ralpha");
        checkText("\nalpha", list(LF), "\nalpha");
        checkText("\nalpha", list(CRLF), "\r\nalpha");

        checkText("\nalpha\nbeta\ngamma\n", list(CR, LF, CR, LF), "\ralpha\nbeta\rgamma\n");
        checkText("\nalpha\nbeta\ngamma\n", list(LF, CR, LF, CR), "\nalpha\rbeta\ngamma\r");

        checkText("alpha\nbeta\ngamma", list(LF, CR), "alpha\nbeta\rgamma");
        checkText("alpha\nbeta\ngamma", list(CR, LF), "alpha\rbeta\ngamma");
    }

    private static LineSeparator[] list(LineSeparator... lineSeparators) {
        return lineSeparators;
    }

    private void checkText(String lines, LineSeparator[] lineSeparators, String expectedResult) {
        assertEquals("Text does not match",
                     expectedResult,
                     MatchingObject.makeStringToWrite(new StringBuilder(lines), lineSeparators));
    }

}
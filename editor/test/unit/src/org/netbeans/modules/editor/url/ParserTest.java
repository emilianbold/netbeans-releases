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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2008-2009 Sun
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
package org.netbeans.modules.editor.url;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jan Lahoda
 */
public class ParserTest {

    public ParserTest() {
    }

    /**
     * Test of recognizeURLs method, of class Parser.
     */
    @Test
    public void testRecognizeURLs() {
        performTest("    http://www.netbeans.org/\n", 4, 28);
        performTest("    http://www.netbeans.org/~s?d=3_\n", 4, 35);
        performTest("    http://www.test-test.test/\n", 4, 30);
        performTest("    http://www.test-test.test/a.jsp?tt\\&t$=$\n", 4, 44);
        performTest("    ftp://www.test-test.test/a.jsp?tt\\&t$=$\n", 4, 43);
        performTest("    f t p://www.test-test.test/a.jsp?tt\\&t$=$\n", null);
        performTest("    ftp://www.test-test.test/a.jsp?tt\\&t$=$", 4, 43);
        performTest("    \"http://www.netbeans.org/\"", 5, 29);
        performTest("    h", null);
    }

    @Test
    public void testContentType() {
        assertEquals("ISO-8859-2", Parser.decodeContentType("text/html; charset=ISO-8859-2"));
        assertEquals("ISO-8859-2", Parser.decodeContentType("text/html; charset=ISO-8859-2;"));
    }

//    @Test
    public void testPerformance() {
        StringBuilder sb = new StringBuilder();

        for (int cntr = 0; cntr < 100000; cntr++) {
            sb.append("test test test http://www.netbeans.org/index.html adsfasdf adsfadsfadf asdfadf\n");
        }

        long s = System.currentTimeMillis();

        for (int cntr = 0; cntr < 100; cntr++) {
            Parser.recognizeURLs(sb);
        }

        long handWritten = System.currentTimeMillis() - s;

        s = System.currentTimeMillis();

        for (int cntr = 0; cntr < 100; cntr++) {
            Parser.recognizeURLsREBased(sb);
        }

        long reBased = System.currentTimeMillis() - s;

        System.err.println("times=" + handWritten + " vs. " + reBased);
    }

    private void performTest(String code, int... spans) {
        List<List<Integer>> golden = new LinkedList<List<Integer>>();

        if (spans != null) {
            List<Integer> goldenSpans = new LinkedList<Integer>();

            for (int off : spans) {
                goldenSpans.add(off);
            }
            
            golden.add(goldenSpans);
        }

        assertEquals(golden, unpackArray(Parser.recognizeURLs(code)));
        assertEquals(golden, unpackArray(Parser.recognizeURLsREBased(code)));
    }
    
    private List<List<Integer>> unpackArray(Iterable<int[]> spans) {
        List<List<Integer>> r = new LinkedList<List<Integer>>();

        for (int[] span : spans) {
            r.add(Arrays.asList(span[0], span[1]));
        }

        return r;
    }
    
}
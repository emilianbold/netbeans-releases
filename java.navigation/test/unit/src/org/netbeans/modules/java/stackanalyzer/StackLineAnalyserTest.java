/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009-2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.stackanalyzer;

import java.io.BufferedReader;
import java.io.StringReader;
import javax.swing.DefaultListModel;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jan Lahoda
 */
public class StackLineAnalyserTest extends NbTestCase {

    public StackLineAnalyserTest(String name) {
        super(name);
    }

    @Test
    public void testMatches() throws Exception {
        assertTrue(StackLineAnalyser.matches("at java.lang.String.lastIndexOf(String.java:1627)"));
        assertTrue(StackLineAnalyser.matches(" at java.lang.String.lastIndexOf(String.java:1627)"));
        assertTrue(StackLineAnalyser.matches("asdf at java.lang.String.lastIndexOf(String.java:1627)"));
        assertTrue(StackLineAnalyser.matches("at java.lang.String.lastIndexOf(String.java:1627) asdf"));
        assertTrue(StackLineAnalyser.matches("at org.netbeans.modules.x.SessionNode.<init>(SessionNode.java:131)"));
        assertTrue(StackLineAnalyser.matches("at org.netbeans.modules.x.SessionNode.<clinit>(SessionNode.java:131)"));
        assertTrue(StackLineAnalyser.matches("at javaapplication8.Main$1.run(Main.java:32)"));
        assertTrue(StackLineAnalyser.matches("at javaapplication8.Main$Inner.go(Main.java:40)"));
    }
    @Test
    public void testAnalyse() throws Exception {
        StackLineAnalyser.Link l = StackLineAnalyser.analyse("at java.lang.String.lastIndexOf(String.java:1627)");
        assertEquals(3, l.getStartOffset());
        assertEquals(49, l.getEndOffset());
        l = StackLineAnalyser.analyse(" at java.lang.String.lastIndexOf(String.java:1627)");
        assertEquals(4, l.getStartOffset());
        assertEquals(50, l.getEndOffset());
        l = StackLineAnalyser.analyse("asdf at java.lang.String.lastIndexOf(String.java:1627)");
        assertEquals(8, l.getStartOffset());
        assertEquals(54, l.getEndOffset());
        l = StackLineAnalyser.analyse("at java.lang.String.lastIndexOf(String.java:1627) dfasdf");
        assertEquals(3, l.getStartOffset());
        assertEquals(49, l.getEndOffset());
    }

    @Test
    public void testFillListModelSimple() throws Exception {
        testFillListModelSkeleton(
                      "at java.lang.String.lastIndexOf(String.java:1627)",
        new String[] {"at java.lang.String.lastIndexOf(String.java:1627)"});
    }

    private void testFillListModelSkeleton(String input, String []output) throws Exception {
        StringReader sr = new StringReader(input);
        BufferedReader r = new BufferedReader (sr);
        DefaultListModel model = new DefaultListModel ();
        AnalyzeStackTopComponent.fillListModel(r, model);
        for (int i = 0; i < output.length; i++) {
            assertEquals(output[i], model.elementAt(i));
        }
    }

    @Test
    public void testFillListModelTrim() throws Exception {
        testFillListModelSkeleton(
                      "  at java.lang.String.lastIndexOf(String.java:1627)  ",
        new String[] {"at java.lang.String.lastIndexOf(String.java:1627)"});
    }
    @Test
    public void testFillListModelTwoLines1() throws Exception {
        testFillListModelSkeleton(
                      "at java.lang.Str\ning.lastIndexOf(String.java:1627)",
        new String[] {"at java.lang.String.lastIndexOf(String.java:1627)"});
    }
    @Test
    public void testFillListModelTwoLines2() throws Exception {
        testFillListModelSkeleton(
                      "asdf at java.lang.String.last\nIndexOf(String.java:1627)",
        new String[] {"asdf at java.lang.String.lastIndexOf(String.java:1627)"});
    }
    @Test
    public void testFillListModelNonMatchingLine() throws Exception {
        testFillListModelSkeleton(
                      "asdf a sdfasdf asdf a fafd \n" +
                      "at java.lang.String.lastIndexOf(String.java:1627)",
        new String[] {"asdf a sdfasdf asdf a fafd",
                      "at java.lang.String.lastIndexOf(String.java:1627)"});
    }
    @Test
    public void testFillListModelNonMatchingLine2() throws Exception {
        testFillListModelSkeleton(
                      "asdf a sdfasdf asdf a fafd \n" +
                      "at java.lang.String.lastIn\ndexOf(String.java:1627)",
        new String[] {"asdf a sdfasdf asdf a fafd",
                      "at java.lang.String.lastIndexOf(String.java:1627)"});
    }
    @Test
    public void testFillListModelInner() throws Exception {
        testFillListModelSkeleton(
                      "at javaapplication8.Main$Inn\ner.go(Main.java:40)",
        new String[] {"at javaapplication8.Main$Inner.go(Main.java:40)"});
    }
    @Test
    public void testFillListModelAnonymous() throws Exception {
        testFillListModelSkeleton(
                      " at javaapp\nlication8.Main$1.run(Main.java:32)",
        new String[] {"at javaapplication8.Main$1.run(Main.java:32)"});
    }

}

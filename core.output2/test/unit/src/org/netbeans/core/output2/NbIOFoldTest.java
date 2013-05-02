/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.core.output2;

import static org.junit.Assert.*;
import org.junit.Test;
import org.openide.windows.FoldHandle;
import org.openide.windows.IOFolding;

/**
 * @author jhavlin
 */
public class NbIOFoldTest {

    /**
     * Test writing folded InputOutput to the first-level InputOutput.
     */
    @Test
    public void printToNbIO2() throws Exception {

        AbstractLines lines = createTestLines();

        assertEquals("Start.\n", lines.getLine(0));
        assertEquals("FoldA\n", lines.getLine(1));
        assertEquals("  FoldA1\n", lines.getLine(2));
        assertEquals("  FoldB\n", lines.getLine(3));
        assertEquals("    FoldB1\n", lines.getLine(4));
        assertEquals("    FoldC\n", lines.getLine(5));
        assertEquals("      FoldC1\n", lines.getLine(6));
        assertEquals("      FoldC2\n", lines.getLine(7));
        assertEquals("    FoldB2\n", lines.getLine(8));
        assertEquals("  FoldD\n", lines.getLine(9));
        assertEquals("    FoldD1\n", lines.getLine(10));
        assertEquals("  FoldA2\n", lines.getLine(11));
        assertEquals("End.\n", lines.getLine(12));
        assertEquals("", lines.getLine(13));

        IntListSimple foldOffsets = lines.getFoldOffsets();

        assertEquals(0, foldOffsets.get(0));
        assertEquals(0, foldOffsets.get(1));
        assertEquals(1, foldOffsets.get(2));
        assertEquals(2, foldOffsets.get(3));
        assertEquals(1, foldOffsets.get(4));
        assertEquals(2, foldOffsets.get(5));
        assertEquals(1, foldOffsets.get(6));
        assertEquals(2, foldOffsets.get(7));
        assertEquals(5, foldOffsets.get(8));
        assertEquals(8, foldOffsets.get(9));
        assertEquals(1, foldOffsets.get(10));
        assertEquals(10, foldOffsets.get(11));
    }

    @Test
    public void testFoldLengths() {
        AbstractLines lines = createTestLines();
        assertEquals(10, lines.foldLength(1));
        assertEquals(5, lines.foldLength(3));
        assertEquals(2, lines.foldLength(5));
        assertEquals(1, lines.foldLength(9));
    }

    @Test
    public void testRealAndVisibleIndexes() {
        AbstractLines lines = createTestLines();
        assertEquals(8, lines.realToVisibleLine(8));

        lines.hideFold(5);
        assertEquals(5, lines.realToVisibleLine(5));
        assertEquals(-1, lines.realToVisibleLine(6));
        assertEquals(-1, lines.realToVisibleLine(7));
        assertEquals(6, lines.realToVisibleLine(8));

        assertEquals(5, lines.visibleToRealLine(5));
        assertEquals(8, lines.visibleToRealLine(6));

        lines.showFold(5);
        assertEquals(6, lines.realToVisibleLine(6));
        assertEquals(7, lines.realToVisibleLine(7));
        assertEquals(8, lines.realToVisibleLine(8));

        assertEquals(5, lines.visibleToRealLine(5));
        assertEquals(6, lines.visibleToRealLine(6));
        assertEquals(7, lines.visibleToRealLine(7));
        assertEquals(8, lines.visibleToRealLine(8));

        lines.hideFold(5);
        assertEquals(8, lines.visibleToRealLine(6));
        assertEquals(6, lines.realToVisibleLine(8));
    }

    @Test
    public void testFoldFirstLine() {
        NbIO nbIO = new NbIO("test");
        nbIO.getOut().println("FoldA");
        FoldHandle foldA = IOFolding.startFold(nbIO, true);
        nbIO.getOut().println("  A");
        nbIO.getOut().println("  B");
        foldA.finish();
        AbstractLines lines = (AbstractLines) ((NbWriter) nbIO.getOut())
                .out().getLines();
        assertEquals(1, lines.visibleToRealLine(1));
        assertEquals(2, lines.visibleToRealLine(2));
        assertEquals(1, lines.realToVisibleLine(1));
        assertEquals(2, lines.realToVisibleLine(2));
        lines.hideFold(0);
        assertEquals(-1, lines.realToVisibleLine(1));
        assertEquals(-1, lines.realToVisibleLine(2));
        lines.showFold(0);
        assertEquals(1, lines.visibleToRealLine(1));
        assertEquals(2, lines.visibleToRealLine(2));
        assertEquals(1, lines.realToVisibleLine(1));
        assertEquals(2, lines.realToVisibleLine(2));
    }

    @Test
    public void testCollapseAndExpandFoldWithNestedHiddenFold() {
        AbstractLines lines = createTestLines();
        lines.hideFold(5);
        assertEquals(6, lines.realToVisibleLine(8));
        lines.hideFold(3);
        assertEquals(4, lines.realToVisibleLine(9));
        lines.showFold(3);
        assertEquals(5, lines.realToVisibleLine(5));
        assertEquals(6, lines.realToVisibleLine(8));
        assertEquals(7, lines.realToVisibleLine(9));
    }

    /**
     * Create Lines object for InputOutput that contains the following structure
     * of lines.
     *
     *  * Situation:
     *
     * <pre>
     * Line String           | IO   | Offset to the first line of parent fold
     * -----------             --     ---------------------------------------
     * Start.
     * FoldA                 | A    | 0
     *   FoldA1              | A    | 1
     * + FoldB               | B    | 2
     *     FoldB1            | B    | 1
     * +   FoldC             | C    | 2
     *       FoldC1          | C    | 1
     *       FoldC2          | C    | 2
     *     FoldB2            | B    | 5
     * + FoldD               | D    | 8
     *     FoldD1            | D    | 1
     *   FoldA2              | A    | 10
     * End.
     * </pre>
     */
    private AbstractLines createTestLines() {
        NbIO nbIO = new NbIO("test");
        nbIO.getOut().println("Start.");
        nbIO.getOut().println("FoldA");
        FoldHandle foldA = IOFolding.startFold(nbIO, true);
        nbIO.getOut().println("  FoldA1");
        nbIO.getOut().println("  FoldB");
        FoldHandle foldB = foldA.startFold(true);
        nbIO.getOut().println("    FoldB1");
        nbIO.getOut().println("    FoldC");
        FoldHandle foldC = foldB.startFold(true);
        nbIO.getOut().println("      FoldC1");
        nbIO.getOut().println("      FoldC2");
        foldC.finish();
        nbIO.getOut().println("    FoldB2");
        foldB.finish();
        nbIO.getOut().print("  ");
        nbIO.getOut().print("FoldD\n");
        FoldHandle foldD = foldA.startFold(true);
        nbIO.getOut().print("    FoldD");
        nbIO.getOut().println(1);
        foldD.finish();
        nbIO.getOut().println("  FoldA2");
        foldA.finish();
        nbIO.getOut().println("End.");
        AbstractLines lines = (AbstractLines) ((NbWriter) nbIO.getOut())
                .out().getLines();
        return lines;
    }
}
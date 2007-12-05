/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.api.server.output;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 *
 * @author Petr Hejl
 */
public class PrintingLineProcessorTest extends NbTestCase {

    private static final List<String> TEST_LINES = new ArrayList<String>(5);

    static {
        Collections.addAll(TEST_LINES,
                "the first test line",
                "the second test line",
                "the third test line",
                "the fourth test line",
                "the fifth test line");
    }

    public PrintingLineProcessorTest(String name) {
        super(name);
    }

    public void testLineProcessing() {
        TestOutputWriter writer = new TestOutputWriter(new PrintWriter(System.out));
        PrintingLineProcessor lineProcessor = new PrintingLineProcessor(writer, true);
        for (String line : TEST_LINES) {
            lineProcessor.processLine(line);
        }

        assertEquals(TEST_LINES.size(), writer.getPrinted().size());
        for (int i = 0; i < TEST_LINES.size(); i++) {
            assertEquals(TEST_LINES.get(i), writer.getPrinted().get(i));
        }

        lineProcessor.reset();
        assertEquals(1, writer.getResetsProcessed());

        for (String line : TEST_LINES) {
            lineProcessor.processLine(line);
        }
        for (int i = 0; i < TEST_LINES.size(); i++) {
            assertEquals(TEST_LINES.get(i), writer.getPrinted().get(i));
        }

        writer = new TestOutputWriter(new PrintWriter(System.out));
        lineProcessor = new PrintingLineProcessor(writer, false);
        lineProcessor.reset();
        assertEquals(0, writer.getResetsProcessed());
    }

    private static class TestOutputWriter extends OutputWriter {

        private List<String> printed = new ArrayList<String>();

        private int resetsProcessed;

        public TestOutputWriter(Writer w) {
            super(w);
        }

        @Override
        public void println(String s) {
            printed.add(s);
            super.println(s);
        }

        @Override
        public void println(String s, OutputListener l) throws IOException {
            println(s);
        }

        @Override
        public void reset() throws IOException {
            resetsProcessed++;
            printed.clear();
        }

        public List<String> getPrinted() {
            return Collections.unmodifiableList(printed);
        }

        public int getResetsProcessed() {
            return resetsProcessed;
        }

    }
}

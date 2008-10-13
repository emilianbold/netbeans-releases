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

package org.netbeans.modules.extexecution.api.print;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;

/**
 *
 * @author Petr Hejl
 */
public class LineConvertorsTest extends NbTestCase {

    public LineConvertorsTest(String name) {
        super(name);
    }

    public void testFilePattern() {
        TestConvertor fallback = new TestConvertor();
        LineConvertor convertor = LineConvertors.filePattern(fallback,
                null, Pattern.compile("myline:\\s*(myfile\\w*\\.\\w{3})\\s.*"), null, 1, -1);
        
        List<ConvertedLine> lines = new ArrayList<ConvertedLine>();
        lines.addAll(convertor.convert("otherline: something.txt"));
        lines.addAll(convertor.convert("myline: myfile01.txt other stuff"));
        lines.addAll(convertor.convert("total mess"));
        lines.addAll(convertor.convert("myline: myfile02.txt other stuff"));
        lines.addAll(convertor.convert("otherline: http://www.netbeans.org"));
        
        List<String> ignored = new ArrayList<String>();
        Collections.addAll(ignored, "otherline: something.txt", "total mess",
                "otherline: http://www.netbeans.org");
        assertEquals(ignored, fallback.getLines());
        
        assertEquals(2, lines.size());
        assertEquals("myline: myfile01.txt other stuff", lines.get(0).getText());
        assertEquals("myline: myfile02.txt other stuff", lines.get(1).getText());

        for (ConvertedLine line : lines) {
            assertNotNull(line.getListener());
        }        
    }

    public void testFilePatternLocator() {
        TestConvertor fallback = new TestConvertor();
        TestFileLocator locator = new TestFileLocator();

        LineConvertor convertor = LineConvertors.filePattern(fallback,
                locator, Pattern.compile("myline:\\s*(myfile\\w*\\.\\w{3})\\s.*"), null, 1, -1);
        
        List<ConvertedLine> lines = new ArrayList<ConvertedLine>();
        lines.addAll(convertor.convert("myline: myfile01.txt other stuff"));
        lines.addAll(convertor.convert("myline: myfile02.txt other stuff"));
        
        assertEquals(2, lines.size());
        assertEquals("myline: myfile01.txt other stuff", lines.get(0).getText());
        assertEquals("myline: myfile02.txt other stuff", lines.get(1).getText());

        for (ConvertedLine line : lines) {
            line.getListener().outputLineAction(new OutputEvent(InputOutput.NULL) {
                @Override
                public String getLine() {
                    return "line";
                }
            });
        }
        
        List<String> paths = new ArrayList<String>();
        Collections.addAll(paths, "myfile01.txt", "myfile02.txt");        
        assertEquals(paths, locator.getPaths());
    }

    public void testHttpUrl() {
        TestConvertor fallback = new TestConvertor();
        LineConvertor convertor = LineConvertors.httpUrl(fallback);

        List<ConvertedLine> lines = new ArrayList<ConvertedLine>();
        lines.addAll(convertor.convert("nourl1"));
        lines.addAll(convertor.convert("NetBeans site: http://www.netbeans.org"));
        lines.addAll(convertor.convert("nourl2"));
        lines.addAll(convertor.convert("https://www.netbeans.org"));
        lines.addAll(convertor.convert("nourl3"));

        List<String> ignored = new ArrayList<String>();
        Collections.addAll(ignored, "nourl1", "nourl2", "nourl3");
        assertEquals(ignored, fallback.getLines());

        assertEquals(2, lines.size());
        assertEquals("NetBeans site: http://www.netbeans.org", lines.get(0).getText());
        assertEquals("https://www.netbeans.org", lines.get(1).getText());

        for (ConvertedLine line : lines) {
            assertNotNull(line.getListener());
        }
    }

    private static <T> void assertEquals(List<T> expected, List<T> value) {
        assertEquals(expected.size(), value.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), value.get(i));
        }
    }

    private static class TestConvertor implements LineConvertor {

        private final List<String> lines = new ArrayList<String>();

        public List<ConvertedLine> convert(String line) {
            lines.add(line);
            return Collections.emptyList();
        }

        public List<String> getLines() {
            return lines;
        }
    }
    
    private static class TestFileLocator implements LineConvertors.FileLocator {
        
        private final List<String> paths = new ArrayList<String>();

        public FileObject find(String filename) {
            paths.add(filename);
            return null;
        }

        public List<String> getPaths() {
            return paths;
        }
    }
}

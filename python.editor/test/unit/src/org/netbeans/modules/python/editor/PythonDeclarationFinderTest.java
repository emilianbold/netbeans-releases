/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.python.editor;

import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.GsfHtmlFormatter;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.DeclarationFinder.AlternativeLocation;
import org.netbeans.modules.gsf.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.gsf.spi.GsfUtilities;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tor Norbye
 */
public class PythonDeclarationFinderTest extends PythonTestBase {

    public PythonDeclarationFinderTest(String testName) {
        super(testName);
    }

    private boolean skipJython = true;

    @Override
    protected List<URL> getExtraCpUrls() {
        // I'm overriding various Jython classes here for tests which causes
        // confusion when it's trying to locate classes and finds it in multiple places
        if (!skipJython) {
            return super.getExtraCpUrls();
        }

        return null;
    }

    // Not yet provided by GSF so manual testing here
    protected void checkOverrides(String relFilePath, String caretLine) throws Exception {

        CompilationInfo info = getInfo(relFilePath);

        String text = info.getText();

        int caretDelta = caretLine.indexOf('^');
        assertTrue(caretDelta != -1);
        caretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
        int lineOffset = text.indexOf(caretLine);
        assertTrue(lineOffset != -1);
        int caretOffset = lineOffset + caretDelta;

        PythonDeclarationFinder finder = new PythonDeclarationFinder();
        DeclarationLocation location = finder.getSuperImplementations(info, caretOffset);
        String annotate = annotateFullDeclarationLocation(location);
        assertDescriptionMatches(relFilePath, annotate, true, ".declarations");
    }

    private String annotateFileLocation(DeclarationLocation location) throws BadLocationException {
        StringBuilder sb = new StringBuilder();
        FileObject fo = location.getFileObject();
        sb.append(fo.getNameExt());
        sb.append(":");
        int offset = location.getOffset();
        sb.append(offset);
        sb.append(":");
        BaseDocument document = GsfUtilities.getDocument(fo, true);
        if (document != null) {
            String text = document.getText(0, document.getLength());
            sb.append(getSourceWindow(text, offset));
        }
        sb.append("\n");
        return sb.toString();
    }

    private String annotateFullDeclarationLocation(DeclarationLocation location) throws BadLocationException {
        StringBuilder sb = new StringBuilder();

        if (location == DeclarationLocation.NONE) {
            sb.append("NONE\n");
        } else {
            if (location.getInvalidMessage() != null) {
                sb.append(location.getInvalidMessage());
                sb.append("\n");
            }
            if (location.getUrl() != null) {
                sb.append("URL: " + location.getUrl());
                sb.append("\n");
            } else {
                sb.append(annotateFileLocation(location));
                sb.append("\n\n");
                sb.append("Alternative Locations:\n");
                if (location.getAlternativeLocations() != null) {
                    List<AlternativeLocation> locations = location.getAlternativeLocations();
                    Collections.sort(locations, new Comparator<AlternativeLocation>() {

                        public int compare(AlternativeLocation l1, AlternativeLocation l2) {
                            String d1 = l1.getDisplayHtml(new PlainHtmlFormatter());
                            String d2 = l1.getDisplayHtml(new PlainHtmlFormatter());
                            int ret = d1.compareTo(d2);
                            if (ret != 0) {
                                return ret;
                            }
                            DeclarationLocation loc1 = l1.getLocation();
                            DeclarationLocation loc2 = l2.getLocation();
                            return loc1.toString().compareTo(loc2.toString());
                        }

                    });
                    for (AlternativeLocation alt : locations) {
                        sb.append(alt.getDisplayHtml(new PlainHtmlFormatter()));
                        sb.append("\n");
                        DeclarationLocation loc = alt.getLocation();
                        sb.append(annotateFileLocation(loc));
                        sb.append("\n\n");
                    }
                }
            }
        }

        return sb.toString();
    }

    private class PlainHtmlFormatter extends HtmlFormatter {
        private StringBuilder sb = new StringBuilder();

        @Override
        public void reset() {
            sb.setLength(0);
        }

        @Override
        public void appendHtml(String html) {
            sb.append(html);
        }

        @Override
        public void appendText(String text, int fromInclusive, int toExclusive) {
            sb.append(text, fromInclusive, toExclusive);
        }

        @Override
        public void name(ElementKind kind, boolean start) {
        }

        @Override
        public void active(boolean start) {
        }

        @Override
        public void parameters(boolean start) {
        }

        @Override
        public void type(boolean start) {
        }

        @Override
        public void deprecated(boolean start) {
        }

        @Override
        public String getText() {
            return sb.toString();
        }

        @Override
        public void emphasis(boolean start) {
        }
    }

    public void testDeclaration1() throws Exception {
        checkDeclaration("testfiles/ConfigParser.py", "% (line^no, line)", "def append(self, ^lineno, line)");
    }

    public void testDeclaration2() throws Exception {
        checkDeclaration("testfiles/rawstringdoc.py", "import rawstr^ingdoc", "rawstringdoc.py", 0);
    }

    public void testDeclaration3() throws Exception {
        checkDeclaration("testfiles/ConfigParser.py", "raise Interpola^tionSyntaxError(option, section,", "class ^InterpolationSyntaxError(InterpolationError):");
    }

    public void testDeclaration4() throws Exception {
        checkDeclaration("testfiles/ConfigParser.py", "opt = self.optio^nxform(option)", "def ^optionxform(self, optionstr):");
    }

    public void testDeclaration5() throws Exception {
        checkDeclaration("testfiles/gotolocal.py", "print na^me;", "def ggg(^name)");
    }

    public void testDeclaration6() throws Exception {
        checkDeclaration("testfiles/datetime.py", "http://we^bexhibits.org/daylightsaving/", new URL("http://webexhibits.org/daylightsaving/"));
    }

    public void testDeclaration7() throws Exception {
        checkDeclaration("testfiles/datetime.py", "converter = _ti^me.localtime", "^import time as _time");
    }

    public void testDeclaration8() throws Exception {
        checkOverrides("testfiles/overrides.py", "def ov^erridden_method1(self, a, b): # Final");
    }

    public void testDeclaration9() throws Exception {
        checkOverrides("testfiles/overrides.py", "ov^erridden_method2(self, c, d): # Final");
    }

// Not yet working
//    public void testDeclaration8() throws Exception {
//        checkDeclaration("testfiles/datetime.py", "_time.loc^altime", "time.rst", 0);
//    }

//    public void testDeclaration9() throws Exception {
//        // Broken because of Jython ast offset bug -- see PythonAstOffsetsTest.testAttributes
//        checkDeclaration("testfiles/datetime.py", "dayfrac, days = _ma^th.modf(days)", "^import math as _math");
//    }


// Works for me but not on the build machine
//    public void testDeclaration10() throws Exception {
//        DeclarationLocation location = findDeclaration("testfiles/datetime.py", "assert ab^s(daysecondsfrac) <= 1.0");
//        assertNotNull(location);
//        assertTrue(location != DeclarationLocation.NONE);
//
//        String message = NbBundle.getMessage(PythonDeclarationFinder.class, "BuiltinPython", "abs");
//        assertEquals(message, location.getInvalidMessage());
//    }

// For some reason, the fnmatch module isn't found from the test infrastructure
//    public void testDeclaration11() throws Exception {
//        checkDeclaration("testfiles/declarations.py", "fnmatchca^se", "fnmatch.py", 0);
//    }

// No such package found in the index, make custom test file
//    public void testDeclaration12() throws Exception {
//        checkDeclaration("testfiles/minicompat.py", "import xml.d^om", "xml.py", 0);
//    }
}

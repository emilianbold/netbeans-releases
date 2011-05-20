/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.bugs;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import javax.swing.text.Document;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.TreeLoader;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author lahvac
 */
public class UnusedAssignmentOrBranchTest extends NbTestCase {

    public UnusedAssignmentOrBranchTest(String name) {
        super(name);
    }

    public void testSimpleUnusedAssignment() throws Exception {
        performUnusedAssignmentTest("package test;\n" +
                                    "public class Test {\n" +
                                    "     {\n" +
                                    "         int i = |0|;\n" +
                                    "         i = 1;\n" +
                                    "         System.err.println(i);\n" +
                                    "     }\n" +
                                    "}\n");
    }

    public void testNoHighlightingForUnusedVariables() throws Exception {
        performUnusedAssignmentTest("package test;\n" +
                                    "public class Test {\n" +
                                    "     {\n" +
                                    "         int i = 0;\n" +
                                    "         i = 1;\n" +
                                    "     }\n" +
                                    "}\n");
    }

    public void testAttributeValues() throws Exception {
        performUnusedAssignmentTest("package test;\n" +
                                    "@SuppressWarnings(\"a\")\n" +
                                    "public class Test {\n" +
                                    "}\n");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SourceUtilsTestUtil.prepareTest(new String[] {"org/netbeans/modules/java/editor/resources/layer.xml"}, new Object[0]);
        TreeLoader.DISABLE_CONFINEMENT_TEST = true;
    }

    private void prepareTest(String fileName, String code) throws Exception {
        clearWorkDir();
        File wdFile = getWorkDir();
        FileUtil.refreshFor(wdFile);

        FileObject wd = FileUtil.toFileObject(wdFile);
        assertNotNull(wd);
        sourceRoot = FileUtil.createFolder(wd, "src");
        FileObject buildRoot = FileUtil.createFolder(wd, "build");
        FileObject cache = FileUtil.createFolder(wd, "cache");

        FileObject data = FileUtil.createData(sourceRoot, fileName);
        File dataFile = FileUtil.toFile(data);

        assertNotNull(dataFile);

        TestUtilities.copyStringToFile(dataFile, code);

        SourceUtilsTestUtil.setSourceLevel(data, sourceLevel);
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache, new FileObject[0]);

        DataObject od = DataObject.find(data);
        EditorCookie ec = od.getCookie(EditorCookie.class);

        assertNotNull(ec);

        doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        doc.putProperty("mimeType", "text/x-java");

        JavaSource js = JavaSource.forFileObject(data);

        assertNotNull(js);

        info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);

        assertNotNull(info);
    }

    private String sourceLevel = "1.5";
    private FileObject sourceRoot;
    private CompilationInfo info;
    private Document doc;

    protected void setSourceLevel(String sourceLevel) {
        this.sourceLevel = sourceLevel;
    }

    protected void performUnusedAssignmentTest(String code) throws Exception {
        List<String> splitted = new LinkedList<String>(Arrays.asList(code.split(Pattern.quote("|"))));
        int[] goldenSpans = new int[splitted.size() - 1];
        StringBuilder realCode = new StringBuilder();
        int i = 0;

        realCode.append(splitted.remove(0));

        for (String s : splitted) {
            goldenSpans[i++] = realCode.length();
            realCode.append(s);
        }

        prepareTest("test/Test.java", realCode.toString());

        HighlightsSequence highlights = UnusedAssignmentOrBranch.compute(info, doc, new AtomicBoolean()).getHighlights(0, doc.getLength());
        
        List<Integer> realSpansList = new ArrayList<Integer>();

        while (highlights.moveNext()) {
            realSpansList.add(highlights.getStartOffset());
            realSpansList.add(highlights.getEndOffset());
        }

        int[] realSpans = new int[realSpansList.size()];

        i = 0;

        for (int s : realSpansList) {
            realSpans[i++] = s;
        }

        assertArrayEquals(goldenSpans, realSpans);
    }

}

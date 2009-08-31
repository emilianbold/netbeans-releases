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
 */
package org.netbeans.modules.java.hints;

import com.sun.source.util.TreePath;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.errors.SuppressWarningsFixer;
import org.netbeans.modules.java.hints.infrastructure.TreeRuleTestBase;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;
import static org.junit.Assert.*;

/**
 * The following shell script was used to generate the code snippets
 * <code>cat test/unit/data/test/Test.java | tr '\n' ' ' | tr '\t' ' ' | sed -E 's| +| |g' | sed 's|"|\\"|g'</code>
 * @author Samuel Halliday
 */
public class SerialVersionUIDTest extends TreeRuleTestBase {

    private final SerialVersionUID computer = new SerialVersionUID();
    private static final String HINT_SUPPRESS = NbBundle.getMessage(SuppressWarningsFixer.class, "LBL_FIX_Suppress_Waning", "serial");
    private static final String HINT_DEFAULT = NbBundle.getMessage(SerialVersionUID.class, "HINT_SerialVersionUID");
    private static final String HINT_GENERATED = NbBundle.getMessage(SerialVersionUID.class, "HINT_SerialVersionUID_Generated");

    public SerialVersionUIDTest(String name) {
        super(name);
    }

    public void testSerialVersionUID1() throws Exception {
        String test = "package test; import java.io.Serializable; public interface T|est implements Serializable { }";
        performAnalysisTest(test);
    }

    public void testSerialVersionUID2() throws Exception {
        String test = "package test; import java.io.Serializable; @SuppressWarnings(\"serial\") public class T|est implements Serializable { }";
        performAnalysisTest(test);
    }

    public void testSerialVersionUID3() throws Exception {
        String test = "package test; import java.io.Serializable; @SuppressWarnings(\"serial\") abstract public class T|est implements Serializable { }";
        performAnalysisTest(test);
    }

    public void testSerialVersionUID4() throws Exception {
        String test = "package test; import java.io.Serializable; public class Te|st implements Serializable { private static final long serialVersionUID = 1L; }";
        performAnalysisTest(test);
    }

    public void testSerialVersionUID5() throws Exception {
        String test = "package test; import java.io.Serializable; abstract public class Te|st implements Serializable { private static final long serialVersionUID = 1L; }";
        performAnalysisTest(test);
    }

    public void testSerialVersionUIDSuppress1() throws Exception {
        String test = "package test; import java.io.Serializable; public class Te|st implements Serializable { }";
        String golden = "package test; import java.io.Serializable; @SuppressWarnings(\"serial\") public class Test implements Serializable { }";
        performFixTest(test, golden, HINT_SUPPRESS);
    }

    public void testSerialVersionUIDSuppress2() throws Exception {
        String test = "package test; import java.io.Serializable; abstract public class T|est implements Serializable { }";
        String golden = "package test; import java.io.Serializable; @SuppressWarnings(\"serial\") abstract public class Test implements Serializable { }";
        performFixTest(test, golden, HINT_SUPPRESS);
    }

    public void testSerialVersionUIDDefault1() throws Exception {
        String test = "package test; import java.io.Serializable; public class Te|st implements Serializable { }";
        String golden = "package test; import java.io.Serializable; public class Test implements Serializable { private static final long serialVersionUID = 1L; }";
        performFixTest(test, golden, HINT_DEFAULT);
    }

    public void testSerialVersionUIDDefault2() throws Exception {
        String test = "package test; import java.io.Serializable; abstract public class Te|st implements Serializable { }";
        String golden = "package test; import java.io.Serializable; abstract public class Test implements Serializable { private static final long serialVersionUID = 1L; }";
        performFixTest(test, golden, HINT_DEFAULT);
    }

    //w/o semicolon on end of enum constants
    public void testSerialVersionEnum0() throws Exception {
        String test = "package test; public enum Te|st { B, C }";
        String golden = "package test; public enum Test { B, C; private static final long serialVersionUID = 1L; }";
        performFixTest(test, golden, HINT_DEFAULT);
    }

    //with semicolon on end of enum constants
    public void testSerialVersionEnum1() throws Exception {
        String test = "package test; public enum Te|st { B, C; }";
        String golden = "package test; public enum Test { B, C; private static final long serialVersionUID = 1L; }";
        performFixTest(test, golden, HINT_DEFAULT);
    }

    //correct position
    public void testSerialVersionEnum2() throws Exception {
        String test = "package test; public enum Te|st { B, C; private int i;}";
        String golden = "package test; public enum Test { B, C; private int i; private static final long serialVersionUID = 1L; }";
        performFixTest(test, golden, HINT_DEFAULT);
    }

    public void testAnonymous() throws Exception {
        String test = "package test; public class Test {private Serializable ser = new Serializable() {|public String toString() {return \"Hello from serializable\";}};}";
        String golden = "package test; public class Test {private Serializable ser = new Serializable() {private static final long serialVersionUID = 1L; public String toString() {return \"Hello from serializable\";}};}";
        performFixTest("test/Test.java", test, "0:64-0:76:verifier:serialVersionUID not defined", HINT_DEFAULT, golden);
    }

    // test is single line source code for test.Test, | in the CLASS, space before, space after
    // golden is the output to test against
    private void performFixTest(String test, String golden, String hint) throws Exception {
        int offset = test.indexOf("|");
        assertTrue(offset != -1);
        int end = test.indexOf(" ", offset) - 1;
        assertTrue(end > 0);
        int start = test.lastIndexOf(" ", offset) + 1;
        assertTrue(start > 0);
        performFixTest("test/Test.java",
                test.replace("|", ""),
                offset,
                "0:" + start + "-0:" + end + ":verifier:" + NbBundle.getMessage(SerialVersionUID.class, "DSC_SerialVersionUID"),
                hint,
                golden);
    }

    // test is single line source code for test.Test, | in the CLASS, space before, space after
    // completes successfully if there are no hints presented
    private void performAnalysisTest(String test) throws Exception {
        int offset = test.indexOf("|");
        assertTrue(offset != -1);
        performAnalysisTest("test/Test.java", test.replace("|", ""), offset);
    }

    @Override
    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path) {
        return computer.run(info, path);
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return f.getText();
    }
//    // uncomment to speed up development cycle
    @Override
    public void testIssue105979() throws Exception {
    }

    @Override
    public void testIssue108246() throws Exception {
    }

    @Override
    public void testIssue113933() throws Exception {
    }

    @Override
    public void testNoHintsForSimpleInitialize() throws Exception {
    }
}
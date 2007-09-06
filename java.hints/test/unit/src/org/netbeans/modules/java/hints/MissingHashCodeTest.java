/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.hints;

import com.sun.source.util.TreePath;
import java.util.List;
import java.util.Locale;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.modules.java.hints.infrastructure.TreeRuleTestBase;
import org.netbeans.spi.editor.hints.ErrorDescription;

/**
 *
 * @author Jaroslav Tulach
 */
public class MissingHashCodeTest extends TreeRuleTestBase {

    public MissingHashCodeTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        Locale.setDefault(Locale.US);
        SourceUtilsTestUtil.setLookup(new Object[0], getClass().getClassLoader());
    }

    public void testMissingHashCode() throws Exception {
        String before = "package test; public class Test extends Object {" + " public boolean ";
        String after = " equals(Object snd) {" + "  return snd != null && getClass().equals(snd.getClass());" + " }" + "}";

        performAnalysisTest("test/Test.java", before + after, before.length(), "0:65-0:71:verifier:Generate missing hashCode()");
    }

    public void testMissingEquals() throws Exception {
        String before = "package test; public class Test extends Object {" + " public int ";
        String after = " hashCode() {" + "  return 1;" + " }" + "}";

        performAnalysisTest("test/Test.java", before + after, before.length(), "0:61-0:69:verifier:Generate missing equals(Object)");
    }

    public void testNoHintOnOtherMethods() throws Exception {
        String before = "package test; public class Test extends Object {" + " public boolean equals(Object snd) {" + "  return snd != null && getClass().equals(snd.getClass());" + " }\n" + " public static void main(String[] ";
        String after = "   args) {" + " }" + "}";

        performAnalysisTest("test/Test.java", before + after, before.length());
    }

    public void testWhenNoFieldsGenerateHashCode() throws Exception {
        String before = "package test; public class Test extends Object {" + " public boolean equa";
        String after = "ls(Object snd) { return snd == this; } }";

        String res = performFixTest("test/Test.java", before + after, before.length(), "0:64-0:70:verifier:Generate missing hashCode()", "Fix", null);

        if (!res.matches(".*equals.*hashCode.*")) {
            fail("We want equals and hashCode:\n" + res);
        }
    }

    public void testWhenNoFieldsGenerateEquals() throws Exception {
        String before = "package test; public class Test extends Object {" + " public int hash";
        String after = "Code() { return 1; } }";

        String res = performFixTest("test/Test.java", before + after, before.length(), "0:60-0:68:verifier:Generate missing equals(Object)", "Fix", null);

        if (!res.matches(".*hashCode.*equals.*")) {
            fail("We want equals and hashCode:\n" + res);
        }
    }

    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path) {
        SourceUtilsTestUtil.setSourceLevel(info.getFileObject(), sourceLevel);
        return new MissingHashCode().run(info, path);
    }
    private String sourceLevel = "1.5";
}

//This class is automatically generated - DO NOT MODIFY (ever)
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.test.java.editor.completion;
import java.io.PrintWriter;

//import org.openide.filesystems.*;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.test.editor.LineDiff;
import java.io.File;

/**This class is automatically generated from <I>config.txt</I> using bash
 * script <I>create</I>. For any changes, change the code generating script
 * and re-generate.
 *
 * Althought this class is runned as a test, there is no real code. This class
 * is only wrapper between xtest and harness independet test code. Main information
 * source is <B>CompletionTest</B> class ({@link CompletionTest}).
 *
 * @see CompletionTest
 */
public class CompletionTestPerformer extends NbTestCase {

    /** Need to be defined because of JUnit */
    public CompletionTestPerformer(String name) {
        super(name);
    }

    protected void setUp() {
        log("CompletionTestPerformer.setUp started.");
        log("CompletionTestPerformer.setUp finished.");
    }

    private String getJDKVersionCode() {
        String specVersion = System.getProperty("java.version");
        
        if (specVersion.startsWith("1.4"))
            return "jdk14";
        
        if (specVersion.startsWith("1.5"))
            return "jdk15";
        
        throw new IllegalStateException("Specification version: " + specVersion + " not recognized.");
    }

    private File resolveGoldenFile(String proposedGoldenFileName) {
        if ("@".equals(proposedGoldenFileName.trim()))
            return getGoldenFile(getJDKVersionCode() + "-" + getName() + ".pass");
        else
            return getGoldenFile(getJDKVersionCode() + "-" + proposedGoldenFileName + ".pass");
    }

public void testarrayunsorted() throws Exception {
    log("testarrayunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, "int[] a; a.", false, getDataDir(), "cp-prj-1", "org/netbeans/test/editor/completion/TestFile.java", 20);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testarrayunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testcommonunsorted() throws Exception {
    log("testcommonunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " ", false, getDataDir(), "cp-prj-1", "org/netbeans/test/editor/completion/TestFile.java", 20);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testcommonunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testtypecastunsorted() throws Exception {
    log("testtypecastunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, "Object a = new Integer(1);((Integer) a.getClass()).", false, getDataDir(), "cp-prj-1", "org/netbeans/test/editor/completion/TestFile.java", 20);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testtypecastunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testarrayIIunsorted() throws Exception {
    log("testarrayIIunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, "String[] a = new String[10]; a[\"test\".length()].", false, getDataDir(), "cp-prj-1", "org/netbeans/test/editor/completion/TestFile.java", 20);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testarrayIIunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testinsideunsorted() throws Exception {
    log("testinsideunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, "java.", false, getDataDir(), "cp-prj-1", "org/netbeans/test/editor/completion/TestFile.java", 20);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testinsideunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testcomplexunsorted() throws Exception {
    log("testcomplexunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, "Class.forName(\"\").getConstructor(new Class[] {}).", false, getDataDir(), "cp-prj-1", "org/netbeans/test/editor/completion/TestFile.java", 20);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testcomplexunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testoutterIunsorted() throws Exception {
    log("testoutterIunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, "InnerOutter.this.", false, getDataDir(), "cp-prj-1", "org/netbeans/test/editor/completion/InnerOutter.java", 20);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testoutterIunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testoutterIIunsorted() throws Exception {
    log("testoutterIIunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, "Innerer.this.", false, getDataDir(), "cp-prj-1", "org/netbeans/test/editor/completion/InnerOutter.java", 20);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testoutterIIunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testequalSignIunsorted() throws Exception {
    log("testequalSignIunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, "String x; x = ", false, getDataDir(), "cp-prj-1", "org/netbeans/test/editor/completion/TestFile.java", 20);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testequalSignIunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testfirstArgumentunsorted() throws Exception {
    log("testfirstArgumentunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, "first.", false, getDataDir(), "cp-prj-1", "org/netbeans/test/editor/completion/ArgumentTest.java", 14);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testfirstArgumentunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testsecondArgumentunsorted() throws Exception {
    log("testsecondArgumentunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, "second.", false, getDataDir(), "cp-prj-1", "org/netbeans/test/editor/completion/ArgumentTest.java", 14);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testsecondArgumentunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testthirdArgumentunsorted() throws Exception {
    log("testthirdArgumentunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, "third.", false, getDataDir(), "cp-prj-1", "org/netbeans/test/editor/completion/ArgumentTest.java", 14);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testthirdArgumentunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testfourthArgumentunsorted() throws Exception {
    log("testfourthArgumentunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, "fourth.", false, getDataDir(), "cp-prj-1", "org/netbeans/test/editor/completion/ArgumentTest.java", 14);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testfourthArgumentunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest1iunsorted() throws Exception {
    log("testjdk15CCTest1iunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " private static List<java.lang.", false, getDataDir(), "CC15Tests", "test1/CCTest1i.java", 7);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest1iunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("allclasses"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest1iiunsorted() throws Exception {
    log("testjdk15CCTest1iiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " l = new ArrayList<java.lang.", false, getDataDir(), "CC15Tests", "test1/CCTest1ii.java", 10);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest1iiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("allclasses"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest1iiiunsorted() throws Exception {
    log("testjdk15CCTest1iiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " l.add", false, getDataDir(), "CC15Tests", "test1/CCTest1iii.java", 12);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest1iiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest1ivunsorted() throws Exception {
    log("testjdk15CCTest1ivunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " l.get", false, getDataDir(), "CC15Tests", "test1/CCTest1iv.java", 14);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest1ivunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest1vunsorted() throws Exception {
    log("testjdk15CCTest1vunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " l.get(0).", false, getDataDir(), "CC15Tests", "test1/CCTest1v.java", 14);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest1vunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("string"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest2iunsorted() throws Exception {
    log("testjdk15CCTest2iunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " List<java.lang.", false, getDataDir(), "CC15Tests", "test2/CCTest2i.java", 9);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest2iunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("allclasses"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest2iiunsorted() throws Exception {
    log("testjdk15CCTest2iiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " l = new ArrayList<java.lang.", false, getDataDir(), "CC15Tests", "test2/CCTest2ii.java", 11);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest2iiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("allclasses"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest2iiiunsorted() throws Exception {
    log("testjdk15CCTest2iiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " l.add", false, getDataDir(), "CC15Tests", "test2/CCTest2iii.java", 13);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest2iiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest2ivunsorted() throws Exception {
    log("testjdk15CCTest2ivunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " l.get", false, getDataDir(), "CC15Tests", "test2/CCTest2iv.java", 15);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest2ivunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest2vunsorted() throws Exception {
    log("testjdk15CCTest2vunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " l.get(0).", false, getDataDir(), "CC15Tests", "test2/CCTest2v.java", 15);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest2vunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("string"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest11iunsorted() throws Exception {
    log("testjdk15CCTest11iunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " private static List<java.lang.", false, getDataDir(), "CC15Tests", "test11/CCTest11i.java", 4);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest11iunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("allclasses"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest11iiunsorted() throws Exception {
    log("testjdk15CCTest11iiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " l = new List<java.lang.", false, getDataDir(), "CC15Tests", "test11/CCTest11ii.java", 7);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest11iiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("allclasses"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest11iiiunsorted() throws Exception {
    log("testjdk15CCTest11iiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " l.add", false, getDataDir(), "CC15Tests", "test11/CCTest11iii.java", 9);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest11iiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest11ivunsorted() throws Exception {
    log("testjdk15CCTest11ivunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " l.get", false, getDataDir(), "CC15Tests", "test11/CCTest11iv.java", 11);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest11ivunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest11vunsorted() throws Exception {
    log("testjdk15CCTest11vunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " l.get(0).", false, getDataDir(), "CC15Tests", "test11/CCTest11v.java", 11);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest11vunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("string"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest12iunsorted() throws Exception {
    log("testjdk15CCTest12iunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " private static List<java.lang.", false, getDataDir(), "CC15Tests", "test12/CCTest12i.java", 4);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest12iunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("allclasses"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest12iiunsorted() throws Exception {
    log("testjdk15CCTest12iiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " l = new List<java.lang.", false, getDataDir(), "CC15Tests", "test12/CCTest12ii.java", 7);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest12iiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("allclasses"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest12iiiunsorted() throws Exception {
    log("testjdk15CCTest12iiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " l.add", false, getDataDir(), "CC15Tests", "test12/CCTest12iii.java", 9);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest12iiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest12ivunsorted() throws Exception {
    log("testjdk15CCTest12ivunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " l.get", false, getDataDir(), "CC15Tests", "test12/CCTest12iv.java", 11);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest12ivunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest12vunsorted() throws Exception {
    log("testjdk15CCTest12vunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " l.get(0).", false, getDataDir(), "CC15Tests", "test12/CCTest12v.java", 11);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest12vunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("string"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest13iunsorted() throws Exception {
    log("testjdk15CCTest13iunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " private static List<java.lang.", false, getDataDir(), "CC15Tests", "test13/CCTest13i.java", 4);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest13iunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("allclasses"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest13iiunsorted() throws Exception {
    log("testjdk15CCTest13iiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " l = new List<java.lang.", false, getDataDir(), "CC15Tests", "test13/CCTest13ii.java", 7);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest13iiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("allclasses"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest13iiiunsorted() throws Exception {
    log("testjdk15CCTest13iiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " l.add", false, getDataDir(), "CC15Tests", "test13/CCTest13iii.java", 9);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest13iiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest13ivunsorted() throws Exception {
    log("testjdk15CCTest13ivunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " l.get", false, getDataDir(), "CC15Tests", "test13/CCTest13iv.java", 11);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest13ivunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest13vunsorted() throws Exception {
    log("testjdk15CCTest13vunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " l.get(0).", false, getDataDir(), "CC15Tests", "test13/CCTest13v.java", 11);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest13vunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("string"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest14iunsorted() throws Exception {
    log("testjdk15CCTest14iunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " List<java.lang.", false, getDataDir(), "CC15Tests", "test14/CCTest14i.java", 6);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest14iunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("allclasses"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest14iiunsorted() throws Exception {
    log("testjdk15CCTest14iiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " l = new List<java.lang.", false, getDataDir(), "CC15Tests", "test14/CCTest14ii.java", 8);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest14iiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("allclasses"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest14iiiunsorted() throws Exception {
    log("testjdk15CCTest14iiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " l.add", false, getDataDir(), "CC15Tests", "test14/CCTest14iii.java", 10);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest14iiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest14ivunsorted() throws Exception {
    log("testjdk15CCTest14ivunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " l.get", false, getDataDir(), "CC15Tests", "test14/CCTest14iv.java", 12);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest14ivunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest14vunsorted() throws Exception {
    log("testjdk15CCTest14vunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " l.get(0).", false, getDataDir(), "CC15Tests", "test14/CCTest14v.java", 12);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest14vunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("string"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest3iunsorted() throws Exception {
    log("testjdk15CCTest3iunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, "", false, getDataDir(), "CC15Tests", "test3/CCTest3i.java", 16);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest3iunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest3iiunsorted() throws Exception {
    log("testjdk15CCTest3iiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " s.", false, getDataDir(), "CC15Tests", "test3/CCTest3i.java", 16);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest3iiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("string"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest4aiunsorted() throws Exception {
    log("testjdk15CCTest4aiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, "import ", false, getDataDir(), "CC15Tests", "test4/CCTest4ai.java", 4);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest4aiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("allpackages"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest4aiiunsorted() throws Exception {
    log("testjdk15CCTest4aiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, "import j", false, getDataDir(), "CC15Tests", "test4/CCTest4ai.java", 4);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest4aiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest4aiiiunsorted() throws Exception {
    log("testjdk15CCTest4aiiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, "import java.", false, getDataDir(), "CC15Tests", "test4/CCTest4ai.java", 4);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest4aiiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("alljavasubpackages"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest4aivunsorted() throws Exception {
    log("testjdk15CCTest4aivunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, "import java.util.Lis", false, getDataDir(), "CC15Tests", "test4/CCTest4ai.java", 4);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest4aivunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest4avunsorted() throws Exception {
    log("testjdk15CCTest4avunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, "import java.util.List.", false, getDataDir(), "CC15Tests", "test4/CCTest4ai.java", 4);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest4avunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("emptyresult"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest4biunsorted() throws Exception {
    log("testjdk15CCTest4biunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " int x = TEST_FIELD", false, getDataDir(), "CC15Tests", "test4/CCTest4bi.java", 9);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest4biunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest4biiunsorted() throws Exception {
    log("testjdk15CCTest4biiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " testMethod", false, getDataDir(), "CC15Tests", "test4/CCTest4bii.java", 11);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest4biiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest4biiiunsorted() throws Exception {
    log("testjdk15CCTest4biiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " testMethod().get(0).", false, getDataDir(), "CC15Tests", "test4/CCTest4biii.java", 11);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest4biiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("string"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest4bivunsorted() throws Exception {
    log("testjdk15CCTest4bivunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, "import static ", false, getDataDir(), "CC15Tests", "test4/CCTest4biv.java", 4);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest4bivunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("allpackages"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest4bvunsorted() throws Exception {
    log("testjdk15CCTest4bvunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, "import static t", false, getDataDir(), "CC15Tests", "test4/CCTest4biv.java", 4);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest4bvunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest4bviunsorted() throws Exception {
    log("testjdk15CCTest4bviunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, "import static test4.", false, getDataDir(), "CC15Tests", "test4/CCTest4biv.java", 4);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest4bviunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest4bviiunsorted() throws Exception {
    log("testjdk15CCTest4bviiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, "import static test4.CCTest", false, getDataDir(), "CC15Tests", "test4/CCTest4biv.java", 4);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest4bviiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("testjdk15CCTest4bviunsorted"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest4bviiiunsorted() throws Exception {
    log("testjdk15CCTest4bviiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, "import static test4.CCTest4a.", false, getDataDir(), "CC15Tests", "test4/CCTest4biv.java", 4);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest4bviiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest4bixunsorted() throws Exception {
    log("testjdk15CCTest4bixunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, "import static test4.CCTest4a.T", false, getDataDir(), "CC15Tests", "test4/CCTest4biv.java", 4);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest4bixunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest4bxunsorted() throws Exception {
    log("testjdk15CCTest4bxunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, "import static test4.CCTest4a.t", false, getDataDir(), "CC15Tests", "test4/CCTest4biv.java", 4);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest4bxunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest4bxiunsorted() throws Exception {
    log("testjdk15CCTest4bxiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, "import test4.CCTest4a.", false, getDataDir(), "CC15Tests", "test4/CCTest4biv.java", 4);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest4bxiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest4bxiiunsorted() throws Exception {
    log("testjdk15CCTest4bxiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, "import test4.CCTest4a.I", false, getDataDir(), "CC15Tests", "test4/CCTest4biv.java", 4);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest4bxiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("testjdk15CCTest4bxiunsorted"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest5biunsorted() throws Exception {
    log("testjdk15CCTest5biunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " int x = TEST_FIELD", false, getDataDir(), "CC15Tests", "test5/CCTest5bi.java", 8);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest5biunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest5biiunsorted() throws Exception {
    log("testjdk15CCTest5biiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " testMethod", false, getDataDir(), "CC15Tests", "test5/CCTest5bii.java", 10);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest5biiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest5biiiunsorted() throws Exception {
    log("testjdk15CCTest5biiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " testMethod().get(0).", false, getDataDir(), "CC15Tests", "test5/CCTest5biii.java", 10);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest5biiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("string"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest5cunsorted() throws Exception {
    log("testjdk15CCTest5cunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " new Inner(", false, getDataDir(), "CC15Tests", "test5/CCTest5c.java", 8);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest5cunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest6iunsorted() throws Exception {
    log("testjdk15CCTest6iunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " t.test", false, getDataDir(), "CC15Tests", "test6/CCTest6i.java", 8);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest6iunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest6iiunsorted() throws Exception {
    log("testjdk15CCTest6iiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " permanent.", false, getDataDir(), "CC15Tests", "test6/CCTest6ii.java", 12);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest6iiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("string"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest6iiiunsorted() throws Exception {
    log("testjdk15CCTest6iiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " int dummy = variable.", false, getDataDir(), "CC15Tests", "test6/CCTest6iii.java", 14);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest6iiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("array"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest6ivunsorted() throws Exception {
    log("testjdk15CCTest6ivunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " variable[0].", false, getDataDir(), "CC15Tests", "test6/CCTest6iv.java", 16);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest6ivunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("string"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest6vunsorted() throws Exception {
    log("testjdk15CCTest6vunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " t.test(", false, getDataDir(), "CC15Tests", "test6/CCTest6i.java", 8);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest6vunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("testjdk15CCTest6iunsorted"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest6viunsorted() throws Exception {
    log("testjdk15CCTest6viunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " t.test(\"Hello\",", false, getDataDir(), "CC15Tests", "test6/CCTest6i.java", 8);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest6viunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("testjdk15CCTest6iunsorted"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest6viiunsorted() throws Exception {
    log("testjdk15CCTest6viiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " t.test(\"Hello\", \"Hello\",", false, getDataDir(), "CC15Tests", "test6/CCTest6i.java", 8);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest6viiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("testjdk15CCTest6iunsorted"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest6biunsorted() throws Exception {
    log("testjdk15CCTest6biunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " t.test", false, getDataDir(), "CC15Tests", "test6/CCTest6b.java", 7);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest6biunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest6biiunsorted() throws Exception {
    log("testjdk15CCTest6biiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " t.test(1, ", false, getDataDir(), "CC15Tests", "test6/CCTest6b.java", 7);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest6biiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest6biiiunsorted() throws Exception {
    log("testjdk15CCTest6biiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " t.test(\"aaa\", ", false, getDataDir(), "CC15Tests", "test6/CCTest6b.java", 7);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest6biiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest6bivunsorted() throws Exception {
    log("testjdk15CCTest6bivunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " t.test(\"aaa\", \"bbb\", ", false, getDataDir(), "CC15Tests", "test6/CCTest6b.java", 7);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest6bivunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest6bvunsorted() throws Exception {
    log("testjdk15CCTest6bvunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " t.test(\"aaa\", null, ", false, getDataDir(), "CC15Tests", "test6/CCTest6b.java", 7);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest6bvunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("testjdk15CCTest6bivunsorted"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest6bviunsorted() throws Exception {
    log("testjdk15CCTest6bviunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " t.test(null, ", false, getDataDir(), "CC15Tests", "test6/CCTest6b.java", 7);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest6bviunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("testjdk15CCTest6biiiunsorted"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest7aiunsorted() throws Exception {
    log("testjdk15CCTest7aiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " permanent.", false, getDataDir(), "CC15Tests", "test7/CCTest7ai.java", 6);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest7aiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("string"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest7aiiunsorted() throws Exception {
    log("testjdk15CCTest7aiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " int dummy = variable.", false, getDataDir(), "CC15Tests", "test7/CCTest7aii.java", 8);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest7aiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("array"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest7aiiiunsorted() throws Exception {
    log("testjdk15CCTest7aiiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " variable[0].", false, getDataDir(), "CC15Tests", "test7/CCTest7aiii.java", 10);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest7aiiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("string"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest7aivunsorted() throws Exception {
    log("testjdk15CCTest7aivunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " permanent.", false, getDataDir(), "CC15Tests", "test7/CCTest7aiv.java", 14);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest7aivunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("string"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest7avunsorted() throws Exception {
    log("testjdk15CCTest7avunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " int dummy = variable.", false, getDataDir(), "CC15Tests", "test7/CCTest7av.java", 16);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest7avunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("array"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest7aviunsorted() throws Exception {
    log("testjdk15CCTest7aviunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " variable[0].", false, getDataDir(), "CC15Tests", "test7/CCTest7avi.java", 18);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest7aviunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("string"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest7biunsorted() throws Exception {
    log("testjdk15CCTest7biunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " t.test", false, getDataDir(), "CC15Tests", "test7/CCTest7bi.java", 10);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest7biunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest7biiunsorted() throws Exception {
    log("testjdk15CCTest7biiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " testStatic", false, getDataDir(), "CC15Tests", "test7/CCTest7bii.java", 11);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest7biiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest8iunsorted() throws Exception {
    log("testjdk15CCTest8iunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " InnerEnum", false, getDataDir(), "CC15Tests", "test8/CCTest8i.java", 6);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest8iunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest8iiunsorted() throws Exception {
    log("testjdk15CCTest8iiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " e = InnerEnum.", false, getDataDir(), "CC15Tests", "test8/CCTest8ii.java", 8);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest8iiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest8iiiunsorted() throws Exception {
    log("testjdk15CCTest8iiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " InnerEnum x = e.", false, getDataDir(), "CC15Tests", "test8/CCTest8iii.java", 10);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest8iiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest9biunsorted() throws Exception {
    log("testjdk15CCTest9biunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " CCTest9a", false, getDataDir(), "CC15Tests", "test9/CCTest9bi.java", 6);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest9biunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest9biiunsorted() throws Exception {
    log("testjdk15CCTest9biiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " e = CCTest9a.", false, getDataDir(), "CC15Tests", "test9/CCTest9bii.java", 8);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest9biiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest9biiiunsorted() throws Exception {
    log("testjdk15CCTest9biiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " CCTest9a x = e.", false, getDataDir(), "CC15Tests", "test9/CCTest9biii.java", 10);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest9biiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest9ciunsorted() throws Exception {
    log("testjdk15CCTest9ciunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " case ", false, getDataDir(), "CC15Tests", "test9/CCTest9c.java", 10);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest9ciunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("emptyresult"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest9ciiunsorted() throws Exception {
    log("testjdk15CCTest9ciiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " case ", false, getDataDir(), "CC15Tests", "test9/CCTest9c.java", 13);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest9ciiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest9ciiiunsorted() throws Exception {
    log("testjdk15CCTest9ciiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " case A", false, getDataDir(), "CC15Tests", "test9/CCTest9c.java", 13);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest9ciiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest9civunsorted() throws Exception {
    log("testjdk15CCTest9civunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " case ", false, getDataDir(), "CC15Tests", "test9/CCTest9c.java", 15);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest9civunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("emptyresult"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest10biunsorted() throws Exception {
    log("testjdk15CCTest10biunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " CCTest9a", false, getDataDir(), "CC15Tests", "test10/CCTest10bi.java", 8);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest10biunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest10biiunsorted() throws Exception {
    log("testjdk15CCTest10biiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " e = CCTest9a.", false, getDataDir(), "CC15Tests", "test10/CCTest10bii.java", 10);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest10biiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15CCTest10biiiunsorted() throws Exception {
    log("testjdk15CCTest10biiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " CCTest9a x = e.", false, getDataDir(), "CC15Tests", "test10/CCTest10biii.java", 12);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15CCTest10biiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15GenericsTestiunsorted() throws Exception {
    log("testjdk15GenericsTestiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " ", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTesti.java", 6);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15GenericsTestiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15GenericsTestiiunsorted() throws Exception {
    log("testjdk15GenericsTestiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " param.", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTesti.java", 6);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15GenericsTestiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("number"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15GenericsTestiiiunsorted() throws Exception {
    log("testjdk15GenericsTestiiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " MyGenericsTest<", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestii.java", 10);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15GenericsTestiiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15GenericsTestivunsorted() throws Exception {
    log("testjdk15GenericsTestivunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " MyGenericsTest<Int", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestii.java", 10);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15GenericsTestivunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15GenericsTestvunsorted() throws Exception {
    log("testjdk15GenericsTestvunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " MyGenericsTest<Integer, ", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestii.java", 10);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15GenericsTestvunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15GenericsTestviunsorted() throws Exception {
    log("testjdk15GenericsTestviunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " MyGenericsTest<Integer, ArithmeticException, ", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestii.java", 10);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15GenericsTestviunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("emptyresult"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15GenericsTestviiunsorted() throws Exception {
    log("testjdk15GenericsTestviiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " MyGenericsTest<?, ", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestii.java", 13);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15GenericsTestviiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("testjdk15GenericsTestvunsorted"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15GenericsTestviiiunsorted() throws Exception {
    log("testjdk15GenericsTestviiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " MyGenericsTest<? extends Number, ", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestii.java", 13);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15GenericsTestviiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("testjdk15GenericsTestvunsorted"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15GenericsTestixunsorted() throws Exception {
    log("testjdk15GenericsTestixunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " MyGenericsTest<? super Number, ", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestii.java", 13);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15GenericsTestixunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("testjdk15GenericsTestvunsorted"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15GenericsTestxunsorted() throws Exception {
    log("testjdk15GenericsTestxunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " genericstest.MyGenericClass<", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestii.java", 13);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15GenericsTestxunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("testjdk15GenericsTestiiiunsorted"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15GenericsTestxiunsorted() throws Exception {
    log("testjdk15GenericsTestxiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " genericstest.MyGenericClass<java.lang.", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestii.java", 13);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15GenericsTestxiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15GenericsTestxiiunsorted() throws Exception {
    log("testjdk15GenericsTestxiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " genericstest.MyGenericClass<java.lang.L", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestii.java", 13);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15GenericsTestxiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15GenericsTestxiiiunsorted() throws Exception {
    log("testjdk15GenericsTestxiiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " genericstest.MyGenericClass<java.lang.Long, ", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestii.java", 13);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15GenericsTestxiiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("emptyresult"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15GenericsTestxivunsorted() throws Exception {
    log("testjdk15GenericsTestxivunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " ", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestiii.java", 17);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15GenericsTestxivunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15GenericsTestxvunsorted() throws Exception {
    log("testjdk15GenericsTestxvunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " mgt.", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestiii.java", 17);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15GenericsTestxvunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15GenericsTestxviunsorted() throws Exception {
    log("testjdk15GenericsTestxviunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " mc.", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestiii.java", 17);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15GenericsTestxviunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15GenericsTestxviiunsorted() throws Exception {
    log("testjdk15GenericsTestxviiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " mcl.", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestiii.java", 17);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15GenericsTestxviiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15GenericsTestxviiiunsorted() throws Exception {
    log("testjdk15GenericsTestxviiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " mci.", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestiii.java", 17);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15GenericsTestxviiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15GenericsTestxixunsorted() throws Exception {
    log("testjdk15GenericsTestxixunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " mcgi.", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestiii.java", 17);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15GenericsTestxixunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15GenericsTestxxunsorted() throws Exception {
    log("testjdk15GenericsTestxxunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " mcsi.", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestiii.java", 17);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15GenericsTestxxunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15GenericsTestxxiunsorted() throws Exception {
    log("testjdk15GenericsTestxxiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " mcdgi.", false, getDataDir(), "CC15Tests", "genericstest/MyGenericsTestiii.java", 17);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15GenericsTestxxiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15AccessControlTestiunsorted() throws Exception {
    log("testjdk15AccessControlTestiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " accesscontroltest.points.", false, getDataDir(), "CC15Tests", "accesscontroltest/points/Test.java", 7);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15AccessControlTestiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15AccessControlTestiiunsorted() throws Exception {
    log("testjdk15AccessControlTestiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " accesscontroltest.points.", false, getDataDir(), "CC15Tests", "accesscontroltest/morepoints/Test.java", 5);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15AccessControlTestiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15AccessControlTestiiiunsorted() throws Exception {
    log("testjdk15AccessControlTestiiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " ", false, getDataDir(), "CC15Tests", "accesscontroltest/morepoints/PlusPoint.java", 5);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15AccessControlTestiiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15AccessControlTestivunsorted() throws Exception {
    log("testjdk15AccessControlTestivunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " super.", false, getDataDir(), "CC15Tests", "accesscontroltest/morepoints/PlusPoint.java", 5);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15AccessControlTestivunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15AccessControlTestvunsorted() throws Exception {
    log("testjdk15AccessControlTestvunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " a.", false, getDataDir(), "CC15Tests", "accesscontroltest/points/Point.java", 9);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15AccessControlTestvunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15AccessControlTestviunsorted() throws Exception {
    log("testjdk15AccessControlTestviunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " p.", false, getDataDir(), "CC15Tests", "accesscontroltest/morepoints/Point3d.java", 8);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15AccessControlTestviunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15AccessControlTestviiunsorted() throws Exception {
    log("testjdk15AccessControlTestviiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " this.", false, getDataDir(), "CC15Tests", "accesscontroltest/morepoints/Point3d.java", 8);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15AccessControlTestviiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("testjdk15AccessControlTestviiiunsorted"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15AccessControlTestviiiunsorted() throws Exception {
    log("testjdk15AccessControlTestviiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " q.", false, getDataDir(), "CC15Tests", "accesscontroltest/morepoints/Point3d.java", 11);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15AccessControlTestviiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15AccessControlTestixunsorted() throws Exception {
    log("testjdk15AccessControlTestixunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " this.", false, getDataDir(), "CC15Tests", "accesscontroltest/morepoints/Point3d.java", 11);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15AccessControlTestixunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("testjdk15AccessControlTestviiiunsorted"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15AccessControlTestxunsorted() throws Exception {
    log("testjdk15AccessControlTestxunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " r.", false, getDataDir(), "CC15Tests", "accesscontroltest/morepoints/Point3d.java", 15);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15AccessControlTestxunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("testjdk15AccessControlTestviiiunsorted"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15AccessControlTestxiunsorted() throws Exception {
    log("testjdk15AccessControlTestxiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " ", false, getDataDir(), "CC15Tests", "accesscontroltest/morepoints/Point3d.java", 15);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15AccessControlTestxiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15AccessControlTestxiiunsorted() throws Exception {
    log("testjdk15AccessControlTestxiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " ", false, getDataDir(), "CC15Tests", "accesscontroltest/points/Test.java", 7);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15AccessControlTestxiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15AccessControlTestxiiiunsorted() throws Exception {
    log("testjdk15AccessControlTestxiiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " super(", false, getDataDir(), "CC15Tests", "accesscontroltest/morepoints/Point3d.java", 20);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15AccessControlTestxiiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15LocalVarsTestiunsorted() throws Exception {
    log("testjdk15LocalVarsTestiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " ", false, getDataDir(), "CC15Tests", "localvarstest/Test.java", 6);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15LocalVarsTestiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15LocalVarsTestiiunsorted() throws Exception {
    log("testjdk15LocalVarsTestiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " ", false, getDataDir(), "CC15Tests", "localvarstest/Test.java", 8);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15LocalVarsTestiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15LocalVarsTestiiiunsorted() throws Exception {
    log("testjdk15LocalVarsTestiiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " ", false, getDataDir(), "CC15Tests", "localvarstest/Test.java", 10);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15LocalVarsTestiiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15LocalVarsTestivunsorted() throws Exception {
    log("testjdk15LocalVarsTestivunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " ", false, getDataDir(), "CC15Tests", "localvarstest/Test.java", 13);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15LocalVarsTestivunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15LocalVarsTestvunsorted() throws Exception {
    log("testjdk15LocalVarsTestvunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " ", false, getDataDir(), "CC15Tests", "localvarstest/Test.java", 16);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15LocalVarsTestvunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15LocalVarsTestviunsorted() throws Exception {
    log("testjdk15LocalVarsTestviunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " ", false, getDataDir(), "CC15Tests", "localvarstest/Test.java", 18);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15LocalVarsTestviunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("testjdk15LocalVarsTestiiiunsorted"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15LocalVarsTestviiunsorted() throws Exception {
    log("testjdk15LocalVarsTestviiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " ", false, getDataDir(), "CC15Tests", "localvarstest/Test.java", 20);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15LocalVarsTestviiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("testjdk15LocalVarsTestiiunsorted"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15LocalVarsTestviiiunsorted() throws Exception {
    log("testjdk15LocalVarsTestviiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " if (", false, getDataDir(), "CC15Tests", "localvarstest/Test.java", 10);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15LocalVarsTestviiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("testjdk15LocalVarsTestiiiunsorted"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15LocalVarsTestixunsorted() throws Exception {
    log("testjdk15LocalVarsTestixunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " if (i < ", false, getDataDir(), "CC15Tests", "localvarstest/Test.java", 10);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15LocalVarsTestixunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("testjdk15LocalVarsTestiiiunsorted"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15LocalVarsTestxunsorted() throws Exception {
    log("testjdk15LocalVarsTestxunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " args[i].", false, getDataDir(), "CC15Tests", "localvarstest/Test.java", 10);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15LocalVarsTestxunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("string"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15LocalVarsTestxiunsorted() throws Exception {
    log("testjdk15LocalVarsTestxiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " for(int j = 0; ", false, getDataDir(), "CC15Tests", "localvarstest/Test.java", 20);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15LocalVarsTestxiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15LocalVarsTestxiiunsorted() throws Exception {
    log("testjdk15LocalVarsTestxiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " for(int j = 0; j < 10; j++) ", false, getDataDir(), "CC15Tests", "localvarstest/Test.java", 20);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15LocalVarsTestxiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("testjdk15LocalVarsTestxiunsorted"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15LocalVarsTestxiiiunsorted() throws Exception {
    log("testjdk15LocalVarsTestxiiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " ((String[])objs)[0].", false, getDataDir(), "CC15Tests", "localvarstest/Test.java", 25);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15LocalVarsTestxiiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("string"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15ArraysTestiunsorted() throws Exception {
    log("testjdk15ArraysTestiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " args.", false, getDataDir(), "CC15Tests", "arraystest/Test.java", 9);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15ArraysTestiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("array"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15ArraysTestiiunsorted() throws Exception {
    log("testjdk15ArraysTestiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " args[0].", false, getDataDir(), "CC15Tests", "arraystest/Test.java", 9);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15ArraysTestiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("string"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15ArraysTestiiiunsorted() throws Exception {
    log("testjdk15ArraysTestiiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " new String[0].", false, getDataDir(), "CC15Tests", "arraystest/Test.java", 9);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15ArraysTestiiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("array"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15ArraysTestivunsorted() throws Exception {
    log("testjdk15ArraysTestivunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " new String[] {\"one\", \"two\"}.", false, getDataDir(), "CC15Tests", "arraystest/Test.java", 9);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15ArraysTestivunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("array"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15ArraysTestvunsorted() throws Exception {
    log("testjdk15ArraysTestvunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " new String[] {\"one\", \"two\"}[0].", false, getDataDir(), "CC15Tests", "arraystest/Test.java", 9);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15ArraysTestvunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("string"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15ArraysTestviunsorted() throws Exception {
    log("testjdk15ArraysTestviunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " Test.this.testArray[2].", false, getDataDir(), "CC15Tests", "arraystest/Test.java", 15);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15ArraysTestviunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("string"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15ArraysTestviiunsorted() throws Exception {
    log("testjdk15ArraysTestviiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " testString.", false, getDataDir(), "CC15Tests", "arraystest/Test.java", 17);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15ArraysTestviiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("string"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15ArraysTestviiiunsorted() throws Exception {
    log("testjdk15ArraysTestviiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " Test.this.oneString.", false, getDataDir(), "CC15Tests", "arraystest/Test.java", 17);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15ArraysTestviiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("string"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15ArraysTestixunsorted() throws Exception {
    log("testjdk15ArraysTestixunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " ((String)objs[0]).", false, getDataDir(), "CC15Tests", "arraystest/Test.java", 24);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15ArraysTestixunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("string"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15ConstructorsTestiunsorted() throws Exception {
    log("testjdk15ConstructorsTestiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " new NoCtor(", false, getDataDir(), "CC15Tests", "ctorstest/Test.java", 6);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15ConstructorsTestiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15ConstructorsTestiiunsorted() throws Exception {
    log("testjdk15ConstructorsTestiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " new DefaultCtor(", false, getDataDir(), "CC15Tests", "ctorstest/Test.java", 6);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15ConstructorsTestiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15ConstructorsTestiiiunsorted() throws Exception {
    log("testjdk15ConstructorsTestiiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " new CopyCtor(", false, getDataDir(), "CC15Tests", "ctorstest/Test.java", 6);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15ConstructorsTestiiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15ConstructorsTestivunsorted() throws Exception {
    log("testjdk15ConstructorsTestivunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " new MoreCtors(", false, getDataDir(), "CC15Tests", "ctorstest/Test.java", 6);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15ConstructorsTestivunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15ConstructorsTestvunsorted() throws Exception {
    log("testjdk15ConstructorsTestvunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " new GenericNoCtor<Long>(", false, getDataDir(), "CC15Tests", "ctorstest/Test.java", 6);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15ConstructorsTestvunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15ConstructorsTestviunsorted() throws Exception {
    log("testjdk15ConstructorsTestviunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " new GenericDefaultCtor<Long>(", false, getDataDir(), "CC15Tests", "ctorstest/Test.java", 6);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15ConstructorsTestviunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15ConstructorsTestviiunsorted() throws Exception {
    log("testjdk15ConstructorsTestviiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " new GenericCopyCtor<Long>(", false, getDataDir(), "CC15Tests", "ctorstest/Test.java", 6);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15ConstructorsTestviiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15ConstructorsTestviiiunsorted() throws Exception {
    log("testjdk15ConstructorsTestviiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " new GenericMoreCtors<Long>(", false, getDataDir(), "CC15Tests", "ctorstest/Test.java", 6);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15ConstructorsTestviiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15ConstructorsTestixunsorted() throws Exception {
    log("testjdk15ConstructorsTestixunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " super(", false, getDataDir(), "CC15Tests", "ctorstest/Test.java", 53);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15ConstructorsTestixunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("testjdk15ConstructorsTestiunsorted"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15ConstructorsTestxunsorted() throws Exception {
    log("testjdk15ConstructorsTestxunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " super(", false, getDataDir(), "CC15Tests", "ctorstest/Test.java", 59);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15ConstructorsTestxunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("testjdk15ConstructorsTestiiunsorted"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15ConstructorsTestxiiunsorted() throws Exception {
    log("testjdk15ConstructorsTestxiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " super(", false, getDataDir(), "CC15Tests", "ctorstest/Test.java", 65);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15ConstructorsTestxiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("testjdk15ConstructorsTestiiiunsorted"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15ConstructorsTestxiiiunsorted() throws Exception {
    log("testjdk15ConstructorsTestxiiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " super(", false, getDataDir(), "CC15Tests", "ctorstest/Test.java", 71);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15ConstructorsTestxiiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("testjdk15ConstructorsTestivunsorted"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15ConstructorsTestxivunsorted() throws Exception {
    log("testjdk15ConstructorsTestxivunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " super(", false, getDataDir(), "CC15Tests", "ctorstest/Test.java", 77);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15ConstructorsTestxivunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("testjdk15ConstructorsTestvunsorted"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15ConstructorsTestxvunsorted() throws Exception {
    log("testjdk15ConstructorsTestxvunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " super(", false, getDataDir(), "CC15Tests", "ctorstest/Test.java", 83);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15ConstructorsTestxvunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("testjdk15ConstructorsTestviunsorted"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15ConstructorsTestxviunsorted() throws Exception {
    log("testjdk15ConstructorsTestxviunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " super(", false, getDataDir(), "CC15Tests", "ctorstest/Test.java", 89);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15ConstructorsTestxviunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("testjdk15ConstructorsTestviiunsorted"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15ConstructorsTestxviiunsorted() throws Exception {
    log("testjdk15ConstructorsTestxviiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " super(", false, getDataDir(), "CC15Tests", "ctorstest/Test.java", 95);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15ConstructorsTestxviiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("testjdk15ConstructorsTestviiiunsorted"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
public void testjdk15ConstructorsTestxviiiunsorted() throws Exception {
    log("testjdk15ConstructorsTestxviiiunsorted() start");
    PrintWriter outputWriter  = null;
    PrintWriter logWriter = null;
    try {
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        new CompletionTest().test(outputWriter, logWriter, " new ArrayList<String[]>(", false, getDataDir(), "CC15Tests", "ctorstest/Test.java", 6);
    } finally {
        if (outputWriter != null) {
            outputWriter.flush();
        }
        if (logWriter != null) {
            logWriter.flush();
        }
      log("testjdk15ConstructorsTestxviiiunsorted() end");
    }

    assertFile("Output does not match golden file.", resolveGoldenFile("@"), new File(getWorkDir(), this.getName() + ".ref"), null, new LineDiff(false));
}
}

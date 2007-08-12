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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints;

import com.sun.source.tree.NewClassTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.io.File;
import java.io.IOException;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class ConvertAnonymousToInnerTest extends NbTestCase {
    
    public ConvertAnonymousToInnerTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        clearWorkDir();
        
        File cache = new File(getWorkDir(), "cache");
        
        cache.mkdirs();
        
        IndexUtil.setCacheFolder(cache);
    }

    private static final class FindNewClassTree extends TreePathScanner<TreePath, Void> {
        @Override
        public TreePath visitNewClass(NewClassTree node, Void p) {
            return getCurrentPath();
        }

        @Override
        public TreePath reduce(TreePath r1, TreePath r2) {
            if (r1 == null)
                return r2;
            return r1;
        }
        
    }
    
    public void testSimpleConvert() throws Exception {
        performTest(
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        new Runnable() {\n" +
                "            public void run() {}\n" +
                "        };\n" +
                "    }\n" +
                "}\n",
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        new RunnableImpl();\n" +
                "    }\n" +
                "    private static class RunnableImpl implements Runnable {\n" +
                "        private RunnableImpl() {\n" + 
                "        }\n" + 
                "        public void run() {\n" +
                "        }\n" +
                "    }\n" +
                "}\n");
    }

    public void testDetectLocalVars() throws Exception {
        performTest(
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        final int i = 0;\n"+
                "        new Runnable() {\n" +
                "            public void run() {\n" + 
                "                System.err.println(i);\n" +
                "            }\n" +
                "        };\n" +
                "    }\n" +
                "}\n",
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        final int i = 0;\n"+
                "        new RunnableImpl(i) ;\n" +
                "    }\n" +
                "    private static class RunnableImpl implements Runnable {\n" +
                "        private final int i;\n" +
                "        private RunnableImpl(int i) {\n" + 
                "            this.i = i;\n" +
                "        }\n" + 
                "        public void run() {\n" +
                "            System.err.println(i);\n" +
                "        }\n" +
                "    }\n" +
                "}\n");
    }
    
    public void testDetectLocalVars2() throws Exception {
        performTest(
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        final int i = 0;\n"+
                "        final String s = \"\";\n"+
                "        new Runnable() {\n" +
                "            public void run() {\n" + 
                "                for (int cntr = 0; cntr < i; cntr++) {\n" +
                "                    System.err.println(s);\n" +
                "                }\n" + 
                "            }\n" +
                "        };\n" +
                "    }\n" +
                "}\n",
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        final int i = 0;\n"+
                "        final String s = \"\";\n"+
                "        new RunnableImpl(i, s) ;\n" +
                "    }\n" +
                "    private static class RunnableImpl implements Runnable {\n" +
                "        private final int i;\n" +
                "        private final String s;\n" +
                "        private RunnableImpl(int i, String s) {\n" + 
                "            this.i = i;\n" +
                "            this.s = s;\n" +
                "        }\n" + 
                "        public void run() {\n" + 
                "            for (int cntr = 0; cntr < i; cntr++) {\n" +
                "                System.err.println(s);\n" +
                "            }\n" + 
                "        }\n" +
                "    }\n" +
                "}\n");
    }
    
    public void testNonStatic() throws Exception {
        performTest(
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    private int x;\n" +
                "    public void taragui() {\n" +
                "        new Runnable() {\n" +
                "            public void run() {\n" +
                "                System.err.println(x);\n" +
                "            }\n" +
                "        };\n" +
                "    }\n" +
                "}\n",
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    private int x;\n" +
                "    public void taragui() {\n" +
                "        new RunnableImpl();\n" +
                "    }\n" +
                "    private class RunnableImpl implements Runnable {\n" +
                "        private RunnableImpl() {\n" + 
                "        }\n" + 
                "        public void run() {\n" +
                "            System.err.println(x);\n" +
                "        }\n" +
                "    }\n" +
                "}\n");
    }
    
    public void testConstructorWithParameters1() throws Exception {
        performTest(
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        new java.util.ArrayList(3) {};\n" +
                "    }\n" +
                "}\n",
                "package hierbas.del.litoral;\n\n" +
                "import java.util.ArrayList;\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        new ArrayListImpl(3);\n" +
                "    }\n" +
                "    private static class ArrayListImpl extends ArrayList {\n" +
                "        private ArrayListImpl(int arg0) {\n" + 
                "            super(arg0);\n" + 
                "        }\n" + 
                "    }\n" +
                "}\n");
    }
    
    public void testConstructorWithParameters2() throws Exception {
        performTest(
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        int i = 3;\n" +
                "        new java.util.ArrayList(i) {};\n" +
                "    }\n" +
                "}\n",
                "package hierbas.del.litoral;\n\n" +
                "import java.util.ArrayList;\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        int i = 3;\n" +
                "        new ArrayListImpl(i);\n" +
                "    }\n" +
                "    private static class ArrayListImpl extends ArrayList {\n" +
                "        private ArrayListImpl(int arg0) {\n" + 
                "            super(arg0);\n" + 
                "        }\n" + 
                "    }\n" +
                "}\n");
    }
    
    public void testConstructorWithParameters3() throws Exception {
        performTest(
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    int i = 3;\n" +
                "    public void taragui() {\n" +
                "        new java.util.ArrayList(i) {};\n" +
                "    }\n" +
                "}\n",
                "package hierbas.del.litoral;\n\n" +
                "import java.util.ArrayList;\n" +
                "public class Test {\n" +
                "    int i = 3;\n" +
                "    public void taragui() {\n" +
                "        new ArrayListImpl(i);\n" +
                "    }\n" +
                "    private static class ArrayListImpl extends ArrayList {\n" +
                "        private ArrayListImpl(int arg0) {\n" + 
                "            super(arg0);\n" + 
                "        }\n" + 
                "    }\n" +
                "}\n");
    }
    
    public void testConstructorWithParameters4() throws Exception {
        performTest(
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    java.util.List<? extends CharSequence> l;\n" +
                "    public void taragui() {\n" +
                "        new java.util.ArrayList<CharSequence>(l) {};\n" +
                "    }\n" +
                "}\n",
                "package hierbas.del.litoral;\n\n" +
                "import java.util.ArrayList;\n" +
                "import java.util.Collection;\n" +
                "public class Test {\n" +
                "    java.util.List<? extends CharSequence> l;\n" +
                "    public void taragui() {\n" +
                "        new ArrayListImpl(l);\n" +
                "    }\n" +
                "    private static class ArrayListImpl extends ArrayList<CharSequence> {\n" +
                "        private ArrayListImpl(Collection<? extends CharSequence> arg0) {\n" + 
                "            super(arg0);\n" + 
                "        }\n" + 
                "    }\n" +
                "}\n");
    }
    
    public void testInnerClass1() throws Exception {
        performTest(
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        new X() {};\n" +
                "    }\n" +
                "    class X {}\n" +
                "}\n",
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        new XImpl();\n" +
                "    }\n" +
                "    class X {}\n" +
                "    private class XImpl extends Test.X {\n" +
                "        private XImpl() {\n" +
                "        }\n" +
                "    }\n" +
                "}\n");
    }
    
    public void testInnerClass2() throws Exception {
        performTest(
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        new Test() {};\n" +
                "    }\n" +
                "}\n",
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        new TestImpl();\n" +
                "    }\n" +
                "    private static class TestImpl extends Test {\n" +
                "        private TestImpl() {\n" +
                "        }\n" +
                "    }\n" +
                "}\n");
    }
    
    public void testInnerClass3() throws Exception {
        performTest(
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        new X() {};\n" +
                "    }\n" +
                "    static class X {}\n" +
                "}\n",
                "package hierbas.del.litoral;\n\n" +
                "import hierbas.del.litoral.Test.X;\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        new XImpl();\n" +
                "    }\n" +
                "    static class X {}\n" +
                "    private static class XImpl extends X {\n" +
                "        private XImpl() {\n" +
                "        }\n" +
                "    }\n" +
                "}\n");
    }
    
    public void testInnerClass4() throws Exception {
        performTest(
                "package hierbas.del.litoral;\n\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        new X() {};\n" +
                "    }\n" +
                "    interface X {}\n" +
                "}\n",
                "package hierbas.del.litoral;\n\n" +
                "import hierbas.del.litoral.Test.X;\n" +
                "public class Test {\n" +
                "    public void taragui() {\n" +
                "        new XImpl();\n" +
                "    }\n" +
                "    interface X {}\n" +
                "    private static class XImpl implements X {\n" +
                "        private XImpl() {\n" +
                "        }\n" +
                "    }\n" +
                "}\n");
    }
    
    private void performTest(String test, String golden) throws Exception {
        File testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, test);
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                
                TreePath nct = new FindNewClassTree().scan(workingCopy.getCompilationUnit(), null);
                
                ConvertAnonymousToInner.convertAnonymousToInner(workingCopy, nct);
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(removeWhitespaces(golden), removeWhitespaces(res));
    }
    
    private static String removeWhitespaces(String text) {
        return text.replaceAll(" ", "").replaceAll("\n", "");
    }
}

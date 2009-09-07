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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.java.hints;

import com.sun.source.util.TreePath;
import java.lang.reflect.Method;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.modules.java.hints.infrastructure.TreeRuleTestBase;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Lahoda
 */
public class AssignResultToVariableTest extends TreeRuleTestBase {
    
    public AssignResultToVariableTest(String testName) {
        super(testName);
    }
    
    public void testDoNothingForVoidReturnType() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public void t() {get();} public void get() {}}", 51);
    }
    
    public void testProposeHint() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public void t() {get();} public int get() {}}", 51, "0:50-0:53:hint:Assign Return Value To New Variable");
    }
    
    public void testApplyHintGenericType() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void t() {java.util.List<String> l = null; l.get(0);}}",
                       111 - 25,
                       "0:83-0:88:hint:Assign Return Value To New Variable",
                       "FixImpl",
                       "package test; public class Test {public void t() {java.util.List<String> l = null;String get = l.get(0); }}");
    }

    public void testApplyHintGenericType2() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void t() {java.util.List<? extends String> l = null; l.get(0);}}",
                       121 - 25,
                       "0:93-0:98:hint:Assign Return Value To New Variable",
                       "FixImpl",
                       "package test; public class Test {public void t() {java.util.List<? extends String> l = null;String get = l.get(0); }}");
    }
    
    public void testApplyHintGenericType3() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test<T> {public void t() {get();} T get() {return null;}}",
                       79 - 25,
                       "0:53-0:56:hint:Assign Return Value To New Variable",
                       "FixImpl",
                       "package test; public class Test<T> {public void t() {T get = get(); } T get() {return null;}}");
    }
    
    public void testApplyHintGenericType4() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void t() {test();} private Iterable<? extends CharSequence> test() {return null;}}",
                       77 - 25,
                       "0:50-0:54:hint:Assign Return Value To New Variable",
                       "FixImpl",
                       "package test; public class Test {public void t() {Iterable<? extends CharSequence> test = test(); } private Iterable<? extends CharSequence> test() {return null;}}");
    }
    
    public void testApplyHintGenericType5() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void t() {test();} private Iterable<? super CharSequence> test() {return null;}}",
                       77 - 25,
                       "0:50-0:54:hint:Assign Return Value To New Variable",
                       "FixImpl",
                       "package test; public class Test {public void t() {Iterable<? super CharSequence> test = test(); } private Iterable<? super CharSequence> test() {return null;}}");
    }
    
    public void testApplyHintGenericType6() throws Exception {
        performFixTest("test/Test.java",
                       "package test; import java.util.List; public class Test {public Test() {List<?> l = null;l.get(0); } }",
                       117 - 25,
                       "0:88-0:93:hint:Assign Return Value To New Variable",
                       "FixImpl",
                       "package test; import java.util.List; public class Test {public Test() {List<?> l = null;Object get = l.get(0); } }");
    }
    
    public void testCommentsCopied() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void t() {\n/*t*/get();\n} String get() {return null;}}",
                       82 - 25,
                       "1:5-1:8:hint:Assign Return Value To New Variable",
                       "FixImpl",
                       "package test; public class Test {public void t() { /*t*/ String get = get(); } String get() {return null;}}");
    }
    
    public void testNewClass1() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void t() { new Te|st(); } private static class Test {} }",
                       "0:51-0:61:hint:Assign Return Value To New Variable",
                       "FixImpl",
                       "package test; public class Test {public void t() {Test test = new Test(); } private static class Test {} }");
    }
    
    public void testNewClass2() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void t() { new te|st(); } private static class test {} }",
                       "0:51-0:61:hint:Assign Return Value To New Variable",
                       "FixImpl",
                       "package test; public class Test {public void t() {test test = new test(); } private static class test {} }");
    }
    
    public void testNewClass133825a() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void t() { new Te|st<String>(); } private static class Test<T> {}}",
                       "0:51-0:69:hint:Assign Return Value To New Variable",
                       "FixImpl",
                       "package test; public class Test {public void t() {Test<String> test = new Test<String>(); } private static class Test<T> {}}");
    }
    
    public void testNewClass133825b() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void t() { new Test.In|ner(); } private static class Inner {} }",
                       "0:51-0:67:hint:Assign Return Value To New Variable",
                       "FixImpl",
                       "package test; public class Test {public void t() {Inner inner = new Test.Inner(); } private static class Inner {} }");
    }

    public void testAnonymousClass138223() throws Exception {
        performFixTest("test/Test.java",
                "package test; public class Test {public void t() { new Run|nable() { public void run() {}}; } }",
                "0:51-0:89:hint:Assign Return Value To New Variable",
                "FixImpl",
                "package test; public class Test {public void t() {Runnable runnable = new Runnable() { public void run() { } }; } }");
    }
    
    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path) {
        int offset = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), path.getLeaf());
        
        while (path != null && !new AssignResultToVariable().getTreeKinds().contains(path.getLeaf().getKind()))
            path = path.getParentPath();
        
        if (path == null)
            return null;
        
        try {
            Method m = CaretAwareJavaSourceTaskFactory.class.getDeclaredMethod("setLastPosition", FileObject.class, int.class);

            assertNotNull(m);

            m.setAccessible(true);

            m.invoke(null, new Object[]{info.getFileObject(), offset});
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        
        return new AssignResultToVariable().run(info, path);
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        if (f instanceof AssignResultToVariable.FixImpl) {
            return "FixImpl";
        } else {
            return super.toDebugString(info, f);
        }
    }
    
}

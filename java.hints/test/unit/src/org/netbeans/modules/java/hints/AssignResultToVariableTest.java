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

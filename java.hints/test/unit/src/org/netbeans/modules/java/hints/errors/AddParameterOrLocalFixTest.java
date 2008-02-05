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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.errors;

import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.spi.editor.hints.Fix;

/**
 *
 * @author Jan Lahoda
 */
public class AddParameterOrLocalFixTest extends ErrorHintsTestBase {
    
    private boolean parameter = true;
    
    public AddParameterOrLocalFixTest(String testName) {
        super(testName);
    }

    public void testAddBeforeVararg() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test(String... a) {bbb = 0;}}",
                       91 - 25,
                       "AddParameterOrLocalFix:bbb:int:true",
                       "package test; public class Test {public void test(int bbb,String... a) {bbb = 0;}}");
    }
    
    public void testAddToTheEnd() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test(String[] a) {bbb = 0;}}",
                       90 - 25,
                       "AddParameterOrLocalFix:bbb:int:true",
                       "package test; public class Test {public void test(String[] a,int bbb) {bbb = 0;}}");
    }
    
    public void testAddToTheEmptyParamsList() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test() {bbb = 0;}}",
                       80 - 25,
                       "AddParameterOrLocalFix:bbb:int:true",
                       "package test; public class Test {public void test(int bbb) {bbb = 0;}}");
    }
    
    public void testAddLocalVariableWithComments() throws Exception {
        parameter = false;
        
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test() {int a;\n //test\n |bbb = 0;\n int c; }}",
                       "AddParameterOrLocalFix:bbb:int:false",
                       "package test; public class Test {public void test() {int a; //test int bbb = 0; int c; }}");
    }
    
    public void testAddLocalVariableNotInPlace() throws Exception {
        parameter = false;
        boolean orig = ErrorFixesFakeHint.isCreateLocalVariableInPlace();

        try {
            ErrorFixesFakeHint.setCreateLocalVariableInPlace(false);

            performFixTest("test/Test.java",
                    "package test; public class Test {public void test() {int a;\n |bbb = 0;\n int c; }}",
                    "AddParameterOrLocalFix:bbb:int:false",
                    "package test; public class Test {public void test() {int bbb; int a; bbb = 0; int c; }}");
        } finally {
            ErrorFixesFakeHint.setCreateLocalVariableInPlace(orig);
        }
    }

    public void testAddLocalVariableNotInPlaceInConstr() throws Exception {
        parameter = false;
        
        boolean orig = ErrorFixesFakeHint.isCreateLocalVariableInPlace();

        try {
            ErrorFixesFakeHint.setCreateLocalVariableInPlace(false);

            performFixTest("test/Test.java",
                    "package test; public class Test {public Test() {super();\n int a;\n |bbb = 0;\n int c; }}",
                    "AddParameterOrLocalFix:bbb:int:false",
                    "package test; public class Test {public Test() {super(); int bbb; int a; bbb = 0; int c; }}");
        } finally {
            ErrorFixesFakeHint.setCreateLocalVariableInPlace(orig);
        }
    }
    
    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws IOException {
        List<Fix> fixes = CreateElement.analyze(info, pos);
        List<Fix> result=  new LinkedList<Fix>();
        
        for (Fix f : fixes) {
            if (f instanceof AddParameterOrLocalFix) {
                if (((AddParameterOrLocalFix) f).isParameter() == parameter)
                    result.add(f);
            }
        }
        
        return result;
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return ((AddParameterOrLocalFix) f).toDebugString(info);
    }
}

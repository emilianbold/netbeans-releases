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
    
    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws IOException {
        List<Fix> fixes = CreateElement.analyze(info, pos);
        List<Fix> result=  new LinkedList<Fix>();
        
        for (Fix f : fixes) {
            if (f instanceof AddParameterOrLocalFix) {
                if (((AddParameterOrLocalFix) f).isParameter())
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

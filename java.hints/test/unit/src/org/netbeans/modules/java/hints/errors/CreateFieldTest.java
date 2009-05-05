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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.util.TreePath;
import java.io.IOException;
import org.netbeans.api.java.source.CompilationInfo;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.spi.editor.hints.Fix;


/**
 *
 * @author Jan Lahoda
 */
public class CreateFieldTest extends ErrorHintsTestBase {
    
    /** Creates a new instance of CreateElementTest */
    public CreateFieldTest(String name) {
        super(name);
    }
    
    public void test96590() throws Exception {
        performAnalysisTest("test/Test.java", "package test; import java.lang.annotation.Retention; @Retention(value = RetentionPolicy) public @interface Test {}", 125 - 48);
    }
    
    //Creating a field from an annonymous class expression
    public void test118701() throws Exception {
        performFixTest("test/Test.java",
                       "package test;import java.io.Writer;public class Test {public static void main(String[] args) {ww = new Writer() {public void write(char[] cbuf, int off, int len) {}public void close() {}public void flush() {}};}}",
                       120 - 25,
                       "CreateFieldFix:ww:test.Test:java.io.Writer:[private, static]",
                       "package test;import java.io.Writer;public class Test { private static Writer ww; public static void main(String[] args) {ww = new Writer() {public void write(char[] cbuf, int off, int len) {}public void close() {}public void flush() {}};}}");
    }

    public void testFinalFromCtor() throws Exception {
        performFixTest("test/Test.java",
                "package test;public class Test {public Test() { i|i = 1; }}",
                "CreateFieldFix:ii:test.Test:int:[private, final]",
                "package test;public class Test { private final int ii; public Test() { ii = 1; }}");
    }
    
    protected List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws IOException {
        List<Fix> fixes = CreateElement.analyze(info, pos);
        List<Fix> result=  new LinkedList<Fix>();
        
        for (Fix f : fixes) {
            if (f instanceof CreateFieldFix)
                result.add(f);
        }
        
        return result;
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return ((CreateFieldFix) f).toDebugString(info);
    }
    
}

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
import javax.lang.model.type.TypeMirror;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Lahoda
 */
public class UtilitiesTest extends NbTestCase {
    
    public UtilitiesTest(String testName) {
        super(testName);
    }

    protected @Override void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        super.setUp();
    }

    public void testNameGuess1() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() {toString();}}", 54, "toString");
    }

    public void testNameGuess2() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() {getX();} public int getX() {return 0;}}", 54, "x");
    }
    
    public void testNameGuess3() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() {getData();} public int getData() {return 0;}}", 54, "data");
    }
    
    public void testNameGuess4() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() {getProcessedData();} public int getProcessedData() {return 0;}}", 54, "processedData");
    }
    
    public void testNameGuess5() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() {isEnabled();} public boolean isEnabled() {return true;}}", 54, "enabled");
    }
    
    public void testNameGuess6() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() {get();} public int get() {return 0;}}", 52, "get");
    }
    
    public void testNameGuessKeyword() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() {getDo();} public int getDo() {return 0;}}", 52, "aDo");
    }
    
    public void testNameGuessKeywordNoShortName() throws Exception {
        performNameGuessTest("package test; public class Test {public void t() {t(this);}}", 54, "aThis");
    }
    
    public void testNameGuessKeywordNoShortName2() throws Exception {
        assertEquals("aDo", Utilities.adjustName("do"));
    }
    
    public void testCapturedTypeArray164543() throws Exception {
        performCapturedTypeTest("package test; public class Test {public void t() {java.util.Map m; m.getClass().getTypeParameters(|); }}",
                                "java.lang.reflect.TypeVariable<java.lang.Class<? extends java.util.Map>>[]");
    }

    public void testCapturedTypeExtends170574() throws Exception {
        performCapturedTypeTest("package test; interface Foo<T> {Foo<? extends T> foo();}" +
                "public class Test {public void t() {Foo<? extends Number> bar = null; bar.foo(|);}}",
                                "test.Foo<? extends java.lang.Number>");
    }

    public void testCapturedTypeSuper170574() throws Exception {
        performCapturedTypeTest("package test; interface Foo<T> {Foo<? super T> foo();}" +
                "public class Test {public void t() {Foo<? super Number> bar = null; bar.foo(|);}}",
                                "test.Foo<? super java.lang.Number>");
    }

    protected void prepareTest(String code) throws Exception {
        clearWorkDir();
        FileObject workFO = FileUtil.toFileObject(getWorkDir());
        
        assertNotNull(workFO);
        
        FileObject sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");
        
        FileObject data = FileUtil.createData(sourceRoot, "test/Test.java");
        
        TestUtilities.copyStringToFile(FileUtil.toFile(data), code);
        
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache);
        
        DataObject od = DataObject.find(data);
        EditorCookie ec = od.getCookie(EditorCookie.class);
        
        assertNotNull(ec);
        
        Document doc = ec.openDocument();
        
        doc.putProperty(Language.class, JavaTokenId.language());
        doc.putProperty("mimeType", "text/x-java");
        
        JavaSource js = JavaSource.forFileObject(data);
        
        assertNotNull(js);
        
        info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        
        assertNotNull(info);
    }
    
    private CompilationInfo info;
    
    private void performNameGuessTest(String code, int position, String desiredName) throws Exception {
        prepareTest(code);
        
        TreePath tp = info.getTreeUtilities().pathFor(position);
        
        String name = Utilities.guessName(info, tp);
        
        assertEquals(desiredName, name);
    }

    private void performCapturedTypeTest(String code, String golden) throws Exception {
        int[] position = new int[1];
        code = org.netbeans.modules.java.hints.TestUtilities.detectOffsets(code, position);

        performCapturedTypeTest(code, position[0], golden);
    }
    
    private void performCapturedTypeTest(String code, int position, String golden) throws Exception {
        prepareTest(code);

        TreePath tp = info.getTreeUtilities().pathFor(position);
        TypeMirror type = info.getTrees().getTypeMirror(tp);
        TypeMirror resolved = Utilities.resolveCapturedType(info, type);

        assertEquals(golden, org.netbeans.modules.editor.java.Utilities.getTypeName(resolved, true).toString());
    }
}

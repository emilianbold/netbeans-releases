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

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.infrastructure.TreeRuleTestBase;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Jan Lahoda
 */
public class IllegalInstanceOfTest extends TreeRuleTestBase {
    
    public IllegalInstanceOfTest(String testName) {
        super(testName);
    }

    public void testSimple1() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; import javax.lang.model.element.*; public class Test {public void test() {Element e = null; boolean b = e instanceof TypeElement;}}",
                            154 - 30,
                            "0:118-0:142:verifier:Illegal Use of instanceOf");
    }
    
    public void testSimple2() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; import javax.lang.model.element.*; public class Test {public void test() {Element e = null; boolean b = e instanceof CharSequence;}}",
                            154 - 30);
    }
    
    public void testSimple3() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; import javax.lang.model.type.*; public class Test {public void test() {TypeMirror e = null; boolean b = e instanceof DeclaredType;}}",
                            154 - 30,
                            "0:118-0:143:verifier:Illegal Use of instanceOf");
    }
    
    public void testSimple4() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; import com.sun.source.tree.*; public class Test {public void test() {Tree e = null; boolean b = e instanceof StatementTree;}}",
                            146 - 30,
                            "0:110-0:136:verifier:Illegal Use of instanceOf");
    }
    
    public void testSimple5() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; import com.sun.source.tree.*; public class Test {public void test() {Tree e = null; boolean b = e instanceof Scope;}}",
                            146 - 30);
    }
    
    public void test106461() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; import javax.lang.model.element.*; public class Test {public void test() {Element e = null; if (e instanceof )}}",
                            146 - 30);
    }
    
    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path) {
        return new IllegalInstanceOf().run(info, path);
    }

    @Override
    protected FileObject[] extraClassPath() {
        FileObject api = URLMapper.findFileObject(Tree.class.getProtectionDomain().getCodeSource().getLocation());
        
        assertNotNull(api);
        
        return new FileObject[] {FileUtil.getArchiveRoot(api)};
    }

}

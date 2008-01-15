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

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.WrongPackageSuggestion.CorrectPackageDeclarationFix;
import org.netbeans.modules.java.hints.options.HintsSettings;
import org.netbeans.modules.java.hints.spi.AbstractHint.HintSeverity;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Lahoda
 */
public class WrongPackageSuggestionTest extends NbTestCase {
    
    static {
        try {
            SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public WrongPackageSuggestionTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        HintsSettings.setSeverity(new WrongPackageSuggestion().getPreferences(null), HintSeverity.WARNING);
        super.setUp();
    }
    
    public void testEvaluate1() throws Exception {
        performAnalysisTest("test/Test.java", "package other; public class Test{}", Collections.singletonList("0:8-0:13:verifier:Incorrect Package"));
    }
    
    public void testEvaluate2() throws Exception {
        performAnalysisTest("Test.java", "package other; public class Test{}", Collections.singletonList("0:8-0:13:verifier:Incorrect Package"));
    }
    
    public void testEvaluate3() throws Exception {
        performAnalysisTest("test/Test.java", "public class Test{}", Collections.singletonList("0:0-0:1:verifier:Incorrect Package"));
    }
    
    public void testEvaluate4() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test{}", Collections.<String>emptyList());
    }
    
    public void testEvaluate5() throws Exception {
        performAnalysisTest("Test.java", "public class Test{}", Collections.<String>emptyList());
    }
    
    public void testEvaluate121562() throws Exception {
        performAnalysisTest("test/Test.java", "", Collections.<String>emptyList());
    }
    
    public void testAdjustPackageClause1() throws Exception {
        performAdjustPackageClauseTest("test/Test.java", "package other; public class Test{}", "test", "package test; public class Test{}");
    }
    
    public void testAdjustPackageClause2() throws Exception {
        performAdjustPackageClauseTest("Test.java", "package other; public class Test{}", "", "public class Test{}");
    }
    
    public void testAdjustPackageClause3() throws Exception {
        performAdjustPackageClauseTest("test/Test.java", "public class Test{}", "test", "package test; public class Test{}");
    }
    
    protected void prepareTest(String fileName, String code) throws Exception {
        FileObject workFO = makeScratchDir(this);
        
        assertNotNull(workFO);
        
        FileObject sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");
        
        FileObject data = FileUtil.createData(sourceRoot, fileName);
        File dataFile = FileUtil.toFile(data);
        
        assertNotNull(dataFile);
        
        TestUtilities.copyStringToFile(dataFile, code);
        
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache);
        
        DataObject od = DataObject.find(data);
        EditorCookie ec = od.getCookie(EditorCookie.class);
        
        assertNotNull(ec);
        
        doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        
        JavaSource js = JavaSource.forFileObject(data);
        
        assertNotNull(js);
        
        info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        
        assertNotNull(info);
    }
    
    private CompilationInfo info;
    private Document doc;
    
    private void performAnalysisTest(String fileName, String code, List<String> golden) throws Exception {
        prepareTest(fileName, code);
        
        final WrongPackageSuggestion wps = new WrongPackageSuggestion();
        final List<ErrorDescription> errors = new ArrayList<ErrorDescription>();
        
        class ScannerImpl extends TreePathScanner {
            @Override
            public Object scan(Tree tree, Object p) {
                if (tree != null && wps.getTreeKinds().contains(tree.getKind())) {
                    List<ErrorDescription> localErrors = wps.run(info, new TreePath(getCurrentPath(), tree));
                    
                    if (localErrors != null) {
                        errors.addAll(localErrors);
                    }
                }
                return super.scan(tree, p);
            }
        };
        
        new ScannerImpl().scan(info.getCompilationUnit(), null);
        
        List<String> errorDisplayNames = new ArrayList<String>();
        
        for (ErrorDescription ed : errors) {
            errorDisplayNames.add(ed.toString());
        }
        
        assertEquals(golden, errorDisplayNames);
    }
    
    private void performAdjustPackageClauseTest(String fileName, String code, String correctPackageName, String correct) throws Exception {
        prepareTest(fileName, code);
        
        CorrectPackageDeclarationFix fix = new CorrectPackageDeclarationFix(info.getFileObject(), correctPackageName);
        
        fix.implement();
        
        TokenHierarchy th1 = TokenHierarchy.get(doc);
        TokenHierarchy th2 = TokenHierarchy.create(correct, JavaTokenId.language());
        
        TokenSequence<JavaTokenId> ts1 = th1.tokenSequence(JavaTokenId.language());
        TokenSequence<JavaTokenId> ts2 = th2.tokenSequence(JavaTokenId.language());
        
        ts1.moveNext();
        ts2.moveNext();
        
        boolean firstHasNext = true;
        boolean secondHasNext = true;
        
        do {
            Token<JavaTokenId> firstToken = ts1.token();
            Token<JavaTokenId> secondToken = ts2.token();
            
            while (IGNORED_TOKENS.contains(firstToken.id()) && firstHasNext) {
                firstHasNext = ts1.moveNext();
                firstToken = ts1.token();
            }
            
            while (IGNORED_TOKENS.contains(secondToken.id()) && secondHasNext) {
                secondHasNext = ts2.moveNext();
                secondToken = ts2.token();
            }
            
            if (!firstHasNext || !secondHasNext)
                break;
            
            if (firstToken.id() != secondToken.id() || !TokenUtilities.equals(firstToken.text(), secondToken.text())) {
                //does not match:
                assertEquals(correct, doc.getText(0, doc.getLength()));
            }
            
            firstHasNext = ts1.moveNext();
            secondHasNext = ts2.moveNext();
        } while (firstHasNext && secondHasNext);
        
        if (firstHasNext || secondHasNext) {
            //does not match:
            assertEquals(correct, doc.getText(0, doc.getLength()));
        }
        
    }
    
    private Set<JavaTokenId> IGNORED_TOKENS = EnumSet.of(JavaTokenId.WHITESPACE, JavaTokenId.LINE_COMMENT, JavaTokenId.BLOCK_COMMENT, JavaTokenId.JAVADOC_COMMENT);
    
    /**Copied from org.netbeans.api.project.
     * Create a scratch directory for tests.
     * Will be in /tmp or whatever, and will be empty.
     * If you just need a java.io.File use clearWorkDir + getWorkDir.
     */
    public static FileObject makeScratchDir(NbTestCase test) throws IOException {
        test.clearWorkDir();
        File root = test.getWorkDir();
        assert root.isDirectory() && root.list().length == 0;
        FileObject fo = FileUtil.toFileObject(root);
        if (fo != null) {
            // Presumably using masterfs.
            return fo;
        } else {
            // For the benefit of those not using masterfs.
            LocalFileSystem lfs = new LocalFileSystem();
            try {
                lfs.setRootDirectory(root);
            } catch (PropertyVetoException e) {
                assert false : e;
            }
            Repository.getDefault().addFileSystem(lfs);
            return lfs.getRoot();
        }
    }
    
}

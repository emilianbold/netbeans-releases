/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.cnd.highlight.semantic;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author Sergey Grinev
 */
public class MarkOccurrencesTest extends SemanticHighlightingTestBase {

    public MarkOccurrencesTest(String testName) {
        super(testName);
    }
    private static final String SOURCE = "markocc.cc"; // NOI18N

    @Override
    protected void setUp() throws Exception {
        try {
            super.setUp();
        } catch (IOException e) {
            final String message = e.getMessage();
            if (message != null && message.startsWith("Cannot delete file")) { // NOI18N
                Logger.getInstance().log(Level.INFO, "MarkOccurrencesTest {0}", e);
            } else {
                throw e;
            }
        }
    }
    
    public void testMacro() throws Exception {
        // FOO 
        performTest(SOURCE, 214);
    }

    public void testLocalVariable() throws Exception {
        performTest(SOURCE, 236);
    }

    public void testGlobalVariable() throws Exception {
        // int bar
        performTest(SOURCE, 264);
    }

    public void testField() throws Exception {
        //boo 
        performTest(SOURCE, 122);
    }

    public void testCtor() throws Exception {
        // Foo() 
        performTest(SOURCE, 115);
    }

    public void testCtor2() throws Exception {
        // Foo(int) 
        performTest(SOURCE, 138);
    }

    public void testClassName() throws Exception {
        // class Foo 
        performTest(SOURCE, 110);
    }

    public void testPreproc1() throws Exception {
        performTest(SOURCE, 30, 1);
    }

    public void testPreproc2() throws Exception {
        performTest(SOURCE, 32, 3);
    }

    public void testPreproc3() throws Exception {
        performTest(SOURCE, 38, 3);
    }

    public void testPreproc4() throws Exception {
        performTest(SOURCE, 44, 7);
    }

    public void testPreproc5() throws Exception {
        performTest(SOURCE, 34, 3);
    }

    public void testPreproc6() throws Exception {
        performTest(SOURCE, 42, 5);
    }

    public void testSeveralDeclarations() throws Exception {
        performTest(SOURCE, 48, 10);
    }

    public void testConstAndNonConstMethods() throws Exception {
        performTest(SOURCE, 57, 15);
    }

    public void testIZ175700() throws Exception {
        // IZ#175700 : [code model] Parser does not recognized inline initialization in constructor
        performTest(SOURCE, 80, 5);
    }
    
    public void testStringLiterals() throws Exception {
        performTest(SOURCE, 84, 16);
    }
    
    public void testCharLiterals() throws Exception {
        performTest(SOURCE, 94, 22);
    }

    public void testAddSymbolZeroParams() throws Exception {
        performTest(SOURCE, 107, 15);
        clearWorkDir();
        performTest(SOURCE, 116, 25);
        clearWorkDir();
        performTest(SOURCE, 139, 35);
    }
    
    public void testAddSymbolOneParam() throws Exception {
        performTest(SOURCE, 108, 15);
        clearWorkDir();
        performTest(SOURCE, 121, 25);
        clearWorkDir();
        performTest(SOURCE, 138, 35);
    }
    
    public void testAddSymbolTwoParams() throws Exception {
        performTest(SOURCE, 109, 15);
        clearWorkDir();
        performTest(SOURCE, 126, 25);
        clearWorkDir();
        performTest(SOURCE, 141, 35);
        clearWorkDir();
    }
    
    public void test206416_1() throws Exception {
        if (!Utilities.isWindows()) { // somehow it is failing on windows due to not removed file
            // #206416 - Renaming a local variable in C/C++ code changes the name of other variables with the same name in other scopes
            performTest(SOURCE, 148, 14);
            clearWorkDir();
            performTest(SOURCE, 152, 10);
        }
    }
    
    public void test206416_2() throws Exception {
        // #206416 - Renaming a local variable in C/C++ code changes the name of other variables with the same name in other scopes
        performTest(SOURCE, 149, 19);
        clearWorkDir();
        performTest(SOURCE, 150, 20);
    }
    
    public void test206416_3() throws Exception {
        // #206416 - Renaming a local variable in C/C++ code changes the name of other variables with the same name in other scopes
        performTest(SOURCE, 155, 14);
        clearWorkDir();
        performTest(SOURCE, 156, 10);
        clearWorkDir();
    }
    
    public void testAddSymbolMoreParams() throws Exception {
        performTest(SOURCE, 111, 15);
        clearWorkDir();
        performTest(SOURCE, 132, 25);
        clearWorkDir();
        performTest(SOURCE, 140, 35);
        clearWorkDir();
    }
    
    public void test231272_1() throws Exception {
        performTest(SOURCE, 164, 15);
        clearWorkDir();
        performTest(SOURCE, 181, 20);
    }
    
    public void test231272_2() throws Exception {
        performTest(SOURCE, 171, 15);
        clearWorkDir();
        performTest(SOURCE, 181, 10);
    }

    @Override
    protected Collection<? extends CsmOffsetable> getBlocks(FileImpl testFile, int offset) {
        BaseDocument doc;
        try {
            doc = getBaseDocument(FileUtil.toFile(testFile.getFileObject()));
        } catch (Exception e) {
            e.printStackTrace(System.err);
            doc = null;
        }
        return MarkOccurrencesHighlighter.getOccurrences(doc, testFile, offset, null);
    }
}

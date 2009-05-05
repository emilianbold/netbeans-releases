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
package org.netbeans.modules.cnd.highlight.semantic;

import java.util.Collection;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;

/**
 *
 * @author Sergey Grinev
 */
public class MarkOccurrencesTest extends SemanticHighlightingTestBase {

    public MarkOccurrencesTest(String testName) {
        super(testName);
    }
    private static final String SOURCE = "markocc.cc"; // NOI18N

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
    
    protected Collection<? extends CsmOffsetable> getBlocks(FileImpl testFile, int offset) {
        BaseDocument doc;
        try {
            doc = getBaseDocument(testFile.getFile());
        } catch (Exception e) {
            e.printStackTrace(System.err);
            doc = null;
        }
        return MarkOccurrencesHighlighter.getOccurrences(doc, testFile, offset, null);
    }
}

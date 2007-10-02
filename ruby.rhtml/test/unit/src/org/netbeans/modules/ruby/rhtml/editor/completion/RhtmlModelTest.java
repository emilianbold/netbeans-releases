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

package org.netbeans.modules.ruby.rhtml.editor.completion;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.CharBuffer;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.RubyTestBase;
import org.netbeans.modules.ruby.rhtml.lexer.api.RhtmlTokenId;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class RhtmlModelTest extends RubyTestBase {

    public RhtmlModelTest(String testName) {
        super(testName);
    }

    @SuppressWarnings(value = "unchecked")
    private static String rhtmlToRuby(String rhtml) {
        TokenHierarchy hi = TokenHierarchy.create(rhtml, RhtmlTokenId.language());
        TokenSequence<RhtmlTokenId> ts = hi.tokenSequence();

        RhtmlModel model = new RhtmlModel(null);

        StringBuilder buffer = new StringBuilder();
        model.eruby(buffer, hi, ts);

        return buffer.toString();
    }

    public void checkEruby(NbTestCase test, String relFilePath) throws Exception {
        File rhtmlFile = new File(test.getDataDir(), relFilePath + ".rhtml");
        if (!rhtmlFile.exists()) {
            NbTestCase.fail("File " + rhtmlFile + " not found.");
        }
        String rhtml = readFile(test, rhtmlFile);

        String generatedRuby = rhtmlToRuby(rhtml);

        File rubyFile = new File(test.getDataDir(), relFilePath + ".rb");
        if (!rubyFile.exists()) {
            if (!rubyFile.createNewFile()) {
                NbTestCase.fail("Cannot create file " + rubyFile);
            }
            FileWriter fw = new FileWriter(rubyFile);
            try {
                fw.write(generatedRuby.toString());
            }
            finally{
                fw.close();
            }
            NbTestCase.fail("Created generated ruby dump file " + rubyFile + "\nPlease re-run the test.");
        }

        String ruby = readFile(test, rubyFile);
        assertEquals(ruby, generatedRuby);

        // Make sure the generated file doesn't have errors
        FileObject rubyFo = FileUtil.toFileObject(rubyFile);
        assertNotNull(rubyFo);
        CompilationInfo info = getInfo(rubyFo);
        assertNotNull(info);
        assertNotNull(AstUtilities.getRoot(info));
        assertTrue(info.getDiagnostics().size() == 0);
    }

    private static String readFile(NbTestCase test, File f) throws Exception {
        FileReader r = new FileReader(f);
        int fileLen = (int)f.length();
        CharBuffer cb = CharBuffer.allocate(fileLen);
        r.read(cb);
        cb.rewind();
        return cb.toString();
    }

    public void testEruby() throws Exception {
        checkEruby(this, "testfiles/conv");
    }

    public void testEruby2() throws Exception {
        checkEruby(this, "testfiles/test2");
    }

    public void testEruby108990() throws Exception {
        checkEruby(this, "testfiles/quotes");
    }

    public void testEruby112877() throws Exception {
        checkEruby(this, "testfiles/other-112877");
    }
}

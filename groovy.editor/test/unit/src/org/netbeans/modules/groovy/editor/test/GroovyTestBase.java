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

package org.netbeans.modules.groovy.editor.test;

import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.groovy.editor.Formatter;
import org.netbeans.modules.groovy.editor.GroovyIndex;
import org.netbeans.modules.groovy.editor.GroovyLanguage;
import org.netbeans.modules.groovy.editor.lexer.GroovyTokenId;
import org.netbeans.modules.gsf.GsfTestBase;
import org.netbeans.modules.gsf.spi.DefaultLanguageConfig;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Adamek
 */
public class GroovyTestBase extends GsfTestBase {

    protected FileObject testFO;

    public GroovyTestBase(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        GroovyIndex.setClusterUrl("file:/bogus"); // No translation
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        testFO = workDir.createData("Test.groovy");
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new GroovyLanguage();
    }
    
    @Override
    protected String getPreferredMimeType() {
        return GroovyTokenId.GROOVY_MIME_TYPE;
    }

    @Override
    public org.netbeans.modules.gsf.api.Formatter getFormatter(IndentPrefs preferences) {
        /* Findbugs-removed: 
        if (preferences == null) {
        preferences = new IndentPrefs(4,4);
        }*/

//        Preferences prefs = NbPreferences.forModule(JsFormatterTest.class);
//        prefs.put(FmtOptions.indentSize, Integer.toString(preferences.getIndentation()));
//        prefs.put(FmtOptions.continuationIndentSize, Integer.toString(preferences.getHangingIndentation()));
//        CodeStyle codeStyle = CodeStyle.getTestStyle(prefs);
        
        Formatter formatter = new Formatter();//codeStyle, 80);
        
        return formatter;
    }

    @Override
    protected FileObject getTestFileObject() {
        return testFO;
    }

    // Called via reflection from NbUtilities and AstUtilities. This is necessary because
    // during tests, going from a FileObject to a BaseDocument only works
    // if all the correct data loaders are installed and working - and that
    // hasn't been the case; we end up with PlainDocuments instead of BaseDocuments.
    // If anyone can figure this out, please let me know and simplify the
    // test infrastructure.
    public static BaseDocument createDocument(String s) {
        BaseDocument doc = GsfTestBase.createDocument(s);
        doc.putProperty(org.netbeans.api.lexer.Language.class, GroovyTokenId.language());
        doc.putProperty("mimeType", GroovyTokenId.GROOVY_MIME_TYPE);

        return doc;
    }
    
    public static BaseDocument getDocumentFor(FileObject fo) {
        return createDocument(read(fo));
    }
}

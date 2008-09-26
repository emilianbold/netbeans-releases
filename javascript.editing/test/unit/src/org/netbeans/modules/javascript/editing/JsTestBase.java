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

package org.netbeans.modules.javascript.editing;

import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.api.Parser;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.GsfTestBase;
import org.netbeans.modules.gsf.api.Formatter;
import org.netbeans.modules.gsf.spi.DefaultLanguageConfig;
import org.netbeans.modules.javascript.editing.lexer.JsTokenId;
import org.openide.filesystems.FileObject;

/**
 * @author Tor Norbye
 */
public abstract class JsTestBase extends GsfTestBase {

    public JsTestBase(String testName) {
        super(testName);
    }

    @Override
    protected boolean runInEQ() {
        // Must run in AWT thread (BaseKit.install() checks for that)
        return true;
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new JsLanguage();
    }
    
    @Override
    protected String getPreferredMimeType() {
        return JsTokenId.JAVASCRIPT_MIME_TYPE;
    }
    
    @Override
    protected Parser getParser() {
        JsParser.runtimeException = null;
        return super.getParser();
    }

    @Override
    protected void validateParserResult(ParserResult result) {
        JsTestBase.assertNull(JsParser.runtimeException != null ? JsParser.runtimeException.toString() : "", JsParser.runtimeException);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        JsIndex.setClusterUrl("file:/bogus"); // No translation
        getXTestJsCluster();
    }
    
    @Override
    public Formatter getFormatter(IndentPrefs preferences) {
        if (preferences == null) {
            preferences = new IndentPrefs(4,4);
        }

        Preferences prefs = MimeLookup.getLookup(MimePath.get(JsTokenId.JAVASCRIPT_MIME_TYPE)).lookup(Preferences.class);
        prefs.putInt(SimpleValueNames.SPACES_PER_TAB, preferences.getIndentation());
        
        JsFormatter formatter = new JsFormatter();
        
        return formatter;
    }
    
    // Called via reflection from GsfUtilities. This is necessary because
    // during tests, going from a FileObject to a BaseDocument only works
    // if all the correct data loaders are installed and working - and that
    // hasn't been the case; we end up with PlainDocuments instead of BaseDocuments.
    // If anyone can figure this out, please let me know and simplify the
    // test infrastructure.
    public static BaseDocument getDocumentFor(FileObject fo) {
        BaseDocument doc = GsfTestBase.createDocument(read(fo));
        doc.putProperty(org.netbeans.api.lexer.Language.class, JsTokenId.language());
        doc.putProperty("mimeType", JsTokenId.JAVASCRIPT_MIME_TYPE);

        return doc;
    }
    
    protected String[] JAVASCRIPT_TEST_FILES = new String[] {
        "testfiles/arraytype.js",
        "testfiles/bubble.js",
        "testfiles/class-inheritance-ext.js",
        "testfiles/class-via-function.js",
        "testfiles/classes.js",
        "testfiles/classprops.js",
        "testfiles/completion/lib/comments.js",
        "testfiles/completion/lib/expressions.js",
        "testfiles/completion/lib/expressions2.js",
        "testfiles/completion/lib/expressions3.js",
        "testfiles/completion/lib/expressions4.js",
        "testfiles/completion/lib/expressions5.js",
        "testfiles/completion/lib/test1.js",
        "testfiles/completion/lib/test129036.js",
        "testfiles/completion/lib/test2.js",
        "testfiles/completion/lib/yahoo.js",
        "testfiles/dnd.js",
        "testfiles/dragdrop.js",
        "testfiles/e4x.js",
        "testfiles/e4x2.js",
        "testfiles/e4xexample1.js",
        "testfiles/e4xexample2.js",
        "testfiles/embedding/convertscript.html.js",
        "testfiles/embedding/embed124916.erb.js",
        "testfiles/embedding/fileinclusion.html.js",
        "testfiles/embedding/mixed.erb.js",
        "testfiles/embedding/rails-index.html.js",
        "testfiles/embedding/sideeffects.html.js",
        "testfiles/embedding/yuisample.html.js",
        "testfiles/events.js",
        "testfiles/fileinclusion.html.js",
        "testfiles/indexable/dojo.js",
        "testfiles/indexable/dojo.uncompressed.js",
        "testfiles/indexable/ext-all-debug.js",
        "testfiles/indexable/ext-all.js",
        "testfiles/indexable/foo.js",
        "testfiles/indexable/foo.min.js",
        "testfiles/indexable/lib.js",
        "testfiles/indexable/yui-debug.js",
        "testfiles/indexable/yui-min.js",
        "testfiles/indexable/yui.js",
        "testfiles/jmaki-uncompressed.js",
        "testfiles/jsexample1.js",
        "testfiles/newstyle-prototype.js",
        "testfiles/occurrences.js",
        "testfiles/occurrences2.js",
        "testfiles/oldstyle-prototype.js",
        "testfiles/orig-dojo.js.uncompressed.js",
        "testfiles/prototype-new.js",
        "testfiles/prototype.js",
        "testfiles/rename.js",
        "testfiles/returntypes.js",
        "testfiles/semantic1.js",
        "testfiles/semantic2.js",
        "testfiles/semantic3.js",
        "testfiles/semantic4.js",
        "testfiles/semantic5.js",
        "testfiles/semantic6.js",
        "testfiles/semantic7.js",
        "testfiles/simple.js",
        "testfiles/SpryAccordion.js",
        "testfiles/SpryData.js",
        "testfiles/SpryEffects.js",
        "testfiles/SpryXML.js",
        "testfiles/stub_dom2_Node.js",
        "testfiles/stub_dom_Window.js",
        "testfiles/stub_Element.js",
        "testfiles/switches.js",
        "testfiles/tryblocks.js",
        "testfiles/two-names.js",
        "testfiles/types1.js",
        "testfiles/types2.js",
        "testfiles/woodstock-body.js",
        "testfiles/woodstock2.js",
        "testfiles/yui-anim.js",
        "testfiles/yui.js",
    };
}

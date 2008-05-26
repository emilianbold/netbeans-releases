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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.Action;
import org.mozilla.javascript.Node;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.api.ParseListener;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.TranslatedSource;
import org.netbeans.modules.gsf.GsfTestBase;
import org.netbeans.modules.gsf.GsfTestCompilationInfo;
import org.netbeans.modules.gsf.Language;
import org.netbeans.modules.gsf.LanguageRegistry;
import org.netbeans.modules.gsf.api.Formatter;
import org.netbeans.modules.gsf.api.GsfLanguage;
import org.netbeans.modules.javascript.editing.lexer.JsTokenId;
import org.netbeans.modules.gsf.spi.DefaultParseListener;
import org.netbeans.modules.gsf.spi.DefaultParserFile;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbPreferences;

/**
 * @author Tor Norbye
 */
public abstract class JsTestBase extends GsfTestBase {

    public JsTestBase(String testName) {
        super(testName);
    }
    
    @Override
    protected void initializeClassPaths() {
        String jsDir = System.getProperty("xtest.js.home");
        if (jsDir == null) {
            throw new RuntimeException("xtest.js.home property has to be set when running within binary distribution");
        }
        File clusterDir = new File(jsDir);
        if (clusterDir.exists()) {
            FileObject preindexed = FileUtil.toFileObject(clusterDir).getFileObject("preindexed");
            if (preindexed != null) {
                JsIndexer.setPreindexedDb(preindexed);
            }
        }
        
        initializeRegistry();
        // Force classpath initialization
        LanguageRegistry.getInstance().getLibraryUrls();
        Language language = LanguageRegistry.getInstance().getLanguageByMimeType(JsTokenId.JAVASCRIPT_MIME_TYPE);
        org.netbeans.modules.gsfret.source.usages.ClassIndexManager.get(language).getBootIndices();
    }    

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        JsIndex.setClusterUrl("file:/bogus"); // No translation
        getXTestJsCluster();
        
//        TestLanguageProvider.register(RhtmlTokenId.language());
//        TestLanguageProvider.register(HTMLTokenId.language());
        
//        rhtmlReformatFactory = new RhtmlIndentTaskFactory();
//        MockMimeLookup.setInstances(MimePath.parse(RubyInstallation.RHTML_MIME_TYPE), rhtmlReformatFactory);
//        htmlReformatFactory = new HtmlIndentTaskFactory();
//        MockMimeLookup.setInstances(MimePath.parse("text/html"), htmlReformatFactory);
//        MockMimeLookup.setInstances(MimePath.parse("text/html"), HTMLTokenId.language());
        // Can't do this without LanguageRegistry finding Ruby
        //rubyReformatFactory = new GsfIndentTaskFactory();
        //IndentTestMimeDataProvider.addInstances(RubyInstallation.RUBY_MIME_TYPE, rubyReformatFactory);
    }
    
    @Override
    protected GsfTestCompilationInfo getInfo(FileObject fo, BaseDocument doc, String source) throws Exception {
        return new TestCompilationInfo(this, fo, doc, source);
    }
    
    @Override
    protected void initializeRegistry() {
        super.initializeRegistry();
        LanguageRegistry registry = LanguageRegistry.getInstance();
        if (!LanguageRegistry.getInstance().isSupported(JsTokenId.JAVASCRIPT_MIME_TYPE)) {
            List<Action> actions = Collections.emptyList();
            org.netbeans.modules.gsf.Language dl = new Language("org/netbeans/modules/javascript/editing/javascript.png", JsTokenId.JAVASCRIPT_MIME_TYPE, actions, new JsLanguage(), new JsParser(), new JsCodeCompletion(), null, new JsDeclarationFinder(), new JsFormatter(), new JsBracketCompleter(), new JsIndexer(), new JsAnalyzer(), null, false);
            List<org.netbeans.modules.gsf.Language> languages = new ArrayList<org.netbeans.modules.gsf.Language>();
            languages.add(dl);
            registry.addLanguages(languages);
        }
    }

    @Override
    protected GsfLanguage getPreferredLanguage() {
        return new JsLanguage();
    }
    
    @Override
    protected String getPreferredMimeType() {
        return JsTokenId.JAVASCRIPT_MIME_TYPE;
    }
    
    @Override
    public Formatter getFormatter(IndentPrefs preferences) {
        if (preferences == null) {
            preferences = new IndentPrefs(4,4);
        }

        Preferences prefs = NbPreferences.forModule(JsFormatterTest.class);
        prefs.put(FmtOptions.indentSize, Integer.toString(preferences.getIndentation()));
        prefs.put(FmtOptions.continuationIndentSize, Integer.toString(preferences.getHangingIndentation()));
        CodeStyle codeStyle = CodeStyle.getTestStyle(prefs);
        
        JsFormatter formatter = new JsFormatter(codeStyle, 80);
        
        return formatter;
    }
    
//    @Override
//    public TestCompilationInfo getInfo(String file) throws Exception {
//        FileObject fileObject = getTestFile(file);
//
//        return getInfo(fileObject);
//    }
//
//    public TestCompilationInfo getInfo(FileObject fileObject) throws Exception {
//        String text = readFile(fileObject);
//        if (text == null) {
//            text = "";
//        }
//        BaseDocument doc = getDocument(text, JsTokenId.JAVASCRIPT_MIME_TYPE, new JsLanguage().getLexerLanguage());
//
//        TestCompilationInfo info = new TestCompilationInfo(this, fileObject, doc, text);
//
//        return info;
//    }
    
    public static BaseDocument createDocument(String s) {
        BaseDocument doc = GsfTestBase.createDocument(s);
        doc.putProperty(org.netbeans.api.lexer.Language.class, JsTokenId.language());
        doc.putProperty("mimeType", JsTokenId.JAVASCRIPT_MIME_TYPE);

        return doc;
    }
    
    public static BaseDocument getDocumentFor(FileObject fo) {
        return createDocument(read(fo));
    }
    
    protected ParserResult parse(FileObject fileObject) {
        JsParser parser = new JsParser();
        int caretOffset = -1;

        ParserFile file = new DefaultParserFile(fileObject, null, false);
        String sequence = "";
        ParseListener listener = new DefaultParseListener();
//        BaseDocument baseDoc = null;
        try {
//            DataObject dobj = DataObject.find(fileObject);
//            EditorCookie cookie = dobj.getCookie(EditorCookie.class);
//            Document doc = cookie.openDocument();
//            sequence = doc.getText(0, doc.getLength());
            sequence = readFile(fileObject);
//            baseDoc = getDocument(sequence);
        }
        catch (Exception ex){
            fail(ex.toString());
        }
TranslatedSource translatedSource = null; // TODO            
        JsParser.Context context = new JsParser.Context(file, listener, sequence, caretOffset, translatedSource);
        ParserResult result = parser.parseBuffer(context, JsParser.Sanitize.NEVER);
        return result;
    }
    
    protected Node getRootNode(String relFilePath) {
        FileObject fileObject = getTestFile(relFilePath);
        ParserResult result = parse(fileObject);
        assertNotNull(result);
        JsParseResult rpr = (JsParseResult)result;
        Node root = rpr.getRootNode();

        return root;
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

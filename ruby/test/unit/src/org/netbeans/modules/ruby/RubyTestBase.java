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

package org.netbeans.modules.ruby;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.Action;
import org.jruby.ast.Node;
import org.netbeans.modules.gsf.api.ParseListener;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.TranslatedSource;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.api.ruby.platform.RubyPlatformManager;
import org.netbeans.api.ruby.platform.TestUtil;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.GsfTestCompilationInfo;
import org.netbeans.modules.gsf.Language;
import org.netbeans.modules.gsf.LanguageRegistry;
import org.netbeans.modules.gsf.spi.DefaultLanguageConfig;
import org.netbeans.modules.ruby.options.CodeStyle;
import org.netbeans.modules.ruby.options.FmtOptions;
import org.netbeans.modules.gsf.spi.DefaultParseListener;
import org.netbeans.modules.gsf.spi.DefaultParserFile;
import org.openide.filesystems.FileObject;
import org.openide.util.NbPreferences;

/**
 * @author Tor Norbye
 */
public abstract class RubyTestBase extends org.netbeans.api.ruby.platform.RubyTestBase {

    public RubyTestBase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        RubyIndex.setClusterUrl("file:/bogus"); // No translation
        TestSourceModelFactory.currentTest = this;
    }
    
    @Override
    protected void initializeClassPaths() {
        System.setProperty("netbeans.user", getWorkDirPath());
        FileObject jrubyHome = TestUtil.getXTestJRubyHomeFO();
        assertNotNull(jrubyHome);
        FileObject preindexed = jrubyHome.getParent().getFileObject("preindexed");
        RubyIndexer.setPreindexedDb(preindexed);
        initializeRegistry();
        // Force classpath initialization
        RubyPlatform platform = RubyPlatformManager.getDefaultPlatform();
        platform.getGemManager().getNonGemLoadPath();
        Language language = LanguageRegistry.getInstance().getLanguageByMimeType(RubyMimeResolver.RUBY_MIME_TYPE);
        org.netbeans.modules.gsfret.source.usages.ClassIndexManager.get(language).getBootIndices();
    }

    protected ParserResult parse(FileObject fileObject) {
        RubyParser parser = new RubyParser();
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
        RubyParser.Context context = new RubyParser.Context(file, listener, sequence, caretOffset, translatedSource);
        ParserResult result = parser.parseBuffer(context, RubyParser.Sanitize.NEVER);
        return result;
    }

    @Override
    protected void initializeRegistry() {
        LanguageRegistry registry = LanguageRegistry.getInstance();
        if (!LanguageRegistry.getInstance().isSupported(RubyInstallation.RUBY_MIME_TYPE)) {
            List<Action> actions = Collections.emptyList();
            org.netbeans.modules.gsf.Language dl = new Language("org/netbeans/modules/ruby/jrubydoc.png", "text/x-ruby", actions, new RubyLanguage(), new RubyParser(), new RubyCodeCompleter(), new RubyRenameHandler(), new RubyDeclarationFinder(), new RubyFormatter(), new RubyKeystrokeHandler(), new RubyIndexer(), new RubyStructureAnalyzer(), null, false);
            List<org.netbeans.modules.gsf.Language> languages = new ArrayList<org.netbeans.modules.gsf.Language>();
            languages.add(dl);
            registry.addLanguages(languages);
        }
    }
    
    protected Node getRootNode(String relFilePath) {
        FileObject fileObject = getTestFile(relFilePath);
        ParserResult result = parse(fileObject);
        assertNotNull(result);
        RubyParseResult rpr = (RubyParseResult)result;
        Node root = rpr.getRootNode();

        return root;
    }

    // Locate as many Ruby files from the JRuby distribution as possible: libs, gems, etc.
    protected List<FileObject> findJRubyRubyFiles() {
        List<FileObject> l = new ArrayList<FileObject>();
        addRubyFiles(l, TestUtil.getXTestJRubyHomeFO());

        return l;
    }

    private void addRubyFiles(List<FileObject> list, FileObject parent) {
        for (FileObject child : parent.getChildren()) {
            if (child.isFolder()) {
                addRubyFiles(list, child);
            } else if (child.getMIMEType().equals(RubyMimeResolver.RUBY_MIME_TYPE)) {
                list.add(child);
            }
        }
    }
    
    public static BaseDocument getDocumentFor(FileObject fo) {
        return createDocument(read(fo));
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new RubyLanguage();
    }

    @Override
    protected String getPreferredMimeType() {
        return RubyInstallation.RUBY_MIME_TYPE;
    }
    
    @Override
    protected GsfTestCompilationInfo getInfo(FileObject fo, BaseDocument doc, String source) throws Exception {
        return new TestCompilationInfo(this, fo, doc, source);
    }
    
    @Override
    protected RubyFormatter getFormatter(IndentPrefs preferences) {
        if (preferences == null) {
            preferences = new IndentPrefs(2,2);
        }

        Preferences prefs = NbPreferences.forModule(RubyFormatterTest.class);
        prefs.put(FmtOptions.indentSize, Integer.toString(preferences.getIndentation()));
        prefs.put(FmtOptions.continuationIndentSize, Integer.toString(preferences.getHangingIndentation()));
        CodeStyle codeStyle = CodeStyle.getTestStyle(prefs);
        
        RubyFormatter formatter = new RubyFormatter(codeStyle, 80);
        
        return formatter;
    }
}

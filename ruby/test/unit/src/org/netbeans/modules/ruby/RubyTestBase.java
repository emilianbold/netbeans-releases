/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.prefs.Preferences;
import javax.swing.text.Document;
import org.jrubyparser.ast.Node;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.api.ruby.platform.TestUtil;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.ruby.options.CodeStyle;
import org.netbeans.modules.ruby.options.FmtOptions;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 * @author Tor Norbye
 */
public abstract class RubyTestBase extends org.netbeans.api.ruby.platform.RubyTestBase {

    static {
        //RubyIndex.setClusterUrl("file:/bogus"); // No translation
        try {
            RubyIndex.setClusterUrl(TestUtil.getXTestJRubyHome().getParentFile().toURI().toURL().toExternalForm());
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public RubyTestBase(String testName) {
        super(testName);
    }

    @Override
    protected boolean runInEQ() {
        // Must run in AWT thread (BaseKit.install() checks for that)
        return true;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // XXX Parsing API
//        TestSourceModelFactory.currentTest = this;
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
    protected void setupDocumentIndentation(Document doc, IndentPrefs preferences) {
        // Ensure that I pick up the code style settings
        super.setupDocumentIndentation(doc, preferences);
        int size = preferences != null ? preferences.getIndentation() : 2;
        CodeStylePreferences.get(doc).getPreferences().putInt(SimpleValueNames.INDENT_SHIFT_WIDTH, size);
    }

    protected final Map<String, ClassPath> rubyTestsClassPath() {
        // XXX - Parsing API - preindexing
//        System.setProperty("netbeans.user", getWorkDirPath());
//        FileObject jrubyHome = TestUtil.getXTestJRubyHomeFO();
//        assertNotNull(jrubyHome);
//        FileObject preindexed = jrubyHome.getParent().getFileObject("preindexed");
//        RubyIndexer.setPreindexedDb(preindexed);
//
//        initializeRegistry();
//        // Force classpath initialization
//        RubyPlatform platform = RubyPlatformManager.getDefaultPlatform();
//        platform.getGemManager().getNonGemLoadPath();

        Map<String, ClassPath> loadPath = new HashMap<String, ClassPath>();

        // rubystubs
        loadPath.put(RubyLanguage.BOOT, ClassPathSupport.createClassPath(RubyPlatform.getRubyStubs()));

        // golden files
        FileObject testFileFO = FileUtil.toFileObject(getDataFile("/testfiles"));
        loadPath.put(RubyLanguage.SOURCE, ClassPathSupport.createClassPath(testFileFO));
        return loadPath;
    }

    protected Node getRootNode(String relFilePath) {
        return AstUtilities.getRoot(getTestFile(relFilePath));
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
            } else if (child.getMIMEType().equals(RubyInstallation.RUBY_MIME_TYPE)) {
                list.add(child);
            }
        }
    }
    
    // Called via reflection from GsfUtilities. This is necessary because
    // during tests, going from a FileObject to a BaseDocument only works
    // if all the correct data loaders are installed and working - and that
    // hasn't been the case; we end up with PlainDocuments instead of BaseDocuments.
    // If anyone can figure this out, please let me know and simplify the
    // test infrastructure.
    // XXX Parsing API
//    public static BaseDocument getDocumentFor(FileObject fo) {
//        return createDocument(read(fo));
//    }

//    @Override
//    protected GsfTestCompilationInfo getInfo(FileObject fo, BaseDocument doc, String source) throws Exception {
//        return new TestCompilationInfo(this, fo, doc, source);
//    }
    
    @Override
    protected RubyFormatter getFormatter(IndentPrefs preferences) {
        if (preferences == null) {
            preferences = new IndentPrefs(2, 2);
        }

        Preferences prefs = NbPreferences.forModule(RubyFormatterTest.class);
        prefs.put(FmtOptions.indentSize, Integer.toString(preferences.getIndentation()));
        prefs.put(FmtOptions.continuationIndentSize, Integer.toString(preferences.getHangingIndentation()));
        CodeStyle codeStyle = CodeStyle.get(prefs);

        RubyFormatter formatter = new RubyFormatter(codeStyle, 80);

        return formatter;
    }

    protected ParserResult getParserResult(FileObject file) {
        Source source = Source.create(file);
        final ParserResult[] resultHolder = new ParserResult[1];
        try {
            ParserManager.parse(Collections.singleton(source), new UserTask() {

                @Override
                public void run(ResultIterator ri) throws Exception {
                    resultHolder[0] = (ParserResult) ri.getParserResult();
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return resultHolder[0];
    }

    protected ParserResult getParserResult(String string) {
        return getParserResult(getTestFile(string));
    }

    protected String getText(Parser.Result parserResult) {
        return parserResult.getSnapshot().getText().toString();
    }

    protected void failsDueToIssue182494(Callable<Void> test) throws Exception {
        try {
            test.call();
            System.out.println("Expected " + getName() + " to fail, but it passes.");
        } catch (AssertionError ae) {
            String msg = "Skipping " + getName() + " - fails due to #182494";
            System.out.println(msg);
            log(msg);
        }
    }
}

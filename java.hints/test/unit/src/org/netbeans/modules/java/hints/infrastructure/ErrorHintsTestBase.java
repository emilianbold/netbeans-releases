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
package org.netbeans.modules.java.hints.infrastructure;

import com.sun.source.util.TreePath;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.source.TestUtil;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer;
import org.netbeans.modules.java.source.parsing.JavacParserFactory;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageProvider;
import org.openide.LifecycleManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * @author Jan Lahoda
 */
public abstract class ErrorHintsTestBase extends NbTestCase {

    private File cache;
    private FileObject cacheFO;
    
    public ErrorHintsTestBase(String name) {
        super(name);
        
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String[] additionalLayers = getAdditionalLayers();
        String[] layers = new String[additionalLayers.length + 1];
        
        System.arraycopy(additionalLayers, 0, layers, 1, additionalLayers.length);
        layers[0] = "org/netbeans/modules/java/editor/resources/layer.xml";
        
        SourceUtilsTestUtil.prepareTest(layers, new Object[]{
                    JavaDataLoader.class,
                    new MimeDataProvider() {

                        public Lookup getLookup(MimePath mimePath) {
                            return Lookups.fixed(new Object[]{
                                        new JavaKit(), new JavacParserFactory(), new JavaCustomIndexer.Factory()});
                        }
                    },
                    new LanguageProvider() {

                        public Language<?> findLanguage(String mimePath) {
                            return JavaTokenId.language();
                        }

                        public LanguageEmbedding<?> findLanguageEmbedding(Token<?> token,
                                LanguagePath languagePath,
                                InputAttributes inputAttributes) {
                            return null;
                        }
                    }});
        
        if (cache == null) {
            cache = FileUtil.normalizeFile(TestUtil.createWorkFolder());
            cacheFO = FileUtil.toFileObject(cache);

            cache.deleteOnExit();

            IndexUtil.setCacheFolder(cache);

            TestUtilities.analyzeBinaries(SourceUtilsTestUtil.getBootClassPath());
        }
    }
    
    private void prepareTest(String fileName, String code) throws Exception {
        clearWorkDir();
        
        FileObject workFO = FileUtil.toFileObject(getWorkDir());
        
        assertNotNull(workFO);
        
        sourceRoot = workFO.createFolder("src");
        
        FileObject buildRoot  = workFO.createFolder("build");
        
        FileObject data = FileUtil.createData(sourceRoot, fileName);
        File dataFile = FileUtil.toFile(data);
        
        assertNotNull(dataFile);
        
        TestUtilities.copyStringToFile(dataFile, code);

        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cacheFO, getExtraClassPathElements());
        
        DataObject od = DataObject.find(data);
        EditorCookie ec = od.getCookie(EditorCookie.class);

        assertNotNull(ec);

        doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        doc.putProperty("mimeType", "text/x-java");

        //XXX: takes a long time
        //re-index, in order to find classes-living-elsewhere
        IndexingManager.getDefault().refreshIndexAndWait(sourceRoot.getURL(), null);

        JavaSource js = JavaSource.forFileObject(data);
        
        assertNotNull(js);
        
        info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        
        assertNotNull(info);
    }
    
    private FileObject sourceRoot;
    private CompilationInfo info;
    private Document doc;
    
    protected abstract List<Fix> computeFixes(CompilationInfo info, int pos, TreePath path) throws Exception;
    
    protected String toDebugString(CompilationInfo info, Fix f) {
        return f.toString();
    }
    
    protected String[] getAdditionalLayers() {
        return new String[0];
    }
            
    protected FileObject[] getExtraClassPathElements() {
        return new FileObject[0];
    }

    protected void performAnalysisTest(String fileName, String code, String... golden) throws Exception {
        int[] caretPosition = new int[1];
        
        code = org.netbeans.modules.java.hints.TestUtilities.detectOffsets(code, caretPosition);
        
        performAnalysisTest(fileName, code, caretPosition[0], golden);
    }
    
    protected void performAnalysisTest(String fileName, String code, int pos, String... golden) throws Exception {
        prepareTest(fileName, code);
        
        TreePath path = info.getTreeUtilities().pathFor(pos);
        
        List<Fix> fixes = computeFixes(info, pos, path);
        List<String> fixesNames = new LinkedList<String>();
        
        fixes = fixes != null ? fixes : Collections.<Fix>emptyList();
        
        for (Fix e : fixes) {
            fixesNames.add(toDebugString(info, e));
        }
        
        assertTrue(fixesNames.toString(), Arrays.equals(golden, fixesNames.toArray(new String[0])));
    }
    
    protected void performFixTest(String fileName, String code, String fixCode, String golden) throws Exception {
        int[] caretPosition = new int[1];
        
        code = org.netbeans.modules.java.hints.TestUtilities.detectOffsets(code, caretPosition);
        
        performFixTest(fileName, code, caretPosition[0], fixCode, golden);
    }
    
    protected void performFixTest(String fileName, String code, int pos, String fixCode, String golden) throws Exception {
        performFixTest(fileName, code, pos, fixCode, fileName, golden);
    }
    
    protected void performFixTest(String fileName, String code, int pos, String fixCode, String goldenFileName, String golden) throws Exception {
        prepareTest(fileName, code);
        
        TreePath path = info.getTreeUtilities().pathFor(pos);

        List<Fix> fixes = computeFixes(info, pos, path);
        List<String> fixesNames = new LinkedList<String>();
        
        fixes = fixes != null ? fixes : Collections.<Fix>emptyList();
        
        Fix fix = null;
        
        for (Fix e : fixes) {
            String debugString = toDebugString(info, e);
            
            fixesNames.add(debugString);
            
            if (fixCode.equals(debugString))
                fix = e;
        }
        
        assertNotNull(fixesNames.toString(), fix);
        
        fix.implement();
        
        FileObject toCheck = sourceRoot.getFileObject(goldenFileName);
        
        assertNotNull(toCheck);
        
        DataObject toCheckDO = DataObject.find(toCheck);
        EditorCookie ec = toCheckDO.getLookup().lookup(EditorCookie.class);
        Document toCheckDocument = ec.openDocument();
        
        String realCode = toCheckDocument.getText(0, toCheckDocument.getLength());
        
        //ignore whitespaces:
        realCode = realCode.replaceAll("[ \t\n]+", " ");
        
        assertEquals(golden, realCode);
        
        LifecycleManager.getDefault().saveAll();
    }
    
}

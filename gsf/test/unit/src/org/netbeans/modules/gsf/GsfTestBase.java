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

package org.netbeans.modules.gsf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.editor.Utilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.LanguageManager;
import org.netbeans.modules.gsf.api.CodeCompletionContext;
import org.netbeans.modules.gsf.api.Error;
import org.netbeans.modules.gsf.api.HintFix;
import org.netbeans.modules.gsf.api.KeystrokeHandler;
import org.netbeans.modules.gsf.api.ColoringAttributes;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.CodeCompletionHandler;
import org.netbeans.modules.gsf.api.CodeCompletionHandler.QueryType;
import org.netbeans.modules.gsf.api.CodeCompletionResult;
import org.netbeans.modules.gsf.api.CompletionProposal;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.Formatter;
import org.netbeans.modules.gsf.api.GsfLanguage;
import org.netbeans.modules.gsf.api.Hint;
import org.netbeans.modules.gsf.api.HintsProvider;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.gsf.api.IndexDocument;
import org.netbeans.modules.gsf.api.IndexDocumentFactory;
import org.netbeans.modules.gsf.api.Indexer;
import org.netbeans.modules.gsf.api.InstantRenamer;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.gsf.api.OccurrencesFinder;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.SemanticAnalyzer;
import org.netbeans.modules.gsf.api.StructureItem;
import org.netbeans.modules.gsf.api.StructureScanner;
import org.netbeans.modules.gsf.spi.DefaultParserFile;
import org.netbeans.modules.gsf.api.HintSeverity;
import org.netbeans.modules.gsf.api.Rule;
import org.netbeans.modules.gsf.api.Rule.AstRule;
import org.netbeans.modules.gsf.api.Rule.ErrorRule;
import org.netbeans.modules.gsf.api.Rule.SelectionRule;
import org.netbeans.modules.gsf.api.Rule.UserConfigurableRule;
import org.netbeans.modules.gsf.api.RuleContext;
import org.netbeans.modules.gsfret.hints.infrastructure.GsfHintsManager;
import org.netbeans.modules.gsfret.hints.infrastructure.HintsSettings;
import org.netbeans.napi.gsfret.source.Source;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

/**
 * @author Tor Norbye
 */
public abstract class GsfTestBase extends NbTestCase {

    public GsfTestBase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        System.setProperty("netbeans.user", getWorkDirPath());
    }

    protected void initializeRegistry() {
    }
    
    public static File getXTestJsCluster() {
        String destDir = System.getProperty("xtest.js.home");
        if (destDir == null) {
            throw new RuntimeException("xtest.js.home property has to be set when running within binary distribution");
        }
        return new File(destDir);
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    protected FileObject touch(final String dir, final String path) throws IOException {
        return touch(new File(dir), path);
    }

    protected FileObject touch(final File dir, final String binary) throws IOException {
        if (!dir.isDirectory()) {
            assertTrue("success to create " + dir, dir.mkdirs());
        }
        FileObject dirFO = FileUtil.toFileObject(FileUtil.normalizeFile(dir));
        return FileUtil.createData(dirFO, binary);
    }

    /** Copy-pasted from APISupport. */
    protected static String slurp(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileUtil.copy(is, baos);
            return baos.toString("UTF-8");
        } finally {
            is.close();
        }
    }
    
    protected FileObject getTestFile(String relFilePath) {
        File wholeInputFile = new File(getDataDir(), relFilePath);
        if (!wholeInputFile.exists()) {
            NbTestCase.fail("File " + wholeInputFile + " not found.");
        }
        FileObject fo = FileUtil.toFileObject(wholeInputFile);
        assertNotNull(fo);

        return fo;
    }

    protected String readFile(final FileObject fo) {
        return read(fo);
    }
    
    public static String read(final FileObject fo) {
        try {
            final StringBuilder sb = new StringBuilder(5000);
            fo.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {

                public void run() throws IOException {

                    if (fo == null) {
                        return;
                    }

                    InputStream is = fo.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                    while (true) {
                        String line = reader.readLine();

                        if (line == null) {
                            break;
                        }

                        sb.append(line);
                        sb.append('\n');
                    }
                }
            });

            if (sb.length() > 0) {
                return sb.toString();
            } else {
                return null;
            }
        }
        catch (IOException ioe){
            ErrorManager.getDefault().notify(ioe);

            return null;
        }
    }

    public BaseDocument getDocument(String s, String mimeType, Language language) {
        try {
            BaseDocument doc = new BaseDocument(null, false);

            doc.putProperty("mimeType", mimeType);
            doc.putProperty(org.netbeans.api.lexer.Language.class, language);

            doc.insertString(0, s, null);

            return doc;
        }
        catch (Exception ex){
            fail(ex.toString());
            return null;
        }
    }
    
    public BaseDocument getDocument(String s, String mimeType) {
        Language<?> language = LanguageManager.getInstance().findLanguage(mimeType);
        assertNotNull(language);
        
        return getDocument(s, mimeType, language);
    }
    
    public static BaseDocument createDocument(String s) {
        try {
            BaseDocument doc = new BaseDocument(null, false);
            doc.insertString(0, s, null);

            return doc;
        }
        catch (Exception ex){
            fail(ex.toString());
            return null;
        }
    }

    protected BaseDocument getDocument(String s) {
        String mimeType = getPreferredMimeType();
        assertNotNull("You must implement " + getClass().getName() + ".getPreferredMimeType()", mimeType);

        GsfLanguage language = getPreferredLanguage();
        assertNotNull("You must implement " + getClass().getName() + ".getPreferredLanguage()", language);
        
        return getDocument(s, mimeType, language.getLexerLanguage());
    }
    
    protected BaseDocument getDocument(FileObject fo) {
        try {
//             DataObject dobj = DataObject.find(fo);
//             assertNotNull(dobj);
//
//             EditorCookie ec = (EditorCookie)dobj.getCookie(EditorCookie.class);
//             assertNotNull(ec);
//
//             return (BaseDocument)ec.openDocument();
            BaseDocument doc = getDocument(readFile(fo));
            try {
                DataObject dobj = DataObject.find(fo);
                doc.putProperty(Document.StreamDescriptionProperty, dobj);
            } catch (DataObjectNotFoundException dnfe) {
                fail(dnfe.toString());
            }

            return doc;
        }
        catch (Exception ex){
            fail(ex.toString());
            return null;
        }
    }

    public static String readFile(File f) throws Exception {
        FileReader r = new FileReader(f);
        int fileLen = (int)f.length();
        CharBuffer cb = CharBuffer.allocate(fileLen);
        r.read(cb);
        cb.rewind();
        return cb.toString();
    }

    protected File getDataSourceDir() {
        // Check whether token dump file exists
        // Try to remove "/build/" from the dump file name if it exists.
        // Otherwise give a warning.
        File inputFile = getDataDir();
        String inputFilePath = inputFile.getAbsolutePath();
        boolean replaced = false;
        if (inputFilePath.indexOf(pathJoin("build", "test")) != -1) {
            inputFilePath = inputFilePath.replace(pathJoin("build", "test"), pathJoin("test"));
            replaced = true;
        }
        if (!replaced && inputFilePath.indexOf(pathJoin("test", "work", "sys")) != -1) {
            inputFilePath = inputFilePath.replace(pathJoin("test", "work", "sys"), pathJoin("test", "unit"));
            replaced = true;
        }
        if (!replaced) {
            System.err.println("Warning: Attempt to use dump file " +
                    "from sources instead of the generated test files failed.\n" +
                    "Patterns '/build/test/' or '/test/work/sys/' not found in " + inputFilePath
            );
        }
        inputFile = new File(inputFilePath);
        assertTrue(inputFile.exists());
        
        return inputFile;
    }
    
    private static String pathJoin(String... chunks) {
        StringBuilder result = new StringBuilder(File.separator);
        for (String chunk : chunks) {
            result.append(chunk).append(File.separatorChar);            
        }
        return result.toString();
    }
    
    protected File getDataFile(String relFilePath) {
        File inputFile = new File(getDataSourceDir(), relFilePath);
        return inputFile;
    }
    
    protected boolean failOnMissingGoldenFile() {
        return true;
    }

    protected void assertDescriptionMatches(String relFilePath,
            String description, boolean includeTestName, String ext) throws Exception {
        File rubyFile = getDataFile(relFilePath);
        if (!rubyFile.exists()) {
            NbTestCase.fail("File " + rubyFile + " not found.");
        }

        File goldenFile = getDataFile(relFilePath + (includeTestName ? ("." + getName()) : "") + ext);
        if (!goldenFile.exists()) {
            if (!goldenFile.createNewFile()) {
                NbTestCase.fail("Cannot create file " + goldenFile);
            }
            FileWriter fw = new FileWriter(goldenFile);
            try {
                fw.write(description);
            }
            finally{
                fw.close();
            }
            if (failOnMissingGoldenFile()) {
                NbTestCase.fail("Created generated golden file " + goldenFile + "\nPlease re-run the test.");
            }
            return;
        }

        String expected = readFile(goldenFile);

        // Because the unit test differ is so bad...
        if (false) { // disabled
            if (!expected.equals(description)) {
                BufferedWriter fw = new BufferedWriter(new FileWriter("/tmp/expected.txt"));
                fw.write(expected);
                fw.close();
                fw = new BufferedWriter(new FileWriter("/tmp/actual.txt"));
                fw.write(description);
                fw.close();
            }
        }

        assertEquals(expected.trim(), description.trim());
    }

    protected void assertDescriptionMatches(FileObject fileObject, 
            String description, boolean includeTestName, String ext) throws Exception {
        File goldenFile = getDataFile("testfiles/" + fileObject.getName() + (includeTestName ? ("." + getName()) : "") + ext);
        if (!goldenFile.exists()) {
            if (!goldenFile.createNewFile()) {
                NbTestCase.fail("Cannot create file " + goldenFile);
            }
            FileWriter fw = new FileWriter(goldenFile);
            try {
                fw.write(description);
            }
            finally{
                fw.close();
            }
            NbTestCase.fail("Created generated golden file " + goldenFile + "\nPlease re-run the test.");
        }

        String expected = readFile(goldenFile);

        // Because the unit test differ is so bad...
        if (false) { // disabled
            if (!expected.equals(description)) {
                BufferedWriter fw = new BufferedWriter(new FileWriter("/tmp/expected.txt"));
                fw.write(expected);
                fw.close();
                fw = new BufferedWriter(new FileWriter("/tmp/actual.txt"));
                fw.write(description);
                fw.close();
            }
        }

        assertEquals("Not matching goldenfile: " + FileUtil.getFileDisplayName(fileObject), expected.trim(), description.trim());
    }
    
    protected void assertFileContentsMatches(String relFilePath, String description, boolean includeTestName, String ext) throws Exception {
        File rubyFile = getDataFile(relFilePath);
        if (!rubyFile.exists()) {
            NbTestCase.fail("File " + rubyFile + " not found.");
        }

        File goldenFile = getDataFile(relFilePath + (includeTestName ? ("." + getName()) : "") + ext);
        if (!goldenFile.exists()) {
            if (!goldenFile.createNewFile()) {
                NbTestCase.fail("Cannot create file " + goldenFile);
            }
            FileWriter fw = new FileWriter(goldenFile);
            try {
                fw.write(description);
            }
            finally{
                fw.close();
            }
            NbTestCase.fail("Created generated golden file " + goldenFile + "\nPlease re-run the test.");
        }

        String expected = readFile(goldenFile);
        assertEquals(expected.trim(), description.trim());
    }

    public void assertEquals(Collection<String> s1, Collection<String> s2) {
        List<String> l1 = new ArrayList<String>();
        l1.addAll(s1);
        Collections.sort(l1);
        List<String> l2 = new ArrayList<String>();
        l2.addAll(s2);
        Collections.sort(l2);
        
        assertEquals(l1.toString(), l2.toString());
    }
    
    protected void createFilesFromDesc(FileObject folder, String descFile) throws Exception {
        File taskFile = new File(getDataDir(), descFile);
        assertTrue(taskFile.exists());
        BufferedReader br = new BufferedReader(new FileReader(taskFile));
        while (true) {
            String line = br.readLine();
            if (line == null || line.trim().length() == 0) {
                break;
            }
            
            if (line.endsWith("\r")) {
                line = line.substring(0, line.length()-1);
            }

            String path = line;
            if (path.endsWith("/")) {
                path = path.substring(0, path.length()-1);
                FileObject f = FileUtil.createFolder(folder, path);
                assertNotNull(f);
            } else {
                FileObject f = FileUtil.createData(folder, path);
                assertNotNull(f);
            }
        }
    }

   public static void createFiles(File baseDir, String... paths) throws IOException {
        assertNotNull(baseDir);
        for (String path : paths) {
            FileObject baseDirFO = FileUtil.toFileObject(baseDir);
            assertNotNull(baseDirFO);
            assertNotNull(FileUtil.createData(baseDirFO, path));
        }
    }

    public static void createFile(FileObject dir, String relative, String contents) throws IOException {
        FileObject datafile = FileUtil.createData(dir, relative);
        OutputStream os = datafile.getOutputStream();
        Writer writer = new BufferedWriter(new OutputStreamWriter(os));
        writer.write(contents);
        writer.close();
    }

    
    ////////////////////////////////////////////////////////////////////////////
    // Parsing Info Based Tests
    ////////////////////////////////////////////////////////////////////////////
    protected GsfTestCompilationInfo getInfo(FileObject fo, BaseDocument doc, String source) throws Exception {
        return null;
    }

    protected GsfLanguage getPreferredLanguage() {
        return null;
    }
    
    protected String getPreferredMimeType() {
        return null;
    }
    
    public GsfTestCompilationInfo getInfo(String file) throws Exception {
        FileObject fileObject = getTestFile(file);

        return getInfo(fileObject);
    }
    
    public GsfTestCompilationInfo getInfo(FileObject fileObject) throws Exception {
        String text = readFile(fileObject);
        if (text == null) {
            text = "";
        }

        String mimeType = getPreferredMimeType();
        assertNotNull("You must implement " + getClass().getName() + ".getPreferredMimeType()", mimeType);

        GsfLanguage language = getPreferredLanguage();
        assertNotNull("You must implement " + getClass().getName() + ".getPreferredLanguage()", language);
        
        BaseDocument doc = getDocument(text, mimeType, language.getLexerLanguage());

        return getInfo(fileObject, doc, text);
    }
    
    public GsfTestCompilationInfo getInfoForText(String text, String tempName) throws Exception {
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        FileObject testFO = workDir.getFileObject(tempName);
        if (testFO != null) {
            testFO.delete();
        }
        testFO = workDir.createData(tempName);
        FileObject fileObject = copyStringToFileObject(testFO, text);
        return getInfo(fileObject);
    }

    private static final FileObject copyStringToFileObject(FileObject fo, String content) throws IOException {
        OutputStream os = fo.getOutputStream();
        try {
            InputStream is = new ByteArrayInputStream(content.getBytes("UTF-8"));
            FileUtil.copy(is, os);
            return fo;
        } finally {
            os.close();
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Bracket completion tests
    ////////////////////////////////////////////////////////////////////////////
    protected KeystrokeHandler getBracketCompletion() {
        return null;
    }

    // Also requires getFormatter(IndentPref) defined below under the formatting tests
    
    protected void assertMatches(String original) throws BadLocationException {
        KeystrokeHandler bc = getBracketCompletion();
        assertNotNull("getBracketCompletion() must be implemented!", bc);
        int caretPos = original.indexOf('^');
        
        original = original.substring(0, caretPos) + original.substring(caretPos+1);
        int matchingCaretPos = original.indexOf('^');
        assert caretPos < matchingCaretPos;
        original = original.substring(0, matchingCaretPos) + original.substring(matchingCaretPos+1);

        BaseDocument doc = getDocument(original);

        OffsetRange range = bc.findMatching(doc, caretPos);
        
        assertNotSame("Didn't find matching token for " + /*LexUtilities.getToken(doc, caretPos).text().toString()*/ " position " + caretPos, 
                OffsetRange.NONE, range);
        assertEquals("forward match not found; found '" +
                doc.getText(range.getStart(), range.getLength()) + "' instead of " +
                /*LexUtilities.getToken(doc, matchingCaretPos).text().toString()*/ " position " + matchingCaretPos, 
                matchingCaretPos, range.getStart());
        
        // Perform reverse match
        range = bc.findMatching(doc, matchingCaretPos);
        
        assertNotSame(OffsetRange.NONE, range);
        assertEquals("reverse match not found; found '" +
                doc.getText(range.getStart(), range.getLength()) + "' instead of " + 
                /*LexUtilities.getToken(doc, caretPos).text().toString()*/ " position " + caretPos, 
                caretPos, range.getStart());
    }
    
    protected void insertBreak(String original, String expected) throws BadLocationException {
        KeystrokeHandler bc = getBracketCompletion();
        assertNotNull("getBracketCompletion() must be implemented!", bc);
        
        int insertOffset = original.indexOf('^');
        int finalCaretPos = expected.indexOf('^');
        original = original.substring(0, insertOffset) + original.substring(insertOffset+1);
        expected = expected.substring(0, finalCaretPos) + expected.substring(finalCaretPos+1);

        BaseDocument doc = getDocument(original);

        JTextArea ta = new JTextArea(doc);
        Caret caret = ta.getCaret();
        caret.setDot(insertOffset);
        int newOffset = bc.beforeBreak(doc, insertOffset, ta);
        doc.atomicLock();
        DocumentUtilities.setTypingModification(doc, true);

        try {
            doc.insertString(caret.getDot(), "\n", null);
            // Indent the new line
            //ParserResult result = parse(fo);

            int startPos = caret.getDot()+1;
            int endPos = startPos+1;

            IndentPrefs preferences = null; // Test should supply default - new IndentPrefs(2, 2);
            Formatter formatter = getFormatter(preferences);
            assertNotNull("getFormatter() must be implemented", formatter);
            
            //ParserResult result = parse(fo);
            formatter.reindent(doc, startPos, endPos);
            int indent = getLineIndent(doc, insertOffset+1);

            //bc.afterBreak(doc, insertOffset, caret);
            String formatted = doc.getText(0, doc.getLength());
            assertEquals(expected, formatted);
            if (newOffset != -1) {
                caret.setDot(newOffset);
            } else {
                caret.setDot(insertOffset+1+indent);
            }
            if (finalCaretPos != -1) {
                assertEquals(finalCaretPos, caret.getDot());
            }
        } finally {
            DocumentUtilities.setTypingModification(doc, false);
            doc.atomicUnlock();
        }
    }

    // Copied from LexUtilities
    public static int getLineIndent(BaseDocument doc, int offset) {
        try {
            int start = Utilities.getRowStart(doc, offset);
            int end;

            if (Utilities.isRowWhite(doc, start)) {
                end = Utilities.getRowEnd(doc, offset);
            } else {
                end = Utilities.getRowFirstNonWhite(doc, start);
            }

            int indent = Utilities.getVisualColumn(doc, end);

            return indent;
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);

            return 0;
        }
    }

    protected void insertChar(String original, char insertText, String expected, String selection, boolean codeTemplateMode) throws BadLocationException {
        KeystrokeHandler bc = getBracketCompletion();
        assertNotNull("getBracketCompletion() must be implemented!", bc);
        
        int insertOffset = original.indexOf('^');
        int finalCaretPos = expected.indexOf('^');
        original = original.substring(0, insertOffset) + original.substring(insertOffset+1);
        expected = expected.substring(0, finalCaretPos) + expected.substring(finalCaretPos+1);

        BaseDocument doc = getDocument(original);
        
        if (codeTemplateMode) {
            // Copied from editor/codetemplates/src/org/netbeans/lib/editor/codetemplates/CodeTemplateInsertHandler.java
            String EDITING_TEMPLATE_DOC_PROPERTY = "processing-code-template"; // NOI18N        
            doc.putProperty(EDITING_TEMPLATE_DOC_PROPERTY, Boolean.TRUE);            
        }

        JTextArea ta = new JTextArea(doc);
        Caret caret = ta.getCaret();
        caret.setDot(insertOffset);
        if (selection != null) {
            int start = original.indexOf(selection);
            assertTrue(start != -1);
            assertTrue("Ambiguous selection - multiple occurrences of selection string",
                    original.indexOf(selection, start+1) == -1);
            ta.setSelectionStart(start);
            ta.setSelectionEnd(start+selection.length());
            assertEquals(selection, ta.getSelectedText());
        }

        doc.atomicLock();
        DocumentUtilities.setTypingModification(doc, true);

        boolean handled = false;
        try {
            handled = bc.beforeCharInserted(doc, insertOffset, ta, insertText);
        } finally {
            DocumentUtilities.setTypingModification(doc, false);
            doc.atomicUnlock();
        }
        if (!handled) {
            if (ta.getSelectedText() != null && ta.getSelectedText().length() > 0) {
                insertOffset = ta.getSelectionStart();
                doc.remove(ta.getSelectionStart(), ta.getSelectionEnd()-ta.getSelectionStart());
                caret.setDot(insertOffset);
            }
            doc.insertString(caret.getDot(), ""+insertText, null);
            caret.setDot(insertOffset+1);
            bc.afterCharInserted(doc, insertOffset, ta, insertText);
        }
        String formatted = doc.getText(0, doc.getLength());
        assertEquals(expected, formatted);
        if (finalCaretPos != -1) {
            assertEquals(finalCaretPos, caret.getDot());
        }
    }

    protected void deleteChar(String original, String expected) throws BadLocationException {
        KeystrokeHandler bc = getBracketCompletion();
        assertNotNull("getBracketCompletion() must be implemented!", bc);

        int afterRemoveOffset = original.indexOf('^');
        int finalCaretPos = expected.indexOf('^');
        original = original.substring(0, afterRemoveOffset) + original.substring(afterRemoveOffset+1);
        expected = expected.substring(0, finalCaretPos) + expected.substring(finalCaretPos+1);

        BaseDocument doc = getDocument(original);

        JTextArea ta = new JTextArea(doc);
        Caret caret = ta.getCaret();
        caret.setDot(afterRemoveOffset);
        int dot = afterRemoveOffset;
        char ch = doc.getChars(dot-1, 1)[0];

        doc.atomicLock();
        DocumentUtilities.setTypingModification(doc, true);

        try {
            doc.remove(dot - 1, 1);
            caret.setDot(dot-1);
            boolean handled = bc.charBackspaced(doc, dot-1, ta, ch);
            String formatted = doc.getText(0, doc.getLength());
            assertEquals(expected, formatted);
            if (finalCaretPos != -1) {
                assertEquals(finalCaretPos, caret.getDot());
            }
        } finally {
            DocumentUtilities.setTypingModification(doc, false);
            doc.atomicUnlock();
        }
    }
    
    protected void deleteWord(String original, String expected) throws BadLocationException {
        KeystrokeHandler bc = getBracketCompletion();
        assertNotNull("getBracketCompletion() must be implemented!", bc);

        int afterRemoveOffset = original.indexOf('^');
        int finalCaretPos = expected.indexOf('^');
        original = original.substring(0, afterRemoveOffset) + original.substring(afterRemoveOffset+1);
        expected = expected.substring(0, finalCaretPos) + expected.substring(finalCaretPos+1);

        BaseDocument doc = getDocument(original);

        JTextArea ta = new JTextArea(doc);
        Caret caret = ta.getCaret();
        caret.setDot(afterRemoveOffset);
        int dot = afterRemoveOffset;
        //REMOVE char ch = doc.getChars(dot-1, 1)[0];

        int begin = bc.getNextWordOffset(doc, dot, true);
        if (begin == -1) {
            begin = Utilities.getPreviousWord(ta, dot);
        }
        
        doc.atomicLock();
        DocumentUtilities.setTypingModification(doc, true);

        try {
            doc.remove(begin, dot-begin);
            caret.setDot(begin);
            String formatted = doc.getText(0, doc.getLength());
            assertEquals(expected, formatted);
            if (finalCaretPos != -1) {
                assertEquals(finalCaretPos, caret.getDot());
            }
        } finally {
            DocumentUtilities.setTypingModification(doc, false);
            doc.atomicUnlock();
        }
    }

    protected void assertLogicalRange(String source, boolean up, String expected) throws Exception {
        KeystrokeHandler completer = getBracketCompletion();
        assertNotNull("getBracketCompletion() must be implemented!", completer);

        String BEGIN = "%<%"; // NOI18N
        String END = "%>%"; // NOI18N
        int sourceStartPos = source.indexOf(BEGIN);
        if (sourceStartPos != -1) {
            source = source.substring(0, sourceStartPos) + source.substring(sourceStartPos+BEGIN.length());
        }
        
        int caretPos = source.indexOf('^');
        source = source.substring(0, caretPos) + source.substring(caretPos+1);

        int sourceEndPos = source.indexOf(END);
        if (sourceEndPos != -1) {
            source = source.substring(0, sourceEndPos) + source.substring(sourceEndPos+END.length());
        }
        
        int expectedStartPos = expected.indexOf(BEGIN);
        if (expectedStartPos != -1) {
            expected = expected.substring(0, expectedStartPos) + expected.substring(expectedStartPos+BEGIN.length());
        }

        int expectedCaretPos = expected.indexOf('^');
        expected = expected.substring(0, expectedCaretPos) + expected.substring(expectedCaretPos+1);
        
        int expectedEndPos = expected.indexOf(END);
        if (expectedEndPos != -1) {
            expected = expected.substring(0, expectedEndPos) + expected.substring(expectedEndPos+END.length());
        }

        assertEquals("Only range markers should differ", source,expected);

        OffsetRange selected = null;
        
        BaseDocument doc = getDocument(source);
        FileObject fileObject = null;
        CompilationInfo info = getInfo(fileObject, doc, source);
        assertNotNull("To run this test you must have implemented getInfo(FileObject,DocumentString)!", info);
        
        List<OffsetRange> ranges = completer.findLogicalRanges(info, caretPos);
        OffsetRange expectedRange;
        if (expectedStartPos != -1) {
            expectedRange = new OffsetRange(expectedStartPos, expectedEndPos);
        } else {
            expectedRange = new OffsetRange(expectedCaretPos, expectedCaretPos);
        }

        if (sourceStartPos != -1) {
            assert sourceEndPos != -1;
            selected = new OffsetRange(sourceStartPos, sourceEndPos);            

            for (int i = 0; i < ranges.size(); i++) {
                if (ranges.get(i).equals(selected)) {
                    if (up) {
                        assertTrue(i < ranges.size()-1);
                        OffsetRange was = ranges.get(i+1);
                        assertEquals("Wrong selection: expected \"" + 
                                expected.substring(expectedRange.getStart(),expectedRange.getEnd()) + "\" and was \"" +
                                source.substring(was.getStart(), was.getEnd()) + "\"",
                                expectedRange, was);
                        return;
                    } else {
                        if (i == 0) {
                            assertEquals(caretPos, expectedCaretPos);
                            return;
                        }
                        OffsetRange was = ranges.get(i-1);
                        assertEquals("Wrong selection: expected \"" + 
                                expected.substring(expectedRange.getStart(),expectedRange.getEnd()) + "\" and was \"" +
                                source.substring(was.getStart(), was.getEnd()) + "\"",
                                expectedRange, was);
                        return;
                    }
                }
            }
            fail("Selection range " + selected + " is not in the range; ranges=" + ranges);
        } else {
            assert ranges.size() > 0;
            OffsetRange was = ranges.get(0);
            assertEquals("Wrong selection: expected \"" + 
                    expected.substring(expectedRange.getStart(),expectedRange.getEnd()) + "\" and was \"" +
                    source.substring(was.getStart(), was.getEnd()) + "\"",
                    expectedRange, was);
            return;
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Mark Occurrences Tests
    ////////////////////////////////////////////////////////////////////////////
    protected OccurrencesFinder getOccurrencesFinder() {
        return null;
    }
    
    /** Test the occurrences to make sure they equal the golden file.
     * If the symmetric parameter is set, this test will also ensure that asking for
     * occurrences on ANY of the matches produced by the original caret position will
     * produce the exact same map. This is obviously not appropriate for things like
     * occurrences on the exit points.
     */
    protected void checkOccurrences(String relFilePath, String caretLine, boolean symmetric) throws Exception {
        CompilationInfo info = getInfo(relFilePath);

        String text = info.getText();

        int caretDelta = caretLine.indexOf('^');
        assertTrue(caretDelta != -1);
        caretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
        int lineOffset = text.indexOf(caretLine);
        assertTrue(lineOffset != -1);

        int caretOffset = lineOffset + caretDelta;

        OccurrencesFinder finder = getOccurrencesFinder();
        assertNotNull("getOccurrencesFinder must be implemented", finder);
        finder.setCaretPosition(caretOffset);
        finder.run(info);
        Map<OffsetRange, ColoringAttributes> occurrences = finder.getOccurrences();
        if (occurrences == null) {
            occurrences = Collections.emptyMap();
        }

        String annotatedSource = annotateFinderResult((BaseDocument)info.getDocument(), occurrences, caretOffset);

        assertDescriptionMatches(relFilePath, annotatedSource, true, ".occurrences");
        
        if (symmetric) {
            // Extra check: Ensure that occurrences are symmetric: Placing the caret on ANY of the occurrences
            // should produce the same set!!
            for (OffsetRange range : occurrences.keySet()) {
                finder.setCaretPosition(range.getStart()+range.getLength()/2);
                finder.run(info);
                Map<OffsetRange, ColoringAttributes> alternates = finder.getOccurrences();
                assertEquals("Marks differ between caret positions", occurrences, alternates);
            }
        }
    }

    private String annotateFinderResult(BaseDocument doc, Map<OffsetRange, ColoringAttributes> highlights, int caretOffset) throws Exception {
        Set<OffsetRange> ranges = highlights.keySet();
        StringBuilder sb = new StringBuilder();
        String text = doc.getText(0, doc.getLength());
        Map<Integer, OffsetRange> starts = new HashMap<Integer, OffsetRange>(100);
        Map<Integer, OffsetRange> ends = new HashMap<Integer, OffsetRange>(100);
        for (OffsetRange range : ranges) {
            starts.put(range.getStart(), range);
            ends.put(range.getEnd(), range);
        }

        int index = 0;
        int length = text.length();
        while (index < length) {
            int lineStart = Utilities.getRowStart(doc, index);
            int lineEnd = Utilities.getRowEnd(doc, index);
            OffsetRange lineRange = new OffsetRange(lineStart, lineEnd);
            boolean skipLine = true;
            for (OffsetRange range : ranges) {
                if (lineRange.containsInclusive(range.getStart()) || lineRange.containsInclusive(range.getEnd())) {
                    skipLine = false;
                }
            }
            if (!skipLine) {
                for (int i = lineStart; i <= lineEnd; i++) {
                    if (i == caretOffset) {
                        sb.append("^");
                    }
                    if (starts.containsKey(i)) {
                        sb.append("|>");
                        OffsetRange range = starts.get(i);
                        ColoringAttributes ca = highlights.get(range);
                        if (ca != null) {
                            sb.append(ca.name());
                            sb.append(':');
                        }
                    }
                    if (ends.containsKey(i)) {
                        sb.append("<|");
                    }
                    sb.append(text.charAt(i));
                }
            }
            index = lineEnd + 1;
        }

        return sb.toString();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Semantic Highlighting Tests
    ////////////////////////////////////////////////////////////////////////////
    protected SemanticAnalyzer getSemanticAnalyzer() {
        return null;
    }
    
    protected void checkSemantic(String relFilePath, String caretLine) throws Exception {
        SemanticAnalyzer analyzer = getSemanticAnalyzer();
        assertNotNull("getSemanticAnalyzer must be implemented", analyzer);
        GsfTestCompilationInfo info = getInfo(relFilePath);
        
        String text = info.getText();
        assertNotNull(text);

        int caretOffset = -1;
        if (caretLine != null) {
            int caretDelta = caretLine.indexOf("^");
            assertTrue(caretDelta != -1);
            caretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
            int lineOffset = text.indexOf(caretLine);
            assertTrue(lineOffset != -1);

            caretOffset = lineOffset + caretDelta;
            info.setCaretOffset(caretOffset);
        }

        ParserResult pr = info.getEmbeddedResult(info.getPreferredMimeType(), 0);
        assertNotNull(pr);
        
        analyzer.run(info);
        Map<OffsetRange, ColoringAttributes> highlights = analyzer.getHighlights();

        String annotatedSource = annotateSemanticResults(info.getDocument(), highlights);

        assertDescriptionMatches(relFilePath, annotatedSource, false, ".semantic");
    }

    private String annotateSemanticResults(Document doc, Map<OffsetRange, ColoringAttributes> highlights) throws Exception {
        StringBuilder sb = new StringBuilder();
        String text = doc.getText(0, doc.getLength());
        Map<Integer, OffsetRange> starts = new HashMap<Integer, OffsetRange>(100);
        Map<Integer, OffsetRange> ends = new HashMap<Integer, OffsetRange>(100);
        for (OffsetRange range : highlights.keySet()) {
            starts.put(range.getStart(), range);
            ends.put(range.getEnd(), range);
        }

        for (int i = 0; i < text.length(); i++) {
            if (starts.containsKey(i)) {
                sb.append("|>");
                OffsetRange range = starts.get(i);
                ColoringAttributes ca = highlights.get(range);
                if (ca != null) {
                    sb.append(ca.name());
                    sb.append(':');
                }
            }
            if (ends.containsKey(i)) {
                sb.append("<|");
            }
            sb.append(text.charAt(i));
        }

        return sb.toString();
    }

    protected void checkSemantic(String relFilePath) throws Exception {
        checkSemantic(relFilePath, null);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Rename Handling Tests
    ////////////////////////////////////////////////////////////////////////////
    protected InstantRenamer getRenameHandler() {
        return null;
    }

    protected void checkRenameSections(String relFilePath, String caretLine) throws Exception {
        CompilationInfo info = getInfo(relFilePath);
        InstantRenamer handler = getRenameHandler();
        assertNotNull("getRenameHandler must be implemented", handler);

        int caretOffset = -1;
        if (caretLine != null) {
            int caretDelta = caretLine.indexOf("^");
            assertTrue(caretDelta != -1);
            caretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
            int lineOffset = info.getText().indexOf(caretLine);
            assertTrue(lineOffset != -1);

            caretOffset = lineOffset + caretDelta;
        }

        String annotatedSource;
        String[] desc = new String[1];
        if (handler.isRenameAllowed(info, caretOffset, desc)) {
            Set<OffsetRange> renameRegions = handler.getRenameRegions(info, caretOffset);
            annotatedSource = annotateRenameRegions(info.getDocument(), renameRegions);
        } else {
            annotatedSource = "Refactoring not allowed here\n";
            if (desc[0] != null) {
                annotatedSource += desc[0] + "\n";
            }
        }

        assertDescriptionMatches(relFilePath, annotatedSource, true, ".rename");
    }

    private String annotateRenameRegions(Document doc, Set<OffsetRange> ranges) throws Exception {
        if (ranges.size() == 0) {
            return "Requires Interactive Refactoring\n";
        }
        StringBuilder sb = new StringBuilder();
        String text = doc.getText(0, doc.getLength());
        Map<Integer, OffsetRange> starts = new HashMap<Integer, OffsetRange>(100);
        Map<Integer, OffsetRange> ends = new HashMap<Integer, OffsetRange>(100);
        for (OffsetRange range : ranges) {
            starts.put(range.getStart(), range);
            ends.put(range.getEnd(), range);
        }

        for (int i = 0; i < text.length(); i++) {
            if (starts.containsKey(i)) {
                sb.append("|>");
            }
            if (ends.containsKey(i)) {
                sb.append("<|");
            }
            sb.append(text.charAt(i));
        }
        // Only print lines with result
        String[] lines = sb.toString().split("\n");
        sb = new StringBuilder();
        int lineno = 1;
        for (String line : lines) {
            if (line.indexOf("|>") != -1) {
                sb.append(Integer.toString(lineno));
                sb.append(": ");
                sb.append(line);
                sb.append("\n");
            }
            lineno++;
        }
        
        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Indexing Tests
    ////////////////////////////////////////////////////////////////////////////
    public Indexer getIndexer() {
        return null;
    }
    
    protected List<IndexDocument> indexFile(String relFilePath) throws Exception {
        GsfTestCompilationInfo info = getInfo(relFilePath);
        ParserResult rpr = info.getEmbeddedResult(info.getPreferredMimeType(), 0);
        assertNotNull(rpr);

        Indexer indexer = getIndexer();
        assertNotNull("getIndexer must be implemented", indexer);
        IndexDocumentFactory factory = new IndexDocumentFactoryImpl(/*info.getIndex(info.getPreferredMimeType())*/);
        List<IndexDocument> result = indexer.index(rpr, factory);
        if (result == null) {
            // GSF also makes this conversion
            result = Collections.emptyList();
        }
        
        return result;
    }
    
    protected void checkIndexer(String relFilePath) throws Exception {
        File jsFile = new File(getDataDir(), relFilePath);
        String fileUrl = jsFile.toURI().toURL().toExternalForm();
        String localUrl = fileUrl;
        int index = localUrl.lastIndexOf('/');
        if (index != -1) {
            localUrl = localUrl.substring(0, index);
        }
        
        List<IndexDocument> result = indexFile(relFilePath);
        String annotatedSource = prettyPrint(fileUrl, result, localUrl);

        assertDescriptionMatches(relFilePath, annotatedSource, false, ".indexed");
    }
    
    
    protected void checkIsIndexable(String relFilePath, boolean isIndexable) throws Exception {
        Indexer indexer = getIndexer();
        assertNotNull("getIndexer must be implemented", indexer);
        FileObject fo = getTestFile(relFilePath);
        assertNotNull(fo);
        ParserFile file = new DefaultParserFile(fo, null, false);
        
        assertEquals(isIndexable, indexer.isIndexable(file));
    }
    
    private String sortCommaList(String s) {
        String[] items = s.split(",");
        Arrays.sort(items);
        StringBuilder sb = new StringBuilder();
        for (String item : items) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(item);
        }

        return sb.toString();
    }
    
    protected String prettyPrintValue(String key, String value) {
        return value;
    }
    
    public String prettyPrint(String fileUrl, List<IndexDocument> documents, String localUrl) throws IOException {
        List<String> nonEmptyDocuments = new ArrayList<String>();
        List<String> emptyDocuments = new ArrayList<String>();

        StringBuilder sb = new StringBuilder();
        sb.append("Delete:");
        sb.append("  ");
        sb.append("source");
        sb.append(" : ");
        sb.append(fileUrl);
        sb.append("\n");
        sb.append("\n");  
        nonEmptyDocuments.add(sb.toString());
        

        for (IndexDocument d : documents) {
            IndexDocumentImpl doc = (IndexDocumentImpl)d;
        
            sb = new StringBuilder();
            
            if (doc.overrideUrl != null) {
                sb.append("Override URL: ");
                sb.append(doc.overrideUrl);
                sb.append("\n");
            }
                            
            sb.append("Indexed:");
            sb.append("\n");
            List<String> strings = new ArrayList<String>();

            List<String> keys = doc.indexedKeys;
            List<String> values = doc.indexedValues;
            for (int i = 0, n = keys.size(); i < n; i++) {
                String key = keys.get(i);
                String value = values.get(i);
                strings.add(key + " : " + prettyPrintValue(key, value));
            }
            Collections.sort(strings);
            for (String string : strings) {
                sb.append("  ");
                sb.append(string);
                sb.append("\n");
            }

            sb.append("\n");
            sb.append("Not Indexed:");
            sb.append("\n");
            strings = new ArrayList<String>();
            keys = doc.unindexedKeys;
            values = doc.unindexedValues;
            for (int i = 0, n = keys.size(); i < n; i++) {
                String key = keys.get(i);
                String value = prettyPrintValue(key, values.get(i));
                if (value.indexOf(',') != -1) {
                    value = sortCommaList(value);
                }
                strings.add(key + " : " + value);
            }

            Collections.sort(strings);
            for (String string : strings) {
                sb.append("  ");
                sb.append(string);
                sb.append("\n");
            }

            String s = sb.toString();
            if (doc.indexedKeys.size() == 0 && doc.unindexedKeys.size() == 0) {
                emptyDocuments.add(s);
            } else {
                nonEmptyDocuments.add(s);
            }
        }

        Collections.sort(emptyDocuments);
        Collections.sort(nonEmptyDocuments);
        sb = new StringBuilder();
        int documentNumber = 0;
        for (String s : emptyDocuments) {
            sb.append("\n\nDocument ");
            sb.append(Integer.toString(documentNumber++));
            sb.append("\n");
            sb.append(s);
        }

        for (String s : nonEmptyDocuments) {
            sb.append("\n\nDocument ");
            sb.append(Integer.toString(documentNumber++));
            sb.append("\n");
            sb.append(s);
        }


        return sb.toString().replace(localUrl, "<TESTURL>");
    }
        
        
    public class IndexDocumentImpl implements IndexDocument {
        public List<String> indexedKeys = new ArrayList<String>();
        public List<String> indexedValues = new ArrayList<String>();
        public List<String> unindexedKeys = new ArrayList<String>();
        public List<String> unindexedValues = new ArrayList<String>();

        public String overrideUrl;

        IndexDocumentImpl(String overrideUrl) {
            this.overrideUrl = overrideUrl;
        }

        public void addPair(String key, String value, boolean indexed) {
            if (indexed) {
                indexedKeys.add(key);
                indexedValues.add(value);
            } else {
                unindexedKeys.add(key);
                unindexedValues.add(value);
            }
        }
    }

    public class IndexDocumentFactoryImpl implements IndexDocumentFactory {
        IndexDocumentFactoryImpl() {
        }

        public IndexDocument createDocument(int initialPairs) {
            return new IndexDocumentImpl(null);
        }

        public IndexDocument createDocument(int initialPairs, String overrideUrl) {
            return new IndexDocumentImpl(overrideUrl);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Structure Analyzer Tests
    ////////////////////////////////////////////////////////////////////////////
    public StructureScanner getStructureScanner() {
        return null;
    }
    
    protected void checkStructure(String relFilePath) throws Exception {
        CompilationInfo info = getInfo(relFilePath);

        StructureScanner analyzer = getStructureScanner();
        assertNotNull("getStructureScanner must be implemented", analyzer);
        
        HtmlFormatter formatter = new HtmlFormatter() {
            private StringBuilder sb = new StringBuilder();
            
            @Override
            public void reset() {
                sb.setLength(0);
            }

            @Override
            public void appendHtml(String html) {
                sb.append(html);
            }

            @Override
            public void appendText(String text, int fromInclusive, int toExclusive) {
                sb.append("ESCAPED{");
                sb.append(text, fromInclusive, toExclusive);
                sb.append("}");
            }
            
            @Override
            public void name(ElementKind kind, boolean start) {
                if (start) {
                    sb.append(kind);
                }
            }

            @Override
            public void active(boolean start) {
                if (start) {
                    sb.append("ACTIVE{");
                } else {
                    sb.append("}");
                }
            }
            
            @Override
            public void parameters(boolean start) {
                if (start) {
                    sb.append("PARAMETERS{");
                } else {
                    sb.append("}");
                }
            }

            @Override
            public void type(boolean start) {
                if (start) {
                    sb.append("TYPE{");
                } else {
                    sb.append("}");
                }
            }

            @Override
            public void deprecated(boolean start) {
                if (start) {
                    sb.append("DEPRECATED{");
                } else {
                    sb.append("}");
                }
            }

            @Override
            public String getText() {
                return sb.toString();
            }

            @Override
            public void emphasis(boolean start) {
            }
        };
        List<? extends StructureItem> structure = analyzer.scan(info, formatter);
        
        String annotatedSource = annotateStructure(info.getDocument(), structure);
        assertDescriptionMatches(relFilePath, annotatedSource, false, ".structure");
    }
    
    protected void checkFolds(String relFilePath) throws Exception {
        CompilationInfo info = getInfo(relFilePath);

        StructureScanner analyzer = getStructureScanner();
        assertNotNull("getStructureScanner must be implemented", analyzer);

        Map<String,List<OffsetRange>> foldsMap = analyzer.folds(info);
        
        // Write folding structure
        String source = info.getText();
        List<Integer> begins = new ArrayList<Integer>();
        List<Integer> ends = new ArrayList<Integer>();
        
        begins.add(0);
        
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            if (c == '\n') {
                ends.add(i);
                if (i < source.length()) {
                    begins.add(i+1);
                }
            }
        }
        
        ends.add(source.length());

        assertEquals(begins.size(), ends.size());
        List<Character> margin = new ArrayList<Character>(begins.size());
        for (int i = 0; i < begins.size(); i++) {
            margin.add(' ');
        }

        List<String> typeList = new ArrayList<String>(foldsMap.keySet());
        Collections.sort(typeList);
        for (String type : typeList) {
            List<OffsetRange> ranges = foldsMap.get(type);
            for (OffsetRange range : ranges) {
                int beginIndex = Collections.binarySearch(begins, range.getStart());
                if (beginIndex < 0) {
                    beginIndex = -(beginIndex+2);
                }
                int endIndex = Collections.binarySearch(ends, range.getEnd());
                if (endIndex < 0) {
                    endIndex = -(endIndex+2);
                }
                for (int i = beginIndex; i <= endIndex; i++) {
                    char c = margin.get(i);
                    if (i == beginIndex) {
                        c = '+';
                    } else if (c != '+') {
                        if (i == endIndex) {
                            c = '-';
                        } else {
                            c = '|';
                        }
                    }
                    margin.set(i, c);
                }
            }
        }
        
        StringBuilder sb = new StringBuilder(3000);
        for (int i = 0; i < begins.size(); i++) {
            sb.append(margin.get(i));
            sb.append(' ');
            for (int j = begins.get(i), max = ends.get(i); j < max; j++) {
                sb.append(source.charAt(j));
            }
            sb.append('\n');
        }
        String annotatedSource = sb.toString();
        
        assertDescriptionMatches(relFilePath, annotatedSource, false, ".folds");
    }

    private void annotateStructureItem(int indent, StringBuilder sb, Document document, List<? extends StructureItem> structure) {
        for (StructureItem element : structure) {
            for (int i = 0; i < indent; i++) {
                sb.append("  ");
            }
            sb.append(element.getName());
            sb.append(":");
            sb.append(element.getKind());
            sb.append(":");
            sb.append(element.getModifiers());
            sb.append(":");
            sb.append(element.getHtml());
            sb.append(":");
            sb.append("\n");
            List<? extends StructureItem> children = element.getNestedItems();
            if (children != null && children.size() > 0) {
                List<? extends StructureItem> c = new ArrayList<StructureItem>(children);
                // Sort children to make tests more stable
                Collections.sort(c, new Comparator<StructureItem>() {
                    public int compare(StructureItem s1, StructureItem s2) {
                        String s1Name = s1.getName();
                        String s2Name = s2.getName();
                        if (s1Name == null || s2Name == null) {
                            if (s1Name == s2Name) {
                                return 0;
                            } else if (s1Name == null) {
                                return -1;
                            } else {
                                return 1;
                            }
                        } else {
                            return s1Name.compareTo(s2Name);
                        }
                    }
                    
                });
                
                annotateStructureItem(indent+1, sb, document, c);
            }
        }
    }

    private String annotateStructure(Document document, List<? extends StructureItem> structure) {
        StringBuilder sb = new StringBuilder();
        annotateStructureItem(0, sb, document, structure);
        
        return sb.toString();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Formatting Tests
    ////////////////////////////////////////////////////////////////////////////
    protected Formatter getFormatter(IndentPrefs preferences) {
        return null;
    }

    public class IndentPrefs {

        private final int hanging;

        private final int indent;

        public IndentPrefs(int indent, int hanging) {
            super();
            this.indent = indent;
            this.hanging = hanging;
        }

        public int getIndentation() {
            return indent;
        }

        public int getHangingIndentation() {
            return hanging;
        }
    }
    
    public void format(String source, String reformatted, IndentPrefs preferences) throws Exception {
        Formatter formatter = getFormatter(preferences);
        assertNotNull("getFormatter must be implemented", formatter);

        String BEGIN = "%<%"; // NOI18N
        int startPos = source.indexOf(BEGIN);
        if (startPos != -1) {
            source = source.substring(0, startPos) + source.substring(startPos+BEGIN.length());
        } else {
            startPos = 0;
        }
        
        String END = "%>%"; // NOI18N
        int endPos = source.indexOf(END);
        if (endPos != -1) {
            source = source.substring(0, endPos) + source.substring(endPos+END.length());
        }

        BaseDocument doc = getDocument(source);

        if (endPos == -1) {
            endPos = doc.getLength();
        }
        
        formatter.reformat(doc, startPos, endPos, getInfoForText(source, "unittestdata"));

        String formatted = doc.getText(0, doc.getLength());
        assertEquals(reformatted, formatted);
    }
    
    protected void reformatFileContents(String file, IndentPrefs preferences) throws Exception {
        FileObject fo = getTestFile(file);
        assertNotNull(fo);
        BaseDocument doc = getDocument(fo);
        assertNotNull(doc);
        //String before = doc.getText(0, doc.getLength());
        
        Formatter formatter = getFormatter(preferences);
        assertNotNull("getFormatter must be implemented", formatter);

        formatter.reformat(doc, 0, doc.getLength(), getInfo(fo));
        String after = doc.getText(0, doc.getLength());
        
        assertDescriptionMatches(file, after, false, ".formatted");
    }
    
    public void insertNewline(String source, String reformatted, IndentPrefs preferences) throws Exception {
        Formatter formatter = getFormatter(preferences);
        assertNotNull("getFormatter must be implemented", formatter);

        int sourcePos = source.indexOf('^');     
        assertNotNull(sourcePos);
        source = source.substring(0, sourcePos) + source.substring(sourcePos+1);

        int reformattedPos = reformatted.indexOf('^');        
        assertNotNull(reformattedPos);
        reformatted = reformatted.substring(0, reformattedPos) + reformatted.substring(reformattedPos+1);
        
        BaseDocument doc = getDocument(source);

        JTextArea ta = new JTextArea(doc);
        Caret caret = ta.getCaret();
        caret.setDot(sourcePos);
        doc.atomicLock();
        DocumentUtilities.setTypingModification(doc, true);

        try {
            doc.insertString(caret.getDot(), "\n", null);
        
            int startPos = caret.getDot()+1;
            int endPos = startPos;

            //ParserResult result = parse(fo);
            formatter.reindent(doc, startPos, endPos);

            String formatted = doc.getText(0, doc.getLength());
            assertEquals(reformatted, formatted);
        } finally {
            DocumentUtilities.setTypingModification(doc, false);
            doc.atomicUnlock();
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Code Completion Tests
    ////////////////////////////////////////////////////////////////////////////
    protected CodeCompletionHandler getCodeCompleter() {
        return null;
    }

    protected String describe(String caretLine, NameKind kind, QueryType type, List<CompletionProposal> proposals, 
            boolean includeModifiers, boolean[] deprecatedHolder) {
        assert deprecatedHolder != null && deprecatedHolder.length == 1;
        StringBuilder sb = new StringBuilder();
        sb.append("Results for " + caretLine + " with queryType=" + type + " and nameKind=" + kind);
        sb.append("\n");

        // Sort to make test more stable
        Collections.sort(proposals, new Comparator<CompletionProposal>() {

            public int compare(CompletionProposal p1, CompletionProposal p2) {
                // Smart items first
                if (p1.isSmart() != p2.isSmart()) {
                    return p1.isSmart() ? -1 : 1;
                }

                if (p1.getKind() != p2.getKind()) {
                    return p1.getKind().compareTo(p2.getKind());
                }
                
                if (!p1.getLhsHtml().equals(p2.getLhsHtml())) {
                    return p1.getLhsHtml().compareTo(p2.getLhsHtml());
                }

                String p1Rhs = p1.getRhsHtml();
                String p2Rhs = p2.getRhsHtml();
                if (p1Rhs == null) {
                    p1Rhs = "";
                }
                if (p2Rhs == null) {
                    p2Rhs = "";
                }
                if (!p1Rhs.equals(p2Rhs)) {
                    return p1Rhs.compareTo(p2Rhs);
                }

                // Yuck - tostring comparison of sets!!
                if (!p1.getModifiers().toString().equals(p2.getModifiers().toString())) {
                    return p1.getModifiers().toString().compareTo(p2.getModifiers().toString());
                }
                
                return 0;
            }
        });
        
        boolean isSmart = true;
        for (CompletionProposal proposal : proposals) {
            if (isSmart && !proposal.isSmart()) {
                sb.append("------------------------------------\n");
                isSmart = false;
            }

            deprecatedHolder[0] = false;
            proposal.getLhsHtml(); // Side effect to deprecatedHolder used
            boolean strike = includeModifiers && deprecatedHolder[0];
            
            String n = proposal.getKind().toString();
            int MAX_KIND = 10;
            if (n.length() > MAX_KIND) {
                sb.append(n.substring(0, MAX_KIND));
            } else {
                sb.append(n);
                for (int i = n.length(); i < MAX_KIND; i++) {
                    sb.append(" ");
                }
            }
            
//            if (proposal.getModifiers().size() > 0) {
//                List<String> modifiers = new ArrayList<String>();
//                for (Modifier mod : proposal.getModifiers()) {
//                    modifiers.add(mod.name());
//                }
//                Collections.sort(modifiers);
//                sb.append(modifiers);
//            }

            sb.append(" ");
            
            n = proposal.getLhsHtml();
            int MAX_LHS = 30;
            if (strike) {
                MAX_LHS -= 6; // Account for the --- --- strikethroughs
                sb.append("---");
            }
            if (n.length() > MAX_LHS) {
                sb.append(n.substring(0, MAX_LHS));
            } else {
                sb.append(n);
                for (int i = n.length(); i < MAX_LHS; i++) {
                    sb.append(" ");
                }
            }

            if (strike) {
                sb.append("---");
            }
            
            sb.append("  ");

            if (proposal.getModifiers().isEmpty()) {
                n = "";
            } else {
                n = proposal.getModifiers().toString();
            }
            int MAX_MOD = 9;
            if (n.length() > MAX_MOD) {
                sb.append(n.substring(0, MAX_MOD));
            } else {
                sb.append(n);
                for (int i = n.length(); i < MAX_MOD; i++) {
                    sb.append(" ");
                }
            }

            sb.append("  ");
            
            sb.append(proposal.getRhsHtml());
            sb.append("\n");
            
            isSmart = proposal.isSmart();
        }
        
        return sb.toString();
    }
    
    public void checkCompletion(String file, String caretLine, boolean includeModifiers) throws Exception {
        initializeClassPaths();

        // TODO call TestCompilationInfo.setCaretOffset!        
        final QueryType type = QueryType.COMPLETION;
        final boolean caseSensitive = true;
        final NameKind kind = caseSensitive ? NameKind.PREFIX : NameKind.CASE_INSENSITIVE_PREFIX;

        final GsfTestCompilationInfo ci = getInfo(file);
        String text = ci.getText();
        assertNotNull(text);

        int caretOffset = -1;
        if (caretLine != null) {
            int caretDelta = caretLine.indexOf("^");
            assertTrue(caretDelta != -1);
            caretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
            int lineOffset = text.indexOf(caretLine);
            assertTrue(lineOffset != -1);

            caretOffset = lineOffset + caretDelta;
            ci.setCaretOffset(caretOffset);
        }

        ParserResult pr = ci.getEmbeddedResult(ci.getPreferredMimeType(), 0);
        assertNotNull(pr);
        
        CodeCompletionHandler cc = getCodeCompleter();
        assertNotNull("getSemanticAnalyzer must be implemented", cc);
        final boolean deprecatedHolder[] = new boolean[1];
        
        final HtmlFormatter formatter = new HtmlFormatter() {
            private StringBuilder sb = new StringBuilder();
            
            @Override
            public void reset() {
                sb.setLength(0);
            }

            @Override
            public void appendHtml(String html) {
                sb.append(html);
            }

            @Override
            public void appendText(String text, int fromInclusive, int toExclusive) {
                sb.append(text, fromInclusive, toExclusive);
            }
            
            @Override
            public void emphasis(boolean start) {
            }

            @Override
            public void active(boolean start) {
            }

            @Override
            public void name(ElementKind kind, boolean start) {
            }

            @Override
            public void parameters(boolean start) {
            }

            @Override
            public void type(boolean start) {
            }

            @Override
            public void deprecated(boolean start) {
                deprecatedHolder[0] = true;
            }

            @Override
            public String getText() {
                return sb.toString();
            }
        };
        boolean upToOffset = type == QueryType.COMPLETION;
        String prefix = cc.getPrefix(ci, caretOffset, upToOffset);
        if (prefix == null) {
            if (prefix == null) {
                int[] blk =
                    org.netbeans.editor.Utilities.getIdentifierBlock((BaseDocument)ci.getDocument(),
                        caretOffset);

                if (blk != null) {
                    int start = blk[0];

                    if (start < caretOffset ) {
                        if (upToOffset) {
                            prefix = ci.getDocument().getText(start, caretOffset - start);
                        } else {
                            prefix = ci.getDocument().getText(start, blk[1]-start);
                        }
                    }
                }
            }
        }

        Source js = Source.forFileObject(ci.getFileObject());
        assertNotNull(js);
        //ci.getIndex();
        //index.setDirty(js);
        js.testUpdateIndex();
        
        final int finalCaretOffset = caretOffset;
        final String finalPrefix = prefix;
        CodeCompletionContext context = new CodeCompletionContext() {

            @Override
            public int getCaretOffset() {
                return finalCaretOffset;
            }

            @Override
            public CompilationInfo getInfo() {
                return ci;
            }

            @Override
            public String getPrefix() {
                return finalPrefix;
            }

            @Override
            public NameKind getNameKind() {
                return kind;
            }

            @Override
            public QueryType getQueryType() {
                return type;
            }

            @Override
            public boolean isCaseSensitive() {
                return caseSensitive;
            }

            @Override
            public HtmlFormatter getFormatter() {
                return formatter;
            }
        };
        
        CodeCompletionResult completionResult = cc.complete(context);
        List<CompletionProposal> proposals = completionResult.getItems();
        
        String described = describe(caretLine, kind, type, proposals, includeModifiers, deprecatedHolder);
        assertDescriptionMatches(file, described, true, ".completion");
    }
    
    protected void checkCall(GsfTestCompilationInfo info, int caretOffset, String param, boolean expectSuccess) {
    }

    protected void initializeClassPaths() {
    }
    
    public void checkComputeMethodCall(String file, String caretLine, String fqn, String param, boolean expectSuccess) throws Exception {
        initializeClassPaths();
        
        QueryType type = QueryType.COMPLETION;
        //boolean caseSensitive = true;

        GsfTestCompilationInfo ci = getInfo(file);
        String text = ci.getText();
        assertNotNull(text);
        
        int caretOffset = -1;
        if (caretLine != null) {
            int caretDelta = caretLine.indexOf("^");
            assertTrue(caretDelta != -1);
            caretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
            int lineOffset = text.indexOf(caretLine);
            assertTrue(lineOffset != -1);

            caretOffset = lineOffset + caretDelta;
            ci.setCaretOffset(caretOffset);
        }
        
        ParserResult pr = ci.getEmbeddedResult(ci.getPreferredMimeType(), 0);
        assertNotNull(pr);
        
        CodeCompletionHandler cc = getCodeCompleter();
        assertNotNull("getSemanticAnalyzer must be implemented", cc);

        boolean upToOffset = type == QueryType.COMPLETION;
        String prefix = cc.getPrefix(ci, caretOffset, upToOffset);
        if (prefix == null) {
            if (prefix == null) {
                int[] blk =
                    org.netbeans.editor.Utilities.getIdentifierBlock((BaseDocument)ci.getDocument(),
                        caretOffset);

                if (blk != null) {
                    int start = blk[0];

                    if (start < caretOffset ) {
                        if (upToOffset) {
                            prefix = ci.getDocument().getText(start, caretOffset - start);
                        } else {
                            prefix = ci.getDocument().getText(start, blk[1]-start);
                        }
                    }
                }
            }
        }

        Source js = Source.forFileObject(ci.getFileObject());
        assertNotNull(js);
        //ci.getIndex();
        //index.setDirty(js);
        js.testUpdateIndex();
        
        checkCall(ci, caretOffset, param, expectSuccess);
    }
    
    public void checkPrefix(String relFilePath) throws Exception {
        CompilationInfo info = getInfo(relFilePath);

        BaseDocument doc = (BaseDocument)info.getDocument();
        StringBuilder sb = new StringBuilder();

        CodeCompletionHandler completer = getCodeCompleter();
        assertNotNull("getSemanticAnalyzer must be implemented", completer);

        int index = 0;
        while (index < doc.getLength()) {
            int lineStart = index;
            int lineEnd = Utilities.getRowEnd(doc, index);
            if (lineEnd == -1) {
                break;
            }
            if (Utilities.getRowFirstNonWhite(doc, index) != -1) {
                String line = doc.getText(lineStart, lineEnd-lineStart);
                for (int i = lineStart; i <= lineEnd; i++) {
                    String prefix = completer.getPrefix(info, i, true); // line.charAt(i)
                    if (prefix == null) {
                        continue;
                    }
                    String wholePrefix = completer.getPrefix(info, i, false);
                    assertNotNull(wholePrefix);

                    sb.append(line +"\n");
                    //sb.append("Offset ");
                    //sb.append(Integer.toString(i));
                    //sb.append(" : \"");
                    for (int j = lineStart; j < i; j++) {
                        sb.append(' ');
                    }
                    sb.append('^');
                    sb.append(prefix.length() > 0 ? prefix : "\"\"");
                    sb.append(",");
                    sb.append(wholePrefix.length() > 0 ? wholePrefix : "\"\"");
                    sb.append("\n");
                }
            }
            
            index = lineEnd+1;
        }

        String annotatedSource = sb.toString();

        assertDescriptionMatches(relFilePath, annotatedSource, false, ".prefixes");
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Ast Offsets Test
    ////////////////////////////////////////////////////////////////////////////
    protected String describeNode(CompilationInfo info, Object node, boolean includePath) throws Exception {
        // Override in your test
        return null;
    }
    
    protected void initializeNodes(CompilationInfo info, List<Object> validNodes,
            Map<Object,OffsetRange> positions, List<Object> invalidNodes) throws Exception {
        // Override in your test
    }
    
    protected void checkOffsets(String relFilePath) throws Exception {
        checkOffsets(relFilePath, null);
    }
    
    protected void checkOffsets(String relFilePath, String caretLine) throws Exception {
        GsfTestCompilationInfo info = getInfo(relFilePath);
        
        String text = info.getText();
        assertNotNull(text);

        int caretOffset = -1;
        if (caretLine != null) {
            int caretDelta = caretLine.indexOf("^");
            assertTrue(caretDelta != -1);
            caretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
            int lineOffset = text.indexOf(caretLine);
            assertTrue(lineOffset != -1);

            caretOffset = lineOffset + caretDelta;
            info.setCaretOffset(caretOffset);
        }

        ParserResult pr = info.getEmbeddedResult(info.getPreferredMimeType(), 0);
        assertNotNull(pr);

        List<Object> validNodes = new ArrayList<Object>();
        List<Object> invalidNodes = new ArrayList<Object>();
        Map<Object,OffsetRange> positions = new HashMap<Object,OffsetRange>();
        initializeNodes(info, validNodes, positions, invalidNodes);

        BaseDocument doc = (BaseDocument) info.getDocument();
        String annotatedSource = annotateOffsets(validNodes, positions, invalidNodes, info);
        assertDescriptionMatches(relFilePath, annotatedSource, false, ".offsets");
    }
    
    
    /** Pass the nodes in an in-order traversal order such that it can properly nest
     * items when they have identical starting or ending endpoints */
    private String annotateOffsets(List<Object> validNodes, Map<Object,OffsetRange> positions,
            List<Object> invalidNodes, CompilationInfo info) throws Exception {
        //
        StringBuilder sb = new StringBuilder();
        BaseDocument doc = (BaseDocument) info.getDocument();
        String text = doc.getText(0, doc.getLength());

        final Map<Object,Integer> traversalNumber = new HashMap<Object,Integer>();
        int id = 0;
        for (Object node : validNodes) {
            traversalNumber.put(node, id++);
        }
                
        Comparator<Object> FORWARDS_COMPARATOR = new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                assertTrue(traversalNumber.containsKey(o1));
                assertTrue(traversalNumber.containsKey(o2));
                
                return traversalNumber.get(o1) - traversalNumber.get(o2);
            }
        };

        Comparator<Object> BACKWARDS_COMPARATOR = new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                assertTrue(traversalNumber.containsKey(o1));
                assertTrue(traversalNumber.containsKey(o2));
                
                return traversalNumber.get(o2) - traversalNumber.get(o1);
            }
        };
        
        Map<Integer,List<Object>> starts = new HashMap<Integer,List<Object>>(100);
        Map<Integer,List<Object>> ends = new HashMap<Integer,List<Object>>(100);
    
        for (Object node : validNodes) {
            OffsetRange range = positions.get(node);
            List<Object> list = starts.get(range.getStart());
            if (list == null) {
                list = new ArrayList<Object>();
                starts.put(range.getStart(), list);
            }
            list.add(node);
            list = ends.get(range.getEnd());
            if (list == null) {
                list = new ArrayList<Object>();
                ends.put(range.getEnd(), list);
            }
            list.add(node);
        }
        
        // Sort nodes
        for (List<Object> list : starts.values()) {
            Collections.sort(list, FORWARDS_COMPARATOR);
        }
        for (List<Object> list : ends.values()) {
            Collections.sort(list, BACKWARDS_COMPARATOR);
        }
        
        // Include 0-0 nodes first
        List<String> missing = new ArrayList<String>();
        for (Object n : invalidNodes) {
            String desc = describeNode(info, n, true);
            assertNotNull("You must implement describeNode()", desc);
            
            missing.add("Missing position for node " + desc);
        }
        Collections.sort(missing);
        for (String s : missing) {
            sb.append(s);
            sb.append("\n");
        }
        sb.append("\n");
        
        for (int i = 0; i < text.length(); i++) {
            if (starts.containsKey(i)) {
                List<Object> ns = starts.get(i);
                for (Object n : ns) {
                    sb.append("<");
                    String desc = describeNode(info, n, false);
                    assertNotNull(desc);
                    sb.append(desc);
                    sb.append(">");
                }
            }
            if (ends.containsKey(i)) {
                List<Object> ns = ends.get(i);
                for (Object n : ns) {
                    sb.append("</");
                    String desc = describeNode(info, n, false);
                    assertNotNull(desc);
                    sb.append(desc);
                    sb.append(">");
                }
            }
            char c = text.charAt(i);
            switch (c) {
            case '&': sb.append("&amp;"); break;
            case '<': sb.append("&lt;"); break;
            case '>': sb.append("&gt;"); break;
            default:
                sb.append(c);
            }
        }

        return sb.toString();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Type Test
    ////////////////////////////////////////////////////////////////////////////
    protected void initializeTypeNodes(CompilationInfo info, List<Object> nodes,
            Map<Object,OffsetRange> positions, Map<Object,String> types) throws Exception {
        // Override in your test
        // Associate type descriptions with a bunch of nodes.
        // For every node that has an associated type, add position and description information about it.
        // This will then be used to generate type hints in the source
    }
    
    protected void checkTypes(String relFilePath) throws Exception {
        checkTypes(relFilePath, null);
    }
    
    protected void checkTypes(String relFilePath, String caretLine) throws Exception {
        GsfTestCompilationInfo info = getInfo(relFilePath);
        
        String text = info.getText();
        assertNotNull(text);

        int caretOffset = -1;
        if (caretLine != null) {
            int caretDelta = caretLine.indexOf("^");
            assertTrue(caretDelta != -1);
            caretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
            int lineOffset = text.indexOf(caretLine);
            assertTrue(lineOffset != -1);

            caretOffset = lineOffset + caretDelta;
            info.setCaretOffset(caretOffset);
        }

        ParserResult pr = info.getEmbeddedResult(info.getPreferredMimeType(), 0);
        assertNotNull(pr);

        List<Object> nodes = new ArrayList<Object>();
        Map<Object,String> types = new HashMap<Object,String>();
        Map<Object,OffsetRange> positions = new HashMap<Object,OffsetRange>();
        initializeTypeNodes(info, nodes, positions, types);

        BaseDocument doc = (BaseDocument) info.getDocument();
        String annotatedSource = annotateTypes(nodes, positions, types, info);
        assertDescriptionMatches(relFilePath, annotatedSource, false, ".types");
    }
    
    
    /** Pass the nodes in an in-order traversal order such that it can properly nest
     * items when they have identical starting or ending endpoints */
    private String annotateTypes(List<Object> validNodes, Map<Object,OffsetRange> positions,
            Map<Object,String> types, CompilationInfo info) throws Exception {
        //
        StringBuilder sb = new StringBuilder();
        BaseDocument doc = (BaseDocument) info.getDocument();
        String text = doc.getText(0, doc.getLength());

        final Map<Object,Integer> traversalNumber = new HashMap<Object,Integer>();
        int id = 0;
        for (Object node : validNodes) {
            traversalNumber.put(node, id++);
        }
                
        Comparator<Object> FORWARDS_COMPARATOR = new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                assertTrue(traversalNumber.containsKey(o1));
                assertTrue(traversalNumber.containsKey(o2));
                
                return traversalNumber.get(o1) - traversalNumber.get(o2);
            }
        };

        Comparator<Object> BACKWARDS_COMPARATOR = new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                assertTrue(traversalNumber.containsKey(o1));
                assertTrue(traversalNumber.containsKey(o2));
                
                return traversalNumber.get(o2) - traversalNumber.get(o1);
            }
        };
        
        Map<Integer,List<Object>> starts = new HashMap<Integer,List<Object>>(100);
        Map<Integer,List<Object>> ends = new HashMap<Integer,List<Object>>(100);
    
        for (Object node : validNodes) {
            OffsetRange range = positions.get(node);
            List<Object> list = starts.get(range.getStart());
            if (list == null) {
                list = new ArrayList<Object>();
                starts.put(range.getStart(), list);
            }
            list.add(node);
            list = ends.get(range.getEnd());
            if (list == null) {
                list = new ArrayList<Object>();
                ends.put(range.getEnd(), list);
            }
            list.add(node);
        }
        
        // Sort nodes
        for (List<Object> list : starts.values()) {
            Collections.sort(list, FORWARDS_COMPARATOR);
        }
        for (List<Object> list : ends.values()) {
            Collections.sort(list, BACKWARDS_COMPARATOR);
        }
        
        // TODO - include information here about nodes without correct positions
        
        for (int i = 0; i < text.length(); i++) {
            if (starts.containsKey(i)) {
                List<Object> ns = starts.get(i);
                for (Object n : ns) {
                    sb.append("<");
                    String desc = types.get(n);
                    //String desc = describeNode(info, n, false);
                    assertNotNull(desc);
                    sb.append(desc);
                    sb.append(">");
                }
            }
            if (ends.containsKey(i)) {
                List<Object> ns = ends.get(i);
                for (Object n : ns) {
                    sb.append("</");
                    //String desc = describeNode(info, n, false);
                    String desc = types.get(n);
                    assertNotNull(desc);
                    sb.append(desc);
                    sb.append(">");
                }
            }
            char c = text.charAt(i);
            switch (c) {
            case '&': sb.append("&amp;"); break;
            case '<': sb.append("&lt;"); break;
            case '>': sb.append("&gt;"); break;
            default:
                sb.append(c);
            }
        }

        return sb.toString();
    }
    
    
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Hints / Quickfix Tests
    ////////////////////////////////////////////////////////////////////////////
    protected HintsProvider getHintsProvider() {
        throw new RuntimeException("You must override getHintsProvider before runnign hints tests!");
    }

    private GsfHintsManager getHintsManager() {
        return new GsfHintsManager(getPreferredMimeType(), getHintsProvider());
    }
    
    protected String annotateHints(BaseDocument doc, List<Hint> result, int caretOffset) throws Exception {
        Map<OffsetRange, List<Hint>> posToDesc = new HashMap<OffsetRange, List<Hint>>();
        Set<OffsetRange> ranges = new HashSet<OffsetRange>();
        for (Hint desc : result) {
            int start = desc.getRange().getStart();
            int end = desc.getRange().getEnd();
            OffsetRange range = new OffsetRange(start, end);
            List<Hint> l = posToDesc.get(range);
            if (l == null) {
                l = new ArrayList<Hint>();
                posToDesc.put(range, l);
            }
            l.add(desc);
            ranges.add(range);
        }
        StringBuilder sb = new StringBuilder();
        String text = doc.getText(0, doc.getLength());
        Map<Integer, OffsetRange> starts = new HashMap<Integer, OffsetRange>(100);
        Map<Integer, OffsetRange> ends = new HashMap<Integer, OffsetRange>(100);
        for (OffsetRange range : ranges) {
            starts.put(range.getStart(), range);
            ends.put(range.getEnd(), range);
        }

        int index = 0;
        int length = text.length();
        while (index < length) {
            int lineStart = Utilities.getRowStart(doc, index);
            int lineEnd = Utilities.getRowEnd(doc, index);
            OffsetRange lineRange = new OffsetRange(lineStart, lineEnd);
            boolean skipLine = true;
            for (OffsetRange range : ranges) {
                if (lineRange.containsInclusive(range.getStart()) || lineRange.containsInclusive(range.getEnd())) {
                    skipLine = false;
                }
            }
            if (!skipLine) {
                List<Hint> descsOnLine = null;
                int underlineStart = -1;
                int underlineEnd = -1;
                for (int i = lineStart; i <= lineEnd; i++) {
                    if (i == caretOffset) {
                        sb.append("^");
                    }
                    if (starts.containsKey(i)) {
                        if (descsOnLine == null) {
                            descsOnLine = new ArrayList<Hint>();
                        }
                        underlineStart = i-lineStart;
                        OffsetRange range = starts.get(i);
                        if (posToDesc.get(range) != null) {
                            for (Hint desc : posToDesc.get(range)) {
                                descsOnLine.add(desc);
                            }
                        }
                    }
                    if (ends.containsKey(i)) {
                        underlineEnd = i-lineStart;
                    }
                    sb.append(text.charAt(i));
                }
                if (underlineStart != -1) {
                    for (int i = 0; i < underlineStart; i++) {
                        sb.append(" ");
                    }
                    for (int i = underlineStart; i < underlineEnd; i++) {
                        sb.append("-");
                    }
                    sb.append("\n");
                }
                if (descsOnLine != null) {
                    Collections.sort(descsOnLine, new Comparator<Hint>() {
                        public int compare(Hint arg0, Hint arg1) {
                            return arg0.getDescription().compareTo(arg1.getDescription());
                        }
                    });
                    for (Hint desc : descsOnLine) {
                        sb.append("HINT:");
                        sb.append(desc.getDescription());
                        sb.append("\n");
                        List<HintFix> list = desc.getFixes();
                        if (list != null) {
                            for (HintFix fix : list) {
                                sb.append("FIX:");
                                sb.append(fix.getDescription());
                                sb.append("\n");
                            }
                        }
                    }
                }
            }
            index = lineEnd + 1;
        }

        return sb.toString();
    }
 
    protected boolean parseErrorsOk;
    
    protected ComputedHints getHints(NbTestCase test, Rule hint, String relFilePath, FileObject fileObject, String caretLine) throws Exception {
        assert relFilePath == null || fileObject == null;
        UserConfigurableRule ucr = null;
        if (hint instanceof UserConfigurableRule) {
            ucr = (UserConfigurableRule)hint;
        }

        // Make sure the hint is enabled
        if (ucr != null && !HintsSettings.isEnabled(ucr)) {
            Preferences p = HintsSettings.getPreferences(ucr, HintsSettings.getCurrentProfileId());
            HintsSettings.setEnabled(p, true);
        }
        
        GsfTestCompilationInfo info = fileObject != null ? getInfo(fileObject) : getInfo(relFilePath);
        ParserResult pr = info.getEmbeddedResult(info.getPreferredMimeType(), 0);
        if (pr == null /*|| pr.hasErrors()*/ && !(hint instanceof ErrorRule)) { // only expect testcase source errors in error tests
            if (parseErrorsOk) {
                List<Hint> result = new ArrayList<Hint>();
                int caretOffset = 0;
                return new ComputedHints(info, result, caretOffset);
            }
            assertTrue("Unexpected parse error in test case " + 
                    FileUtil.getFileDisplayName(info.getFileObject()) + "\nErrors = " + 
                    info.getErrors(), pr != null && !pr.hasErrors());
        }

        String text = info.getText();

        int caretOffset = -1;
        if (caretLine != null) {
            int caretDelta = caretLine.indexOf("^");
            assertTrue(caretDelta != -1);
            caretLine = caretLine.substring(0, caretDelta) + caretLine.substring(caretDelta + 1);
            int lineOffset = text.indexOf(caretLine);
            assertTrue("NOT FOUND: " + info.getFileObject().getName() + ":" + caretLine, lineOffset != -1);

            caretOffset = lineOffset + caretDelta;
        }

        HintsProvider provider = getHintsProvider();
        GsfHintsManager manager = getHintsManager();

        initializeRegistry();
        org.netbeans.modules.gsf.Language language = LanguageRegistry.getInstance().getLanguageByMimeType(getPreferredMimeType());

        List<Hint> hints = new ArrayList<Hint>();
        if (hint instanceof ErrorRule) {
            RuleContext context = manager.createRuleContext(info, language, -1, -1, -1);
            // It's an error!
            // Create a hint registry which contains ONLY our hint (so other registered
            // hints don't interfere with the test)
            Map<Object, List<? extends ErrorRule>> testHints = new HashMap<Object, List<? extends ErrorRule>>();
            if (hint.appliesTo(context)) {
                ErrorRule ErrorRule = (ErrorRule)hint;
                for (Object key : ErrorRule.getCodes()) {
                    testHints.put(key, Collections.singletonList(ErrorRule));
                }
            }
            manager.setTestingRules(testHints, null, null, null);
            provider.computeErrors(manager, context, hints, new ArrayList<Error>());
        } else if (hint instanceof SelectionRule) {
            SelectionRule rule = (SelectionRule)hint;
            List<SelectionRule> testHints = new ArrayList<SelectionRule>();
            testHints.add(rule);
            
            manager.setTestingRules(null, null, null, testHints);
            
            if (caretLine != null) {
                int start = text.indexOf(caretLine);
                int end = start+caretLine.length();
                RuleContext context = manager.createRuleContext(info, language, -1, start, end);
                provider.computeSelectionHints(manager, context, hints, start, end);
            }
        } else {
            assert hint instanceof AstRule && ucr != null;
            AstRule AstRule = (AstRule)hint;
            // Create a hint registry which contains ONLY our hint (so other registered
            // hints don't interfere with the test)
            Map<Object, List<? extends AstRule>> testHints = new HashMap<Object, List<? extends AstRule>>();
            RuleContext context = manager.createRuleContext(info, language, caretOffset, -1, -1);
            if (hint.appliesTo(context)) {
                for (Object nodeId : AstRule.getKinds()) {
                    testHints.put(nodeId, Collections.singletonList(AstRule));
                }
            }
            if (HintsSettings.getSeverity(ucr) == HintSeverity.CURRENT_LINE_WARNING) {
                manager.setTestingRules(null, Collections.EMPTY_MAP, testHints, null);
                provider.computeSuggestions(manager, context, hints, caretOffset);
            } else {
                manager.setTestingRules(null, testHints, null, null);
                context.caretOffset = -1;
                provider.computeHints(manager, context, hints);
            }
        }

        return new ComputedHints(info, hints, caretOffset);
    }
    
    protected void checkHints(NbTestCase test, Rule hint, String relFilePath, String caretLine) throws Exception {
        findHints(test, hint, relFilePath, null, caretLine);
    }
    
    protected void checkHints(Rule hint, String relFilePath,
            String selStartLine, String selEndLine) throws Exception {
        FileObject fo = getTestFile(relFilePath);
        String text = read(fo);

        assert selStartLine != null;
        assert selEndLine != null;
        
        int selStartOffset = -1;
        int lineDelta = selStartLine.indexOf("^");
        assertTrue(lineDelta != -1);
        selStartLine = selStartLine.substring(0, lineDelta) + selStartLine.substring(lineDelta + 1);
        int lineOffset = text.indexOf(selStartLine);
        assertTrue(lineOffset != -1);

        selStartOffset = lineOffset + lineDelta;
        
        int selEndOffset = -1;
        lineDelta = selEndLine.indexOf("^");
        assertTrue(lineDelta != -1);
        selEndLine = selEndLine.substring(0, lineDelta) + selEndLine.substring(lineDelta + 1);
        lineOffset = text.indexOf(selEndLine);
        assertTrue(lineOffset != -1);

        selEndOffset = lineOffset + lineDelta;

        String caretLine = text.substring(selStartOffset, selEndOffset) + "^";
        
        checkHints(this, hint, relFilePath, caretLine);
    }

    // TODO - rename to "checkHints"
    protected void findHints(NbTestCase test, Rule hint, FileObject fileObject, String caretLine) throws Exception {
        findHints(test, hint, null, fileObject, caretLine);
    }
    
    protected String getGoldenFileSuffix() {
        return "";
    }
    
    // TODO - rename to "checkHints"
    protected void findHints(NbTestCase test, Rule hint, String relFilePath, FileObject fileObject, String caretLine) throws Exception {
        ComputedHints r = getHints(test, hint, relFilePath, fileObject, caretLine);
        CompilationInfo info = r.info;
        List<Hint> result = r.hints;
        int caretOffset = r.caretOffset;
        
        String annotatedSource = annotateHints((BaseDocument)info.getDocument(), result, caretOffset);

        if (fileObject != null) {
            assertDescriptionMatches(fileObject, annotatedSource, true, getGoldenFileSuffix() + ".hints");
        } else {
            assertDescriptionMatches(relFilePath, annotatedSource, true, getGoldenFileSuffix() + ".hints");
        }
    }

    protected void applyHint(NbTestCase test, Rule hint, String relFilePath,
            String selStartLine, String selEndLine, String fixDesc) throws Exception {
        FileObject fo = getTestFile(relFilePath);
        String text = read(fo);

        assert selStartLine != null;
        assert selEndLine != null;
        
        int selStartOffset = -1;
        int lineDelta = selStartLine.indexOf("^");
        assertTrue(lineDelta != -1);
        selStartLine = selStartLine.substring(0, lineDelta) + selStartLine.substring(lineDelta + 1);
        int lineOffset = text.indexOf(selStartLine);
        assertTrue(lineOffset != -1);

        selStartOffset = lineOffset + lineDelta;
        
        int selEndOffset = -1;
        lineDelta = selEndLine.indexOf("^");
        assertTrue(lineDelta != -1);
        selEndLine = selEndLine.substring(0, lineDelta) + selEndLine.substring(lineDelta + 1);
        lineOffset = text.indexOf(selEndLine);
        assertTrue(lineOffset != -1);

        selEndOffset = lineOffset + lineDelta;

        String caretLine = text.substring(selStartOffset, selEndOffset) + "^";
        
        applyHint(test, hint, relFilePath, caretLine, fixDesc);
    }

    protected void applyHint(NbTestCase test, Rule hint, String relFilePath,
            String caretLine, String fixDesc) throws Exception {
        ComputedHints r = getHints(test, hint, relFilePath, null, caretLine);
        CompilationInfo info = r.info;
        
        HintFix fix = findApplicableFix(r, fixDesc);
        assertNotNull(fix);
        
        fix.implement();
        
        Document doc = info.getDocument();
        String fixed = doc.getText(0, doc.getLength());

        assertDescriptionMatches(relFilePath, fixed, true, ".fixed");
    }
    
//    public void ensureRegistered(AstRule hint) throws Exception {
//        Map<Integer, List<AstRule>> hints = JsRulesManager.getInstance().getHints();
//        Set<Integer> kinds = hint.getKinds();
//        for (int nodeType : kinds) {
//            List<AstRule> rules = hints.get(nodeType);
//            assertNotNull(rules);
//            boolean found = false;
//            for (AstRule rule : rules) {
//                if (rule instanceof BlockVarReuse) {
//                    found  = true;
//                    break;
//                }
//            }
//            
//            assertTrue(found);
//        }
//    }

    private HintFix findApplicableFix(ComputedHints r, String text) {
        boolean substringMatch = true;
        if (text.endsWith("\n")) {
            text = text.substring(0, text.length()-1);
            substringMatch = false;
        }
        int caretOffset = r.caretOffset;
        for (Hint desc : r.hints) {
            int start = desc.getRange().getStart();
            int end = desc.getRange().getEnd();
            OffsetRange range = new OffsetRange(start, end);
            if (range.containsInclusive(caretOffset) || caretOffset == range.getEnd()+1) { // special case for wrong JRuby offsets
                // Optionally make sure the text is the one we're after such that
                // tests can disambiguate among multiple fixes
                // special case for wrong JRuby offsets
                // Optionally make sure the text is the one we're after such that
                // tests can disambiguate among multiple fixes
                List<HintFix> list = desc.getFixes();
                assertNotNull(list);
                for (HintFix fix : list) {
                    if (text == null ||
                            (substringMatch && fix.getDescription().indexOf(text) != -1) ||
                            (!substringMatch && fix.getDescription().equals(text))) {
                        return fix;
                    }
                }
            }
        }
        
        return null;
    }
    
    protected static class ComputedHints {
        ComputedHints(CompilationInfo info, List<Hint> hints, int caretOffset) {
            this.info = info;
            this.hints = hints;
            this.caretOffset = caretOffset;
        }

        public CompilationInfo info;
        public List<Hint> hints;
        public int caretOffset;
    }
    
}

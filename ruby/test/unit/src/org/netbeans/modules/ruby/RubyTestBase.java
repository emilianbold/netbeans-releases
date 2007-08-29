/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ruby;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.text.Document;
import org.jruby.ast.Node;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.FormattingPreferences;
import org.netbeans.api.gsf.ParseListener;
import org.netbeans.api.gsf.ParserFile;
import org.netbeans.api.gsf.ParserResult;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ruby.lexer.RubyTokenId;
import org.netbeans.modules.ruby.options.CodeStyle;
import org.netbeans.modules.ruby.options.FmtOptions;
import org.netbeans.spi.gsf.DefaultParseListener;
import org.netbeans.spi.gsf.DefaultParserFile;
import org.netbeans.spi.gsf.DefaultParserFile;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbPreferences;

/**
 * @author Tor Norbye
 */
public abstract class RubyTestBase extends NbTestCase {

    public RubyTestBase(String testName) {
        super(testName);
        System.setProperty("ruby.interpreter", FileUtil.toFile(findJRuby().getFileObject("bin/jruby")).getAbsolutePath());
    }

    protected ParserResult parse(FileObject fileObject) {
        RubyParser parser = new RubyParser();
        int caretOffset = -1;

        ParserFile file = new DefaultParserFile(fileObject, null, false);
        String sequence = "";
        ParseListener listener = new DefaultParseListener();
        try {
            DataObject dobj = DataObject.find(fileObject);
            EditorCookie cookie = dobj.getCookie(EditorCookie.class);
            Document doc = cookie.openDocument();
            sequence = doc.getText(0, doc.getLength());
        }
        catch (Exception ex){
            fail(ex.toString());
        }
        ParserResult result = parser.parseBuffer(file, caretOffset, -1, sequence, listener, RubyParser.Sanitize.NEVER);
        return result;
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

    protected Node getRootNode(String relFilePath) {
        FileObject fileObject = getTestFile(relFilePath);
        ParserResult result = parse(fileObject);
        assertNotNull(result);
        RubyParseResult rpr = (RubyParseResult)result;
        Node root = rpr.getRootNode();

        return root;
    }

    protected FileObject findJRuby() {
        File data = getDataDir();
        File nbtree = data.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getParentFile();
        assertNotNull(nbtree);
        assertTrue(nbtree.exists());
        if (!new File(nbtree, "nbbuild").exists()) {
            nbtree = nbtree.getParentFile();
        }
        assertNotNull(nbtree);
        assertTrue(nbtree.exists());
        // TODO - use constants in RubyInstallation
        File jruby = new File(nbtree, "nbbuild" + File.separator + "netbeans" + File.separator + "ruby1" + File.separator + "jruby-1.0.1");
        assertTrue(jruby.exists());
        try {
            jruby = jruby.getCanonicalFile();
        }
        catch (Exception ex){
            fail(ex.toString());
        }
        assertTrue(jruby.exists());
        FileObject fo = FileUtil.toFileObject(jruby);
        assertNotNull(fo);

        return fo;
    }

    // Locate as many Ruby files from the JRuby distribution as possible: libs, gems, etc.
    protected List<FileObject> findJRubyRubyFiles() {
        List<FileObject> l = new ArrayList<FileObject>();
        addRubyFiles(l, findJRuby());

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

    protected String readFile(final FileObject fo) {
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

    protected BaseDocument getDocument(String s) {
        try {
            BaseDocument doc = new BaseDocument(null, false);
            doc.putProperty(org.netbeans.api.lexer.Language.class, RubyTokenId.language());
            doc.putProperty("mimeType", RubyInstallation.RUBY_MIME_TYPE);

            doc.insertString(0, s, null);

            return doc;
        }
        catch (Exception ex){
            fail(ex.toString());
            return null;
        }
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

    public CompilationInfo getInfo(String file) throws Exception {
        FileObject fileObject = getTestFile(file);

        return getInfo(fileObject);
    }

    public CompilationInfo getInfo(FileObject fileObject) throws Exception {
        String text = readFile(fileObject);
        if (text == null) {
            text = "";
        }
        BaseDocument doc = getDocument(text);

        CompilationInfo info = new TestCompilationInfo(fileObject, doc, text);

        return info;
    }
    
    protected static String readFile(File f) throws Exception {
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

    protected Formatter getFormatter(FormattingPreferences preferences) {
        if (preferences == null) {
            preferences = new IndentPrefs(2,2);
        }

        Preferences prefs = NbPreferences.forModule(FormatterTest.class);
        prefs.put(FmtOptions.indentSize, Integer.toString(preferences.getIndentation()));
        prefs.put(FmtOptions.continuationIndentSize, Integer.toString(preferences.getHangingIndentation()));
        CodeStyle codeStyle = CodeStyle.getTestStyle(prefs);
        
        Formatter formatter = new Formatter(codeStyle, 80);
        
        return formatter;
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

            String path = line;
            FileObject f = FileUtil.createData(folder, path);
            assertNotNull(f);
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
}
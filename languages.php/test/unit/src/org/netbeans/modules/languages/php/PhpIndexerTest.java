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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.languages.php;

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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.text.Document;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.Index;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.gsf.api.ParseEvent;
import org.netbeans.modules.gsf.api.ParseListener;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.SourceFileReader;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.lexer.PhpTokenId;
import org.netbeans.napi.gsfret.source.ClasspathInfo;
import org.netbeans.napi.gsfret.source.Source;
import org.netbeans.modules.gsf.spi.DefaultParseListener;
import org.netbeans.modules.gsf.spi.DefaultParserFile;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbPreferences;

/**
 *
 * @author tor
 */
public class PhpIndexerTest extends NbTestCase {
    
    public PhpIndexerTest(String testName) {
        super(testName);
    }            
    
    
    private class TestIndex extends Index {
        private final String localUrl;
        private List<String> documents = new ArrayList<String>();
        private List<String> emptyDocuments = new ArrayList<String>();
        
        public TestIndex(String localUrl) {
            // Leave the end
            int index = localUrl.lastIndexOf('/');
            if (index != -1) {
                localUrl = localUrl.substring(0, index);
            }
            this.localUrl = localUrl;
        }
        
        @Override
        public String toString() {
            Collections.sort(emptyDocuments);
            Collections.sort(documents);
            StringBuilder sb = new StringBuilder();
            int documentNumber = 0;
            for (String s : emptyDocuments) {
                sb.append("\n\nDocument ");
                sb.append(Integer.toString(documentNumber++));
                sb.append("\n");
                sb.append(s);
            }

            for (String s : documents) {
                sb.append("\n\nDocument ");
                sb.append(Integer.toString(documentNumber++));
                sb.append("\n");
                sb.append(s);
            }


            return sb.toString().replace(localUrl, "<TESTURL>");
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
        
        private String prettyPrintValue(String key, String value) {
//            if (value == null) {
                return value;
//            }
//            if (PhpIndexer.FIELD_METHOD_NAME.equals(key)) {
//                // Decode the attributes
//                int attributeIndex = value.indexOf(';');
//                if (attributeIndex != -1) {
//                    int flags = IndexedElement.stringToFlag(value, attributeIndex+1);
//                    if (flags != 0) {
//                        String desc = IndexedMethod.decodeFlags(flags);
//                        value = value.substring(0, attributeIndex) + desc + value.substring(attributeIndex+3);
//                    }
//                }
//            } else if (PhpIndexer.FIELD_CLASS_ATTRS.equals(key)) {
//                // Decode the attributes
//                int flags = IndexedElement.stringToFlag(value, 0);
//                if (flags != 0) {
//                    String desc = IndexedClass.decodeFlags(flags);
//                    value = desc + value.substring(2);
//                } else {
//                    value = "|CLASS|";
//                }
//            } else if (PhpIndexer.FIELD_FIELD_NAME.equals(key)) {
//                // Decode the attributes
//                int attributeIndex = value.indexOf(';');
//                if (attributeIndex != -1) {
//                    int flags = IndexedElement.stringToFlag(value, attributeIndex+1);
//                    if (flags != 0) {
//                        String desc = IndexedField.decodeFlags(flags);
//                        value = value.substring(0, attributeIndex) + desc + value.substring(attributeIndex+3);
//                    }
//                }
//            }
//            
//            return value;
        }

        public void gsfStore(Set<Map<String, String>> fieldToData, Set<Map<String, String>> noIndexData, Map<String, String> toDelete) throws IOException {
            StringBuilder sb = new StringBuilder();
            sb.append("Delete:");
            List<String> keys = new ArrayList<String>(toDelete.keySet());
            Collections.sort(keys);
            for (String key : keys) {
                sb.append("  ");
                sb.append(key);
                sb.append(" : ");
                sb.append(toDelete.get(key));
                sb.append("\n");
            }
            sb.append("\n");

            sb.append("Indexed:");
            sb.append("\n");
            List<String> strings = new ArrayList<String>();
            for (Map<String,String> map : fieldToData) {
                for (String key : map.keySet()) {
                    strings.add(key + " : " + prettyPrintValue(key, map.get(key)));
                }
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
            for (Map<String,String> map : noIndexData) {
                for (String key : map.keySet()) {
                    String value = prettyPrintValue(key, map.get(key));
                    if (value.indexOf(',') != -1) {
                        value = sortCommaList(value);
                    }
                    strings.add(key + " : " + value);
                }
            }
            Collections.sort(strings);
            for (String string : strings) {
                sb.append("  ");
                sb.append(string);
                sb.append("\n");
            }
            
            String s = sb.toString();
            if (fieldToData.size() == 0 && noIndexData.size() == 0) {
                emptyDocuments.add(s);
            } else {
                documents.add(s);
            }
        }

        public void gsfSearch(String primaryField, String name, NameKind kind, Set<SearchScope> scope, Set<SearchResult> result) throws IOException {
            throw new UnsupportedOperationException("Not supported in this test.");
        }
        
    }
    
    
    
    protected ParserResult parse(FileObject fileObject) {
        PhpParser parser = new PhpParser();
        int caretOffset = -1;

        ParserFile file = new DefaultParserFile(fileObject, null, false);
        String sequence = "";
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
        final String source = sequence;
        List<ParserFile> files = Collections.singletonList(file);
        final ParserResult[] resultHolder = new ParserResult[1];
        ParseListener listener = new DefaultParseListener() {

            @Override
            public void finished(ParseEvent e) {
                resultHolder[0] = e.getResult();
            }
            
        };
        SourceFileReader reader = new SourceFileReader() {

            public CharSequence read(ParserFile file) throws IOException {
                return source;
            }

            public int getCaretOffset(ParserFile file) {
                return -1;
            }
        };
        parser.parseFiles(files, listener, reader);
        
        return resultHolder[0];
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

    
    public static BaseDocument createDocument(String s) {
        try {
            BaseDocument doc = new BaseDocument(null, false);
            doc.putProperty(org.netbeans.api.lexer.Language.class, PhpTokenId.language());
            doc.putProperty("mimeType", PhpTokenId.MIME_TYPE);

            doc.insertString(0, s, null);

            return doc;
        }
        catch (Exception ex){
            fail(ex.toString());
            return null;
        }
    }

    public static BaseDocument getDocumentFor(FileObject fo) {
        return createDocument(read(fo));
    }

    protected BaseDocument getDocument(String s) {
        return createDocument(s);
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

    
    class TestCompilationInfo extends CompilationInfo {
        private final String text;
        private Document doc;
        private Source source;
        private ParserResult result;
        private int caretOffset = -1;
        private NbTestCase test;

        public TestCompilationInfo(NbTestCase test, FileObject fileObject, BaseDocument doc, String text) throws IOException {
            super(fileObject);
            this.test = test;
            this.text = text;
            assert text != null;
            this.doc = doc;
            setParser(new PhpParser());
            if (fileObject != null) {
                //source = Source.forFileObject(fileObject);
                ClasspathInfo cpInfo = ClasspathInfo.create(fileObject);
                source = Source.create(cpInfo, Collections.singletonList(fileObject));
            }
        }

        public void setCaretOffset(int caretOffset) {
            this.caretOffset = caretOffset;
        }

        public String getText() {
            return text;
        }

        public Source getSource() {
            return source;
        }

        public Index getIndex() {
            ClasspathInfo cpi = source.getClasspathInfo();
            if (cpi != null) {
                return cpi.getClassIndex();
            }

            return null;
        }

        @Override
        public Document getDocument() throws IOException {
            return this.doc;
        }

        @Override
        public ParserResult getParserResult() {
            ParserResult r = super.getParserResult();
            if (r == null) {
                r = result;
            }
            if (r == null) {
                final ParserResult[] resultHolder = new ParserResult[1];

                List<ParserFile> sourceFiles = new ArrayList<ParserFile>(1);
                ParserFile file = new DefaultParserFile(getFileObject(), null, false);
                sourceFiles.add(file);

                //setPositionManager(parser.getPositionManager());
                //ParserResult pr = parser.parseBuffer(context, RubyParser.Sanitize.NONE);

                List<ParserFile> files = Collections.singletonList(file);
                ParseListener listener = new DefaultParseListener() {

                    @Override
                    public void finished(ParseEvent e) {
                        resultHolder[0] = e.getResult();
                    }

                };
                SourceFileReader reader = new SourceFileReader() {

                    public CharSequence read(ParserFile file) throws IOException {
                        return text;
                    }

                    public int getCaretOffset(ParserFile file) {
                        return caretOffset;
                    }
                };
                PhpParser parser = new PhpParser();
                parser.parseFiles(files, listener, reader);


                r = result = resultHolder[0];
            }

            return r;
        }
    }
    
    protected FileObject getTestFile(String relFilePath) {
        File dataDir = getDataDir();
        if (dataDir.getPath().indexOf("/build/") != -1) {
            dataDir = new File(dataDir.getPath().replace("/build/", "/"));
        }
        File wholeInputFile = new File(dataDir, relFilePath);
        if (!wholeInputFile.exists()) {
            NbTestCase.fail("File " + wholeInputFile + " not found.");
        }
        FileObject fo = FileUtil.toFileObject(wholeInputFile);
        assertNotNull(fo);

        return fo;
    }
    
    protected TestCompilationInfo getInfo(String file) throws Exception {
        FileObject fileObject = getTestFile(file);
        return getInfo(fileObject);
    }

    protected TestCompilationInfo getInfo(FileObject fileObject) throws Exception {
        String text = readFile(fileObject);
        if (text == null) {
            text = "";
        }
        BaseDocument doc = getDocument(text);

        TestCompilationInfo info = new TestCompilationInfo(this, fileObject, doc, text);

        return info;
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

    protected void assertDescriptionMatches(String relFilePath,
            String description, boolean includeTestName, String ext) throws Exception {
        File PhpFile = getDataFile(relFilePath);
        if (!PhpFile.exists()) {
            NbTestCase.fail("File " + PhpFile + " not found.");
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
        File PhpFile = getDataFile(relFilePath);
        if (!PhpFile.exists()) {
            NbTestCase.fail("File " + PhpFile + " not found.");
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
    
    
    
    private void checkIndexer(String relFilePath) throws Exception {
        CompilationInfo info = getInfo(relFilePath);
        PhpParseResult rpr = (PhpParseResult) info.getParserResult();

        File PhpFile = new File(getDataDir(), relFilePath);
        Index index = new TestIndex(PhpFile.toURI().toURL().toExternalForm());
        PhpIndexer indexer = new PhpIndexer();
        //PhpIndex.setClusterUrl("file:/bogus"); // No translation
        indexer.updateIndex(index, rpr);
        
        String annotatedSource = index.toString();

        assertDescriptionMatches(relFilePath, annotatedSource, false, ".indexed");
    }
    
    public void testAnalysis2() throws Exception {
        checkIndexer("testfiles/statements.php");
    }
}

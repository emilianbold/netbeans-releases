/*
 * RhtmlModelTest.java
 * JUnit 4.x based test
 *
 * Created on June 8, 2007, 1:20 PM
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
import org.netbeans.modules.ruby.RubyParseResult;
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

        RhtmlModel model = new RhtmlModel(null, null, null);

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

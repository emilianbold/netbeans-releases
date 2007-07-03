/*
 * FormattingTest.java
 *
 * Created on February 23, 2007, 4:30 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.ruby;

import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.api.gsf.GsfTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.ruby.lexer.RubyTokenId;
import org.openide.filesystems.FileObject;

/**
 * Lex all files in the ruby distribution to make sure there are no problems
 * 
 * @author Tor Norbye
 */
public class RubyLexAllTest extends RubyTestBase {
    public RubyLexAllTest(String testName) {
        super(testName);
    }

    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }
    
    public void testLexAll() throws BadLocationException {
        // Find ruby files
        List<FileObject> files = findJRubyRubyFiles();
        assertTrue(files.size() > 0);

        // indent each one
        for (FileObject fo : files) {
            //System.out.println("Formatting file " /*+ count*/ + " : " + FileUtil.getFileDisplayName(fo));
            
            // check that we end up at indentation level 0
            BaseDocument doc = getDocument(fo);
            
            String text = doc.getText(0, doc.getLength());
            TokenHierarchy hi = TokenHierarchy.create(text, RubyTokenId.language());
            @SuppressWarnings("unchecked")
            TokenSequence<?extends GsfTokenId> ts = hi.tokenSequence();
            // Just iterate through the sequence to make sure it's okay - this throws an exception because of bug 93990
            while (ts.moveNext()) {
                ;
            }
        }
    }
}

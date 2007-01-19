/*
 * SyntaxHighlightingTest.java
 *
 * Created on January 18, 2007, 9:47 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.spi.editor.highlighting.performance;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.File;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.SimpleAttributeSet;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.lib2.highlighting.SyntaxHighlighting;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;

/**
 *
 * @author vita
 */
public class SyntaxHighlightingTest extends NbTestCase {
    
    /** Creates a new instance of SyntaxHighlightingTest */
    public SyntaxHighlightingTest(String name) {
        super(name);
    }

    private Document document;
    
    protected void setUp() {
        // Create and load document
        Document d = new PlainDocument();
        try {
            File dataFolder = new File(getClass().getResource("data").toURI());
            InputStream io = new FileInputStream(new File(dataFolder, "VeryBigClassThatWasStolenAndDoesNotCompile-java"));
            try {
                byte [] buffer = new byte [1024];
                int size;

                while (0 < (size = io.read(buffer, 0, buffer.length))) {
                    String s = new String(buffer, 0, size);
                    d.insertString(d.getLength(), s, SimpleAttributeSet.EMPTY);
                }
            } finally {
                io.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        
        Language<JavaTokenId> javaLang = JavaTokenId.language();
        d.putProperty(Language.class, javaLang);
        d.putProperty("mimeType", javaLang.mimeType());
        
        
        this.document = d;
    }
    
    public void testLexer() {
        long timestamp1, timestamp2;
        
        timestamp1 = System.currentTimeMillis();
        TokenHierarchy th = TokenHierarchy.get(document);
        timestamp2 = System.currentTimeMillis();
        System.out.println("TokenHierarchy.get(document) took " + (timestamp2 - timestamp1) + " msecs.");
        
        timestamp1 = System.currentTimeMillis();
        TokenSequence ts = th.tokenSequence();
        timestamp2 = System.currentTimeMillis();
        System.out.println("TokenHierarchy.tokenSequence() took " + (timestamp2 - timestamp1) + " msecs.");

        timestamp1 = System.currentTimeMillis();
        iterateOver(ts);
        timestamp2 = System.currentTimeMillis();
        System.out.println("Iterating through TokenSequence took " + (timestamp2 - timestamp1) + " msecs.");
        
        timestamp1 = System.currentTimeMillis();
        SyntaxHighlighting layer = new SyntaxHighlighting(document);
        timestamp2 = System.currentTimeMillis();
        System.out.println("SyntaxHighlighting creation took " + (timestamp2 - timestamp1) + " msecs.");
        
        timestamp1 = System.currentTimeMillis();
        HighlightsSequence hs = layer.getHighlights(0, Integer.MAX_VALUE);
        timestamp2 = System.currentTimeMillis();
        System.out.println("SyntaxHighlighting.getHighlights() took " + (timestamp2 - timestamp1) + " msecs.");

        timestamp1 = System.currentTimeMillis();
        iterateOver(hs);
        timestamp2 = System.currentTimeMillis();
        System.out.println("Iterating through HighlightsSequence took " + (timestamp2 - timestamp1) + " msecs.");
    }
    
    private void iterateOver(TokenSequence ts) {
        for( ; ts.moveNext(); ) {
            String name = ts.token().id().name();
            assertNotNull("Token name must not be null", name);
            
            TokenSequence embedded = ts.embedded();
            if (embedded != null) {
                iterateOver(embedded);
            }
        }
    }

    private void iterateOver(HighlightsSequence hs) {
        for( ; hs.moveNext(); ) {
            AttributeSet attribs = hs.getAttributes();
            assertNotNull("AttributeSet must not be null", attribs);
        }
    }

}

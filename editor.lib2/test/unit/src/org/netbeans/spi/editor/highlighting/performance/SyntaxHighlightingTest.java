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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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

    public static void main(String [] args) {
        junit.textui.TestRunner.run(SyntaxHighlightingTest.class);
    }

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
        lexerWarmUp();
        highlightingCheck();
    }
    
    private void lexerWarmUp() {
        long timestamp1, timestamp2;
        for(int i = 0; i < 5; i++) {
            timestamp1 = System.currentTimeMillis();
            TokenHierarchy th = TokenHierarchy.get(document);
            timestamp2 = System.currentTimeMillis();
            System.out.println(i + ". TokenHierarchy.get(document) took " + (timestamp2 - timestamp1) + " msecs.");

            timestamp1 = System.currentTimeMillis();
            TokenSequence ts = th.tokenSequence();
            timestamp2 = System.currentTimeMillis();
            System.out.println(i + ". TokenHierarchy.tokenSequence() took " + (timestamp2 - timestamp1) + " msecs.");

            timestamp1 = System.currentTimeMillis();
            iterateOver(ts, null, null);
            timestamp2 = System.currentTimeMillis();
            System.out.println(i + ". Iterating through TokenSequence took " + (timestamp2 - timestamp1) + " msecs.");
        }
    }
    
    private void highlightingCheck() {
        long timestamp1, timestamp2;
        for(int i = 0; i < 5; i++) {
            timestamp1 = System.currentTimeMillis();
            SyntaxHighlighting layer = new SyntaxHighlighting(document);
            timestamp2 = System.currentTimeMillis();
            System.out.println(i + ". SyntaxHighlighting creation took " + (timestamp2 - timestamp1) + " msecs.");

            timestamp1 = System.currentTimeMillis();
            HighlightsSequence hs = layer.getHighlights(0, Integer.MAX_VALUE);
            timestamp2 = System.currentTimeMillis();
            System.out.println(i + ". SyntaxHighlighting.getHighlights() took " + (timestamp2 - timestamp1) + " msecs.");

            timestamp1 = System.currentTimeMillis();
            iterateOver(hs);
            timestamp2 = System.currentTimeMillis();
            System.out.println(i + ". Iterating through HighlightsSequence took " + (timestamp2 - timestamp1) + " msecs.");
        }
    }
    
    private void iterateOver(TokenSequence ts, HashMap<String, Integer> distro, HashMap<String, Integer> flyweightDistro) {
        for( ; ts.moveNext(); ) {
            String name = ts.token().id().name();
            assertNotNull("Token name must not be null", name);
            
            if (distro != null) {
                String tokenId = ts.languagePath().mimePath() + ":" + name;
                {
                    Integer freq = distro.get(tokenId);
                    if (freq == null) {
                        freq = 1;
                    } else {
                        freq++;
                    }
                    distro.put(tokenId, freq);
                }
                
                if (ts.token().isFlyweight()) {
                    Integer freq = flyweightDistro.get(tokenId);
                    if (freq == null) {
                        freq = 1;
                    } else {
                        freq++;
                    }
                    flyweightDistro.put(tokenId, freq);
                }
                
//                if (ts.token().id() == JavadocTokenId.IDENT) {
//                    System.out.println("Javadoc IDENT: '" + ts.token().text() + "'");
//                }
//                if (ts.token().id() == JavadocTokenId.OTHER_TEXT) {
//                    System.out.println("Javadoc OTHER_TEXT: '" + ts.token().text() + "'");
//                }
            }
            
            TokenSequence embedded = ts.embedded();
            if (embedded != null) {
                iterateOver(embedded, distro, flyweightDistro);
            }
        }
    }

    private void iterateOver(HighlightsSequence hs) {
        for( ; hs.moveNext(); ) {
            AttributeSet attribs = hs.getAttributes();
            assertNotNull("AttributeSet must not be null", attribs);
        }
    }

    public void testLexerEmbedding() {
        long timestamp1, timestamp2;
        TokenHierarchy th = TokenHierarchy.get(document);
        
        TokenSequence embeddedSeq = null;
        TokenSequence ts = th.tokenSequence();
        for( ; ts.moveNext(); ) {
            String name = ts.token().id().name();
            assertNotNull("Token name must not be null", name);
            
            timestamp1 = System.currentTimeMillis();
            embeddedSeq = ts.embedded();
            timestamp2 = System.currentTimeMillis();
            if (embeddedSeq != null) {
                System.out.println("First TS.embedded() took " + (timestamp2 - timestamp1) + " msecs.");
                // found embedded
                break;
            }
        }
        
        assertNotNull("Can't find embedded sequence", embeddedSeq);
        
        timestamp1 = System.currentTimeMillis();
        TokenSequence embeddedSeq2 = ts.embedded();
        timestamp2 = System.currentTimeMillis();
        System.out.println("Second TS.embedded() took " + (timestamp2 - timestamp1) + " msecs.");
        
        assertNotNull("Second call to TS.embedded() produced null", embeddedSeq2);
    }

    public void testDistro() {
        TokenHierarchy th = TokenHierarchy.get(document);
        TokenSequence ts = th.tokenSequence();
        HashMap<String, Integer> distro = new HashMap<String, Integer>();
        HashMap<String, Integer> flyweightDistro = new HashMap<String, Integer>();
        iterateOver(ts, distro, flyweightDistro);
        
        {
            long totalTokens = 0;
            
            System.out.println("\nAll tokens sorted by names:");
            ArrayList<String> names = new ArrayList<String>(distro.keySet());
            Collections.sort(names);
            for(String tokenId : names) {
                Integer freq = distro.get(tokenId);
                System.out.println(tokenId + " -> " + freq);
                totalTokens += freq;
            }
            System.out.println("    Total tokens count: " + totalTokens);

            totalTokens = 0;
            System.out.println("\nAll tokens sorted by freq:");
            ArrayList<Map.Entry<String, Integer>> entries = new ArrayList<Map.Entry<String, Integer>>(distro.entrySet());
            Collections.sort(entries, new FreqCmp());
            for(Map.Entry<String, Integer> entry : entries) {
                System.out.println(entry.getKey() + " -> " + entry.getValue());
                totalTokens += entry.getValue();
            }
            System.out.println("    Total tokens count: " + totalTokens);
        }
        
        {
            long totalFlyweightTokens = 0;
            
            System.out.println("\nFlyweight tokens sorted by names:");
            ArrayList<String> names = new ArrayList<String>(flyweightDistro.keySet());
            Collections.sort(names);
            for(String tokenId : names) {
                Integer freq = flyweightDistro.get(tokenId);
                System.out.println(tokenId + " -> " + freq);
                totalFlyweightTokens += freq;
            }
            System.out.println("    Total tokens count: " + totalFlyweightTokens);

            
            totalFlyweightTokens = 0;
            System.out.println("\nFlyweight tokens sorted by freq:");
            ArrayList<Map.Entry<String, Integer>> entries = new ArrayList<Map.Entry<String, Integer>>(flyweightDistro.entrySet());
            Collections.sort(entries, new FreqCmp());
            for(Map.Entry<String, Integer> entry : entries) {
                System.out.println(entry.getKey() + " -> " + entry.getValue());
                totalFlyweightTokens += entry.getValue();
            }
            System.out.println("    Total flyweight tokens count: " + totalFlyweightTokens);
        }

        System.out.println("\nChecking flywights:");
        for(String tokenId : flyweightDistro.keySet()) {
            Integer freq = distro.get(tokenId);
            Integer flyweightFreq = flyweightDistro.get(tokenId);
            assertNotNull("No freq for " + tokenId, freq);
            assertNotNull("No flyweightDistro freq for " + tokenId, flyweightFreq);
            if (freq.intValue() != flyweightFreq.intValue()) {
                System.out.println(tokenId + " : freq = " + freq + " is different from flyweightFreq = " + flyweightFreq);
            }
        }
        System.out.println("Check done!");
    }
    
    private static final class FreqCmp implements Comparator<Map.Entry<String, Integer>> {
        public int compare(Entry<String, Integer> e1,
                           Entry<String, Integer> e2
        ) {
            Integer freq1 = e1.getValue();
            Integer freq2 = e2.getValue();
            int f1 = freq1 == null ? 0 : freq1.intValue();
            int f2 = freq2 == null ? 0 : freq2.intValue();
            return f1 - f2;
        }
    }
}

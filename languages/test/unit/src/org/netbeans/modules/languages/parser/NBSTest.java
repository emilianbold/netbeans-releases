/*
 * NBSTest.java
 *
 * Created on September 1, 2006, 2:46 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.languages.parser;

import org.netbeans.api.languages.ParseException;
import org.netbeans.modules.languages.parser.TokenInput;
import org.netbeans.api.languages.ASTNode;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.NBSLanguageReader;


/**
 *
 * @author Jan Jancura
 */
public class NBSTest extends TestCase {
    
    public NBSTest (String testName) {
        super (testName);
    }
    
    public void testFirst () {
        InputStream is = getClass ().getClassLoader ().getResourceAsStream ("org/netbeans/modules/languages/resources/NBS.nbs");
        try {
            Language l = NBSLanguageReader.readLanguage ("test", is, "test/x-nbs");
            List r = l.getAnalyser ().getRules ();
//            AnalyserAnalyser.printRules (r, null);
//            AnalyserAnalyser.printUndefinedNTs (r, null);
            Map f = Petra.first2 (r);
//            AnalyserAnalyser.printDepth (f, null);
//            AnalyserAnalyser.printConflicts (f, null);
//            AnalyserAnalyser.printF (f, null);
            assertFalse (AnalyserAnalyser.hasConflicts (f));
        } catch (ParseException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public void test2 () throws ParseException, IOException {
        InputStream is = getClass ().getClassLoader ().getResourceAsStream ("org/netbeans/modules/languages/resources/NBS.nbs");
        Language l = NBSLanguageReader.readLanguage ("test", is, "test/x-nbs");

        is = getClass ().getClassLoader ().getResourceAsStream ("org/netbeans/modules/languages/resources/NBS.nbs");
        BufferedReader br = new BufferedReader (new InputStreamReader (is));
        StringBuilder sb = new StringBuilder ();
        String ln = br.readLine ();
        while (ln != null) {
            sb.append (ln).append ('\n');
            ln = br.readLine ();
        }
        TokenInput ti = TokenInput.create (
            l.getParser (), 
            new StringInput (sb.toString (), "NBS.nbs"),
            l.getSkipTokenTypes ()
        );
        ASTNode n = l.getAnalyser ().read (ti, false);
        assertNotNull (n);
    }
}

/*
 * NBSTest.java
 *
 * Created on September 1, 2006, 2:46 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.languages.javascript;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;

import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.TokenInput;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.NBSLanguageReader;
import org.netbeans.modules.languages.parser.AnalyserAnalyser;
import org.netbeans.modules.languages.parser.Petra;
import org.netbeans.modules.languages.parser.StringInput;


/**
 *
 * @author Jan Jancura
 */
public class JavaScriptTest extends TestCase {
    
    public JavaScriptTest (String testName) {
        super (testName);
    }
    
    public void testConficts () {
        InputStream is = getClass ().getClassLoader ().getResourceAsStream ("org/netbeans/modules/languages/javascript/JavaScript.nbs");
        try {
            Language l = NBSLanguageReader.readLanguage ("test", is, "test/mimeType");
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
    
    public void testTokens () throws ParseException, IOException {
        InputStream is = getClass ().getClassLoader ().getResourceAsStream ("org/netbeans/modules/languages/javascript/JavaScript.nbs");
        Language l = NBSLanguageReader.readLanguage ("test", is, "test/x-nbs");

        is = getClass ().getClassLoader ().getResourceAsStream ("org/netbeans/modules/languages/javascript/tokens.js");
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
        assertEquals ("identifier", ti.read ().getType ());
        assertEquals ("keyword", ti.read ().getType ());
        assertEquals ("operator", ti.read ().getType ());
        assertEquals ("string", ti.read ().getType ());
        assertEquals ("operator", ti.read ().getType ());
        assertEquals ("string", ti.read ().getType ());
        assertEquals ("operator", ti.read ().getType ());
        assertEquals ("string", ti.read ().getType ());
        assertEquals ("operator", ti.read ().getType ());
        assertEquals ("string", ti.read ().getType ());
        assertEquals ("operator", ti.read ().getType ());
        assertEquals ("string", ti.read ().getType ());
        assertEquals ("operator", ti.read ().getType ());
        assertEquals ("string", ti.read ().getType ());
        assertEquals ("separator", ti.read ().getType ());
        assertTrue (ti.eof ());
    }
}

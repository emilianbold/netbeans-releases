/*
 * LLAnalyserTest.java
 * JUnit based test
 *
 * Created on March 26, 2006, 9:57 AM
 */

package org.netbeans.modules.languages.parser;

import java.io.IOException;
import java.io.InputStream;
import junit.framework.TestCase;

import org.netbeans.api.languages.ParseException;
import org.netbeans.modules.languages.LanguageImpl;
import org.netbeans.modules.languages.NBSLanguageReader;
import org.netbeans.modules.languages.TestUtils;
import org.openide.util.Exceptions;


/**
 *
 * @author Jan Jancura
 */
public class Grammar1 extends TestCase {
    
    public Grammar1 (String testName) {
        super (testName);
    }

    public void test1 () throws ParseException {
        InputStream is = getClass ().getClassLoader ().getResourceAsStream ("org/netbeans/modules/languages/parser/Grammar1.nbs");
        try {
            NBSLanguageReader reader = NBSLanguageReader.create (is, "Grammar1.nbs", "test/mimeType");
            LanguageImpl l = TestUtils.createLanguage (reader);
            l.read ();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
    }
}

/*
 * LanguageTest.java
 * JUnit based test
 *
 * Created on March 19, 2007, 9:26 AM
 */

package org.netbeans.modules.languages;

import org.netbeans.api.languages.ParseException;

/**
 *
 * @author Jan Jancura
 */
public class TestUtils {

    public static LanguageImpl createLanguage (String nbsText) throws ParseException {
        return createLanguage (NBSLanguageReader.create (nbsText, "test.nbs", "test/test"));
    }

    public static LanguageImpl createLanguage (NBSLanguageReader reader) throws ParseException {
        return new LanguageImpl ("test/test", reader);
    }
}





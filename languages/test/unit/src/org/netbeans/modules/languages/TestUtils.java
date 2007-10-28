/*
 * LanguageTest.java
 * JUnit based test
 *
 * Created on March 19, 2007, 9:26 AM
 */

package org.netbeans.modules.languages;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.languages.ParseException;
import org.netbeans.modules.languages.parser.LLSyntaxAnalyser;
import org.netbeans.modules.languages.parser.Parser;


/**
 *
 * @author Jan Jancura
 */
public class TestUtils {

    public static Language createLanguage (String nbsText) throws ParseException {
        return createLanguage (NBSLanguageReader.create (nbsText, "test.nbs", "test/test"));
    }

    public static Language createLanguage (NBSLanguageReader reader) throws ParseException {
        List<TokenType> tokenTypes = reader.getTokenTypes ();
        Map<Integer,String> tokensMap = new HashMap<Integer,String> ();
        Iterator<TokenType> it = tokenTypes.iterator ();
        while (it.hasNext()) {
            TokenType tokenType = it.next ();
            tokensMap.put (tokenType.getTypeID (), tokenType.getType ());
        }
        Language language = Language.create ("test/test", tokensMap, Collections.<Feature>emptyList (), Parser.create (tokenTypes));
        List<Feature> features = reader.getFeatures ();
        List<Rule> rules = reader.getRules (language);
        Set<Integer> skipTokenIDs = new HashSet<Integer> ();
        Iterator<Feature> it2 = features.iterator ();
        while (it2.hasNext ()) {
            Feature feature = it2.next ();
            if (feature.getFeatureName ().equals ("SKIP")) {
                skipTokenIDs.add (language.getTokenID (feature.getSelector ().toString ()));
            }
        }
        LLSyntaxAnalyser analyser = LLSyntaxAnalyser.create (language, rules, skipTokenIDs);
        language.setAnalyser (analyser);
        return language;
    }
}





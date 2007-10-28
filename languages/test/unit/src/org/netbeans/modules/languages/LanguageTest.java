/*
 * LanguageTest.java
 * JUnit based test
 *
 * Created on March 19, 2007, 9:26 AM
 */

package org.netbeans.modules.languages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.ParseException;
import org.netbeans.modules.languages.TokenType;
import org.netbeans.modules.languages.parser.Parser;
import org.netbeans.modules.languages.parser.Parser.Cookie;
import org.netbeans.modules.languages.parser.Pattern;
import org.netbeans.modules.languages.parser.StringInput;


/**
 *
 * @author Jan Jancura
 */
public class LanguageTest extends TestCase {
    
    public LanguageTest(String testName) {
        super(testName);
    }
    
    public void testFeatures1 () {
        Feature feature = Feature.create ("feature", Selector.create ("selector"));
        Language language = Language.create (
            "text/test",
            Collections.<Integer,String>emptyMap (),
            Collections.<Feature>singletonList (feature),
            null
        );
        assertEquals ("selector", language.getFeature ("feature").getSelector ().getAsString ());
        assertEquals (1, language.getFeatures ("feature").size ());
    }
    
    public void testFeatures2 () {
        List<Feature> features = new ArrayList<Feature> ();
        features.add (Feature.create ("feature", Selector.create ("selector")));
        features.add (Feature.create ("feature", Selector.create ("selector2")));
        Language language = Language.create (
            "text/test",
            Collections.<Integer,String>emptyMap (),
            features,
            null
        );
        assertEquals (2, language.getFeatures ("feature").size ());
        try {
            language.getFeature ("feature");
            assert (false);
        } catch (IllegalArgumentException e) {}
        
    }
    
    public void testFeatures3 () {
        List<Feature> features = new ArrayList<Feature> ();
        features.add (Feature.create ("feature", Selector.create ("a")));
        features.add (Feature.create ("feature", Selector.create ("a.b")));
        features.add (Feature.create ("feature", Selector.create ("c.a")));
        Map<Integer,String> tokensMap = new HashMap<Integer,String> ();
        tokensMap.put (0, "a");
        tokensMap.put (1, "b");
        tokensMap.put (2, "c");
        Language language = Language.create (
            "text/test",
            tokensMap,
            features,
            null
        );
        assertEquals ("a", language.getFeature ("feature", "a").getSelector ().getAsString ());
        assertNull (language.getFeature ("feature", "b"));
        assertNull (language.getFeature ("feature", "c"));
        
        ASTNode n = ASTNode.create (language, "x", Arrays.asList (new ASTItem[] {
            ASTNode.create (language, "a", Arrays.asList (new ASTItem[] {
                ASTToken.create (language, "b", "bbb", 0, 3, null),
                ASTToken.create (language, "a", "aaa", 3, 3, null),
            }), 0),
            ASTNode.create (language, "c", Arrays.asList (new ASTItem[] {
                ASTToken.create (language, "b", "bbb", 6, 3, null),
                ASTToken.create (language, "a", "aaa", 9, 3, null),
            }), 6)
        }), 0);
        List<Feature> fs = language.getFeatures ("feature", n.findPath (1));
        assertEquals (1, fs.size ());
        assertEquals ("a.b", fs.get (0).getSelector ().getAsString ());
        fs = language.getFeatures ("feature", n.findPath (4));
        assertEquals (1, fs.size ());
        assertEquals ("a", fs.get (0).getSelector ().getAsString ());
        fs = language.getFeatures ("feature", n.findPath (7));
        assertEquals (0, fs.size ());
        fs = language.getFeatures ("feature", n.findPath (10));
        assertEquals (2, fs.size ());
        Set<String> s = new HashSet<String> ();
        s.add (fs.get (0).getSelector ().getAsString ());
        s.add (fs.get (1).getSelector ().getAsString ());
        assertTrue (s.contains ("a"));
        assertTrue (s.contains ("c.a"));
    }
    
    public void testTokens1 () throws ParseException {
        List<TokenType> tokenTypes = new ArrayList<TokenType> ();
        tokenTypes.add (new TokenType (null, Pattern.create ("['0'-'9']+"), "jedna", 1, "number", 0, Feature.create ("cislo", Selector.create ("a"))));
        tokenTypes.add (new TokenType ("number", Pattern.create ("['a'-'z']+"), "dve", 2, "number and character", 1, Feature.create ("cislo a pismeno", Selector.create ("b"))));
        tokenTypes.add (new TokenType ("number and character", Pattern.create ("'$'"), "tri", 3, null, 2, Feature.create ("prachy", Selector.create ("c"))));
        tokenTypes.add (new TokenType (null, Pattern.create ("['A'-'Z']+"), "ctyri", 4, "big character", 3, Feature.create ("velke pismeno", Selector.create ("d"))));
        tokenTypes.add (new TokenType ("big character", Pattern.create ("'+'"), "pet", 5, null, 4, Feature.create ("plus", Selector.create ("e"))));
        Map<Integer,String> tokensMap = new HashMap<Integer,String> ();
        tokensMap.put (1, "jedna");
        tokensMap.put (2, "dve");
        tokensMap.put (3, "tri");
        tokensMap.put (4, "ctyri");
        tokensMap.put (5, "pet");
        Language language = Language.create ("text/test", tokensMap, Collections.<Feature>emptyList (), Parser.create (tokenTypes));
        assertEquals (5, language.getParser ().getTokenTypes ().size ());
        Parser p = language.getParser ();
        MyCookie cookie = new MyCookie ();
        StringInput input = new StringInput ("10090aas$AAQ+");
        ASTToken t = p.read (cookie, input, language);
        assertEquals ("jedna", t.getTypeName ());
        assertEquals ("10090", t.getIdentifier ());
        assertEquals (cookie.getState (), p.getState ("number"));
        t = p.read (cookie, input, language);
        assertEquals ("dve", t.getTypeName ());
        assertEquals ("aas", t.getIdentifier ());
        assertEquals (cookie.getState (), p.getState ("number and character"));
        t = p.read (cookie, input, language);
        assertEquals ("tri", t.getTypeName ());
        assertEquals ("$", t.getIdentifier ());
        assertEquals (cookie.getState (), -1);
        t = p.read (cookie, input, language);
        assertEquals ("ctyri", t.getTypeName ());
        assertEquals ("AAQ", t.getIdentifier ());
        assertEquals (cookie.getState (), p.getState ("big character"));
        t = p.read (cookie, input, language);
        assertEquals ("pet", t.getTypeName ());
        assertEquals ("+", t.getIdentifier ());
        assertEquals (cookie.getState (), -1);
        assertTrue (input.eof ());
    }
    
    private static class MyCookie implements Cookie {

        private int state = -1;
        
        public int getState() {
            return state;
        }

        public void setState (int state) {
            this.state = state;
        }

        public void setProperties(Feature tokenProperties) {
        }
    }
}





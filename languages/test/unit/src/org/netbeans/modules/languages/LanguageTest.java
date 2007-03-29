/*
 * LanguageTest.java
 * JUnit based test
 *
 * Created on March 19, 2007, 9:26 AM
 */

package org.netbeans.modules.languages;

import java.util.Arrays;
import java.util.List;
import junit.framework.TestCase;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.ParseException;
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
        Language l = new Language ("text/test");
        l.addFeature (Feature.create("feature", Selector.create ("selector")));
        assertEquals ("selector", l.getFeature ("feature").getSelector ().getAsString ());
        assertEquals (1, l.getFeatures ("feature").size ());
    }
    
    public void testFeatures2 () {
        Language l = new Language ("text/test");
        l.addFeature (Feature.create("feature", Selector.create ("selector")));
        l.addFeature (Feature.create("feature", Selector.create ("selector2")));
        assertEquals (2, l.getFeatures ("feature").size ());
        try {
            l.getFeature ("feature");
            assert (false);
        } catch (IllegalArgumentException e) {}
        
    }
    
    public void testFeatures3 () {
        Language l = new Language ("text/test");
        l.addFeature (Feature.create("feature", Selector.create ("a")));
        l.addFeature (Feature.create("feature", Selector.create ("a.b")));
        l.addFeature (Feature.create("feature", Selector.create ("c.a")));
        assertEquals ("a", l.getFeature ("feature", "a").getSelector ().getAsString ());
        assertNull (l.getFeature ("feature", "b"));
        assertNull (l.getFeature ("feature", "c"));
        
        ASTNode n = ASTNode.create ("mt", "x", Arrays.asList (new ASTItem[] {
            ASTNode.create ("mt", "a", Arrays.asList (new ASTItem[] {
                ASTToken.create ("mt", "b", "bbb", 0),
                ASTToken.create ("mt", "a", "aaa", 3),
            }), 0),
            ASTNode.create ("mt", "c", Arrays.asList (new ASTItem[] {
                ASTToken.create ("mt", "b", "bbb", 6),
                ASTToken.create ("mt", "a", "aaa", 9),
            }), 6)
        }), 0);
        List<Feature> fs = l.getFeatures ("feature", n.findPath (1));
        assertEquals (1, fs.size ());
        assertEquals ("a.b", fs.get (0).getSelector ().getAsString ());
        fs = l.getFeatures ("feature", n.findPath (4));
        assertEquals (1, fs.size ());
        assertEquals ("a", fs.get (0).getSelector ().getAsString ());
        fs = l.getFeatures ("feature", n.findPath (7));
        assertEquals (0, fs.size ());
        fs = l.getFeatures ("feature", n.findPath (10));
        assertEquals (1, fs.size ());
        assertEquals ("c.a", fs.get (0).getSelector ().getAsString ());
    }
    
    public void testTokens1 () throws ParseException {
        Language l = new Language ("text/test");
        l.addToken (null, "jedna", Pattern.create ("['0'-'9']+"), "number", Feature.create ("cislo", Selector.create ("a")));
        l.addToken ("number", "dve", Pattern.create ("['a'-'z']+"), "number and character", Feature.create ("cislo a pismeno", Selector.create ("b")));
        l.addToken ("number and character", "tri", Pattern.create ("'$'"), null, Feature.create ("prachy", Selector.create ("c")));
        l.addToken (null, "ctyri", Pattern.create ("['A'-'Z']+"), "big character", Feature.create ("velke pismeno", Selector.create ("d")));
        l.addToken ("big character", "pet", Pattern.create ("'+'"), null, Feature.create ("plus", Selector.create ("e")));
        assertEquals (5, l.getTokenTypes ().size ());
        Parser p = l.getParser ();
        MyCookie cookie = new MyCookie ();
        StringInput input = new StringInput ("10090aas$AAQ+");
        ASTToken t = p.read (cookie, input, l.getMimeType ());
        assertEquals ("jedna", t.getType ());
        assertEquals ("10090", t.getIdentifier ());
        assertEquals (cookie.getState (), p.getState ("number"));
        t = p.read (cookie, input, l.getMimeType ());
        assertEquals ("dve", t.getType ());
        assertEquals ("aas", t.getIdentifier ());
        assertEquals (cookie.getState (), p.getState ("number and character"));
        t = p.read (cookie, input, l.getMimeType ());
        assertEquals ("tri", t.getType ());
        assertEquals ("$", t.getIdentifier ());
        assertEquals (cookie.getState (), -1);
        t = p.read (cookie, input, l.getMimeType ());
        assertEquals ("ctyri", t.getType ());
        assertEquals ("AAQ", t.getIdentifier ());
        assertEquals (cookie.getState (), p.getState ("big character"));
        t = p.read (cookie, input, l.getMimeType ());
        assertEquals ("pet", t.getType ());
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





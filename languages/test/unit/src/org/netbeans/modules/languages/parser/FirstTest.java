/*
 * PetraTest.java
 * JUnit based test
 *
 * Created on August 18, 2006, 8:32 AM
 */

package org.netbeans.modules.languages.parser;

import org.netbeans.api.languages.ASTToken;
import junit.framework.TestCase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.languages.ParseException;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.Rule;


/**
 *
 * @author Jan Jancura
 */
public class FirstTest extends TestCase {
    
    public FirstTest (String testName) {
        super (testName);
    }

    
    public void testFirst1 () throws ParseException {
        Map<Integer,String> tokensMap = new HashMap<Integer,String> ();
        tokensMap.put (0, "a");
        tokensMap.put (1, "b");
        tokensMap.put (2, "c");
        Language language = Language.create ("test/test", tokensMap, Collections.<Feature>emptyList (), null);
        List<Rule> rules = new ArrayList<Rule> ();
        rules.add (Rule.create ("S", new ArrayList (Arrays.asList (new Object[] {
            "A", 
            ASTToken.create (language, "a", null, 0, 0, null),
            ASTToken.create (language, "b", null, 0, 0, null)
        }))));
        rules.add (Rule.create ("A", new ArrayList (Arrays.asList (new Object[] {
        }))));
        rules.add (Rule.create ("A", new ArrayList (Arrays.asList (new Object[] {
            ASTToken.create (language, "a", null, 0, 0, null),
            ASTToken.create (language, "c", null, 0, 0, null)
        }))));
        First first = First.create (rules, language);
        System.out.println(first);
        assertEquals (1, first.getRule (
            language.getNTID ("A"), 
            TokenInputUtils.create (new ASTToken[] {
                ASTToken.create (language, 0, "a", 0),
                ASTToken.create (language, 1, "b", 0)
            }),
            Collections.<Integer>emptySet ()
        ));
        assertEquals (2, first.getRule (
            language.getNTID ("A"), 
            TokenInputUtils.create (new ASTToken[] {
                ASTToken.create (language, 0, "a", 0),
                ASTToken.create (language, 2, "c", 0)
            }),
            Collections.<Integer>emptySet ()
        ));
    }
    
    public void testFirst2 () throws ParseException {
        Map<Integer,String> tokensMap = new HashMap<Integer,String> ();
        tokensMap.put (0, "a");
        tokensMap.put (1, "b");
        tokensMap.put (2, "c");
        tokensMap.put (3, "d");
        Language language = Language.create ("test/test", tokensMap, Collections.<Feature>emptyList (), null);
        List<Rule> rules = new ArrayList<Rule> ();
        rules.add (Rule.create ("S", new ArrayList (Arrays.asList (new Object[] {
            "A", 
            ASTToken.create (language, "b", null, 0, 0, null),
            ASTToken.create (language, "c", null, 0, 0, null)
        }))));
        rules.add (Rule.create ("S", new ArrayList (Arrays.asList (new Object[] {
            ASTToken.create (language, "b", null, 0, 0, null),
            "A", 
            ASTToken.create (language, "c", null, 0, 0, null)
        }))));
        rules.add (Rule.create ("S", new ArrayList (Arrays.asList (new Object[] {
            "B",
            ASTToken.create (language, "c", null, 0, 0, null)
        }))));
        rules.add (Rule.create ("S", new ArrayList (Arrays.asList (new Object[] {
            ASTToken.create (language, "b", null, 0, 0, null),
            ASTToken.create (language, "d", null, 0, 0, null),
            ASTToken.create (language, "a", null, 0, 0, null)
        }))));
        rules.add (Rule.create ("A", new ArrayList (Arrays.asList (new Object[] {
            ASTToken.create (language, "d", null, 0, 0, null)
        }))));
        rules.add (Rule.create ("B", new ArrayList (Arrays.asList (new Object[] {
            ASTToken.create (language, "d", null, 0, 0, null)
        }))));
        First first = First.create (rules, language);
        
        assertEquals (3, first.getRule (
            0,
            TokenInputUtils.create (new ASTToken[] {
                ASTToken.create (language, 1, "b", 0),
                ASTToken.create (language, 3, "d", 0),
                ASTToken.create (language, 0, "a", 0)
            }),
            Collections.<Integer>emptySet ()
        ));
        assertEquals (1, first.getRule (
            0, 
            TokenInputUtils.create(new ASTToken[] {
                ASTToken.create (language, 1, "b", 0),
                ASTToken.create (language, 3, "d", 0),
                ASTToken.create (language, 2, "c", 0)
            }),
            Collections.<Integer>emptySet ()
        ));
        assertEquals (0, first.getRule (
            0, 
            TokenInputUtils.create (new ASTToken[] {
                ASTToken.create (language, 3, "d", 0),
                ASTToken.create (language, 1, "b", 0)
            }),
            Collections.<Integer>emptySet ()
        ));
        assertEquals (2, first.getRule (
            0, 
            TokenInputUtils.create (new ASTToken[] {
                ASTToken.create (language, 3, "d", 0),
                ASTToken.create (language, 2, "c", 0)
            }),
            Collections.<Integer>emptySet ()
        ));
        assertEquals (-2, first.getRule (
            0, 
            TokenInputUtils.create (new ASTToken[] {
                ASTToken.create (language, 3, "d", 0)
            }),
            Collections.<Integer>emptySet ()
        ));
        assertEquals (-2, first.getRule (
            0, 
            TokenInputUtils.create(new ASTToken[] {
                ASTToken.create (language, 1, "b", 0),
                ASTToken.create (language, 1, "b", 0)
            }),
            Collections.<Integer>emptySet ()
        ));
        System.out.println(first);
    }
}

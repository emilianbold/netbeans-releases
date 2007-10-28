/*
 * LanguageTest.java
 * JUnit based test
 *
 * Created on March 19, 2007, 9:26 AM
 */

package org.netbeans.api.languages;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;


/**
 *
 * @author Jan Jancura
 */
public class ASTTest extends TestCase {
    
    public ASTTest (String testName) {
        super (testName);
    }
    
    public void testAST1 () {
        Map<Integer,String> tokensMap = new HashMap<Integer, String> ();
        tokensMap.put (0, "a");
        tokensMap.put (1, "b");
        Language language = Language.create ("test/test", tokensMap, Collections.<Feature>emptyList (), null);
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
        assertEquals (2, n.getChildren ().size ());
        assertEquals ("x", n.getNT ());
        assertEquals ("test/test", n.getMimeType ());
        assertEquals ("bbbaaabbbaaa", n.getAsText ());
        ASTPath path = n.findPath (3);
        assertEquals (3, path.size ());
        ASTToken t = (ASTToken) path.getLeaf ();
        assertEquals ("a", t.getTypeName ());
        assertEquals ("test/test", t.getMimeType ());
        assertEquals ("aaa", t.getIdentifier ());
        assertEquals (6, t.getEndOffset ());
        n = (ASTNode) path.getRoot ();
        assertEquals (n, path.get (0));
        assertEquals (2, n.getChildren ().size ());
        assertEquals ("x", n.getNT ());
        assertEquals ("test/test", n.getMimeType ());
        assertEquals ("bbbaaabbbaaa", n.getAsText ());
        n = (ASTNode) path.get (1);
        assertEquals (2, n.getChildren ().size ());
        assertEquals ("a", n.getNT ());
        assertEquals ("test/test", n.getMimeType ());
        assertEquals ("bbbaaa", n.getAsText ());
    }
}





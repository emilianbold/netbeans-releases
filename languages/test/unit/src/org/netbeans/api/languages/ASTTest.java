/*
 * LanguageTest.java
 * JUnit based test
 *
 * Created on March 19, 2007, 9:26 AM
 */

package org.netbeans.api.languages;

import org.netbeans.api.languages.ASTPath;
import org.netbeans.modules.languages.*;
import java.util.Arrays;
import junit.framework.TestCase;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTToken;


/**
 *
 * @author Jan Jancura
 */
public class ASTTest extends TestCase {
    
    public ASTTest(String testName) {
        super(testName);
    }
    
    public void testAST1 () {
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
        assertEquals (2, n.getChildren ().size ());
        assertEquals ("x", n.getNT ());
        assertEquals ("mt", n.getMimeType ());
        assertEquals ("bbbaaabbbaaa", n.getAsText ());
        ASTPath path = n.findPath (3);
        assertEquals (3, path.size ());
        ASTToken t = (ASTToken) path.getLeaf ();
        assertEquals ("a", t.getType ());
        assertEquals ("mt", t.getMimeType ());
        assertEquals ("aaa", t.getIdentifier ());
        assertEquals (6, t.getEndOffset ());
        n = (ASTNode) path.getRoot ();
        assertEquals (n, path.get (0));
        assertEquals (2, n.getChildren ().size ());
        assertEquals ("x", n.getNT ());
        assertEquals ("mt", n.getMimeType ());
        assertEquals ("bbbaaabbbaaa", n.getAsText ());
        n = (ASTNode) path.get (1);
        assertEquals (2, n.getChildren ().size ());
        assertEquals ("a", n.getNT ());
        assertEquals ("mt", n.getMimeType ());
        assertEquals ("bbbaaa", n.getAsText ());
    }
}





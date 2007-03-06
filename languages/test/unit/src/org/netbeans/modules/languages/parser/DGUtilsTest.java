/*
 * DGUtilsTest.java
 * JUnit based test
 *
 * Created on February 1, 2006, 2:18 PM
 */

package org.netbeans.modules.languages.parser;

import java.util.Collections;
import junit.framework.*;


/**
 *
 * @author Jan Jancura
 */
public class DGUtilsTest extends TestCase {
    
    public DGUtilsTest (String testName) {
        super (testName);
    }


    public void testReduce () {
        DG dg = DG.createDG (new Integer (1));
        dg.addNode (new Integer (2));
        dg.addNode (new Integer (3));
        dg.addNode (new Integer (4));
        dg.addEdge (new Integer (1), new Integer (2), new Character ('a'));
        dg.addEdge (new Integer (2), new Integer (3), new Character ('b'));
        dg.addEdge (new Integer (3), new Integer (4), new Character ('a'));
        dg.addEdge (new Integer (4), new Integer (3), new Character ('b'));
        dg.setStart (new Integer (1));
        dg.setEnds (Collections.singleton (new Integer (3)));
        
        dg.setProperty (new Integer (1), "jedna", null);
        dg.setProperty (new Integer (2), "dve", null);
        dg.setProperty (new Integer (3), "tri", null);
        dg.setProperty (new Integer (4), "ctyri", null);
        dg.setProperty (new Integer (1), new Character ('a'), "jedna a", null);
        dg.setProperty (new Integer (2), new Character ('b'), "dve b", null);
        dg.setProperty (new Integer (3), new Character ('a'), "tri a", null);
        dg.setProperty (new Integer (4), new Character ('b'), "ctyri b", null);
        dg = DGUtils.reduce (dg, nodeFactory);
        
        assertEquals (new Integer (1), dg.getStartNode ());
        assertEquals (Collections.singleton (new Integer (3)), dg.getEnds ());
        assertEquals (3, dg.getNodes ().size ());
        
    }
    
    private NodeFactory nodeFactory = new NodeFactory<Integer> () {
        public Integer createNode() {
            return Integer.valueOf (counter++);
        }

        private int counter = 1;
    };
}

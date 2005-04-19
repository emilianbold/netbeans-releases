/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.swing.tabcontrol.plaf;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.awt.event.*;
import junit.framework.*;
import org.netbeans.swing.tabcontrol.DefaultTabDataModel;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabDataModel;
import org.netbeans.swing.tabcontrol.event.*;
import org.netbeans.swing.tabcontrol.plaf.*;

/** Tests for all of the functionality of TabLayoutModel instances
 *
 * @author  Tim Boudreau
 */
public class LayoutModelTest extends TestCase {
    DefaultTabDataModel mdl=null;
    DefaultTabSelectionModel sel = null;
    TestLayoutModel lay = null;

    public LayoutModelTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(LayoutModelTest.class);
        return suite;
    }
    
    public void setUp() {
        prepareModel();
    }
    
    public static void main(String[] args) {
        new LayoutModelTest("foo").run();
    }    
    
    Icon ic = new Icon () {
        public int getIconWidth() {
            return 16;
        }
        public int getIconHeight() {
            return 16;
        }
        public void paintIcon (Component c, Graphics g, int x, int y) {
            //do nothing
        }
    };
    
    Icon sameSizeIcon = new Icon () {
        public int getIconWidth() {
            return 16;
        }
        public int getIconHeight() {
            return 16;
        }
        public void paintIcon (Component c, Graphics g, int x, int y) {
            //do nothing
        }
    };
    
    Icon biggerIcon = new Icon () {
        public int getIconWidth() {
            return 22;
        }
        public int getIconHeight() {
            return 22;
        }
        public void paintIcon (Component c, Graphics g, int x, int y) {
            //do nothing
        }
    };    
    
    /** Weird, but this class was adapted from a standalone test written
     * long ago and rescued from cvs history.  It didn't use JUnit, and 
     * the assertTrue argument order was reversed.  So in the interest of 
     * laziness... */
    private void assertPravda (boolean val, String msg) {
        assertTrue (msg, val);
    }
    
    int padX;
    int padY;
    private void prepareModel() {
        TabData[] td = new TabData[25];
        int ct = 0;
        for (char c='a'; c < 'z'; c++) {
            char[] ch = new char[ct+1];
            Arrays.fill (ch, c);
            String name = new String (ch);
            Component comp = new JLabel(name);
            comp.setName (name);
            td[ct] = new TabData (comp, ic, name, "tip:"+name);
            ct++;
        }
        padX = 2;
        padY = 2;
        mdl = new DefaultTabDataModel (td);
        JLabel jl = new JLabel();
        jl.setBorder (BorderFactory.createEmptyBorder());
        lay = new TestLayoutModel (mdl, jl);
        lay.setPadding (new Dimension(padX, padY));
    }
    
    /*
    public void run() {
        testSizes();
        testRemoval();
        System.err.println("All tests passed for layout model");
    }
     */
    
    public void testSizes() {
        System.err.println("testSizes");
        int pos=0;
        for (int i=0; i < mdl.size(); i++) {
            int expectedSize = ic.getIconWidth() + i + padX + 1;
            assertPravda (lay.getW(i) == expectedSize, "Width of " + (i+1) + " - "
            + mdl.getTab(i).getText() + " should be " + expectedSize + " but is " 
            + lay.getW(i));
            assertPravda (pos == lay.getX(i), "X at " +  i + " should be " + pos + " but is " + lay.getX(i));
            pos += lay.getW(i);
        }
    }
    
    public void testRemoval() {
        System.err.println("testRemoval");
        mdl.removeTab (0);
        int expectedSize = ic.getIconWidth() + 2 + padX;
        assertPravda (lay.getW(0) == expectedSize, "Removed item at 0, new 0 item not correct size");
    }
    

    /** A default model subclass that uses character count for width for testing   */
    class TestLayoutModel extends BaseTabLayoutModel {
        public TestLayoutModel(TabDataModel model, JComponent target) {
            super (model, new JLabel()); 
        }        
        
        protected int textWidth (int index) {
            return model.getTab (index).getText().length();
        }
        
        protected int textHeight (int index) {
            return 16;
        }
    }
    
}

/*
 * $Id$
 *
 * ---------------------------------------------------------------------------
 *
 * Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version 1.0
 * (the "License"). You may not use this file except in compliance with the
 * License. A copy of the License is available at http://www.sun.com/.
 *
 * The Original Code is the Jemmy library. The Initial Developer of the
 * Original Code is Alexandre Iline. All Rights Reserved.
 *
 * ---------------------------------------------------------------------------
 *
 * Contributor(s): Manfred Riem (mriem@netbeans.org).
 */
package org.netbeans.jemmy.operators;

import java.awt.Color;
import java.util.Hashtable;
import javax.swing.Icon;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ColorChooserUI;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for JColorChooserOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class JColorChooserOperatorTest extends TestCase {
    /**
     * Stores the frame we use for testing.
     */
    private JFrame frame;
    
    /**
     * Stores the color chooser we use for testing.
     */
    private JColorChooser colorChooser;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public JColorChooserOperatorTest(String testName) {
        super(testName);
    }
    
    /**
     * Setup for testing.
     */
    protected void setUp() throws Exception {
        frame = new JFrame();
        colorChooser = new JColorChooser();
        colorChooser.setName("JColorChooserOperatorTest");
        frame.add(colorChooser);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }
    
    /**
     * Cleanup after testing.
     */
    protected void tearDown() throws Exception {
        frame.setVisible(false);
        frame.dispose();
        frame = null;
    }
    
    /**
     * Suite method.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(JColorChooserOperatorTest.class);
        
        return suite;
    }

    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JColorChooserOperator operator2 = new JColorChooserOperator(operator1);
        assertNotNull(operator2);

        JColorChooserOperator operator3 = new JColorChooserOperator(operator1, new NameComponentChooser("JColorChooserOperatorTest"));
        assertNotNull(operator3);
    }
    
    /**
     * Test findJColorChooser method.
     */
    public void testFindJColorChooser() {
        frame.setVisible(true);
        
        JColorChooser colorChooser = JColorChooserOperator.findJColorChooser(frame);
        assertNotNull(colorChooser);

        JColorChooser colorChooser2 = JColorChooserOperator.findJColorChooser(frame, new NameComponentChooser("JColorChooserOperatorTest"));
        assertNotNull(colorChooser2);
    }
    
    /**
     * Test waitJColorChooser method.
     */
    public void testWaitJColorChooser() {
        frame.setVisible(true);
        
        JColorChooser colorChooser = JColorChooserOperator.waitJColorChooser(frame);
        assertNotNull(colorChooser);

        JColorChooser colorChooser2 = JColorChooserOperator.waitJColorChooser(frame, new NameComponentChooser("JColorChooserOperatorTest"));
        assertNotNull(colorChooser2);
    }
    
    /**
     * Test setOutput method.
     */
    public void testSetOutput() {
    }
    
    /**
     * Test getOutput method.
     */
    public void testGetOutput() {
    }
    
    /**
     * Test enterRed method.
     *
     * @todo review the enterRed method, unclear what the API contract 
     *       really is.
     */
    public void testEnterRed() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JColorChooserOperator operator2 = new JColorChooserOperator(operator1);
        assertNotNull(operator2);
        
        operator2.enterColor(0);
        operator2.enterRed(255);
        
        JColorChooserOperator operator3 = new JColorChooserOperator(operator1);
        assertEquals(operator3.getColor().getRed(), colorChooser.getColor().getRed());
        /*
        assertEquals(Color.RED, colorChooser.getColor());
         */
    }
    
    /**
     * Test enterGreen method.
     *
     * @todo review the enterGreen method, unclear what the API contract 
     *       really is.
     */
    public void testEnterGreen() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JColorChooserOperator operator2 = new JColorChooserOperator(operator1);
        assertNotNull(operator2);
        
        operator2.enterColor(0);
        operator2.enterGreen(255);
        
        JColorChooserOperator operator3 = new JColorChooserOperator(operator1);
        assertEquals(operator3.getColor().getGreen(), colorChooser.getColor().getGreen());
        /*
        assertEquals(Color.GREEN, colorChooser.getColor());
         */
    }
    
    /**
     * Test enterBlue method.
     *
     * @todo review the enterBlue method, unclear what the API contract 
     *       really is.
     */
    public void testEnterBlue() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JColorChooserOperator operator2 = new JColorChooserOperator(operator1);
        assertNotNull(operator2);

        operator2.enterColor(0);
        operator2.enterBlue(255);

        JColorChooserOperator operator3 = new JColorChooserOperator(operator1);
        assertEquals(operator3.getColor(), colorChooser.getColor());
        /*
        assertEquals(Color.BLUE, colorChooser.getColor());
         */
    }
    
    /**
     * Test enterColor method.
     *
     * @todo review the enterColor method, unclear what the API contract 
     *       really is.
     */
    public void testEnterColor() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JColorChooserOperator operator2 = new JColorChooserOperator(operator1);
        assertNotNull(operator2);

        operator2.enterColor(Color.GREEN);

        JColorChooserOperator operator3 = new JColorChooserOperator(operator1);
        assertEquals(operator3.getColor(), colorChooser.getColor());
        /*
        assertEquals(Color.GREEN, colorChooser.getColor());
         */

        operator3.enterColor(0, 0, 0);

        JColorChooserOperator operator4 = new JColorChooserOperator(operator1);
        assertEquals(operator4.getColor(), colorChooser.getColor());
        /*
        assertEquals(Color.BLACK, colorChooser.getColor());
         */
    }
    
    /**
     * Test getDump method.
     */
    public void testGetDump() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JColorChooserOperator operator2 = new JColorChooserOperator(operator1);
        assertNotNull(operator2);
        
        operator2.setColor(Color.CYAN);
        Hashtable hashtable = operator2.getDump();
        assertEquals(Color.CYAN.toString(), hashtable.get(JColorChooserOperator.COLOR_DPROP));
    }
    
    /**
     * Test addChooserPanel method.
     */
    public void testAddChooserPanel() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JColorChooserOperator operator2 = new JColorChooserOperator(operator1);
        assertNotNull(operator2);
        
        ColorChooserTestPanel panel = new ColorChooserTestPanel();
        operator2.addChooserPanel(panel);
        assertEquals(operator2.getChooserPanels().length, 4);
        assertEquals(colorChooser.getChooserPanels().length, 4);
        
        operator2.removeChooserPanel(panel);
        assertEquals(operator2.getChooserPanels().length, 3);
        assertEquals(colorChooser.getChooserPanels().length, 3);

        AbstractColorChooserPanel[] panels = new AbstractColorChooserPanel[1];
        panels[0] = panel;
        operator2.setChooserPanels(panels);
        assertEquals(operator2.getChooserPanels().length, 1);
        assertEquals(colorChooser.getChooserPanels().length, 1);
    }
    
    /**
     * An inner class used for testing.
     */
    public class ColorChooserTestPanel extends AbstractColorChooserPanel {
        public void updateChooser() {
        }

        protected void buildChooser() {
        }

        public String getDisplayName() {
            return "";
        }

        public Icon getSmallDisplayIcon() {
            return null;
        }

        public Icon getLargeDisplayIcon() {
            return null;
        }
    }
    
    /**
     * Test getColor method.
     */
    public void testGetColor() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JColorChooserOperator operator2 = new JColorChooserOperator(operator1);
        assertNotNull(operator2);
        
        operator2.setColor(Color.GREEN);
        
        JColorChooserOperator operator3 = new JColorChooserOperator(operator1);
        assertEquals(operator3.getColor(), colorChooser.getColor());
        assertEquals(Color.GREEN, colorChooser.getColor());

        operator2.setColor(0);
        
        JColorChooserOperator operator4 = new JColorChooserOperator(operator1);
        assertEquals(operator4.getColor(), colorChooser.getColor());
        assertEquals(Color.BLACK, colorChooser.getColor());

        operator2.setColor(255, 255, 255);
        
        JColorChooserOperator operator5 = new JColorChooserOperator(operator1);
        assertEquals(operator5.getColor(), colorChooser.getColor());
        assertEquals(Color.WHITE, colorChooser.getColor());
    }
    
    /**
     * Test getPreviewPanel method.
     */
    public void testGetPreviewPanel() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JColorChooserOperator operator2 = new JColorChooserOperator(operator1);
        assertNotNull(operator2);
        
        JPanel panel = new JPanel();
        operator2.setPreviewPanel(panel);
        assertEquals(operator2.getPreviewPanel(), colorChooser.getPreviewPanel());
        assertEquals(panel, colorChooser.getPreviewPanel());
    }
    
    /**
     * Test getSelectionModel method.
     */
    public void testGetSelectionModel() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JColorChooserOperator operator2 = new JColorChooserOperator(operator1);
        assertNotNull(operator2);
        
        ColorSelectionModelTest selectionModel = new ColorSelectionModelTest();
        operator2.setSelectionModel(selectionModel);
        assertEquals(operator2.getSelectionModel(), colorChooser.getSelectionModel());
        assertEquals(selectionModel, colorChooser.getSelectionModel());
    }
    
    /**
     * An inner class used for testing.
     */
    public class ColorSelectionModelTest implements ColorSelectionModel {
        public Color getSelectedColor() {
            return Color.BLACK;
        }

        public void setSelectedColor(Color color) {
        }

        public void addChangeListener(ChangeListener listener) {
        }

        public void removeChangeListener(ChangeListener listener) {
        }
    }
    
    /**
     * Test getUI method.
     */
    public void testGetUI() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JColorChooserOperator operator2 = new JColorChooserOperator(operator1);
        assertNotNull(operator2);
        
        ColorChooserUITest colorChooserUI = new ColorChooserUITest();
        operator2.setUI(colorChooserUI);
        assertEquals(operator2.getUI(), colorChooser.getUI());
        assertEquals(colorChooserUI, colorChooser.getUI());
    }
    
    /**
     * Inner class needed for testing.
     */
    public class ColorChooserUITest extends ColorChooserUI {
    }
}

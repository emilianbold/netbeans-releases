/*
 * AquaSlidingButtonUI.java
 *
 * Created on May 28, 2004, 7:45 AM
 */

package org.netbeans.swing.tabcontrol.plaf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;
import org.netbeans.swing.tabcontrol.SlidingButtonUI;
import org.netbeans.swing.tabcontrol.plaf.GenericGlowingChiclet;

//import org.netbeans.swing.tabcontrol.plaf.GenericGlowingChiclet;

/**
 *
 * @author  mkleint
 */
public class AquaSlidingButtonUI extends SlidingButtonUI {
    
    private static AquaSlidingButtonUI AQUA_INSTANCE = null;
    
    static Color[] rollover = new Color[]{
        new Color(222, 222, 227), new Color(220, 238, 255), new Color(190, 247, 255),
        new Color(205, 205, 205)};
    
    
    /** Creates a new instance of AquaSlidingButtonUI */
    private AquaSlidingButtonUI() {
    }
    
    
    /** Aqua ui for sliding buttons.  This class is public so it can be
     * instantiated by UIManager, but is of no interest as API. */
    public static ComponentUI createUI(JComponent c) {
        if (AQUA_INSTANCE == null) {
            AQUA_INSTANCE = new AquaSlidingButtonUI();
        }
        return AQUA_INSTANCE;
    }
    
    protected void installBorder(AbstractButton b) {
        b.setBorder(BorderFactory.createEmptyBorder(5,2,2,2));    
    }
    
    
   protected void paintBackground(Graphics2D graph, AbstractButton b) {
        GenericGlowingChiclet chic = GenericGlowingChiclet.INSTANCE;
        chic.setState(0);
        chic.setArcs(0.5f, 0.5f, 0.5f, 0.5f);
        chic.setBounds(0, 1, b.getWidth() - 2, b.getHeight() - 2);
        chic.setAllowVertical(true);
        chic.draw(graph);
        chic.setAllowVertical(false);
    }    
    
    protected void paintButtonPressed(Graphics graph, AbstractButton b) {
        GenericGlowingChiclet chic = GenericGlowingChiclet.INSTANCE;
        int state = 0;
//        if (b.getModel().isPressed()) {
//            chic.setColors(selectedPressedActive[0], selectedPressedActive[1], selectedPressedActive[2], selectedPressedActive[3]);
//        } else {
//            chic.setColors(selectedActive[0], selectedActive[1], selectedActive[2], selectedActive[3]);
//        }
        state |= b.getModel().isSelected() ? chic.STATE_SELECTED : 0;
        state |= b.getModel().isPressed() ? chic.STATE_PRESSED : 0;
        state |= b.getModel().isRollover() ? chic.STATE_ACTIVE : 0;
//        System.out.println("state=" + state);
        if (state != chic.STATE_ACTIVE) {
            chic.setState(state);
        } else {
            // now we are only active (rollover).. have special colors..
            chic.setColors(rollover[0], rollover[1], rollover[2], rollover[3]);
        }
        chic.setArcs(0.5f, 0.5f, 0.5f, 0.5f);
        chic.setBounds(0, 1, b.getWidth() - 2, b.getHeight() - 2);
        chic.setAllowVertical(true);
        chic.draw((Graphics2D)graph);
        chic.setAllowVertical(false);
    }
    
   // ********************************
    //          Layout Methods
    // ********************************
    public Dimension getPreferredSize(JComponent c) {
	Dimension d = super.getPreferredSize(c);

        AbstractButton b = (AbstractButton)c;
        d.width += 5;
	d.height += 5; 
	return d;
    } 

    
}

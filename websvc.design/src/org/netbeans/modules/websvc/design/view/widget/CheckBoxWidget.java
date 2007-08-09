/*
 * ToggleButtonWidget.java
 *
 * Created on April 18, 2007, 2:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.design.view.widget;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.Scene;

/**
 *
 * @author Ajit
 */
public class CheckBoxWidget extends ButtonWidget {
    
    /** The expand button image. */
    private static final Image IMAGE_CHECKBOX = new BufferedImage(8, 8,
            BufferedImage.TYPE_INT_ARGB);
    /** The collapse button image. */
    private static final Image IMAGE_CHECKBOX_SELECTED = new BufferedImage(8, 8,
            BufferedImage.TYPE_INT_ARGB);
    private boolean isSelected;

    public static final String ACTION_COMMAND_SELECTED = "toggle-button-selected";
    public static final String ACTION_COMMAND_DESELECTED = "toggle-button-deselected";

    static {

        // Create the checkbox image.
        Graphics2D g2 = ((BufferedImage) IMAGE_CHECKBOX).createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        float w = IMAGE_CHECKBOX.getWidth(null);
        float h = IMAGE_CHECKBOX.getHeight(null);
        Rectangle2D gp = new Rectangle2D.Double(0,0,w,h);
        g2.setPaint(Color.WHITE);
        g2.fill(gp);
        g2.setPaint(Color.GRAY);
        g2.draw(gp);

        // Create the checkbox selected image.
        g2 = ((BufferedImage) IMAGE_CHECKBOX_SELECTED).createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        w = IMAGE_CHECKBOX_SELECTED.getWidth(null);
        h = IMAGE_CHECKBOX_SELECTED.getHeight(null);
        gp = new Rectangle2D.Double(0,0,w,h);
        g2.setPaint(Color.WHITE);
        g2.fill(gp);
        g2.setPaint(Color.GRAY);
        g2.draw(gp);
        gp = new Rectangle2D.Double(1.5,1.5,w-3,h-3);
        g2.fill(gp);
    }

    /**
     *
     * @param scene
     * @param text
     */
    public CheckBoxWidget(Scene scene, String text) {
        super(scene, IMAGE_CHECKBOX, text);
        setBorder(BorderFactory.createEmptyBorder(1));
    }
    
    
    public boolean isSelected() {
        return isSelected;
    }
    
    public void setSelected(boolean flag) {
        if(isSelected!=flag) {
            isSelected=flag;
            setImage(isSelected ? IMAGE_CHECKBOX_SELECTED : IMAGE_CHECKBOX);
        }
    }
    
    public void performAction() {
        setSelected(!isSelected());
        super.performAction();
    }
    
    public String getActionCommand() {
        return isSelected()?ACTION_COMMAND_SELECTED:ACTION_COMMAND_DESELECTED;
    }

    @Override
    protected void notifyStateChanged(ObjectState previousState, ObjectState state) {
        if (previousState.isFocused() != state.isFocused()) {
            setBorder(state.isFocused()?BorderFactory.createDashedBorder
                    (BORDER_COLOR, 2, 2, true):BorderFactory.createEmptyBorder(1));
        }
        super.notifyStateChanged(previousState,state);
    }
    
}

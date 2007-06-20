/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.design.view.widget;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Scene;

/**
 * @author Ajit Bhate
 */
public class ButtonWidget extends AbstractMouseActionsWidget {
    
    private ImageLabelWidget button;
    private Action action;
    private Insets margin = new Insets(2, 2, 2, 2);
    
    /**
     *
     * @param scene
     * @param text
     */
    public ButtonWidget(Scene scene, String text) {
        this(scene, null, text);
    }
    
    
    /**
     *
     * @param scene
     * @param icon
     */
    public ButtonWidget(Scene scene, Image image) {
        this(scene, image, null);
    }
    
    
    /**
     *
     * @param scene
     * @param icon
     * @param text
     */
    public ButtonWidget(Scene scene, Image image, String text) {
        this(scene, new ImageLabelWidget(scene,image,text));
    }
    
    
    /**
     *
     * @param scene
     * @param action
     */
    public ButtonWidget(Scene scene, Action action) {
        this(scene, createImageLabelWidget(scene,action));
        this.action = action;
    }
    
    
    /**
     *
     * @param scene
     * @param button
     */
    private ButtonWidget(Scene scene, ImageLabelWidget button) {
        super(scene);
        this.button = button;
        addChild(button);
        setBorder(new ButtonBorder(this,margin));
        button.setBorder(BorderFactory.createEmptyBorder(4));
        setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.CENTER, 4));
        getActions().addAction(ButtonAction.DEFAULT);
    }
    
    
    protected Insets getMargin() {
        return margin;
    }
    
    /**
     *
     * @return
     */
    public ImageLabelWidget getButton() {
        return button;
    }
    
    /**
     *
     * @param action
     */
    public void setAction(Action action) {
        this.action = action;
    }
    
    /**
     *
     * @return
     */
    public String getText() {
        return getButton().getLabel();
    }
    
    
    /**
     *
     * @return
     */
    public Image getIcon() {
        return getButton().getImage();
    }
    
    
    /**
     *
     * @param text
     */
    public void setText(String text) {
        getButton().setLabel(text);
    }
    
    
    /**
     *
     * @param image
     */
    public void setImage(Image image) {
        getButton().setImage(image);
    }
    
    /**
     * Changed method name so that it doesnt clash with Widget.setEnabled.
     * @param v
     */
    public void setButtonEnabled(boolean v) {
        getButton().setEnabled(v);
        getButton().setPaintAsDisabled(!v);
        revalidate();
        repaint();
    }
    
    /*
     *
     */
    /**
     * Changed method name so that it doesnt clash with Widget.isEnabled.
     * @return
     */
    public boolean isButtonEnabled() {
        return getButton().isEnabled();
    }
    
    /**
     * Called when mouse is clicked on the widget.
     */
    public void mouseClicked() {
        super.mouseClicked();
        //simply delegate to swing action
        if(isButtonEnabled() && action!=null) {
            action.actionPerformed(new ActionEvent(this,0, getActionCommand()));
            //validate scene as called from ActionListeners
            getScene().validate();
        }
    }
    
    public String getActionCommand() {
        return (String)action.getValue(Action.ACTION_COMMAND_KEY);
    }
    
    private static ImageLabelWidget createImageLabelWidget(Scene scene, Action action) {
        String label = (String)action.getValue(Action.NAME);
        Object icon = action.getValue(Action.SMALL_ICON);
        Image image = icon instanceof ImageIcon ? ((ImageIcon)icon).getImage(): null;
        return new ImageLabelWidget(scene,image,label);
    }
    
    protected static class ButtonBorder implements Border, MouseActions {
        private boolean rollover = false;
        private boolean pressed = false;
        private ButtonWidget button;
        private Insets insets;
        
        public ButtonBorder(ButtonWidget button, Insets insets) {
            this.button = button;
            this.insets = insets;
        }
        
        public Insets getInsets() {
            return insets;
        }
        
        public void paint(Graphics2D g2, Rectangle rect) {
            Paint oldPaint = g2.getPaint();
            
            RoundRectangle2D buttonRect = new RoundRectangle2D.Double
                    (rect.x+0.5, rect.y+0.5, rect.width-1, rect.height-1, 6, 6);
            if (button.isButtonEnabled()) {
                
                if (pressed) {
                    g2.setPaint(new Color(0xCCCCCC));
                    g2.fill(buttonRect);
                } else if (button.isOpaque()){
                    g2.setPaint(new GradientPaint(
                            0, rect.y , BACKGROUND_COLOR_1,
                            0, rect.y + rect.height * 0.5f,
                            BACKGROUND_COLOR_2, true));
                    g2.fill(buttonRect);
                }
                
                g2.setPaint(BORDER_COLOR);
                if (rollover) {
                    g2.setPaint(BORDER_COLOR);
                    g2.draw(new RoundRectangle2D.Double(rect.x + 1.5, rect.y + 1.5,
                            rect.width - 3, rect.height - 3, 3, 3));
                }
                g2.draw(buttonRect);
            } else {
                if(button.isOpaque()) {
                    g2.setPaint(BACKGROUND_COLOR_DISABLED);
                    g2.fill(buttonRect);
                    
                }
                g2.setPaint(grayFilter(BORDER_COLOR));
                g2.draw(buttonRect);
            }
            
            g2.setPaint(oldPaint);
        }
        
        public boolean isOpaque() {
            return false;
        }
        
        protected boolean isPressed() {
            return pressed;
        }
        
        protected  void setPressed(boolean flag) {
            pressed = flag;
        }
        
        protected  void setRolledOver(boolean flag) {
            rollover = flag;
        }
        
        public void mousePressed() {
            setPressed(true);
        }
        
        public void mouseReleased() {
            setPressed(false);
        }
        
        public void mouseEntered() {
            setRolledOver(true);
        }
        
        public void mouseExited() {
            setRolledOver(false);
        }
        
        public void mouseClicked() {
        }
        
    }
    
    
    private static Color grayFilter(Color color) {
        int y = Math.round(0.299f * color.getRed()
                + 0.587f * color.getGreen()
                + 0.114f * color.getBlue());
        
        if (y < 0) {
            y = 0;
        } else if (y > 255) {
            y = 255;
        }
        
        return new Color(y, y, y);
    }
    
    private static final Color BORDER_COLOR = new Color(0x7F9DB9);
    private static final Color BACKGROUND_COLOR_1 = new Color(0xD2D2DD);
    private static final Color BACKGROUND_COLOR_2 = new Color(0xF8F8F8);
    private static final Color BACKGROUND_COLOR_DISABLED = new Color(0xE4E4E4);
    private static final Color ENABLED_TEXT_COLOR = new Color(0x222222);
    private static final Color DISABLED_TEXT_COLOR = new Color(0x888888);
}

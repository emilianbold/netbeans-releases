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

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author anjeleevich
 */
public class ButtonWidget extends Widget implements FocusableWidget {

    private ImageWidget imageWidget = null;
    private LabelWidget labelWidget = null;
    
    
    private Insets margin = new Insets(2, 8, 2, 8);
    
    
    private boolean rollover = false;
    private boolean pressed = false;
    private boolean enabled = true;
    private boolean focused = false;
    private boolean parentSelectionAllowed = false;
    
    
    private ActionListener actionListener = null;
    private boolean focusable;

    
    public ButtonWidget(Scene scene, String text) {
        this(scene, text, false);
    }
    
    public ButtonWidget(Scene scene, String text, boolean focusable) {
        this(scene, null, text, focusable);
    }
    
    public ButtonWidget(Scene scene, Image icon) {
        this(scene, icon, null, false);
    }
    
    public ButtonWidget(Scene scene, Image icon, boolean focusable) {
        this(scene, icon, null, focusable);
    }
    
    
    public ButtonWidget(Scene scene, Image icon, String text, boolean focusable) {
        super(scene);
        this.focusable = focusable;
        setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory
                .SerialAlignment.CENTER, 4));
        setIconAndText(icon, text);
        
        setBorder(new ButtonBorder());
        
        getActions().addAction(((PartnerScene) scene).getButtonAction());
        getActions().addAction(((PartnerScene) scene).getSelectAction());
    }
    
    
    public String getText() {
        return (labelWidget == null) ? null : labelWidget.getLabel();
    }
    
    
    public Image getIcon() {
        return (imageWidget == null) ? null : imageWidget.getImage();
    }
    
    
    public void setText(String text) {
        setIconAndText(getIcon(), text);
    }
    
    
    public void setIcon(Image image) {
        setIconAndText(image, getText());
    }
    
    
    public Insets getMargin() {
        return new Insets(margin.top, margin.left, margin.bottom, margin.right);
    }
    
    
    public void setMargin(Insets margin) {
        this.margin.set(margin.top, margin.left, margin.bottom, margin.right);
        revalidate();
        repaint();
    }
    
    
    public void setIconAndText(Image icon, String text) {
        removeButtonChildren();
        
        String oldText = getText();
        Image oldIcon = getIcon();
        
        if (imageWidget == null) {
            if (icon != null) {
                imageWidget = new ImageWidget(getScene(), icon);
            }
        } else {
            if (icon == null) {
                imageWidget = null;
            } else if (icon != oldIcon) {
                imageWidget.setImage(icon);
            }
        }
        
        if (labelWidget == null) {
            if (text != null) {
                labelWidget = new LabelWidget(getScene(), text);
                labelWidget.setFont(getScene().getDefaultFont());
                labelWidget.setForeground((isButtonEnabled()) 
                        ? ENABLED_TEXT_COLOR 
                        : DISABLED_TEXT_COLOR);
            }
        } else {
            if (text == null) {
                labelWidget = null;
            } else if (!((oldText != null) && text.equals(oldText))) {
                labelWidget.setLabel(text);
            }
        }
        
        addButtonChildren();
    }
    
    
    private void removeButtonChildren() {
        if (labelWidget != null) {
            removeChild(labelWidget);
        }
        
        if (imageWidget != null) {
            removeChild(imageWidget);
        }
    }
    
    
    private void addButtonChildren() {
        if (imageWidget != null) {
            addChild(imageWidget);
        }
        
        if (labelWidget != null) {
            addChild(labelWidget);
        }
    }
    
    
    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }
    
    
    private void fireActionPerformed() {
        if ((actionListener != null) && enabled) {
            actionListener.actionPerformed(new ActionEvent(this, 0, 
                    "button-pressed")); // NOI18N
        }
    }    
    
    
    public boolean isParenSelectionAllowed() {
        return parentSelectionAllowed;
    }
    
    
    public void setParentSelectionAllowed(boolean allowed) {
        parentSelectionAllowed = allowed;
    }
    
    
    public void mouseEntered() {
    	if (!getState().isFocused()) {
    		rollover = true;
    	}
        repaint();
    }
    
    
    public void mouseExited() {
    	rollover = false;
    	repaint();
    }
    
    
    public void mousePressed() {
        pressed = true;
        repaint();
    }
    
    
    public void mouseReleased(boolean inside) {
        pressed = false;
        focused = false;
        repaint();
        
        if (inside) {
            fireActionPerformed();
        }
    }
    
    /*
     * Changed method name so that it doesnt clash with Widget.setEnabled.
     */
    public void setButtonEnabled(boolean v) {
        enabled = v;
        
        if (labelWidget != null) {
            labelWidget.setForeground((v) 
                    ? ENABLED_TEXT_COLOR 
                    : DISABLED_TEXT_COLOR);
        }
 
        revalidate();
        repaint();
    }
    
    /*
     * Changed method name so that it doesnt clash with Widget.isEnabled.
     */
    public boolean isButtonEnabled() {
        return enabled;
    }
    
    
    @Override
    protected void notifyStateChanged(ObjectState previousState,
    		ObjectState state) {
    	if (!enabled) return;
    	
    	if (!previousState.isFocused() && state.isFocused()) {
    		focused = true;
            revalidate();
    	} else if (previousState.isFocused() && !state.isFocused()){
    		focused = false;
            revalidate();
        }
    	repaint();
    }
    
    
    private class ButtonBorder implements Border {
        public ButtonBorder() {}
        
        public Insets getInsets() {
            return new Insets(margin.top, margin.left, 
                    margin.bottom, margin.right);
        }

        public void paint(Graphics2D g2, Rectangle rect) {
            Paint oldPaing = g2.getPaint();
            
            if (enabled) {
                g2.setPaint(BORDER_COLOR);
                g2.fill(new RoundRectangle2D.Double(rect.x, rect.y,
                            rect.width, rect.height, 6, 6));

                if (pressed) {
                    g2.setPaint(new Color(0xCCCCCC));
                } else {
                    g2.setPaint(new GradientPaint(
                            0, rect.y + 1, BACKGROUND_COLOR_1,
                            0, rect.y + rect.height * 0.5f, 
                            BACKGROUND_COLOR_2, true));
                }

                if (rollover) {
                    g2.fill(new RoundRectangle2D.Double(rect.x + 1.5, rect.y + 1.5,
                            rect.width - 3, rect.height - 3, 3, 3));
                } else if (focused){
                	g2.setPaint(WidgetConstants.SELECTION_COLOR);
                    g2.fill(new RoundRectangle2D.Double(rect.x + 1, rect.y + 1,
                            rect.width - 2, rect.height - 2, 4, 4));
                } else {
                	g2.fill(new RoundRectangle2D.Double(rect.x + 1, rect.y + 1,
                			rect.width - 2, rect.height - 2, 4, 4));
                }
            } else {
                g2.setPaint(grayFilter(BORDER_COLOR));
                g2.fill(new RoundRectangle2D.Double(rect.x, rect.y,
                            rect.width, rect.height, 6, 6));
                
                g2.setPaint(BACKGROUND_COLOR_DISABLED);
                g2.fill(new RoundRectangle2D.Double(rect.x + 1, rect.y + 1,
                        rect.width - 2, rect.height - 2, 4, 4));
            }
            
            g2.setPaint(oldPaing);
        }

        public boolean isOpaque() {
            return true;
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


    public boolean isFocusable() {
        return focusable && isButtonEnabled() && isWidgetVisible();
    }
    
    private boolean isWidgetVisible() {
        Widget temp = this;
        while (temp.isVisible() && (temp = temp.getParentWidget()) != null);
        
        if (temp == null) return true;
        
        return false;
    }
    
    public void setFocusable(boolean focusable) {
        this.focusable = focusable; 
    }
    
    @Override
    public String toString() {
        return (labelWidget != null ? labelWidget.getLabel() : "");
    }
}
 
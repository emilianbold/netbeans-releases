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

import java.awt.Image;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.Scene;

/**
 * @author Ajit Bhate
 */
public class ButtonWidget extends Widget {
    
    private ImageLabelWidget button;
    private Action action;
   
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
        setBorder(BorderFactory.createBevelBorder(true));
        button.setBorder(BorderFactory.createEmptyBorder(4));
        setLayout(LayoutFactory.createHorizontalFlowLayout(
                LayoutFactory.SerialAlignment.CENTER, 4));
        getActions().addAction(ButtonAction.DEFAULT);
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
    protected void mouseClicked() {
        //simply delegate to swing action
        if(action!=null) {
            action.actionPerformed(new ActionEvent(this,0, 
                    (String)action.getValue(Action.ACTION_COMMAND_KEY)));
            //validate scene as called from ActionListeners
            getScene().validate();
        }
    }

    /**
     * Called when mouse is moved over on the widget.
     * change color?
     */
    protected void mouseEntered() {
    }
    
    /**
     * Called when mouse is moved away from the widget.
     * change color?
     */
    protected void mouseExited() {
    }
    
    private static ImageLabelWidget createImageLabelWidget(Scene scene, Action action) {
        String label = (String)action.getValue(Action.NAME);
        Image image = ((ImageIcon)action.getValue(Action.SMALL_ICON)).getImage();
        return new ImageLabelWidget(scene,image,label);
    }
}

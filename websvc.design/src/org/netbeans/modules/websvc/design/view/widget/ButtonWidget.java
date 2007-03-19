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

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.ComponentWidget;
import org.netbeans.api.visual.widget.Scene;

/**
 * @author Ajit Bhate
 */
public class ButtonWidget extends ComponentWidget {
    
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
    public ButtonWidget(Scene scene, Icon icon) {
        this(scene, icon, null);
    }
    
    
    /**
     *
     * @param scene
     * @param icon
     * @param text
     */
    public ButtonWidget(Scene scene, Icon icon, String text) {
        this(scene,new JButton(text,icon));
    }
    
    
    /**
     *
     * @param scene
     * @param action
     */
    public ButtonWidget(Scene scene, Action action) {
        this(scene,new JButton(action));
    }
    
    
    /**
     *
     * @param scene
     * @param button
     */
    public ButtonWidget(Scene scene, JButton button) {
        super(scene,button);
        getButton().setContentAreaFilled(false);
        getButton().setBorder(BorderFactory.createRaisedBevelBorder());
        setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory
                .SerialAlignment.CENTER, 4));
    }
    
    
    /**
     *
     * @return
     */
    public JButton getButton() {
        return (JButton)super.getComponent();
    }
    
    /**
     *
     * @param action
     */
    public void setAction(Action action) {
        getButton().setAction(action);
    }
    
    /**
     *
     * @return
     */
    public String getText() {
        return getButton().getText();
    }
    
    
    /**
     *
     * @return
     */
    public Icon getIcon() {
        return getButton().getIcon();
    }
    
    
    /**
     *
     * @param text
     */
    public void setText(String text) {
        getButton().setText(text);
    }
    
    
    /**
     *
     * @param icon
     */
    public void setIcon(Icon icon) {
        getButton().setIcon(icon);
    }
    
    /**
     * Changed method name so that it doesnt clash with Widget.setEnabled.
     * @param v
     */
    public void setButtonEnabled(boolean v) {
        JButton button = getButton();
        button.setEnabled(v);
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
    
}

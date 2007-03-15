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
import javax.swing.Icon;
import javax.swing.JButton;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.ComponentWidget;
import org.netbeans.api.visual.widget.Scene;

/**
 * @author Ajit Bhate
 * @author anjeleevich
 */
public class ButtonWidget extends ComponentWidget {

    public ButtonWidget(Scene scene, String text) {
        this(scene, null, text);
    }
    
    
    public ButtonWidget(Scene scene, Icon icon) {
        this(scene, icon, null);
    }
    
    
    public ButtonWidget(Scene scene, Icon icon, String text) {
        super(scene,new JButton(text,icon));
        setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory
                .SerialAlignment.CENTER, 4));
    }
    
    
    protected JButton getButton() {
        return (JButton)super.getComponent();
    }

    public void setAction(Action action) {
        getButton().setAction(action);
    }

    public String getText() {
        return getButton().getText();
    }
    
    
    public Icon getIcon() {
        return getButton().getIcon();
    }
    
    
    public void setText(String text) {
        getButton().setText(text);
    }
    
    
    public void setIcon(Icon icon) {
        getButton().setIcon(icon);
    }
    
    /*
     * Changed method name so that it doesnt clash with Widget.setEnabled.
     */
    public void setButtonEnabled(boolean v) {
        JButton button = getButton();
        button.setEnabled(v);
        revalidate();
        repaint();
    }
    
    /*
     * Changed method name so that it doesnt clash with Widget.isEnabled.
     */
    public boolean isButtonEnabled() {
        return getButton().isEnabled();
    }
    
}
 
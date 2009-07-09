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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-200? Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.frameworks.facelets.ui;

import java.awt.Component;
import java.io.InputStream;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 *
 * @author Petr Pisl
 */
public class TemplatePanel implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel {
    
    private TemplatePanelVisual component;
    
    /** Creates a new instance of TemplatePanel */
    public TemplatePanel() {
        component = null;
    }
    
    public Component getComponent() {
        if (component == null)
            component = new TemplatePanelVisual();
        
        return component;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx(TemplatePanel.class);
    }
    
    public void readSettings(Object settings) {
    }
    
    public void storeSettings(Object settings) {
    }
    
    public boolean isValid() {
        return true;
    }
    
    public void addChangeListener(ChangeListener l) {
    }
    
    public void removeChangeListener(ChangeListener l) {
    }
    
    public boolean isFinishPanel() {
        return true;
    }
    
    public InputStream getTemplate(){
        getComponent();
        return component.getTemplate();
    }
    
    public InputStream getDefaultCSS(){
        getComponent();
        return component.getDefaultCSS();
    }
    
    public InputStream getLayoutCSS(){
        getComponent();
        return component.getLayoutCSS();
    }
    
    public String getLayoutFileName(){
        getComponent();
        return component.getLayoutFileName();
    }
}

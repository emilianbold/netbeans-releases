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

package org.netbeans.modules.visualweb.project.jsf.framework;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.web.spi.webmodule.FrameworkConfigurationPanel;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.Utilities;
import org.openide.util.HelpCtx;

/**
 *
 * @author Po-Ting Wu
 */
public class JSFConfigurationPanel implements FrameworkConfigurationPanel, WizardDescriptor.FinishablePanel, WizardDescriptor.ValidatingPanel {
    private WizardDescriptor wizardDescriptor;
    private JSFConfigurationPanelVisual component;

    /** Creates a new instance of JSFConfigurationPanel */
    public JSFConfigurationPanel(boolean customizer) {
        this.customizer = customizer;
        getComponent();
    }

    private boolean customizer;



    public boolean isFinishPanel() {
        return true;
    }

    public Component getComponent() {
        if (component == null)
            component = new JSFConfigurationPanelVisual(this, customizer);

        return component;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(JSFConfigurationPanel.class);
    }

    public boolean isValid() {
        getComponent();
        return component.valid(wizardDescriptor);
    }

    private final Set/*<ChangeListener>*/ listeners = new HashSet(1);

    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }

    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        component.read (wizardDescriptor);

        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewProjectWizard to modify the title
        Object substitute = ((JComponent) component).getClientProperty("NewProjectWizard_Title"); // NOI18N
        if (substitute != null)
            wizardDescriptor.putProperty("NewProjectWizard_Title", substitute); // NOI18N

        // <RAVE> Default Bean Package
        String name = (String) wizardDescriptor.getProperty("name"); // NOI18N
        if (name != null && name.length() > 0) {
            setBeanPackage(name);
        }
        // </RAVE>
    }

    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor) settings;
        component.store(d);
        ((WizardDescriptor) d).putProperty("NewProjectWizard_Title", null); // NOI18N
    }

    public void validate() throws WizardValidationException {
        getComponent ();
        component.validate (wizardDescriptor);
    }

    public void enableComponents(boolean enable) {
        getComponent();
        component.enableComponents(enable);

    }

    // <RAVE> Default Bean Package
    public String getBeanPackage(){
        return component.getBeanPackage();
    }

    public void setBeanPackage(String pkg_name){
        component.setBeanPackage(deriveSafeName(pkg_name));
    }

    /**
     * Derive an identifier suitable for a java package name or context path
     * @param sourceName Original name from which to derive the name
     * @return An identifier suitable for a java package name or context path
     */
    public static String deriveSafeName(String sourceName) {
        StringBuffer dest = new StringBuffer(sourceName.length());
        int sourceLen = sourceName.length();
        if (sourceLen > 0) {
            int pos = 0;
            while (pos < sourceLen) {
                if (Character.isJavaIdentifierStart(sourceName.charAt(pos))) {
                    dest.append(Character.toLowerCase(sourceName.charAt(pos)));
                    pos++;
                    break;
                }
                pos++;
            }

            for (int i = pos; i < sourceLen; i++) {
                if (Character.isJavaIdentifierPart(sourceName.charAt(i)))
                    dest.append(Character.toLowerCase(sourceName.charAt(i)));
            }
        }
        if (dest.length() == 0 || !Utilities.isJavaIdentifier(dest.toString()))
            return "untitled";  // NOI18N
        else
            return dest.toString();
    }
    // </RAVE>

    public String getServletName(){
        return component.getServletName();
    }

    public void setServletName(String name){
        component.setServletName(name);
    }

    public String getURLPattern(){
        return component.getURLPattern();
    }

    public void setURLPattern(String pattern){
        component.setURLPattern(pattern);
    }

    public boolean validateXML(){
        return component.validateXML();
    }

    public void setValidateXML(boolean ver){
        component.setValidateXML(ver);
    }

    public boolean verifyObjects(){
        return component.verifyObjects();
    }
    
    public void setVerifyObjects(boolean val){
        component.setVerifyObjects(val);
    }
    
    public boolean packageJars(){
        return component.packageJars();
    }
    
}

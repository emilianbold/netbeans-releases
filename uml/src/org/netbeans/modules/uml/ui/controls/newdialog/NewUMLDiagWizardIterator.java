/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.uml.ui.controls.newdialog;

import java.awt.Component;
import java.util.NoSuchElementException;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

public final class NewUMLDiagWizardIterator implements WizardDescriptor.Iterator {
    
    // To invoke this wizard, copy-paste and run the following code, e.g. from
    // SomeAction.performAction():
    /*
    WizardDescriptor.Iterator iterator = new NewUMLDiagWizardIterator();
    WizardDescriptor wizardDescriptor = new WizardDescriptor(iterator);
    // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
    // {1} will be replaced by WizardDescriptor.Iterator.name()
    wizardDescriptor.setTitleFormat(new MessageFormat("{0} ({1})"));
    wizardDescriptor.setTitle("Your wizard dialog title here");
    Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
    dialog.setVisible(true);
    dialog.toFront();
    boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
    if (!cancelled) {
        // do something
    }
     */
    
    private int index;
    
    private WizardDescriptor.Panel[] panels;
    public static final String DIAGRAM_DETAILS = "DIAGRAM_DETAILS";      //NOI18N
    static final String PROP_DIAG_KIND = "DIAGRAM_KIND"; //NOI18N
    static final String PROP_DIAG_NAME = "DIAGRAM_NAME"; //NOI18N
    static final String PROP_NAMESPACE = "NAMESPACE"; //NOI18N
    
    
    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[] {
                new NewUMLDiagWizardPanel1()
            };
            String[] steps = new String[panels.length];
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                // Default step name to component name of panel.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
                    // Sets steps names for a panel
                    jc.putClientProperty("WizardPanel_contentData", steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);
                }
            }
        }
        return panels;
    }
    
    public WizardDescriptor.Panel current() {
        return getPanels()[index];
    }
    
    public String name() {
        //return index + 1 + " of " + getPanels().length;
        // Get the string from the bundle for l10n purpose
        return  NbBundle.getMessage(
                NewUMLDiagWizardIterator.class,
                "PSK_NEWWIZARD_TITLE_INDEX", // NOI18N
                String.valueOf((index + 1)),
                String.valueOf(getPanels().length) );
    }
    
    public boolean hasNext() {
        return index < getPanels().length - 1;
    }
    
    public boolean hasPrevious() {
        return index > 0;
    }
    
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }
    
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public void addChangeListener(ChangeListener l) {}
    public void removeChangeListener(ChangeListener l) {}
    
}

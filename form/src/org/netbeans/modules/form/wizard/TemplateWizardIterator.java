/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.wizard;

import java.util.*;
import java.io.IOException;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;

import org.openide.*;
import org.openide.loaders.*;
import org.openide.src.*;
import org.openide.cookies.SourceCookie;
import org.openide.util.NbBundle;

import org.netbeans.modules.java.ui.wizard.JavaWizardIterator;

/**
 * Special template wizard iterator for BeanForm template - requires to
 * specify superclass additionally.
 * [Unfortunately this class needs to extend JavaWizardIterator.]
 *
 * @author Tomas Pavek
 */

class TemplateWizardIterator extends JavaWizardIterator {

    private transient WizardDescriptor.Panel[] panels;
    private transient int panelIndex;

    private static JavaWizardIterator instance;

    public static synchronized JavaWizardIterator singleton() {
        if (instance == null)
            instance = new TemplateWizardIterator();
        return instance;
    }

    public void initialize(TemplateWizard wizard) {
        super.initialize(wizard);
        if (panels == null) {
            WizardDescriptor.Panel panel1 = wizard.targetChooser();
            WizardDescriptor.Panel panel2 = new SuperclassWizardPanel();

            ResourceBundle bundle = NbBundle.getBundle(TemplateWizardIterator.class);
            String[] panelNames = new String[] {
                bundle.getString("CTL_NewFormTitle"), // NOI18N
                bundle.getString("CTL_SuperclassTitle") }; // NOI18N

            Component comp1 = panel1.getComponent();
            if (comp1 instanceof javax.swing.JComponent) {
                comp1.setName(bundle.getString("CTL_NewFormTitle")); // NOI18N
                ((javax.swing.JComponent)comp1).putClientProperty(
                    "WizardPanel_contentData", panelNames); // NOI18N
            }

            panels = new WizardDescriptor.Panel[] { panel1, panel2 };
        }
    }

    public void uninitialize(TemplateWizard wiz) {
        super.uninitialize(wiz);
        panels = null;
    }

    public Set instantiate(TemplateWizard wiz)
        throws IOException, IllegalArgumentException
    {
        Set set = super.instantiate(wiz);
        
        try {
            DataObject dobj = (DataObject) set.iterator().next();
            SourceCookie src = (SourceCookie) dobj.getCookie(SourceCookie.class);
            if (src != null) {
                ClassElement[] classes = src.getSource().getClasses();
                if (classes != null && classes.length > 0) {
                    ClassElement formClass = classes[0];
                    String superclassName =
                        ((SuperclassWizardPanel)panels[1]).getSuperclassName();
                    formClass.setSuperclass(Identifier.create(superclassName));
                }
            }
        }
        catch (Exception ex) {}
        
        return set;
    }

    public WizardDescriptor.Panel current() {
        return panels[panelIndex];
    }

    public boolean hasNext() {
        return panelIndex < 1;
    }
    
    public boolean hasPrevious() {
        return panelIndex > 0;
    }
    
    public void nextPanel() {
        if (panelIndex < 1)
            panelIndex++;
        else
            throw new NoSuchElementException();
    }
    
    public void previousPanel() {
        if (panelIndex > 0)
            panelIndex--;
        else
            throw new NoSuchElementException();
    }

    // ---------

    static class SuperclassWizardPanel implements WizardDescriptor.FinishPanel {

        private SuperclassPanel panelUI;

        String getSuperclassName() {
            String name = panelUI != null ?
                          panelUI.superclassTextField.getText() : null;
            return name != null && !"".equals(name) ? name : "java.lang.Object"; // NOI18N
        }

        public Component getComponent() {
            if (panelUI == null)
                panelUI = new SuperclassPanel();
            return panelUI;
        }

        public boolean isValid() {
            return true;
        }

        public void readSettings(Object settings) {
        }

        public void storeSettings(Object settings) {
        }

        public void addChangeListener(ChangeListener l) {
        }

        public void removeChangeListener(ChangeListener l) {
        }

        public org.openide.util.HelpCtx getHelp () {
            return new org.openide.util.HelpCtx("gui.creatingforms"); // NOI18N
        }
    }

    // -------

    static class SuperclassPanel extends javax.swing.JPanel {

        SuperclassPanel() {
            ResourceBundle bundle = NbBundle.getBundle(TemplateWizardIterator.class);
            setName(bundle.getString("CTL_SuperclassTitle")); // NOI18N
            putClientProperty("WizardPanel_contentSelectedIndex", new Integer(1)); //NOI18N
            getAccessibleContext()
                .setAccessibleDescription(bundle.getString("ACSD_SuperclassPanel")); // NOI18N

            setLayout(new GridBagLayout());
            setBorder(new javax.swing.border.EmptyBorder(8, 8, 8, 8));

            label1 = new JLabel();
            superclassTextField = new JTextField();

            label1.setLabelFor(superclassTextField);
            label1.setText(bundle.getString("CTL_SuperclassName")); // NOI18N
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = new Insets(0, 0, 0, 12);
            add(label1, gridBagConstraints);

            superclassTextField.setText("java.lang.Object"); // NOI18N
            superclassTextField.setToolTipText(bundle.getString("CTL_SuperclassName_Hint")); // NOI18N
            superclassTextField.getAccessibleContext()
                .setAccessibleDescription(bundle.getString("ACSD_SuperclassTextField"));  // NOI18N
            superclassTextField.addFocusListener(new FocusAdapter() {
                public void focusGained(java.awt.event.FocusEvent evt) {
                    superclassTextField.selectAll();
                }
            });

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            add(superclassTextField, gridBagConstraints);
        }

        public void addNotify() {
            super.addNotify();
            superclassTextField.requestFocus();
        }

        private JLabel label1;
        private JTextField superclassTextField;
    }

    // -----

    private Object readResolve() {
        return singleton();
    }
}

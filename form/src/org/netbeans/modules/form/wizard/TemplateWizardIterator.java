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
import org.openide.cookies.SaveCookie;
import org.openide.cookies.SourceCookie;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;

import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;

/**
 * Special template wizard iterator for BeanForm template - requires to
 * specify superclass additionally.
 *
 * @author Tomas Pavek, Jan Stola
 */

class TemplateWizardIterator implements WizardDescriptor.InstantiatingIterator {

    private transient WizardDescriptor.Panel superclassPanel;
    private transient boolean superclassPanelCurrent;
    private transient WizardDescriptor.InstantiatingIterator delegateIterator;

    private boolean specifySuperclass;

    public static TemplateWizardIterator createForSuperclass() {
        return new TemplateWizardIterator(true);
    }

    public static TemplateWizardIterator create() {
        return new TemplateWizardIterator(false);
    }

    public TemplateWizardIterator(boolean specifySuperclass) {
        delegateIterator = JavaTemplates.createJavaTemplateIterator();
        this.specifySuperclass = specifySuperclass;
    }

    public void initialize(WizardDescriptor wizard) {
        delegateIterator.initialize(wizard);
        superclassPanelCurrent = false;
        if (superclassPanel == null && specifySuperclass) {
            superclassPanel = new SuperclassWizardPanel();
            
            ResourceBundle bundle = NbBundle.getBundle(TemplateWizardIterator.class);
            JComponent comp = (JComponent)delegateIterator.current().getComponent();
            String[] contentData = (String[])comp.getClientProperty("WizardPanel_contentData"); // NOI18N
            String[] newContentData = new String[contentData.length+1];
            System.arraycopy(contentData, 0, newContentData, 0, contentData.length);
            newContentData[contentData.length] = bundle.getString("CTL_SuperclassTitle"); // NOI18N
            comp.putClientProperty("WizardPanel_contentData", newContentData); // NOI18N
        }    
    }

    public void uninitialize(WizardDescriptor wizard) {
        delegateIterator.uninitialize(wizard);
        superclassPanel = null;
    }

    public Set instantiate() throws IOException, IllegalArgumentException {
        Set set = delegateIterator.instantiate();
        
        try {
            FileObject template = (FileObject) set.iterator().next();
            DataObject dobj = DataObject.find(template);
            if (specifySuperclass) {
                SourceCookie src = (SourceCookie) dobj.getCookie(SourceCookie.class);
                if (src != null) {
                    ClassElement[] classes = src.getSource().getClasses();
                    if (classes != null && classes.length > 0) {
                        ClassElement formClass = classes[0];
                        String superclassName =
                            ((SuperclassWizardPanel)superclassPanel).getSuperclassName();
                        formClass.setSuperclass(Identifier.create(superclassName));
                        SaveCookie savec = (SaveCookie) dobj.getCookie(SaveCookie.class);
                        if (savec != null) {
                            savec.save();
                        }
                    }
                }
            }
            dobj.getPrimaryFile().setAttribute("justCreatedByNewWizard", Boolean.TRUE); // NOI18N
        }
        catch (Exception ex) {}
        
        return set;
    }

    public WizardDescriptor.Panel current() {
        return superclassPanelCurrent ? superclassPanel : delegateIterator.current();
    }

    public boolean hasNext() {
        return !superclassPanelCurrent && superclassPanel != null;
    }
    
    public boolean hasPrevious() {
        return superclassPanelCurrent ? true : delegateIterator.hasPrevious();
    }
    
    public void nextPanel() {
        if (delegateIterator.hasNext()) {
            delegateIterator.nextPanel();
        } else {
            if (superclassPanelCurrent || superclassPanel == null) {
                throw new NoSuchElementException();
            } else {
                superclassPanelCurrent = true;
            }
        }
    }
    
    public void previousPanel() {
        if (superclassPanelCurrent) {
            superclassPanelCurrent = false;
        } else {
            delegateIterator.previousPanel();
        }
    }
    
    public void addChangeListener(ChangeListener l) {
        delegateIterator.addChangeListener(l);
    }
    
    public String name() {
        return superclassPanelCurrent ? "" : delegateIterator.name(); // NOI18N
    }
    
    public void removeChangeListener(ChangeListener l) {
        delegateIterator.removeChangeListener(l);
    }

    // ---------

    static class SuperclassWizardPanel implements WizardDescriptor.FinishablePanel {

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
        
        public boolean isFinishPanel() {
            return true;
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
}

/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.search;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.BeanInfo;
import java.beans.Customizer;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openidex.search.SearchType;


/**
 * Panel which shows to user one search type allowing it to customize.
 *
 * @author  Peter Zavadsky
 * @see SearchPanel
 */
public class SearchTypePanel extends JPanel implements PropertyChangeListener {

    /** Name of customized property. */
    public static final String PROP_CUSTOMIZED = "customized"; // NOI18N
    /** Modificator suffix.  */
    private static final String MODIFICATOR_SUFFIX = " *"; // NOI18N
    /** Customized property. Indicates this criterion model 
     * was customized by user. */
    private boolean customized;
    /** Search type this model is customized by. */
    private SearchType searchType;
    /** Beaninfo of search type. */
    private BeanInfo beanInfo;
    /** Customizer for search type. */
    private Customizer customizer;
    /** Customizer component. */
    private Component customizerComponent;
    /**
     * saved criteria for this panel
     *
     * @see  #addSavedCriteria
     */
    private SearchCriterion[] savedCriteria;

    private String lastSavedName;
    
    
    /** Creates new form <code>SearchTypePanel</code>. */
    public SearchTypePanel(SearchType searchType) {
        initComponents();
        initAccessibility();
                
        this.searchType = searchType;

        try {
            beanInfo = Utilities.getBeanInfo(this.searchType.getClass());

            if(hasCustomizer()) {
                customizer = getCustomizer();
                customizerComponent = (Component)customizer;
            } else {
                // PENDING use property sheet as it will implement Customizer
                // allow hiding tabs, ....
                System.err.println("No customizer for "                 //NOI18N
                                   + this.searchType.getName()
                                   + ", skipping...");                  //NOI18N
            }
        } catch(IntrospectionException ie) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ie);
        }

        customizer.setObject(this.searchType);
        this.searchType.addPropertyChangeListener(this);
        
        ResourceBundle bundle = NbBundle.getBundle(SearchTypePanel.class);
        Mnemonics.setLocalizedText(
                applyCheckBox,
                bundle.getString("TEXT_BUTTON_APPLY"));                 //NOI18N
        
        Mnemonics.setLocalizedText(
                saveButton,
                bundle.getString("TEXT_BUTTON_SAVE_AS"));               //NOI18N
        
        saveButton.setEnabled(false);
        
        Mnemonics.setLocalizedText(
                restoreButton,
                bundle.getString("TEXT_BUTTON_RESTORE"));               //NOI18N

        /* The button is disabled until saved criteria are available. */
        restoreButton.setEnabled(false);

        customizerPanel.add(customizerComponent, BorderLayout.CENTER);

        setCustomized(this.searchType.isValid());
        
        // obtain tab label string & icon
        setName(createName());
    }

    private void initAccessibility() {
        ResourceBundle bundle = NbBundle.getBundle(SearchTypePanel.class);
        this.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACS_DIALOG_DESC"));                   //NOI18N        
        restoreButton.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACS_TEXT_BUTTON_RESTORE"));           //NOI18N        
        saveButton.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACS_TEXT_BUTTON_SAVE_AS"));           //NOI18N        
        applyCheckBox.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACS_TEXT_BUTTON_APPLY"));             //NOI18N        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        customizerPanel = new javax.swing.JPanel();
        applyCheckBox = new javax.swing.JCheckBox();
        saveButton = new javax.swing.JButton();
        restoreButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        customizerPanel.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 0, 11);
        add(customizerPanel, gridBagConstraints);

        applyCheckBox.setText("jCheckBox2");
        applyCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyCheckBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(17, 11, 0, 11);
        add(applyCheckBox, gridBagConstraints);

        saveButton.setText("jButton3");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 11, 0);
        add(saveButton, gridBagConstraints);

        restoreButton.setText("jButton4");
        restoreButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                restoreButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 11, 11);
        add(restoreButton, gridBagConstraints);

    }//GEN-END:initComponents

    private void restoreButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_restoreButtonActionPerformed
        restoreCriterion();
    }//GEN-LAST:event_restoreButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        saveCriterion();
    }//GEN-LAST:event_saveButtonActionPerformed

    private void applyCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyCheckBoxActionPerformed
        // PENDING Some better solution of valid / customized needed.
        boolean selected = applyCheckBox.isSelected();
        setCustomized(selected);
        searchType.setValid(selected);
    }//GEN-LAST:event_applyCheckBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel customizerPanel;
    private javax.swing.JButton restoreButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JCheckBox applyCheckBox;
    // End of variables declaration//GEN-END:variables

    // PENDING Better solution for these properties are needed.
    /** Listens on search type PROP_VALID property change and sets
     * customized property accordingly. */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == searchType) {

            // if Type fires valid property change listens for
            // its invalidity -> mark itself as unmodified
            if (SearchType.PROP_VALID.equals(evt.getPropertyName()) ) {
                
                if (evt.getNewValue().equals(Boolean.FALSE)) {
                    setCustomized (false);
                    return;
                } else {
                    setCustomized (true);
                }
            }
        }
    }
    
    /**
     * Creates name used as tab name, 
     * @return name. */
    private String createName() {
        String name = searchType.getName();

        if(customized) {        
            return  name + MODIFICATOR_SUFFIX;
        } else {
            return  name;
        }
    }

    /** Indicates whether the search type has customizer. */
    private boolean hasCustomizer() {
        // true if we have already computed beanInfo and it has customizer class
        return beanInfo.getBeanDescriptor().getCustomizerClass() != null;
    }

    /** Gets customizer for the search type. 
     * @return customizer object. */
    private Customizer getCustomizer() {
        if (customizer != null) return customizer;

        Class clazz = beanInfo.getBeanDescriptor ().getCustomizerClass ();
        if (clazz == null) return null;

        Object o;
        try {
            o = clazz.newInstance ();
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }

        if (!(o instanceof Component) ||
                !(o instanceof Customizer)) return null;

        return (Customizer) o;
    }
    
    public Component getComponent() {
        return customizerComponent;
    }

    /**
     * Sets customized property.
     *
     * @param cust value to which customized property to set.
     */
    private void setCustomized(boolean cust) {
        customized = cust;

        saveButton.setEnabled(customized);
        applyCheckBox.setSelected(customized);

        setName(createName());

        firePropertyChange(PROP_CUSTOMIZED, !cust, cust);
    }

    /** Tests whether this panel is customized. */
    public boolean isCustomized() {
        return customized;
    }

    /** Saves the criterion. */
    private void saveCriterion() {
        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout(12,0));
        
        ResourceBundle bundle = NbBundle.getBundle(SearchTypePanel.class);
        JLabel nameLab = new JLabel();
        Mnemonics.setLocalizedText(
                nameLab,
                bundle.getString("TEXT_LABEL_NAME"));                   //NOI18N
        
        pane.add(nameLab, BorderLayout.WEST); 
        pane.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACS_SaveAsPanel"));                   //NOI18N
        
        JTextField textField;
        if (lastSavedName != null) {
            textField = new JTextField(lastSavedName, 20);
        } else {
            textField = new JTextField(20);
        }
        textField.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACS_TEXT_LABEL_SELECT"));             //NOI18N
        
        nameLab.setLabelFor(textField);
        pane.add(textField, BorderLayout.CENTER);
        pane.setBorder(BorderFactory.createEmptyBorder(12,12,0,11));
        
        DialogDescriptor desc = new DialogDescriptor(
                pane,
                bundle.getString("TEXT_LABEL_SAVE_CRITERION"));         //NOI18N        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(desc);
        
        while (true) {
            dialog.setVisible(true);
            if (desc.getValue().equals(DialogDescriptor.OK_OPTION)) {
                String name = textField.getText();
                if (name.length() > 0) {
                    saveCriterion(name);
                    lastSavedName = name;
                    break;
                }
            } else {
                return; // cancel
            }
        }
    }
    
    /** */
    private void saveCriterion(String name) {
        SearchType copy = (SearchType) searchType.clone();
        copy.setName(name);
        
        SearchCriterion toSave;
        try {
            toSave = new SearchCriterion(copy);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            return;
        }
        
        /* file the new criterion into the list of saved criteria: */
        String className = toSave.searchTypeClassName;
        boolean found = false;
        if (savedCriteria != null) {
            for (int i = 0; i < savedCriteria.length; i++) {
                if (savedCriteria[i].name.equals(name)
                        && savedCriteria[i].searchTypeClassName.equals(className)) {
                    found = true;
                    SearchProjectSettings.getInstance()
                            .replaceSearchCriterion(name, className, toSave);
                    savedCriteria[i] = toSave;
                    break;
                }
            }
        }
        if (!found) {
            SearchProjectSettings.getInstance().addSearchCriterion(toSave);
            addSavedCriteria(Collections.singleton(toSave));
        }
    }
    
    /**
     * Displays a dialog for choosing from a list of (saved) criteria
     * and loads the selected criterion (if the choice is confirmed).
     */
    private void restoreCriterion() {
        JPanel pane = new JPanel();
        pane.setLayout(new GridBagLayout());
        
        ResourceBundle bundle = NbBundle.getBundle(SearchTypePanel.class);
        pane.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACS_RestorePanel"));                  //NOI18N
        
        JLabel resLabel = new JLabel();
        Mnemonics.setLocalizedText(
                resLabel,
                bundle.getString("TEXT_LABEL_SELECT"));                 //NOI18N
        
        JComboBox combo = new JComboBox(savedCriteria);
        combo.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACSD_SELECT_CRITERION"));             //NOI18N
        resLabel.setLabelFor(combo);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 12);
        pane.add(resLabel, gbc);
        
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0d;
        gbc.insets = new Insets(0, 0, 0, 0);
        pane.add(combo, gbc);
        
        pane.setBorder(BorderFactory.createEmptyBorder(12,12,0,11));
        
        DialogDescriptor desc = new DialogDescriptor(
                pane,
                bundle.getString("TEXT_LABEL_RESTORE_CRITERION"));      //NOI18N
        DialogDisplayer.getDefault().createDialog(desc).setVisible(true);
        
        if (desc.getValue().equals(DialogDescriptor.OK_OPTION)) {
            SearchCriterion c = (SearchCriterion) combo.getSelectedItem();
            restoreCriterion(c);
        }
    }
    
    /** */
    private void restoreCriterion(SearchCriterion c) {
        SearchType searchType;
        ObjectInputStream ois = null;
        try {
            ois = new SearchTypeInputStream(
                    new ByteArrayInputStream(c.criterionData));
            searchType = (SearchType) ois.readObject();
        } catch (Exception ex) {
            String msg = NbBundle.getMessage(
                    SearchTypePanel.class,
                    "TEXT_MSG_Error_while_loading_criterion");          //NOI18N
            ErrorManager.getDefault().notify(
                    ErrorManager.EXCEPTION,
                    ErrorManager.getDefault().annotate(ex, msg));
            return;
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException ex2) {
                    /* give up */
                }
            }
        }
        restoreSearchType(searchType);
    }

    /** Restores the search type. */
    private void restoreSearchType(SearchType searchType) {
        this.searchType.removePropertyChangeListener(this);

        this.searchType = (SearchType) searchType.clone();
        getCustomizer().setObject(this.searchType);
        this.searchType.addPropertyChangeListener(this);

        setCustomized(true);
    }    

    /** Return currently hold bean. */
    public SearchType getSearchType() {
        return searchType;
    }
    
    /**
     * Class equality
     *
     * @return this.bean.getClass().equals(bean.getClass());
     */
    public boolean equals(Object obj) {
        try {
            return searchType.getClass().equals(
                    ((SearchTypePanel) obj).getSearchType().getClass());
        } catch (ClassCastException ex) {
            return false;
        }
    }

    /** Gets help context. */
    public HelpCtx getHelpCtx() {
        return searchType.getHelpCtx();
    }
    
    /**
     * Adds the specified set of search criteria to the list of saved criteria
     * available in this panel.
     *
     * @param  criteria  search criteria to add
     */
    void addSavedCriteria(Collection criteria) {
        if (criteria.isEmpty()) {
            return;
        }
        
        SearchCriterion[] newCriteria = new SearchCriterion[criteria.size()];
        criteria.toArray(newCriteria);
        if (savedCriteria == null) {
            savedCriteria = newCriteria;
            restoreButton.setEnabled(true);
        } else {
            
            /* append the specified criteria to the current set of criteria: */
            SearchCriterion[] oldCriteria = savedCriteria;
            savedCriteria = new SearchCriterion[oldCriteria.length
                                                + newCriteria.length];
            System.arraycopy(oldCriteria, 0,
                             savedCriteria, 0,
                             oldCriteria.length);
            System.arraycopy(newCriteria, 0,
                             savedCriteria, oldCriteria.length,
                             newCriteria.length);
        }
    }
    
}

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


package org.netbeans.modules.i18n;


import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.classpath.ClassPath;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.WeakListener;
import org.openide.util.Lookup;
import org.netbeans.api.javahelp.Help;
import org.netbeans.api.project.Project;


/**
 * Panel which provides GUI for i18n action.
 * Customizes <code>I18nString</code> object and is used by <code>I18nSupport<code> for i18n-izing 
 * one source.
 *
 * @author  Peter Zavadsky
 */
public class I18nPanel extends JPanel {

    /** <code>I18nString</code> cusomized in this panel. */
    private I18nString i18nString;
    
    /** Helper bundle used for i18n-zing strings in this source.  */
    private ResourceBundle bundle;

    /** Helper property change support. */
    private PropertyChangeListener propListener;
    
    /** Generated serial version ID. */
    static final long serialVersionUID =-6982482491017390786L;

    private Project project;
    
    static final long ALL_BUTTONS = 0xffffff;
    static final long NO_BUTTONS = 0x0;
    static final long REPLACE_BUTTON = 0xf0000;
    static final long SKIP_BUTTON    = 0x0f000;
    static final long INFO_BUTTON    = 0x00f00;
    static final long CANCEL_BUTTON   = 0x000f0;
    static final long HELP_BUTTON    = 0x0000f;


    
    /** Creates new I18nPanel. */
    public I18nPanel(PropertyPanel propertyPanel, Project project) {
        this(propertyPanel, true, project);
    }

    /** Creates i18n panel.
     * @param propertyPanel panel for customizing i18n strings 
     * @param withButtons if panel with replace, skip ect. buttons should be added */
    public I18nPanel(PropertyPanel propertyPanel, boolean withButtons, Project project) {
        this.project = project;

        // Init bundle.
        bundle = I18nUtil.getBundle();
        
        this.propertyPanel = propertyPanel;
        
        initComponents();
        initAccessibility();        
        
        if(!withButtons)
            remove(buttonsPanel);
        
        // create empty panel
        emptyPanel = new EmptyPropertyPanel();
        propertyPanelShown = true;

        showBundleMessage("TXT_SearchingForStrings");
    }


    private boolean propertyPanelShown;

    public void showBundleMessage(String bundleKey) {

        emptyPanel.setBundleText(bundleKey);
        if (propertyPanelShown) {
            propertyPanelPlaceholder.remove(propertyPanel);
            propertyPanelPlaceholder.add(emptyPanel);
            propertyPanelPlaceholder.validate();
            propertyPanelPlaceholder.repaint();
            propertyPanelShown = false;
        }
    }

    public void showPropertyPanel() {
        if (!propertyPanelShown) {
            propertyPanelPlaceholder.remove(emptyPanel);
            propertyPanelPlaceholder.add(propertyPanel);
            propertyPanelPlaceholder.validate();
            propertyPanelPlaceholder.repaint();
            propertyPanelShown = true;
        }
    }


    
 
    
    
    /** Overrides superclass method to set default button. */
    public void addNotify() {
        super.addNotify();
        
        if(SwingUtilities.isDescendingFrom(replaceButton, this))
            getRootPane().setDefaultButton(replaceButton);
    }
    
    /** Getter for <code>i18nString</code>. */
    public I18nString getI18nString() {
        return i18nString;
    }
    
    /** Setter for i18nString property. */
    public void setI18nString(I18nString i18nString) {
        this.i18nString = i18nString;

        ((PropertyPanel)propertyPanel).setI18nString(i18nString);
        ((ResourcePanel)resourcePanel).setI18nString(i18nString);
        
        // Set listener to enable/disable replace button.
        resourcePanel.addPropertyChangeListener(WeakListener.propertyChange(
            propListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if(ResourcePanel.PROP_RESOURCE.equals(evt.getPropertyName())) {
                        replaceButton.setEnabled(evt.getNewValue() != null);
                        ((PropertyPanel)propertyPanel).updateBundleKeys();
                    }
                }
            },
            resourcePanel
        ));

        replaceButton.setEnabled(i18nString.getSupport().getResourceHolder().getResource() != null);
        
        showPropertyPanel();
    }

    /** Replace button accessor. */
    JButton getReplaceButton() {
        return replaceButton;
    }
    
    /** Skip button accessor. */
    JButton getSkipButton() {
        return skipButton;
    }

    /** Info button accessor. */
    JButton getInfoButton() {
        return infoButton;
    }
    
    /** Cancel/Close button accessor. */
    JButton getCancelButton() {
        return cancelButton;
    }
    
    /** Enables/disables buttons. */
    private void buttonsEnableDisable() {
        boolean isBundle = i18nString.getSupport().getResourceHolder().getResource() != null;
        replaceButton.setEnabled(isBundle);
    }

    public void setDefaultResource(DataObject dataObject) {
        if (dataObject != null) {
            // look for peer Bundle.properties
            FileObject fo = dataObject.getPrimaryFile();
            ClassPath cp = ClassPath.getClassPath( fo, ClassPath.SOURCE );
            
            FileObject folder = cp.findResource( cp.getResourceName( fo.getParent() ) );
            
            while( folder != null && cp.contains( folder ) ) {
                
                String rn = cp.getResourceName( folder ) + "/Bundle.properties"; // NOI18N
                
                FileObject peer = cp.findResource( rn );
                if (peer != null) {
                    try {
                        DataObject peerDataObject = DataObject.find(peer);
                        ((ResourcePanel)resourcePanel).setResource(peerDataObject);
                        return;
                    } catch (IOException ex) {
                        // no default resource
                    }
                }
                folder = folder.getParent();
            }            
        }        
    }
    
    /** Creates <code>ResourcePanel</code>. */
    private JPanel createResourcePanel() {
        return new ResourcePanel(project);
    }
    
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_I18nPanel"));        
        skipButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CTL_SkipButton"));        
        cancelButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CTL_CancelButton"));        
        replaceButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CTL_ReplaceButton"));        
        infoButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CTL_InfoButton"));        
        helpButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CTL_HelpButton"));        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        resourcePanel = createResourcePanel();
        propertyPanelPlaceholder = new javax.swing.JPanel();
        fillPanel = new javax.swing.JPanel();
        buttonsPanel = new javax.swing.JPanel();
        replaceButton = new javax.swing.JButton();
        skipButton = new javax.swing.JButton();
        infoButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        helpButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(resourcePanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(propertyPanelPlaceholder, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(fillPanel, gridBagConstraints);

        buttonsPanel.setLayout(new java.awt.GridLayout(1, 5, 5, 0));

        replaceButton.setMnemonic(Util.getChar("CTL_ReplaceButton_Mnem"));
        replaceButton.setText(bundle.getString("CTL_ReplaceButton"));
        buttonsPanel.add(replaceButton);

        skipButton.setMnemonic(Util.getChar("CTL_SkipButton_Mnem"));
        skipButton.setText(bundle.getString("CTL_SkipButton"));
        buttonsPanel.add(skipButton);

        infoButton.setMnemonic(Util.getChar("CTL_InfoButton_Mnem"));
        infoButton.setText(bundle.getString("CTL_InfoButton"));
        buttonsPanel.add(infoButton);

        cancelButton.setMnemonic(Util.getChar("CTL_CloseButton_mne"));
        cancelButton.setText(bundle.getString("CTL_CloseButton"));
        buttonsPanel.add(cancelButton);

        helpButton.setMnemonic(Util.getChar("CTL_HelpButton_Mnem"));
        helpButton.setText(bundle.getString("CTL_HelpButton"));
        helpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpButtonActionPerformed(evt);
            }
        });

        buttonsPanel.add(helpButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(17, 0, 11, 11);
        add(buttonsPanel, gridBagConstraints);

    }//GEN-END:initComponents

  private void helpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpButtonActionPerformed
      HelpCtx help = new HelpCtx(I18nUtil.HELP_ID_AUTOINSERT);
      
      String sysprop = System.getProperty("org.openide.actions.HelpAction.DEBUG"); // NOI18N
      
      if("true".equals(sysprop) || "full".equals(sysprop)) // NOI18N
          System.err.println ("I18n module: Help button showing: " + help); // NOI18N, please do not comment out

      Help helpSystem = (Help)Lookup.getDefault().lookup(Help.class);
      helpSystem.showHelp(help);
  }//GEN-LAST:event_helpButtonActionPerformed

    public void enableButtons(long buttonMask) {
        replaceButton.setEnabled((buttonMask & REPLACE_BUTTON) != 0);
        skipButton.setEnabled((buttonMask & SKIP_BUTTON) != 0);
        infoButton.setEnabled((buttonMask & INFO_BUTTON) != 0);
        cancelButton.setEnabled((buttonMask & CANCEL_BUTTON) != 0);
        helpButton.setEnabled((buttonMask & HELP_BUTTON) != 0);
    }

        


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel fillPanel;
    private javax.swing.JButton helpButton;
    private javax.swing.JButton infoButton;
    private javax.swing.JPanel propertyPanelPlaceholder;
    private javax.swing.JButton replaceButton;
    private javax.swing.JPanel resourcePanel;
    private javax.swing.JButton skipButton;
    // End of variables declaration//GEN-END:variables
    private PropertyPanel propertyPanel;
    private EmptyPropertyPanel emptyPanel;
}

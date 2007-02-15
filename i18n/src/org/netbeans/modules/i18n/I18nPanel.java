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


package org.netbeans.modules.i18n;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.classpath.ClassPath;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.WeakListeners;
import org.openide.util.Lookup;
import org.netbeans.api.javahelp.Help;
import org.netbeans.api.project.Project;
import javax.swing.BoxLayout;


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

    private FileObject file;
    
    static final long ALL_BUTTONS = 0xffffff;
    static final long NO_BUTTONS = 0x0;
    static final long REPLACE_BUTTON = 0xf0000;
    static final long SKIP_BUTTON    = 0x0f000;
    static final long INFO_BUTTON    = 0x00f00;
    static final long CANCEL_BUTTON   = 0x000f0;
    static final long HELP_BUTTON    = 0x0000f;


    
    /** 
     * Creates new I18nPanel.  In order to correctly localize
     * classpath for property bundle chooser, the dialog must know the
     * project and a file to choose the bundle for.
     * @param propertyPanel panel for customizing i18n strings 
     * @param project the Project to choose bundles from
     * @param file the FileObject to choose bundles for
     */
    public I18nPanel(PropertyPanel propertyPanel, Project project, FileObject file) {
        this(propertyPanel, true, project, file);
    }

    /** Creates i18n panel.
     * @param propertyPanel panel for customizing i18n strings 
     * @param withButtons if panel with replace, skip ect. buttons should be added 
     * @param project the Project to choose bundles from
     * @param file the FileObject to choose bundles for
     */
    public I18nPanel(PropertyPanel propertyPanel, boolean withButtons, Project project, FileObject file) {
        this.project = project;
        this.file = file;
        this.propertyPanel = propertyPanel;
        this.propertyPanel.setFile(file);
        this.propertyPanel.setEnabled(project != null);

        // Init bundle.
        bundle = I18nUtil.getBundle();
        
        initComponents();
        myInitComponents();
        initAccessibility();        
        
        if(!withButtons)
            remove(buttonsPanel);
        
        // create empty panel
        emptyPanel = new EmptyPropertyPanel();
        contentsShown = true;

        showBundleMessage("TXT_SearchingForStrings");
    }


    private boolean contentsShown;

    public void showBundleMessage(String bundleKey) {

        emptyPanel.setBundleText(bundleKey);
        if (contentsShown) {
            contentsPanelPlaceholder.remove(propertyPanel);
            contentsPanelPlaceholder.add(emptyPanel);
            contentsPanelPlaceholder.validate();
            contentsPanelPlaceholder.repaint();
            contentsShown = false;
        }
        buttonsEnableDisable();
    }

    public void showPropertyPanel() {
        if (!contentsShown) {
            contentsPanelPlaceholder.remove(emptyPanel);
            contentsPanelPlaceholder.add(propertyPanel);
            contentsPanelPlaceholder.validate();
            contentsPanelPlaceholder.repaint();
            contentsShown = true;
        }
        buttonsEnableDisable();        
    }


    
    /**
     * Reset associated project to a new value
     */
//    public void setProject(Project project) {
////        ((ResourcePanel)resourcePanel).setProject(project);
//        propertyPanel.setEnabled(project != null);
//
//    }
//
//    public Project getProject() { 
//        return ((ResourcePanel)resourcePanel).getProject();
//    }
    
    /**
     * Sets the file associated with this panel -- the one, which
     * is localized
     */ 
    public void setFile(FileObject file) {
//        ((ResourcePanel)resourcePanel).setFile(file);
        propertyPanel.setFile(file);
    }
    
    /**
     * Gets the file associated with this panel -- the one, which
     * is localized
     */ 
    public FileObject getFile() {
//        return ((ResourcePanel)resourcePanel).getFile();
        return propertyPanel.getFile();
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

        propertyPanel.setI18nString(i18nString);
//        ((ResourcePanel)resourcePanel).setI18nString(i18nString);        
        
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
    
    /** Enables/disables buttons based on the contents of the dialog. */
    private void buttonsEnableDisable() {
        if (contentsShown) {
            enableButtons(ALL_BUTTONS);
            boolean isBundle = (i18nString != null) && (i18nString.getSupport().getResourceHolder().getResource() != null);
            boolean keyEmpty = (getI18nString()==null || 
                                getI18nString().getKey()==null || 
                                getI18nString().getKey().trim().length()==0);
            replaceButton.setEnabled(isBundle && !keyEmpty);
        } else  
            enableButtons(CANCEL_BUTTON | HELP_BUTTON);
        
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
//                        ((ResourcePanel)resourcePanel).setResource(peerDataObject);
                        propertyPanel.setResource(peerDataObject);
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
//    private JPanel createResourcePanel() {
//        return new ResourcePanel(project, file);
//    }
    
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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        fillPanel = new javax.swing.JPanel();
        buttonsPanel = new javax.swing.JPanel();
        replaceButton = new javax.swing.JButton();
        skipButton = new javax.swing.JButton();
        infoButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        helpButton = new javax.swing.JButton();
        contentsPanelPlaceholder = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(fillPanel, gridBagConstraints);

        buttonsPanel.setLayout(new java.awt.GridLayout(1, 5, 5, 0));

        org.openide.awt.Mnemonics.setLocalizedText(replaceButton, bundle.getString("CTL_ReplaceButton")); // NOI18N
        buttonsPanel.add(replaceButton);

        org.openide.awt.Mnemonics.setLocalizedText(skipButton, bundle.getString("CTL_SkipButton")); // NOI18N
        buttonsPanel.add(skipButton);

        org.openide.awt.Mnemonics.setLocalizedText(infoButton, bundle.getString("CTL_InfoButton")); // NOI18N
        buttonsPanel.add(infoButton);

        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, bundle.getString("CTL_CloseButton")); // NOI18N
        buttonsPanel.add(cancelButton);

        org.openide.awt.Mnemonics.setLocalizedText(helpButton, bundle.getString("CTL_HelpButton")); // NOI18N
        helpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpButtonActionPerformed(evt);
            }
        });
        buttonsPanel.add(helpButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(17, 0, 11, 11);
        add(buttonsPanel, gridBagConstraints);

        contentsPanelPlaceholder.setLayout(new javax.swing.BoxLayout(contentsPanelPlaceholder, javax.swing.BoxLayout.Y_AXIS));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(contentsPanelPlaceholder, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void myInitComponents() {
//        resourcePanel = createResourcePanel();
//        contentsPanel = new JPanel();
//        contentsPanel.setLayout(new BoxLayout(contentsPanel, BoxLayout.Y_AXIS));

//        contentsPanel.add(resourcePanel);
//        contentsPanel.add(propertyPanel);
        contentsShown = true;
        contentsPanelPlaceholder.add(propertyPanel);
        
      
        propertyPanel.addPropertyChangeListener(PropertyPanel.PROP_STRING, 
                                                new PropertyChangeListener() {
                                                    public void propertyChange(PropertyChangeEvent evt) {
                                                        buttonsEnableDisable();
                                                    }
                                                });
        
        propertyPanel.addPropertyChangeListener(PropertyPanel.PROP_RESOURCE,
//                WeakListeners.propertyChange(
                    new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent evt) {
                            if(PropertyPanel.PROP_RESOURCE.equals(evt.getPropertyName())) {
                                buttonsEnableDisable();
    //                            ((PropertyPanel)propertyPanel).updateAllValues();
                            }
                        }
                    }
//                },resourcePanel
//               )
        );
        
    }

  private void helpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpButtonActionPerformed
      HelpCtx help = new HelpCtx(I18nUtil.HELP_ID_AUTOINSERT);
      
      String sysprop = System.getProperty("org.openide.actions.HelpAction.DEBUG"); // NOI18N
      
      if("true".equals(sysprop) || "full".equals(sysprop)) // NOI18N
          System.err.println ("I18n module: Help button showing: " + help); // NOI18N, please do not comment out

      Help helpSystem = (Help)Lookup.getDefault().lookup(Help.class);
      helpSystem.showHelp(help);
  }//GEN-LAST:event_helpButtonActionPerformed

    private void enableButtons(long buttonMask) {
        replaceButton.setEnabled((buttonMask & REPLACE_BUTTON) != 0);
        skipButton.setEnabled((buttonMask & SKIP_BUTTON) != 0);
        infoButton.setEnabled((buttonMask & INFO_BUTTON) != 0);
        cancelButton.setEnabled((buttonMask & CANCEL_BUTTON) != 0);
        helpButton.setEnabled((buttonMask & HELP_BUTTON) != 0);               
    }

        


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel contentsPanelPlaceholder;
    private javax.swing.JPanel fillPanel;
    private javax.swing.JButton helpButton;
    private javax.swing.JButton infoButton;
    private javax.swing.JButton replaceButton;
    private javax.swing.JButton skipButton;
    // End of variables declaration//GEN-END:variables

    private EmptyPropertyPanel emptyPanel;
//    private javax.swing.JPanel resourcePanel;
    private PropertyPanel propertyPanel;
//    private javax.swing.JPanel contentsPanel;

}

/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.i18n;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.text.Caret;
import javax.swing.text.StyledDocument;

import org.netbeans.modules.properties.PropertiesModule;

import org.netbeans.modules.form.FormDataObject;

import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.util.NbBundle;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.Workspace;


/**
 * Interantionalization manager.
 *
 * @author   Peter Zavadsky
 */
public class I18nManager {
    
    /** Name of property in document holding I18nFinder reference. */
    public static final String I18N_SUPPORT_PROP = "org.netbeans.modules.i18n.support"; // NOI18N
    
    /** Common name for I18N mode. */
    public static final String I18N_MODE = "internationalization"; // NOI18N
    
    /** Holds the only instance of I18nSupport. */
    private static I18nManager instance;
    
    /** Reference to top component providing internationalize dialog. */
    private TopComponent topComponent;
    
    /** Reference to <code>i18nPanel</code>, part of internationalize dialog. */
    private I18nPanel i18nPanel;
    
    /** Holds document object. */
    private StyledDocument document;
    
    /** Caret in opened document. */
    private Caret caret;
    
    /** Holds <code>DataObject</code> which document is internationalized. 
     * @see org.openide.loaders.DataObject */
    private DataObject targetDataObject;
    
    
    /** Constructor. Don't call this. Use getI18nSupport method instead. */
    private I18nManager(StyledDocument document, DataObject targetDataObject) {
        initialize(document, targetDataObject);
    }

    
    /** Initializes the <code>document</code> and <code>targetDataObject</code> variables. */
    private void initialize(StyledDocument document, DataObject targetDataObject) {
        if(this.document == null || !this.document.equals(document) )
            this.document = document;
        if(this.targetDataObject == null || !this.targetDataObject.equals(targetDataObject) )
            this.targetDataObject = targetDataObject;
    }
    
    /** Gets the only insance of I18nSupport. */
    public static I18nManager getI18nManager(StyledDocument document, DataObject targetDataObject) {
        if(instance == null)
            instance = new I18nManager(document, targetDataObject);
        else {
            instance.initialize(document, targetDataObject);
        }
            
        return instance;
    }
    
    /** The 'heart' method called by <code>I18nAction</code>. */
    public void internationalize() {
        // Always close dialog from previous search if necessary.
        closeDialog();
       
        // initialize the component
        EditorCookie ec = (EditorCookie)targetDataObject.getCookie(EditorCookie.class);
        if(ec == null)
            return;
        
        JEditorPane[] panes = ec.getOpenedPanes();
        if(panes == null) {
            NotifyDescriptor.Message message = new NotifyDescriptor.Message(NbBundle.getBundle(I18nModule.class).
            getString("MSG_CouldNotOpen"), NotifyDescriptor.ERROR_MESSAGE);
            TopManager.getDefault().notify(message);
            return;
        }
        
        caret = panes[0].getCaret();
        
        // Initializes finder.
        getI18nSupport().initialize();
        
        // do the search
        if(find()) {
            createDialog();
            fillDialogValues();
            showDialog();
        } else {
            NotifyDescriptor.Message message = new NotifyDescriptor.Message(NbBundle.getBundle(I18nModule.class).
            getString("MSG_NoInternationalizableString"), NotifyDescriptor.INFORMATION_MESSAGE); // to info message
            TopManager.getDefault().notify(message);
        }
    }

    /** Finds hard coded string. */
    private boolean find() {
        I18nJavaSupport support = getI18nSupport();

        // Actual find on finder.
        boolean found = support.findNext();

        if(found) {
            // Highlight found hard coded string.
            caret.setDot(support.getHardStringStartOffset());
            caret.moveDot(support.getHardStringEndOffset());

            return true;
        } 
        
        // not found in entire source document
        return false;
    }

    /** Get i18n support. */
    private I18nJavaSupport getI18nSupport() {
        I18nJavaSupport i18nJavaSupport;
        i18nJavaSupport = (I18nJavaSupport)document.getProperty(I18N_SUPPORT_PROP);
        
        if(i18nJavaSupport == null) {
            if(isFormDataObject(targetDataObject))
                i18nJavaSupport = new I18nFormSupport(document, (FormDataObject)targetDataObject);
            else
                i18nJavaSupport = new I18nJavaSupport(document);
            document.putProperty(I18N_SUPPORT_PROP, i18nJavaSupport);
        }
        
        return i18nJavaSupport;
    }
    
    /* Replace button handler. */
    private void doReplace() {
        ResourceBundleString rbString = null;
        try {
            rbString = i18nPanel.getResourceBundlePanel().getResourceBundleString();
        } catch (IllegalStateException e) {
            NotifyDescriptor.Message nd = new NotifyDescriptor.Message(NbBundle.getBundle(I18nModule.class).
            getString("EXC_BadKey"), NotifyDescriptor.ERROR_MESSAGE);
            TopManager.getDefault().notify(nd);
            return;
        }

        // Try to add key to bundle.
        I18nUtil.addKeyToBundle(rbString);

        // Hack for creating field.
        I18nUtil.createField(rbString, targetDataObject);
        
        // Replace hardcoded string.
        getI18nSupport().replace(rbString);
        
        if(find()) {
            createDialog();
            fillDialogValues();
            showDialog();
        } else
            doCancel();
    }
    
    /* Replace All button handler. At the time does nothing. */
    private void doReplaceAll() {
        // PENDING
    }
    
    /* Skip button handler. */
    private void doSkip() {
        if(find()) {
            createDialog();
            fillDialogValues();
            showDialog();
        } else
            doCancel();
    }
    
    /* Cancel button handler. */
    private void doCancel() {
        // no memory leaks
        document = null;
        targetDataObject = null;
        caret = null;
        closeDialog();
    }
    
    /** Finds from the <code>lastPos</code> (or the current position if position == -1).
     * @return true if a hardcoded string was found
     * @see #lastPos
     */
    /** Creates dialog. In our case it is a top component. */
    private void createDialog() {
        if(topComponent == null) {
            
            // prepare panel which will reside inside top component
            i18nPanel = new I18nPanel();
            
            final JButton[] buttons = i18nPanel.getButtons();
            
            for (int i = 0; i < buttons.length; i++) {
                buttons[i].addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        if (evt.getSource() == buttons[0])
                            doReplace();
                        if (evt.getSource() == buttons[1])
                            doReplaceAll();
                        if (evt.getSource() == buttons[2])
                            doSkip();
                        if (evt.getSource() == buttons[3])
                            doCancel();
                    }
                });
            }
            
            // actually create the dialog as top component
            topComponent = new TopComponent();
            topComponent.setCloseOperation(TopComponent.CLOSE_EACH);
            topComponent.setLayout(new BorderLayout());
            topComponent.add(i18nPanel, BorderLayout.CENTER);
            topComponent.setName(targetDataObject.getName());
            
            // dock into I18N mode if possible
            Workspace[] currentWs = TopManager.getDefault().getWindowManager().getWorkspaces();
            for (int i = currentWs.length; --i >= 0; ) {
                Mode i18nMode = currentWs[i].findMode(I18N_MODE);
                if (i18nMode == null) {
                    i18nMode = currentWs[i].createMode(
                        I18N_MODE,
                        NbBundle.getBundle(I18nModule.class).getString("CTL_I18nDialogTitle"),
                        I18nSupport.class.getResource("/org/netbeans/modules/i18n/i18nAction.gif") // NOI18N
                    );
                }
                i18nMode.dockInto(topComponent);
            }
        }
        
        // Hook for setting target data object for resource bundle panel.
        i18nPanel.getResourceBundlePanel().setTargetDataObject(targetDataObject);
    }
    
    /** Shows dialog. In our case it is a top component. */
    private void showDialog() {
        topComponent.open();
        topComponent.requestFocus();
    }
    
    /** Closes dialog. In our case it is a top component. */
    private void closeDialog() {
        if(topComponent != null) {
            topComponent.close();
            
            topComponent = null;
            i18nPanel = null;
        }
    }

    /** Fills values presented in internationalize dialog. */
    private void fillDialogValues() {
        ResourceBundleString oldRbString = i18nPanel.getResourceBundlePanel().getResourceBundleString();
        ResourceBundleString newRbString = getI18nSupport().getDefaultBundleString(oldRbString);
        
        // Check in case new value is added and not initialized.
        if(newRbString.getResourceBundle() == null)
            newRbString.setResourceBundle(PropertiesModule.getLastBundleUsed());
       
        i18nPanel.setResourceBundleString(newRbString);
        i18nPanel.setI18nInfo(getI18nSupport().getInfo());
    }
    
    /** Utility method. 
     * @param dataObject checked <code>DataObject</code>
     * @return true if form module is available and <code>dataObject</code> is instnce of <code>FormDataObject</code> */
    private static boolean isFormDataObject(DataObject dataObject) {
        try {
             // Test for form module presence.
            Class formModule = Class.forName("org.netbeans.modules.form.FormModule", // NOI18N
                false, I18nManager.class.getClassLoader());

            // Form module is available -> call dependent code.
            if(dataObject instanceof FormDataObject)
                return true;
        } catch (NoClassDefFoundError err) {
        } catch (ClassNotFoundException e) {
        }

        return false;
    }

    /** Interface providing information about i18n parameters
     * used in I18nPanel.
     */
    public interface I18nInfo {
        /** Getter for property hardString.
         *@return Value of property hardString.
         */
        public String getHardString();
        
        /** Getter for property hardLine.
         *@return Value of property hardLine.
         */
        public String getHardLine();
        
        /** Getter for property componentName.
         * @return Value of property componentName. */
        public String getComponentName();
        
        /** Getter for property propertyName.
         *@return Value of property propertyName. */
        public String getPropertyName();
        
    } // End of I18nInfo interface.
    
}
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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Caret;
import javax.swing.text.StyledDocument;

import org.netbeans.modules.properties.PropertiesModule;

import org.netbeans.modules.form.FormDataObject;

import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;


/**
 * Internationalization manager.
 *
 * @author   Peter Zavadsky
 */
public class I18nManager {
    
    /** Singleton instance of I18nManager. */
    private static I18nManager manager;

    /** Support for this internatioanlize session. */
    private I18nJavaSupport support;

    /** Weak reference to i18n panel. */
    private WeakReference i18nPanelWRef;
    
    /** Weak reference to top component in which internationalizing will be provided. */
    private WeakReference topComponentWRef;
    
    /** Weak reference to caret in editor pane. */
    private WeakReference caretWRef;
    
    
    /** Private constructor. To ge instance use <code>getI18nMananger</code> method instead. */
    private I18nManager() {
    }

    
    /** Gets the only instance of I18nSupport. */
    public static I18nManager getDefault() {
        if(manager == null) {
            synchronized(I18nManager.class) {
                if(manager == null)
                    manager = new I18nManager();
            }
        }
            
        return manager;
    }
    
    /** Get i18n support. */
    private void initSupport(StyledDocument document, DataObject targetDataObject) {
        if(isFormDataObject(targetDataObject))
            support = new I18nFormSupport(document, (FormDataObject)targetDataObject);
        else
            support = new I18nJavaSupport(document, targetDataObject);
    }
    
    /** The 'heart' method called by <code>I18nAction</code>. */
    public void internationalize(StyledDocument document, DataObject targetDataObject) {
        // If there is insert i18n action working on the same document -> cancel it.
        ((InsertI18nStringAction)SystemAction.get(InsertI18nStringAction.class)).cancel();
        
        // If there is i18n action working -> cancel it.
        closeDialog();

        // Initilialize support.
        initSupport(document, targetDataObject);
       
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

        // Gets the top component and keep weak reference to it.
        topComponentWRef = new WeakReference((TopComponent)SwingUtilities.getAncestorOfClass(TopComponent.class, panes[0]));
        
        // Add i18n panel to top component.
        addI18nPanel();
        
        // Keep only weak ref to caret, the strong one maintains editor pane itself.
        caretWRef = new WeakReference(panes[0].getCaret());
        
        // Initializes support.
        support.initialize();
        
        // do the search
        if(find())
            fillDialogValues();
        else {
            NotifyDescriptor.Message message = new NotifyDescriptor.Message(NbBundle.getBundle(I18nModule.class).
                getString("MSG_NoInternationalizableString"), NotifyDescriptor.INFORMATION_MESSAGE); // to info message
            TopManager.getDefault().notify(message);
        }
    }

    /** Finds hard coded string. */
    private boolean find() {
        // Actual find on finder.
        boolean found = support.findNext();

        if(found) {
            // Highlight found hard coded string.
            Object referent = caretWRef.get();
            if(referent != null) {
                ((Caret)referent).setDot(support.getHardStringStartOffset());
                ((Caret)referent).moveDot(support.getHardStringEndOffset());
            }

            return true;
        } 
        
        // not found in entire source document
        return false;
    }

    /** Fills values presented in internationalize dialog. */
    private void fillDialogValues() {
        // It has to work this way, at this time the strong reference in top component have to exist.
        I18nPanel i18nPanel = (I18nPanel)i18nPanelWRef.get();
        
        ResourceBundleString oldRbString = i18nPanel.getResourceBundlePanel().getResourceBundleString();
        ResourceBundleString newRbString = support.getDefaultBundleString(oldRbString);
        
        i18nPanel.setResourceBundleString(newRbString);
        i18nPanel.setI18nInfo(support.getInfo());
        
        showDialog();
    }
    
    /** Replaces current found hard coded string and continue the search for next one. */
    private void replace() {
        ResourceBundleString rbString = null;
        try {
            // To call weak without check have to be save here cause stronmg reference in top component have to exist.
            rbString = ((I18nPanel)i18nPanelWRef.get()).getResourceBundlePanel().getResourceBundleString();
        } catch (IllegalStateException e) {
            NotifyDescriptor.Message nd = new NotifyDescriptor.Message(NbBundle.getBundle(I18nModule.class).
            getString("EXC_BadKey"), NotifyDescriptor.ERROR_MESSAGE);
            TopManager.getDefault().notify(nd);
            return;
        }

        // Try to add key to bundle.
        I18nUtil.addKeyToBundle(rbString);

        // Replace hardcoded string.
        support.replace(rbString);

        skip();
    }
    
    /** At the time not implemented. */
    private void replaceAll() {
        // PENDING
    }
    
    /** Skips foudn hard coded string and conitnue to search for next one. */
    private void skip() {
        if(find())
            fillDialogValues();
        else
            cancel();
    }
    
    /** Cancels current internationalizing session and re-layout top component to original layout. */
    public void cancel() {
        // No memory leaks.
        support = null;
        
        closeDialog();
    }
    
    /** Creates dialog. In our case it is a top component. */
    private void addI18nPanel() {
        // Create i18n panel.
        I18nPanel i18nPanel = new I18nPanel();
        
        final JButton[] buttons = i18nPanel.getButtons();

        for(int i = 0; i < buttons.length; i++) {
            buttons[i].addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if (evt.getSource() == buttons[0]) // Replace button.
                        replace();
                    if (evt.getSource() == buttons[1]) // Replace All button.
                        replaceAll();
                    if (evt.getSource() == buttons[2]) // Skip button.
                        skip();
                    if (evt.getSource() == buttons[3]) // Cancel button.
                        cancel();
                }
            });
        }

        // Get top component from weak reference.
        Object referent = topComponentWRef.get();

        if(referent == null) {
            // Shouldn't happen.
            if(Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                throw new InternalError("I18n: Manager. Top componnet has been lost from weak reference."); // NOI18N
        }
 
        TopComponent topComponent = (TopComponent)referent;
        
        // Change layout the way split pane is first component i18n panel at the left and original component at the right side.
        Component component = topComponent.getComponent(0);

        // Keep weak reference to i18n panel only.
        i18nPanelWRef = new WeakReference(i18nPanel);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, i18nPanel, component);
        splitPane.setOneTouchExpandable(true);
        
        // Remove original component.
        topComponent.remove(component);
        
        // Add our split pane.
        topComponent.add(splitPane, BorderLayout.CENTER);

        topComponent.revalidate();
    }
    
    /** Shows dialog. In our case opens top component if it is necessary and
     * sets caret visible in editor part. */
    private void showDialog() {
        // Open top component if it is not already.
        Object topCompReferent = topComponentWRef.get();
        if(topCompReferent != null) {
            if(!((TopComponent)topCompReferent).isOpened()) {
                ((TopComponent)topCompReferent).open();
                ((TopComponent)topCompReferent).requestFocus();
            }
        }

        // Set caret visible.
        Object caretReferent = caretWRef.get();
        if(caretReferent != null) {
            if(!((Caret)caretReferent).isVisible())
                ((Caret)caretReferent).setVisible(true);
        }
    }
    
    /** Closes dialog. In our case removes <code>I18nPanel</code> from top component
     * and 'reconstruct it' to it's original layout. */
    private void closeDialog() {
        // At start check if there is open from previous internationalize action.
        if(topComponentWRef == null)
            return;
        
        Object referent = topComponentWRef.get();

        if(referent != null) {
            TopComponent topComponent = (TopComponent)referent;
        
            if(topComponent != null && topComponent.getComponentCount() == 1 && topComponent.getComponent(0) instanceof JSplitPane) {

                JSplitPane splitPane = (JSplitPane)topComponent.getComponent(0);

                // Remove our split pane.
                topComponent.remove(splitPane);

                // Add original component.
                topComponent.add(splitPane.getRightComponent(), BorderLayout.CENTER);

                topComponent.revalidate();
                topComponent.repaint();
            }
        }
    }

    /** Utility method. 
     * @param dataObject checked <code>DataObject</code>
     * @return true if form module is available and <code>dataObject</code> is instnce of <code>FormDataObject</code> */
    private static boolean isFormDataObject(DataObject dataObject) {
        try {
             // Test for form module presence.
            Class formModule = Class.forName("org.netbeans.modules.form.FormEditorModule", // NOI18N
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
     * used in <code>I18nPanel</code>.
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
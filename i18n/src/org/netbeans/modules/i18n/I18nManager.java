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
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.lang.ref.WeakReference;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.Caret;

import org.openide.cookies.EditorCookie;
import org.openide.DialogDescriptor;
import org.openide.loaders.DataObject;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.util.actions.SystemAction;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.Workspace;


/**
 * Manages performing of i18n action -> i18n-zation of one source.
 *
 * @author   Peter Zavadsky
 */
public class I18nManager {
    
    /** Internationalization mode. */
    public static final String I18N_MODE = "internationalization"; // NOI18N
    
    /** Singleton instance of I18nManager. */
    private static I18nManager manager;

    /** Support for this internatioanlize session. */
    private I18nSupport support;

    /** Weak reference to i18n panel. */
    private WeakReference i18nPanelWRef = new WeakReference(null);
    
    /** Weak reference to top component in which internationalizing will be provided. */
    private WeakReference topComponentWRef = new WeakReference(null);
    
    /** Weak reference to caret in editor pane. */
    private WeakReference caretWRef;

    /** Found hard coded string. */
    private HardCodedString hcString;
    
    
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
    private void initSupport(DataObject sourceDataObject) throws IOException {
        I18nSupport.Factory factory = FactoryRegistry.getFactory(sourceDataObject.getClass());
        
        support = factory.create(sourceDataObject);
    }
    
    /** The 'heart' method called by <code>I18nAction</code>. */
    public void internationalize(final DataObject sourceDataObject) {
        // If there is insert i18n action working on the same document -> cancel it.
        ((InsertI18nStringAction)SystemAction.get(InsertI18nStringAction.class)).cancel();

        // If there is i18n action working -> cancel it.
        closeDialog();

        // Initilialize support.
        try {
            initSupport(sourceDataObject);
        } catch(IOException ioe) {
            if(Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                System.err.println("I18N: Document could not be loaded for "+sourceDataObject.getName()); // NOI18N
            
            return;
        }

        // initialize the component
        final EditorCookie ec = (EditorCookie)sourceDataObject.getCookie(EditorCookie.class);
        if(ec == null)
            return;

        // do the search
        if(find()) {
            // XXX It's necessary to send it to AWT Thread thus assure the 
            // editor cookie succed to open the panes via open() method ->
            // subclasses usually sends opening to AWT thread.
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    initCaret(ec);
                    highlightHCString();

                    // Add i18n panel to top component.
                    getDialog(sourceDataObject.getName());

                    fillDialogValues();
                }
            });
        } else {
            NotifyDescriptor.Message message = new NotifyDescriptor.Message(
                I18nUtil.getBundle().getString("MSG_NoInternationalizableString"), NotifyDescriptor.INFORMATION_MESSAGE); // to info message
            TopManager.getDefault().notify(message);
        }
    }

    /** Initializes caret. */
    private void initCaret(EditorCookie ec) {
        JEditorPane[] panes = ec.getOpenedPanes();
        if(panes == null) {
            NotifyDescriptor.Message message = new NotifyDescriptor.Message(
                I18nUtil.getBundle().getString("MSG_CouldNotOpen"), NotifyDescriptor.ERROR_MESSAGE);
            TopManager.getDefault().notify(message);

            return;
        }

        // Keep only weak ref to caret, the strong one maintains editor pane itself.
        caretWRef = new WeakReference(panes[0].getCaret());
    }
    
    /** Highlights found hasrdcoded string. */
    private void highlightHCString() {
        HardCodedString hStr = hcString;
        
        if(hStr == null) {
            return;
        }
        
        // Highlight found hard coded string.
        Caret caret = (Caret)caretWRef.get();
        
        if(caret != null) {
            caret.setDot(hStr.getStartPosition().getOffset());
            caret.moveDot(hStr.getEndPosition().getOffset());
        }
    }

    /** Finds hard coded string. */
    private boolean find() {
        // Actual find on finder.
        hcString = support.getFinder().findNextHardCodedString();

        if(hcString != null) {
            return true;
        } 
        
        // not found in entire source document
        return false;
    }

    /** Fills values presented in internationalize dialog. */
    private void fillDialogValues() {
        // It has to work this way, at this time the strong reference in top component have to exist.
        I18nPanel i18nPanel = (I18nPanel)i18nPanelWRef.get();

        i18nPanel.setI18nString(support.getDefaultI18nString(hcString));
        
        showDialog();
    }
    
    /** Replaces current found hard coded string and continue the search for next one. */
    private void replace() {
        I18nString i18nString = null;
        
        try {
            // To call weak without check have to be save here cause strong reference in the top component have to exist.
            i18nString = ((I18nPanel)i18nPanelWRef.get()).getI18nString();
        } catch (IllegalStateException e) {
            NotifyDescriptor.Message nd = new NotifyDescriptor.Message(
                I18nUtil.getBundle().getString("EXC_BadKey"), NotifyDescriptor.ERROR_MESSAGE);
            TopManager.getDefault().notify(nd);
            return;
        }

        // Try to add key to bundle.
        support.getResourceHolder().addProperty(i18nString.getKey(), i18nString.getValue(), i18nString.getComment());

        // Provide additional changes if they are available.
        if(support.hasAdditionalCustomizer())
            support.performAdditionalChanges();
        
        // Replace hardcoded string.
        support.getReplacer().replace(hcString, i18nString);

        skip();
    }
    
    /** Skips foudn hard coded string and conitnue to search for next one. */
    private void skip() {
        if(find()) {
            highlightHCString();
            
            fillDialogValues();
        } else {
            cancel();
        }
    }
    
    /** Shows info about found hard coded string. */
    private void showInfo() {
        JPanel infoPanel = support.getInfo(hcString);

        DialogDescriptor dd = new DialogDescriptor(infoPanel, I18nUtil.getBundle().getString("CTL_InfoPanelTitle"));
        
        dd.setModal(true);
        dd.setOptionType(DialogDescriptor.DEFAULT_OPTION);
        dd.setOptions(new Object[] {DialogDescriptor.OK_OPTION});
        dd.setAdditionalOptions(new Object[0]);

        
        Dialog infoDialog = TopManager.getDefault().createDialog(dd);
        infoDialog.setVisible(true);
    }
    
    /** Cancels current internationalizing session and re-layout top component to original layout. */
    public void cancel() {
        // No memory leaks.
        support = null;
        
        closeDialog();
    }
    
    /** Gets dialog. In our case it is a top component. 
     * @param name name of top component */
    private void getDialog(String name) {
        TopComponent topComponent = (TopComponent)topComponentWRef.get();
        I18nPanel i18nPanel = (I18nPanel)i18nPanelWRef.get();

        // Dialog was not created yet or garbaged already.
        if(i18nPanel == null) {
            
            // Create i18n panel.
            i18nPanel = new I18nPanel(support.getPropertyPanel());

            // Helper final.
            final I18nPanel panel = i18nPanel;
            
            // Set button listeners.
            ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if(evt.getSource() == panel.getReplaceButton())
                        replace();
                    else if(evt.getSource() == panel.getSkipButton())
                        skip();
                    else if(evt.getSource() == panel.getInfoButton())
                        showInfo();
                    else if(evt.getSource() == panel.getCancelButton())
                        cancel();
                }
            };
            
            i18nPanel.getReplaceButton().addActionListener(listener);
            i18nPanel.getSkipButton().addActionListener(listener);
            i18nPanel.getInfoButton().addActionListener(listener);
            i18nPanel.getCancelButton().addActionListener(listener);
            
            // Reset weak reference.
            i18nPanelWRef = new WeakReference(i18nPanel);
        }
        
        // Set default i18n string.
        i18nPanel.setI18nString(support.getDefaultI18nString());
        
        if(topComponent == null) {
            // Actually create dialog, as non serializable top component.
            topComponent = new TopComponent() {
                public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
                }
                
                public void writeExternal(ObjectOutput out) throws IOException {
                }
                
                protected Object writeReplace() throws ObjectStreamException {
                    return null;
                }
            };
            topComponent.setCloseOperation(TopComponent.CLOSE_EACH);
            topComponent.setLayout(new BorderLayout());
            topComponent.add(i18nPanel, BorderLayout.CENTER);
            topComponent.setName(name);

             // dock into I18N mode if possible
            Workspace[] currentWs = TopManager.getDefault().getWindowManager().getWorkspaces();
            for (int i = currentWs.length; --i >= 0; ) {
                Mode i18nMode = currentWs[i].findMode(I18N_MODE);
                if (i18nMode == null) {
                    i18nMode = currentWs[i].createMode(
                        I18N_MODE,
                        I18nUtil.getBundle().getString("CTL_I18nDialogTitle"),
                        I18nManager.class.getResource("/org/netbeans/modules/i18n/i18nAction.gif") // NOI18N
                    );
                }
                i18nMode.dockInto(topComponent);
            }
                
            // Reset weak reference.
            topComponentWRef = new WeakReference(topComponent);
        }
        
        topComponent.open();
        topComponent.requestFocus();
    }
    
    /** Shows dialog. In our case opens top component if it is necessary and
     * sets caret visible in editor part. */
    private void showDialog() {
        // Open top component if it is not already.
        TopComponent topComponent = (TopComponent)topComponentWRef.get();
        if(topComponent != null) {
            if(!topComponent.isOpened()) {
                topComponent.open();
                topComponent.requestFocus();
            }
        }

        // Set caret visible.
        Caret caret = (Caret)caretWRef.get();
        if(caret != null) {
            if(!caret.isVisible())
                caret.setVisible(true);
        }
    }
    
    /** Closes dialog. In our case removes <code>I18nPanel</code> from top component
     * and 'reconstruct it' to it's original layout. */
    private void closeDialog() {
        TopComponent topComponent = (TopComponent)topComponentWRef.get();
        
        if(topComponent != null)
            topComponent.close();
    }

}
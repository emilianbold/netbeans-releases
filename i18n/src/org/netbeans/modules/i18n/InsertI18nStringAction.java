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
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.lang.ref.SoftReference;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import org.netbeans.modules.properties.PropertiesModule;

import org.openide.DialogDescriptor;
import org.openide.cookies.SourceCookie;
import org.openide.nodes.Node;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.Workspace;


/**
* Insert internationalized string at caret position (if it is not in guarded block).
*
* @author   Petr Jiricka
*/
public class InsertI18nStringAction extends CookieAction {

    /** Soft reference to <code>InsertI18nComponent</code> instance. */
    private SoftReference topComponentSoftRef;
    
    /** Generated serial version UID. */
    static final long serialVersionUID =-7002111874047983222L;
    
    /** Actually performs InsertI18nStringAction
    * @param activatedNodes Currently activated nodes.
    */
    public void performAction (final Node[] activatedNodes) {
        final SourceCookie.Editor sec = (SourceCookie.Editor)(activatedNodes[0]).getCookie(SourceCookie.Editor.class);
        if(sec == null)
            return;
        
        sec.open(); 

        // Set data object.
        DataObject dataObject = (DataObject)sec.getSource().getCookie(DataObject.class);
        if(dataObject == null)
            return; 

        // Set position. 
        int position = sec.getOpenedPanes()[0].getCaret().getDot();
        
        // Set document.
        StyledDocument document = sec.getDocument();
        if(document == null) {
            // Shouldn't happen.
            if(Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                TopManager.getDefault().notifyException(new InternalError("I18N: InsertI18nAction: Document not initialized.")); // NOI18N
            dataObject = null;            
            return;
        }

        InsertI18nComponent insertComponent = getTopComponent();
        insertComponent.initialize(dataObject, document, position);

        insertComponent.open();
        insertComponent.requestFocus();
    }

    /** Gets <code>TopComponent</code>. */
    private InsertI18nComponent getTopComponent() {
        Object topComponent = null;
        
        if(topComponentSoftRef != null)
            topComponent = topComponentSoftRef.get();
        
        if(topComponent == null) {
            synchronized(this) {
                if(topComponent == null) {
                    topComponent = new InsertI18nComponent();
                    topComponentSoftRef = new SoftReference(topComponent);
                }
            }
        }
        
        return (InsertI18nComponent)topComponent;
    }
    
    /** Overrides superclass method.
    * @return true if action will be present 
    */
    protected boolean enable(Node[] activatedNodes) {
        if (!super.enable(activatedNodes))
            return false;
        
        // if has an open editor pane must not be in a guarded block
        // PENDING>>
        // It causes StackOverflowError
        // I18nSupport.isGuardedPosittion() checks teh way it causes change cookies (remove add SaveCookie), what
        // in turn calls back enable method, it calls isGueardedPosition again etc. etc.
        /*final SourceCookie.Editor sec = (SourceCookie.Editor)(activatedNodes[0]).getCookie(SourceCookie.Editor.class);        
        if (sec != null) {
            JEditorPane[] edits = sec.getOpenedPanes();
            if (edits != null && edits.length > 0) {
                int position = edits[0].getCaret().getDot();
                StyledDocument doc = sec.getDocument();
                DataObject obj = (DataObject)sec.getSource().getCookie(DataObject.class);
                if(I18nSupport.getI18nSupport(doc, obj).isGuardedPosition(position))
                    return false;
            }
        }*/
        // PENDING<<        
        
        return true;
    }

    /**
    * @return MODE_EXACTLY_ONE.
    */
    protected int mode () {
        return MODE_EXACTLY_ONE;
    }

    /**
    * @return ThreadCookie
    */
    protected Class[] cookieClasses () {
        return new Class [] {
            SourceCookie.Editor.class
        };
    }

    /** 
    * @return the action's icon 
    */
    public String getName() {
        return org.openide.util.NbBundle.getBundle(I18nModule.class).getString("CTL_InsertI18nString");
    }

    /** 
    * @return the action's help context 
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (InsertI18nStringAction.class);
    }

    /** The action's icon location.
    * @return the action's icon location
    */
    protected String iconResource () {
        return "/org/netbeans/modules/i18n/insertI18nStringAction.gif"; // NOI18N
    }
    
    /** Class which represents <code>TopConrnt</code> for <code>InsertI18nStringAction</code> action. */
    private class InsertI18nComponent extends TopComponent {

        /** DataObject in which document will be i18n string insterted. */
        private DataObject dataObject;
        /** Document where the string will be inserted. */
        private StyledDocument document;
        /** Position at which the string will be inserted. */
        private int position;
        /** Refernce to <code>ResourcebundlePanel</code>. */
        private ResourceBundlePanel rbPanel;
        
        /** Constructor. */
        public InsertI18nComponent() {
            initComponents();
            dockIntoI18nMode();
        }
        
        /** intis components. */
        private void initComponents() {
            rbPanel = new ResourceBundlePanel();
            JButton OKButton = new JButton(NbBundle.getBundle(I18nModule.class).getString("CTL_OKButton"));
            JButton cancelButton = new JButton(NbBundle.getBundle(I18nModule.class).getString("CTL_CancelButton"));
            
            // Actually create the dialog as top component.
            TopComponent topComponent = new TopComponent();

            topComponent.setCloseOperation(TopComponent.CLOSE_EACH);
            topComponent.setLayout(new BorderLayout());
            topComponent.add(rbPanel, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel(new GridBagLayout());

            // OK button.
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.anchor = GridBagConstraints.EAST;
            gridBagConstraints.fill = GridBagConstraints.NONE;
            gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 0.0;        
            buttonPanel.add(OKButton, gridBagConstraints); 

            // Cancel button.
            gridBagConstraints.gridx = 1;
            gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);        
            gridBagConstraints.weightx = 0.0;
            buttonPanel.add(cancelButton, gridBagConstraints);

            // Panel.
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 3;
            gridBagConstraints.fill = GridBagConstraints.NONE;
            gridBagConstraints.insets = new java.awt.Insets(5, 12, 11, 11);
            gridBagConstraints.weightx = 0.0;
            gridBagConstraints.weighty = 0.0;
            rbPanel.add(buttonPanel, gridBagConstraints);
            
            // Set listeners for buttons.
            OKButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        // OK button.
                        try {
                            ResourceBundleString newRbString = rbPanel.getResourceBundleString();

                            // Try to add key to bundle.
                            I18nUtil.addKeyToBundle(newRbString);
                            
                            // Create field in necessary.
                            I18nUtil.createField(newRbString, dataObject);
                            // Replace string.
                            document.insertString(position, I18nUtil.getReplaceJavaCode(newRbString, dataObject), null);
                            
                            InsertI18nComponent.this.close();
                        } catch (IllegalStateException e) {
                            NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                                NbBundle.getBundle(I18nModule.class).getString("EXC_BadKey"),
                                NotifyDescriptor.ERROR_MESSAGE);
                            TopManager.getDefault().notify(msg);
                        } catch (BadLocationException e) {
                            TopManager.getDefault().notify(
                                new NotifyDescriptor.Message(
                                    NbBundle.getBundle(I18nModule.class).getString("MSG_CantInsertInGuarded"),
                                    NotifyDescriptor.INFORMATION_MESSAGE
                                )
                            );
                        }
                    }
                }
            );

            cancelButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        InsertI18nComponent.this.close();
                    }
                }
            );

            setLayout(new BorderLayout());
            add(rbPanel, BorderLayout.CENTER);
        }
        
        /** Docks this <code>TopComponent into i18n mode. 
         * @see I18nSupport#I18N_Mode */
        private void dockIntoI18nMode() {
            // Dock into I18N mode.
            Workspace[] currentWs = TopManager.getDefault().getWindowManager().getWorkspaces();
            for(int i = currentWs.length; --i >= 0; ) {
                Mode i18nMode = currentWs[i].findMode(I18nManager.I18N_MODE);
                if(i18nMode == null) {
                    i18nMode = currentWs[i].createMode(
                        I18nManager.I18N_MODE,
                        NbBundle.getBundle(I18nModule.class).getString("CTL_I18nDialogTitle"),
                        InsertI18nStringAction.class.getResource("/org/netbeans/modules/i18n/I18nAction.gif") // NOI18N
                    );
                }
                i18nMode.dockInto(this);
            }
        }
        
        /** Initializes(resets) the top component.  */
        private void initialize(DataObject dataObject, StyledDocument document, int position) {
            this.dataObject = dataObject;
            this.document = document;
            this.position = position;
            
            setName(dataObject.getName());
            
            rbPanel.setTargetDataObject(dataObject);
            
            ResourceBundleString rbString = new ResourceBundleString();
            rbString.setResourceBundle(PropertiesModule.getLastBundleUsed());
            rbPanel.setResourceBundleString(rbString);
        }
  
    } // End of inner class InsertI18nTopComponent.
}
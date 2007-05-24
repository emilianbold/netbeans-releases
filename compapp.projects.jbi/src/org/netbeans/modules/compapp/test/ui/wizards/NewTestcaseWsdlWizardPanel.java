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
package org.netbeans.modules.compapp.test.ui.wizards;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.compapp.test.wsdl.WsdlSupport;
import java.awt.Component;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

public class NewTestcaseWsdlWizardPanel implements WizardDescriptor.ValidatingPanel, NewTestcaseConstants {
    private static final Logger mLog =
            Logger.getLogger("org.netbeans.modules.compapp.test.ui.wizards.NewTestcaseWsdlWizardPanel"); // NOI18N
    
    /**
     * The visual mComponent that displays this panel. If you need to access the
     * mComponent from this class, just use getComponent().
     */
    private NewTestcaseWsdlVisualPanel mComponent;
    private Project mProject;
    private FileObject mWsdlFile;
    private WsdlSupport mWsdlSupport;
    private WizardDescriptor wiz;
    
    public NewTestcaseWsdlWizardPanel(Project project) {
        mProject = project;
    }
    
    
    // Get the visual mComponent for the panel. In this template, the mComponent
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (mComponent == null) {
            mComponent = new NewTestcaseWsdlVisualPanel(mProject, this);
        }
        return mComponent;
    }
    
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx(SampleWizardPanel1.class);
    }
    
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        // return true;   
        // If it depends on some condition (form filled out...), then:
        // return someCondition();
        // and when this condition changes (last form field filled in...) then:
        // fireChangeEvent();
        // and uncomment the complicated stuff below.
        if (mComponent.getSelectedWsdlFile() == null) {
            wiz.putProperty("WizardPanel_errorMessage", 
                    NbBundle.getMessage(NewTestcaseNameVisualPanel.class, 
                    "LBL_One_WSDL_document_must_be_selected")); //NOI18N
            return false; // WSDL not selected
        }
                
        wiz.putProperty("WizardPanel_errorMessage", ""); //NOI18N
        return true;
    }
    
//    public final void addChangeListener(ChangeListener l) {}
//    public final void removeChangeListener(ChangeListener l) {}
    
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }
    
    
    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(Object settings) {
        wiz = (WizardDescriptor) settings;
    }
    
    public void storeSettings(Object settings) {
        try {
            FileObject wsdlFile = mComponent.getSelectedWsdlFile();
            if (wsdlFile == mWsdlFile) {
                return;
            }
            mWsdlFile = wsdlFile;
            WsdlSupport wsdlSupport = null;
            if (wsdlFile != null) {
                wsdlSupport = new WsdlSupport(wsdlFile);  // NOI18N  
            }
            mWsdlSupport = wsdlSupport;
            wiz = (WizardDescriptor) settings;
            wiz.putProperty(WSDL_SUPPORT, wsdlSupport);
            mLog.info("Writing WsdlSupport to WizardDescriptor"); // NOI18N
        } catch (Exception e) {
             ErrorManager.getDefault().notify(e);
        }
    }
    
    /**
     * Is called when Next of Finish buttons are clicked and
     * allows deeper check to find out that panel is in valid
     * state and it is ok to leave it.
     *
     * This Panel is valid if
     * 1. mComponent.getSelectedWsdlFile() is not null
     *
     *
     * @since 4.28
     * @throws WizardValidationException when validation fails
     */
    public void validate() throws WizardValidationException {
        
        if (mWsdlSupport != null) {
            // 1. check if wsdl support creation
            if (mWsdlSupport.getWsdlSupportError().length() != 0) {
                String msg = mWsdlSupport.getWsdlSupportError();
                NotifyDescriptor d =
                        new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                String msg2 = NbBundle.getMessage(NewTestcaseWsdlWizardPanel.class,
                        "MSG_Error_Load_WSDL"); // NOI18N
                throw new WizardValidationException(mComponent.getWsdlTreeView(), msg2, msg2);
            }
            
            try {
                // TODO: fixme
//                // 2. Check for missing binding
//                List<Binding> bindings = Util.getSortedBindings(mWsdlSupport.getWsdlModel().getDefinitions());
//                for (Binding binding : bindings) {
//                    List<Operation> opList = Util.getSortedOperations(binding);
//                }
            } catch (Exception e) {
                String msg = NbBundle.getMessage(NewTestcaseWsdlWizardPanel.class,
                        "LBL_No_Binding_Found");   // NOI18N
                // binding not found.
                throw new WizardValidationException(mComponent.getWsdlTreeView(), msg, msg);
            }
            
            mWsdlSupport.setWsdlSupportError("");
        }
    }
}
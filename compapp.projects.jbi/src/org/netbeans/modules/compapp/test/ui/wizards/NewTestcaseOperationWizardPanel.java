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

import org.netbeans.modules.compapp.test.wsdl.BindingSupport;
import org.netbeans.modules.compapp.test.wsdl.BindingSupportFactory;
import org.netbeans.modules.compapp.test.wsdl.SoapBindingSupportFactory;
import org.netbeans.modules.compapp.test.wsdl.WsdlSupport;
import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class NewTestcaseOperationWizardPanel implements WizardDescriptor.ValidatingPanel, 
                                                        NewTestcaseConstants 
{
    private static final Logger mLog = 
        Logger.getLogger("org.netbeans.modules.compapp.test.ui.wizards.NewTestcaseOperationWizardPanel"); // NOI18N
    
    private static BindingSupportFactory[] mBindingSupportFactories = new BindingSupportFactory[] {
        new SoapBindingSupportFactory(),
    };

    /**
     * The visual mComponent that displays this panel. If you need to access the
     * mComponent from this class, just use getComponent().
     */
    private NewTestcaseOperationVisualPanel mComponent;
    private WsdlSupport mWsdlSupport;
    private WizardDescriptor wiz;
    private boolean mWsdlSupportInitialized = false;
    
    // Get the visual mComponent for the panel. In this template, the mComponent
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (mComponent == null) {
            mComponent = new NewTestcaseOperationVisualPanel(this);
        }
        if (mWsdlSupport != null && ! mWsdlSupportInitialized) {
            mComponent.setWsdlSupport(mWsdlSupport);
            mWsdlSupportInitialized = true;
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
        
        if (mComponent.getSelectedBindingOperation() == null) {
            wiz.putProperty("WizardPanel_errorMessage",
                    NbBundle.getMessage(NewTestcaseOperationVisualPanel.class,
                    "LBL_One_operation_must_be_selected")); //NOI18N
            return false; // WSDL not selected
        }
        
        boolean isSupported = false;
        Binding binding = mComponent.getSelectedBinding();
        for (int i = 0; i < mBindingSupportFactories.length; i++) {
            if (mBindingSupportFactories[i].supports(binding)) {
                isSupported = true;
                break;
            }
        }
        if (!isSupported) {
            String msg = NbBundle.getMessage(NewTestcaseOperationWizardPanel.class,
                    "LBL_Binding_is_not_supported", binding.getName()); // NOI18N 
            wiz.putProperty("WizardPanel_errorMessage", msg); // NOI18N
            return false;
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
        mWsdlSupport = (WsdlSupport) wiz.getProperty(WSDL_SUPPORT);
        if (mWsdlSupport == null) {
            mLog.info(NbBundle.getMessage(NewTestcaseOperationWizardPanel.class,
                    "LBL_No_WSDL_document_is_provided")); // NOI18N
        }
    }
    
    public void storeSettings(Object settings) {
        BindingSupport bindingSupport = null;
        Binding binding = mComponent.getSelectedBinding();
        if (binding != null) {
            for (int i = 0; i < mBindingSupportFactories.length; i++) {
                if (mBindingSupportFactories[i].supports(binding)) {
                    try {
                        bindingSupport = 
                                mBindingSupportFactories[i].createBindingSupport(
                                binding,
                                mWsdlSupport.getWsdlModel().getDefinitions(), 
                                mWsdlSupport.getSchemaTypeLoader());
                        break;
                    } catch (Exception e) {
                    }
                }
            }
        }
        wiz = (WizardDescriptor) settings;
        wiz.putProperty(BINDING_SUPPORT, bindingSupport);
        wiz.putProperty(BINDING_OPERATION, mComponent.getSelectedBindingOperation());
    }
    
    /**
     * Is called when Next of Finish buttons are clicked and
     * allows deeper check to find out that panel is in valid
     * state and it is ok to leave it.
     * 
     * This Panel is valid if
     * 1. mComponent.getSelectedBindingOperation() is not null
     * 2. mComponent.getSelectedBinding() is supported
     * 
     * 
     * @since 4.28
     * @throws WizardValidationException when validation fails
     */
    public void validate() throws WizardValidationException {
        /*
        // 1. mComponent.getSchemaName() is not ""
        if (mComponent.getSelectedBindingOperation() == null) {
            String msg = NbBundle.getMessage(NewTestcaseOperationWizardPanel.class,
                    "LBL_One_operation_must_be_selected"); // NOI18N
            throw new WizardValidationException(mComponent.getBindingTree(), msg, msg);
        }
        boolean isSupported = false;
        Binding binding = mComponent.getSelectedBinding();
        for (int i = 0; i < mBindingSupportFactories.length; i++) {
            if (mBindingSupportFactories[i].supports(binding)) {
                isSupported = true;
                break;
            }
        }
        if (!isSupported) {
            String msg = NbBundle.getMessage(NewTestcaseOperationWizardPanel.class,
                    "LBL_Binding_is_not_supported", binding.getQName().toString()); // NOI18N
            throw new WizardValidationException(mComponent.getBindingTree(), msg, msg);
        }
        */        
    }
}


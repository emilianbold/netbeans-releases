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


package org.netbeans.modules.i18n.wizard;


import java.awt.Dialog;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.ArrayList;

import org.netbeans.modules.i18n.I18nUtil;

import org.openide.nodes.Node;
import org.openide.TopManager;
import org.openide.util.actions.NodeAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.WizardDescriptor;


/**
 * Action which runs i18n wizard.
 *
 * @author  Peter Zavadsky
 */
public class I18nWizardAction extends NodeAction {

    /** Generated serial version UID. */
    static final long serialVersionUID = 6965968608028644524L;

    /** Weak reference to dialog. */
    private WeakReference dialogWRef = new WeakReference(null);

    
    /** Implements superclass abstract method. 
     * @return <code>true</code> */
    protected boolean enable(Node[] activatedNodes) {
        return true;
    }
    
    /** Actually performs action. Implements superclass abstract method. */
    protected void performAction(Node[] activatedNodes) {
        Dialog dialog = (Dialog)dialogWRef.get();
        
        if(dialog != null) {
            dialog.setVisible(false);
            dialog.dispose();
        }

        WizardDescriptor wizardDesc = I18nWizardDescriptor.createI18nWizardDescriptor(
            getWizardIterator(),
            I18nUtil.createWizardSettings(activatedNodes)
        );

        initWizard(wizardDesc);
        
        dialog = TopManager.getDefault().createDialog(wizardDesc);
        
        dialog.show();

        dialogWRef = new WeakReference(dialog);        
    }

    /** Gets wizard iterator thru panels used in wizard invoked by this action, 
     * i.e I18N wizard. */
    private WizardDescriptor.Iterator getWizardIterator() {
        ArrayList panels = new ArrayList(4);
        
        panels.add(new SourceWizardPanel.Panel());
        panels.add(new ResourceWizardPanel.Panel());
        
        if(I18nUtil.getOptions().isAdvancedWizard())
            panels.add(new AdditionalWizardPanel.Panel());
        
        panels.add(new HardStringWizardPanel.Panel());
        
        return new WizardDescriptor.ArrayIterator(
            (WizardDescriptor.Panel[])panels.toArray(new WizardDescriptor.Panel[panels.size()]));
    }

    /** Initializes wizard descriptor. */
    private void initWizard(WizardDescriptor wizardDesc) {
        // Init properties.
        wizardDesc.putProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); // NOI18N
        wizardDesc.putProperty("WizardPanel_contentDisplayed", Boolean.TRUE); // NOI18N
        wizardDesc.putProperty("WizardPanel_contentNumbered", Boolean.TRUE); // NOI18N

        ArrayList contents = new ArrayList(4);
        contents.add(NbBundle.getBundle(I18nWizardAction.class).getString("TXT_SelectSourcesHelp"));
        contents.add(NbBundle.getBundle(I18nWizardAction.class).getString("TXT_SelectResourceHelp"));
        
        if(I18nUtil.getOptions().isAdvancedWizard())
            contents.add(NbBundle.getBundle(I18nWizardAction.class).getString("TXT_AdditionalHelp"));
        
        contents.add(NbBundle.getBundle(I18nWizardAction.class).getString("TXT_FoundStringsHelp"));
        
        wizardDesc.putProperty("WizardPanel_contentData", (String[])contents.toArray(new String[contents.size()])); // NOI18N
        
        wizardDesc.setTitle(NbBundle.getBundle(I18nWizardAction.class).getString("LBL_WizardTitle"));
        wizardDesc.setTitleFormat(new MessageFormat("{0} ({1})")); // NOI18N

        wizardDesc.setModal(false);
    }

    /** Gets localized name of action. Overrides superclass method. */
    public String getName() {
        return NbBundle.getBundle(I18nWizardAction.class).getString("LBL_WizardActionName");
    }

    /** Gets the action's icon location.
     * @return the action's icon location */
    protected String iconResource () {
        return "/org/netbeans/modules/i18n/i18nAction.gif"; // NOI18N
    }
    
    /** Gets the action's help context. Implemenst superclass abstract method. */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(I18nUtil.HELP_ID_WIZARD);
    }
    
}

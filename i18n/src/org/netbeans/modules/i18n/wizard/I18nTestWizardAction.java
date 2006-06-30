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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.i18n.wizard;

import java.awt.Dialog;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.ArrayList;

import org.netbeans.modules.i18n.I18nUtil;

import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.WizardDescriptor;
import org.openide.DialogDisplayer;
import org.netbeans.api.project.Project;

/**
 * Action which runs i18n test wizard.
 *
 * @author  Peter Zavadsky
 * @author  Petr Kuzel
 */
public class I18nTestWizardAction extends NodeAction {

    public I18nTestWizardAction() {
        putValue("noIconInMenu", Boolean.TRUE);
    }
    
    /** Generated serial version UID. */
    static final long serialVersionUID = -3265587506739081248L;

    /** Weak reference to dialog. */
    private static WeakReference dialogWRef = new WeakReference(null);
    
    
    /** 
     * We create non-modal but not rentrant dialog. Wait until
     * previous one is closed.
     */
    protected boolean enable(Node[] activatedNodes) {

        if (Util.wizardEnabled(activatedNodes) == false) {
            return false;
        }
        
        Dialog previous = (Dialog) dialogWRef.get();
        if (previous == null) return true;
        return previous.isVisible() == false;
    }
    
    /** 
     * Popup non modal wizard.
     */
    protected void performAction(Node[] activatedNodes) {
        Dialog dialog = (Dialog)dialogWRef.get();
        
        if(dialog != null) {
            dialog.setVisible(false);
            dialog.dispose();
        }

        Project project = org.netbeans.modules.i18n.Util.getProjectFor(activatedNodes);
	if (project == null) return;

        WizardDescriptor wizardDescriptor = I18nWizardDescriptor.createI18nWizardDescriptor(
            getWizardIterator(),
            new I18nWizardDescriptor.
	    Settings(Util.createWizardSourceMap(activatedNodes),
		     project)

        );

        initWizard(wizardDescriptor);
        
        dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialogWRef = new WeakReference(dialog);
        dialog.setVisible(true);
    }

    /** Gets wizard iterator thru panels used in wizard invoked by this action, 
     * i.e I18N wizard. */
    private WizardDescriptor.Iterator getWizardIterator() {
        WizardDescriptor.Panel[] panels = new WizardDescriptor.Panel[3];
        
        panels[0] = new SourceWizardPanel.Panel(true);
        panels[1] = new ResourceWizardPanel.Panel(true);
        panels[2] = new TestStringWizardPanel.Panel();
        
        return new WizardDescriptor.ArrayIterator(panels);
            
    }
    
    /** Initializes wizard descriptor. */
    private void initWizard(WizardDescriptor wizardDesc) {
        // Init properties.
        wizardDesc.putProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);    // NOI18N
        wizardDesc.putProperty("WizardPanel_contentDisplayed", Boolean.TRUE);   // NOI18N
        wizardDesc.putProperty("WizardPanel_contentNumbered", Boolean.TRUE);    // NOI18N

        ArrayList contents = new ArrayList(3);
        contents.add(Util.getString("TXT_SelectTestSources"));
        contents.add(Util.getString("TXT_SelectTestResources"));
        contents.add(Util.getString("TXT_FoundMissingResources"));
        
        wizardDesc.putProperty("WizardPanel_contentData", (String[])contents.toArray(new String[contents.size()])); // NOI18N
        
        wizardDesc.setTitle(Util.getString("LBL_TestWizardTitle"));
        wizardDesc.setTitleFormat(new MessageFormat("{0} ({1})"));              // NOI18N

        wizardDesc.setModal(false);
    }
    
    /** Gets localized name of action. Overrides superclass method. */
    public String getName() {
        return Util.getString("LBL_TestWizardActionName");
    }
    
    /** Gets the action's help context. Implemenst superclass abstract method. */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(I18nUtil.HELP_ID_TESTING);
    }

    protected boolean asynchronous() {
      return false;
    }
    

}

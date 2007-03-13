/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.uml.ui.controls.newdialog;

import java.awt.Component;
import java.util.Arrays;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.uml.common.Util;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.ui.support.NewElementKind;
import org.netbeans.modules.uml.util.StringTokenizer2;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

public class AddElementWizardPanel1 implements WizardDescriptor.Panel {
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private Component component;
    private INewDialogElementDetails m_details;
    private AddElementVisualPanel1 panelComponent = null;
    private boolean resultFlag = false;
    
    public AddElementWizardPanel1(INewDialogElementDetails details) {
        super();
        this.m_details = details;
    }
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new AddElementVisualPanel1(m_details);
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx(SampleWizardPanel1.class);
    }
    
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        return true;
        // If it depends on some condition (form filled out...), then:
        // return someCondition();
        // and when this condition changes (last form field filled in...) then:
        // fireChangeEvent();
        // and uncomment the complicated stuff below.
    }
    
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}
    /*
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
     */
    
    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(Object settings) {    
    }
    
    public void storeSettings(Object settings) {
        if (resultFlag) {
            return;
        }
        //get the user selected data from the panel
        INewDialogElementDetails details = getResults();
        if (details != null) {            
            //set the details object
            WizardDescriptor wizDesc = (WizardDescriptor)settings;
            wizDesc.putProperty(AddElementWizardIterator.ELEMENT_DETAILS, details);
        }
    }
    
    
    private INewDialogElementDetails getResults() {       
        
        if (component instanceof AddElementVisualPanel1) {
            panelComponent = (AddElementVisualPanel1)component;
        }
        
        INewDialogElementDetails details = null;
        ETPairT< Boolean, String > pair = validData();
        boolean valid = pair.getParamOne().booleanValue();
        String msg = pair.getParamTwo();
        if (valid && msg == null) {
            details = new NewDialogElementDetails();
            
            // Get the kind of Element to create
            String selOnTab = (String)panelComponent.getSelectedListElement();
            
            // CR#6263225 cvc
            //  added arrays to NewElementKind to make the
            //  maintenance of adding/changing/removing elements much easier
            //	switch/case logic no longer needed
            
            String eleDisplayName =
                    StringTokenizer2.replace(selOnTab, " ", ""); // NOI18N
            
            List eleNameList = Arrays.asList(NewElementKind.ELEMENT_NAMES);
            int index = eleNameList.indexOf(eleDisplayName);
            
            if (index == -1)
                // "None" element type
                details.setElementKind(
                        NewElementKind.ELEMENT_NUMBERS[0].intValue());
            else
                details.setElementKind(
                        NewElementKind.ELEMENT_NUMBERS[index].intValue());
            
            //get the name
            details.setName(panelComponent.getElementName());
            
            // Get the namespace
            INamespace pSelectedNamespace =
                    NewDialogUtilities.getNamespace(
                    (String)panelComponent.getSelectedNamespace());
            details.setNamespace(pSelectedNamespace);
        }        
        else {
            if (msg != null && msg.length() > 0) {
                //show error message
                NotifyDescriptor.Message notifyDesc =
                        new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(notifyDesc);
            }
        }
        resultFlag = true;
        return details;
    }
    
    private ETPairT< Boolean, String > validData() {
        boolean bDataIsValid = false;
        String message = null;
        ETPairT< Boolean, String > retVals = null;
        
        String sElementName = panelComponent.getElementName();
        if (sElementName != null && sElementName.length() > 0) {
            // Get the namespace
            INamespace pSelectedNamespace = NewDialogUtilities
                    .getNamespace((String)panelComponent.getSelectedNamespace());
            if ( pSelectedNamespace != null ) {
                bDataIsValid = true;
                int selectedItem = panelComponent.getSelectedListIndex();
                if (selectedItem == -1) {
                    // Nothing selected
                    message = NewDialogResources
                            .getString("IDS_PLEASESELECTAELEMENT"); // NOI18N
                    bDataIsValid = false;
                }
                
                if (bDataIsValid) {
                    String selOnTab = (String)panelComponent.getSelectedListElement();
                    
                    // CR#6263225 - cvc
                    //  naming conventions for elements makes this easy
                    //  the display name, "Abc Xyz" should correspond to its
                    //  id name "AbcXyz" (removed spaces)
                    String sElementTypeToLookFor =
                            StringTokenizer2.replace(selOnTab, " ", ""); // NOI18N
                    
                    if (sElementTypeToLookFor != null &&
                            sElementTypeToLookFor.length() > 0) {
                        if (Util.hasNameCollision(pSelectedNamespace,sElementName, sElementTypeToLookFor, null)) {
                            bDataIsValid = false;
                            DialogDisplayer.getDefault().notify(
                                    new NotifyDescriptor.Message(NbBundle.getMessage(
                                    AddElementWizardPanel1.class, "IDS_NAMESPACECOLLISION")));
                        }
                    }
                }
            } else {
                // Something bad happened.  We added a namespace to the combo
                // and couldn't get it back out!
                message = NewDialogResources.getString(
                        "IDS_FAILEDTOGETNAMESPACE"); // NOI18N
            }
        } else {
            message = NewDialogResources.getString(
                    "IDS_PLEASEENTERELEMENTNAME"); // NOI18N
        }
        
        if( null == retVals ) {
            retVals = new ETPairT<Boolean, String>(
                    new Boolean(bDataIsValid), message );
        }
        
        return retVals;
    }

}


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

package org.netbeans.modules.autoupdate.ui.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.swing.JButton;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.UpdateElement;
import org.openide.WizardDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Rechtacek
 */
public abstract class OperationWizardModel {
    private Set<UpdateElement> primaryElements;
    private Set<UpdateElement> requiredElements = null;
    private Set<UpdateElement> allElements = null;
    private JButton originalCancel = null;
    private JButton originalNext = null;
    private JButton originalFinish = null;
    private boolean reconized = false;
    
    abstract OperationType getOperation ();
    abstract OperationContainer getContainer ();
    
    public static enum OperationType {
        /** Install <code>UpdateElement</code> */
        INSTALL,
        /** Uninstall <code>UpdateElement</code> */
        UNINSTALL,
        /** Update installed <code>UpdateElement</code> to newer version. */
        UPDATE,
        /** Rollback installed <code>UpdateElement</code> to previous version. */
        REVERT,
        /** Enable <code>UpdateElement</code> */
        ENABLE,
        /** Disable <code>UpdateElement</code> */
        DISABLE
    }
    
    public Set<UpdateElement> getPrimaryUpdateElements () {
        if (primaryElements == null) {
            assert getContainer () != null;
            primaryElements = new HashSet<UpdateElement> ();
            List<OperationInfo> l = (List<OperationInfo>) getContainer ().listAll ();
            for (OperationInfo info : l) {
                primaryElements.add (info.getUpdateElement ());
            }
        }
        return primaryElements;
    }
    
    public boolean hasRequiredUpdateElements () {
        return ! getRequiredUpdateElements ().isEmpty ();
    }
    
    public Set<UpdateElement> getRequiredUpdateElements () {
        if (requiredElements == null) {
            requiredElements = new HashSet<UpdateElement> ();
            
            List<OperationInfo> l = (List<OperationInfo>) getContainer ().listAll ();
            for (OperationInfo info : l) {
                requiredElements.addAll (info.getRequiredElements ());
            }
            
            // add requiredElements to container
            getContainer ().add (requiredElements);
            
        }
        return requiredElements;
    }
    
    public boolean hasBrokenDependencies () {
        return ! getBrokenDependencies ().isEmpty ();
    }
    
    public SortedMap<String, Set<String>> getBrokenDependencies () {
        SortedMap<String, Set<String>> brokenDeps = new TreeMap<String, Set<String>> ();

        List<OperationInfo> l = (List<OperationInfo>) getContainer ().listAll ();
        for (OperationInfo info : l) {
            Set<String> broken = info.getBrokenDependencies ();
            if (! broken.isEmpty()) {
                brokenDeps.put (info.getUpdateElement ().getDisplayName (),
                        new HashSet<String> (broken));
            }
        }
        return brokenDeps;
    }
    
    public Set<UpdateElement> getAllUpdateElements () {
        if (allElements == null) {
            allElements = new HashSet<UpdateElement> (getPrimaryUpdateElements ());
            allElements.addAll (getRequiredUpdateElements ());
            assert allElements.size () == getPrimaryUpdateElements ().size () + getRequiredUpdateElements ().size () :
                "Primary [" + getPrimaryUpdateElements ().size () + "] plus " +
                "Required [" + getRequiredUpdateElements ().size () + "] is All [" + allElements.size () + "] ";
        }
        return allElements;
    }
    
    // XXX Hack in WizardDescriptor
    public void modifyOptionsForDoClose (WizardDescriptor wd) {
        recognizeButtons (wd);
        JButton b = getOriginalCancel (wd);
        Mnemonics.setLocalizedText (b, getBundle ("InstallUnitWizardModel_Buttons_Close"));
        wd.setOptions (new JButton [] {b});
    }
    
    // XXX Hack in WizardDescriptor
    public void modifyOptionsForStartWizard (WizardDescriptor wd) {
        recognizeButtons (wd);
        removeFinish (wd);
        Mnemonics.setLocalizedText (getOriginalNext (wd), NbBundle.getMessage (InstallUnitWizardModel.class,
                "InstallUnitWizardModel_Buttons_MnemonicNext", getBundle ("InstallUnitWizardModel_Buttons_Next")));
    }
    
    // XXX Hack in WizardDescriptor
    public void modifyOptionsForDoOperation (WizardDescriptor wd) {
        recognizeButtons (wd);
        removeFinish (wd);
        switch (getOperation ()) {
            case INSTALL :
                Mnemonics.setLocalizedText (getOriginalNext (wd), getBundle ("InstallUnitWizardModel_Buttons_Install"));
                break;
            case UPDATE :
                Mnemonics.setLocalizedText (getOriginalNext (wd), getBundle ("InstallUnitWizardModel_Buttons_Update"));
                break;
            case UNINSTALL :
                Mnemonics.setLocalizedText (getOriginalNext (wd), getBundle ("UninstallUnitWizardModel_Buttons_Uninstall"));
                break;
            case ENABLE :
                Mnemonics.setLocalizedText (getOriginalNext (wd), getBundle ("UninstallUnitWizardModel_Buttons_TurnOn"));
                break;
            case DISABLE :
                Mnemonics.setLocalizedText (getOriginalNext (wd), getBundle ("UninstallUnitWizardModel_Buttons_TurnOff"));
                break;
            default:
                assert false : "Unknown operationType " + getOperation ();
        }
    }
    
    // XXX Hack in WizardDescriptor
    public JButton getCancelButton (WizardDescriptor wd) {
        return getOriginalCancel (wd);
    }
    
    // XXX Hack in WizardDescriptor
    public void modifyOptionsForDisabledCancel (WizardDescriptor wd) {
        recognizeButtons (wd);
        Object [] options = wd.getOptions ();
        List<JButton> newOptionsL = new ArrayList<JButton> ();
        List<Object> optionsL = Arrays.asList (options);
        for (Object o : optionsL) {
            assert o instanceof JButton : o + " instanceof JButton";
            if (o instanceof JButton) {
                JButton b = (JButton) o;
                if (b.equals (getOriginalCancel (wd))) {
                    JButton disabledCancel = new JButton (b.getText ());
                    disabledCancel.setEnabled (false);
                    newOptionsL.add (disabledCancel);
                } else {
                    newOptionsL.add (b);
                }
            }
        }
        wd.setOptions (newOptionsL.toArray ());
    }
    
    private void recognizeButtons (WizardDescriptor wd) {
        if (! reconized) {
            Object [] options = wd.getOptions ();
            assert options != null && options.length >= 4;
            assert options [1] instanceof JButton : options [1] + " instanceof JButton";
            originalNext = (JButton) options [1];
            assert options [2] instanceof JButton : options [2] + " instanceof JButton";
            originalFinish = (JButton) options [2];
            assert options [3] instanceof JButton : options [3] + " instanceof JButton";
            originalCancel = (JButton) options [3];
            reconized = true;
        }
        
    }
    
    private JButton getOriginalNext (WizardDescriptor wd) {
        return originalNext;
    }
    
    private JButton getOriginalCancel (WizardDescriptor wd) {
        return originalCancel;
    }
    
    private void removeFinish (WizardDescriptor wd) {
        Object [] options = wd.getOptions ();
        List<JButton> newOptionsL = new ArrayList<JButton> ();
        List<Object> optionsL = Arrays.asList (options);
        for (Object o : optionsL) {
            assert o instanceof JButton : o + " instanceof JButton";
            if (o instanceof JButton) {
                JButton b = (JButton) o;
                if (! b.equals (originalFinish)) {
                    newOptionsL.add (b);
                }
            }
        }
        wd.setOptions (newOptionsL.toArray ());
    }
    
    private String getBundle (String key) {
        return NbBundle.getMessage (InstallUnitWizardModel.class, key);
    }
}

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

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.modules.autoupdate.ui.Containers;
import org.netbeans.modules.autoupdate.ui.Utilities;
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
    private Set<UpdateElement> customHandledElements = null;
    private Set<UpdateElement> allElements = null;
    private JButton originalCancel = null;
    private JButton originalNext = null;
    private JButton originalFinish = null;
    private boolean reconized = false;
    static Dimension PREFFERED_DIMENSION = new Dimension (500, 550);
    
    abstract OperationType getOperation ();
    abstract OperationContainer getBaseContainer ();
    abstract OperationContainer<OperationSupport> getCustomHandledContainer ();
    
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
        DISABLE,
        /** Install or update <code>UpdateElement</code> from local NBM. */
        LOCAL_DOWNLOAD
    }
    
    public Set<UpdateElement> getPrimaryUpdateElements () {
        if (primaryElements == null) {
            primaryElements = new HashSet<UpdateElement> ();
            for (OperationInfo<?> info : getStandardInfos ()) {
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
            
            for (OperationInfo<?> info : getStandardInfos ()) {
                requiredElements.addAll (info.getRequiredElements ());
            }
            
            // add requiredElements to container
            addRequiredElements (requiredElements);
            
        }
        return requiredElements;
    }
    
    public boolean hasBrokenDependencies () {
        return ! getBrokenDependencies ().isEmpty ();
    }
    
    public boolean hasCustomComponents () {
        return ! getCustomHandledContainer ().listAll ().isEmpty ();
    }
    
    public boolean hasStandardComponents () {
        return ! getBaseContainer ().listAll ().isEmpty ();
    }
    
    public Set<UpdateElement> getCustomHandledComponents () {
        if (customHandledElements == null) {
            customHandledElements = new HashSet<UpdateElement> ();
            
            for (OperationInfo<?> info : getCustomHandledInfos ()) {
                customHandledElements.add (info.getUpdateElement ());
                customHandledElements.addAll (info.getRequiredElements ());
            }
        }
        return customHandledElements;
    }
    
    private List<OperationInfo<OperationSupport>> getCustomHandledInfos () {
        return getCustomHandledContainer ().listAll ();
    }
    
    @SuppressWarnings("unchecked")
    private List<OperationInfo> getStandardInfos () {
        List<OperationInfo> infos = new ArrayList<OperationInfo> ();
        if (OperationType.LOCAL_DOWNLOAD == getOperation ()) {
            infos.addAll (Containers.forAvailableNbms ().listAll ());
            infos.addAll (Containers.forUpdateNbms ().listAll ());
        } else {
            infos.addAll (getBaseContainer ().listAll ());
        }
        return infos;
    }
    
    public SortedMap<String, Set<String>> getBrokenDependencies () {
        SortedMap<String, Set<String>> brokenDeps = new TreeMap<String, Set<String>> ();

        for (OperationInfo<?> info : listAll ()) {
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
    
    public static Set<UpdateElement> getVisibleUpdateElements (Set<UpdateElement> all, boolean canBeEmpty, OperationType operationType) {
        if (Utilities.modulesOnly () || OperationType.LOCAL_DOWNLOAD == operationType) {
            return all;
        } else {
            Set<UpdateElement> visible = new HashSet<UpdateElement> ();
            for (UpdateElement el : all) {
                if (UpdateManager.TYPE.KIT_MODULE == el.getUpdateUnit ().getType ()) {
                    visible.add (el);
                }
            }
            if (visible.isEmpty () && ! canBeEmpty) {
                // in Downloaded tab may become all NBMs are hidden
                visible = all;
            }
            return visible;
        }
    }
    
    // XXX Hack in WizardDescriptor
    public void modifyOptionsForDoClose (WizardDescriptor wd) {
        recognizeButtons (wd);
        JButton b = getOriginalFinish (wd);
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
    
    public void modifyOptionsForContinue (WizardDescriptor wd, boolean canFinish) {
        if (canFinish) {
            recognizeButtons (wd);
            JButton b = getOriginalFinish (wd);
            Mnemonics.setLocalizedText (b, getBundle ("InstallUnitWizardModel_Buttons_Close"));
            wd.setOptions (new JButton [] {b});
        } else {
            recognizeButtons (wd);
            removeFinish (wd);
            Mnemonics.setLocalizedText (getOriginalNext (wd), NbBundle.getMessage (InstallUnitWizardModel.class,
                    "InstallUnitWizardModel_Buttons_MnemonicNext", getBundle ("InstallUnitWizardModel_Buttons_Next")));
        }
    }
    
    // XXX Hack in WizardDescriptor
    public void modifyOptionsForDoOperation (WizardDescriptor wd) {
        recognizeButtons (wd);
        removeFinish (wd);
        switch (getOperation ()) {
        case LOCAL_DOWNLOAD :
            if (Containers.forUpdateNbms ().listAll ().isEmpty ()) {
                Mnemonics.setLocalizedText (getOriginalNext (wd), getBundle ("InstallUnitWizardModel_Buttons_Install"));
            } else {
                Mnemonics.setLocalizedText (getOriginalNext (wd), getBundle ("InstallUnitWizardModel_Buttons_Update"));
            }
            break;
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
    
    public void doCleanup (boolean cancel) throws OperationException {
        getBaseContainer ().removeAll ();
        getCustomHandledContainer ().removeAll ();
    }
    
    @SuppressWarnings("unchecked")
    private Set<OperationInfo> listAll () {
        Set<OperationInfo> infos = new HashSet<OperationInfo> ();
        infos.addAll (getStandardInfos ());
        infos.addAll (getCustomHandledInfos ());
        return infos;
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
    
    private JButton getOriginalFinish (WizardDescriptor wd) {
        return originalFinish;
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
    
    private void addRequiredElements (Set<UpdateElement> elems) {
        for (UpdateElement el : elems) {
            if (el == null || el.getUpdateUnit () == null) {
                Logger.getLogger (OperationWizardModel.class.getName ()).log (Level.INFO, "UpdateElement " + el + " cannot be null"
                        + (el == null ? "" : " or UpdateUnit " + el.getUpdateUnit () + " cannot be null"));
            }
            if (UpdateManager.TYPE.CUSTOM_HANDLED_COMPONENT == el.getUpdateUnit ().getType ()) {
                getCustomHandledContainer ().add (el);
            } else {
                getBaseContainer ().add (el);
            }
        }
    }
    
    private String getBundle (String key) {
        return NbBundle.getMessage (InstallUnitWizardModel.class, key);
    }
}

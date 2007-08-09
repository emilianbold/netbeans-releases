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

package org.netbeans.modules.autoupdate.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import org.netbeans.api.autoupdate.*;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.modules.autoupdate.updateprovider.InstalledModuleProvider;
import org.openide.modules.ModuleInfo;

/**
 *
 * @author Radek Matous
 */
public final class OperationContainerImpl<Support> {
    private OperationContainer<Support> container;
    private OperationContainerImpl () {}
    private List<OperationInfo<Support>> operations = new ArrayList<OperationInfo<Support>>();
    public static OperationContainerImpl<InstallSupport> createForInstall () {
        return new OperationContainerImpl<InstallSupport> (OperationType.INSTALL);
    }
    public static OperationContainerImpl<InstallSupport> createForUpdate () {
        return new OperationContainerImpl<InstallSupport> (OperationType.UPDATE);
    }
    public static OperationContainerImpl<OperationSupport> createForDirectInstall () {
        return new OperationContainerImpl<OperationSupport> (OperationType.INSTALL);
    }
    public static OperationContainerImpl<OperationSupport> createForDirectUpdate () {
        return new OperationContainerImpl<OperationSupport> (OperationType.UPDATE);
    }
    public static OperationContainerImpl<OperationSupport> createForUninstall () {
        return new OperationContainerImpl<OperationSupport> (OperationType.UNINSTALL);
    }
    public static OperationContainerImpl<OperationSupport> createForEnable () {
        return new OperationContainerImpl<OperationSupport> (OperationType.ENABLE);
    }
    public static OperationContainerImpl<OperationSupport> createForDisable () {
        return new OperationContainerImpl<OperationSupport> (OperationType.DISABLE);
    }
    public static OperationContainerImpl<OperationSupport> createForInstallNativeComponent () {
        return new OperationContainerImpl<OperationSupport> (OperationType.CUSTOM_INSTALL);
    }
    public static OperationContainerImpl<OperationSupport> createForUninstallNativeComponent () {
        return new OperationContainerImpl<OperationSupport> (OperationType.CUSTOM_INSTALL);
    }
    @SuppressWarnings("unchecked")
    public OperationInfo<Support> add (UpdateUnit updateUnit, UpdateElement updateElement) throws IllegalArgumentException {
        OperationInfo<Support> retval = null;
        boolean isValid = isValid (updateUnit, updateElement);
        if (!isValid) {
            throw new IllegalArgumentException ("Invalid " + updateElement.getCodeName () + " for operation " + type);
        }
        if (type == OperationType.INSTALL || type == OperationType.UPDATE) {
            if (UpdateUnitFactory.getDefault().isScheduledForRestart (updateElement)) {
                Logger.getLogger(this.getClass ().getName ()).log (Level.INFO, updateElement + " is scheduled for restart IDE.");
                return null;
            }
        }
        if (isValid) {
            if (type == OperationType.UNINSTALL || type == OperationType.ENABLE || type == OperationType.DISABLE) {
                if (updateUnit.getInstalled () != updateElement) {
                    throw new IllegalArgumentException (updateUnit.getInstalled () + " and " + updateElement + "must be same for operation " + type);
                }
            } else {
                if (updateUnit.getInstalled () == updateElement) {
                    throw new IllegalArgumentException (updateUnit.getInstalled () + " and " + updateElement + "cannot be same for operation " + type);
                }
            }
        }
        synchronized(this) {
            if (!contains (updateUnit, updateElement)) {
                retval = Trampoline.API.createOperationInfo (new OperationInfoImpl<Support> (updateUnit, updateElement));
                operations.add (retval);
            }
        }
        return retval;
    }
    public boolean remove (UpdateElement updateElement) {
        OperationInfo toRemove = find (updateElement);
        if (toRemove != null) {
            remove (toRemove);
        }
        return toRemove != null;
    }
    
    public boolean contains (UpdateElement updateElement) {
        return find (updateElement) != null;
    }
    
    public void setOperationContainer (OperationContainer<Support> container) {
        this.container = container;
    }
    
    private OperationInfo<Support> find (UpdateElement updateElement) {
        OperationInfo<Support> toRemove = null;
        for (OperationInfo<Support> info : listAll ()) {
            if (info.getUpdateElement ().equals (updateElement)) {
                toRemove = info;
                break;
            }
        }
        return toRemove;
    }
    
    private boolean contains (UpdateUnit unit, UpdateElement element) {
        List<OperationInfo<Support>> infos = operations;
        for (OperationInfo info : infos) {
            if (info.getUpdateElement ().equals (element) ||
                    info.getUpdateUnit ().equals (unit)) {
                return true;
            }
        }
        return false;
    }
    
    public List<OperationInfo<Support>> listAll () {
        return new ArrayList<OperationInfo<Support>>(operations);
    }
    
    public List<OperationInfo<Support>> listAllWithPossibleEager () {
        // handle eager modules
        if (type == OperationType.INSTALL) {
            Collection<UpdateElement> all = new HashSet<UpdateElement> ();
            for (OperationInfo<?> i : operations) {
                all.add (i.getUpdateElement ());
            }
            for (UpdateElement eagerEl : UpdateManagerImpl.getInstance ().getAvailableEagers ()) {
                UpdateElementImpl impl = Trampoline.API.impl (eagerEl);
                assert impl instanceof ModuleUpdateElementImpl : eagerEl + " must instanceof ModuleUpdateElementImpl";
                ModuleUpdateElementImpl eagerImpl = (ModuleUpdateElementImpl) impl;
                ModuleInfo mi = eagerImpl.getModuleInfo ();
                
                Set<ModuleInfo> installed = new HashSet<ModuleInfo> (InstalledModuleProvider.getInstalledModules ().values ());
                Set<UpdateElement> reqs = Utilities.findRequiredModules (mi.getDependencies (), installed);
                if (! reqs.isEmpty() && all.containsAll (reqs) && ! all.contains (eagerEl)) {
                    add (eagerEl.getUpdateUnit (), eagerEl);
                }
            }
        }
        return new ArrayList<OperationInfo<Support>>(operations);
    }
    
    public List<OperationInfo<Support>> listInvalid () {
        List<OperationInfo<Support>> retval = new ArrayList<OperationInfo<Support>>();
        List<OperationInfo<Support>> infos = listAll ();
        for (OperationInfo<Support> oii: infos) {
            // find type of operation
            // differ primary element and required elements
            // primary use-case can be Install but could required update of other elements
            if (!isValid (oii.getUpdateUnit (), oii.getUpdateElement ())) {
                retval.add (oii);
            }
        }
        return retval;
    }
    
    public boolean isValid (UpdateUnit updateUnit, UpdateElement updateElement) {
        if (updateElement == null) {
            throw new IllegalArgumentException ("UpdateElement cannot be null for UpdateUnit " + updateUnit);
        } else if (updateUnit == null) {
            throw new IllegalArgumentException ("UpdateUnit cannot be null for UpdateElement " + updateElement);
        }
        boolean isValid = false;
        switch (type) {
        case INSTALL :
            isValid = OperationValidator.isValidOperation (type, updateUnit, updateElement);
            // at least first add must pass and respect type of operation
            if (! isValid && operations.size () > 0) {
                // try Update
                isValid = OperationValidator.isValidOperation (OperationType.UPDATE, updateUnit, updateElement);
            }
            break;
        case UPDATE :
            isValid = OperationValidator.isValidOperation (type, updateUnit, updateElement);
            // at least first add must pass and respect type of operation
            if (! isValid && operations.size () > 0) {
                // try Update
                isValid = OperationValidator.isValidOperation (OperationType.INSTALL, updateUnit, updateElement);
            }
            break;
        default:
            isValid = OperationValidator.isValidOperation (type, updateUnit, updateElement);
        }
        
        return isValid;
    }
    
    public synchronized void remove (OperationInfo op) {
        synchronized(this) {
            operations.remove (op);
        }
    }
    public synchronized void removeAll () {
        synchronized(this) {
            operations.clear ();
        }
    }
    public class OperationInfoImpl<Support> {
        private final UpdateElement updateElement;
        private final UpdateUnit uUnit;
        private OperationInfoImpl (UpdateUnit uUnit, UpdateElement updateElement) {
            this.updateElement = updateElement;
            this.uUnit = uUnit;
        }
        public UpdateElement/*or null*/ getUpdateElement () {
            return updateElement;
        }
        public UpdateUnit/*or null*/ getUpdateUnit () {
            return uUnit;
        }
        public List<UpdateElement> getRequiredElements (){
            List<ModuleInfo> moduleInfos = new ArrayList<ModuleInfo>();
            for (OperationContainer.OperationInfo oii : listAll ()) {
                UpdateElementImpl impl = Trampoline.API.impl (oii.getUpdateElement ());
                List<ModuleInfo> infos = impl.getModuleInfos ();
                assert infos != null : "ModuleInfo for UpdateElement " + oii.getUpdateElement () + " found.";
                moduleInfos.addAll (infos);
            }
            return OperationValidator.getRequiredElements (type, getUpdateElement (), moduleInfos);
        }
        /*
        public Set<Dependency> getBrokenDependencies(){
            return Utilities.findBrokenDependencies(getUpdateElement());
        }*/
        public Set<String> getBrokenDependencies (){
            List<ModuleInfo> moduleInfos = new ArrayList<ModuleInfo>();
            for (OperationContainer.OperationInfo oii : listAll ()) {
                UpdateElementImpl impl = Trampoline.API.impl (oii.getUpdateElement ());
                List<ModuleInfo> infos = impl.getModuleInfos ();
                assert infos != null : "ModuleInfo for UpdateElement " + oii.getUpdateElement () + " found.";
                moduleInfos.addAll (infos);
            }
            
            return Utilities.getBrokenDependencies (getUpdateElement (), moduleInfos);
        }
    }
    
    /** Creates a new instance of OperationContainer */
    private OperationContainerImpl (OperationType type) {
        this.type = type;
    }
    
    public OperationType getType () {
        return type;
    }
    
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
        /** Install <code>UpdateElement</code> with custom installer. */
        CUSTOM_INSTALL,
        /** Uninstall <code>UpdateElement</code> with custom installer. */
        CUSTOM_UNINSTALL
    }
    private OperationType type;
}
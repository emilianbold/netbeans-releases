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
import java.util.Collections;
import org.netbeans.api.autoupdate.*;
import java.util.List;
import java.util.Set;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;

/**
 *
 * @author Radek Matous
 */
public final class OperationContainerImpl<Support> {
    private OperationContainer<Support> container;
    private OperationContainerImpl() {}    
    private List<OperationInfo<Support>> operations = new ArrayList<OperationInfo<Support>>();
    private List<UpdateElement> scheduledForReboot = new ArrayList<UpdateElement> ();        
    public static OperationContainerImpl<InstallSupport> createForInstall() {
        return new OperationContainerImpl<InstallSupport> (OperationType.INSTALL);
    }
    public static OperationContainerImpl<InstallSupport> createForUpdate() {
        return new OperationContainerImpl<InstallSupport> (OperationType.UPDATE);
    }
    public static OperationContainerImpl<OperationSupport> createForDirectInstall() {
        return new OperationContainerImpl<OperationSupport> (OperationType.INSTALL);
    }
    public static OperationContainerImpl<OperationSupport> createForDirectUpdate() {
        return new OperationContainerImpl<OperationSupport> (OperationType.UPDATE);
    }
    public static OperationContainerImpl<OperationSupport> createForUninstall() {
        return new OperationContainerImpl<OperationSupport> (OperationType.UNINSTALL);
    }
    public static OperationContainerImpl<OperationSupport> createForEnable() {
        return new OperationContainerImpl<OperationSupport> (OperationType.ENABLE);
    }
    public static OperationContainerImpl<OperationSupport> createForDisable() {
        return new OperationContainerImpl<OperationSupport> (OperationType.DISABLE);
    }
    public OperationInfo<Support> add(UpdateUnit updateUnit, UpdateElement updateElement) throws IllegalArgumentException {
        OperationInfo<Support> retval = null;
        boolean isValid = isValid(updateUnit, updateElement);
        if (!isValid) {
            throw new IllegalArgumentException(updateElement.getCodeName());
        }
        if (type == OperationType.INSTALL || type == OperationType.UPDATE) {
            if (scheduledForReboot.contains(updateElement)) {
                return null;
            }
        }
        if (isValid) {
            if (type == OperationType.UNINSTALL || type == OperationType.ENABLE || type == OperationType.DISABLE) {
                isValid = (updateUnit.getInstalled() == updateElement);
                if (!isValid) {
                    throw new IllegalArgumentException(updateElement.getCodeName());
                }                
            } else {
                isValid = (updateUnit.getInstalled() != updateElement);
                if (!isValid) {
                    throw new IllegalArgumentException(updateElement.getCodeName());
                }                
            }
        }
        synchronized(this) {
            if (!contains(updateUnit, updateElement)) {
                retval = Trampoline.API.createOperationInfo (new OperationInfoImpl<Support> (updateUnit, updateElement));
                operations.add (retval);
            }
        }
        return retval;
    }
    public boolean remove(UpdateElement updateElement) {
        OperationInfo toRemove = find(updateElement);
        if (toRemove != null) {
            remove(toRemove);
        }
        return toRemove != null;
    }
    
    public void addScheduledForReboot (UpdateElement el) {
        scheduledForReboot.add (el);
    }
    
    public boolean contains(UpdateElement updateElement) {
        return find(updateElement) != null;
    }
    
    public void setOperationContainer (OperationContainer<Support> container) {
        this.container = container;
    }
    
    private OperationInfo<Support> find(UpdateElement updateElement) {
        OperationInfo<Support> toRemove = null;
        for (OperationInfo<Support> info : listAll ()) {
            if (info.getUpdateElement().equals(updateElement)) {
                toRemove = info;
                break;
            }
        }
        return toRemove;
    }
    
    private boolean contains(UpdateUnit unit, UpdateElement element) {
        List<OperationInfo<Support>> infos = listAll();
        for (OperationInfo info : infos) {
            if (info.getUpdateElement().equals(element) ||
                    info.getUpdateUnit().equals(unit)) {
                return true;
            }
        }
        return false;
    }
    
    public List<OperationInfo<Support>> listAll () {
        return new ArrayList<OperationInfo<Support>>(operations);
    }
    
    public List<OperationInfo<Support>> listInvalid () {
        List<OperationInfo<Support>> retval = new ArrayList<OperationInfo<Support>>();
        List<OperationInfo<Support>> infos = listAll ();
        for (OperationInfo<Support> oii: infos) {
            // find type of operation
            // differ primary element and required elements
            // primary use-case can be Install but could required update of other elements
            if (!isValid(oii.getUpdateUnit(), oii.getUpdateElement())) {
                retval.add(oii);
            }
        }
        return retval;
    }
    
    public boolean isValid(UpdateUnit updateUnit, UpdateElement updateElement) {
        if (updateElement == null) {
            throw new IllegalArgumentException ("UpdateElement cannot be null.");
        } else if (updateUnit == null) {
            throw new IllegalArgumentException ("UpdateUnit cannot be null.");
        }
        boolean isValid = false;
        switch (type) {
            case INSTALL : 
                isValid = OperationValidator.isValidOperation(type, updateUnit, updateElement);
                // at least first add must pass and respect type of operation
                if (! isValid && operations.size() > 0) {
                    // try Update
                    isValid = OperationValidator.isValidOperation (OperationType.UPDATE, updateUnit, updateElement);
                }
                break;
            case UPDATE :
                isValid = OperationValidator.isValidOperation(type, updateUnit, updateElement);
                // at least first add must pass and respect type of operation
                if (! isValid && operations.size() > 0) {
                    // try Update
                    isValid = OperationValidator.isValidOperation (OperationType.INSTALL, updateUnit, updateElement);
                }
                break;
            default:
                isValid = OperationValidator.isValidOperation(type, updateUnit, updateElement);
        }
                    
        return isValid;
    }
    
    public synchronized void remove(OperationInfo op) {
        synchronized(this) {
            operations.remove(op);
        }
    }
    public synchronized void removeAll() {
        synchronized(this) {
            operations.clear();
        }
    }
    public class OperationInfoImpl<Support> {
        private final UpdateElement updateElement;
        private final UpdateUnit uUnit;
        private OperationInfoImpl (UpdateUnit uUnit, UpdateElement updateElement) {
            this.updateElement = updateElement;
            this.uUnit = uUnit;
        }
        public UpdateElement/*or null*/ getUpdateElement() {
            return updateElement;
        }
        public UpdateUnit/*or null*/ getUpdateUnit() {
            return uUnit;
        }
        public List<UpdateElement> getRequiredElements(){
            List<ModuleInfo> moduleInfos = new ArrayList<ModuleInfo>();
            for (OperationContainer.OperationInfo oii : listAll ()) {
                UpdateElementImpl impl = Trampoline.API.impl(oii.getUpdateElement());
                ModuleInfo info = impl.getModuleInfo();
                assert info != null : "ModuleInfo for UpdateElement " + oii.getUpdateElement () + " found.";
                moduleInfos.add(info);
            }
            return OperationValidator.getRequiredElements(type, getUpdateElement(), moduleInfos);
        }
        /*
        public Set<Dependency> getBrokenDependencies(){
            return Utils.findBrokenDependencies(getUpdateElement());
        }*/
        public Set<String> getBrokenDependencies (){
            List<ModuleInfo> moduleInfos = new ArrayList<ModuleInfo>();
            for (OperationContainer.OperationInfo oii : listAll ()) {
                UpdateElementImpl impl = Trampoline.API.impl(oii.getUpdateElement());
                ModuleInfo info = impl.getModuleInfo();
                assert info != null : "ModuleInfo for UpdateElement " + oii.getUpdateElement () + " found.";
                moduleInfos.add(info);
            }
            
            return Utils.getBrokenDependencies (getUpdateElement(), moduleInfos);
        }
    }
    
    /** Creates a new instance of OperationContainer */
    private OperationContainerImpl(OperationType type) {
        this.type = type;
    }
    
    public OperationType getType() {
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
        DISABLE
    }
    private OperationType type;
}
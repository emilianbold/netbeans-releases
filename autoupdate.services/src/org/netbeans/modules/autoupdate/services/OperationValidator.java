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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;

/**
 *
 * @author Radek Matous, Jiri Rechtacek
 */
abstract class OperationValidator {
    private final static OperationValidator FOR_INSTALL = new InstallValidator();
    private final static OperationValidator FOR_UNINSTALL = new UninstallValidator();
    private final static OperationValidator FOR_UPDATE = new UpdateValidator();
    private final static OperationValidator FOR_ENABLE = new EnableValidator();
    private final static OperationValidator FOR_DISABLE = new DisableValidator();
    private final static OperationValidator FOR_CUSTOM_INSTALL = new CustomInstallValidator();
    private static final Logger LOGGER = Logger.getLogger ("org.netbeans.modules.autoupdate.services.OperationValidator");    
    /** Creates a new instance of OperationValidator */
    private OperationValidator() {}
    static boolean isValidOperation(OperationContainerImpl.OperationType type, UpdateUnit updateUnit, UpdateElement updateElement) {
        boolean isValid = false;
        switch(type){
        case INSTALL:
            isValid = FOR_INSTALL.isValidOperationImpl(updateUnit, updateElement);
            break;
        case UNINSTALL:
            isValid = FOR_UNINSTALL.isValidOperationImpl(updateUnit, updateElement);
            break;
        case UPDATE:
            isValid = FOR_UPDATE.isValidOperationImpl(updateUnit, updateElement);
            break;
        case ENABLE:
            isValid = FOR_ENABLE.isValidOperationImpl(updateUnit, updateElement);
            break;
        case DISABLE:
            isValid = FOR_DISABLE.isValidOperationImpl(updateUnit, updateElement);
            break;
        case CUSTOM_INSTALL:
            isValid = FOR_CUSTOM_INSTALL.isValidOperationImpl(updateUnit, updateElement);
            break;
        default:
            assert false;
        }
        return isValid;
    }
    
    static List<UpdateElement> getRequiredElements(OperationContainerImpl.OperationType type, UpdateElement updateElement, List<ModuleInfo> moduleInfos) {
        List<UpdateElement> retval = Collections.emptyList ();
        switch(type){
        case INSTALL:
            retval = FOR_INSTALL.getRequiredElementsImpl(updateElement, moduleInfos);
            break;
        case UNINSTALL:
            retval = FOR_UNINSTALL.getRequiredElementsImpl(updateElement, moduleInfos);
            break;
        case UPDATE:
            retval = FOR_UPDATE.getRequiredElementsImpl(updateElement, moduleInfos);
            break;
        case ENABLE:
            retval = FOR_ENABLE.getRequiredElementsImpl(updateElement, moduleInfos);
            break;
        case DISABLE:
            retval = FOR_DISABLE.getRequiredElementsImpl(updateElement, moduleInfos);
            break;
        case CUSTOM_INSTALL:
            retval = FOR_CUSTOM_INSTALL.getRequiredElementsImpl(updateElement, moduleInfos);
            break;
        default:
            assert false;
        }
        return retval;
    }
    
    abstract boolean isValidOperationImpl(UpdateUnit updateUnit, UpdateElement uElement);
    abstract  List<UpdateElement> getRequiredElementsImpl(UpdateElement uElement, List<ModuleInfo> moduleInfos);
    
    
    private static class InstallValidator extends OperationValidator {
        boolean isValidOperationImpl(UpdateUnit unit, UpdateElement uElement) {
            return unit.getInstalled() == null && containsElement (uElement, unit);
        }
        
        List<UpdateElement> getRequiredElementsImpl(UpdateElement uElement, List<ModuleInfo> moduleInfos) {
            return new LinkedList<UpdateElement> (Utilities.findRequiredUpdateElements(uElement, moduleInfos));
        }
    }
    
    private static class UninstallValidator extends OperationValidator {
        
        boolean isValidOperationImpl(UpdateUnit unit, UpdateElement uElement) {
            return unit.getInstalled() != null && isValidOperationImpl (Trampoline.API.impl (uElement));
        }
        
        private boolean isValidOperationImpl (UpdateElementImpl impl) {
            boolean res = false;
            switch (impl.getType ()) {
            case MODULE :
                Module m =  Utilities.toModule (((ModuleUpdateElementImpl) impl).getModuleInfo ());
                res = ModuleDeleterImpl.getInstance ().canDelete (m);
                break;
            case FEATURE :
                for (ModuleInfo info : ((FeatureUpdateElementImpl) impl).getModuleInfos ()) {
                    Module module = Utilities.toModule (info);
                    res |= ModuleDeleterImpl.getInstance ().canDelete (module);
                }
                break;
            default:
                assert false : "Not supported for impl " + impl;
            }
            return res;
        }
        
        List<UpdateElement> getRequiredElementsImpl (UpdateElement uElement, List<ModuleInfo> moduleInfos) {
            ModuleManager mm = null;
            final Set<Module> modules = new LinkedHashSet<Module>();
            for (ModuleInfo moduleInfo : moduleInfos) {
                Module m = Utilities.toModule (moduleInfo);
                if (m == null) {
                    continue;
                }
                if (m.isEnabled ()) {
                    modules.add(m);
                }
                if (mm == null) {
                    mm = m.getManager();
                }
            }
            List<UpdateElement> retval = new ArrayList<UpdateElement>();
            if (mm != null) {
                List<Module> toUninstall = requiredForUninstall (new ArrayList<Module> (), modules, mm.getEnabledModules (), mm);
                for (Module module : toUninstall) {
                    if (!modules.contains (module) && ! module.isFixed ()) {
                        // XXX: breaks detail level features/modules
                        retval.add (Utilities.toUpdateUnit (module).getInstalled ());
                    }
                }
            }
            // XXX: do transform to feature if possible
            return retval;
        }
        
        private static List<Module> requiredForUninstall(List<Module> resultToUninstall, final Set<Module> requestedToUninstall, final Set<Module> stillEnabled, ModuleManager mm) {
            resultToUninstall.addAll(requestedToUninstall);            
            stillEnabled.removeAll(resultToUninstall);
            Set<Module> dependenciesToUninstall = new HashSet<Module>();
            for (Module m : requestedToUninstall) {
                for (Module other: stillEnabled) {
                    Dependency[] dependencies = other.getDependenciesArray();
                    boolean added = false;
                    for (int i = 0; !added && i < dependencies.length; i++) {
                        Dependency dep = dependencies[i];
                        if (dep.getType() == Dependency.TYPE_MODULE) {
                            if (dep.getName().equals(m.getCodeName())) {
                                added = true;
                                dependenciesToUninstall.add(other);
                                continue;
                            }
                        } else if (
                                dep.getType() == Dependency.TYPE_REQUIRES ||
                                dep.getType() == Dependency.TYPE_NEEDS ||
                                dep.getType() == Dependency.TYPE_RECOMMENDS
                                ) {
                            if (m.provides(dep.getName())) {
                                boolean foundOne = false;
                                for (Module third: mm.getEnabledModules()) {
                                    if (third.isEnabled() &&
                                            !resultToUninstall.contains(third) && !dependenciesToUninstall.contains(third) &&
                                            third.provides(dep.getName())) {
                                        foundOne = true;
                                        break;
                                    }
                                }
                                if (!foundOne) {
                                    // Nope, we were the only/last one to provide it.
                                    added = true;
                                    dependenciesToUninstall.add(other);
                                    continue;
                                }                                    
                            }
                        }
                    }
                }
            }
                
            if (dependenciesToUninstall.size() > 0) {                    
                requiredForUninstall(resultToUninstall, dependenciesToUninstall, stillEnabled, mm);
            }
            return resultToUninstall;
        }        
    }
    
    private static class UpdateValidator extends OperationValidator {
        boolean isValidOperationImpl(UpdateUnit unit, UpdateElement uElement) {
            return unit.getInstalled() != null && containsElement (uElement, unit);
        }
        
        List<UpdateElement> getRequiredElementsImpl(UpdateElement uElement, List<ModuleInfo> moduleInfos) {
             return FOR_INSTALL.getRequiredElementsImpl(uElement, moduleInfos);
        }
    }
    
    private static boolean containsElement (UpdateElement el, UpdateUnit unit) {
        return unit.getAvailableUpdates ().contains (el);
    }
    
    private static class EnableValidator extends OperationValidator {
        boolean isValidOperationImpl(UpdateUnit unit, UpdateElement uElement) {
            return unit.getInstalled () != null && isValidOperationImpl (Trampoline.API.impl (uElement));
        }
        
        private boolean isValidOperationImpl (UpdateElementImpl impl) {
            boolean res = false;
            switch (impl.getType ()) {
            case MODULE :
                Module module =  Utilities.toModule (((ModuleUpdateElementImpl) impl).getModuleInfo ());
                res = Utilities.canEnable (module);
                break;
            case FEATURE :
                for (ModuleInfo info : ((FeatureUpdateElementImpl) impl).getModuleInfos ()) {
                    Module m =  Utilities.toModule (info);
                    res |= Utilities.canEnable (m);
                }
                break;
            default:
                assert false : "Not supported for impl " + impl;
            }
            return res;
        }
        
        List<UpdateElement> getRequiredElementsImpl(UpdateElement uElement, List<ModuleInfo> moduleInfos) {
            ModuleManager mm = null;
            final Set<Module> modules = new LinkedHashSet<Module>();
            for (ModuleInfo moduleInfo : moduleInfos) {
                Module m = Utilities.toModule (moduleInfo);
                if (Utilities.canEnable (m)) {
                    modules.add(m);
                }
                if (mm == null) {
                    mm = m.getManager();
                }
            }
            List<UpdateElement> retval = new ArrayList<UpdateElement>();
            if (mm != null) {
                List<Module> toEnable = mm.simulateEnable(modules);
                for (Module module : toEnable) {
                    if (!modules.contains(module) && Utilities.canEnable (module)) {
                        retval.add(Utilities.toUpdateUnit(module).getInstalled());
                    }
                }
            }
            return retval;
        }
    }
    
    private static class DisableValidator extends OperationValidator {
        boolean isValidOperationImpl(UpdateUnit unit, UpdateElement uElement) {
            return unit.getInstalled () != null && isValidOperationImpl (Trampoline.API.impl (uElement));
        }
        
        private boolean isValidOperationImpl (UpdateElementImpl impl) {
            boolean res = false;
            switch (impl.getType ()) {
            case MODULE :
                Module module =  Utilities.toModule (((ModuleUpdateElementImpl) impl).getModuleInfo ());
                res = Utilities.canDisable (module);
                break;
            case FEATURE :
                for (ModuleInfo info : ((FeatureUpdateElementImpl) impl).getModuleInfos ()) {
                    Module m =  Utilities.toModule (info);
                    res |= Utilities.canDisable (m);
                }
                break;
            default:
                assert false : "Not supported for impl " + impl;
            }
            return res;
        }
        
        List<UpdateElement> getRequiredElementsImpl(UpdateElement uElement, List<ModuleInfo> moduleInfos) {
            ModuleManager mm = null;
            final Set<Module> modules = new LinkedHashSet<Module>();
            for (ModuleInfo moduleInfo : moduleInfos) {
                Module m = Utilities.toModule (moduleInfo);
                if (Utilities.canDisable (m)) {
                    modules.add(m);
                }
                if (mm == null) {
                    mm = m.getManager();
                }
            }
            List<UpdateElement> retval = new ArrayList<UpdateElement>();
            if (mm != null) {
                List<Module> toDisable = mm.simulateDisable(modules);
                for (Module module : toDisable) {
                    if (!modules.contains(module) && Utilities.canDisable (module)) {
                        retval.add(Utilities.toUpdateUnit(module).getInstalled());
                    }
                }
            }
            return retval;
        }
    }
    
    private static class CustomInstallValidator extends OperationValidator {
        boolean isValidOperationImpl (UpdateUnit unit, UpdateElement uElement) {
            boolean res = false;
            UpdateElementImpl impl = Trampoline.API.impl (uElement);
            assert impl != null;
            if (impl != null && impl instanceof NativeComponentUpdateElementImpl) {
                NativeComponentUpdateElementImpl ni = (NativeComponentUpdateElementImpl) impl;
                if (ni.getInstallInfo ().getCustomInstaller () != null) {
                    res = unit.getInstalled() == null && containsElement (uElement, unit);
                }
            }
            return res;
        }
        
        List<UpdateElement> getRequiredElementsImpl(UpdateElement uElement, List<ModuleInfo> moduleInfos) {
            LOGGER.log (Level.INFO, "CustomInstallValidator doesn't care about required elements."); // XXX
            return Collections.emptyList ();
            //return new LinkedList<UpdateElement> (Utilities.findRequiredUpdateElements (uElement, moduleInfos));
        }
    }
    
}

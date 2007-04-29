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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;

/** XXX Only Modules are supposed here. Needs to impl. for Features or Localization types.
 *
 * @author Radek Matous
 */
abstract class OperationValidator {
    private final static OperationValidator FOR_INSTALL = new InstallValidator();
    private final static OperationValidator FOR_UNINSTALL = new UninstallValidator();
    private final static OperationValidator FOR_UPDATE = new UpdateValidator();
    private final static OperationValidator FOR_ENABLE = new EnableValidator();
    private final static OperationValidator FOR_DISABLE = new DisableValidator();
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
        default:
            assert false;
        }
        return retval;
    }
    
    abstract boolean isValidOperationImpl(UpdateUnit updateUnit, UpdateElement uElement);
    abstract  List<UpdateElement> getRequiredElementsImpl(UpdateElement uElement, List<ModuleInfo> moduleInfos);
    
    
    private static class InstallValidator extends OperationValidator {
        boolean isValidOperationImpl(UpdateUnit unit, UpdateElement uElement) {
            Module m =  Utilities.toModule (unit.getCodeName(), uElement.getSpecificationVersion ());
            return m == null && unit.getInstalled() == null && containsElement (uElement, unit);
        }
        
        List<UpdateElement> getRequiredElementsImpl(UpdateElement uElement, List<ModuleInfo> moduleInfos) {
            return Utilities.findRequiredModules(uElement, moduleInfos);
        }
    }
    
    private static class UninstallValidator extends OperationValidator {
        boolean isValidOperationImpl(UpdateUnit unit, UpdateElement uElement) {
            Module m =  Utilities.toModule (unit.getCodeName (), uElement.getSpecificationVersion ());
            return m != null && unit.getInstalled() != null &&
                    ModuleDeleterImpl.getInstance().canDelete(Utilities.toModule(unit));
        }
        
        
        List<UpdateElement> getRequiredElementsImpl(UpdateElement uElement, List<ModuleInfo> moduleInfos) {
            //TODO: copy/pasted from DisableValidator - should use rather method than simulateDisable later
            ModuleManager mm = null;
            final Set<Module> modules = new LinkedHashSet<Module>();
            for (ModuleInfo moduleInfo : moduleInfos) {
                Module m = Utilities.toModule(moduleInfo.getCodeNameBase(), moduleInfo.getSpecificationVersion ().toString ());
                if (m.isEnabled()) {
                    modules.add(m);
                }
                if (mm == null) {
                    mm = m.getManager();
                }
            }
            List<UpdateElement> retval = new ArrayList<UpdateElement>();
            if (mm != null) {
                List<Module> toUninstall = requiredForUninstall(new ArrayList<Module>(),modules,mm.getEnabledModules(), mm);
                for (Module module : toUninstall) {
                    if (!modules.contains(module) &&  !module.isFixed()) {
                        retval.add(Utilities.toUpdateUnit(module).getInstalled());
                    }
                }
            }                        
            return retval;
        }
        
        private static List<Module> requiredForUninstall(List<Module> resultToUninstall, final Set<Module> requestedToUninstall, final Set<Module> stillEnabled, ModuleManager mm) {
            resultToUninstall.addAll(requestedToUninstall);            
            stillEnabled.removeAll(resultToUninstall);
            Set<Module> dependenciesToUninstall = new HashSet<Module>();
            Iterator<Module> it = requestedToUninstall.iterator();
                while (it.hasNext()) {
                    Module m = it.next();
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
            // module with UpdateElement specificationVersion cannot exist
            Module m =  Utilities.toModule (unit.getCodeName(), uElement.getSpecificationVersion ());
            return m == null && unit.getInstalled() != null && containsElement (uElement, unit);
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
            Module m =  Utilities.toModule (unit.getCodeName (), uElement.getSpecificationVersion ());
            return  (unit.getInstalled() != null) && m != null && !m.isEnabled() &&
                    !m.isFixed() && !m.isAutoload() && !m.isEager();
        }
        
        
        List<UpdateElement> getRequiredElementsImpl(UpdateElement uElement, List<ModuleInfo> moduleInfos) {
            ModuleManager mm = null;
            final Set<Module> modules = new LinkedHashSet<Module>();
            for (ModuleInfo moduleInfo : moduleInfos) {
                Module m = Utilities.toModule (moduleInfo.getCodeNameBase (), moduleInfo.getSpecificationVersion ().toString ());
                modules.add(m);
                if (mm == null) {
                    mm = m.getManager();
                }
            }
            List<UpdateElement> retval = new ArrayList<UpdateElement>();
            if (mm != null) {
                List<Module> toDisable = mm.simulateEnable(modules);
                for (Module module : toDisable) {
                    if (!modules.contains(module) && !module.isAutoload() && !module.isEager() && !module.isFixed()) {
                        retval.add(Utilities.toUpdateUnit(module).getInstalled());
                    }
                }
            }
            return retval;
        }
    }
    
    private static class DisableValidator extends OperationValidator {
        boolean isValidOperationImpl(UpdateUnit unit, UpdateElement uElement) {
            Module m =  Utilities.toModule (unit.getCodeName (), uElement.getSpecificationVersion ());
            return  (unit.getInstalled() != null) && m != null && m.isEnabled() &&
                    !m.isFixed() && !m.isAutoload() && !m.isEager();
        }
        
        List<UpdateElement> getRequiredElementsImpl(UpdateElement uElement, List<ModuleInfo> moduleInfos) {
            ModuleManager mm = null;
            final Set<Module> modules = new LinkedHashSet<Module>();
            for (ModuleInfo moduleInfo : moduleInfos) {
                Module m = Utilities.toModule (moduleInfo.getCodeNameBase (), moduleInfo.getSpecificationVersion ().toString ());
                modules.add(m);
                if (mm == null) {
                    mm = m.getManager();
                }
            }
            List<UpdateElement> retval = new ArrayList<UpdateElement>();
            if (mm != null) {
                List<Module> toDisable = mm.simulateDisable(modules);
                for (Module module : toDisable) {
                    if (!modules.contains(module) && !module.isAutoload() && !module.isEager() && !module.isFixed()) {
                        retval.add(Utilities.toUpdateUnit(module).getInstalled());
                    }
                }
            }
            return retval;
        }
    }
}

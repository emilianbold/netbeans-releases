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
        if (LOGGER.isLoggable (Level.FINE)) {
            LOGGER.log (Level.FINE, "== do getRequiredElements for " + type + " of " + updateElement + " ==");
            for (UpdateElement el : retval) {
                LOGGER.log (Level.FINE, "--> " + el);
            }
            LOGGER.log (Level.FINE, "== done. ==");
        }
        return retval;
    }
    
    abstract boolean isValidOperationImpl(UpdateUnit updateUnit, UpdateElement uElement);
    abstract List<UpdateElement> getRequiredElementsImpl(UpdateElement uElement, List<ModuleInfo> moduleInfos);
    
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
            case KIT_MODULE :
            case MODULE :
                Module m =  Utilities.toModule (((ModuleUpdateElementImpl) impl).getModuleInfo ());
                res = ModuleDeleterImpl.getInstance ().canDelete (m);
                break;
            case STANDALONE_MODULE :
            case FEATURE :
                for (ModuleInfo info : ((FeatureUpdateElementImpl) impl).getModuleInfos ()) {
                    Module module = Utilities.toModule (info);
                    res |= ModuleDeleterImpl.getInstance ().canDelete (module);
                }
                break;
            case CUSTOM_HANDLED_COMPONENT :
                LOGGER.log (Level.INFO, "CUSTOM_HANDLED_COMPONENT doesn't support custom uninstaller yet."); // XXX
                res = false;
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
                if (! m.isFixed ()) {
                    modules.add (m);
                }
                if (mm == null) {
                    mm = m.getManager();
                }
            }
            Set<UpdateElement> retval = new HashSet<UpdateElement>();
            if (mm != null) {
                Set<Module> toUninstall = requiredForUninstall (new HashSet<Module> (), new LinkedHashSet<Module> (modules), mm);
                toUninstall.removeAll (modules);
                for (Module module : toUninstall) {
                    if (Utilities.isEssentialModule (module)) {
                        LOGGER.log (Level.WARNING, "Essential module cannot be planned for uninstall but " + module);
                        continue;
                    }
                    // !!! e.g. applemodule can be found in the list for uninstall but has UpdateUnit nowhere else MacXOS
                    UpdateUnit unit = Utilities.toUpdateUnit (module);
                    if (unit != null) {
                        retval.add (unit.getInstalled ());
                    }
                }
            }
            return new ArrayList<UpdateElement> (retval);
        }
        
        private static Set<Module> requiredForUninstall (Set<Module> resultToUninstall, final Set<Module> requestedToUninstall, ModuleManager mm) {
            resultToUninstall.addAll (getRequiredModules (requestedToUninstall, mm));

            // do traversal up
            Set<Module> dependenciesToUninstall = new HashSet<Module> ();
            for (Module m : resultToUninstall) {
                dependenciesToUninstall.addAll (Utilities.findDependingModules (m, mm));
            }
                
            resultToUninstall.addAll (requestedToUninstall);
            resultToUninstall.addAll (dependenciesToUninstall);
            
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
            case KIT_MODULE :
            case MODULE :
                Module module =  Utilities.toModule (((ModuleUpdateElementImpl) impl).getModuleInfo ());
                res = Utilities.canEnable (module);
                break;
            case STANDALONE_MODULE :
            case FEATURE :
                for (ModuleInfo info : ((FeatureUpdateElementImpl) impl).getModuleInfos ()) {
                    Module m =  Utilities.toModule (info);
                    res |= Utilities.canEnable (m);
                }
                break;
            case CUSTOM_HANDLED_COMPONENT :
                res = false;
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
            case KIT_MODULE :
            case MODULE :
                Module module =  Utilities.toModule (((ModuleUpdateElementImpl) impl).getModuleInfo ());
                res = Utilities.canDisable (module);
                break;
            case STANDALONE_MODULE :
            case FEATURE :
                for (ModuleInfo info : ((FeatureUpdateElementImpl) impl).getModuleInfos ()) {
                    Module m =  Utilities.toModule (info);
                    res |= Utilities.canDisable (m);
                }
                break;
            case CUSTOM_HANDLED_COMPONENT :
                res = false;
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
            
            if (mm == null) {
                LOGGER.log (Level.WARNING, "No modules can be disabled when disabling UpdateElement " + uElement);
                return Collections.emptyList ();
            } 
            
            Set<Module> requestedToDisable = getRequiredModules (modules, mm);

            List<Module> toDisable = mm.simulateDisable (modules);
            boolean wasAdded = requestedToDisable.addAll (toDisable);
            
            // XXX why sometimes happens that no all module for deactivated found?
            // assert ! wasAdded : "The requestedToDisable cannot be enlarged by " + toDisable;
            if (LOGGER.isLoggable (Level.FINE) && wasAdded) {
                toDisable.removeAll (getRequiredModules (modules, mm));
                LOGGER.log (Level.FINE, "requestedToDisable was enlarged by " + toDisable);
            }
            
            Set<UpdateElement> retval = new HashSet<UpdateElement> ();
            for (Module module : requestedToDisable) {
                if (! modules.contains (module) && Utilities.canDisable (module)) {
                    // !!! e.g. applemodule can be found in the list for uninstall but has UpdateUnit nowhere else MacXOS
                    UpdateUnit unit = Utilities.toUpdateUnit (module);
                    if (unit != null) {
                        retval.add (unit.getInstalled ());
                    }
                }
            }
            
            return new ArrayList<UpdateElement> (retval);
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
    
    private static Set<Module> getRequiredModules (final Collection<Module> requested, ModuleManager mm) {
        // loop over all dependencies and add as many as possible for KIT_MODULE
        // do traversal down
        Set<Module> candidates = new HashSet<Module> (requested);
        for (Module m : requested) {
            if (Utilities.isKitModule (m)) {
                candidates.addAll (Utilities.findRequiredModules (m, mm, new HashSet<Module> ()));
                LOGGER.log (Level.FINEST, "Inspect modules this module depends upon for KIT_MODULE " +
                        m.getCodeNameBase () + ". The modules has added " + candidates.size ());
            }
        }

        Set<Module> result = new HashSet<Module> (requested);
        result.addAll (candidates);
        candidates.removeAll (requested);
        
        Set<Module> canSkip = new HashSet<Module> ();
        for (Module depM : candidates) {
            if (canSkip.contains (depM)) {
                LOGGER.log (Level.FINE, "The module " + depM.getCodeNameBase () + " was investigated already and won't be deactivated now.");
            } else {
                Set<Module> depends = Utilities.findDependingModules (depM, mm);
                if (! result.containsAll (depends)) {
                    canSkip.addAll (Utilities.findRequiredModules (depM, mm, new HashSet<Module> ()));
                    LOGGER.log (Level.FINE, "The module " + depM.getCodeNameBase () + " is shared and cannot be deactivated now.");
                    if (LOGGER.isLoggable (Level.FINER)) {
                        Set<Module> outsideModules = new HashSet<Module> (depends);
                        outsideModules.removeAll (result);
                        LOGGER.log (Level.FINER, "On " + depM.getCodeNameBase () + " depending modules outside of set now deactivateing modules: " + outsideModules);
                    }
                    result.remove (depM);
                } else {
                    LOGGER.log (Level.FINEST, "The module " + depM.getCodeNameBase () + " is not needed anymore and can be deactivated.");
                }
            }
        }
        result.removeAll (canSkip);
        return result;
    }
    
}

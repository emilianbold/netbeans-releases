/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.autoupdate.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import org.netbeans.api.autoupdate.*;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;

/**
 *
 * @author Radek Matous, Jiri Rechtacek
 */
public final class OperationContainerImpl<Support> {
    private OperationContainer<Support> container;
    private boolean upToDate = false;
    private OperationContainerImpl () {}
    private static final Logger LOGGER = Logger.getLogger (OperationContainerImpl.class.getName ());    
    private List<OperationInfo<Support>> operations = new ArrayList<OperationInfo<Support>>();
    private Collection<OperationInfo<Support>> affectedEagers = new HashSet<OperationInfo<Support>> ();
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
    public static OperationContainerImpl<OperationSupport> createForDirectUninstall () {
        return new OperationContainerImpl<OperationSupport> (OperationType.DIRECT_UNINSTALL);
    }
    public static OperationContainerImpl<OperationSupport> createForEnable () {
        return new OperationContainerImpl<OperationSupport> (OperationType.ENABLE);
    }
    public static OperationContainerImpl<OperationSupport> createForDisable () {
        return new OperationContainerImpl<OperationSupport> (OperationType.DISABLE);
    }
    public static OperationContainerImpl<OperationSupport> createForDirectDisable () {
        return new OperationContainerImpl<OperationSupport> (OperationType.DIRECT_DISABLE);
    }
    public static OperationContainerImpl<OperationSupport> createForInstallNativeComponent () {
        return new OperationContainerImpl<OperationSupport> (OperationType.CUSTOM_INSTALL);
    }
    public static OperationContainerImpl<OperationSupport> createForUninstallNativeComponent () {
        return new OperationContainerImpl<OperationSupport> (OperationType.CUSTOM_UNINSTALL);
    }
    @SuppressWarnings({"unchecked"})
    public OperationInfo<Support> add (UpdateUnit updateUnit, UpdateElement updateElement) throws IllegalArgumentException {
        OperationInfo<Support> retval = null;
        boolean isValid = isValid (updateUnit, updateElement);
        if (!isValid) {
            throw new IllegalArgumentException ("Invalid " + updateElement.getCodeName () + " for operation " + type);
        }
        if (UpdateUnitFactory.getDefault().isScheduledForRestart (updateElement)) {
            LOGGER.log (Level.INFO, updateElement + " is scheduled for restart IDE.");
            throw new IllegalArgumentException (updateElement + " is scheduled for restart IDE.");
        }
        if (isValid) {
            switch (type) {
            case UNINSTALL :
            case DIRECT_UNINSTALL :
            case CUSTOM_UNINSTALL :
            case ENABLE :
            case DISABLE :
            case DIRECT_DISABLE :
                if (updateUnit.getInstalled () != updateElement) {
                    throw new IllegalArgumentException (updateUnit.getInstalled () +
                            " and " + updateElement + " must be same for operation " + type);
                }
                break;
            case INSTALL :
            case UPDATE :
            case CUSTOM_INSTALL:
                if (updateUnit.getInstalled () == updateElement) {
                    throw new IllegalArgumentException (updateUnit.getInstalled () +
                            " and " + updateElement + " cannot be same for operation " + type);
                }
                break;
            default:
                assert false : "Unknown type of operation " + type;
            }
        }
        synchronized(this) {
            if (!contains (updateUnit, updateElement)) {
                retval = Trampoline.API.createOperationInfo (new OperationInfoImpl<Support> (updateUnit, updateElement));
                changeState (operations.add (retval));
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
    
    synchronized public List<OperationInfo<Support>> listAllWithPossibleEager () {
        if (upToDate) {
            return new ArrayList<OperationInfo<Support>>(operations);
        }
        clearCache ();
        // handle eager modules
        
        if (type == OperationType.INSTALL || type == OperationType.UPDATE) {
            Collection<UpdateElement> all = new HashSet<UpdateElement> (operations.size ());
            Collection<ModuleInfo> allModuleInfos = new HashSet<ModuleInfo> (operations.size ());
            for (OperationInfo<?> i : operations) {
                all.add (i.getUpdateElement ());
                UpdateElementImpl elImpl = Trampoline.API.impl (i.getUpdateElement ());
                if (elImpl instanceof ModuleUpdateElementImpl) {
                    allModuleInfos.add (((ModuleUpdateElementImpl) elImpl).getModuleInfo ());
                }
                //TODO: what if elImpl instanceof FeatureUpdateElementImpl ?
            }
            for (UpdateElement eagerEl : UpdateManagerImpl.getInstance ().getAvailableEagers ()) {
                UpdateElementImpl impl = Trampoline.API.impl (eagerEl);
                List <ModuleInfo> infos = new ArrayList <ModuleInfo>();
                if(impl instanceof ModuleUpdateElementImpl) {
                    ModuleUpdateElementImpl eagerImpl = (ModuleUpdateElementImpl) impl;
                    infos.add(eagerImpl.getModuleInfo ());
                } else if (impl instanceof FeatureUpdateElementImpl) {
                    FeatureUpdateElementImpl eagerImpl = (FeatureUpdateElementImpl) impl;
                    infos.addAll(eagerImpl.getModuleInfos ());
                } else {
                    assert false : eagerEl + " must instanceof ModuleUpdateElementImpl or FeatureUpdateElementImpl";
                }

                for(ModuleInfo mi: infos) {
                    Set<UpdateElement> reqs = new HashSet<UpdateElement> ();
                    for (Dependency dep : mi.getDependencies ()) {
                        UpdateElement req = Utilities.handleDependency (dep, Collections.singleton (mi), new HashSet<Dependency> ());
                        if (req != null) {
                            reqs.add (req);
                        }
                    }
                    if ((! reqs.isEmpty() && all.containsAll (reqs) && ! all.contains (eagerEl)) ||
                            (reqs.isEmpty() && impl.getUpdateUnit().getInstalled()!=null && type == OperationType.UPDATE && operations.size() > 0)) {
                        // adds affectedEager into list of elements for the operation
                        OperationInfo<Support> i = add (eagerEl.getUpdateUnit (), eagerEl);
                        if (i != null) {
                            affectedEagers.add (i);
                        }
                    }
                }
            }
        }
        if (LOGGER.isLoggable (Level.FINE)) {
            LOGGER.log (Level.FINE, "== do listAllWithPossibleEager for " + type + " operation ==");
            for (OperationInfo info : operations) {
                LOGGER.log (Level.FINE, "--> " + info.getUpdateElement ());
            }
            if (affectedEagers != null) {
                LOGGER.log (Level.FINE, "   == includes affected eagers for " + type + " operation ==");
                for (OperationInfo eagerInfo : affectedEagers) {
                    LOGGER.log (Level.FINE, "   --> " + eagerInfo.getUpdateElement ());
                }
                LOGGER.log (Level.FINE, "   == done eagers. ==");
            }
            LOGGER.log (Level.FINE, "== done. ==");
        }
        upToDate = true;
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
            changeState (operations.remove (op));
            changeState (operations.removeAll (affectedEagers));
            affectedEagers.clear ();
        }
    }
    public synchronized void removeAll () {
        synchronized(this) {
            changeState (true);
            operations.clear ();
            affectedEagers.clear ();
        }
    }
    
    private void clearCache () {
        OperationValidator.clearMaps ();
    }
    
    private void changeState (boolean changed) {
        if (changed) {
            clearCache ();
        }
        upToDate = upToDate && ! changed;
    }
    
    public class OperationInfoImpl<Support> {
        private final UpdateElement updateElement;
        private final UpdateUnit uUnit;
        private Set<String> brokenDeps = null;
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
            brokenDeps = new HashSet<String> ();
            return OperationValidator.getRequiredElements (type, getUpdateElement (), moduleInfos, brokenDeps);
        }

        public Set<String> getBrokenDependencies () {
            if (! upToDate) {
                brokenDeps = null;
            }
            if (brokenDeps != null) {
                return brokenDeps;
            }
            List<ModuleInfo> moduleInfos = new ArrayList<ModuleInfo>();
            for (OperationContainer.OperationInfo oii : listAll ()) {
                UpdateElementImpl impl = Trampoline.API.impl (oii.getUpdateElement ());
                Collection<ModuleInfo> infos = impl.getModuleInfos ();
                assert infos != null : "ModuleInfo for UpdateElement " + oii.getUpdateElement () + " found.";
                moduleInfos.addAll (infos);
            }
            return OperationValidator.getBrokenDependencies (type, getUpdateElement (), moduleInfos);
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
        /** Uninstall <code>UpdateElement</code> on-the-fly */
        DIRECT_UNINSTALL,
        /** Update installed <code>UpdateElement</code> to newer version. */
        UPDATE,
        /** Rollback installed <code>UpdateElement</code> to previous version. */
        REVERT,
        /** Enable <code>UpdateElement</code> */
        ENABLE,
        /** Disable <code>UpdateElement</code> */
        DIRECT_DISABLE,
        /** Disable <code>UpdateElement</code> on-the-fly */
        DISABLE,
        /** Install <code>UpdateElement</code> with custom installer. */
        CUSTOM_INSTALL,
        /** Uninstall <code>UpdateElement</code> with custom installer. */
        CUSTOM_UNINSTALL
    }
    private OperationType type;
}
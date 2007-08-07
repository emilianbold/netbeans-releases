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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateManager.TYPE;
import org.netbeans.modules.autoupdate.updateprovider.ArtificialFeaturesProvider;
import org.netbeans.modules.autoupdate.updateprovider.FeatureItem;
import org.openide.util.NbBundle;


public class FeatureUpdateUnitImpl extends UpdateUnitImpl {
    private Logger err = Logger.getLogger (this.getClass ().getName ());
    private UpdateElement installedElement = null;
    private UpdateElement updateElement = null;
    private boolean initialized = false;
    private UpdateManager.TYPE type;

    public FeatureUpdateUnitImpl (String codename, UpdateManager.TYPE type) {
        super (codename);
        this.type = type;
    }

    @Override
    public UpdateElement getInstalled () {
        if (! initialized) {
            initializeFeature ();
        }
        return installedElement;
    }
    
    @Override
    public List<UpdateElement> getAvailableUpdates () {
        if (! initialized) {
            initializeFeature ();
        }
        
        if (updateElement == null) {
            return Collections.emptyList ();
        }
        
        String id = updateElement.getCodeName ();
        err.log (Level.FINE, "UpdateElement " + id + "[" +
                (installedElement == null ? "<not installed>" : installedElement.getSpecificationVersion ()) + "] has update " +
                id + "[" + updateElement.getSpecificationVersion () + "]");
        
        return Collections.singletonList (updateElement);
    }

    public TYPE getType () {
        return type;
    }
    
    private void initializeFeature () {
        List<UpdateElement> featureElements = getUpdates ();
        
        installedElement = null;
        updateElement = null;
        
        UpdateElement res = null;
        FeatureUpdateElementImpl featureImpl = null;
        Set<ModuleUpdateElementImpl> installedModules = new HashSet<ModuleUpdateElementImpl> ();
        Set<ModuleUpdateElementImpl> availableModules = new HashSet<ModuleUpdateElementImpl> ();
        Set<ModuleUpdateElementImpl> missingModules = new HashSet<ModuleUpdateElementImpl> ();
        assert featureElements != null : "FeatureUpdateUnitImpl " + getCodeName () + " contains some available elements.";
        for (UpdateElement el : featureElements) {
            featureImpl = (FeatureUpdateElementImpl) Trampoline.API.impl (el);
            boolean installed = false;
            for (ModuleUpdateElementImpl moduleImpl : featureImpl.getContainedModuleElements ()) {
                installed |= moduleImpl.getUpdateUnit ().getInstalled () != null;
                UpdateElement iue = moduleImpl.getUpdateUnit ().getInstalled ();
                UpdateElementImpl iuei = iue == null ? null : Trampoline.API.impl (iue);
                assert iuei == null || iuei instanceof ModuleUpdateElementImpl : "Impl of " + iue + " is instanceof ModuleUpdateElementImpl";
                if (iue != null) {
                    installedModules.add ((ModuleUpdateElementImpl) iuei);
                } else {
                    err.log (Level.FINER, this.getCodeName () + " misses required module " + moduleImpl.getUpdateElement ());
                    missingModules.add (moduleImpl);
                }
                if (! moduleImpl.getUpdateUnit ().getAvailableUpdates ().isEmpty ()) {
                    UpdateElement aue = moduleImpl.getUpdateUnit ().getAvailableUpdates ().get (0);
                    UpdateElementImpl auei = Trampoline.API.impl (aue);
                    assert auei instanceof ModuleUpdateElementImpl : "Impl of " + aue + " is instanceof ModuleUpdateElementImpl";
                    availableModules.add ((ModuleUpdateElementImpl) auei);
                    err.log (Level.FINER, this + " has a update of module " + moduleImpl.getUpdateElement () + " to " + auei.getUpdateElement ());
                }
            }
            if (installed) {
                res = el;
            }
        }
        
        boolean isStandalone = UpdateManager.TYPE.STANDALONE_MODULE == getType ();
        
        // if some element is whole installed
        if (res != null) {
            // create new one element contains all installed modules
            FeatureItem item = ArtificialFeaturesProvider.createFeatureItem (
                    getCodeName (),
                    installedModules,
                    featureImpl,
                    isStandalone ? null : presentAddionallyDescription (installedModules, presentMissingModules (missingModules)));
            FeatureUpdateElementImpl featureElementImpl = new FeatureUpdateElementImpl (
                    item,
                    res.getSource (),
                    installedModules,
                    featureImpl.getType ());
            installedElement = Trampoline.API.createUpdateElement (featureElementImpl);
            featureElementImpl.setUpdateUnit (res.getUpdateUnit ());
        }
        
        // add also new update element
        if (! featureElements.isEmpty () && ! availableModules.isEmpty ()) {
            // add available modules to missing
            missingModules.addAll (availableModules);
            FeatureItem item = ArtificialFeaturesProvider.createFeatureItem (
                    getCodeName (),
                    availableModules,
                    featureImpl,
                    isStandalone ? null : presentAddionallyDescription (presentUpdatableModules (missingModules), installedModules));
            FeatureUpdateElementImpl featureElementImpl = new FeatureUpdateElementImpl (
                    item,
                    featureElements.get (0).getSource (),
                    availableModules,
                    featureImpl.getType ());
            updateElement = Trampoline.API.createUpdateElement (featureElementImpl);
            featureElementImpl.setUpdateUnit (featureElements.get (0).getUpdateUnit ());
            addUpdate (updateElement);
        }
        
        initialized = true;
    }

    @Override
    public void setInstalled (UpdateElement installed) {
        assert false : "Invalid calling setInstalled (" + installed + ") on FeatureUpdateUnitImpl.";
    }

    @Override
    public void setAsUninstalled () {
        initialized = false;
    }
    
    @Override
    public void updateInstalled (UpdateElement installed) {
        initialized = false;
    }
    
    private static String getDisplayNames (Set<ModuleUpdateElementImpl> moduleImpls) {
        assert moduleImpls != null && ! moduleImpls.isEmpty () : "Some ModuleUpdateElementImpl must found to take its display names.";
        String res = "";
        for (ModuleUpdateElementImpl moduleImpl : moduleImpls) {
            res += (res.length () == 0 ? "" : ", ") + moduleImpl.getDisplayName ();
        }
        return res;
    }
    
    private static String presentMissingModules (Set<ModuleUpdateElementImpl> missingModuleImpls) {
        if (missingModuleImpls.isEmpty ()) {
            return "";
        }
        
        boolean once = missingModuleImpls.size () == 1;
        String res;
        if (once) {
            res = NbBundle.getMessage (FeatureUpdateUnitImpl.class, "FeatureUpdateUnitImpl_MissingModule", getDisplayNames (missingModuleImpls));
        } else {
            res = NbBundle.getMessage (FeatureUpdateUnitImpl.class, "FeatureUpdateUnitImpl_MissingModules", getDisplayNames (missingModuleImpls));
        }
        
        return res;
    }

    private static String presentUpdatableModules (Set<ModuleUpdateElementImpl> updatebleModuleImpls) {
        if (updatebleModuleImpls.isEmpty ()) {
            return "";
        }
        
        boolean once = updatebleModuleImpls.size () == 1;
        String res;
        if (once) {
            res = NbBundle.getMessage (FeatureUpdateUnitImpl.class, "FeatureUpdateUnitImpl_UpdatableModule", getDisplayNames (updatebleModuleImpls));
        } else {
            res = NbBundle.getMessage (FeatureUpdateUnitImpl.class, "FeatureUpdateUnitImpl_UpdatableModules", getDisplayNames (updatebleModuleImpls));
        }
        
        return res;
    }
    
    private static String presentIncludedModules (Set<ModuleUpdateElementImpl> includedModuleImpls) {
        if (includedModuleImpls.isEmpty ()) {
            return "";
        }
        
        boolean once = includedModuleImpls.size () == 1;
        String res;
        if (once) {
            res = NbBundle.getMessage (FeatureUpdateUnitImpl.class, "FeatureUpdateUnitImpl_ContainedModule", getDisplayNames (includedModuleImpls));
        } else {
            res = NbBundle.getMessage (FeatureUpdateUnitImpl.class, "FeatureUpdateUnitImpl_ContainedModules", getDisplayNames (includedModuleImpls));
        }
        
        return res;
    }
    
    private static String presentAddionallyDescription (Set<ModuleUpdateElementImpl> included, String more) {
        String add = presentIncludedModules (included) + more;
        return add.length () > 0 ? add : null;
    }

    private static String presentAddionallyDescription (String more, Set<ModuleUpdateElementImpl> included) {
        String add = more + presentIncludedModules (included);
        return add.length () > 0 ? add : null;
    }

}
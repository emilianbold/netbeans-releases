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
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;


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
        
        List<UpdateElement> res = Collections.emptyList ();
        if (updateElement != null) {
            if (installedElement == null) {
                res = Collections.singletonList (updateElement);
            } else {
                if (new SpecificationVersion(updateElement.getSpecificationVersion ()).compareTo (new SpecificationVersion(installedElement.getSpecificationVersion ())) > 0) {
                        res = Collections.singletonList (updateElement);
                        String id = updateElement.getCodeName ();
                        err.log (Level.FINE, "UpdateElement " + id + "[" + installedElement.getSpecificationVersion () + "] has update " + id + "[" + updateElement.getSpecificationVersion () + "]");
                }
            }
        }
        
        return res;
    }

    public TYPE getType () {
        return type;
    }
    
    private void initializeFeature () {
        List<UpdateElement> featureElements = getUpdates ();
        UpdateElement res = null;
        FeatureUpdateElementImpl featureImpl = null;
        Set<ModuleInfo> installedModules = new HashSet<ModuleInfo> ();
        Set<ModuleInfo> availableModules = new HashSet<ModuleInfo> ();
        assert featureElements != null : "FeatureUpdateUnitImpl " + getCodeName () + " contains some available elements.";
        for (UpdateElement el : featureElements) {
            featureImpl = (FeatureUpdateElementImpl) Trampoline.API.impl (el);
            boolean installed = ! featureImpl.getContainedModuleElements ().isEmpty ();
            for (ModuleUpdateElementImpl moduleImpl : featureImpl.getContainedModuleElements ()) {
                installed &= moduleImpl.getUpdateUnit ().getInstalled () != null;
                UpdateElement iue = moduleImpl.getUpdateUnit ().getInstalled ();
                UpdateElementImpl iuei = iue == null ? null : Trampoline.API.impl (iue);
                assert iuei ==null || iuei instanceof ModuleUpdateElementImpl : "Impl of " + iue + " is instanceof ModuleUpdateElementImpl";
                if (iue != null) {
                    installedModules.add (((ModuleUpdateElementImpl) iuei).getModuleInfo ());
                }
                if (! moduleImpl.getUpdateUnit ().getAvailableUpdates ().isEmpty ()) {
                    UpdateElement aue = moduleImpl.getUpdateUnit ().getAvailableUpdates ().get (0);
                    UpdateElementImpl auei = Trampoline.API.impl (aue);
                    assert auei instanceof ModuleUpdateElementImpl : "Impl of " + aue + " is instanceof ModuleUpdateElementImpl";
                    availableModules.add (((ModuleUpdateElementImpl) auei).getModuleInfo ());
                } else {
                    availableModules.add (((ModuleUpdateElementImpl) iuei).getModuleInfo ());
                }
            }
            if (installed) {
                res = el;
            }
        }
        
        // if some element is whole installed
        if (res != null) {
            // create new one element contains all installed modules
            FeatureItem item = ArtificialFeaturesProvider.createFeatureItem (getCodeName (), installedModules, featureImpl);
            FeatureUpdateElementImpl featureElementImpl = new FeatureUpdateElementImpl (
                    item,
                    res.getSource (),
                    featureImpl.getType ());
            installedElement = Trampoline.API.createUpdateElement (featureElementImpl);
            featureElementImpl.setUpdateUnit (res.getUpdateUnit ());
        }
        
        // add also new update element
        if (! featureElements.isEmpty () && ! availableModules.isEmpty ()) {
            FeatureItem item = ArtificialFeaturesProvider.createFeatureItem (getCodeName (), availableModules, featureImpl);
            FeatureUpdateElementImpl featureElementImpl = new FeatureUpdateElementImpl (
                    item,
                    featureElements.get (0).getSource (),
                    featureImpl.getType ());
            updateElement = Trampoline.API.createUpdateElement (featureElementImpl);
            featureElementImpl.setUpdateUnit (featureElements.get (0).getUpdateUnit ());
            addUpdate (updateElement);
        }
        
        initialized = true;
    }

    @Override
    public void setInstalled (UpdateElement installed) {
        // initialized = false;
        assert false : "Invalid calling setInstalled (" + installed + ") on FeatureUpdateUnitImpl.";
    }

    @Override
    public void setAsUninstalled () {
        installedElement = null;
    }
    
    @Override
    public void updateInstalled (UpdateElement installed) {
        initialized = false;
    }

}


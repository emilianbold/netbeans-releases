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

import java.text.ParseException;
import org.netbeans.modules.autoupdate.updateprovider.InstallInfo;
import org.netbeans.modules.autoupdate.updateprovider.FeatureItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.modules.autoupdate.updateprovider.ArtificialFeaturesProvider;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Rechtacek
 */
public class FeatureUpdateElementImpl extends UpdateElementImpl {
    private String codeName;
    private String displayName;
    private SpecificationVersion specVersion;
    private String description;
    private String homepage;
    private String category;
    private InstallInfo installInfo;
    private FeatureItem featureItem;
    private static Logger LOG = null;
    private Set<ModuleUpdateElementImpl> moduleElementsImpl;
    private UpdateManager.TYPE type;
    
    public FeatureUpdateElementImpl (FeatureItem item, String providerName, Set<ModuleUpdateElementImpl> moduleElementsImpl, UpdateManager.TYPE type) {
        super (item, providerName);
        this.type = type;
        this.moduleElementsImpl = moduleElementsImpl;
        codeName = item.getCodeName ();
        String itemSpec = item.getSpecificationVersion ();
        if (itemSpec == null) {
            getLogger ().log (Level.INFO, codeName + " has no specificationVersion.");
        } else {
            specVersion = new SpecificationVersion (itemSpec);
        }
        installInfo = new InstallInfo (item);
        displayName = item.getDisplayName ();
        description = item.getDescription ();
        this.featureItem = item;
        category = item.getCategory ();
        if (category == null) {
            category = NbBundle.getMessage (UpdateElementImpl.class, "UpdateElementImpl_Feature_CategoryName");
        }
    }
    
    public String getCodeName () {
        return codeName;
    }
    
    public String getDisplayName () {
        return displayName;
    }
    
    public SpecificationVersion getSpecificationVersion () {
        if (specVersion == null) {
            specVersion = new SpecificationVersion (ArtificialFeaturesProvider.createVersion (getModuleInfos ()));
        }
        return specVersion;
    }
    
    public String getDescription () {
        return description;
    }
    
    public String getAuthor () {
        String res = "";
        Set<String> authors = new HashSet<String> ();
        for (ModuleUpdateElementImpl impl : getContainedModuleElements ()) {
            if (impl.getAuthor () != null) {
                if (authors.add (impl.getAuthor ())) {
                    res += res.length () == 0 ? impl.getAuthor () : ", " + impl.getAuthor (); // NOI18N
                }
            }
        }
        return res;
    }
    
    public String getHomepage () {
        return homepage;
    }
    
    public int getDownloadSize () {
        int res = 0;
        for (ModuleUpdateElementImpl impl : getContainedModuleElements ()) {
            if (! impl.getUpdateUnit ().getAvailableUpdates ().isEmpty ()) {
                res += impl.getUpdateUnit ().getAvailableUpdates ().get (0).getDownloadSize ();
            }
        }
        return res;
    }
    
    public String getSource () {
        String res = "";
        Set<String> sources = new HashSet<String> ();
        for (ModuleUpdateElementImpl impl : getContainedModuleElements ()) {
            if (sources.add (impl.getSource ())) {
                res += res.length () == 0 ? impl.getSource () : ", " + impl.getSource (); // NOI18N
            }
        }
        return res;
    }
    
    public String getCategory () {
        if (isAutoload () || isFixed ()) {
            category = UpdateUnitFactory.LIBRARIES_CATEGORY;
        } else if (isEager ()) {
            category = UpdateUnitFactory.BRIDGES_CATEGORY;
        } else if (category == null || category.length () == 0) {
            category = UpdateUnitFactory.UNSORTED_CATEGORY;
        }
        return category;
    }
    
    public String getDate () {
        String res = null;
        Date date = null;
        for (ModuleUpdateElementImpl impl : getContainedModuleElements ()) {
            String sd = impl.getDate ();
            if (sd != null) {
                try {
                    Date d = Utilities.DATE_FORMAT.parse (sd);
                    date = date == null ? d : new Date (Math.max (date.getTime (), d.getTime ()));
                } catch (ParseException pe) {
                    assert false : pe + " cannot happened.";
                }
            }
        }
        if (date != null) {
            res = Utilities.DATE_FORMAT.format (date);
        }
        return res;
    }
    
    public String getLicence () {
        String res = "";
        Set<String> licenses = new HashSet<String> ();
        for (ModuleUpdateElementImpl impl : getContainedModuleElements ()) {
            if (! impl.getUpdateUnit ().getAvailableUpdates ().isEmpty ()) {
                String lic = impl.getUpdateUnit ().getAvailableUpdates ().get (0).getLicence ();
                if (licenses.add (lic)) {
                    res += res.length () == 0 ? lic : "<br>" + lic; // NOI18N
                }
            }
        }
        return res;
    }

    public InstallInfo getInstallInfo () {
        return installInfo;
    }
    
    public List<ModuleInfo> getModuleInfos () {
        List<ModuleInfo> infos = new ArrayList<ModuleInfo> ();
        for (ModuleUpdateElementImpl impl : getContainedModuleElements ()) {
            if (! infos.contains (impl.getModuleInfo ())) {
                infos.add (impl.getModuleInfo ());
            }
        }
        return infos;
    }
    
    public Set<ModuleUpdateElementImpl> getContainedModuleElements () {
        assert moduleElementsImpl != null : "FeatureUpdateElementImpl contains modules " + moduleElementsImpl;
        return moduleElementsImpl;
    }
    
    public UpdateManager.TYPE getType () {
        return type;
    }

    public boolean isEnabled () {
        boolean res = false;
        for (ModuleUpdateElementImpl impl : getContainedModuleElements ()) {
            res |= impl.isEnabled ();
        }
        return res;
    }
    
    public boolean isAutoload () {
        boolean res = true;
        for (ModuleUpdateElementImpl impl : getContainedModuleElements ()) {
            res &= impl.isAutoload ();
        }
        return res;
    }

    public boolean isEager () {
        boolean res = true;
        for (ModuleUpdateElementImpl impl : getContainedModuleElements ()) {
            res &= impl.isEager ();
        }
        return res;
    }
    
    public boolean isFixed () {
        boolean res = true;
        for (ModuleUpdateElementImpl impl : getContainedModuleElements ()) {
            res &= impl.isFixed ();
        }
        return res;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final FeatureUpdateElementImpl other = (FeatureUpdateElementImpl) obj;

        if (this.specVersion != other.specVersion &&
            (this.specVersion == null ||
             !this.specVersion.equals(other.specVersion)))
            return false;
        if (this.codeName != other.codeName &&
            (this.codeName == null || !this.codeName.equals(other.codeName)))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;

        hash = 61 * hash + (this.codeName != null ? this.codeName.hashCode()
                                                  : 0);
        hash = 61 * hash +
               (this.specVersion != null ? this.specVersion.hashCode()
                                         : 0);
        return hash;
    }
    
    private static Logger getLogger () {
        if (LOG == null) {
            LOG = Logger.getLogger (FeatureUpdateElementImpl.class.getName ());
        }
        return LOG;
    }
    
    public static class Agent extends FeatureUpdateElementImpl {
        
        private Set<ModuleUpdateElementImpl> moduleElementsImpl;
        private FeatureItem featureItem;
        
        public Agent (FeatureItem item, String providerName, UpdateManager.TYPE type) {
            super (item, providerName, null, type);
            this.featureItem = item;
        }
        
        @Override
        public Set<ModuleUpdateElementImpl> getContainedModuleElements () {
            if (moduleElementsImpl == null) {
                moduleElementsImpl = processContainedModules (featureItem.getModuleCodeNames (), null);
            }
            assert moduleElementsImpl != null : "FeatureUpdateElementImpl contains modules " + moduleElementsImpl;
            return moduleElementsImpl;
        }
        
        private Set<ModuleUpdateElementImpl> processContainedModules (Set<String> dependenciesToModules, UpdateUnitProvider provider) {
            Set<ModuleUpdateElementImpl> res = new HashSet<ModuleUpdateElementImpl> ();
            assert dependenciesToModules != null : "Invalid Feature " + this + " with null modules.";
            if (dependenciesToModules == null) {
                dependenciesToModules = Collections.emptySet ();
            } 
            Set<Dependency> deps = new HashSet<Dependency> ();
            for (String depSpec : dependenciesToModules) {
                deps.addAll (Dependency.create (Dependency.TYPE_MODULE, depSpec));
            }
            List<UpdateUnit> moduleUnits = provider == null ?
                UpdateManager.getDefault ().getUpdateUnits (UpdateManager.TYPE.MODULE) :
                provider.getUpdateUnits (UpdateManager.TYPE.MODULE);
            for (UpdateUnit unit : moduleUnits) {
                for (Dependency dep : deps) {
                    assert Dependency.TYPE_MODULE == dep.getType () : "Only Dependency.TYPE_MODULE supported, but " + dep;
                    String name = dep.getName ();
                    // trim release impl.
                    if (name.indexOf ('/') != -1) {
                        int to = name.indexOf ('/');
                        name = name.substring (0, to);
                    }
                    if (unit.getCodeName ().equals (name)) {
                        UpdateElement el = getMatchedUpdateElement (unit, dep);
                        if (el != null) {
                            assert Trampoline.API.impl (el) instanceof ModuleUpdateElementImpl : "Impl of " + el + " is instanceof ModuleUpdateElementImpl.";
                            ModuleUpdateElementImpl impl = (ModuleUpdateElementImpl) Trampoline.API.impl (el);
                            res.add (impl);
                        } else {
                            LOG.log (Level.INFO, getUpdateUnit () + " requires a module " + name + " what is not present.");
                        }
                    }
                }
            }
            return res;
        }

        private static UpdateElement getMatchedUpdateElement (UpdateUnit unit, Dependency dep) {
            // find installed
            if (match (unit.getInstalled (), dep)) {
                return unit.getInstalled ();
            } else {
                // find available updates
                if (! unit.getAvailableUpdates ().isEmpty ()) {
                    if (match (unit.getAvailableUpdates ().get (0), dep)) {
                        return unit.getAvailableUpdates ().get (0);
                    }
                }
            }
            return null;
        }

        private static boolean match (UpdateElement el, Dependency dep) {
            if (el == null) {
                return false;
            }
            return DependencyChecker.checkDependencyModuleAllowEqual (dep, Utilities.takeModuleInfo (el));
        }

   }

}

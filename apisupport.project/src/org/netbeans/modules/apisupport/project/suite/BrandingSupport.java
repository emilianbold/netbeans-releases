/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.suite;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.universe.LocalizedBundleInfo;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;

/**
 * Provide set of helper methods for branding purposes
 * @author Radek Matous
 */
final class BrandingSupport {
    private final SuiteProject suiteProject;
    private Set brandedModules = null;
    private Set brandedBundleKeys = null;
    private NbPlatform platform;
    private final File brandingDir;
    
    private static final String NAME_OF_BRANDING_FOLDER="branding";//NOI18N
    
    public static BrandingSupport getInstance(final SuiteProject suiteProject) throws IOException {
        BrandingSupport retval = new BrandingSupport(suiteProject);
        retval.init();
        
        return retval;
    }
    
    private BrandingSupport(final SuiteProject suiteProject) {
        this.suiteProject = suiteProject;
        File suiteDir = FileUtil.toFile(suiteProject.getProjectDirectory());
        assert suiteDir != null && suiteDir.exists();
        brandingDir = new File(suiteDir,NAME_OF_BRANDING_FOLDER);//NOI18N
    }
    
    /**
     * @return the project directory beneath which everything in the project lies
     */
    public File getProjectDirectory() {
        return FileUtil.toFile(suiteProject.getProjectDirectory());
    }

    /**
     * @return the top-level branding directory
     */    
    public File getBrandingRoot() {
        return new File(getProjectDirectory(),NAME_OF_BRANDING_FOLDER);
    }

    /**
     * @return the branding directory for NetBeans module represented as 
     * <code>ModuleEntry</code>
     */        
    public File getModuleEntryDirectory(ModuleEntry mEntry) {
        String relativePath;
        relativePath = PropertyUtils.relativizeFile( mEntry.getClusterDirectory(),
                mEntry.getJarLocation());
        return new File(getBrandingRoot(),relativePath);
    }

    /**
     * @return the file representing localizing bundle for NetBeans module
     */            
    public File getBundleDirectory(final ModuleEntry mEntry) {
        ManifestManager mfm = ManifestManager.getInstanceFromJAR(mEntry.getJarLocation());
        File bundle = null;
        if (mfm != null) {
            String bundlePath = mfm.getLocalizingBundle();
            if (bundlePath != null) {
                bundle = new File(getModuleEntryDirectory(mEntry),bundlePath);
            }
        }
        return bundle;
    }

    public boolean isBranded(final BundleKey key) {
        boolean retval = getListOfBrandedBundleKeys(key.getModuleEntry()).contains(key);
        if (retval) {
            assert (getBundleDirectory(key.getModuleEntry()).exists());
        }
        return retval;
        
    }
    
    /**
     * @return true if NetBeans module is already branded
     */                
    public boolean isBranded(final ModuleEntry entry) {
        boolean retval = getListOfBrandedModules().contains(entry);
        assert (retval == getModuleEntryDirectory(entry).exists());
        return retval;
    }
        
    public Set getListOfBrandedModules() {
        return brandedModules;
    }
    
    public Set getListOfBrandedBundleKeys(final ModuleEntry moduleEntry) {
        return brandedBundleKeys;
    }
    
    public Set getOrCreateBundleKeys(final String moduleCodeNameBase, final Set keys) {
        ModuleEntry foundEntry = null;
        for (Iterator it = Arrays.asList(getActivePlatform().getModules()).iterator(); it.hasNext();) {
            ModuleEntry entry = (ModuleEntry)it.next();
            if (entry.getCodeNameBase().equals(moduleCodeNameBase)) {
                foundEntry = entry;
                break;
            }
        }
        return (foundEntry != null) ? getOrCreateBundleKeys(foundEntry, keys) : null;
    }
    
    public Set getOrCreateBundleKeys(final ModuleEntry moduleEntry, final Set keys) {
        Set retval = new HashSet();
        for (Iterator it = getListOfBrandedBundleKeys(moduleEntry).iterator();
        it.hasNext() && retval.size() != keys.size();) {
            BundleKey bKey = (BundleKey)it.next();
            if (keys.contains(bKey.getKey())) {
                retval.add(bKey);
            }
        }
        
        if (retval.size() != keys.size()) {
            loadBundleKeysFromPlatform(moduleEntry, keys, retval);
        }
        return (retval.size() != keys.size()) ? null : retval;
    }
    
    public void brandBundleKeys(final Set bundleKeys) throws IOException {
        init();
        Map mentryToEditProp = new HashMap();
        for (Iterator it = bundleKeys.iterator();it.hasNext();) {
            BundleKey bKey = (BundleKey)it.next();
            if (bKey.isModified()) {
                ModuleEntry mEntry = bKey.getModuleEntry();
                EditableProperties ep = (EditableProperties)mentryToEditProp.get(mEntry);
                if (ep == null) {
                    File bundle = getBundleDirectory(mEntry);
                    if (!bundle.exists()) {
                        bundle.getParentFile().mkdirs();
                        bundle.createNewFile();
                    }
                    ep = getEditableProperties(bundle);
                    mentryToEditProp.put(mEntry, ep);
                }
                ep.setProperty(bKey.getKey(), bKey.getValue());
            }
        }
        
        for (Iterator it = mentryToEditProp.keySet().iterator();it.hasNext();) {
            ModuleEntry mEntry = (ModuleEntry)it.next();
            File bundle = getBundleDirectory(mEntry);
            assert bundle.exists();
            storeEditableProperties((EditableProperties)mentryToEditProp.get(mEntry), bundle);
            brandedModules.add(mEntry);
            for (Iterator it2 = bundleKeys.iterator();it2.hasNext();) {
                BundleKey bKey = (BundleKey)it2.next();
                ModuleEntry mEntry2 = bKey.getModuleEntry();
                if (mEntry2.equals(mEntry)) {
                    brandedBundleKeys.add(bKey);
                }
            }
        }
    }
    
    private void init() throws IOException {
        NbPlatform newPlatform = getActivePlatform();
        
        if (brandedModules == null || !newPlatform.equals(platform)) {
            brandedModules = new HashSet();
            brandedBundleKeys = new HashSet();
            platform = newPlatform;
            
            if (brandingDir.exists()) {
                assert brandingDir.isDirectory();
                scanModulesInBrandingDir(brandingDir, platform.getModules());
            }
        }
    }
    
    private NbPlatform getActivePlatform() {
        NbPlatform newPlatform = NbPlatform.getPlatformByID(
                suiteProject.getEvaluator().getProperty("nbplatform.active")); // NOI18N
        return newPlatform;
    }
    
    private  void scanModulesInBrandingDir(final File srcDir, final ModuleEntry[] platformModules) throws IOException  {
        if (srcDir.getName().endsWith(".jar")) {//NOI18N
            ModuleEntry foundEntry = null;
            for (int i = 0; i < platformModules.length; i++){
                if (isBrandingForModuleEntry(srcDir, platformModules[i])) {
                    loadBundleKeysFromBrandingDir(platformModules[i]);
                    
                    foundEntry = platformModules[i];
                    break;
                }
            }
            if (foundEntry != null) {
                brandedModules.add(foundEntry);
            } else {
                //TODO: just for testing should be deleted
                assert foundEntry != null;
            }
        } else {
            String[] kids = srcDir.list();
            assert (kids != null);
            
            for (int i = 0; i < kids.length; i++) {
                File kid = new File(srcDir, kids[i]);
                if (!kid.isDirectory()) {
                    continue;
                }
                scanModulesInBrandingDir(kid, platformModules);
            }
        }
    }
    
    private void loadBundleKeysFromBrandingDir(final ModuleEntry mEntry) throws IOException {
        File bundle = getBundleDirectory(mEntry);
        if (bundle != null && bundle.exists()) {
            loadBundleKeysFromBrandingDir(mEntry, bundle, brandedBundleKeys);
        }
        
    }
    
    private static EditableProperties getEditableProperties(final File bundle) throws IOException {
        EditableProperties p = new EditableProperties();
        InputStream is;
        is = new FileInputStream(bundle);
        try {
            p.load(is);
        } finally {
            is.close();
        }
        
        
        return p;
    }
    
    private static void storeEditableProperties(final EditableProperties p, final File bundle) throws IOException {
        OutputStream os;
        os = new FileOutputStream(bundle);
        try {
            p.store(os);
        } finally {
            os.close();
        }
    }
    
    private static void loadBundleKeysFromBrandingDir(final ModuleEntry mEntry,
            final File bundle, final Set allBundleKeys) throws IOException {
        
        EditableProperties p = getEditableProperties(bundle);
        
        for (Iterator it = p.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            allBundleKeys.add(new BundleKey(mEntry, (String)entry.getKey(), (String)entry.getValue()));
        }
    }
    
    private void loadBundleKeysFromPlatform(final ModuleEntry moduleEntry, final Set keys, final Set bundleKeys) {
        EditableProperties p;
        p = ModuleList.loadBundleInfo(moduleEntry.getSourceLocation()).toEditableProperties();
        for (Iterator it = p.keySet().iterator(); it.hasNext(); ) {
            String key = (String)it.next();
            if (keys.contains(key)) {
                String value = (String)p.getProperty(key);
                bundleKeys.add(new BundleKey(moduleEntry, key, value));
            }
        }
    }
    
    private boolean isBrandingForModuleEntry(final File srcDir, final ModuleEntry mEntry) {
        boolean retval = mEntry.getJarLocation().getName().equals(srcDir.getName());
        if (retval) {
            String relPath1 = PropertyUtils.relativizeFile( mEntry.getClusterDirectory(), mEntry.getJarLocation().getParentFile());
            String relPath2 = PropertyUtils.relativizeFile(brandingDir, srcDir.getParentFile());
            
            retval = relPath1.equals(relPath2);
        }
        return retval;
    }
    
    public static final class BundleKey {
        private final ModuleEntry moduleEntry;
        private final String key;
        private String value;
        private boolean modified = false;
        
        private BundleKey(final ModuleEntry moduleEntry, final String key, final String value) {
            this.moduleEntry = moduleEntry;
            this.key = key;
            this.value = value;
        }
        
        public ModuleEntry getModuleEntry() {
            return moduleEntry;
        }
        
        public String getKey() {
            return key;
        }
        
        public String getValue() {
            return value;
        }
        
        public void setValue(final String value) {
            if (!this.value.equals(value)) {
                modified = true;
            }
            this.value = value;
        }
        
        public boolean equals(Object obj) {
            boolean retval = false;
            
            if (obj instanceof BundleKey) {
                BundleKey bKey = (BundleKey)obj;
                retval = getKey().equals(bKey.getKey())
                && getModuleEntry().equals(bKey.getModuleEntry());
            }
            
            return  retval;
        }
        
        private boolean isModified() {
            return modified;
        }
    }
}

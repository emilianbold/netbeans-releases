/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.apisupport.project.suite;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteProperties;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbCollections;
import org.openide.util.Utilities;

/**
 * Provide set of helper methods for branding purposes.
 * @author Radek Matous
 */
public final class BrandingSupport {
    
    private final Project project;
    private Set<ModuleEntry> brandedModules;
    private Set<BundleKey> brandedBundleKeys;
    private Set<BrandedFile> brandedFiles;
    
    private NbPlatform platform;
    private final String brandingPath;
    private final File brandingDir;
    
    public static final String BRANDING_DIR_PROPERTY = "branding.dir"; // NOI18N
    private static final String BUNDLE_NAME = "Bundle.properties"; //NOI18N

    public static BrandingSupport getInstance(final SuiteProperties suiteProperties) throws IOException {
        SuiteProject suiteProject = suiteProperties.getProject();
        String brandingPath = suiteProject.getEvaluator().getProperty(BRANDING_DIR_PROPERTY);
        if (brandingPath == null) { // #125160
            brandingPath = "branding"; // NOI18N
        }
        return new BrandingSupport(suiteProject, brandingPath);
    }

    /**
     * Create branding support for non-suite projects, e.g. Maven branding module
     * @param p Project to be branded.
     * @param brandingPath Path relative to project's dir where branded resources are stored in.
     * @return New instance
     * @throws IOException
     */
    public static BrandingSupport getInstance( Project p, String brandingPath ) throws IOException {
        return new BrandingSupport(p, brandingPath);
    }
        
    private BrandingSupport(Project p, String brandingPath) throws IOException {
        this.project = p;
        this.brandingPath = brandingPath;
        File suiteDir = FileUtil.toFile(project.getProjectDirectory());
        assert suiteDir != null && suiteDir.exists();
        brandingDir = new File(suiteDir, brandingPath);//NOI18N
        init();        
    }        
    
    /**
     * @return the project directory beneath which everything in the project lies
     */
    public File getProjectDirectory() {
        return FileUtil.toFile(project.getProjectDirectory());
    }
    
    /**
     * @return the top-level branding directory
     */
    public File getBrandingRoot() {
        return new File(getProjectDirectory(), getNameOfBrandingFolder());
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
    private File getLocalizingBundle(final ModuleEntry mEntry) {
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
        boolean retval = getBrandedBundleKeys().contains(key);
        return retval;
        
    }
    
    public boolean isBranded(final BrandedFile bFile) {
        return getBrandedFiles().contains(bFile);
    }
    
    /**
     * @return true if NetBeans module is already branded
     */
    public boolean isBranded(final ModuleEntry entry) {
        boolean retval = getBrandedModules().contains(entry);
        assert (retval == getModuleEntryDirectory(entry).exists());
        return retval;
    }
    
    public Set<ModuleEntry> getBrandedModules() {
        return brandedModules;
    }
    
    public Set<BundleKey> getBrandedBundleKeys() {
        return brandedBundleKeys;
    }
    
    public Set getBrandedFiles() {
        return brandedFiles;
    }
    
    public Set<BundleKey> getLocalizingBundleKeys(final String moduleCodeNameBase, final Set<String> keys) {
        ModuleEntry foundEntry = getModuleEntry(moduleCodeNameBase);
        return (foundEntry != null) ? getLocalizingBundleKeys(foundEntry, keys) : null;
    }
    
    public Set<BundleKey> getLocalizingBundleKeys(final ModuleEntry moduleEntry, final Set<String> keys) {
        Set<BundleKey> retval = new HashSet<BundleKey>();
        for (Iterator<BundleKey> it = getBrandedBundleKeys().iterator();
        it.hasNext() && retval.size() != keys.size();) {
            BundleKey bKey = it.next();
            if (keys.contains(bKey.getKey())) {
                retval.add(bKey);
            }
        }
        
        if (retval.size() != keys.size()) {
            loadLocalizedBundlesFromPlatform(moduleEntry, keys, retval);
        }
        return (retval.size() != keys.size()) ? null : retval;
    }
    
    public BrandedFile getBrandedFile(final String moduleCodeNameBase, final String entryPath) {
        ModuleEntry foundEntry = getModuleEntry(moduleCodeNameBase);
        return (foundEntry != null) ? getBrandedFile(foundEntry,entryPath) : null;
    }
    
    public BrandedFile getBrandedFile(final ModuleEntry moduleEntry, final String entryPath) {
        BrandedFile retval = null;
        try {
            retval = new BrandedFile(moduleEntry, entryPath);
            for (Iterator it = getBrandedFiles().iterator();it.hasNext() ;) {
                BrandedFile bFile = (BrandedFile)it.next();
                
                if (retval.equals(bFile)) {
                    retval = bFile;
                    
                }
            }
        } catch (MalformedURLException ex) {
            retval = null;
        }
        return retval;
    }
    
    public BundleKey getBundleKey(final String moduleCodeNameBase,
            final String bundleEntry,final String key) {
        Set<BundleKey> keys = getBundleKeys(moduleCodeNameBase, bundleEntry, Collections.singleton(key));
        return (keys == null) ? null : (BrandingSupport.BundleKey) keys.toArray()[0];
    }
    
    public Set<BundleKey> getBundleKeys(final String moduleCodeNameBase, final String bundleEntry, final Set<String> keys) {
        ModuleEntry foundEntry = getModuleEntry(moduleCodeNameBase);
        return (foundEntry != null) ? getBundleKeys(foundEntry, bundleEntry, keys) : null;
    }
    
    public Set<BundleKey> getBundleKeys(final ModuleEntry moduleEntry, final String bundleEntry, final Set<String> keys) {
        Set<BundleKey> retval = new HashSet<BundleKey>();
        for (Iterator<BundleKey> it = getBrandedBundleKeys().iterator();
        it.hasNext() && retval.size() != keys.size();) {
            BundleKey bKey = it.next();
            if (keys.contains(bKey.getKey())) {
                retval.add(bKey);
            } 
        }
        
        if (retval.size() != keys.size()) {
            try {
                loadLocalizedBundlesFromPlatform(moduleEntry, bundleEntry, keys, retval);
            } catch (IOException ex) {
                //ex.printStackTrace();
                throw new IllegalStateException();
            }
        }
                    
        return (retval.size() != keys.size()) ? null : retval;
    }
    
    private ModuleEntry getModuleEntry(final String moduleCodeNameBase) {
        NbPlatform p = getActivePlatform();
        return p != null ? p.getModule(moduleCodeNameBase) : null;
    }

    private NbPlatform getActivePlatform() {
        NbPlatform retval = null;
        if( project instanceof SuiteProject ) {
            ((SuiteProject)project).getPlatform(true);
        } else {
            NbModuleProvider moduleProvider = project.getLookup().lookup(NbModuleProvider.class);
            if( null != moduleProvider ) {
                File platformDir = moduleProvider.getActivePlatformLocation();
                if( null != platformDir )
                    retval = NbPlatform.getPlatformByDestDir(platformDir);
            }
        }
        if (retval != null) {
            return retval;
        } else {
            return NbPlatform.getDefaultPlatform();
        }
    }
    
    public void brandFile(final BrandedFile bFile) throws IOException {
        if (!bFile.isModified()) return;

        File target = bFile.getFileLocation();
        if (!target.exists()) {
            target.getParentFile().mkdirs();
            target.createNewFile();
        }
        
        assert target.exists();
        FileObject fo = FileUtil.toFileObject(target);
        InputStream is = null;
        OutputStream os = null;
        try {
            is = bFile.getBrandingSource().openStream();
            os = fo.getOutputStream();
            FileUtil.copy(is, os);
        } finally {
            if (is != null) {
                is.close();
            }
            
            if (os != null) {
                os.close();
            }
            
            brandedFiles.add(bFile);
            bFile.modified = false;
        }
    }
    
    public void brandFile(final BrandedFile bFile, final Runnable saveTask) throws IOException {
        if (!bFile.isModified()) return;
        
        saveTask.run();
        brandedFiles.add(bFile);
        bFile.modified = false;
    }
    
    public void brandBundleKey(final BundleKey bundleKey) throws IOException {
        if (bundleKey == null) {
            return;
        }
        brandBundleKeys(Collections.singleton(bundleKey));
    }
    
    public void brandBundleKeys(final Set<BundleKey> bundleKeys) throws IOException {
        init();
        Map<File,EditableProperties> mentryToEditProp = new HashMap<File,EditableProperties>();
        for (BundleKey bKey : bundleKeys) {
            if (bKey.isModified()) {
                EditableProperties ep = mentryToEditProp.get(bKey.getBrandingBundle());
                if (ep == null) {
                    File bundle = bKey.getBrandingBundle();
                    if (!bundle.exists()) {
                        bundle.getParentFile().mkdirs();
                        bundle.createNewFile();
                    }
                    ep = getEditableProperties(bundle);
                    mentryToEditProp.put(bKey.getBrandingBundle(), ep);
                }
                ep.setProperty(bKey.getKey(), bKey.getValue());
            }
        }
        
        for (Map.Entry<File,EditableProperties> entry : mentryToEditProp.entrySet()) {
            File bundle = entry.getKey();
            assert bundle.exists();
            storeEditableProperties(entry.getValue(), bundle);
            for (BundleKey bKey: bundleKeys) {
                File bundle2 = bKey.getBrandingBundle();
                if (bundle2.equals(bundle)) {
                    brandedBundleKeys.add(bKey);
                    bKey.modified = false;
                    brandedModules.add(bKey.getModuleEntry());
                }
            }
        }
    }
    
    private void init() throws IOException {
        NbPlatform newPlatform = getActivePlatform();
        if (newPlatform == null) {
            return;
        }
        
        if (brandedModules == null || !newPlatform.equals(platform)) {
            brandedModules = new HashSet<ModuleEntry>();
            brandedBundleKeys = new HashSet<BundleKey>();
            brandedFiles = new HashSet<BrandedFile>();
            platform = newPlatform;
            
            if (brandingDir.exists()) {
                assert brandingDir.isDirectory();
                scanModulesInBrandingDir(brandingDir, platform.getModules());
            }
        }
    }
    
    private  void scanModulesInBrandingDir(final File srcDir, final Set<ModuleEntry> platformModules) throws IOException  {
        if (srcDir.getName().endsWith(".jar")) {//NOI18N
            ModuleEntry foundEntry = null;
            for (ModuleEntry platformModule : platformModules) {
                if (isBrandingForModuleEntry(srcDir, platformModule)) {
                    scanBrandedFiles(srcDir, platformModule);
                    
                    foundEntry = platformModule;
                    break;
                }
            }
            if (foundEntry != null) {
                brandedModules.add(foundEntry);
            }
        } else {
            String[] kids = srcDir.list();
            assert (kids != null);
            
            for (String kidName : kids) {
                File kid = new File(srcDir, kidName);
                if (!kid.isDirectory()) {
                    continue;
                }
                scanModulesInBrandingDir(kid, platformModules);
            }
        }
    }
    
    private void scanBrandedFiles(final File srcDir, final ModuleEntry mEntry) throws IOException {
        String[] kids = srcDir.list();
        assert (kids != null);
        
        for (String kidName : kids) {
            File kid = new File(srcDir, kidName);
            if (!kid.isDirectory()) {
                if (kid.getName().endsWith(BUNDLE_NAME)) {
                    loadBundleKeys(mEntry, kid);
                } else {
                    loadBrandedFiles(mEntry, kid);
                }
                
                continue;
            }
            scanBrandedFiles(kid, mEntry);
        }
    }
    
    private void loadBundleKeys(final ModuleEntry mEntry,
            final File bundle) throws IOException {
        EditableProperties p = getEditableProperties(bundle);
        for (Map.Entry<String,String> entry : p.entrySet()) {
            brandedBundleKeys.add(new BundleKey(mEntry, bundle, entry.getKey(), entry.getValue()));
        }
    }
    
    private void loadBrandedFiles(final ModuleEntry mEntry,
            final File file) throws IOException {
        String entryPath = PropertyUtils.relativizeFile(getModuleEntryDirectory(mEntry),file);
        BrandedFile bf = new BrandedFile(mEntry, file.toURI().toURL(), entryPath);
        brandedFiles.add(bf);
    }
    
    
    private static EditableProperties getEditableProperties(final File bundle) throws IOException {
        EditableProperties p = new EditableProperties(true);
        InputStream is = new FileInputStream(bundle);
        try {
            p.load(is);
        } finally {
            is.close();
        }
        return p;
    }
    
    private static void storeEditableProperties(final EditableProperties p, final File bundle) throws IOException {
        FileObject fo = FileUtil.toFileObject(bundle);
        OutputStream os = null == fo ? new FileOutputStream(bundle) : fo.getOutputStream();
        try {
            p.store(os);
        } finally {
            os.close();
        }
    }

    
    private void loadLocalizedBundlesFromPlatform(final ModuleEntry moduleEntry, final Set<String> keys, final Set<BundleKey> bundleKeys) {
        EditableProperties p = ModuleList.loadBundleInfo(moduleEntry.getSourceLocation()).toEditableProperties();
        for (String key : p.keySet()) {
            if (keys.contains(key)) {
                String value = p.getProperty(key);
                bundleKeys.add(new BundleKey(moduleEntry, key, value));
            }
        }
    }
    
    private void loadLocalizedBundlesFromPlatform(final ModuleEntry moduleEntry,
            final String bundleEntry, final Set<String> keys, final Set<BundleKey> bundleKeys) throws IOException {
        Properties p = new Properties();
        JarFile module = new JarFile(moduleEntry.getJarLocation());
        JarEntry je = module.getJarEntry(bundleEntry);
        InputStream is = module.getInputStream(je);
        File bundle = new File(getModuleEntryDirectory(moduleEntry),bundleEntry);
        try {
            
            p.load(is);
        } finally {
            is.close();
        }
        for (String key : NbCollections.checkedMapByFilter(p, String.class, String.class, true).keySet()) {
            if (keys.contains(key)) {
                String value = p.getProperty(key);
                bundleKeys.add(new BundleKey(moduleEntry, bundle, key, value));
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
    
    public final class BundleKey {
        private final File brandingBundle;
        private final ModuleEntry moduleEntry;
        private final @NonNull String key;
        private @NonNull String value;
        private boolean modified = false;
        
        private BundleKey(final ModuleEntry moduleEntry, final File brandingBundle, final String key, final String value) {
            this.moduleEntry = moduleEntry;
            assert key != null && value != null;
            this.key = key;
            this.value = value;
            this.brandingBundle = brandingBundle;
        }
        
        private BundleKey(final ModuleEntry mEntry, final String key, final String value) {
            this(mEntry, getLocalizingBundle(mEntry), key,value);
        }
        
        public ModuleEntry getModuleEntry() {
            return moduleEntry;
        }
        
        public @NonNull String getKey() {
            return key;
        }
        
        public @NonNull String getValue() {
            return value;
        }
        
        public void setValue(@NonNull String value) {
            assert value != null;
            if (!this.value.equals(value)) {
                modified = true;
            }
            this.value = value;
        }
        
        @Override
        public boolean equals(Object obj) {
            boolean retval = false;
            
            if (obj instanceof BundleKey) {
                BundleKey bKey = (BundleKey)obj;
                retval = getKey().equals(bKey.getKey())
                && getModuleEntry().equals(bKey.getModuleEntry())
                && getBrandingBundle().equals(bKey.getBrandingBundle());
            }
            
            return  retval;
        }
        
        @Override
        public int hashCode() {
            return 0;
        }
        
        boolean isModified() {
            return modified;
        }
        
        private File getBrandingBundle() {
            return brandingBundle;
        }

        public String getBundleFilePath() {
            return  brandingBundle.getPath();
        }
    }
    
    public class BrandedFile {
        private final ModuleEntry moduleEntry;
        private final String entryPath;
        private URL brandingSource;
        private boolean modified = false;
        
        private BrandedFile(final ModuleEntry moduleEntry, final String entry) throws MalformedURLException {
            this(moduleEntry, null, entry);
        }
        
        private BrandedFile(final ModuleEntry moduleEntry, final URL source, final String entry) throws MalformedURLException {
            this.moduleEntry = moduleEntry;
            this.entryPath = entry;
            if (source == null) {
                brandingSource = moduleEntry.getJarLocation().toURI().toURL();
                brandingSource =  new URL("jar:" + brandingSource + "!/" + entryPath); // NOI18N
            } else {
                brandingSource = source;
            }
            
        }
        
        public ModuleEntry getModuleEntry() {
            return moduleEntry;
        }
        
        public String getEntryPath() {
            return entryPath;
        }
        
        public File getFileLocation() {
            return new File(getModuleEntryDirectory(getModuleEntry()), getEntryPath());
        }
        
        public URL getBrandingSource()  {
            return brandingSource;
        }
        
        public void setBrandingSource(URL brandingSource) {
            if (!Utilities.compareObjects(brandingSource, this.brandingSource)) {
                modified = true;
            }
            this.brandingSource = brandingSource;
        }
        
        public void setBrandingSource(File brandingFile) throws MalformedURLException {
            setBrandingSource(brandingFile.toURI().toURL());
        }
        
        public boolean isModified() {
            return modified;
        }
        
        @Override
        public boolean equals(Object obj) {
            boolean retval = false;
            
            if (obj instanceof BrandedFile) {
                BrandedFile bFile = (BrandedFile)obj;
                retval = getModuleEntry().equals(bFile.getModuleEntry())
                && getFileLocation().equals(bFile.getFileLocation());
            }
            
            //if ()
            return  retval;
        }

        @Override
        public int hashCode() {
            return 0;
        }
        
    }

    public String getNameOfBrandingFolder() {
        return brandingPath;
    }
}

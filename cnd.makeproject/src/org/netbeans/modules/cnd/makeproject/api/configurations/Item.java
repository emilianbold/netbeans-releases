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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.makeproject.api.configurations;

import org.netbeans.modules.cnd.makeproject.spi.configurations.AllOptionsProvider;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItem.Language;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.makeproject.spi.configurations.UserOptionsProvider;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.MIMESupport;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;

public class Item implements NativeFileItem, PropertyChangeListener {

    private final String path;
    //private final String sortName;
    private Folder folder;
    private File file = null;
    private DataObject lastDataObject = null;

    public Item(String path) {
        this.path = path;
        //this.sortName = CndPathUtilitities.getBaseName(path);
//        int i = sortName.lastIndexOf("."); // NOI18N
//        if (i > 0) {
//            this.sortName = sortName.substring(0, i);
//        } else {
//            this.sortName = sortName;
//        }
        folder = null;
    }
    
    private void rename(String newname, boolean nameWithoutExtension) {
        if (newname == null || newname.length() == 0 || getFolder() == null) {
            return;
        }
        if (path.equals(newname)) {
            return;
        }

        // Rename name in path
        int indexName = path.lastIndexOf('/');
        if (indexName < 0) {
            indexName = 0;
        } else {
            indexName++;
        }

        int indexDot = path.lastIndexOf('.');
        if (indexDot < indexName || !nameWithoutExtension) {
            indexDot = -1;
        }

        String oldname;
        if (indexDot >= 0) {
            oldname = path.substring(indexName, indexDot);
        } else {
            oldname = path.substring(indexName);
        }
        if (oldname.equals(newname)) {
            return;
        }

        String newPath = ""; // NOI18N
        if (indexName > 0) {
            newPath = path.substring(0, indexName);
        }
        newPath += newname;
        if (indexDot >= 0) {
            newPath += path.substring(indexDot);
        }
        // Remove old item and insert new with new name
        renameTo(newPath);
    }

    private void renameTo(String newPath) {
        Folder f = getFolder();
        String oldPath = getAbsPath();
        Item item = f.addItem(new Item(newPath));
        if (item != null && item.getFolder() != null) {
            if (item.getFolder().isProjectFiles()) {
                copyItemConfigurations(this, item);
            }
            f.removeItem(this);
            f.renameItemAction(oldPath, item);
        }
    }

    public String getPath() {
        return path;
    }

    public String getSortName() {
        //return sortName;
        return getName();
    }

    public String getName() {
        return CndPathUtilitities.getBaseName(path);
    }

    public String getPath(boolean norm) {
        String pat = "./"; // UNIX path  // NOI18N
        if (norm && getPath().startsWith(pat)) {
            return getPath().substring(2);
        } else {
            return getPath();
        }
    }

    public String getAbsPath() {
        String retPath = null;
        if (CndPathUtilitities.isPathAbsolute(getPath())) {// UNIX path
            retPath = getPath();
        } else if (getFolder() != null) {
            retPath = getFolder().getConfigurationDescriptor().getBaseDir() + '/' + getPath(); // UNIX path
        }
        return retPath;
    }

    public void setFolder(Folder folder) {
        if (folder == null && file == null) {
            // store file in field. method getFile() will works after removing item
            getCanonicalFile();
        }
        // leave folder if it is remove
        if (folder == null) { // Item is removed, let's clean up.
            synchronized (this) {
                if (lastDataObject != null) {
                    lastDataObject.removePropertyChangeListener(this);
                    lastDataObject = null;
                }
            }
        } else {
            this.folder = folder;
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("name")) { // NOI18N
            // File has been renamed
            boolean nameWithoutExtension = true;
            Object o = evt.getSource();
            if (o instanceof DataObject) {
                String nodeName = ((DataObject) o).getName();
                FileObject fo = ((DataObject) o).getPrimaryFile();
                if (fo != null) {
                    String fileName = fo.getNameExt();
                    if (nodeName.equals(fileName)) {
                        nameWithoutExtension = false;
                    }

                }
            }
            rename(newName, nameWithoutExtension);
        } else if (evt.getPropertyName().equals("valid")) { // NOI18N
            // File has been deleted
            // Do nothing (IZ 87557, 94935)
            if (!((Boolean) evt.getNewValue()).booleanValue()) {
//                getFolder().removeItemAction(this);
                Folder containingFolder = getFolder();
                if (containingFolder != null) {
                    containingFolder.refresh(this);
                }
            }
        } else if (evt.getPropertyName().equals("primaryFile")) { // NOI18N
            // File has been moved
            if (getFolder() != null) {
                FileObject fo = (FileObject) evt.getNewValue();
                String newPath = FileUtil.toFile(fo).getPath();
                if (!CndPathUtilitities.isPathAbsolute(getPath())) {
                    newPath = CndPathUtilitities.toRelativePath(getFolder().getConfigurationDescriptor().getBaseDir(), newPath);
                }
                newPath = CndPathUtilitities.normalize(newPath);
                renameTo(newPath);
            }
        }
    }

    public Folder getFolder() {
        return folder;
    }

    public File getNormalizedFile() {
        String aPath = getAbsPath();
        if (aPath != null) {
            return CndFileUtils.normalizeFile(new File(aPath));
        }
        return file;
    }

    @Override
    public File getFile() {
        // let's try to use normalized, not canonical paths
        return getNormalizedFile();
    }

    public File getCanonicalFile() {
        if (file == null) {
            try {
                file = new File(getAbsPath()).getCanonicalFile();
            } catch (IOException ioe) {
                file = CndFileUtils.normalizeFile(new File(getAbsPath()));
            }
        }
        return file;
    }

    public String getId() {
        // ID of other objects shouldn't be like to path
        return getPath();
    }

    public ItemConfiguration getItemConfiguration(Configuration configuration) {
        if (configuration != null) {
            return (ItemConfiguration) configuration.getAuxObject(getId());
        }
        return null;
    }

    public ItemConfiguration[] getItemConfigurations() {
        ItemConfiguration[] itemConfigurations;
        MakeConfigurationDescriptor makeConfigurationDescriptor = getMakeConfigurationDescriptor();
        if (makeConfigurationDescriptor == null) {
            return new ItemConfiguration[0];
        }
        Configuration[] configurations = makeConfigurationDescriptor.getConfs().toArray();
        itemConfigurations = new ItemConfiguration[configurations.length];
        for (int i = 0; i < configurations.length; i++) {
            itemConfigurations[i] = getItemConfiguration(configurations[i]);
        }
        return itemConfigurations;
    }

    public void copyConfigurations(Item src) {
        if (src.getFolder() == null) {
            return;
        }

        MakeConfigurationDescriptor makeConfigurationDescriptor = src.getFolder().getConfigurationDescriptor();
        if (makeConfigurationDescriptor == null) {
            return;
        }

        for (Configuration conf : makeConfigurationDescriptor.getConfs().toArray()) {
            ItemConfiguration srcItemConfiguration = src.getItemConfiguration(conf);
            ItemConfiguration dstItemConfiguration = getItemConfiguration(conf);
            if (srcItemConfiguration != null && dstItemConfiguration != null) {
                dstItemConfiguration.assignValues(srcItemConfiguration);
            }
        }
    }

    /**
     * Copies configuration from <code>src</code> item to <code>dst</code> item.
     * Both items must be assigned to folders to correctly operate with
     * their configurations. Otherwise NPEs will be thrown.
     *
     * @param src  item to copy configuration from
     * @param dst  item to copy configuration to
     */
    private static void copyItemConfigurations(Item src, Item dst) {
        MakeConfigurationDescriptor makeConfigurationDescriptor = src.getMakeConfigurationDescriptor();
        if (makeConfigurationDescriptor != null) {
            for (Configuration conf : makeConfigurationDescriptor.getConfs().toArray()) {
                ItemConfiguration newConf = new ItemConfiguration(conf, dst);
                newConf.assignValues(src.getItemConfiguration(conf));
                conf.addAuxObject(newConf);
            }
        }
    }

    public FileObject getFileObject() {
        File curFile = getNormalizedFile();
        FileObject fo = FileUtil.toFileObject(curFile);
        if (fo == null) {
            fo = FileUtil.toFileObject(getCanonicalFile());
        }
        return fo;
    }

    public DataObject getDataObject() {
        synchronized (this) {
            if (lastDataObject != null && lastDataObject.isValid()){
                return lastDataObject;
            }
        }
        DataObject dataObject = null;
        FileObject fo = getFileObject();
        if (fo != null) {
            try {
                dataObject = DataObject.find(fo);
            } catch (DataObjectNotFoundException e) {
                // should not happen
                ErrorManager.getDefault().notify(e);
            }
        }
        synchronized (this) {
            if (dataObject != lastDataObject) {
                // DataObject can change without notification. We need to track this
                // and properly attach/detach listeners.
                if (lastDataObject != null) {
                    lastDataObject.removePropertyChangeListener(this);
                }
                if (dataObject != null) {
                    dataObject.addPropertyChangeListener(this);
                }
                lastDataObject = dataObject;
            }
        }
        return dataObject;
    }

    public final String getMIMEType() {
        DataObject dataObject = getDataObject();
        FileObject fo = dataObject == null ? null : dataObject.getPrimaryFile();
        String mimeType = "";
        if (fo == null) {
            mimeType = MIMESupport.getFileMIMEType(getNormalizedFile());
        } else {
            mimeType = MIMESupport.getFileMIMEType(fo);
        }
        return mimeType;
    }

    public PredefinedToolKind getDefaultTool() {
        PredefinedToolKind tool;
        String mimeType = getMIMEType();
        if (MIMENames.C_MIME_TYPE.equals(mimeType)) {
            tool = PredefinedToolKind.CCompiler;
        } else if (MIMENames.HEADER_MIME_TYPE.equals(mimeType)) {
            tool = PredefinedToolKind.CustomTool;
        } else if (MIMENames.CPLUSPLUS_MIME_TYPE.equals(mimeType)) {
            tool = PredefinedToolKind.CCCompiler;
        } else if (MIMENames.FORTRAN_MIME_TYPE.equals(mimeType)) {
            tool = PredefinedToolKind.FortranCompiler;
        } else if (MIMENames.ASM_MIME_TYPE.equals(mimeType)) {
            tool = PredefinedToolKind.Assembler;
        } else {
            tool = PredefinedToolKind.CustomTool;
        }
        return tool;
    }

    private MakeConfigurationDescriptor getMakeConfigurationDescriptor() {
        if (getFolder() == null) {
            return null;
        }
        return getFolder().getConfigurationDescriptor();
    }

    private MakeConfiguration getMakeConfiguration() {
        MakeConfigurationDescriptor makeConfigurationDescriptor = getMakeConfigurationDescriptor();
        if (makeConfigurationDescriptor == null) {
            return null;
        }
        return makeConfigurationDescriptor.getActiveConfiguration();
    }

    @Override
    public NativeProject getNativeProject() {
        Folder curFolder = getFolder();
        if (curFolder != null) {
            Project project = curFolder.getProject();
            if (project != null) {
                return project.getLookup().lookup(NativeProject.class);
            }
        }
        return null;
    }

    @Override
    public List<String> getSystemIncludePaths() {
        List<String> vec = new ArrayList<String>();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        ItemConfiguration itemConfiguration = getItemConfiguration(makeConfiguration);//ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) // FIXUP: sometimes itemConfiguration is null (should not happen)
        {
            return vec;
        }
        CompilerSet compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
        if (compilerSet == null) {
            return vec;
        }
        AbstractCompiler compiler = (AbstractCompiler) compilerSet.getTool(itemConfiguration.getTool());
        BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
        if (compilerConfiguration instanceof CCCCompilerConfiguration) {
            // Get include paths from compiler
            if (compiler != null && compiler.getPath() != null && compiler.getPath().length() > 0) {
                vec.addAll(compiler.getSystemIncludeDirectories());
            }
        }
        return vec;
    }

    @Override
    public List<String> getUserIncludePaths() {
        List<String> vec = new ArrayList<String>();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        ItemConfiguration itemConfiguration = getItemConfiguration(makeConfiguration);//ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) // FIXUP: sometimes itemConfiguration is null (should not happen)
        {
            return vec;
        }
        CompilerSet compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
        if (compilerSet == null) {
            return vec;
        }
        AbstractCompiler compiler = (AbstractCompiler) compilerSet.getTool(itemConfiguration.getTool());
        BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
        if (compilerConfiguration instanceof CCCCompilerConfiguration) {
            // Get include paths from project/file
            List<String> vec2 = new ArrayList<String>();
            CCCCompilerConfiguration cccCompilerConfiguration = (CCCCompilerConfiguration) compilerConfiguration;
            CCCCompilerConfiguration master = (CCCCompilerConfiguration) cccCompilerConfiguration.getMaster();
            while (master != null && cccCompilerConfiguration.getInheritIncludes().getValue()) {
                vec2.addAll(master.getIncludeDirectories().getValue());
                if (master.getInheritIncludes().getValue()) {
                    master = (CCCCompilerConfiguration) master.getMaster();
                } else {
                    master = null;
                }
            }
            vec2.addAll(cccCompilerConfiguration.getIncludeDirectories().getValue());
            // Convert all paths to absolute paths
            Iterator<String> iter = vec2.iterator();
            while (iter.hasNext()) {
                vec.add(CndPathUtilitities.toAbsolutePath(getFolder().getConfigurationDescriptor().getBaseDir(), iter.next()));
            }
            if (cccCompilerConfiguration instanceof AllOptionsProvider) {
                vec = SPI_ACCESSOR.getItemUserIncludePaths(vec, (AllOptionsProvider) cccCompilerConfiguration, compiler, makeConfiguration);
            }
        }
        return vec;
    }

    @Override
    public List<String> getSystemMacroDefinitions() {
        List<String> vec = new ArrayList<String>();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        ItemConfiguration itemConfiguration = getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) // FIXUP: itemConfiguration should never be null
        {
            return vec;
        }
        CompilerSet compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
        if (compilerSet == null) {
            return vec;
        }
        AbstractCompiler compiler = (AbstractCompiler) compilerSet.getTool(itemConfiguration.getTool());
        BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
        if (compilerConfiguration instanceof CCCCompilerConfiguration) {
            if (compiler != null && compiler.getPath() != null && compiler.getPath().length() > 0) {
                // Get macro definitions from compiler
                vec.addAll(compiler.getSystemPreprocessorSymbols());
            }
        }
        return vec;
    }

    @Override
    public List<String> getUserMacroDefinitions() {
        List<String> vec = new ArrayList<String>();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        ItemConfiguration itemConfiguration = getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) // FIXUP: itemConfiguration should never be null
        {
            return vec;
        }
        CompilerSet compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
        if (compilerSet == null) {
            return vec;
        }
        AbstractCompiler compiler = (AbstractCompiler) compilerSet.getTool(itemConfiguration.getTool());
        BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
        if (compilerConfiguration instanceof CCCCompilerConfiguration) {
            CCCCompilerConfiguration cccCompilerConfiguration = (CCCCompilerConfiguration) compilerConfiguration;
            CCCCompilerConfiguration master = (CCCCompilerConfiguration) cccCompilerConfiguration.getMaster();
            while (master != null && cccCompilerConfiguration.getInheritPreprocessor().getValue()) {
                vec.addAll(master.getPreprocessorConfiguration().getValue());
                if (master.getInheritIncludes().getValue()) {
                    master = (CCCCompilerConfiguration) master.getMaster();
                } else {
                    master = null;
                }
            }
            vec.addAll(cccCompilerConfiguration.getPreprocessorConfiguration().getValue());
            if (cccCompilerConfiguration instanceof AllOptionsProvider) {
                vec = SPI_ACCESSOR.getItemUserMacros(vec, (AllOptionsProvider) cccCompilerConfiguration, compiler, makeConfiguration);
            }
        }
        return vec;
    }

    public boolean hasHeaderOrSourceExtension(boolean cFiles, boolean ccFiles) {
        // Method return true for source files also.
        String mimeType = getMIMEType();
        return MIMENames.HEADER_MIME_TYPE.equals(mimeType) ||
                (ccFiles && MIMENames.CPLUSPLUS_MIME_TYPE.equals(mimeType)) ||
                (cFiles && MIMENames.C_MIME_TYPE.equals(mimeType));
    }

    /**
     * NativeFileItem interface
     **/
    @Override
    public Language getLanguage() {
        PredefinedToolKind tool;
        Language language;
        ItemConfiguration itemConfiguration = null;
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        if (makeConfiguration != null) {
            itemConfiguration = getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(getPath()));
        }
        if (itemConfiguration != null) {
            tool = itemConfiguration.getTool();
        } else {
            tool = getDefaultTool();
        }

        if (tool == PredefinedToolKind.CCompiler) {
            language = NativeFileItem.Language.C;
        } else if (tool == PredefinedToolKind.CCCompiler) {
            language = NativeFileItem.Language.CPP;
        } else if (tool == PredefinedToolKind.FortranCompiler) {
            language = NativeFileItem.Language.FORTRAN;
        } else if (hasHeaderOrSourceExtension(true, true)) {
            language = NativeFileItem.Language.C_HEADER;
        } else {
            language = NativeFileItem.Language.OTHER;
        }

        return language;
    }

    /**
     * NativeFileItem interface
     **/
    @Override
    public LanguageFlavor getLanguageFlavor() {
        return NativeFileItem.LanguageFlavor.GENERIC;
    }

    /**
     * NativeFileItem interface
     **/
    @Override
    public boolean isExcluded() {
        ItemConfiguration itemConfiguration = getItemConfiguration(getMakeConfiguration());
        if (itemConfiguration != null) {
            return itemConfiguration.getExcluded().getValue();
        }
        return true;
    }

    @Override
    public String toString() {
        return path;
    }
    private static final SpiAccessor SPI_ACCESSOR = new SpiAccessor();

    private static final class SpiAccessor {

        private UserOptionsProvider provider;

        private synchronized UserOptionsProvider getProvider() {
            if (provider == null) {
                provider = Lookup.getDefault().lookup(UserOptionsProvider.class);
            }
            return provider;
        }

        private SpiAccessor() {
        }

        private List<String> getItemUserIncludePaths(List<String> includes, AllOptionsProvider compilerOptions, AbstractCompiler compiler, MakeConfiguration makeConfiguration) {
            if (getProvider() != null) {
                return getProvider().getItemUserIncludePaths(includes, compilerOptions, compiler, makeConfiguration);
            } else {
                return includes;
            }
        }

        private List<String> getItemUserMacros(List<String> macros, AllOptionsProvider compilerOptions, AbstractCompiler compiler, MakeConfiguration makeConfiguration) {
            if (getProvider() != null) {
                return getProvider().getItemUserMacros(macros, compilerOptions, compiler, makeConfiguration);
            } else {
                return macros;
            }
        }
    }
}

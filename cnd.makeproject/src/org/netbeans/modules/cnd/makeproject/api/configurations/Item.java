/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItem.Language;
import org.netbeans.modules.cnd.api.project.NativeFileItemSet;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.makeproject.BrokenReferencesSupport;
import org.netbeans.modules.cnd.makeproject.spi.configurations.AllOptionsProvider;
import org.netbeans.modules.cnd.makeproject.spi.configurations.IncludePathExpansionProvider;
import org.netbeans.modules.cnd.makeproject.spi.configurations.UserOptionsProvider;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.MIMESupport;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.dlight.libs.common.InvalidFileObjectSupport;
import org.netbeans.modules.dlight.libs.common.PerformanceLogger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory.MacroExpander;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

public final class Item implements NativeFileItem, PropertyChangeListener {
    private static final Logger LOG = Logger.getLogger("makeproject.folder"); // NOI18N

    private final String path;
    private Folder folder;
    private FileObject file = null;
    private final FileSystem fileSystem;
    private final String normalizedPath;
    private DataObject lastDataObject = null;

    public static Item createInBaseDir(FileObject baseDirFileObject, String path) {
        return new Item(baseDirFileObject, path);
    }
    
    private Item(FileObject baseDirFileObject, String path) {
        try {
            this.fileSystem = baseDirFileObject.getFileSystem();
        } catch (FileStateInvalidException ex) {
            throw new IllegalStateException(ex);
        }
        String absPath = CndPathUtilities.toAbsolutePath(baseDirFileObject, path);
        this.normalizedPath = FileSystemProvider.normalizeAbsolutePath(absPath, fileSystem);
        this.path = CndPathUtilities.normalizeSlashes(path);
    }

    public static Item createInFileSystem(FileSystem fileSystem, String path) {
        return new Item(fileSystem, path);
    }

    public static Item createDetachedViewItem(FileSystem fileSystem, String path) {
        CndUtils.assertNonUiThread();
        Item out = new Item(fileSystem, path);
        // This method is executed in not EDT and first call to getDataObject() is quite expensive operation.
        // If we call this method here then result will be calculated and cached. So cached version will be
        // used in createNodes and won't freeze EDT.
        // See Bug 221962 - [73cat] 3.s - Blocked by cnd.makeproject.ui.LogicalViewChildren.createNodes().
        DataObject dobj = out.getDataObject();
        // detach resources to prevent memory leaks
        out.detachFrom(dobj);
        CndUtils.assertTrueInConsole(out.lastDataObject == dobj, "data object should stay the same ", out.lastDataObject);
        return out;
    }

    // XXX:fullRemote deprecate and remove!
    private Item(FileSystem fileSystem, String path) {
        CndUtils.assertNotNull(path, "Path should not be null"); //NOI18N
        this.path = path;
        this.fileSystem = fileSystem; //CndFileUtils.getLocalFileSystem();
        this.normalizedPath = null;
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
        String oldPath;
        if (normalizedPath != null) {
            oldPath = normalizedPath;
        } else {
            oldPath = CndFileUtils.normalizeAbsolutePath(fileSystem, getAbsPath());
        }
        Item item = f.addItem(new Item(fileSystem, newPath));
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

    @Override
    public String getAbsolutePath() {
        return getNormalizedPath();
    }

    public String getSortName() {
        //return sortName;
        return getName();
    }

    @Override
    public String getName() {
        return CndPathUtilities.getBaseName(path);
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
        if (CndPathUtilities.isPathAbsolute(fileSystem, getPath())) {// UNIX path
            retPath = getPath();
        } else if (getFolder() != null) {
            retPath = getFolder().getConfigurationDescriptor().getBaseDir() + '/' + getPath(); // UNIX path
        }
        return retPath;
    }

    public void setFolder(Folder folder) {
        if (folder == null && file == null) {
            // store file in field. method getFile() will works after removing item
            ensureFileNotNull();
        }
        // leave folder if it is remove
        if (folder == null) { // Item is removed, let's clean up.
            synchronized (this) {
                detachFrom(lastDataObject);
                lastDataObject = null;
            }
        } else {
            this.folder = folder;
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("name")) { // NOI18N
            // File has been renamed.
            String newName = (String) evt.getNewValue();
            boolean nameWithoutExtension = true;
            Object o = evt.getSource();
            // New name is unpedictable. It can contain or not contain extension.
            // Detect true name with extension.
            if (o instanceof DataObject) {
                FileObject fo = ((DataObject) o).getPrimaryFile();
                if (fo != null) {
                    newName = fo.getNameExt();
                    nameWithoutExtension = false;
                }
            }
            rename(newName, nameWithoutExtension);
        } else if (evt.getPropertyName().equals("valid")) { // NOI18N
            if (!((Boolean) evt.getNewValue())) {
                // Data object invalid.
                // It may be renaming.
                Object o = evt.getSource();
                if (o instanceof DataObject) {
                    DataObject dao = ((DataObject) o);
                    FileObject fo = dao.getPrimaryFile();
                    if (fo != null && fo.isValid()) {
                        // Old data object invalid and valid file object.
                        // Rename and attach new data object.
                        rename(fo.getNameExt(), false);
                        DataObject dataObject;
                        try {
                            dataObject = DataObject.find(fo);
                            synchronized (this) {
                                if (dataObject != lastDataObject) {
                                    detachFrom(lastDataObject);
                                    attachTo(dataObject);
                                }
                            }
                        } catch (DataObjectNotFoundException ex) {
                            LOG.log(Level.FINE, "Can not find data object", ex); //NOI18N
                        }
                        return;
                    }
                }
                // File has been deleted.
                // Refresh folder. See also IZ 87557 and IZ 94935
                Folder containingFolder = getFolder();
                if (containingFolder != null) {
                    containingFolder.refresh(this);
                }
            } else {
                // Data object valid.
                // Attach new data object.
                Object o = evt.getSource();
                if (o instanceof DataObject) {
                    DataObject dao = ((DataObject) o);
                    synchronized (this) {
                        if (lastDataObject != null) {
                            detachFrom(lastDataObject);
                        }
                        lastDataObject = dao;
                        attachTo(lastDataObject);
                    }
                }
            }
        } else if (evt.getPropertyName().equals("primaryFile")) { // NOI18N
            // File has been moved.
            if (getFolder() != null) {
                FileObject fo = (FileObject) evt.getNewValue();
                String newPath = fo.getPath();
                if (!CndPathUtilities.isPathAbsolute(fileSystem, getPath())) {
                    newPath = CndPathUtilities.toRelativePath(getFolder().getConfigurationDescriptor().getBaseDirFileObject(), newPath);
                }
                newPath = CndPathUtilities.normalizeSlashes(newPath);
                renameTo(newPath);
            }
        }
    }

    public Folder getFolder() {
        return folder;
    }

    public FSPath getFSPath() {
        return new FSPath(fileSystem, getNormalizedPath());
    }

    public String getNormalizedPath() {
        synchronized (this) {
            if (normalizedPath != null) {
                return normalizedPath;
            }
        }
        String absPath = getAbsPath();
        return FileSystemProvider.normalizeAbsolutePath(absPath, fileSystem);
    }
    
    public String getCanonicalPath() {
        final FileObject canonicalFile = getCanonicalFile();
        if (canonicalFile != null) {
            return canonicalFile.getPath();
        }
        return getNormalizedPath();
    }

    private void ensureFileNotNull() {
        if (file == null) {
            try {
                file = CndFileUtils.getCanonicalFileObject(getFileObject());
            } catch (IOException ioe) {
                file = getFSPath().getFileObject();
            }
        }
        if (file == null) {
            LOG.log(Level.SEVERE, "Can not resolve file {0}", getAbsPath());
        }
    }

    public FileObject getCanonicalFile() {
        ensureFileNotNull();
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

    @Override
    public FileObject getFileObject() {
        FileObject fo = getFileObjectImpl();
        if (fo == null) {
            String p = (normalizedPath != null) ? normalizedPath : getAbsPath();
            return InvalidFileObjectSupport.getInvalidFileObject(fileSystem, p);
        }
        return fo;
    }

    /** 
     * Returns file object for this item.
     * If not found, returns a honest null, no dummies (InvalidFileObjectSupport.getInvalidFileObject)
     */
    private FileObject getFileObjectImpl() {
        PerformanceLogger.PerformaceAction performanceEvent = PerformanceLogger.getLogger().start(Folder.GET_ITEM_FILE_OBJECT_PERFORMANCE_EVENT, this);
        FileObject fileObject = null;
        try {
            performanceEvent.setTimeOut(Folder.FS_TIME_OUT);
            if (normalizedPath != null) {
                fileObject = fileSystem.findResource(normalizedPath);
            } else {
                Folder f = getFolder();
                if (f == null) {
                    // don't know file system, fall back to the default one
                    // but do not cache file object
                    String p = getPath();
                    if (CndPathUtilities.isPathAbsolute(fileSystem, p)) {// UNIX path
                        p = FileSystemProvider.normalizeAbsolutePath(p, fileSystem);                        
                        fileObject = fileSystem.findResource(p);
                    }
                } else {                    
                    MakeConfigurationDescriptor cfgDescr = f.getConfigurationDescriptor();
                    FileObject baseDirFO = cfgDescr.getBaseDirFileObject();
                    fileObject = RemoteFileUtil.getFileObject(baseDirFO, getPath());
                }
            }
        } finally {
            performanceEvent.log(fileObject);
        }
        return fileObject;
    }
    

    public DataObject getDataObject() {
        synchronized (this) {
            if (lastDataObject != null && lastDataObject.isValid()) {
                return lastDataObject;
            }
        }
        DataObject dataObject = null;
        FileObject fo = getFileObjectImpl();
        if (fo != null && fo.isValid()) {
            try {
                dataObject = DataObject.find(fo);
            } catch (DataObjectNotFoundException e) {
                // that's normal, for example, "myfile.xyz" won't have data object
                // ErrorManager.getDefault().notify(e);
                LOG.log(Level.FINE, "Can not find data object", e); //NOI18N
            }
        }
        synchronized (this) {
            if (dataObject != lastDataObject) {
                // DataObject can change without notification. We need to track this
                // and properly attach/detach listeners.
                detachFrom(lastDataObject);
                attachTo(dataObject);
            }
        }
        return dataObject;
    }
    
    private void attachTo(DataObject dataObject) {
        if (dataObject != null) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "attaching {0} to {1}", new Object[]{System.identityHashCode(this), dataObject});
            }
            dataObject.removePropertyChangeListener(this);
            dataObject.addPropertyChangeListener(this);
            NativeFileItemSet set = dataObject.getLookup().lookup(NativeFileItemSet.class);
            if (set != null) {
                set.add(this);
            }
        }
        lastDataObject = dataObject;
    }
    
    public final void onOpen() {
        synchronized (this) {
            // attach only if was initialized
            attachTo(lastDataObject);
        }
    }
    
    private void detachFrom(DataObject dao) {
        if (dao != null) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "detaching {0} from {1}", new Object[]{System.identityHashCode(this), dao});
            }
            dao.removePropertyChangeListener(this);
            NativeFileItemSet set = dao.getLookup().lookup(NativeFileItemSet.class);
            if (set != null) {
                set.remove(this);
            }
        }
    }
    
    final void onClose() {
        synchronized (this) {
            // detach but leave object reference for further possible reopen
            detachFrom(lastDataObject);
        }
    }
    
    public final String getMIMEType() {
        // use file object of this item
        return getMIMETypeImpl(this.getDataObject(), this);
    }
    
    private static String getMIMETypeImpl(DataObject dataObject, Item item) {
        FileObject fobj = dataObject == null ? null : dataObject.getPrimaryFile();
        if (fobj == null) {
            fobj = item.getFileObjectImpl();
        }
        String mimeType;
        if (fobj == null || ! fobj.isValid()) {
            mimeType = MIMESupport.getKnownSourceFileMIMETypeByExtension(item.getName());
        } else {
            mimeType = MIMESupport.getSourceFileMIMEType(fobj);
        }
        return mimeType;
    }

    /*package*/ static PredefinedToolKind getDefaultToolForItem(DataObject dataObject, Item item) {
        PredefinedToolKind tool;
        // use mime type of passed data object
        String mimeType = getMIMETypeImpl(dataObject, item);
        if (MIMENames.C_MIME_TYPE.equals(mimeType)) {
//            DataObject dataObject = getDataObject();
//            FileObject fo = dataObject == null ? null : dataObject.getPrimaryFile();
//            // Do not use C for .pc files
//            if (fo != null && "pc".equals(fo.getExt())) { //NOI18N
//                tool = PredefinedToolKind.CustomTool;
//            } else {
            tool = PredefinedToolKind.CCompiler;
//            }
        } else if (MIMENames.HEADER_MIME_TYPE.equals(mimeType)) {
            tool = PredefinedToolKind.CustomTool;
        } else if (MIMENames.CPLUSPLUS_MIME_TYPE.equals(mimeType)) {
            tool = PredefinedToolKind.CCCompiler;
        } else if (MIMENames.FORTRAN_MIME_TYPE.equals(mimeType)) {
            tool = PredefinedToolKind.FortranCompiler;
        } else if (MIMENames.ASM_MIME_TYPE.equals(mimeType)) {
            FileObject fobj = dataObject == null ? null : dataObject.getPrimaryFile();
            if (fobj == null) {
                fobj = item.getFileObjectImpl();
            }
            // Do not use assembler for .il files
            if (fobj != null && "il".equals(fobj.getExt())) { //NOI18N
                tool = PredefinedToolKind.CustomTool;
            } else {
                tool = PredefinedToolKind.Assembler;
            }
        } else {
            tool = PredefinedToolKind.CustomTool;
        }
        return tool;
    }
    
    public PredefinedToolKind getDefaultTool() {
        // use data object of this item
        return getDefaultToolForItem(this.getDataObject(), this);
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
    public List<FSPath> getSystemIncludePaths() {
        List<FSPath> vec = new ArrayList<>();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        ItemConfiguration itemConfiguration = getItemConfiguration(makeConfiguration);//ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) { // FIXUP: sometimes itemConfiguration is null (should not happen)
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
                FileSystem fs = FileSystemProvider.getFileSystem(compiler.getExecutionEnvironment());
                if (makeConfiguration.isMakefileConfiguration()) {
                    vec.addAll(CndFileUtils.toFSPathList(fs, compiler.getSystemIncludeDirectories(getImportantFlags())));
                } else {
                    String importantFlags = SPI_ACCESSOR.getImportantFlags(compilerConfiguration, compiler, makeConfiguration);
                    vec.addAll(CndFileUtils.toFSPathList(fs, compiler.getSystemIncludeDirectories(importantFlags)));
                }
            }
        }
        return SPI_ACCESSOR.expandIncludePaths(vec, compilerConfiguration, compiler, makeConfiguration);
    }

    @Override
    public List<FSPath> getUserIncludePaths() {
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        ItemConfiguration itemConfiguration = getItemConfiguration(makeConfiguration);//ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) { // FIXUP: sometimes itemConfiguration is null (should not happen)
            return Collections.<FSPath>emptyList();
        }
        CompilerSet compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
        if (compilerSet == null) {
            return Collections.<FSPath>emptyList();
        }
        AbstractCompiler compiler = (AbstractCompiler) compilerSet.getTool(itemConfiguration.getTool());
        BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
        if (compilerConfiguration instanceof CCCCompilerConfiguration) {
            // Get include paths from project/file
            CCCCompilerConfiguration cccCompilerConfiguration = (CCCCompilerConfiguration) compilerConfiguration;
            List<List<String>> list = new ArrayList<>();
            for(BasicCompilerConfiguration master : cccCompilerConfiguration.getMasters(true)) {
                list.add(((CCCCompilerConfiguration)master).getIncludeDirectories().getValue());
                if (!((CCCCompilerConfiguration)master).getInheritIncludes().getValue()) {
                    break;
                }
            }
            List<String> vec2 = new ArrayList<>();
            for(int i = list.size() - 1; i >= 0; i--) {
                vec2.addAll(list.get(i));
            }
            ExecutionEnvironment env = compiler.getExecutionEnvironment();            
            MacroConverter macroConverter = null;
            // Convert all paths to absolute paths
            FileSystem compilerFS = FileSystemProvider.getFileSystem(env);
            FileSystem projectFS = fileSystem;
            List<FSPath> result = new ArrayList<>();            
            for (String p : vec2) {
                if (p.contains("$")) { // NOI18N
                    // macro based path
                    if (macroConverter == null) {
                        macroConverter = new MacroConverter(env);
                    }
                    p = macroConverter.expand(p);
                }
                if (CndPathUtilities.isPathAbsolute(fileSystem, p)) {
                    result.add(new FSPath(compilerFS, p));
                } else {
                    String absPath = CndPathUtilities.toAbsolutePath(getFolder().getConfigurationDescriptor().getBaseDirFileObject(), p);
                    result.add(new FSPath(projectFS, absPath));
                }
            }
            List<FSPath> vec3 = new ArrayList<>();
            vec3 = SPI_ACCESSOR.getItemUserIncludePaths(vec3, cccCompilerConfiguration, compiler, makeConfiguration);
            result.addAll(vec3);
            return SPI_ACCESSOR.expandIncludePaths(result, cccCompilerConfiguration, compiler, makeConfiguration);
        }
        return Collections.<FSPath>emptyList();
    }

    @Override
    public List<String> getIncludeFiles() {
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        ItemConfiguration itemConfiguration = getItemConfiguration(makeConfiguration);//ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) { // FIXUP: sometimes itemConfiguration is null (should not happen)
            return Collections.<String>emptyList();
        }
        CompilerSet compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
        if (compilerSet == null) {
            return Collections.<String>emptyList();
        }
        AbstractCompiler compiler = (AbstractCompiler) compilerSet.getTool(itemConfiguration.getTool());
        BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
        if (compilerConfiguration instanceof CCCCompilerConfiguration) {
            // Get include paths from project/file
            CCCCompilerConfiguration cccCompilerConfiguration = (CCCCompilerConfiguration) compilerConfiguration;
            List<List<String>> list = new ArrayList<>();
            for(BasicCompilerConfiguration master : cccCompilerConfiguration.getMasters(true)) {
                list.add(((CCCCompilerConfiguration)master).getIncludeFiles().getValue());
                if (!((CCCCompilerConfiguration)master).getInheritFiles().getValue()) {
                    break;
                }
            }
            List<String> vec2 = new ArrayList<>();
            for(int i = list.size() - 1; i >= 0; i--) {
                vec2.addAll(list.get(i));
            }
            ExecutionEnvironment env = compiler.getExecutionEnvironment();            
            MacroConverter macroConverter = null;
            List<String> result = new ArrayList<>();            
            for (String p : vec2) {
                if (p.contains("$")) { // NOI18N
                    // macro based path
                    if (macroConverter == null) {
                        macroConverter = new MacroConverter(env);
                    }
                    p = macroConverter.expand(p);
                }
                String absPath = CndPathUtilities.toAbsolutePath(getFolder().getConfigurationDescriptor().getBaseDirFileObject(), p);
                result.add(absPath);
            }
            return result;
        }
        return Collections.<String>emptyList();
    }

    @Override
    public List<String> getSystemMacroDefinitions() {
        List<String> vec = new ArrayList<>();
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
                if (makeConfiguration.isMakefileConfiguration()) {
                     vec.addAll(compiler.getSystemPreprocessorSymbols(getImportantFlags()));
                } else {
                    String importantFlags = SPI_ACCESSOR.getImportantFlags(compilerConfiguration, compiler, makeConfiguration);
                    vec.addAll(compiler.getSystemPreprocessorSymbols(importantFlags));
                }
            }
        }
        List<String> undefinedMacros = getUndefinedMacros();
        if (undefinedMacros.size() > 0) {
            List<String> out = new ArrayList<>();
            for(String macro : vec) {
                boolean remove = true;
                for(String undef : undefinedMacros) {
                    if (macro.equals(undef) ||
                        macro.startsWith(undef+"=")) { //NOI18N
                        remove = false;
                        break;
                    }
                }
                if (remove) {
                    out.add(macro);
                }
            }
            vec = out;
        }
        return vec;
    }

    @Override
    public List<String> getUserMacroDefinitions() {
        List<String> vec = new ArrayList<>();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        ItemConfiguration itemConfiguration = getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) { // FIXUP: itemConfiguration should never be null
            return vec;
        }
        CompilerSet compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
        if (compilerSet == null) {
            return vec;
        }
        AbstractCompiler compiler = (AbstractCompiler) compilerSet.getTool(itemConfiguration.getTool());
        BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
        if (compilerConfiguration instanceof CCCCompilerConfiguration) {
            Map<String, String> res = new LinkedHashMap<>();
            CCCCompilerConfiguration cccCompilerConfiguration = (CCCCompilerConfiguration) compilerConfiguration;
            if (cccCompilerConfiguration.getInheritPreprocessor().getValue()) {
                for(BasicCompilerConfiguration master : cccCompilerConfiguration.getMasters(false)) {
                    addToMap(res, ((CCCCompilerConfiguration)master).getPreprocessorConfiguration().getValue(), false);
                    if (!((CCCCompilerConfiguration)master).getInheritPreprocessor().getValue()) {
                        break;
                    }

                }
            }
            addToMap(res, cccCompilerConfiguration.getPreprocessorConfiguration().getValue(), true);
            addToList(res, vec);
            vec = SPI_ACCESSOR.getItemUserMacros(vec, cccCompilerConfiguration, compiler, makeConfiguration);
        }
        return vec;
    }
    
    public List<String> getUndefinedMacros() {
        List<String> vec = new ArrayList<>();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        ItemConfiguration itemConfiguration = getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) { // FIXUP: itemConfiguration should never be null
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
            for(BasicCompilerConfiguration master : cccCompilerConfiguration.getMasters(true)) {
                vec.addAll(((CCCCompilerConfiguration)master).getUndefinedPreprocessorConfiguration().getValue());
                if (!((CCCCompilerConfiguration)master).getInheritUndefinedPreprocessor().getValue()) {
                    break;
                }
            }
        }
        return vec;
    }
    
    public String getImportantFlags() {
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        ItemConfiguration itemConfiguration = getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) { // FIXUP: itemConfiguration should never be null
            return "";
        }
        CompilerSet compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
        if (compilerSet == null) {
            return "";
        }
        if (makeConfiguration.isMakefileConfiguration()) {
            BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
            if (compilerConfiguration instanceof CCCCompilerConfiguration) {
                CCCCompilerConfiguration cccCompilerConfiguration = (CCCCompilerConfiguration) compilerConfiguration;
                return cccCompilerConfiguration.getImportantFlags().getValue();
            }
        } else {
            AbstractCompiler compiler = (AbstractCompiler) compilerSet.getTool(itemConfiguration.getTool());
            BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
            if (compilerConfiguration instanceof CCCCompilerConfiguration) {
                // Get include paths from compiler
                if (compiler != null && compiler.getPath() != null && compiler.getPath().length() > 0) {
                    final String importantFlags = SPI_ACCESSOR.getImportantFlags(compilerConfiguration, compiler, makeConfiguration);
                    if (importantFlags != null) {
                        return importantFlags;
                    }
                }
            }
        }
        
        return "";
    }
    
    private void addToMap(Map<String, String> res, List<String> list, boolean override) {
        for(String macro : list){
            int i = macro.indexOf('=');
            String key;
            String value;
            if ( i > 0){
                key = macro.substring(0,i).trim();
                value = macro.substring(i+1).trim();
            } else {
                key = macro;
                value = null;
            }
            if (!res.containsKey(key) || override) {
                res.put(key, value);
            }
        }
    }
    
    private void addToList(Map<String, String> res, List<String> list) {
        for(Map.Entry<String,String> e : res.entrySet()) {
            if (e.getValue() == null) {
                list.add(e.getKey());
            } else {
                list.add(e.getKey()+"="+e.getValue()); //NOI18N
            }
        }
    }

    public boolean hasHeaderOrSourceExtension(boolean cFiles, boolean ccFiles) {
        // Method return true for source files also.
        String mimeType = getMIMEType();
        return MIMENames.HEADER_MIME_TYPE.equals(mimeType)
                || (ccFiles && MIMENames.CPLUSPLUS_MIME_TYPE.equals(mimeType))
                || (cFiles && MIMENames.C_MIME_TYPE.equals(mimeType));
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
        LanguageFlavor flavor = LanguageFlavor.UNKNOWN;
        ItemConfiguration itemConfiguration = null;
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        if (makeConfiguration != null) {
            itemConfiguration = getItemConfiguration(makeConfiguration);
        }
        if (itemConfiguration != null && itemConfiguration.isCompilerToolConfiguration()) {
            flavor = itemConfiguration.getLanguageFlavor();
            if (flavor == LanguageFlavor.UNKNOWN || flavor == LanguageFlavor.DEFAULT) {
                CompilerSet compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
                if (compilerSet != null) {
                    Tool tool = compilerSet.getTool(itemConfiguration.getTool());
                    if (tool instanceof AbstractCompiler) {
                        AbstractCompiler compiler = (AbstractCompiler) tool;
                        if (itemConfiguration.isCompilerToolConfiguration()) {
                            BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
                            if (compilerConfiguration != null) {
                                LanguageFlavor aFlavor = SPI_ACCESSOR.getLanguageFlavor(compilerConfiguration, compiler, makeConfiguration);
                                if (aFlavor != LanguageFlavor.UNKNOWN) {
                                    flavor = aFlavor;
                                }
                            }
                        }
                    }
                }
            }
            if (flavor == LanguageFlavor.UNKNOWN) {
                if (itemConfiguration.getTool() == PredefinedToolKind.CCompiler) {
                    switch (itemConfiguration.getCCompilerConfiguration().getInheritedCStandard()) {
                        case CCompilerConfiguration.STANDARD_C99:
                            return LanguageFlavor.C99;
                        case CCompilerConfiguration.STANDARD_C11:
                            return LanguageFlavor.C11;
                        case CCompilerConfiguration.STANDARD_C89:
                        case CCompilerConfiguration.STANDARD_DEFAULT:
                            return LanguageFlavor.C89;
                    }
                } else if (itemConfiguration.getTool() == PredefinedToolKind.CCCompiler) {
                    switch (itemConfiguration.getCCCompilerConfiguration().getInheritedCppStandard()) {
                        case CCCompilerConfiguration.STANDARD_CPP11:
                            return LanguageFlavor.CPP11;
                        case CCCompilerConfiguration.STANDARD_CPP14:
                            return LanguageFlavor.CPP14;
                        case CCCompilerConfiguration.STANDARD_CPP98:
                        case CCCompilerConfiguration.STANDARD_DEFAULT:
                            return LanguageFlavor.CPP;
                    }
                }
            }            
        }
        
        if (flavor == LanguageFlavor.UNKNOWN) {
            if (makeConfiguration != null) {
                CCCompilerConfiguration ccCompilerConfiguration = makeConfiguration.getCCCompilerConfiguration();
                if(ccCompilerConfiguration != null) {
                    switch (ccCompilerConfiguration.getInheritedCppStandard()) {
                        case CCCompilerConfiguration.STANDARD_CPP11:
                            return LanguageFlavor.CPP11;
                        case CCCompilerConfiguration.STANDARD_CPP14:
                            return LanguageFlavor.CPP14;
                }
                }
            }
        }
        
        return flavor;
    }

    /**
     * NativeFileItem interface
     **/
    @Override
    public boolean isExcluded() {
        ItemConfiguration itemConfiguration = getItemConfiguration(getMakeConfiguration());
        if (itemConfiguration != null) {
            boolean value = itemConfiguration.getExcluded().getValue();
            if (value) {
                if (getMakeConfiguration().getCodeAssistanceConfiguration().includeInCA(this)) {
                    return false;
                }
            } else {
                if (getMakeConfiguration().getCodeAssistanceConfiguration().excludeInCA(this)) {
                    return true;
                }
            }
            return value;
        }
        return true;
    }

    /*package*/ int getCRC() {
        int res = 0;
        for(FSPath aPath : getUserIncludePaths()) {
            res += 37 * aPath.getPath().hashCode();
        }
        for(String aPath : getIncludeFiles()) {
            res += 37 * aPath.hashCode();
        }
        for(String macro: getUserMacroDefinitions()) {
            res += 37 * macro.hashCode();
        }
        for(String macro: getUndefinedMacros()) {
            res += 37 * macro.hashCode();
        }
        for(FSPath aPath : getSystemIncludePaths()) {
            res += 37 * aPath.getPath().hashCode();
        }
        for(String macro: getSystemMacroDefinitions()) {
            res += 37 * macro.hashCode();
        }
        res += 37 * getLanguage().hashCode();
        res += 37 * getLanguageFlavor().hashCode();
        return res;
    }
    
    @Override
    public String toString() {
        return path;
    }
    private static final SpiAccessor SPI_ACCESSOR = new SpiAccessor();

    public boolean hasImportantAttributes() {
        for (ItemConfiguration conf : getItemConfigurations()) {
            if (conf != null && !conf.isDefaultConfiguration() ) {
                return true;
            }
        }
        return false;
    }

    private static final class SpiAccessor {

        private Collection<? extends UserOptionsProvider> uoProviders;
        private Collection<? extends IncludePathExpansionProvider> ipeProviders;

        private synchronized Collection<? extends UserOptionsProvider> getUserOptionsProviders() {
            if (uoProviders == null) {
                uoProviders = Lookup.getDefault().lookupAll(UserOptionsProvider.class);
            }
            return uoProviders;
        }

        private synchronized Collection<? extends IncludePathExpansionProvider> getIncludePathExpansionProviders() {
            if (ipeProviders == null) {
                ipeProviders = Lookup.getDefault().lookupAll(IncludePathExpansionProvider.class);
            }
            return ipeProviders;
        }
        
        private SpiAccessor() {
        }

        private List<FSPath> getItemUserIncludePaths(List<FSPath> includes, AllOptionsProvider compilerOptions, AbstractCompiler compiler, MakeConfiguration makeConfiguration) {
            if(!getUserOptionsProviders().isEmpty()) {
                List<FSPath> res = new ArrayList<>(includes);
                for (UserOptionsProvider provider : getUserOptionsProviders()) {
                    res.addAll(provider.getItemUserIncludePaths(includes, compilerOptions, compiler, makeConfiguration));
                }
                return res;
            } else {
                return includes;
            }
        }

        private List<String> getItemUserMacros(List<String> macros, AllOptionsProvider compilerOptions, AbstractCompiler compiler, MakeConfiguration makeConfiguration) {
            if(!getUserOptionsProviders().isEmpty()) {
                List<String> res = new ArrayList<>(macros);
                for (UserOptionsProvider provider : getUserOptionsProviders()) {
                    res.addAll(provider.getItemUserMacros(macros, compilerOptions, compiler, makeConfiguration));
                }
                return res;
            } else {
                return macros;
            }
        }

        private String getImportantFlags(AllOptionsProvider compilerOptions, AbstractCompiler compiler, MakeConfiguration makeConfiguration) {
            if(!getUserOptionsProviders().isEmpty()) {
                for (UserOptionsProvider provider : getUserOptionsProviders()) {
                    String itemImportantFlags = provider.getItemImportantFlags(compilerOptions, compiler, makeConfiguration);
                    if (itemImportantFlags != null) {
                        return itemImportantFlags;
                    }
                }
            }
            return null;
        }
        
        private LanguageFlavor getLanguageFlavor(AllOptionsProvider compilerOptions, AbstractCompiler compiler, MakeConfiguration makeConfiguration) {
            if(!getUserOptionsProviders().isEmpty()) {
                for (UserOptionsProvider provider : getUserOptionsProviders()) {
                    LanguageFlavor languageFlavor = provider.getLanguageFlavor(compilerOptions, compiler, makeConfiguration);
                    if(languageFlavor != null && languageFlavor != LanguageFlavor.UNKNOWN) {
                        return languageFlavor;
                    }
                }
                return LanguageFlavor.UNKNOWN;
            } else {
                return LanguageFlavor.UNKNOWN;
            }
        }
        
        private List<FSPath> expandIncludePaths(List<FSPath> includes, AllOptionsProvider compilerOptions, AbstractCompiler compiler, MakeConfiguration makeConfiguration) {
            for (IncludePathExpansionProvider provider : getIncludePathExpansionProviders()) {
                includes = provider.expandIncludePaths(includes, compilerOptions, compiler, makeConfiguration);
            }
            return includes;
        }
    }
    
    private static final class MacroConverter {

        private final MacroExpander expander;
        private final Map<String, String> envVariables;

        public MacroConverter(ExecutionEnvironment env) {
            envVariables = new HashMap<>();
            if (HostInfoUtils.isHostInfoAvailable(env)) {
                try {
                    HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
                    envVariables.putAll(hostInfo.getEnvironment());                    
                } catch (IOException | CancellationException ex) {
                    // should never == null occur if isHostInfoAvailable(env) => report
                    Exceptions.printStackTrace(ex);
                }                    
            }
            BrokenReferencesSupport.addTemporaryEnv(env, envVariables);
            this.expander = (envVariables == null) ? null : MacroExpanderFactory.getExpander(env, false);
        }

        public String expand(String in) {
            try {
                return expander != null ? expander.expandMacros(in, envVariables) : in;
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
            return in;
        }
    }
}

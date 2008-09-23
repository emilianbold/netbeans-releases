/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
import org.netbeans.modules.cnd.loaders.CCDataLoader;
import org.netbeans.modules.cnd.loaders.CCDataObject;
import org.netbeans.modules.cnd.loaders.CDataObject;
import org.netbeans.modules.cnd.loaders.FortranDataObject;
import org.netbeans.modules.cnd.loaders.HDataObject;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.loaders.CDataLoader;
import org.netbeans.modules.cnd.loaders.FortranDataLoader;
import org.netbeans.modules.cnd.loaders.HDataLoader;
import org.netbeans.modules.cnd.makeproject.api.compilers.BasicCompiler;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.settings.CppSettings;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

public class Item implements NativeFileItem, PropertyChangeListener {
    private final String path;
    private final String sortName;
    private Folder folder;
    private File file = null;
    private String id = null;
    private DataObject lastDataObject = null;

    public Item(String path) {
        this.path = path;
        this.sortName = IpeUtils.getBaseName(path).toLowerCase();
//        int i = sortName.lastIndexOf("."); // NOI18N
//        if (i > 0) {
//            this.sortName = sortName.substring(0, i);
//        } else {
//            this.sortName = sortName;
//        }
        folder = null;
    }
    
    /**
     * Rename item.
     * @param newname new name without suffic or path
     */
    public void rename(String newname) {
        if (newname == null || newname.length() == 0 || getFolder() == null)
            return;
        
        // Rename name in path
        int indexName = path.lastIndexOf('/');
        if (indexName < 0)
            indexName = 0;
        else
            indexName++;
        int indexDot = path.lastIndexOf('.');
        if (indexDot < indexName)
            indexDot = -1;
        
        String oldname;
        if (indexDot >= 0)
            oldname = path.substring(indexName, indexDot);
        else
            oldname = path.substring(indexName);
        if (oldname.equals(newname))
            return;
        
        String newPath = ""; // NOI18N
        if (indexName > 0)
            newPath = path.substring(0, indexName);
        newPath += newname;
        if (indexDot >= 0)
            newPath += path.substring(indexDot);
        // Remove old item and insert new with new name
        moveTo(newPath);
    }
    
    public void moveTo(String newPath) {
        Folder f = getFolder();
        String oldPath = getAbsPath();
        Item item = new Item(newPath);
        f.addItem(item);
        copyItemConfigurations(this, item);
        f.removeItem(this);
        f.renameItemAction(oldPath,  item);
    }
    
    public String getPath() {
        return path;
    }
    
    public String getSortName() {
        return sortName;
    }
    
    public String getPath(boolean norm) {
        String pat = "./"; // UNIX path  // NOI18N
        if (norm && getPath().startsWith(pat))
            return getPath().substring(2);
        else
            return getPath();
    }
    
    public String getAbsPath() {
        String retPath = null;
        if (IpeUtils.isPathAbsolute(getPath()))  {// UNIX path
            retPath = getPath();
            retPath = FilePathAdaptor.mapToLocal(retPath);
        } else if (getFolder() != null) {
            retPath = getFolder().getConfigurationDescriptor().getBaseDir() + '/' + getPath(); // UNIX path
        }
        return retPath;
    }
    
    public void setFolder(Folder folder) {
        this.folder = folder;
        if (folder == null) { // Item is removed, let's clean up.
            synchronized (this) {
                if (lastDataObject != null) {
                    lastDataObject.removePropertyChangeListener(this);
                    lastDataObject = null;
                }
            }
        }
    }
    
    public DataObject getLastDataObject(){
        return lastDataObject;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("name")) { // NOI18N
            // File has been renamed
            rename((String)evt.getNewValue());
        } else if (evt.getPropertyName().equals("valid")) { // NOI18N
            // File has been deleted
            // Do nothing (IZ 87557, 94935)
            if (!((Boolean)evt.getNewValue()).booleanValue()) {
//                getFolder().removeItemAction(this);
                getFolder().refresh(this);
            }
        } else if (evt.getPropertyName().equals("primaryFile")) { // NOI18N
            // File has been moved
            FileObject fo = (FileObject)evt.getNewValue();
            String newPath = FileUtil.toFile(fo).getPath();
            if (!IpeUtils.isPathAbsolute(getPath())) {
                newPath = IpeUtils.toRelativePath(getFolder().getConfigurationDescriptor().getBaseDir(), newPath);
            }
            newPath = FilePathAdaptor.normalize(newPath);
            moveTo(newPath);
        }
    }
    
    public Folder getFolder() {
        return folder;
    }
    
    public File getFile() {
        String aPath = getAbsPath();
        if (aPath != null) {
            return FileUtil.normalizeFile(new File(aPath));
        }
        return null;
    }
    
    public File getCanonicalFile() {
        if (file == null) {
            try {
                file = new File(getAbsPath()).getCanonicalFile();
            } catch (IOException ioe) {
                file = new File(getAbsPath());
            }
        }
        return file;
    }
    
    public String getId() {
        if (id == null) {
            id = "i-" + getPath(); // NOI18N
        }
        return id;
    }
    
    public ItemConfiguration getItemConfiguration(Configuration configuration) {
        if (configuration != null) {
            return (ItemConfiguration)configuration.getAuxObject(getId());
        }
        return null;
    }
    
    public ItemConfiguration[] getItemConfigurations() {
        ItemConfiguration[] itemConfigurations;
        MakeConfigurationDescriptor makeConfigurationDescriptor = getMakeConfigurationDescriptor();
        if (makeConfigurationDescriptor == null)
            return new ItemConfiguration[0];
        Configuration[] configurations = makeConfigurationDescriptor.getConfs().getConfs();
        itemConfigurations = new ItemConfiguration[configurations.length];
        for (int i = 0; i < configurations.length; i++) {
            itemConfigurations[i] = getItemConfiguration(configurations[i]);
        }
        return itemConfigurations;
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
            for (Configuration conf : makeConfigurationDescriptor.getConfs().getConfs()) {
                ItemConfiguration newConf = new ItemConfiguration(conf, dst);
                newConf.assignValues(src.getItemConfiguration(conf));
                conf.addAuxObject(newConf);
            }
        }
    }

    public FileObject getFileObject() {
        File file = getCanonicalFile();
        FileObject fo = null;
        try {
            fo = FileUtil.toFileObject(file.getCanonicalFile());
        } catch (IOException e) {
        }
        return fo;
    }
    
    public DataObject getDataObject() {
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
        if (dataObject != lastDataObject) {
            // DataObject can change without notification. We need to track this
            // and properly attach/detach listeners.
            synchronized (this) {
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
    
    public int getDefaultTool() {
        DataObject dataObject = getDataObject();
        int tool;
        
        if (dataObject == null) {
            if (CCDataLoader.getInstance().getExtensions().isRegistered(path)) {
                tool = Tool.CCCompiler;
            } else if (CDataLoader.getInstance().getExtensions().isRegistered(path)) {
                tool = Tool.CCompiler;
            } else if (FortranDataLoader.getInstance().getExtensions().isRegistered(path)) {
                tool = Tool.FortranCompiler;
            } else {
                tool = Tool.CustomTool;
            }
        } else if (dataObject instanceof CDataObject)
            tool = Tool.CCompiler;
        else if (dataObject instanceof HDataObject)
            tool = Tool.CustomTool;
        else if (dataObject instanceof CCDataObject)
            tool = Tool.CCCompiler;
        else if (CppSettings.getDefault().isFortranEnabled() && dataObject instanceof FortranDataObject)
            tool = Tool.FortranCompiler;
        else
            tool = Tool.CustomTool;
        return tool;
    }
    
    private MakeConfigurationDescriptor getMakeConfigurationDescriptor() {
        if (getFolder() == null)
            return null;
        return (MakeConfigurationDescriptor)getFolder().getConfigurationDescriptor();
    }
    
    private MakeConfiguration getMakeConfiguration() {
        MakeConfigurationDescriptor makeConfigurationDescriptor = getMakeConfigurationDescriptor();
        if (makeConfigurationDescriptor == null)
            return null;
        Configurations confs = makeConfigurationDescriptor.getConfs();
        if( confs == null )
            return null;
        return (MakeConfiguration)confs.getActive();
    }
    
    public NativeProject getNativeProject() {
        Folder folder = getFolder();
        if (folder != null) {
            Project project = folder.getProject();
            return (NativeProject)project.getLookup().lookup(NativeProject.class);
        }
        return null;
    }
    
    public List getSystemIncludePaths() {
        List vec = new ArrayList();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        ItemConfiguration itemConfiguration = getItemConfiguration(makeConfiguration);//ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) // FIXUP: sometimes itemConfiguration is null (should not happen)
            return vec;
        CompilerSet compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
        if (compilerSet == null)
            return vec;
        BasicCompiler compiler = (BasicCompiler)compilerSet.getTool(itemConfiguration.getTool());
        BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
        if (compilerConfiguration instanceof CCCCompilerConfiguration) {
            // Get include paths from compiler
            if( compiler != null  && compiler.getPath() != null && compiler.getPath().length() > 0) {
                vec.addAll(compiler.getSystemIncludeDirectories());
            }
        }
        return vec;
    }
    
    public List getUserIncludePaths() {
        List vec = new ArrayList();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        ItemConfiguration itemConfiguration = getItemConfiguration(makeConfiguration);//ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) // FIXUP: sometimes itemConfiguration is null (should not happen)
            return vec;
        CompilerSet compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
        if (compilerSet == null)
            return vec;
        BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
        if (compilerConfiguration instanceof CCCCompilerConfiguration) {
            // Get include paths from project/file
            List vec2 = new ArrayList();
            CCCCompilerConfiguration cccCompilerConfiguration = (CCCCompilerConfiguration)compilerConfiguration;
            CCCCompilerConfiguration master = (CCCCompilerConfiguration)cccCompilerConfiguration.getMaster();
            while (master != null && cccCompilerConfiguration.getInheritIncludes().getValue()) {
                vec2.addAll(master.getIncludeDirectories().getValue());
                if (master.getInheritIncludes().getValue())
                    master = (CCCCompilerConfiguration)master.getMaster();
                else
                    master = null;
            }
            vec2.addAll(cccCompilerConfiguration.getIncludeDirectories().getValue());
            // Convert all paths to absolute paths
            Iterator iter = vec2.iterator();
            while (iter.hasNext()) {
                vec.add(IpeUtils.toAbsolutePath(getFolder().getConfigurationDescriptor().getBaseDir(), (String)iter.next()));
            }
        }
        return vec;
    }
    
    public List getSystemMacroDefinitions() {
        List vec = new ArrayList();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        ItemConfiguration itemConfiguration = getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) // FIXUP: itemConfiguration should never be null
            return vec;
        CompilerSet compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
        if (compilerSet == null)
            return vec;
        BasicCompiler compiler = (BasicCompiler)compilerSet.getTool(itemConfiguration.getTool());
        BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
        if (compilerConfiguration instanceof CCCCompilerConfiguration) {
            if( compiler != null && compiler.getPath() != null && compiler.getPath().length() > 0) {
                // Get macro definitions from compiler
                vec.addAll(compiler.getSystemPreprocessorSymbols());
            }
        }
        return vec;
    }
    
    public List getUserMacroDefinitions() {
        List vec = new ArrayList();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        ItemConfiguration itemConfiguration = getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) // FIXUP: itemConfiguration should never be null
            return vec;
        CompilerSet compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
        if (compilerSet == null)
            return vec;
        BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
        if (compilerConfiguration instanceof CCCCompilerConfiguration) {
            CCCCompilerConfiguration cccCompilerConfiguration = (CCCCompilerConfiguration)compilerConfiguration;
            CCCCompilerConfiguration master = (CCCCompilerConfiguration)cccCompilerConfiguration.getMaster();
            while (master != null && cccCompilerConfiguration.getInheritPreprocessor().getValue()) {
                vec.addAll(master.getPreprocessorConfiguration().getValue());
                if (master.getInheritIncludes().getValue())
                    master = (CCCCompilerConfiguration)master.getMaster();
                else
                    master = null;
            }
            vec.addAll(cccCompilerConfiguration.getPreprocessorConfiguration().getValue());
        }
        return vec;
    }
    
    public boolean hasHeaderOrSourceExtension(boolean cFiles, boolean ccFiles) {
        // Method return true for source files also.
        String itemPath = getPath();
        return HDataLoader.getInstance().getExtensions().isRegistered(itemPath) ||
               ccFiles && CCDataLoader.getInstance().getExtensions().isRegistered(itemPath) ||
               cFiles && CDataLoader.getInstance().getExtensions().isRegistered(itemPath);
    }
    
    /**
     * NativeFileItem interface
     **/
    public Language getLanguage() {
        int tool;
        Language language;
        ItemConfiguration itemConfiguration = null;
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        if (makeConfiguration != null)
            itemConfiguration = getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(getPath()));
        
        if (itemConfiguration != null)
            tool = itemConfiguration.getTool();
        else
            tool = getDefaultTool();
        
        if (tool == Tool.CCompiler)
            language = NativeFileItem.Language.C;
        else if (tool == Tool.CCCompiler)
            language = NativeFileItem.Language.CPP;
        else if (tool == Tool.FortranCompiler)
            language = NativeFileItem.Language.FORTRAN;
        else if (hasHeaderOrSourceExtension(true,true))
            language = NativeFileItem.Language.C_HEADER;
        else
            language = NativeFileItem.Language.OTHER;
        
        return language;
    }
    
    /**
     * NativeFileItem interface
     **/
    public LanguageFlavor getLanguageFlavor() {
        return NativeFileItem.LanguageFlavor.GENERIC;
    }
    
    /**
     * NativeFileItem interface
     **/
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

}

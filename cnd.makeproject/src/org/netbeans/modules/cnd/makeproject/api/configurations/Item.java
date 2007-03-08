/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
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
import org.netbeans.modules.cnd.makeproject.MakeOptions;
import org.netbeans.modules.cnd.makeproject.api.compilers.BasicCompiler;
import org.netbeans.modules.cnd.makeproject.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.makeproject.api.compilers.CompilerSets;
import org.netbeans.modules.cnd.makeproject.api.compilers.Tool;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platform;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platforms;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.ExtensionList;

public class Item implements NativeFileItem, PropertyChangeListener {
    private String path;
    private String sortName;
    private Folder folder;
    private File file = null;
    
    public Item(String path) {
        this.path = path;
        sortName = IpeUtils.getBaseName(path).toLowerCase();
        int i = sortName.lastIndexOf("."); // NOI18N
        if (i > 0)
            sortName = sortName.substring(0, i);
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
        // FIXUP: update all configurations with settings from old item....
        f.removeItem(this);
        Item item = f.addItem(new Item(newPath));
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
        if (folder != null)
            addPropertyChangeListener();
    }
    
    private DataObject dataObject = null;
    public void addPropertyChangeListener() {
        dataObject = getDataObject();
        if (dataObject != null) {
            dataObject.addPropertyChangeListener(this);
        }
    }
    
    public void removePropertyChangeListener() {
        //DataObject dataObject = getDataObject();
        if (dataObject != null) {
            dataObject.removePropertyChangeListener(this);
            dataObject = null;
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("name")) { // NOI18N
            // File has been renamed
            rename((String)evt.getNewValue());
        }
        else if (evt.getPropertyName().equals("valid")) { // NOI18N
            // File has been deleted
            if (!((Boolean)evt.getNewValue()).booleanValue()) {
              getFolder().removeItem(this);
    }
        }
        else if (evt.getPropertyName().equals("primaryFile")) { // NOI18N
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
        return FileUtil.normalizeFile(new File(getAbsPath()));
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
        else if (MakeOptions.getInstance().getFortran() && dataObject instanceof FortranDataObject)
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
        return (MakeConfiguration)makeConfigurationDescriptor.getConfs().getActive();
    }
    
    public NativeProject getNativeProject() {
        Project project = getFolder().getProject();
        return (NativeProject)project.getLookup().lookup(NativeProject.class);
    }
    
    public List getSystemIncludePaths() {
        Vector vec = new Vector();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        ItemConfiguration itemConfiguration = (ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) // FIXUP: sometimes itemConfiguration is null (should not happen)
            return vec;
        Platform platform = Platforms.getPlatform(makeConfiguration.getPlatform().getValue());
        CompilerSet compilerSet = CompilerSets.getCompilerSet(makeConfiguration.getCompilerSet().getValue());
        BasicCompiler compiler = (BasicCompiler)compilerSet.getTool(itemConfiguration.getTool());
        BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
        if (compilerConfiguration instanceof CCCCompilerConfiguration) {
            // Get include paths from compiler
            vec.addAll(compiler.getSystemIncludeDirectories(platform));
        }
        return vec;
    }
    
    public List getUserIncludePaths() {
        Vector vec = new Vector();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        ItemConfiguration itemConfiguration = (ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) // FIXUP: sometimes itemConfiguration is null (should not happen)
            return vec;
        Platform platform = Platforms.getPlatform(makeConfiguration.getPlatform().getValue());
        CompilerSet compilerSet = CompilerSets.getCompilerSet(makeConfiguration.getCompilerSet().getValue());
        BasicCompiler compiler = (BasicCompiler)compilerSet.getTool(itemConfiguration.getTool());
        BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
        if (compilerConfiguration instanceof CCCCompilerConfiguration) {
            // Get include paths from project/file
            Vector vec2 = new Vector();
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
        Vector vec = new Vector();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        ItemConfiguration itemConfiguration = (ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) // FIXUP: itemConfiguration should never be null
            return vec;
        Platform platform = Platforms.getPlatform(makeConfiguration.getPlatform().getValue());
        CompilerSet compilerSet = CompilerSets.getCompilerSet(makeConfiguration.getCompilerSet().getValue());
        BasicCompiler compiler = (BasicCompiler)compilerSet.getTool(itemConfiguration.getTool());
        BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
        if (compilerConfiguration instanceof CCCCompilerConfiguration) {
            // Get macro definitions from compiler
            vec.addAll(compiler.getSystemPreprocessorSymbols(platform));
        }
        return vec;
    }
    
    public List getUserMacroDefinitions() {
        Vector vec = new Vector();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        ItemConfiguration itemConfiguration = (ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) // FIXUP: itemConfiguration should never be null
            return vec;
        Platform platform = Platforms.getPlatform(makeConfiguration.getPlatform().getValue());
        CompilerSet compilerSet = CompilerSets.getCompilerSet(makeConfiguration.getCompilerSet().getValue());
        BasicCompiler compiler = (BasicCompiler)compilerSet.getTool(itemConfiguration.getTool());
        BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
        if (compilerConfiguration instanceof CCCCompilerConfiguration) {
            // get macro definitions from project/file
            CCCCompilerConfiguration cccCompilerConfiguration = (CCCCompilerConfiguration)compilerConfiguration;
            CCCCompilerConfiguration master = (CCCCompilerConfiguration)cccCompilerConfiguration.getMaster();
            while (master != null && cccCompilerConfiguration.getInheritPreprocessor().getValue()) {
                vec.addAll(master.getPreprocessorConfiguration().getValuesAsVector());
                if (master.getInheritPreprocessor().getValue())
                    master = (CCCCompilerConfiguration)master.getMaster();
                else
                    master = null;
            }
            vec.addAll(cccCompilerConfiguration.getPreprocessorConfiguration().getValuesAsVector());
        }
        return vec;
    }

    private boolean isHeaderFile() {
        ExtensionList hlist = HDataLoader.getInstance().getExtensions();
        if (hlist.isRegistered(getPath())) {
            return true;
        } else {
            return false;
        }
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
            itemConfiguration = (ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(getPath()));
            
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
        else if (isHeaderFile())
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
}

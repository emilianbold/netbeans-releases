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

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.text.DateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import org.netbeans.modules.cnd.api.project.NativeFileItem.LanguageFlavor;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.ToolKind;
import org.netbeans.modules.cnd.api.xml.XMLDecoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoder;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.BooleanNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ItemXMLCodec;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

public class ItemConfiguration implements ConfigurationAuxObject {
    
    // enabled by default for now, see #217779
    private static final boolean SHOW_HEADER_EXCLUDE = CndUtils.getBoolean("cnd.makeproject.showHeaderExclude", true); // NOI18N

    private boolean needSave = false;
    private Configuration configuration;
    private Item item;
    // General
    private BooleanConfiguration excluded;
    private PredefinedToolKind tool = PredefinedToolKind.UnknownTool;
    private boolean toolDirty = false;
    private LanguageFlavor languageFlavor = LanguageFlavor.UNKNOWN;
    // Tools
    private ConfigurationBase lastConfiguration;
    private CustomToolConfiguration customToolConfiguration;

    // cached id of item
//    private String id;
    public ItemConfiguration(Configuration configuration, Item item) {
        // General
        this.configuration = configuration;
        setItem(item);
        // we want non-default (bold) title to be only for excluded items => use false
        this.excluded = new BooleanConfiguration(false);

        // This is side effect of lazy configuration. We should init folder configuration
        // TODO: remove folder initialization. Folder should be responsible for it
        item.getFolder().getFolderConfiguration(configuration);

        clearChanged();
    }

    public ItemConfiguration(ItemConfiguration itemConfiguration) {
        this.configuration = itemConfiguration.configuration;
        this.item = itemConfiguration.item;
        this.excluded = itemConfiguration.excluded;
        this.needSave = itemConfiguration.needSave;
        this.tool = itemConfiguration.tool;
        this.languageFlavor = itemConfiguration.languageFlavor;
        this.lastConfiguration = itemConfiguration.lastConfiguration;
        this.customToolConfiguration = itemConfiguration.customToolConfiguration;
    }

    public boolean isDefaultConfiguration() {
        // was included => not default state to allow serialization
        if (!excluded.getValue()) {
            return false;
        }
        if (lastConfiguration != null && lastConfiguration.getModified()) {
            return false;
        }
        if (customToolConfiguration != null && customToolConfiguration.getModified()) {
            return false;
        }
        if (getLanguageFlavor() != null && getLanguageFlavor() != LanguageFlavor.UNKNOWN) {
            return false;
        }
        // we do not check tools for excluded items in unmanaged projects
        // but check for all logical as before
        if (!isItemFromDiskFolder()) {
            if (getTool() != item.getDefaultTool()) {
                return false;
            }
        }
        return true;
    }

    public boolean isCompilerToolConfiguration() {
        switch (getTool()) {
            case Assembler:
            case CCCompiler:
            case CCompiler:
            case FortranCompiler:
                return true;
        }
        return false;
    }

    public BasicCompilerConfiguration getCompilerConfiguration() {
        switch (getTool()) {
            case Assembler:
                return getAssemblerConfiguration();
            case CCCompiler:
                return getCCCompilerConfiguration();
            case CCompiler:
                return getCCompilerConfiguration();
            case FortranCompiler:
                return getFortranCompilerConfiguration();
        }
        return null;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public Item getItem() {
        return item;
    }

    private void setItem(Item item) {
        if (this.item != item) {
            this.item = item;
            this.needSave = true;
//            this.id = null;
        //this.tool = item.getDefaultTool();
        }
    }

    // General
    public BooleanConfiguration getExcluded() {
        return excluded;
    }

    public void setExcluded(BooleanConfiguration excluded) {
        this.excluded = excluded;
        needSave = true;
    }
    
    public boolean isToolDirty() {
        return toolDirty;
    }
    
    public void setToolDirty(boolean dirty) {
        toolDirty = dirty;
    }
    
    public void setTool(PredefinedToolKind tool) {
        if (this.tool != tool){
            lastConfiguration = null;
            toolDirty = true;
        }
        this.tool = tool;
   }

    public PredefinedToolKind getTool() {
        if (tool == PredefinedToolKind.UnknownTool) {
            tool = item.getDefaultTool();
        }
        return tool;
    }

    public void setLanguageFlavor(LanguageFlavor flavor) {
        this.languageFlavor = flavor;
        {
            CCompilerConfiguration conf = getCCompilerConfiguration();
            if (conf != null) {
                conf.setCStandardExternal(flavor.toExternal());
                return;
            }
        }
        {
            CCCompilerConfiguration conf = getCCCompilerConfiguration();
            if (conf != null) {
                conf.setCppStandardExternal(flavor.toExternal());
            }
        }
    }
    
    private void updateLanguageFlavor() {
        {
            CCompilerConfiguration conf = getCCompilerConfiguration();
            if (conf != null) {
                languageFlavor = LanguageFlavor.fromExternal(conf.getCStandardExternal());
                return;
            }
        }
        {
            CCCompilerConfiguration conf = getCCCompilerConfiguration();
            if (conf != null) {
                languageFlavor = LanguageFlavor.fromExternal(conf.getCppStandardExternal());
            }
        }
   }

    public LanguageFlavor getLanguageFlavor() {
        return languageFlavor;
    }

    protected String[] getToolNames() {
        return new String[]{PredefinedToolKind.CCompiler.getDisplayName(), PredefinedToolKind.CCCompiler.getDisplayName(),
                            PredefinedToolKind.FortranCompiler.getDisplayName(), PredefinedToolKind.Assembler.getDisplayName(),
                            PredefinedToolKind.CustomTool.getDisplayName()};
    }

    // Custom Tool
    public void setCustomToolConfiguration(CustomToolConfiguration customToolConfiguration) {
        this.customToolConfiguration = customToolConfiguration;
    }

    public synchronized CustomToolConfiguration getCustomToolConfiguration() {
        if (getTool() == PredefinedToolKind.CustomTool || isProCFile()) {
            if (customToolConfiguration == null) {
                customToolConfiguration = new CustomToolConfiguration();
            }
            return customToolConfiguration;
        }
        return null;
    }

    // C Compiler
    public void setCCompilerConfiguration(CCompilerConfiguration cCompilerConfiguration) {
        this.lastConfiguration = cCompilerConfiguration;
    }

    public synchronized CCompilerConfiguration getCCompilerConfiguration() {
        if (getTool() == PredefinedToolKind.CCompiler) {
            if (lastConfiguration == null) {
                FolderConfiguration folderConfiguration = item.getFolder().getFolderConfiguration(configuration);
                if (folderConfiguration != null) {
                    lastConfiguration = new CCompilerConfiguration(((MakeConfiguration) configuration).getBaseDir(), folderConfiguration.getCCompilerConfiguration(), (MakeConfiguration) configuration);
                } else {
                    lastConfiguration = new CCompilerConfiguration(((MakeConfiguration) configuration).getBaseDir(), null, (MakeConfiguration) configuration);
                }
            }
            assert lastConfiguration instanceof CCompilerConfiguration;
            return  (CCompilerConfiguration) lastConfiguration;
        }
        return null;
    }

    // CC Compiler
    public void setCCCompilerConfiguration(CCCompilerConfiguration ccCompilerConfiguration) {
        this.lastConfiguration = ccCompilerConfiguration;
    }

    public synchronized CCCompilerConfiguration getCCCompilerConfiguration() {
        if (getTool() == PredefinedToolKind.CCCompiler) {
            if (lastConfiguration == null) {
                FolderConfiguration folderConfiguration = item.getFolder().getFolderConfiguration(configuration);
                if (folderConfiguration != null) {
                    lastConfiguration = new CCCompilerConfiguration(((MakeConfiguration) configuration).getBaseDir(), folderConfiguration.getCCCompilerConfiguration(), (MakeConfiguration) configuration);
                } else {
                    lastConfiguration = new CCCompilerConfiguration(((MakeConfiguration) configuration).getBaseDir(), null, (MakeConfiguration) configuration);
                }
            }
            assert lastConfiguration instanceof CCCompilerConfiguration;
            return  (CCCompilerConfiguration) lastConfiguration;
        }
        return null;
    }

    // Fortran Compiler
    public void setFortranCompilerConfiguration(FortranCompilerConfiguration fortranCompilerConfiguration) {
        this.lastConfiguration = fortranCompilerConfiguration;
    }

    public synchronized FortranCompilerConfiguration getFortranCompilerConfiguration() {
        if (getTool() == PredefinedToolKind.FortranCompiler) {
            if (lastConfiguration == null) {
                lastConfiguration = new FortranCompilerConfiguration(((MakeConfiguration) configuration).getBaseDir(), ((MakeConfiguration) configuration).getFortranCompilerConfiguration());
            }
            assert lastConfiguration instanceof FortranCompilerConfiguration;
            return  (FortranCompilerConfiguration) lastConfiguration;
        }
        return null;
    }

    // Assembler
    public void setAssemblerConfiguration(AssemblerConfiguration assemblerConfiguration) {
        this.lastConfiguration = assemblerConfiguration;
    }

    public synchronized AssemblerConfiguration getAssemblerConfiguration() {
        if (getTool() == PredefinedToolKind.Assembler) {
            if (lastConfiguration == null) {
                lastConfiguration = new AssemblerConfiguration(((MakeConfiguration) configuration).getBaseDir(), ((MakeConfiguration) configuration).getAssemblerConfiguration());
            }
            assert lastConfiguration instanceof AssemblerConfiguration;
            return  (AssemblerConfiguration) lastConfiguration;
        }
        return null;
    }

    // interface ConfigurationAuxObject
    @Override
    public boolean shared() {
        return true;
    }
    
    public boolean isVCSVisible() {
        if (item != null && getExcluded() != null && isItemFromDiskFolder()) {
            return !getExcluded().getValue();
        }
        return shared();
    }
    
    // interface ConfigurationAuxObject
    @Override
    public boolean hasChanged() {
        return needSave;
    }

    // interface ProfileAuxObject
    @Override
    public final void clearChanged() {
        needSave = false;
    }

    /**
     * Returns an unique id (String) used to retrive this object from the
     * pool of aux objects
     */
//    public String getId() {
//        if (this.id == null) {
//            this.id = getId(getItem().getPath());
//        }
//        assert this.id != null;
//        return this.id;
//    }
//    
//    static public String getId(String path) {
//        return "item-" + path; // NOI18N
//    }
    @Override
    public String getId() {
        return item.getId();
    }

    @Override
    public void assign(ConfigurationAuxObject profileAuxObject) {
        if (!(profileAuxObject instanceof ItemConfiguration)) {
            // FIXUP: exception ????
            System.err.println("Item - assign: Profile object type expected - got " + profileAuxObject); // NOI18N
            return;
        }
        ItemConfiguration i = (ItemConfiguration) profileAuxObject;
        if (!getId().equals(i.getItem().getId())) {
            System.err.println("Item - assign: Item ID " + getId() + " expected - got " + i.getItem().getId()); // NOI18N
            return;
        }
        setConfiguration(i.getConfiguration());
        setItem(i.getItem());
        getExcluded().assign(i.getExcluded());
        setTool(i.getTool());
        switch (getTool()) {
            case Assembler:
                getAssemblerConfiguration().assign(i.getAssemblerConfiguration());
                break;
            case CCCompiler:
                getCCCompilerConfiguration().assign(i.getCCCompilerConfiguration());
                if(isProCFile()) {
                    getCustomToolConfiguration().assign(i.getCustomToolConfiguration());
                }
                break;
            case CCompiler:
                getCCompilerConfiguration().assign(i.getCCompilerConfiguration());
                if(isProCFile()) {
                    getCustomToolConfiguration().assign(i.getCustomToolConfiguration());
                }
                break;
            case CustomTool:
                getCustomToolConfiguration().assign(i.getCustomToolConfiguration());
                break;
            case FortranCompiler:
                getFortranCompilerConfiguration().assign(i.getFortranCompilerConfiguration());
                break;
            default:
                assert false;
        }
        updateLanguageFlavor();
    }

    public void assignValues(ConfigurationAuxObject profileAuxObject) {
        if (!(profileAuxObject instanceof ItemConfiguration)) {
            // FIXUP: exception ????
            System.err.println("Item - assign: Profile object type expected - got " + profileAuxObject); // NOI18N
            return;
        }
        ItemConfiguration i = (ItemConfiguration) profileAuxObject;
        getExcluded().assign(i.getExcluded());
        setTool(i.getTool());
        setLanguageFlavor(i.getLanguageFlavor());
        switch (getTool()) {
            case Assembler:
                getAssemblerConfiguration().assign(i.getAssemblerConfiguration());
                break;
            case CCCompiler:
                getCCCompilerConfiguration().assign(i.getCCCompilerConfiguration());
                if(isProCFile()) {
                    getCustomToolConfiguration().assign(i.getCustomToolConfiguration());
                }
                break;
            case CCompiler:
                getCCompilerConfiguration().assign(i.getCCompilerConfiguration());
                if(isProCFile()) {
                    getCustomToolConfiguration().assign(i.getCustomToolConfiguration());
                }
                break;
            case CustomTool:
                getCustomToolConfiguration().assign(i.getCustomToolConfiguration());
                break;
            case FortranCompiler:
                getFortranCompilerConfiguration().assign(i.getFortranCompilerConfiguration());
                break;
            default:
                assert false;
        }
    }

    public ItemConfiguration copy(MakeConfiguration makeConfiguration) {
        ItemConfiguration copy = new ItemConfiguration(makeConfiguration, getItem());
        // safe using
        copy.assign(this);
        copy.setConfiguration(makeConfiguration);
        return copy;
    }

    @Override
    public ItemConfiguration clone(Configuration conf) {
        ItemConfiguration i = new ItemConfiguration(conf, getItem());

        i.setExcluded(getExcluded().clone());
        i.setTool(getTool());
        switch (getTool()) {
            case Assembler:
                i.setAssemblerConfiguration(getAssemblerConfiguration().clone());
                break;
            case CCCompiler:
                i.setCCCompilerConfiguration(getCCCompilerConfiguration().clone());
                if(isProCFile()) {
                    i.setCustomToolConfiguration(getCustomToolConfiguration().clone());
                }
                break;
            case CCompiler:
                i.setCCompilerConfiguration(getCCompilerConfiguration().clone());
                if(isProCFile()) {
                    i.setCustomToolConfiguration(getCustomToolConfiguration().clone());
                }
                break;
            case CustomTool:
                i.setCustomToolConfiguration(getCustomToolConfiguration().clone());
                break;
            case FortranCompiler:
                i.setFortranCompilerConfiguration(getFortranCompilerConfiguration().clone());
                break;
            default:
                assert false;
        }
        return i;
    }

    //
    // XML codec support
    @Override
    public XMLDecoder getXMLDecoder() {
        return new ItemXMLCodec(this);
    }

    @Override
    public XMLEncoder getXMLEncoder() {
        return new ItemXMLCodec(this);
    }

    @Override
    public void initialize() {
        // FIXUP: this doesn't make sense...
    }

    public Sheet getGeneralSheet() {
        Sheet sheet = new Sheet();

        Sheet.Set set = new Sheet.Set();
        set.setName("Item"); // NOI18N
        set.setDisplayName(getString("ItemTxt"));
        set.setShortDescription(getString("ItemHint"));
        set.put(new StringRONodeProp(getString("NameTxt"), CndPathUtilities.getBaseName(item.getPath())));
        set.put(new StringRONodeProp(getString("FilePathTxt"), item.getPath()));
        String mdate = ""; // NOI18N
        String fullPath;
        FileObject itemFO;
        MakeConfiguration mc = (MakeConfiguration) configuration;
        FileSystem sourceFS = mc.getSourceFileSystem();
        if (sourceFS == null) {
            sourceFS = CndFileUtils.getLocalFileSystem();
        }
        final String baseDir = mc.getSourceBaseDir();
        FileObject baseDirFO = sourceFS.findResource(baseDir);
        if (baseDirFO != null && baseDirFO.isValid()) {
            fullPath = CndPathUtilities.toAbsolutePath(baseDirFO, item.getPath());            
            itemFO = sourceFS.findResource(FileSystemProvider.normalizeAbsolutePath(fullPath, sourceFS));
        } else {
            fullPath = CndPathUtilities.toAbsolutePath(baseDir, item.getPath());
            itemFO = null;
        }
        if (itemFO != null && itemFO.isValid()) {
            Date lastModified = itemFO.lastModified();
            mdate = DateFormat.getDateInstance().format(lastModified);
            mdate += " " + DateFormat.getTimeInstance().format(lastModified); // NOI18N
        }
        set.put(new StringRONodeProp(getString("FullFilePathTxt"), fullPath));
        set.put(new StringRONodeProp(getString("LastModifiedTxt"), mdate));
        sheet.put(set);

        set = new Sheet.Set();
        set.setName("ItemConfiguration"); // NOI18N
        set.setDisplayName(getString("ItemConfigurationTxt"));
        set.setShortDescription(getString("ItemConfigurationHint"));
        
        if (SHOW_HEADER_EXCLUDE || !MIMENames.isHeader(item.getMIMEType())) {
            if ((getConfiguration() instanceof MakeConfiguration) &&
                    ((MakeConfiguration) getConfiguration()).isMakefileConfiguration()) {
                set.put(new BooleanNodeProp(getExcluded(), true, "ExcludedFromBuild", getString("ExcludedFromCodeAssistanceTxt"), getString("ExcludedFromCodeAssistanceHint"))); // NOI18N
            } else {
                set.put(new BooleanNodeProp(getExcluded(), true, "ExcludedFromBuild", getString("ExcludedFromBuildTxt"), getString("ExcludedFromBuildHint"))); // NOI18N
            }
        }
        set.put(new ToolNodeProp());
        sheet.put(set);

        return sheet;
    }

    public boolean isProCFile() {
        return isProCFile(item, tool);
    }

    public static boolean isProCFile(Item item, PredefinedToolKind tool) {
        if (tool == PredefinedToolKind.CCompiler
                || tool == PredefinedToolKind.CCCompiler) {
            if (item != null) {
                if (item.getName().toLowerCase().endsWith(".pc")) { //NOI18N
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isItemFromDiskFolder() {
        if (item != null) {
            Folder folder = item.getFolder();
            if (folder != null && folder.isDiskFolder()) {
                return true;
            }
        }
        return false;
    }

    private class ToolNodeProp extends Node.Property<PredefinedToolKind> {

        public ToolNodeProp() {
            super(PredefinedToolKind.class);
        }

        @Override
        public String getName() {
            return getString("ToolTxt1");
        }

        @Override
        public PredefinedToolKind getValue() {
            return getTool();
        }

        @Override
        public void setValue(PredefinedToolKind v) {
            setTool(v);
        }

        @Override
        public boolean canWrite() {
            return true;
        }

        @Override
        public boolean canRead() {
            return true;
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return new ToolEditor();
        }
    }

    private class ToolEditor extends PropertyEditorSupport {

        @Override
        public String getJavaInitializationString() {
            return getAsText();
        }

        @Override
        public String getAsText() {
            ToolKind val = (ToolKind) getValue();
            return val.getDisplayName();
//            CompilerSet set = CompilerSetManager.getDefault(((MakeConfiguration)configuration).getDevelopmentHost().getName()).getCompilerSet(((MakeConfiguration)configuration).getCompilerSet().getValue());
//            return set.getTool(val).getGenericName();
        }

        @Override
        public void setAsText(String text) throws java.lang.IllegalArgumentException {
//            setValue(text);
            setValue(PredefinedToolKind.getTool(text));
        }

        @Override
        public String[] getTags() {
            return getToolNames();
        }
    }

    private static class StringRONodeProp extends PropertySupport<String> {

        private String value;

        public StringRONodeProp(String name, String value) {
            super(name, String.class, name, name, true, false);
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public void setValue(String v) {
        }
    }

    @Override
    public String toString() {
        String pref = "";
        if (this.excluded != null && excluded.getValue()) {
            pref = "[excluded]"; // NOI18N
        }
        return pref + getItem().getPath();
    }
    private static ResourceBundle bundle = null;

    private static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(ItemConfiguration.class);
        }
        return bundle.getString(s);
    }
}

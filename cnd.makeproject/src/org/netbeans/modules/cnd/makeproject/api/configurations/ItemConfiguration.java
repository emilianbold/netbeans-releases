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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import org.netbeans.modules.cnd.makeproject.configurations.ItemXMLCodec;
import org.netbeans.modules.cnd.makeproject.configurations.ui.BooleanNodeProp;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.api.xml.XMLDecoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoder;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.makeproject.api.configurations.BasicCompilerConfiguration;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

public class ItemConfiguration implements ConfigurationAuxObject {
    private boolean needSave = false;
    
    private Configuration configuration;
    private Item item;
    
    // General
    private BooleanConfiguration excluded;
    private int tool = -1;
    
    // Tools
    private CustomToolConfiguration customToolConfiguration;
    private CCompilerConfiguration cCompilerConfiguration;
    private CCCompilerConfiguration ccCompilerConfiguration;
    private FortranCompilerConfiguration fortranCompilerConfiguration;

    // cached id of item
//    private String id;
    
    public ItemConfiguration(Configuration configuration, Item item) {
        // General
        this.configuration = configuration;
        setItem(item);
        excluded = new BooleanConfiguration(null, false);
        // Compilers
        customToolConfiguration = new CustomToolConfiguration();
        cCompilerConfiguration = new CCompilerConfiguration(((MakeConfiguration)configuration).getBaseDir(), item.getFolder().getFolderConfiguration(configuration).getCCompilerConfiguration());
        ccCompilerConfiguration = new CCCompilerConfiguration(((MakeConfiguration)configuration).getBaseDir(), item.getFolder().getFolderConfiguration(configuration).getCCCompilerConfiguration());
        fortranCompilerConfiguration = new FortranCompilerConfiguration(((MakeConfiguration)configuration).getBaseDir(), ((MakeConfiguration)configuration).getFortranCompilerConfiguration());
        clearChanged();
    }
    
    public boolean isCompilerToolConfiguration() {
        return getTool() == Tool.CCompiler ||
                getTool() == Tool.CCCompiler ||
                getTool() == Tool.FortranCompiler;
    }
    
    public BasicCompilerConfiguration getCompilerConfiguration() {
        if (getTool() == Tool.CCompiler)
            return cCompilerConfiguration;
        else if (getTool() == Tool.CCCompiler)
            return ccCompilerConfiguration;
        else if (getTool() == Tool.FortranCompiler)
            return fortranCompilerConfiguration;
        else
            assert false;
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
    
    // Tool
    public void setTool(String genericName) {
        if (genericName != null) {
            CompilerSet set = CompilerSetManager.getDefault().getCompilerSet(((MakeConfiguration)configuration).getCompilerSet().getValue());
            tool = set.getToolKind(genericName);
        }
    }
    public void setTool(int tool) {
        this.tool = tool;
    }
    public int getTool() {
        if (tool == -1) {
            tool = item.getDefaultTool();
        }
        return tool;
    }
    protected String getToolName() {
        CompilerSet set = CompilerSetManager.getDefault().getCompilerSet(((MakeConfiguration)configuration).getCompilerSet().getValue());
        return set.getTool(getTool()).getName();
    }
    protected String[] getToolNames() {
        CompilerSet set = CompilerSetManager.getDefault().getCompilerSet(((MakeConfiguration)configuration).getCompilerSet().getValue());
        return set.getToolGenericNames();
    }
    
    // Custom Tool
    public void setCustomToolConfiguration(CustomToolConfiguration customToolConfiguration) {
        this.customToolConfiguration = customToolConfiguration;
    }
    
    public CustomToolConfiguration getCustomToolConfiguration() {
        return customToolConfiguration;
    }
    
    // C Compiler
    public void setCCompilerConfiguration(CCompilerConfiguration cCompilerConfiguration) {
        this.cCompilerConfiguration = cCompilerConfiguration;
    }
    
    public CCompilerConfiguration getCCompilerConfiguration() {
        return cCompilerConfiguration;
    }
    
    // CC Compiler
    public void setCCCompilerConfiguration(CCCompilerConfiguration ccCompilerConfiguration) {
        this.ccCompilerConfiguration = ccCompilerConfiguration;
    }
    
    public CCCompilerConfiguration getCCCompilerConfiguration() {
        return ccCompilerConfiguration;
    }
    
    // Fortran Compiler
    public void setFortranCompilerConfiguration(FortranCompilerConfiguration fortranCompilerConfiguration) {
        this.fortranCompilerConfiguration = fortranCompilerConfiguration;
    }
    
    public FortranCompilerConfiguration getFortranCompilerConfiguration() {
        return fortranCompilerConfiguration;
    }
    
    // interface ConfigurationAuxObject
    public boolean shared() {
        return true;
    }
    
    // interface ConfigurationAuxObject
    public boolean hasChanged() {
        return needSave;
    }
    
    // interface ProfileAuxObject
    public void clearChanged() {
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
    
    public String getId() {
        return item.getId();
    }
    
    public void assign(ConfigurationAuxObject profileAuxObject) {
        if (!(profileAuxObject instanceof ItemConfiguration)) {
            // FIXUP: exception ????
            System.err.println("Item - assign: Profile object type expected - got " + profileAuxObject); // NOI18N
            return;
        }
        ItemConfiguration i = (ItemConfiguration)profileAuxObject;
        if (!getId().equals(i.getItem().getId())){
            System.err.println("Item - assign: Item ID "+getId()+" expected - got " + i.getItem().getId()); // NOI18N
            return;
        }
        setConfiguration(i.getConfiguration());
        setItem(i.getItem());
        getExcluded().assign(i.getExcluded());
        setTool(i.getTool());
        
        getCustomToolConfiguration().assign(i.getCustomToolConfiguration());
        getCCompilerConfiguration().assign(i.getCCompilerConfiguration());
        getCCCompilerConfiguration().assign(i.getCCCompilerConfiguration());
        getFortranCompilerConfiguration().assign(i.getFortranCompilerConfiguration());
    }
    
    public void assignValues(ConfigurationAuxObject profileAuxObject) {
        if (!(profileAuxObject instanceof ItemConfiguration)) {
            // FIXUP: exception ????
            System.err.println("Item - assign: Profile object type expected - got " + profileAuxObject); // NOI18N
            return;
        }
        ItemConfiguration i = (ItemConfiguration)profileAuxObject;
        getExcluded().assign(i.getExcluded());
        setTool(i.getTool());
        
        getCustomToolConfiguration().assign(i.getCustomToolConfiguration());
        getCCompilerConfiguration().assign(i.getCCompilerConfiguration());
        getCCCompilerConfiguration().assign(i.getCCCompilerConfiguration());
        getFortranCompilerConfiguration().assign(i.getFortranCompilerConfiguration());
    }
    
    public ItemConfiguration copy(MakeConfiguration makeConfiguration) {
        ItemConfiguration copy = new ItemConfiguration(makeConfiguration, getItem());
        // safe using
        copy.assign(this);
        return copy;
    }
    
    public Object clone() {
        ItemConfiguration i = new ItemConfiguration(getConfiguration(), getItem());
        
        i.setExcluded((BooleanConfiguration)getExcluded().clone());
        i.setTool(getTool());
        
        i.setCustomToolConfiguration((CustomToolConfiguration)getCustomToolConfiguration().clone());
        i.setCCompilerConfiguration((CCompilerConfiguration)getCCompilerConfiguration().clone());
        i.setCCCompilerConfiguration((CCCompilerConfiguration)getCCCompilerConfiguration().clone());
        i.setFortranCompilerConfiguration((FortranCompilerConfiguration)getFortranCompilerConfiguration().clone());
        return i;
    }
    
    //
    // XML codec support
    public XMLDecoder getXMLDecoder() {
        return new ItemXMLCodec(this);
    }
    
    public XMLEncoder getXMLEncoder() {
        return new ItemXMLCodec(this);
    }
    
    public void initialize() {
        // FIXUP: this doesn't make sense...
    }
    
    public Sheet getGeneralSheet() {
        Sheet sheet = new Sheet();
        
        Sheet.Set set = new Sheet.Set();
        set.setName("Item"); // NOI18N
        set.setDisplayName(getString("ItemTxt"));
        set.setShortDescription(getString("ItemHint"));
        set.put(new StringRONodeProp(getString("NameTxt"), IpeUtils.getBaseName(item.getPath())));
        set.put(new StringRONodeProp(getString("FilePathTxt"), item.getPath()));
        String fullPath = IpeUtils.toAbsolutePath(((MakeConfiguration)configuration).getBaseDir(), item.getPath());
        String mdate = ""; // NOI18N
        File itemFile = new File(fullPath);
        if (itemFile.exists()) {
            mdate = DateFormat.getDateInstance().format(new Date(itemFile.lastModified()));
            mdate += " " + DateFormat.getTimeInstance().format(new Date(itemFile.lastModified())); // NOI18N
        }
        set.put(new StringRONodeProp(getString("FullFilePathTxt"), fullPath));
        set.put(new StringRONodeProp(getString("LastModifiedTxt"), mdate));
        sheet.put(set);
        
        set = new Sheet.Set();
        set.setName("ItemConfiguration"); // NOI18N
        set.setDisplayName(getString("ItemConfigurationTxt"));
        set.setShortDescription(getString("ItemConfigurationHint"));
        if ((getConfiguration() instanceof MakeConfiguration) &&
            ((MakeConfiguration)getConfiguration()).isMakefileConfiguration()){
            set.put(new BooleanNodeProp(getExcluded(), true, "ExcludedFromBuild", getString("ExcludedFromCodeAssistanceTxt"), getString("ExcludedFromCodeAssistanceHint"))); // NOI18N
        } else {
            set.put(new BooleanNodeProp(getExcluded(), true, "ExcludedFromBuild", getString("ExcludedFromBuildTxt"), getString("ExcludedFromBuildHint"))); // NOI18N
        }
        set.put(new ToolNodeProp());
        sheet.put(set);
        
        return sheet;
    }
    
    private class ToolNodeProp extends Node.Property {
        public ToolNodeProp() {
            super(Integer.class);
        }
        
        public String getName() {
            return getString("ToolTxt1");
        }
        
        public Object getValue() {
            return new Integer(getTool());
        }
        
        public void setValue(Object v) {
            String newTool = (String)v;
            setTool(newTool);
        }
        
        public boolean canWrite() {
            return true;
        }
        
        public boolean canRead() {
            return true;
        }
        
        public PropertyEditor getPropertyEditor() {
            return new ToolEditor();
        }
    }
    
    private class ToolEditor extends PropertyEditorSupport {
        
        public String getJavaInitializationString() {
            return getAsText();
        }
        
        public String getAsText() {
            int val = ((Integer)getValue()).intValue();
            CompilerSet set = CompilerSetManager.getDefault().getCompilerSet(((MakeConfiguration)configuration).getCompilerSet().getValue());
            return set.getTool(val).getGenericName();
        }
        
        public void setAsText(String text) throws java.lang.IllegalArgumentException {
            setValue(text);
        }
        
        public String[] getTags() {
            return getToolNames();
        }
    }
    
    private class StringRONodeProp extends PropertySupport {
        String value;
        public StringRONodeProp(String name, String value) {
            super(name, String.class, name, name, true, false);
            this.value = value;
        }
        
        public Object getValue() {
            return value;
        }
        
        public void setValue(Object v) {
        }
    }
    
    
    public String toString() {
        return getItem().getPath();
    }
    
    private static ResourceBundle bundle = null;
    private static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(ItemConfiguration.class);
        }
        return bundle.getString(s);
    }
}

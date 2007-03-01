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

import java.util.ResourceBundle;
import org.netbeans.modules.cnd.api.xml.XMLDecoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoder;
import org.netbeans.modules.cnd.makeproject.configurations.FolderXMLCodec;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

public class FolderConfiguration implements ConfigurationAuxObject {
    private boolean needSave = false;
    
    private Configuration configuration;
    private Folder folder;
    
    // Tools
    private CCompilerConfiguration cCompilerConfiguration;
    private CCCompilerConfiguration ccCompilerConfiguration;
    
    public FolderConfiguration(Configuration configuration, CCompilerConfiguration parentCCompilerConfiguration, CCCompilerConfiguration parentCCCompilerConfiguration, Folder folder) {
        // General
        this.configuration = configuration;
        setFolder(folder);
        // Compilers
        cCompilerConfiguration = new CCompilerConfiguration(((MakeConfiguration)configuration).getBaseDir(), parentCCompilerConfiguration);
        ccCompilerConfiguration = new CCCompilerConfiguration(((MakeConfiguration)configuration).getBaseDir(), parentCCCompilerConfiguration);
        clearChanged();
    }
    
    public Configuration getConfiguration() {
        return configuration;
    }
    
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
    
    public Folder getFolder() {
        return folder;
    }
    
    public void setFolder(Folder folder) {
        this.folder = folder;
        needSave = true;
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
    public String getId() {
        return folder.getId();
    }
    
    
    public void assign(ConfigurationAuxObject profileAuxObject) {
        if (!(profileAuxObject instanceof FolderConfiguration)) {
            // FIXUP: exception ????
            System.err.print("Item - assign: Profile object type expected - got " + profileAuxObject); // NOI18N
            return;
        }
        FolderConfiguration i = (FolderConfiguration)profileAuxObject;
        setConfiguration(i.getConfiguration());
        setFolder(i.getFolder());
        
        getCCompilerConfiguration().assign(i.getCCompilerConfiguration());
        getCCCompilerConfiguration().assign(i.getCCCompilerConfiguration());
    }
    
    public FolderConfiguration copy(MakeConfiguration makeConfiguration) {
        FolderConfiguration copy = new FolderConfiguration(makeConfiguration, (CCompilerConfiguration)getCCompilerConfiguration().getMaster(), (CCCompilerConfiguration)getCCCompilerConfiguration().getMaster(), getFolder());
        copy.assign(this);
        return copy;
    }
    
    public Object clone() {
        FolderConfiguration i = new FolderConfiguration(getConfiguration(), (CCompilerConfiguration)getCCompilerConfiguration().getMaster(), (CCCompilerConfiguration)getCCCompilerConfiguration().getMaster(), getFolder());
        
        i.setCCompilerConfiguration((CCompilerConfiguration)getCCompilerConfiguration().clone());
        i.setCCCompilerConfiguration((CCCompilerConfiguration)getCCCompilerConfiguration().clone());
        return i;
    }
    
    //
    // XML codec support
    public XMLDecoder getXMLDecoder() {
        return new FolderXMLCodec(this);
    }
    
    public XMLEncoder getXMLEncoder() {
        return new FolderXMLCodec(this);
    }
    
    public void initialize() {
        // FIXUP: this doesn't make sense...
    }
    
    public Sheet getGeneralSheet() {
        Sheet sheet = new Sheet();
        
        Sheet.Set set = new Sheet.Set();
        set.setName("FolderConfiguration"); // NOI18N
        set.setDisplayName(getString("FolderConfigurationTxt"));
        set.setShortDescription(getString("FolderConfigurationHint"));
        set.put(new StringRONodeProp(getString("NameTxt"), folder.getDisplayName()));
        sheet.put(set);
        
        return sheet;
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
//    public String toString() {
//        return getFolder().getPath();
//    }
    
    private static ResourceBundle bundle = null;
    private static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(FolderConfiguration.class);
        }
        return bundle.getString(s);
    }
}

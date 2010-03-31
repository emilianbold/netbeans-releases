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
    private LinkerConfiguration linkerConfiguration = null;

    public FolderConfiguration(Configuration configuration, CCompilerConfiguration parentCCompilerConfiguration, CCCompilerConfiguration parentCCCompilerConfiguration, Folder folder) {
        // General
        this.configuration = configuration;
        setFolder(folder);
        // Compilers
        cCompilerConfiguration = new CCompilerConfiguration(((MakeConfiguration) configuration).getBaseDir(), parentCCompilerConfiguration);
        ccCompilerConfiguration = new CCCompilerConfiguration(((MakeConfiguration) configuration).getBaseDir(), parentCCCompilerConfiguration);
        if(folder.isTest() || folder.isTestLogicalFolder() || folder.isTestRootFolder()) {
            linkerConfiguration = new LinkerConfiguration((MakeConfiguration) configuration);
            if(folder.isTest()) {
                linkerConfiguration.getOutput().setValue("${TESTDIR}/" + folder.getPath()); // NOI18N
            }
        }
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

    private void setFolder(Folder folder) {
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

    // Linker
    public void setLinkerConfiguration(LinkerConfiguration linkerConfiguration) {
        this.linkerConfiguration = linkerConfiguration;
    }

    public LinkerConfiguration getLinkerConfiguration() {
        return linkerConfiguration;
    }

    // interface ConfigurationAuxObject
    @Override
    public boolean shared() {
        return true;
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
    @Override
    public String getId() {
        return folder.getId();
    }

    public void assignValues(FolderConfiguration folderConfiguration) {
        getCCompilerConfiguration().assign(folderConfiguration.getCCompilerConfiguration());
        getCCCompilerConfiguration().assign(folderConfiguration.getCCCompilerConfiguration());
        if(getLinkerConfiguration() != null && folderConfiguration.getLinkerConfiguration() != null) {
            getLinkerConfiguration().assign(folderConfiguration.getLinkerConfiguration());
        }
    }

    @Override
    public void assign(ConfigurationAuxObject profileAuxObject) {
        if (!(profileAuxObject instanceof FolderConfiguration)) {
            // FIXUP: exception ????
            System.err.println("Folder - assign: Profile object type expected - got " + profileAuxObject); // NOI18N
            return;
        }
        FolderConfiguration i = (FolderConfiguration) profileAuxObject;
        if (!getId().equals(i.getFolder().getId())) {
            System.err.println("Item - assign: Item ID " + getId() + " expected - got " + i.getFolder().getId()); // NOI18N
            return;
        }
        setConfiguration(i.getConfiguration());
        setFolder(i.getFolder());

        getCCompilerConfiguration().assign(i.getCCompilerConfiguration());
        getCCCompilerConfiguration().assign(i.getCCCompilerConfiguration());
        if(getLinkerConfiguration() != null && i.getLinkerConfiguration() != null) {
            getLinkerConfiguration().assign(i.getLinkerConfiguration());
        }
    }

    public FolderConfiguration copy(MakeConfiguration makeConfiguration) {
        FolderConfiguration copy = new FolderConfiguration(makeConfiguration, (CCompilerConfiguration) getCCompilerConfiguration().getMaster(), (CCCompilerConfiguration) getCCCompilerConfiguration().getMaster(), getFolder());
        // safe using
        copy.assign(this);
        return copy;
    }

    @Override
    public FolderConfiguration clone(Configuration conf) {
        FolderConfiguration i = new FolderConfiguration(getConfiguration(), (CCompilerConfiguration) getCCompilerConfiguration().getMaster(), (CCCompilerConfiguration) getCCCompilerConfiguration().getMaster(), getFolder());
        i.setCCompilerConfiguration(getCCompilerConfiguration().clone());
        i.setCCCompilerConfiguration(getCCCompilerConfiguration().clone());
        if(getLinkerConfiguration() != null) {
            i.setLinkerConfiguration(getLinkerConfiguration().clone());
        }
        return i;
    }

    //
    // XML codec support
    @Override
    public XMLDecoder getXMLDecoder() {
        return new FolderXMLCodec(this);
    }

    @Override
    public XMLEncoder getXMLEncoder() {
        return new FolderXMLCodec(this);
    }

    @Override
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

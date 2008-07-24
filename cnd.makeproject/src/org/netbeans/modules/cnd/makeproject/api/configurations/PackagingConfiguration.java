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
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platform;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platforms;
import org.netbeans.modules.cnd.makeproject.configurations.ui.IntNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.PackagingNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.StringNodeProp;
import org.netbeans.modules.cnd.makeproject.ui.customizer.MakeCustomizer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

public class PackagingConfiguration {
    private MakeConfiguration makeConfiguration;
    
    // Types
    private static String[] TYPE_NAMES = {
        getString("SCR4Package"),
        getString("IPSPackage"),
        getString("Tar"),
        getString("Zip"),
    };
    public static final int TYPE_SVR4_PACKAGE = 0;
    public static final int TYPE_IPS_PACKAGE = 1;
    public static final int TYPE_TAR = 2;
    public static final int TYPE_ZIP = 3;
    
    private IntConfiguration type;
    private VectorConfiguration header;
    private VectorConfiguration files;
    private StringConfiguration outputZip;
    private StringConfiguration outputTar;
    
    // Constructors
    public PackagingConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
        type = new IntConfiguration(null, TYPE_ZIP, TYPE_NAMES, null);
        header = new VectorConfiguration(null); // NOI18N
        files = new VectorConfiguration(null); // NOI18N
	outputZip = new StringConfiguration(null, ""); // NOI18N
	outputTar = new StringConfiguration(null, ""); // NOI18N
    }
    
    // MakeConfiguration
    public void setMakeConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
    }
    
    public MakeConfiguration getMakeConfiguration() {
        return makeConfiguration;
    }
    

    public IntConfiguration getType() {
        return type;
    }

    public void setType(IntConfiguration type) {
        this.type = type;
    }

    public VectorConfiguration getHeader() {
        return header;
    }

    public void setHeader(VectorConfiguration header) {
        this.header = header;
    }
    
    public VectorConfiguration getFiles() {
        return files;
    }

    public void setFiles(VectorConfiguration files) {
        this.files = files;
    }
    
    public void setOutputZip(StringConfiguration output) {
	this.outputZip = output;
    }
    public StringConfiguration getOutputZip() {
	return outputZip;
    }
    
    public void setOutputTar(StringConfiguration output) {
	this.outputTar = output;
    }
    public StringConfiguration getOutputTar() {
	return outputTar;
    }
    
    // Clone and assign
    public void assign(PackagingConfiguration conf) {
        setMakeConfiguration(conf.getMakeConfiguration());
        getType().assign(conf.getType());
        getHeader().assign(conf.getHeader());
        getFiles().assign(conf.getFiles());
	getOutputZip().assign(conf.getOutputZip());
	getOutputTar().assign(conf.getOutputTar());
    }
    
    @Override
    public Object clone() {
        PackagingConfiguration clone = new PackagingConfiguration(getMakeConfiguration());
        clone.setType((IntConfiguration)getType().clone());
        clone.setHeader((VectorConfiguration)getHeader().clone());
        clone.setFiles((VectorConfiguration)getFiles().clone());
	clone.setOutputZip((StringConfiguration)getOutputZip().clone());
	clone.setOutputTar((StringConfiguration)getOutputTar().clone());
        return clone;
    }
    
    TypePropertyChangeListener typePropertyChangeListener;
    // Sheet
    public Sheet getGeneralSheet(MakeCustomizer makeCustomizer) {
        IntNodeProp intNodeprop;
        OutputNodeProp outputNodeProp;
    
        Sheet sheet = new Sheet();
        Sheet.Set set = new Sheet.Set();
        set.setName("General"); // NOI18N
        set.setDisplayName(getString("GeneralTxt"));
        set.setShortDescription(getString("GeneralHint"));
        String[] texts = new String[] {"Packaging", "Packaging", "Packaging..."};
        set.put(intNodeprop = new IntNodeProp(getType(), true, "Type", "Type", "Type")); // NOI18N
        set.put(new PackagingNodeProp(this, makeConfiguration, texts)); // NOI18N
	set.put(outputNodeProp = new OutputNodeProp(getOutputZip(), getOutputDefault(), "Output", getString("OutputTxt"), getString("OutputHint"))); // NOI18N
        sheet.put(set);
        
        intNodeprop.getPropertyEditor().addPropertyChangeListener(typePropertyChangeListener = new TypePropertyChangeListener(makeCustomizer, outputNodeProp));
        return sheet;
    }
    
    class TypePropertyChangeListener implements PropertyChangeListener {
        private MakeCustomizer makeCustomizer;
        private OutputNodeProp outputNodeProp;
        
        TypePropertyChangeListener(MakeCustomizer makeCustomizer, OutputNodeProp outputNodeProp) {
            this.makeCustomizer = makeCustomizer;
            this.outputNodeProp = outputNodeProp;
        }

        public void propertyChange(PropertyChangeEvent arg0) {
            String od = getOutputDefault();
            if (!outputZip.getModified()) {
                outputNodeProp.setDefaultValue(od);
                outputZip.reset();
                makeCustomizer.validate(); // this swill trigger repainting of the property
                makeCustomizer.repaint();
            }
        }
    }
    
    private String getOutputDefault() {
	String outputName = IpeUtils.getBaseName(getMakeConfiguration().getBaseDir());
	if (getMakeConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_APPLICATION)
	    outputName = outputName.toLowerCase();
	else if (getMakeConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_DYNAMIC_LIB) {
            Platform platform = Platforms.getPlatform(getMakeConfiguration().getPlatform().getValue());
            outputName = platform.getLibraryName(outputName);
        }
        outputName = ConfigurationSupport.makeNameLegal(outputName);
	String outputPath = MakeConfiguration.DIST_FOLDER + "/" + getMakeConfiguration().getName() + "/" + getMakeConfiguration().getVariant() + "/"; // NOI18N 
        
        if (getType().getValue() == PackagingConfiguration.TYPE_IPS_PACKAGE) {
            outputPath += "<TBD>"; // NOI18N // FIXUP
        }
        else if (getType().getValue() == PackagingConfiguration.TYPE_SVR4_PACKAGE) {
            outputPath += "<TBD>"; // NOI18N // FIXUP 
        }
        else if (getType().getValue() == PackagingConfiguration.TYPE_TAR) {
            outputPath += outputName + ".tar"; // NOI18N
        }
        else if (getType().getValue() == PackagingConfiguration.TYPE_ZIP) {
            outputPath += outputName + ".zip"; // NOI18N
        } 
        
        return outputPath;
    }
    
    private class OutputNodeProp extends StringNodeProp {
        public OutputNodeProp(StringConfiguration stringConfiguration, String def, String txt1, String txt2, String txt3) {
            super(stringConfiguration, def, txt1, txt2, txt3);
        }
        
        @Override
        public void setValue(Object v) {
            if (IpeUtils.hasMakeSpecialCharacters((String)v)) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(getString("SPECIAL_CHARATERS_ERROR"), NotifyDescriptor.ERROR_MESSAGE));
                return;
            }
            super.setValue(v);
        }
        }

    public String[] getDisplayNames() {
        return TYPE_NAMES;
    }
    
    public String getDisplayName() {
        return TYPE_NAMES[getType().getValue()];
    }
    
    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(PackagingConfiguration.class, s);
    }
}

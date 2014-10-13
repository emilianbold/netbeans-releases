/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.JFileChooser;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.BooleanNodeProp;
import org.netbeans.modules.cnd.makeproject.api.wizards.PreBuildSupport;
import org.netbeans.modules.cnd.makeproject.configurations.ui.MacroExpandedEditorPanel;
import org.netbeans.modules.cnd.makeproject.configurations.ui.StringNodeProp;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Alexander Simon
 */
public class PreBuildConfiguration implements Cloneable {

    private MakeConfiguration makeConfiguration;
    private StringConfiguration preBuildCommandWorkingDir;
    private StringConfiguration preBuildCommand;
    private BooleanConfiguration preBuildFirst;
    
    private static final RequestProcessor RP = new RequestProcessor("MakeConfiguration", 1); // NOI18N
    
    // Constructors
    public PreBuildConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
        preBuildCommandWorkingDir = new StringConfiguration(null, "."); // NOI18N
        preBuildCommand = new StringConfiguration(null, ""); // NOI18N
        preBuildFirst = new BooleanConfiguration(false);
    }
    
    // MakeConfiguration
    public void setMakeConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
    }
    public MakeConfiguration getMakeConfiguration() {
        return makeConfiguration;
    }
    
    public void setPreBuildFirst(BooleanConfiguration preBuildFirst){
        this.preBuildFirst = preBuildFirst;
    }
    
    public BooleanConfiguration getPreBuildFirst(){
        return preBuildFirst;
    }
    
    // Working Dir
    public StringConfiguration getPreBuildCommandWorkingDir() {
        return preBuildCommandWorkingDir;
    }
    
    // Working Dir
    public String getPreBuildCommandWorkingDirValue() {
        if (preBuildCommandWorkingDir.getValue().length() == 0) {
            return "."; // NOI18N
        } else {
            return preBuildCommandWorkingDir.getValue();
        }
    }
    
    public void setPreBuildCommandWorkingDir(StringConfiguration buildCommandWorkingDir) {
        this.preBuildCommandWorkingDir = buildCommandWorkingDir;
    }
    
    // Pre-Build Command
    public StringConfiguration getPreBuildCommand() {
        return preBuildCommand;
    }
    
    public void setPreBuildCommand(StringConfiguration buildCommand) {
        this.preBuildCommand = buildCommand;
    }
    
    // the "Abs" part does not make sense for file objects, 
    // but let's keep function name close to getAbsBuildCommandWorkingDir()
    public FileObject getAbsPreBuildCommandFileObject() {        
        String path = getAbsPreBuildCommandWorkingDir();
        return FileSystemProvider.getFileObject(getSourceExecutionEnvironment(), path);
    }

    public String getAbsPreBuildCommandWorkingDir() {
        String wd;
        if (getPreBuildCommandWorkingDirValue().length() > 0 && CndPathUtilities.isPathAbsolute(getPreBuildCommandWorkingDirValue())) {
            wd = getPreBuildCommandWorkingDirValue();
        } else {
            wd = getMakeConfiguration().getBaseDir() + "/" + getPreBuildCommandWorkingDirValue(); // NOI18N
        }
        // Normalize            
        wd = FileSystemProvider.normalizeAbsolutePath(wd, getSourceExecutionEnvironment());
        return wd;
    }

    // Clone and assign
    public void assign(PreBuildConfiguration conf) {
        getPreBuildCommandWorkingDir().assign(conf.getPreBuildCommandWorkingDir());
        getPreBuildCommand().assign(conf.getPreBuildCommand());
        getPreBuildFirst().assign(conf.getPreBuildFirst());
    }

    @Override
    public PreBuildConfiguration clone() {
        PreBuildConfiguration clone = new PreBuildConfiguration(getMakeConfiguration());
        clone.setPreBuildCommandWorkingDir(getPreBuildCommandWorkingDir().clone());
        clone.setPreBuildCommand(getPreBuildCommand().clone());
        clone.setPreBuildFirst(getPreBuildFirst().clone());
        return clone;
    }

    public Sheet getSheet() {
        Sheet sheet = new Sheet();
        
        Sheet.Set set = new Sheet.Set();
        set.setName("PreBuild"); // NOI18N
        set.setDisplayName(getString("PreBuildTxt"));
        set.setShortDescription(getString("PreBuildHint"));
        set.put(new DirStringNodeProp(getPreBuildCommandWorkingDir(), "PreBuildWorkingDirectory", getString("PreBuildWorkingDirectory_LBL"), getString("PreBuildWorkingDirectory_TT"))); // NOI18N
        set.put(new PreviewStringNodeProp(getPreBuildCommand(), "PreBuildCommandLine", getString("PreBuildCommandLine_LBL"), getString("PreBuildCommandLine_TT"))); // NOI18N
        set.put(new BooleanNodeProp(getPreBuildFirst(), true, "PreBuildFirst",  getString("PreBuildFirst_LBL"), getString("PreBuildFirst_TT"))); // NOI18N
        sheet.put(set);
        return sheet;
    }
    
    private ExecutionEnvironment getSourceExecutionEnvironment() {
        ExecutionEnvironment env = null;
        MakeConfiguration mc = this.getMakeConfiguration();
        if (mc != null) {
            return FileSystemProvider.getExecutionEnvironment(mc.getBaseFSPath().getFileSystem());
        }
        if (env == null) {
            env = ExecutionEnvironmentFactory.getLocal();
        }
        return env;
    }

    private class DirStringNodeProp extends StringNodeProp {
        public DirStringNodeProp(StringConfiguration stringConfiguration, String txt1, String txt2, String txt3) {
            super(stringConfiguration, txt1, txt2, txt3);
        }
        
        @Override
        public void setValue(String v) {
            String path = CndPathUtilities.toRelativePath(getMakeConfiguration().getBaseDir(), v); // FIXUP: not always relative path
            path = CndPathUtilities.normalizeSlashes(path);
            super.setValue(path);
        }
        
        @Override
        public PropertyEditor getPropertyEditor() {
            return new DirEditor(getAbsPreBuildCommandWorkingDir());
        }
    }
    
    private class DirEditor extends PropertyEditorSupport implements ExPropertyEditor {
        private PropertyEnv propenv;
        private final String seed;
        
        public DirEditor(String seed) {
            this.seed = seed;
        }
        
        @Override
        public void setAsText(String text) {
            getPreBuildCommandWorkingDir().setValue(text);
        }
        
        @Override
        public String getAsText() {
            return getPreBuildCommandWorkingDir().getValue();
        }
        
        @Override
        public Object getValue() {
            return getPreBuildCommandWorkingDir().getValue();
        }
        
        @Override
        public void setValue(Object v) {
            getPreBuildCommandWorkingDir().setValue((String)v);
        }
        
        @Override
        public boolean supportsCustomEditor() {
            return true;
        }
        
        @Override
        public java.awt.Component getCustomEditor() {
            return createDirPanel(seed, this, propenv);
        }
        
        @Override
        public void attachEnv(PropertyEnv propenv) {
            this.propenv = propenv;
        }
    }

    private JFileChooser createDirPanel(String seed, final PropertyEditorSupport editor, PropertyEnv propenv) {
        String titleText = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/api/Bundle").getString("Run_Directory");
        String buttonText = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/api/Bundle").getString("SelectLabel");
        final JFileChooser chooser = RemoteFileUtil.createFileChooser(getSourceExecutionEnvironment(), titleText, buttonText,
                JFileChooser.DIRECTORIES_ONLY, null, seed, true);
        chooser.putClientProperty("title", chooser.getDialogTitle()); // NOI18N
        chooser.setControlButtonsAreShown(false);
        propenv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        propenv.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) && evt.getNewValue() == PropertyEnv.STATE_VALID) {
                    File selectedFile= chooser.getSelectedFile();
                    String path = CndPathUtilities.toRelativePath(makeConfiguration.getBaseDir(), selectedFile.getPath()); // FIXUP: not always relative path
                    path = CndPathUtilities.normalizeSlashes(path);
                    editor.setValue(path);
                }
            }
        });
        return chooser;
    }
   
    /** Look up i18n strings here */
    private static ResourceBundle bundle;
    private static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(PreBuildConfiguration.class);
        }
        return bundle.getString(s);
    }
    
    private final class PreviewStringNodeProp extends StringNodeProp {
        private PreviewStringNodeProp(StringConfiguration stringConfiguration, String txt1, String txt2, String txt3) {
            super(stringConfiguration, txt1, txt2, txt3);
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            Map<String,String> macros = new HashMap<>();
            CompilerSet cs = makeConfiguration.getCompilerSet().getCompilerSet();
            if (cs != null) {
                Tool tool = cs.getTool(PredefinedToolKind.CCompiler);
                if (tool != null) {
                    macros.put(PreBuildSupport.C_COMPILER_MACRO, tool.getPath());
                }
                tool = cs.getTool(PredefinedToolKind.CCCompiler);
                if (tool != null) {
                    macros.put(PreBuildSupport.CPP_COMPILER_MACRO, tool.getPath());
                }
            }
            return new PreviewCommandLinePropEditor(macros);
        }
    }
    
    private static class PreviewCommandLinePropEditor extends PropertyEditorSupport implements ExPropertyEditor {

        private PropertyEnv env;
        private final Map<String,String> macros;
        private PreviewCommandLinePropEditor(Map<String,String> macros) {
            this.macros = macros;
        }

        @Override
        public java.awt.Component getCustomEditor() {
            MacroExpandedEditorPanel commandLineEditorPanel = new MacroExpandedEditorPanel(this, env, macros);
            return commandLineEditorPanel;
        }

        @Override
        public boolean supportsCustomEditor() {
            return true;
        }

        @Override
        public void attachEnv(PropertyEnv env) {
            this.env = env;
        }
    }

}

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
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.remote.RemoteProject;
import org.netbeans.modules.cnd.makeproject.configurations.ui.StringNodeProp;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.FileFilterFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class MakefileConfiguration implements Cloneable {
    private MakeConfiguration makeConfiguration;
    
    private StringConfiguration buildCommandWorkingDir;
    private StringConfiguration buildCommand;
    private StringConfiguration cleanCommand;
    private StringConfiguration output;
    
    private static final RequestProcessor RP = new RequestProcessor("MakeConfiguration", 1); // NOI18N
    
    // Constructors
    public MakefileConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
        buildCommandWorkingDir = new StringConfiguration(null, "."); // NOI18N
        buildCommand = new StringConfiguration(null, "${MAKE}"); // NOI18N
        cleanCommand = new StringConfiguration(null, "${MAKE} clean"); // NOI18N
        output = new StringConfiguration(null, ""); // NOI18N
    }
    
    // MakeConfiguration
    public void setMakeConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
    }
    public MakeConfiguration getMakeConfiguration() {
        return makeConfiguration;
    }
    
    // Working Dir
    public StringConfiguration getBuildCommandWorkingDir() {
        return buildCommandWorkingDir;
    }
    
    // Working Dir
    public String getBuildCommandWorkingDirValue() {
        if (buildCommandWorkingDir.getValue().length() == 0) {
            return "."; // NOI18N
        } else {
            return buildCommandWorkingDir.getValue();
        }
    }
    
    public void setBuildCommandWorkingDir(StringConfiguration buildCommandWorkingDir) {
        this.buildCommandWorkingDir = buildCommandWorkingDir;
    }
    
    // Build Command
    public StringConfiguration getBuildCommand() {
        return buildCommand;
    }
    
    public void setBuildCommand(StringConfiguration buildCommand) {
        this.buildCommand = buildCommand;
    }
    
    // Build Command
    public StringConfiguration getCleanCommand() {
        return cleanCommand;
    }
    
    public void setCleanCommand(StringConfiguration cleanCommand) {
        this.cleanCommand = cleanCommand;
    }
    
    // Output
    public StringConfiguration getOutput() {
        return output;
    }
    
    public void setOutput(StringConfiguration output) {
        this.output = output;
    }
    
    // Extra
    public boolean canBuild() {
        return getBuildCommand().getValue().length() > 0;
    }
    
    // the "Abs" part does not make sense for file objects, 
    // but let's keep function name close to getAbsBuildCommandWorkingDir()
    public FileObject getAbsBuildCommandFileObject() {        
        String path = getAbsBuildCommandWorkingDir();
        return FileSystemProvider.getFileObject(getSourceExecutionEnvironment(), path);
    }

    public String getAbsBuildCommandWorkingDir() {
        String wd;
        if (getBuildCommandWorkingDirValue().length() > 0 && CndPathUtilities.isPathAbsolute(getBuildCommandWorkingDirValue())) {
            wd = getBuildCommandWorkingDirValue();
        } else {
            wd = getMakeConfiguration().getBaseDir() + "/" + getBuildCommandWorkingDirValue(); // NOI18N
        }
        // Normalize            
        wd = FileSystemProvider.normalizeAbsolutePath(wd, getSourceExecutionEnvironment());
        return wd;
    }
    
    public boolean canClean() {
        return getCleanCommand().getValue().length() > 0;
    }
    
    public String getAbsOutput() {
        if (getOutput().getValue().length() == 0) {
            return ""; // NOI18N
        } else if (CndPathUtilities.isPathAbsolute(getOutput().getValue())) {
            return getOutput().getValue();
        } else {
            return getMakeConfiguration().getBaseDir() + "/" + getOutput().getValue(); // NOI18N
        }
    }
    
    // Clone and assign
    public void assign(MakefileConfiguration conf) {
        // MakefileConfiguration
        //setMakeConfiguration(conf.getMakeConfiguration()); // MakeConfiguration should not be assigned
        getBuildCommandWorkingDir().assign(conf.getBuildCommandWorkingDir());
        getBuildCommand().assign(conf.getBuildCommand());
        getCleanCommand().assign(conf.getCleanCommand());
        getOutput().assign(conf.getOutput());
    }

    @Override
    public MakefileConfiguration clone() {
        MakefileConfiguration clone = new MakefileConfiguration(getMakeConfiguration());
        clone.setBuildCommandWorkingDir(getBuildCommandWorkingDir().clone());
        clone.setBuildCommand(getBuildCommand().clone());
        clone.setCleanCommand(getCleanCommand().clone());
        clone.setOutput(getOutput().clone());
        return clone;
    }

    public Sheet getSheet() {
        Sheet sheet = new Sheet();
        
        Sheet.Set set = new Sheet.Set();
        set.setName("Makefile"); // NOI18N
        set.setDisplayName(getString("MakefileTxt"));
        set.setShortDescription(getString("MakefileHint"));
        set.put(new DirStringNodeProp(getBuildCommandWorkingDir(), "WorkingDirectory", getString("WorkingDirectory_LBL"), getString("WorkingDirectory_TT"))); // NOI18N
        set.put(new StringNodeProp(getBuildCommand(), "BuildCommandLine", getString("BuildCommandLine_LBL"), getString("BuildCommandLine_TT"))); // NOI18N
        set.put(new StringNodeProp(getCleanCommand(),  "CleanCommandLine", getString("CleanCommandLine_LBL"), getString("CleanCommandLine_TT"))); // NOI18N
        set.put(new OutputStringNodeProp(getOutput(), "BuildResult", getString("BuildResult_LBL"), getString("BuildResult_TT"))); // NOI18N
        sheet.put(set);
        
        return sheet;
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
            return new DirEditor(getAbsBuildCommandWorkingDir());
        }
    }
    
    private class OutputStringNodeProp extends StringNodeProp {
        public OutputStringNodeProp(StringConfiguration stringConfiguration, String txt1, String txt2, String txt3) {
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
            String seed = getAbsOutput();
            if (seed.length() == 0) {
                seed = getMakeConfiguration().getBaseDir();
            }
            return new ElfEditor(seed);
        }
    }
    
    private class DirEditor extends PropertyEditorSupport implements ExPropertyEditor {
        private PropertyEnv propenv;
        private String seed;
        
        public DirEditor(String seed) {
            this.seed = seed;
        }
        
        @Override
        public void setAsText(String text) {
            getBuildCommandWorkingDir().setValue(text);
        }
        
        @Override
        public String getAsText() {
            return getBuildCommandWorkingDir().getValue();
        }
        
        @Override
        public Object getValue() {
            return getBuildCommandWorkingDir().getValue();
        }
        
        @Override
        public void setValue(Object v) {
            getBuildCommandWorkingDir().setValue((String)v);
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
   
    private final class ElfEditor extends PropertyEditorSupport implements ExPropertyEditor {
        private PropertyEnv propenv;
        private String seed;
        
        public ElfEditor(String seed) {
            this.seed = seed;
        }
        
        @Override
        public void setAsText(String text) {
            getOutput().setValue(text);
        }
        
        @Override
        public String getAsText() {
            return getOutput().getValue();
        }
        
        @Override
        public Object getValue() {
            return getOutput().getValue();
        }
        
        @Override
        public void setValue(Object v) {
            getOutput().setValue((String)v);
        }
        
        @Override
        public boolean supportsCustomEditor() {
            return true;
        }
        
        @Override
        public java.awt.Component getCustomEditor() {
            return createElfPanel(seed, this, propenv);
        }
        
        @Override
        public void attachEnv(PropertyEnv propenv) {
            this.propenv = propenv;
        }
    }

    private void setElfFilters(final ExecutionEnvironment execEnv, final JFileChooser chooser) {
        final List<FileFilter> filters = new ArrayList<FileFilter>();
        // to be run in EDT
        Runnable setFiltersRunner = new Runnable() {
            @Override
            public void run() {
                for (FileFilter f : filters) {
                    chooser.addChoosableFileFilter(f);
                }
                chooser.setFileFilter(chooser.getAcceptAllFileFilter());
            }
        };
        Runnable determineFiltersRunner = new Runnable() {
            @Override
            public void run() {
                try {
                    HostInfo hostInfo = HostInfoUtils.getHostInfo(execEnv);
                    chooser.addChoosableFileFilter(FileFilterFactory.getAllBinaryFileFilter());
                    if (hostInfo.getOSFamily() ==  HostInfo.OSFamily.WINDOWS) {
                        chooser.addChoosableFileFilter(FileFilterFactory.getPeExecutableFileFilter());
                        chooser.addChoosableFileFilter(FileFilterFactory.getPeStaticLibraryFileFilter());
                        chooser.addChoosableFileFilter(FileFilterFactory.getPeDynamicLibraryFileFilter());
                    } else if (hostInfo.getOSFamily() ==  HostInfo.OSFamily.MACOSX) {
                        chooser.addChoosableFileFilter(FileFilterFactory.getMacOSXExecutableFileFilter());
                        chooser.addChoosableFileFilter(FileFilterFactory.getElfStaticLibraryFileFilter());
                        chooser.addChoosableFileFilter(FileFilterFactory.getMacOSXDynamicLibraryFileFilter());
                    } else {
                        chooser.addChoosableFileFilter(FileFilterFactory.getElfExecutableFileFilter());
                        chooser.addChoosableFileFilter(FileFilterFactory.getElfStaticLibraryFileFilter());
                        chooser.addChoosableFileFilter(FileFilterFactory.getElfDynamicLibraryFileFilter());
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (CancellationException ex) {
                    // don't report CancellationException
                }
            }
        };
        RP.post(determineFiltersRunner);
    }

    private JFileChooser createElfPanel(String seed, final PropertyEditorSupport editor, PropertyEnv propenv) {
        ExecutionEnvironment execEnv = getSourceExecutionEnvironment();
        final JFileChooser chooser = RemoteFileUtil.createFileChooser(execEnv,
                "", "", JFileChooser.FILES_ONLY, null, seed, true); //NOI18N
        chooser.setControlButtonsAreShown(false);
        chooser.putClientProperty("title", chooser.getDialogTitle()); // NOI18N
        setElfFilters(execEnv, chooser);
        propenv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        propenv.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                File selectedFile = chooser.getSelectedFile();
                if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) && evt.getNewValue() == PropertyEnv.STATE_VALID && selectedFile != null) {
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
            bundle = NbBundle.getBundle(MakefileConfiguration.class);
        }
        return bundle.getString(s);
    }
}

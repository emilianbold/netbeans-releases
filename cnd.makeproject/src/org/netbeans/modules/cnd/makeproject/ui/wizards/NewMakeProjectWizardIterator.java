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

package org.netbeans.modules.cnd.makeproject.ui.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.MakeProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.BasicCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Wizard to create a new Make project.
 */
public class NewMakeProjectWizardIterator implements WizardDescriptor.InstantiatingIterator {
    private static final long serialVersionUID = 1L;
    
    static final String PROP_NAME_INDEX = "nameIndex"; // NOI18N
    
    // Wizard types
    public static final int TYPE_MAKEFILE = 0;
    public static final int TYPE_APPLICATION = 1;
    public static final int TYPE_DYNAMIC_LIB = 2;
    public static final int TYPE_STATIC_LIB = 3;
    
    private int wizardtype;
    private String name;
    private String wizardTitle;
    private String wizardACSD;
    
    public NewMakeProjectWizardIterator(int wizardtype, String name, String wizardTitle, String wizardACSD) {
        this.wizardtype = wizardtype;
	this.name = name;
	this.wizardTitle = wizardTitle;
	this.wizardACSD = wizardACSD;
    }

    public static NewMakeProjectWizardIterator newApplication() {
	String name = getString("NativeNewApplicationName"); // NOI18N
	String wizardTitle = getString("Templates/Project/Native/newApplication.xml"); // NOI18N
	String wizardACSD = getString("NativeNewLibraryACSD"); // NOI18N
        return new NewMakeProjectWizardIterator(TYPE_APPLICATION, name, wizardTitle, wizardACSD);
    }
        
    public static NewMakeProjectWizardIterator newDynamicLibrary() {
	String name = getString("NativeNewDynamicLibraryName"); // NOI18N
	String wizardTitle = getString("Templates/Project/Native/newDynamicLibrary.xml"); // NOI18N
	String wizardACSD = getString("NativeNewDynamicLibraryACSD"); // NOI18N
        return new NewMakeProjectWizardIterator(TYPE_DYNAMIC_LIB, name, wizardTitle, wizardACSD);
    }

    public static NewMakeProjectWizardIterator newStaticLibrary() {
	String name = getString("NativeNewStaticLibraryName");
	String wizardTitle = getString("Templates/Project/Native/newStaticLibrary.xml");
	String wizardACSD = getString("NativeNewStaticLibraryACSD");
        return new NewMakeProjectWizardIterator(TYPE_STATIC_LIB, name, wizardTitle, wizardACSD);
    }
    
    public static NewMakeProjectWizardIterator makefile() {
	String name = getString("NativeMakefileName"); // NOI18N
	String wizardTitle = getString("Templates/Project/Native/makefile.xml"); // NOI18N
	String wizardACSD = getString("NativeMakefileNameACSD"); // NOI18N
        return new NewMakeProjectWizardIterator(TYPE_MAKEFILE, name, wizardTitle, wizardACSD);
    }

    private WizardDescriptor.Panel[] createPanels (String name) {
	if (wizardtype == TYPE_APPLICATION || wizardtype == TYPE_DYNAMIC_LIB || wizardtype == TYPE_STATIC_LIB) {
	    return new WizardDescriptor.Panel[] {
		    new PanelConfigureProject(name, wizardtype, wizardTitle, wizardACSD, true)
		};
	}
	else if (wizardtype == TYPE_MAKEFILE) {
	    return new WizardDescriptor.Panel[] {
		    new BuildActionsDescriptorPanel(),
		    new SourceFoldersDescriptorPanel(),
		    new PanelConfigureProject(name, wizardtype, wizardTitle, wizardACSD, false),
		};
	}
	else {
	    return null; // FIXUP
	}
    }
    
    private String[] createSteps(WizardDescriptor.Panel[] panels) {
	String[] steps = new String[panels.length];
	for (int i = 0; i < panels.length; i++)
	    steps[i] = ((Name)panels[i]).getName();
	return steps;
    }
    
    
    public Set/*<FileObject>*/ instantiate () throws IOException {
        Set resultSet = new HashSet ();
        File dirF = (File)wiz.getProperty("projdir"); // NOI18N
        if (dirF != null) {
            dirF = FileUtil.normalizeFile(dirF);
        }
        String name = (String)wiz.getProperty("name"); // NOI18N
        String makefileName = (String)wiz.getProperty("makefilename"); // NOI18N
        if (wizardtype == TYPE_MAKEFILE) { // thp
	    MakeConfiguration extConf = new MakeConfiguration(dirF.getPath(), "Default", MakeConfiguration.TYPE_MAKEFILE); // NOI18N
            String workingDir = (String)wiz.getProperty("buildCommandWorkingDirTextField"); // NOI18N
            String workingDirRel = IpeUtils.toRelativePath(dirF.getPath(), FilePathAdaptor.naturalize(workingDir));
            workingDirRel = FilePathAdaptor.normalize(workingDirRel);
	    extConf.getMakefileConfiguration().getBuildCommandWorkingDir().setValue(workingDirRel);
	    extConf.getMakefileConfiguration().getBuildCommand().setValue((String)wiz.getProperty("buildCommandTextField")); // NOI18N
	    extConf.getMakefileConfiguration().getCleanCommand().setValue((String)wiz.getProperty("cleanCommandTextField")); // NOI18N
            // Build result
            String buildResult = (String)wiz.getProperty("outputTextField"); // NOI18N
            if (buildResult != null && buildResult.length() > 0) {
                buildResult = IpeUtils.toRelativePath(dirF.getPath(), FilePathAdaptor.naturalize(buildResult)); // NOI18N
                buildResult = FilePathAdaptor.normalize(buildResult);
                extConf.getMakefileConfiguration().getOutput().setValue(buildResult);
            }
            // Include directories
            String includeDirectories = (String)wiz.getProperty("includeTextField"); // NOI18N
            if (includeDirectories != null && includeDirectories.length() > 0) {
                StringTokenizer tokenizer = new StringTokenizer(includeDirectories, ";"); // NOI18N
                Vector includeDirectoriesVector = new Vector();
                while (tokenizer.hasMoreTokens()) {
                    String includeDirectory = (String)tokenizer.nextToken();
                    includeDirectory = IpeUtils.toRelativePath(dirF.getPath(), FilePathAdaptor.naturalize(includeDirectory));
                    includeDirectory = FilePathAdaptor.normalize(includeDirectory);
                    includeDirectoriesVector.add(includeDirectory);
                }
                extConf.getCCompilerConfiguration().getIncludeDirectories().setValue(includeDirectoriesVector);
                extConf.getCCCompilerConfiguration().getIncludeDirectories().setValue(includeDirectoriesVector);
            }
            // Macros
            String macros = (String)wiz.getProperty("macroTextField"); // NOI18N
            if (macros != null && macros.length() > 0) {
                StringTokenizer tokenizer = new StringTokenizer(macros, "; ");
                String macrosString = ""; // NOI18N
                while (tokenizer.hasMoreTokens()) {
                    macrosString += (String)tokenizer.nextToken();
                    if (tokenizer.hasMoreTokens())
                        macrosString += " "; // NOI18N
                }
                extConf.getCCompilerConfiguration().getPreprocessorConfiguration().setValue(macrosString);
                extConf.getCCCompilerConfiguration().getPreprocessorConfiguration().setValue(macrosString);
            }
            // Add other makefile to important files
            Iterator importantItemsIterator = null;
            String makefilePath = (String)wiz.getProperty("makefileNameTextField"); // NOI18N
            if (makefilePath != null && makefilePath.length() > 0) {
            Vector importantItems = null;
                importantItems = new Vector();
                makefilePath = IpeUtils.toRelativePath(dirF.getPath(), FilePathAdaptor.naturalize(makefilePath));
                makefilePath = FilePathAdaptor.normalize(makefilePath);
                importantItems.add(makefilePath);
                importantItemsIterator = importantItems.iterator();
            }

            MakeProjectGenerator.createProject(dirF, name, makefileName, new MakeConfiguration[] {extConf}, (Iterator)wiz.getProperty("sourceFolders"), importantItemsIterator); // NOI18N
	    FileObject dir = FileUtil.toFileObject(dirF);
	    resultSet.add (dir);
	}
        else if (wizardtype == TYPE_APPLICATION || wizardtype == TYPE_DYNAMIC_LIB || wizardtype == TYPE_STATIC_LIB) { 
	    int conftype = -1;
	    if (wizardtype == TYPE_APPLICATION)
		conftype = MakeConfiguration.TYPE_APPLICATION;
	    else if (wizardtype == TYPE_DYNAMIC_LIB)
		conftype = MakeConfiguration.TYPE_DYNAMIC_LIB;
	    else if (wizardtype == TYPE_STATIC_LIB)
		conftype = MakeConfiguration.TYPE_STATIC_LIB;
	    MakeConfiguration debug = new MakeConfiguration(dirF.getPath(), "Debug", conftype); // NOI18N
	    debug.getCCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_DEBUG);
	    debug.getCCCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_DEBUG);
	    debug.getFortranCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_DEBUG);
	    MakeConfiguration release = new MakeConfiguration(dirF.getPath(), "Release", conftype); // NOI18N
	    release.getCCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_RELEASE);
	    release.getCCCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_RELEASE);
	    release.getFortranCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_RELEASE);
	    MakeConfiguration[] confs = new MakeConfiguration[] {debug, release};
            MakeProjectGenerator.createProject(dirF, name, makefileName, confs, null, null);
	    FileObject dir = FileUtil.toFileObject(dirF);
	    resultSet.add (dir);
	}
        return resultSet;
    }

        
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels(name.replaceAll(" ", "")); // NOI18N
        // Make sure list of steps is accurate.
        String[] steps = createSteps(panels);
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent)c;
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
            }
        }
    }
    public void uninitialize(WizardDescriptor wiz) {
        this.wiz.putProperty("projdir",null); // NOI18N
        this.wiz.putProperty("name",null); // NOI18N
        this.wiz.putProperty("mainClass",null); // NOI18N
        if (wizardtype == TYPE_MAKEFILE) {
            this.wiz.putProperty("sourceRoot",null); // NOI18N
        }
        this.wiz = null;
        panels = null;
    }
    
    public String name() {
        return MessageFormat.format (NbBundle.getMessage(NewMakeProjectWizardIterator.class,"LAB_IteratorName"), // NOI18N
            new Object[] {new Integer (index + 1), new Integer (panels.length) });                                
    }
    
    public boolean hasNext() {
        return index < panels.length - 1;
    }
    public boolean hasPrevious() {
        return index > 0;
    }
    public void nextPanel() {
        if (!hasNext()) throw new NoSuchElementException();
        index++;
    }
    public void previousPanel() {
        if (!hasPrevious()) throw new NoSuchElementException();
        index--;
    }
    public WizardDescriptor.Panel current () {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}
    
    interface Name {
	public String getName();
    }

    /** Look up i18n strings here */
    private static ResourceBundle bundle;
    private static String getString(String s) {
	if (bundle == null) {
	    bundle = NbBundle.getBundle(NewMakeProjectWizardIterator.class);
	}
	return bundle.getString(s);
    }
}

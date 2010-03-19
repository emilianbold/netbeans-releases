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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.ruby.railsprojects;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.RubyUtils;
import org.netbeans.modules.ruby.platform.gems.Gem;
import org.netbeans.modules.ruby.platform.gems.GemAction;
import org.netbeans.modules.ruby.platform.gems.GemManager;
import org.netbeans.modules.ruby.railsprojects.Generator.Script;
import org.netbeans.modules.ruby.railsprojects.RailsProjectUtil.RailsVersion;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * TODO: Do some checking of the arguments. For example, the documentation says
 * to not suffix plugins with the name "Plugin", or test with "Test", so I
 * should enforce that. The docs also give some clues about what you should be
 * typing; it would be nice to include this documentation right there in the
 * dialog, or perhaps through a validator.
 * TODO: I should use the args-splitting logic from Utilities here such that
 *  the usage examples work better
 * 
 * TODO: Probably all generators don't have a corresponding destroy script, need 
 * to think of a way to check that.
 *
 * @author  Tor Norbye
 */
public class GeneratorPanel extends javax.swing.JPanel implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(GeneratorPanel.class.getName());
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private ChangeListener changeListener;
    private List<Generator> generators = new ArrayList<Generator>();
    private final Project project;
    private final Future<RailsVersion> railsVersion;

    /** Creates new form GeneratorPanel */
    public GeneratorPanel(Project project, Generator initialGenerator) {
        this.project = project;
        // might take some time due to reading files etc, so run outside of EDT
        this.railsVersion = EXECUTOR.submit(new Callable<RailsVersion>() {

            @Override
            public RailsVersion call() throws Exception {
                return RailsProjectUtil.getRailsVersion(GeneratorPanel.this.project);
            }
        });

        initComponents();
        actionTypeButtonGroup.add(generateButton);
        actionTypeButtonGroup.add(destroyButton);
        generateButton.setSelected(true);

        if (initialGenerator != Generator.NONE) {
            typeCombo.setSelectedItem(initialGenerator.getName());
        }
        showGenerator(initialGenerator); 
        typeCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String item = e.getItem().toString();

                    for (Generator generator : generators) {
                        if (generator.getName().equals(item)) {
                            showGenerator(generator);
                        }
                    }
                    
                    changeListener.stateChanged(new ChangeEvent(e));
                }
            }
        });
        nameText.requestFocus();
        nameText.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent ev) {
                if (changeListener != null) {
                    changeListener.stateChanged(new ChangeEvent(ev));
                }
            }
            public void removeUpdate(DocumentEvent ev) {
                if (changeListener != null) {
                    changeListener.stateChanged(new ChangeEvent(ev));
                }
            }
            
            public void changedUpdate(DocumentEvent ev) {
            }
        });
    }

    void setInitialState(String name, String params) {
        assert name != null;
        nameText.setText(name);
        if (params != null) {
            parameter1Text.setText(params);
        }
    }

    private RailsVersion getRailsVersion() {
        try {
            return railsVersion.get();
        } catch (InterruptedException ex) {
            LOGGER.log(Level.FINE, null, ex);
        } catch (ExecutionException ex) {
            LOGGER.log(Level.FINE, null, ex);
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
        
    private Generator getSelectedGenerator() {
        String item = typeCombo.getSelectedItem().toString();
        for (Generator generator : generators) {
            if (generator.getName().equals(item)) {
                return generator;
            }
        }
        
        return Generator.NONE;
    }
    
    void setForcing(boolean forcing) {
        overwriteRadio.setSelected(forcing);
        skipRadio.setSelected(!forcing);
    }

    void setPretend(boolean pretend) {
        pretendCB.setSelected(pretend);
    }
    
    private ComboBoxModel getTypeModel() {
        generators = findGenerators();

        List<String> generatorNames = new ArrayList<String>();
        for (Generator generator : generators) {
            if (generator == Generator.NONE) {
                continue;
            }
            generatorNames.add(generator.getName());
        }
        
        DefaultComboBoxModel model = new DefaultComboBoxModel(generatorNames.toArray());
        return model;
    }

    private FileObject getRailsHome() {
        // This method tries to replicate the logic in Rails' lookup.rb's Dir.user_home method
        // Otherwise it could have user Java's "user.home" property
        String home = System.getenv("HOME"); // NOI18N
        if (home == null) {
            home = System.getenv("USERPROFILE"); // NOI18N
        }
        if (home == null) {
            String homedrive = System.getenv("HOMEDRIVE"); // NOI18N
            String homepath = System.getenv("HOMEPATH"); // NOI18N
            if (homedrive != null && homepath != null) {
                home = homedrive + ":" + homepath; // NOI18N
            }
        }
        if (home == null) {
            // File.expand_path '~'
            try {
                File f = new File("~"); // NOI18N
                f = f.getCanonicalFile().getAbsoluteFile();
                if (f.exists()) {
                    home = f.getAbsolutePath();
                }
            } catch (IOException ioe) {
                // Don't complain, we're searching
            }
        }
        // Fallback (in case my ~ code etc. doesn't work)
        if (home == null) {
            home = System.getProperty("user.home"); // NOI18N
        }
        
        if (home != null) {
            File f = new File(home);
            if (f.exists()) {
                try {
                    f = f.getCanonicalFile();
                    return FileUtil.toFileObject(f);
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        }
        
        return null;
    }
    
    private void findGeneratorDirs(List<FileObject> generatorDirs, FileObject dir, String subdir) {
        if (!dir.isFolder()) {
            return;
        }

        FileObject[] subdirs = dir.getChildren();
        if (subdirs == null) {
            return;
        }
        
        if (subdir == null || (dir.getName().equals(subdir))) {
            for (FileObject child : subdirs) {
                if (child.isFolder()) {
                    generatorDirs.add(child);
                }
            }
        } else {
            // Keep searching
            for (FileObject child : subdirs) {
                if (child.isFolder()) {
                    findGeneratorDirs(generatorDirs, child, subdir);
                }
            }
        }
    }

    /** 
     * Scan the given root directory for generators in the given location. Only add generators
     * if there isn't already a generator of the same name in the added set. If the subdir string
     * is non null, search recursively for a dir of the given name.
     * NOTE: The relative path name will be searched by Fileobject, not File, so it should NOT
     * be using File.separator, it should ALWAYS be using "/"!
     */
    private void scan(List<Generator> generators, FileObject root, String relative, String subdir, Set<String> added) {
        FileObject dir = root.getFileObject(relative);
        if (dir == null) {
            return;
        }

        List<FileObject> generatorDirs = new ArrayList<FileObject>();
        findGeneratorDirs(generatorDirs, dir, subdir);
        for (FileObject generatorDir : generatorDirs) {
            // TODO - is it always true that the generator directory
            // in the rails project is the generator name? What about
            // version stamps?
            String name = generatorDir.getName();
            
            if (added.contains(name)) {
                continue;
            }
            
            int argsRequired = 0; // I could look at the usage files here to determine # of required arguments...
            Generator generator = new Generator(name, generatorDir, argsRequired);
            generators.add(generator);
            added.add(name);
        }
    }
    
    private List<Generator> findGenerators() {
        Set<String> added = new HashSet<String>();
        
        // Look for extra installed generators and list them here
        // The search logic for this in Rails is lib/rails_generator/lookup.rb
        // # Use component generators (model, controller, etc).
        // # 1.  Rails application.  If RAILS_ROOT is defined we know we're
        // #     generating in the context of a Rails application, so search
        // #     RAILS_ROOT/generators.
        // # 2.  User home directory.  Search ~/.rails/generators.
        // # 3.  RubyGems.  Search for gems named *_generator.
        // # 4.  Builtins.  Model, controller, mailer, scaffold.

        // 1. Look in the Rails application
        FileObject dir = project.getProjectDirectory();
        List<Generator> generators = new ArrayList<Generator>();
        // NOTE - we need to use / as the path separator, NOT File.separator here because
        // these relative path names are passed to FileObject.getFileObject() which
        // always wants /

        scan(generators, dir, "lib/generators", null, added);  // NOI18N
        scan(generators, dir, "lib/rails_generators", null, added);  // NOI18N
        scan(generators, dir, "vendor/generators", null, added); // NOI18N
        // TODO: Look recursively for a "generators" directory under vendor/plugins, e.g.
        //  RAILS_ROOT/vendor/plugins/**/generatorsi
        scan(generators, dir, "vendor/plugins", "generators", added); // NOI18N
                    
        // 2. Look in the user's home directory (as defined by Rails)
        FileObject railsHome = getRailsHome();
        if (railsHome != null) {
            // Look in ~/.rails/generators
            scan(generators, railsHome, ".rails/generators", null, added); // NOI18N
            
        }
        
        GemManager gemManager = RubyPlatform.gemManagerFor(project);
        if (gemManager != null) {
            // 3. Add in RubyGem generators
            for (File repo : gemManager.getRepositories()) {
                File gemDir = new File(repo, "gems"); // NOI18N
                if (gemDir.exists()) {
                    Set<String> gems = gemManager.getInstalledGemsFiles();
                    for (String gem : gems) {
                        if (added.contains(gem)) {
                            continue;
                        }

                        String version = gemManager.getLatestVersion(gem);
                        if (version != null) {
                            File f = new File(gemDir, gem + "-" + version); // NOI18N
                            if (f.exists()) {
                                FileObject fo = FileUtil.toFileObject(f);
                                String name = null;
                                if (gem.endsWith("_generator")) {
                                    // Chop off _generator suffix
                                    name = gem.substring(0, gem.length() - "_generator".length());
                                } else if (fo.getFileObject("generators/") != null) {
                                    name = gem;
                                } else {
                                    // not a generator
                                    continue;
                                }
                                int argsRequired = 0; // I could look at the usage files here to determine # of required arguments...
                                Generator generator = new Generator(name, fo, argsRequired);
                                generators.add(generator);
                                added.add(generator.getName());
                            }
                        }

                    }
                }
            }
        }

        // 4. Finally add in the built-in generators.
        // Rather than using a hardcoded list, I could go looking in Rails itself:
        // rails-X.Y.Z/lib/rails_generator/generators/components

        // Add in the builtins first (since they provide some more specific
        // UI configuration for known generators (labelling the arguments etc.)

        List<Generator> foundBuiltins = new ArrayList<Generator>();

        FileObject railsInstall = project.getProjectDirectory().getFileObject("vendor/rails/railties"); // NOI18N
        if (railsInstall != null) {
            scan(generators, railsInstall, 
                "lib/rails_generator/generators/components", null, added); // NOI18N
        } else if (gemManager != null) {
            for (File repo : gemManager.getRepositories()) {
                File gemDir = new File(repo, "gems"); // NOI18N
                if (!gemDir.exists()) {
                    continue;
                }
                // both rails and railties may contain generators
                String[] gemsToTry = {"rails", "railties"};
                for (String gemToTry : gemsToTry) {
                    String path = gemToTry + "-" + getRailsVersion().asString();
                    File railsDir = new File(gemDir, path); // NOI18N
                    if (!railsDir.exists()) {
                        continue;
                    }
                    railsInstall = FileUtil.toFileObject(railsDir);
                    scan(foundBuiltins, railsInstall,
                            "lib/rails_generator/generators/components", null, added); // NOI18N
                    scan(foundBuiltins, railsInstall,
                            "lib//generators/rails", null, added); // NOI18N
                }
            }
        }

        List<Generator> builtins = Generator.getBuiltinGenerators(getRailsVersion().asString(), foundBuiltins);
        for (Generator builtin : builtins) {
            add(builtin, generators);
        }

        Collections.sort(generators, new Comparator<Generator>() {

            @Override
            public int compare(Generator o1, Generator o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });


        return generators;
    }
    
    private static boolean add(Generator toAdd, List<Generator> result) {
        for (Generator each : result) {
            if (each.getName().equals(toAdd.getName())) {
                return false;
            }
        }
        return result.add(toAdd);
        
    }

    public String getGeneratedName() {
        return nameText.getText().trim();
    }
    
    public String getType() {
        Object o = typeCombo.getSelectedItem();

        return o != null ? o.toString() : "";
    }
    
    Script getScript() {
        String action = destroyButton.isSelected() ? "destroy" : "generate"; //NOI18N
        if (!getRailsVersion().isRails3OrHigher()) {
            return new Script(action);
        }
        // in rails 3 there is just the 'rails' script; usage is
        // e.g. 'rails generate scaffold ...'
        return new Script("rails").addArgs(action);
    }
    
    public boolean isForce() {
        return overwriteRadio.isSelected();
    }

    public boolean isPretend() {
        return pretendCB.isSelected();
    }

    private void showGenerator(Generator generator) {
        setOptions(generator.getNameLabel(), generator.getArg1Label(), generator.getArg2Label());
        showUsage(generator);
    }
    
    private void showUsage(Generator generator) {
        // Look up the Rails directory and read the USAGE file, then stick
        // it into the usageText.
        String usage = generator.getUsage(project);

        if (usage != null) {
            usageText.setText(usage);
            usageText.getCaret().setDot(0);
        } else {
            usageText.setText("");
        }
    }
    
    private void setOptions(String name, String firstParameter, String secondParameter) {
        nameLabel.setText(name);
        nameLabel.setLabelFor(nameText);
        Mnemonics.setLocalizedText(nameLabel, name);

        boolean visible = firstParameter != null;
        parameter1Label.setVisible(visible);
        parameter1Text.setVisible(visible);
        if (visible) {
            Mnemonics.setLocalizedText(parameter1Label, firstParameter);
            parameter1Label.setLabelFor(parameter1Text);
        }

        visible = secondParameter != null;
        parameter2Label.setVisible(visible);
        parameter2Text.setVisible(visible);
        if (visible) {
            Mnemonics.setLocalizedText(parameter2Label, secondParameter);
            parameter2Label.setLabelFor(parameter2Text);
        }
        
        invalidate();
        revalidate();
        repaint();
    }
    
    public String[] getFirstParameterList() {
        if (parameter1Text.isVisible()) {
            // Change commas to spaces since lists are space separated, not comma separated
            return parameter1Text.getText().replace(',', ' ').replace("  ", " ").trim().split(" "); // NOI18N
        } else {
            return null;
        }
    }
    
    public String[] getSecondParameterList() {
        if (parameter2Text.isVisible()) {
            return parameter2Text.getText().replace(',', ' ').replace("  ", " ").trim().split(" "); // NOI18N
        } else {
            return null;
        }
    }

    public void setChangeListener (ChangeListener l) {
        changeListener = l; 
    }      
    
    public boolean isDataValid() {
        Generator generator = getSelectedGenerator();
        if (generator == Generator.NONE) {
            return false;
        }

        String name = getGeneratedName();
        if (name.length() > 0 || generator.getArgsRequired() < 1) {
            return true;
        }

        String msg = RubyUtils.getIdentifierWarning(name, 0);
        if (msg != null) {
            //wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, // NOI18N
            //        msg);
            // warning only, don't return false
        }
        
        
        // TODO - if getArgsRequired > 0, validate on the additional parameter fields
        
        return false;
    }
    
    public void run() {
        // Refresh generator list
        RubyPlatform.platformFor(project).recomputeRoots();
        typeCombo.setModel(getTypeModel());
        
        typeCombo.invalidate();
        typeCombo.repaint();

        Generator generator = getSelectedGenerator();
        if (generator != Generator.NONE) {
            showGenerator(generator);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        forceGroup = new javax.swing.ButtonGroup();
        actionTypeButtonGroup = new javax.swing.ButtonGroup();
        generateLabel = new javax.swing.JLabel();
        pretendCB = new javax.swing.JCheckBox();
        typeCombo = new javax.swing.JComboBox();
        skipRadio = new javax.swing.JRadioButton();
        overwriteRadio = new javax.swing.JRadioButton();
        optionsPanel = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        nameText = new javax.swing.JTextField();
        parameter1Label = new javax.swing.JLabel();
        parameter1Text = new javax.swing.JTextField();
        parameter2Label = new javax.swing.JLabel();
        parameter2Text = new javax.swing.JTextField();
        forceLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        usageText = new javax.swing.JTextArea();
        jSeparator2 = new javax.swing.JSeparator();
        installGeneratorsButton = new javax.swing.JButton();
        generateButton = new javax.swing.JRadioButton();
        destroyButton = new javax.swing.JRadioButton();

        FormListener formListener = new FormListener();

        generateLabel.setLabelFor(typeCombo);
        org.openide.awt.Mnemonics.setLocalizedText(generateLabel, org.openide.util.NbBundle.getMessage(GeneratorPanel.class, "GeneratorPanel.generateLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(pretendCB, org.openide.util.NbBundle.getMessage(GeneratorPanel.class, "GeneratorPanel.pretendCB.text")); // NOI18N

        typeCombo.setMaximumRowCount(14);
        typeCombo.setModel(getTypeModel());

        forceGroup.add(skipRadio);
        skipRadio.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(skipRadio, org.openide.util.NbBundle.getMessage(GeneratorPanel.class, "GeneratorPanel.skipRadio.text")); // NOI18N

        forceGroup.add(overwriteRadio);
        org.openide.awt.Mnemonics.setLocalizedText(overwriteRadio, org.openide.util.NbBundle.getMessage(GeneratorPanel.class, "GeneratorPanel.overwriteRadio.text")); // NOI18N

        optionsPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, NbBundle.getMessage(GeneratorPanel.class, "Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 11);
        optionsPanel.add(nameLabel, gridBagConstraints);
        nameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GeneratorPanel.class, "AD_NameLabel")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        optionsPanel.add(nameText, gridBagConstraints);
        nameText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GeneratorPanel.class, "AD_NameText")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 11);
        optionsPanel.add(parameter1Label, gridBagConstraints);
        parameter1Label.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GeneratorPanel.class, "AD_Parameter1Label")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        optionsPanel.add(parameter1Text, gridBagConstraints);
        parameter1Text.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GeneratorPanel.class, "AD_Parameter1Text")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 11);
        optionsPanel.add(parameter2Label, gridBagConstraints);
        parameter2Label.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GeneratorPanel.class, "AD_Parameter2Label")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        optionsPanel.add(parameter2Text, gridBagConstraints);
        parameter2Text.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GeneratorPanel.class, "AD_Parameter2Text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(forceLabel, org.openide.util.NbBundle.getMessage(GeneratorPanel.class, "GeneratorPanel.forceLabel.text")); // NOI18N

        usageText.setColumns(20);
        usageText.setEditable(false);
        usageText.setRows(5);
        jScrollPane1.setViewportView(usageText);
        usageText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GeneratorPanel.class, "AD_UsageText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(installGeneratorsButton, org.openide.util.NbBundle.getMessage(GeneratorPanel.class, "GeneratorPanel.installGeneratorsButton.text")); // NOI18N
        installGeneratorsButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(generateButton, org.openide.util.NbBundle.getMessage(GeneratorPanel.class, "GeneratorPanel.generateButton.text_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(destroyButton, org.openide.util.NbBundle.getMessage(GeneratorPanel.class, "GeneratorPanel.destroyButton.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 644, Short.MAX_VALUE)
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 644, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(forceLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(skipRadio)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(overwriteRadio))
                    .add(optionsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 644, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, pretendCB)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(generateLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(typeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(generateButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(destroyButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 219, Short.MAX_VALUE)
                        .add(installGeneratorsButton))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 644, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(generateLabel)
                    .add(typeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(installGeneratorsButton)
                    .add(generateButton)
                    .add(destroyButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(optionsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 92, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(forceLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(skipRadio)
                    .add(overwriteRadio))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pretendCB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
                .addContainerGap())
        );

        generateLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GeneratorPanel.class, "AD_GenerateLabel")); // NOI18N
        pretendCB.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GeneratorPanel.class, "AD_PretendCB")); // NOI18N
        typeCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GeneratorPanel.class, "AD_TypeCombo")); // NOI18N
        skipRadio.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GeneratorPanel.class, "AD_SkipRadio")); // NOI18N
        overwriteRadio.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GeneratorPanel.class, "AD_OverwriteRadio")); // NOI18N
        forceLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GeneratorPanel.class, "AD_ForceLabel")); // NOI18N
        installGeneratorsButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GeneratorPanel.class, "AD_InstallGeneratorsButton")); // NOI18N
        generateButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GeneratorPanel.class, "GeneratorPanel.generateButton.AccessibleContext.accessibleDescription")); // NOI18N
        destroyButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GeneratorPanel.class, "GeneratorPanel.destroyButton.AccessibleContext.accessibleDescription")); // NOI18N
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == installGeneratorsButton) {
                GeneratorPanel.this.installGeneratorsButtonActionPerformed(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

private void installGeneratorsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_installGeneratorsButtonActionPerformed
    // Bring up remote gem installer with a "generator" filter
    boolean changed = GemAction.showGemManager(RubyPlatform.platformFor(project), "generator$"); // NOI18N
    if (changed) {
        run();
    }
}//GEN-LAST:event_installGeneratorsButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup actionTypeButtonGroup;
    private javax.swing.JRadioButton destroyButton;
    private javax.swing.ButtonGroup forceGroup;
    private javax.swing.JLabel forceLabel;
    private javax.swing.JRadioButton generateButton;
    private javax.swing.JLabel generateLabel;
    private javax.swing.JButton installGeneratorsButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameText;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JRadioButton overwriteRadio;
    private javax.swing.JLabel parameter1Label;
    private javax.swing.JTextField parameter1Text;
    private javax.swing.JLabel parameter2Label;
    private javax.swing.JTextField parameter2Text;
    private javax.swing.JCheckBox pretendCB;
    private javax.swing.JRadioButton skipRadio;
    private javax.swing.JComboBox typeCombo;
    private javax.swing.JTextArea usageText;
    // End of variables declaration//GEN-END:variables
    
}

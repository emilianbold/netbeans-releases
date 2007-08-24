/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.ruby.railsprojects;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.modules.ruby.RubyUtils;
import org.netbeans.modules.ruby.rubyproject.gems.GemAction;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * TODO: Do some checking of the arguments. For example, the documentation says
 * to not suffix plugins with the name "Plugin", or test with "Test", so I
 * should enforce that. The docs also give some clues about what you should be
 * typing; it would be nice to include this documentation right there in the
 * dialog, or perhaps through a validator.
 * TODO: I should use the args-splitting logic from Utilities here such that
 *  the usage examples work better
 *
 * @author  Tor Norbye
 */
public class GeneratorPanel extends javax.swing.JPanel implements Runnable {
    private ChangeListener changeListener;
    private List<Generator> generators = new ArrayList<Generator>();
    private Project project;

    /** Creates new form GeneratorPanel */
    public GeneratorPanel(Project project, Generator initialGenerator) {
        this.project = project;
        initComponents();
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
        
        // 3. Add in RubyGem generators
        File gemDir = new File(RubyInstallation.getInstance().getRubyLibGemDir() + File.separator + "gems"); // NOI18N
        if (gemDir.exists()) {
            Set<String> gems = RubyInstallation.getInstance().getInstalledGems();
            for (String gem : gems) {
                if (added.contains(gem)) {
                    continue;
                }

                if (gem.endsWith("_generator")) { // NOI18N
                    String version = RubyInstallation.getInstance().getVersion(gem);
                    if (version != null) {
                        File f = new File(gemDir, gem + "-" + version); // NOI18N
                        if (f.exists()) {
                            FileObject fo = FileUtil.toFileObject(f);
                            // The generator is named "gem"
                            int argsRequired = 0; // I could look at the usage files here to determine # of required arguments...
                            // Chop off _generator suffix
                            String name = gem.substring(0, gem.length()-"_generator".length()); // NOI18N
                            Generator generator = new Generator(name, fo, argsRequired);
                            generators.add(generator);
                            added.add(generator.getName());
                        }
                    }
                    
                }
            }
        } else {
            gemDir = null;
        }

        // 4. Finally add in the built-in generators.
        // Rather than using a hardcoded list, I could go looking in Rails itself:
        // rails-X.Y.Z/lib/rails_generator/generators/components

        // Add in the builtins first (since they provide some more specific
        // UI configuration for known generators (labelling the arguments etc.)
        List<Generator> builtins = Generator.getBuiltinGenerators();
        for (Generator builtin : builtins) {
            if (!added.contains(builtin.getName())) {
                generators.add(builtin);
                added.add(builtin.getName());
            }
        }

        if (gemDir != null) {
            String version = RubyInstallation.getInstance().getVersion("rails"); // NOI18N
            if (version != null) {
                File railsDir = new File(gemDir, "rails" + "-" + version); // NOI18N
                assert railsDir.exists();
                FileObject railsInstall = FileUtil.toFileObject(railsDir);
                assert railsInstall != null;
                scan(generators, railsInstall, 
                    "lib/rails_generator/generators/components", null, added); // NOI18N
            }
        } else if (!Utilities.isWindows()) {
            // On some Linux distros the Rails distribution is quite different
            FileObject railsInstall = project.getProjectDirectory().getFileObject("vendor/rails/railties"); // NOI18N
            if (railsInstall != null) {
                scan(generators, railsInstall, 
                    "lib/rails_generator/generators/components", null, added); // NOI18N
            }            
        }

        return generators;
    }
    
    public String getGeneratedName() {
        return nameText.getText().trim();
    }
    
    public String getType() {
        Object o = typeCombo.getSelectedItem();

        return o != null ? o.toString() : "";
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
        String usage = generator.getUsage();

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
    
    public boolean isValid() {
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
            //wizardDescriptor.putProperty("WizardPanel_errorMessage", // NOI18N
            //        msg);
            // warning only, don't return false
        }
        
        
        // TODO - if getArgsRequired > 0, validate on the additional parameter fields
        
        return false;
    }
    
    public void run() {
        // Refresh generator list
        RubyInstallation.getInstance().recomputeRoots();
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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        forceGroup = new javax.swing.ButtonGroup();
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

        FormListener formListener = new FormListener();

        generateLabel.setLabelFor(typeCombo);
        org.openide.awt.Mnemonics.setLocalizedText(generateLabel, org.openide.util.NbBundle.getMessage(GeneratorPanel.class, "GeneratorPanel.generateLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(pretendCB, org.openide.util.NbBundle.getMessage(GeneratorPanel.class, "GeneratorPanel.pretendCB.text")); // NOI18N
        pretendCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        typeCombo.setMaximumRowCount(14);
        typeCombo.setModel(getTypeModel());

        forceGroup.add(skipRadio);
        skipRadio.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(skipRadio, org.openide.util.NbBundle.getMessage(GeneratorPanel.class, "GeneratorPanel.skipRadio.text")); // NOI18N
        skipRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        forceGroup.add(overwriteRadio);
        org.openide.awt.Mnemonics.setLocalizedText(overwriteRadio, org.openide.util.NbBundle.getMessage(GeneratorPanel.class, "GeneratorPanel.overwriteRadio.text")); // NOI18N
        overwriteRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        optionsPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, NbBundle.getMessage(GeneratorPanel.class, "Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 11);
        optionsPanel.add(nameLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        optionsPanel.add(nameText, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 11);
        optionsPanel.add(parameter1Label, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        optionsPanel.add(parameter1Text, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 11);
        optionsPanel.add(parameter2Label, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        optionsPanel.add(parameter2Text, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(forceLabel, org.openide.util.NbBundle.getMessage(GeneratorPanel.class, "GeneratorPanel.forceLabel.text")); // NOI18N

        usageText.setColumns(20);
        usageText.setEditable(false);
        usageText.setRows(5);
        jScrollPane1.setViewportView(usageText);

        org.openide.awt.Mnemonics.setLocalizedText(installGeneratorsButton, org.openide.util.NbBundle.getMessage(GeneratorPanel.class, "GeneratorPanel.installGeneratorsButton.text")); // NOI18N
        installGeneratorsButton.addActionListener(formListener);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 615, Short.MAX_VALUE)
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 615, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(forceLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(skipRadio)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(overwriteRadio))
                    .add(optionsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 615, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, pretendCB)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(generateLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(typeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 383, Short.MAX_VALUE)
                        .add(installGeneratorsButton))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 615, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(generateLabel)
                    .add(typeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(installGeneratorsButton))
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
    GemAction.showGemManager("generator$"); // NOI18N
}//GEN-LAST:event_installGeneratorsButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup forceGroup;
    private javax.swing.JLabel forceLabel;
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

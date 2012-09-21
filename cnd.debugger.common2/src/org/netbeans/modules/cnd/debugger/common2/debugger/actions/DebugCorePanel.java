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

package org.netbeans.modules.cnd.debugger.common2.debugger.actions;

import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import java.awt.event.ActionListener;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.Exceptions;

import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.project.Project;

import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;
import org.netbeans.modules.cnd.debugger.common2.utils.CorefileFilter;
import org.netbeans.modules.cnd.debugger.common2.utils.masterdetail.RecordListListener;
import org.netbeans.modules.cnd.debugger.common2.utils.masterdetail.RecordListEvent;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineCapability;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineDescriptor;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineType;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineTypeManager;

import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Host;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.CustomizableHostList;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.HostListEditor;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.CndRemote;
import java.awt.event.ItemEvent;
import java.util.Collection;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.ExecutableProjectPanel.ProjectCBItem;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.utils.FileFilterFactory;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.api.ui.FileChooserBuilder;
import org.openide.util.RequestProcessor;

/**
 * Chooser for corefile, executable and project.
 *
 * SHOULD factor with very identical code in ExecutableProjectPanel!
 */

final class DebugCorePanel extends javax.swing.JPanel {
    private DocumentListener corefileValidateListener = null;
    private DocumentListener executableValidateListener = null;
    private JButton actionButton = null;
    private String autoString = null;
    private boolean readonly;
    private boolean noproject;

    private static Project lastSelectedProject = null;

    private final RequestProcessor RP = new RequestProcessor();

    public DebugCorePanel(String corePath, String[] exePaths, JButton actionButton, boolean readonly, String host) {
	this.actionButton = actionButton;
	this.readonly = readonly;
	initialize(corePath, exePaths, host);
    }

    private void initialize(String corePath, String[] exePaths, String host) {
        initComponents();
	if (readonly) {
	    corefileTextField.setEditable(false);
	    corefileBrowseButton.setEnabled(false);
	    guidanceTextArea.setText(Catalog.get("LOADCORE_GUIDANCETEXT2")); // NOI18N
	    Catalog.setAccessibleDescription(guidanceTextArea, 
		"LOADCORE_GUIDANCETEXT2"); // NOI18N
	}
	errorLabel.setForeground(javax.swing.UIManager.getColor("nb.errorForeground")); // NOI18N
	if (corePath != null)
	    corefileTextField.setText(corePath);
	initGui();
        guidanceTextArea.setBackground(getBackground());
	setPreferredSize(new java.awt.Dimension(700, (int)getPreferredSize().getHeight()));

	corefileValidateListener = new CorefileValidateListener();
	executableValidateListener = new ExecutableValidateListener();
	corefileTextField.getDocument().addDocumentListener(corefileValidateListener);

	executableComboBox.setModel(new DefaultComboBoxModel(exePaths));
	((JTextField)executableComboBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(executableValidateListener);

        if (host != null) {
            lastHostChoice = host;
        }
	initRemoteHost();
	initEngine();
	lastHostChoice = null;
	adjustAutoCore();

        validateAll();

        projectComboBox.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                if (evt.getStateChange() == ItemEvent.SELECTED) {
                    projectChanged();
                }
            }
        });
    }

    // TODO: copied from ExecutableProjectPanel
    private void projectChanged() {
        validateAll();
    }

    private void validateProject() {
        // Validate that project toolchain family is the same as debugger type
        Project selectedProject = getSelectedProject();
        if (selectedProject != null) {
            final MakeConfiguration conf = ConfigurationSupport.getProjectActiveConfiguration(selectedProject);
            if (conf != null) {
                EngineType projectDebuggerType = NativeDebuggerManager.debuggerType(conf);
                if (getEngine() != projectDebuggerType) {
                    setError("ERROR_WRONG_FAMILY", false); // NOI18N
                }
            }
        }
    }

    public EngineType getEngine() {
        Object selected = engineComboBox.getSelectedItem();
        Collection<EngineType> engineTypes = EngineTypeManager.getEngineTypes(false);
        for (EngineType engineType : engineTypes) {
            if (engineType.getDisplayName().equals(selected)) {
                return engineType;
            }
        }
        assert false : "selected object doesn't have associated engine type " + selected;
        return null;
    }

    private int lookupAutoEntry() {
	int count = executableComboBox.getItemCount();
	for (int i = 0; i < count; i++) {
	    if (((String)executableComboBox.getItemAt(i)).equals(Catalog.get("AutoCoreExe"))) // NOI18N
		return i;
	}
	return -1;
    }

    /**
     *
     * Add or remove a "Choose from corefile" item to the executableComboBox.
     */
    private void adjustAutoCore() {
	String exec = getExecutablePath();
        EngineDescriptor descriptor = new EngineDescriptor(getEngine());
	if (!descriptor.hasCapability(EngineCapability.DERIVE_EXECUTABLE)) {
            autoString = null;
	    int i = lookupAutoEntry(); // look up <from core> item
	    if (executableComboBox.getItemCount() > 0 && i != -1) {
		executableComboBox.removeItemAt(i); // remove <from core> item
//		executableComboBox.insertItemAt(" ", i);
	    }

	    if (exec.equals(Catalog.get("AutoCoreExe"))) { // NOI18N
                exec = "";
            }
	} else {
            autoString = Catalog.get("AutoCoreExe"); // NOI18N
	    int i = lookupAutoEntry(); // look up <from core> item
	    if (i == -1) {
		executableComboBox.addItem(Catalog.get("AutoCoreExe")); // NOI18N
	    }
	    if (exec.isEmpty()) {
                exec = autoString;
            }
        }
        setExecutable(exec);
    }

    private void setExecutable(String exec) {
        ((JTextField)executableComboBox.getEditor().getEditorComponent()).setText(exec);
    }

    private void engineComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
	adjustAutoCore();
	// update validity status
        validateAll();
    }

    private void initEngine() {
        ActionListener engineComboBoxActionListener = engineComboBox.getActionListeners()[0];
        engineComboBox.removeActionListener(engineComboBoxActionListener);
        engineComboBox.removeAllItems();
        Collection<EngineType> engineTypes = EngineTypeManager.getEngineTypes(false);
        for (EngineType engineType : engineTypes) {
            engineComboBox.addItem(engineType.getDisplayName());
        }
        engineComboBox.addActionListener(engineComboBoxActionListener);
    }

    private void initRemoteHost() {
        updateRemoteHostList();

	if (NativeDebuggerManager.isStandalone()) {
	    CustomizableHostList hostlist = NativeDebuggerManager.get().getHostList();

	    // listen to host host list model
	    if (hostlist != null) {
		hostlist.addRecordListListener(new RecordListListener() {
		    public void contentsChanged(RecordListEvent e) {
			if (e.getHostName() != null)
			    lastHostChoice = e.getHostName();
			updateRemoteHostList();
			// setDirty(true);
		    }
		} );
	    }
	} else {
	}

        // Listen to hostComboBox events
        // but only after we've initialized all the above.
        hostComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hostComboBoxActionPerformed(evt);
            }
        });
    }

    private void hostComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
	String ac = evt.getActionCommand();
	if ((ac != null) && ac.equals("comboBoxChanged")) { // NOI18N
	    JComboBox cb = (JComboBox)evt.getSource();
	    if (cb != null && cb.getItemCount() > 0) {
		String hostName = getHostName();
                CndRemote.validate(hostName, new Runnable() {
                    public void run() {
                        whenHostUpdated();
                    }
                });
	    }
	}
    }

    private void whenHostUpdated() {
	// update validity status
	validateAll();
    }

    private void validateAll() {
        final String corePath = getCorefilePath();
        final String execPath = getExecutablePath();
        RP.post(new Runnable() {
            public void run() {
                clearError();
                if (validateCorefilePath(corePath)) {
                    if (validateExecutablePath(execPath)) {
                        validateProject();
                    }
                }
            }
        });
    }


    /**
     * Refresh hostComboBox with new remote host list.
     */
    private void updateRemoteHostList() {
        AttachPanel.fillHostsCombo(hostComboBox);
        // current value
        AttachPanel.setHostChoice(lastHostChoice, hostComboBox);
    }

    public String getCorefilePath() {
	return corefileTextField.getText();
    }

    /**
     * Return hostComboBox's current selection.
     * May return null if the current selection is not in the remote host DB.
     */
    public String getHostName() {
        return hostComboBox.getSelectedItem().toString();
    }

    public String getExecutablePath() {
	return ((JTextField)executableComboBox.getEditor().getEditorComponent()).getText();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

	Catalog.setAccessibleDescription(this, "ACSD_DebugCoreFile"); // NOI18N

        java.awt.GridBagConstraints gridBagConstraints;

        guidanceTextArea = new javax.swing.JTextArea();
        corefileLabel = new javax.swing.JLabel();
        corefileTextField = new javax.swing.JTextField();
        corefileBrowseButton = new javax.swing.JButton();

        executableLabel = new javax.swing.JLabel();
        executableComboBox = new javax.swing.JComboBox();
        executableBrowseButton = new javax.swing.JButton();

        projectLabel = new javax.swing.JLabel();
        projectComboBox = new javax.swing.JComboBox();

        engineLabel = new javax.swing.JLabel();
        engineComboBox = new javax.swing.JComboBox();

        hostComboBox = new javax.swing.JComboBox();
        hostLabel = new javax.swing.JLabel();
        hostsButton = new javax.swing.JButton();


        fill = new javax.swing.JPanel();
        errorLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

	int gridy = 0;

	Catalog.setAccessibleName(guidanceTextArea, "ACSN_Guidance"); // NOI18N
        guidanceTextArea.setEditable(false);
        guidanceTextArea.setLineWrap(true);
        guidanceTextArea.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/common2/debugger/actions/Bundle").getString("LOADCORE_GUIDANCETEXT1")); // NOI18N
	Catalog.setAccessibleDescription(guidanceTextArea, "LOADCORE_GUIDANCETEXT1"); // NOI18N
        guidanceTextArea.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = gridy++;
        //gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 12);
        add(guidanceTextArea, gridBagConstraints);

        hostLabel.setText(Catalog.get("HOST_LBL")); //NOI18N
        hostLabel.setLabelFor(hostComboBox);
	hostLabel.setDisplayedMnemonic(
	           Catalog.getMnemonic("MNEM_Host")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 13, 8, 0);
	add(hostLabel, gridBagConstraints);

        hostComboBox.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 8, 0);
	add(hostComboBox, gridBagConstraints);

	if (!NativeDebuggerManager.isStandalone())
	    hostsButton.setEnabled(false);      // IZ 147543

        hostsButton.setText(Catalog.get("TITLE_Hosts")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = gridy++;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 8, 12);
	add(hostsButton, gridBagConstraints);

        hostsButton.setMnemonic(Catalog.
                getMnemonic("MNEM_Hosts")); // NOI18N
        Catalog.setAccessibleDescription(hostsButton, 
                "ACSD_EditHosts"); // NOI18N

        hostsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hostsButtonActionPerformed(evt);
            }
        });

        engineLabel.setLabelFor(engineComboBox);
        engineLabel.setDisplayedMnemonic(Catalog.
            getMnemonic("MNEM_Engine")); // NOI18N
        Catalog.setAccessibleDescription(engineComboBox,
            "ACSD_Engine"); // NOI18N
        engineLabel.setText(Catalog.get("ASSOCIATED_ENGINE_LBL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 8, 0);
	/* CR 7013811
        String engine = System.getProperty("debug.engine");
        if (engine != null && engine.equals("on")) // NOI18N
	 *
	 */
	add(engineLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = gridy++;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 6, 12);
        // CR 7013811 if (engine != null && engine.equals("on")) // NOI18N
	add(engineComboBox, gridBagConstraints);

        engineComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                engineComboBoxActionPerformed(evt);
            }
        });

        corefileLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/common2/debugger/actions/Bundle").getString("LOADCORE_COREFILE_MN").charAt(0));
        corefileLabel.setLabelFor(corefileTextField);
        corefileLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/common2/debugger/actions/Bundle").getString("LOADCORE_COREFILE_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = gridy;
        //gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 8, 0);
        add(corefileLabel, gridBagConstraints);

	Catalog.setAccessibleDescription(corefileTextField,
	    "ACSD_Corefile"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = gridy;
        //gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 8, 0);
        add(corefileTextField, gridBagConstraints);

	Catalog.setAccessibleDescription(corefileBrowseButton, 
	    "ACSD_CorefileBrowse"); // NOI18N
        corefileBrowseButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/common2/debugger/actions/Bundle").getString("LOADCORE_COREFILEBROWSE_BUTTON_MN").charAt(0));
        corefileBrowseButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/common2/debugger/actions/Bundle").getString("BROWSE_BUTTON_TXT"));
        corefileBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                corefileBrowseButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = gridy++;
        //gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 8, 12);
        add(corefileBrowseButton, gridBagConstraints);

        executableLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/common2/debugger/actions/Bundle").getString("EXECUTABLE_MN").charAt(0));
        executableLabel.setLabelFor(executableComboBox);
        executableLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/common2/debugger/actions/Bundle").getString("EXECUTABLE_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = gridy;
        //gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 13, 8, 0);
        add(executableLabel, gridBagConstraints);

        executableComboBox.setEditable(true);
        executableComboBox.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/common2/debugger/actions/Bundle").getString("ProgramPathname"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = gridy;
        //gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 8, 0);
        add(executableComboBox, gridBagConstraints);

	Catalog.setAccessibleDescription(executableBrowseButton, 
	    "ACSD_ExecutableBrowse"); // NOI18N
        executableBrowseButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/common2/debugger/actions/Bundle").getString("EXECUTABLEBROWSE_BUTTON_MN").charAt(0));
        executableBrowseButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/common2/debugger/actions/Bundle").getString("BROWSE_BUTTON_TXT"));
        executableBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                executableBrowseButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = gridy++;
        //gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 8, 12);
        add(executableBrowseButton, gridBagConstraints);

        projectLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/common2/debugger/actions/Bundle").getString("ASSOCIATED_PROJECT_MN").charAt(0));
        projectLabel.setLabelFor(projectComboBox);
	Catalog.setAccessibleDescription(projectComboBox,
	    "ACSD_Project"); // NOI18N
        projectLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/common2/debugger/actions/Bundle").getString("ASSOCIATED_PROJECT_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = gridy;
        //gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 6, 0);
	if (!NativeDebuggerManager.isStandalone())
	    add(projectLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = gridy++;
        //gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 6, 12);
	if (!NativeDebuggerManager.isStandalone())
	    add(projectComboBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = gridy++;
        //gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(fill, gridBagConstraints);

        errorLabel.setText(" "); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = gridy++;
        //gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
        add(errorLabel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void executableBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_executableBrowseButtonActionPerformed
        final String hostname = (String)hostComboBox.getSelectedItem();
        String startFolder = getExecutablePath();
        if (startFolder.isEmpty()) {
            startFolder = getCorefilePath();
            if (startFolder.isEmpty()) {
                startFolder = System.getProperty("user.home");
            }
        }
        if (startFolder.equals(autoString)) {
	    startFolder = getCorefilePath();
        }
        final String startF = startFolder;
        RP.post(new Runnable() {
            public void run() {
                try {
                    Host host = Host.byName(hostname);
                    final ExecutionEnvironment exEnv = host.executionEnvironment();
                    ConnectionManager.getInstance().connectTo(exEnv);

                    FileChooserBuilder fcb = new FileChooserBuilder(exEnv);
                    final JFileChooser fileChooser = fcb.createFileChooser(startF);
                    fileChooser.setDialogTitle(getString("SelectExecutable"));
                    fileChooser.setApproveButtonText(getString("CHOOSER_BUTTON"));
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    fileChooser.addChoosableFileFilter(FileFilterFactory.getElfExecutableFileFilter());
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            int ret = fileChooser.showOpenDialog(DebugCorePanel.this);
                            if (ret == JFileChooser.CANCEL_OPTION) {
                                return;
                            }
                            ((JTextField)executableComboBox.getEditor().getEditorComponent()).setText(fileChooser.getSelectedFile().getPath());
                        }
                    });
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (CancellationException ex) {
                    // do nothing
                }
            }
        });
    }//GEN-LAST:event_executableBrowseButtonActionPerformed

    private void hostsButtonActionPerformed(java.awt.event.ActionEvent evt) {
        // It's effect will come back to us via
        // contentsChanged(RecordListEvent)
        HostListEditor editor = new HostListEditor();
        editor.showDialog(this);
    }

    private void corefileBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_corefileBrowseButtonActionPerformed
        final String hostname = (String)hostComboBox.getSelectedItem();
        String startFolder = getCorefilePath();
        if (startFolder.isEmpty()) {
            startFolder = System.getProperty("user.home");
        }
        final String startF = startFolder;
        RP.post(new Runnable() {
            public void run() {
                try {
                    Host host = Host.byName(hostname);
                    final ExecutionEnvironment exEnv = host.executionEnvironment();
                    ConnectionManager.getInstance().connectTo(exEnv);

                    FileChooserBuilder fcb = new FileChooserBuilder(exEnv);
                    final JFileChooser fileChooser = fcb.createFileChooser(startF);
                    fileChooser.setDialogTitle(getString("CorefileChooser"));
                    fileChooser.setApproveButtonText(getString("CHOOSER_BUTTON"));
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    fileChooser.addChoosableFileFilter(FileFilterFactory.getCoreFileFilter());
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            int ret = fileChooser.showOpenDialog(DebugCorePanel.this);
                            if (ret == JFileChooser.CANCEL_OPTION) {
                                return;
                            }
                            corefileTextField.setText(fileChooser.getSelectedFile().getPath());
                        }
                    });
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (CancellationException ex) {
                    // do nothing
                }
            }
        });
    }//GEN-LAST:event_corefileBrowseButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton corefileBrowseButton;
    private javax.swing.JLabel corefileLabel;
    private javax.swing.JTextField corefileTextField;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JButton executableBrowseButton;
    private javax.swing.JComboBox executableComboBox;
    private javax.swing.JLabel executableLabel;
    private javax.swing.JPanel fill;
    private javax.swing.JTextArea guidanceTextArea;
    private javax.swing.JComboBox projectComboBox;
    private javax.swing.JLabel projectLabel;

    private javax.swing.JComboBox engineComboBox;
    private javax.swing.JLabel engineLabel;

    private static String lastHostChoice;
    private javax.swing.JComboBox hostComboBox;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JButton hostsButton;


    // End of variables declaration//GEN-END:variables

    private void initGui() {
	projectComboBox.removeAllItems();
        // fake items
	projectComboBox.addItem(getString("NO_PROJECT")); // always first
	projectComboBox.addItem(getString("NEW_PROJECT")); // always first
        
        ExecutableProjectPanel.fillProjectsCombo(projectComboBox, lastSelectedProject);
    }

    private boolean validateCorefilePath(String corePath) {
	corePath = corePath.trim();
	if (corePath.length() == 0) {
	    setError("ERROR_CORE_NOT_SPECIFIED", true); // NOI18N
	    return false;
	}

        Host host = Host.byName(getHostName());
        final ExecutionEnvironment exEnv = host.executionEnvironment();

        try {
            if (!HostInfoUtils.fileExists(exEnv, corePath)) {
                setError("ERROR_CORE_DONTEXIST", true); // NOI18N
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        // Some more validation locally
        if (exEnv.isLocal()) {
            File coreFile = new File(corePath);
            if (coreFile.isDirectory()) {
                setError("ERROR_NOTACOREFILE", true); // NOI18N
                return false;
            }
            /* fromFile Deprecated replaced by toFileObject
            FileObject fo[] = FileUtil.fromFile(coreFile);
            if (fo == null || fo.length == 0) {
                setError("ERROR_CORE_DONTEXIST", true);
                return false;
            }
            DataObject dataObject = null;
            try {
                dataObject = DataObject.find(fo[0]);
            }
            catch (Exception e) {
                setError("ERROR_CORE_DONTEXIST", true);
                return false;
            }
            */

            FileObject fo = FileUtil.toFileObject(coreFile);
            if (fo == null) {
                setError("ERROR_CORE_DONTEXIST", true); // NOI18N
                return false;
            }
            DataObject dataObject = null;
            try {
                dataObject = DataObject.find(fo);
            }
            catch (Exception e) {
                setError("ERROR_CORE_DONTEXIST", true); // NOI18N
                return false;
            }
            if (!MIMENames.ELF_CORE_MIME_TYPE.equals(IpeUtils.getMime(dataObject))) {
                setError("ERROR_NOTACOREFILE", true); // NOI18N
                return false;
            }
        }

	return true;
    }

    private boolean validateExecutablePath(String exePath) {
	exePath = exePath.trim();
	if (exePath.equals(autoString)) {
            /* 6966340
	    if (!matchProject(pName)) 
	        setProject();
             *
             */
	    return true;
	}

        Host host = Host.byName(getHostName());
        final ExecutionEnvironment exEnv = host.executionEnvironment();

        try {
            if (!HostInfoUtils.fileExists(exEnv, exePath)) {
                EngineDescriptor descriptor = new EngineDescriptor(getEngine());
                if (descriptor.hasCapability(EngineCapability.DERIVE_EXECUTABLE)) {
                    setError("ERROR_DONTEXIST", true); // NOI18N
                } else {
                    setError("DBG_ERROR_DONTEXIST", false); // NOI18N
                }
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        // more validation locally
        if (exEnv.isLocal()) {
            File exeFile = new File(exePath);
            if (exeFile.isDirectory()) {
                setError("ERROR_NOTAEXEFILE", true); // NOI18N
                return false;
            }

            FileObject fo = FileUtil.toFileObject(exeFile);
            if (fo == null) {
                setError("ERROR_NOTAEXEFILE", true); // NOI18N
                return false;
            }
            DataObject dataObject = null;
            try {
                dataObject = DataObject.find(fo);
            }
            catch (Exception e) {
                setError("ERROR_DONTEXIST", true); // NOI18N
                return false;
            }
            if (!MIMENames.isBinary(IpeUtils.getMime(dataObject))) {
                setError("ERROR_NOTAEXEFILE", true); // NOI18N
                return false;
            }
        }
	
	return true;
    }

    private boolean matchProject(String executable) {
	// <no project> is the default for  <from core>
	if (executable.equals(autoString)) {
 //           projectComboBox.setEnabled(false);
	    projectComboBox.setSelectedIndex(0);
	    return true;
	}
        projectComboBox.setEnabled(true);
	// match opened Project first
	for (int i = 0; i < projectComboBox.getItemCount(); i++) {
	    if (executable.equalsIgnoreCase(
                projectComboBox.getItemAt(i).toString())) {
	        projectComboBox.setSelectedIndex(i);
		return true;
	    }
	}
	return false;
    }

    private void setError(final String errorMsg, final boolean disable) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                errorLabel.setText(getString(errorMsg));
                if (disable) {
                    projectComboBox.setEnabled(false);
                }
                actionButton.setEnabled(false);
            }
        });
    }

    private void clearError() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                errorLabel.setText(" "); // NOI18N
                projectComboBox.setEnabled(true);
                actionButton.setEnabled(true);
            }
        });
    }

    // ModifiedDocumentListener
    public class CorefileValidateListener extends AttachPanel.AnyChangeDocumentListener {
        @Override
        protected void documentChanged(DocumentEvent e) {
            validateAll();
	}
    }

    // ModifiedDocumentListener
    public class ExecutableValidateListener extends AttachPanel.AnyChangeDocumentListener {
        @Override
        protected void documentChanged(DocumentEvent e) {
            String pName = CndPathUtilitities.getBaseName(getExecutablePath());
            matchProject(pName);
            validateAll();
	}
    }

    public Project getSelectedProject() {
        Object selectedItem = projectComboBox.getSelectedItem();
        if (selectedItem instanceof ProjectCBItem) {
            noproject = false;
            return ((ProjectCBItem)selectedItem).getProject();
        }
        // set noproject if NO_PROJECT is selected
        noproject = (projectComboBox.getSelectedIndex() == 0);
        return null;
    }

    /** Look up i18n strings here */
    private ResourceBundle bundle;
    private String getString(String s) {
	if (bundle == null) {
	    bundle = NbBundle.getBundle(DebugCorePanel.class);
	}
	return bundle.getString(s);
    }

    public boolean asynchronous() {
	return false;
    }

    public void setLastSelectedProject (Project l) {
	lastSelectedProject = l;
    }

    public boolean getNoProject() {
	return noproject;
    }
}

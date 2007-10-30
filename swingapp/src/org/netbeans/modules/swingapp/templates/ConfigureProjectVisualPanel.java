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

package org.netbeans.modules.swingapp.templates;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.net.URL;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.ErrorManager;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.ListView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author  tpavek
 */
public class ConfigureProjectVisualPanel extends javax.swing.JPanel
        implements DocumentListener, PropertyChangeListener, ExplorerManager.Provider
{
    private ConfigureProjectPanel wizardPanel;
    private ExplorerManager explorerManager;
    private boolean configuring;

    /** Creates new form ConfigureProjectVisualProject */
    public ConfigureProjectVisualPanel(ConfigureProjectPanel panel) {
        wizardPanel = panel;
        initComponents();

        explorerManager = new ExplorerManager();
        explorerManager.setRootContext(getTemplatesRootNode());
        explorerManager.addPropertyChangeListener(this);

        shellList.setPopupAllowed(false);
        shellList.setBorder((Border)UIManager.get("Nb.ScrollPane.border")); // NOI18N

        // register listener on the textFields to make the automatic updates
        projectNameTextField.getDocument().addDocumentListener(this);
        projectLocationTextField.getDocument().addDocumentListener(this);
        appNameTextField.getDocument().addDocumentListener(this);
    }

    void setConfig(File projectLocation, String projectName, String appName) {
        configuring = true;

        projectLocationTextField.setText(projectLocation.getAbsolutePath());
        projectNameTextField.setText(projectName);
        projectNameTextField.setSelectionStart(0);
        projectNameTextField.setSelectionEnd(projectName.length());
        if (appName != null) {
            appNameTextField.setText(appName);
        }

        if (explorerManager.getSelectedNodes().length == 0) { // pre-select the first shell
            Node[] nodes = explorerManager.getRootContext().getChildren().getNodes(true);
            try {
                explorerManager.setSelectedNodes(new Node[] { nodes[0] });
            } catch (PropertyVetoException ex) {
            }
        }

        configuring = false;
        wizardPanel.visualPanelChanged(false);
    }

    File getProjectDirectory() {
        return new File(createdFolderTextField.getText());
    }

    String getProjectName() {
        return projectNameTextField.getText().trim();
    }

    String getApplicationClassName() {
        return appNameTextField.getText();
    }

    FileObject getSelectedTemplate() {
        Node[] selected = explorerManager.getSelectedNodes();
        if (selected.length == 1)
            return fileFromNode(selected[0]);

        return null;
    }

    boolean isSetMainProject() {
        return mainProjectCheckBox.isSelected();
    }

    // DocumentListener
    public void changedUpdate(DocumentEvent e)  {
    }

    // DocumentListener
    public void insertUpdate(DocumentEvent e) {
        updateTexts(e);
    }

    // DocumentListener
    public void removeUpdate(DocumentEvent e) {
        updateTexts(e);
    }

    // PropertyChangeListener - called from ExplorerManager when node selection changes
    public void propertyChange(PropertyChangeEvent ev) {
        if (ev.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
            if (!configuring) {
                wizardPanel.visualPanelChanged(true);
            }

            FileObject template = getSelectedTemplate();
            URL url = template != null ?
                (URL) template.getAttribute("instantiatingWizardURL") : null; // NOI18N
            descBrowser.setURL(url);
        }
    }

    // ExplorerManager.Provider - for the ListView to work
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    private void updateTexts(DocumentEvent e) {
        if (e.getDocument() == projectNameTextField.getDocument()
                || e.getDocument() == projectLocationTextField.getDocument()) {
            String projectName = projectNameTextField.getText();
            String projectFolder = projectLocationTextField.getText(); 
            createdFolderTextField.setText(projectFolder + File.separatorChar + projectName);

            if (e.getDocument() == projectNameTextField.getDocument()) {
                String projClassName = getClassNameFromProject(projectName);
                String packageName = projClassName.toLowerCase();
                String appClassName;
                if (!packageName.contains("application") && !projClassName.endsWith("App")) { // NOI18N
                    appClassName = projClassName + "App"; // NOI18N
                } else {
                    appClassName = projClassName;
                }
                if (Character.isLowerCase(appClassName.charAt(0))) {
                    appClassName = appClassName.substring(0, 1).toUpperCase() + appClassName.substring(1);
                }
                appNameTextField.setText(packageName + "." + appClassName); // NOI18N
                return; // change will be fired through appNameTextField change
            }
        }

        if (!configuring) {
            wizardPanel.visualPanelChanged(false);
        }
    }

    private static String getClassNameFromProject(String projectName) {
        if (!Utilities.isJavaIdentifier(projectName)) {
            StringBuilder buf = new StringBuilder(projectName.length());
            for (int i=0; i < projectName.length(); i++) {
                char c = projectName.charAt(i);
                if (buf.length() == 0) {
                    if (Character.isJavaIdentifierStart(c)) {
                        buf.append(c);
                    }
                } else if (Character.isJavaIdentifierPart(c)) {
                    buf.append(c);
                }
            }
            return (buf.length() > 0) ? buf.toString() : "MyApplication"; // NOI18N
        }
        return projectName;
    }

    private Node getTemplatesRootNode() {
        try {
            FileObject shellFolder = Repository.getDefault().getDefaultFileSystem().findResource(
                "org-netbeans-modules-swingapp/appshells"); // NOI18N
            DataObject dobj = DataObject.find(shellFolder);
            return dobj.getNodeDelegate();
        }
        catch (Exception ex) { // should not happen, but...
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return new AbstractNode(new Children.Array());
        }
    }

    private static FileObject fileFromNode(Node n) {
        DataObject dobj = (DataObject) n.getCookie(DataObject.class);
        return dobj != null ? dobj.getPrimaryFile() : null;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        projectNameTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        projectLocationTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        createdFolderTextField = new javax.swing.JTextField();
        mainProjectCheckBox = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        appNameTextField = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        shellListLabel = new javax.swing.JLabel();
        shellList = new TemplatesListView();
        descBrowser = new org.openide.awt.HtmlBrowser();

        setName(org.openide.util.NbBundle.getMessage(ConfigureProjectVisualPanel.class, "ConfigureProjectVisualPanel.name")); // NOI18N

        jLabel1.setLabelFor(projectNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ConfigureProjectVisualPanel.class, "ConfigureProjectVisualPanel.jLabel1.text")); // NOI18N

        jLabel2.setLabelFor(projectLocationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ConfigureProjectVisualPanel.class, "ConfigureProjectVisualPanel.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(ConfigureProjectVisualPanel.class, "ConfigureProjectVisualPanel.jButton1.text")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        jLabel3.setLabelFor(createdFolderTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(ConfigureProjectVisualPanel.class, "ConfigureProjectVisualPanel.jLabel3.text")); // NOI18N

        createdFolderTextField.setEditable(false);

        mainProjectCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(mainProjectCheckBox, org.openide.util.NbBundle.getMessage(ConfigureProjectVisualPanel.class, "ConfigureProjectVisualPanel.mainProjectCheckBox.text")); // NOI18N
        mainProjectCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        mainProjectCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel4.setLabelFor(appNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(ConfigureProjectVisualPanel.class, "ConfigureProjectVisualPanel.jLabel4.text")); // NOI18N

        shellListLabel.setLabelFor(shellList);
        org.openide.awt.Mnemonics.setLocalizedText(shellListLabel, org.openide.util.NbBundle.getMessage(ConfigureProjectVisualPanel.class, "ConfigureProjectVisualPanel.shellListLabel.text")); // NOI18N
        shellListLabel.setToolTipText(org.openide.util.NbBundle.getMessage(ConfigureProjectVisualPanel.class, "ConfigureProjectVisualPanel.shellListLabel.toolTipText")); // NOI18N

        shellList.setTraversalAllowed(false);

        descBrowser.setStatusLineVisible(false);
        descBrowser.setToolbarVisible(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(mainProjectCheckBox)
                .addContainerGap(348, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel4)
                    .add(jLabel3)
                    .add(jLabel2)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(appNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
                    .add(createdFolderTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
                    .add(projectLocationTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
                    .add(projectNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(browseButton))
            .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 457, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(shellListLabel)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(shellList, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 187, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(descBrowser, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(projectNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(projectLocationTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browseButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(createdFolderTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mainProjectCheckBox)
                .add(10, 10, 10)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(11, 11, 11)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(appNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(20, 20, 20)
                .add(shellListLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(descBrowser, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE)
                    .add(shellList, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE)))
        );

        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigureProjectVisualPanel.class, "ConfigureProjectVisualPanel.jLabel1.accessibleDescription")); // NOI18N
        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigureProjectVisualPanel.class, "ConfigureProjectVisualPanel.jLabel2.accessibleDescription")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigureProjectVisualPanel.class, "ConfigureProjectVisualPanel.jButton1.accessibleDescription")); // NOI18N
        jLabel3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigureProjectVisualPanel.class, "ConfigureProjectVisualPanel.jLabel3.accessibleDescription")); // NOI18N
        mainProjectCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigureProjectVisualPanel.class, "ConfigureProjectVisualPanel.mainProjectCheckBox.accessibleDescription")); // NOI18N
        jLabel4.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigureProjectVisualPanel.class, "ConfigureProjectVisualPanel.jLabel4.accessibleDescription")); // NOI18N
        descBrowser.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConfigureProjectVisualPanel.class, "ConfigureProjectVisualPanel.descBrowser.accessibleName")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConfigureProjectVisualPanel.class, "TITLE_NewDesktopApp")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigureProjectVisualPanel.class, "ConfigureProjectVisualPanel.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        JFileChooser chooser = new JFileChooser ();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setDialogTitle(NbBundle.getMessage(ConfigureProjectVisualPanel.class,
                "ConfigureProjectVisualPanel.locationChooserTitle")); // NOI18N
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        String path = projectLocationTextField.getText();
        if (path.length() > 0) {
            File f = new File (path);
            if (f.exists ()) {
                chooser.setSelectedFile(f);
            }
        }
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File projectDir = chooser.getSelectedFile();
            projectLocationTextField.setText(FileUtil.normalizeFile(projectDir).getAbsolutePath());
        }            
        wizardPanel.visualPanelChanged(false);
    }//GEN-LAST:event_browseButtonActionPerformed
    
    /**
     * Taken from org.netbeans.modules.project.ui.TemplatesPanelGUI (i.e. the
     * first panel of New Project wizard). It makes the enter key work for the
     * default button in the window (bug 102364).
     */
    private static class TemplatesListView extends ListView implements ActionListener {
        public TemplatesListView() {
            super();
            // bugfix #44717, Enter key must work regardless if TemplatesPanels is focused
            list.unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false));
            setDefaultProcessor(this);
        }
        public void actionPerformed(ActionEvent e) {
            // Do nothing
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField appNameTextField;
    private javax.swing.JButton browseButton;
    private javax.swing.JTextField createdFolderTextField;
    private org.openide.awt.HtmlBrowser descBrowser;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JCheckBox mainProjectCheckBox;
    private javax.swing.JTextField projectLocationTextField;
    private javax.swing.JTextField projectNameTextField;
    private org.openide.explorer.view.ListView shellList;
    private javax.swing.JLabel shellListLabel;
    // End of variables declaration//GEN-END:variables
    
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.clientproject.ui.wizard;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.ClientSideProjectConstants;
import org.netbeans.modules.web.clientproject.sites.SiteZip;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.CheckableNode;
import org.openide.explorer.view.OutlineView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

@NbBundle.Messages({"CreateSiteTemplate_Name=Create site template",
    "CreateSiteTemplate_Title=Describe template",
    "CreateSiteTemplate_Label=Name template and select files",
    "CreateSiteTemplate_WizardTitle=Create Site Template from current project",
    "CreateSiteTemplate_Error1=Template name must be specified",
    "CreateSiteTemplate_Error2=Destination name must be specified",
    "CreateSiteTemplate_Error3=Destination is not a valid folder",
    "CreateSiteTemplate_Error4=Template file {0} already exists. Do you want to override it?",
    "CreateSiteTemplate_FileChooser=Select folder to store template in",
    "CreateSiteTemplate_FileChooserButton=Select"
})
public class CreateSiteTemplate extends javax.swing.JPanel implements ExplorerManager.Provider, DocumentListener {

    private FileObject root;
    private OutlineView tree;
    private ExplorerManager manager;
    private WizardPanel wp;
    
    public CreateSiteTemplate(FileObject root, WizardPanel wp) {
        this.root = root;
        this.manager = new ExplorerManager();
        this.wp = wp;
        try {
            manager.setRootContext(new FNode(DataObject.find(root).getNodeDelegate(), root.isFolder()));
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        initComponents();
        tree = new OutlineView();
        tree.setTreeSortable(false);
        placeholder.setLayout(new BorderLayout());
        placeholder.add(tree, BorderLayout.CENTER);
        nameTextField.getDocument().addDocumentListener(this);
        fileTextField.getDocument().addDocumentListener(this);
    }

    public String getName() {
        return Bundle.CreateSiteTemplate_Label();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        placeholder = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        fileTextField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();

        jLabel1.setText(org.openide.util.NbBundle.getMessage(CreateSiteTemplate.class, "CreateSiteTemplate.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout placeholderLayout = new javax.swing.GroupLayout(placeholder);
        placeholder.setLayout(placeholderLayout);
        placeholderLayout.setHorizontalGroup(
            placeholderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 393, Short.MAX_VALUE)
        );
        placeholderLayout.setVerticalGroup(
            placeholderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 185, Short.MAX_VALUE)
        );

        jLabel2.setText(org.openide.util.NbBundle.getMessage(CreateSiteTemplate.class, "CreateSiteTemplate.jLabel2.text")); // NOI18N

        fileTextField.setText(org.openide.util.NbBundle.getMessage(CreateSiteTemplate.class, "CreateSiteTemplate.fileTextField.text")); // NOI18N

        jLabel3.setText(org.openide.util.NbBundle.getMessage(CreateSiteTemplate.class, "CreateSiteTemplate.jLabel3.text")); // NOI18N

        nameTextField.setText(org.openide.util.NbBundle.getMessage(CreateSiteTemplate.class, "CreateSiteTemplate.nameTextField.text")); // NOI18N

        browseButton.setText(org.openide.util.NbBundle.getMessage(CreateSiteTemplate.class, "CreateSiteTemplate.browseButton.text")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fileTextField)
                    .addComponent(nameTextField))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(browseButton))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(0, 177, Short.MAX_VALUE))
            .addComponent(placeholder, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(fileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(placeholder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(Bundle.CreateSiteTemplate_FileChooser());
        chooser.setMultiSelectionEnabled(false);
        chooser.setApproveButtonText(Bundle.CreateSiteTemplate_FileChooserButton());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showDialog(this, null) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if (f.isFile()) {
                f = f.getParentFile();
            }
            fileTextField.setText(f.getAbsolutePath());
        }

    }//GEN-LAST:event_browseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JPanel placeholder;
    // End of variables declaration//GEN-END:variables

    private String getErrorMessage() {
        if (getTemplateName().trim().length() == 0) {
            return Bundle.CreateSiteTemplate_Error1();
        }
        if (getTemplateFolder().trim().length() == 0) {
            return Bundle.CreateSiteTemplate_Error2();
        }
        if (!new File(getTemplateFolder()).exists()) {
            return Bundle.CreateSiteTemplate_Error3();
        }
        return ""; //NOI18N
    }
    
    public String getTemplateName() {
        return nameTextField.getText();
    }

    public String getTemplateFolder() {
        return fileTextField.getText();
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        wp.fireChange();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        wp.fireChange();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        wp.fireChange();
    }

    private static class WizardPanel implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel {

        private CreateSiteTemplate comp;
        private ChangeSupport sup = new ChangeSupport(this);
        private WizardDescriptor wd;

        public WizardPanel(ClientSideProject p) {
            comp = new CreateSiteTemplate(p.getProjectDirectory(), this);
            comp.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(0)); //NOI18N
            // Sets steps names for a panel
            comp.putClientProperty("WizardPanel_contentData", new String[]{Bundle.CreateSiteTemplate_Title()}); //NOI18N
            // Turn on subtitle creation on each step
            comp.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); //NOI18N
            // Show steps on the left side with the image on the background
            comp.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE); //NOI18N
            // Turn on numbering of all steps
            comp.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE); //NOI18N
        }
        
        @Override
        public Component getComponent() {
            return comp;
        }

        @Override
        public HelpCtx getHelp() {
            return new HelpCtx(CreateSiteTemplate.class);
        }

        @Override
        public boolean isValid() {
            String error = comp.getErrorMessage();
            setErrorMessage(error);
            return error.length() == 0;
        }

        public void setErrorMessage(String message) {
            if (wd != null) {
                wd.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
            }
        }
        
        @Override
        public void addChangeListener(ChangeListener l) {
            sup.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            sup.removeChangeListener(l);
        }

        void fireChange() {
            sup.fireChange();
        }
        
        @Override
        public boolean isFinishPanel() {
            return true;
        }

        @Override
        public void readSettings(Object settings) {
            this.wd = (WizardDescriptor)settings;
        }

        @Override
        public void storeSettings(Object settings) {
        }
    }
    
    private static class WizardIterator implements WizardDescriptor.InstantiatingIterator {

        private WizardPanel panel;
        private ClientSideProject p;
        private ChangeSupport sup = new ChangeSupport(this);

        public WizardIterator(ClientSideProject p) {
            this.p = p;
            panel = new WizardPanel(p);
            panel.addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    sup.fireChange();
                }
            });
        }
        
        @Override
        public Set instantiate() throws IOException {
            String name = panel.comp.getTemplateName();
            if (!name.endsWith(".zip")) { //NOI18N
                name += ".zip"; //NOI18N
            }
            File f = new File(panel.comp.getTemplateFolder(), name);
            if (f.exists()) {
                if (DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Confirmation(Bundle.CreateSiteTemplate_Error4(f.getAbsolutePath()))) != NotifyDescriptor.YES_OPTION) {
                    return null;
                }
            }
            createZipFile(f, p, panel.comp.manager.getRootContext());
            return null;
        }

        @Override
        public void initialize(WizardDescriptor wizard) {
        }

        @Override
        public void uninitialize(WizardDescriptor wizard) {
        }

        @Override
        public Panel current() {
            return panel;
        }

        @Override
        public String name() {
            return Bundle.CreateSiteTemplate_Name();
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public boolean hasPrevious() {
            return false;
        }

        @Override
        public void nextPanel() {
        }

        @Override
        public void previousPanel() {
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            sup.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            sup.removeChangeListener(l);
        }
        
    }
    
    public static void showWizard(ClientSideProject p) {
        WizardDescriptor wd = new WizardDescriptor(new WizardIterator(p));
        wd.setTitleFormat(new MessageFormat("{0}")); //NOI18N
        wd.setTitle(Bundle.CreateSiteTemplate_WizardTitle());
        DialogDisplayer.getDefault().notify(wd);
    }

    private class FNode extends FilterNode {

        public FNode(Node original, boolean hasChildren) {
            super(original, hasChildren ? new FChildren(original) : Children.LEAF, 
                    Lookups.fixed(new Checkable(), original.getLookup().lookup(FileObject.class)));
            Checkable ch = getLookup().lookup(Checkable.class);
            ch.setOwner(this);
            ch.setComponent(tree);
        }
        
        public void refresh() {
            fireIconChange();
        }
        
        
    }
    
    private class FChildren extends FilterNode.Children {
        
        public FChildren(Node owner) {
            super(owner);
        }
 
        @Override
        protected Node copyNode(Node node) {
            FileObject fo = node.getLookup().lookup(FileObject.class);
            assert fo != null;
            return new FNode(node, fo.isFolder());
        }

        @Override
        protected Node[] createNodes(Node key) {
            FileObject fo = key.getLookup().lookup(FileObject.class);
            if (fo == null) {
                return new Node[0];
            }
            if ("nbproject".equals(fo.getName())) { //NOI18N
                return new Node[0];
            }
            return super.createNodes(key);
        }
    }
    
    
    private static class Checkable implements CheckableNode {

        private static boolean internalUpdate = false;
        
        private Boolean checked = Boolean.TRUE;
        private FNode node;
        private JComponent comp;

        @Override
        public boolean isCheckable() {
            return true;
        }

        @Override
        public boolean isCheckEnabled() {
            return true;
        }

        @Override
        public Boolean isSelected() {
            return checked;
        }

        @Override
        public void setSelected(Boolean selected) {
            checked = selected;
            if (internalUpdate) {
                return;
            } else {
                try {
                    internalUpdate = true;
                    if (checked != null) {
                        propagateChanges(node, checked);
                    }
                } finally {
                    internalUpdate = false;
                }
            }
        }
        
        private static void propagateChanges(FNode node, boolean checked) {
            if (checked) {
                tick(node.getChildren(), true);
                FNode n = node;
                while (n.getParentNode() != null) {
                    n = (FNode)n.getParentNode();
                    n.getLookup().lookup(Checkable.class).setSelected(Boolean.TRUE);
                    n.refresh();
                }
            } else {
                tick(node.getChildren(), false);
            }
        }
        
        private static void tick(Children ch, boolean tick) {
            if (ch == null) {
                return;
            }
            for (Node n : ch.getNodes(true)) {
                n.getLookup().lookup(Checkable.class).setSelected(tick ? Boolean.TRUE : Boolean.FALSE);
                ((FNode)n).refresh();
                tick(n.getChildren(), tick);
            }
        }

        private void setOwner(FNode aThis) {
            node = aThis;
        }

        public void setComponent(JComponent comp) {
            this.comp = comp;
        }
        
    }
    
    private static void createZipFile(File templateFile, ClientSideProject project, Node rootNode) throws IOException {
        if (!templateFile.exists()) {
            templateFile.createNewFile();
        }
        ZipOutputStream str = new ZipOutputStream(new FileOutputStream(templateFile));
        try {
            writeProjectMetadata(str, project);
            writeChildren(str, project.getProjectDirectory(), rootNode.getChildren());
        } finally {
            str.close();
        }
        SiteZip.registerTemplate(templateFile);
    }

    private static void writeProjectMetadata(ZipOutputStream str, ClientSideProject project) throws IOException {
        ZipEntry ze = new ZipEntry(ClientSideProjectConstants.TEMPLATE_DESCRIPTOR);
        str.putNextEntry(ze);
        EditableProperties ep = new EditableProperties(false);
        String s = project.getEvaluator().getProperty(ClientSideProjectConstants.PROJECT_SITE_ROOT_FOLDER);
        if (s == null) {
            s = ""; //NOI18N
        }
        ep.setProperty(ClientSideProjectConstants.PROJECT_SITE_ROOT_FOLDER, s);
        s = project.getEvaluator().getProperty(ClientSideProjectConstants.PROJECT_TEST_FOLDER);
        if (s == null) {
            s = ""; //NOI18N
        }
        ep.setProperty(ClientSideProjectConstants.PROJECT_TEST_FOLDER, s);
        s = project.getEvaluator().getProperty(ClientSideProjectConstants.PROJECT_CONFIG_FOLDER);
        if (s == null) {
            s = ""; //NOI18N
        }
        ep.setProperty(ClientSideProjectConstants.PROJECT_CONFIG_FOLDER, s);
        ep.store(str);
    }

    private static void writeChildren(ZipOutputStream str, FileObject root, Children children) throws IOException {
        for (Node node : children.getNodes(true)) {
            FileObject fo = node.getLookup().lookup(FileObject.class);
            InputStream is = null;
            if (!fo.isFolder()) {
                is = fo.getInputStream();
            }
            try {
                Checkable ch = node.getLookup().lookup(Checkable.class);
                if (Boolean.TRUE != ch.isSelected()) {
                    continue;
                }
                String relPath = FileUtil.getRelativePath(root, fo);
                if (fo.isFolder()) {
                    relPath += "/"; //NOI18N
                }
                ZipEntry ze = new ZipEntry(relPath);
                str.putNextEntry(ze);
                if (is != null) {
                    FileUtil.copy(fo.getInputStream(), str);
                }
            } finally {
                if (is != null) {
                    is.close();
                }
            }
            if (!node.isLeaf()) {
                writeChildren(str, root, node.getChildren());
            }
        }
    }

}

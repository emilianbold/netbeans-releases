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

package org.netbeans.modules.cnd.ui.options;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * A simple dialog for adding and removing compilers.
 *
 * @author  Gordon Prieur
 */
public class AddRemoveToolPanel extends JPanel implements ActionListener {
    
    AddRemoveListModel model;
    CompilerSet cs;
    
    /** Creates new form AddRemoveToolPanel */
    public AddRemoveToolPanel(JComboBox jc, CompilerSet cs) {
        Object o;
        
        this.cs = cs;
        model = new AddRemoveListModel();
        for (int i = 0; i < jc.getItemCount(); i++) {
            o = jc.getItemAt(i);
            if (o instanceof Tool) {
                model.addElement(o);
            }
        }
        
        initComponents();
    }
    
    public void actionPerformed(ActionEvent ev) {
        Object o = ev.getSource();
         if (o == addButton) {
            String name = addUserTool();
            if (name != null && !model.contains(name)) {
                model.addAddElement(name);
                compilerList.setSelectedValue(name, true);
                removeButton.setEnabled(true);
            }
         } else if (o == removeButton) {
            int idx = compilerList.getSelectedIndex();
            if (idx >= 0) {
                model.remove(idx);
                if (model.getSize() > 0) {
                    compilerList.setSelectedIndex(idx < model.getSize() ? idx : (model.getSize() - 1));
                    removeButton.setEnabled(true);
                } else {
                    removeButton.setEnabled(false);
                }
            }
         }
    }
    
    private String addUserTool() {
        JFileChooser fc = new FileOnlyChooser(new CompilerSetView(cs.getDirectory()));
        fc.setDialogType(JFileChooser.FILES_ONLY);
        fc.setFileFilter(new FileOnlyFilter());
        int rc = fc.showDialog(this, NbBundle.getMessage(ToolsPanel.class, "LBL_DialogAddButton"));
        if (rc == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            String name;
            String path = file.getAbsolutePath();
            int pos = path.lastIndexOf(File.separator);
            if (pos >= 0) {
                name = path.substring(pos + 1);
                path = path.substring(0, pos);
            } else {
                name = file.getName();
            }
            
            // validate the input
            StringTokenizer tok = new StringTokenizer(cs.getDirectory());
            boolean valid = false;
            while (tok.hasMoreTokens()) {
                String dir = tok.nextToken();
                if (dir.equals(path)) {
                    file = new File(dir, name);
                    if (!file.isDirectory() && file.exists()) {
                        return name;
                    }
                }
            }
        }
        return null;
    }
    
    protected AddRemoveListModel getModel() {
        return model;
    }
    
    private class FileOnlyChooser extends JFileChooser {
        
        public FileOnlyChooser(FileSystemView view) {
            super(view);
        }
        
        // Correctly implementing this method will get proper icons in the file chooser!
//        public Icon getIcon(File file) {
//            FileObject fo = FileUtil.toFileObject(file);
//            return super.getIcon(file);
//        }
        
        public boolean isTraversible(File f) {
            return false;
        }
        
        public boolean isDirectorySelectionEnabled() {
            return false;
        }
        
        public boolean isAcceptAllFileFilterUsed() {
            return false;
        }
    }
    
    private class CompilerSetView extends FileSystemView {
        
        File[] roots;
        
        public CompilerSetView(String dirs) {
            int pos = dirs.indexOf(File.pathSeparator);
            if (pos >= 0) {
                roots = new File[2];
                roots[0] = new File(dirs.substring(0, pos));
                roots[1] = new File(dirs.substring(pos + 1));
            } else {
                roots = new File[1];
                roots[0] = new File(dirs);
            }
        }
        
        public String getSystemDisplayName(File file) {
            if (file.isDirectory()) {
                return file.getAbsolutePath();
            } else {
                return super.getSystemDisplayName(file);
            }
        }
        
        public File[] getRoots() {
            return roots;
        }
        
        public File getDefaultDirectory() {
            return roots[0];
        }
        
        public File getParentDirectory() {
            return null;
        }
        
        public File getHomeDirectory() {
            return getDefaultDirectory();
        }
        
        public Boolean isTraversable(File file) {
            if (file.isDirectory()) {
                for (int i = 0; i < roots.length; i++) {
                    if (roots[i].equals(file)) {
                        return Boolean.TRUE;
                    }
                }
            }
            return Boolean.FALSE;
        }
        
        public File createNewFolder(File file) throws IOException {
            return file;
        }
    }
    
    private class FileOnlyFilter extends FileFilter {
        
        public boolean accept(File file) {
            if (!file.isDirectory()) {
                String mimetype = FileUtil.toFileObject(file).getMIMEType();
                if (mimetype.startsWith(MIMENames.EXE_MIME_TYPE) || mimetype.equals(MIMENames.SHELL_MIME_TYPE)) {
                    return true;
                }
            }
            return false;
        }
        
        public String getDescription() {
            return NbBundle.getMessage(ToolsPanel.class, "LBL_ExecutableFilesOnly"); // NOI18N
        } 
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        compilerListLabel = new javax.swing.JLabel();
        compilerListScrollPane = new javax.swing.JScrollPane();
        compilerList = new javax.swing.JList();
        compilerList.setModel(model);
        addButton = new javax.swing.JButton();
        addButton.addActionListener(this);
        removeButton = new javax.swing.JButton();
        removeButton.addActionListener(this);
        if (model.getSize() > 0) {
            compilerList.setSelectedIndex(0);
            removeButton.setEnabled(true);
        } else {
            removeButton.setEnabled(false);
        }

        compilerListLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_CompilerList").charAt(0));
        compilerListLabel.setLabelFor(compilerList);
        compilerListLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_CompilerList"));

        compilerListScrollPane.setViewportView(compilerList);

        addButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_AddToolButton").charAt(0));
        addButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_AddToolButton"));
        addButton.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSN_AddToolButton"));
        addButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSD_AddToolButton"));

        removeButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("MNEM_RemoveToolButton").charAt(0));
        removeButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("LBL_RemoveToolButton"));
        removeButton.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSN_RemoveToolButton"));
        removeButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/ui/options/Bundle").getString("ACSD_RemoveToolButton"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(compilerListScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(addButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE)
                            .add(removeButton)))
                    .add(compilerListLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 259, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(compilerListLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(addButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(208, Short.MAX_VALUE))
                    .add(compilerListScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)))
        );

        layout.linkSize(new java.awt.Component[] {addButton, removeButton}, org.jdesktop.layout.GroupLayout.VERTICAL);

    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JList compilerList;
    private javax.swing.JLabel compilerListLabel;
    private javax.swing.JScrollPane compilerListScrollPane;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables
    
}

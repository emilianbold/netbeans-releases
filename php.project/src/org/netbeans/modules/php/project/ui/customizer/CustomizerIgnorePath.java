/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.customizer;

import java.awt.Component;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.classpath.BasePathSupport;
import org.netbeans.modules.php.project.ui.PathUiSupport;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class CustomizerIgnorePath extends JPanel implements HelpCtx.Provider {
    private static final long serialVersionUID = 32813080125323378L;

    private final Category category;
    private final PhpProject project;

    public CustomizerIgnorePath(Category category, PhpProjectProperties uiProps) {

        this.category = category;
        project = uiProps.getProject();

        initComponents();

        PathUiSupport.EditMediator.FileChooserDirectoryHandler directoryHandler = new PathUiSupport.EditMediator.FileChooserDirectoryHandler() {
            public File getCurrentDirectory() {
                return FileUtil.toFile(ProjectPropertiesSupport.getSourcesDirectory(project));
            }
            public void setCurrentDirectory(File currentDirectory) {
            }
        };

        ignorePathList.setModel(uiProps.getIgnorePathListModel());
        ignorePathList.setCellRenderer(uiProps.getIgnorePathListRenderer());
        PathUiSupport.EditMediator.register(uiProps.getProject(),
                                               ignorePathList,
                                               addButton.getModel(),
                                               removeButton.getModel(),
                                               directoryHandler);

        ignorePathList.getModel().addListDataListener(new ListDataListener() {
            public void intervalAdded(ListDataEvent e) {
                validateData();
            }

            public void intervalRemoved(ListDataEvent e) {
                validateData();
            }

            public void contentsChanged(ListDataEvent e) {
                validateData();
            }
        });
    }

    void validateData() {
        int size = ignorePathList.getModel().getSize();
        for (int i = 0; i < size; ++i) {
            BasePathSupport.Item item = (BasePathSupport.Item) ignorePathList.getModel().getElementAt(i);
            FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(new File(item.getFilePath())));
            if (fo != null) {
                if (!CommandUtils.isUnderAnySourceGroup(project, fo, false)) {
                    category.setErrorMessage(NbBundle.getMessage(CustomizerIgnorePath.class, "MSG_NotSourceGroupSubdirectory", fo.getNameExt()));
                    category.setValid(false);
                    return;
                }
            }
        }
        category.setErrorMessage(null);
        category.setValid(true);
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ignorePathLabel = new JLabel();
        ignorePathScrollPane = new JScrollPane();
        ignorePathList = new JList();
        addButton = new JButton();
        removeButton = new JButton();

        ignorePathLabel.setLabelFor(ignorePathList);

        Mnemonics.setLocalizedText(ignorePathLabel, NbBundle.getMessage(CustomizerIgnorePath.class, "CustomizerIgnorePath.ignorePathLabel.text")); // NOI18N
        ignorePathScrollPane.setViewportView(ignorePathList);


        Mnemonics.setLocalizedText(addButton, NbBundle.getMessage(CustomizerIgnorePath.class, "CustomizerIgnorePath.addButton.text"));
        Mnemonics.setLocalizedText(removeButton, NbBundle.getMessage(CustomizerIgnorePath.class, "CustomizerIgnorePath.removeButton.text"));
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(ignorePathLabel)
            .add(GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(ignorePathScrollPane, GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(removeButton)
                    .add(addButton))
                .add(0, 0, 0))
        );

        layout.linkSize(new Component[] {addButton, removeButton}, GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(ignorePathLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(addButton)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(removeButton))
                    .add(ignorePathScrollPane, GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE))
                .add(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton addButton;
    private JLabel ignorePathLabel;
    private JList ignorePathList;
    private JScrollPane ignorePathScrollPane;
    private JButton removeButton;
    // End of variables declaration//GEN-END:variables

    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizerIgnorePath.class);
    }
}

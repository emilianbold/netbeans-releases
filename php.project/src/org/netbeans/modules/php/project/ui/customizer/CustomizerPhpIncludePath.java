/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.customizer;

import java.awt.Component;
import java.awt.EventQueue;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.classpath.BasePathSupport;
import org.netbeans.modules.php.project.classpath.IncludePathSupport;
import org.netbeans.modules.php.project.ui.LastUsedFolders;
import org.netbeans.modules.php.project.ui.PathUiSupport;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public final class CustomizerPhpIncludePath extends JPanel implements HelpCtx.Provider {

    private static final long serialVersionUID = 897213245757143454L;

    private final Category category;
    private final PhpProjectProperties uiProps;
    private final DefaultListModel<BasePathSupport.Item> includePathListModel;


    public CustomizerPhpIncludePath(Category category, PhpProjectProperties uiProps) {
        initComponents();

        this.category = category;
        this.uiProps = uiProps;
        includePathListModel = uiProps.getIncludePathListModel();

        PathUiSupport.EditMediator.FileChooserDirectoryHandler directoryHandler = new PathUiSupport.EditMediator.FileChooserDirectoryHandler() {
            @Override
            public String getDirKey() {
                return LastUsedFolders.PROJECT_INCLUDE_PATH;
            }
            @Override
            public File getCurrentDirectory() {
                return null;
            }
        };

        includePathList.setModel(includePathListModel);
        includePathList.setCellRenderer(uiProps.getIncludePathListRenderer());
        includePathListModel.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                validateData();
            }
            @Override
            public void intervalRemoved(ListDataEvent e) {
                validateData();
            }
            @Override
            public void contentsChanged(ListDataEvent e) {
                validateData();
            }
        });
        PathUiSupport.EditMediator.register(uiProps.getProject(),
                                               includePathList,
                                               addFolderButton.getModel(),
                                               removeButton.getModel(),
                                               moveUpButton.getModel(),
                                               moveDownButton.getModel(),
                                               directoryHandler);
        // initial validation
        validateData();
    }

    void validateData() {
        assert EventQueue.isDispatchThread();
        ValidationResult result = new IncludePathSupport.Validator()
                .validatePaths(uiProps.getProject(), convertToList(includePathListModel))
                .getResult();
        if (result.isFaultless()) {
            category.setErrorMessage(null);
            category.setValid(true);
            return;
        }
        // error?
        if (result.hasErrors()) {
            final ValidationResult.Message error = result.getErrors().get(0);
            if (error.isType(IncludePathSupport.Validator.ANOTHER_PROJECT_MESSAGE_TYPE)) {
                // postpone dialog so customizer is shown first
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        askUserToFixPath((BasePathSupport.Item) error.getSource());
                    }
                });
            }
            category.setErrorMessage(error.getMessage());
            category.setValid(false);
            return;
        }
        // warning
        assert result.hasWarnings();
        category.setErrorMessage(result.getWarnings().get(0).getMessage());
        category.setValid(true);
    }

    private List<BasePathSupport.Item> convertToList(DefaultListModel<BasePathSupport.Item> listModel) {
        List<BasePathSupport.Item> items = new ArrayList<>(listModel.getSize());
        for (int i = 0; i < listModel.getSize(); i++) {
            items.add(listModel.get(i));
        }
        return items;
    }

    @NbBundle.Messages({
        "# {0} - file path",
        "# {1} - project name",
        "CustomizerPhpIncludePath.error.anotherProjectSubFile=Path {0} belongs to project {1}. Remove it and add Source Files of that project?",
    })
    private void askUserToFixPath(BasePathSupport.Item item) {
        PhpProject currentProject = uiProps.getProject();
        FileObject fileObject = item.getFileObject(currentProject.getProjectDirectory());
        assert fileObject != null;
        PhpProject owningProject = PhpProjectUtils.getPhpProject(fileObject);
        assert owningProject != null;
        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(
                Bundle.CustomizerPhpIncludePath_error_anotherProjectSubFile(item.getAbsoluteFilePath(currentProject.getProjectDirectory()), owningProject.getName()),
                NotifyDescriptor.YES_NO_OPTION);
        if (DialogDisplayer.getDefault().notify(descriptor) != NotifyDescriptor.YES_OPTION) {
            return;
        }
        // fix path
        FileObject sourcesDirectory = ProjectPropertiesSupport.getSourcesDirectory(owningProject);
        assert sourcesDirectory != null;
        int index = includePathListModel.indexOf(item);
        assert index != -1;
        includePathListModel.set(index, BasePathSupport.Item.create(FileUtil.toFile(sourcesDirectory).getAbsolutePath(), null));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        includePathScrollPane = new JScrollPane();
        includePathList = new JList<BasePathSupport.Item>();
        addFolderButton = new JButton();
        removeButton = new JButton();
        moveUpButton = new JButton();
        moveDownButton = new JButton();
        includePathLabel = new JLabel();
        includePathInfoLabel = new JLabel();

        includePathScrollPane.setViewportView(includePathList);
        includePathList.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerPhpIncludePath.class, "CustomizerPhpIncludePath.includePathList.AccessibleContext.accessibleDescription")); // NOI18N

        Mnemonics.setLocalizedText(addFolderButton, NbBundle.getMessage(CustomizerPhpIncludePath.class, "LBL_AddFolder")); // NOI18N

        Mnemonics.setLocalizedText(removeButton, NbBundle.getMessage(CustomizerPhpIncludePath.class, "LBL_Remove")); // NOI18N

        Mnemonics.setLocalizedText(moveUpButton, NbBundle.getMessage(CustomizerPhpIncludePath.class, "LBL_MoveUp")); // NOI18N

        Mnemonics.setLocalizedText(moveDownButton, NbBundle.getMessage(CustomizerPhpIncludePath.class, "LBL_MoveDown")); // NOI18N

        includePathLabel.setLabelFor(includePathList);
        Mnemonics.setLocalizedText(includePathLabel, NbBundle.getMessage(CustomizerPhpIncludePath.class, "LBL_PhpIncludePath")); // NOI18N

        Mnemonics.setLocalizedText(includePathInfoLabel, NbBundle.getMessage(CustomizerPhpIncludePath.class, "CustomizerPhpIncludePath.includePathInfoLabel.text")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(includePathScrollPane)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(moveDownButton)
                    .addComponent(moveUpButton)
                    .addComponent(removeButton)
                    .addComponent(addFolderButton)))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(includePathLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, 0)
                        .addComponent(includePathInfoLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {addFolderButton, moveDownButton, moveUpButton, removeButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(includePathLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addFolderButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(removeButton)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(moveUpButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(moveDownButton))
                    .addComponent(includePathScrollPane, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(includePathInfoLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        includePathScrollPane.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerPhpIncludePath.class, "CustomizerPhpIncludePath.includePathScrollPane.AccessibleContext.accessibleName")); // NOI18N
        includePathScrollPane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerPhpIncludePath.class, "CustomizerPhpIncludePath.includePathScrollPane.AccessibleContext.accessibleDescription")); // NOI18N
        addFolderButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerPhpIncludePath.class, "CustomizerPhpIncludePath.addFolderButton.AccessibleContext.accessibleName")); // NOI18N
        addFolderButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerPhpIncludePath.class, "CustomizerPhpIncludePath.addFolderButton.AccessibleContext.accessibleDescription")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerPhpIncludePath.class, "CustomizerPhpIncludePath.removeButton.AccessibleContext.accessibleName")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerPhpIncludePath.class, "CustomizerPhpIncludePath.removeButton.AccessibleContext.accessibleDescription")); // NOI18N
        moveUpButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerPhpIncludePath.class, "CustomizerPhpIncludePath.moveUpButton.AccessibleContext.accessibleName")); // NOI18N
        moveUpButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerPhpIncludePath.class, "CustomizerPhpIncludePath.moveUpButton.AccessibleContext.accessibleDescription")); // NOI18N
        moveDownButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerPhpIncludePath.class, "CustomizerPhpIncludePath.moveDownButton.AccessibleContext.accessibleName")); // NOI18N
        moveDownButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerPhpIncludePath.class, "CustomizerPhpIncludePath.moveDownButton.AccessibleContext.accessibleDescription")); // NOI18N
        includePathLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerPhpIncludePath.class, "CustomizerPhpIncludePath.includePathLabel.AccessibleContext.accessibleName")); // NOI18N
        includePathLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerPhpIncludePath.class, "CustomizerPhpIncludePath.includePathLabel.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerPhpIncludePath.class, "CustomizerPhpIncludePath.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerPhpIncludePath.class, "CustomizerPhpIncludePath.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton addFolderButton;
    private JLabel includePathInfoLabel;
    private JLabel includePathLabel;
    private JList<BasePathSupport.Item> includePathList;
    private JScrollPane includePathScrollPane;
    private JButton moveDownButton;
    private JButton moveUpButton;
    private JButton removeButton;
    // End of variables declaration//GEN-END:variables

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.php.project.ui.customizer.CustomizerPhpIncludePath"); // NOI18N
    }

}

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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.dlight.core.ui.components;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import org.openide.util.NbBundle;

/**
 *
 * @author ak119685
 */
public class SelectExecutableTargetDialog extends DLightTargetSelectionDialog {

  private DefaultComboBoxModel cmbExecutableModel = new DefaultComboBoxModel();
  private DefaultComboBoxModel cmbArgumentsModel = new DefaultComboBoxModel();
  private DefaultComboBoxModel cmbWorkingDirModel = new DefaultComboBoxModel();
  private JButton btnBrowseExecutable = new JButton(NbBundle.getMessage(SelectExecutableTargetDialog.class, "SelectTarget.Browse.Title")); //NOI18N
  private JButton btnBrowseWorkingDir = new JButton(NbBundle.getMessage(SelectExecutableTargetDialog.class, "SelectTarget.Browse.Title")); //NOI18N
  private ContentValidator workingDirValidator = new ContentValidator() {

    public String validate(String value) {
      File dir = new File(value);
      if (!dir.exists() || !dir.isDirectory()) {
        return NbBundle.getMessage(SelectExecutableTargetDialog.class, "SelectTarget.SpecifyExistentWorkingDir"); //NOI18N
      }
      return (dir.canWrite()) ? null : NbBundle.getMessage(SelectExecutableTargetDialog.class, "SelectTarget.WorkingDirIsNotWritable"); //NOI18N
    }
  };
  private ContentValidator executableValidator = new ContentValidator() {

    public String validate(String value) {
      return (new File(value).exists()) ? null : NbBundle.getMessage(SelectExecutableTargetDialog.class, "SelectTarget.SpecifyExistentExecutable"); //NOI18N
    }
  };

  public SelectExecutableTargetDialog() {
    super(NbBundle.getMessage(SelectExecutableTargetDialog.class, "SelectExecutableTargetDialog.Title")); //NOI18N
    setDefaults();
    init();
  }

  @Override
  public String getProgramName() {
    return ((String) cmbExecutableModel.getSelectedItem());
  }

  @Override
  public String getProgramArguments() {
    return ((String) cmbArgumentsModel.getSelectedItem());
  }

  @Override
  public String getWorkingDirectory() {
    return ((String) cmbWorkingDirModel.getSelectedItem());
  }

  @Override
  void initComponents() {
    setLayout(new GridLayout(4, 1));

    btnBrowseExecutable.addActionListener(this);
    btnBrowseWorkingDir.addActionListener(this);

    btnBrowseExecutable.setToolTipText(NbBundle.getMessage(SelectExecutableTargetDialog.class, "SelectTarget.BrowseExecutable.Tooltip")); //NOI18N
    btnBrowseWorkingDir.setToolTipText(NbBundle.getMessage(SelectExecutableTargetDialog.class, "SelectTarget.BrowseWorkingDir.Tooltip")); //NOI18N

    addPanel(NbBundle.getMessage(SelectExecutableTargetDialog.class, "SelectTarget.Executable"), new JComboBox2(cmbExecutableModel, 10, true, executableValidator), btnBrowseExecutable); //NOI18N
    addPanel(NbBundle.getMessage(SelectExecutableTargetDialog.class, "SelectTarget.Arguments"), new JComboBox2(cmbArgumentsModel), null); //NOI18N
    addPanel(NbBundle.getMessage(SelectExecutableTargetDialog.class,"SelectTarget.WorkingDir"), new JComboBox2(cmbWorkingDirModel, 10, true, workingDirValidator), btnBrowseWorkingDir); //NOI18N
  }

  private void setDefaults() {
    updateModel(cmbExecutableModel, "/bin/tar"); //NOI18N
    updateModel(cmbArgumentsModel, "-cvf /tmp/f1.tar /usr/include"); //NOI18N
    updateModel(cmbWorkingDirModel, "/tmp"); //NOI18N
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    super.actionPerformed(e);

    Object src = e.getSource();
    if (!(src instanceof JButton)) {
      return;
    }

    if (src == btnBrowseExecutable) {
      String execPath = selectFile((String) cmbExecutableModel.getSelectedItem());
      updateModel(cmbExecutableModel, execPath);
    } else if (src == btnBrowseWorkingDir) {
      String workDir = selectDirectory((String) cmbWorkingDirModel.getSelectedItem(), workingDirValidator);
      updateModel(cmbWorkingDirModel, workDir);
    }
  }
}

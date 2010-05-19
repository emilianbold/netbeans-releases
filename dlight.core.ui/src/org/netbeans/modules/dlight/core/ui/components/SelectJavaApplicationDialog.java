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
import java.util.Locale;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import org.openide.util.NbBundle;

/**
 *
 * @author ak119685
 */
public class SelectJavaApplicationDialog extends DLightTargetSelectionDialog {

  private DefaultComboBoxModel cmbJvmPathModel = new DefaultComboBoxModel();
  private DefaultComboBoxModel cmbJvmOptionsModel = new DefaultComboBoxModel();
  private DefaultComboBoxModel cmbJarFileModel = new DefaultComboBoxModel();
  private DefaultComboBoxModel cmbMainClassModel = new DefaultComboBoxModel();
  private DefaultComboBoxModel cmbArgumentsModel = new DefaultComboBoxModel();
  private DefaultComboBoxModel cmbClassPathModel = new DefaultComboBoxModel();
  private DefaultComboBoxModel cmbWorkingDirModel = new DefaultComboBoxModel();
  private JButton btnBrowseJDK = new JButton(NbBundle.getMessage(SelectJavaApplicationDialog.class, "SelectTarget.Browse.Title")); //NOI18N
  private JButton btnBrowseJARFile = new JButton(NbBundle.getMessage(SelectJavaApplicationDialog.class, "SelectTarget.Browse.Title")); //NOI18N
  private JButton btnBrowseWorkingDir = new JButton(NbBundle.getMessage(SelectJavaApplicationDialog.class, "SelectTarget.Browse.Title")); //NOI18N
  private ContentValidator fileValidator = new ContentValidator() {
    public String validate(String value) {
      return (new File(value).exists()) ? null : NbBundle.getMessage(SelectJavaApplicationDialog.class, "SelectTarget.SpecifyPathToJVM"); //NOI18N
    }
  };
  private ContentValidator workingDirValidator = new ContentValidator() {
    public String validate(String value) {
      File dir = new File(value);
      if (!dir.exists() || !dir.isDirectory()) {
        return NbBundle.getMessage(SelectJavaApplicationDialog.class, "SelectTarget.SpecifyExistentWorkingDir"); //NOI18N
      }
      return (dir.canWrite()) ? null : NbBundle.getMessage(SelectJavaApplicationDialog.class, "SelectTarget.WorkingDirIsNotWritable"); //NOI18N
    }
  };

  public SelectJavaApplicationDialog() {
    super(NbBundle.getMessage(SelectJavaApplicationDialog.class, "SelectJavaApplicationDialog.Title")); //NOI18N
    setDefaults();
    init();
  }

  public String getProgramArguments() {
    StringBuffer args = new StringBuffer();
    
    String jvmOptions = getJvmOptions();
    
    if (jvmOptions != null) {
      args.append(" ").append(jvmOptions); // NOI18N
    }
    
    String jarFile = getJarFile();
    if (jarFile != null) {
      args.append(" -jar " + jarFile); //NOI18N
    }

    String mainClass = getMainClass();
    if (mainClass != null) {
      args.append(" ").append(mainClass); //NOI18N
    }

    String progArgs = getArguments();
    if (progArgs != null) {
      args.append(" ").append(progArgs); //NOI18N
    }

    String classPath = getClassPath();
    if (classPath != null) {
      args.append(" -cp ").append(classPath); //NOI18N
    }

    return args.toString();
  }

  public String getWorkingDirectory() {
    return ((String) cmbWorkingDirModel.getSelectedItem());
  }

  private String getArguments() {
    return ((String) cmbArgumentsModel.getSelectedItem());
  }

  private String getClassPath() {
    return ((String) cmbClassPathModel.getSelectedItem());
  }

  private String getJvmOptions() {
    return ((String) cmbJvmOptionsModel.getSelectedItem());
  }

  private String getJvmPath() {
    return ((String) cmbJvmPathModel.getSelectedItem());
  }

  private String getJarFile() {
    return ((String) cmbJarFileModel.getSelectedItem());
  }

  private String getMainClass() {
    return ((String) cmbMainClassModel.getSelectedItem());
  }

  private void setDefaults() {
    String[] jdkPathsGuess = new String[]{
      System.getProperty("java.home"), //NOI18N
      "/usr/java", //NOI18N
      System.getProperty("JAVA_HOME"), //NOI18N
      System.getProperty("JDK_HOME") //NOI18N
    };

    for (int i = jdkPathsGuess.length - 1; i >= 0; i--) {
      File jvmPathGuess = new File(jdkPathsGuess[i], "bin/java"); //NOI18N
      if (jvmPathGuess.exists()) {
        updateModel(cmbJvmPathModel, jvmPathGuess.getPath());
        File demoJar = new File(jdkPathsGuess[i], "demo/jfc/Java2D/Java2Demo.jar"); //NOI18N
        if (demoJar.exists()) {
          updateModel(cmbJarFileModel, demoJar.getPath());
        }
      }
    }

    updateModel(cmbJarFileModel, ""); //NOI18N
    updateModel(cmbJvmOptionsModel, "-Xmx128m  -XX:+ExtendedDTraceProbes"); //NOI18N
    updateModel(cmbWorkingDirModel, "/tmp"); //NOI18N
  }

  void initComponents() {
    setLayout(new GridLayout(8, 1));

    btnBrowseJDK.addActionListener(this);
    btnBrowseJARFile.addActionListener(this);
    btnBrowseWorkingDir.addActionListener(this);

    btnBrowseJDK.setToolTipText(NbBundle.getMessage(SelectJavaApplicationDialog.class,
        "SelectJavaApplicationDialog.BrowseJDK.Tooltip")); //NOI18N
    btnBrowseJARFile.setToolTipText(NbBundle.getMessage(SelectJavaApplicationDialog.class,
        "SelectJavaApplicationDialog.BrowseJar.Tooltip")); //NOI18N
    btnBrowseWorkingDir.setToolTipText(NbBundle.getMessage(SelectJavaApplicationDialog.class,
        "SelectTarget.BrowseWorkingDir.Tooltip")); //NOI18N


    addPanel(NbBundle.getMessage(SelectJavaApplicationDialog.class,"SelectJavaApplicationDialog.JVMPath"), new JComboBox2(cmbJvmPathModel, 10, true, fileValidator), btnBrowseJDK); //NOI18N
    addPanel(NbBundle.getMessage(SelectJavaApplicationDialog.class,"SelectJavaApplicationDialog.JVMOptions"), new JComboBox2(cmbJvmOptionsModel, 10, true, null), null); //NOI18N
    addPanel(NbBundle.getMessage(SelectJavaApplicationDialog.class,"SelectJavaApplicationDialog.JARFile"), new JComboBox2(cmbJarFileModel, 10, true, fileValidator), btnBrowseJARFile); //NOI18N
    addPanel(NbBundle.getMessage(SelectJavaApplicationDialog.class,"SelectJavaApplicationDialog.MainClass"), new JComboBox2(cmbMainClassModel, 10, true, null), null); //NOI18N
    addPanel(NbBundle.getMessage(SelectJavaApplicationDialog.class,"SelectTarget.Arguments"), new JComboBox2(cmbArgumentsModel, 10, true, null), null); //NOI18N
    addPanel(NbBundle.getMessage(SelectJavaApplicationDialog.class,"SelectJavaApplicationDialog.ClassPath"), new JComboBox2(cmbClassPathModel, 10, true, null), null); //NOI18N
    addPanel(NbBundle.getMessage(SelectJavaApplicationDialog.class,"SelectTarget.WorkingDir"), new JComboBox2(cmbWorkingDirModel, 10, true, workingDirValidator), btnBrowseWorkingDir); //NOI18N

  }

  @Override
  public void actionPerformed(ActionEvent e) {
    super.actionPerformed(e);

    Object src = e.getSource();
    if (!(src instanceof JButton)) {
      return;
    }

    if (src == btnBrowseJDK) {
      String jvmPath = selectFile((String) cmbJvmPathModel.getSelectedItem());
      updateModel(cmbJvmPathModel, jvmPath);
    } else if (src == btnBrowseJARFile) {
      String jarFile = selectFile((String) cmbJarFileModel.getSelectedItem(),
              new FileFilter() {

                @Override
                public boolean accept(File f) {
                  if (f != null) {
                    if (f.isDirectory()) {
                      return true;
                    }

                    String fileName = f.getName();
                    int i = fileName.lastIndexOf('.');
                    if (i > 0 && i < fileName.length() - 1) {
                      String desiredExtension = fileName.substring(i + 1).toLowerCase(Locale.ENGLISH);
                      if (desiredExtension.equals("jar")) { //NOI18N
                        return true;
                      }
                    }
                  }
                  return false;
                }

                @Override
                public String getDescription() {
                  return "JAR file (*.jar)"; //NOI18N
                }
              });

      updateModel(cmbJarFileModel, jarFile);
    } else if (src == btnBrowseWorkingDir) {
      String workDir = selectDirectory((String) cmbWorkingDirModel.getSelectedItem(), workingDirValidator);
      updateModel(cmbWorkingDirModel, workDir);
    }
  }

  @Override
  public void approveSelection() {
    String jarFile = getJarFile();
    String mainClass = getMainClass();

    if ((jarFile == null || jarFile.equals("")) && //NOI18N
            (mainClass == null || mainClass.equals(""))) { //NOI18N
      JOptionPane.showMessageDialog(this, NbBundle.getMessage(SelectExecutableTargetDialog.class, "SelectJavaApplicationDialog.SpecifyJarOrMainClass")); //NOI18N

    } else {
      super.approveSelection();
    }
  }

  public String getProgramName() {
    return getJvmPath();
  }
}


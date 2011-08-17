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

package org.netbeans.modules.profiler.j2ee;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.profiler.spi.LoadGenPlugin;
import org.netbeans.modules.profiler.nbimpl.project.ProjectUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author  Jaroslav Bachorik
 */
public class LoadGenPanel extends javax.swing.JPanel {
  final private static String LASTFILE_PROPERTY = LoadGenPanel.class.getName() + "_lastfile"; // NOI18N
  final private static String ENABLESELECTOR_PROPERTY = LoadGenPanel.class.getName() + "_enable"; // NOI18N
  public static final String PATH = "org.netbeans.modules.profiler.j2ee.LoadGenPanel#path"; // NOI18N
  
  private String lastSelectedPath = null;
  private FriendlyFileObject lastSelectedFile = null;
  private Boolean shouldEnableSelector = false;
  
  private Project attachedProject = null;
  private Set<String> supportedExtensions = null;
  
  private DefaultComboBoxModel model = new DefaultComboBoxModel();
  
  volatile private boolean internalChanges = false;
  
  private class FriendlyFileObject {
    private FileObject delegate;
    
    public FriendlyFileObject(final FileObject obj) {
      delegate = obj;
    }
    
    public FileObject getFileObject() {
      return delegate;
    }
    
    @Override
    public String toString() {
      return delegate != null ? delegate.getNameExt() : ""; // NOI18N
    }
    
    @Override
    public int hashCode() {
      return delegate != null ? delegate.hashCode() : 0;
    }
    
    @Override
    public boolean equals(Object obj) {
      if (delegate == null) return false;
      if (obj == null) return false;
      if (!(obj instanceof FriendlyFileObject)) return false;
      if (((FriendlyFileObject)obj).delegate == null) return false;
      return delegate.equals(((FriendlyFileObject)obj).delegate);
    }
  }
  
  /** Creates new form LoadGenPanel */
  private LoadGenPanel() {
    initComponents();
  }
  
  // Singleton model to allow easy access from both J2EEProjectTypeProfiler and J2EEProfilingSettingsSupportProvider
  private static LoadGenPanel INSTANCE = null;
  public static synchronized LoadGenPanel instance() {
      if (!hasInstance()) INSTANCE = new LoadGenPanel();
      return INSTANCE;
  }
  public static synchronized boolean hasInstance() {
      return INSTANCE != null;
  }
  //
  
  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scriptsCombo = new javax.swing.JComboBox();
        scriptsEnabled = new javax.swing.JCheckBox();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 15, 5, 5));

        scriptsCombo.setModel(getModel());
        scriptsCombo.setEnabled(false);
        scriptsCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pathChangeListener(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(scriptsEnabled, org.openide.util.NbBundle.getMessage(LoadGenPanel.class, "LoadGenPanel.scriptsEnabled.text")); // NOI18N
        scriptsEnabled.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scriptsEnabled.setMargin(new java.awt.Insets(0, 0, 0, 0));
        scriptsEnabled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scriptsEnabledHandler(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(scriptsEnabled)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scriptsCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(scriptsEnabled)
                .addComponent(scriptsCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        scriptsCombo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LoadGenPanel.class, "LoadGenPanel.scriptsCombo.AccessibleContext.accessibleName")); // NOI18N
        scriptsCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LoadGenPanel.class, "LoadGenPanel.scriptsCombo.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
  
  private void scriptsEnabledHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scriptsEnabledHandler
    if (internalChanges) return;
    lastSelectedFile = (FriendlyFileObject)getModel().getSelectedItem();
    String path = getSelectedScript();
    firePropertyChange(PATH, lastSelectedPath, path);
    scriptsCombo.setEnabled(scriptsEnabled.isSelected());
    shouldEnableSelector = scriptsEnabled.isSelected();
    lastSelectedPath = path;
  }//GEN-LAST:event_scriptsEnabledHandler
  
  private void pathChangeListener(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pathChangeListener
    if (internalChanges) return;
    lastSelectedFile = (FriendlyFileObject)getModel().getSelectedItem();
    String path = getSelectedScript();
    firePropertyChange(PATH, lastSelectedPath, path);
    lastSelectedPath = path;
  }//GEN-LAST:event_pathChangeListener
  
  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox scriptsCombo;
    private javax.swing.JCheckBox scriptsEnabled;
    // End of variables declaration//GEN-END:variables
    
  public void attach(final Project project) {
    internalChanges = true;
    try {
      attachedProject = project;
      List<FileObject> allScripts = new ArrayList<FileObject>();
      allScripts.addAll(findScripts(project));
      model.removeAllElements();
      for(FileObject fo : allScripts) {
        model.addElement(new FriendlyFileObject(fo));
      }
      adjustUI();
    } finally {
      internalChanges = false;
    }
  }
  
  public String getSelectedScript() {
    if (!scriptsEnabled.isSelected())
      return null;
    
    if (scriptsCombo.getSelectedItem() == null)
      return null;
    
    try {
      Object script = scriptsCombo.getSelectedItem();
      if (script instanceof FriendlyFileObject) {
        FileObject pathFo = ((FriendlyFileObject)script).getFileObject();
        String path = FileUtil.toFile(pathFo).getCanonicalPath();
        return path;
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    
    return null;
  }
  
  public void loadCustomSettings(Properties settings) {
    if (settings != null) {
      lastSelectedFile = null;
      String lastScriptPath = settings.getProperty(LASTFILE_PROPERTY);
      if (lastScriptPath != null) {
        File lastScriptFile = new File(lastScriptPath);
        if (lastScriptFile.exists()) {
          FileObject scriptFO = FileUtil.toFileObject(lastScriptFile);
          if (scriptFO != null) {
            lastSelectedFile = new FriendlyFileObject(scriptFO);
          }
        }
      }
      shouldEnableSelector = false;
      String enablerText = settings.getProperty(ENABLESELECTOR_PROPERTY);
      if (enablerText != null) {
        shouldEnableSelector = Boolean.parseBoolean(enablerText);
      }
      adjustUI();
    }
  }
  
  public void storeCustomSettings(Properties settings) {
    if (settings != null) {
      if (lastSelectedFile != null) {
        String path = FileUtil.toFile(lastSelectedFile.delegate).getAbsolutePath();
        settings.setProperty(LASTFILE_PROPERTY, path);
      }
      settings.setProperty(ENABLESELECTOR_PROPERTY, shouldEnableSelector.toString());
    }
  }
  
  private List<FileObject> findScripts(final Project project) {
    List<FileObject> scripts = new ArrayList<FileObject>();
    LoadGenPlugin plugin = Lookup.getDefault().lookup(LoadGenPlugin.class);
    if (plugin != null) {
      scripts.addAll(plugin.listScripts(project));
    }
    return scripts;
  }
  
  private ComboBoxModel getModel() {
    return model;
  }
  
  private void adjustUI() {
    if (isModelEmpty()) {
      scriptsEnabled.setSelected(false);
      scriptsEnabled.setEnabled(false);
      scriptsCombo.setEnabled(false);
      model.addElement(NbBundle.getMessage(this.getClass(), "LoadGenPanel_NoScripts")); // NOI18N
      firePropertyChange(PATH, null, null);
    } else {
      scriptsEnabled.setSelected(shouldEnableSelector);
      scriptsEnabled.setEnabled(true);
      scriptsCombo.setEnabled(scriptsEnabled.isSelected());
      if (lastSelectedFile != null && model.getIndexOf(lastSelectedFile) > -1) {
        model.setSelectedItem(lastSelectedFile);
      }
      String path = getSelectedScript();
      firePropertyChange(PATH, lastSelectedPath, path);
      lastSelectedPath = path;
    }
  }
  
  private boolean isModelEmpty() {
    return model.getSize() == 0 || model.getElementAt(0).equals(NbBundle.getMessage(this.getClass(), "LoadGenPanel_NoScripts")); // NOI18N
  }
}

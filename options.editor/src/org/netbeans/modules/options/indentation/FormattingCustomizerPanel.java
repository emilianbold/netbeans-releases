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

package org.netbeans.modules.options.indentation;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
public final class FormattingCustomizerPanel extends javax.swing.JPanel implements ActionListener {
    
    // ------------------------------------------------------------------------
    // ProjectCustomizer.CompositeCategoryProvider implementation
    // ------------------------------------------------------------------------

    public static class Factory implements ProjectCustomizer.CompositeCategoryProvider {
 
        private static final String CATEGORY_FORMATTING = "Formatting"; // NOI18N

        public ProjectCustomizer.Category createCategory(Lookup context) {
            return context.lookup(Project.class) == null ? null : ProjectCustomizer.Category.create(
                    CATEGORY_FORMATTING, 
                    NbBundle.getMessage(Factory.class, "LBL_CategoryFormatting"), //NOI18N
                    null);
        }

        public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
            FormattingCustomizerPanel customizerPanel = new FormattingCustomizerPanel(context);
            category.setStoreListener(customizerPanel);
            return customizerPanel;
        }
    } // End of Factory class
    
    // ------------------------------------------------------------------------
    // ActionListener implementation
    // ------------------------------------------------------------------------

    // this is called when OK button is clicked to store the controlled preferences
    public void actionPerformed(ActionEvent e) {
        String profile = globalButton.isSelected() ? DEFAULT_PROFILE : PROJECT_PROFILE;
        pf.getPreferences("").parent().put(USED_PROFILE, profile); //NOI18N
        pf.applyChanges();
    }

    // ------------------------------------------------------------------------
    // private implementation
    // ------------------------------------------------------------------------
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        group = new javax.swing.ButtonGroup();
        globalButton = new javax.swing.JRadioButton();
        editGlobalButton = new javax.swing.JButton();
        projectButton = new javax.swing.JRadioButton();
        loadButton = new javax.swing.JButton();
        customizerPanel = new javax.swing.JPanel();

        group.add(globalButton);
        globalButton.setText(org.openide.util.NbBundle.getMessage(FormattingCustomizerPanel.class, "LBL_FormattingCustomizer_Global")); // NOI18N
        globalButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                globalButtonActionPerformed(evt);
            }
        });

        editGlobalButton.setText(org.openide.util.NbBundle.getMessage(FormattingCustomizerPanel.class, "LBL_FormattingCustomizer_EditGlobal")); // NOI18N
        editGlobalButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editGlobalButtonActionPerformed(evt);
            }
        });

        group.add(projectButton);
        projectButton.setText(org.openide.util.NbBundle.getMessage(FormattingCustomizerPanel.class, "LBL_FormattingCustomizer_Project")); // NOI18N
        projectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectButtonActionPerformed(evt);
            }
        });

        loadButton.setText(org.openide.util.NbBundle.getMessage(FormattingCustomizerPanel.class, "LBL_ForamttingCustomizer_Load")); // NOI18N
        loadButton.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        loadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadButtonActionPerformed(evt);
            }
        });

        customizerPanel.setLayout(new java.awt.BorderLayout());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(projectButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE)
                    .add(globalButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE))
                .add(14, 14, 14)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(editGlobalButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(loadButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .add(layout.createSequentialGroup()
                .add(12, 12, 12)
                .add(customizerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(globalButton)
                    .add(editGlobalButton))
                .add(8, 8, 8)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectButton)
                    .add(loadButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(customizerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 239, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void globalButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_globalButtonActionPerformed
    loadButton.setEnabled(false);
    setEnabled(panel, false);
}//GEN-LAST:event_globalButtonActionPerformed

private void projectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectButtonActionPerformed
    loadButton.setEnabled(true);
    setEnabled(panel, true);
}//GEN-LAST:event_projectButtonActionPerformed

private void loadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadButtonActionPerformed
    JFileChooser chooser = ProjectChooser.projectChooser();
    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        File f = chooser.getSelectedFile();
        FileObject fo = FileUtil.toFileObject(f);
        if (fo != null) {
            try {
                Project p = ProjectManager.getDefault().findProject(fo);
//                controller.loadFrom(FmtOptions.getProjectPreferences(p));
            } catch (Exception e) {}
        }
    }
}//GEN-LAST:event_loadButtonActionPerformed

private void editGlobalButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editGlobalButtonActionPerformed
    OptionsDisplayer.getDefault().open(GLOBAL_OPTIONS_CATEGORY);
}//GEN-LAST:event_editGlobalButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel customizerPanel;
    private javax.swing.JButton editGlobalButton;
    private javax.swing.JRadioButton globalButton;
    private javax.swing.ButtonGroup group;
    private javax.swing.JButton loadButton;
    private javax.swing.JRadioButton projectButton;
    // End of variables declaration//GEN-END:variables

    private static final Logger LOG = Logger.getLogger(FormattingCustomizerPanel.class.getName());
    
    private static final String GLOBAL_OPTIONS_CATEGORY = "Editor/Formating"; //NOI18N
    private static final String CODE_STYLE_PROFILE = "CodeStyle"; // NOI18N
    private static final String DEFAULT_PROFILE = "default"; // NOI18N
    private static final String PROJECT_PROFILE = "project"; // NOI18N
    private static final String USED_PROFILE = "usedProfile"; // NOI18N
    
    private final ProjectPreferencesFactory pf;
    private final CustomizerSelector selector;
    private final FormattingPanel panel;
    
    /** Creates new form CodeStyleCustomizerPanel */
    private FormattingCustomizerPanel(Lookup context) {
        this.pf = new ProjectPreferencesFactory(context.lookup(Project.class));
        this.selector = new CustomizerSelector(pf);
        this.panel = new FormattingPanel();
        this.panel.setSelector(selector);

        initComponents();
        customizerPanel.add(panel, BorderLayout.CENTER);
        
        Preferences prefs = pf.getPreferences("").parent(); //NOI18N
        String profile = prefs.get(USED_PROFILE, DEFAULT_PROFILE);
        if (DEFAULT_PROFILE.equals(profile)) {
            globalButton.doClick();
        } else {
            projectButton.doClick();
        }
    }
    
    private void setEnabled(Component component, boolean enabled) {
        component.setEnabled(enabled);
        if (component instanceof Container) {
            for (Component c : ((Container)component).getComponents())
                setEnabled(c, enabled);
        }
    }

    private static final class ProjectPreferencesFactory implements CustomizerSelector.PreferencesFactory {

        public ProjectPreferencesFactory(Project project) {
            this.project = project;
        }

        public synchronized Preferences getPreferences(String mimeType) {
            ProxyPreferences prefs = mimeTypePreferences.get(mimeType);

            if (prefs == null) {
                if (projectPrefs == null) {
                    Preferences p = ProjectUtils.getPreferences(project, IndentUtils.class, true);
                    projectPrefs = ProxyPreferences.get(p);
                }
                prefs = (ProxyPreferences) projectPrefs.node(mimeType).node(CODE_STYLE_PROFILE).node(PROJECT_PROFILE);
                mimeTypePreferences.put(mimeType, prefs);
            }

            return prefs;
        }

        public synchronized void applyChanges() {
            for(String mimeType : mimeTypePreferences.keySet()) {
                if (mimeType.length() == 0) {
                    continue;
                }

                ProxyPreferences pp = mimeTypePreferences.get(mimeType);
                pp.silence();

                assert pp.get(FormattingPanelController.OVERRIDE_GLOBAL_FORMATTING_OPTIONS, null) != null;
                if (!pp.getBoolean(FormattingPanelController.OVERRIDE_GLOBAL_FORMATTING_OPTIONS, false)) {
                    // remove the basic settings if a language is not overriding the 'all languages' values
                    pp.remove(SimpleValueNames.EXPAND_TABS);
                    pp.remove(SimpleValueNames.INDENT_SHIFT_WIDTH);
                    pp.remove(SimpleValueNames.SPACES_PER_TAB);
                    pp.remove(SimpleValueNames.TAB_SIZE);
                    pp.remove(SimpleValueNames.TEXT_LIMIT_WIDTH);
                }
                pp.remove(FormattingPanelController.OVERRIDE_GLOBAL_FORMATTING_OPTIONS);
                
                try {
                    LOG.fine("Flushing pp for '" + mimeType + "'"); //NOI18N
                    pp.flush();
                } catch (BackingStoreException ex) {
                    LOG.log(Level.WARNING, "Can't flush preferences for '" + mimeType + "'", ex); //NOI18N
                }
            }

            // flush the root prefs
            ProxyPreferences pp = mimeTypePreferences.get("");
            if (pp != null) {
                assert pp.get(FormattingPanelController.OVERRIDE_GLOBAL_FORMATTING_OPTIONS, null) == null;
                pp.silence();

                try {
                    LOG.fine("Flushing root pp"); //NOI18N
                    pp.parent().flush();
                } catch (BackingStoreException ex) {
                    LOG.log(Level.WARNING, "Can't flush project codestyle root preferences", ex); //NOI18N
                }
            }
            
            projectPrefs.destroy();
            projectPrefs = null;
        }

        public boolean isKeyOverridenForMimeType(String key, String mimeType) {
            Preferences p = ProjectUtils.getPreferences(project, IndentUtils.class, true).node(CODE_STYLE_PROFILE);
            p = p.node(mimeType);
            return p.get(key, null) != null;
        }

        // --------------------------------------------------------------------
        // private implementation
        // --------------------------------------------------------------------

        private final Project project;
        private final Map<String, ProxyPreferences> mimeTypePreferences = new HashMap<String, ProxyPreferences>();
        private ProxyPreferences projectPrefs;

    } // End of ProjectPreferencesFactory class
}

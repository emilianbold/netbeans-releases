/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.customizer;

import java.awt.Component;
import java.beans.BeanInfo;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.customizer.ModelHandle;
import org.netbeans.modules.maven.nodes.DependencyNode;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author  mkleint
 */
public class ScopedDependenciesPanel extends javax.swing.JPanel  {
    private int type = COMPILE;
    static final int COMPILE = 0;
    static final int TEST = 2;
    static final int RUN = 1;
    private NbMavenProjectImpl project;
    private ModelHandle handle;
    /** Creates new form ScopedDependenciesPanel */
    public ScopedDependenciesPanel(int type, NbMavenProjectImpl prj, ModelHandle hndl) {
        initComponents();
        this.type = type;
        project = prj;
        handle = hndl;
        lstDependencies.setCellRenderer(new MyRenderer());
        switch (type) {
            case COMPILE :
                Mnemonics.setLocalizedText(lblDependencies, NbBundle.getMessage(ScopedDependenciesPanel.class, "ScopedDependenciesPanel.lblDependencies.text")); // NOI18N
                break;
            case RUN :
                Mnemonics.setLocalizedText(lblDependencies, NbBundle.getMessage(ScopedDependenciesPanel.class, "ScopedDependenciesPanel.lblDependencies.text2")); // NOI18N
                break;
            case TEST :
                Mnemonics.setLocalizedText(lblDependencies, NbBundle.getMessage(ScopedDependenciesPanel.class, "ScopedDependenciesPanel.lblDependencies.text3")); // NOI18N
                break;
        }
        createChildren();
        lstDependencies.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                Object[] obj = lstDependencies.getSelectedValues();
                boolean isAbstract = false;
                boolean noSelected = obj.length == 0;
                for (int i = 0; i < obj.length; i++) {
                    Object object = obj[i];
                    if (object instanceof String) {
                        isAbstract = true;
                    }
                }
                updateButtons(isAbstract, noSelected);
                if (obj.length == 1) {
                    if (obj[0] instanceof DependencyNode) {
                        DependencyNode nd = (DependencyNode)obj[0];
                        btnSource.setEnabled(!nd.hasSourceInRepository());
                        btnInstall.setEnabled(!nd.isLocal());
                        btnJavadoc.setEnabled(!nd.hasJavadocInRepository());
                    }
                } else {
                    btnSource.setEnabled(false);
                    btnInstall.setEnabled(false);
                    btnJavadoc.setEnabled(false);
                }
            }
        });
        updateButtons(false, true);
    }
    
    private void updateButtons(boolean abstractSelected, boolean anySelected) {
        btnRemove.setEnabled(!(abstractSelected || anySelected));
        btnSource.setEnabled(!(abstractSelected || anySelected));
        btnInstall.setEnabled(!(abstractSelected || anySelected));
        btnJavadoc.setEnabled(!(abstractSelected || anySelected));
        
    }
            
    
    public void setTransitive(boolean trans) {
        createChildren();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblDependencies = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstDependencies = new javax.swing.JList();
        btnAddLibrary = new javax.swing.JButton();
        btnInstall = new javax.swing.JButton();
        btnJavadoc = new javax.swing.JButton();
        btnSource = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();

        lblDependencies.setLabelFor(lstDependencies);
        org.openide.awt.Mnemonics.setLocalizedText(lblDependencies, org.openide.util.NbBundle.getMessage(ScopedDependenciesPanel.class, "ScopedDependenciesPanel.lblDependencies.text")); // NOI18N

        jScrollPane1.setViewportView(lstDependencies);

        org.openide.awt.Mnemonics.setLocalizedText(btnAddLibrary, org.openide.util.NbBundle.getMessage(ScopedDependenciesPanel.class, "ScopedDependenciesPanel.btnAddLibrary.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnInstall, org.openide.util.NbBundle.getMessage(ScopedDependenciesPanel.class, "ScopedDependenciesPanel.btnInstall.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnJavadoc, org.openide.util.NbBundle.getMessage(ScopedDependenciesPanel.class, "ScopedDependenciesPanel.btnJavadoc.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnSource, org.openide.util.NbBundle.getMessage(ScopedDependenciesPanel.class, "ScopedDependenciesPanel.btnSource.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnRemove, org.openide.util.NbBundle.getMessage(ScopedDependenciesPanel.class, "ScopedDependenciesPanel.btnRemove.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(btnAddLibrary)
                            .add(btnInstall)
                            .add(btnJavadoc)
                            .add(btnSource)
                            .add(btnRemove)))
                    .add(lblDependencies))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {btnAddLibrary, btnInstall, btnJavadoc, btnRemove, btnSource}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(lblDependencies)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(btnAddLibrary)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnInstall)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnJavadoc)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnSource)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnRemove))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddLibrary;
    private javax.swing.JButton btnInstall;
    private javax.swing.JButton btnJavadoc;
    private javax.swing.JButton btnRemove;
    private javax.swing.JButton btnSource;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblDependencies;
    private javax.swing.JList lstDependencies;
    // End of variables declaration//GEN-END:variables
    
    private void createChildren() {
        Model mdl = handle.getPOMModel();
        List<Dependency> lst = mdl.getDependencies();
        DefaultListModel keys = new DefaultListModel();
        if (type == RUN) {
            keys.addElement("Classpath for Compiling Sources");
        }
        if (type == TEST) {
            keys.addElement("Classpath for Running Sources");
        }
        for (Dependency dep : lst) {
            if (type == RUN && Artifact.SCOPE_RUNTIME.equals(dep.getScope())) {
                keys.addElement(createDepNode(dep));
            }
            if (type == TEST && Artifact.SCOPE_TEST.equals(dep.getScope())) {
                keys.addElement(createDepNode(dep));
            }
            if (type == COMPILE && (   !Artifact.SCOPE_RUNTIME.equals(dep.getScope()) 
                                    && !Artifact.SCOPE_TEST.equals(dep.getScope()))) {
                keys.addElement(createDepNode(dep));
            }
        }
        lstDependencies.setModel(keys);
    }
    
    private Object createDepNode(Dependency dep) {
        Artifact art = getArtForDep(dep);
        if (art != null) {
            return new DependencyNode(Lookups.fixed(art, project, dep), false);
        }
        return dep;
    }
    
    private Artifact getArtForDep(Dependency dep) {
        List<Artifact> lst = handle.getProject().getTestArtifacts();
        for (Artifact ar : lst) {
            if (dep.getManagementKey().equals(ar.getDependencyConflictId())) {
                return ar;
            }
        }
        return null;
    }
    
    
    private class MyRenderer extends DefaultListCellRenderer {
            public Component getListCellRendererComponent(JList arg0,
                                                          Object value, int arg2,
                                                          boolean arg3,
                                                          boolean arg4) {
                Component supers;
                if (value instanceof String) {
                    supers = super.getListCellRendererComponent(arg0, value, arg2, arg3, arg4);
                    if (supers instanceof JLabel) {
                        ((JLabel)supers).setIcon(new ImageIcon(Utilities.loadImage("org/netbeans/modules/maven/customizer/referencedClasspath.gif")));
                    }
                } else if (value instanceof Dependency) {
                    Dependency dep = (Dependency)value;
                    supers = super.getListCellRendererComponent(arg0, dep.getArtifactId() + "  " + dep.getVersion(),
                            arg2, arg3, arg4);
                    if (supers instanceof JLabel) {
                        ((JLabel)supers).setIcon(new ImageIcon(Utilities.loadImage("org/netbeans/modules/maven/customizer/libraries.gif")));
                    }
                } else {
                    DependencyNode nd = (DependencyNode)value;
                    Dependency dep = nd.getLookup().lookup(Dependency.class);
                    supers = super.getListCellRendererComponent(arg0, dep.getArtifactId() + "  " + dep.getVersion(),
                            arg2, arg3, arg4);
                    if (supers instanceof JLabel) {
                        ((JLabel)supers).setIcon(new ImageIcon(nd.getIcon(BeanInfo.ICON_COLOR_16x16)));
                    }
                }
                return supers;
            }
        
    }
}

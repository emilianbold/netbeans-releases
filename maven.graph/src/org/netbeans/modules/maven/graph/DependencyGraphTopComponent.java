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

package org.netbeans.modules.maven.graph;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.netbeans.api.project.Project;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.openide.awt.Mnemonics;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

/**
 * component showing graph of dependencies for project.
 * @author Milos Kleint 
 */
public class DependencyGraphTopComponent extends TopComponent implements LookupListener, MultiViewElement {
//    public static final String ATTRIBUTE_DEPENDENCIES_LAYOUT = "MavenProjectDependenciesLayout"; //NOI18N
    
//    private Project project;
    private Lookup.Result<DependencyNode> result;
    private Lookup.Result<MavenProject> result2;
    private Lookup.Result<POMModel> result3;

    private DependencyGraphScene scene;
    private MultiViewElementCallback callback;
    final JScrollPane pane = new JScrollPane();
    
    private HighlightVisitor highlightV;
    
    private Timer timer = new Timer(500, new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
            checkFindValue();
        }
    });
    private JToolBar toolbar;
    
    /** Creates new form ModulesGraphTopComponent */
    public DependencyGraphTopComponent(Lookup lookup) {
        super();
        associateLookup(lookup);
        initComponents();
//        project = proj;
        //sldDepth.getLabelTable().put(new Integer(0), new JLabel(NbBundle.getMessage(DependencyGraphTopComponent.class, "LBL_All")));
        timer.setDelay(500);
        timer.setRepeats(false);
        txtFind.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent arg0) {
                timer.restart();
            }

            public void removeUpdate(DocumentEvent arg0) {
                timer.restart();
            }

            public void changedUpdate(DocumentEvent arg0) {
                timer.restart();
            }
        });
        comScopes.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                @SuppressWarnings("unchecked")
                int scopesSize = ((List<String>) value).size();
                String bundleKey;
                if (scopesSize == 0) {
                    bundleKey = "LBL_Scope_All";
                } else if (scopesSize == 2) {
                    bundleKey = "LBL_Scope_Compile";
                } else if (scopesSize == 3) {
                    bundleKey = "LBL_Scope_Runtime";
                } else {
                    bundleKey = "LBL_Scope_Test";
                }

                return super.getListCellRendererComponent(list,
                        NbBundle.getMessage(DependencyGraphTopComponent.class, bundleKey),
                        index, isSelected, cellHasFocus);
            }
        });
        DefaultComboBoxModel mdl = new DefaultComboBoxModel();
        mdl.addElement(Arrays.asList(new String[0]));
        mdl.addElement(Arrays.asList(new String[] {
            Artifact.SCOPE_PROVIDED,
            Artifact.SCOPE_COMPILE
        }));
        mdl.addElement(Arrays.asList(new String[] {
            Artifact.SCOPE_PROVIDED,
            Artifact.SCOPE_COMPILE,
            Artifact.SCOPE_RUNTIME
        }));
        mdl.addElement(Arrays.asList(new String[] {
            Artifact.SCOPE_PROVIDED,
            Artifact.SCOPE_COMPILE,
            Artifact.SCOPE_RUNTIME,
            Artifact.SCOPE_TEST
        }));
        comScopes.setModel(mdl);
        comScopes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (scene != null) {
                    @SuppressWarnings("unchecked")
                    List<String> selected = (List<String>) comScopes.getSelectedItem();
                    ScopesVisitor vis = new ScopesVisitor(scene, selected);
                    scene.getRootGraphNode().getArtifact().accept(vis);
                    scene.validate();
                    scene.repaint();
                    revalidate();
                    repaint();
                }
            }
        });
    }
    
    private void checkFindValue() {
        String val = txtFind.getText().trim();
        if ("".equals(val)) { //NOI18N
            val = null;
        }
        SearchVisitor visitor = new SearchVisitor(scene);
        visitor.setSearchString(val);
        DependencyNode node = scene.getRootGraphNode().getArtifact();
        node.accept(visitor);
        scene.validate();
        scene.repaint();
        revalidate();
        repaint();

    }
    
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }
    
    @Override
    public void componentOpened() {
        super.componentOpened();
        pane.setWheelScrollingEnabled(true);
        maxPathSpinner.setEnabled(false);
        maxPathSpinner.setVisible(false);
        lblPath.setVisible(false);
        txtFind.setEnabled(false);
        btnBigger.setEnabled(false);
        btnSmaller.setEnabled(false);
        comScopes.setEnabled(false);
        add(pane, BorderLayout.CENTER);
        setPaneText(NbBundle.getMessage(DependencyGraphTopComponent.class, "LBL_Loading"), true);
        result = getLookup().lookup(new Lookup.Template<DependencyNode>(DependencyNode.class));
        result.addLookupListener(this);
        result2 = getLookup().lookup(new Lookup.Template<MavenProject>(MavenProject.class));
        result2.addLookupListener(this);
        result3 = getLookup().lookup(new Lookup.Template<POMModel>(POMModel.class));
        result3.addLookupListener(this);
        createScene();
    }
    
    @Override
    public void componentActivated() {
        super.componentActivated();
    }

    @Override
    public void componentClosed() {
        super.componentClosed();
    }

    @Override
    public void componentDeactivated() {
        super.componentDeactivated();
    }

    @Override
    public void componentHidden() {
        super.componentHidden();
    }

    @Override
    public void componentShowing() {
        super.componentShowing();
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        btnBigger = new javax.swing.JButton();
        btnSmaller = new javax.swing.JButton();
        lblFind = new javax.swing.JLabel();
        txtFind = new javax.swing.JTextField();
        lblPath = new javax.swing.JLabel();
        maxPathSpinner = new javax.swing.JSpinner();
        lblScopes = new javax.swing.JLabel();
        comScopes = new javax.swing.JComboBox();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        btnBigger.setIcon(ImageUtilities.image2Icon(ImageUtilities.loadImage("org/netbeans/modules/maven/graph/zoomin.gif", true)));
        btnBigger.setFocusable(false);
        btnBigger.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnBigger.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnBigger.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBiggerActionPerformed(evt);
            }
        });
        jToolBar1.add(btnBigger);

        btnSmaller.setIcon(ImageUtilities.image2Icon(ImageUtilities.loadImage("org/netbeans/modules/maven/graph/zoomout.gif", true)));
        btnSmaller.setFocusable(false);
        btnSmaller.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSmaller.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSmaller.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSmallerActionPerformed(evt);
            }
        });
        jToolBar1.add(btnSmaller);

        org.openide.awt.Mnemonics.setLocalizedText(lblFind, org.openide.util.NbBundle.getMessage(DependencyGraphTopComponent.class, "DependencyGraphTopComponent.lblFind.text")); // NOI18N
        jToolBar1.add(lblFind);

        txtFind.setMaximumSize(new java.awt.Dimension(200, 19));
        txtFind.setMinimumSize(new java.awt.Dimension(50, 19));
        txtFind.setPreferredSize(new java.awt.Dimension(150, 19));
        jToolBar1.add(txtFind);

        jPanel1.add(jToolBar1);

        lblPath.setLabelFor(maxPathSpinner);
        org.openide.awt.Mnemonics.setLocalizedText(lblPath, org.openide.util.NbBundle.getMessage(DependencyGraphTopComponent.class, "DependencyGraphTopComponent.lblPath.text")); // NOI18N
        lblPath.setToolTipText(org.openide.util.NbBundle.getMessage(DependencyGraphTopComponent.class, "DependencyGraphTopComponent.maxPathSpinner.toolTipText")); // NOI18N
        jPanel1.add(lblPath);

        maxPathSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, 5, 1));
        maxPathSpinner.setToolTipText(org.openide.util.NbBundle.getMessage(DependencyGraphTopComponent.class, "DependencyGraphTopComponent.maxPathSpinner.toolTipText")); // NOI18N
        maxPathSpinner.setRequestFocusEnabled(false);
        maxPathSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                maxPathSpinnerStateChanged(evt);
            }
        });
        jPanel1.add(maxPathSpinner);

        org.openide.awt.Mnemonics.setLocalizedText(lblScopes, org.openide.util.NbBundle.getMessage(DependencyGraphTopComponent.class, "DependencyGraphTopComponent.lblScopes.text")); // NOI18N
        jPanel1.add(lblScopes);
        jPanel1.add(comScopes);

        add(jPanel1, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents
    
    private void btnSmallerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSmallerActionPerformed
        scene.setZoomFactor(scene.getZoomFactor() * 0.8);
        scene.validate();
        scene.repaint();
        if (!pane.getHorizontalScrollBar().isVisible() && 
            !pane.getVerticalScrollBar().isVisible()) {
            revalidate();
            repaint();
        }
        
    }//GEN-LAST:event_btnSmallerActionPerformed
    
    private void btnBiggerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBiggerActionPerformed
        scene.setZoomFactor(scene.getZoomFactor() * 1.2);
        scene.validate();
        scene.repaint();
        if (pane.getHorizontalScrollBar().isVisible() || 
            pane.getVerticalScrollBar().isVisible()) {
            revalidate();
            repaint();
        }
        
    }//GEN-LAST:event_btnBiggerActionPerformed

    private void maxPathSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_maxPathSpinnerStateChanged
        depthHighlight();
    }//GEN-LAST:event_maxPathSpinnerStateChanged

    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBigger;
    private javax.swing.JButton btnSmaller;
    private javax.swing.JComboBox comScopes;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel lblFind;
    private javax.swing.JLabel lblPath;
    private javax.swing.JLabel lblScopes;
    private javax.swing.JSpinner maxPathSpinner;
    private javax.swing.JTextField txtFind;
    // End of variables declaration//GEN-END:variables

    public void resultChanged(LookupEvent ev) {
        createScene();
    }

    /** Highlights/diminishes graph nodes and edges based on path from root depth */
    public void depthHighlight () {
        if (highlightV == null) {
            highlightV = new HighlightVisitor(scene);
        }
        //int value = sldDepth.getValue();
        int value = ((SpinnerNumberModel)maxPathSpinner.getModel()).getNumber().intValue();
        highlightV.setMaxDepth(value);
        DependencyNode node = scene.getRootGraphNode().getArtifact();
        node.accept(highlightV);
        scene.validate();
        scene.repaint();
    }

    JScrollPane getScrollPane () {
        return pane;
    }

    private void createScene() {
        Iterator<? extends DependencyNode> it1 = result.allInstances().iterator();
        Iterator<? extends MavenProject> it2 = result2.allInstances().iterator();
        Iterator<? extends POMModel> it3 = result3.allInstances().iterator();
        final MavenProject prj = it2.hasNext() ? it2.next() : null;
        if (prj != null && "error".equals(prj.getGroupId()) && "error".equals(prj.getArtifactId())) { //NOI18N
            setPaneText(org.openide.util.NbBundle.getMessage(DependencyGraphTopComponent.class, "Err_CannotLoad"), false);
        }
        final Project nbProj = getLookup().lookup(Project.class);
        if (prj != null && it1.hasNext()) {
            final DependencyNode root = it1.next();
            final POMModel model = it3.hasNext() ? it3.next() : null;
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    scene = new DependencyGraphScene(prj, nbProj, DependencyGraphTopComponent.this, model);
                    GraphConstructor constr = new GraphConstructor(scene);
                    root.accept(constr);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            JComponent sceneView = scene.getView();
                            if (sceneView == null) {
                                sceneView = scene.createView();
                                // vlv: print
                                sceneView.putClientProperty("print.printable", Boolean.TRUE); // NOI18N
                            }
                            pane.setViewportView(sceneView);
                            scene.cleanLayout(pane);
                            scene.setSelectedObjects(Collections.singleton(scene.getRootGraphNode()));
                            txtFind.setEnabled(true);
                            btnBigger.setEnabled(true);
                            btnSmaller.setEnabled(true);
                            comScopes.setEnabled(true);
                            if (scene.getMaxNodeDepth() > 1) {
                                lblPath.setVisible(true);
                                ((SpinnerNumberModel)maxPathSpinner.getModel()).
                                        setMaximum(Integer.valueOf(scene.getMaxNodeDepth()));
                                maxPathSpinner.setEnabled(true);
                                maxPathSpinner.setVisible(true);
                            }
                            depthHighlight();
                        }
                    });
                }
            });
        }
    }

    public JComponent getVisualRepresentation() {
        jPanel1.removeAll();
        jToolBar1.removeAll();
        return this;
    }

    public JComponent getToolbarRepresentation() {
        if (toolbar == null) {
            toolbar = new JToolBar();
            toolbar.setFloatable(false);
            toolbar.setRollover(true);
//            Action[] a = new Action[1];
//            Action[] actions = getLookup().lookup(a.getClass());
//            for (Action act : actions) {
//                JButton btn = new JButton();
//                Actions.connect(btn, act);
//                toolbar.add(btn);
//            }
            Dimension space = new Dimension(3, 0);
            toolbar.addSeparator(space);
            toolbar.add(btnBigger);
            toolbar.addSeparator(space);
            toolbar.add(btnSmaller);
            toolbar.addSeparator(space);
            toolbar.add(lblFind);
            toolbar.add(txtFind);
            toolbar.addSeparator(space);
            toolbar.add(lblPath);
            toolbar.add(maxPathSpinner);
            toolbar.addSeparator(space);
            toolbar.add(lblScopes);
            toolbar.add(comScopes);
        }
        return toolbar;
    }

    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
    }

    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    private void setPaneText(String text, boolean progress)  {
        JComponent vView = null;
        if (progress) {
            JPanel panel = new JPanel();
            JProgressBar pb = new JProgressBar();
            JLabel lbl = new JLabel();

            panel.setLayout(new java.awt.GridBagLayout());
            panel.setOpaque(false);

            pb.setIndeterminate(true);
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            panel.add(pb, gridBagConstraints);

            Mnemonics.setLocalizedText(lbl, text);
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
            panel.add(lbl, gridBagConstraints);
            vView = panel;
        } else {
            JLabel lbl = new JLabel(text);
            lbl.setHorizontalAlignment(JLabel.CENTER);
            lbl.setVerticalAlignment(JLabel.CENTER);
            vView = lbl;
        }

        pane.setViewportView(vView);
    }
}

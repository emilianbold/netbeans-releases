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
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * component showing graph of dependencies for project.
 * @author Milos Kleint 
 */
public class DependencyGraphTopComponent extends TopComponent {
//    public static final String ATTRIBUTE_DEPENDENCIES_LAYOUT = "MavenProjectDependenciesLayout"; //NOI18N
    
    private Project project;
    private DependencyGraphScene scene;
    final JScrollPane pane = new JScrollPane();
    
    
    private Timer timer = new Timer(1000, new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
            checkFindValue();
        }
    });
    
    /** Creates new form ModulesGraphTopComponent */
    public DependencyGraphTopComponent(Project proj) {
        initComponents();
        project = proj;
        sldDepth.getLabelTable().put(new Integer(0), new JLabel("All"));
        ProjectInformation info = project.getLookup().lookup(ProjectInformation.class);
        setName("DependencyGraph" + info.getName()); //NOI18N
        setDisplayName(NbBundle.getMessage(DependencyGraphTopComponent.class, 
                "TIT_DepGraphTC", info.getDisplayName()));
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
    protected void componentOpened() {
        super.componentOpened();
        pane.setWheelScrollingEnabled(true);
        sldDepth.setEnabled(false);
        sldDepth.setVisible(false);
        txtFind.setEnabled(false);
        add(pane, BorderLayout.CENTER);
        JLabel lbl = new JLabel(NbBundle.getMessage(DependencyGraphTopComponent.class, "LBL_Loading"));
        lbl.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        lbl.setAlignmentY(JLabel.CENTER_ALIGNMENT);
        pane.setViewportView(lbl);
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                scene = GraphDocumentFactory.createDependencyDocument(project);
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            JComponent sceneView = scene.getView ();
                            if (sceneView == null) {
                                sceneView = scene.createView ();
                            }
                            pane.setViewportView(sceneView);
                            scene.cleanLayout(pane);
                            scene.setSelectedObjects(Collections.singleton(scene.getRootGraphNode()));
                            sldDepth.setMaximum(scene.getMaxNodeDepth());
                            sldDepth.setEnabled(true);
                            sldDepth.setVisible(true);
                            txtFind.setEnabled(true);
                        }
                    });
                } catch (Exception e) {
                    
                }
            }
        });
    }
    
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        btnBigger = new javax.swing.JButton();
        btnSmaller = new javax.swing.JButton();
        lblFind = new javax.swing.JLabel();
        txtFind = new javax.swing.JTextField();
        sldDepth = new javax.swing.JSlider();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        btnBigger.setIcon(new ImageIcon(Utilities.loadImage("org/netbeans/modules/maven/graph/zoomin.gif")));
        btnBigger.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBiggerActionPerformed(evt);
            }
        });
        jPanel1.add(btnBigger);

        btnSmaller.setIcon(new ImageIcon(Utilities.loadImage("org/netbeans/modules/maven/graph/zoomout.gif")));
        btnSmaller.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSmallerActionPerformed(evt);
            }
        });
        jPanel1.add(btnSmaller);

        org.openide.awt.Mnemonics.setLocalizedText(lblFind, org.openide.util.NbBundle.getMessage(DependencyGraphTopComponent.class, "DependencyGraphTopComponent.lblFind.text")); // NOI18N
        jPanel1.add(lblFind);

        txtFind.setMinimumSize(new java.awt.Dimension(100, 19));
        txtFind.setPreferredSize(new java.awt.Dimension(150, 19));
        jPanel1.add(txtFind);

        sldDepth.setMajorTickSpacing(1);
        sldDepth.setMaximum(5);
        sldDepth.setPaintLabels(true);
        sldDepth.setSnapToTicks(true);
        sldDepth.setValue(0);
        sldDepth.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sldDepthStateChanged(evt);
            }
        });
        jPanel1.add(sldDepth);

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

    private void sldDepthStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sldDepthStateChanged
        if (!sldDepth.isEnabled() || sldDepth.getValueIsAdjusting()) {
            return;
        }

        HighlightVisitor visitor = new HighlightVisitor(scene);
        int value = sldDepth.getValue();
        visitor.setMaxDepth(value == 0 ? Integer.MAX_VALUE : value);
        DependencyNode node = scene.getRootGraphNode().getArtifact();
        node.accept(visitor);
        Dimension dim = visitor.getVisibleRectangle().getSize ();
        Dimension viewDim = pane.getViewportBorderBounds ().getSize ();
        scene.setZoomFactor (Math.min ((float) viewDim.width / dim.width, (float) viewDim.height / dim.height));
        scene.validate();
        Rectangle viewpoint = scene.convertSceneToView(visitor.getVisibleRectangle());
        int hgrow = ((viewDim.width - viewpoint.width) / 2) - 5;
        int wgrow = ((viewDim.height - viewpoint.height) / 2) - 5;
        viewpoint.grow(hgrow, wgrow);
        scene.getView().scrollRectToVisible(viewpoint);

        revalidate();
        repaint();

    }//GEN-LAST:event_sldDepthStateChanged

    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBigger;
    private javax.swing.JButton btnSmaller;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblFind;
    private javax.swing.JSlider sldDepth;
    private javax.swing.JTextField txtFind;
    // End of variables declaration//GEN-END:variables
    
}

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
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.visual.widget.BirdViewController;
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
    private BirdViewController birdView;
    private JComponent satelliteView;
    
    
    private Timer timer = new Timer(1000, new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
            checkFindValue();
        }
    });
    
    /** Creates new form ModulesGraphTopComponent */
    public DependencyGraphTopComponent(Project proj) {
        initComponents();
        project = proj;
        ProjectInformation info = project.getLookup().lookup(ProjectInformation.class);
        setName("DependencyGraph" + info.getName());
        setDisplayName("Dependencies - " + info.getDisplayName());
        timer.setDelay(1000);
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
        if ("".equals(val)) {
            scene.clearFind();
        } else {
            scene.findNodeByText(val);
        }
    }
    
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }
    
    @Override
    protected void componentOpened() {
        super.componentOpened();
        pane.setWheelScrollingEnabled(true);
        add(pane, BorderLayout.CENTER);
        JLabel lbl = new JLabel("Loading...");
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
                            birdView = scene.createBirdView();
                            satelliteView = scene.createSatelliteView();
                            scene.setSelectedObjects(Collections.singleton(scene.getRootArtifact()));
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
        btnBirdEye = new javax.swing.JToggleButton();
        lblFind = new javax.swing.JLabel();
        txtFind = new javax.swing.JTextField();

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

        btnBirdEye.setText("BirdEye");
        btnBirdEye.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBirdEyeActionPerformed(evt);
            }
        });
        jPanel1.add(btnBirdEye);

        lblFind.setText("Find:");
        jPanel1.add(lblFind);

        txtFind.setMinimumSize(new java.awt.Dimension(100, 19));
        txtFind.setPreferredSize(new java.awt.Dimension(150, 19));
        jPanel1.add(txtFind);

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
        if (btnBirdEye.isSelected()) {
            birdView.hide();
        }
        scene.setZoomFactor(scene.getZoomFactor() * 1.2);
        scene.validate();
        scene.repaint();
        if (pane.getHorizontalScrollBar().isVisible() || 
            pane.getVerticalScrollBar().isVisible()) {
            satelliteView.setLocation(0,0);
            revalidate();
            repaint();
        }
        
    }//GEN-LAST:event_btnBiggerActionPerformed

    private void btnBirdEyeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBirdEyeActionPerformed
        if (btnBirdEye.isSelected()) {
            birdView.setZoomFactor(1.1);
            birdView.show();
        } else {
            birdView.hide();
        }
        
    }//GEN-LAST:event_btnBirdEyeActionPerformed

    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBigger;
    private javax.swing.JToggleButton btnBirdEye;
    private javax.swing.JButton btnSmaller;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblFind;
    private javax.swing.JTextField txtFind;
    // End of variables declaration//GEN-END:variables
    
}

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
package org.netbeans.modules.vmd.game.editor.tiledlayer;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;
import org.netbeans.modules.vmd.game.model.TiledLayer;
import org.netbeans.modules.vmd.game.model.TiledLayerListener;

/**
 *
 * @author  kherink
 */
public class TiledLayerNavigator extends javax.swing.JPanel implements TiledLayerListener {

    private TiledLayer tiledLayer;
    private TiledLayerPreviewPanel preview;
    private boolean autoUpdate;

    /** Creates new form SpritePreview */
    public TiledLayerNavigator(TiledLayer tiledLayer) {
        this.tiledLayer = tiledLayer;
        initComponents();
        this.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                refreshSizeInfo();
            }
        });
        manualInit();
    }

    public void setAutoUpdate(boolean autoUpdate) {
        if (autoUpdate == this.autoUpdate) {
            return;
        }
        if (autoUpdate) {
            this.tiledLayer.addTiledLayerListener(this);
        } else {
            this.tiledLayer.removeTiledLayerListener(this);
        }
        this.autoUpdate = autoUpdate;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        refreshSizeInfo();
    }

    private void manualInit() {
        textFieldLayerName.setText(this.tiledLayer.getName());
        textFieldImage.setText(this.tiledLayer.getImageResource().getRelativeResourcePath());
        refreshSizeInfo();
        preview = new TiledLayerPreviewPanel(tiledLayer, toggleButtonAutoRefresh.isSelected());
        panelTiledLayer.add(preview);

        panelTiledLayer.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                preview.refresh();
                refreshSizeInfo();
            }
        });

        toggleButtonAutoRefresh.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (toggleButtonAutoRefresh.isSelected()) {
                    preview.setAutoUpdate(true);
                    setAutoUpdate(true);
                    buttonRefreshNow.setEnabled(false);
                } else {
                    preview.setAutoUpdate(false);
                    setAutoUpdate(false);
                    buttonRefreshNow.setEnabled(true);
                }
            }
        });

        buttonRefreshNow.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                preview.refresh();
                refreshSizeInfo();
            }
        });

    }

    private void refreshSizeInfo() {
        textFieldRows.setText(Integer.toString(tiledLayer.getRowCount()));
        textFieldCols.setText(Integer.toString(tiledLayer.getColumnCount()));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelSprite = new javax.swing.JLabel();
        textFieldLayerName = new javax.swing.JTextField();
        panelTiledLayer = new javax.swing.JPanel();
        labelFrames = new javax.swing.JLabel();
        textFieldRows = new javax.swing.JTextField();
        labelDelay = new javax.swing.JLabel();
        textFieldCols = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        textFieldImage = new javax.swing.JTextField();
        toggleButtonAutoRefresh = new javax.swing.JToggleButton();
        buttonRefreshNow = new javax.swing.JButton();

        setMinimumSize(new java.awt.Dimension(10, 180));

        labelSprite.setBackground(new java.awt.Color(255, 255, 255));
        labelSprite.setText(org.openide.util.NbBundle.getMessage(TiledLayerNavigator.class, "TiledLayerNavigator.tiledLayerLabel.txt")); // NOI18N

        textFieldLayerName.setEditable(false);
        textFieldLayerName.setText(this.tiledLayer.getName());
        textFieldLayerName.setBorder(null);

        panelTiledLayer.setBackground(new java.awt.Color(255, 255, 255));
        panelTiledLayer.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(99, 114, 136)));
        panelTiledLayer.setToolTipText(org.openide.util.NbBundle.getMessage(TiledLayerNavigator.class, "TiledLayerNavigator.preview.tooltip")); // NOI18N
        panelTiledLayer.setLayout(new java.awt.BorderLayout());

        labelFrames.setBackground(new java.awt.Color(255, 255, 255));
        labelFrames.setText(org.openide.util.NbBundle.getMessage(TiledLayerNavigator.class, "TiledLayerNavigator.rowsLabel.txt")); // NOI18N

        textFieldRows.setEditable(false);
        textFieldRows.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        textFieldRows.setText(Integer.toString(this.tiledLayer.getRowCount()));
        textFieldRows.setBorder(null);
        textFieldRows.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textFieldRowsActionPerformed(evt);
            }
        });

        labelDelay.setBackground(new java.awt.Color(255, 255, 255));
        labelDelay.setText(org.openide.util.NbBundle.getMessage(TiledLayerNavigator.class, "TiledLayerNavigator.colsLabel.txt")); // NOI18N

        textFieldCols.setEditable(false);
        textFieldCols.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        textFieldCols.setText(Integer.toString(this.tiledLayer.getColumnCount()));
        textFieldCols.setBorder(null);

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setText(org.openide.util.NbBundle.getMessage(TiledLayerNavigator.class, "TiledLayerNavigator.imageLabel.txt")); // NOI18N

        textFieldImage.setEditable(false);
        textFieldImage.setText(this.tiledLayer.getImageResource().getURL().toString());
        textFieldImage.setBorder(null);

        toggleButtonAutoRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/vmd/game/editor/tiledlayer/res/connection_mode.png"))); // NOI18N
        toggleButtonAutoRefresh.setToolTipText(org.openide.util.NbBundle.getMessage(TiledLayerNavigator.class, "TiledLayerNavigator.buttonSync.tooltip")); // NOI18N
        toggleButtonAutoRefresh.setBorder(null);
        toggleButtonAutoRefresh.setBorderPainted(false);
        toggleButtonAutoRefresh.setRolloverEnabled(true);

        buttonRefreshNow.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/vmd/game/editor/tiledlayer/res/refresh.png"))); // NOI18N
        buttonRefreshNow.setToolTipText(org.openide.util.NbBundle.getMessage(TiledLayerNavigator.class, "TiledLayerNavigator.buttonRefresh.tooltip")); // NOI18N
        buttonRefreshNow.setBorder(null);
        buttonRefreshNow.setBorderPainted(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(labelFrames)
                            .add(jLabel1))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(textFieldRows, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                            .add(textFieldCols, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                            .add(textFieldImage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(labelDelay, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(labelSprite)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(textFieldLayerName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(buttonRefreshNow)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(toggleButtonAutoRefresh, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(panelTiledLayer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 239, Short.MAX_VALUE)))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {buttonRefreshNow, toggleButtonAutoRefresh}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labelSprite)
                    .add(toggleButtonAutoRefresh, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(buttonRefreshNow)
                    .add(textFieldLayerName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(panelTiledLayer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 68, Short.MAX_VALUE)
                .add(8, 8, 8)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labelFrames)
                    .add(textFieldRows, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(3, 3, 3)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(labelDelay)
                    .add(textFieldCols, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(textFieldImage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {buttonRefreshNow, toggleButtonAutoRefresh}, org.jdesktop.layout.GroupLayout.VERTICAL);

    }// </editor-fold>//GEN-END:initComponents

private void textFieldRowsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textFieldRowsActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_textFieldRowsActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JButton buttonRefreshNow;
    public javax.swing.JLabel jLabel1;
    public javax.swing.JLabel labelDelay;
    public javax.swing.JLabel labelFrames;
    public javax.swing.JLabel labelSprite;
    public javax.swing.JPanel panelTiledLayer;
    public javax.swing.JTextField textFieldCols;
    public javax.swing.JTextField textFieldImage;
    public javax.swing.JTextField textFieldLayerName;
    public javax.swing.JTextField textFieldRows;
    public javax.swing.JToggleButton toggleButtonAutoRefresh;
    // End of variables declaration//GEN-END:variables

    // TiledLayerListener implementation
    public void tileChanged(TiledLayer source, int row, int col) {
        refreshSizeInfo();
    }

    public void tilesChanged(TiledLayer source, Set positions) {
        refreshSizeInfo();
    }

    public void tilesStructureChanged(TiledLayer source) {
        refreshSizeInfo();
    }

    public void columnsInserted(TiledLayer source, int index, int count) {
        refreshSizeInfo();
    }

    public void columnsRemoved(TiledLayer source, int index, int count) {
        refreshSizeInfo();
    }

    public void rowsInserted(TiledLayer source, int index, int count) {
        refreshSizeInfo();
    }

    public void rowsRemoved(TiledLayer source, int index, int count) {
        refreshSizeInfo();
    }
}

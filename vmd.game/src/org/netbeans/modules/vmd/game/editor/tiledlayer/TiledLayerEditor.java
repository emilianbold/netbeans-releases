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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import javax.swing.JScrollPane;
import org.netbeans.modules.vmd.game.dialog.NewAnimatedTileDialog;
import org.netbeans.modules.vmd.game.model.Editable;
import org.netbeans.modules.vmd.game.model.Tile;
import org.netbeans.modules.vmd.game.model.TiledLayer;
import org.netbeans.modules.vmd.game.model.TiledLayerListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
/**
 *
 * @author  kherink
 */
public class TiledLayerEditor extends javax.swing.JPanel implements TiledLayerListener, PropertyChangeListener {
        
    public static final int MAX_COMPILABLE_CELLS = 8000;
    
        private TiledLayer tiledLayer;
        private TiledLayerEditorComponent editorComponent;
        private JScrollPane editorScroll;
        
        /**
     * Creates new form TiledLayerEditor
     */
        public TiledLayerEditor(final TiledLayer tiledLayer) {
                this.tiledLayer = tiledLayer;
                this.tiledLayer.addTiledLayerListener(this);
                this.tiledLayer.addPropertyChangeListener(this);
                this.editorComponent = new TiledLayerEditorComponent(this.tiledLayer);
                initComponents();
                
                this.getAccessibleContext().setAccessibleName(NbBundle.getMessage(TiledLayerEditor.class, "TiledLayerEditor.accessible.name"));
                this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TiledLayerEditor.class, "TiledLayerEditor.accessible.description"));
                
                this.toggleButtonPaint.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (toggleButtonPaint.isSelected()) {
                            editorComponent.setEditMode(TiledLayerEditorComponent.EDIT_MODE_PAINT);
                        }
                    }
                });
                this.toggleButtonSelect.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (toggleButtonSelect.isSelected()) {
                            editorComponent.setEditMode(TiledLayerEditorComponent.EDIT_MODE_SELECT);
                        }
                    }
                });
                this.resizableModeButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        editorComponent.setAutoResizable(resizableModeButton.isSelected());
                    }
                });
                
                this.toggleButtonPaint.setSelected(true);
                this.resizableModeButton.setSelected(true);
                
                this.buttonAddAnimatedTile.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                                NewAnimatedTileDialog dialog = new NewAnimatedTileDialog(tiledLayer.getImageResource(), tiledLayer.getTileWidth(), tiledLayer.getTileHeight());
                                DialogDescriptor dd = new DialogDescriptor(dialog, NbBundle.getMessage(TiledLayerEditor.class, "TiledLayerEditor.newAnimatedTileButton.txt"));
                                dd.setButtonListener(dialog);
                                dd.setValid(false);
                                dialog.setDialogDescriptor(dd);
                                Dialog d = DialogDisplayer.getDefault().createDialog(dd);
                                d.setVisible(true);
                        }
                });
                this.textFieldName.setBackground(this.textFieldName.getParent().getBackground());
                this.textFieldRows.setBackground(this.textFieldRows.getParent().getBackground());
                this.textFieldCols.setBackground(this.textFieldCols.getParent().getBackground());
                
                this.editorScroll = new JScrollPane();
                this.editorScroll.getViewport().setBackground(Color.WHITE);
                this.editorScroll.setViewportView(this.editorComponent);
                this.editorScroll.setColumnHeaderView(this.editorComponent.rulerHorizontal);
                this.editorScroll.setRowHeaderView(this.editorComponent.rulerVertical);
                this.editorScroll.setCorner(JScrollPane.UPPER_LEFT_CORNER, this.editorComponent.getGridButton());
                this.jPanel2.add(this.editorScroll, BorderLayout.CENTER);
                
                JScrollPane scrollAnimTiles = new JScrollPane(new AnimatedTileList(editorComponent));
                scrollAnimTiles.setBorder(null);
                this.panelAnimatedTiles.add(scrollAnimTiles, BorderLayout.CENTER);
        
        this.updateTextLabels();
        }
        
        /** This method is called from within the constructor to
         * initialize the form.
         * WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the Form Editor.
         */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroupMouseMode = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        textFieldName = new javax.swing.JTextField();
        toggleButtonPaint = new javax.swing.JToggleButton();
        toggleButtonSelect = new javax.swing.JToggleButton();
        jLabel2 = new javax.swing.JLabel();
        textFieldRows = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        textFieldCols = new javax.swing.JTextField();
        resizableModeButton = new javax.swing.JToggleButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        panelAnimatedTiles = new javax.swing.JPanel();
        buttonAddAnimatedTile = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        jLabel1.setText(org.openide.util.NbBundle.getMessage(TiledLayerEditor.class, "TiledLayerEditor.tiledLayerLabel.txt")); // NOI18N

        textFieldName.setEditable(false);
        textFieldName.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        textFieldName.setText(this.tiledLayer.getName());
        textFieldName.setBorder(null);

        buttonGroupMouseMode.add(toggleButtonPaint);
        toggleButtonPaint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/vmd/game/editor/tiledlayer/res/drawing_mode_16.png"))); // NOI18N
        toggleButtonPaint.setToolTipText(org.openide.util.NbBundle.getMessage(TiledLayerEditor.class, "TiledLayerEditor.paintModeButton.tooltip")); // NOI18N
        toggleButtonPaint.setBorderPainted(false);
        toggleButtonPaint.setRolloverEnabled(true);

        buttonGroupMouseMode.add(toggleButtonSelect);
        toggleButtonSelect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/vmd/game/editor/tiledlayer/res/selection_mode_16.png"))); // NOI18N
        toggleButtonSelect.setToolTipText(org.openide.util.NbBundle.getMessage(TiledLayerEditor.class, "TiledLayerEditor.selectModeButton.tooltip")); // NOI18N
        toggleButtonSelect.setBorderPainted(false);
        toggleButtonSelect.setRolloverEnabled(true);

        jLabel2.setText(org.openide.util.NbBundle.getMessage(TiledLayerEditor.class, "TiledLayerEditor.tiledLayerRowsLabel.txt")); // NOI18N

        textFieldRows.setEditable(false);
        textFieldRows.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        textFieldRows.setText(Integer.toString(this.tiledLayer.getRowCount()));
        textFieldRows.setBorder(null);

        jLabel3.setText(org.openide.util.NbBundle.getMessage(TiledLayerEditor.class, "TiledLayerEditor.tiledLayerColsLabel.txt")); // NOI18N

        textFieldCols.setEditable(false);
        textFieldCols.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        textFieldCols.setText(Integer.toString(this.tiledLayer.getColumnCount()));
        textFieldCols.setBorder(null);

        resizableModeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/vmd/game/editor/tiledlayer/res/resizable_mode_16.png"))); // NOI18N
        resizableModeButton.setToolTipText(org.openide.util.NbBundle.getMessage(TiledLayerEditor.class, "TiledLayerEditor.resizableModeButton.tooltip")); // NOI18N
        resizableModeButton.setBorderPainted(false);
        resizableModeButton.setRolloverEnabled(true);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(toggleButtonPaint, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(toggleButtonSelect, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(textFieldName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(resizableModeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(10, 10, 10)
                .add(jLabel2)
                .add(6, 6, 6)
                .add(textFieldRows, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel3)
                .add(6, 6, 6)
                .add(textFieldCols, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel1Layout.linkSize(new java.awt.Component[] {toggleButtonPaint, toggleButtonSelect}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                .add(jLabel1)
                .add(textFieldName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(toggleButtonPaint, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(toggleButtonSelect, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jLabel2)
                .add(textFieldRows, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jLabel3)
                .add(textFieldCols, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(resizableModeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        resizableModeButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TiledLayerEditor.class, "ACSN_resizableModeButton")); // NOI18N
        resizableModeButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TiledLayerEditor.class, "ACSD_resizableModeButton")); // NOI18N

        add(jPanel1, java.awt.BorderLayout.NORTH);

        jSplitPane1.setBorder(null);
        jSplitPane1.setDividerSize(5);
        jSplitPane1.setResizeWeight(1.0);
        jSplitPane1.setContinuousLayout(true);

        panelAnimatedTiles.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        panelAnimatedTiles.setLayout(new java.awt.BorderLayout());

        buttonAddAnimatedTile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/vmd/game/editor/tiledlayer/res/new_animated_tile_16.png"))); // NOI18N
        buttonAddAnimatedTile.setText(org.openide.util.NbBundle.getMessage(TiledLayerEditor.class, "TiledLayerEditor.newAnimatedTileButton.txt")); // NOI18N
        buttonAddAnimatedTile.setToolTipText(org.openide.util.NbBundle.getMessage(TiledLayerEditor.class, "TiledLayerEditor.newAnimTileButton.tooltip")); // NOI18N
        panelAnimatedTiles.add(buttonAddAnimatedTile, java.awt.BorderLayout.NORTH);

        jSplitPane1.setRightComponent(panelAnimatedTiles);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setToolTipText("");
        jPanel2.setPreferredSize(new java.awt.Dimension(10000, 10000));
        jPanel2.setLayout(new java.awt.BorderLayout());
        jSplitPane1.setLeftComponent(jPanel2);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
        
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JButton buttonAddAnimatedTile;
    public javax.swing.ButtonGroup buttonGroupMouseMode;
    public javax.swing.JLabel jLabel1;
    public javax.swing.JLabel jLabel2;
    public javax.swing.JLabel jLabel3;
    public javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    public javax.swing.JSplitPane jSplitPane1;
    public javax.swing.JPanel panelAnimatedTiles;
    public javax.swing.JToggleButton resizableModeButton;
    public javax.swing.JTextField textFieldCols;
    public javax.swing.JTextField textFieldName;
    public javax.swing.JTextField textFieldRows;
    public javax.swing.JToggleButton toggleButtonPaint;
    public javax.swing.JToggleButton toggleButtonSelect;
    // End of variables declaration//GEN-END:variables
        
        public void setPaintTile(Tile tile) {
                this.editorComponent.setPaintTileIndex(tile.getIndex());
        }
        
    public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getSource() == this.tiledLayer) {
                        if (evt.getPropertyName().equals(Editable.PROPERTY_NAME)) {
                                textFieldName.setText(this.tiledLayer.getName());
                        }
                }
    }

    public void updateTextLabels() {
                textFieldRows.setText(Integer.toString(this.tiledLayer.getRowCount()));
                textFieldCols.setText(Integer.toString(this.tiledLayer.getColumnCount()));
        Color textColor = null;
        if (this.tiledLayer.getRowCount() * this.tiledLayer.getColumnCount() > MAX_COMPILABLE_CELLS) {
            textColor = Color.RED;
            textFieldName.setToolTipText(NbBundle.getMessage(TiledLayerEditor.class, "TiledLayerEditor.textFieldName.err.tooltip"));
        }
        else {
            textColor = Color.BLACK;
            textFieldName.setToolTipText(null);
        }
        textFieldName.setForeground(textColor);
    }

    public void tileChanged(TiledLayer t, int row, int col) {
    }

    public void tilesChanged(TiledLayer t, Set positions) {
    }

        public void tilesStructureChanged(TiledLayer source) {
                this.updateTextLabels();
        }

    public void columnsInserted(TiledLayer t, int index, int count) {
                this.updateTextLabels();
    }

    public void columnsRemoved(TiledLayer t, int index, int count) {
                this.updateTextLabels();
    }

    public void rowsInserted(TiledLayer t, int index, int count) {
                this.updateTextLabels();
    }

    public void rowsRemoved(TiledLayer t, int index, int count) {
                this.updateTextLabels();
    }

}

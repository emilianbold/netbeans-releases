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

package org.netbeans.modules.vmd.game.editor.tiledlayer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
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
    public static final int MAX_EXTENDABLE_CELLS = 100000;
    
    private static final float ZOOM_STEP = (float) 1.1;
    private static final String[] ZOOM_VALUES = new String[]{"400%", "300%", "200%", "100%", "75%", "50%", "25%"}; //NOI18N
    
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

        final ComboBoxEditor zce = this.zoomCombo.getEditor();
        this.zoomCombo.setEditor(new ZoomComboBoxEditor(zce));
        this.updateZoomCombo();

        MouseWheelListener mouseWheelListener = new MouseWheelListener(){
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getWheelRotation() < 0) {
                    zoomOut();
                } else {
                    zoomIn();
                }
            }
        };
        this.addMouseWheelListener(mouseWheelListener);
        editorScroll.addMouseWheelListener(mouseWheelListener);
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
        zoomCombo = new javax.swing.JComboBox();
        zoomOutButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        zoomInButton = new javax.swing.JButton();
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

        buttonGroupMouseMode.add(toggleButtonSelect);
        toggleButtonSelect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/vmd/game/editor/tiledlayer/res/selection_mode_16.png"))); // NOI18N
        toggleButtonSelect.setToolTipText(org.openide.util.NbBundle.getMessage(TiledLayerEditor.class, "TiledLayerEditor.selectModeButton.tooltip")); // NOI18N
        toggleButtonSelect.setBorderPainted(false);

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

        zoomCombo.setEditable(true);
        zoomCombo.setModel(new DefaultComboBoxModel(ZOOM_VALUES));
        zoomCombo.setSelectedIndex(0);
        zoomCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomComboActionPerformed(evt);
            }
        });

        zoomOutButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/vmd/game/editor/tiledlayer/res/zoom_out.png"))); // NOI18N
        zoomOutButton.setToolTipText(org.openide.util.NbBundle.getMessage(TiledLayerEditor.class, "TiledLayerEditor.zoomOutButton.tooltip")); // NOI18N
        zoomOutButton.setBorder(null);
        zoomOutButton.setBorderPainted(false);
        zoomOutButton.setOpaque(false);
        zoomOutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomOutButtonActionPerformed(evt);
            }
        });

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        zoomInButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/vmd/game/editor/tiledlayer/res/zoom_in.png"))); // NOI18N
        zoomInButton.setToolTipText(org.openide.util.NbBundle.getMessage(TiledLayerEditor.class, "TiledLayerEditor.zoomInButton.tooltip")); // NOI18N
        zoomInButton.setBorder(null);
        zoomInButton.setBorderPainted(false);
        zoomInButton.setOpaque(false);
        zoomInButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomInButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(toggleButtonPaint, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(toggleButtonSelect, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textFieldName, javax.swing.GroupLayout.DEFAULT_SIZE, 1, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(zoomCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(zoomInButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addComponent(zoomOutButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resizableModeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addGap(6, 6, 6)
                .addComponent(textFieldRows, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addGap(6, 6, 6)
                .addComponent(textFieldCols, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {toggleButtonPaint, toggleButtonSelect});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(jLabel1)
                .addComponent(textFieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(toggleButtonPaint, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(toggleButtonSelect, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel2)
                .addComponent(textFieldRows, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel3)
                .addComponent(textFieldCols, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(resizableModeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(zoomOutButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(zoomInButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(zoomCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        resizableModeButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TiledLayerEditor.class, "ACSN_resizableModeButton")); // NOI18N
        resizableModeButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TiledLayerEditor.class, "ACSD_resizableModeButton")); // NOI18N
        zoomCombo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TiledLayerEditor.class, "ACSN_zoomCombo")); // NOI18N
        zoomCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TiledLayerEditor.class, "ACSD_zoomCombo")); // NOI18N
        zoomOutButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TiledLayerEditor.class, "ACSN_zoomOutButton")); // NOI18N
        zoomOutButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TiledLayerEditor.class, "ACSD_zoomOutButton")); // NOI18N
        zoomInButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TiledLayerEditor.class, "ACSN_zoomInButton")); // NOI18N
        zoomInButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TiledLayerEditor.class, "ACSD_zoomInButton")); // NOI18N

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

    private void zoomComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomComboActionPerformed
        String selection = (String) zoomCombo.getSelectedItem();
        if (selection != null) {
            selection = selection.trim();
            if (selection.endsWith("%")) {//NOI18N
                selection = selection.substring(0, selection.length() - 1);
            }
            try {
                float zoom = Float.parseFloat(selection) / 100;
                if (zoom > 0 && zoom < 100) {
                    editorComponent.setZoom(zoom);
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
}//GEN-LAST:event_zoomComboActionPerformed

    private void zoomInButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomInButtonActionPerformed
        zoomIn();
}//GEN-LAST:event_zoomInButtonActionPerformed

    private void zoomOutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomOutButtonActionPerformed
        zoomOut();
    }//GEN-LAST:event_zoomOutButtonActionPerformed
        
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JButton buttonAddAnimatedTile;
    public javax.swing.ButtonGroup buttonGroupMouseMode;
    public javax.swing.JLabel jLabel1;
    public javax.swing.JLabel jLabel2;
    public javax.swing.JLabel jLabel3;
    public javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    public javax.swing.JSeparator jSeparator1;
    public javax.swing.JSplitPane jSplitPane1;
    public javax.swing.JPanel panelAnimatedTiles;
    public javax.swing.JToggleButton resizableModeButton;
    public javax.swing.JTextField textFieldCols;
    public javax.swing.JTextField textFieldName;
    public javax.swing.JTextField textFieldRows;
    public javax.swing.JToggleButton toggleButtonPaint;
    public javax.swing.JToggleButton toggleButtonSelect;
    public javax.swing.JComboBox zoomCombo;
    public javax.swing.JButton zoomInButton;
    public javax.swing.JButton zoomOutButton;
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
        int cellsCnt = this.tiledLayer.getRowCount() * this.tiledLayer.getColumnCount();
        if (cellsCnt > MAX_EXTENDABLE_CELLS) {
            textColor = Color.RED;
            textFieldName.setToolTipText(NbBundle.getMessage(TiledLayerEditor.class,
                    "TiledLayerEditor.textFieldName.notExtendable.err.tooltip",
                    MAX_EXTENDABLE_CELLS));
            resizableModeButton.setSelected(false);
            editorComponent.setAutoResizable(false);
            resizableModeButton.setEnabled(false);
        }
        else if (cellsCnt > MAX_COMPILABLE_CELLS) {
            textColor = Color.RED;
            textFieldName.setToolTipText(NbBundle.getMessage(TiledLayerEditor.class,
                    "TiledLayerEditor.textFieldName.notCompilable.err.tooltip"));
            if (!this.resizableModeButton.isEnabled()){
                this.resizableModeButton.setEnabled(true);
            }
        }
        else {
            textColor = Color.BLACK;
            textFieldName.setToolTipText(null);
            if (!this.resizableModeButton.isEnabled()){
                this.resizableModeButton.setEnabled(true);
            }
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

    private void zoomIn(){
        editorComponent.setZoom(editorComponent.getZoom() * ZOOM_STEP);
        updateZoomCombo();
    }

    private void zoomOut(){
        editorComponent.setZoom(editorComponent.getZoom() / ZOOM_STEP);
        updateZoomCombo();
    }

    protected void updateZoomCombo() {
        zoomCombo.getEditor().setItem(Integer.toString((int) (editorComponent.getZoom() * 100)) + "%"); //NOI18N
    }

    private class ZoomComboBoxEditor implements ComboBoxEditor {
        private ComboBoxEditor cbe;
        
        ZoomComboBoxEditor(ComboBoxEditor editor){
            this.cbe = editor;
        }

        private String m_lastValue = ""; //NOI18N

        public Component getEditorComponent() {
            return cbe.getEditorComponent();
        }

        public void setItem(Object anObject) {
            cbe.setItem(anObject);
        }

        public Object getItem() {
            Object o = cbe.getItem();
            if (o != null) {
                String value = o.toString();
                if (value != null) {
                    value = value.trim();
                    int len = value.length();
                    if (len > 0) {
                        if (value.endsWith("%")) { //NOI18N
                            value = value.substring(0, len - 1);
                        }
                        try {
                            float floatValue = Float.parseFloat(value);
                            m_lastValue = Math.round(floatValue) + "%"; //NOI18N
                            return m_lastValue;
                        } catch (NumberFormatException e) {
                        }
                    }
                }
            }
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ((JTextComponent) cbe.getEditorComponent()).setText(m_lastValue);
                }
            });
            return m_lastValue;
        }

        public void selectAll() {
            cbe.selectAll();
        }

        public void addActionListener(ActionListener l) {
            cbe.addActionListener(l);
        }

        public void removeActionListener(ActionListener l) {
            cbe.removeActionListener(l);
        }
    }

}

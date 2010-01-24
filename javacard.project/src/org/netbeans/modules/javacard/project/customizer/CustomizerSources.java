/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.project.customizer;

import org.netbeans.modules.java.api.common.project.ui.customizer.SourceRootsUi;
import org.netbeans.modules.javacard.constants.ProjectPropertyNames;
import org.netbeans.modules.javacard.project.JCProjectProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import javax.swing.*;
import javax.swing.plaf.UIResource;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.IllegalCharsetNameException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import org.netbeans.modules.javacard.common.GuiUtils;

/**
 * Customizer panel "Sources": source roots, level, includes/excludes.
 * @author  Tomas Zezula
 */
public class CustomizerSources extends javax.swing.JPanel implements HelpCtx.Provider, ActionListener {
    
    
    private String originalEncoding;
    private boolean notified;

    private final JCProjectProperties uiProperties;
    private final SourceRootsUi.EditMediator emSR;

    public CustomizerSources( JCProjectProperties uiProperties ) {
        this.uiProperties = uiProperties;
        initComponents();
        GuiUtils.prepareContainer(this);
        jScrollPane1.getViewport().setBackground( sourceRoots.getBackground() );
        
        sourceRoots.setModel( uiProperties.SOURCE_ROOTS_MODEL );
        sourceRoots.getTableHeader().setReorderingAllowed(false);
        
        FileObject projectFolder = uiProperties.getProject().getProjectDirectory();
        File pf = FileUtil.toFile( projectFolder );
        this.projectLocation.setText( pf == null ? "" : pf.getPath() ); // NOI18N
        
        emSR = SourceRootsUi.registerEditMediator(
            uiProperties.getProject(),
            uiProperties.getProject().getRoots(),
            sourceRoots,
            addSourceRoot,
            removeSourceRoot, 
            upSourceRoot, 
            downSourceRoot,
            new LabelCellEditor(sourceRoots),
            false);

        this.sourceLevel.setEditable(false);

        enableSourceLevel ();
        this.originalEncoding = this.uiProperties.getProject().evaluator().getProperty(ProjectPropertyNames.PROJECT_PROP_SOURCE_ENCODING);
        if (this.originalEncoding == null) {
            this.originalEncoding = Charset.defaultCharset().name();
        }
        
        this.encoding.setModel(new EncodingModel(this.originalEncoding));
        this.encoding.setRenderer(new EncodingRenderer());
        final String lafid = UIManager.getLookAndFeel().getID();
        if (!"Aqua".equals(lafid)) { //NOI18N
            this.encoding.putClientProperty ("JComboBox.isTableCellEditor", Boolean.TRUE);    //NOI18N
            this.encoding.addItemListener(new java.awt.event.ItemListener(){ 
                public void itemStateChanged(java.awt.event.ItemEvent e){
                    javax.swing.JComboBox combo = (javax.swing.JComboBox)e.getSource(); 
                    combo.setPopupVisible(false); 
                } 
            });
        }
        this.encoding.addActionListener(new ActionListener () {
            public void actionPerformed(ActionEvent arg0) {
                handleEncodingChange();
            }            
        });
        initTableVisualProperties(sourceRoots);
        HelpCtx.setHelpIDString(this, "org.netbeans.modules.javacard.SourcesPanel"); //NOI18N
        addSourceRoot.removeActionListener (emSR);
    }
    
    private class TableColumnSizeComponentAdapter extends ComponentAdapter {
        private JTable table = null;
        
        public TableColumnSizeComponentAdapter(JTable table){
            this.table = table;
        }
        
        @Override
        public void componentResized(ComponentEvent evt){
            double pw = table.getParent().getParent().getSize().getWidth();
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            TableColumn column = table.getColumnModel().getColumn(0);
            column.setWidth( ((int)pw/2) - 1 );
            column.setPreferredWidth( ((int)pw/2) - 1 );
            column = table.getColumnModel().getColumn(1);
            column.setWidth( ((int)pw/2) - 1 );
            column.setPreferredWidth( ((int)pw/2) - 1 );
        }
    }
    
    private void initTableVisualProperties(JTable table) {

        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));
        // set the color of the table's JViewport
        table.getParent().setBackground(table.getBackground());
        
        //we'll get the parents width so we can use that to set the column sizes.
        double pw = table.getParent().getParent().getPreferredSize().getWidth();
        
        //#88174 - Need horizontal scrollbar for library names
        //ugly but I didn't find a better way how to do it
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumn column = table.getColumnModel().getColumn(0);
        column.setMinWidth(226);
        column.setWidth( ((int)pw/2) - 1 );
        column.setPreferredWidth( ((int)pw/2) - 1 );
        column.setMinWidth(75);
        column = table.getColumnModel().getColumn(1);
        column.setMinWidth(226);
        column.setWidth( ((int)pw/2) - 1 );
        column.setPreferredWidth( ((int)pw/2) - 1 );
        column.setMinWidth(75);
        this.addComponentListener(new TableColumnSizeComponentAdapter(table));
    }
    
    private void handleEncodingChange () {
            Charset enc = (Charset) encoding.getSelectedItem();
            String encName;
            if (enc != null) {
                encName = enc.name();
            }
            else {
                encName = originalEncoding;
            }
            if (!notified && encName!=null && !encName.equals(originalEncoding)) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(CustomizerSources.class,"MSG_EncodingWarning"), //NOI18N
                        NotifyDescriptor.WARNING_MESSAGE));
                notified=true;
            }
            this.uiProperties.setSourceEncoding(encName);
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx (CustomizerSources.class);
    }
    
    private void enableSourceLevel () {
        this.sourceLevel.setEnabled(sourceLevel.getItemCount()>0);
    }
    
    
    private static class EncodingRenderer extends JLabel implements ListCellRenderer, UIResource {
        
        public EncodingRenderer() {
            setOpaque(true);
        }
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            assert value instanceof Charset;
            setName("ComboBox.listRenderer"); // NOI18N
            setText(((Charset) value).displayName());
            setIcon(null);
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());             
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }
        
        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name; // NOI18N
        }
        
    }
    
    private static class EncodingModel extends DefaultComboBoxModel {
        
        public EncodingModel (String originalEncoding) {
            Charset defEnc = null;
            for (Charset c : Charset.availableCharsets().values()) {
                if (c.name().equals(originalEncoding)) {
                    defEnc = c;
                }
                addElement(c);
            }
            if (defEnc == null) {
                //Create artificial Charset to keep the original value
                //May happen when the project was set up on the platform
                //which supports more encodings
                try {
                    defEnc = new UnknownCharset (originalEncoding);
                    addElement(defEnc);
                } catch (IllegalCharsetNameException e) {
                    //The source.encoding property is completely broken
                    Logger.getLogger(this.getClass().getName()).info(
                            "IllegalCharsetName: " + originalEncoding); //NOI18N
                }
            }
            if (defEnc == null) {
                defEnc = Charset.defaultCharset();
            }
            setSelectedItem(defEnc);
        }
    }
    
    private static class UnknownCharset extends Charset {
        
        UnknownCharset (String name) {
            super (name, new String[0]);
        }
    
        @Override
        public boolean contains(Charset c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CharsetDecoder newDecoder() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CharsetEncoder newEncoder() {
            throw new UnsupportedOperationException();
        }
}
    
    private static class ResizableRowHeightTable extends JTable {

        private boolean needResize = true;
        
        @Override
        public void setFont(Font font) {
            needResize = true;
            super.setFont(font);
        }

        @Override
        public void paint(Graphics g) {
            if(needResize) {
                this.setRowHeight(g.getFontMetrics(this.getFont()).getHeight());
                needResize = false;
            }
            super.paint(g);
        }
        
    }
    
    private static class LabelCellEditor extends DefaultCellEditor {
        
        private JTable sourceRoots;
        
        public LabelCellEditor(JTable sourceRoots) {
            super(new JTextField());
            this.sourceRoots = sourceRoots;
        }
        
        @Override
        public boolean stopCellEditing() {
            JTextField field = (JTextField) getComponent();
            String text = field.getText();
            boolean validCell = true;
            TableModel model = sourceRoots.getModel();
            int rowCount = model.getRowCount();
            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                String value = (String) model.getValueAt(rowIndex, 1);
                if (text.equals(value)) {
                    validCell = false;
                }
            }
            rowCount = model.getRowCount();
            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                String value = (String) model.getValueAt(rowIndex, 1);
                if (text.equals(value)) {
                    validCell = false;
                }
            }
            
            return validCell == false ? validCell : super.stopCellEditing();
        }
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        projectLocation = new javax.swing.JTextField();
        sourceRootsPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        sourceRoots = new ResizableRowHeightTable();
        addSourceRoot = new javax.swing.JButton();
        removeSourceRoot = new javax.swing.JButton();
        upSourceRoot = new javax.swing.JButton();
        downSourceRoot = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        sourceLevel = uiProperties.createSourceLevelComboBox();
        jLabel5 = new javax.swing.JLabel();
        encoding = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(projectLocation);
        jLabel1.setText(NbBundle.getMessage (CustomizerSources.class, "CTL_ProjectFolder")); // NOI18N
        add(jLabel1, new java.awt.GridBagConstraints());

        projectLocation.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(projectLocation, gridBagConstraints);
        projectLocation.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSources.class, "AD_CustomizerSources_projectLocation")); // NOI18N

        sourceRootsPanel.setLayout(new java.awt.GridBagLayout());

        jLabel2.setLabelFor(sourceRoots);
        jLabel2.setText(NbBundle.getMessage (CustomizerSources.class, "CTL_SourceRoots")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        sourceRootsPanel.add(jLabel2, gridBagConstraints);

        jScrollPane1.setPreferredSize(new java.awt.Dimension(450, 150));

        sourceRoots.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Package Folder", "Label"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(sourceRoots);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        sourceRootsPanel.add(jScrollPane1, gridBagConstraints);

        addSourceRoot.setText(NbBundle.getMessage (CustomizerSources.class, "CTL_AddSourceRoot")); // NOI18N
        addSourceRoot.addActionListener(this);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        sourceRootsPanel.add(addSourceRoot, gridBagConstraints);

        removeSourceRoot.setText(NbBundle.getMessage (CustomizerSources.class, "CTL_RemoveSourceRoot")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        sourceRootsPanel.add(removeSourceRoot, gridBagConstraints);

        upSourceRoot.setText(NbBundle.getMessage (CustomizerSources.class, "CTL_UpSourceRoot")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        sourceRootsPanel.add(upSourceRoot, gridBagConstraints);

        downSourceRoot.setText(NbBundle.getMessage (CustomizerSources.class, "CTL_DownSourceRoot")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        sourceRootsPanel.add(downSourceRoot, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.45;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(sourceRootsPanel, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel4.setLabelFor(sourceLevel);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, NbBundle.getMessage (CustomizerSources.class, "TXT_SourceLevel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        jPanel1.add(jLabel4, gridBagConstraints);

        sourceLevel.setMinimumSize(this.sourceLevel.getPreferredSize());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(sourceLevel, gridBagConstraints);

        jLabel5.setLabelFor(encoding);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, NbBundle.getMessage (CustomizerSources.class, "TXT_Encoding")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 12);
        jPanel1.add(jLabel5, gridBagConstraints);

        encoding.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        jPanel1.add(encoding, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jPanel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(jPanel1, gridBagConstraints);
    }

    // Code for dispatching events from components to event handlers.

    public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == addSourceRoot) {
            CustomizerSources.this.addSourceRootActionPerformed(evt);
        }
    }// </editor-fold>//GEN-END:initComponents

    private void addSourceRootActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSourceRootActionPerformed
        //SourceRootsUI supports source roots not under the project - but we don't
        //so handle this manually
        ActionEvent ae = new ActionEvent(addSourceRoot, evt.getID(), evt.getActionCommand());
        Set<File> currRoots = currSourceRoots();
        emSR.actionPerformed(ae);
        Set<File> newRoots = currSourceRoots();
        newRoots.removeAll(currRoots);
        FileObject projectRoot = uiProperties.getProject().getProjectDirectory();
        Set<File> badRoots = new HashSet<File>();
        for (File f : newRoots) {
            FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(f));
            if (!FileUtil.isParentOf(projectRoot, fo)) {
                badRoots.add(f);
            }
        }
        if (!badRoots.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (File bad : badRoots) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(bad.getName());
                removeSourceRoot(bad);
            }
            NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(CustomizerSources.class, "ERR_BAD_ROOT", sb));
            DialogDisplayer.getDefault().notify(nd);
        }
}//GEN-LAST:event_addSourceRootActionPerformed

    private void removeSourceRoot(File f) {
        DefaultTableModel mdl = (DefaultTableModel) sourceRoots.getModel();
        int max = mdl.getRowCount();
        for (int i=max-1; i >= 0; i--) {
            if (f.equals(mdl.getValueAt(i, 0))) {
                mdl.removeRow(i);
            }
        }
    }

    private Set<File> currSourceRoots() {
        Set<File> result = new HashSet<File>();
        int max = sourceRoots.getModel().getRowCount();
        for (int i=0; i < max; i++) {
            File f = (File) sourceRoots.getModel().getValueAt(i, 0);
            result.add (f);
        }
        return result;
    }

   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addSourceRoot;
    private javax.swing.JButton downSourceRoot;
    private javax.swing.JComboBox encoding;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField projectLocation;
    private javax.swing.JButton removeSourceRoot;
    private javax.swing.JComboBox sourceLevel;
    private javax.swing.JTable sourceRoots;
    private javax.swing.JPanel sourceRootsPanel;
    private javax.swing.JButton upSourceRoot;
    // End of variables declaration//GEN-END:variables
    
}

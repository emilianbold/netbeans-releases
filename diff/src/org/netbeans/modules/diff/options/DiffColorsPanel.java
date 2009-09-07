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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.diff.options;

import org.netbeans.modules.options.colors.spi.FontsColorsController;
import org.netbeans.modules.options.colors.ColorModel;
import org.netbeans.modules.diff.DiffModuleConfig;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.openide.util.NbBundle;

import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.*;

/**
 * Copied from org.netbeans.modules.options.colors.HighlightingPanel
 * 
 * copied from editor/options.
 * @author Maros Sandor
 */
public class DiffColorsPanel extends javax.swing.JPanel implements ActionListener, FontsColorsController, PropertyChangeListener {
    
    private static final String ATTR_NAME_ADDED = "added";
    private static final String ATTR_NAME_DELETED = "deleted";
    private static final String ATTR_NAME_CHANGED = "changed";

    private static final String ATTR_NAME_MERGE_UNRESOLVED = "merge.unresolved";
    private static final String ATTR_NAME_MERGE_APPLIED = "merge.applied";
    private static final String ATTR_NAME_MERGE_NOTAPPLIED = "merge.notapplied";
    private static final String ATTR_NAME_SIDEBAR_DELETED = "sidebar.deleted";
    private static final String ATTR_NAME_SIDEBAR_CHANGED = "sidebar.changed";
    
    private boolean		        listen;
    private List<AttributeSet>  categories;
    private boolean             changed;
    
    public DiffColorsPanel() {
        initComponents ();

        setName(loc("LBL_DiffOptions_Tab")); //NOI18N
        
        org.netbeans.modules.diff.options.ColorComboBox.init (cbBackground);
        lCategories.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
        lCategories.setVisibleRowCount (6);
        lCategories.addListSelectionListener (new ListSelectionListener() {
            public void valueChanged (ListSelectionEvent e) {
                if (!listen) return;
                refreshUI ();
            }
        });
        lCategories.setCellRenderer (new CategoryRenderer());
        cbBackground.addActionListener (this);
        ((JComponent)cbBackground.getEditor()).addPropertyChangeListener (this);
    }

    
    public void actionPerformed (ActionEvent evt) {
        if (!listen) return;
        updateData ();
        changed = true;
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        if (!listen) return;
        if (evt.getPropertyName() == ColorComboBox.PROP_COLOR) {
            updateData ();
            changed = true;
        }
    }
    
    public void update(ColorModel colorModel) {
        listen = false;
        lCategories.setListData(new Vector(getCategories()));
        lCategories.setSelectedIndex(0);
        refreshUI ();	
        listen = true;
        changed = false;
    }
    
    public void cancel () {
        changed = false;
    }
    
    public void applyChanges() {
        List<AttributeSet> colors = getCategories();
        for (AttributeSet color : colors) {
            if (ATTR_NAME_ADDED.equals(color.getAttribute(StyleConstants.NameAttribute))) DiffModuleConfig.getDefault().setAddedColor((Color) color.getAttribute(StyleConstants.Background)); 
            if (ATTR_NAME_CHANGED.equals(color.getAttribute(StyleConstants.NameAttribute))) DiffModuleConfig.getDefault().setChangedColor((Color) color.getAttribute(StyleConstants.Background)); 
            if (ATTR_NAME_DELETED.equals(color.getAttribute(StyleConstants.NameAttribute))) DiffModuleConfig.getDefault().setDeletedColor((Color) color.getAttribute(StyleConstants.Background)); 
            if (ATTR_NAME_MERGE_APPLIED.equals(color.getAttribute(StyleConstants.NameAttribute))) DiffModuleConfig.getDefault().setAppliedColor((Color) color.getAttribute(StyleConstants.Background)); 
            if (ATTR_NAME_MERGE_NOTAPPLIED.equals(color.getAttribute(StyleConstants.NameAttribute))) DiffModuleConfig.getDefault().setNotAppliedColor((Color) color.getAttribute(StyleConstants.Background)); 
            if (ATTR_NAME_MERGE_UNRESOLVED.equals(color.getAttribute(StyleConstants.NameAttribute))) DiffModuleConfig.getDefault().setUnresolvedColor((Color) color.getAttribute(StyleConstants.Background));
            if (ATTR_NAME_SIDEBAR_DELETED.equals(color.getAttribute(StyleConstants.NameAttribute))) DiffModuleConfig.getDefault().setSidebarDeletedColor((Color) color.getAttribute(StyleConstants.Background));
            if (ATTR_NAME_SIDEBAR_CHANGED.equals(color.getAttribute(StyleConstants.NameAttribute))) DiffModuleConfig.getDefault().setSidebarChangedColor((Color) color.getAttribute(StyleConstants.Background));
        }
    }
    
    public boolean isChanged () {
        return changed;
    }
    
    public void setCurrentProfile (String currentProfile) {
        refreshUI ();
    }

    public void deleteProfile (String profile) {
    }
    
    public JComponent getComponent() {
        return this;
    }
        
    // other methods ...........................................................
    
    Collection<AttributeSet> getHighlightings () {
        return getCategories();
    }
    
    private static String loc (String key) {
        return NbBundle.getMessage (DiffColorsPanel.class, key);
    }
    
    private void updateData () {
        int index = lCategories.getSelectedIndex();
        if (index < 0) return;
        
        List<AttributeSet> categories = getCategories();
        AttributeSet category = categories.get(lCategories.getSelectedIndex());
        SimpleAttributeSet c = new SimpleAttributeSet(category);
        
        Color color = ColorComboBox.getColor(cbBackground);
        if (color != null) {
            c.addAttribute(StyleConstants.Background, color);
        } else {
            c.removeAttribute(StyleConstants.Background);
        }
        
        categories.set(index, c);
    }
    
    private void refreshUI () {
        int index = lCategories.getSelectedIndex();
        if (index < 0) {
            cbBackground.setEnabled(false);
            return;
        }
        cbBackground.setEnabled(true);
        
        List<AttributeSet> categories = getCategories();
	    AttributeSet category = categories.get(index);
        
        listen = false;
        // set values
        org.netbeans.modules.diff.options.ColorComboBox.setColor(cbBackground, (Color) category.getAttribute(StyleConstants.Background));
        listen = true;
    }
    
    private List<AttributeSet> getCategories() {
        if (categories == null) {
            categories = getDiffHighlights();
//            Collections.sort(categories, new org.netbeans.modules.options.colors.CategoryComparator());
        }
        return categories;
    }

    private List<AttributeSet> getDiffHighlights() {
        List<AttributeSet> attrs = new ArrayList<AttributeSet>();
        SimpleAttributeSet sas = null;
        
        sas = new SimpleAttributeSet();
        StyleConstants.setBackground(sas, DiffModuleConfig.getDefault().getAddedColor());
        sas.addAttribute(StyleConstants.NameAttribute, ATTR_NAME_ADDED);
        sas.addAttribute(EditorStyleConstants.DisplayName, NbBundle.getMessage(DiffOptionsPanel.class, "LBL_AddedColor"));
        attrs.add(sas);

        sas = new SimpleAttributeSet();
        StyleConstants.setBackground(sas, DiffModuleConfig.getDefault().getDeletedColor());
        sas.addAttribute(StyleConstants.NameAttribute, ATTR_NAME_DELETED);
        sas.addAttribute(EditorStyleConstants.DisplayName, NbBundle.getMessage(DiffOptionsPanel.class, "LBL_DeletedColor"));
        attrs.add(sas);

        sas = new SimpleAttributeSet();
        StyleConstants.setBackground(sas, DiffModuleConfig.getDefault().getChangedColor());
        sas.addAttribute(StyleConstants.NameAttribute, ATTR_NAME_CHANGED);
        sas.addAttribute(EditorStyleConstants.DisplayName, NbBundle.getMessage(DiffOptionsPanel.class, "LBL_ChangedColor"));
        attrs.add(sas);
        
        sas = new SimpleAttributeSet();
        StyleConstants.setBackground(sas, DiffModuleConfig.getDefault().getAppliedColor());
        sas.addAttribute(StyleConstants.NameAttribute, ATTR_NAME_MERGE_APPLIED);
        sas.addAttribute(EditorStyleConstants.DisplayName, NbBundle.getMessage(DiffOptionsPanel.class, "LBL_AppliedColor"));
        attrs.add(sas);

        sas = new SimpleAttributeSet();
        StyleConstants.setBackground(sas, DiffModuleConfig.getDefault().getNotAppliedColor());
        sas.addAttribute(StyleConstants.NameAttribute, ATTR_NAME_MERGE_NOTAPPLIED);
        sas.addAttribute(EditorStyleConstants.DisplayName, NbBundle.getMessage(DiffOptionsPanel.class, "LBL_NotAppliedColor"));
        attrs.add(sas);

        sas = new SimpleAttributeSet();
        StyleConstants.setBackground(sas, DiffModuleConfig.getDefault().getUnresolvedColor());
        sas.addAttribute(StyleConstants.NameAttribute, ATTR_NAME_MERGE_UNRESOLVED);
        sas.addAttribute(EditorStyleConstants.DisplayName, NbBundle.getMessage(DiffOptionsPanel.class, "LBL_UnresolvedColor"));
        attrs.add(sas);

        sas = new SimpleAttributeSet();
        StyleConstants.setBackground(sas, DiffModuleConfig.getDefault().getSidebarDeletedColor());
        sas.addAttribute(StyleConstants.NameAttribute, ATTR_NAME_SIDEBAR_DELETED);
        sas.addAttribute(EditorStyleConstants.DisplayName, NbBundle.getMessage(DiffOptionsPanel.class, "LBL_SidebarDeletedColor"));
        attrs.add(sas);

        sas = new SimpleAttributeSet();
        StyleConstants.setBackground(sas, DiffModuleConfig.getDefault().getSidebarChangedColor());
        sas.addAttribute(StyleConstants.NameAttribute, ATTR_NAME_SIDEBAR_CHANGED);
        sas.addAttribute(EditorStyleConstants.DisplayName, NbBundle.getMessage(DiffOptionsPanel.class, "LBL_SidebarChangedColor"));
        attrs.add(sas);

        return attrs;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lCategories = new javax.swing.JList();
        jLabel3 = new javax.swing.JLabel();
        cbBackground = new javax.swing.JComboBox();

        jLabel1.setLabelFor(lCategories);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DiffColorsPanel.class, "DiffColorsPanel.jLabel1.text")); // NOI18N

        lCategories.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(lCategories);

        jLabel3.setLabelFor(cbBackground);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(DiffColorsPanel.class, "DiffColorsPanel.jLabel3.text")); // NOI18N

        cbBackground.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jLabel3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cbBackground, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 215, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jLabel1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(cbBackground, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel3))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbBackground;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList lCategories;
    // End of variables declaration//GEN-END:variables
    
}

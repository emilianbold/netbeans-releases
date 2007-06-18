/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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

    private ColorModel colorModel = null;
    private boolean		listen = false;
    private String              currentProfile;
    /** cache Map (String (profile name) > Vector (AttributeSet)). */
    private Map<String, Vector<AttributeSet>> profileToCategories = new HashMap<String, Vector<AttributeSet>>();
    /** Set (String (profile name)) of changed profile names. */
    private Set<String> toBeSaved = new HashSet<String>();
    private boolean             changed = false;
    
    public DiffColorsPanel() {
        initComponents ();

        setName(loc("LBL_DiffOptions_Tab")); //NOI18N
        
        org.netbeans.modules.diff.options.ColorComboBox.init (cbBackground);
        lCategories.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
        lCategories.setVisibleRowCount (3);
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
    
    public void update (ColorModel colorModel) {
        this.colorModel = colorModel;
        currentProfile = colorModel.getCurrentProfile ();
        listen = false;
        setCurrentProfile (currentProfile);
        lCategories.setListData (getCategories (currentProfile));
        lCategories.setSelectedIndex (0);
        refreshUI ();	
        listen = true;
        changed = false;
    }
    
    public void cancel () {
        toBeSaved = new HashSet<String>();
        profileToCategories = new HashMap<String, Vector<AttributeSet>>();        
        changed = false;
    }
    
    public void applyChanges() {
        if (colorModel == null) return;
        for(String profile : toBeSaved) {
            Vector<AttributeSet> colors = getCategories(profile);
            for (AttributeSet color : colors) {
                if (ATTR_NAME_ADDED.equals(color.getAttribute(StyleConstants.NameAttribute))) DiffModuleConfig.getDefault().setAddedColor((Color) color.getAttribute(StyleConstants.Background)); 
                if (ATTR_NAME_CHANGED.equals(color.getAttribute(StyleConstants.NameAttribute))) DiffModuleConfig.getDefault().setChangedColor((Color) color.getAttribute(StyleConstants.Background)); 
                if (ATTR_NAME_DELETED.equals(color.getAttribute(StyleConstants.NameAttribute))) DiffModuleConfig.getDefault().setDeletedColor((Color) color.getAttribute(StyleConstants.Background)); 
            }
        }
        toBeSaved = new HashSet<String>();
        profileToCategories = new HashMap<String, Vector<AttributeSet>>();
    }
    
    public boolean isChanged () {
        return changed;
    }
    
    public void setCurrentProfile (String currentProfile) {
        String oldScheme = this.currentProfile;
        this.currentProfile = currentProfile;
        if (!colorModel.getProfiles ().contains (currentProfile)) {
            // clone profile
            Vector<AttributeSet> categories = getCategories (oldScheme);
            profileToCategories.put (currentProfile, new Vector<AttributeSet>(categories));
            toBeSaved.add (currentProfile);
        }
        refreshUI ();
    }

    public void deleteProfile (String profile) {
        if (colorModel.isCustomProfile (profile))
            profileToCategories.put (profile, null);
        else {
            profileToCategories.put (profile, getDefaults (profile));
            refreshUI ();
        }
        toBeSaved.add (profile);
    }
    
    public JComponent getComponent() {
        return this;
    }
        
    // other methods ...........................................................
    
    Collection<AttributeSet> getHighlightings () {
        return getCategories(currentProfile);
    }
    
    private static String loc (String key) {
        return NbBundle.getMessage (DiffColorsPanel.class, key);
    }
    
    private void updateData () {
        int index = lCategories.getSelectedIndex();
        if (index < 0) return;
        
        Vector<AttributeSet> categories = getCategories(currentProfile);
        AttributeSet category = categories.get(lCategories.getSelectedIndex());
        SimpleAttributeSet c = new SimpleAttributeSet(category);
        
        Color color = ColorComboBox.getColor(cbBackground);
        if (color != null) {
            c.addAttribute(StyleConstants.Background, color);
        } else {
            c.removeAttribute(StyleConstants.Background);
        }
        
        categories.set(index, c);
        toBeSaved.add(currentProfile);
    }
    
    private void refreshUI () {
        int index = lCategories.getSelectedIndex ();
        if (index < 0) {
            cbBackground.setEnabled (false);
            return;
        }
        cbBackground.setEnabled (true);
        
        Vector<AttributeSet> categories = getCategories (currentProfile);
	    AttributeSet category = categories.get (index);
        
        listen = false;
        
        // set defaults
        AttributeSet defAs = getDefaultColoring();
        if (defAs != null) {
            Color inheritedForeground = (Color) defAs.getAttribute(StyleConstants.Foreground);
            if (inheritedForeground == null) {
                inheritedForeground = Color.black;
            }
            
            Color inheritedBackground = (Color) defAs.getAttribute(StyleConstants.Background);
            if (inheritedBackground == null) {
                inheritedBackground = Color.white;
            }
            org.netbeans.modules.diff.options.ColorComboBox.setInheritedColor(cbBackground, inheritedBackground);
        }
        
        // set values
        org.netbeans.modules.diff.options.ColorComboBox.setColor (
            cbBackground,
            (Color) category.getAttribute (StyleConstants.Background)
        );
        listen = true;
    }
    
    private AttributeSet getDefaultColoring() {
        Collection/*<AttributeSet>*/ defaults = colorModel.getCategories(currentProfile, ColorModel.ALL_LANGUAGES);
        
        for(Iterator i = defaults.iterator(); i.hasNext(); ) {
            AttributeSet as = (AttributeSet) i.next();
            String name = (String) as.getAttribute(StyleConstants.NameAttribute);
            if (name != null && "default".equals(name)) { //NOI18N
                return as;
            }
        }
        
        return null;
    }
    
    private Vector<AttributeSet> getCategories(String profile) {
        if (colorModel == null) return null;
        if (!profileToCategories.containsKey(profile)) {
            Collection<AttributeSet> c = getDiffHighlights(colorModel, profile);
            if (c == null) {
                c = Collections.<AttributeSet>emptySet(); // XXX OK?
            }
            List<AttributeSet> l = new ArrayList<AttributeSet>(c);
            Collections.sort(l, new org.netbeans.modules.options.colors.CategoryComparator());
            profileToCategories.put(profile, new Vector<AttributeSet>(l));
        }
        return profileToCategories.get(profile);
    }

    private Collection<AttributeSet> getDiffHighlights(ColorModel colorModel, String profile) {
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
        
        return attrs;
    }

    /** cache Map (String (profile name) > Vector (AttributeSet)). */
    private Map<String, Vector<AttributeSet>> profileToDefaults = new HashMap<String, Vector<AttributeSet>>();
    
    private Vector<AttributeSet> getDefaults(String profile) {
        if (!profileToDefaults.containsKey(profile)) {
            Collection<AttributeSet> c = colorModel.getHighlightingDefaults(profile);
            List<AttributeSet> l = new ArrayList<AttributeSet>(c);
            Collections.sort(l, new org.netbeans.modules.options.colors.CategoryComparator());
            profileToDefaults.put(profile, new Vector<AttributeSet>(l));
        }
        return profileToDefaults.get(profile);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
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

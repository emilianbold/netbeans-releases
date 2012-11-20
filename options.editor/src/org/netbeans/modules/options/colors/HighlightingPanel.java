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

package org.netbeans.modules.options.colors;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.options.colors.spi.FontsColorsController;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.awt.ColorComboBox;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author  Jan Jancura
 */
@OptionsPanelController.Keywords(keywords={"#KW_HighlightPanel"}, location=OptionsDisplayer.FONTSANDCOLORS, tabTitle= "#Editor_tab.displayName")
public class HighlightingPanel extends JPanel implements ActionListener, ItemListener, FontsColorsController {
    
    private ColorModel          colorModel = null;
    private boolean             listen = false;
    private String              currentProfile;
    /** cache Map (String (profile name) > Vector (AttributeSet)). */
    private Map<String, Vector<AttributeSet>> profileToCategories = new HashMap<String, Vector<AttributeSet>>();
    /** Set (String (profile name)) of changed profile names. */
    private Set<String>         toBeSaved = new HashSet<String>();
    private boolean             changed = false;

    
    /** Creates new form Highlightingpanel1 */
    public HighlightingPanel () {
        initComponents ();

        setName(loc("Editor_tab")); //NOI18N
        
        // 1) init components
        lCategories.getAccessibleContext ().setAccessibleName (loc ("AN_Categories"));
        lCategories.getAccessibleContext ().setAccessibleDescription (loc ("AD_Categories"));
        cbForeground.getAccessibleContext ().setAccessibleName (loc ("AN_Foreground_Chooser"));
        cbForeground.getAccessibleContext ().setAccessibleDescription (loc ("AD_Foreground_Chooser"));
        cbBackground.getAccessibleContext ().setAccessibleName (loc ("AN_Background_Chooser"));
        cbBackground.getAccessibleContext ().setAccessibleDescription (loc ("AD_Background_Chooser"));
        lCategories.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
        lCategories.setVisibleRowCount (3);
        lCategories.addListSelectionListener (new ListSelectionListener () {
            @Override
            public void valueChanged (ListSelectionEvent e) {
                if (!listen) return;
                refreshUI ();
            }
        });
        lCategories.setCellRenderer (new CategoryRenderer ());
        cbForeground.addItemListener(this);
        cbBackground.addItemListener (this);

        lCategory.setLabelFor (lCategories);
        loc (lCategory, "CTL_Category");
        loc (lForeground, "CTL_Foreground_label");
        loc (lBackground, "CTL_Background_label");
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lCategory = new javax.swing.JLabel();
        cpCategories = new javax.swing.JScrollPane();
        lCategories = new javax.swing.JList();
        lForeground = new javax.swing.JLabel();
        lBackground = new javax.swing.JLabel();
        cbBackground = new ColorComboBox();
        cbForeground = new ColorComboBox();

        lCategory.setText(org.openide.util.NbBundle.getMessage(HighlightingPanel.class, "CTL_Category")); // NOI18N

        cpCategories.setViewportView(lCategories);

        lForeground.setText(org.openide.util.NbBundle.getMessage(HighlightingPanel.class, "CTL_Foreground_label")); // NOI18N

        lBackground.setText(org.openide.util.NbBundle.getMessage(HighlightingPanel.class, "CTL_Background_label")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cpCategories, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE)
                        .addGap(20, 20, 20)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lBackground)
                            .addComponent(lForeground))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbBackground, javax.swing.GroupLayout.Alignment.TRAILING, 0, 49, Short.MAX_VALUE)
                            .addComponent(cbForeground, javax.swing.GroupLayout.Alignment.TRAILING, 0, 49, Short.MAX_VALUE)))
                    .addComponent(lCategory))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lCategory)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lForeground)
                            .addComponent(cbForeground, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lBackground)
                            .addComponent(cbBackground, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(cpCategories, javax.swing.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbBackground;
    private javax.swing.JComboBox cbForeground;
    private javax.swing.JScrollPane cpCategories;
    private javax.swing.JLabel lBackground;
    private javax.swing.JList lCategories;
    private javax.swing.JLabel lCategory;
    private javax.swing.JLabel lForeground;
    // End of variables declaration//GEN-END:variables
    
 
    @Override
    public void actionPerformed (ActionEvent evt) {
        if (!listen) return;
        updateData ();
        changed = true;
    }
    
    @Override
    public void itemStateChanged( ItemEvent e ) {
        if( e.getStateChange() == ItemEvent.DESELECTED )
            return;
        if (!listen) return;
        updateData ();
        changed = true;
    }
    
    @Override
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
    
    @Override
    public void cancel () {
        toBeSaved = new HashSet<String>();
        profileToCategories = new HashMap<String, Vector<AttributeSet>>();        
        changed = false;
    }
    
    @Override
    public void applyChanges() {
        if (colorModel == null) return;
        for(String profile : toBeSaved) {
            Vector<AttributeSet> cat = null;
            // Fix of #191686: don't ask just deleted profile for its
            // categories - it caused recreation of the profile
            if (profileToCategories.containsKey(profile)) {
                cat = getCategories(profile);
            }
            colorModel.setHighlightings(profile, cat);
        }
        toBeSaved = new HashSet<String>();
        profileToCategories = new HashMap<String, Vector<AttributeSet>>();
    }
    
    @Override
    public boolean isChanged () {
        return changed;
    }
    
    @Override
    public void setCurrentProfile (String currentProfile) {
        String oldScheme = this.currentProfile;
        this.currentProfile = currentProfile;
        if (!colorModel.getProfiles ().contains (currentProfile) &&
            !profileToCategories.containsKey (currentProfile)
        ) {
            // clone profile
            Vector<AttributeSet> categories = getCategories (oldScheme);
            profileToCategories.put (currentProfile, new Vector<AttributeSet>(categories));
            toBeSaved.add (currentProfile);
        }
        refreshUI ();
    }

    @Override
    public void deleteProfile (String profile) {
        if (colorModel.isCustomProfile (profile))
            profileToCategories.remove (profile);
        else {
            profileToCategories.put (profile, getDefaults (profile));
            refreshUI ();
        }
        toBeSaved.add (profile);
    }
    
    @Override
    public JComponent getComponent() {
        return this;
    }
        
    // other methods ...........................................................
    
    Collection<AttributeSet> getHighlightings () {
        return getCategories(currentProfile);
    }
    
    private static String loc (String key) {
        return NbBundle.getMessage (SyntaxColoringPanel.class, key);
    }
    
    private static void loc (Component c, String key) {
        if (c instanceof AbstractButton)
            Mnemonics.setLocalizedText (
                (AbstractButton) c, 
                loc (key)
            );
        else
            Mnemonics.setLocalizedText (
                (JLabel) c, 
                loc (key)
            );
    }

    private void updateData () {
        int index = lCategories.getSelectedIndex();
        if (index < 0) return;
        
        Vector<AttributeSet> categories = getCategories(currentProfile);
        AttributeSet category = categories.get(lCategories.getSelectedIndex());
        SimpleAttributeSet c = new SimpleAttributeSet(category);
        
        Color color = ColorComboBoxSupport.getSelectedColor( (ColorComboBox)cbBackground );
        if (color != null) {
            c.addAttribute(StyleConstants.Background, color);
        } else {
            c.removeAttribute(StyleConstants.Background);
        }
        
        color = ColorComboBoxSupport.getSelectedColor( (ColorComboBox)cbForeground );
        if (color != null) {
            c.addAttribute(StyleConstants.Foreground, color);
        } else {
            c.removeAttribute(StyleConstants.Foreground);
        }
        
        categories.set(index, c);
        toBeSaved.add(currentProfile);
    }
    
    private void refreshUI () {
        int index = lCategories.getSelectedIndex ();
        if (index < 0) {
            cbForeground.setEnabled (false);
            cbBackground.setEnabled (false);
            return;
        }
        cbForeground.setEnabled (true);
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
            ColorComboBoxSupport.setInheritedColor((ColorComboBox)cbForeground, inheritedForeground);
            
            Color inheritedBackground = (Color) defAs.getAttribute(StyleConstants.Background);
            if (inheritedBackground == null) {
                inheritedBackground = Color.white;
            }
            ColorComboBoxSupport.setInheritedColor((ColorComboBox)cbBackground, inheritedBackground);
        }
        
        // set values
        ColorComboBoxSupport.setSelectedColor((ColorComboBox)cbForeground, (Color) category.getAttribute (StyleConstants.Foreground));
        ColorComboBoxSupport.setSelectedColor((ColorComboBox)cbBackground, (Color) category.getAttribute (StyleConstants.Background));
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
            Collection<AttributeSet> c = colorModel.getHighlightings(profile);
            if (c == null) {
                c = Collections.<AttributeSet>emptySet(); // XXX OK?
            }
            List<AttributeSet> l = new ArrayList<AttributeSet>(c);
            Collections.sort(l, new CategoryComparator());
            profileToCategories.put(profile, new Vector<AttributeSet>(l));
        }
        return profileToCategories.get(profile);
    }

    /** cache Map (String (profile name) > Vector (AttributeSet)). */
    private Map<String, Vector<AttributeSet>> profileToDefaults = new HashMap<String, Vector<AttributeSet>>();
    
    private Vector<AttributeSet> getDefaults(String profile) {
        if (!profileToDefaults.containsKey(profile)) {
            Collection<AttributeSet> c = colorModel.getHighlightingDefaults(profile);
            List<AttributeSet> l = new ArrayList<AttributeSet>(c);
            Collections.sort(l, new CategoryComparator());
            profileToDefaults.put(profile, new Vector<AttributeSet>(l));
        }
        return profileToDefaults.get(profile);
    }
}

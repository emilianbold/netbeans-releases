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

import org.netbeans.modules.options.colors.spi.FontsColorsController;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.modules.options.colors.ColorModel.Preview;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author  Jan Jancura
 */
public class SyntaxColoringPanel extends JPanel implements ActionListener, 
    PropertyChangeListener, FontsColorsController {
    
    
    private Preview             preview;
    private Task                selectTask;
    private ColorModel          colorModel = null;
    private String              currentLanguage;
    private String              currentProfile;
    /** cache Map (String (profile name) > Map (String (language name) > Vector (AttributeSet))). */
    private Map<String, Map<String, Vector<AttributeSet>>>
                                profiles = new HashMap<String, Map<String, Vector<AttributeSet>>>();
    /** Map (String (profile name) > Set (String (language name))) of names of changed languages. */
    private Map<String, Set<String>>
                                toBeSaved = new HashMap<String, Set<String>>();
    private boolean             listen = false;


    /** Creates new form SyntaxColoringPanel1 */
    public SyntaxColoringPanel () {
        initComponents ();
        setName(loc("Syntax_coloring_tab")); //NOI18N
        // 1) init components
        cbLanguage.getAccessibleContext ().setAccessibleName (loc ("AN_Languages"));
        cbLanguage.getAccessibleContext ().setAccessibleDescription (loc ("AD_Languages"));
        lCategories.getAccessibleContext ().setAccessibleName (loc ("AN_Categories"));
        lCategories.getAccessibleContext ().setAccessibleDescription (loc ("AD_Categories"));
        bFont.getAccessibleContext ().setAccessibleName (loc ("AN_Font"));
        bFont.getAccessibleContext ().setAccessibleDescription (loc ("AD_Font"));
        cbForeground.getAccessibleContext ().setAccessibleName (loc ("AN_Foreground_Chooser"));
        cbForeground.getAccessibleContext ().setAccessibleDescription (loc ("AD_Foreground_Chooser"));
        cbBackground.getAccessibleContext ().setAccessibleName (loc ("AN_Background_Chooser"));
        cbBackground.getAccessibleContext ().setAccessibleDescription (loc ("AD_Background_Chooser"));
        cbEffects.getAccessibleContext ().setAccessibleName (loc ("AN_Efects_Color_Chooser"));
        cbEffects.getAccessibleContext ().setAccessibleDescription (loc ("AD_Efects_Color_Chooser"));
        cbEffectColor.getAccessibleContext ().setAccessibleName (loc ("AN_Efects_Color"));
        cbEffectColor.getAccessibleContext ().setAccessibleDescription (loc ("AD_Efects_Color"));
        ColorComboBox.init (cbBackground);
        ColorComboBox.init (cbForeground);
        ColorComboBox.init (cbEffectColor);
        cbLanguage.addActionListener (this);
        lCategories.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
        lCategories.setVisibleRowCount (3);
        lCategories.setCellRenderer (new CategoryRenderer ());
        lCategories.addListSelectionListener (new ListSelectionListener () {
            @Override
            public void valueChanged (ListSelectionEvent e) {
                if (!listen) return;
                selectTask.schedule (200);
            }
        });
        tfFont.setEditable (false);
        bFont.addActionListener (this);
        bFont.setMargin (new Insets (0, 0, 0, 0));
        cbForeground.addActionListener (this);
        ((JComponent)cbForeground.getEditor()).addPropertyChangeListener (this);

        cbBackground.addActionListener (this);
        ((JComponent)cbBackground.getEditor()).addPropertyChangeListener (this);
        
        cbEffects.addItem (loc ("CTL_Effects_None"));
        cbEffects.addItem (loc ("CTL_Effects_Underlined"));
        cbEffects.addItem (loc ("CTL_Effects_Wave_Underlined"));
        cbEffects.addItem (loc ("CTL_Effects_Strike_Through"));
        cbEffects.getAccessibleContext ().setAccessibleName (loc ("AN_Effects"));
        cbEffects.getAccessibleContext ().setAccessibleDescription (loc ("AD_Effects"));
        cbEffects.addActionListener (this);
        ((JComponent)cbEffectColor.getEditor()).addPropertyChangeListener (this);
        cbEffectColor.addActionListener (this);
        
        loc(bFont, "CTL_Font_button");
        loc(lBackground, "CTL_Background_label");
        loc(lCategory, "CTL_Category");
        loc(lEffectColor, "CTL_Effects_color");
        loc(lEffects, "CTL_Effects_label");
        loc(lFont, "CTL_Font");
        loc(lForeground, "CTL_Foreground_label");
        loc(lLanguage, "CTL_Languages");
        loc(lPreview, "CTL_Preview");

        selectTask = new RequestProcessor ("SyntaxColoringPanel1").create (
            new Runnable () {
                public void run () {
                    refreshUI ();
                    if (!blink) return;
                    startBlinking ();
                }
            }
        );
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        lLanguage = new javax.swing.JLabel();
        cbLanguage = new javax.swing.JComboBox();
        lCategory = new javax.swing.JLabel();
        spCategories = new javax.swing.JScrollPane();
        lCategories = new javax.swing.JList();
        lPreview = new javax.swing.JLabel();
        spPreview = new javax.swing.JScrollPane();
        pPreview = new javax.swing.JPanel();
        lFont = new javax.swing.JLabel();
        lForeground = new javax.swing.JLabel();
        lBackground = new javax.swing.JLabel();
        lEffects = new javax.swing.JLabel();
        lEffectColor = new javax.swing.JLabel();
        cbForeground = new javax.swing.JComboBox();
        cbBackground = new javax.swing.JComboBox();
        cbEffects = new javax.swing.JComboBox();
        cbEffectColor = new javax.swing.JComboBox();
        tfFont = new javax.swing.JTextField();
        bFont = new javax.swing.JButton();

        lLanguage.setLabelFor(cbLanguage);
        lLanguage.setText("Language:");

        lCategory.setLabelFor(lCategories);
        lCategory.setText("Category:");

        spCategories.setViewportView(lCategories);

        lPreview.setText("Preview:");

        spPreview.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        spPreview.setAutoscrolls(true);

        pPreview.setAutoscrolls(true);
        pPreview.setLayout(new java.awt.BorderLayout());
        spPreview.setViewportView(pPreview);

        lFont.setLabelFor(bFont);
        lFont.setText("Font:");

        lForeground.setLabelFor(cbForeground);
        lForeground.setText("Foreground:");

        lBackground.setLabelFor(cbBackground);
        lBackground.setText("Background:");

        lEffects.setLabelFor(cbEffects);
        lEffects.setText("Effects:");

        lEffectColor.setLabelFor(cbEffectColor);
        lEffectColor.setText("Effect Color:");

        bFont.setText("...");
        bFont.setMargin(new java.awt.Insets(2, 2, 2, 2));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(spPreview, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(lLanguage)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cbLanguage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(lCategory)
                    .add(layout.createSequentialGroup()
                        .add(spCategories, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lEffectColor)
                            .add(lForeground)
                            .add(lFont)
                            .add(lEffects)
                            .add(lBackground))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(tfFont, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 13, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(bFont))
                            .add(cbForeground, 0, 38, Short.MAX_VALUE)
                            .add(cbBackground, 0, 38, Short.MAX_VALUE)
                            .add(cbEffects, 0, 38, Short.MAX_VALUE)
                            .add(cbEffectColor, 0, 38, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                    .add(lPreview))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lLanguage)
                    .add(cbLanguage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lCategory)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(spCategories, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 130, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(lPreview))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(lFont)
                            .add(tfFont, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(bFont))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(lForeground)
                            .add(cbForeground, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(lBackground)
                            .add(cbBackground, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(lEffects)
                            .add(cbEffects, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(lEffectColor)
                            .add(cbEffectColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(spPreview, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bFont;
    private javax.swing.JComboBox cbBackground;
    private javax.swing.JComboBox cbEffectColor;
    private javax.swing.JComboBox cbEffects;
    private javax.swing.JComboBox cbForeground;
    private javax.swing.JComboBox cbLanguage;
    private javax.swing.JLabel lBackground;
    private javax.swing.JList lCategories;
    private javax.swing.JLabel lCategory;
    private javax.swing.JLabel lEffectColor;
    private javax.swing.JLabel lEffects;
    private javax.swing.JLabel lFont;
    private javax.swing.JLabel lForeground;
    private javax.swing.JLabel lLanguage;
    private javax.swing.JLabel lPreview;
    private javax.swing.JPanel pPreview;
    private javax.swing.JScrollPane spCategories;
    private javax.swing.JScrollPane spPreview;
    private javax.swing.JTextField tfFont;
    // End of variables declaration//GEN-END:variables
    

    @Override
    public void actionPerformed (ActionEvent evt) {
        if (!listen) return;
	if (evt.getSource () == cbEffects) {
	    cbEffectColor.setEnabled (cbEffects.getSelectedIndex () > 0);
            if (cbEffects.getSelectedIndex () == 0)
                ColorComboBox.setColor (cbEffectColor, null);
            updateData ();
	} else
	if (evt.getSource () == cbLanguage) {
	    setCurrentLanguage ((String) cbLanguage.getSelectedItem ());
	} else
        if (evt.getSource () == bFont) {
            PropertyEditor pe = PropertyEditorManager.findEditor (Font.class);
            AttributeSet category = getCurrentCategory ();
            if (category == null) {
                return;
            }
            Font f = getFont (category);
            pe.setValue (f);
            DialogDescriptor dd = new DialogDescriptor (
                pe.getCustomEditor (),
                loc ("CTL_Font_Chooser")                          // NOI18N
            );
            dd.setOptions (new Object[] {
                DialogDescriptor.OK_OPTION, 
                loc ("CTL_Font_Inherited"),                          // NOI18N
                DialogDescriptor.CANCEL_OPTION
            });
            DialogDisplayer.getDefault ().createDialog (dd).setVisible (true);
            if (dd.getValue () == DialogDescriptor.OK_OPTION) {
                f = (Font) pe.getValue ();
                category = modifyFont (category, f);
                replaceCurrrentCategory (category);
                setToBeSaved (currentProfile, currentLanguage);
                refreshUI (); // refresh font viewer
            } else
            if (dd.getValue ().equals (loc ("CTL_Font_Inherited"))) {
                String fontName = (String) getDefault (currentLanguage, category, StyleConstants.FontFamily);
                int style = 0;
                if (Boolean.TRUE.equals(getDefault (currentLanguage, category, StyleConstants.Bold))) {
                    style += Font.BOLD;
                }
                if (Boolean.TRUE.equals(getDefault (currentLanguage, category, StyleConstants.Italic))) {
                    style += Font.ITALIC;
                }
                Integer size = (Integer) getDefault (currentLanguage, category, StyleConstants.FontSize);
                
                f = new Font (fontName, style, size == null ? getDefaultFontSize() : size);
                category = modifyFont (category, f);
                replaceCurrrentCategory (category);
                setToBeSaved (currentProfile, currentLanguage);
                refreshUI (); // refresh font viewer
            }
        } else
        if (evt.getSource () instanceof JComboBox) {
            updateData ();
        }
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!listen || evt.getPropertyName() == null ) {
            return;
        }
        
        if (Preview.PROP_CURRENT_ELEMENT.equals(evt.getPropertyName())) {
            String currentCategory = (String) evt.getNewValue();
            Vector<AttributeSet> categories = getCategories(currentProfile, currentLanguage);
            if (currentLanguage.equals(ColorModel.ALL_LANGUAGES)) {
                String converted = (String) convertALC.get(currentCategory);
                if (converted != null) {
                    currentCategory = converted;
                }
            }

            for (int i = 0; i < categories.size(); i++) {
                AttributeSet as = categories.get(i);
                if (currentCategory.equals(as.getAttribute(StyleConstants.NameAttribute))) {
                    blink = false;
                    lCategories.setSelectedIndex(i);
                    lCategories.ensureIndexIsVisible(i);
                    blink = true;
                    break;
                }
            }
        } else if (ColorComboBox.PROP_COLOR.equals(evt.getPropertyName())) {
            updateData();
        }
    }
    
    @Override
    public void update (ColorModel colorModel) {
        this.colorModel = colorModel;
        currentProfile = colorModel.getCurrentProfile ();
        currentLanguage = ColorModel.ALL_LANGUAGES;
        if (preview != null) 
            preview.removePropertyChangeListener 
                (Preview.PROP_CURRENT_ELEMENT, this);
        Component component = colorModel.getSyntaxColoringPreviewComponent 
            (currentLanguage);
        preview = (Preview) component;
        pPreview.removeAll ();
        pPreview.add ("Center", component);
        preview.addPropertyChangeListener 
            (Preview.PROP_CURRENT_ELEMENT, this);
        listen = false;
        List<String> languages = new ArrayList<String>(colorModel.getLanguages ());
        languages.remove ("text/x-all-languages");
        Collections.sort (languages, new LanguagesComparator ());
        Iterator it = languages.iterator ();
        Object lastLanguage = cbLanguage.getSelectedItem ();
        cbLanguage.removeAllItems ();
        while (it.hasNext ())
            cbLanguage.addItem (it.next ());
        listen = true;
        if (lastLanguage != null) {
            cbLanguage.setSelectedItem (lastLanguage);
        } else {
            cbLanguage.setSelectedIndex (0);
        }
    }
    
    @Override
    public void cancel () {
        toBeSaved = new HashMap<String, Set<String>>();
        profiles = new HashMap<String, Map<String, Vector<AttributeSet>>>();
    }
    
    @Override
    public void applyChanges() {
        if (colorModel == null) return;
        for(String profile : toBeSaved.keySet()) {
            Set<String> toBeSavedLanguages = toBeSaved.get(profile);
            Map<String, Vector<AttributeSet>> schemeMap = profiles.get(profile);
            for(String languageName : toBeSavedLanguages) {
                colorModel.setCategories(
                    profile,
                    languageName,
                    schemeMap.get(languageName)
                );
            }
        }
        toBeSaved = new HashMap<String, Set<String>>();
        profiles = new HashMap<String, Map<String, Vector<AttributeSet>>>();
    }
    
    @Override
    public boolean isChanged () {
        return !toBeSaved.isEmpty ();
    }
    
    @Override
    public void setCurrentProfile (String currentProfile) {
        String oldProfile = this.currentProfile;
        this.currentProfile = currentProfile;
        if (!colorModel.getProfiles ().contains (currentProfile) && 
            !profiles.containsKey (currentProfile)
        )
            cloneScheme (oldProfile, currentProfile);
        Vector categories = getCategories (currentProfile, currentLanguage);
        lCategories.setListData (categories);
        blink = false;
        lCategories.setSelectedIndex (0);
        blink = true;
        refreshUI ();
    }

    @Override
    public void deleteProfile (String profile) {
        Map<String, Vector<AttributeSet>> m = new HashMap<String, Vector<AttributeSet>> ();
        boolean custom = colorModel.isCustomProfile (profile);
        for (String language : colorModel.getLanguages ()) {
            if (custom) {
                m.put (language, null);
            } else {
                m.put (language, getDefaults (profile, language));
            }
        }
        profiles.put (profile, m);
        toBeSaved.put (profile, new HashSet<String> (colorModel.getLanguages ()));
        if (!custom) {
            refreshUI ();
        }
    }
    
    @Override
    public JComponent getComponent() {
        return this;
    }
        
    // other methods ...........................................................
    
    private void cloneScheme(String oldScheme, String newScheme) {
        Map<String, Vector<AttributeSet>> m = new HashMap<String, Vector<AttributeSet>>();
        for(String language : colorModel.getLanguages()) {
            Vector<AttributeSet> v = getCategories(oldScheme, language);
            Vector<AttributeSet> newV = new Vector<AttributeSet> ();
            Iterator<AttributeSet> it = v.iterator ();
            while (it.hasNext ()) {
                AttributeSet attributeSet = it.next ();
                newV.add(new SimpleAttributeSet (attributeSet));
            }
            m.put(language, new Vector<AttributeSet>(newV));
            setToBeSaved(newScheme, language);
        }
        profiles.put(newScheme, m);
    }
    
    Collection<AttributeSet> getAllLanguages() {
        return getCategories(currentProfile, ColorModel.ALL_LANGUAGES);
    }
    
    Collection<AttributeSet> getSyntaxColorings() {
        return getCategories(currentProfile, currentLanguage);
    }
    
    private void setCurrentLanguage (String language) {
	currentLanguage = language;
        
        // setup categories list
        blink = false;
        lCategories.setListData (getCategories (currentProfile, currentLanguage));
        lCategories.setSelectedIndex (0);
        blink = true;
        refreshUI ();
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

    /**
     * Called on user change.
     * Updates data structures and preview panel.
     */
    private void updateData () {
        int i = lCategories.getSelectedIndex ();
        if (i < 0) return;
        
        AttributeSet category = getCurrentCategory ();
        Color underline = null, 
              wave = null, 
              strikethrough = null;
        if (cbEffects.getSelectedIndex () == 1)
            underline = ColorComboBox.getColor(cbEffectColor);
        if (cbEffects.getSelectedIndex () == 2)
            wave = ColorComboBox.getColor(cbEffectColor);
        if (cbEffects.getSelectedIndex () == 3)
            strikethrough = ColorComboBox.getColor(cbEffectColor);
        
        SimpleAttributeSet c = category != null ? new SimpleAttributeSet(category) : new SimpleAttributeSet();
        
        Color color = ColorComboBox.getColor(cbBackground);
        if (color != null) {
            c.addAttribute(StyleConstants.Background, color);
        } else {
            c.removeAttribute(StyleConstants.Background);
        }
        
        color = ColorComboBox.getColor(cbForeground);
        if (color != null) {
            c.addAttribute(StyleConstants.Foreground, color);
        } else {
            c.removeAttribute(StyleConstants.Foreground);
        }
        
        if (underline != null) {
            c.addAttribute(StyleConstants.Underline, underline);
        } else {
            c.removeAttribute(StyleConstants.Underline);
        }
        
        if (strikethrough != null) {
            c.addAttribute(StyleConstants.StrikeThrough, strikethrough);
        } else {
            c.removeAttribute(StyleConstants.StrikeThrough);
        }
        
        if (wave != null) {
            c.addAttribute(EditorStyleConstants.WaveUnderlineColor, wave);
        } else {
            c.removeAttribute(EditorStyleConstants.WaveUnderlineColor);
        }
        
        replaceCurrrentCategory(c);
        setToBeSaved(currentProfile, currentLanguage);
        updatePreview();
    }

    private boolean                 blink = true;
    private int                     blinkSequence = 0;
    private RequestProcessor.Task   task = new RequestProcessor 
        ("SyntaxColoringPanel").create (new Runnable () {
        public void run () {
            updatePreview ();
            if (blinkSequence == 0) return;
            blinkSequence --;
            task.schedule (250);
        }
    });
    
    private void startBlinking () {
        blinkSequence = 5;
        task.schedule (0);
    }
    
    private void updatePreview () {
        Collection<AttributeSet> syntaxColorings = getSyntaxColorings();
        Collection<AttributeSet> allLanguages = getAllLanguages();
        if ((blinkSequence % 2) == 1) {
            if (ColorModel.ALL_LANGUAGES.equals(currentLanguage)) {
                allLanguages = invertCategory(allLanguages, getCurrentCategory());
            } else {
                syntaxColorings = invertCategory(syntaxColorings, getCurrentCategory());
            }
        }
        preview.setParameters (
            currentLanguage,
            allLanguages,
            Collections.<AttributeSet>emptySet(),
            syntaxColorings
        );
    }
    
    private Collection<AttributeSet> invertCategory (Collection<AttributeSet> c, AttributeSet category) {
        if (category == null) return c;
        ArrayList<AttributeSet> result = new ArrayList<AttributeSet> (c);
        int i = result.indexOf (category);
        SimpleAttributeSet as = new SimpleAttributeSet (category);
        Color highlight = (Color) getValue (currentLanguage, category, StyleConstants.Background);
        if (highlight == null) return result;
        Color newColor = new Color (
            255 - highlight.getRed (),
            255 - highlight.getGreen (),
            255 - highlight.getBlue ()
        );
        as.addAttribute (
            StyleConstants.Underline,
            newColor
        );
        result.set (i, as);
        return result;
    }
    
    /**
     * Called when current category, profile or language has been changed.
     * Updates all ui components.
     */
    private void refreshUI () {
        listen = false;
        AttributeSet category = getCurrentCategory ();
        if (category == null) {
            // no category selected > disable all elements
	    tfFont.setText ("");
            bFont.setEnabled (false);
            cbEffects.setEnabled (false);
            cbForeground.setEnabled (false);
	    cbForeground.setSelectedItem (new ColorValue (null, null));
            cbBackground.setEnabled (false);
	    cbBackground.setSelectedItem (new ColorValue (null, null));
            cbEffectColor.setEnabled (false);
	    cbEffectColor.setSelectedItem (new ColorValue (null, null));
            updatePreview ();
            return;
        }
        bFont.setEnabled (true);
        cbEffects.setEnabled (true);
        cbForeground.setEnabled (true);
        cbBackground.setEnabled (true);
        
        // set defaults
        Color inheritedForeground = (Color) getDefault 
            (currentLanguage, category, StyleConstants.Foreground);
        if (inheritedForeground == null) inheritedForeground = Color.black;
        ColorComboBox.setInheritedColor (cbForeground, inheritedForeground);
        Color inheritedBackground = (Color) getDefault 
            (currentLanguage, category, StyleConstants.Background);
        if (inheritedBackground == null) inheritedBackground = Color.white;
        ColorComboBox.setInheritedColor (cbBackground, inheritedBackground);
        
        String font = fontToString (category);
        tfFont.setText (font);
        ColorComboBox.setColor (
            cbForeground,
            (Color) category.getAttribute (StyleConstants.Foreground)
        );
        ColorComboBox.setColor (
            cbBackground,
            (Color) category.getAttribute (StyleConstants.Background)
        );
        
        if (category.getAttribute (StyleConstants.Underline) != null) {
            cbEffects.setSelectedIndex (1);
            cbEffectColor.setEnabled (true);
            ColorComboBox.setColor (
                cbEffectColor,
                (Color) category.getAttribute (StyleConstants.Underline)
            );
        } else
        if (category.getAttribute (EditorStyleConstants.WaveUnderlineColor) != null) {
            cbEffects.setSelectedIndex (2);
            cbEffectColor.setEnabled (true);
            ColorComboBox.setColor (
                cbEffectColor,
                (Color) category.getAttribute (EditorStyleConstants.WaveUnderlineColor)
            );
        } else
        if (category.getAttribute (StyleConstants.StrikeThrough) != null) {
            cbEffects.setSelectedIndex (3);
            cbEffectColor.setEnabled (true);
            ColorComboBox.setColor (
                cbEffectColor,
                (Color) category.getAttribute (StyleConstants.StrikeThrough)
            );
        } else {
            cbEffects.setSelectedIndex (0);
            cbEffectColor.setEnabled (false);
	    cbEffectColor.setSelectedItem (new ColorValue (null, null));
        }
        updatePreview ();
        listen = true;
    }
    
    private void setToBeSaved(String currentProfile, String currentLanguage) {
        Set<String> s = toBeSaved.get(currentProfile);
        if (s == null) {
            s = new HashSet<String>();
            toBeSaved.put(currentProfile, s);
        }
        s.add(currentLanguage);
    }
    
    private Vector<AttributeSet> getCategories(String profile, String language) {
        if (colorModel == null) return null;
        Map<String, Vector<AttributeSet>> m = profiles.get(profile);
        if (m == null) {
            m = new HashMap<String, Vector<AttributeSet>>();
            profiles.put(profile, m);
        }
        Vector<AttributeSet> v = m.get(language);
        if (v == null) {
            Collection<AttributeSet> c = colorModel.getCategories(profile, language);
            if (c == null) {
                c = Collections.<AttributeSet>emptySet(); // XXX OK?
            }
            List<AttributeSet> l = new ArrayList<AttributeSet>(c);
            Collections.sort(l, new CategoryComparator());
            v = new Vector<AttributeSet>(l);
            m.put(language, v);
        }
        return v;
    }

    private Map<String, Map<String, Vector<AttributeSet>>> defaults = new HashMap<String, Map<String, Vector<AttributeSet>>>();
    /**
     * Returns original colors for given profile.
     */
    private Vector<AttributeSet> getDefaults(String profile, String language) {
        Map<String, Vector<AttributeSet>> m = defaults.get(profile);
        if (m == null) {
            m = new HashMap<String, Vector<AttributeSet>>();
            defaults.put(profile, m);
        }
        Vector<AttributeSet> v = m.get(language);
        if (v == null) {
            Collection<AttributeSet> c = colorModel.getDefaults(profile, language);
            List<AttributeSet> l = new ArrayList<AttributeSet>(c);
            Collections.sort(l, new CategoryComparator());
            v = new Vector<AttributeSet>(l);
            m.put(language, v);
        }
        return new Vector<AttributeSet>(v);
    }
    
    private AttributeSet getCurrentCategory () {
        int i = lCategories.getSelectedIndex ();
        Vector<AttributeSet> c = getCategories(currentProfile, currentLanguage);
        return i >= 0 && i < c.size() ? (AttributeSet) c.get(i) : null;
    }
    
    private void replaceCurrrentCategory (AttributeSet newValues) {
        int i = lCategories.getSelectedIndex ();
        getCategories (currentProfile, currentLanguage).set (i, newValues);
    }
    
    private AttributeSet getCategory (
        String profile, 
        String language, 
        String name
    ) {
        Vector v = getCategories (profile, language);
        Iterator it = v.iterator ();
        while (it.hasNext ()) {
            AttributeSet c = (AttributeSet) it.next ();
            if (c.getAttribute (StyleConstants.NameAttribute).equals (name)) 
                return c;
        }
        return null;
    }
    
    private Object getValue (String language, AttributeSet category, Object key) {
        if (category.isDefined (key))
            return category.getAttribute (key);
        return getDefault (language, category, key);
    }
    
    private Object getDefault (String language, AttributeSet category, Object key) {
	String name = (String) category.getAttribute (EditorStyleConstants.Default);
	if (name == null) name = "default";

	// 1) search current language
	if (!name.equals (category.getAttribute (StyleConstants.NameAttribute))
        ) {
            AttributeSet defaultAS = getCategory 
                (currentProfile, language, name);
            if (defaultAS != null)
                return getValue (language, defaultAS, key);
	}
	
	// 2) search default language
        if (!language.equals (ColorModel.ALL_LANGUAGES)) {
            AttributeSet defaultAS = getCategory 
                (currentProfile, ColorModel.ALL_LANGUAGES, name);
            if (defaultAS != null)
                return getValue (ColorModel.ALL_LANGUAGES, defaultAS, key);
        }
        
        if (key == StyleConstants.FontFamily) return "Monospaced";    // NOI18N
        if (key == StyleConstants.FontSize) return getDefaultFontSize ();
        return null;
    }
    
    private Font getFont (AttributeSet category) {
        String name = (String) getValue (currentLanguage, category, StyleConstants.FontFamily);
        if (name == null) name = "Monospaced";                        // NOI18N
        Integer size = (Integer) getValue (currentLanguage, category, StyleConstants.FontSize);
        if (size == null)
            size = getDefaultFontSize ();
        Boolean bold = (Boolean) getValue (currentLanguage, category, StyleConstants.Bold);
        if (bold == null) bold = Boolean.FALSE;
        Boolean italic = (Boolean) getValue (currentLanguage, category, StyleConstants.Italic);
        if (italic == null) italic = Boolean.FALSE;
        int style = bold.booleanValue () ? Font.BOLD : Font.PLAIN;
        if (italic.booleanValue ()) style += Font.ITALIC;
        return new Font (name, style, size.intValue ());
    }
    
    private AttributeSet modifyFont (AttributeSet category, Font f) {
        String fontName = f.getName ();
        Integer fontSize = new Integer (f.getSize ());
        Boolean bold = Boolean.valueOf (f.isBold ());
        Boolean italic = Boolean.valueOf (f.isItalic ());
        boolean isDefault = "default".equals (
            category.getAttribute (StyleConstants.NameAttribute)
        );
        if (fontName.equals (
            getDefault (currentLanguage, category, StyleConstants.FontFamily)
        ) && !isDefault)
            fontName = null;
        if (fontSize.equals (
            getDefault (currentLanguage, category, StyleConstants.FontSize)
        ) && !isDefault)
            fontSize = null;
        if (bold.equals (getDefault (currentLanguage, category, StyleConstants.Bold))
        )
            bold = null;
        else
        if (bold.equals (Boolean.FALSE) &&
            getDefault (currentLanguage, category, StyleConstants.Bold) == null
        )
            bold = null;
        if (italic.equals (getDefault (currentLanguage, category, StyleConstants.Italic))
        )
            italic = null;
        else
        if (italic.equals (Boolean.FALSE) &&
            getDefault (currentLanguage, category, StyleConstants.Italic) == null
        )
            italic = null;
        SimpleAttributeSet c = new SimpleAttributeSet (category);
        if (fontName != null)
            c.addAttribute (
                StyleConstants.FontFamily,
                fontName
            );
        else
            c.removeAttribute (StyleConstants.FontFamily);
        if (fontSize != null)
            c.addAttribute (
                StyleConstants.FontSize,
                fontSize
            );
        else
            c.removeAttribute (StyleConstants.FontSize);
        if (bold != null)
            c.addAttribute (
                StyleConstants.Bold,
                bold
            );
        else
            c.removeAttribute (StyleConstants.Bold);
        if (italic != null)
            c.addAttribute (
                StyleConstants.Italic,
                italic
            );
        else
            c.removeAttribute (StyleConstants.Italic);
        
        return c;
    }
    
    private String fontToString (AttributeSet category) {
        if ("default".equals (
            category.getAttribute (StyleConstants.NameAttribute)
        )) {
            StringBuffer sb = new StringBuffer ();
            sb.append (getValue (currentLanguage, category, StyleConstants.FontFamily));
            sb.append (' ');
            sb.append (getValue (currentLanguage, category, StyleConstants.FontSize));
            Boolean bold = (Boolean) getValue (currentLanguage, category, StyleConstants.Bold);
            if (bold != null && bold.booleanValue ())
                sb.append (' ').append (loc ("CTL_Bold"));                // NOI18N
            Boolean italic = (Boolean) getValue (currentLanguage, category, StyleConstants.Italic);
            if (italic != null && italic.booleanValue ())
                sb.append (' ').append (loc ("CTL_Italic"));              // NOI18N
            return sb.toString ();
        }
        boolean def = false;
        StringBuffer sb = new StringBuffer ();
        if (category.getAttribute (StyleConstants.FontFamily) != null)
            sb.append ('+').append (category.getAttribute (StyleConstants.FontFamily));
        else
            def = true;
        if (category.getAttribute (StyleConstants.FontSize) != null)
            sb.append ('+').append (category.getAttribute (StyleConstants.FontSize));
        else
            def = true;
        if (Boolean.TRUE.equals (category.getAttribute (StyleConstants.Bold)))
            sb.append ('+').append (loc ("CTL_Bold"));                // NOI18N
        if (Boolean.FALSE.equals (category.getAttribute (StyleConstants.Bold)))
            sb.append ('-').append (loc ("CTL_Bold"));                // NOI18N
        if (Boolean.TRUE.equals (category.getAttribute (StyleConstants.Italic)))
            sb.append ('+').append (loc ("CTL_Italic"));              // NOI18N
        if (Boolean.FALSE.equals (category.getAttribute (StyleConstants.Italic)))
            sb.append ('-').append (loc ("CTL_Italic"));              // NOI18N
        
        if (def) {
            sb.insert (0, loc ("CTL_Inherited"));                     // NOI18N
            return sb.toString ();
        } else {
            String result = sb.toString ();
            return result.replace ('+', ' ');
        }
    }
    
    private static Map<String, String> convertALC = new HashMap<String, String>();
    
    static {
        convertALC.put("character", "char"); //NOI18N
        convertALC.put("errors", "error"); //NOI18N
        convertALC.put("literal", "keyword"); //NOI18N
        convertALC.put("keyword-directive", "keyword"); //NOI18N
    }
    
    private static Integer defaultFontSize;
    private static Integer getDefaultFontSize () {
        if (defaultFontSize == null) {
            defaultFontSize = (Integer) UIManager.get("customFontSize"); // NOI18N
            if (defaultFontSize == null) {
                int s = UIManager.getFont ("TextField.font").getSize (); // NOI18N
                if (s < 12) s = 12;
                defaultFontSize = new Integer (s);
            }
        }
        return defaultFontSize;
    }
    
    private static final class LanguagesComparator implements Comparator<String> {
        public int compare(String o1, String o2) {
            if (o1.equals(ColorModel.ALL_LANGUAGES))
                return o2.equals(ColorModel.ALL_LANGUAGES) ? 0 : -1;
            if (o2.equals(ColorModel.ALL_LANGUAGES))
                return 1;
            return o1.compareTo(o2);
        }
    }
}

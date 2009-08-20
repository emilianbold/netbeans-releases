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

package org.netbeans.modules.options.indentation;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.options.editor.spi.PreviewProvider;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;


/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
public class IndentationPanel extends JPanel implements ChangeListener, ActionListener, PreferenceChangeListener {

    private static final Logger LOG = Logger.getLogger(IndentationPanel.class.getName());

    private final MimePath mimePath;
    private final CustomizerSelector.PreferencesFactory prefsFactory;
    private final Preferences allLangPrefs;
    private final Preferences prefs;
    private final PreviewProvider preview;
    private final boolean showOverrideGlobalOptions;
    
    /** 
     * Creates new form IndentationPanel.
     */
    public IndentationPanel(MimePath mimePath, CustomizerSelector.PreferencesFactory prefsFactory, Preferences prefs, Preferences allLangPrefs, PreviewProvider preview) {
        this.mimePath = mimePath;
        this.prefsFactory = prefsFactory;
        this.prefs = prefs;
        this.prefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, this, prefs));

        this.allLangPrefs = allLangPrefs;
        if (this.allLangPrefs == null) {
            assert preview == null;
            assert mimePath == MimePath.EMPTY;
            PreviewProvider pp;
            try {
                pp = new TextPreview(prefs, "text/xml", getClass().getClassLoader(), "org/netbeans/modules/options/indentation/indentationExample"); //NOI18N
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, null, ioe);
                pp = new NoPreview();
            }
            this.preview = pp;
            this.showOverrideGlobalOptions = false;
        } else {
            assert preview != null;
            assert mimePath != MimePath.EMPTY;
            this.preview = preview;
            this.showOverrideGlobalOptions = true;
            this.allLangPrefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, this, this.allLangPrefs));
        }
        
        initComponents ();
        cbOverrideGlobalOptions.setVisible(showOverrideGlobalOptions);
        
        // localization
        loc (cbOverrideGlobalOptions, "Override_Global_Options"); //NOI18N
        loc (lNumberOfSpacesPerIndent, "Indent"); //NOI18N
        loc (lTabSize, "TabSize"); //NOI18N
        loc (cbExpandTabsToSpaces, "Expand_Tabs"); //NOI18N
        loc (lRightMargin, "Right_Margin"); //NOI18N
        cbExpandTabsToSpaces.getAccessibleContext ().setAccessibleName (loc ("AN_Expand_Tabs")); //NOI18N
        cbExpandTabsToSpaces.getAccessibleContext ().setAccessibleDescription (loc ("AD_Expand_Tabs")); //NOI18N

        //listeners
        cbOverrideGlobalOptions.addActionListener(this);
        cbExpandTabsToSpaces.addActionListener(this);
        sNumberOfSpacesPerIndent.setModel(new SpinnerNumberModel(4, 1, 50, 1));
        sNumberOfSpacesPerIndent.addChangeListener(this);
        sTabSize.setModel(new SpinnerNumberModel(4, 1, 50, 1));
        sTabSize.addChangeListener(this);
        sRightMargin.setModel(new SpinnerNumberModel(120, 1, 200, 10));
        sRightMargin.addChangeListener(this);

        // initialize controls
        prefsChange(null);
        if (showOverrideGlobalOptions && 
            null == this.prefs.get(FormattingPanelController.OVERRIDE_GLOBAL_FORMATTING_OPTIONS, null))
        {
            // FormattingCustomizerPanel and FormattingPanelController expect this to be set
            this.prefs.putBoolean(FormattingPanelController.OVERRIDE_GLOBAL_FORMATTING_OPTIONS, areBasicOptionsOverriden());
        }
    }

    public PreviewProvider getPreviewProvider() {
        return preview;
    }

    // ------------------------------------------------------------------------
    // ChangeListener implementation
    // ------------------------------------------------------------------------

    public void stateChanged (ChangeEvent e) {
        if (sNumberOfSpacesPerIndent == e.getSource()) {
            prefs.putInt(SimpleValueNames.INDENT_SHIFT_WIDTH, (Integer) sNumberOfSpacesPerIndent.getValue());
            prefs.putInt(SimpleValueNames.SPACES_PER_TAB, (Integer) sNumberOfSpacesPerIndent.getValue());
        } else if (sTabSize == e.getSource()) {
            prefs.putInt(SimpleValueNames.TAB_SIZE, (Integer) sTabSize.getValue());
        } else if (sRightMargin == e.getSource()) {
            prefs.putInt(SimpleValueNames.TEXT_LIMIT_WIDTH, (Integer) sRightMargin.getValue());
        }
    }
    
    // ------------------------------------------------------------------------
    // ActionListener implementation
    // ------------------------------------------------------------------------

    public void actionPerformed (ActionEvent e) {
        if (cbOverrideGlobalOptions == e.getSource()) {
            prefs.putBoolean(FormattingPanelController.OVERRIDE_GLOBAL_FORMATTING_OPTIONS, cbOverrideGlobalOptions.isSelected());
        } else if (cbExpandTabsToSpaces == e.getSource()) {
            prefs.putBoolean(SimpleValueNames.EXPAND_TABS, cbExpandTabsToSpaces.isSelected());
        }
    }

    // ------------------------------------------------------------------------
    // PreferenceChangeListener implementation
    // ------------------------------------------------------------------------

    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getSource() == prefs) {
            prefsChange(evt);
        } else if (evt.getSource() == allLangPrefs) {
            allLangPrefsChange(evt);
        } else {
            assert false;
        }
    }
    
    // ------------------------------------------------------------------------
    // private implementation
    // ------------------------------------------------------------------------

    private void prefsChange(PreferenceChangeEvent evt) {
        String key = evt == null ? null : evt.getKey();
        boolean needsRefresh = false;

//        System.out.println("~~~ prefsChange: key=" + key
//                + (key == null ? "" : " prefs(" + key + ")=" + prefs.get(key, null)
//                + (allLangPrefs == null ? "" : ", allLangPrefs(" + key + ")=" + allLangPrefs.get(key, null)))
//                + "; override=" + prefs.getBoolean(FormattingPanelController.OVERRIDE_GLOBAL_FORMATTING_OPTIONS, areGlobalOptionsOverriden()));

        if (key == null || SimpleValueNames.EXPAND_TABS.equals(key)) {
            boolean value = prefs.getBoolean(SimpleValueNames.EXPAND_TABS, getDefBoolean(SimpleValueNames.EXPAND_TABS, true));
            if (value != cbExpandTabsToSpaces.isSelected()) {
                cbExpandTabsToSpaces.setSelected(value);
            }
            needsRefresh = true;
        }
        
        if (key == null || SimpleValueNames.INDENT_SHIFT_WIDTH.equals(key)) {
            int nue = prefs.getInt(SimpleValueNames.INDENT_SHIFT_WIDTH, getDefInt(SimpleValueNames.INDENT_SHIFT_WIDTH, 4));
            if (nue != (Integer) sNumberOfSpacesPerIndent.getValue()) {
                sNumberOfSpacesPerIndent.setValue(nue);
            }
            needsRefresh = true;
        }
        
        if (key == null || SimpleValueNames.SPACES_PER_TAB.equals(key)) {
            if (prefs.get(SimpleValueNames.INDENT_SHIFT_WIDTH, null) == null) {
                int nue = prefs.getInt(SimpleValueNames.SPACES_PER_TAB, getDefInt(SimpleValueNames.SPACES_PER_TAB, 4));
                if (nue != (Integer) sNumberOfSpacesPerIndent.getValue()) {
                    sNumberOfSpacesPerIndent.setValue(nue);
                }
                needsRefresh = true;
            }
        }
        
        if (key == null || SimpleValueNames.TAB_SIZE.equals(key)) {
            int nue = prefs.getInt(SimpleValueNames.TAB_SIZE, getDefInt(SimpleValueNames.TAB_SIZE, 8));
            if (nue != (Integer) sTabSize.getValue()) {
                sTabSize.setValue(nue);
            }
            needsRefresh = true;
        }

        if (key == null || SimpleValueNames.TEXT_LIMIT_WIDTH.equals(key)) {
            int nue = prefs.getInt(SimpleValueNames.TEXT_LIMIT_WIDTH, getDefInt(SimpleValueNames.TEXT_LIMIT_WIDTH, 80));
            if (nue != (Integer) sRightMargin.getValue()) {
                sRightMargin.setValue(nue);
            }
            needsRefresh = true;
        }

        if (showOverrideGlobalOptions) {
            if (key == null || FormattingPanelController.OVERRIDE_GLOBAL_FORMATTING_OPTIONS.equals(key)) {
                boolean nue = prefs.getBoolean(FormattingPanelController.OVERRIDE_GLOBAL_FORMATTING_OPTIONS, areBasicOptionsOverriden());
                if (nue != cbOverrideGlobalOptions.isSelected()) {
                    cbOverrideGlobalOptions.setSelected(nue);
                }
                
                if (!nue) {
                    prefs.putBoolean(SimpleValueNames.EXPAND_TABS, allLangPrefs.getBoolean(SimpleValueNames.EXPAND_TABS, true));
                    prefs.putInt(SimpleValueNames.INDENT_SHIFT_WIDTH, allLangPrefs.getInt(SimpleValueNames.INDENT_SHIFT_WIDTH, 4));
                    prefs.putInt(SimpleValueNames.SPACES_PER_TAB, allLangPrefs.getInt(SimpleValueNames.SPACES_PER_TAB, 4));
                    prefs.putInt(SimpleValueNames.TAB_SIZE, allLangPrefs.getInt(SimpleValueNames.TAB_SIZE, 4));
                    prefs.putInt(SimpleValueNames.TEXT_LIMIT_WIDTH, allLangPrefs.getInt(SimpleValueNames.TEXT_LIMIT_WIDTH, 80));
                }
                
                needsRefresh = true;
                ((ControlledCheckBox) cbExpandTabsToSpaces).setEnabledInternal(nue);
                ((ControlledLabel) lNumberOfSpacesPerIndent).setEnabledInternal(nue);
                ((ControlledSpinner) sNumberOfSpacesPerIndent).setEnabledInternal(nue);
                ((ControlledLabel) lTabSize).setEnabledInternal(nue);
                ((ControlledSpinner) sTabSize).setEnabledInternal(nue);
                ((ControlledLabel) lRightMargin).setEnabledInternal(nue);
                ((ControlledSpinner) sRightMargin).setEnabledInternal(nue);
            }
        }

        if (needsRefresh) {
            preview.refreshPreview();
        }
    }

    // just copy the values over to prefs
    private void allLangPrefsChange(PreferenceChangeEvent evt) {
        String key = evt == null ? null : evt.getKey();

//        System.out.println("~~~ allLangPrefsChange: key=" + key
//                + (key == null ? "" : " prefs(" + key + ")=" + prefs.get(key, null)
//                + ", allLangPrefs(" + key + ")=" + allLangPrefs.get(key, null))
//                + "; override=" + prefs.getBoolean(FormattingPanelController.OVERRIDE_GLOBAL_FORMATTING_OPTIONS, areGlobalOptionsOverriden()));
        
        if (prefs.getBoolean(FormattingPanelController.OVERRIDE_GLOBAL_FORMATTING_OPTIONS, areBasicOptionsOverriden())) {
            // ignore allLangPrefs changes when we are actually overriding the all languages values
            return;
        }

        if (key == null || SimpleValueNames.EXPAND_TABS.equals(key)) {
            prefs.putBoolean(SimpleValueNames.EXPAND_TABS, allLangPrefs.getBoolean(SimpleValueNames.EXPAND_TABS, true));
        }
        
        if (key == null || SimpleValueNames.INDENT_SHIFT_WIDTH.equals(key)) {
            prefs.putInt(SimpleValueNames.INDENT_SHIFT_WIDTH, allLangPrefs.getInt(SimpleValueNames.INDENT_SHIFT_WIDTH, 4));
        }
        
        if (key == null || SimpleValueNames.SPACES_PER_TAB.equals(key)) {
            prefs.putInt(SimpleValueNames.SPACES_PER_TAB, allLangPrefs.getInt(SimpleValueNames.SPACES_PER_TAB, 4));
        }
        
        if (key == null || SimpleValueNames.TAB_SIZE.equals(key)) {
            prefs.putInt(SimpleValueNames.TAB_SIZE, allLangPrefs.getInt(SimpleValueNames.TAB_SIZE, 4));
        }

        if (key == null || SimpleValueNames.TEXT_LIMIT_WIDTH.equals(key)) {
            prefs.putInt(SimpleValueNames.TEXT_LIMIT_WIDTH, allLangPrefs.getInt(SimpleValueNames.TEXT_LIMIT_WIDTH, 80));
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cbOverrideGlobalOptions = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        cbExpandTabsToSpaces = new ControlledCheckBox();
        lNumberOfSpacesPerIndent = new ControlledLabel();
        sNumberOfSpacesPerIndent = new ControlledSpinner();
        lTabSize = new ControlledLabel();
        sTabSize = new ControlledSpinner();
        lRightMargin = new ControlledLabel();
        sRightMargin = new ControlledSpinner();

        setOpaque(false);

        org.openide.awt.Mnemonics.setLocalizedText(cbOverrideGlobalOptions, org.openide.util.NbBundle.getMessage(IndentationPanel.class, "CTL_Override_Global_Options")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbExpandTabsToSpaces, org.openide.util.NbBundle.getMessage(IndentationPanel.class, "CTL_Expand_Tabs")); // NOI18N

        lNumberOfSpacesPerIndent.setLabelFor(sNumberOfSpacesPerIndent);
        org.openide.awt.Mnemonics.setLocalizedText(lNumberOfSpacesPerIndent, org.openide.util.NbBundle.getMessage(IndentationPanel.class, "CTL_Indent")); // NOI18N

        lTabSize.setLabelFor(sTabSize);
        org.openide.awt.Mnemonics.setLocalizedText(lTabSize, org.openide.util.NbBundle.getMessage(IndentationPanel.class, "CTL_TabSize")); // NOI18N

        lRightMargin.setLabelFor(sRightMargin);
        org.openide.awt.Mnemonics.setLocalizedText(lRightMargin, org.openide.util.NbBundle.getMessage(IndentationPanel.class, "CTL_Right_Margin")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(cbExpandTabsToSpaces, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                        .add(54, 54, 54))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lNumberOfSpacesPerIndent, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                            .add(lRightMargin, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, lTabSize, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, sNumberOfSpacesPerIndent, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
                            .add(sTabSize, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
                            .add(sRightMargin, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)))))
        );

        jPanel1Layout.linkSize(new java.awt.Component[] {sNumberOfSpacesPerIndent, sRightMargin, sTabSize}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(cbExpandTabsToSpaces, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(63, 63, 63))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(25, Short.MAX_VALUE)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(sNumberOfSpacesPerIndent, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lNumberOfSpacesPerIndent))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(sTabSize, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lTabSize))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(sRightMargin, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lRightMargin)))
        );

        jPanel1Layout.linkSize(new java.awt.Component[] {sNumberOfSpacesPerIndent, sRightMargin, sTabSize}, org.jdesktop.layout.GroupLayout.VERTICAL);

        cbExpandTabsToSpaces.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IndentationPanel.class, "AD_Expand_Tabs")); // NOI18N
        lNumberOfSpacesPerIndent.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(IndentationPanel.class, "AN_Indent")); // NOI18N
        lNumberOfSpacesPerIndent.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IndentationPanel.class, "AD_Indent")); // NOI18N
        sNumberOfSpacesPerIndent.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(IndentationPanel.class, "AN_sNumberOfSpacesPerIndent")); // NOI18N
        sNumberOfSpacesPerIndent.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IndentationPanel.class, "AD_sNumberOfSpacesPerIndent")); // NOI18N
        lTabSize.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(IndentationPanel.class, "AN_TabSize")); // NOI18N
        lTabSize.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IndentationPanel.class, "AD_TabSize")); // NOI18N
        sTabSize.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(IndentationPanel.class, "AN_sTabSize")); // NOI18N
        sTabSize.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IndentationPanel.class, "AD_sTabSize")); // NOI18N
        lRightMargin.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(IndentationPanel.class, "AN_Right_Margin")); // NOI18N
        lRightMargin.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IndentationPanel.class, "AD_Right_Margin")); // NOI18N
        sRightMargin.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(IndentationPanel.class, "AN_sRightMargin")); // NOI18N
        sRightMargin.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IndentationPanel.class, "AD_sRightMargin")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(cbOverrideGlobalOptions, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 239, Short.MAX_VALUE)
                .add(66, 66, 66))
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(cbOverrideGlobalOptions)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        cbOverrideGlobalOptions.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(IndentationPanel.class, "AN_Override_Global_Options")); // NOI18N
        cbOverrideGlobalOptions.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IndentationPanel.class, "AD_Override_Global_Options")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbExpandTabsToSpaces;
    private javax.swing.JCheckBox cbOverrideGlobalOptions;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lNumberOfSpacesPerIndent;
    private javax.swing.JLabel lRightMargin;
    private javax.swing.JLabel lTabSize;
    private javax.swing.JSpinner sNumberOfSpacesPerIndent;
    private javax.swing.JSpinner sRightMargin;
    private javax.swing.JSpinner sTabSize;
    // End of variables declaration//GEN-END:variables
    
    
    private static String loc (String key) {
        return NbBundle.getMessage (IndentationPanel.class, key);
    }
    
    private static void loc (Component c, String key) {
        if (!(c instanceof JLabel)) {
            c.getAccessibleContext ().setAccessibleName (loc ("AN_" + key)); //NOI18N
            c.getAccessibleContext ().setAccessibleDescription (loc ("AD_" + key)); //NOI18N
        }
        if (c instanceof AbstractButton) {
            Mnemonics.setLocalizedText ((AbstractButton) c, loc ("CTL_" + key)); //NOI18N
        } else {
            Mnemonics.setLocalizedText ((JLabel) c, loc ("CTL_" + key)); //NOI18N
        }
    }

    private boolean areBasicOptionsOverriden() {
        String mimeType = mimePath.getPath();
        return prefsFactory.isKeyOverridenForMimeType(SimpleValueNames.EXPAND_TABS, mimeType) ||
            prefsFactory.isKeyOverridenForMimeType(SimpleValueNames.INDENT_SHIFT_WIDTH, mimeType) ||
            prefsFactory.isKeyOverridenForMimeType(SimpleValueNames.SPACES_PER_TAB, mimeType) ||
            prefsFactory.isKeyOverridenForMimeType(SimpleValueNames.TAB_SIZE, mimeType) ||
            prefsFactory.isKeyOverridenForMimeType(SimpleValueNames.TEXT_LIMIT_WIDTH, mimeType);
    }

    private boolean getDefBoolean(String key, boolean def) {
        return allLangPrefs != null ? allLangPrefs.getBoolean(key, def) : def;
    }

    private int getDefInt(String key, int def) {
        return allLangPrefs != null ? allLangPrefs.getInt(key, def) : def;
    }

    public static final class TextPreview implements PreviewProvider {

        private final Preferences prefs;
        private final String mimeType;
        private final String previewText;
        
        private JEditorPane jep;

        public TextPreview(Preferences prefs, FileObject previewFile) throws IOException {
            this(prefs, previewFile.getMIMEType(), loadPreviewText(previewFile.getInputStream()));
        }

        public TextPreview(Preferences prefs, String mimeType, FileObject previewFile) throws IOException {
            this(prefs, mimeType, loadPreviewText(previewFile.getInputStream()));
        }

        public TextPreview(Preferences prefs, String mimeType, String previewText) {
            this.prefs = prefs;
            this.mimeType = mimeType;
            this.previewText = previewText;
        }

        public TextPreview(Preferences prefs, String mimeType, ClassLoader loader, String resourceName) throws IOException {
            this(prefs, mimeType, loadPreviewText(loader.getResourceAsStream(resourceName)));
        }

        public TextPreview(Preferences prefs, String mimeType, Class clazz, String bundleKey) {
            this(prefs, mimeType, NbBundle.getMessage(clazz, bundleKey));
        }

        public JComponent getPreviewComponent() {
            if (jep == null) {
                jep = new JEditorPane();
                jep.getAccessibleContext().setAccessibleName(NbBundle.getMessage(IndentationPanel.class, "AN_Preview")); //NOI18N
                jep.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(IndentationPanel.class, "AD_Preview")); //NOI18N
                jep.putClientProperty("HighlightsLayerIncludes", "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.SyntaxHighlighting$"); //NOI18N
                jep.setEditorKit(CloneableEditorSupport.getEditorKit(mimeType)); //NOI18N
                jep.setEditable(false);
            }
            return jep;
        }

        public void refreshPreview() {
            JEditorPane pane = (JEditorPane) getPreviewComponent();
            pane.setText(previewText);
            
            final Document doc = pane.getDocument();
            if (doc instanceof BaseDocument) {
                final Reformat reformat = Reformat.get(doc);
                reformat.lock();
                try {
                    ((BaseDocument) doc).runAtomic(new Runnable() {
                        public void run() {
                            if (LOG.isLoggable(Level.FINE)) {
                                LOG.fine("Refreshing preview: expandTabs=" + IndentUtils.isExpandTabs(doc) //NOI18N
                                        + ", indentLevelSize=" + IndentUtils.indentLevelSize(doc) //NOI18N
                                        + ", tabSize=" + IndentUtils.tabSize(doc) //NOI18N
                                        + ", mimeType='" + doc.getProperty("mimeType") + "'" //NOI18N
                                        + ", doc=" + s2s(doc)); //NOI18N
                            }

                            try {
                                reformat.reformat(0, doc.getLength());
                            } catch (BadLocationException ble) {
                                LOG.log(Level.WARNING, null, ble);
                            }
                        }
                    });
                } finally {
                    reformat.unlock();
                }
            } else {
                LOG.warning("Can't format " + doc + "; it's not BaseDocument."); //NOI18N
            }
        }

        private static String loadPreviewText(InputStream is) throws IOException {
            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            try {
                StringBuilder sb = new StringBuilder();
                for (String line = r.readLine(); line != null; line = r.readLine()) {
                    sb.append(line).append('\n'); //NOI18N
                }
                return sb.toString();
            } finally {
                r.close();
            }
        }
    } // End of IndentationPreview class

    public static final class NoPreview implements PreviewProvider {
        private JComponent component = null;

        public JComponent getPreviewComponent() {
            if (component == null) {
                JLabel noPreviewLabel = new JLabel(NbBundle.getMessage(IndentationPanel.class, "MSG_no_preview_available")); //NOI18N
                noPreviewLabel.setOpaque(true);
                noPreviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
                noPreviewLabel.setBorder(new EmptyBorder(new Insets(11, 11, 11, 11)));
                noPreviewLabel.setVisible(true);
                component = new JPanel(new BorderLayout());
                component.add(noPreviewLabel, BorderLayout.CENTER);
            }
            return component;
        }

        public void refreshPreview() {
            // noop
        }
    } // End of NoPreview class

    private static String s2s(Object o) {
        return o == null ? "null" : o.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(o));
    }

    private static final class ControlledCheckBox extends JCheckBox {

        private boolean externallyEnabled = true;
        private boolean internallyEnabled = true;
        
        public @Override void setEnabled(boolean b) {
            if (externallyEnabled == b) {
                return;
            } else {
                externallyEnabled = b;
                if (externallyEnabled) {
                    if (internallyEnabled) {
                        super.setEnabled(true);
                    }
                } else {
                    super.setEnabled(false);
                }
            }
        }

        public void setEnabledInternal(boolean b) {
            if (internallyEnabled == b) {
                return;
            } else {
                internallyEnabled = b;
                if (internallyEnabled) {
                    if (externallyEnabled) {
                        super.setEnabled(true);
                    }
                } else {
                    super.setEnabled(false);
                }
            }
        }
    } // End of ControlledCheckBox class

    private static final class ControlledLabel extends JLabel {

        private boolean externallyEnabled = true;
        private boolean internallyEnabled = true;
        
        public @Override void setEnabled(boolean b) {
            if (externallyEnabled == b) {
                return;
            } else {
                externallyEnabled = b;
                if (externallyEnabled) {
                    if (internallyEnabled) {
                        super.setEnabled(true);
                    }
                } else {
                    super.setEnabled(false);
                }
            }
        }

        public void setEnabledInternal(boolean b) {
            if (internallyEnabled == b) {
                return;
            } else {
                internallyEnabled = b;
                if (internallyEnabled) {
                    if (externallyEnabled) {
                        super.setEnabled(true);
                    }
                } else {
                    super.setEnabled(false);
                }
            }
        }
    } // End of ControlledLabel class
    
    private static final class ControlledSpinner extends JSpinner {

        private boolean externallyEnabled = true;
        private boolean internallyEnabled = true;
        
        public @Override void setEnabled(boolean b) {
            if (externallyEnabled == b) {
                return;
            } else {
                externallyEnabled = b;
                if (externallyEnabled) {
                    if (internallyEnabled) {
                        super.setEnabled(true);
                    }
                } else {
                    super.setEnabled(false);
                }
            }
        }

        public void setEnabledInternal(boolean b) {
            if (internallyEnabled == b) {
                return;
            } else {
                internallyEnabled = b;
                if (internallyEnabled) {
                    if (externallyEnabled) {
                        super.setEnabled(true);
                    }
                } else {
                    super.setEnabled(false);
                }
            }
        }
    } // End of ControlledSpinner class

}


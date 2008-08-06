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

import java.awt.Component;
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
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.options.editor.spi.PreviewProvider;
import org.openide.awt.Mnemonics;
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
    
    private final Preferences prefs;
    private final PreviewProvider preview;
    private final boolean showOverrideGlobalOptions;
    
    /** 
     * Creates new form IndentationPanel.
     */
    public IndentationPanel(Preferences prefs, PreviewProvider preview) {
        this.prefs = prefs;
        this.prefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, this, prefs));

        if (preview == null) {
            this.preview = new IndentationPreview(prefs);
            this.showOverrideGlobalOptions = false;
        } else {
            this.preview = preview;
            this.showOverrideGlobalOptions = true;
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
        preferenceChange(null);
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
            prefs.putBoolean(SimpleValueNames.OVERRIDE_GLOBAL_FORMATTING_OPTIONS, cbOverrideGlobalOptions.isSelected());
        } else if (cbExpandTabsToSpaces == e.getSource()) {
            prefs.putBoolean(SimpleValueNames.EXPAND_TABS, cbExpandTabsToSpaces.isSelected());
        }
    }

    // ------------------------------------------------------------------------
    // PreferenceChangeListener implementation
    // ------------------------------------------------------------------------

    public void preferenceChange(PreferenceChangeEvent evt) {
        String key = evt == null ? null : evt.getKey();
        boolean needsRefresh = false;

        if (key == null || SimpleValueNames.EXPAND_TABS.equals(key)) {
            boolean value = prefs.getBoolean(SimpleValueNames.EXPAND_TABS, true);
            if (value != cbExpandTabsToSpaces.isSelected()) {
                cbExpandTabsToSpaces.setSelected(value);
            }
            needsRefresh = true;
        }
        
        if (key == null || SimpleValueNames.INDENT_SHIFT_WIDTH.equals(key)) {
            int nue = prefs.getInt(SimpleValueNames.INDENT_SHIFT_WIDTH, 4);
            if (nue != (Integer) sNumberOfSpacesPerIndent.getValue()) {
                sNumberOfSpacesPerIndent.setValue(nue);
            }
            needsRefresh = true;
        }
        
        if (key == null || SimpleValueNames.SPACES_PER_TAB.equals(key)) {
            int nue = prefs.getInt(SimpleValueNames.SPACES_PER_TAB, 4);
            if (nue != (Integer) sNumberOfSpacesPerIndent.getValue()) {
                sNumberOfSpacesPerIndent.setValue(nue);
            }
            needsRefresh = true;
        }
        
        if (key == null || SimpleValueNames.TAB_SIZE.equals(key)) {
            int nue = prefs.getInt(SimpleValueNames.TAB_SIZE, 8);
            if (nue != (Integer) sTabSize.getValue()) {
                sTabSize.setValue(nue);
            }
            needsRefresh = true;
        }

        if (key == null || SimpleValueNames.TEXT_LIMIT_WIDTH.equals(key)) {
            int nue = prefs.getInt(SimpleValueNames.TEXT_LIMIT_WIDTH, 80);
            if (nue != (Integer) sRightMargin.getValue()) {
                sRightMargin.setValue(nue);
            }
            needsRefresh = true;
        }

        if (showOverrideGlobalOptions) {
            if (key == null || SimpleValueNames.OVERRIDE_GLOBAL_FORMATTING_OPTIONS.equals(key)) {
                boolean nue = prefs.getBoolean(SimpleValueNames.OVERRIDE_GLOBAL_FORMATTING_OPTIONS, false);
                if (nue != cbOverrideGlobalOptions.isSelected()) {
                    cbOverrideGlobalOptions.setSelected(nue);
                }
                needsRefresh = true;
                cbExpandTabsToSpaces.setEnabled(nue);
                lNumberOfSpacesPerIndent.setEnabled(nue);
                sNumberOfSpacesPerIndent.setEnabled(nue);
                lTabSize.setEnabled(nue);
                sTabSize.setEnabled(nue);
                lRightMargin.setEnabled(nue);
                sRightMargin.setEnabled(nue);
            }
        }
        
        if (needsRefresh) {
            preview.refreshPreview();
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
        cbExpandTabsToSpaces = new javax.swing.JCheckBox();
        lNumberOfSpacesPerIndent = new javax.swing.JLabel();
        sNumberOfSpacesPerIndent = new javax.swing.JSpinner();
        lTabSize = new javax.swing.JLabel();
        sTabSize = new javax.swing.JSpinner();
        lRightMargin = new javax.swing.JLabel();
        sRightMargin = new javax.swing.JSpinner();

        cbOverrideGlobalOptions.setText("Override Global Options");
        cbOverrideGlobalOptions.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        cbExpandTabsToSpaces.setText("Expand Tabs to Spaces");
        cbExpandTabsToSpaces.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbExpandTabsToSpaces.setMargin(new java.awt.Insets(0, 0, 0, 0));

        lNumberOfSpacesPerIndent.setLabelFor(sNumberOfSpacesPerIndent);
        lNumberOfSpacesPerIndent.setText("Number of Spaces per Indent:");

        lTabSize.setLabelFor(sTabSize);
        lTabSize.setText("Tab Size:");

        lRightMargin.setLabelFor(sRightMargin);
        lRightMargin.setText("Right Margin:");

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(lNumberOfSpacesPerIndent, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE))
                    .add(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(lTabSize, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE))
                    .add(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(lRightMargin, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 195, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(sRightMargin, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
                        .addContainerGap())
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(sTabSize, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
                        .addContainerGap())
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                        .add(sNumberOfSpacesPerIndent, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
                        .addContainerGap())))
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(cbExpandTabsToSpaces, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)
                .add(54, 54, 54))
        );

        jPanel1Layout.linkSize(new java.awt.Component[] {sNumberOfSpacesPerIndent, sRightMargin, sTabSize}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(cbExpandTabsToSpaces, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lNumberOfSpacesPerIndent)
                    .add(sNumberOfSpacesPerIndent, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lTabSize)
                    .add(sTabSize, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(6, 6, 6)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lRightMargin)
                    .add(sRightMargin, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1Layout.linkSize(new java.awt.Component[] {sNumberOfSpacesPerIndent, sRightMargin, sTabSize}, org.jdesktop.layout.GroupLayout.VERTICAL);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(cbOverrideGlobalOptions, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE)
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

    private static class IndentationPreview implements PreviewProvider {

        private final Preferences prefs;
        private JEditorPane jep;
        private String previewText;

        public IndentationPreview(Preferences prefs) {
            this.prefs = prefs;
        }

        public JComponent getPreviewComponent() {
            if (jep == null) {
                jep = new JEditorPane();
                jep.getAccessibleContext().setAccessibleName(NbBundle.getMessage(IndentationPanel.class, "AN_Preview")); //NOI18N
                jep.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(IndentationPanel.class, "AD_Preview")); //NOI18N
                jep.putClientProperty("HighlightsLayerIncludes", "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.SyntaxHighlighting$"); //NOI18N
                jep.setEditorKit(CloneableEditorSupport.getEditorKit("text/xml")); //NOI18N
                jep.setEditable(false);
            }
            return jep;
        }

        public void refreshPreview() {
            if (previewText == null) {
                // add text to preview
                try {
                    InputStream is = getClass ().getResourceAsStream("/org/netbeans/modules/options/indentation/indentationExample"); //NOI18N
                    BufferedReader r = new BufferedReader (new InputStreamReader (is));
                    try {
                        StringBuilder sb = new StringBuilder();
                        for (String line = r.readLine (); line != null; line = r.readLine()) {
                            sb.append (line).append ('\n'); //NOI18N
                        }
                        previewText = sb.toString();
                    } finally {
                        r.close();
                    }
                } catch (IOException ioe) {
                    LOG.log(Level.WARNING, null, ioe);
                }
            }

            JEditorPane pane = (JEditorPane) getPreviewComponent();
            pane.setText(previewText);
            
            BaseDocument doc = (BaseDocument) pane.getDocument();
            // This is here solely for the purpose of previewing changes in formatting settings
            // in Tools-Options. This is NOT, repeat NOT, to be used by anybody else!
            // The name of this property is also hardcoded in editor.indent/.../CodeStylePreferences.java
            doc.putProperty("Tools-Options->Editor->Formatting->Preview - Preferences", prefs); //NOI18N
            
            Reformat reformat = Reformat.get(doc);
            reformat.lock();
            try {
                doc.atomicLock();
                try {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Refreshing preview: expandTabs=" + IndentUtils.isExpandTabs(doc) //NOI18N
                                + ", indentLevelSize=" + IndentUtils.indentLevelSize(doc) //NOI18N
                                + ", tabSize=" + IndentUtils.tabSize(doc) //NOI18N
                                + ", mimeType='" + doc.getProperty("mimeType") + "'" //NOI18N
                                + ", doc=" + doc); //NOI18N
                    }
                    reformat.reformat(0, doc.getLength());
                } catch (BadLocationException ble) {
                    LOG.log(Level.WARNING, null, ble);
                } finally {
                    doc.atomicUnlock();
                }
            } finally {
                reformat.unlock();
            }
            
        }

    } // End of IndentationPreview class
}

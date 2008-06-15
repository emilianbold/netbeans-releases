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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.awt.Mnemonics;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;


/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
public class IndentationPanel extends JPanel implements ChangeListener, 
ActionListener, PreferenceChangeListener {

    private static final Logger LOG = Logger.getLogger(IndentationPanel.class.getName());
    
    private IndentationModel    model;
    private String              originalText;
    private boolean             listen = false;
    private boolean             changed = false;
    private Preferences prefs = null;
    
    /** 
     * Creates new form IndentationPanel.
     */
    public IndentationPanel () {
        initComponents ();
        
        // localization
        setName(loc ("Indentation_Tab")); //NOI18N
        loc (lNumberOfSpacesPerIndent, "Indent"); //NOI18N
        loc (lTabSize, "TabSize"); //NOI18N
        loc (lPreview, "Preview"); //NOI18N
        loc (lExpandTabsToSpaces, "Expand_Tabs"); //NOI18N
        loc (lRightMargin, "Right_Margin"); //NOI18N
        cbExpandTabsToSpaces.getAccessibleContext ().setAccessibleName (loc ("AN_Expand_Tabs")); //NOI18N
        cbExpandTabsToSpaces.getAccessibleContext ().setAccessibleDescription (loc ("AD_Expand_Tabs")); //NOI18N
        epPreview.getAccessibleContext ().setAccessibleName (loc ("AN_Preview")); //NOI18N
        epPreview.getAccessibleContext ().setAccessibleDescription (loc ("AD_Preview")); //NOI18N

        //listeners
        epPreview.setBorder (new EtchedBorder ());
        cbExpandTabsToSpaces.addActionListener (this);
        sNumberOfSpacesPerIndent.setModel (new SpinnerNumberModel (4, 1, 50, 1));
        sNumberOfSpacesPerIndent.addChangeListener (this);
        sTabSize.setModel (new SpinnerNumberModel (4, 1, 50, 1));
        sTabSize.addChangeListener (this);
        sRightMargin.setModel (new SpinnerNumberModel (120, 1, 200, 10));
        sRightMargin.addChangeListener (this);
        epPreview.setEnabled (false);
        epPreview.putClientProperty("HighlightsLayerIncludes", "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.SyntaxHighlighting$"); //NOI18N
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        lNumberOfSpacesPerIndent = new javax.swing.JLabel();
        sNumberOfSpacesPerIndent = new javax.swing.JSpinner();
        cbExpandTabsToSpaces = new javax.swing.JCheckBox();
        lPreview = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        epPreview = new javax.swing.JEditorPane();
        lTabSize = new javax.swing.JLabel();
        sTabSize = new javax.swing.JSpinner();
        lExpandTabsToSpaces = new javax.swing.JLabel();
        lRightMargin = new javax.swing.JLabel();
        sRightMargin = new javax.swing.JSpinner();

        lNumberOfSpacesPerIndent.setLabelFor(sNumberOfSpacesPerIndent);
        lNumberOfSpacesPerIndent.setText("Number of Spaces per Indent:");

        cbExpandTabsToSpaces.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbExpandTabsToSpaces.setMargin(new java.awt.Insets(0, 0, 0, 0));

        lPreview.setText("Preview:");

        jScrollPane1.setViewportView(epPreview);

        lTabSize.setLabelFor(sTabSize);
        lTabSize.setText("Tab Size:");

        lExpandTabsToSpaces.setLabelFor(cbExpandTabsToSpaces);
        lExpandTabsToSpaces.setText("Expand Tabs To Spaces:");

        lRightMargin.setLabelFor(sRightMargin);
        lRightMargin.setText("Right Margin:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 763, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(lPreview)
                .addContainerGap())
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(lNumberOfSpacesPerIndent, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
                                    .add(lTabSize, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                            .add(layout.createSequentialGroup()
                                .add(lRightMargin)
                                .add(140, 140, 140)))
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(sRightMargin)
                            .add(sTabSize)
                            .add(sNumberOfSpacesPerIndent, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE))
                        .add(204, 204, 204))
                    .add(layout.createSequentialGroup()
                        .add(cbExpandTabsToSpaces)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(lExpandTabsToSpaces)))
                .add(294, 294, 294))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(14, 14, 14)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cbExpandTabsToSpaces)
                    .add(lExpandTabsToSpaces))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lNumberOfSpacesPerIndent)
                    .add(sNumberOfSpacesPerIndent, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lTabSize)
                    .add(sTabSize, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(6, 6, 6)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lRightMargin)
                    .add(sRightMargin, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(lPreview)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbExpandTabsToSpaces;
    private javax.swing.JEditorPane epPreview;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lExpandTabsToSpaces;
    private javax.swing.JLabel lNumberOfSpacesPerIndent;
    private javax.swing.JLabel lPreview;
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

    private void updateModel () {
        model.setExpandTabs (cbExpandTabsToSpaces.isSelected ());
        model.setSpacesPerTab (
            (Integer) sNumberOfSpacesPerIndent.getValue ()
        );
        model.setTabSize(
            (Integer) sTabSize.getValue ()
        );
        model.setRightMargin(
            (Integer) sRightMargin.getValue ()
        );
    }
    
    private void refreshPreview() {
        // start formatter
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                epPreview.setText(originalText);
		BaseDocument doc = (BaseDocument) epPreview.getDocument();
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
		    } finally {
			doc.atomicUnlock();
		    }
		} catch (BadLocationException ex) {
		    Exceptions.printStackTrace(ex);
		} finally {
		    reformat.unlock();
		}
            }
        });
    }
    
    
    // ActionListener ..........................................................
    
    public void stateChanged (ChangeEvent e) {
        if (!listen) return;
        updateModel ();
        if (changed != model.isChanged ()) {
            firePropertyChange (
                OptionsPanelController.PROP_CHANGED,
                Boolean.valueOf (changed),
                Boolean.valueOf (model.isChanged ())
            );
        }
        changed = model.isChanged ();
    }
    
    public void actionPerformed (ActionEvent e) {
        if (!listen) return;
        updateModel ();
        if (changed != model.isChanged ()) {
            firePropertyChange (
                OptionsPanelController.PROP_CHANGED,
                Boolean.valueOf (changed),
                Boolean.valueOf (model.isChanged ())
            );
        }
        changed = model.isChanged ();
    }

    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey() == null || SimpleValueNames.EXPAND_TABS.equals(evt.getKey())
                || SimpleValueNames.INDENT_SHIFT_WIDTH.equals(evt.getKey())
                || SimpleValueNames.SPACES_PER_TAB.equals(evt.getKey())
                || SimpleValueNames.TAB_SIZE.equals(evt.getKey()))
        {
            // some of the formatting settings has changed, reformat the preview
            refreshPreview();
        }
    }

    public void update () {
        model = new IndentationModel ();

        if (prefs == null) {
            prefs = MimeLookup.getLookup(MIME_TYPE).lookup(Preferences.class);
            prefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, this, prefs));
        }
        
        if (originalText == null) {
            // add text to preview
            try {
                InputStream is = getClass ().getResourceAsStream("/org/netbeans/modules/options/indentation/indentationExample"); //NOI18N
                BufferedReader r = new BufferedReader (new InputStreamReader (is));
                try {
                    StringBuffer sb = new StringBuffer ();
                    String line = r.readLine ();
                    while (line != null) {
                        sb.append (line).append ('\n'); //NOI18N
                        line = r.readLine ();
                    }
                        originalText = new String (sb);
                } finally {
                    r.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace ();
            }
        }
        
        // init components
        listen = false;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                epPreview.setEditorKit(CloneableEditorSupport.getEditorKit(MIME_TYPE)); //NOI18N
                epPreview.setText(originalText);
                cbExpandTabsToSpaces.setSelected(model.isExpandTabs());
                sNumberOfSpacesPerIndent.setValue(model.getSpacesPerTab());
                sTabSize.setValue(model.getTabSize());
                sRightMargin.setValue(model.getRightMargin());
                listen = true;

                // update preview
                refreshPreview();
            }
        });
    }
    
    public void applyChanges () {
        if (model != null) {
            model.applyChanges ();
        }
    }
    
    public void cancel () {
        if (model != null) {
            model.revertChanges ();
        }
    }
    
    public boolean dataValid () {
        return true;
    }
    
    public boolean isChanged () {
        if (model == null) {
            return false;
        } else {
            return model.isChanged ();
        }
    }
    
    private static final String MIME_TYPE = "text/xml";
}

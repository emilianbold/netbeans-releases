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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.editor.options;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.editor.reformat.Reformatter;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author  as204739
 */
public class EditorPropertySheet extends javax.swing.JPanel implements ActionListener, PropertyChangeListener, PreferenceChangeListener {
    private EditorOptionsPanelController topControler;
    private boolean loaded = false;
    private CodeStyle.Language currentLanguage;
    private String lastChangedproperty;
    private Map<CodeStyle.Language, String> defaultStyles = new HashMap<CodeStyle.Language, String>();
    private Map<CodeStyle.Language, Map<String,Preferences>> allPreferences = new HashMap<CodeStyle.Language, Map<String, Preferences>>();


    EditorPropertySheet(EditorOptionsPanelController topControler) {
        this.topControler = topControler;
        initComponents();
        setName("Tab_Name"); // NOI18N (used as a bundle key)
        if( "Windows".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
            setOpaque( false );
        }
        previewPane.setContentType("text/x-c++"); // NOI18N
        // Don't highlight caret row 
        previewPane.putClientProperty(
            "HighlightsLayerExcludes", // NOI18N
            "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.CaretRowHighlighting$" // NOI18N
        );
        previewPane.setText("1234567890123456789012345678901234567890"); // NOI18N
        previewPane.setDoubleBuffered(true);
        initLanguages();
        initLanguageCategory();
    }
    
    private void initLanguageStylePreferences(CodeStyle.Language language, String style){
        Map<String, Preferences> map = allPreferences.get(language);
        if (map == null){
            map = new TreeMap<String, Preferences>();
            allPreferences.put(language, map);
        }
        Preferences clone = new PreviewPreferences(EditorOptions.getPreferences(language, style));
        clone.addPreferenceChangeListener(this);
        map.put(style, clone);
    }
    
    
    private void initLanguages(){
        DefaultComboBoxModel model = new DefaultComboBoxModel();

        model.addElement(CodeStyle.Language.C);
        initLanguageStylePreferences(CodeStyle.Language.C, EditorOptions.DEFAULT_PROFILE);
        initLanguageStylePreferences(CodeStyle.Language.C, EditorOptions.APACHE_PROFILE);
        defaultStyles.put(CodeStyle.Language.C, EditorOptions.getCurrentProfileId(CodeStyle.Language.C));
        
        model.addElement(CodeStyle.Language.CPP);
        initLanguageStylePreferences(CodeStyle.Language.CPP, EditorOptions.DEFAULT_PROFILE);
        initLanguageStylePreferences(CodeStyle.Language.CPP, EditorOptions.APACHE_PROFILE);
        defaultStyles.put(CodeStyle.Language.CPP, EditorOptions.getCurrentProfileId(CodeStyle.Language.CPP));
        
        languagesComboBox.setModel(model);
        currentLanguage = CodeStyle.Language.C;
        languagesComboBox.setSelectedIndex(0);
        languagesComboBox.addActionListener(this);
    }

    private void initLanguageCategory(){
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        Map<String, Preferences> map = allPreferences.get(currentLanguage);
        String currentProfile = defaultStyles.get(currentLanguage);
        int index = 0;
        int i = 0;
        for(Map.Entry<String, Preferences> entry : map.entrySet()) {
            if (entry.getKey().equals(currentProfile)) {
                index = i;
            }
            model.addElement(new EntryWrapper(entry));
            i++;
        }
        styleComboBox.setModel(model);
        styleComboBox.setSelectedIndex(index);
        actionPerformed(new ActionEvent(styleComboBox, 0, null));
        EntryWrapper entry = (EntryWrapper)styleComboBox.getSelectedItem();
        initSheets(entry.preferences);
        styleComboBox.addActionListener(this);
    }
    
    private void initSheets(Preferences preferences){
	Sheet sheet = new Sheet();
	Sheet.Set set = new Sheet.Set();
	set.setName("Indents"); // NOI18N
	set.setDisplayName(getString("LBL_TabsAndIndents"));
        set.setShortDescription(getString("HINT_TabsAndIndents"));
	set.put(new IntNodeProp(currentLanguage, preferences, EditorOptions.statementContinuationIndent));
	set.put(new PreprocessorIndentProperty(currentLanguage, preferences, EditorOptions.indentPreprocessorDirectives));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.sharpAtStartLine));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.indentCasesFromSwitch));
        sheet.put(set);
        set.addPropertyChangeListener(this);
        
	set = new Sheet.Set();
	set.setName("BracesPlacement"); // NOI18N
	set.setDisplayName(getString("LBL_BracesPlacement"));
	set.setShortDescription(getString("HINT_BracesPlacement"));
	set.put(new BracePlacementProperty(currentLanguage, preferences, EditorOptions.newLineBeforeBraceNamespace));
	set.put(new BracePlacementProperty(currentLanguage, preferences, EditorOptions.newLineBeforeBraceClass));
	set.put(new BracePlacementProperty(currentLanguage, preferences, EditorOptions.newLineBeforeBraceDeclaration));
	set.put(new BracePlacementProperty(currentLanguage, preferences, EditorOptions.newLineBeforeBrace));
        sheet.put(set);
        set.addPropertyChangeListener(this);
        
	set = new Sheet.Set();
	set.setName("MultilineAlignment"); // NOI18N
	set.setDisplayName(getString("LBL_MultilineAlignment"));
	set.setShortDescription(getString("HINT_MultilineAlignment"));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.alignMultilineMethodParams));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.alignMultilineCallArgs));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.alignMultilineArrayInit));
        sheet.put(set);
        set.addPropertyChangeListener(this);

        set = new Sheet.Set();
	set.setName("NewLine"); // NOI18N
	set.setDisplayName(getString("LBL_NewLine"));
	set.setShortDescription(getString("HINT_NewLine"));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.newLineCatch));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.newLineElse));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.newLineWhile));
        sheet.put(set);
        set.addPropertyChangeListener(this);
        
        set = new Sheet.Set();
	set.setName("SpacesBeforeKeywords"); // NOI18N
	set.setDisplayName(getString("LBL_BeforeKeywords"));
	set.setShortDescription(getString("HINT_BeforeKeywords"));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeWhile));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeElse));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeCatch));
        sheet.put(set);
        set.addPropertyChangeListener(this);
        
        set = new Sheet.Set();
	set.setName("SpacesBeforeParentheses"); // NOI18N
	set.setDisplayName(getString("LBL_BeforeParentheses"));
	set.setShortDescription(getString("HINT_BeforeParentheses"));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeMethodDeclParen));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeMethodCallParen));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeIfParen));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeForParen));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeWhileParen));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeCatchParen));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeSwitchParen));
        sheet.put(set);
        set.addPropertyChangeListener(this);
        
        set = new Sheet.Set();
	set.setName("SpacesAroundOperators"); // NOI18N
	set.setDisplayName(getString("LBL_AroundOperators"));
	set.setShortDescription(getString("HINT_AroundOperators"));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceAroundUnaryOps));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceAroundBinaryOps));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceAroundTernaryOps));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceAroundAssignOps));
        sheet.put(set);
        set.addPropertyChangeListener(this);
    
        set = new Sheet.Set();
	set.setName("SpacesBeforeLeftBracess"); // NOI18N
	set.setDisplayName(getString("LBL_BeforeLeftBraces"));
	set.setShortDescription(getString("HINT_BeforeLeftBraces"));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeClassDeclLeftBrace));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeMethodDeclLeftBrace));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeIfLeftBrace));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeElseLeftBrace));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeWhileLeftBrace));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeForLeftBrace));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeDoLeftBrace));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeSwitchLeftBrace));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeTryLeftBrace));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeCatchLeftBrace));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeArrayInitLeftBrace));
        sheet.put(set);
        set.addPropertyChangeListener(this);

        set = new Sheet.Set();
	set.setName("SpacesWithinParentheses"); // NOI18N
	set.setDisplayName(getString("LBL_WithinParentheses"));
	set.setShortDescription(getString("HINT_WithinParentheses"));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceWithinParens));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceWithinMethodDeclParens));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceWithinMethodCallParens));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceWithinIfParens));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceWithinForParens));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceWithinWhileParens));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceWithinSwitchParens));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceWithinCatchParens));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceWithinTypeCastParens));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceWithinBraces));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceWithinArrayInitBrackets));
        sheet.put(set);
        set.addPropertyChangeListener(this);
                
        set = new Sheet.Set();
	set.setName("SpacesOther"); // NOI18N
	set.setDisplayName(getString("LBL_Other"));
	set.setShortDescription(getString("HINT_Other"));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeComma));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceAfterComma));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeSemi));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceAfterSemi));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceBeforeColon));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceAfterColon));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.spaceAfterTypeCast));
        sheet.put(set);
        set.addPropertyChangeListener(this);
        
        set = new Sheet.Set();
	set.setName("BlankLines"); // NOI18N
	set.setDisplayName(getString("LBL_BlankLines"));
	set.setShortDescription(getString("HINT_BlankLines"));
	set.put(new IntNodeProp(currentLanguage, preferences, EditorOptions.blankLinesBeforeClass));
	set.put(new IntNodeProp(currentLanguage, preferences, EditorOptions.blankLinesAfterClass));
	set.put(new IntNodeProp(currentLanguage, preferences, EditorOptions.blankLinesAfterClassHeader));
	set.put(new IntNodeProp(currentLanguage, preferences, EditorOptions.blankLinesBeforeFields));
	set.put(new IntNodeProp(currentLanguage, preferences, EditorOptions.blankLinesAfterFields));
	set.put(new IntNodeProp(currentLanguage, preferences, EditorOptions.blankLinesBeforeMethods));
	set.put(new IntNodeProp(currentLanguage, preferences, EditorOptions.blankLinesAfterMethods));
        sheet.put(set);
        set.addPropertyChangeListener(this);
        
        set = new Sheet.Set();
	set.setName("Other"); // NOI18N
	set.setDisplayName(getString("LBL_Other"));
	set.setShortDescription(getString("HINT_Other"));
	set.put(new BooleanNodeProp(currentLanguage, preferences, EditorOptions.addLeadingStarInComment));
        sheet.put(set);
        set.addPropertyChangeListener(this);

        DummyNode[] dummyNodes = new DummyNode[1];
        dummyNodes[0] = new DummyNode(sheet, "Sheet");
        PropertySheet holder = new PropertySheet();
        holder.setNodes(dummyNodes);
        categoryPanel.add(holder, BorderLayout.CENTER);
        categoryPanel.validate();
        categoryPanel.repaint();
    }

    void load() {
        loaded = false;
//        for (Category category : categories) {
//            category.update();
//        }
        loaded = true;
        repaintPreview();        
    }
    
    void store() {
        for(Map.Entry<CodeStyle.Language, Map<String,Preferences>> entry : allPreferences.entrySet()){
            CodeStyle.Language language = entry.getKey();
            Map<String,Preferences> map = entry.getValue();
            EditorOptions.setCurrentProfileId(language, defaultStyles.get(language));
            for(Map.Entry<String,Preferences> prefEntry : map.entrySet()){
                String style = prefEntry.getKey();
                Preferences preferences = prefEntry.getValue();
                Preferences toSave = EditorOptions.getPreferences(language, style);
                try {
                    for (String key : preferences.keys()) {
                            Object def = EditorOptions.getDefault(key);
                            if (def instanceof Boolean) {
                                toSave.putBoolean(key, preferences.getBoolean(key, (Boolean) def));
                            } else if (def instanceof Integer) {
                                toSave.putInt(key, preferences.getInt(key, (Integer) def));
                            } else {
                                toSave.put(key, preferences.get(key, (String) def));
                            }
                    }
                } catch (BackingStoreException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
    
    void cancel() {
        //EditorOptions.lastValues = null;
    }

    // Change in the combo
    public void actionPerformed(ActionEvent e) {
        lastChangedproperty = null;
        if (styleComboBox.equals(e.getSource())){
            EntryWrapper category = (EntryWrapper)styleComboBox.getSelectedItem();
            if (category != null) {
                defaultStyles.put(currentLanguage,category.name);
                categoryPanel.setVisible(false);
                categoryPanel.removeAll();
                initSheets(category.preferences);
                categoryPanel.setVisible(true);
                if (CodeStyle.Language.C.equals(currentLanguage)){
                    previewPane.setContentType("text/x-c"); // NOI18N
                } else {
                    previewPane.setContentType("text/x-c++"); // NOI18N
                }
                if (loaded) {
                    repaintPreview();
                }
            }
        } else if (languagesComboBox.equals(e.getSource())){
            currentLanguage = (CodeStyle.Language)languagesComboBox.getSelectedItem();
            initLanguageCategory();
        }
    }

    public void preferenceChange(PreferenceChangeEvent evt) {
        lastChangedproperty = evt.getKey();
        change();
    }
    

    // Change in some of the subpanels
    public void propertyChange(PropertyChangeEvent evt) {
        change();
    }

    private void change(){
        if ( !loaded ) {
            return;
        }
        Runnable run = new Runnable() {
            public void run() {
                // Notify the main controler that the page has changed
                topControler.changed();
                // Repaint the preview
                repaintPreview();
            }
        };
        if(SwingUtilities.isEventDispatchThread()){
            run.run();
        } else {
            SwingUtilities.invokeLater(run);
        }
    }
    
    private void repaintPreview() { 
        EntryWrapper category = (EntryWrapper)styleComboBox.getSelectedItem();
        Preferences p = new PreviewPreferences(category.preferences);
        if (category != null) {
            jScrollPane1.setIgnoreRepaint(true);
            refreshPreview(previewPane, p);
            previewPane.setIgnoreRepaint(false);
            previewPane.scrollRectToVisible(new Rectangle(0,0,10,10) );
            previewPane.repaint(100);
        }
    }
    
    private String getPreviwText(){
        if (lastChangedproperty != null) {
            if (lastChangedproperty.startsWith("space")) {
                return getString("SAMPLE_Spaces");
            } else if (lastChangedproperty.startsWith("blank")) {
                return getString("SAMPLE_BlankLines");
            }  else if (lastChangedproperty.startsWith("align") ||
                        lastChangedproperty.startsWith("new")) {
                return getString("SAMPLE_AlignBraces");
            }
        }
            return getString("SAMPLE_TabsIndents");
    }
    
    public void refreshPreview(JEditorPane pane, Preferences p) {
        pane.setText(getPreviwText());
        CodeStyle codeStyle = EditorOptions.createCodeStyle(currentLanguage, p);
        BaseDocument bd = (BaseDocument) pane.getDocument();
        try {
            new Reformatter(bd, codeStyle).reformat();
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
//        Preferences oldPreferences = ((CodeStyleImpl)CodeStyle.getDefault(language)).getPref();
//        ((CodeStyleImpl)CodeStyle.getDefault(language)).setPref(p);
//        Formatter f = bd.getFormatter();
//        try {
//	    f.reformatLock();
//            f.reformat(bd, 0, bd.getLength());
//            String x = bd.getText(0, bd.getLength());
//            pane.setText(x);
//        } catch (BadLocationException ex) {
//            Exceptions.printStackTrace(ex);
//        } finally {
//            ((CodeStyleImpl)CodeStyle.getDefault(language)).setPref(oldPreferences);
//	    f.reformatUnlock();
//	}
    }

    private static String getString(String key) {
        return NbBundle.getMessage(EditorPropertySheet.class, key);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel2 = new javax.swing.JLabel();
        languagesComboBox = new javax.swing.JComboBox();
        jSplitPane1 = new javax.swing.JSplitPane();
        oprionsPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        styleComboBox = new javax.swing.JComboBox();
        categoryPanel = new javax.swing.JPanel();
        manageStyles = new javax.swing.JButton();
        previewPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        previewPane = new javax.swing.JEditorPane();
        jSeparator1 = new javax.swing.JSeparator();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(EditorPropertySheet.class, "EditorPropertySheet.jLabel2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        add(jLabel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(languagesComboBox, gridBagConstraints);

        jSplitPane1.setBorder(null);
        jSplitPane1.setDividerLocation(300);

        oprionsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        oprionsPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(EditorPropertySheet.class, "LBL_Style_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 6);
        oprionsPanel.add(jLabel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        oprionsPanel.add(styleComboBox, gridBagConstraints);

        categoryPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        categoryPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        oprionsPanel.add(categoryPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(manageStyles, org.openide.util.NbBundle.getMessage(EditorPropertySheet.class, "EditorPropertySheet.manageStyles.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 6);
        oprionsPanel.add(manageStyles, gridBagConstraints);

        jSplitPane1.setLeftComponent(oprionsPanel);

        previewPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        previewPanel.setLayout(new java.awt.GridBagLayout());

        jScrollPane1.setViewportView(previewPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        previewPanel.add(jScrollPane1, gridBagConstraints);

        jSplitPane1.setRightComponent(previewPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(jSplitPane1, gridBagConstraints);

        jSeparator1.setForeground(javax.swing.UIManager.getDefaults().getColor("Button.disabledText"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        add(jSeparator1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel categoryPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JComboBox languagesComboBox;
    private javax.swing.JButton manageStyles;
    private javax.swing.JPanel oprionsPanel;
    private javax.swing.JEditorPane previewPane;
    private javax.swing.JPanel previewPanel;
    private javax.swing.JComboBox styleComboBox;
    // End of variables declaration//GEN-END:variables

    public static class PreviewPreferences extends AbstractPreferences {
        private Map<String,Object> map = new HashMap<String, Object>();
        public PreviewPreferences(Preferences master) {
            super(null, ""); // NOI18N
            try {
                for (String key : master.keys()) {
                    Object o = EditorOptions.getDefault(key);
                    if (o instanceof Boolean) {
                        putBoolean(key, master.getBoolean(key, (Boolean) EditorOptions.getDefault(key)));
                    } else if (o instanceof Integer) {
                        putInt(key, master.getInt(key, (Integer) EditorOptions.getDefault(key)));
                    } else {
                        map.put(key, master.get(key, EditorOptions.getDefault(key).toString()));
                    }
                }
            } catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        protected void putSpi(String key, String value) {
            map.put(key, value);            
        }
        protected String getSpi(String key) {
            return (String)map.get(key);                    
        }
        protected void removeSpi(String key) {
            map.remove(key);
        }
        protected void removeNodeSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        protected String[] keysSpi() throws BackingStoreException {
            String array[] = new String[map.keySet().size()];
            return map.keySet().toArray( array );
        }
        protected String[] childrenNamesSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        protected AbstractPreferences childSpi(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        protected void syncSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        protected void flushSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private static class EntryWrapper {
        private final String name;
        private final Preferences preferences;
        private EntryWrapper(Map.Entry<String, Preferences> enrty){
            this.name = enrty.getKey();
            this.preferences = enrty.getValue();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private class DummyNode extends AbstractNode {
        public DummyNode(Sheet sheet, String name) {
            super(Children.LEAF);
            if (sheet != null) {
                setSheet(sheet);
            }
            setName(name);
        }
    }
}

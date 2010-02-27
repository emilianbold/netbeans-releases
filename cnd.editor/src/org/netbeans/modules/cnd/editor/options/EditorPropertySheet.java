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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.editor.reformat.Reformatter;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * @author  as204739
 */
public class EditorPropertySheet extends javax.swing.JPanel
        implements ActionListener, PropertyChangeListener, PreferenceChangeListener {

    private static final boolean TRACE = false;

    private final EditorOptionsPanelController topController;
    private boolean loaded = false;
    private final CodeStyle.Language language;
    private String lastChangedproperty;
    private String defaultStyles;
    private Map<String, PreviewPreferences> preferences = new HashMap<String, PreviewPreferences>();
    private PropertySheet holder;
    private Object[] originalEditorProperties = null;

    EditorPropertySheet(EditorOptionsPanelController topControler, CodeStyle.Language language) {
        this.topController = topControler;
        this.language = language;
        initComponents();
        overrideGlobalOptions.setSelected(EditorOptions.getOverideTabIndents(language));
        overrideGlobalOptions.addActionListener(this);

        holder = new PropertySheet();
        holder.setOpaque(false);
        holder.setDescriptionAreaVisible(false);
        holder.setPreferredSize(new Dimension(250,150));
        GridBagConstraints fillConstraints = new GridBagConstraints();
        fillConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        fillConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        fillConstraints.fill = java.awt.GridBagConstraints.BOTH;
        fillConstraints.weightx = 1.0;
        fillConstraints.weighty = 1.0;
        categoryPanel.add(holder, fillConstraints);

        manageStyles.setMinimumSize(new Dimension(126,26));
        setName(getString("Tab_Name")); // NOI18N
    }

    private void initLanguageMap(){
        for(String style:EditorOptions.getAllStyles(language)){
            initLanguageStylePreferences(language, style);
        }
        defaultStyles = EditorOptions.getCurrentProfileId(language);
    }

    private void initLanguageStylePreferences(CodeStyle.Language language, String styleId){
        if (preferences == null){
            preferences = new HashMap<String, PreviewPreferences>();
        }
        PreviewPreferences clone = new PreviewPreferences(
                                   EditorOptions.getPreferences(language, styleId), language, styleId);
        preferences.put(styleId, clone);
    }


    private void initLanguageCategory(){
        styleComboBox.removeActionListener(this);
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        List<EntryWrapper> list = new ArrayList<EntryWrapper>();
        for(Map.Entry<String, PreviewPreferences> entry : preferences.entrySet()) {
            list.add(new EntryWrapper(entry));
        }
        Collections.sort(list);
        int index = 0;
        int i = 0;
        for(EntryWrapper entry : list) {
            if (entry.name.equals(defaultStyles)) {
                index = i;
            }
            model.addElement(entry);
            i++;
        }
        styleComboBox.setModel(model);
        styleComboBox.setSelectedIndex(index);
        EntryWrapper entry = (EntryWrapper)styleComboBox.getSelectedItem();
        initSheets(entry.preferences);
        defaultStyles = entry.name;
        styleComboBox.addActionListener(this);
        repaintPreview();
    }

    private PreviewPreferences lastSheetPreferences = null;

    private void initSheets(PreviewPreferences preferences){
        if (TRACE) {
            System.out.println("Set properties for "+preferences.getLanguage()+" "+preferences.getStyleId()); // NOI18N
        }
        if (lastSheetPreferences != null){
            lastSheetPreferences.removePreferenceChangeListener(this);
        }
        Sheet sheet = new Sheet();
        Sheet.Set set = new Sheet.Set();
        set.setName("Indents"); // NOI18N
        set.setDisplayName(getString("LBL_TabsAndIndents")); // NOI18N
        set.setShortDescription(getString("HINT_TabsAndIndents")); // NOI18N
        if (overrideGlobalOptions.isSelected()) {
            set.put(new IntNodeProp(language, preferences, EditorOptions.indentSize));
        	set.put(new BooleanNodeProp(language, preferences, EditorOptions.expandTabToSpaces));
            set.put(new IntNodeProp(language, preferences, EditorOptions.tabSize));
        } else {
            set.put(new IntNodeProp(language, preferences, EditorOptions.indentSize, EditorOptions.getGlobalIndentSize()));
        	set.put(new BooleanNodeProp(language, preferences, EditorOptions.expandTabToSpaces, EditorOptions.getGlobalExpandTabs()));
            set.put(new IntNodeProp(language, preferences, EditorOptions.tabSize, EditorOptions.getGlobalTabSize()));
        }
        set.put(new IntNodeProp(language, preferences, EditorOptions.statementContinuationIndent));
        set.put(new IntNodeProp(language, preferences, EditorOptions.constructorListContinuationIndent));
        set.put(new PreprocessorIndentProperty(language, preferences, EditorOptions.indentPreprocessorDirectives));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.sharpAtStartLine));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.indentNamespace));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.indentCasesFromSwitch));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.absoluteLabelIndent));
        set.put(new VisibilityIndentProperty(language, preferences, EditorOptions.indentVisibility));
        set.put(new  BooleanNodeProp(language, preferences, EditorOptions.spaceKeepExtra));
        sheet.put(set);

        set = new Sheet.Set();
        set.setName("BracesPlacement"); // NOI18N
        set.setDisplayName(getString("LBL_BracesPlacement")); // NOI18N
        set.setShortDescription(getString("HINT_BracesPlacement")); // NOI18N
        set.put(new BracePlacementProperty(language, preferences, EditorOptions.newLineBeforeBraceNamespace));
        set.put(new BracePlacementProperty(language, preferences, EditorOptions.newLineBeforeBraceClass));
        set.put(new BracePlacementProperty(language, preferences, EditorOptions.newLineBeforeBraceDeclaration));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.ignoreEmptyFunctionBody));
        set.put(new BracePlacementProperty(language, preferences, EditorOptions.newLineBeforeBraceSwitch));
        set.put(new BracePlacementProperty(language, preferences, EditorOptions.newLineBeforeBrace));
        sheet.put(set);

        set = new Sheet.Set();
        set.setName("MultilineAlignment"); // NOI18N
        set.setDisplayName(getString("LBL_MultilineAlignment")); // NOI18N
        set.setShortDescription(getString("HINT_MultilineAlignment")); // NOI18N
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.alignMultilineMethodParams));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.alignMultilineCallArgs));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.alignMultilineArrayInit));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.alignMultilineFor));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.alignMultilineIfCondition));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.alignMultilineWhileCondition));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.alignMultilineParen));
        sheet.put(set);

        set = new Sheet.Set();
        set.setName("NewLine"); // NOI18N
        set.setDisplayName(getString("LBL_NewLine")); // NOI18N
        set.setShortDescription(getString("HINT_NewLine")); // NOI18N
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.newLineFunctionDefinitionName));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.newLineCatch));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.newLineElse));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.newLineWhile));
        sheet.put(set);

        set = new Sheet.Set();
        set.setName("SpacesBeforeKeywords"); // NOI18N
        set.setDisplayName(getString("LBL_BeforeKeywords")); // NOI18N
        set.setShortDescription(getString("HINT_BeforeKeywords")); // NOI18N
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeCatch));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeElse));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeWhile));
        sheet.put(set);

        set = new Sheet.Set();
        set.setName("SpacesBeforeParentheses"); // NOI18N
        set.setDisplayName(getString("LBL_BeforeParentheses")); // NOI18N
        set.setShortDescription(getString("HINT_BeforeParentheses")); // NOI18N
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeMethodDeclParen));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeMethodCallParen));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeCatchParen));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeForParen));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeIfParen));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeSwitchParen));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeWhileParen));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeKeywordParen));
        sheet.put(set);

        set = new Sheet.Set();
        set.setName("SpacesAroundOperators"); // NOI18N
        set.setDisplayName(getString("LBL_AroundOperators")); // NOI18N
        set.setShortDescription(getString("HINT_AroundOperators")); // NOI18N
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceAroundAssignOps));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceAroundBinaryOps));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceAroundTernaryOps));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceAroundUnaryOps));
        sheet.put(set);

        set = new Sheet.Set();
        set.setName("SpacesBeforeLeftBracess"); // NOI18N
        set.setDisplayName(getString("LBL_BeforeLeftBraces")); // NOI18N
        set.setShortDescription(getString("HINT_BeforeLeftBraces")); // NOI18N
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeClassDeclLeftBrace));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeMethodDeclLeftBrace));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeArrayInitLeftBrace));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeCatchLeftBrace));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeDoLeftBrace));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeElseLeftBrace));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeForLeftBrace));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeIfLeftBrace));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeSwitchLeftBrace));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeTryLeftBrace));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeWhileLeftBrace));
        sheet.put(set);

        set = new Sheet.Set();
        set.setName("SpacesWithinParentheses"); // NOI18N
        set.setDisplayName(getString("LBL_WithinParentheses")); // NOI18N
        set.setShortDescription(getString("HINT_WithinParentheses")); // NOI18N
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceWithinMethodDeclParens));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceWithinMethodCallParens));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceWithinBraces));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceWithinParens));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceWithinCatchParens));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceWithinForParens));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceWithinIfParens));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceWithinSwitchParens));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceWithinTypeCastParens));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceWithinWhileParens));
        sheet.put(set);

        set = new Sheet.Set();
        set.setName("SpacesOther"); // NOI18N
        set.setDisplayName(getString("LBL_Other_Spaces")); // NOI18N
        set.setShortDescription(getString("HINT_Other_Spaces")); // NOI18N
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeComma));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceAfterComma));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeSemi));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceAfterSemi));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeColon));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceAfterColon));
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceAfterTypeCast));
        sheet.put(set);

        set = new Sheet.Set();
        set.setName("BlankLines"); // NOI18N
        set.setDisplayName(getString("LBL_BlankLines")); // NOI18N
        set.setShortDescription(getString("HINT_BlankLines")); // NOI18N
        set.put(new IntNodeProp(language, preferences, EditorOptions.blankLinesBeforeClass));
        //set.put(new IntNodeProp(currentLanguage, preferences, EditorOptions.blankLinesAfterClass));
        set.put(new IntNodeProp(language, preferences, EditorOptions.blankLinesAfterClassHeader));
        //set.put(new IntNodeProp(currentLanguage, preferences, EditorOptions.blankLinesBeforeFields));
        //set.put(new IntNodeProp(currentLanguage, preferences, EditorOptions.blankLinesAfterFields));
        set.put(new IntNodeProp(language, preferences, EditorOptions.blankLinesBeforeMethods));
        //set.put(new IntNodeProp(currentLanguage, preferences, EditorOptions.blankLinesAfterMethods));
        sheet.put(set);

        set = new Sheet.Set();
        set.setName("Other"); // NOI18N
        set.setDisplayName(getString("LBL_Other")); // NOI18N
        set.setShortDescription(getString("HINT_Other")); // NOI18N
        set.put(new BooleanNodeProp(language, preferences, EditorOptions.addLeadingStarInComment));
        sheet.put(set);

        final DummyNode[] dummyNodes = new DummyNode[1];
        dummyNodes[0] = new DummyNode(sheet, "Sheet"); // NOI18N
        holder.setNodes(dummyNodes);

        preferences.addPreferenceChangeListener(this);
        lastSheetPreferences = preferences;
    }

    void load() {
        loaded = false;
        originalEditorProperties = preserveEditorProperties();
        initLanguageMap();
        initLanguageCategory();
        loaded = true;
        repaintPreview();
    }

    void store() {
        EditorOptions.setCurrentProfileId(language, defaultStyles);
        EditorOptions.setOverideTabIndents(language, overrideGlobalOptions.isSelected());
        StringBuilder buf = new StringBuilder();
        for(Map.Entry<String, PreviewPreferences> prefEntry : preferences.entrySet()){
            String style = prefEntry.getKey();
            if (buf.length() > 0){
                buf.append(',');
            }
            buf.append(style);
            PreviewPreferences pref = prefEntry.getValue();
            Preferences toSave = EditorOptions.getPreferences(language, style);
            if (style.equals(defaultStyles)){
                EditorOptions.setPreferences(CodeStyle.getDefault(language), toSave);
            }
            for(String key : EditorOptions.keys()){
                Object o = EditorOptions.getDefault(language, style, key);
                if (o instanceof Boolean) {
                    Boolean v = pref.getBoolean(key, (Boolean) o);
                    if (!o.equals(v)) {
                        toSave.putBoolean(key, v);
                    } else {
                        toSave.remove(key);
                    }
                } else if (o instanceof Integer) {
                    Integer v = pref.getInt(key, (Integer) o);
                    if (!o.equals(v)) {
                        toSave.putInt(key, v);
                    } else {
                        toSave.remove(key);
                    }
                } else {
                    String v = pref.get(key, o.toString());
                    if (!o.equals(v)) {
                        toSave.put(key, v);
                    } else {
                        toSave.remove(key);
                    }
                }
            }
            if (style.equals(defaultStyles)){
                EditorOptions.updateSimplePreferences(language, CodeStyle.getDefault(language));
            }
        }
        EditorOptions.setAllStyles(language, buf.toString());
        defaultStyles = null;
        preferences.clear();
        holder.setNodes(null);
    }

    void cancel() {
        if (originalEditorProperties != null) {
            restoreEditorProperties(originalEditorProperties);
            originalEditorProperties = null;
        }
        defaultStyles = null;
        preferences.clear();
        holder.setNodes(null);
    }

    // Change in the combo
    public void actionPerformed(ActionEvent e) {
        lastChangedproperty = null;
        if (styleComboBox.equals(e.getSource())){
            EntryWrapper category = (EntryWrapper)styleComboBox.getSelectedItem();
            if (category != null) {
                defaultStyles = category.name;
                initSheets(category.preferences);
                repaintPreview();
            }
        } else if (overrideGlobalOptions.equals(e.getSource())) {
            EntryWrapper category = (EntryWrapper)styleComboBox.getSelectedItem();
            if (category != null) {
                defaultStyles = category.name;
                initSheets(category.preferences);
                repaintPreview();
            }
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
                topController.changed();
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

    public void repaintPreview() {
        EntryWrapper category = (EntryWrapper)styleComboBox.getSelectedItem();
        if (category == null) {
            category = (EntryWrapper)styleComboBox.getItemAt(0);
        }
        JEditorPane previewPane = (JEditorPane) topController.getPreviewComponent();
        if (loaded) {
            PreviewPreferences p = new PreviewPreferences(category.preferences,
                    category.preferences.getLanguage(), category.preferences.getStyleId());
            p.makeAllKeys(category.preferences);
            p.putBoolean(EditorOptions.overrideTabIndents, overrideGlobalOptions.isSelected());
            if (!overrideGlobalOptions.isSelected()){
                p.putInt(EditorOptions.indentSize, EditorOptions.getGlobalIndentSize());
                p.putBoolean(EditorOptions.expandTabToSpaces, EditorOptions.getGlobalExpandTabs());
                p.putInt(EditorOptions.tabSize, EditorOptions.getGlobalTabSize());
           }
            p.putInt(SimpleValueNames.TAB_SIZE, p.getInt(EditorOptions.tabSize, EditorOptions.tabSizeDefault));
            p.putInt(SimpleValueNames.SPACES_PER_TAB, p.getInt(EditorOptions.tabSize, EditorOptions.tabSizeDefault));
            p.putBoolean(SimpleValueNames.EXPAND_TABS, p.getBoolean(EditorOptions.expandTabToSpaces, EditorOptions.expandTabToSpacesDefault));
            p.putInt(SimpleValueNames.INDENT_SHIFT_WIDTH, p.getInt(EditorOptions.indentSize, EditorOptions.indentSizeDefault));
            previewPane.setIgnoreRepaint(true);
            refreshPreview(previewPane, p);
            previewPane.setIgnoreRepaint(false);
            previewPane.scrollRectToVisible(new Rectangle(0,0,10,10) );
            previewPane.repaint(100);
        }
    }

    private String getPreviewText(){
        String suffix;
        switch (language){
            case C:
                suffix = ".c"; // NOI18N
                break;
            case HEADER:
                suffix = ".cpp"; // NOI18N
                break;
            case CPP:
            default:
                suffix = ".cpp"; // NOI18N
                break;
        }
        if (lastChangedproperty != null) {
            if (lastChangedproperty.startsWith("space")) { // NOI18N
                return loadPreviewExample("SAMPLE_Spaces"+suffix); // NOI18N
            } else if (lastChangedproperty.startsWith("blank")) { // NOI18N
                return loadPreviewExample("SAMPLE_BlankLines"+suffix); // NOI18N
            }  else if (lastChangedproperty.startsWith("align") || // NOI18N
                        lastChangedproperty.startsWith("new")) { // NOI18N
                return loadPreviewExample("SAMPLE_AlignBraces"+suffix); // NOI18N
            }
        }
        return loadPreviewExample("SAMPLE_TabsIndents"+suffix); // NOI18N
    }

    private String loadPreviewExample(String example) {
        FileObject exampleFile = FileUtil.getConfigFile("OptionsDialog/CPlusPlus/FormatterPreviewExamples/" + example); //NOI18N
        if (exampleFile != null && exampleFile.getSize() > 0) {
            StringBuilder sb = new StringBuilder((int) exampleFile.getSize());
            try {
                InputStreamReader is = new InputStreamReader(exampleFile.getInputStream());
                char[] buffer = new char[1024];
                int size;
                try {
                    while (0 < (size = is.read(buffer, 0, buffer.length))) {
                        sb.append(buffer, 0, size);
                    }
                } finally {
                    is.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            return sb.toString();
        } else {
            return ""; //NOI18N
        }
    }

    private void refreshPreview(JEditorPane pane, Preferences p) {
        pane.setText(getPreviewText());
        BaseDocument bd = (BaseDocument) pane.getDocument();
        CodeStyle codeStyle = EditorOptions.createCodeStyle(language, p, false);
        setEditorProperties(p);
        try {
            if (TRACE) {
                System.err.println("Refreshing preview"); // NOI18N
                System.err.println("          tabSize=" + IndentUtils.tabSize(bd)+"/"+codeStyle.getTabSize()); // NOI18N
                System.err.println("       expandTabs=" + IndentUtils.isExpandTabs(bd)+"/"+codeStyle.expandTabToSpaces()); // NOI18N
                System.err.println("  indentLevelSize=" + IndentUtils.indentLevelSize(bd)+"/"+codeStyle.indentSize()); // NOI18N
                System.err.println("  doc=" + bd); //NOI18N
            }
            new Reformatter(bd, codeStyle).reformat();
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void setEditorProperties(Preferences p) {
        Preferences def = null;
        switch (language){
            case C:
                def = MimeLookup.getLookup(MIMENames.C_MIME_TYPE).lookup(Preferences.class);
                break;
            case HEADER:
                def = MimeLookup.getLookup(MIMENames.HEADER_MIME_TYPE).lookup(Preferences.class);
                break;
            case CPP:
            default:
                def = MimeLookup.getLookup(MIMENames.CPLUSPLUS_MIME_TYPE).lookup(Preferences.class);
                break;
        }
        if (def != null) {
            def.putInt(SimpleValueNames.TAB_SIZE, p.getInt(EditorOptions.tabSize, EditorOptions.tabSizeDefault));
            def.putInt(SimpleValueNames.SPACES_PER_TAB, p.getInt(EditorOptions.tabSize, EditorOptions.tabSizeDefault));
            def.putBoolean(SimpleValueNames.EXPAND_TABS, p.getBoolean(EditorOptions.expandTabToSpaces, EditorOptions.expandTabToSpacesDefault));
            def.putInt(SimpleValueNames.INDENT_SHIFT_WIDTH, p.getInt(EditorOptions.indentSize, EditorOptions.indentSizeDefault));
        }
    }

    private Object[] preserveEditorProperties() {
        Preferences def = null;
        Object oldValues[] = null;
        switch (language){
            case C:
                def = MimeLookup.getLookup(MIMENames.C_MIME_TYPE).lookup(Preferences.class);
                break;
            case HEADER:
                def = MimeLookup.getLookup(MIMENames.HEADER_MIME_TYPE).lookup(Preferences.class);
                break;
            case CPP:
            default:
                def = MimeLookup.getLookup(MIMENames.CPLUSPLUS_MIME_TYPE).lookup(Preferences.class);
                break;
        }
        if (def != null) {
            oldValues = new Object[]{null, null, null, null};
            if (null != def.get(SimpleValueNames.TAB_SIZE, null)) {
                oldValues[0] = def.getInt(SimpleValueNames.TAB_SIZE, EditorOptions.tabSizeDefault);
            }
            if (null != def.get(SimpleValueNames.SPACES_PER_TAB, null)) {
                oldValues[1] = def.getInt(SimpleValueNames.SPACES_PER_TAB, EditorOptions.tabSizeDefault);
            }
            if (null != def.get(SimpleValueNames.EXPAND_TABS, null)) {
                oldValues[2] = def.getBoolean(SimpleValueNames.EXPAND_TABS, EditorOptions.expandTabToSpacesDefault);
            }
            if (null != def.get(SimpleValueNames.INDENT_SHIFT_WIDTH, null)) {
                oldValues[3] = def.getInt(SimpleValueNames.INDENT_SHIFT_WIDTH, EditorOptions.indentSizeDefault);
            }
            if (TRACE) {
                System.err.println("Preserving editor properties:"); //NOI18N
                System.err.println("           tabSize=" + oldValues[0]); //NOI18N
                System.err.println("      spacesPerTab=" + oldValues[1]); //NOI18N
                System.err.println("        expandTabs=" + oldValues[2]); //NOI18N
                System.err.println("  indentShiftWidth=" + oldValues[3]); //NOI18N
            }
        }
        return oldValues;
    }

    private void restoreEditorProperties(Object[] oldValues) {
        Preferences def = null;
        switch (language){
            case C:
                def = MimeLookup.getLookup(MIMENames.C_MIME_TYPE).lookup(Preferences.class);
                break;
            case HEADER:
                def = MimeLookup.getLookup(MIMENames.HEADER_MIME_TYPE).lookup(Preferences.class);
                break;
            case CPP:
            default:
                def = MimeLookup.getLookup(MIMENames.CPLUSPLUS_MIME_TYPE).lookup(Preferences.class);
                break;
        }
        if (def != null && oldValues != null) {
            if (TRACE) {
                System.err.println("Restoring editor properties:"); //NOI18N
                System.err.println("           tabSize=" + oldValues[0]); //NOI18N
                System.err.println("      spacesPerTab=" + oldValues[1]); //NOI18N
                System.err.println("        expandTabs=" + oldValues[2]); //NOI18N
                System.err.println("  indentShiftWidth=" + oldValues[3]); //NOI18N
            }
            if (oldValues[0] == null) {
                def.remove(SimpleValueNames.TAB_SIZE);
            } else {
                def.putInt(SimpleValueNames.TAB_SIZE, ((Integer)oldValues[0]).intValue());
            }
            if (oldValues[1] == null) {
                def.remove(SimpleValueNames.SPACES_PER_TAB);
            } else {
                def.putInt(SimpleValueNames.SPACES_PER_TAB, ((Integer)oldValues[1]).intValue());
            }
            if (oldValues[2] == null) {
                def.remove(SimpleValueNames.EXPAND_TABS);
            } else {
                def.putBoolean(SimpleValueNames.EXPAND_TABS, ((Boolean)oldValues[2]).booleanValue());
            }
            if (oldValues[3] == null) {
                def.remove(SimpleValueNames.INDENT_SHIFT_WIDTH);
            } else {
                def.putInt(SimpleValueNames.INDENT_SHIFT_WIDTH, ((Integer)oldValues[3]).intValue());
            }
        }
    }

    private static String getString(String key) {
        return NbBundle.getMessage(EditorPropertySheet.class, key);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked") // NOI18N
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        styleComboBox = new javax.swing.JComboBox();
        categoryPanel = new javax.swing.JPanel();
        manageStyles = new javax.swing.JButton();
        overrideGlobalOptions = new javax.swing.JCheckBox();

        setOpaque(false);
        setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(styleComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(EditorPropertySheet.class, "LBL_Style_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 6);
        add(jLabel1, gridBagConstraints);

        styleComboBox.setMaximumSize(new java.awt.Dimension(100, 25));
        styleComboBox.setPreferredSize(new java.awt.Dimension(100, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(styleComboBox, gridBagConstraints);

        categoryPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        categoryPanel.setOpaque(false);
        categoryPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(categoryPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(manageStyles, org.openide.util.NbBundle.getMessage(EditorPropertySheet.class, "EditorPropertySheet.manageStyles.text")); // NOI18N
        manageStyles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageStylesActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 6);
        add(manageStyles, gridBagConstraints);

        overrideGlobalOptions.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(overrideGlobalOptions, org.openide.util.NbBundle.getMessage(EditorPropertySheet.class, "LBL_OverrideGlobalOptions")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(overrideGlobalOptions, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void manageStylesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageStylesActionPerformed
    Map<String,PreviewPreferences> clone =  clonePreferences();
    ManageStylesPanel stylesPanel = new ManageStylesPanel(language, clone);
    DialogDescriptor dd = new DialogDescriptor(stylesPanel, getString("MANAGE_STYLES_DIALOG_TITLE")); // NOI18N
    DialogDisplayer.getDefault().notify(dd);
    if (dd.getValue() == DialogDescriptor.OK_OPTION) {
        preferences = clone;
        initLanguageCategory();
    }
}//GEN-LAST:event_manageStylesActionPerformed

private Map<String,PreviewPreferences> clonePreferences(){
    Map<String,PreviewPreferences> newAllPreferences = new HashMap<String, PreviewPreferences>();
    for(Map.Entry<String, PreviewPreferences> entry2: preferences.entrySet()){
        PreviewPreferences pref = entry2.getValue();
        PreviewPreferences newPref = new PreviewPreferences(pref, language, entry2.getKey());
        newAllPreferences.put(entry2.getKey(), newPref);
    }
    return newAllPreferences;
}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel categoryPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton manageStyles;
    private javax.swing.JCheckBox overrideGlobalOptions;
    private javax.swing.JComboBox styleComboBox;
    // End of variables declaration//GEN-END:variables

    private static class EntryWrapper implements Comparable<EntryWrapper> {
        private final String name;
        private String displayName;
        private final PreviewPreferences preferences;
        private EntryWrapper(Map.Entry<String, PreviewPreferences> enrty){
            this.name = enrty.getKey();
            this.preferences = enrty.getValue();
            displayName = EditorOptions.getStyleDisplayName(preferences.getLanguage(),name);
        }

        @Override
        public String toString() {
            return displayName;
        }

        public int compareTo(EntryWrapper o) {
            return this.displayName.compareTo(o.displayName);
        }
    }

    private static class DummyNode extends AbstractNode {
        public DummyNode(Sheet sheet, String name) {
            super(Children.LEAF);
            if (sheet != null) {
                setSheet(sheet);
            }
            setName(name);
        }
    }

}

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

package org.netbeans.modules.editor.options;

import java.awt.Color;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.io.IOException;
import java.io.ObjectInput;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.awt.Toolkit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsDefaults;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.MultiKeyBinding;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.SimpleIndentEngine;
import org.openide.text.IndentEngine;
import java.beans.IntrospectionException;
import org.openide.loaders.DataObject;
import java.io.ObjectOutput;
import org.openide.loaders.DataFolder;
import org.openide.filesystems.FileObject;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.util.Collections;
import org.openide.util.Lookup;
import org.netbeans.modules.editor.NbEditorUtilities;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.editor.lib.ColoringMap;
import org.netbeans.modules.editor.lib.KitsTracker;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;



/**
 * Options for the base editor kit.
 * 
 * <b>Mime type vs editor kit class</b>
 * 
 * <p>The <code>BaseOptions</code> class suffers the conceptual problem introduced
 * in the editor infrastructure in early days of the Netbeans project. The problem
 * is that an implementation class of an editor kit is used for determining the
 * mime type of a file being edited. Following this concept a lot of other stuff
 * in the editor infrastructure tied up its initialization or registration with
 * editor kit implementation classes. While in many situations this only causes
 * efficiency problems or coding annoyances it is disasterous for modules that
 * need to provide dynamic support for many mime types.
 * 
 * <p>The following description tries to remedy the situation by providing guidelines
 * for writing <code>BaseOptions<code> subclasses that are nearly editor kit
 * independent.
 * 
 * <p>First, use <code>MimeLookup</code> whenever you can and avoid using static
 * getters such as <code>getOptions(Class editorKitClass)</code>. Although the
 * current <code>MimeLookup</code> infrastructure allows registering editor related
 * classes in a declrative way based on a mime type the <code>getOptions</code> method
 * can't simply query <code>MimeLookup</code>, because it doesn't know the mime type!
 * The correct way of retrieving an instance of your options class from
 * <code>MimeLookup</code> is this:
 * 
 * <pre>
 *     MimePath mimePath = MimePath.parse("your-mime-type-here");
 *     YourOptions options = MimeLookup.getLookup(mimePath).lookup(YourOptions.class);
 * </pre>
 * 
 * <p>When implementing <code>BaseOptions</code> for a specific mime type override
 * the <code>getContentType</code> method and return your mime type. Besides the
 * <code>getOptions</code> method this is currently the only place in this class
 * where an editor kit implementation class is used.
 * 
 *
 * @author Miloslav Metelka
 * @author Vita Stejskal
 * @version 1.1
 */
public class BaseOptions extends OptionSupport {

    private static final Logger LOG = Logger.getLogger(BaseOptions.class.getName());
    
    /** Latest version of the options. It must be increased
     * manually when new patch is added to the options.
     */
    protected static final int LATEST_OPTIONS_VERSION = 21;
    
    protected static final String OPTIONS_VERSION_PROP = "optionsVersion"; // NOI18N
    
    /** @deprecated Use Editor Settings Storage API instead. */
    public static final String ABBREV_MAP_PROP = "abbrevMap"; // NOI18N
    public static final String BASE = "base"; // NOI18N
    public static final String CARET_BLINK_RATE_PROP = "caretBlinkRate"; // NOI18N
    public static final String CARET_COLOR_INSERT_MODE_PROP = "caretColorInsertMode"; // NOI18N
    public static final String CARET_COLOR_OVERWRITE_MODE_PROP = "caretColorOverwriteMode"; // NOI18N
    public static final String CARET_ITALIC_INSERT_MODE_PROP = "caretItalicInsertMode"; // NOI18N
    public static final String CARET_ITALIC_OVERWRITE_MODE_PROP = "caretItalicOverwriteMode"; // NOI18N
    public static final String CARET_TYPE_INSERT_MODE_PROP = "caretTypeInsertMode"; // NOI18N
    public static final String CARET_TYPE_OVERWRITE_MODE_PROP = "caretTypeOverwriteMode"; // NOI18N
    public static final String COLORING_MAP_PROP = "coloringMap"; // NOI18N
    public static final String EXPAND_TABS_PROP = "expandTabs"; // NOI18N
    public static final String FIND_HIGHLIGHT_SEARCH_PROP = "findHighlightSearch"; // NOI18N
    public static final String FIND_HISTORY_PROP = "findHistory"; // NOI18N
    public static final String FIND_HISTORY_SIZE_PROP = "findHistorySize"; // NOI18N
    public static final String FIND_INC_SEARCH_DELAY_PROP = "findIncSearchDelay"; // NOI18N
    public static final String FIND_INC_SEARCH_PROP = "findIncSearch"; // NOI18N
    public static final String FIND_MATCH_CASE_PROP = "findMatchCase"; // NOI18N
    public static final String FIND_REG_EXP_PROP = "findRegExp"; // NOI18N
    public static final String FIND_SMART_CASE_PROP = "findSmartCase"; // NOI18N
    public static final String FIND_WHOLE_WORDS_PROP = "findWholeWords"; // NOI18N
    public static final String FIND_WRAP_SEARCH_PROP = "findWrapSearch"; // NOI18N
    public static final String FIND_BLOCK_SEARCH_PROP = "findBlockSearch"; // NOI18N    
    public static final String FONT_SIZE_PROP = "fontSize"; // NOI18N
    public static final String HIGHLIGHT_CARET_ROW_PROP = "highlightCaretRow"; // NOI18N
    public static final String HIGHLIGHT_MATCHING_BRACKET_PROP = "highlightMatchingBracket"; // NOI18N
    public static final String INDENT_ENGINE_PROP = "indentEngine"; // NOI18N
    public static final String KEY_BINDING_LIST_PROP = "keyBindingList"; // NOI18N
    public static final String LINE_HEIGHT_CORRECTION_PROP = "lineHeightCorrection"; // NOI18N
    public static final String LINE_NUMBER_VISIBLE_PROP = "lineNumberVisible"; // NOI18N
    public static final String MACRO_MAP_PROP = "macroMap"; // NOI18N
    public static final String MARGIN_PROP = "margin"; // NOI18N
    public static final String PAIR_CHARACTERS_COMPLETION = "pairCharactersCompletion"; // NOI18N
    public static final String SCROLL_FIND_INSETS_PROP = "scrollFindInsets"; // NOI18N
    public static final String SCROLL_JUMP_INSETS_PROP = "scrollJumpInsets"; // NOI18N
    public static final String SPACES_PER_TAB_PROP = "spacesPerTab"; // NOI18N
    public static final String STATUS_BAR_CARET_DELAY_PROP = "statusBarCaretDelay"; // NOI18N
    public static final String STATUS_BAR_VISIBLE_PROP = "statusBarVisible"; // NOI18N
    public static final String TAB_SIZE_PROP = "tabSize"; // NOI18N
    public static final String TEXT_LIMIT_LINE_COLOR_PROP = "textLimitLineColor"; // NOI18N
    public static final String TEXT_LIMIT_LINE_VISIBLE_PROP = "textLimitLineVisible"; // NOI18N
    public static final String TEXT_LIMIT_WIDTH_PROP = "textLimitWidth"; // NOI18N
    public static final String TOOLBAR_VISIBLE_PROP = "toolbarVisible"; // NOI18N
    public static final String TEXT_ANTIALIASING_PROP = "textAntialiasing"; // NOI18N
    public static final String CODE_FOLDING_PROPS_PROP = "codeFoldingProps"; // NOI18N    
    
    protected static final String[] BASE_PROP_NAMES = {
        ABBREV_MAP_PROP,
        CARET_BLINK_RATE_PROP,
        CARET_COLOR_INSERT_MODE_PROP,
        CARET_COLOR_OVERWRITE_MODE_PROP,
        CARET_ITALIC_INSERT_MODE_PROP,
        CARET_ITALIC_OVERWRITE_MODE_PROP,
        CARET_TYPE_INSERT_MODE_PROP,
        CARET_TYPE_OVERWRITE_MODE_PROP,
        COLORING_MAP_PROP,
        EXPAND_TABS_PROP,
        FONT_SIZE_PROP,
        HIGHLIGHT_CARET_ROW_PROP,
        HIGHLIGHT_MATCHING_BRACKET_PROP,
        INDENT_ENGINE_PROP,
        KEY_BINDING_LIST_PROP,
        LINE_HEIGHT_CORRECTION_PROP,
        //LINE_NUMBER_VISIBLE_PROP,
        MACRO_MAP_PROP,
        MARGIN_PROP,
        PAIR_CHARACTERS_COMPLETION,
        SCROLL_FIND_INSETS_PROP,
        SCROLL_JUMP_INSETS_PROP,
        SPACES_PER_TAB_PROP,
        STATUS_BAR_CARET_DELAY_PROP,
        STATUS_BAR_VISIBLE_PROP,
        TAB_SIZE_PROP,
        TEXT_LIMIT_LINE_COLOR_PROP,
        TEXT_LIMIT_LINE_VISIBLE_PROP,
        TEXT_LIMIT_WIDTH_PROP,
        OPTIONS_VERSION_PROP
    };
    
    static final long serialVersionUID =-5469192431366914841L;
    
    private static final String NO_INDENT_ENGINE = "NO_INDENT_ENGINE"; // NOI18N
    
    /** Version of the options. It's used for patching the options. */
    private transient int optionsVersion;
    
    private transient MIMEOptionNode mimeNode;
    private transient MIMEOptionFolder settingsFolder;
    private transient boolean settingsFolderInitialization;
    
    /** Map of Kit to Options */
    private static final HashMap kitClass2Options = new HashMap();
    
    /** Code template expand key setting name */
    public static final String CODE_TEMPLATE_EXPAND_KEY = "code-template-expand-key"; // NOI18N

    // ------------------------------------------------------------------------
    // BaseOptions creation
    // ------------------------------------------------------------------------
    
    public BaseOptions() {
        this(BaseKit.class, BASE);
    }
    
    public BaseOptions(Class kitClass, String typeName) {
        super(kitClass, typeName);
        optionsVersion = LATEST_OPTIONS_VERSION;
        kitClass2Options.put(kitClass, this);
//        new Throwable("BaseOptions: " + getClass() + "; kitClass=" + kitClass + "; typeName=" + typeName).printStackTrace();
    }

    /**
     * Gets an instance of <code>BaseOptions</code> for an editir kit implementation
     * class. Please see description of <code>BaseKit.getKit(Class)</code> for
     * more details.
     * 
     * @param kitClass The editor kit implementation class to get <code>BaseOptions</code> for.
     * 
     * @return The <code>BaseOptions</code> or <code>null</code> if the options
     *   can't be found or loaded for some reason.
     * @deprecated Use <code>MimeLookup</code> to find <code>BaseOptions</code>
     *   instances for a mime type.
     * @see org.netbeans.editor.BaseKit#getKit(Class)
     */
    public static BaseOptions getOptions(Class kitClass) {
        BaseOptions options = null;

        if (kitClass != BaseKit.class && kitClass != NbEditorKit.class) {
            String mimeType = KitsTracker.getInstance().findMimeType(kitClass);
            if (mimeType != null) {
                Lookup lookup = MimeLookup.getLookup(MimePath.parse(mimeType));
                options = (BaseOptions) lookup.lookup(BaseOptions.class);
            }
        }
        
        // The old way of doing things
        if (options == null) {
            //check also all superclasses whether they are not in the 
            //kitClass2Options map
            Class c = kitClass;

            while (c != null) {
                options = (BaseOptions)kitClass2Options.get(c);
                if (options != null) 
                    break;

                AllOptionsFolder.getDefault().loadMIMEOption(c, false);
                options = (BaseOptions)kitClass2Options.get(c);
                if (options != null)
                    break;

                c = c.getSuperclass();
            }
        }
        
        return options;
    }

    // ------------------------------------------------------------------------
    // o.n.e.Settings initialization
    // ------------------------------------------------------------------------
    
    // ------------------------------------------------------------------------
    // Antialiasing
    // ------------------------------------------------------------------------
    
    public boolean isTextAntialiasing() {
        FontColorSettings fcs = MimeLookup.getLookup(getContentType()).lookup(FontColorSettings.class);
        Map<?, ?> hints = (Map<?, ?>) fcs.getFontColors(FontColorNames.DEFAULT_COLORING).getAttribute(EditorStyleConstants.RenderingHints);
        Object aa = hints.get(RenderingHints.KEY_TEXT_ANTIALIASING);
        return aa == RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
    }

    public void setTextAntialiasing(boolean textAntialiasing) {
        getPreferences().putBoolean(TEXT_ANTIALIASING_PROP, textAntialiasing);
    }
    
    // ------------------------------------------------------------------------
    // Code Templates (formerly abbreviations) related getters & setters
    // ------------------------------------------------------------------------
    
    /** Saves the keystroke of code tamplate expansion into properties.xml file under Editors/text/base */
    public static void setCodeTemplateExpandKey(KeyStroke ks) {
        Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        prefs.put(CODE_TEMPLATE_EXPAND_KEY, Utilities.keyToString(ks));
    }
    
    /** Gets Code Template Expand Key. Can return null if there is no key in the settings file */
    public static KeyStroke getCodeTemplateExpandKey(){
        Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        String ks = prefs.get(CODE_TEMPLATE_EXPAND_KEY, null);
        if (ks != null) {
            KeyStroke keyStroke = Utilities.stringToKey(ks);
            if (keyStroke != null) {
                return keyStroke;
            }
        }
        return KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
    }

    /** 
     * @return The same as <code>getAbbrevMap</code>.
     * @deprecated Use Editor Settings API instead.
     */
    public Map<String, String> getDefaultAbbrevMap(){
        return getAbbrevMap();
    }
    
    /**
     * @return All code templates available for this mime type.
     * @deprecated Use Editor Settings API instead.
     */
    public Map<String, String> getAbbrevMap() {
        // Use Settings so that registered initializer, filters and evaluators
        // still stand a chance.
        @SuppressWarnings("unchecked")
        Map<String, String> map = (Map<String, String>) Settings.getValue(
            getKitClass(), SettingsNames.ABBREV_MAP, true);
        return map;
    }
    
    /** 
     * Tries to update colorings for the mime type of this instance.
     * 
     * @param map The map with colorings.
     * @param saveToXML Ignored.
     * 
     * @deprecated Use Editor Settings Storage API instead.
     */
    public void setAbbrevMap(Map<String, String> map, boolean saveToXML) {
        throw new UnsupportedOperationException("Use Editor Settings Storage API instead"); //NOI18N
    }
    
    /** 
     * Calls <code>setAbbrevMap(map, true)</code>.
     * 
     * @deprecated Use Editor Settings Storage API instead.
     */
    public void setAbbrevMap(Map map) {
        setAbbrevMap(map, true);
    }
    
    // ------------------------------------------------------------------------
    // Keybindings related getters/setters
    // ------------------------------------------------------------------------
    
    /** 
     * @return The same keybindings as <code>getKeyBindingList</code>, but stored
     *   in a map.
     * @deprecated Use Editor Settings and Editor Settings Storage APIs instead.
     */
    public Map<String, MultiKeyBinding> getDefaultKeyBindingsMap(){
        return OptionUtilities.makeKeyBindingsMap(getKBList());
    }
    
    private List<? extends MultiKeyBinding> getKBList() {
        // Use Settings so that registered initializer, filters and evaluators
        // still stand a chance.
        @SuppressWarnings("unchecked")
        List<? extends MultiKeyBinding> list = (List<? extends MultiKeyBinding>) Settings.getValue(
            getKitClass(), SettingsNames.KEY_BINDING_LIST, true);
        return list;
    }
    
    /** 
     * @return The list of <code>MultiKeyBindings</code> available for this mime type.
     *   The first element in the list is the name of the kit class (ie. <code>String</code>).
     * @deprecated Use Editor Settings and Editor Settings Storage APIs instead.
     */
    public List getKeyBindingList() {
        List kb2 = new ArrayList(getKBList());
        kb2.add(0, getKitClass().getName()); //insert kit class name
        return kb2;
    }
    
    /** 
     * Sets new keybindings map and save the diff-ed changes to XML file. Calls
     * <code>setKeyBindingList(list, true)</code>.
     * 
     * @param list The list with <code>MultiKeyBinding</code>s.
     * 
     * @deprecated Use Editor Settings and Editor Settings Storage APIs instead.
     */
    public void setKeyBindingList(List list) {
        setKeyBindingList(list, true);
    }
    
    
    /** 
     * Saves keybindings settings to XML file. 
     * (This is used especially for record macro action.)
     * @deprecated Use Editor Settings and Editor Settings Storage APIs instead.
     */
    public void setKeyBindingsDiffMap(Map<String, MultiKeyBinding> diffMap) {
        throw new UnsupportedOperationException("Use Editor Settings Storage API instead"); //NOI18N
    }
    
    
    /** 
     * Sets new keybindings list to initializer map and if saveToXML is true,
     * then new settings will be saved to XML file.
     * 
     * @param list The list with <code>MultiKeyBinding</code>s.
     * @param saveToXML Ignored.
     * 
     * @deprecated Use Editor Settings and Editor Settings Storage APIs instead.
     */
    public void setKeyBindingList(List list, boolean saveToXML) {
        throw new UnsupportedOperationException("Use Editor Settings Storage API instead"); //NOI18N
    }
    
    // ------------------------------------------------------------------------
    // Fonts & Colors related getters/setters
    // ------------------------------------------------------------------------
    
    /**
     * Tries to gather all colorings defined for the mime type of this instance.
     * 
     * @return The map with all colorings defined the mime type of this instance.
     *   The returned map may be inaccurate, please use the new Editor Settings
     *   API and its <code>FontColorSettings</code> class.
     * 
     * @deprecated Use Editor Settings API instead.
     */
    public Map<String, Coloring> getColoringMap() {
        return new HashMap<String, Coloring>(ColoringMap.get(getContentType()).getMap());
    }
    
    /** 
     * Calls <code>setColoringMap(coloringMap, true)</code>.
     * 
     * @deprecated Use Editor Settings Storage API instead.
     */
    public void setColoringMap(Map coloringMap) {
        setColoringMap(coloringMap, true);
    }
    
    /** 
     * Tries to update colorings for the mime type of this instance.
     * 
     * @param coloringMap The map with colorings.
     * @param saveToXML Ignored.
     * 
     * @deprecated Use Editor Settings Storage API instead.
     */
    public void setColoringMap(final Map<String, Coloring> coloringMap, boolean saveToXML) {
        throw new UnsupportedOperationException("Use Editor Settings Storage API instead"); //NOI18N
    }
    
    /**
     * @deprecated Use Editor Settings API instead.
     */
    public int getFontSize() {
        Coloring dc = SettingsUtil.getColoring(getKitClass(), SettingsNames.DEFAULT_COLORING, false);
        return (dc != null) ? dc.getFont().getSize() : SettingsDefaults.defaultFont.getSize();
    }
  
    /**
     * Does nothing.
     * 
     * @deprecated Use Editor Settings Storage API instead.
     */
    public void setFontSize(final int size) {
        throw new UnsupportedOperationException("Use Editor Settings Storage API instead"); //NOI18N
    }
    
    public float getLineHeightCorrection() {
        return getPreferences().getFloat(SettingsNames.LINE_HEIGHT_CORRECTION, 1);
    }
    public void setLineHeightCorrection(float f) {
        if (f <= 0) {
            NbEditorUtilities.invalidArgument("MSG_OutOfRange"); // NOI18N
            return;
        }
        getPreferences().putFloat(SettingsNames.LINE_HEIGHT_CORRECTION, f);
    }
    
    // ------------------------------------------------------------------------
    // Editor macros settings
    // ------------------------------------------------------------------------
    
    /**
     * The same as <code>getMacroMap()</code>.
     * @deprecated Without any replacement.
     */
    public Map getDefaultMacrosMap(){
        return getMacroMap();
    }
  
    /** 
     * @return The map of macro names (<code>String</code>) and the associated macro code (<code>String</code>).
     *   The <code>null</code> key entry has value of <code>List&lt;? extends MultiKeyBinding&gt;</code>.
     * @deprecated Without any replacement.
     */
    @SuppressWarnings("unchecked")
    public Map getMacroMap() {
        Map<String, String> settingsMap = (Map<String, String>)super.getSettingValue(SettingsNames.MACRO_MAP);
        Map ret = (settingsMap == null) ? new HashMap() : new HashMap(settingsMap);
        ret.put(null, getKBList());
        return ret;
    }
    
    /** 
     * @deprecated Without any replacement.
     */
    public void setMacroDiffMap(Map diffMap){
        throw new UnsupportedOperationException("Use Editor Settings Storage API instead"); //NOI18N
    }
    
    /** 
     * Sets new macros.
     * 
     * <p>WARNING: This method no longer saves macro shortcuts. Use Editor Settings
     * Storage API to update shortcuts for your macros.
     * 
     * @param macros The map with macro names mapped to macro code.
     * @param saveToXML Ignored.
     * 
     * @deprecated Without any replacement.
     */
    public void setMacroMap(final Map macros, boolean saveToXML) {
        throw new UnsupportedOperationException("Use Editor Settings Storage API instead"); //NOI18N
    }
    
    /** 
     * Sets new macros. This method calls <code>setMacroMap(map, true)</code>.
     * @param macros The map with macro names mapped to macro code.
     * @deprecated Without any replacement.
     */
    public void setMacroMap(Map macros) {
        setMacroMap(macros, true);
    }
    
    // ------------------------------------------------------------------------
    // Miscellaneous getters & setters
    // ------------------------------------------------------------------------
    
    public int getTabSize() {
        return getSettingInteger(SettingsNames.TAB_SIZE);
    }
    public void setTabSize(int tabSize) {
        if (tabSize < 0) {
            NbEditorUtilities.invalidArgument("MSG_NegativeValue"); // NOI18N
            return;
        }
        setSettingInteger(SettingsNames.TAB_SIZE, tabSize, TAB_SIZE_PROP);
    }
    
/*    public boolean getExpandTabs() {
        return getSettingBoolean(SettingsNames.EXPAND_TABS);
    }
    [Mila] Moved to IndentEngine; Setter must stay here
 */
    
    public void setExpandTabs(boolean expandTabs) {
        setSettingBoolean(SettingsNames.EXPAND_TABS, expandTabs, EXPAND_TABS_PROP);
    }
    
/*    public int getSpacesPerTab() {
        return getSettingInteger(SettingsNames.SPACES_PER_TAB);
    }
    [Mila] Moved to IndentEngine; Setter must stay here
 */
    public void setSpacesPerTab(int i){
        if (i > 0)
            setSettingInteger(SettingsNames.SPACES_PER_TAB, i, SPACES_PER_TAB_PROP);
        else
            Toolkit.getDefaultToolkit().beep();
    }
    
    public String getCaretTypeInsertMode() {
        return getPreferences().get(SettingsNames.CARET_TYPE_INSERT_MODE, SettingsDefaults.defaultCaretTypeInsertMode);
    }
    public void setCaretTypeInsertMode(String type) {
        getPreferences().put(SettingsNames.CARET_TYPE_INSERT_MODE, type);
    }
    
    public String getCaretTypeOverwriteMode() {
        return getPreferences().get(SettingsNames.CARET_TYPE_OVERWRITE_MODE, SettingsDefaults.defaultCaretTypeOverwriteMode);
    }
    public void setCaretTypeOverwriteMode(String type) {
        getPreferences().put(SettingsNames.CARET_TYPE_OVERWRITE_MODE, type);
    }

    /**
     * @deprecated Since adaptation to new view implementation the option is not supported.
     */
    public boolean getCaretItalicInsertMode() {
        return false;
    }
    /**
     * @deprecated Since adaptation to new view implementation the option is not supported.
     */
    public void setCaretItalicInsertMode(boolean b) {
        throw new UnsupportedOperationException();
    }
    /**
     * @deprecated Since adaptation to new view implementation the option is not supported.
     */
    public boolean getCaretItalicOverwriteMode() {
        return false;
    }
    /**
     * @deprecated Since adaptation to new view implementation the option is not supported.
     */
    public void setCaretItalicOverwriteMode(boolean b) {
        throw new UnsupportedOperationException();
    }
    
    public Color getCaretColorInsertMode() {
        return (Color)super.getSettingValue(SettingsNames.CARET_COLOR_INSERT_MODE);
    }
    
    /** Sets new CaretColorInsertMode property to initializer map and save the
     *  changes to XML file */
    public void setCaretColorInsertMode(Color color) {
        setCaretColorInsertMode(color, true);
    }
    
    /** Sets new CaretColorInsertMode property to initializer map and if saveToXML is true,
     *  then new settings will be saved to XML file (fontsColors.xml). */
    public void setCaretColorInsertMode(Color color, boolean saveToXML) {
        super.setSettingValue(SettingsNames.CARET_COLOR_INSERT_MODE, color, CARET_COLOR_INSERT_MODE_PROP);
    }
    
    public Color getCaretColorOverwriteMode() {
        return (Color)super.getSettingValue(SettingsNames.CARET_COLOR_OVERWRITE_MODE);
    }
    
    /** Sets new CaretColorOverwriteMode property to initializer map and save the
     *  changes to XML file */
    public void setCaretColorOverwriteMode(Color color) {
        setCaretColorOverwriteMode(color, true);
    }
    
    /** Sets new CaretColorOverwriteMode property to initializer map and if saveToXML is true,
     *  then new settings will be saved to XML file (fontsColors.xml). */
    public void setCaretColorOverwriteMode(Color color, boolean saveToXML) {
        super.setSettingValue(SettingsNames.CARET_COLOR_OVERWRITE_MODE, color, CARET_COLOR_OVERWRITE_MODE_PROP);
    }
    
    public int getCaretBlinkRate() {
        return getSettingInteger(SettingsNames.CARET_BLINK_RATE);
    }
    public void setCaretBlinkRate(int rate) {
        if (rate < 0) {
            NbEditorUtilities.invalidArgument("MSG_NegativeValue"); // NOI18N
            return;
        }
        setSettingInteger(SettingsNames.CARET_BLINK_RATE, rate, CARET_BLINK_RATE_PROP);
    }
    
    public boolean getLineNumberVisible() {
        return getSettingBoolean(SettingsNames.LINE_NUMBER_VISIBLE);
    }
    public void setLineNumberVisible(boolean b) {
        setSettingBoolean(SettingsNames.LINE_NUMBER_VISIBLE, b, LINE_NUMBER_VISIBLE_PROP);
    }

    public Insets getScrollJumpInsets() {
        return (Insets)getSettingValue(SettingsNames.SCROLL_JUMP_INSETS);
    }
    public void setScrollJumpInsets(Insets i) {
        setSettingValue(SettingsNames.SCROLL_JUMP_INSETS, i, SCROLL_JUMP_INSETS_PROP);
    }
    
    public Insets getScrollFindInsets() {
        return (Insets)getSettingValue(SettingsNames.SCROLL_FIND_INSETS);
    }
    public void setScrollFindInsets(Insets i) {
        setSettingValue(SettingsNames.SCROLL_FIND_INSETS, i, SCROLL_FIND_INSETS_PROP);
    }

    public Insets getMargin() {
        return (Insets)getSettingValue(SettingsNames.MARGIN);
    }
    public void setMargin(Insets i) {
        setSettingValue(SettingsNames.MARGIN, i, MARGIN_PROP);
    }
    
    public boolean getStatusBarVisible() {
        return getSettingBoolean(SettingsNames.STATUS_BAR_VISIBLE);
    }
    public void setStatusBarVisible(boolean v) {
        setSettingBoolean(SettingsNames.STATUS_BAR_VISIBLE, v, STATUS_BAR_VISIBLE_PROP);
    }
    
    public int getStatusBarCaretDelay() {
        return getSettingInteger(SettingsNames.STATUS_BAR_CARET_DELAY);
    }
    public void setStatusBarCaretDelay(int delay) {
        if (delay < 0) {
            NbEditorUtilities.invalidArgument("MSG_NegativeValue"); // NOI18N
            return;
        }
        setSettingInteger(SettingsNames.STATUS_BAR_CARET_DELAY, delay, STATUS_BAR_CARET_DELAY_PROP);
    }
    
    public boolean getFindHighlightSearch() {
        return getSettingBoolean(SettingsNames.FIND_HIGHLIGHT_SEARCH);
    }
    
    public void setFindHighlightSearch(boolean b) {
        setSettingBoolean(SettingsNames.FIND_HIGHLIGHT_SEARCH, b, FIND_HIGHLIGHT_SEARCH_PROP);
    }

    public boolean getFindBlockSearch() {
        return getSettingBoolean(SettingsNames.FIND_BLOCK_SEARCH);
    }
    
    public void setFindBlockSearch(boolean b) {
        setSettingBoolean(SettingsNames.FIND_BLOCK_SEARCH, b, FIND_BLOCK_SEARCH_PROP);
    }
    
    public boolean getFindIncSearch() {
        return getSettingBoolean(SettingsNames.FIND_INC_SEARCH);
    }
    
    public void setFindIncSearch(boolean b) {
        setSettingBoolean(SettingsNames.FIND_INC_SEARCH, b, FIND_INC_SEARCH_PROP);
    }
    
    public int getFindIncSearchDelay() {
        return getSettingInteger(SettingsNames.FIND_INC_SEARCH_DELAY);
    }
    
    public void setFindIncSearchDelay(int delay) {
        if (delay < 0) {
            NbEditorUtilities.invalidArgument("MSG_NegativeValue"); // NOI18N
            return;
        }
        setSettingInteger(SettingsNames.FIND_INC_SEARCH_DELAY, delay, FIND_INC_SEARCH_DELAY_PROP);
    }
    
    public boolean getFindWrapSearch() {
        return getSettingBoolean(SettingsNames.FIND_WRAP_SEARCH);
    }
    
    public void setFindWrapSearch(boolean b) {
        setSettingBoolean(SettingsNames.FIND_WRAP_SEARCH, b, FIND_WRAP_SEARCH_PROP);
    }
    
    public boolean getFindSmartCase() {
        return getSettingBoolean(SettingsNames.FIND_SMART_CASE);
    }
    
    public void setFindSmartCase(boolean b) {
        setSettingBoolean(SettingsNames.FIND_SMART_CASE, b, FIND_SMART_CASE_PROP);
    }
    
    public Map getFindHistory() {
        return Collections.EMPTY_MAP;
    }
    
    public void setFindHistory(Map m) {
        throw new UnsupportedOperationException();
    }
    
    public int getFindHistorySize() {
        return getSettingInteger(SettingsNames.FIND_HISTORY_SIZE);
    }
    
    public void setFindHistorySize(int size) {
        setSettingInteger(SettingsNames.FIND_HISTORY_SIZE, size, FIND_HISTORY_SIZE_PROP);
    }
    
    public boolean getPairCharactersCompletion() {
      return getSettingBoolean(SettingsNames.PAIR_CHARACTERS_COMPLETION);
    }

    public void setPairCharactersCompletion(boolean v) {
        setSettingBoolean(SettingsNames.PAIR_CHARACTERS_COMPLETION, v, PAIR_CHARACTERS_COMPLETION);
    }

    public Color getTextLimitLineColor() {
        return (Color)super.getSettingValue(SettingsNames.TEXT_LIMIT_LINE_COLOR);
    }
    
    /** 
     * Sets new TextLimitLineColor property to initializer map and save the
     * changes to XML file
     * 
     * @deprecated Use Editor Settings Storage API instead.
     */
    public void setTextLimitLineColor(Color color) {
        setTextLimitLineColor(color, true);
    }
    
    /** 
     * Sets new TextLimitLineColor property to initializer map and if saveToXML is true,
     * then new settings will be saved to XML file (fontsColors.xml).
     * 
     * @deprecated Use Editor Settings Storage API instead.
     */
    public void setTextLimitLineColor(Color color , boolean saveToXML) {
        super.setSettingValue(SettingsNames.TEXT_LIMIT_LINE_COLOR, color, TEXT_LIMIT_LINE_COLOR_PROP);
    }
    
    public int getTextLimitWidth() {
        return getSettingInteger(SettingsNames.TEXT_LIMIT_WIDTH);
    }
    
    public void setTextLimitWidth(int width) {
        if (width <= 0) {
            NbEditorUtilities.invalidArgument("MSG_OutOfRange"); // NOI18N
            return;
        }
        setSettingInteger(SettingsNames.TEXT_LIMIT_WIDTH, width, TEXT_LIMIT_WIDTH_PROP);
    }
    
    public boolean getTextLimitLineVisible() {
        return getSettingBoolean(SettingsNames.TEXT_LIMIT_LINE_VISIBLE);
    }
    
    public void setTextLimitLineVisible(boolean visible) {
        setSettingBoolean(SettingsNames.TEXT_LIMIT_LINE_VISIBLE, visible, TEXT_LIMIT_LINE_VISIBLE_PROP);
    }
    
    public boolean getHighlightMatchingBracket() {
        return getSettingBoolean(ExtSettingsNames.HIGHLIGHT_MATCH_BRACE);
    }
    
    public void setHighlightMatchingBracket(boolean highlight) {
        setSettingBoolean(ExtSettingsNames.HIGHLIGHT_MATCH_BRACE, highlight, HIGHLIGHT_MATCHING_BRACKET_PROP);
    }
    
    public boolean getHighlightCaretRow() {
        return getSettingBoolean(ExtSettingsNames.HIGHLIGHT_CARET_ROW);
    }
    
    public void setHighlightCaretRow(boolean highlight) {
        setSettingBoolean(ExtSettingsNames.HIGHLIGHT_CARET_ROW, highlight, HIGHLIGHT_CARET_ROW_PROP);
    }

    public boolean isToolbarVisible() {
        return getSettingBoolean(TOOLBAR_VISIBLE_PROP);
    }
    
    public void setToolbarVisible(boolean toolbarVisible) {
        setSettingBoolean(TOOLBAR_VISIBLE_PROP, toolbarVisible, TOOLBAR_VISIBLE_PROP);
    }
    
    public Map<String, Boolean> getCodeFoldingProps(){
        Map<String, Boolean> map = new HashMap<String, Boolean>();
        
        Boolean val = (Boolean)getSettingValue(SettingsNames.CODE_FOLDING_ENABLE);
        map.put(SettingsNames.CODE_FOLDING_ENABLE, val);
        
        return map;
    }
    
    public void setCodeFoldingProps(Map props){
        String name = SettingsNames.CODE_FOLDING_ENABLE;
        setSettingValue(name, props.get(name));
    }
        
    /** 
     * Retrieves the actions from XML file.
     * @deprecated Use the layers, ie. Editors/<mime-type>/Popup.
     */
    public void initPopupMenuItems(){
    }
    
    // ------------------------------------------------------------------------
    // IndentEngines related stuff
    // ------------------------------------------------------------------------
    
    private static final String INDENT_ENGINE_HANDLE = "indent-engine-handle"; //NOI18N
    public IndentEngine getIndentEngine() {
        if (!BASE.equals(getTypeName())) {
            Preferences prefs = MimeLookup.getLookup(MimePath.parse(getContentType())).lookup(Preferences.class);
            String handle = prefs.get(INDENT_ENGINE_HANDLE, null);
            if (handle != null) {
                Object instance = null;

                if (handle.equals(NO_INDENT_ENGINE)) {
                    return IndentEngine.getDefault();
                }

                Lookup.Template tmp = new Lookup.Template(null, handle, null);
                Lookup.Item item = Lookup.getDefault().lookupItem(tmp);
                if (item != null) {
                    instance = item.getInstance();
                    if (instance instanceof IndentEngine) {
                        return (IndentEngine) instance;
                    }
                }
            }
        }

        // [BACKWARD-COMPATIBILITY-START]
        /* Either handle or real indent-egine is attempted
         * to be obtained from property.
         */
        Object o = getProperty(INDENT_ENGINE_PROP);
        if (o instanceof IndentEngine.Handle) {
            IndentEngine eng = (IndentEngine)((IndentEngine.Handle)o).getServiceType();
            if (eng != null) {
                setIndentEngine(eng);
                return eng;
            }

        } else if (o instanceof IndentEngine) {
            setIndentEngine((IndentEngine)o);
            return (IndentEngine)o;
        }
        // [BACKWARD-COMPATIBILITY-END]
                                       
        
        // Try to find the default indent engine in Services registry
        IndentEngine eng = findDefaultIndentEngine();
        if (eng != null) { // found
            setIndentEngine(eng);
        }
        
        return eng;
    }
    
    public void setIndentEngine(IndentEngine eng) {
        String id = null;
        if (eng != null) {
            Lookup.Template tmp = new Lookup.Template(null, null, eng);
            Lookup.Item item = Lookup.getDefault().lookupItem(tmp);
            if (item != null) {
                id = item.getId();
            }
        }

        if (!BASE.equals(getTypeName())){
            Preferences prefs = MimeLookup.getLookup(MimePath.parse(getContentType())).lookup(Preferences.class);
            if (id == null) {
                id = NO_INDENT_ENGINE;
            } 
            prefs.put(INDENT_ENGINE_HANDLE, id);
        }

        refreshIndentEngineSettings();
    }
    
    private void refreshIndentEngineSettings() {
        // Touches the settings
        Settings.update(new Runnable(){
            public void run(){
                Settings.touchValue(getKitClass(), NbEditorDocument.INDENT_ENGINE);
                Settings.touchValue(getKitClass(), NbEditorDocument.FORMATTER);
            }
            public boolean asynchronous() {
                return true;
            }
        });
    }
    
    /** Return class of the default indentation engine. */
    protected Class getDefaultIndentEngineClass() {
        return SimpleIndentEngine.class;
    }
    
    private IndentEngine findDefaultIndentEngine() {
        if (getDefaultIndentEngineClass() != null) {
            return (IndentEngine) Lookup.getDefault().lookup(getDefaultIndentEngineClass());
        }
        
        return null;
    }
    
    // ------------------------------------------------------------------------
    // Options versioning
    // ------------------------------------------------------------------------
    
    /**
     * @deprecated Without any replacement.
     */
    public void setOptionsVersion(int optionsVersion) {
        int oldOptionsVersion = this.optionsVersion;
        this.optionsVersion = optionsVersion;

        if (optionsVersion != oldOptionsVersion) {
            firePropertyChange(OPTIONS_VERSION_PROP, new Integer(oldOptionsVersion), new Integer(optionsVersion));
        }
    }

    /**
     * @deprecated Without any replacement.
     */
    public int getOptionsVersion() {
        return optionsVersion;
    }
    
    /** 
     * Upgrade the deserialized options.
     * @param version deserialized version of the options
     * @param latestVersion latest version of the options
     *   that will be set to them after they are upgraded
     * @deprecated Without any replacement. This method is never called.
     */
    protected void upgradeOptions(int version, int latestVersion) {
        // Upgrade in separate class to avoid messing up BaseOptions
        //UpgradeOptions.upgradeOptions(this, version, latestVersion);
    }
    
    // ------------------------------------------------------------------------
    // XML-ization & MIME* stuff
    // ------------------------------------------------------------------------
    
    /** 
     * @return The <code>MIMEOptionFolder</code> instance for the mime type of
     *   this <code>BaseOptions</code> instance.
     * @deprecated Use Editor Settings Storage API.
     */
    protected MIMEOptionFolder getMIMEFolder() {
        /* #25541 - Instead of synchronized getMIMEFolder() the kit
         * is first obtained and then the synchronization is done
         * to avoid the deadlock caused by locking in opposite order.
         */
        String name = getCTImpl();
        if (name == null) {
            return null;
        }

        synchronized (Settings.class) {
            if (settingsFolderInitialization) {
                return null;
            }
            settingsFolderInitialization = true;
            try {
                // return already initialized folder
                if (settingsFolder != null) {
                    return settingsFolder;
                }

                FileObject f = FileUtil.getConfigFile(AllOptionsFolder.FOLDER + "/" + name); //NOI18N

                // MIME folder doesn't exist, let's create it
                if (f == null) {
                    FileObject fo = FileUtil.getConfigFile(AllOptionsFolder.FOLDER);
                    if (fo != null) {
                        try {
                            FileUtil.createFolder(fo, name);
                        } catch (IOException ioe) {
                            LOG.log(Level.WARNING, null, ioe);
                        }

                        f = FileUtil.getConfigFile(AllOptionsFolder.FOLDER + "/" + name); // NOI18N
                    }
                }

                if (f != null) {
                    try {
                        DataObject d = DataObject.find(f);
                        DataFolder df = (DataFolder) d.getCookie(DataFolder.class);
                        if (df != null) {
                            settingsFolder = new MIMEOptionFolder(df, this);
                            return settingsFolder;
                        }
                    } catch (org.openide.loaders.DataObjectNotFoundException ex) {
                        LOG.log(Level.WARNING, null, ex);
                    }
                }

                return null;
            } finally {
                settingsFolderInitialization = false;
            }
        }
    }
    
    /** Gets MIMEOptionNode that belongs to this bean */
    public MIMEOptionNode getMimeNode() {
        synchronized (Settings.class) {
            if (mimeNode == null) {
                createMIMENode(getTypeName());
            }
            return mimeNode;
        }
    }
    
    /** Creates Node in global options for appropriate MIME type */
    private void createMIMENode(String typeName) {
        if (typeName.equals(BASE)) {
            return;
        }
        try {
            mimeNode = new MIMEOptionNode(this);
        } catch (IntrospectionException ie) {
            LOG.log(Level.WARNING, null, ie);
        }
    }
    
    /** 
     * Load all available settings from XML files and initialize them.
     * @deprecated Use Editor Settings Storage API instead. This method is never called.
     */
    protected void loadXMLSettings() {
    }

    // ------------------------------------------------------------------------
    // Serialization, Externalization, etc
    // ------------------------------------------------------------------------
    
    /** 
     * @deprecated Use Editor Settings Storage API instead. BaseOptions are no longer serialized.
     */
    public @Override void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    }
    
    /** 
     * Overriden writeExternal method. 
     * @deprecated Use Editor Settings Storage API instead. BaseOptions are no longer serialized.
     */
    public void writeExternal() throws IOException{
    }
    
    /** 
     * Overriden writeExternal method. 
     * @deprecated Use Editor Settings Storage API instead. BaseOptions are no longer serialized.
     */
    public @Override void writeExternal(ObjectOutput out) throws IOException{
    }

    /**
     * @deprecated Use Editor Settings Storage API instead. BaseOptions are no longer serialized.
     */
    protected @Override void firePropertyChange(String name, Object oldValue, Object newValue){
        // ignore firing... Quick fix of #47261. 
        // BaseOptions should be rewritten to not extend SystemOption ...
        // there is no need to be compatile with NB 3.2 and deserialize its options...
    }
}

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.options;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.io.IOException;
import java.io.ObjectInput;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Enumeration;
import javax.swing.text.JTextComponent;
import java.awt.Toolkit;

import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsDefaults;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.MultiKeyBinding;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.netbeans.editor.ext.ExtSettingsDefaults;
import org.netbeans.editor.ext.ExtKit;

import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.FormatterIndentEngine;
import org.netbeans.modules.editor.IndentEngineFormatter;
import org.netbeans.modules.editor.SimpleIndentEngine;

import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.text.IndentEngine;
import org.openide.ServiceType;
import org.openide.TopManager;

/**
* Options for the base editor kit
*
* @author Miloslav Metelka
* @version 1.00
*/
public class BaseOptions extends OptionSupport {
    
    /** Latest version of the options. It must be increased
     * manually when new patch is added to the options.
     */
    protected static final int LATEST_OPTIONS_VERSION = 1;

    protected static final String OPTIONS_VERSION_PROP = "optionsVersion";

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
    public static final String FONT_SIZE_PROP = "fontSize"; // NOI18N
    public static final String HIGHLIGHT_CARET_ROW_PROP = "highlightCaretRow"; // NOI18N
    public static final String HIGHLIGHT_MATCHING_BRACKET_PROP = "highlightMatchingBracket"; // NOI18N
    public static final String INDENT_ENGINE_PROP = "indentEngine"; // NOI18N
    public static final String KEY_BINDING_LIST_PROP = "keyBindingList"; // NOI18N
    public static final String LINE_HEIGHT_CORRECTION_PROP = "lineHeightCorrection"; // NOI18N
    public static final String LINE_NUMBER_MARGIN_PROP = "lineNumberMargin"; // NOI18N
    public static final String LINE_NUMBER_VISIBLE_PROP = "lineNumberVisible"; // NOI18N
    public static final String MACRO_MAP_PROP = "macroMap"; // NOI18N
    public static final String MARGIN_PROP = "margin"; // NOI18N
    public static final String SCROLL_FIND_INSETS_PROP = "scrollFindInsets"; // NOI18N
    public static final String SCROLL_JUMP_INSETS_PROP = "scrollJumpInsets"; // NOI18N
    public static final String SPACES_PER_TAB_PROP = "spacesPerTab"; // NOI18N
    public static final String STATUS_BAR_CARET_DELAY_PROP = "statusBarCaretDelay"; // NOI18N
    public static final String STATUS_BAR_VISIBLE_PROP = "statusBarVisible"; // NOI18N
    public static final String TAB_SIZE_PROP = "tabSize"; // NOI18N
    public static final String TEXT_LIMIT_LINE_COLOR_PROP = "textLimitLineColor"; // NOI18N
    public static final String TEXT_LIMIT_LINE_VISIBLE_PROP = "textLimitLineVisible"; // NOI18N
    public static final String TEXT_LIMIT_WIDTH_PROP = "textLimitWidth"; // NOI18N

    static final String[] BASE_PROP_NAMES = {
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
        LINE_NUMBER_MARGIN_PROP,
        LINE_NUMBER_VISIBLE_PROP,
        MACRO_MAP_PROP,
        MARGIN_PROP,
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

    private static final String HELP_ID = "editing.global"; // !!! NOI18N

    /** Whether formatting debug messages should be displayed */
    private static final boolean debugFormat
        = Boolean.getBoolean("netbeans.debug.editor.format"); // NOI18N
    
    private transient Settings.Initializer coloringMapInitializer;
    
    /** Version of the options. It's used for patching the options. */
    private transient int optionsVersion;
    
    /* Indent engine available during readExternal() */
    private transient IndentEngine readExternalIndentEngine;
    private transient boolean inReadExternal;
    
    public BaseOptions() {
        this(BaseKit.class, BASE);
    }

    public BaseOptions(Class kitClass, String typeName) {
        super(kitClass, typeName);
        
        // Create evaluators for indentEngine and formatter
        setSettingValue(NbEditorDocument.INDENT_ENGINE,
            new Settings.Evaluator() {
                public Object getValue(Class kitClass2, String settingName) {
                        return getIndentEngine();
                }
            },
            null
        );

        setSettingValue(NbEditorDocument.FORMATTER,
            new Settings.Evaluator() {
                public Object getValue(Class kitClass2, String settingName) {
                    IndentEngine eng = getIndentEngine();
                    return (eng != null)
                        ? ((eng instanceof FormatterIndentEngine)
                            ? ((FormatterIndentEngine)eng).getFormatter()
                            : ((Formatter)new IndentEngineFormatter(getKitClass(), eng)))
                        : null;
                }
            },
            null
        );

    }
    
    protected void updateSettingsMap(Class kitClass, Map settingsMap) {
        super.updateSettingsMap(kitClass, settingsMap);

        if (coloringMapInitializer != null) {
            coloringMapInitializer.updateSettingsMap(kitClass, settingsMap);
        }
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (HELP_ID);
    }

    public int getTabSize() {
        return getSettingInteger(SettingsNames.TAB_SIZE);
    }
    public void setTabSize(int tabSize) {
        if (tabSize > 0)
            setSettingInteger(SettingsNames.TAB_SIZE, tabSize, TAB_SIZE_PROP);
        else
            Toolkit.getDefaultToolkit().beep();
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

    public Map getAbbrevMap() {
        return new HashMap((Map)getSettingValue(SettingsNames.ABBREV_MAP) );
    }

    public void setAbbrevMap(Map map) {
        setSettingValue(SettingsNames.ABBREV_MAP, map, ABBREV_MAP_PROP);
    }

    public String getCaretTypeInsertMode() {
        return (String)getSettingValue(SettingsNames.CARET_TYPE_INSERT_MODE);
    }
    public void setCaretTypeInsertMode(String type) {
        setSettingValue(SettingsNames.CARET_TYPE_INSERT_MODE, type,
            CARET_TYPE_INSERT_MODE_PROP);
    }

    public String getCaretTypeOverwriteMode() {
        return (String)getSettingValue(SettingsNames.CARET_TYPE_OVERWRITE_MODE);
    }
    public void setCaretTypeOverwriteMode(String type) {
        setSettingValue(SettingsNames.CARET_TYPE_OVERWRITE_MODE, type,
            CARET_TYPE_OVERWRITE_MODE_PROP);
    }

    public boolean getCaretItalicInsertMode() {
        return getSettingBoolean(SettingsNames.CARET_ITALIC_INSERT_MODE);
    }
    public void setCaretItalicInsertMode(boolean b) {
        setSettingBoolean(SettingsNames.CARET_ITALIC_INSERT_MODE, b,
            CARET_ITALIC_INSERT_MODE_PROP);
    }

    public boolean getCaretItalicOverwriteMode() {
        return getSettingBoolean(SettingsNames.CARET_ITALIC_OVERWRITE_MODE);
    }
    public void setCaretItalicOverwriteMode(boolean b) {
        setSettingBoolean(SettingsNames.CARET_ITALIC_OVERWRITE_MODE, b,
            CARET_ITALIC_OVERWRITE_MODE_PROP);
    }

    public Color getCaretColorInsertMode() {
        return (Color)getSettingValue(SettingsNames.CARET_COLOR_INSERT_MODE);
    }
    public void setCaretColorInsertMode(Color color) {
        setSettingValue(SettingsNames.CARET_COLOR_INSERT_MODE, color,
            CARET_COLOR_INSERT_MODE_PROP);
    }

    public Color getCaretColorOverwriteMode() {
        return (Color)getSettingValue(SettingsNames.CARET_COLOR_OVERWRITE_MODE);
    }
    public void setCaretColorOverwriteMode(Color color) {
        setSettingValue(SettingsNames.CARET_COLOR_OVERWRITE_MODE, color,
            CARET_COLOR_OVERWRITE_MODE_PROP);
    }

    public int getCaretBlinkRate() {
        return getSettingInteger(SettingsNames.CARET_BLINK_RATE);
    }
    public void setCaretBlinkRate(int rate) {
        setSettingInteger(SettingsNames.CARET_BLINK_RATE, rate,
            CARET_BLINK_RATE_PROP);
    }

    public boolean getLineNumberVisible() {
        return getSettingBoolean(SettingsNames.LINE_NUMBER_VISIBLE);
    }
    public void setLineNumberVisible(boolean b) {
        setSettingBoolean(SettingsNames.LINE_NUMBER_VISIBLE, b,
            LINE_NUMBER_VISIBLE_PROP);
    }

    public Insets getScrollJumpInsets() {
        return (Insets)getSettingValue(SettingsNames.SCROLL_JUMP_INSETS);
    }
    public void setScrollJumpInsets(Insets i) {
        setSettingValue(SettingsNames.SCROLL_JUMP_INSETS, i,
            SCROLL_JUMP_INSETS_PROP);
    }

    public Insets getScrollFindInsets() {
        return (Insets)getSettingValue(SettingsNames.SCROLL_FIND_INSETS);
    }
    public void setScrollFindInsets(Insets i) {
        setSettingValue(SettingsNames.SCROLL_FIND_INSETS, i,
            SCROLL_FIND_INSETS_PROP);
    }

    public List getKeyBindingList() {
        Class kitClass = getKitClass();
        Settings.KitAndValue[] kav = getSettingValueHierarchy(SettingsNames.KEY_BINDING_LIST);
        List kbList = null;
        for (int i = 0; i < kav.length; i++) {
            if (kav[i].kitClass == kitClass) {
                kbList = (List)kav[i].value;
            }
        }
        if (kbList == null) {
            kbList = new ArrayList();
        }

        // must convert all members to serializable MultiKeyBinding
        int cnt = kbList.size();
        for (int i = 0; i < cnt; i++) {
            Object o = kbList.get(i);
            if (!(o instanceof MultiKeyBinding) && o != null) {
                JTextComponent.KeyBinding b = (JTextComponent.KeyBinding)o;
                kbList.set(i, new MultiKeyBinding(b.key, b.actionName));
            }
        }

        List kb2 = new ArrayList( kbList );
        kb2.add( 0, kitClass.getName() ); //insert kit class name
        return kb2;
    }

    public void setKeyBindingList(List list) {
        if( list.size() > 0 &&
            ( list.get( 0 ) instanceof Class || list.get( 0 ) instanceof String )
        ) {         
            list.remove( 0 ); //remove kit class name
        }

        /* Patch for the older projects, where the ExtKit actions
         * were not added to the map so we need to add them manually.
         */
        if (getKitClass() == BaseKit.class) {
            int i;
            for (i = list.size() - 1; i >= 0; i--) {
                MultiKeyBinding mkb = (MultiKeyBinding)list.get(i);
                if (ExtKit.completionShowAction.equals(mkb.actionName)) {
                    break;
                }
            }
            
            if (i < 0) {
                list.addAll(Arrays.asList(ExtSettingsDefaults.defaultExtKeyBindings));
            }
        }
        
        setSettingValue(SettingsNames.KEY_BINDING_LIST, list, KEY_BINDING_LIST_PROP);
    }

    public Map getColoringMap() {
        Map cm = new HashMap( SettingsUtil.getColoringMap(getKitClass(), false, true) ); // !!! !evaluateEvaluators
        cm.put(null, getKitClass().getName() ); // add kit class
        return cm;
    }

    public void setColoringMap(Map coloringMap) {
        if (coloringMap != null) {
            if (inReadExternal) {
                /* Fix of #11115
                 * The better place would be in upgradeOptions()
                 * which was attempted originally. However the normal
                 * behavior of setColoringMap() destroys the colorings
                 * if they are not upgraded immediately. Therefore
                 * the readExternalColoringMap approach was attempted.
                 * However there was an NPE in
                 * properties.syntax.EditorSettingsCopy.updateColors
                 * at line 235 the keyColoring was null.
                 * Therefore the patch appears here.
                 */
                coloringMap = UpgradeOptions.patchColorings(getKitClass(), coloringMap);
            }

            coloringMap.remove(null); // remove kit class
            SettingsUtil.setColoringMap( getKitClass(), coloringMap, false );
            
            coloringMapInitializer = SettingsUtil.getColoringMapInitializer(
                getKitClass(), coloringMap, false,
                getTypeName() + "-coloring-map-initializer"
            );
                
            
            firePropertyChange(COLORING_MAP_PROP, null, null);
        }
    }

    public int getFontSize() {
        Coloring dc = SettingsUtil.getColoring(getKitClass(), SettingsNames.DEFAULT_COLORING, false);
        return (dc != null) ? dc.getFont().getSize() : SettingsDefaults.defaultFont.getSize();
    }

    public void setFontSize(final int size) {
        final int oldSize = getFontSize();
        Map cm = SettingsUtil.getColoringMap(getKitClass(), false, true); // !!! !evaluateEvaluators
        if (cm != null) {
            Iterator it = cm.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry)it.next();
                Object value = entry.getValue();
                if (value instanceof Coloring) {
                    Coloring c = (Coloring)value;
                    Font font = c.getFont();
                    if (font != null && font.getSize() != size) {
                        font = font.deriveFont((float)size);
                        Coloring newColoring = new Coloring(font, c.getFontMode(),
                            c.getForeColor(), c.getBackColor()); // this way due to bug in Coloring
                        entry.setValue(newColoring);
                    }
                }
            }
            setColoringMap(cm);

            firePropertyChange(FONT_SIZE_PROP, null, null);
        }
    }

    public float getLineHeightCorrection() {
        return ((Float) getSettingValue(SettingsNames.LINE_HEIGHT_CORRECTION)).floatValue();
    }
    public void setLineHeightCorrection(float f) {
        setSettingValue(SettingsNames.LINE_HEIGHT_CORRECTION, new Float(f),
            LINE_HEIGHT_CORRECTION_PROP);
    }

    public Map getMacroMap() {
        return new HashMap( (Map)getSettingValue(SettingsNames.MACRO_MAP) );
    }

    public void setMacroMap(Map map) {
        setSettingValue(SettingsNames.MACRO_MAP, map);
    }

    public Insets getMargin() {
        return (Insets)getSettingValue(SettingsNames.MARGIN);
    }
    public void setMargin(Insets i) {
        setSettingValue(SettingsNames.MARGIN, i, MARGIN_PROP);
    }

    public Insets getLineNumberMargin() {
        return (Insets)getSettingValue(SettingsNames.LINE_NUMBER_MARGIN);
    }
    public void setLineNumberMargin(Insets i) {
        setSettingValue(SettingsNames.LINE_NUMBER_MARGIN, i, LINE_NUMBER_MARGIN_PROP);
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
        setSettingInteger(SettingsNames.STATUS_BAR_CARET_DELAY, delay,
            STATUS_BAR_CARET_DELAY_PROP);
    }

    public boolean getFindHighlightSearch() {
        return getSettingBoolean(SettingsNames.FIND_HIGHLIGHT_SEARCH);
    }

    public void setFindHighlightSearch(boolean b) {
        setSettingBoolean(SettingsNames.FIND_HIGHLIGHT_SEARCH, b,
            FIND_HIGHLIGHT_SEARCH_PROP);
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
        setSettingInteger(SettingsNames.FIND_INC_SEARCH_DELAY, delay,
            FIND_INC_SEARCH_DELAY_PROP);
    }

    public boolean getFindWrapSearch() {
        return getSettingBoolean(SettingsNames.FIND_WRAP_SEARCH);
    }

    public void setFindWrapSearch(boolean b) {
        setSettingBoolean(SettingsNames.FIND_WRAP_SEARCH, b,
            FIND_WRAP_SEARCH_PROP);
    }

    public boolean getFindSmartCase() {
        return getSettingBoolean(SettingsNames.FIND_SMART_CASE);
    }

    public void setFindSmartCase(boolean b) {
        setSettingBoolean(SettingsNames.FIND_SMART_CASE, b, FIND_SMART_CASE_PROP);
    }

    public Map getFindHistory() {
        return new HashMap( (Map)getSettingValue(SettingsNames.FIND_HISTORY) );
    }

    public void setFindHistory(Map m) {
        setSettingValue(SettingsNames.FIND_HISTORY, m, FIND_HISTORY_PROP);
    }

    public int getFindHistorySize() {
        return getSettingInteger(SettingsNames.FIND_HISTORY_SIZE);
    }

    public void setFindHistorySize(int size) {
        setSettingInteger(SettingsNames.FIND_HISTORY_SIZE, size,
            FIND_HISTORY_SIZE_PROP);
    }

    public Color getTextLimitLineColor() {
        return (Color)getSettingValue(SettingsNames.TEXT_LIMIT_LINE_COLOR);
    }

    public void setTextLimitLineColor(Color color) {
        setSettingValue(SettingsNames.TEXT_LIMIT_LINE_COLOR, color,
            TEXT_LIMIT_LINE_COLOR_PROP);
    }

    public int getTextLimitWidth() {
        return getSettingInteger(SettingsNames.TEXT_LIMIT_WIDTH);
    }

    public void setTextLimitWidth(int width) {
        setSettingInteger(SettingsNames.TEXT_LIMIT_WIDTH, width,
            TEXT_LIMIT_WIDTH_PROP);
    }

    public boolean getTextLimitLineVisible() {
        return getSettingBoolean(SettingsNames.TEXT_LIMIT_LINE_VISIBLE);
    }

    public void setTextLimitLineVisible(boolean visible) {
        setSettingBoolean(SettingsNames.TEXT_LIMIT_LINE_VISIBLE, visible,
            TEXT_LIMIT_LINE_VISIBLE_PROP);
    }

    public boolean getHighlightMatchingBracket() {
        return getSettingBoolean(ExtSettingsNames.HIGHLIGHT_MATCH_BRACE);
    }

    public void setHighlightMatchingBracket(boolean highlight) {
        setSettingBoolean(ExtSettingsNames.HIGHLIGHT_MATCH_BRACE, highlight,
            HIGHLIGHT_MATCHING_BRACKET_PROP);
    }

    public boolean getHighlightCaretRow() {
        return getSettingBoolean(ExtSettingsNames.HIGHLIGHT_CARET_ROW);
    }

    public void setHighlightCaretRow(boolean highlight) {
        setSettingBoolean(ExtSettingsNames.HIGHLIGHT_CARET_ROW, highlight,
            HIGHLIGHT_CARET_ROW_PROP);
    }


    public IndentEngine getIndentEngine() {
        // Due to #11212
        if (inReadExternal) {
            return readExternalIndentEngine;
        }

        ServiceType.Handle h = (ServiceType.Handle)getProperty(INDENT_ENGINE_PROP);
        IndentEngine eng;
        if (h != null) { // handle already set
            eng = (IndentEngine)h.getServiceType();
            
        } else { // handle not yet set
            // Try to find the default indent engine in Services registry
            eng = findDefaultIndentEngine();
        
            if (eng != null) { // found
                setIndentEngine(eng);
            }
        }
        
        return eng;
    }
    
    public void setIndentEngine(IndentEngine eng) {
        /* Disabled direct setting of the engine
         * during project deserialization to avoid doubled
         * indent engine as described in #9687
         */
        if (!isReadExternal()) {
            // To force serialization of the handle instead of service type
            putProperty(INDENT_ENGINE_PROP, new ServiceType.Handle(eng), false);
            
            refreshIndentEngineSettings();
        }
    }

    private void refreshIndentEngineSettings() {
        // Touches the settings
        Settings.touchValue(getKitClass(), NbEditorDocument.INDENT_ENGINE);
        Settings.touchValue(getKitClass(), NbEditorDocument.FORMATTER);
    }

    /** Return class of the default indentation engine. */
    protected Class getDefaultIndentEngineClass() {
        return SimpleIndentEngine.class;
    }

    private IndentEngine findDefaultIndentEngine() {
        if (getDefaultIndentEngineClass() != null) {
            ServiceType.Registry sr = TopManager.getDefault().getServices();
            Enumeration en = sr.services(getDefaultIndentEngineClass());
            if (en.hasMoreElements()) {
                return (IndentEngine)en.nextElement();
            }
        }
        
        return null;
    }

    public void setOptionsVersion(int optionsVersion) {
        int oldOptionsVersion = this.optionsVersion;
        this.optionsVersion = optionsVersion;
        if (optionsVersion != oldOptionsVersion) {
            firePropertyChange(OPTIONS_VERSION_PROP,
                new Integer(oldOptionsVersion), new Integer(optionsVersion));
        }
    }
    
    public int getOptionsVersion() {
        return optionsVersion;
    }
    
    public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException {

        /** Hold the current indent engine due to #11212 */
        readExternalIndentEngine = getIndentEngine();
        inReadExternal = true;

        /* Make the current options version to be zero
         * temporarily to distinguish whether the options
         * imported were old and the setOptionsVersion()
         * was not called or whether the options
         * were new so the options version was set
         * to the LATEST_OPTIONS_VERSION value.
         */
        optionsVersion = 0;
        
        // Read the serialized options
        super.readExternal(in);
        
        // Make sure the indent engine settings are propagated
        // (SharedClassObject.putProperty() is final)
        refreshIndentEngineSettings();

        // Possibly upgrade the options
        if (optionsVersion < LATEST_OPTIONS_VERSION) {
            upgradeOptions(optionsVersion, LATEST_OPTIONS_VERSION);
        }
        
        optionsVersion = LATEST_OPTIONS_VERSION;

        /** Release temp indent engine -  #11212 */
        inReadExternal = false;
        readExternalIndentEngine = null;
    }
    
    /** Upgrade the deserialized options.
     * @param version deserialized version of the options
     * @param latestVersion latest version of the options
     *   that will be set to them after they are upgraded
     */
    protected void upgradeOptions(int version, int latestVersion) {
        // Upgrade in separate class to avoid messing up BaseOptions
        UpgradeOptions.upgradeOptions(this, version, latestVersion);
    }
        
}

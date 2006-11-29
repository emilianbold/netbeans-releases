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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.options;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.io.IOException;
import java.io.ObjectInput;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Enumeration;
import javax.swing.text.AttributeSet;
import javax.swing.text.JTextComponent;
import java.awt.Toolkit;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.editor.settings.KeyBindingSettings;

import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsDefaults;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.MultiKeyBinding;
import org.netbeans.editor.ext.ExtSettingsNames;

import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.FormatterIndentEngine;
import org.netbeans.modules.editor.IndentEngineFormatter;
import org.netbeans.modules.editor.SimpleIndentEngine;

import org.openide.text.IndentEngine;
import java.beans.IntrospectionException;
import org.openide.loaders.DataObject;
import java.io.ObjectOutput;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.loaders.DataFolder;
import org.openide.filesystems.FileObject;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import org.openide.util.Lookup;
import java.util.StringTokenizer;
import org.netbeans.modules.editor.NbEditorUtilities;
import java.util.Set;
import java.util.HashSet;
import java.awt.RenderingHints;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.editor.NbEditorKit;
import org.openide.filesystems.Repository;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;



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
    
    /** Latest version of the options. It must be increased
     * manually when new patch is added to the options.
     */
    protected static final int LATEST_OPTIONS_VERSION = 21;
    
    protected static final String OPTIONS_VERSION_PROP = "optionsVersion"; // NOI18N
    
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
    
    //private static final String HELP_ID = "editing.global"; // !!! NOI18N
    private static final String NO_INDENT_ENGINE = "NO_INDENT_ENGINE"; // NOI18N
    
    private transient Settings.Initializer coloringMapInitializer;
    
    /** Version of the options. It's used for patching the options. */
    private transient int optionsVersion;
    
    /* Indent engine available during readExternal() */
    private transient IndentEngine readExternalIndentEngine;
    private transient boolean inReadExternal;
    
    private transient MIMEOptionNode mimeNode;
    private transient Map defaultAbbrevsMap;
    private transient Map defaultMacrosMap;
    private transient Map defaultKeyBindingsMap;
    private transient MIMEOptionFolder settingsFolder;
    private transient Boolean usingNewOptions = null;
    private transient FontColorSettings fontColorSettings;    
    private transient KeyBindingSettings keyBindingsSettings;    
    private transient LookupListener lookupListener;
    
    private transient boolean coloringsInitialized = false;
    private transient boolean keybindingsInitialized = false;
    
    /** Map of Kit to Options */
    private static final HashMap kitClass2Options = new HashMap();
    
    /** Code template expand key setting name */
    public static final String CODE_TEMPLATE_EXPAND_KEY = "code-template-expand-key"; // NOI18N

    private Lookup.Result resultKB;
    private LookupListener weakLookupListenerKB;
    private LookupListener lookupListenerKB;
    
    public BaseOptions() {
        this(BaseKit.class, BASE);
        optionsVersion = LATEST_OPTIONS_VERSION;
    }
    
    public BaseOptions(Class kitClass, String typeName) {
        super(kitClass, typeName);
        kitClass2Options.put(kitClass, this);

        // Hook up the settings initializer
        Settings.Initializer si = getSettingsInitializer();
        Settings.removeInitializer(si.getName());
        Settings.addInitializer(si, Settings.OPTION_LEVEL);
    }
    
    public boolean usesNewOptionsDialog() {
        if (usingNewOptions == null) {
            boolean b = false;
            if (!BASE.equals(getTypeName())){
                FontColorSettings fcs = getFontColorSettings();
                if (fcs != null){
                    AttributeSet as = fcs.getTokenFontColors(FontColorNames.DEFAULT_COLORING);
                    if (as !=null){
                        b = true;
                    }
                }
            }
            
            usingNewOptions = b ? Boolean.TRUE : Boolean.FALSE;
        }
        
        return usingNewOptions.booleanValue();
    }

    protected String getContentType(){
        BaseKit kit = BaseKit.getKit(getKitClass());
        return kit.getContentType();
    }
    
    public static BaseOptions getOptions(Class kitClass) {
        BaseOptions options = null;

        if (kitClass != BaseKit.class && kitClass != NbEditorKit.class) {
            // XXX: improve this by implementing BaseKit.guessMimeTypeFromKitClass
            // it can lookup a kit class in all registered mime types and collect
            // mime types where the kit class matches the one we are looking for
            // this way there would be no need for kitClass.newInstance() ...
            BaseKit kit = BaseKit.getKit(kitClass);
            if (kit != null) {
                String mimeType = kit.getContentType();
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

    /** Listening for Settings.settings creation.*/
    
    /** Lazy initialization of the MIME specific settings folder. The folder should be created
     *  via XML layers, if not, it will be created.
     *  Instances of all XML file in this folder will be created.
     */
    protected MIMEOptionFolder getMIMEFolder(){
        /* #25541 - Instead of synchronized getMIMEFolder() the kit
         * is first obtained and then the synchronization is done
         * to avoid the deadlock caused by locking in opposite order.
         */
        String name = getContentType();
        if (name == null) {
            return null;
        }

        synchronized (this) {
            // return already initialized folder
            if (settingsFolder!=null) return settingsFolder;
            
            FileObject f = Repository.getDefault().getDefaultFileSystem().
            findResource(AllOptionsFolder.FOLDER+"/"+name); //NOI18N
            
            // MIME folder doesn't exist, let's create it
            if (f==null){
                FileObject fo = Repository.getDefault().getDefaultFileSystem().
                findResource(AllOptionsFolder.FOLDER);
                
                if (fo != null){
                    try{
                        StringTokenizer stok = new StringTokenizer(name,"/"); // NOI18N
                        while (stok.hasMoreElements()) {
                            String newFolder = stok.nextToken();
                            if (fo.getFileObject(newFolder) == null){
                                fo = fo.createFolder(newFolder);
                            }
                            else
                                fo = fo.getFileObject(newFolder);
                        }
                    }catch(IOException ioe){
                        ioe.printStackTrace();
                    }
                    
                    f = Repository.getDefault().getDefaultFileSystem().
                    findResource(AllOptionsFolder.FOLDER+"/"+name); // NOI18N
                }
            }
            
            if (f != null) {
                try {
                    DataObject d = DataObject.find(f);
                    DataFolder df = (DataFolder)d.getCookie(DataFolder.class);
                    if (df != null) {
                        settingsFolder = new MIMEOptionFolder(df, this);
                        return settingsFolder;
                    }
                } catch (org.openide.loaders.DataObjectNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
            
            return null;
        }
    }
    
    /** Gets MIMEOptionNode that belongs to this bean */
    public synchronized MIMEOptionNode getMimeNode(){
        if (mimeNode == null) createMIMENode(getTypeName());
        return mimeNode;
    }
    
    /** Creates Node in global options for appropriate MIME type */
    private void createMIMENode(String typeName){
        if (typeName.equals(BASE)){
            return;
        }
        try{
            mimeNode = new MIMEOptionNode(this);
        }catch(IntrospectionException ie){
            ie.printStackTrace();
        }
    }
    
    protected void updateSettingsMap(Class kitClass, Map settingsMap) {
        super.updateSettingsMap(kitClass, settingsMap);

        if (kitClass == getKitClass()) {
            // Create evaluators for indentEngine and formatter
            settingsMap.put(NbEditorDocument.INDENT_ENGINE,
                new Settings.Evaluator() {
                    public Object getValue(Class kitClass2, String settingName) {
                        return getIndentEngine();
                    }
                }
            );

            settingsMap.put(NbEditorDocument.FORMATTER,
                new Settings.Evaluator() {
                    public Object getValue(Class kitClass2, String settingName) {
                        IndentEngine eng = getIndentEngine();
                        return (eng != null)
                            ? ((eng instanceof FormatterIndentEngine)
                                ? ((FormatterIndentEngine)eng).getFormatter()
                                : ((Formatter)new IndentEngineFormatter(getKitClass(), eng)))
                            : null;
                    }
                }
            );
            
            // Antialiasing evaluator - for global options only
            if (kitClass == BaseKit.class) {
                settingsMap.put(SettingsNames.RENDERING_HINTS,
                    new Settings.Evaluator() {
                        public Object getValue(Class kitClass2, String settingName) {
                            return computeTextAntialiasingMap( isTextAntialiasing() );
                        }
                    }
                );
            }

            if (coloringMapInitializer != null) {
                coloringMapInitializer.updateSettingsMap(kitClass, settingsMap);
            }
        }
        
        if (kitClass == BaseKit.class && coloringMapInitializer != null) {
            coloringMapInitializer.updateSettingsMap(BaseKit.class, settingsMap);
        }
    }
    
    private Map computeTextAntialiasingMap( boolean aaSetting ) {
        
        Map result;
        
        Map defaultHints = (Map)(Toolkit.getDefaultToolkit().getDesktopProperty(
                "awt.font.desktophints")); //NOI18N        
        
        if ( defaultHints != null ) { // OK We're at 1.6 or higher
            if (true) // On 1.6 always use the default rendering hints regardless of the aaSetting
                return defaultHints;
            
            Object systemSetting = defaultHints.get( RenderingHints.KEY_TEXT_ANTIALIASING );
                        
            if ( systemSetting == RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT || 
                 systemSetting == RenderingHints.VALUE_TEXT_ANTIALIAS_OFF || 
                 isGasp( systemSetting ) ) {
                result = new HashMap();
                result.put( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
            }
            else {
                result = defaultHints;
            }
        }
        else {  // Lower than 1.6 Jdk            
            if ( !aaSetting ) {
                return Collections.EMPTY_MAP;
            }
            result = new HashMap();
            result.put(RenderingHints.KEY_TEXT_ANTIALIASING,
                       RenderingHints.VALUE_TEXT_ANTIALIAS_ON);                        
        }
        
        return result;        
    }
    
    private static Object gaspConst = RenderingHints.VALUE_TEXT_ANTIALIAS_OFF; 
    
    private boolean isGasp( Object systemSetting ) {
        // RenderingHints.VALUE_TEXT_ANTIALIAS_GASP is available on JDK 1.6+ only
        if ( gaspConst == RenderingHints.VALUE_TEXT_ANTIALIAS_OFF ) {
            try {
                Field aaConst = RenderingHints.class.getField( "VALUE_TEXT_ANTIALIAS_GASP" ); // NOI18N
                gaspConst = aaConst.get( null );
            }
            catch ( NoSuchFieldException e ) {
                gaspConst = null;
            }
            catch ( IllegalAccessException e ) {
                gaspConst = null;
            }
        }
        
        return systemSetting == gaspConst;
    }
    
    /* #54893
    public HelpCtx getHelpCtx() {
        return new HelpCtx(HELP_ID);
    }
     */
    
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
    
    /** Gets Map of default Abbreviations as they are stored in
     *  MIMEFolder/Defaults/abbreviations.xml */
    public Map getDefaultAbbrevMap(){
        loadDefaultAbbreviations();
        return defaultAbbrevsMap;
    }
    
    /** Loads default abbreviations from MIMEFolder/Defaults/abbreviations.xml and
     * stores them to defaultAbbrevsMap */
    private void loadDefaultAbbreviations(){
        if (defaultAbbrevsMap!=null) return;
        MIMEOptionFolder mimeFolder = getMIMEFolder();
        if (mimeFolder == null) return;
        MIMEOptionFolder mof = mimeFolder.getFolder(OptionUtilities.DEFAULT_FOLDER);
        if (mof == null) {
            return;
        }
        
        MIMEOptionFile file = mof.getFile(AbbrevsMIMEProcessor.class, false);
        if ((file!=null) && (!file.isLoaded())) {
            file.loadSettings(false);
            defaultAbbrevsMap = new HashMap(file.getAllProperties());
        }
    }
    
    public Map getAbbrevMap() {
        loadDefaultAbbreviations();
        loadSettings(AbbrevsMIMEProcessor.class);
        Map settingsMap = (Map)super.getSettingValue(SettingsNames.ABBREV_MAP);
        if (settingsMap == null){
            org.netbeans.editor.Utilities.annotateLoggable(new NullPointerException("settingsMap is null for kit:"+getKitClass())); //NOI18N
        }
        Map ret = (settingsMap == null) ? new HashMap() : new HashMap(settingsMap);
        return ret;
    }
    
    /** Sets new abbreviations map to initializer map and if saveToXML is true,
     *  then new settings will be saved to XML file. */
    public void setAbbrevMap(Map map, boolean saveToXML) {
        Map diffMap = null;
        if (saveToXML){
            // we are going to save the diff-ed changes to XML, all default
            // properties have to be available
            loadDefaultAbbreviations();
            diffMap = OptionUtilities.getMapDiff(getAbbrevMap(),map,true);
            if (diffMap.size()>0){
                // settings has changed, write changed settings to XML file
                updateSettings(AbbrevsMIMEProcessor.class, diffMap);
            }
        }
        super.setSettingValue(SettingsNames.ABBREV_MAP, map, ABBREV_MAP_PROP);
    }
    
    /** Sets new abbreviations map and save the diff-ed changes to XML file*/
    public void setAbbrevMap(Map map) {
        setAbbrevMap(map, true);
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

    /**
     *@deprecated since adaptation to new view implementation
        the option is not supported 
     */
    public boolean getCaretItalicInsertMode() {
        return false;//getSettingBoolean(SettingsNames.CARET_ITALIC_INSERT_MODE);
    }
    /**
     *@deprecated since adaptation to new view implementation
        the option is not supported 
     */
    public void setCaretItalicInsertMode(boolean b) {
        setSettingBoolean(SettingsNames.CARET_ITALIC_INSERT_MODE, b,
        CARET_ITALIC_INSERT_MODE_PROP);
    }

    /**
     *@deprecated since adaptation to new view implementation
        the option is not supported 
     */
    public boolean getCaretItalicOverwriteMode() {
        return false;//getSettingBoolean(SettingsNames.CARET_ITALIC_OVERWRITE_MODE);
    }
    /**
     *@deprecated since adaptation to new view implementation
        the option is not supported 
     */
    public void setCaretItalicOverwriteMode(boolean b) {
        setSettingBoolean(SettingsNames.CARET_ITALIC_OVERWRITE_MODE, b,
        CARET_ITALIC_OVERWRITE_MODE_PROP);
    }
    
    public Color getCaretColorInsertMode() {
        loadSettings(FontsColorsMIMEProcessor.class);
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
        if (saveToXML){
            if (!getCaretColorInsertMode().equals(color) && (color!=null)){
                // settings has changed, write changed settings to XML file
                Map map = new HashMap();
                map.put(SettingsNames.CARET_COLOR_INSERT_MODE,color);
                if (map!=null){
                    updateSettings(FontsColorsMIMEProcessor.class,
                    map);
                }
            }
        }
        
        super.setSettingValue(SettingsNames.CARET_COLOR_INSERT_MODE, color,
        CARET_COLOR_INSERT_MODE_PROP);
    }
    
    public Color getCaretColorOverwriteMode() {
        loadSettings(FontsColorsMIMEProcessor.class);
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
        if (saveToXML){
            if (!getCaretColorOverwriteMode().equals(color) && (color!=null)){
                // settings has changed, write changed settings to XML file
                Map map = new HashMap();
                map.put(SettingsNames.CARET_COLOR_OVERWRITE_MODE,color);
                if (map!=null){
                    updateSettings(FontsColorsMIMEProcessor.class,
                    map);
                }
            }
        }
        
        super.setSettingValue(SettingsNames.CARET_COLOR_OVERWRITE_MODE, color,
        CARET_COLOR_OVERWRITE_MODE_PROP);
    }
    
    public int getCaretBlinkRate() {
        return getSettingInteger(SettingsNames.CARET_BLINK_RATE);
    }
    public void setCaretBlinkRate(int rate) {
        if (rate < 0) {
            NbEditorUtilities.invalidArgument("MSG_NegativeValue"); // NOI18N
            return;
        }
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
    
    /** Gets Map of default KeyBindings as they are stored in
     *  MIMEFolder/Defaults/keybindings.xml */
    public Map getDefaultKeyBindingsMap(){
        loadDefaultKeyBindings();
        return defaultKeyBindingsMap;
    }
    
    /** Loads default abbreviations from MIMEFolder/Defaults/keybindings.xml and
     *  stores them to defaultKeyBindingsMap */
    private void loadDefaultKeyBindings(){
        if (defaultKeyBindingsMap!=null) return;
        MIMEOptionFolder mof;
        MIMEOptionFolder mimeFolder = AllOptionsFolder.getDefault().getMIMEFolder();
        if (mimeFolder == null) return;
        mof = mimeFolder.getFolder(OptionUtilities.DEFAULT_FOLDER);
        if (mof == null) {
            return;
        }
        MIMEOptionFile file = mof.getFile(KeyBindingsMIMEProcessor.class, false);
        if ((file!=null) && (!file.isLoaded())) {
            file.loadSettings(false);
            defaultKeyBindingsMap = new HashMap(file.getAllProperties());
        }
        //#68762 - Basic keybinding broken for old editor kits
        if (!usesNewOptionsDialog() && !BASE.equals(getTypeName())){
                super.setSettingValue(SettingsNames.KEY_BINDING_LIST,
                        new ArrayList(defaultKeyBindingsMap.values()),
                        KEY_BINDING_LIST_PROP);        
        }
    }
    
    private List getKBList(){
        if (!keybindingsInitialized) {
            keybindingsInitialized = true;
            if (usesNewOptionsDialog()){
                updateKeybindingsFromNewOptionsDialogAttributes();
            } else {
                loadDefaultKeyBindings();
                loadSettings(KeyBindingsMIMEProcessor.class);
            } 
        }
        
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
        return new ArrayList( kbList );
    }
    
    public List getKeyBindingList() {
        List kb2 = new ArrayList( getKBList() );
        kb2.add( 0, getKitClass().getName() ); //insert kit class name
        return kb2;
    }
    
    /** Sets new keybindings map and save the diff-ed changes to XML file*/
    public void setKeyBindingList(List list) {
        setKeyBindingList(list, true);
    }
    
    
    /** Saves keybindings settings to XML file. 
     *  (This is used especially for record macro action.)*/
    public void setKeyBindingsDiffMap(Map diffMap){
        if ((diffMap != null) && (diffMap.size()>0)){
            updateSettings(KeyBindingsMIMEProcessor.class, diffMap);
        }
    }
    
    
    /** Sets new keybindings list to initializer map and if saveToXML is true,
     *  then new settings will be saved to XML file. */
    public void setKeyBindingList(List list, boolean saveToXML) {
        if( list.size() > 0 &&
        ( list.get( 0 ) instanceof Class || list.get( 0 ) instanceof String )
        ) {
            list.remove( 0 ); //remove kit class name
        }
        
        Map diffMap = null;
        if (saveToXML){
            // we are going to save the diff-ed changes to XML, all default
            // properties have to be available
            loadDefaultKeyBindings();
            List kbMap = getKeyBindingList();
            if( kbMap.size() > 0 &&
            ( kbMap.get( 0 ) instanceof Class || kbMap.get( 0 ) instanceof String )
            ) {
                kbMap.remove( 0 ); //remove kit class name
            }
            
            diffMap = OptionUtilities.getMapDiff(OptionUtilities.makeKeyBindingsMap(kbMap),
            OptionUtilities.makeKeyBindingsMap(list),true);
            if (diffMap.size()>0){
                // settings has changed, write changed settings to XML file
                updateSettings(KeyBindingsMIMEProcessor.class, diffMap);
            }
        }
        
        super.setSettingValue(SettingsNames.KEY_BINDING_LIST, list, KEY_BINDING_LIST_PROP);
    }
    
    static Font toFont (AttributeSet s, Integer defSize) {
        Object fontFamily = s.getAttribute (StyleConstants.FontFamily);
        Object fontSize = s.getAttribute (StyleConstants.FontSize);
        if (fontFamily == null){ 
            return null;
        }
        
        if (fontSize == null){
            fontSize = defSize;
        }
        
	int style = 0;
        if (Boolean.TRUE.equals(s.getAttribute (StyleConstants.Bold))){
	    style += Font.BOLD;
        }
        
	if (Boolean.TRUE.equals(s.getAttribute (StyleConstants.Italic))){
	    style += Font.ITALIC;
        }
        
	return new Font (
	    (String) fontFamily, 
	    style,
	    ((Integer) fontSize).intValue ()
	);
    }
    
    private void updateKeybindingsFromNewOptionsDialogAttributes(){
        KeyBindingSettings kbs = getKeybindingSettings();
        if (kbs == null){
            return;
        }
        List newKeybs = new ArrayList();
        List newOptionDialogKeybs = kbs.getKeyBindings();
        // convert to editor's MultiKeyBinding
        for (int i=0; i<newOptionDialogKeybs.size(); i++){
            org.netbeans.api.editor.settings.MultiKeyBinding mkb = 
            (org.netbeans.api.editor.settings.MultiKeyBinding) newOptionDialogKeybs.get(i);
            List lst = mkb.getKeyStrokeList();
            KeyStroke keys[] = new KeyStroke[lst.size()];
            lst.toArray(keys);
            MultiKeyBinding editorMkb = new MultiKeyBinding(keys, mkb.getActionName());
            newKeybs.add(editorMkb);
        }
        super.setSettingValue(SettingsNames.KEY_BINDING_LIST, newKeybs, KEY_BINDING_LIST_PROP);
    }
    
    private void updateColoringsFromNewOptionsDialogAttributes(){
        Map m = getColoringMap_old();
        m.remove(null);
        FontColorSettings fcs = getFontColorSettings();
        if (fcs == null){
            return;
        }
        Iterator it = m.keySet ().iterator ();
        
        AttributeSet defAS = fcs.getTokenFontColors("default"); //NOI18N        
        Integer defSize = null;
        if (defAS != null){
            Object fsObj = defAS.getAttribute(StyleConstants.FontSize);
            if (fsObj instanceof Integer){
                defSize = (Integer) fsObj;
            }
        }

        if (defSize == null){
            defSize = new Integer(SettingsDefaults.defaultFont.getSize());
        }
        
        while (it.hasNext ()) {
            String category = (String) it.next ();
	    if (category == null) continue;
            AttributeSet as = fcs.getTokenFontColors (category);
	    if (as == null) 
		as = fcs.getFontColors (category);
	    if (as == null) {
		System.out.println("ColorBridge.unknown category " + category);
                continue;
	    }
	    Font font = as.getAttribute (StyleConstants.FontFamily) != null ?
                toFont (as, defSize) : null;
	    if (category.equals ("default")) { //NOI18N
		if (font == null) {
		    System.out.println("ColorBridge null font!!! ");
		    continue;		
		}
		if (as.getAttribute (StyleConstants.Foreground) == null) {
		    System.out.println("ColorBridge null Foreground!!! ");
		    continue;		
		}
		if (as.getAttribute (StyleConstants.Background) == null) {
		    System.out.println("ColorBridge null Background!!! ");
		    continue;		
		}
	    }
            Coloring c = new Coloring (
                font,
                Coloring.FONT_MODE_DEFAULT,
                (Color) as.getAttribute (StyleConstants.Foreground),
                (Color) as.getAttribute (StyleConstants.Background),
                (Color) as.getAttribute (StyleConstants.Underline),
                (Color) as.getAttribute (StyleConstants.StrikeThrough),
                (Color) as.getAttribute (EditorStyleConstants.WaveUnderlineColor)
            );
            m.put (category, c);
        }
        m.put(null, getKitClass().getName() ); // add kit class                               
        setColoringMap (m);

        // set other colorings like text limit line, caret
        AttributeSet set = (AttributeSet) fcs.getFontColors(SettingsNames.CARET_COLOR_INSERT_MODE);
        if (set != null){
            Color color = (Color)set.getAttribute(StyleConstants.Foreground);
            Color current  = getCaretColorInsertMode();
            if (color!=null && !color.equals(current)){
                setCaretColorInsertMode(color);
                setCaretColorOverwriteMode(color);
            }
        }

        set = (AttributeSet) fcs.getFontColors(SettingsNames.TEXT_LIMIT_LINE_COLOR);
        if (set != null){
            Color color = (Color)set.getAttribute(StyleConstants.Foreground);
            Color current  = getTextLimitLineColor();
            if (color!=null && !color.equals(current)){
                setTextLimitLineColor(color);
            }
        }
    }
    
    private synchronized KeyBindingSettings getKeybindingSettings(){
        if (keyBindingsSettings == null){
            String mime = getContentType();
            Lookup lookup = MimeLookup.getLookup(MimePath.parse(mime));
            resultKB = lookup.lookup(new Lookup.Template(KeyBindingSettings.class));
            Collection inst = resultKB.allInstances();
            lookupListenerKB = new LookupListener(){
                public void resultChanged(LookupEvent ev){
                    Lookup.Result result = ((Lookup.Result)ev.getSource());
                    // refresh keyBindingsSettings
                    Collection newInstances = result.allInstances();
                    if (newInstances.size() > 0){
                        keyBindingsSettings = (KeyBindingSettings)newInstances.iterator().next();
                    }
                    
                    updateKeybindingsFromNewOptionsDialogAttributes();
                }
            };
            
            weakLookupListenerKB = (LookupListener) WeakListeners.create(
                    LookupListener.class, lookupListenerKB, resultKB);
            
            resultKB.addLookupListener(weakLookupListenerKB);            
            if (inst.size() > 0){
                keyBindingsSettings = (KeyBindingSettings)inst.iterator().next();
            }
        }
        return keyBindingsSettings;
    }
    
    private synchronized FontColorSettings getFontColorSettings(){
        if (fontColorSettings == null){
            String mime = getContentType();
            Lookup lookup = MimeLookup.getLookup(MimePath.parse(mime));
            Lookup.Result result = lookup.lookup(new Lookup.Template(FontColorSettings.class));
            Collection inst = result.allInstances();
            lookupListener = new LookupListener(){
                public void resultChanged(LookupEvent ev){
                    Lookup.Result result = ((Lookup.Result)ev.getSource());
                    // refresh fontColorSettings
                    Collection newInstances = result.allInstances();
                    if (newInstances.size() > 0){
                        fontColorSettings = (FontColorSettings)newInstances.iterator().next();
                    }
                    
                    updateColoringsFromNewOptionsDialogAttributes();
                }
            };
            result.addLookupListener(lookupListener);
            if (inst.size() > 0){
                fontColorSettings = (FontColorSettings)inst.iterator().next();
            }
        }
        return fontColorSettings;
    }
    
    public Map getColoringMap() {
        if (!coloringsInitialized) {
            coloringsInitialized = true;
            if (usesNewOptionsDialog()) {
                updateColoringsFromNewOptionsDialogAttributes();
            } else {
                loadSettings(FontsColorsMIMEProcessor.class);
            }
        }
        
        return getColoringMap_old();
    }
    
    private Map getColoringMap_old() {
        Map settingsMap = SettingsUtil.getColoringMap(getKitClass(), false, true); // !!! !evaluateEvaluators
        if (settingsMap == null){
            org.netbeans.editor.Utilities.annotateLoggable(new NullPointerException("settingsMap is null for kit:"+getKitClass())); //NOI18N
        }
        Map cm = (settingsMap == null) ? new HashMap() : new HashMap(settingsMap);
        cm.put(null, getKitClass().getName() ); // add kit class
        return cm;
    }
    
    /** Sets new coloring map and save the diff-ed changes to XML file*/
    public void setColoringMap(Map coloringMap) {
        setColoringMap(coloringMap, true);
    }
    
    /** Sets new coloring map to initializer map and if saveToXML is true,
     *  then new settings will be saved to XML file. */
    public void setColoringMap(final Map coloringMap, boolean saveToXML){
        Map diffMap = null;
        if (coloringMap != null) {
//            if (inReadExternal) {
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
                //coloringMap = UpgradeOptions.patchColorings(getKitClass(), coloringMap);
//            }
            
            if (!usesNewOptionsDialog() && saveToXML){
                diffMap = OptionUtilities.getMapDiff(getColoringMap(),coloringMap,false);
                if (diffMap.size()>0){
                    // settings has changed, write changed settings to XML file
                    //System.out.println("SETTING COLORING MAP:"+diffMap);
                    updateSettings(FontsColorsMIMEProcessor.class, diffMap);
                }
            }
            
            coloringMap.remove(null); // remove kit class
            
            Settings.update(new Runnable() {
                public void run() {
                    SettingsUtil.setColoringMap( getKitClass(), coloringMap, false );
                }
            });
            
            coloringMapInitializer = SettingsUtil.getColoringMapInitializer(
            getKitClass(), coloringMap, false,
            getTypeName() + "-coloring-map-initializer" //NOI18N
            );
            
            firePropertyChange(COLORING_MAP_PROP, null, null);
        }
    }
    
    public int getFontSize() {
        Coloring dc = SettingsUtil.getColoring(getKitClass(), SettingsNames.DEFAULT_COLORING, false);
        return (dc != null) ? dc.getFont().getSize() : SettingsDefaults.defaultFont.getSize();
    }
    
    public void setFontSize(final int size) {
        if (size < 0) {
            NbEditorUtilities.invalidArgument("MSG_NegativeValue"); // NOI18N
            return;
        }
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
        if (f <= 0) {
            NbEditorUtilities.invalidArgument("MSG_OutOfRange"); // NOI18N
            return;
        }
        setSettingValue(SettingsNames.LINE_HEIGHT_CORRECTION, new Float(f),
        LINE_HEIGHT_CORRECTION_PROP);
    }
    
    /** Gets Map of default Macros as they are stored in
     *  MIMEFolder/Defaults/macros.xml */
    public Map getDefaultMacrosMap(){
        loadDefaultMacros();
        return defaultMacrosMap;
    }
    
    /** Loads default abbreviations from MIMEFolder/Defaults/macros.xml and
     *  stores them to defaultMacrosMap */
    private void loadDefaultMacros(){
        if (defaultMacrosMap!=null) return;
        MIMEOptionFolder mimeFolder = getMIMEFolder();
        if (mimeFolder == null) return;

        MIMEOptionFolder mof = mimeFolder.getFolder(OptionUtilities.DEFAULT_FOLDER);
        if (mof == null) return;
        
        MIMEOptionFile file = mof.getFile(MacrosMIMEProcessor.class, false);
        if ((file!=null) && (!file.isLoaded())) {
            file.loadSettings(false);
            defaultMacrosMap = new HashMap(file.getAllProperties());
        }
    }

    /** removes keybindings from deleted macros, or if macro deletion was cancelled
     *  it restores old keybinding value */
    private void processMacroKeyBindings(Map diff, List oldKB){
        List deletedKB = new ArrayList();
        List addedKB = new ArrayList();
        List newKB = getKBList();        

        for( Iterator i = diff.keySet().iterator(); i.hasNext(); ) {
            String key = (String)i.next();
            if (!(diff.get(key) instanceof String)) continue;
            String action = (String) diff.get(key);
            String kbActionName = BaseKit.macroActionPrefix+key;

            if (action.length()!=0){
                // process restored macros
                for (int j = 0; j < oldKB.size(); j++){
                    if(oldKB.get(j) instanceof MultiKeyBinding){
                        MultiKeyBinding mkb = (MultiKeyBinding) oldKB.get(j);
                        if (!kbActionName.equals(mkb.actionName)) continue;
                        addedKB.add(mkb);
                        break;
                    }
                }
                continue;
            }
            
            for (int j = 0; j < newKB.size(); j++){
                // process deleted macros
                if(newKB.get(j) instanceof MultiKeyBinding){
                    MultiKeyBinding mkb = (MultiKeyBinding) newKB.get(j);
                    if (!kbActionName.equals(mkb.actionName)) continue;
                    deletedKB.add(mkb);
                    break;
                }
            }
        }
        
        if ((deletedKB.size()>0) || (addedKB.size()>0)){
            newKB.removeAll(deletedKB);
            newKB.addAll(addedKB);
            // save changed keybindings to XML file
            setKeyBindingsDiffMap(OptionUtilities.getMapDiff(OptionUtilities.makeKeyBindingsMap(getKBList()), 
                OptionUtilities.makeKeyBindingsMap(newKB), true));
            // set new keybindings
            Settings.setValue( getKitClass(), SettingsNames.KEY_BINDING_LIST, newKB);
        }
    }
    
    /** Gets Macro Map */
    public Map getMacroMap() {
        loadDefaultMacros();
        loadSettings(MacrosMIMEProcessor.class);
        Map settingsMap = (Map)super.getSettingValue(SettingsNames.MACRO_MAP);
        if (settingsMap == null){
            org.netbeans.editor.Utilities.annotateLoggable(new NullPointerException("settingsMap is null for kit:"+getKitClass())); //NOI18N
        }
        Map ret = (settingsMap == null) ? new HashMap() : new HashMap(settingsMap);
        ret.put(null, getKBList());
        return ret;
    }
    
    /** Saves macro settings to XML file. 
     *  (This is used especially for record macro action.)*/
    public void setMacroDiffMap(Map diffMap){
        if ((diffMap != null) && (diffMap.size()>0)){
            updateSettings(MacrosMIMEProcessor.class, diffMap);
        }
    }
    
    /** Sets new macro map to initializer map and if saveToXML is true,
     *  then new settings will be saved to XML file. */
    public void setMacroMap(Map map, boolean saveToXML) {
        Map diffMap = null;
        List kb = new ArrayList();
        if (map.containsKey(null)){
            kb.addAll((List)map.get(null));
            map.remove(null);
        }
        if (saveToXML){
            // we are going to save the diff-ed changes to XML, all default
            // properties have to be available
            loadDefaultMacros();
            diffMap = OptionUtilities.getMapDiff(getMacroMap(),map,true);
            if (diffMap.containsKey(null)) diffMap.remove(null);
            if (diffMap.size()>0){
                // settings has changed, write changed settings to XML file
                processMacroKeyBindings(diffMap,kb);
                updateSettings(MacrosMIMEProcessor.class, diffMap);
            }
        }
        
        super.setSettingValue(SettingsNames.MACRO_MAP, map);
    }
    
    /** Sets new macros map and save the diff-ed changes to XML file*/
    public void setMacroMap(Map map) {
        setMacroMap(map, true);
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
    
    public boolean getPairCharactersCompletion() {
      return getSettingBoolean(SettingsNames.PAIR_CHARACTERS_COMPLETION);
    }

    public void setPairCharactersCompletion(boolean v) {
        setSettingBoolean(SettingsNames.PAIR_CHARACTERS_COMPLETION, v,
            PAIR_CHARACTERS_COMPLETION);
    }

    
    public Color getTextLimitLineColor() {
        loadSettings(FontsColorsMIMEProcessor.class);
        return (Color)super.getSettingValue(SettingsNames.TEXT_LIMIT_LINE_COLOR);
    }
    
    /** Sets new TextLimitLineColor property to initializer map and save the
     *  changes to XML file */
    public void setTextLimitLineColor(Color color) {
        setTextLimitLineColor(color, true);
    }
    
    /** Sets new TextLimitLineColor property to initializer map and if saveToXML is true,
     *  then new settings will be saved to XML file (fontsColors.xml). */
    public void setTextLimitLineColor(Color color , boolean saveToXML) {
        if (saveToXML){
            if (!getTextLimitLineColor().equals(color) && (color!=null)){
                // settings has changed, write changed settings to XML file
                Map map = new HashMap();
                map.put(SettingsNames.TEXT_LIMIT_LINE_COLOR,color);
                if (map!=null){
                    updateSettings(FontsColorsMIMEProcessor.class,
                    map);
                }
            }
        }
        
        super.setSettingValue(SettingsNames.TEXT_LIMIT_LINE_COLOR, color,
        TEXT_LIMIT_LINE_COLOR_PROP);
    }
    
    public int getTextLimitWidth() {
        return getSettingInteger(SettingsNames.TEXT_LIMIT_WIDTH);
    }
    
    public void setTextLimitWidth(int width) {
        if (width <= 0) {
            NbEditorUtilities.invalidArgument("MSG_OutOfRange"); // NOI18N
            return;
        }
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

    public boolean isToolbarVisible() {
        return getSettingBoolean(TOOLBAR_VISIBLE_PROP);
    }
    
    public void setToolbarVisible(boolean toolbarVisible) {
        setSettingBoolean(TOOLBAR_VISIBLE_PROP, toolbarVisible, TOOLBAR_VISIBLE_PROP);
    }
    
    public boolean isTextAntialiasing() {
        // artificial setting -> evaluator used (at begining of this class)
        Boolean val = (Boolean)getSettingValue(TEXT_ANTIALIASING_PROP);
        if (val != null) {
            return val.booleanValue();
        } else {
            //XXX this prop is not used anymore, but anyway, I believe
            //it should have been swing.aatext - I've never seen
            //javax.aatext.  -Tim
            // #56234: Check -Djavax.aatext=true
            if (Boolean.getBoolean("javax.aatext")) {
                return true;

            } else {
                // fix of #31758
                if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
                    // On OSX, default to true
                    return true;
                } else {
                    return isSystemAntialias();
                }
            }
        }
    }
    
    private static boolean isSystemAntialias() {
        Map systemHints = (Map)(Toolkit.getDefaultToolkit().getDesktopProperty(
                "awt.font.desktophints")); //NOI18N
        if (systemHints != null) {
            Object o = systemHints.get(RenderingHints.KEY_TEXT_ANTIALIASING);
            boolean result = o != null && 
                             o != RenderingHints.VALUE_TEXT_ANTIALIAS_OFF &&  
                             o != RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT;
            return result;
        } else {
            return false;
        }
    }
    
    public void setTextAntialiasing(boolean textAntialiasing) {
        setSettingBoolean(TEXT_ANTIALIASING_PROP, textAntialiasing, TEXT_ANTIALIASING_PROP);
        // Cause refresh or renderingHints variable in EditorUI
        Settings.touchValue(getKitClass(), SettingsNames.RENDERING_HINTS);
    }
    
    public Map getCodeFoldingProps(){
        Map map = new HashMap();
        
        Boolean val = (Boolean)getSettingValue(SettingsNames.CODE_FOLDING_ENABLE);
        map.put(SettingsNames.CODE_FOLDING_ENABLE, val);
        
        return map;
    }
    
    public void setCodeFoldingProps(Map props){
        String name = SettingsNames.CODE_FOLDING_ENABLE;
        setSettingValue(name, props.get(name));
    }
        
    /** Retrieves the actions from XML file */
    public void initPopupMenuItems(){
        List orderedPopupFiles = getOrderedMultiPropertyFolderFiles("Popup"); //NOI18N
        //if (orderedPopupFiles.size() >0){
        //    super.setSettingValue(ExtSettingsNames.POPUP_MENU_ACTION_NAME_LIST,
        //        OptionUtilities.getPopupStrings(orderedPopupFiles)); //NOI18N
        //}
    }
    
    public List getOrderedMultiPropertyFolderFiles(String folderName){
        if (!BASE.equals(getTypeName())){
            MIMEOptionFolder mimeFolder = getMIMEFolder();
            if (mimeFolder != null){
                MultiPropertyFolder mpf = mimeFolder.getMPFolder(folderName,false); //NOI18N
                if (mpf!=null){
                    DataFolder df = mpf.getDataFolder();
                    List mimeFolderAttribs = new ArrayList();
                    for (Enumeration e = df.getPrimaryFile().getAttributes() ; e.hasMoreElements() ;) {
                        mimeFolderAttribs.add(e.nextElement());
                    }

                    // merge folders only if mime folder has some relevant info
                    if ( (mpf.getProperties().size()!=0) || (mimeFolderAttribs.size() != 0)) {
                        
                        // We are going to merge global items and mime popup items ...
                        // Firstly merge popup items
                        Set mergedItems = new HashSet(OptionUtilities.getGlobalMenuItems(folderName));

                        mergedItems.addAll(mpf.getProperties());
                        
                        // Then merge attribs
                        Set mergedAttribs = new HashSet(OptionUtilities.getGlobalAttribs(folderName));
                        mergedAttribs.addAll(mimeFolderAttribs);
                        
                        // Sort it in accordance with merged Attribs
                        return OptionUtilities.arrangeMergedFolderObjects(mergedItems, mergedAttribs);
                    }
                }
            }
        }

        return OptionUtilities.getGlobalMenuItems(folderName);
    }
    
    public IndentEngine getIndentEngine() {
        // Due to #11212
        if (inReadExternal) {
            return readExternalIndentEngine;
        }

        if (!BASE.equals(getTypeName())){
            loadSettings(PropertiesMIMEProcessor.class);        

            MIMEOptionFile file; 
            MIMEOptionFolder mimeFolder = getMIMEFolder();
            if (mimeFolder != null){
                file= mimeFolder.getFile(PropertiesMIMEProcessor.class, false);
                if (file != null) {
                    Map setMap = file.getAllProperties();
                    Object handle = setMap.get(INDENT_ENGINE_PROP);
                    if (handle instanceof String){
                        Object instance = null;
                        String handleString = (String) handle;
                        
                        if (handleString.equals(NO_INDENT_ENGINE)){
                            return IndentEngine.getDefault();
                        }
                        
                        Lookup.Template tmp = new Lookup.Template(null, handleString, null);
                        Lookup.Item item = Lookup.getDefault().lookupItem(tmp);
                        if (item != null) {
                            instance = item.getInstance();
                            if(instance instanceof IndentEngine){
                                return (IndentEngine) instance;
                            }
                        }

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
        /* Disabled direct setting of the engine
         * during project deserialization to avoid doubled
         * indent engine as described in #9687
         */
        if (!inReadExternal) {
            String id = null;
            if (eng != null) {
                Lookup.Template tmp = new Lookup.Template(null, null, eng);
                Lookup.Item item = Lookup.getDefault().lookupItem(tmp);
                if (item != null) id = item.getId();
                
            }

            if (!BASE.equals(getTypeName())){
                Map map = new HashMap();
                if (id == null) id = NO_INDENT_ENGINE; 
                map.put(INDENT_ENGINE_PROP, id);
                updateSettings(PropertiesMIMEProcessor.class, map, false);
            }

            refreshIndentEngineSettings();
        }
    }
    
    private void refreshIndentEngineSettings() {
        // Touches the settings
        RequestProcessor.postRequest(new Runnable(){
            public void run(){
                Settings.touchValue(getKitClass(), NbEditorDocument.INDENT_ENGINE);
                Settings.touchValue(getKitClass(), NbEditorDocument.FORMATTER);
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
        
        try {
            // Read the serialized options
            super.readExternal(in);
        }catch(java.io.OptionalDataException ode){
            // #17385. It occurs during reading Settings.settings, that is unimportant
        } finally {

            // Make sure the indent engine settings are propagated
            // (SharedClassObject.putProperty() is final)
            refreshIndentEngineSettings();

            // Possibly upgrade the options
            //if (optionsVersion < LATEST_OPTIONS_VERSION) {
            //    upgradeOptions(optionsVersion, LATEST_OPTIONS_VERSION);
            //}

            optionsVersion = LATEST_OPTIONS_VERSION;

            /** Release temp indent engine -  #11212 */
            inReadExternal = false;
            readExternalIndentEngine = null;
        }
    }
    
    /** Upgrade the deserialized options.
     * @param version deserialized version of the options
     * @param latestVersion latest version of the options
     *   that will be set to them after they are upgraded
     */
    protected void upgradeOptions(int version, int latestVersion) {
        // Upgrade in separate class to avoid messing up BaseOptions
        //UpgradeOptions.upgradeOptions(this, version, latestVersion);
    }
    
    /** Load settings from XML files and initialize changes */
    private void loadSettings(Class processor){
        MIMEOptionFile file;
        if (BASE.equals(getTypeName())){
            MIMEOptionFolder mimeFolder = AllOptionsFolder.getDefault().getMIMEFolder();
            if (mimeFolder == null) return;
            file= mimeFolder.getFile(processor, false);
        }else{
            MIMEOptionFolder mimeFolder = getMIMEFolder();
            if (mimeFolder == null) return;
            file= mimeFolder.getFile(processor, false);
        }
        if ((file!=null) && (!file.isLoaded())) {
            file.loadSettings();
        }
    }
    
    /** Save changes to XML files.
     * @see #updateSettings(java.lang.Class, java.util.Map, boolean )
     */
    private void updateSettings(Class processor, Map settings){
        updateSettings(processor, settings, true);
    }
    
    /** Save changes to XML files 
     *  @param processor MIMEProcessor class
     *  @param settings the settings map 
     *  @param useRequestProcessorForSaving if true settings will be saved in RequestProcessor thread.
     */
    private void updateSettings(Class processor, Map settings, boolean useRequestProcessorForSaving){
        if (usesNewOptionsDialog() && processor == FontsColorsMIMEProcessor.class){
            return;
        }
        MIMEOptionFile fileX;
        MIMEOptionFolder mimeFolder;
        if (BASE.equals(getTypeName())){
            mimeFolder = AllOptionsFolder.getDefault().getMIMEFolder();
            if (mimeFolder == null) return;
            fileX = mimeFolder.getFile(processor, true);
        }else{
            mimeFolder = getMIMEFolder();
            if (mimeFolder == null) return;
            fileX = mimeFolder.getFile(processor, true);
        }
        final Map finalSettings = settings;
        final MIMEOptionFile file = fileX;
        if (file!=null){
            if (useRequestProcessorForSaving){
                RequestProcessor.postRequest(new Runnable(){
                    public void run(){
                        file.updateSettings(finalSettings);
                    }
                });
            }else{
                file.updateSettings(finalSettings);                
            }
            
        } else {
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!"+processor.toString()+" type file haven't been found in folder:"+mimeFolder.getDataFolder()); //TEMP
        }
    }
    
    public void setSettingValue(String settingName, Object newValue) {
        setSettingValue(settingName, newValue, settingName);
    }
    
    private boolean isTheSame(String settingName, Object newValue){
        if (settingName == null ||
        settingName.equals(NbEditorDocument.INDENT_ENGINE) ||
        settingName.equals(NbEditorDocument.FORMATTER) ){
            return true;
        }
        Object oldValue = getSettingValue(settingName);
        if ((oldValue == null && newValue == null)
        || (oldValue != null && oldValue.equals(newValue))
        ) {
            return true; // the same object
        }
        return false;
    }
    
    /** Sets setting value to initializer Map and save the changes to XML file
     *  (properties.xml) */
    public void setSettingValue(String settingName, Object newValue,
    String propertyName) {
        if (!isTheSame(settingName, newValue)){
            Map map = new HashMap();
            map.put(settingName, newValue);
            updateSettings(PropertiesMIMEProcessor.class, map);
        }
        super.setSettingValue(settingName,newValue,propertyName);
    }
    
    public Object getSettingValue(String settingName) {
        loadSettings(PropertiesMIMEProcessor.class);
        return super.getSettingValue(settingName);
    }
    
    protected final void setSettingBoolean(String settingName, boolean newValue, String propertyName) {
        setSettingValue(settingName, newValue ? Boolean.TRUE : Boolean.FALSE);
    }
    
    protected final void setSettingInteger(String settingName, int newValue, String propertyName) {
        setSettingValue(settingName, new Integer(newValue));
    }
    
    /** Load all available settings from XML files and initialize them */
    protected void loadXMLSettings(){
        getKeyBindingList();
        getAbbrevMap();
        getMacroMap();
        getColoringMap();
        loadSettings(PropertiesMIMEProcessor.class);
    }

    /** Overriden writeExternal method. BaseOptions are no longer serialized. */
    public void writeExternal() throws IOException{
    }
    
    /** Overriden writeExternal method. BaseOptions are no longer serialized. */
    public void writeExternal(ObjectOutput out) throws IOException{
    }

    protected void firePropertyChange(String name, Object oldValue, Object newValue){
        // ignore firing... Quick fix of #47261. 
        // BaseOptions should be rewritten to not extend SystemOption ...
        // there is no need to be compatile with NB 3.2 and deserialize its options...
    }

    /** Saves the keystroke of code tamplate expansion into properties.xml file under Editors/text/base */
    public static void setCodeTemplateExpandKey(KeyStroke ks){
        String s = OptionUtilities.keyToString(ks);
        BaseOptions base = getOptions(BaseKit.class);
        Map map = new HashMap();
        map.put(CODE_TEMPLATE_EXPAND_KEY, s);
        base.updateSettings(PropertiesMIMEProcessor.class, map);
    }
    
    /** Gets Code Template Expand Key. Can return null if there is no key in the settings file */
    public static KeyStroke getCodeTemplateExpandKey(){
        MIMEOptionFolder mimeFolder = AllOptionsFolder.getDefault().getMIMEFolder();
        if (mimeFolder != null){
            MIMEOptionFile file = mimeFolder.getFile(PropertiesMIMEProcessor.class, false);
            if (file != null){
                if (!file.isLoaded()) {
                    file.loadSettings(false);
                }
                Map properties = file.getAllProperties();
                String s = (String) properties.get(CODE_TEMPLATE_EXPAND_KEY);
                if (s != null){
                    return OptionUtilities.stringToKey(s);
                }
            }
        }
        return KeyStroke.getKeyStroke (KeyEvent.VK_SPACE, 0);
    }
}

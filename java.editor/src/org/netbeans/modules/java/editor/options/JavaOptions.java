/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.editor.options;

import java.util.Enumeration;
import java.util.List;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.netbeans.editor.ext.java.JavaSettingsNames;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.editor.java.JavaIndentEngine;
import org.netbeans.modules.editor.options.OptionSupport;
import org.openide.util.HelpCtx;
import org.netbeans.modules.editor.NbEditorUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.ext.java.JavaFastImport;
import org.netbeans.modules.editor.options.BaseOptions;
import org.openide.util.NbBundle;

/**
* Options for the java editor kit
*
* @author Miloslav Metelka
* @version 1.00
*/
public class JavaOptions extends BaseOptions {

    public static final String JAVA = "java"; // NOI18N

    public static final String COMPLETION_AUTO_POPUP_PROP = "completionAutoPopup"; // NOI18N
    
    public static final String COMPLETION_CASE_SENSITIVE_PROP = "completionCaseSensitive"; // NOI18N
    
    public static final String COMPLETION_NATURAL_SORT_PROP = "completionNaturalSort"; // NOI18N    
    
    public static final String COMPLETION_AUTO_POPUP_DELAY_PROP = "completionAutoPopupDelay"; // NOI18N

    public static final String FORMAT_SPACE_BEFORE_PARENTHESIS_PROP = "formatSpaceBeforeParenthesis"; // NOI18N

    public static final String FORMAT_COMPOUND_BRACKET_ADD_NL_PROP = "formatCompoundBracketAddNL"; // NOI18N
    
    public static final String FAST_IMPORT_SELECTION_PROP = "fastImportSelection"; //NOI18N

    private static final String HELP_ID = "editing.editor.java"; // !!! NOI18N
    
    public static final String JAVADOC_BGCOLOR = "javaDocBGColor"; // NOI18N
    
    public static final String JAVADOC_AUTO_POPUP_DELAY_PROP = "javaDocAutoPopupDelay"; //NOI18N
    
    public static final String JAVADOC_PREFERRED_SIZE_PROP = "javaDocPreferredSize"; //NOI18N
    
    public static final String JAVADOC_AUTO_POPUP_PROP = "javaDocAutoPopup"; //NOI18N
    
    public static final String SHOW_DEPRECATED_MEMBERS_PROP = "showDeprecatedMembers"; //NOI18N

    public static final String COMPLETION_INSTANT_SUBSTITUTION_PROP = "completionInstantSubstitution"; // NOI18N
    
    public static final String FAST_IMPORT_PACKAGE_PROP = "fastImportPackage"; // NOI18N    
     
    public static final String PAIR_CHARACTERS_COMPLETION = "pairCharactersCompletion"; // NOI18N

    public static final String GOTO_CLASS_CASE_SENSITIVE_PROP = "gotoClassCaseSensitive"; //NOI18N

    public static final String GOTO_CLASS_SHOW_INNER_CLASSES_PROP = "gotoClassShowInnerClasses"; //NOI18N

    public static final String GOTO_CLASS_SHOW_LIBRARY_CLASSES_PROP = "gotoClassShowLibraryClasses"; //NOI18N

    static final String[] JAVA_PROP_NAMES = OptionSupport.mergeStringArrays(BaseOptions.BASE_PROP_NAMES, new String[] {
                                                COMPLETION_AUTO_POPUP_PROP,
                                                COMPLETION_CASE_SENSITIVE_PROP,
                                                COMPLETION_AUTO_POPUP_DELAY_PROP,
                                                FORMAT_SPACE_BEFORE_PARENTHESIS_PROP,
                                                FORMAT_COMPOUND_BRACKET_ADD_NL_PROP,
                                                JAVADOC_BGCOLOR,
                                                //JAVADOC_AUTO_POPUP_DELAY_PROP,
                                                JAVADOC_PREFERRED_SIZE_PROP,
                                                JAVADOC_AUTO_POPUP_PROP,
                                                SHOW_DEPRECATED_MEMBERS_PROP,
                                                COMPLETION_INSTANT_SUBSTITUTION_PROP,
                                                COMPLETION_NATURAL_SORT_PROP,
                                                FAST_IMPORT_PACKAGE_PROP,
                                                GOTO_CLASS_CASE_SENSITIVE_PROP,
                                                GOTO_CLASS_SHOW_INNER_CLASSES_PROP,
                                                GOTO_CLASS_SHOW_LIBRARY_CLASSES_PROP,
                                                CODE_FOLDING_PROPS_PROP,
						PAIR_CHARACTERS_COMPLETION
                                            });

    static final long serialVersionUID =-7951549840240159575L;

    public JavaOptions() {
        this(JavaKit.class, JAVA);
    }

    public JavaOptions(Class kitClass, String typeName) {
        super(kitClass, typeName);
    }

/*    public boolean getFormatSpaceBeforeParenthesis() {
        return ((Boolean)getSettingValue(JavaSettingsNames.JAVA_FORMAT_SPACE_BEFORE_PARENTHESIS)).booleanValue();
    }
    [Mila] Removed because it was moved to JavaIndentEngine; setter must stay here
*/
    public void setFormatSpaceBeforeParenthesis(boolean v) {
        setSettingBoolean(JavaSettingsNames.JAVA_FORMAT_SPACE_BEFORE_PARENTHESIS, v,
            FORMAT_SPACE_BEFORE_PARENTHESIS_PROP);

        // Need to check whether the service exists and if not, add it
//        checkJavaIndentEngineRegistered();
    }

/*    public boolean getFormatCompoundBracketAddNL() {
        return ((Boolean)getSettingValue(JavaSettingsNames.JAVA_FORMAT_NEWLINE_BEFORE_BRACE)).booleanValue();
    }
    [Mila] Removed because it was moved to JavaIndentEngine; setter must stay here
*/
    public void setFormatCompoundBracketAddNL(boolean v) {
        setSettingBoolean(JavaSettingsNames.JAVA_FORMAT_NEWLINE_BEFORE_BRACE, v,
            FORMAT_COMPOUND_BRACKET_ADD_NL_PROP);

        // Need to check whether the service exists and if not, add it
//        checkJavaIndentEngineRegistered();
    }

/*    private void checkJavaIndentEngineRegistered() {
        ServiceType.Registry sr = TopManager.getDefault().getServices();
        Enumeration en = sr.services(JavaIndentEngine.class);
        if (!en.hasMoreElements()) {
            // Need to add
            List l = sr.getServiceTypes();
            l.add(new JavaIndentEngine());
            sr.setServiceTypes(l);
        }
    }
 */

    public boolean getCompletionAutoPopup() {
        return getSettingBoolean(ExtSettingsNames.COMPLETION_AUTO_POPUP);
    }
    public void setCompletionAutoPopup(boolean v) {
        setSettingBoolean(ExtSettingsNames.COMPLETION_AUTO_POPUP, v,
            COMPLETION_AUTO_POPUP_PROP);
    }

    public boolean getCompletionCaseSensitive() {
        return getSettingBoolean(ExtSettingsNames.COMPLETION_CASE_SENSITIVE);
    }
    public void setCompletionCaseSensitive(boolean v) {
        setSettingBoolean(ExtSettingsNames.COMPLETION_CASE_SENSITIVE, v,
            COMPLETION_CASE_SENSITIVE_PROP);
    }
    
    public boolean getCompletionInstantSubstitution() {
        return getSettingBoolean(ExtSettingsNames.COMPLETION_INSTANT_SUBSTITUTION);
    }
    public void setCompletionInstantSubstitution(boolean v) {
        setSettingBoolean(ExtSettingsNames.COMPLETION_INSTANT_SUBSTITUTION, v,
            COMPLETION_INSTANT_SUBSTITUTION_PROP);
    }
    
    public int getCompletionAutoPopupDelay() {
        return getSettingInteger(ExtSettingsNames.COMPLETION_AUTO_POPUP_DELAY);
    }
    public void setCompletionAutoPopupDelay(int delay) {
        if (delay < 0) {
            NbEditorUtilities.invalidArgument("MSG_NegativeValue"); // NOI18N
            return;
        }
        setSettingInteger(ExtSettingsNames.COMPLETION_AUTO_POPUP_DELAY, delay,
            COMPLETION_AUTO_POPUP_DELAY_PROP);
    }

    /*
    public int getJavaDocAutoPopupDelay() {
        return getSettingInteger(ExtSettingsNames.JAVADOC_AUTO_POPUP_DELAY);
    }
    public void setJavaDocAutoPopupDelay(int delay) {
        if (delay < 0) {
            NbEditorUtilities.invalidArgument("MSG_NegativeValue"); // NOI18N
            return;
        }
        setSettingInteger(ExtSettingsNames.JAVADOC_AUTO_POPUP_DELAY, delay,
            JAVADOC_AUTO_POPUP_DELAY_PROP);
    }
     */
    
    public boolean getJavaDocAutoPopup() {
        return getSettingBoolean(ExtSettingsNames.JAVADOC_AUTO_POPUP);
    }
    public void setJavaDocAutoPopup(boolean auto) {
        setSettingBoolean(ExtSettingsNames.JAVADOC_AUTO_POPUP, auto,
            JAVADOC_AUTO_POPUP_PROP);
    }
    
    public Color getJavaDocBGColor() {
        return (Color)getSettingValue(ExtSettingsNames.JAVADOC_BG_COLOR);
    }
    public void setJavaDocBGColor(Color c) {
        setSettingValue(ExtSettingsNames.JAVADOC_BG_COLOR, c,
            JAVADOC_BGCOLOR);
    }
    
    public Dimension getJavaDocPreferredSize() {
        return (Dimension)getSettingValue(ExtSettingsNames.JAVADOC_PREFERRED_SIZE);
    }
    public void setJavaDocPreferredSize(Dimension d) {
        setSettingValue(ExtSettingsNames.JAVADOC_PREFERRED_SIZE, d,
            JAVADOC_PREFERRED_SIZE_PROP);
    }
    
    public boolean getShowDeprecatedMembers() {
        return getSettingBoolean(ExtSettingsNames.SHOW_DEPRECATED_MEMBERS);
    }
    public void setShowDeprecatedMembers(boolean v) {
        setSettingBoolean(ExtSettingsNames.SHOW_DEPRECATED_MEMBERS, v,
            SHOW_DEPRECATED_MEMBERS_PROP);
    }
    
    public boolean getCompletionNaturalSort() {
        return getSettingBoolean(ExtSettingsNames.COMPLETION_NATURAL_SORT);
    }
    public void setCompletionNaturalSort(boolean v) {
        setSettingBoolean(ExtSettingsNames.COMPLETION_NATURAL_SORT, v,
            COMPLETION_NATURAL_SORT_PROP);
    }
    
    public int getFastImportSelection() {
        return getSettingInteger(ExtSettingsNames.FAST_IMPORT_SELECTION);
    }
    public void setFastImportSelection(int sel) {
        setSettingInteger(ExtSettingsNames.FAST_IMPORT_SELECTION, sel,
            FAST_IMPORT_SELECTION_PROP);
    }
    
    public boolean getFastImportPackage() {
        return (getFastImportSelection() == JavaFastImport.IMPORT_PACKAGE);
    }
    public void setFastImportPackage(boolean v) {
        if (v) setFastImportSelection(JavaFastImport.IMPORT_PACKAGE);
    }

    public boolean getGotoClassCaseSensitive() {
        return getSettingBoolean(JavaSettingsNames.GOTO_CLASS_CASE_SENSITIVE);
    }
    public void setGotoClassCaseSensitive(boolean v) {
        setSettingBoolean(JavaSettingsNames.GOTO_CLASS_CASE_SENSITIVE, v, GOTO_CLASS_CASE_SENSITIVE_PROP);
    }

    public boolean getGotoClassShowInnerClasses() {
        return getSettingBoolean(JavaSettingsNames.GOTO_CLASS_SHOW_INNER_CLASSES);
    }
    public void setGotoClassShowInnerClasses(boolean v) {
        setSettingBoolean(JavaSettingsNames.GOTO_CLASS_SHOW_INNER_CLASSES, v, GOTO_CLASS_SHOW_INNER_CLASSES_PROP);
    }

    public boolean getGotoClassShowLibraryClasses() {
        return getSettingBoolean(JavaSettingsNames.GOTO_CLASS_SHOW_LIBRARY_CLASSES);
    }
    public void setGotoClassShowLibraryClasses(boolean v) {
        setSettingBoolean(JavaSettingsNames.GOTO_CLASS_SHOW_LIBRARY_CLASSES, v, GOTO_CLASS_SHOW_LIBRARY_CLASSES_PROP);
    }

    protected Class getDefaultIndentEngineClass() {
        return JavaIndentEngine.class;
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (HELP_ID);
    }

    public boolean getPairCharactersCompletion() {
      return getSettingBoolean(JavaSettingsNames.PAIR_CHARACTERS_COMPLETION);
    }

    public void setPairCharactersCompletion(boolean v) {
        setSettingBoolean(JavaSettingsNames.PAIR_CHARACTERS_COMPLETION, v,
            PAIR_CHARACTERS_COMPLETION);
    }

    public Map getCodeFoldingProps(){
        Map map = new HashMap(super.getCodeFoldingProps());
        
        Boolean val = (Boolean)getSettingValue(JavaSettingsNames.CODE_FOLDING_COLLAPSE_METHOD);
        map.put(JavaSettingsNames.CODE_FOLDING_COLLAPSE_METHOD, val);

        val = (Boolean)getSettingValue(JavaSettingsNames.CODE_FOLDING_COLLAPSE_INNERCLASS);
        map.put(JavaSettingsNames.CODE_FOLDING_COLLAPSE_INNERCLASS, val);

        val = (Boolean)getSettingValue(JavaSettingsNames.CODE_FOLDING_COLLAPSE_IMPORT);
        map.put(JavaSettingsNames.CODE_FOLDING_COLLAPSE_IMPORT, val);

        val = (Boolean)getSettingValue(JavaSettingsNames.CODE_FOLDING_COLLAPSE_JAVADOC);
        map.put(JavaSettingsNames.CODE_FOLDING_COLLAPSE_JAVADOC, val);

        val = (Boolean)getSettingValue(JavaSettingsNames.CODE_FOLDING_COLLAPSE_INITIAL_COMMENT);
        map.put(JavaSettingsNames.CODE_FOLDING_COLLAPSE_INITIAL_COMMENT, val);
        
        return map;
    }

    public void setCodeFoldingProps(Map props){
        String name = SettingsNames.CODE_FOLDING_ENABLE;
        setSettingValue(name, props.get(name));
        name = JavaSettingsNames.CODE_FOLDING_COLLAPSE_METHOD;
        setSettingValue(name, props.get(name));
        name = JavaSettingsNames.CODE_FOLDING_COLLAPSE_INNERCLASS;
        setSettingValue(name, props.get(name));
        name = JavaSettingsNames.CODE_FOLDING_COLLAPSE_IMPORT;
        setSettingValue(name, props.get(name));
        name = JavaSettingsNames.CODE_FOLDING_COLLAPSE_JAVADOC;
        setSettingValue(name, props.get(name));
        name = JavaSettingsNames.CODE_FOLDING_COLLAPSE_INITIAL_COMMENT;
        setSettingValue(name, props.get(name));
    }
    
    /**
     * Get localized string
     */
    protected String getString(String key) {
        try {
            return NbBundle.getMessage(JavaOptions.class, key);
        } catch (MissingResourceException e) {
            return super.getString(key);
        }
    }

    
}

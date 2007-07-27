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
package org.netbeans.modules.gsf;

import org.netbeans.editor.SettingsNames;
import org.netbeans.modules.editor.options.BaseOptions;
import org.netbeans.modules.editor.options.OptionSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import java.util.MissingResourceException;
import org.openide.filesystems.FileObject;

import org.netbeans.editor.ext.ExtSettingsNames;
import org.openide.util.HelpCtx;
import org.netbeans.modules.editor.NbEditorUtilities;
import java.util.MissingResourceException;
import org.netbeans.modules.editor.options.BaseOptions;
import org.openide.util.NbBundle;

public class GsfOptions extends BaseOptions {
    
    public static String LANGUAGES = "ScriptLanguages"; // NOI18N

    public static final String COMPLETION_AUTO_POPUP_PROP = "completionAutoPopup"; // NOI18N
    
    public static final String COMPLETION_CASE_SENSITIVE_PROP = "completionCaseSensitive"; // NOI18N
    
    public static final String COMPLETION_AUTO_POPUP_DELAY_PROP = "completionAutoPopupDelay"; // NOI18N

    public static final String COMPLETION_INSTANT_SUBSTITUTION_PROP = "completionInstantSubstitution"; // NOI18N
    
//    public static final String CODE_FOLDING_ENABLE_PROP = "codeFoldingEnable"; //NOI18N
//
//    
//    static final String[] LANGUAGES_PROP_NAMES = OptionSupport.mergeStringArrays (
//        BaseOptions.BASE_PROP_NAMES, 
//        new String[] {
//            CODE_FOLDING_ENABLE_PROP
//        }
//    );
    

    public static final GsfOptions create(FileObject fo) {
        String mimeType = fo.getParent().getPath().substring(8); //'Editors/'
//        System.out.println("@@@ GsfOptions.create from " + fo.getPath() + " mimeType = '" + mimeType + "'");
        return new GsfOptions(mimeType);
    }
 
    /** Name of property. */
    private static final String HELP_ID = "editing.editor.gsf"; // NOI18N
    
    
    private String mimeType;
    
    public GsfOptions(String mimeType) {
        super(GsfEditorKitFactory.GsfEditorKit.class, LANGUAGES);
        this.mimeType = mimeType;
    }
    
    protected String getContentType() {
        return mimeType;
    }

//    public boolean getCodeFoldingEnable() {
//        return getSettingBoolean(SettingsNames.CODE_FOLDING_ENABLE);
//    }
//    
//    public void setCodeFoldingEnable(boolean state) {
//        setSettingBoolean(SettingsNames.CODE_FOLDING_ENABLE, state, CODE_FOLDING_ENABLE_PROP);
//    }
    
    /**
     * Determines the class of the default indentation engine, in this case
     * LanguagesIndentEngine.class
     */
//    protected Class getDefaultIndentEngineClass() {
//        return LanguagesIndentEngine.class;
//    }
    
    /**
     * Gets the help ID
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(HELP_ID);
    }
    
    /**
     * Look up a resource bundle message, if it is not found locally defer to
     * the super implementation
     */
    protected String getString(String key) {
        try {
            return NbBundle.getMessage(GsfOptions.class, key);
        } catch (MissingResourceException e) {
            return super.getString(key);
        }
    }    

    // Copied from Java
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
    
//
//    public Map getCodeFoldingProps(){
//        Map map = new HashMap(super.getCodeFoldingProps());
//        
//        Boolean val = (Boolean)getSettingValue(JavaSettingsNames.CODE_FOLDING_COLLAPSE_METHOD);
//        map.put(JavaSettingsNames.CODE_FOLDING_COLLAPSE_METHOD, val);
//
//        val = (Boolean)getSettingValue(JavaSettingsNames.CODE_FOLDING_COLLAPSE_INNERCLASS);
//        map.put(JavaSettingsNames.CODE_FOLDING_COLLAPSE_INNERCLASS, val);
//
//        val = (Boolean)getSettingValue(JavaSettingsNames.CODE_FOLDING_COLLAPSE_IMPORT);
//        map.put(JavaSettingsNames.CODE_FOLDING_COLLAPSE_IMPORT, val);
//
//        val = (Boolean)getSettingValue(JavaSettingsNames.CODE_FOLDING_COLLAPSE_JAVADOC);
//        map.put(JavaSettingsNames.CODE_FOLDING_COLLAPSE_JAVADOC, val);
//
//        val = (Boolean)getSettingValue(JavaSettingsNames.CODE_FOLDING_COLLAPSE_INITIAL_COMMENT);
//        map.put(JavaSettingsNames.CODE_FOLDING_COLLAPSE_INITIAL_COMMENT, val);
//        
//        return map;
//    }
//
//    public void setCodeFoldingProps(Map props){
//        String name = SettingsNames.CODE_FOLDING_ENABLE;
//        setSettingValue(name, props.get(name));
//        name = JavaSettingsNames.CODE_FOLDING_COLLAPSE_METHOD;
//        setSettingValue(name, props.get(name));
//        name = JavaSettingsNames.CODE_FOLDING_COLLAPSE_INNERCLASS;
//        setSettingValue(name, props.get(name));
//        name = JavaSettingsNames.CODE_FOLDING_COLLAPSE_IMPORT;
//        setSettingValue(name, props.get(name));
//        name = JavaSettingsNames.CODE_FOLDING_COLLAPSE_JAVADOC;
//        setSettingValue(name, props.get(name));
//        name = JavaSettingsNames.CODE_FOLDING_COLLAPSE_INITIAL_COMMENT;
//        setSettingValue(name, props.get(name));
//    }
}

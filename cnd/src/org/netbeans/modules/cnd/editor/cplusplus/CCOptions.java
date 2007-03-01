/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.editor.cplusplus;

import java.awt.Color;
import java.awt.Dimension;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.options.BaseOptions;
import org.netbeans.modules.editor.options.OptionSupport;

/** Options for the CC editor kit */
public class CCOptions extends BaseOptions {
    
    static final long serialVersionUID = 6972381723748170673L;
    
    public static final String CC = "CPLUSPLUS"; //NOI18N

    public static final String DOCUMENTATION_URLBASE_PROP = "documentationURLBase"; // NOI18N

    public static final String COMPLETION_AUTO_POPUP_PROP = "completionAutoPopup"; // NOI18N

    public static final String COMPLETION_AUTO_POPUP_DELAY_PROP = "completionAutoPopupDelay"; // NOI18N
    
    public static final String COMPLETION_CASE_SENSITIVE_PROP = "completionCaseSensitive"; // NOI18N
            
    public static final String COMPLETION_INSTANT_SUBSTITUTION_PROP = "completionInstantSubstitution"; // NOI18N                
    
    public static final String COMPLETION_LOWER_CASE_PROP = "completionLowerCase"; // NOI18N    

    private static final String HELP_ID = "Welcome_opt_editor_cpp"; // !!! NOI18N

    public static final String JAVADOC_AUTO_POPUP_PROP = "javaDocAutoPopup"; //NOI18N

    public static final String JAVADOC_AUTO_POPUP_DELAY_PROP = "javaDocAutoPopupDelay"; //NOI18N
    
    public static final String JAVADOC_BGCOLOR = "javaDocBGColor"; // NOI18N
    
    public static final String JAVADOC_PREFERRED_SIZE_PROP = "javaDocPreferredSize"; //NOI18N
    
    //code folding properties
    public static final String CODE_FOLDING_UPDATE_TIMEOUT_PROP = "codeFoldingUpdateInterval"; //NOI18N
    
    public static final String CODE_FOLDING_ENABLE_PROP = "codeFoldingEnable"; //NOI18N
    
    public static final String PAIR_CHARACTERS_COMPLETION = "pairCharactersCompletion"; // NOI18N
    
    static final String[] CC_PROP_NAMES = OptionSupport.mergeStringArrays(BaseOptions.BASE_PROP_NAMES, new String[] {
                                                DOCUMENTATION_URLBASE_PROP,
                                                COMPLETION_AUTO_POPUP_PROP,
                                                COMPLETION_AUTO_POPUP_DELAY_PROP,
                                                COMPLETION_CASE_SENSITIVE_PROP,
                                                COMPLETION_INSTANT_SUBSTITUTION_PROP,
//                                                COMPLETION_LOWER_CASE_PROP,
                                                JAVADOC_AUTO_POPUP_PROP,
                                                JAVADOC_AUTO_POPUP_DELAY_PROP,
                                                JAVADOC_PREFERRED_SIZE_PROP,
                                                JAVADOC_BGCOLOR,
                                                CODE_FOLDING_UPDATE_TIMEOUT_PROP,
                                                CODE_FOLDING_ENABLE_PROP,
                                                PAIR_CHARACTERS_COMPLETION
                                            });

    public CCOptions() {
        super(CCKit.class, CC);
    }
    
    public CCOptions(Class kitClass, String typeName) {
        super(kitClass, typeName);
    }
  
    /** Return the CC Indent Engine class */
    protected Class getDefaultIndentEngineClass() {
        return CCIndentEngine.class;
    }
                                            
    public String getDocumentationURLBase() {
        String s = (String)getSettingValue(CCSettingsNames.DOCUMENTATION_URLBASE);
	if (s == null) {
	    s = CCSettingsDefaults.defaultDocURLbase;
	}
	return s;
    }
    
    public void setDocumentationURLBase(String v) {
        setSettingValue(CCSettingsNames.DOCUMENTATION_URLBASE, v,
			DOCUMENTATION_URLBASE_PROP);
    }

    public boolean getCompletionAutoPopup() {
        return getSettingBoolean(ExtSettingsNames.COMPLETION_AUTO_POPUP);
    }

    public void setCompletionAutoPopup(boolean v) {
        setSettingBoolean(ExtSettingsNames.COMPLETION_AUTO_POPUP, v, COMPLETION_AUTO_POPUP_PROP);
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
    
    public boolean getCompletionLowerCase() {
        return getSettingBoolean(CCSettingsNames.COMPLETION_LOWER_CASE); 
    }
    public void setCompletionLowerCase(boolean v) {
        setSettingBoolean(CCSettingsNames.COMPLETION_LOWER_CASE, v, 
            COMPLETION_LOWER_CASE_PROP);
    }        

    public boolean getJavaDocAutoPopup() {
        return getSettingBoolean(ExtSettingsNames.JAVADOC_AUTO_POPUP);
    }
    
    public void setJavaDocAutoPopup(boolean auto) {
        setSettingBoolean(ExtSettingsNames.JAVADOC_AUTO_POPUP, auto,
            JAVADOC_AUTO_POPUP_PROP);
    }
    
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

    public boolean getPairCharactersCompletion() {
      return getSettingBoolean(CCSettingsNames.PAIR_CHARACTERS_COMPLETION);
    }

    public void setPairCharactersCompletion(boolean v) {
        setSettingBoolean(CCSettingsNames.PAIR_CHARACTERS_COMPLETION, v,
            PAIR_CHARACTERS_COMPLETION);
    }    
    
    public int getCodeFoldingUpdateInterval() {
        return getSettingInteger(CCSettingsNames.CODE_FOLDING_UPDATE_TIMEOUT);
    }
    
    public void setCodeFoldingUpdateInterval(int timeout) {
        if (timeout < 0) {
            NbEditorUtilities.invalidArgument("MSG_NegativeValue"); // NOI18N
            return;
        }
        setSettingInteger(CCSettingsNames.CODE_FOLDING_UPDATE_TIMEOUT, timeout, CODE_FOLDING_UPDATE_TIMEOUT_PROP);
    }
    
    public boolean getCodeFoldingEnable() {
        return getSettingBoolean(SettingsNames.CODE_FOLDING_ENABLE);
    }
    
    public void setCodeFoldingEnable(boolean state) {
        setSettingBoolean(SettingsNames.CODE_FOLDING_ENABLE, state, CODE_FOLDING_ENABLE_PROP);
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(HELP_ID);
    }

    /**
     * Get the localized string from the argument. This method is called up the stack.
     * So even though its not called inside this class, its definately needed!
     */
    protected String getString(String s) {
        try {
            String res = NbBundle.getBundle(CCOptions.class).getString(s);
            return (res == null) ? super.getString(s) : res;
        }
        catch (Exception e) {
            return super.getString(s);
        }
    }
}

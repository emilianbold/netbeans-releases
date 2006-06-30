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

package org.netbeans.modules.html.editor.options;

import java.util.MissingResourceException;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.netbeans.editor.ext.html.HTMLSettingsNames;
import org.netbeans.modules.editor.html.HTMLKit;
import org.netbeans.modules.editor.html.HTMLIndentEngine;
import org.netbeans.modules.editor.options.OptionSupport;
import org.openide.util.HelpCtx;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.options.BaseOptions;
import org.openide.util.NbBundle;
import java.awt.Color;
import java.awt.Dimension;

/**
* Options for the java editor kit
*
* @author Miloslav Metelka
* @version 1.00
*/
public class HTMLOptions extends BaseOptions {

    public static final String HTML = "html"; // NOI18N

    public static final String COMPLETION_AUTO_POPUP_PROP = "completionAutoPopup"; // NOI18N

    public static final String COMPLETION_AUTO_POPUP_DELAY_PROP = "completionAutoPopupDelay"; // NOI18N
    
    public static final String COMPLETION_INSTANT_SUBSTITUTION_PROP = "completionInstantSubstitution"; // NOI18N                
    
    public static final String COMPLETION_LOWER_CASE_PROP = "completionLowerCase"; // NOI18N    

    private static final String HELP_ID = "editing.editor.html"; // !!! NOI18N

    public static final String JAVADOC_AUTO_POPUP_PROP = "javaDocAutoPopup"; //NOI18N
    
    public static final String JAVADOC_BGCOLOR = "javaDocBGColor"; // NOI18N
    
    public static final String JAVADOC_PREFERRED_SIZE_PROP = "javaDocPreferredSize"; //NOI18N
    
    //code folding properties
    public static final String CODE_FOLDING_UPDATE_TIMEOUT_PROP = "codeFoldingUpdateInterval"; //NOI18N
    
    public static final String CODE_FOLDING_ENABLE_PROP = "codeFoldingEnable"; //NOI18N
    
    static final String[] HTML_PROP_NAMES = OptionSupport.mergeStringArrays(BaseOptions.BASE_PROP_NAMES, new String[] {
                                                COMPLETION_AUTO_POPUP_PROP,
                                                COMPLETION_AUTO_POPUP_DELAY_PROP,
                                                COMPLETION_INSTANT_SUBSTITUTION_PROP,
                                                COMPLETION_LOWER_CASE_PROP,
                                                JAVADOC_AUTO_POPUP_PROP,
                                                JAVADOC_PREFERRED_SIZE_PROP,
                                                JAVADOC_BGCOLOR,
                                                CODE_FOLDING_UPDATE_TIMEOUT_PROP,
                                                CODE_FOLDING_ENABLE_PROP
                                            });

                                        
    static final long serialVersionUID = 75289734362748537L;
   
    public HTMLOptions() {
        super(HTMLKit.class, HTML);
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
    
    public boolean getCompletionInstantSubstitution() {
        return getSettingBoolean(ExtSettingsNames.COMPLETION_INSTANT_SUBSTITUTION);
    }
    public void setCompletionInstantSubstitution(boolean v) {
        setSettingBoolean(ExtSettingsNames.COMPLETION_INSTANT_SUBSTITUTION, v,
            COMPLETION_INSTANT_SUBSTITUTION_PROP);
    }        
    
    public boolean getCompletionLowerCase() {
        return getSettingBoolean(HTMLSettingsNames.COMPLETION_LOWER_CASE);
    }
    public void setCompletionLowerCase(boolean v) {
        setSettingBoolean(HTMLSettingsNames.COMPLETION_LOWER_CASE, v,
            COMPLETION_LOWER_CASE_PROP);
    }        

    protected Class getDefaultIndentEngineClass() {                             
	return HTMLIndentEngine.class;                                          
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (HELP_ID);
    }
    
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
    
    
    public int getCodeFoldingUpdateInterval() {
        return getSettingInteger(HTMLSettingsNames.CODE_FOLDING_UPDATE_TIMEOUT);
    }
    
    public void setCodeFoldingUpdateInterval(int timeout) {
        if (timeout < 0) {
            NbEditorUtilities.invalidArgument("MSG_NegativeValue"); // NOI18N
            return;
        }
        setSettingInteger(HTMLSettingsNames.CODE_FOLDING_UPDATE_TIMEOUT, timeout, CODE_FOLDING_UPDATE_TIMEOUT_PROP);
    }
    
    public boolean getCodeFoldingEnable() {
        return getSettingBoolean(SettingsNames.CODE_FOLDING_ENABLE);
    }
    
    public void setCodeFoldingEnable(boolean state) {
        setSettingBoolean(SettingsNames.CODE_FOLDING_ENABLE, state, CODE_FOLDING_ENABLE_PROP);
    }
    
    /**
     * Get localized string
     */
    protected String getString(String key) {
        try {
            return NbBundle.getMessage(HTMLOptions.class, key);
        } catch (MissingResourceException e) {
            return super.getString(key);
        }
    }

}

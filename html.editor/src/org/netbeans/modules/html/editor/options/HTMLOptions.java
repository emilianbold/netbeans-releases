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

package org.netbeans.modules.html.editor.options;

import java.util.MissingResourceException;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.netbeans.editor.ext.html.HTMLSettingsNames;
import org.netbeans.modules.editor.html.HTMLKit;
import org.netbeans.modules.editor.html.HTMLIndentEngine;
import org.netbeans.modules.editor.options.OptionSupport;
import org.openide.util.HelpCtx;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.options.BaseOptions;
import org.openide.util.NbBundle;

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
    
    static final String[] HTML_PROP_NAMES = OptionSupport.mergeStringArrays(BaseOptions.BASE_PROP_NAMES, new String[] {
                                                COMPLETION_AUTO_POPUP_PROP,
                                                COMPLETION_AUTO_POPUP_DELAY_PROP,
                                                COMPLETION_INSTANT_SUBSTITUTION_PROP,
                                                COMPLETION_LOWER_CASE_PROP
                                            });


    static final long serialVersionUID =3409313048987440397L;
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

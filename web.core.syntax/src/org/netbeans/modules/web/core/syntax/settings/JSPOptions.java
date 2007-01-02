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

package org.netbeans.modules.web.core.syntax.settings;

import org.netbeans.modules.web.core.syntax.settings.JspSettings;
import org.netbeans.modules.web.core.syntax.*;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.openide.text.IndentEngine;
import org.openide.util.Lookup;
import java.awt.Color;
import java.awt.Dimension;
import org.netbeans.editor.SettingsNames;
import org.netbeans.modules.editor.options.BaseOptions;
import org.netbeans.modules.editor.options.OptionSupport;

/**
 * Options for the java editor kit
 *                  
 * @author Petr Jiricka, Libor Kramolis
 */
public class JSPOptions extends BaseOptions {
    static final long serialVersionUID = 1954408023433388323L;
  
    /** help context ID */
    private static final String HELP_ID = "editing.editor.jsp"; // NOI18N
    
    public static final String JSP = "jsp"; // NOI18N

    public static final String COMPLETION_AUTO_POPUP_PROP = "completionAutoPopup"; // NOI18N

    public static final String COMPLETION_AUTO_POPUP_DELAY_PROP = "completionAutoPopupDelay"; // NOI18N

    public static final String JAVADOC_AUTO_POPUP_PROP = "javaDocAutoPopup"; //NOI18N
        
    public static final String JAVADOC_BGCOLOR = "javaDocBGColor"; // NOI18N
    
    public static final String JAVADOC_PREFERRED_SIZE_PROP = "javaDocPreferredSize"; //NOI18N
    
    //code folding properties
    public static final String CODE_FOLDING_UPDATE_TIMEOUT_PROP = "codeFoldingUpdateInterval"; //NOI18N
    
    public static final String CODE_FOLDING_ENABLE_PROP = "codeFoldingEnable"; //NOI18N
   
    public static final String COMPLETION_INSTANT_SUBSTITUTION_PROP = "completionInstantSubstitution"; // NOI18N                
    
    static final String[] JSP_PROP_NAMES = OptionSupport.mergeStringArrays(
                                            BaseOptions.BASE_PROP_NAMES,
                                            new String[] {
                                                COMPLETION_AUTO_POPUP_PROP,
                                                COMPLETION_AUTO_POPUP_DELAY_PROP,
                                                COMPLETION_INSTANT_SUBSTITUTION_PROP,
                                                JAVADOC_AUTO_POPUP_PROP,
                                                JAVADOC_PREFERRED_SIZE_PROP,
                                                JAVADOC_BGCOLOR,
                                                CODE_FOLDING_UPDATE_TIMEOUT_PROP,
                                                CODE_FOLDING_ENABLE_PROP
                                            });
    public JSPOptions() {
        super (JSPKit.class, JSP);
    }
  
    /** @return localized string */
    protected String getString(String s) {
        try {
            String res = NbBundle.getBundle(JSPKit.class).getString(s);
            return (res == null) ? super.getString(s) : res;
        }
        catch (Exception e) {
            return super.getString(s);
        }
    }
    
    public boolean getJavaDocAutoPopup() {
        return getSettingBoolean(ExtSettingsNames.JAVADOC_AUTO_POPUP);
    }
    
    public void setJavaDocAutoPopup(boolean auto) {
        setSettingBoolean(ExtSettingsNames.JAVADOC_AUTO_POPUP, auto,
            JAVADOC_AUTO_POPUP_PROP);
    }
    
    public boolean getCompletionAutoPopup() {
        return getSettingBoolean(ExtSettingsNames.COMPLETION_AUTO_POPUP);
    }
    
    public void setCompletionAutoPopup(boolean v) {
        setSettingBoolean(ExtSettingsNames.COMPLETION_AUTO_POPUP, v,
            COMPLETION_AUTO_POPUP_PROP);
    }

    public int getCompletionAutoPopupDelay() {
        return getSettingInteger(ExtSettingsNames.COMPLETION_AUTO_POPUP_DELAY);
    }
    public void setCompletionAutoPopupDelay(int delay) {
        setSettingInteger(ExtSettingsNames.COMPLETION_AUTO_POPUP_DELAY, delay,
            COMPLETION_AUTO_POPUP_DELAY_PROP);
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
    
    protected Class getDefaultIndentEngineClass() {
	Class engineClass = null;
	
	Lookup.Template tmp = new Lookup.Template(IndentEngine.class);
        Lookup.Result res = Lookup.getDefault().lookup(tmp);
        Set allClasses = res.allClasses();
        for (Iterator it = allClasses.iterator(); it.hasNext();) {
            Class cls = (Class)it.next();
            if (cls.getName().equals(org.netbeans.modules.web.core.syntax.formatting.JspIndentEngine.class.getName())) { //NOI18N
                engineClass = cls;
		break;
            }
        }
        
        return (engineClass != null) ? engineClass : super.getDefaultIndentEngineClass();
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx (HELP_ID);
    }    

    public int getCodeFoldingUpdateInterval() {
        return getSettingInteger(JspSettings.CODE_FOLDING_UPDATE_TIMEOUT);
    }
    
    public void setCodeFoldingUpdateInterval(int timeout) {
        if (timeout < 0) {
            NbEditorUtilities.invalidArgument("MSG_NegativeValue"); // NOI18N
            return;
        }
        setSettingInteger(JspSettings.CODE_FOLDING_UPDATE_TIMEOUT, timeout, CODE_FOLDING_UPDATE_TIMEOUT_PROP);
    }
    
   public boolean getCodeFoldingEnable() {
        return getSettingBoolean(SettingsNames.CODE_FOLDING_ENABLE);
    }
    
    public void setCodeFoldingEnable(boolean state) {
        setSettingBoolean(SettingsNames.CODE_FOLDING_ENABLE, state, CODE_FOLDING_ENABLE_PROP);
    }

    public boolean getCompletionInstantSubstitution() {
        return getSettingBoolean(ExtSettingsNames.COMPLETION_INSTANT_SUBSTITUTION);
    }
    public void setCompletionInstantSubstitution(boolean v) {
        setSettingBoolean(ExtSettingsNames.COMPLETION_INSTANT_SUBSTITUTION, v,
            COMPLETION_INSTANT_SUBSTITUTION_PROP);
    }        
    
}

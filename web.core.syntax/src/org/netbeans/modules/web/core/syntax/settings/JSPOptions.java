/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.web.core.syntax.settings;

import org.netbeans.modules.web.core.syntax.*;
import java.util.Iterator;
import java.util.Set;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.openide.text.IndentEngine;
import org.openide.util.Lookup;
import java.awt.Color;
import java.awt.Dimension;
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
   
    public static final String COMPLETION_INSTANT_SUBSTITUTION_PROP = "completionInstantSubstitution"; // NOI18N                
    
    static final String[] JSP_PROP_NAMES = OptionSupport.mergeStringArrays(
                                            BaseOptions.BASE_PROP_NAMES,
                                            new String[] {
                                                COMPLETION_AUTO_POPUP_PROP,
                                                COMPLETION_AUTO_POPUP_DELAY_PROP,
                                                COMPLETION_INSTANT_SUBSTITUTION_PROP,
                                                JAVADOC_AUTO_POPUP_PROP,
                                                JAVADOC_PREFERRED_SIZE_PROP,
                                                JAVADOC_BGCOLOR
                                            });
    public JSPOptions() {
        super (JSPKit.class, JSP);
    }
  
    /** @return localized string */
    protected @Override String getString(String s) {
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
    
    protected @Override Class getDefaultIndentEngineClass() {
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
    
    public @Override HelpCtx getHelpCtx() {
        return new HelpCtx (HELP_ID);
    }    

    public boolean getCompletionInstantSubstitution() {
        return getSettingBoolean(ExtSettingsNames.COMPLETION_INSTANT_SUBSTITUTION);
    }
    public void setCompletionInstantSubstitution(boolean v) {
        setSettingBoolean(ExtSettingsNames.COMPLETION_INSTANT_SUBSTITUTION, v,
            COMPLETION_INSTANT_SUBSTITUTION_PROP);
    }

    // maybe we should have another options class for text/x-tag too
    protected @Override String getContentType() {
        return JSPKit.JSP_MIME_TYPE;
    }
    
}

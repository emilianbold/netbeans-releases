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
package org.netbeans.modules.gsf;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.editor.SettingsNames;
import org.netbeans.modules.editor.options.BaseOptions;
import org.netbeans.modules.editor.options.MIMEOptionNode;
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
   
    /** Collapse methods by default
     * NOTE: This must be kept in sync with string literal in editor/options
     */
    public static final String CODE_FOLDING_COLLAPSE_METHOD = "code-folding-collapse-method"; //NOI18N
    
    /**
     * Collapse inner classes by default 
     * NOTE: This must be kept in sync with string literal in editor/options
     */
    public static final String CODE_FOLDING_COLLAPSE_INNERCLASS = "code-folding-collapse-innerclass"; //NOI18N
    
    /**
     * Collapse import section default
     * NOTE: This must be kept in sync with string literal in editor/options
     */
    public static final String CODE_FOLDING_COLLAPSE_IMPORT = "code-folding-collapse-import"; //NOI18N
    
    /**
     * Collapse javadoc comment by default
     * NOTE: This must be kept in sync with string literal in editor/options
     */
    public static final String CODE_FOLDING_COLLAPSE_JAVADOC = "code-folding-collapse-javadoc"; //NOI18N

    /**
     * Collapse initial comment by default
     * NOTE: This must be kept in sync with string literal in editor/options
     */
    public static final String CODE_FOLDING_COLLAPSE_INITIAL_COMMENT = "code-folding-collapse-initial-comment"; //NOI18N

    private static GsfOptions defaultInstance;
    public static synchronized GsfOptions create(FileObject fo) {
        // XXX HACK ALERT!  We need to have one options object per mime type,
        // but BaseOptions (which we have to use because the editor requires it)
        // asserts that it's a singleton. Thus we just have to use a shared
        // instance. This means the mime type looked up can be wrong.
        // This needs to get fixed better, probably by the editor dumping BaseOptions.
        if (defaultInstance == null) {
            String mimeType = fo.getParent().getPath().substring(8); //'Editors/'
            defaultInstance = new GsfOptions(mimeType);
        }
        return defaultInstance;
    }
 
    /** Name of property. */
    private static final String HELP_ID = "editing.editor.gsf"; // NOI18N
    
    
    private String mimeType;
    
    public GsfOptions(String mimeType) {
        super(GsfEditorKitFactory.GsfEditorKit.class, LANGUAGES);
        this.mimeType = mimeType;
    }
    
    @Override
    protected String getContentType() {
        return mimeType;
    }

    /**
     * Gets the help ID
     */
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(HELP_ID);
    }
    
    /**
     * Look up a resource bundle message, if it is not found locally defer to
     * the super implementation
     */
    @Override
    protected String getString(String key) {
        try {
            return NbBundle.getMessage(GsfOptions.class, key);
        } catch (MissingResourceException e) {
            return super.getString(key);
        }
    }

    @Override
    public MIMEOptionNode getMimeNode() {
        // Do not show the shared instance in Advanced Options -> Editor Settings
        return null;
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
    
    @Override
    public Map getCodeFoldingProps(){
        Map map = new HashMap(super.getCodeFoldingProps());

        Boolean val = (Boolean)getSettingValue(CODE_FOLDING_COLLAPSE_METHOD);
        map.put(CODE_FOLDING_COLLAPSE_METHOD, val);

        val = (Boolean)getSettingValue(CODE_FOLDING_COLLAPSE_INNERCLASS);
        map.put(CODE_FOLDING_COLLAPSE_INNERCLASS, val);

        val = (Boolean)getSettingValue(CODE_FOLDING_COLLAPSE_IMPORT);
        map.put(CODE_FOLDING_COLLAPSE_IMPORT, val);

        val = (Boolean)getSettingValue(CODE_FOLDING_COLLAPSE_JAVADOC);
        map.put(CODE_FOLDING_COLLAPSE_JAVADOC, val);

        val = (Boolean)getSettingValue(CODE_FOLDING_COLLAPSE_INITIAL_COMMENT);
        map.put(CODE_FOLDING_COLLAPSE_INITIAL_COMMENT, val);

        return map;
    }

    @Override
    public void setCodeFoldingProps(Map props){
        String name = SettingsNames.CODE_FOLDING_ENABLE;
        setSettingValue(name, props.get(name));
        name = CODE_FOLDING_COLLAPSE_METHOD;
        setSettingValue(name, props.get(name));
        name = CODE_FOLDING_COLLAPSE_INNERCLASS;
        setSettingValue(name, props.get(name));
        name = CODE_FOLDING_COLLAPSE_IMPORT;
        setSettingValue(name, props.get(name));
        name = CODE_FOLDING_COLLAPSE_JAVADOC;
        setSettingValue(name, props.get(name));
        name = CODE_FOLDING_COLLAPSE_INITIAL_COMMENT;
        setSettingValue(name, props.get(name));
    }
    
}

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

package org.netbeans.modules.languages.dataobject;

import java.util.Collections;
import org.netbeans.editor.SettingsNames;
import org.netbeans.modules.editor.options.BaseOptions;
import org.netbeans.modules.editor.options.MIMEOptionNode;
import org.netbeans.modules.editor.options.OptionSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.Repository;

/**
 *
 * @author Administrator
 */
public class LanguagesOptions extends BaseOptions {
    
    private static final Logger LOG = Logger.getLogger(LanguagesOptions.class.getName());
    
    public static String LANGUAGES = "Languages"; // NOI18N
    private static final String OPTIONS_LANGUAGES = "OPTIONS_" + LANGUAGES; //NOI18N

    public static final String CODE_FOLDING_ENABLE_PROP = "codeFoldingEnable"; //NOI18N

    private static LanguagesOptions defaultInstance;
    
    static final String[] LANGUAGES_PROP_NAMES = OptionSupport.mergeStringArrays (
        BaseOptions.BASE_PROP_NAMES, 
        new String[] {
            CODE_FOLDING_ENABLE_PROP
        }
    );
    
    public static synchronized LanguagesOptions create(FileObject fo) {
        if (defaultInstance == null) {
            String mimeType = fo.getParent().getPath().substring(8); //'Editors/'
    //        S ystem.out.println("@@@ LanguagesOptions.create from " + fo.getPath() + " mimeType = '" + mimeType + "'");
            defaultInstance = new LanguagesOptions(mimeType);
        } 
        return defaultInstance;
    }
    
    /** Name of property. */
    private static final String HELP_ID = "editing.editor.php"; // NOI18N
    

    private String mimeType;
    
    private LanguagesOptions(String mimeType) {
        super(LanguagesEditorKit.class, LANGUAGES);
        this.mimeType = mimeType;
//        S ystem.out.println(this + " : " + getClass ().getClassLoader ());
//        T hread.dumpStack();
    }
    
    protected String getContentType() {
        return mimeType;
    }
    
    public boolean getCodeFoldingEnable() {
        return getSettingBoolean(SettingsNames.CODE_FOLDING_ENABLE);
    }
    
    public void setCodeFoldingEnable(boolean state) {
        setSettingBoolean(SettingsNames.CODE_FOLDING_ENABLE, state, CODE_FOLDING_ENABLE_PROP);
    }
    
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

    public MIMEOptionNode getMimeNode() {
        // Do not show the shared instance in Advanced Options -> Editor Settings
        return null;
    }
    
    /**
     * Look up a resource bundle message, if it is not found locally defer to
     * the super implementation
     */
    protected String getString(String key) {
        try {
            if (OPTIONS_LANGUAGES.equals(key)) {
                return getMimeTypeDisplayName(getContentType());
            } else {
                return NbBundle.getMessage(LanguagesOptions.class, key);
            }
        } catch (MissingResourceException e) {
            return super.getString(key);
        }
    }

    private static String getMimeTypeDisplayName(String mimeType) {
        String displayName = null;
        
        FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource("Editors/" + mimeType); //NOI18N
        if (fo != null) {
            try {
                displayName = fo.getFileSystem().getStatus().annotateName(null, Collections.singleton(fo));
            } catch (FileStateInvalidException ex) {
            }

            if (displayName == null) {
                Object attrValue = fo.getAttribute("SystemFileSystem.localizingBundle"); //NOI18N
                if (attrValue instanceof String) {
                    try {
                        ResourceBundle bundle = NbBundle.getBundle((String) attrValue);
                        if (bundle != null) {
                            displayName = bundle.getString(mimeType);
                        }
                    } catch (MissingResourceException mre) {
                        LOG.log(Level.WARNING, "Can't find display name for mime type '" + mimeType + "'", mre); //NOI18N
                    }
                }
            }
        }
        
        return displayName == null ? mimeType : NbBundle.getMessage(LanguagesOptions.class, "Languages_options_name", displayName);
    }
}
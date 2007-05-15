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

package org.netbeans.modules.languages.dataobject;

import java.util.Collections;
import org.netbeans.editor.SettingsNames;
import org.netbeans.modules.editor.options.BaseOptions;
import org.netbeans.modules.editor.options.OptionSupport;
import org.netbeans.modules.languages.dataobject.LanguagesEditorKit;
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
    
    public static final LanguagesOptions create(FileObject fo) {
        if (defaultInstance == null) {
            String mimeType = fo.getParent().getPath().substring(8); //'Editors/'
    //        System.out.println("@@@ LanguagesOptions.create from " + fo.getPath() + " mimeType = '" + mimeType + "'");
            defaultInstance = new LanguagesOptions(mimeType);
        } 
        return defaultInstance;
    }
    
    /** Name of property. */
    private static final String HELP_ID = "editing.editor.php"; // NOI18N
    

    private String mimeType;
    
    public LanguagesOptions(String mimeType) {
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
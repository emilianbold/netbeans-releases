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


package org.netbeans.modules.i18n;


import java.util.ResourceBundle;

import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;


/**
 * Utilities class for I18N module.
 *
 * @author  Peter Zavadsky
 */
public abstract class I18nUtil {

    /** Property name of debug flag. */
    private static final String DEBUG = "netbeans.debug.exceptions"; // NOI18N
    
    /** Items for init format customizer. */
    private static String[] initFormatItems;

    /** Help description for init format customizer. */
    private static String[][] initHelpItems;

    /** Items for replace format customizer. */
    private static String[] replaceFormatItems;

    /** Help description for replace format customizer. */
    private static String[][] replaceHelpItems;

    /** Items for regular expression customizer. */
    private static String[] regExpItems;

    /** Help description for regular expression customizer. */
    private static String[][] regExpHelpItems;
    
    /** Items for i18n regular expression customizer. */
    private static String[] i18nRegExpItems;

    /** Resource bundle used in i18n module. */
    private static ResourceBundle bundle;
    
    
    /** Gets <code>initFormatItems</code>. */
    public static String[] getInitFormatItems() { 
        if(initFormatItems == null) {
            initFormatItems = new String[] {
                "java.util.ResourceBundle.getBundle(\"{bundleNameSlashes}\")", // NOI18N
                "org.openide.util.NbBundle.getBundle({sourceFileName}.class)" // NOI18N
            };
        }
              
        return initFormatItems;
    }

    /** Gets <code>InitHelpFormats</code>. */
    public static String[][] getInitHelpItems() {
        if(initHelpItems == null) {
            initHelpItems = new String[][] {
                new String[] {"{bundleNameSlashes}","{bundleNameDots}","{sourceFileName}"}, // NOI18N
                new String[] {
                    getBundle().getString("TXT_PackageNameSlashes"),
                    getBundle().getString("TXT_PackageNameDots"),
                    getBundle().getString("TXT_SourceDataObjectName")
                }
            };
        }
         
        return initHelpItems;
    }

    /** Gets <code>replaceFormatItems</code>. */
    public static String[] getReplaceFormatItems() {
        if(replaceFormatItems == null) {
            replaceFormatItems = new String[] {
                "{identifier}.getString(\"{key}\")", // NOI18N
                "Utilities.getString(\"{key}\")", // NOI18N
                "java.util.ResourceBundle.getBundle(\"{bundleNameSlashes}\").getString(\"{key}\")", // NOI18N
                "org.openide.util.NbBundle.getBundle({sourceFileName}.class).getString(\"{key}\")" // NOI18N
            };
        }
            
        return replaceFormatItems;
    }

    /** Gets <code>replaceHeplItems</code>.*/
    public static String[][] getReplaceHelpItems() {
        if(replaceHelpItems == null) {
            replaceHelpItems = new String[][] { 
                new String[] {"{identifier}", "{key}", "{bundleNameSlashes}", "{bundleNameDots}", "{sourceFileName}"}, // NOI18N
                new String[] {
                    getBundle().getString("TXT_FieldIdentifier"),
                    getBundle().getString("TXT_KeyHelp"),
                    getBundle().getString("TXT_PackageNameSlashes"),
                    getBundle().getString("TXT_PackageNameDots"),
                    getBundle().getString("TXT_SourceDataObjectName")
                }
            };
        }
            
        return replaceHelpItems;
    }

    /** Gets <code>regExpItems</code>. */
    public static String[] getRegExpItems() {
        if(regExpItems == null) {
            regExpItems = new String[] {
                "(getString|getBundle)([:space:]*)\\(([:space:])*{hardString}", // NOI18N
                "// NOI18N", // NOI18N
                "((getString|getBundle)([:space:]*)\\(([:space:])*{hardString})|(// NOI18N)" // NOI18N
            };
        }
            
        return regExpItems;
    }
    
    /** Gets <code>i18nRegExpItems</code>. */
    public static String[] getI18nRegExpItems() {
        if(i18nRegExpItems == null) {
            i18nRegExpItems = new String[] {
                "(getString)([:space:]*)\\(([:space:])*{hardString}", // NOI18N
                "(getString|getMessage)([:space:]*)\\(([:space:])*{hardString}", // NOI18N
            };
        }
            
        return i18nRegExpItems;
    }
    
    /** Gets <code>regExpHelpItems</code>. */
    public static String[][] getRegExpHelpItems() {
        if(regExpHelpItems == null) {
            regExpHelpItems = new String[][] {
                new String[] {
                    "{hardString}", // NOI18N
                    "[:alnum:]", // NOI18N
                    "[:alpha:]", // NOI18N
                    "[:blank:]", // NOI18N
                    "[:cntrl:]", // NOI18N
                    "[:digit:]", // NOI18N
                    "[:graph:]", // NOI18N
                    "[:lower:]", // NOI18N
                    "[:print:]", // NOI18N
                    "[:punct:]", // NOI18N
                    "[:space:]", // NOI18N
                    "[:upper:]", // NOI18N
                    "[:xdigit:]", // NOI18N
                    "[:javastart:]", // NOI18N
                    "[:javapart:]" // NOI18N
                },
                new String[] {
                    getBundle().getString("TXT_HardString"),
                    getBundle().getString("TXT_Alnum"),
                    getBundle().getString("TXT_Alpha"),
                    getBundle().getString("TXT_Blank"),
                    getBundle().getString("TXT_Cntrl"),
                    getBundle().getString("TXT_Digit"),
                    getBundle().getString("TXT_Graph"),
                    getBundle().getString("TXT_Lower"),
                    getBundle().getString("TXT_Print"),
                    getBundle().getString("TXT_Punct"),
                    getBundle().getString("TXT_Space"),
                    getBundle().getString("TXT_Upper"),
                    getBundle().getString("TXT_Xdigit"),
                    getBundle().getString("TXT_Javastart"),
                    getBundle().getString("TXT_Javapart")
                }
            };
        }
        
        return regExpHelpItems;
    }

    /** Gets resource bundle for i18n module. */
    public static ResourceBundle getBundle() {
        if(bundle == null)
            bundle = NbBundle.getBundle(I18nModule.class);
        
        return bundle;
    }
    
    /** Gets i18n options. */
    public static I18nOptions getOptions() {
        return (I18nOptions)SharedClassObject.findObject(I18nOptions.class, true);
    }
    
}

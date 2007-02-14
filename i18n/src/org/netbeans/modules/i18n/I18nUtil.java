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


package org.netbeans.modules.i18n;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;
import org.netbeans.api.queries.VisibilityQuery;

/**
 * Utilities class for I18N module.
 *
 * @author  Peter Zavadsky
 */
public final class I18nUtil {

    /** Help ID for i18n module in general. */
    public static final String HELP_ID_I18N = "internation.internation"; // NOI18N
    /** Help ID for I18N dialog. */
    public static final String HELP_ID_AUTOINSERT = "internation.autoinsert"; // NOI18N
    /** Help ID for Insert I18N dialog. */
    public static final String HELP_ID_MANINSERT = "internation.maninsert"; // NOI18N
    /** Help ID for I18N form property editor. You can see it in Component inspector. */
    public static final String HELP_ID_FORMED = "internation.formed"; // NOI18N
    /** Help ID for I18N test wizard. */
    public static final String HELP_ID_TESTING = "internation.testing"; // NOI18N
    /** Help ID for I18N wizard. */
    public static final String HELP_ID_WIZARD = "internation.wizard"; // NOI18N
    /** Help ID for I18N options. */
    public static final String HELP_ID_CUSTOM = "internation.custom"; // NOI18N
    /** Help ID for parameters dialog. */
    public static final String HELP_ID_ADDPARAMS = "internation.addparams"; // NOI18N
    /** Help ID for replacing format. */
    public static final String HELP_ID_REPLFORMAT = "internation.replformat"; // NOI18N
    /** Help ID for Locale execution. */
    public static final String HELP_ID_RUNLOCALE = "internation.runlocale"; // NOI18N
    
    /** Help ID for property editor */
    public static final String PE_REPLACE_CODE_HELP_ID = "i18n.pe.replacestring"; // NOI18N
    /** Help ID for property editor */
    public static final String PE_I18N_REGEXP_HELP_ID = "i18n.pe.i18nregexp";   // NOI18N
    /** Help ID for property editor */
    public static final String PE_BUNDLE_CODE_HELP_ID = "i18n.pe.bundlestring"; // NOI18N
    /** Help ID for property editor */
    public static final String PE_TEST_REGEXP_HELP_ID = "i18n.pe.testregexp";   // NOI18N
    /** Help ID for javaI18nString. It is a universal one for all subclasses. */
    public static final String PE_I18N_STRING_HELP_ID = "i18n.pe.i18nString";   // NOI18N

    
    /** Items for init format customizer. */
    private static List initFormatItems;

    /** Help description for init format customizer. */
    private static List initHelpItems;

    /** Items for replace format customizer. */
    private static List replaceFormatItems;

    /** Help description for replace format customizer. */
    private static List replaceHelpItems;

    /** Items for regular expression customizer. */
    private static List regExpItems;

    /** Help description for regular expression customizer. */
    private static List regExpHelpItems;
    
    /** Items for i18n regular expression customizer. */
    private static List i18nRegExpItems;

    /** Gets <code>initFormatItems</code>. */
    public static List getInitFormatItems() { 
        if(initFormatItems == null) {
            initFormatItems = new ArrayList(2);
            initFormatItems.add("java.util.ResourceBundle.getBundle(\"{bundleNameSlashes}\")"); // NOI18N
            initFormatItems.add("org.openide.util.NbBundle.getBundle({sourceFileName}.class)"); // NOI18N
        }
              
        return initFormatItems;
    }

    /** Gets <code>InitHelpFormats</code>. */
    public static List getInitHelpItems() {
        if(initHelpItems == null) {
            ResourceBundle bundle = getBundle();
            initHelpItems = new ArrayList(3);
            initHelpItems.add("{bundleNameSlashes} - "+ bundle.getString("TXT_PackageNameSlashes")); // NOI18N
            initHelpItems.add("{bundleNameDots} - " + bundle.getString("TXT_PackageNameDots")); // NOI18N
            initHelpItems.add("{sourceFileName} - " + bundle.getString("TXT_SourceDataObjectName")); // NOI18N
        }
         
        return initHelpItems;
    }

    /** Gets <code>replaceFormatItems</code>. */
    public static List getReplaceFormatItems() {
        if(replaceFormatItems == null) {
            replaceFormatItems = new ArrayList(7);
            replaceFormatItems.add("{identifier}.getString(\"{key}\")"); // NOI18N
            replaceFormatItems.add("Utilities.getString(\"{key}\")"); // NOI18N
            replaceFormatItems.add("java.util.ResourceBundle.getBundle(\"{bundleNameSlashes}\").getString(\"{key}\")"); // NOI18N
            replaceFormatItems.add("org.openide.util.NbBundle.getBundle({sourceFileName}.class).getString(\"{key}\")"); // NOI18N
            replaceFormatItems.add("java.text.MessageFormat.format(java.util.ResourceBundle.getBundle(\"{bundleNameSlashes}\").getString(\"{key}\"), {arguments})"); // NOI18N
            replaceFormatItems.add("org.openide.util.NbBundle.getMessage({sourceFileName}.class, \"{key}\")"); // NOI18N
            replaceFormatItems.add("org.openide.util.NbBundle.getMessage({sourceFileName}.class, \"{key}\", {arguments})"); // NOI18N
        }
            
        return replaceFormatItems;
    }

    /** Gets default replace format - based on whether the project type is
     * a NB module project or not. (Module projects use NbBundle preferentially.)
     */
    public static String getDefaultReplaceFormat(boolean nbProject) {
        return (String) getReplaceFormatItems().get(nbProject ? 5 : 2);
    }

    /** Gets <code>replaceHeplItems</code>.*/
    public static List getReplaceHelpItems() {
        if(replaceHelpItems == null) {
            ResourceBundle bundle = getBundle();
            replaceHelpItems = new ArrayList(6);
            replaceHelpItems.add("{identifier} - " + bundle.getString("TXT_FieldIdentifier")); // NOI18N
            replaceHelpItems.add("{key} - " + bundle.getString("TXT_KeyHelp")); // NOI18N
            replaceHelpItems.add("{bundleNameSlashes} - " + bundle.getString("TXT_PackageNameSlashes")); // NOI18N
            replaceHelpItems.add("{bundleNameDots} - " + bundle.getString("TXT_PackageNameDots")); // NOI18N
            replaceHelpItems.add("{sourceFileName} - " + bundle.getString("TXT_SourceDataObjectName")); // NOI18N
            replaceHelpItems.add("{arguments} - " + bundle.getString("TXT_Arguments")); // NOI18N
        }
            
        return replaceHelpItems;
    }

    /** Gets <code>regExpItems</code>. */
    public static List getRegExpItems() {
        if(regExpItems == null) {
            regExpItems = new ArrayList(4);
            regExpItems.add("(getString|getBundle)[:space:]*\\([:space:]*{hardString}|// *NOI18N"); // NOI18N
            regExpItems.add("(getString|getBundle)[:space:]*\\([:space:]*{hardString}"); // NOI18N
            regExpItems.add("// *NOI18N"); // NOI18N
            regExpItems.add("(getString|getBundle)[:space:]*\\([:space:]*|getMessage[:space:]*\\(([:alnum:]|[:punct:]|[:space:])*,[:space:]*{hardString}|// *NOI18N"); // NOI18N
        }
            
        return regExpItems;
    }
    
    /** Gets <code>i18nRegExpItems</code>. */
    public static List getI18nRegExpItems() {
        if(i18nRegExpItems == null) {
            i18nRegExpItems = new ArrayList(2);
            i18nRegExpItems.add("getString[:space:]*\\([:space:]*{hardString}"); // NOI18N
            i18nRegExpItems.add("(getString[:space:]*\\([:space:]*|getMessage[:space:]*\\(([:alnum:]|[:punct:]|[:space:])*,[:space:]*){hardString}"); // NOI18N
        }
            
        return i18nRegExpItems;
    }
    
    /** Gets <code>regExpHelpItems</code>. */
    public static List getRegExpHelpItems() {
        if(regExpHelpItems == null) {
            ResourceBundle bundle = getBundle();
            regExpHelpItems = new ArrayList(13);
            regExpHelpItems.add("{hardString} - " + bundle.getString("TXT_HardString")); // NOI18N
            regExpHelpItems.add("[:alnum:] - " + bundle.getString("TXT_Alnum")); // NOI18N
            regExpHelpItems.add("[:alpha:] - " + bundle.getString("TXT_Alpha")); // NOI18N
            regExpHelpItems.add("[:blank:] - " + bundle.getString("TXT_Blank")); // NOI18N
            regExpHelpItems.add("[:cntrl:] - " + bundle.getString("TXT_Cntrl")); // NOI18N
            regExpHelpItems.add("[:digit:] - " + bundle.getString("TXT_Digit")); // NOI18N
            regExpHelpItems.add("[:graph:] - " + bundle.getString("TXT_Graph")); // NOI18N
            regExpHelpItems.add("[:lower:] - " + bundle.getString("TXT_Lower")); // NOI18N
            regExpHelpItems.add("[:print:] - " + bundle.getString("TXT_Print")); // NOI18N
            regExpHelpItems.add("[:punct:] - " + bundle.getString("TXT_Punct")); // NOI18N
            regExpHelpItems.add("[:space:] - " + bundle.getString("TXT_Space")); // NOI18N
            regExpHelpItems.add("[:upper:] - " + bundle.getString("TXT_Upper")); // NOI18N
            regExpHelpItems.add("[:xdigit:] - " + bundle.getString("TXT_Xdigit")); // NOI18N
            //regExpHelpItems.add("[:javastart:] - " + bundle.getString("TXT_Javastart")); // NOI18N
            //regExpHelpItems.add("[:javapart:] - " + bundle.getString("TXT_Javapart")); // NOI18N
        }
        
        return regExpHelpItems;
    }

    /** 
     * Indicates if folder or its subfolders contains data object
     * that is supported by any internationalization factory. 
     */
    public static boolean containsAcceptedDataObject(DataFolder folder) {
        DataObject[] children = folder.getChildren();
        DataObject[] folders = new DataObject[children.length];
        int i, foldersCount = 0;

        for (i = 0; i < children.length; i++) {
            if (children[i] instanceof DataFolder) {  
                folders[foldersCount++] = children[i];
            } else if (FactoryRegistry.hasFactory(children[i].getClass())) {
                return true;
            }
        }
        for (i = 0; i < foldersCount; i++) {
            if (containsAcceptedDataObject((DataFolder) children[i])) {
                return true;
            }
        }
        return false;
    }
    
    /** 
     * Recursivelly get all accepted data objects starting from given folder. 
     */
    public static List getAcceptedDataObjects(DataObject.Container folder) {
        List accepted = new ArrayList();
        
        final VisibilityQuery visQuery = VisibilityQuery.getDefault();

        DataObject[] children = folder.getChildren();

        for(int i = 0; i < children.length; i++) {
            if (!visQuery.isVisible(children[i].getPrimaryFile())) {
                continue;
            }
            if(children[i] instanceof DataObject.Container) {
                accepted.addAll(getAcceptedDataObjects((DataObject.Container)children[i]));
            } else {
                if(FactoryRegistry.hasFactory(children[i].getClass()))
                    accepted.add(children[i]);
            }
        }

        return accepted;
    }
    
    /** Gets resource bundle for i18n module. */
    public static ResourceBundle getBundle() {
        return NbBundle.getBundle(I18nUtil.class);
    }
    
    /** Gets i18n options. */
    public static I18nOptions getOptions() {
        return I18nOptions.getDefault();
    }
    

}

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


import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.openide.cookies.SourceCookie;
import org.openide.loaders.DataObject;
import org.openide.src.ClassElement;
import org.openide.src.FieldElement;
import org.openide.src.Identifier;
import org.openide.src.SourceElement;
import org.openide.src.SourceException;
import org.openide.src.Type;
import org.openide.util.MapFormat;
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
    
    /** Gets the string used to replace found hardcoded string. */
    public static String getReplaceJavaCode(JavaI18nString javaI18nString, DataObject sourceDataObject) {
        if(javaI18nString != null) {
            if(javaI18nString.getSupport().getResourceHolder().getResource() != null && javaI18nString.getKey() != null) {
                String replaceJavaFormat = javaI18nString.getReplaceFormat();
                
                if(replaceJavaFormat == null)
                    replaceJavaFormat = ((I18nOptions)SharedClassObject.findObject(I18nOptions.class, true)).getReplaceJavaCode();

                // Create map.
                Map map = new HashMap(5);
                
                map.put("identifier", ((JavaI18nSupport)javaI18nString.getSupport()).getIdentifier()); // NOI18N
                map.put("key", javaI18nString.getKey()); // NOI18N
                map.put("bundleNameSlashes", javaI18nString.getSupport().getResourceHolder().getResource().getPrimaryFile().getPackageName('/')); // NOI18N
                map.put("bundleNameDots", javaI18nString.getSupport().getResourceHolder().getResource().getPrimaryFile().getPackageName('.')); // NOI18N
                map.put("sourceFileName", sourceDataObject == null ? "" : sourceDataObject.getPrimaryFile().getName()); // NOI18N
                
                // Gets the default replace string.
                String result = MapFormat.format(
                    replaceJavaFormat,
                    map
                );
                    
                // If arguments were set get the message format replace string.
                String[] arguments = javaI18nString.getArguments();
                if (arguments.length > 0) {
                    StringBuffer stringBuffer = new StringBuffer("java.text.MessageFormat.format("); // NOI18N
                    stringBuffer.append(result);
                    stringBuffer.append(", new Object[] {"); // NOI18N
                    for (int i = 0; i < arguments.length; i++) {
                        stringBuffer.append(arguments[i]);
                        if (i < arguments.length - 1)
                            stringBuffer.append(", "); // NOI18N
                    }
                    stringBuffer.append("})"); // NOI18N
                    result = stringBuffer.toString();
                }
                return result;
            }
        }
        return null;
    }
    
    /** Creates a new field in java source hierarchy. 
     * @param javaI18nString which holds info about going-to-be created field element
     * @param sourceDataObject object to which source will be new field added,
     * the object have to have <code>SourceCookie</code>
     * @see org.openide.cookies.SourceCookie */
    public static void createField(JavaI18nString javaI18nString) {
        JavaI18nSupport javaI18nSupport = (JavaI18nSupport)javaI18nString.getSupport();
        
        // Check if we have to generate field.
        if(!javaI18nSupport.isGenerateField())
            return;

        ClassElement sourceClass = getSourceClassElement(javaI18nSupport.getSourceDataObject());

        if(sourceClass.getField(Identifier.create(javaI18nSupport.getIdentifier())) != null)
            // Field with such identifer exsit already, do nothing.
            return;
        
        try {
            FieldElement newField = new FieldElement();
            newField.setName(Identifier.create(javaI18nSupport.getIdentifier()));
            newField.setModifiers(javaI18nSupport.getModifiers());
            newField.setType(Type.parse("java.util.ResourceBundle")); // NOI18N
            newField.setInitValue(getInitJavaCode(javaI18nString));
            
            if(sourceClass != null)
                // Trying to add new field.
                sourceClass.addField(newField);
        } catch(SourceException se) {
            // do nothing, means the field already exist
            if(Boolean.getBoolean(DEBUG)) // NOI18N
                se.printStackTrace();
        } catch(NullPointerException npe) {
            // something wrong happened, probably sourceDataObject was not initialized
            if(Boolean.getBoolean(DEBUG)) // NOI18N
                npe.printStackTrace();
        }

    }

    /** 
     * Helper method. Gets the string, the piece of code which initializes
     * field resource bundle in the source.
     * <p>
     * java.util.ResourceBundle <identifier name> = <b>java.util.ResourceBundle.getBundle("<package name></b>")
     * @return String -> piece of initilizing code. */
    private static String getInitJavaCode(JavaI18nString javaI18nString) {
        if(javaI18nString == null)
            return null;
            
        String initJavaFormat = ((JavaI18nSupport)javaI18nString.getSupport()).getInitFormat();

        if(initJavaFormat == null)
            initJavaFormat = ((I18nOptions)SharedClassObject.findObject(I18nOptions.class, true)).getInitJavaCode();

        // Create map.
        Map map = new HashMap(3);

        map.put("bundleNameSlashes", javaI18nString.getSupport().getResourceHolder().getResource().getPrimaryFile().getPackageName('/')); // NOI18N
        map.put("bundleNameDots", javaI18nString.getSupport().getResourceHolder().getResource().getPrimaryFile().getPackageName('.')); // NOI18N
        map.put("sourceFileName", javaI18nString.getSupport().getSourceDataObject().getPrimaryFile().getName()); // NOI18N

        return MapFormat.format(initJavaFormat, map);
    }
    
    /** Helper method. Finds main top-level class element for <code>sourceDataObject</code> which should be initialized. */
    private static ClassElement getSourceClassElement(DataObject sourceDataObject) {
        SourceElement sourceElem = ((SourceCookie)sourceDataObject.getCookie(SourceCookie.class)).getSource();
        ClassElement sourceClass = sourceElem.getClass(Identifier.create(sourceDataObject.getName()));
        
        if(sourceClass != null)
            return sourceClass;
        
        ClassElement[] classes = sourceElem.getClasses();
        
        // find source class
        for(int i=0; i<classes.length; i++) {
            int modifs = classes[i].getModifiers();
            if(classes[i].isClass() && Modifier.isPublic(modifs)) {
                sourceClass = classes[i];
                break;
            }
        }
        
        return sourceClass;
    }

    /** Gets resource bundle for i18n module. */
    public static ResourceBundle getBundle() {
        if(bundle == null)
            bundle = NbBundle.getBundle(I18nModule.class);
        
        return bundle;
    }
    
}

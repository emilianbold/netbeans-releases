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
import java.util.ResourceBundle;
import java.text.MessageFormat;

import org.netbeans.modules.properties.BundleStructure;
import org.netbeans.modules.properties.Element;
import org.netbeans.modules.properties.PropertiesOpen;
import org.netbeans.modules.properties.PropertiesStructure;

import org.openide.cookies.SourceCookie;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.DataObject;
import org.openide.src.ClassElement;
import org.openide.src.FieldElement;
import org.openide.src.Identifier;
import org.openide.src.SourceElement;
import org.openide.src.SourceException;
import org.openide.src.Type;
import org.openide.TopManager;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;


/**
 * Utilities class for internationalization module.
 *
 * @author  Peter Zavadsky
 */
public abstract class I18nUtil {

    /** Bundle with internationalized strings for this source. */
    private static final ResourceBundle bundle = NbBundle.getBundle(I18nModule.class);
    
    /** Property name of debug flag. */
    private static final String DEBUG = "netbeans.debug.exceptions"; // NOI18N
    
    /** Items for init format customizer. */
    public static final String[] initFormatItems = new String[] {
        "java.util.ResourceBundle.getBundle(\"{0}\")", // NOI18N
        "org.openide.util.NbBundle.getBundle({2}.class)" // NOI18N
    };

    /** Help description for init format customizer. */
    public static final String[][] initHelpItems = new String[][] {
        new String[] {"{0}","{1}","{2}"}, // NOI18N
        new String[] {
            bundle.getString("TXT_PackageNameSlashes"),
            bundle.getString("TXT_PackageNameDots"),
            bundle.getString("TXT_TargetDataObjectName")
        }
    };

    /** Items for replace format customizer. */
    public static final String[] replaceFormatItems = new String[] {
        "{0}.getString(\"{1}\")", // NOI18N
        "Utilities.getString(\"{1}\")", // NOI18N
        "java.util.ResourceBundle.getBundle(\"{2}\").getString(\"{1}\")", // NOI18N
        "org.openide.util.NbBundle.getBundle({4}.class).getString(\"{1}\")" // NOI18N
    };

    /** Help description for replace format customizer. */
    public static final String[][] replaceHelpItems = new String[][] { 
        new String[] {"{0}", "{1}", "{2}", "{3}", "{4}"}, // NOI18N
        new String[] {
            bundle.getString("TXT_FieldIdentifier"),
            bundle.getString("TXT_Key"),
            bundle.getString("TXT_PackageNameSlashes"),
            bundle.getString("TXT_PackageNameDots"),
            bundle.getString("TXT_TargetDataObjectName")
        }
    };

    /** Items for regular expression customizer. */
    public static final String[] regExpItems = new String[] {
        "(getString|getBundle)([:space:]*)\\(([:space:])*{0}", // NOI18N
        "// NOI18N", // NOI18N
        "((getString|getBundle)([:space:]*)\\(([:space:])*{0})|(// NOI18N)" // NOI18N
    };
    
    /** Help description for regular expression customizer. */
    public static final String[][] regExpHelpItems = new String[][] {
        new String[] {
            "{0}", // NOI18N
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
            bundle.getString("TXT_HardString"),
            bundle.getString("TXT_Alnum"),
            bundle.getString("TXT_Alpha"),
            bundle.getString("TXT_Blank"),
            bundle.getString("TXT_Cntrl"),
            bundle.getString("TXT_Digit"),
            bundle.getString("TXT_Graph"),
            bundle.getString("TXT_Lower"),
            bundle.getString("TXT_Print"),
            bundle.getString("TXT_Punct"),
            bundle.getString("TXT_Space"),
            bundle.getString("TXT_Upper"),
            bundle.getString("TXT_Xdigit"),
            bundle.getString("TXT_Javastart"),
            bundle.getString("TXT_Javapart")
        }
    };
    
    /** Gets the string used to replace found hardcoded string. */
    public static String getReplaceJavaCode(ResourceBundleString rbString, DataObject targetDataObject) {
        if(rbString != null) {
            if(rbString.getResourceBundle() != null && rbString.getKey() != null) {
                String replaceJavaFormat = rbString.getReplaceFormat();
                
                if(replaceJavaFormat == null)
                    replaceJavaFormat = ((I18nOptions)SharedClassObject.findObject(I18nOptions.class, true)).getReplaceJavaCode();
                
                // Gets the default replace string.
                String result = MessageFormat.format(
                    replaceJavaFormat, 
                    new Object[] {
                        rbString.getIdentifier(), // {0}
                        rbString.getKey(),         // {1}
                        rbString.getResourceBundle().getPrimaryFile().getPackageName('/'),                  // {2}
                        rbString.getResourceBundle().getPrimaryFile().getPackageName('.'),                  // {3}
                        targetDataObject == null ? "" : targetDataObject.getPrimaryFile().getName() // NOI18N // {4}
                    }
                );
                    
                // If arguments were set get the message format replace string.
                String[] arguments = rbString.getArguments();
                if (arguments.length > 0) {
                    StringBuffer sb = new StringBuffer("java.text.MessageFormat.format("); // NOI18N
                    sb.append(result);
                    sb.append(", new Object[] {"); // NOI18N
                    for (int i = 0; i < arguments.length; i++) {
                        sb.append(arguments[i]);
                        if (i < arguments.length - 1)
                            sb.append(", "); // NOI18N
                    }
                    sb.append("})"); // NOI18N
                    result = sb.toString();
                }
                return result;
            }
        }
        return null;
    }
    
    /** Creates a new field in java source hierarchy. 
     * @param rbString which holds info about going-to-be created field element
     * @param targetDataObject object to which source will be new field added,
     * the object have to have <code>SourceCookie</code>
     * @see org.openide.cookies.SourceCookie */
    public static void createField(ResourceBundleString rbString, DataObject targetDataObject) {
        if(!rbString.getGenerateField())
            return;
        
        try {
            FieldElement newField = new FieldElement();
            newField.setName(Identifier.create(rbString.getIdentifier()));
            newField.setModifiers(rbString.getModifiers());
            newField.setType(Type.parse("java.util.ResourceBundle")); // NOI18N
            newField.setInitValue(getInitJavaCode(rbString, targetDataObject));
            
            final ClassElement sourceClass = getSourceClassElement(targetDataObject);
            
            if(sourceClass != null)
                // Trying to add new field.
                sourceClass.addField(newField);
        } catch(SourceException se) {
            // do nothing, means the field already exist
            if(Boolean.getBoolean(DEBUG)) // NOI18N
                se.printStackTrace();
        } catch(NullPointerException npe) {
            // something wrong happened, probably targetDataObject was not initialized
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
    private static String getInitJavaCode(ResourceBundleString rbString, DataObject targetDataObject) {
        if(rbString != null) {
            String initJavaFormat = rbString.getInitFormat();
            
            if(initJavaFormat == null)
                initJavaFormat = ((I18nOptions)SharedClassObject.findObject(I18nOptions.class, true)).getInitJavaCode();
            
            return MessageFormat.format(initJavaFormat, new Object[] {
                rbString.getResourceBundle().getPrimaryFile().getPackageName('/'),                  // {0}
                rbString.getResourceBundle().getPrimaryFile().getPackageName('.'),                  // {1}
                targetDataObject == null ? "" : targetDataObject.getPrimaryFile().getName() // NOI18N // {2}
            });
        } else
            return null;
    }
    
    /** Helper method. Finds main top-level class element for <code>targetDataObject</code> which should be initialized. */
    private static ClassElement getSourceClassElement(DataObject targetDataObject) {
        SourceElement sourceElem = ((SourceCookie)targetDataObject.getCookie(SourceCookie.class)).getSource();
        ClassElement sourceClass = sourceElem.getClass(Identifier.create(targetDataObject.getName()));
        
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

    /** Attempts to create a new key corresponding to its settings in the resource bundle and opens open support of that bundle. */
    public static void addKeyToBundle(ResourceBundleString rbString) {
        if((rbString.getResourceBundle() == null) || (rbString.getKey() == null))
            return;
        try {
            BundleStructure bundleStructure = rbString.getResourceBundle().getBundleStructure();
            
            String key = rbString.getKey();
            String value = rbString.getDefaultValue();
            String comment = rbString.getDefaultComment();
            
            for(int i=0; i<bundleStructure.getEntryCount(); i++) {
                PropertiesStructure propStructure = bundleStructure.getNthEntry(i).getHandler().getStructure();
                org.netbeans.modules.properties.Element.ItemElem item = propStructure.getItem(key);

                if (item == null) {
                    // Item doesn't exist in this entry -> create it.
                    propStructure.addItem(key, value, comment);
                } // else { // PENDING>>
                    // if item exist in that entry leave it unchanged, don't reset the old value.
                    // Maybe it would be nice if to reset the existing value or leave it unchanged
                    // propbably the best is to make it optional for user -> I18N OPTIONS.
                //} // PENDING<<
            }
            
        } catch (NullPointerException e) {
            if(Boolean.getBoolean(DEBUG)) // NOI18N
                e.printStackTrace();
            TopManager.getDefault().notifyException(e);
        } finally {
            rbString.setDefaultValue(null);
            rbString.setDefaultComment(null);
        }
        
        // Open table component if is not opened already.
        try {
            PropertiesOpen po = (PropertiesOpen)rbString.getResourceBundle().getCookieSet().getCookie(OpenCookie.class);
            if(!po.hasOpenedTableComponent())
                po.open();
        } catch(NullPointerException npe) {
            if(Boolean.getBoolean(DEBUG)) // NOI18N
                npe.printStackTrace();
        }
    }

    /** Gets the value of the property for key from the resource bundle specified in given <code>ResourceBundleString</code>. */
    public static String getExistingValue(ResourceBundleString rbString) {
        Element.ItemElem item = getItem(rbString);
        return (item != null) ? item.getValue() : null;
    }

    /** Gets the comment of the property for key from the resource bundle specified in given <code>ResourceBundleString</code>. */
    public static String getExistingComment(ResourceBundleString rbString) {
        Element.ItemElem item = getItem(rbString);
        return (item != null) ? item.getComment() : null;
    }
    
    /** Helper method. Returns the item for the property from the bundle or null if either the bundle or the key is not valid. */
    private static Element.ItemElem getItem(ResourceBundleString rbString) {
        if ((rbString.getResourceBundle() == null) || (rbString.getKey() == null))
            return null;
        BundleStructure bundleStructure = rbString.getResourceBundle().getBundleStructure();
        if (bundleStructure == null)
            return null;

        // Get item from the first file entry which contains the key.
        // Is looks in default (=primary) entry first.
        for(int i=0; i<bundleStructure.getEntryCount(); i++) {
            Element.ItemElem item = bundleStructure.getItem(i, bundleStructure.getKeyIndexByName(rbString.getKey()));
            if(item != null)
                return item;
        }
        
        return null;
    }
}

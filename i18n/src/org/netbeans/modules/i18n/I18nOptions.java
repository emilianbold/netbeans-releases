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


import org.openide.options.SystemOption;
import org.openide.util.NbBundle;


/**
 * Options for i18n module.
 * @author  Peter Zavadsky
 */
public class I18nOptions extends SystemOption {

    /** Property name for generate field. */
    public static final String PROP_GENERATE_FIELD = "generateField"; // NOI18N
    
    /** Property name for init java code. */
    public static final String PROP_INIT_JAVA_CODE = "initJavaCode"; // NOI18N
    
    /** Property name for replacing java code. */
    public static final String PROP_REPLACE_JAVA_CODE = "replaceJavaCode"; // NOI18N
    
    /** Property name for replacing init java . */
    public static final String PROP_REGULAR_EXPRESSION = "regularExpression"; // NOI18N

    {
        setInitJavaCode(I18nUtil.getInitFormatItems()[0]);
        setReplaceJavaCode(I18nUtil.getReplaceFormatItems()[0]);
        setRegularExpression(I18nUtil.getRegExpItems()[0]);
    }

    
    /** Provided due exeternaliazation only. 
     * Don't create this object directly use superclass <code>findObject</code> method instead. */
    public I18nOptions() {
    }

    
    /** Implements superclass abstract method. */
    public String displayName() {
        return I18nUtil.getBundle().getString("LBL_Internationalization");
    }

    /** Getter for init java code property. */
    public boolean isGenerateField() {
        Boolean prop = (Boolean)getProperty(PROP_GENERATE_FIELD);
        return prop == null ? true : prop.booleanValue();
    }

    /** Setter for init java code property. */
    public void setGenerateField(boolean generateField) {
        // Stores in class-wide state and fires property changes if needed:
        putProperty(PROP_GENERATE_FIELD, new Boolean(generateField), true);
    }
    
    /** Getter for init java code property. */
    public String getInitJavaCode() {
        return (String)getProperty(PROP_INIT_JAVA_CODE);
    }

    /** Setter for init java code property. */
    public void setInitJavaCode(String initJavaCode) {
        // Make sure it is sane.
        if(initJavaCode == null)
            throw new NullPointerException();
        
        // Stores in class-wide state and fires property changes if needed:
        putProperty(PROP_INIT_JAVA_CODE, initJavaCode, true);
    }    
    
    /** Getter for replace java code property. */
    public String getReplaceJavaCode() {
        return (String)getProperty(PROP_REPLACE_JAVA_CODE);
    }

    /** Setter for replace java code property. */
    public void setReplaceJavaCode(String replaceJavaCode) {
        // Make sure it is sane.
        if(replaceJavaCode == null)
            throw new NullPointerException();
        
        // Stores in class-wide state and fires property changes if needed:
        putProperty(PROP_REPLACE_JAVA_CODE, replaceJavaCode, true);
    }    

    /** Getter for regular expression property. */
    public String getRegularExpression() {
        return (String)getProperty(PROP_REGULAR_EXPRESSION);
    }

    /** Setter for regular expression property. */
    public void setRegularExpression(String regExp) {
        // Make sure it is sane.
        if(regExp == null)
            throw new NullPointerException();
        
        // Stores in class-wide state and fires property changes if needed:
        putProperty(PROP_REGULAR_EXPRESSION, regExp, true);
    }    

}

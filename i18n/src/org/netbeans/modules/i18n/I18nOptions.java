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


import java.io.IOException;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.options.SystemOption;
import org.openide.TopManager;


/**
 * Options for i18n module.
 *
 * @author  Peter Zavadsky
 */
public class I18nOptions extends SystemOption {
    
    /** Generated serial version UID.  */
    static final long serialVersionUID = -1045171977263973656L;

    /** Property name for generate field. */
    public static final String PROP_GENERATE_FIELD = "generateField"; // NOI18N

    /** Property name for advanced wizard. */
    public static final String PROP_ADVANCED_WIZARD = "advancedWizard"; // NOI18N
    
    /** Property name for init java code. */
    public static final String PROP_INIT_JAVA_CODE = "initJavaCode"; // NOI18N
    
    /** Property name for replacing java code. */
    public static final String PROP_REPLACE_JAVA_CODE = "replaceJavaCode"; // NOI18N
    
    /** Property name for replacing init java . */
    public static final String PROP_REGULAR_EXPRESSION = "regularExpression"; // NOI18N

    /** Property name for last used resource data object. */
    public static final String PROP_LAST_RESOURCE = "lastResource"; // NOI18N

    
    /** Provided due exeternaliazation only. 
     * Don't create this object directly use superclass <code>findObject</code> method instead. */
    public I18nOptions() {
    }

    
    /** Implements superclass abstract method. */
    public String displayName() {
        return I18nUtil.getBundle().getString("LBL_Internationalization");
    }

    /** Getter for init generate field property. */
    public boolean isGenerateField() {
        // Lazy init.
        if(getProperty(PROP_GENERATE_FIELD) == null)
            putProperty(PROP_GENERATE_FIELD, Boolean.FALSE, true);
        
        return ((Boolean)getProperty(PROP_GENERATE_FIELD)).booleanValue();
    }

    /** Setter for init generate field property. */
    public void setGenerateField(boolean generateField) {
        // Stores in class-wide state and fires property changes if needed:
        putProperty(PROP_GENERATE_FIELD, new Boolean(generateField), true);
    }
    
    /** Getter for init advanced wizard property. */
    public boolean isAdvancedWizard() {
        // Lazy init.
        if(getProperty(PROP_ADVANCED_WIZARD) == null)
            putProperty(PROP_ADVANCED_WIZARD, Boolean.FALSE, true);
        
        return ((Boolean)getProperty(PROP_ADVANCED_WIZARD)).booleanValue();
    }

    /** Setter for init advanced wizard property. */
    public void setAdvancedWizard(boolean generateField) {
        // Stores in class-wide state and fires property changes if needed:
        putProperty(PROP_ADVANCED_WIZARD, new Boolean(generateField), true);
    }
    
    /** Getter for init java code property. */
    public String getInitJavaCode() {
        // Lazy init.
        if(getProperty(PROP_INIT_JAVA_CODE) == null)
            putProperty(PROP_INIT_JAVA_CODE, I18nUtil.getInitFormatItems()[0], true);
            
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
        // Lazy init.
        if(getProperty(PROP_REPLACE_JAVA_CODE) == null)
            putProperty(PROP_REPLACE_JAVA_CODE, I18nUtil.getReplaceFormatItems()[2], true);
        
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
        // Lazy init.
        if(getProperty(PROP_REGULAR_EXPRESSION) == null)
            putProperty(PROP_REGULAR_EXPRESSION, I18nUtil.getRegExpItems()[0], true);
        
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
    
    /** Getter for last resource property. */
    public DataObject getLastResource() {
        String resourceName = (String)getProperty(PROP_LAST_RESOURCE);
        
        if(resourceName == null)
            return null;

        DataObject lastResource = null;
        
        FileObject fileObject = TopManager.getDefault().getRepository().findResource(resourceName);
        if(fileObject != null) {
            try {
                lastResource = TopManager.getDefault().getLoaderPool().findDataObject(fileObject);
            } catch (IOException ioe) {
                if(Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                    ioe.printStackTrace();
            }
        }

        return lastResource;
    }
    
    /** Setter for last resource property. */
    public void setLastResource(DataObject lastResource) {
        // Make sure it is sane.        
        if(lastResource == null)
            throw new NullPointerException();
        
        putProperty(PROP_LAST_RESOURCE, lastResource.getPrimaryFile().getPackageNameExt('/', '.'), true);
    }

}

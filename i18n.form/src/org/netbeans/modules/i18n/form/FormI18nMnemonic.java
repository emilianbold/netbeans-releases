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


package org.netbeans.modules.i18n.form;


import org.netbeans.modules.form.FormDesignValue;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.i18n.I18nSupport;
import org.netbeans.modules.i18n.java.JavaI18nString;

import org.netbeans.modules.properties.UtilConvert;


/**
 * This class extends the capability of <code>JavaI18nString</code> to be 
 * <code>FormDesignValue</code> to be used in form property sheets.
 *  
 * @author  Peter Zavadsky
 * @see org.netbeans.modules.i18n.java.JavaI18nString
 * @see ResourceBundleStringFormEditor
 * @see org.netbeans.modules.form.FormDesignValue
 */
public class FormI18nMnemonic extends FormI18nString {

    /** Creates new <code>FormI18nMnemonic</code>. */
    public FormI18nMnemonic(I18nSupport i18nSupport) {
        super(i18nSupport);
    }

    /** Cretaes new <code>FormI18nMnemonic</code> from <code>JavaI18nString</code>. 
     * @param source source which is created new <code>FormI18nMnemonic</code> from. */
    public FormI18nMnemonic(JavaI18nString source) {
        super(source);
    }
    
    /** Implements <code>FormDesignValue</code> interface. Gets design value.
     * @see org.netbeans.modules.form.FormDesignValue#getDesignValue(RADComponent radComponent)
     */
    public Object getDesignValue() {
        return new Character( ((String)super.getDesignValue()).charAt(0) );
    }
    
    /** The string to replace a property in source code. 
     * @return replacing string
     */
    public String getReplaceString() {
        return super.getReplaceString() + ".charAt(0)";
    }
    
}
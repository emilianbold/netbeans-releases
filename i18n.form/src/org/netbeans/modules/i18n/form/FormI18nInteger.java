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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.i18n.form;


import org.netbeans.modules.i18n.I18nSupport;
import org.netbeans.modules.i18n.java.JavaI18nString;

/**
 * This class extends the capability of <code>JavaI18nString</code> to be
 * <code>FormDesignValue</code> to be used in form property sheets.
 *
 * @author  Peter Zavadsky
 * @see org.netbeans.modules.i18n.java.JavaI18nString
 * @see ResourceBundleStringFormEditor
 * @see org.netbeans.modules.form.FormDesignValue
 */
public class FormI18nInteger extends FormI18nString {

    /** Creates new <code>FormI18nInteger</code>. */
    public FormI18nInteger(I18nSupport i18nSupport) {
        super(i18nSupport);
    }

    /** Cretaes new <code>FormI18nInteger</code> from <code>JavaI18nString</code>. 
     * @param source source which is created new <code>FormI18nInteger</code> from. */
    public FormI18nInteger(JavaI18nString source) {
        super(source);
    }
    
    /** Implements <code>FormDesignValue</code> interface. Gets design value.
     * @see org.netbeans.modules.form.FormDesignValue#getDesignValue(RADComponent radComponent)
     */
    public Object getDesignValue() {
        return Integer.decode( (String)super.getDesignValue() );
    }
    
    /** The string to replace a property in source code. 
     * @return replacing string
     */
    public String getReplaceString() {
        return "Integer.parseInt(" + super.getReplaceString() + ")";
    }
    
}

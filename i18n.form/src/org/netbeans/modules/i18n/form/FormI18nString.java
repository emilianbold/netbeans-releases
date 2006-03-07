/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.i18n.form;

import org.netbeans.modules.form.FormDesignValue;
import org.netbeans.modules.form.FormEditor;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.FormProperty;
import org.netbeans.modules.i18n.I18nSupport;
import org.netbeans.modules.i18n.java.JavaI18nString;
import org.openide.loaders.DataObject;

/**
 * This class extends the capability of <code>JavaI18nString</code> to be 
 * <code>FormDesignValue</code> to be used in form property sheets.
 *  
 * @author  Peter Zavadsky
 * @see org.netbeans.modules.i18n.java.JavaI18nString
 * @see ResourceBundleStringFormEditor
 * @see org.netbeans.modules.form.FormDesignValue
 */
public class FormI18nString extends JavaI18nString implements FormDesignValue {

    String bundleName;   
    
    /** Creates new <code>FormI18nString</code>. */
    public FormI18nString(I18nSupport i18nSupport) {
        super(i18nSupport);
    }

    /** Cretaes new <code>FormI18nString</code> from <code>JavaI18nString</code>. 
     * @param source source which is created new <code>FormI18nString</code> from. */
    public FormI18nString(JavaI18nString source) {
        this(createNewSupport(source.getSupport()), 
             source.getKey(),
             source.getValue(),
             source.getComment(), 
             source.getArguments(), 
             source.getReplaceFormat());
    }

    private FormI18nString(I18nSupport i18nSupport, String key, String value, String commment, String[] arguments, String replaceFormat) {
        super(i18nSupport);

        this.key = key;
        this.value = value;
        this.comment = comment;
        
        this.arguments = arguments;
        this.replaceFormat = replaceFormat;
    }

    public FormDesignValue copy(FormProperty formProperty) {
        FormModel formModel = (formProperty == null) ? null : formProperty.getPropertyContext().getFormModel();
        I18nSupport newSupport = createNewSupport(FormEditor.getFormDataObject(formModel), 
                                                  support.getResourceHolder().getResource());
        return new FormI18nString(newSupport, 
                                  this.getKey(),
                                  this.getValue(),
                                  this.getComment(), 
                                  this.getArguments(), 
                                  this.getReplaceFormat());        
    }
    
    private static I18nSupport createNewSupport(I18nSupport support) {
        return createNewSupport(support.getSourceDataObject(), support.getResourceHolder().getResource());        
    }     

    private static I18nSupport createNewSupport(DataObject sourceDataObject, DataObject resource) {
        I18nSupport newSupport = new FormI18nSupport.Factory().createI18nSupport(sourceDataObject);                
        if(resource != null) {
            newSupport.getResourceHolder().setResource(resource);            
        }                
        return newSupport;        
    }
    
    /**
     * Implements <code>FormDesignValue</code> interface. Gets design value. 
     * @see org.netbeans.modules.form.FormDesignValue#getDesignValue(RADComponent radComponent) */
    public Object getDesignValue() {
        String designValue = getSupport().getResourceHolder().getValueForKey(getKey());

        if(designValue == null)
            return FormDesignValue.IGNORED_VALUE;
        else
            return designValue;
    }
    
    /** Gets description of the design value. Implements <code>FormDesignValue</code> interface.
     * @return key value */
    public String getDescription() {
        return "<" + getKey() + ">"; // NOI18N
    }
}

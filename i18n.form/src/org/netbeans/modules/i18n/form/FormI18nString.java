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


package org.netbeans.modules.i18n.form;

import org.netbeans.modules.form.I18nValue;
import org.netbeans.modules.form.FormDesignValue;
import org.netbeans.modules.form.FormEditor;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.FormProperty;
import org.netbeans.modules.i18n.I18nSupport;
import org.netbeans.modules.i18n.I18nUtil;
import org.netbeans.modules.i18n.ResourceHolder;
import org.netbeans.modules.i18n.java.JavaI18nString;
import org.netbeans.modules.i18n.java.JavaResourceHolder;
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
public class FormI18nString extends JavaI18nString implements I18nValue {

    String bundleName; // to be saved again if file can't be found after form is loaded

    Object allData; // complete data for given key across all locales
                    // stored here for undo/redo purposes

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

    FormI18nString(DataObject srcDataObject) {
        super(new FormI18nSupport.Factory().createI18nSupport(srcDataObject));

        boolean nbBundle = org.netbeans.modules.i18n.Util.isNbBundleAvailable(srcDataObject);
        if (I18nUtil.getDefaultReplaceFormat(!nbBundle).equals(getReplaceFormat())) {
            setReplaceFormat(I18nUtil.getDefaultReplaceFormat(nbBundle));
        }
    }

    private FormI18nString(I18nSupport i18nSupport, String key, String value, String commment, String[] arguments, String replaceFormat) {
        super(i18nSupport);

        this.key = key;
        this.value = value;
        this.comment = comment;
        
        this.arguments = arguments;
        this.replaceFormat = replaceFormat;
    }

    public Object copy(FormProperty formProperty) {
        FormModel form = formProperty.getPropertyContext().getFormModel();
        if (form == null)
            return getValue();

        DataObject sourceDO = FormEditor.getFormDataObject(form);
        if (sourceDO == null)
            return getValue();

        boolean sameForm = (sourceDO == support.getSourceDataObject());
        boolean autoMode = form.getSettings().getI18nAutoMode();
        DataObject resource = sameForm || !autoMode ?
                              support.getResourceHolder().getResource() : null;
        I18nSupport newSupport = createNewSupport(sourceDO, resource);

        FormI18nString newI18nString;
        if (autoMode) { // need auto-generated key (form module must provide)
            newI18nString = new FormI18nString(newSupport,
                                COMPUTE_AUTO_KEY, getValue(), getComment(),
                                getArguments(), getReplaceFormat());
            JavaResourceHolder jrh = (JavaResourceHolder) support.getResourceHolder();
            newI18nString.allData = jrh.getAllData(getKey());
        }
        else {
            newI18nString = new FormI18nString(newSupport,
                                getKey(), getValue(), getComment(),
                                getArguments(), getReplaceFormat());
            if (!sameForm) { // make sure the value is actual according to the target locale
                ResourceHolder rh = newSupport.getResourceHolder();
                newI18nString.value = rh.getValueForKey(getKey());
                newI18nString.comment = rh.getCommentForKey(getKey());
            }
        }
        return newI18nString;
    }

    public String toString() {
        return getValue();
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
        String designValue = getValue(); //getSupport().getResourceHolder().getValueForKey(getKey());

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

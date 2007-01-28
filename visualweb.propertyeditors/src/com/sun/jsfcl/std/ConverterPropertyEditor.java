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
package com.sun.jsfcl.std;

import java.awt.Component;
import java.beans.PropertyEditorSupport;
import javax.faces.convert.BooleanConverter;
import javax.faces.convert.ByteConverter;
import javax.faces.convert.CharacterConverter;
import javax.faces.convert.Converter;
import javax.faces.convert.DateTimeConverter;
import javax.faces.convert.DoubleConverter;
import javax.faces.convert.FloatConverter;
import javax.faces.convert.IntegerConverter;
import javax.faces.convert.LongConverter;
import javax.faces.convert.NumberConverter;
import javax.faces.convert.ShortConverter;
import javax.faces.convert.BigDecimalConverter;
import javax.faces.el.ValueBinding;
import com.sun.jsfcl.util.ComponentBundle;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.PropertyEditor2;
import com.sun.rave.designtime.faces.FacesBindingPropertyEditor;

public class ConverterPropertyEditor extends PropertyEditorSupport implements PropertyEditor2, FacesBindingPropertyEditor {
    // Private variables

    private static final ComponentBundle bundle = ComponentBundle.getBundle(ConverterPropertyEditor.class);

    private DesignProperty liveProperty;

    private Class[] classes = new Class[] {
        null,
        BigDecimalConverter.class,
        BooleanConverter.class,
        ByteConverter.class,
        CharacterConverter.class,
        DateTimeConverter.class,
        DoubleConverter.class,
        FloatConverter.class,
        IntegerConverter.class,
        LongConverter.class,
        NumberConverter.class,
        ShortConverter.class
    };

    private String[] prettyClasses = new String[] {
        bundle.getMessage("parenNewBDC"), //NOI18N
        bundle.getMessage("parenNewBoolC"), //NOI18N
        bundle.getMessage("parenNewByteC"), //NOI18N
        bundle.getMessage("parentNewCC"), //NOI18N
        bundle.getMessage("parenNewDTC"), //NOI18N
        bundle.getMessage("parenNewDC"), //NOI18N
        bundle.getMessage("parenNewFC"), //NOI18N
        bundle.getMessage("parenNewIC"), //NOI18N
        bundle.getMessage("parenNewLC"), //NOI18N
        bundle.getMessage("parenNewNC"), //NOI18N
        bundle.getMessage("parenNewSC"), //NOI18N
        bundle.getMessage("parenNewSTC"), //NOI18N
	bundle.getMessage("parenNewCLC") //NOI18N
    };

    // Methods

    public String[] getTags() {
        DesignBean[] beans = getConverterBeans();
        String[] tags = new String[beans.length + prettyClasses.length + 1];

        int index = 0;
        tags[index++] = "";
        for (int i = 0; i < beans.length; i++) {
            tags[index++] = beans[i].getInstanceName();
        }
        for (int i = 0; i < prettyClasses.length; i++) {
            tags[index++] = prettyClasses[i];
        }
        return tags;
    }

    public void setAsText(String text) throws IllegalArgumentException {
        Converter converter = null;
        DesignBean[] beans = getConverterBeans();
        
        if (text == null || text.trim().length() == 0) {
            converter = null;
            this.setValue(null);
            return;
        }

        for (int i = 0; i < beans.length; i++) {
            if (beans[i].getInstanceName().equals(text)) {
                converter = (Converter)beans[i].getInstance();
                break;
            }
        }

        if (converter == null) {
            for (int i = 0; i < prettyClasses.length; i++) {
                if (prettyClasses[i].equals(text)) {
                    // Need to shift the index by 1 since the first one in classes array is null
                    DesignBean createResult =
                            liveProperty.getDesignBean().getDesignContext().createBean(
                            classes[i+1].getName(), null, null);
                    if (createResult != null) {
                        converter = (Converter)createResult.getInstance();
                    }
                    
                    break;
                }
            }
        }
        setValue(converter);
    }

    public String getAsText() {
        Object value = getValue();
        if (value instanceof ValueBinding) {
            return ((ValueBinding)value).getExpressionString();
        }
        if (value instanceof String) {
            return (String)value;
        }
        DesignBean designBean = getDesignBean();
        if( designBean != null ) {
            if( designBean.getInstance() instanceof NumberConverter ) {
                // Here are the defautls the NumberConverter has:
                // - min integer digits to 1
                // - max integer digits to 40
                // - min fractional digites to 0
                // - max fractional digits to 3
                designBean.getProperty( "minIntegerDigits").setValue( new Integer(1) );
                designBean.getProperty( "maxIntegerDigits").setValue( new Integer(40) );
                designBean.getProperty( "minFractionDigits").setValue( new Integer(0) );
                designBean.getProperty( "maxFractionDigits").setValue( new Integer(3) );
            }
        }
        return (designBean == null) ? "" : designBean.getInstanceName(); //NOI18N
    }

    public String getJavaInitializationString() {
        return getAsText();
    }

    public boolean supportsCustomEditor() {
        return false;
    }

    public Component getCustomEditor() {
        return null;
    }

    public void setDesignProperty(DesignProperty liveProperty) {
        this.liveProperty = liveProperty;
    }

    private DesignBean getDesignBean() {

        Object value = getValue();
        DesignBean[] lbeans = getConverterBeans();
        for (int i = 0; i < lbeans.length; i++) {
            if (lbeans[i].getInstance() == value) {
                return lbeans[i];
            }
        }
        return null;
    }

    private DesignBean[] getConverterBeans() {
        return (liveProperty == null) ? new DesignBean[0] :
            liveProperty.getDesignBean().getDesignContext().getBeansOfType(Converter.class);
    }
}

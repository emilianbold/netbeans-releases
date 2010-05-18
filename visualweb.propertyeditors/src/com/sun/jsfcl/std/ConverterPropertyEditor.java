/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

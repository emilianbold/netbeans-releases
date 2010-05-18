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
package org.netbeans.modules.visualweb.propertyeditors;

import com.sun.rave.designtime.DesignProperty;
import java.beans.PropertyDescriptor;
import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * An abstract property editor base class for editors that deal with numbers.
 * Values submitted must be valid numbers, and must fall within the default
 * range for the type of number, of the range specified by the user, whichever
 * is most restrictive. Range values may be supplied as the property descriptor
 * attributes <code>MIN_VALUE</code> and <code>MAX_VALUE</code>.
 *
 * <p>This editor class may be used with properties of type string, or of any
 * type that inherits from {@link java.lang.Number} or an atomic number type. If
 * used with a property of type string, numbers will be converted to strings
 * before being returned by getValue().
 *
 * <p>This editor class supports the notion of an "unset" or "null" value.
 * Normally, a number editor's value is what it displays. If a non-numeric string
 * is submitted as a candidate text value, the <code>setAsText(String)</code>
 * method will throw an illegal argument exception, causing the previous,
 * correct value to be restored. However, if the editor is configured to
 * accept an "unset" vaue (using the property descriptor attribute
 * <code>UNSET_VALUE</code>), submitting an empty string will cause the editor
 * to return the specified unset value as its new value. When thus configured,
 * the editor maintains a map between one numeric value and the empty text
 * string. Note that the caller of the editor's <code>getValue()</code> method
 * cannot distinguish between the case where the value, if equal to the "unset"
 * value, is being returned because the user entered the empty string, or
 * because the user explicitly typed it in. For the purposes of triggering an
 * "unset" value, a string consisting of only blank characters is considered
 * empty.
 *
 * @author gjmurphy
 */
public abstract class NumberPropertyEditor extends PropertyEditorBase {

    static ResourceBundle bundle =
            ResourceBundle.getBundle(NumberPropertyEditor.class.getPackage().getName() + ".Bundle"); //NOI18N

    /** Name of property descriptor attribute used to specify a minimum value.
     * Values may be specified as strings, or as instances of {@link java.lang.Number}
     * or any of its subclasses.
     */
    public static final String MIN_VALUE =
            "com.sun.rave.propertyeditors.MIN_VALUE"; // NOI18N

    /** Name of property descriptor attribute used to specify a maximum value.
     * Values may be specified as strings, or as instances of {@link java.lang.Number}
     * or any of its subclasses.
     */
    public static final String MAX_VALUE =
            "com.sun.rave.propertyeditors.MAX_VALUE"; // NOI18N

    String unsetText;
    Object value;
    Class propertyType;
    Number minValue;
    Number maxValue;

    private Number defaultMinValue;
    private Number defaultMaxValue;

    NumberPropertyEditor(Number defaultMinValue, Number defaultMaxValue) {
        this.minValue = defaultMinValue;
        this.maxValue = defaultMaxValue;
        this.defaultMinValue = defaultMinValue;
        this.defaultMaxValue = defaultMaxValue;
        this.value = null;
    }

    /**
     * Convert a string to a number. Implementing classes should return a
     * subclass of {@link java.lang.Number} appropriate to the type of number
     * that they support.
     */
    protected abstract Number parseString(String string)
    throws IllegalTextArgumentException;

    public void setDesignProperty(DesignProperty designProperty) {

        super.setDesignProperty(designProperty);
        PropertyDescriptor descriptor = designProperty.getPropertyDescriptor();

        // Initialize min value
        Number number = null;
        Object value = descriptor.getValue(MIN_VALUE);
        if (value instanceof String)
            try {
                number = parseString((String) value);
            } catch (NumberFormatException e) {
                number = null;
                // Report error
            } else if (value instanceof Number)
                number = (Number) value;
        if (number != null && ((Comparable)number).compareTo(defaultMinValue) >= 0)
            minValue = number;
        
        // Initialize max value
        number = null;
        value = descriptor.getValue(MAX_VALUE);
        if (value instanceof String)
            try {
                number = parseString((String) value);
            } catch (NumberFormatException e) {
                number = null;
                // Report error
            } else if (value instanceof Number)
                number = (Number) value;
        if (number != null && ((Comparable)number).compareTo(defaultMaxValue) <= 0)
            maxValue = number;
        
        propertyType = designProperty.getPropertyDescriptor().getPropertyType();
        
    }
    
    public void setValue(Object value) {
        if(value == null) {
            this.value = null;
        } else {
            if(String.class.isAssignableFrom(value.getClass())) {
                this.setAsText((String) value);
            } else if(Number.class.isAssignableFrom(value.getClass())) {
                this.value = (Number) value;
            } else {
                throw new IllegalArgumentException();
            }
        }
    }
    
    public Object getValue() {
        if (this.value == null)
            return null;
        if (String.class.isAssignableFrom(this.propertyType))
            return this.value.toString();
        return this.value;
    }
    
    public String getAsText() {
        if (this.value == null) {
            return "";
        }
        if (this.value.equals(super.unsetValue) && value instanceof Comparable) {
            Comparable c = (Comparable) value;
            if (c.compareTo(this.minValue) <= 0 || c.compareTo(this.maxValue) >= 0)
                return "";
        }
        return this.value.toString();
    }
    
    public void setAsText(String str) throws IllegalArgumentException {
        str = str == null ? "" : str.trim();
        if (str.length() == 0) {
            this.value = super.unsetValue;
        } else {
            Number n = parseString(str);
            if(((Comparable)n).compareTo(minValue) >= 0 && ((Comparable)n).compareTo(maxValue) <= 0) {
                this.value = n;
            } else {
                String rangeErrorMessage = null;
                int maxCompare = ((Comparable)maxValue).compareTo(defaultMaxValue);
                int minCompare = ((Comparable)minValue).compareTo(defaultMinValue);
                if (maxCompare > 0 && minCompare < 0)
                    rangeErrorMessage = MessageFormat.format(bundle.getString("NumberPropertyEditor.rangeErrorMessage"),
                            new Number[]{minValue, maxValue});
                else if (maxCompare < 0)
                    rangeErrorMessage = MessageFormat.format(bundle.getString("NumberPropertyEditor.rangeMaxErrorMessage"),
                            new Number[]{maxValue});
                else if (minCompare > 0)
                    rangeErrorMessage = MessageFormat.format(bundle.getString("NumberPropertyEditor.rangeMinErrorMessage"),
                            new Number[]{minValue});
                throw new IllegalTextArgumentException(rangeErrorMessage);
            }
        }
    }
    
}

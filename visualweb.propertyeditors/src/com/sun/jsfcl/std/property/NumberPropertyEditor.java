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
package com.sun.jsfcl.std.property;

import com.sun.jsfcl.util.ComponentBundle;
import com.sun.rave.designtime.DesignProperty;
import org.openide.ErrorManager;

/**
 * @author eric, jdeva
 *
 * @deprecated
 */
public abstract class NumberPropertyEditor extends AbstractPropertyEditor {

    protected boolean isValueString;
    
    protected static final ComponentBundle bundle = ComponentBundle.getBundle(NumberPropertyEditor.class);
    
    /**
     * The following attributes are read from BeanInfo to check whether
     * the user typed-in value is within the allowed range
     */
    public static final String MIN_VALUE_KEY = "minValue"; // NOI18N    
    public static final String MAX_VALUE_KEY = "maxValue"; // NOI18N    
    public static final String UNSET_VALUE_KEY = "unsetValue"; // NOI18N
    
    public static final String MIN_VALUE_PROP_KEY = "minValueProperty"; // NOI18N    
    public static final String MAX_VALUE_PROP_KEY = "maxValueProperty"; // NOI18N    
    public static final String UNSET_VALUE_PROP_KEY = "unsetValueProperty"; // NOI18N    

    Comparable minValue = null, maxValue = null, unsetValue = null;
    String minValueProperty = null, maxValueProperty = null, unsetValueProperty = null;
    
    public void attachToNewDesignProperty() {
        super.attachToNewDesignProperty();
        isValueString = getDesignProperty().getPropertyDescriptor().getPropertyType() == String.class;
        
        /**
         * Read the min/max/unset values or property names from which we have to 
         * get their values from the property descriptor
         *
         */
        Object temp;
        ErrorManager em = ErrorManager.getDefault();
        String errMsg = "jsfcl.NumberPropertyEditor: The Property Descriptor for " + // NOI18N
                getDesignProperty().getPropertyDescriptor().getName() + " has incorrect value for "; // NOI18N
        
        try {
            temp = getDesignProperty().getPropertyDescriptor().getValue(MIN_VALUE_KEY);
            if (temp != null)
                 minValue = (Comparable)temp;
        } catch(ClassCastException exc) {
            em.log(ErrorManager.ERROR, errMsg + MIN_VALUE_KEY ); 
        }            

        try {        
            temp = getDesignProperty().getPropertyDescriptor().getValue(MAX_VALUE_KEY);
            if (temp != null)
                 maxValue = (Comparable)temp;
        } catch(ClassCastException exc) {
            em.log(ErrorManager.ERROR, errMsg + MAX_VALUE_KEY ); 
        }              

        try {
            temp = getDesignProperty().getPropertyDescriptor().getValue(UNSET_VALUE_KEY);
            if (temp != null)
                 unsetValue = (Comparable)temp;
        } catch(ClassCastException exc) {
            //Use the default value if something wrong is specified in BeanInfo
            em.log(ErrorManager.ERROR, errMsg + UNSET_VALUE_KEY ); 
        }
        
        try {            
            temp = getDesignProperty().getPropertyDescriptor().getValue(MIN_VALUE_PROP_KEY);
            if (temp != null)
                 minValueProperty = (String)temp;
        } catch(ClassCastException exc) {
            //Use the default value if something wrong is specified in BeanInfo
            em.log(ErrorManager.ERROR, errMsg + MIN_VALUE_PROP_KEY ); 
        }
            
        try {            
            temp = getDesignProperty().getPropertyDescriptor().getValue(MAX_VALUE_PROP_KEY);
            if (temp != null)
                 maxValueProperty = (String)temp;
        } catch(ClassCastException exc) {
            em.log(ErrorManager.ERROR, errMsg + MAX_VALUE_PROP_KEY ); 
        }  
            
        try {            
            temp = getDesignProperty().getPropertyDescriptor().getValue(UNSET_VALUE_PROP_KEY);
            if (temp != null)
                 unsetValueProperty = (String)temp;
        } catch(ClassCastException exc) {
            em.log(ErrorManager.ERROR, errMsg + UNSET_VALUE_PROP_KEY ); 
        }
    }
    
    public Object getUnsetValue() {
        try {
            if(unsetValueProperty != null) {
                DesignProperty temp = liveProperty.getDesignBean().getProperty(unsetValueProperty);
                if(temp != null) 
                    return (Comparable)temp.getValue();
            } 
        }catch(ClassCastException exc) {
            ErrorManager em = ErrorManager.getDefault();
            em.log(ErrorManager.ERROR,  "jsfcl.NumberPropertyEditor:  value of " + // NOI18N
                    getDesignProperty().getPropertyDescriptor().getName() + " is not of Comparable type" ); // NOI18N    
        }
        
        return unsetValue;
    }    

    public String getAsText() {
        Object value = getValue();
        if (value == null || value.equals(getUnsetValue())) {
            return ""; //NOI18N
        }
        return value.toString();
    }

    public String getJavaInitializationString() {
        // the Java rep and human rep are NOT the same for numbers, "" is not a valid Java string,
        // so 0 must be used.
        Object value = getValue();
        String result = value != null ? value.toString() : "0";
        String suffix = getJavaInitializationStringSuffix();
        if (suffix != null) {
            result += suffix;
        }
        return result;
    }

    public String getJavaInitializationStringSuffix() {
        return null;
    }

    public abstract Object parseString(String string) throws NumberFormatException;

    public void setAsText(String string) throws java.lang.IllegalArgumentException {
        string = string.trim();
        Object value = getUnsetValue();
        boolean unset = true;
        if (string.length() > 0) {
            try {
                value = parseString(string);
            } catch (NumberFormatException nfe) {
                String errMsg = bundle.getMessage("valueInvalid", //NOI18N
                            getDesignProperty().getPropertyDescriptor().getName()); 
                throw new LocalizedMessageRuntimeException(errMsg, nfe);
            }                        
            checkRange(value);
            unset = false;
        }
        if (isValueString) {
            if (value == null) {
                setValue(null);
            } else {
                setValue(String.valueOf(value));
            }
        } else {
            setValue(value);
        }
        if (unset) {
            unsetProperty();
        }
    }
    
    public void checkRange(Object value)throws IllegalArgumentException {
        Comparable min = minValue;
        Comparable max = maxValue;
        String errMsg = null;
        
        /**
         * Get the min/max values from other property of the bean if property names
         * are specified in the BeanInfo
         *
         */
        ErrorManager em = ErrorManager.getDefault();
        try {
            if(minValueProperty != null) {
                DesignProperty minProp;
                minProp = liveProperty.getDesignBean().getProperty(minValueProperty);
                if(minProp != null) {
                    min = (Comparable)minProp.getValue();
                    Comparable minUnset = (Comparable)minProp.getPropertyDescriptor().getValue(UNSET_VALUE_KEY);
                    //We should use the other property value only if the user has changed it
                    if(min.compareTo(minUnset) == 0)
                        min = minValue;
                }
            } 
        } catch(ClassCastException exc) {
            em.log(ErrorManager.ERROR,  "jsfcl.NumberPropertyEditor:  value of " + // NOI18N
                    getDesignProperty().getPropertyDescriptor().getName() + " is not of Comparable type" ); // NOI18N            
        }
        
        try {        
            if(maxValueProperty != null) {
                DesignProperty maxProp;
                maxProp = liveProperty.getDesignBean().getProperty(maxValueProperty);
                if(maxProp != null) {
                    max = (Comparable)maxProp.getValue();
                    Comparable maxUnset = (Comparable)maxProp.getPropertyDescriptor().getValue(UNSET_VALUE_KEY);                    
                    //We should use the other property value only if the user has changed it
                    if(max.compareTo(maxUnset) == 0)
                        max = maxValue;
                }
            }
        } catch(ClassCastException exc) {
            em.log(ErrorManager.ERROR,  "jsfcl.NumberPropertyEditor:  value of " + // NOI18N
                    getDesignProperty().getPropertyDescriptor().getName() + " is not of Comparable type" ); // NOI18N           
        }
        

         /*
         * Make sure the user typed-in value is within the allowed range
         */
        int result = min.compareTo(value);
        if(result > 0) {
            errMsg = bundle.getMessage("valueHigher", //NOI18N
                        getDesignProperty().getPropertyDescriptor().getName(), min); 
            throw new IllegalArgumentException(errMsg);
        } 
        
        result = max.compareTo(value);
        if(result < 0) {
            errMsg = bundle.getMessage("valueLower", //NOI18N
                        getDesignProperty().getPropertyDescriptor().getName(), max);
            throw new IllegalArgumentException(errMsg);
        }        
    }
}

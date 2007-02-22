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

/*
 * NonNegativeIntegerProperty.java
 *
 * Created on January 5, 2006, 3:21 PM
 *
 */

package org.netbeans.modules.xml.schema.abe.nodes.properties;

import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;


/**
 * This class provides property support for properties having
 * non negative integer values(value>=0).
 * This class supports the Integer type properties.
 * Inner class Primitive supports int type properties.
 * Inner class PrimitivePositive supports int type properties
 *  which have positive values (value>0).
 * @author Ajit Bhate
 */
public class NonNegativeIntegerProperty extends BaseABENodeProperty {
    
    /**
     * Creates a new instance of NonNegativeIntegerProperty.
     * 
     * 
     * @param component The schema component which property belongs to.
     * @param property The property name.
     * @param propDispName The display name of the property.
     * @param propDesc Short description about the property.
     * @param isPrimitive distinguish between int and Integer. temporary property
     * Assumes that the property editor is default editor for Integer.
     * If special editor needed, subclasses and instances must set it explicitly.
     * @throws java.lang.NoSuchMethodException If no getter and setter for the property are found
     */
    public NonNegativeIntegerProperty(AXIComponent component, String property, String dispName,
            String desc) throws NoSuchMethodException {
        this(component,Integer.class,property,dispName,desc);
    }
    
    protected NonNegativeIntegerProperty(AXIComponent component, Class type,
            String property, String dispName, String desc)
            throws NoSuchMethodException {
        super(component,type,property,dispName,desc,null);
    }
    
    protected int getLowerLimit() {
        return 0;
    }

    protected int getDefaultValue() {
        return 1;
    }

    /**
     * The getValue method never returns null.
     * So this api is overridden to use super call instead
     */
    @Override
    public boolean isDefaultValue() {
        try {
            return super.getValue()==null;
        } catch (IllegalArgumentException ex) {
        } catch (InvocationTargetException ex) {
        } catch (IllegalAccessException ex) {
        }
        return false;
    }

    /**
     * Overridden to return default value if null.
     */
    @Override
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        Object o = super.getValue();
        return o==null?getDefaultValue():o;
    }

    /**
     * This method sets the value of the property.
     * Overridden to validate positive values.
     */
    @Override
    public void setValue(Object o) throws
            IllegalAccessException, InvocationTargetException{
        if (o instanceof Integer){
            int newVal = ((Integer)o).intValue();
            if(newVal<getLowerLimit()){
                String msg = NbBundle.getMessage(NonNegativeIntegerProperty.class, "MSG_Neg_Int_Value", o); //NOI18N
                IllegalArgumentException iae = new IllegalArgumentException(msg);
                ErrorManager.getDefault().annotate(iae, ErrorManager.USER,
                        msg, msg, null, new java.util.Date());
                throw iae;
            }
        }
        super.setValue(o);
    }
    
    /**
     * Supports properties with non negative int value(>=0)
     */
    public static class Primitive extends NonNegativeIntegerProperty {
        public Primitive(AXIComponent component, String property, String dispName,
                String desc) throws NoSuchMethodException {
            super(component,int.class,property,dispName,desc);
        }

        /**
         * Overridden to return false always
         */
        @Override
        public boolean supportsDefaultValue() {
            return false;
        }
        
    }
    
    /**
     * Supports properties with non negative int value(>0)
     */
    public static class PrimitivePositive extends Primitive {
        public PrimitivePositive(AXIComponent component, String property, String dispName,
                String desc) throws NoSuchMethodException {
            super(component,property,dispName,desc);
        }
        @Override 
        protected int getLowerLimit() {
            return 1;
        }
    }
    
}

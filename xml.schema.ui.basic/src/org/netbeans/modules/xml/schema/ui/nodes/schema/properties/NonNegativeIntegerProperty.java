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

/*
 * NonNegativeIntegerProperty.java
 *
 * Created on January 5, 2006, 3:21 PM
 *
 */

package org.netbeans.modules.xml.schema.ui.nodes.schema.properties;

import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
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
public class NonNegativeIntegerProperty extends BaseSchemaProperty {
    
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
    public NonNegativeIntegerProperty(SchemaComponent component, String property, String dispName,
            String desc) throws NoSuchMethodException {
        this(component,Integer.class,property,dispName,desc);
    }
    
    protected NonNegativeIntegerProperty(SchemaComponent component, Class type,
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
        public Primitive(SchemaComponent component, String property, String dispName,
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
        public PrimitivePositive(SchemaComponent component, String property, String dispName,
                String desc) throws NoSuchMethodException {
            super(component,property,dispName,desc);
        }
        @Override 
        protected int getLowerLimit() {
            return 1;
        }
    }
    
}

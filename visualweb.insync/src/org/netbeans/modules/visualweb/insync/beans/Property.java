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
package org.netbeans.modules.visualweb.insync.beans;

import java.beans.PropertyDescriptor;

import org.netbeans.modules.visualweb.extension.openide.util.Trace;
import org.netbeans.modules.visualweb.insync.java.JavaClass;
import org.netbeans.modules.visualweb.insync.java.Method;
import org.netbeans.modules.visualweb.insync.java.Statement;

/**
 * Representation of a single property setting on our parent bean, which maps to a single property
 * setter statement in the init block.
 */
public class Property extends BeansNode {

    public static final Property[] EMPTY_ARRAY = {};

    // General property fields
    final Bean bean;    // owning bean
    final PropertyDescriptor descriptor;

    // Java source-based property fields
    private JavaClass javaClass;
    private Statement stmt;

    //--------------------------------------------------------------------------------- Construction

    /**
     * Partially construct a property to be fully populated later
     * @param beansUnit
     * @param bean
     * @param name
     */
    protected Property(Bean bean, PropertyDescriptor descriptor, boolean unused) {
        super(bean.getUnit());
        this.bean = bean;
        //this.name = name;
        this.descriptor = descriptor;
        javaClass = unit.getJavaUnit().getJavaClass();
    }

    /**
     * Construct a property bound to existing statement & its bean. Called only from factory method
     * below.
     * @param beansUnit
     */
    private Property(Bean bean, PropertyDescriptor descriptor,
            Statement stmt, Object/*ExpressionTree*/ valueExpr
                     ) {
        this(bean, descriptor, false);
        this.stmt = stmt;
        assert Trace.trace("insync.beans", "P new bound Property: " + this);
    }

    /**
     * Create a property setting bound to a specific statement
     * @param unit
     * @param s
     * @return the new bound property if bindable, else null
     */
    static protected Property newBoundInstance(BeansUnit unit, Statement stmt) {
        Bean bean = unit.getBean(stmt.getBeanName());
        if(bean == null) {
            return null;
        }
        PropertyDescriptor pd = bean.getPropertyDescriptorForSetter(stmt.getPropertySetterName());
        if (pd == null) {
            return null;      
        }
        
        return new Property(bean, pd, stmt, null);
    }

    /**
     * Construct a new property, creating the underlying statement methods
     * @param bean
     * @param descriptor
     */
    protected Property(Bean bean, PropertyDescriptor descriptor) {
        this(bean, descriptor, false);
        assert Trace.trace("insync.beans", "P new created Property: " + this);
    }

    /**
     * Remove this property's statement from the init method. This property instance is dead &
     * should not be used.
     * 
     * @return true iff the source entry for this property was actually removed.
     */
    protected boolean removeEntry() {
        return stmt == null ? false : stmt.remove();
    }

    //------------------------------------------------------------------------------------ Accessors

    /**
     * Get the descriptor for this property
     */
    public PropertyDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Get the name of this property
     */
    public String getName() {
        return descriptor.getName();
    }

    /**
     * 
     */
    public boolean isMarkupProperty() {
        return false;
    }

    /**
     * Get the value of this property as a specified type. Can resurrect bean references and
     * literals.
     */
    public Object getValue(Class type) {
        return stmt == null ? null : stmt.evaluateArgument();
    }

    /**
     * Get the source representation of the value of this property. This will be in Java form by 
     * default, but may be returned in other forms by subclasses.
     */
    public String getValueSource() {
        return stmt == null ? null : stmt.getArgumentSource();
    }

    /**
     * Set the value of this property, creating the call arg expression of the appropriate type
     */
    public void setValue(Object value, String valueSource) {
        if(stmt == null) {
            Method method = unit.getPropertiesInitMethod();
            stmt = method.addPropertyStatement(bean.getName(), 
                    descriptor.getWriteMethod().getName(), valueSource);
        }else {
            stmt.replaceArgument(valueSource);
        }
    }
    
    /**
     * 
     */
    public void toString(StringBuffer sb) {
        sb.append(" n:");
        sb.append(getName());
        sb.append(" vs:\"");
        sb.append(getValueSource());
        sb.append("\"");
    }
}

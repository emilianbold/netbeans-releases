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
package org.netbeans.modules.visualweb.insync.beans;

import java.beans.PropertyDescriptor;

import org.netbeans.modules.visualweb.extension.openide.util.Trace;
import org.netbeans.modules.visualweb.insync.java.JavaClass;
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
    
    private String valueSource;
    private Object value;
    private boolean inserted;

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
        javaClass = unit.getThisClass();
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
        
        Property prop = new Property(bean, pd, stmt, null);
        prop.setInserted(true);
        return prop;
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
        boolean removed = false;
        if(inserted) {
            if(stmt == null) {
                stmt = getStatement();
            }
            removed = stmt.remove();
            stmt = null;
        }
        return removed;
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
        if (inserted && value == null) {
            if (stmt == null) {
                stmt = getStatement();
            }
            value = stmt.evaluateArgument();
        }
        return value;
    }

    /**
     * Get the source representation of the value of this property. This will be in Java form by 
     * default, but may be returned in other forms by subclasses.
     */
    public String getValueSource() {
        if(inserted && valueSource == null) {
            if(stmt == null) {
                stmt = getStatement();
            }
            valueSource = stmt.getArgumentSource();
        }
        return valueSource;
    }

    /**
     * Set the value of this property, creating the call arg expression of the appropriate type
     */
    public void setValue(Object value, String valueSource) {
        this.valueSource = valueSource;
        if(inserted && valueSource != null) {
            if(stmt == null) {
                stmt = getStatement();
            }
            stmt.replaceArgument(valueSource);
        }
    }
    
    //org.netbeans.modules.visualweb.insync.java.Statement holds on to bean name because of bug #96387
    //Until that bug is fixed, this is a workaround to fix #103122 
    public void setBeanName(String name) {
        if (inserted) {
            if (stmt == null) {
                stmt = getStatement();
            }
            stmt.setBeanName(name);
        }
    }
    
    private Statement getStatement() {
        return bean.unit.getPropertiesInitMethod().findPropertyStatement(bean.getName(), getWriteMethodName());
    }
    
    public String getBeanName() {
        return bean != null ? bean.getName() : null;
    }
    
    public String getWriteMethodName() {
        return descriptor != null ? 
            descriptor.getWriteMethod().getName() : null;
    }
    
    public boolean isInserted() {
        return inserted; 
    }
    
    public void setInserted(boolean inserted) {
        this.inserted = inserted;
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

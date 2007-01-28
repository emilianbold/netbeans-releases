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

package org.netbeans.modules.visualweb.jsfsupport.designtime;

import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.PropertyResolver;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import javax.el.ELException;
import javax.el.ExpressionFactory;
import javax.faces.context.FacesContext;

/**
 * DesignTimePropertyResolver provides a JSF property resolver for design-time property lookup
 *
 * @author Carl Quinn
 * @author Winston Prakash - Modifications to support JSF 1.2
 * @version 1.0
 */
public class DesignTimePropertyResolver extends PropertyResolver {

    protected PropertyResolver nested;

    public DesignTimePropertyResolver(PropertyResolver nested) {
        this.nested = nested;
        //Trace.trace("jsfsupport.container", "DesignTimePropertyResolver2 ");
    }

    private DesignBean findLiveDescendent(DesignBean base, String name) {
        // TODO Try to get access to the read only list instead
        DesignBean[] kids = base.getDesignContext().getBeans();
        for (int i = 0; i < kids.length; i++) {
            if (kids[i].getInstanceName().equals(name)) {
                //!CQ TODO: maybe verify that base is really an ancestor...
                return kids[i];
            }
        }
        return null;
    }

    // Specified by javax.faces.el.PropertyResolver.getValue(Object, Object)
    public Object getValue(Object base, Object property) {
        //Trace.trace("jsfsupport.container", "MPR getValue " + base + " " + property);
        if (base instanceof DesignBean) {
            DesignBean lbbase = (DesignBean)base;
            try {
                FacesContext context = FacesContext.getCurrentInstance();
                ExpressionFactory exprFactory = context.getApplication().getExpressionFactory();
                String name = (String) exprFactory.coerceToType(property, String.class);
                DesignProperty lp = lbbase.getProperty(name);
                if (lp != null) {
                    Object o = lp.getValue();
//                    if (o instanceof ResultSet)
//                        ResultSetPropertyResolver.initResultSet((ResultSet)o);
                    return o;
                }
                if (lbbase.isContainer()) {
                    DesignBean lb = findLiveDescendent(((DesignBean)base), name);
                    if (lb != null) {
                        Object o = lb.getInstance();
//                        if (o instanceof ResultSet)
//                            ResultSetPropertyResolver.initResultSet((ResultSet)o);
                        return o;
                    }
                }
            }catch (ELException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            throw new PropertyNotFoundException("" + property);
        }
        return nested.getValue(base, property);
    }

    // Specified by javax.faces.el.PropertyResolver.getValue(Object,int)
    public Object getValue(Object base, int index) {
        return nested.getValue(base, index);
        //!CQ TODO: indexed properties
    }

    // Specified by javax.faces.el.PropertyResolver.setValue(Object,Object,Object)
    public void setValue(Object base, Object property, Object value) {
        //Trace.trace("jsfsupport.container", "MPR setValue " + base + " " + property + " " + value);
        if (base instanceof DesignBean) {
            DesignBean lbbase = (DesignBean)base;
            try {
                FacesContext context = FacesContext.getCurrentInstance();
                ExpressionFactory exprFactory = context.getApplication().getExpressionFactory();
                String name = (String) exprFactory.coerceToType(property, String.class);
                DesignProperty lp = lbbase.getProperty(name);
                if (lp != null) {
                    lp.setValue(value);
                    return;
                }
                if (lbbase.isContainer()) {
                    DesignBean lb = findLiveDescendent(lbbase, name);
                    if (lb != null)
                        throw new EvaluationException("Illegal setting of immutable property: " + base + " . " + name);
                }
            } catch (ELException e) {
            }
            throw new PropertyNotFoundException("" + property);
        } else {
            nested.setValue(base, property, value);
        }
    }
    
    // Specified by javax.faces.el.PropertyResolver.setValue(Object,int,Object)
    public void setValue(Object base, int index, Object value) {
        nested.setValue(base, index, value);
        //!CQ TODO: indexed properties
    }
    
    // Specified by javax.faces.el.PropertyResolver.isReadOnly(Object,String)
    public boolean isReadOnly(Object base, Object property) {
        if (base instanceof DesignBean) {
            DesignBean lbbase = (DesignBean)base;
            try {
                FacesContext context = FacesContext.getCurrentInstance();
                ExpressionFactory exprFactory = context.getApplication().getExpressionFactory();
                String name = (String) exprFactory.coerceToType(property, String.class);
                DesignProperty lp = lbbase.getProperty(name);
                if (lp != null)
                    return lp.getPropertyDescriptor().getWriteMethod() == null;
                if (lbbase.isContainer()) {
                    DesignBean lb = findLiveDescendent(lbbase, name);
                    if (lb != null)
                        return true;
                }
            } catch (ELException e) {
            }
            throw new PropertyNotFoundException("" + property);
        }
        return nested.isReadOnly(base, property);
    }
    
    // Specified by javax.faces.el.PropertyResolver.isReadOnly(Object,int)
    public boolean isReadOnly(Object base, int index) {
        return nested.isReadOnly(base, index);
    }
    
    // Specified by javax.faces.el.PropertyResolver.getType(Object,String)
    public Class getType(Object base, Object property) {
        if (base instanceof DesignBean) {
            DesignBean lbbase = (DesignBean)base;
            try {
                FacesContext context = FacesContext.getCurrentInstance();
                ExpressionFactory exprFactory = context.getApplication().getExpressionFactory();
                String name = (String) exprFactory.coerceToType(property, String.class);
                DesignProperty lp = lbbase.getProperty(name);
                if (lp != null)
                    return lp.getPropertyDescriptor().getPropertyType();
                if (lbbase.isContainer()) {
                    DesignBean[] kids = lbbase.getChildBeans();
                    for (int i = 0; i < kids.length; i++) {
                        if (kids[i].getInstanceName().equals(name))
                            return kids[i].getBeanInfo().getBeanDescriptor().getBeanClass();
                    }
                }
            } catch (ELException e) {
            }
            throw new PropertyNotFoundException("" + property);
        }
        return nested.getType(base, property);
    }
    
    // Specified by javax.faces.el.PropertyResolver.getType(Object,int)
    public Class getType(Object base, int index) {
        return nested.getType(base, index);
    }
    
}

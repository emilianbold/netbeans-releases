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

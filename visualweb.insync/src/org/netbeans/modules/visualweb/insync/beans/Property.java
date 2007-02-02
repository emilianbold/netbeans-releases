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

import org.netbeans.modules.visualweb.insync.java.JMIUtils;
import org.netbeans.modules.visualweb.insync.java.JMIExpressionUtils;
import org.netbeans.modules.visualweb.insync.java.JMIMethodUtils;
import java.beans.PropertyDescriptor;

import org.netbeans.modules.visualweb.extension.openide.util.Trace;
import java.util.ArrayList;
import java.util.List;

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
    Object/*StatementTree*/ stmt;
    Object/*ExpressionTree*/ valueExpr;

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
    }

    /**
     * Construct a property bound to existing statement & its bean. Called only from factory method
     * below.
     * @param beansUnit
     */
    private Property(Bean bean, PropertyDescriptor descriptor,
            Object/*StatementTree*/ stmnt, Object/*ExpressionTree*/ valueExpr
                     ) {
        this(bean, descriptor, false);
        this.stmt = stmnt;
        this.valueExpr = valueExpr;
        assert Trace.trace("insync.beans", "P new bound Property: " + this);
    }


    static protected Object/*MethodInvocationTree*/ getExpression
            (BeansUnit unit, Object/*StatementTree*/ s) {
/*//NB6.0
        if(!(s instanceof ExpressionStatement))
            return null;
        ExpressionStatement exStmt = (ExpressionStatement)s;
 
        if(!(exStmt.getExpression() instanceof MethodInvocation))
            return null;
        MethodInvocation mExpr = (MethodInvocation)exStmt.getExpression();
 
        if(mExpr.getParameters().size() > 1)
            return null;
 
        PrimaryExpression pExpr = mExpr.getParentClass();
        if(pExpr == null && !(pExpr instanceof VariableAccess))
            return null;
        return mExpr; 
//*/
        return null;
    }

    /**
     * Create a property setting bound to a specific statement
     * @param unit
     * @param s
     * @return the new bound property if bindable, else null
     */
    static protected Property newBoundInstance(BeansUnit unit, Object/*StatementTree*/ s) {
/*//NB6.0
        JMIUtils.beginTrans(false);
        try {
            MethodInvocation mExpr = getExpression(unit, s);
            if(mExpr == null)
                return null;
            PrimaryExpression pExpr = mExpr.getParentClass();
            String cname = null;
            if (pExpr instanceof VariableAccess) {
                cname = ((VariableAccess)pExpr).getName();
            } else if (pExpr instanceof MultipartId) {
                cname = ((MultipartId)pExpr).getName();
            }
            Bean bean = unit.getBean(cname);
            if (bean == null)
                return null;
            String mname = mExpr.getName();
            PropertyDescriptor pd = bean.getPropertyDescriptorForSetter(mname);
            if (pd == null)
                return null;
 
            return new Property(bean, pd, s,
                    (Expression) mExpr.getParameters().get(0));
        }finally {
            JMIUtils.endTrans();
        } 
//*/
        return null;
    }

    /**
     * Construct a new property, creating the underlying statement methods
     * @param bean
     * @param descriptor
     */
    protected Property(Bean bean, PropertyDescriptor descriptor) {
        this(bean, descriptor, false);
        insertEntry();
        assert Trace.trace("insync.beans", "P new created Property: " + this);
    }

    /**
     * Insert this property's statement into the init method. 
     */
    protected void insertEntry() {
/*//NB6.0
        JMIUtils.beginTrans(true);
        boolean rollback = true;
        try {
            CallableFeature method = unit.getPropertiesInitMethod();
            JMIMethodUtils.addMethodInvocationStatement(method,
                    bean.getName(), descriptor.getWriteMethod().getName(), new ArrayList());
            rollback = false;
        }finally {
            JMIUtils.endTrans(rollback);
        }
 
        // Args are added in setValue()
//*/
    }

    /**
     * Remove this property's statement from the init method. This property instance is dead &
     * should not be used.
     * 
     * @return true iff the source entry for this property was actually removed.
     */
    protected boolean removeEntry() {        
/*//NB6.0
        boolean retVal = false;
        JMIUtils.beginTrans(true);
        boolean rollback = true;
        try {
            StatementBlock[] blocks = unit.getInitBlocks();
            for(int i = 0; i < blocks.length; i++) {
                Statement stmt = JMIMethodUtils.findStatement(
                        blocks[i],
                        descriptor.getWriteMethod().getName(), bean.getName());
                if (stmt != null) {
                    retVal = JMIMethodUtils.removeStatement(blocks[i], stmt);
                    if(retVal)
                        //!CQ maybe remove the delegate method(s) also? would need to let Event do that...
                        stmt = null;
                    break;
                }
            }
            rollback = false;
        }finally {
            JMIUtils.endTrans(rollback);
        }
 
        if(retVal)
            stmt = null;
        return retVal; 
//*/
        return false;
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
        //!CQ TODO: make use of type to check/coerce idents & literals
        /*
        if (valExpr instanceof Identifier)
            return unit.getBean(((Identifier)valExpr).getName());
        return valExpr.getValue();
         */
        JMIUtils.beginTrans(true);
        try {
            valueExpr = getValueExpression();
            return valueExpr != null ? JMIExpressionUtils.getValue(valueExpr, unit.getJavaUnit()) : null;
        }finally {
            JMIUtils.endTrans();
        }
    }

    /**
     * Get the source representation of the value of this property. This will be in Java form by 
     * default, but may be returned in other forms by subclasses.
     */
    public String getValueSource() {
/*//NB6.0
        JMIUtils.beginTrans(false);
        try {
            if(unit.getJavaUnit() != null) {
                valueExpr = getValueExpression();
                JavaClass jCls = unit.getJavaUnit().getJavaClass();
                return valueExpr != null ? JMIExpressionUtils.getArgumentSource(
                        valueExpr) : null;
            }
            return null;
        }finally {
            JMIUtils.endTrans();
        } 
//*/
        return null;
    }

    /**
     * Set the value of this property, creating the call arg expression of the appropriate type
     */
    public void setValue(Object value, String valueSource) {
/*//NB6.0
        CallableFeature method = unit.getPropertiesInitMethod();
        JMIUtils.beginTrans(true);
        boolean rollback = true;
        try {
            int startPos = -1;
            int endPos = -1;
            MethodInvocation mExpr = getMethodInvocation();
 
            if(mExpr != null) {
                List l = mExpr.getParameters();
                if(l.size() == 0) {
                    startPos = mExpr.getEndOffset()-1;
                    endPos = startPos;
                }else {
                    Element elem = (Element)l.get(0);
                    startPos = elem.getStartOffset();
                    endPos = mExpr.getEndOffset()-1;
                }
            }
 
            int methStartPos = method.getBody().getStartOffset()+1;
            String bodyText = method.getBodyText();
            String body = bodyText.substring(1, startPos-methStartPos);
            body += valueSource;
            body += bodyText.substring(endPos-methStartPos);
            method.setBodyText(body);
            rollback = false;
        }finally {
            JMIUtils.endTrans(rollback);
        }
//*/
    }
    
    Object/*MethodInvocationTree*/ getMethodInvocation() {
/*//NB6.0
        JMIUtils.beginTrans(false);
        try {
            StatementBlock[] blocks = unit.getInitBlocks();
            for (int i = 0; i < blocks.length; i++) {
                Statement stmt = JMIMethodUtils.findStatement(
                        blocks[i],
                        descriptor.getWriteMethod().getName(), bean.getName());
                if (stmt != null) {
                    ExpressionStatement exStmt =
                            (ExpressionStatement)stmt;
 
                    MethodInvocation mExpr =
                            (MethodInvocation)exStmt.getExpression();
                    return mExpr;
                }
            }
        } finally {
            JMIUtils.endTrans();
        }
 
        return null;
//*/
        return null;
    }
    
    Object/*ExpressionTree*/ getValueExpression() {
/*//NB6.0
        MethodInvocation mExpr = getMethodInvocation();
        if (mExpr == null) {
            return null;
        }
        List params = mExpr.getParameters();
        if(params.size() > 0)
            return (Expression)params.get(0);
        else
            return null;
//*/
        return null;
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

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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.form.j2ee;

import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.form.CreationDescriptor;
import org.netbeans.modules.form.FormProperty;
import org.netbeans.modules.form.codestructure.CodeExpression;
import org.netbeans.modules.form.codestructure.CodeExpressionOrigin;

/**
 * Creator for query obtained from entity manager.
 *
 * @author Jan Stola
 */
class QueryCreator implements CreationDescriptor.Creator {
    /** Parameter types. */
    private Class[] paramTypes = new Class[] {Object.class/*EntityManager.class*/, String.class, int.class, int.class};
    /** Exception types. */
    private Class[] exTypes = new Class[0];
    /** Property names. */
    private String[] propNames = new String[] {"entityManager", "query", "firstResult", "maxResults"}; // NOI18N
    
    /**
     * Returns number of parameters of the creator.
     *
     * @return number of parameters of the creator.
     */
    public int getParameterCount() {
        return 4;
    }
    
    /**
     * Returns parameter types of the creator.
     *
     * @return parameter types of the creator.
     */
    public Class[] getParameterTypes() {
        return paramTypes;
    }
    
    /**
     * Returns exception types of the creator.
     *
     * @return exception types of the creator.
     */
    public Class[] getExceptionTypes() {
        return exTypes;
    }
    
    /**
     * Returns property names of the creator.
     *
     * @return property names of the creator.
     */
    public String[] getPropertyNames() {
        return propNames;
    }
    
    /**
     * Creates instance according to given properties.
     *
     * @param props properties describing the instance to create.
     * @return instance that reflects values of the given properties.
     */
    public Object createInstance(FormProperty[] props) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return new Object(); // Hack
    }

    /**
     * Creates instance according to given parameter values.
     *
     * @param paramValues parameter values describing the instance to create.
     * @return instance that reflects values of the given parameters.
     */
    public Object createInstance(Object[] paramValues) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return new Object(); // Hack
    }
    
    /**
     * Returns creation code according to given properties.
     *
     * @param props properties describing the instance whose creation code should be returned.
     * @param expressionType type of the expression to create.
     * @return creation code that reflects values of the given properties.
     */
    public String getJavaCreationCode(FormProperty[] props, Class expressionType, String genericTypes) {
        assert (props.length == 4);
        
        String entityManager = null;
        String query = null;
        String firstResult = null;
        String maxResults = null;
        for (int i=0; i<props.length; i++) {
            String propName = props[i].getName();
            if (propNames[0].equals(propName)) {
                entityManager = props[i].getJavaInitializationString();
            } else if (propNames[1].equals(propName)) {
                query = props[i].getJavaInitializationString();
            } else if (propNames[2].equals(propName)) {
                if (props[i].isChanged()) {
                    firstResult = props[i].getJavaInitializationString();
                }
            } else if (propNames[3].equals(propName)) {
                if (props[i].isChanged()) {
                    maxResults = props[i].getJavaInitializationString();
                }
            } else {
                assert false;
            }
        }

        StringBuilder sb = new StringBuilder();
        if ("null".equals("" + entityManager)) { // NOI18N
            sb.append("((javax.persistence.EntityManager)null)");  // NOI18N
        } else {
            sb.append(entityManager);
        }
        sb.append(".createQuery(").append(query).append(')'); // NOI18N
        if (firstResult != null) {
            sb.append(".setFirstResult(").append(firstResult).append(')'); // NOI18N
        }
        if (maxResults != null) {
            sb.append(".setMaxResults(").append(maxResults).append(')'); // NOI18N
        }
        return sb.toString();
    }
    
    public CodeExpressionOrigin getCodeOrigin(CodeExpression[] params) {
        return null; // PENDING how is this used?
    }
    
}

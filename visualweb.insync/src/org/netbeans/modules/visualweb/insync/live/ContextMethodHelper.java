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
package org.netbeans.modules.visualweb.insync.live;

import org.netbeans.modules.visualweb.insync.java.ClassUtil;
import org.netbeans.modules.visualweb.insync.java.JMIUtils;
import org.netbeans.modules.visualweb.insync.java.JavaClass;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.openide.util.NbBundle;
import com.sun.rave.designtime.ContextMethod;

/**
 *
 * This class provides the implementation for Design time ContextMethod APIs
 *
 * @author jdeva
 *
 */
public class ContextMethodHelper {
    LiveUnit lu;

    public ContextMethodHelper(LiveUnit lu) {
        this.lu = lu;
    }

    /**
     * Returns a set of {@link ContextMethod} objects describing the public methods declared on this
     * DesignContext (source file).
     *
     * @return An array of {@link ContextMethod} objects, describing the public methods declared on
     *         this DesignContext (source file)
     */
    public ContextMethod[] getContextMethods() {
/*//NB6.0
        JMIUtils.beginTrans(false);
        try {
            JavaClassAdapter javaClass = lu.getSourceUnit().getThisClass();
            ArrayList contextMethods = new ArrayList();
 
            //Go through all the available methods
            Method[] methods = javaClass.getMethods();
            for(int i = 0; i < methods.length; i++) {
                //Construct a ContextMethod object
                ContextMethod cm = getContextMethod(methods[i]);
                if(cm != null)
                    contextMethods.add(cm);
            }
            return (ContextMethod[])contextMethods.toArray(new ContextMethod[0]);
        }finally {
            JMIUtils.endTrans();
        }
//*/
        return null;
    }

    /**
     * Returns a {@link ContextMethod} object describing the public method with the specified name
     * and parameter types.  Returns <code>null</code> if no public method exists on this
     * DesignContext with the specified name and parameter types.
     *
     * @param methodName The method name of the desired context method
     * @param parameterTypes The parameter types of the desired context method
     * @return A ContextMethod object describing the requested method, or <code>null</code> if no
     *         method exists with the specified name and parameter types
     */
    public ContextMethod getContextMethod(Object/*ExecutableElement*/ m) {
/*//NB6.0
        JMIUtils.beginTrans(false);
        try {
            if(m != null /NB6.0*&& m.getAccessModifiers() == Modifier.PUBLIC* /) {
                
                List params = m.getParameters();
                Class[] paramTypes = new Class[params.size()];
                String[] paramNames = new String[params.size()];
                Iterator iter = params.iterator();
                int i = 0;
                ClassLoader cl = lu.getSourceUnit().getClassLoader();
                try {
                    while(iter.hasNext()) {
                        Parameter p = (Parameter)iter.next();
                        paramTypes[i] = ClassUtil.getClass(p.getType().getName(), cl);
                        paramNames[i] = p.getName();
                        i++;
                    }
                    Class retType = ClassUtil.getClass(m.getType().getName(), cl);
                    
                    return new ContextMethod(lu, m.getName(), m.getModifiers(), retType,
                            paramTypes, paramNames, m.getBodyText(), m.getJavadocText());
                }catch(ClassNotFoundException cnfe) {
                    //Ignore the exception and return null
                }
            }
    }finally {
        JMIUtils.endTrans();
    }
    return null;
//*/
        return null;
    }

    /**
     * Returns a {@link ContextMethod} object describing the public method with the specified name
     * and no arguments.  Returns <code>null</code> if no public method exists on this
     * DesignContext with the specified name and no arguments.
     *
     * @param methodName The method name of the desired context method
     * @return A ContextMethod object describing the requested method, or <code>null</code> if no
     *         method exists with the specified name and no arguments
     */
    public ContextMethod getContextMethod(String methodName) {
        return getContextMethod(methodName, null);
    }


    /**
     * Returns a {@link ContextMethod} object describing the public method with the specified name
     * which doesn't take anyparameter types.  Returns <code>null</code> if no public method exists on this
     * DesignContext with the specified name and parameter types.
     *
     * @param methodName The method name of the desired context method
     * @param parameterTypes The parameter types of the desired context method
     * @return A ContextMethod object describing the requested method, or <code>null</code> if no
     *         method exists with the specified name and parameter types
     */
    public ContextMethod getContextMethod(String methodName, Class[] parameterTypes) {
/*//NB6.0
        JMIUtils.beginTrans(false);
        try {
            JavaClassAdapter javaClass = lu.getSourceUnit().getThisClass();
            if(parameterTypes == null)
                parameterTypes = new Class[0];
            Method m = javaClass.getMethod(methodName, parameterTypes);
            return getContextMethod(m);
        }finally {
            JMIUtils.endTrans();
        }
//*/
        return null;
    }


    /**
     * <p>Creates a new public method in the source code for this DesignContext.  The passed
     * ContextMethod <strong>must</strong> specify at least the designContext and methodName, and
     * <strong>must not</strong> describe a method that already exists in the DesignContext source.
     * To update an existing method, use the <code>updateContextMethod()</code> method.  These
     * methods are separated to help prevent accidental method overwriting.  The following table
     * details how the specified ContextMethod is used for this method:</p>
     *
     * <p><table border="1">
     * <tr><th>designContext <td><strong>REQUIRED.</strong> Must match the DesignContext that is
     *         being called.  This is essentially a safety precaution to help prevent accidental
     *         method overwriting.
     * <tr><th>methodName <td><strong>REQUIRED.</strong> Defines the method name.
     * <tr><th>parameterTypes <td>Defines the parameter types.  If <code>null</code> or an empty
     *         array is specified, the created method will have no arguments.
     * <tr><th>parameterNames <td>Defines the parameter names.  If <code>null</code> or an empty
     *         array is specified (or an array shorter than the parameterTypes array), default
     *         argument names will be used.
     * <tr><th>returnType <td>Defines the return type.  If <code>null</code> is specified, the
     *         created method will have a <code>void</code> return type.
     * <tr><th>throwsTypes <td>Defines the throws clause types.  If <code>null</code> is specified,
     *         the created method will have no throws clause.
     * <tr><th>bodySource <td>Defines the method body Java source code.  If <code>null</code> is
     *         specified, the method will have an empty body.  If the value is non-null, this must
     *         represent valid (compilable) Java source code.
     * <tr><th>commentText <td>Defines the comment text above the newly created method.  If
     *         <code>null</code> is specified, no comment text will be included.
     * </table></p>
     *
     * @param method A ContextMethod object representing the desired public method.
     * @return <code>true</code> if the method was created successfully, or <code>false</code> if
     *         it was not.
     * @throws IllegalArgumentException If there was a syntax error in any of the ContextMethod
     *         settings, or if the ContextMethod represents a method that already exists on this
     *         DesignContext (<code>updateContextMethod()</code> must be used in this case to avoid
     *         accidental method overwriting)
     */
    public boolean createContextMethod(ContextMethod method) throws IllegalArgumentException {
        if(method == null || method.getDesignContext() != lu)
            return false;
/*//NB6.0
        JMIUtils.beginTrans(true);
        boolean rollback = true;
        try {
            JavaClassAdapter javaClass = lu.getSourceUnit().getThisClass();
            Class[] paramTypes = method.getParameterTypes();
            if(paramTypes == null)
                paramTypes = new Class[0];
            Method m = javaClass.getMethod(method.getName(), paramTypes);
            if(m != null)
                throw new IllegalArgumentException(
                        NbBundle.getMessage(LiveUnit.class, "IllegalMethod"));  //NOI18N;
 
            //Add the new method at the end
            CallableFeature cf = javaClass.addMethod(method);
            if(cf != null) {
                //Check if there are errors because of adding method
                if(lu.getSourceUnit().getJavaUnit().getErrors().length > 0)
                    throw new IllegalArgumentException(
                            NbBundle.getMessage(LiveUnit.class, "IllegalSource"));  //NOI18N;
                rollback = false;
                return true;
            }
        }finally {
            JMIUtils.endTrans(rollback);
        }
//*/

        return false;
    }

    /**
     * <p>Updates an existing public method in the source code for this DesignContext.  The passed
     * ContextMethod will be used to locate the desired public method to update using the
     * designContext, methodName, and parameterTypes.  This method may only be used to update the
     * parameterNames, returnType, throwsTypes, bodySource, or commentText.  Any other changes
     * actually constitute the creation of a new method, as they alter the method signature.  To
     * create a new method, the <code>createContextMethod()</code> method should be used.  These
     * operations are separated to help prevent accidental method overwriting.  The following table
     * details how the specified ContextMethod is used for this method:</p>
     *
     * <p><table border="1">
     * <tr><th>designContext <td><strong>REQUIRED.</strong> Must match the DesignContext that is
     *         being called.  This is essentially a safety precaution to help prevent accidental
     *         method overwriting.
     * <tr><th>methodName <td><strong>REQUIRED.</strong> Specifies the desired method name.
     * <tr><th>parameterTypes <td><strong>REQUIRED.</strong> Specifies the desired method's
     *         parameter types (if it has any).  If <code>null</code> or an empty array is
     *         specified, the desired method is assumed to have zero arguments.
     * <tr><th>parameterNames <td>Defines the parameter names.  If <code>null</code> or an empty
     *         array is specified (or an array shorter than the parameterTypes array), default
     *         argument names will be used.
     * <tr><th>returnType <td>Defines the method's return type.  If <code>null</code> is specified,
     *         the method is assumed to have a <code>void</code> return type.
     * <tr><th>throwsTypes <td>Defines the throws clause types.  If <code>null</code> is specified,
     *         the resulting method will have no throws clause.
     * <tr><th>bodySource <td>Defines the method body Java source code.  If <code>null</code> is
     *         specified, the resulting method body will be empty.  If the value is non-null, this
     *         must represent valid (compilable) Java source code.  Note that a method with a
     *         non-void return type <strong>must</strong> return a value.
     * <tr><th>commentText <td>Defines the comment text above the newly created method.  If
     *         <code>null</code> is specified, no comment text will be included.
     * </table></p>
     *
     * @param method The desired ContextMethod representing the method to be updated
     * @return The resulting ContextMethod object (including any updates from the process)
     * @throws IllegalArgumentException If there was a syntax error in any of the ContextMethod
     *         settings, or if the ContextMethod does not exist in this DesignContext.
     */
    public ContextMethod updateContextMethod(ContextMethod method) throws IllegalArgumentException {
        if(method == null || method.getDesignContext() != lu)
            return null;

        JavaClass javaClass = lu.getSourceUnit().getThisClass();
/*//NB6.0
        JMIUtils.beginTrans(true);
        boolean rollback = true;
        try {
            Class[] paramTypes = method.getParameterTypes();
            if(paramTypes == null)
                paramTypes = new Class[0];
            Method m = javaClass.getMethod(method.getName(), paramTypes);
            if(m != null /NB6.0*&& m.getAccessModifiers() == Modifier.PUBLIC*NB6.0/) {
                javaClass.updateMethod(method);
                //Check if there are errors because of updating method
                /NB6.0*
                if(lu.getSourceUnit().getJavaUnit().getErrors().length > 0)
                    throw new IllegalArgumentException(
                            NbBundle.getMessage(LiveUnit.class, "IllegalSource"));  //NOI18N;
                 *NB6.0/
                ContextMethod retVal = getContextMethod(m);
                rollback = false;
                return retVal;
            }else {
            throw new IllegalArgumentException(
                    NbBundle.getMessage(LiveUnit.class, "IllegalMethod"));  //NOI18N;
            }
    }finally {
        JMIUtils.endTrans(rollback);
    }
//*/
        return null;
    }

    /**
     * <p>Removes an existing method from the source code for this DesignContext.  The passed
     * ContextMethod will be used to locate the desired method to remove using the designContext,
     * methodName, and parameterTypes.  No other portions of the ContextMethod are used.  The
     * following table details how the specified ContextMethod is used for this method:</p>
     *
     * <p><table border="1">
     * <tr><th>designContext <td><strong>REQUIRED.</strong> Must match the DesignContext that is
     *         being called.  This is essentially a safety precaution to help prevent accidental
     *         method removal.
     * <tr><th>methodName <td><strong>REQUIRED.</strong> Specifies the desired method name.
     * <tr><th>parameterTypes <td><strong>REQUIRED.</strong> Specifies the desired method's
     *         parameter types (if it has any).  If <code>null</code> or an empty array is
     *         specified, the desired method is assumed to have zero arguments.
     * <tr><th>parameterNames <td>Ignored.
     * <tr><th>returnType <td>Ignored.
     * <tr><th>throwsTypes <td>Ignored.
     * <tr><th>bodySource <td>Ignored.
     * <tr><th>commentText <td>Ignored.
     * </table></p>
     *
     * @param method A ContextMethod object defining the method to be removed
     * @return <code>true</code> if the method was successfully removed
     * @exception IllegalArgumentException if the specified ContextMethod does not exist or is not
     *            public on this DesignContext
     */
    public boolean removeContextMethod(ContextMethod method) {
        if(method == null || method.getDesignContext() != lu)
            return false;
/*//NB6.0
        JMIUtils.beginTrans(true);
        boolean rollback = true;
        try {
            JavaClassAdapter javaClass = lu.getSourceUnit().getThisClass();
            Class[] paramTypes = method.getParameterTypes();
            if(paramTypes == null)
                paramTypes = new Class[0];
            Method m = javaClass.getMethod(method.getName(), paramTypes);
            if(m != null /NB6.0*&& m.getAccessModifiers() == Modifier.PUBLIC*NB6.0/) {
                javaClass.removeMethod(m);
            } else {
                throw new IllegalArgumentException(
                        NbBundle.getMessage(LiveUnit.class, "IllegalMethod"));  //NOI18N;
            }
            rollback = false;
        } finally {
            JMIUtils.endTrans(rollback);
        }
//*/
        return false;
    }
}

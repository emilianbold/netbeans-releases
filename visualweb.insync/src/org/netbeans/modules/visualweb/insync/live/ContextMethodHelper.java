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
package org.netbeans.modules.visualweb.insync.live;

import javax.lang.model.element.ExecutableElement;
import org.netbeans.modules.visualweb.insync.java.ClassUtil;
import org.netbeans.modules.visualweb.insync.java.JavaClass;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.visualweb.insync.java.Method;
import org.netbeans.modules.visualweb.insync.java.ReadTaskWrapper;
import org.openide.util.NbBundle;
import com.sun.rave.designtime.ContextMethod;
import com.sun.source.tree.MethodTree;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.visualweb.insync.java.TreeUtils;

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
        JavaClass javaClass = lu.getSourceUnit().getThisClass();
        ArrayList contextMethods = new ArrayList();
        
        //Go through all the available methods
        for(Method m : javaClass.getMethods()) {
            //Construct a ContextMethod object
            ContextMethod cm = getContextMethod(m);
            if(cm != null)
                contextMethods.add(cm);
        }
        return (ContextMethod[])contextMethods.toArray(new ContextMethod[0]);
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
    public ContextMethod getContextMethod(final Method m) {
        return (ContextMethod)ReadTaskWrapper.execute( new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                ExecutableElement elem = m.getElement(cinfo);
                if(elem.getModifiers().contains(Modifier.PUBLIC)) {
                    List<? extends VariableElement> params = elem.getParameters();
                    Class[] paramTypes = new Class[params.size()];
                    String[] paramNames = new String[params.size()];
                    int i = 0;
                    ClassLoader cl = lu.getSourceUnit().getClassLoader();
                    try {
                        for(VariableElement param : params) {
                            paramTypes[i] = ClassUtil.getClass(param.asType().toString(), cl);
                            paramNames[i++] = param.getSimpleName().toString();
                        }
                        MethodTree tree = cinfo.getTrees().getTree(elem);
                        Class retType = ClassUtil.getClass(elem.getReturnType().toString(), cl);
                        return new ContextMethod(lu, elem.getSimpleName().toString(),
                                m.getModifierFlags(tree), retType,
                                paramTypes, paramNames, Method.getBodyText(cinfo, tree),
                                m.getCommentText(cinfo, tree));
                    }catch(ClassNotFoundException cnfe) {
                    }
                }
                return null;
            }
        }, lu.getSourceUnit().getJavaUnit().getFileObject());
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
        JavaClass javaClass = lu.getSourceUnit().getThisClass();
        Method m = javaClass.getMethod(methodName, parameterTypes);
        return getContextMethod(m);
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
        JavaClass javaClass = lu.getSourceUnit().getThisClass();
        Method m = javaClass.getPublicMethod(method.getName(), method.getParameterTypes());
        if(m != null)
            throw new IllegalArgumentException(
                    NbBundle.getMessage(LiveUnit.class, "IllegalMethod"));  //NOI18N;
        
        //Add the new method at the end
        m = javaClass.addMethod(method);
        if(m != null) {
            //Check if there are errors because of adding method
            if(lu.getSourceUnit().getJavaUnit().getErrors().length > 0)
                throw new IllegalArgumentException(
                        NbBundle.getMessage(LiveUnit.class, "IllegalSource"));  //NOI18N;
            return true;
        }
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
        Class[] paramTypes = method.getParameterTypes();
        Method m = javaClass.getPublicMethod(method.getName(), paramTypes);
        if(m != null) {
            m.update(method);
            //Check if there are errors because of updating method
                /*
                if(lu.getSourceUnit().getJavaUnit().getErrors().length > 0)
                    throw new IllegalArgumentException(
                            NbBundle.getMessage(LiveUnit.class, "IllegalSource"));  //NOI18N;
                 */
            ContextMethod retVal = getContextMethod(m);
            return retVal;
        }else {
            throw new IllegalArgumentException(
                    NbBundle.getMessage(LiveUnit.class, "IllegalMethod"));  //NOI18N;
        }
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

        JavaClass javaClass = lu.getSourceUnit().getThisClass();
        Class[] paramTypes = method.getParameterTypes();
        if(paramTypes == null)
            paramTypes = new Class[0];
        Method m = javaClass.getPublicMethod(method.getName(), paramTypes);
        if(m != null) {
            m.remove();
        } else {
            throw new IllegalArgumentException(
                    NbBundle.getMessage(LiveUnit.class, "IllegalMethod"));  //NOI18N;
        }
        return true;
    }
}

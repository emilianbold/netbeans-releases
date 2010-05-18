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

package com.sun.rave.designtime;

import java.lang.reflect.Modifier;

/**
 * <p>The ContextMethod class represents a single source code method on a {@link DesignContext}.
 * Use the ContextMethod class to create, update, and remove methods via the DesignContext methods:
 * {@link DesignContext#createContextMethod(ContextMethod)},
 * {@link DesignContext#updateContextMethod(ContextMethod)}, and
 * {@link DesignContext#removeContextMethod(ContextMethod)}.  Use
 * {@link DesignContext#getContextMethods()} to retrieve the list of methods on a DesignContext.</p>
 *
 * @author Joe Nuxoll
 */
public class ContextMethod {

    /**
     * Constructs a default ContextMethod with nothing specified.
     */
    public ContextMethod() {}

    /**
     * Constructs a default ContextMethod with only the DesignContext specified.
     *
     * @param designContext DesignContext for this ContextMethod
     */
    public ContextMethod(DesignContext designContext) {
        this.designContext = designContext;
    }

    /**
     * Constructs a ContextMethod with the specified DesignContext and name.
     *
     * @param designContext DesignContext for this ContextMethod
     * @param name The method name for this ContextMethod
     */
    public ContextMethod(DesignContext designContext, String name) {
        this.designContext = designContext;
        this.name = name;
    }

    /**
     * Constructs a ContextMethod with the specified DesignContext, name, modifiers,
     * returnType, parameterTypes, and parameterNames.
     *
     * @param designContext DesignContext for this ContextMethod
     * @param name The method name for this ContextMethod
     * @param modifiers The method {@link Modifier} bits
     * @param returnType The return type for this ContextMethod
     * @param parameterTypes The parameter types for this ContextMethod
     * @param parameterNames The parameter names for this ContextMethod
     */
    public ContextMethod(DesignContext designContext, String name, int modifiers,
        Class returnType, Class[] parameterTypes, String[] parameterNames) {

        this.designContext = designContext;
        this.name = name;
        this.modifiers = modifiers;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
        this.parameterNames = parameterNames;
    }

    /**
     * Constructs a ContextMethod with the specified DesignContext, name, modifiers,
     * returnType, parameterTypes, parameterNames, methodBody, and commentText.
     *
     * @param designContext DesignContext for this ContextMethod
     * @param name The method name for this ContextMethod
     * @param modifiers The method {@link Modifier} bits
     * @param parameterTypes The parameter types for this ContextMethod
     * @param parameterNames The parameter names for this ContextMethod
     * @param returnType The return type for this ContextMethod
     * @param methodBody The Java source code for the body of this ContextMethod
     * @param commentText The comment text for this ContextMethod
     */
    public ContextMethod(DesignContext designContext, String name, int modifiers,
        Class returnType, Class[] parameterTypes, String[] parameterNames,
        String methodBodyText, String commentText) {

        this.designContext = designContext;
        this.name = name;
        this.modifiers = modifiers;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
        this.parameterNames = parameterNames;
        this.methodBodyText = methodBodyText;
        this.commentText = commentText;
    }

    /**
     * Returns the DesignContext associated with this DesignContext
     *
     * @return The DesignContext associated with this DesignContext
     */
    public DesignContext getDesignContext() {
        return designContext;
    }

    /**
     *
     * @param designContext DesignContext
     */
    public void setDesignContext(DesignContext designContext) {
        this.designContext = designContext;
    }

    /**
     *
     * @param name String
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param modifiers int
     */
    public void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }

    /**
     *
     * @return int
     */
    public int getModifiers() {
        return modifiers;
    }

    /**
     *
     * @param returnType Class
     */
    public void setReturnType(Class returnType) {
        this.returnType = returnType;
    }

    /**
     *
     * @return Class
     */
    public Class getReturnType() {
        return returnType;
    }

    /**
     *
     * @param parameterTypes Class[]
     */
    public void setParameterTypes(Class[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    /**
     *
     * @return Class[]
     */
    public Class[] getParameterTypes() {
        return parameterTypes;
    }

    /**
     *
     * @param parameterNames String[]
     */
    public void setParameterNames(String[] parameterNames) {
        this.parameterNames = parameterNames;
    }

    /**
     *
     * @return String[]
     */
    public String[] getParameterNames() {
        return parameterNames;
    }

    /**
     *
     * @param exceptionTypes Class[]
     */
    public void setExceptionTypes(Class[] exceptionTypes) {
        this.exceptionTypes = exceptionTypes;
    }

    /**
     *
     * @return Class[]
     */
    public Class[] getExceptionTypes() {
        return exceptionTypes;
    }

    /**
     *
     * @param methodBody String
     */
    public void setMethodBodyText(String methodBodyText) {
        this.methodBodyText = methodBodyText;
    }

    /**
     *
     * @return String
     */
    public String getMethodBodyText() {
        return methodBodyText;
    }

    /**
     *
     * @param commentText String
     */
    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    /**
     *
     * @return String
     */
    public String getCommentText() {
        return commentText;
    }

    private DesignContext  designContext;
    private String         name;
    private Class[]        parameterTypes;
    private String[]       parameterNames;
    private Class          returnType;
    private Class[]        exceptionTypes;
    private int            modifiers;
    private String         methodBodyText;
    private String         commentText;
}

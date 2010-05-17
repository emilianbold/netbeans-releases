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
package org.netbeans.modules.visualweb.insync.java;

import java.lang.reflect.Array;
import java.util.HashMap;
import org.netbeans.modules.visualweb.jsfsupport.container.FacesContainer;

/**
 * Provides utilities to obtain the class given the identifier or type name
 *
 * @author jdeva
 *
 */
public class ClassUtil {

    /**
     * Get the actual class given its type. The type may be a primitive and/or it
     * may be an array.
     *
     * @param type  The type retrieve the Class for.
     * @return The Class that represents the given type.
     * @throws ClassNotFoundException
     */
    public static Class getClass(String type, ClassLoader cl) throws ClassNotFoundException {
        if(type == null)
            return null;

        if (type.endsWith("[]")) {
            String ctype = type.substring(0, type.length()-2);
            return Array.newInstance(getClass(ctype, cl), 0).getClass();
        }

        if (type.equals("boolean") || type.equals("Z"))  //NOI18N
            return Boolean.TYPE;
        if (type.equals("byte") || type.equals("B"))  //NOI18N
            return Byte.TYPE;
        if (type.equals("char") || type.equals("C"))  //NOI18N
            return Byte.TYPE;
        if (type.equals("double") || type.equals("D"))  //NOI18N
            return Double.TYPE;
        if (type.equals("float") || type.equals("F"))  //NOI18N
            return Float.TYPE;
        if (type.equals("int") || type.equals("I"))  //NOI18N
            return Integer.TYPE;
        if (type.equals("long") || type.equals("J"))  //NOI18N
            return Long.TYPE;
        if (type.equals("short") || type.equals("S"))  //NOI18N
            return Short.TYPE;
        if (type.equals("void"))  //NOI18N
            return Void.TYPE;

        return Class.forName(type, true, cl);
    }

    /**
     * Get the actual class given its type. The type may be a primitive and/or it
     * may be an array.
     *
     * @param type  The type retrieve the Class for.
     * @return The Class that represents the given type.
     * @throws ClassNotFoundException
     */
    public static Class getClass(String type) throws ClassNotFoundException {
        //Use the project classloader to load the class
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        // assert contextClassLoader.getClass() == org.netbeans.modules.visualweb.insync.ModelSet.ProjectClassLoader.class;
        return getClass(type, contextClassLoader);
    }
}

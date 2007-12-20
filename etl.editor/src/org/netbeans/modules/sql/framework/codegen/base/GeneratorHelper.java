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

package org.netbeans.modules.sql.framework.codegen.base;

import org.netbeans.modules.sql.framework.codegen.AbstractGeneratorFactory;
import org.netbeans.modules.sql.framework.codegen.Generator;
import com.sun.sql.framework.exception.BaseException;

/**
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class GeneratorHelper {

    public static Generator create(String className, AbstractGeneratorFactory factory) throws BaseException {
        Generator generator = null;
        if (className != null) {
            try {
                Class cls = Class.forName(className);
                generator = (Generator) cls.newInstance();
                generator.setGeneratorFactory(factory);
            } catch (ClassNotFoundException ex1) {
                throw new BaseException("Cannot create an instance of generator of class " + className, ex1);
            } catch (InstantiationException ex2) {
                throw new BaseException("Cannot create an instance of generator of class " + className, ex2);
            } catch (IllegalAccessException ex3) {
                throw new BaseException("Cannot create an instance of generator of class " + className, ex3);
            }
        }

        return generator;
    }
}
/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import java.lang.reflect.*;
import org.netbeans.modules.form.codestructure.*;

/**
 * @author Tomas Pavek
 */

public interface CreationDescriptor {

    // style flags - for finding best creator for a set or properties
    public static final int CHANGED_ONLY = 1;
    public static final int PLACE_ALL = 2;

    public Class getDescribedClass();

//    public boolean isCreationProperty(String propName);

    public Creator[] getCreators();

    public Creator findBestCreator(FormProperty[] properties, int style);

    public Object createDefaultInstance()
        throws InstantiationException, IllegalAccessException,
               IllegalArgumentException, InvocationTargetException;

    // ---------

    public interface Creator {

        public int getParameterCount();

        public Class[] getParameterTypes();

        public Class[] getExceptionTypes();

        public String[] getPropertyNames();

        public Object createInstance(FormProperty[] props)
            throws InstantiationException, IllegalAccessException,
                   IllegalArgumentException, InvocationTargetException;

        // [this will become useless when we can rely on getCodeOrigin(...)]
        public String getJavaCreationCode(FormProperty[] props);

        public CodeExpressionOrigin getCodeOrigin(CodeExpression[] params);
    }
}

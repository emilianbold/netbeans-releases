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

package org.netbeans.modules.soa.ui;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import org.openide.ErrorManager;

/**
 * Contains the objects of the specific base class T.
 * The objects can be obtained by the particular class.
 * If there isn't the object with required class, then it is created
 * with the help of default constructor.
 * Hence, it's implied that the class has the default constructor.
 *
 * @author nk160297
 */
public class ClassBasedPool<T> {
    
    private Map<Class<T>, T> myPool = new HashMap<Class<T>, T>();
    
    public ClassBasedPool() {
    }
    
    public <P extends T> P getObjectByClass(Class<P> tClass) {
        P result = (P)myPool.get(tClass);
        //
        if (result == null) {
            Constructor<P> defaultConstructor = null;
            try {
                defaultConstructor = tClass.getConstructor();
            } catch (Exception ex) {
                // do nothing
            }
            if (defaultConstructor != null) {
                try {
                    result = defaultConstructor.newInstance();
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ex);
                    // do nothing
                }
            }
            //
            if (result != null) {
                myPool.put((Class<T>)tClass, result);
            }
        }
        //
        return result;
    }
    
}

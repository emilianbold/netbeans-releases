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

package org.netbeans.modules.iep.model.lib;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.netbeans.modules.iep.model.lib.TcgComponent;
import org.netbeans.modules.iep.model.lib.TcgProperty;

/**
 * Description of the Class
 *
 * @author    Bing Lu
 * @created   May 14, 2003
 */
public class TcgComponentProxy implements InvocationHandler {
    /** Description of the Field */
    private TcgComponent mDelegate = null;

    /** Description of the Field */
    private final static Map mMethods =
            new HashMap();

    static {
        Method[] ms = TcgComponent.class.getDeclaredMethods();
        for (int i = 0; i < ms.length; i++) {
            mMethods.put(ms[i].getName(), ms[i].getName());
        }
    }

    /**
     * Constructor for the TcgComponentProxy object
     *
     * @param delegate  Description of the Parameter
     * @param cl        Description of the Parameter
     */
    public TcgComponentProxy(TcgComponent delegate) {
        mDelegate = delegate;
    }

    /**
     * Description of the Method
     *
     * @param proxy          Description of the Parameter
     * @param m              Description of the Parameter
     * @param args           Description of the Parameter
     * @return               Description of the Return Value
     * @exception Throwable  Description of the Exception
     */
    public Object invoke(Object proxy, Method m, Object[] args)
             throws Throwable {
        Object ret = null;

        String methodName = m.getName();

        if (mMethods.get(methodName) != null) {
            ret = m.invoke(mDelegate, args);
        } else if (methodName.startsWith("get")) {
            String propertyName = methodName.substring(3);

            TcgProperty property = mDelegate.getProperty(
                    propertyName);

            ret = property.getValue();
        } else if (methodName.startsWith("set")) {
            String propertyName = methodName.substring(3);

            TcgProperty property = mDelegate.getProperty(
                    propertyName);

            property.setValue(args[0]);

            ret = null;
        } else {
            throw new UnsupportedOperationException(methodName);
        }

        return ret;
    }

    /**
     * Description of the Method
     *
     * @param delegate  Description of the Parameter
     * @param cl        Description of the Parameter
     * @return          Description of the Return Value
     */
    public static Object newProxy(TcgComponent delegate, Class cl) {
        TcgComponentProxy handler = new TcgComponentProxy(delegate);
        Object proxy = Proxy.newProxyInstance(
                delegate.getClass().getClassLoader(),
                new Class[]{TcgComponent.class, cl}, handler);
        return proxy;
    }

    /**
     * The main program for the TcgComponentProxy class
     *
     * @param args  The command line arguments
     */
    public static void main(String[] args) {
    }
}

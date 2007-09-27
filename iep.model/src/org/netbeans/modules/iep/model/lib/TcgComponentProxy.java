/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

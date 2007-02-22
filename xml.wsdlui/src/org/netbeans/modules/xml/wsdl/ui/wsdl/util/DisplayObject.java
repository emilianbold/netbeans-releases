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

package org.netbeans.modules.xml.wsdl.ui.wsdl.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.openide.ErrorManager;

public class DisplayObject {
    private Object mObj = null;
    private String mMethodToGetDisplayName = null;
    private String mDisplayName = null;
    public DisplayObject (Object obj, String methodToGetDisplayName) {
        mObj = obj;
        mMethodToGetDisplayName = methodToGetDisplayName;
    }

    public DisplayObject(String displayName, Object value) {
        mObj = value;
        mDisplayName = displayName;
    }
    
    public Object getValue() {
        return mObj;
    }
    
    @Override
    public String toString() {
        if (mDisplayName != null) {
            return mDisplayName;
        }
        String str = "";
        if (mMethodToGetDisplayName != null) {
            Class c = mObj.getClass();
            Class[] parameterTypes = new Class[] {String.class};
            Method getDisplayNameMethod;
            Object[] arguments = new Object[] {};
            try {
                getDisplayNameMethod = c.getMethod(mMethodToGetDisplayName, parameterTypes);
                str = (String) getDisplayNameMethod.invoke(mObj, arguments);
            } catch (NoSuchMethodException e) {
                ErrorManager.getDefault().notify(e);
            } catch (IllegalAccessException e) {
                ErrorManager.getDefault().notify(e);
            } catch (InvocationTargetException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        return str;

    }
}

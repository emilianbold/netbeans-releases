/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.debugger.jpda.backend.truffle;

import com.oracle.truffle.api.debug.DebugValue;
import com.oracle.truffle.api.source.SourceSection;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Martin
 */
public class TruffleObject {
    
    static final int DISPLAY_TRIM = 1000;
    
    final String name;
    final String type;
    final String displayValue;
    final boolean writable;
    final boolean leaf;
    final boolean isArray;
    final Collection<DebugValue> properties;
    final List<DebugValue> array;
    final SourcePosition valueSourcePosition;
    final SourcePosition typeSourcePosition;

    TruffleObject(DebugValue value) {
        this.name = value.getName();
        //System.err.println("new TruffleObject("+name+")");
        DebugValue metaObject = null;
        try {
            metaObject = value.getMetaObject();
        } catch (Exception | LinkageError ex) {
            LangErrors.exception("Value "+name+" .getMetaObject()", ex);
        }
        String typeStr = "";
        if (metaObject != null) {
            try {
                typeStr = metaObject.as(String.class);
            } catch (Exception ex) {
                LangErrors.exception("Meta object of "+name+" .as(String.class)", ex);
            }
        }
        this.type = typeStr;
        //this.object = value;
        String valueStr = null;
        try {
            valueStr = value.as(String.class);
        } catch (Exception ex) {
            LangErrors.exception("Value "+name+" .as(String.class)", ex);
        }
        if (valueStr == null) {
            // Hack for R:
            try {
                Method getMethod = DebugValue.class.getDeclaredMethod("get");
                getMethod.setAccessible(true);
                Object realValue = getMethod.invoke(value);
                valueStr = Objects.toString(realValue);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException |
                     NoSuchMethodException | SecurityException ex) {
                LangErrors.exception("Value "+name+" get() invocation", ex);
            }
        }
        this.displayValue = valueStr;
        //System.err.println("  have display value "+valueStr);
        this.writable = value.isWriteable();
        Collection<DebugValue> valueProperties;
        try {
            valueProperties = value.getProperties();
        } catch (Exception ex) {
            LangErrors.exception("Value "+name+" .getProperties()", ex);
            valueProperties = null;
        }
        this.properties = valueProperties;
        //System.err.println("  have properties");
        this.leaf = properties == null || properties.isEmpty();
        boolean valueIsArray;
        try {
            valueIsArray = value.isArray();
        } catch (Exception ex) {
            LangErrors.exception("Value "+name+" .isArray()", ex);
            valueIsArray = false;
        }
        this.isArray = valueIsArray;
        if (isArray) {
            List<DebugValue> valueArray;
            try {
                valueArray = value.getArray();
            } catch (Exception ex) {
                LangErrors.exception("Value "+name+" .getArray()", ex);
                valueArray = null;
            }
            this.array = valueArray;
        } else {
            this.array = null;
        }
        SourcePosition sp = null;
        try {
            SourceSection sourceLocation = value.getSourceLocation();
            //System.err.println("\nSOURCE of "+value.getName()+" is: "+sourceLocation);
            if (sourceLocation != null) {
                sp = JPDATruffleDebugManager.getPosition(sourceLocation);
            }
        } catch (Exception | LinkageError ex) {
            LangErrors.exception("Value "+name+" .getSourceLocation()", ex);
        }
        this.valueSourcePosition = sp;
        sp = null;
        if (metaObject != null) {
            try {
                SourceSection sourceLocation = metaObject.getSourceLocation();
                //System.err.println("\nSOURCE of metaobject "+metaObject+" is: "+sourceLocation);
                if (sourceLocation != null) {
                    sp = JPDATruffleDebugManager.getPosition(sourceLocation);
                }
            } catch (Exception | LinkageError ex) {
                LangErrors.exception("Meta object of "+name+" .getSourceLocation()", ex);
            }
        }
        this.typeSourcePosition = sp;
        /*try {
            System.err.println("new TruffleObject("+name+") displayValue = "+displayValue+", leaf = "+leaf+", properties = "+properties);
        } catch (Exception ex) {
            ex.printStackTrace();
        }*/
    }

    public TruffleObject[] getProperties() {
        if (properties == null) {
            return new TruffleObject[]{};
        }
        int n = 0;
        try {
            n = properties.size();
        } catch (Exception ex) {
            LangErrors.exception("Value "+name+" properties.size()", ex);
        }
        TruffleObject[] children = new TruffleObject[n];
        if (n == 0) {
            return children;
        }
        int i = 0;
        for (DebugValue ch : properties) {
            children[i++] = new TruffleObject(ch);
        }
        return children;
    }

    public int getArraySize() {
        return (array != null) ? array.size() : 0;
    }

    public TruffleObject[] getArrayElements() {
        int n = getArraySize();
        TruffleObject[] elements = new TruffleObject[n];
        if (n == 0) {
            return elements;
        }
        int i = 0;
        for (DebugValue elm : array) {
            elements[i++] = new TruffleObject(elm);
        }
        return elements;
    }

    @Override
    public String toString() {
        return name + " = " + displayValue;
    }

}

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
package org.netbeans.modules.edm.editor.utils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @author Ahimanikya Satapathy
 */
public class AttributeFactory {

    private static HashMap tagToClassMap = new HashMap();
    

    static {
        tagToClassMap.put("attr", "org.netbeans.modules.edm.editor.utils.Attribute");
    }

    public static Object invokeGetter(Object bean, String propertyName) throws NoSuchMethodException, InvocationTargetException,
            IllegalAccessException, IntrospectionException {
        return invokeGetter(bean, propertyName, null, null);
    }

    public static Object invokeGetter(Object obj, String propertyName, Class[] parameterTypes, Object[] params) throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException, IntrospectionException {

        String base = capitalize(propertyName);
        Method readMethod;

        // Since there can be multiple setter methods but only one getter
        // method, find the getter method first so that you know what the
        // property type is. For booleans, there can be "is" and "get"
        // methods. If an "is" method exists, this is the official
        // reader method so look for this one first.
        try {
            readMethod = obj.getClass().getMethod("is" + base, parameterTypes);
        } catch (Exception getterExc) {
            // no "is" method, so look for a "get" method.
            readMethod = obj.getClass().getMethod("get" + base, parameterTypes);
        }

        return readMethod.invoke(obj, params);

    }

    public static Object invokeSetter(Object bean, String propertyName, Object val) throws NoSuchMethodException, InvocationTargetException,
            IllegalAccessException, IntrospectionException {

        PropertyDescriptor pd = new PropertyDescriptor(propertyName, bean.getClass());
        Method method = pd.getWriteMethod();
        return method.invoke(bean, new Object[]{val});
    }

    public static void invokeSetters(Object obj, Attributes attrs) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException,
            IntrospectionException {

        for (int i = 0; i < attrs.getLength(); i++) {
            if (attrs.getQName(i).equals("class")) {
                continue;
            }
            Class[] cls = new Class[]{String.class};
            String base = capitalize(attrs.getQName(i));

            Method method = obj.getClass().getMethod("set" + base, cls);
            method.invoke(obj, new Object[]{attrs.getValue(attrs.getQName(i))});
        }

    }

    static String capitalize(String s) {
        if (s.length() == 0) {
            return s;
        }
        char chars[] = s.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }
    private Map attributeMap = new HashMap();

    public AttributeFactory() {
    }

    public void endElement(String uri, String localName, String qName) {
    }

    public Map getAttributeMap() {
        return this.attributeMap;
    }

    public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
        try {
            if (qName.equals("attr")) {
                Attribute attr = (Attribute) createObject(uri, localName, qName, attrs);
                attributeMap.put(attr.getAttributeName(), attr);
            }

        } catch (Exception ex) {
            String msg = "Error occured while parsing following :" + "\n uri = " + uri + "\n localName = " + localName + "\n qName = " + qName;
            throw new SAXException(msg, ex);
        }
    }

    private Object createObject(String uri, String localName, String qName, Attributes attrs) throws NoSuchMethodException,
            InvocationTargetException, ClassNotFoundException, InstantiationException, IllegalAccessException, IntrospectionException {

        String className = attrs.getValue("class");
        if (className == null) {
            className = (String) tagToClassMap.get(qName);
            if (className == null) {
                return null;
            }
        }

        Class cl = Class.forName(className, true, getClass().getClassLoader());
        Object obj = cl.newInstance();
        invokeSetters(obj, attrs);
        return obj;
    }
}

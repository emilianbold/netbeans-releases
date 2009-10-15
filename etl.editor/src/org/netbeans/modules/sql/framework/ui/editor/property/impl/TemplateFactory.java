/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.sql.framework.ui.editor.property.impl;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Stack;
import org.netbeans.modules.sql.framework.ui.editor.property.IElement;
import org.netbeans.modules.sql.framework.ui.editor.property.INode;
import org.netbeans.modules.sql.framework.ui.editor.property.IResource;
import org.netbeans.modules.sql.framework.ui.editor.property.ITemplateGroup;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class TemplateFactory {

    private static HashMap<String, Class> propertyTypeToClassMap = new HashMap<String, Class>();
    private static HashMap<String, String> tagToClassMap = new HashMap<String, String>();
    static {
        propertyTypeToClassMap.put("string", new String().getClass());
        propertyTypeToClassMap.put("options", new String().getClass());
        propertyTypeToClassMap.put("object", new String().getClass());
        propertyTypeToClassMap.put("boolean", new String().getClass());
    }
    static {
        tagToClassMap.put("TemplateGroup", "org.netbeans.modules.sql.framework.ui.editor.property.impl.TemplateGroup");
        tagToClassMap.put("Template", "org.netbeans.modules.sql.framework.ui.editor.property.impl.Template");
        tagToClassMap.put("PropertyGroup", "org.netbeans.modules.sql.framework.ui.editor.property.impl.PropertyGroup");
        tagToClassMap.put("Property", "org.netbeans.modules.sql.framework.ui.editor.property.impl.BasicPropertySupport");
        tagToClassMap.put("OptionProperty", "org.netbeans.modules.sql.framework.ui.editor.property.impl.OptionPropertySupport");
        tagToClassMap.put("Option", "org.netbeans.modules.sql.framework.ui.editor.property.impl.BasicOption");
    }

    public static Object invokeGetter(Object bean, String propertyName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IntrospectionException {
        return invokeGetter(bean, propertyName, null, null);
    }

    public static Object invokeGetter(Object obj, String propertyName, Class[] parameterTypes, Object[] params) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IntrospectionException {

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

    public static Object invokeSetter(Object bean, String propertyName, Object val) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IntrospectionException {
        PropertyDescriptor pd = new PropertyDescriptor(propertyName, bean.getClass());
        Method method = pd.getWriteMethod();
        return method.invoke(bean, new Object[]{val});
    }

    public static void invokeSetters(Object obj, Attributes attrs) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IntrospectionException {

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
        char[] chars = s.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }
    private IResource rManager;
    private Stack<Object> stack = new Stack<Object>();
    private ITemplateGroup tg;

    /** Creates a new instance of PropertyFactory */
    public TemplateFactory(IResource resManager) {
        this.rManager = resManager;
    }

    public void endElement(String uri, String localName, String qName) {
        if (stack.size() != 0) {
            stack.pop();
        }
    }

    public ITemplateGroup getTemplateGroup() {
        return tg;
    }

    public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
        try {
            Object obj = null;
            obj = createObject(uri, localName, qName, attrs);
            if (qName.equals("TemplateGroup")) {
                tg = (TemplateGroup) obj;
            }

            if (obj != null) {
                IElement element = (IElement) obj;

                if (stack.size() != 0) {
                    INode node = (INode) stack.peek();
                    node.add(element);
                }

                String partialKeyName = getBundleKeyName(element);
                // set localized values from bundle
                if (partialKeyName != null) {

                    String dName = rManager.getLocalizedValue(partialKeyName + "_DISPLAYNAME");
                    element.setDisplayName(dName);

                    String tTip = rManager.getLocalizedValue(partialKeyName + "_TOOLTIP");
                    element.setToolTip(tTip);
                }

                stack.push(obj);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new SAXException("Error occured while parsing following :" + "\n uri = " + uri + "\n localName = " + localName + "\n qName = " + qName, ex);
        }
    }

    private Object createObject(String uri, String localName, String qName, Attributes attrs) throws NoSuchMethodException, InvocationTargetException, ClassNotFoundException, InstantiationException, IllegalAccessException, IntrospectionException {

        String className = attrs.getValue("class");
        if (className == null) {
            className = tagToClassMap.get(qName);
            if (className == null) {
                return null;
            }
        }

        Class cl = Class.forName(className);
        Object obj = cl.newInstance();
        invokeSetters(obj, attrs);
        return obj;
    }

    private String getBundleKeyName(IElement elm) {
        if (elm == null || elm.getName() == null) {
            return null;
        }
        StringBuilder strBuf = new StringBuilder(elm.getName());
        IElement parent = elm.getParent();

        while (parent != null) {
            String parentName = parent.getName();
            if (parentName != null) {
                strBuf.insert(0, parentName + "_");
            }
            parent = parent.getParent();
        }
        return strBuf.toString().toUpperCase();
    }
}

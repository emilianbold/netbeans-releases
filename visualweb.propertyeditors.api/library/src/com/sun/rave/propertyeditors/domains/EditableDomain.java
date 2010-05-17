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
package com.sun.rave.propertyeditors.domains;

import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.DesignContext;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * Specialized domain which may be extended by the addition and re-ordering of
 * elements. Added elements and information about order are stored in one of
 * three contexts: the design context, the project context, or the IDE context.
 * By default, an editable domain contains only its pre-defined elements in
 * their pre-defined order. Any changes made are "permanent" within the context
 * defined by the domain. A storage context must be specified when an instance
 * of this class is created.
 *
 * <p>Editable domains impose the restriction that their elements must all
 * contain values of the same final type. This is in part to facilitate user
 * editing of values in the IDE, in part to make storage of elements more
 * predictable. A value class must be specified when an instance of this class
 * is created. Elements added whose value is not of this type will be rejected.
 *
 * <p><strong>Nota Bene:</strong> Currently only the following value classes
 * will be stored correctly, due to limitations in the design-time support for
 * data storage:
 * <ul>
 *     <li><code>java.lang.String</code></li>
 *     <li><code>java.lang.Integer</code></li>
 *     <li><code>java.util.Date</code></li>
 * </ul>
 *
 */

//TODO Storage proxy and saving/retrieving logic should be moved to editors, so that mods can be canceled

public abstract class EditableDomain extends AttachedDomain {

    protected static int DESIGN_CONTEXT_STORAGE = 0;
    protected static int PROJECT_STORAGE = 1;
    protected static int IDE_STORAGE = 2;

    private int storageScope;
    private Class elementValueClass;
    protected ArrayList elements;

    /**
     * Creates a new instance of ExtensibleDomain, for elements whose values
     * are of type <code>java.lang.String</code>, for which elements will be
     * stored in the scope specified. If the storage scope constant is not
     * recognized, storage will default to the design context.
     */
    protected EditableDomain(int storageScope) {
        this(storageScope, String.class);
    }

    /**
     * Creates a new instance of ExtensibleDomain, for elements whose values
     * are of the class specified, and for which elements will be stored in the
     * scope specified. If the storage scope constant is not recognized, storage
     * will default to the design context.
     */
    protected EditableDomain(int storageScope, Class elementValueClass) {
        this.elementValueClass = elementValueClass;
        if (storageScope == DESIGN_CONTEXT_STORAGE || storageScope == PROJECT_STORAGE ||
                storageScope == IDE_STORAGE)
            this.storageScope = storageScope;
        else
            this.storageScope = DESIGN_CONTEXT_STORAGE;
        this.elements = new ArrayList();
    }

    /**
     * Returns the class of all element values in this domain. Normally, a domain
     * may contain elements of any value. Editable domains require that their
     * elements' values be all of the same type.
     */
    public Class getElementValueClass() {
        return this.elementValueClass;
    }

    /**
     * Returns an array of the elements currently contained in this domain.
     */
    public Element[] getElements() {
        return (Element[]) elements.toArray(Element.EMPTY_ARRAY);
    }

    /**
     * Returns the element at the index specified. If the index is out of bounds
     * or no element exists at the index specified, returns null.
     */
    public Element getElementAt(int index) {
        if (index < 0 || index > elements.size())
            return null;
        return (Element)elements.get(index);
    }

    /**
     * Returns the number of elements currently in this domain.
     */
    public int getSize() {
        return elements.size();
    }

    /**
     * Replace the element at the index specified with the element specified.
     * Returns the element previously at the index specified, null if there
     * wasn't one.
     */
    public Element setElementAt(int index, Element element) {
        Element previousElement = (Element) elements.set(index, element);
        refreshStorageProxy();
        return previousElement;
    }

    /**
     * Add an element to this domain at the index specified. Elements at this
     * and subsequent indexes are all shifted "down" one to make room for the
     * new element.
     */
    public void addElementAt(int index, Element element) {
        elements.add(index, element);
        refreshStorageProxy();
    }

    /**
     * Add an element to the end of this domain's list of elements.
     */
    public void addElement(Element element) {
        elements.add(element);
        refreshStorageProxy();
    }

    /**
     * Remove the element at the index specified, and return it. Subsequent
     * elements are shifted "up" one to vill the void. Returns null if there
     * is no element at the index specified, or if the index is out of bounds.
     */
    public Element removeElementAt(int index) {
        Element previousElement = (Element)elements.remove(index);
        refreshStorageProxy();
        return previousElement;
    }

    /**
     * Set the {@link DesignProperty} with which this domain is associated,
     * and check for a previously stored proxy for this domain's elements. If
     * one is found, then update this domain's elements to reflect what was
     * found in storage.
     *
     * @param designProperty The new associated {@link DesignProperty}
     */
    public void setDesignProperty(DesignProperty designProperty) {
        super.setDesignProperty(designProperty);
        // Check for a stored proxy list of elements
        DesignContext context = designProperty.getDesignBean().getDesignContext();
        Object proxyObject = null;
        if (this.storageScope == EditableDomain.DESIGN_CONTEXT_STORAGE)
            proxyObject = context.getContextData(this.getClass().getName());
        else if (this.storageScope == EditableDomain.PROJECT_STORAGE)
            proxyObject = context.getProject().getProjectData(this.getClass().getName());
        else
            proxyObject = context.getProject().getGlobalData(this.getClass().getName());
        // If no stored proxy list, then there is no saved domain state
        if (proxyObject == null || (proxyObject instanceof String && ((String) proxyObject).length() == 0))
            return;
        // If stored proxy list is of type StorageProxy, it was saved in memory,
        // so just retrieve the list of elements
        if (proxyObject instanceof StorageProxy) {
            this.elements = ((StorageProxy) proxyObject).getElements();
        // If stored proxy list is of type String, it was saved to file, so
        // restore it and then retrieve the list of elements
        } else if (proxyObject instanceof String) {
            this.storageProxy = StorageProxy.restoreInstance((String) proxyObject, this.elementValueClass);
            this.elements = this.storageProxy.getElements();
            refreshStorageProxy();
        }
    }

    private StorageProxy storageProxy;

    void refreshStorageProxy() {
        DesignContext context = designProperty.getDesignBean().getDesignContext();
        if (storageProxy == null)
            storageProxy = StorageProxy.newInstance(this.elements, this.elementValueClass);
        if (this.storageScope == EditableDomain.DESIGN_CONTEXT_STORAGE)
            context.setContextData(this.getClass().getName(), storageProxy);
        else if (this.storageScope == EditableDomain.PROJECT_STORAGE)
            // TO DO - Change string storage to object storage
            context.getProject().setProjectData(this.getClass().getName(), storageProxy.toString());
        else
            // TO DO - Change string storage to object storage
            context.getProject().setGlobalData(this.getClass().getName(), storageProxy.toString());
    }


    /** An abstract utility class used to wrap the domain's list of elements,
     * when passed to the design context for storage. If the storage proxy
     * needs to be saved, its <code>toString()</code> method will be called. A
     * new proxy is created using the static method <code>newIsntance()</code>.
     * If the proxy was stored to file, later calls to retrieve the storage
     * proxy will return the string instead. In this case, the storage proxy
     * must be restored, using the static method <code>restoreInstance()</code>.
     *
     * <p>There are implementations of StorageProxy for all the value object
     * types supported. These implementations take care of converting their
     * values to and from strings.
     */
    static abstract class StorageProxy {

        protected ArrayList elementList;

        private StorageProxy() {
        }

        public ArrayList getElements() {
            return elementList;
        }

        public static StorageProxy newInstance(ArrayList elementList, Class valueClass) {
            StorageProxy proxy;
            if (valueClass == String.class)
                proxy = new StringStorageProxy();
            else if (valueClass == Integer.class)
                proxy = new IntegerStorageProxy();
            else if (valueClass == Date.class)
                proxy = new DateStorageProxy();
            else
                return null;
            proxy.elementList = elementList;
            return proxy;
        }

        public static StorageProxy restoreInstance(String str, Class valueClass) {
            StorageProxy proxy;
            if (valueClass == String.class)
                proxy = new StringStorageProxy();
            else if (valueClass == Integer.class)
                proxy = new IntegerStorageProxy();
            else if (valueClass == Date.class)
                proxy = new DateStorageProxy();
            else
                return null;
            proxy.fromString(str);
            return proxy;
        }

        public String toString() {
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < elementList.size(); i++) {
                Element e = (Element) elementList.get(i);
                appendEncoded(buffer, e.getLabel());
                buffer.append('=');
                appendEncoded(buffer, valueToString(e.getValue()));
                buffer.append(',');
            }
            buffer.setLength(buffer.length() - 1);
            return buffer.toString();
        }

        public void fromString(String str) {
            StringTokenizer tokenizer = new StringTokenizer(str, ",");
            ArrayList elements = new ArrayList();
            DateFormat format = DateFormat.getDateTimeInstance();
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                int i = token.indexOf('=');
                String label = decode(token.substring(0, i));
                Object value = stringToValue(decode(token.substring(i+1)));
                elements.add(new Element(value, label));
            }
            this.elementList = elements;
        }

        abstract protected String valueToString(Object value);

        abstract protected Object stringToValue(String str);

    }

    static class StringStorageProxy extends StorageProxy {
        protected String valueToString(Object value) {
            return value.toString();
        }
        protected Object stringToValue(String str) {
            return str;
        }
    }

    static class IntegerStorageProxy extends StorageProxy {
        protected String valueToString(Object value) {
            return value.toString();
        }
        protected Object stringToValue(String str) {
            return new Integer(Integer.parseInt(str));
        }
    }

    static class DateStorageProxy extends StorageProxy {
        DateFormat format = DateFormat.getDateTimeInstance();
        protected String valueToString(Object value) {
            return format.format((Date) value);
        }
        protected Object stringToValue(String str) {
            try {
                return format.parse(str);
            } catch (ParseException e) {
                return null;
            }
        }
    }


    /**
     * Utility method to encode a string by escaping "=", "," and "%" using HTTP-style
     * escape sequences.
     */
    static void appendEncoded(StringBuffer buffer, String str) {
        char[] chars = str.toCharArray();
        for (int j = 0; j < chars.length; j++) {
            if (chars[j] == '=')
                buffer.append("%3D");
            else if (chars[j] == ',')
                buffer.append("%2C");
            else if (chars[j] == '%')
                buffer.append("%25");
            else
                buffer.append(chars[j]);
        }
    }

    /**
     * Utility method to decode a string by unescaping "=", "," and "%".
     */
    static String decode(String str) {
        return str.replaceAll("%3D", "=").replaceAll("%2C", ",").replaceAll("%25", "%");
    }

}

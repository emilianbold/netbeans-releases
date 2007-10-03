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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

/*
 * ConfigProperty.java
 *
 * Created on August 17, 2001, 1:48 PM
 */

package org.netbeans.modules.j2ee.sun.share.config.ui;

import java.beans.*;
import java.util.*;

import javax.enterprise.deploy.spi.DConfigBean;

import org.openide.nodes.*;

import org.netbeans.modules.j2ee.sun.share.config.ConfigBeanStorage;


public class ConfigProperty {

    public static Node.Property getBraindead(BeanDescriptor descrip) {
        return new Braindead(descrip);
    }

    public static Node.Property getProperty(Object bean,PropertyDescriptor property) {
        if(property instanceof IndexedPropertyDescriptor)
            return new Complex(bean,(IndexedPropertyDescriptor) property);
        return new Simple(bean,property);
    }
    
    public static Node.Property getFixedProperty(ConfigBeanStorage cbs, String xpath) {
        return new Fixed(cbs,xpath);
    }
    
    public static PropertyEditor getEditor(Object bean,PropertyDescriptor property,Class type) {
        PropertyEditor ed = null;
        try {
            ed = (PropertyEditor) property.getPropertyEditorClass().newInstance();
        } catch (Exception e) {}
        if(ed == null)
            ed = PropertyEditorManager.findEditor(type);
        if(ed == null)
            ed = new ConfigPropertyEditor(bean,type);
        //        System.out.println("Created editor " + ed);
        ed.setValue(bean);
        //        System.out.println("Set value to " + bean);
        return ed;
    }
    
    
    private static class Braindead extends PropertySupport.WriteOnly {
        
        Object obj = null;
        Class customizer;
        
        public Braindead(BeanDescriptor descrip) {
            super(descrip.getName(),descrip.getBeanClass(),descrip.getDisplayName(),descrip.getShortDescription());
            customizer = descrip.getCustomizerClass();
            System.err.println("bean customizer class " + customizer);
        }
        
        public void setValue(Object obj) throws java.lang.IllegalAccessException, java.lang.IllegalArgumentException, java.lang.reflect.InvocationTargetException {
            this.obj = obj;
        }
        
        public PropertyEditor getPropertyEditor() {
            return new PropertyEditorSupport() {
                public String getAsText() {return "No text";}
                public boolean supportsCustomEditor() { return true; }
                public synchronized java.awt.Component getCustomEditor() {
                    try {
                        Customizer foo = (Customizer) customizer.newInstance();
                        foo.setObject(obj);
                        return (java.awt.Component) foo;
                    } catch (Exception e) {
                        return null;
                    }
                }
            };
        }
        
    }
    
    private static class Simple extends PropertySupport.Reflection {
        
        PropertyDescriptor property;
        
        Simple(Object bean,PropertyDescriptor property) {
            super(bean,property.getPropertyType(),
            property.getReadMethod(),property.getWriteMethod());
            this.property = property;
            //        System.err.println("Simple property " + instance);
        }
        
        public PropertyEditor getPropertyEditor() {
            //        System.err.println("Editor for Simple property " + instance);
            return ConfigProperty.getEditor(instance,property,property.getPropertyType());
        }
        
        public String getName() {
            return property.getName();
        }
        
        public String getDisplayName() {
            return property.getDisplayName();
        }
        
        public String getShortDescription() {
            return property.getShortDescription();
        }
        
        public Object getValue() throws IllegalArgumentException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
            Object obj = super.getValue();
            if(obj == null)
                try {
                    obj = property.getPropertyType().newInstance();
                    setValue(obj);
                } catch (InstantiationException ie) {
                    //   throw new java.lang.reflect.InvocationTargetException(ie);
                    // PENDING should report the error but return gracefully
                }
            return obj;
        }
        
    }
    
    private static class Complex extends IndexedPropertySupport {
        
        IndexedPropertyDescriptor property;
        
        Complex(Object bean,IndexedPropertyDescriptor descriptor) {
            super(bean,descriptor.getPropertyType(),descriptor.getIndexedPropertyType(),
            descriptor.getReadMethod(),descriptor.getWriteMethod(),
            descriptor.getIndexedReadMethod(),descriptor.getIndexedWriteMethod());
            property = descriptor;
            //        System.err.println("Array property " + instance);
        }
        
        public PropertyEditor getIndexedPropertyEditor() {
            //        System.err.println("Editor for Array property " + instance);
            return ConfigProperty.getEditor(instance,property,property.getIndexedPropertyType());
        }
        
        public String getName() {
            return property.getName();
        }
        
        public String getDisplayName() {
            return property.getDisplayName();
        }
        
        public void setValue(Object obj) throws IllegalArgumentException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
            if(obj.getClass().isArray()) {
                Object[] arr = (Object[]) obj;
                for(int i = 0 ; i < arr.length; i++)
                    if(arr[i] == null)
                        try {
                            arr[i] = property.getIndexedPropertyType().newInstance();
                        } catch (InstantiationException ie) {
                            //  throw new java.lang.reflect.InvocationTargetException(ie);//"Blewed up");
                            // should return gracefully
                        }
            }
            super.setValue(obj);
        }
    }
    
    // A fixed array where the only editable portion is the bean properties
    // of an element of the array.
    private static class Fixed extends IndexedPropertySupport {
        ConfigBeanStorage cbs;
        String xpath;
        Collection objs = new HashSet();
        Class cl = DConfigBean.class;
        // should be able to handle changing arr array
        Fixed(ConfigBeanStorage cbs, String xpath) {
            super(cbs.getConfigBean(),(new DConfigBean[0]).getClass(),DConfigBean.class,
            null,null,null,null);
            this.cbs = cbs; this.xpath = xpath;
            String name = xpath.substring(xpath.lastIndexOf("/")+1);
            setName(name);
            setDisplayName(name);
            //        System.err.println("Fixed property " + instance);
        }
        
        public PropertyEditor getIndexedPropertyEditor() {
            //        System.err.println("Editor for Fixed property " + instance);
            //       System.out.println("Getting indexed editor");
            return ConfigProperty.getEditor(instance,null,cl);
        }
        
        public boolean canIndexedRead() { return true; }
        public boolean canIndexedWrite() { return false; }
        public boolean canRead() { return true; }
        public boolean canWrite() { return false; }
        
        public Object getIndexedValue(int index) {
            //        System.out.println("Getting indexed value");
            return ((ConfigBeanStorage)objs.toArray()[index]).getConfigBean();
        }
        
        public Object getValue() {
            //        System.out.println("Getting array value");
            Object[] arr = objs.toArray();
            DConfigBean[] cb = new DConfigBean[arr.length];
            for(int i = 0; i < cb.length; i++)
                cb[i] = ((ConfigBeanStorage)arr[i]).getConfigBean();
            return cb;
        }
        
        public void addElement(ConfigBeanStorage elem) {
            cl = elem.getConfigBean().getClass();
            objs.add(elem);
        }
        
        public void removeElement(ConfigBeanStorage elem) {
            objs.remove(elem);
        }
    }
}

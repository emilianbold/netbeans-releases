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

package org.netbeans.modules.compapp.javaee.sunresources.ui;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.SimpleBeanInfo;
import java.util.List;
import org.netbeans.modules.compapp.javaee.sunresources.generated.sunresources13.AdminObjectResource;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

/**
 *
 * @author echou
 */
public class AdminObjectResourceBeanNode extends AbstractNode {
    
    private AdminObjectResource bean;
    private FileObject fo;
    
    /** Creates a new instance of AdminObjectResourceBeanNode */
    public AdminObjectResourceBeanNode(AdminObjectResource bean, FileObject fo) {
        super(Children.LEAF);
        this.bean = bean;
        this.fo = fo;
        initPropertySheet();
    }
    
    private void initPropertySheet() {
        AdminObjectResourceBeanInfo info = new AdminObjectResourceBeanInfo();
        BeanNode.Descriptor d = BeanNode.computeProperties(bean, info);
        Sheet sets = getSheet();
        Sheet.Set pset = Sheet.createPropertiesSet();
        
        Node.Property fileProp = new PropertySupport.ReadOnly(
                "filePath", // NOI18N
                String.class, 
                NbBundle.getMessage(AdminObjectResourceBeanNode.class, "LBL_xmlpath_display"),
                NbBundle.getMessage(AdminObjectResourceBeanNode.class, "LBL_xmlpath_desc")
                ) {
                    public Object getValue() {
                        if (fo == null) {
                            return NbBundle.getMessage(AdminObjectResourceBeanNode.class, "LBL_not_exist");
                        } else {
                            return fo.getPath();
                        }
                    }
        };
        pset.put(fileProp);
        
        pset.put(d.property);
        Node.Property p = new PropertySupport.ReadWrite(
                "property", // NOI18N
                List.class, 
                NbBundle.getMessage(AdminObjectResourceBeanNode.class, "LBL_property"), 
                NbBundle.getMessage(AdminObjectResourceBeanNode.class, "LBL_property")
                ) {
                    public boolean canWrite() {
                        return false;
                    }
                    public Object getValue() {
                        return NbBundle.getMessage(AdminObjectResourceBeanNode.class, "LBL_extra_property");
                    }
                    public void setValue(Object val) {
                    }
                    public PropertyEditor getPropertyEditor() {
                        return new SunResourcePropertyEditor(bean.getProperty());
                    }
        };
        pset.put(p);
        sets.put(pset);
    }
    
    public class AdminObjectResourceBeanInfo extends SimpleBeanInfo {
    
        public PropertyDescriptor[] getPropertyDescriptors() {
            PropertyDescriptor[] propertyDescritpors = new PropertyDescriptor[6];
            try {
                propertyDescritpors[0] = new PropertyDescriptor("description",
                        AdminObjectResource.class, "getDescription", "setDescription");
                propertyDescritpors[1] = new PropertyDescriptor("jndi-name",
                        AdminObjectResource.class, "getJndiName", "setJndiName");
                propertyDescritpors[2] = new PropertyDescriptor("res-type",
                        AdminObjectResource.class, "getResType", "setResType");
                propertyDescritpors[3] = new PropertyDescriptor("res-adapter",
                        AdminObjectResource.class, "getResAdapter", "setResAdapter");
                propertyDescritpors[4] = new PropertyDescriptor("object-type",
                        AdminObjectResource.class, "getObjectType", "setObjectType");
                propertyDescritpors[4].setPropertyEditorClass(ObjectTypePropEditor.class);
                propertyDescritpors[5] = new PropertyDescriptor("enabled",
                        AdminObjectResource.class, "getEnabled", "setEnabled");
                propertyDescritpors[5].setPropertyEditorClass(BooleanPropEditor.class);
            } catch (IntrospectionException e) {
                e.printStackTrace();
            }
            return propertyDescritpors;
        }
    }
}

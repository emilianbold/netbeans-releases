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

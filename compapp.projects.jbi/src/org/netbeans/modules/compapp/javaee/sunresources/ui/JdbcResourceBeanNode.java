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
import org.netbeans.modules.compapp.javaee.sunresources.generated.sunresources13.JdbcConnectionPool;
import org.netbeans.modules.compapp.javaee.sunresources.generated.sunresources13.JdbcResource;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;

/**
 *
 * @author echou
 */
public class JdbcResourceBeanNode extends AbstractNode {
    
    private JdbcResource bean;
    private JdbcConnectionPool pool;
    private FileObject fo;
    
    /** Creates a new instance of JdbcResourceBeanNode */
    public JdbcResourceBeanNode(JdbcResource bean, JdbcConnectionPool pool,
            FileObject fo) {
        super(Children.LEAF);
        this.bean = bean;
        this.pool = pool;
        this.fo = fo;
        initPropertySheet();
    }
    
    private void initPropertySheet() {
        JdbcResourceBeanInfo info = new JdbcResourceBeanInfo();
        BeanNode.Descriptor d = BeanNode.computeProperties(bean, info);
        Sheet sets = getSheet();
        Sheet.Set pset = Sheet.createPropertiesSet();

        Node.Property fileProp = new PropertySupport.ReadOnly("filePath", String.class, "resource xml path", "filePath") {
            public Object getValue() {
                if (fo == null) {
                    return "not exist";
                } else {
                    return fo.getPath();
                }
            }
        };
        pset.put(fileProp);
        
        pset.put(d.property);
        Node.Property p = new PropertySupport.ReadWrite("property", List.class, "property", "property") {
            public boolean canWrite() {
                return false;
            }
            public Object getValue() {
                return "extra property";
            }
            public void setValue(Object val) {
            }
            public PropertyEditor getPropertyEditor() {
                return new SunResourcePropertyEditor(bean.getProperty());
            }
        };
        pset.put(p);
        sets.put(pset);
        
        // add pool properties
        if (pool != null) {
            JdbcConnectionPoolBeanInfo poolInfo = new JdbcConnectionPoolBeanInfo();
            BeanNode.Descriptor poolDescriptor = BeanNode.computeProperties(pool, poolInfo);
            Sheet.Set poolSet = Sheet.createExpertSet();
            poolSet.setDisplayName("ConnectionPool Properties");

            poolSet.put(poolDescriptor.property);
            Node.Property poolProperty = new PropertySupport.ReadWrite("property", List.class, "property", "property") {
                public boolean canWrite() {
                    return false;
                }
                public Object getValue() {
                    return "extra property";
                }
                public void setValue(Object val) {
                }
                public PropertyEditor getPropertyEditor() {
                    return new SunResourcePropertyEditor(pool.getProperty());
                }
            };
            poolSet.put(poolProperty);
            sets.put(poolSet);
        }
    }
    
    public class JdbcResourceBeanInfo extends SimpleBeanInfo {
    
        public PropertyDescriptor[] getPropertyDescriptors() {
            PropertyDescriptor[] propertyDescritpors = new PropertyDescriptor[5];
            try {
                propertyDescritpors[0] = new PropertyDescriptor("description",
                        JdbcResource.class, "getDescription", "setDescription");
                propertyDescritpors[1] = new PropertyDescriptor("jndi-name",
                        JdbcResource.class, "getJndiName", "setJndiName");
                propertyDescritpors[2] = new PropertyDescriptor("pool-name",
                        JdbcResource.class, "getPoolName", null);
                propertyDescritpors[3] = new PropertyDescriptor("object-type",
                        JdbcResource.class, "getObjectType", "setObjectType");
                propertyDescritpors[3].setPropertyEditorClass(ObjectTypePropEditor.class);
                propertyDescritpors[4] = new PropertyDescriptor("enabled",
                        JdbcResource.class, "getEnabled", "setEnabled");
                propertyDescritpors[4].setPropertyEditorClass(BooleanPropEditor.class);
            } catch (IntrospectionException e) {
                e.printStackTrace();
            }
            return propertyDescritpors;
        }
    }
    
    public class JdbcConnectionPoolBeanInfo extends SimpleBeanInfo {
    
        public PropertyDescriptor[] getPropertyDescriptors() {
            PropertyDescriptor[] propertyDescritpors = new PropertyDescriptor[17];
            try {
                propertyDescritpors[0] = new PropertyDescriptor("description",
                        JdbcConnectionPool.class, "getDescription", "setDescription");
                propertyDescritpors[1] = new PropertyDescriptor("name",
                        JdbcConnectionPool.class, "getName", null);
                propertyDescritpors[2] = new PropertyDescriptor("datasource-classname",
                        JdbcConnectionPool.class, "getDatasourceClassname", "setDatasourceClassname");
                propertyDescritpors[3] = new PropertyDescriptor("res-type",
                        JdbcConnectionPool.class, "getResType", "setResType");
                propertyDescritpors[3].setPropertyEditorClass(JdbcResTypeEditor.class);
                propertyDescritpors[4] = new PropertyDescriptor("steady-pool-size",
                        JdbcConnectionPool.class, "getSteadyPoolSize", "setSteadyPoolSize");
                propertyDescritpors[5] = new PropertyDescriptor("max-pool-size",
                        JdbcConnectionPool.class, "getMaxPoolSize", "setMaxPoolSize");
                propertyDescritpors[6] = new PropertyDescriptor("max-wait-time-in-millis",
                        JdbcConnectionPool.class, "getMaxWaitTimeInMillis", "setMaxWaitTimeInMillis");
                propertyDescritpors[7] = new PropertyDescriptor("pool-resize-quantity",
                        JdbcConnectionPool.class, "getPoolResizeQuantity", "setPoolResizeQuantity");
                propertyDescritpors[8] = new PropertyDescriptor("idle-timeout-in-seconds",
                        JdbcConnectionPool.class, "getIdleTimeoutInSeconds", "setIdleTimeoutInSeconds");
                propertyDescritpors[9] = new PropertyDescriptor("transaction-isolation-level",
                        JdbcConnectionPool.class, "getTransactionIsolationLevel", "setTransactionIsolationLevel");
                propertyDescritpors[9].setPropertyEditorClass(IsolationLevelEditor.class);
                propertyDescritpors[10] = new PropertyDescriptor("is-isolation-level-guaranteed",
                        JdbcConnectionPool.class, "getIsIsolationLevelGuaranteed", "setIsIsolationLevelGuaranteed");
                propertyDescritpors[10].setPropertyEditorClass(BooleanPropEditor.class);
                propertyDescritpors[11] = new PropertyDescriptor("is-connection-validation-required",
                        JdbcConnectionPool.class, "getIsConnectionValidationRequired", "setIsConnectionValidationRequired");
                propertyDescritpors[11].setPropertyEditorClass(BooleanPropEditor.class);
                propertyDescritpors[12] = new PropertyDescriptor("connection-validation-method",
                        JdbcConnectionPool.class, "getConnectionValidationMethod", "setConnectionValidationMethod");
                propertyDescritpors[12].setPropertyEditorClass(ConnValidationMethod.class);
                propertyDescritpors[13] = new PropertyDescriptor("validation-table-name",
                        JdbcConnectionPool.class, "getValidationTableName", "setValidationTableName");
                propertyDescritpors[14] = new PropertyDescriptor("fail-all-connections",
                        JdbcConnectionPool.class, "getFailAllConnections", "setFailAllConnections");
                propertyDescritpors[14].setPropertyEditorClass(BooleanPropEditor.class);
                propertyDescritpors[15] = new PropertyDescriptor("non-transactional-connections",
                        JdbcConnectionPool.class, "getNonTransactionalConnections", "setNonTransactionalConnections");
                propertyDescritpors[15].setPropertyEditorClass(BooleanPropEditor.class);
                propertyDescritpors[16] = new PropertyDescriptor("allow-non-component-callers",
                        JdbcConnectionPool.class, "getAllowNonComponentCallers", "setAllowNonComponentCallers");
                propertyDescritpors[16].setPropertyEditorClass(BooleanPropEditor.class);
            } catch (IntrospectionException e) {
                e.printStackTrace();
            }
            return propertyDescritpors;
        }
    }
}

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
import org.netbeans.modules.compapp.javaee.sunresources.generated.sunresources13.ConnectorConnectionPool;
import org.netbeans.modules.compapp.javaee.sunresources.generated.sunresources13.ConnectorResource;
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
public class ConnectorResourceBeanNode extends AbstractNode {
    
    private ConnectorResource bean;
    private ConnectorConnectionPool pool;
    private FileObject fo;
    
    /** Creates a new instance of ConnectorResourceBeanNode */
    public ConnectorResourceBeanNode(ConnectorResource bean, ConnectorConnectionPool pool,
            FileObject fo) {
        super(Children.LEAF);
        this.bean = bean;
        this.pool = pool;
        this.fo = fo;
        initPropertySheet();
    }
    
    private void initPropertySheet() {
        ConnectorResourceBeanInfo info = new ConnectorResourceBeanInfo();
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
            ConnectorConnectionPoolBeanInfo poolInfo = new ConnectorConnectionPoolBeanInfo();
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
    
    public class ConnectorResourceBeanInfo extends SimpleBeanInfo {
    
        public PropertyDescriptor[] getPropertyDescriptors() {
            PropertyDescriptor[] propertyDescritpors = new PropertyDescriptor[5];
            try {
                propertyDescritpors[0] = new PropertyDescriptor("description",
                        ConnectorResource.class, "getDescription", "setDescription");
                propertyDescritpors[1] = new PropertyDescriptor("jndi-name",
                        ConnectorResource.class, "getJndiName", "setJndiName");
                propertyDescritpors[2] = new PropertyDescriptor("pool-name",
                        ConnectorResource.class, "getPoolName", null);
                propertyDescritpors[3] = new PropertyDescriptor("object-type",
                        ConnectorResource.class, "getObjectType", "setObjectType");
                propertyDescritpors[3].setPropertyEditorClass(ObjectTypePropEditor.class);
                propertyDescritpors[4] = new PropertyDescriptor("enabled",
                        ConnectorResource.class, "getEnabled", "setEnabled");
                propertyDescritpors[4].setPropertyEditorClass(BooleanPropEditor.class);
            } catch (IntrospectionException e) {
                e.printStackTrace();
            }
            return propertyDescritpors;
        }
    }
    
    public class ConnectorConnectionPoolBeanInfo extends SimpleBeanInfo {
    
        public PropertyDescriptor[] getPropertyDescriptors() {
            PropertyDescriptor[] propertyDescritpors = new PropertyDescriptor[12];
            try {
                propertyDescritpors[0] = new PropertyDescriptor("description",
                        ConnectorConnectionPool.class, "getDescription", "setDescription");
                propertyDescritpors[1] = new PropertyDescriptor("name",
                        ConnectorConnectionPool.class, "getName", null);
                propertyDescritpors[2] = new PropertyDescriptor("resource-adapter-name",
                        ConnectorConnectionPool.class, "getResourceAdapterName", "setResourceAdapterName");
                propertyDescritpors[3] = new PropertyDescriptor("connection-definition-name",
                        ConnectorConnectionPool.class, "getConnectionDefinitionName", "setConnectionDefinitionName");
                propertyDescritpors[4] = new PropertyDescriptor("steady-pool-size",
                        ConnectorConnectionPool.class, "getSteadyPoolSize", "setSteadyPoolSize");
                propertyDescritpors[5] = new PropertyDescriptor("max-pool-size",
                        ConnectorConnectionPool.class, "getMaxPoolSize", "setMaxPoolSize");
                propertyDescritpors[6] = new PropertyDescriptor("max-wait-time-in-millis",
                        ConnectorConnectionPool.class, "getMaxWaitTimeInMillis", "setMaxWaitTimeInMillis");
                propertyDescritpors[7] = new PropertyDescriptor("pool-resize-quantity",
                        ConnectorConnectionPool.class, "getPoolResizeQuantity", "setPoolResizeQuantity");
                propertyDescritpors[8] = new PropertyDescriptor("idle-timeout-in-seconds",
                        ConnectorConnectionPool.class, "getIdleTimeoutInSeconds", "setIdleTimeoutInSeconds");
                propertyDescritpors[9] = new PropertyDescriptor("fail-all-connections",
                        ConnectorConnectionPool.class, "getFailAllConnections", "setFailAllConnections");
                propertyDescritpors[9].setPropertyEditorClass(BooleanPropEditor.class);
                propertyDescritpors[10] = new PropertyDescriptor("transaction-support",
                        ConnectorConnectionPool.class, "getTransactionSupport", "setTransactionSupport");
                propertyDescritpors[10].setPropertyEditorClass(TxSupportPropEditor.class);
                propertyDescritpors[11] = new PropertyDescriptor("is-connection-validation-required",
                        ConnectorConnectionPool.class, "getIsConnectionValidationRequired", "setIsConnectionValidationRequired");
                propertyDescritpors[11].setPropertyEditorClass(BooleanPropEditor.class);
            } catch (IntrospectionException e) {
                e.printStackTrace();
            }
            return propertyDescritpors;
        }
    }
}

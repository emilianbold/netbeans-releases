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

import org.netbeans.modules.compapp.javaee.sunresources.generated.sunresources13.AdminObjectResource;
import org.netbeans.modules.compapp.javaee.sunresources.generated.sunresources13.ConnectorConnectionPool;
import org.netbeans.modules.compapp.javaee.sunresources.generated.sunresources13.ConnectorResource;
import org.netbeans.modules.compapp.javaee.sunresources.generated.sunresources13.CustomResource;
import org.netbeans.modules.compapp.javaee.sunresources.generated.sunresources13.ExternalJndiResource;
import org.netbeans.modules.compapp.javaee.sunresources.generated.sunresources13.JdbcConnectionPool;
import org.netbeans.modules.compapp.javaee.sunresources.generated.sunresources13.JdbcResource;
import org.netbeans.modules.compapp.javaee.sunresources.generated.sunresources13.MailResource;
import org.netbeans.modules.compapp.javaee.sunresources.generated.sunresources13.PersistenceManagerFactoryResource;
import org.netbeans.modules.compapp.javaee.sunresources.generated.sunresources13.ResourceAdapterConfig;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 * @author echou
 */
public class BeanNodeFactory {
    
    public static Node getBeanNode(Object bean, FileObject fo, 
            ResourcesTableModel tableModel) throws Exception {
        if (bean instanceof AdminObjectResource) {
            return new AdminObjectResourceBeanNode((AdminObjectResource) bean, fo);
        } else if (bean instanceof ConnectorResource) {
            ConnectorResource connectorResource = (ConnectorResource) bean;
            ConnectorConnectionPool pool = tableModel.getConnectorConnectionPool(connectorResource.getPoolName());
            if (pool == null) {
                pool = tableModel.createDefaultConnectorConnectionPool(connectorResource.getPoolName());
            }
            return new ConnectorResourceBeanNode(connectorResource, pool, fo);
        } else if (bean instanceof CustomResource) {
            throw new Exception("Unsupported bean type yet: " + bean);
        } else if (bean instanceof ExternalJndiResource) {
            throw new Exception("Unsupported bean type yet: " + bean);
        } else if (bean instanceof JdbcResource) {
            JdbcResource jdbcResource = (JdbcResource) bean;
            JdbcConnectionPool pool = tableModel.getJdbcConnectionPool(jdbcResource.getPoolName());
            if (pool == null) {
                pool = tableModel.createDefaultJdbcConnectionPool(jdbcResource.getPoolName());
            }
            return new JdbcResourceBeanNode(jdbcResource, pool, fo);
        } else if (bean instanceof MailResource) {
            throw new Exception("Unsupported bean type yet: " + bean);
        } else if (bean instanceof PersistenceManagerFactoryResource) {
            throw new Exception("Unsupported bean type yet: " + bean);
        } else if (bean instanceof ResourceAdapterConfig) {
            throw new Exception("Unsupported bean type yet: " + bean);
        } else {
            throw new Exception("Unknown bean type " + bean);
        }
    }
    
    
    public static String generateResourceFileName(Object bean) throws Exception {
        if (bean instanceof AdminObjectResource) {
            AdminObjectResource adminObjectResource = (AdminObjectResource) bean;
            String jndiName = replaceString(adminObjectResource.getJndiName());
            return "adminobj_" + jndiName; // NoI18N
        } else if (bean instanceof ConnectorConnectionPool) {
            ConnectorConnectionPool connectorConnectionPool = (ConnectorConnectionPool) bean;
            String name = replaceString(connectorConnectionPool.getName());
            return "connconnpool_" + name; // NoI18N
        } else if (bean instanceof ConnectorResource) {
            ConnectorResource connectorResource = (ConnectorResource) bean;
            String jndiName = replaceString(connectorResource.getJndiName());
            return "connres_" + jndiName; // NoI18N
        } else if (bean instanceof CustomResource) {
            throw new Exception("Unsupported bean type yet: " + bean);
        } else if (bean instanceof ExternalJndiResource) {
            throw new Exception("Unsupported bean type yet: " + bean);
        } else if (bean instanceof JdbcConnectionPool) {
            JdbcConnectionPool jdbcConnectionPool = (JdbcConnectionPool) bean;
            String name = replaceString(jdbcConnectionPool.getName());
            return "jdbcconnpool_" + name; // NOI18N
        } else if (bean instanceof JdbcResource) {
            JdbcResource jdbcResource = (JdbcResource) bean;
            String name = replaceString(jdbcResource.getJndiName());
            return "jdbcres_" + name; // NOI18N
        } else if (bean instanceof MailResource) {
            throw new Exception("Unsupported bean type yet: " + bean);
        } else if (bean instanceof PersistenceManagerFactoryResource) {
            throw new Exception("Unsupported bean type yet: " + bean);
        } else if (bean instanceof ResourceAdapterConfig) {
            throw new Exception("Unsupported bean type yet: " + bean);
        } else {
            throw new Exception("Unknown bean type " + bean);
        }
    }
    
    private static String replaceString(String s) {
        return s.replace('/', '_').replace(' ', '_');
    }
}

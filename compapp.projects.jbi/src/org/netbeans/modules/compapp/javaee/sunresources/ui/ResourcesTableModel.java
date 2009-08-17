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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.table.AbstractTableModel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.compapp.javaee.sunresources.ResourceAggregator;
import org.netbeans.modules.compapp.javaee.sunresources.ResourceAggregator.OrphanStatus;

import org.netbeans.modules.compapp.javaee.sunresources.generated.sunresources13.*;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * @author echou
 *
 */
@SuppressWarnings("serial")
public class ResourcesTableModel extends AbstractTableModel {

    private static String STR_YES = 
            NbBundle.getMessage(ResourcesTableModel.class, "LBL_yes");
    private static String STR_NO = 
            NbBundle.getMessage(ResourcesTableModel.class, "LBL_no");
    
    private String[] columnNames = new String[] {
        NbBundle.getMessage(ResourcesTableModel.class, "LBL_col_resname"), 
        NbBundle.getMessage(ResourcesTableModel.class, "LBL_col_restype"),
        NbBundle.getMessage(ResourcesTableModel.class, "LBL_col_anno_present"),
        NbBundle.getMessage(ResourcesTableModel.class, "LBL_col_defined")
    };
    
    private ResourceAggregator resAggregator;
    
    public ResourcesTableModel(ResourceAggregator resAggregator) {
        this.resAggregator = resAggregator;    
    }
    
    public int getRowCount() {
        return resAggregator.getResources().size();
    }

    public int getColumnCount() {
        return columnNames.length;
    }
    
    public String getColumnName(int column) {
        return columnNames[column];
    }

    public Object getValueAt(int row, int column) {
        ResourceAggregator.ResourceEntry resourceEntry = resAggregator.getResources().get(row);
        switch (column) {
            case 0:
                return getResourceName(resourceEntry.resource);
            case 1:
                return getResourceType(resourceEntry.resource);
            case 2:
                if (resourceEntry.orphanStatus == OrphanStatus.ANNO_ONLY ||
                        resourceEntry.orphanStatus == OrphanStatus.BOTH) {
                    return STR_YES;
                } else {
                    return STR_NO;
                }
            case 3:
                if (resourceEntry.orphanStatus == OrphanStatus.FILE_ONLY ||
                        resourceEntry.orphanStatus == OrphanStatus.BOTH) {
                    return STR_YES;
                } else {
                    return STR_NO;
                }
            default:
                return null;
        }
    }
    
    public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
    }
    
    private Object getResourceProps(Object obj) {
        Properties props = new Properties();
        return props;
    }
    
    private void setResourceProps(Object value, Object obj) {
        
    }

    private String getResourceType(Object obj) {
        return obj.getClass().getSimpleName();
    }

    private String getResourceName(Object obj) {
        if (obj instanceof CustomResource) {
            CustomResource customResource = (CustomResource) obj;
            return customResource.getJndiName();
        } else if (obj instanceof ExternalJndiResource) {
            ExternalJndiResource externalJndiResource = (ExternalJndiResource) obj;
            return externalJndiResource.getJndiName();
        } else if (obj instanceof JdbcResource) {
            JdbcResource jdbcResource = (JdbcResource) obj;
            return jdbcResource.getJndiName();
        } else if (obj instanceof MailResource) {
            MailResource mailResource = (MailResource) obj;
            return mailResource.getJndiName();
        } else if (obj instanceof PersistenceManagerFactoryResource) {
            PersistenceManagerFactoryResource pmfResource = (PersistenceManagerFactoryResource) obj;
            return pmfResource.getJndiName();
        } else if (obj instanceof AdminObjectResource) {
            AdminObjectResource adminObjectResource = (AdminObjectResource) obj;
            return adminObjectResource.getJndiName();
        } else if (obj instanceof ConnectorResource) {
            ConnectorResource connectorResource = (ConnectorResource) obj;
            return connectorResource.getJndiName();
        } else if (obj instanceof ResourceAdapterConfig) {
            ResourceAdapterConfig resourceAdapterConfig = (ResourceAdapterConfig) obj;
            return resourceAdapterConfig.getName();
        } else if (obj instanceof JdbcConnectionPool) {
            JdbcConnectionPool jdbcConnectionPool = (JdbcConnectionPool) obj;
            return jdbcConnectionPool.getName();
        } else if (obj instanceof ConnectorConnectionPool) {
            ConnectorConnectionPool connectorConnectionPool = (ConnectorConnectionPool) obj;
            return connectorConnectionPool.getName();
        } else {
            return null;
        }
    }
    
    private void setResourceName(String value, Object obj) {
        if (obj instanceof CustomResource) {
            CustomResource customResource = (CustomResource) obj;
            customResource.setJndiName(value);
        } else if (obj instanceof ExternalJndiResource) {
            ExternalJndiResource externalJndiResource = (ExternalJndiResource) obj;
            externalJndiResource.setJndiName(value);
        } else if (obj instanceof JdbcResource) {
            JdbcResource jdbcResource = (JdbcResource) obj;
            jdbcResource.setJndiName(value);
        } else if (obj instanceof MailResource) {
            MailResource mailResource = (MailResource) obj;
            mailResource.setJndiName(value);
        } else if (obj instanceof PersistenceManagerFactoryResource) {
            PersistenceManagerFactoryResource pmfResource = (PersistenceManagerFactoryResource) obj;
            pmfResource.setJndiName(value);
        } else if (obj instanceof AdminObjectResource) {
            AdminObjectResource adminObjectResource = (AdminObjectResource) obj;
            adminObjectResource.setJndiName(value);
        } else if (obj instanceof ConnectorResource) {
            ConnectorResource connectorResource = (ConnectorResource) obj;
            connectorResource.setJndiName(value);
        } else if (obj instanceof ResourceAdapterConfig) {
            ResourceAdapterConfig resourceAdapterConfig = (ResourceAdapterConfig) obj;
            resourceAdapterConfig.setName(value);
        } else if (obj instanceof JdbcConnectionPool) {
            JdbcConnectionPool jdbcConnectionPool = (JdbcConnectionPool) obj;
            jdbcConnectionPool.setName(value);
        } else if (obj instanceof ConnectorConnectionPool) {
            ConnectorConnectionPool connectorConnectionPool = (ConnectorConnectionPool) obj;
            connectorConnectionPool.setName(value);
        } else {
            
        }
    }

    public boolean isCellEditable(int row, int column) {
        return false;
    }

    public boolean isEditButton(int row) {
        ResourceAggregator.ResourceEntry resourceEntry = resAggregator.getResources().get(row);
        if (resourceEntry.orphanStatus == ResourceAggregator.OrphanStatus.ANNO_ONLY) {
            return false;
        }
        return true;
    }
    
    public boolean canDelete(int row) {
        ResourceAggregator.ResourceEntry resourceEntry = resAggregator.getResources().get(row);
        if (resourceEntry.orphanStatus == ResourceAggregator.OrphanStatus.ANNO_ONLY) {
            return false;
        }
        return true;
    }
    
    public void deleteResourceFile(int row) throws Exception {
        resAggregator.deleteResourceFile(row);
    }
    
    public void writeToFile(int row) throws Exception {
        resAggregator.writeToSunXML(row);
    }
    
    public void writeToFile(int row, File resourceFile) throws Exception {
        resAggregator.writeToSunXML(row, resourceFile);
    }
    
    public Project getProject() {
        return resAggregator.getProject();
    }
    
    public Object getBean(int row) {
        return resAggregator.getResources().get(row).resource;
    }
    
    public FileObject getBeanFileObject(int row) {
        return resAggregator.getResources().get(row).fileObject;
    }
    
    public List<ResourceAggregator.ResourceUsage> getBeanUsages(int row) {
        return resAggregator.getResources().get(row).usages;
    }
    
    public ConnectorConnectionPool getConnectorConnectionPool(String poolName) {
        ResourceAggregator.PoolEntry entry = 
                resAggregator.getConnectorConnectionPoolEntry(poolName);
        if (entry == null || !(entry.pool instanceof ConnectorConnectionPool)) {
            return null;
        }
        return (ConnectorConnectionPool) entry.pool;
    }
    
    public ConnectorConnectionPool createDefaultConnectorConnectionPool(String poolName) {
        ResourceAggregator.PoolEntry entry = resAggregator.createDefaultConnectorConnectionPool(poolName);
        return (ConnectorConnectionPool) entry.pool;
    }
    
    public JdbcConnectionPool getJdbcConnectionPool(String poolName) {
        ResourceAggregator.PoolEntry entry = 
                resAggregator.getJdbcConnectionPoolEntry(poolName);
        if (entry == null || !(entry.pool instanceof JdbcConnectionPool)) {
            return null;
        }
        return (JdbcConnectionPool) entry.pool;
    }
    
    public JdbcConnectionPool createDefaultJdbcConnectionPool(String poolName) {
        ResourceAggregator.PoolEntry entry = resAggregator.createDefaultJdbcConnectionPool(poolName);
        return (JdbcConnectionPool) entry.pool;
    }
}
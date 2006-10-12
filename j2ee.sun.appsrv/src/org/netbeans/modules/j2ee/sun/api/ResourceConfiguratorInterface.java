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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * ResourceConfigurationInterface.java
 *
 * Created on August 13, 2005, 8:38 AM
 */
package org.netbeans.modules.j2ee.sun.api;

import java.io.File;
import java.util.HashSet;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;

/**
 *
 * @author Nitya Doraisamy
 */
public interface ResourceConfiguratorInterface {

    public boolean isJMSResourceDefined(String jndiName, File dir);

    public void createJMSResource(String jndiName, String msgDstnType, String msgDstnName, String ejbName, File dir);

    public void createJDBCDataSourceFromRef(String refName, String databaseInfo, File dir);

    public String createJDBCDataSourceForCmp(String beanName, String databaseInfo, File dir);
    
    public Datasource createDataSource(String jndiName, String url, String username, String password, String driver, File dir) throws DatasourceAlreadyExistsException;
    
    public HashSet getServerDataSources();  
    
    public HashSet getResources(File dir);   
    
}

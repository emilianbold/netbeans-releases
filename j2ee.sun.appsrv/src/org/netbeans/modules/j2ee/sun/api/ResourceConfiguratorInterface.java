/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * ResourceConfigurationInterface.java
 *
 * Created on August 13, 2005, 8:38 AM
 */
package org.netbeans.modules.j2ee.sun.api;

import java.io.File;

/**
 *
 * @author Nitya Doraisamy
 */
public interface ResourceConfiguratorInterface {
    
    public boolean isJMSResourceDefined(String jndiName, File dir);
    
    public void createJMSResource(String jndiName, String msgDstnType, String msgDstnName, String ejbName, File dir);
    
    public void createJDBCDataSourceFromRef(String refName, String databaseInfo, File dir);
    
    public String createJDBCDataSourceForCmp(String beanName, String databaseInfo, File dir);
    
}

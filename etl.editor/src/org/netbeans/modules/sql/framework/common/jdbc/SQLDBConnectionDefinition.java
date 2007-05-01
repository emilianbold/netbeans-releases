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

package org.netbeans.modules.sql.framework.common.jdbc;

import java.util.Properties;

import org.netbeans.modules.model.database.DBConnectionDefinition;


/**
 * Extension of DBConnectionDefinition
 * 
 * @author $Author$
 * @version $Revision$
 */

public interface SQLDBConnectionDefinition extends DBConnectionDefinition, Cloneable , Comparable{
   
    public Properties getConnectionProperties();

    public String getJNDIPath();

    public String getOTDPathName();

    public void setConnectionURL(String url);

    public void setDBType(String dbType);

    public void setDriverClass(String className);

    public void setJNDIPath(String path);

    public void setName(String name);

    public void setOTDPathName(String path);

    public void setPassword(String password);

    public void setUserName(String userName);

    public String toXMLString();
    
    public Object cloneObject();
}

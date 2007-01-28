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
/*
 * ConnectionModelEvent.java
 *
 * Created on Feb 9, 2004, 11:18 AM
 */

package org.netbeans.modules.visualweb.dataconnectivity.model;

/**
 * Used to fire information about the DataSourceInfo modification in the DataSourceInfo manager
 * @author  Winston Prakash
 */
public class DataSourceInfoEvent {
    String dataSourceId;

    public DataSourceInfoEvent(String id) {
        dataSourceId = id;
    }

    public void setDataSourceInfoId(String id){
        dataSourceId = id;
    }

    public String getDataSourceInfoId(){
        return dataSourceId;
    }
}

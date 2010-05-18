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
 * 
 * Copyright 2005 Sun Microsystems, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.netbeans.modules.wsdlextensions.jdbc.builder.dbmodel;

/**
 * Interface for defining specifications for a connection to a data source compatible with the
 * DBQueryModel API.
 * 
 * @author
 */
public interface DBConnectionDefinition extends Cloneable {

    /**
     * Get name defined for this DBConnectionDefinition.
     * 
     * @return DBConnectionDefnition name
     */
    public String getName();

    /**
     * Gets user-defined description, if any, for this DBConnectionDefinition.
     * 
     * @return user-defined description, or null if none was defined
     */
    public String getDescription();

    /**
     * Gets descriptive name, if any, of type of DB data source from which this metadata content was
     * derived, e.g., "Oracle9" for an Oracle 9i database, etc. Returns null if content was derived
     * from a non-DB source, such such as a flatfile.
     * 
     * @return vendor name of source database; null if derived from non-DB source
     */
    public String getDBType();

    /**
     * Gets URL used to reference and establish a connection to the data source referenced in this
     * object.
     * 
     * @return URL pointing to the data source
     */
    public String getConnectionURL();

    /**
     * @return
     */
    public String getDriverClass();

    /**
     * Gets username, if any, used in authenticating a connection to the data source referenced in
     * this object.
     * 
     * @return username, if any, used for authentication purposes
     */
    public String getUserName();

    /**
     * Gets password, if any, used in authenticating a connection to the data source referenced in
     * this object.
     * 
     * @return password, if any, used for authentication purposes
     */
    public String getPassword();
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.sql.framework.model;

import java.util.Properties;

/**
 * Interface for defining specifications for a connection to a data source compatible
 * with the DatabaseModel API.
 * 
 * @author Jonathan Giron, Sudhendra Seshachala
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
     * Gets fully-qualified class name of driver used to establish a connection
     * to the data source referenced in this object
     *
     * @return fully-qualified driver class name
     */
    public String getDriverClass();

    /**
     * Gets descriptive name, if any, of type of DB data source from which this 
     * metadata content was derived, e.g., "Oracle9" for an Oracle 9i database, etc.
     * Returns null if content was derived from a non-DB source, such 
     * such as a flatfile.
     *
     * @return vendor name of source database; null if derived from non-DB source
     */
    public String getDBType();

    /**
     * Gets URL used to reference and establish a connection to the data
     * source referenced in this object.
     *
     * @return URL pointing to the data source
     */
    public String getConnectionURL();
    
    /**
     * Gets username, if any, used in authenticating a connection to the 
     * data source referenced in this object.
     *
     * @return username, if any, used for authentication purposes
     */
    public String getUserName();
    
    /**
     * Gets password, if any, used in authenticating a connection to the 
     * data source referenced in this object.
     *
     * @return password, if any, used for authentication purposes
     */
    public String getPassword();
    
    Properties getConnectionProperties();
}


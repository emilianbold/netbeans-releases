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
package org.netbeans.modules.sql.framework.model;

import java.util.Map;
import java.util.Properties;

/**
 * Interface to determine overwritten database catalog and
 * schema names
 *
 * @author  Sherry Weng
 * @version $Revision$
 */

  public interface DBRuntimeEnvAccessor {
 
    /** Returns a flag indicating if the user wants to overwrite the catalog name
      *
      * @return flag indicating if the user wants to overwrite the catalog name
      */
     public boolean isCatalogNameOverwritten();

    /** Gets the map of catalog names to be replaced with
      * In the map, key will have the old name, and value will 
      * be the new name
      *
      * @return the Map of overwritten catalog names
      */
     public Map getOverwrittenCatalogNameMap();
    
    /** Returns a flag indicating if the user wants to overwrite schema names
      *
      * @return flag indicating if the user wants to overwrite the schema name
      */
     public boolean isSchemaNameOverwritten();
    
    /** Gets the map of schema names to be replaced with
      * In the map, key will have the old name, and value will 
      * be the new name
      *
      * @return the Map of overwritten schema names
      */
     public Map getOverwrittenSchemaNameMap(); 
     
    /** Returns the driver class name for Driver Manager
     *
     * @return the driver class name
     */
     public String getDriverClassName();
     
    /** Returns the user ID
      *
      * @return the user ID
      */
     public String getUserId();
     
    /** Returns the password
      *
      * @return the password
      */
     public String getPassword();
     
    /** Returns a well formed database URL
      *
      * @return the well formed database URL
      */
     public String getUrl();
    
    /** Returns additional database driver properties
      *
      * @return the additional driver properties if any
      */
     public Properties getDriverProperties();
    
}    



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

package org.netbeans.modules.jdbcwizard.builder.dbmodel.impl;

public class DatabaseObjectFactory {

    private static DBConnectionDefinitionImpl def;

    public DatabaseObjectFactory() {
    }

    public static DBConnectionDefinitionImpl createDBConnectionDefinition(final String name,
                                                                          final String driverClass,
                                                                          final String url,
                                                                          final String user,
                                                                          final String password,
                                                                          final String description,
                                                                          final String dbtype) throws Exception {
        DatabaseObjectFactory.def = new DBConnectionDefinitionImpl(name, driverClass, url, user, password, description, dbtype);
        return DatabaseObjectFactory.def;
    }

}

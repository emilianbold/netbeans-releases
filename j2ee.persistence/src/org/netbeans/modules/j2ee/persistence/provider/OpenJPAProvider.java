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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.persistence.provider;

import java.util.Collections;
import java.util.Map;
import org.openide.util.NbBundle;

/**
 * 
 * @author Erno Mononen
 */
class OpenJPAProvider extends Provider{

    public OpenJPAProvider() {
        super("org.apache.openjpa.persistence.PersistenceProviderImpl"); //NO18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(KodoProvider.class, "LBL_OpenJPA"); //NOI18N
    }
    
    public String getJdbcUrl() {
        return "openjpa.ConnectionURL";//NOI18N
    }
    
    public String getJdbcDriver() {
        return "openjpa.ConnectionDriverName";//NOI18N
    }
    
    public String getJdbcUsername() {
        return "openjpa.ConnectionUserName";//NOI18N
    }
    
    public String getJdbcPassword() {
        return "openjpa.ConnectionPassword";//NOI18N
    }

    public String getTableGenerationPropertyName() {
        return "openjpa.jdbc.SynchronizeMappings";//NOI18N
    }

    public String getTableGenerationDropCreateValue() {
        return "buildSchema(SchemaAction='add,deleteTableContents',ForeignKeys=true)";//NOI18N
    }

    public String getTableGenerationCreateValue() {
        return "buildSchema(ForeignKeys=true)";//NOI18N
    }

    public Map getUnresolvedVendorSpecificProperties() {
        return Collections.EMPTY_MAP;
    }

    public Map getDefaultVendorSpecificProperties() {
        return Collections.EMPTY_MAP;
    }

}

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

package org.netbeans.modules.j2ee.persistence.provider;

import java.util.Collections;
import java.util.Map;
import org.openide.util.NbBundle;

/**
 * This class represents Hibernate provider.
 *
 * @author Erno Mononen
 */
class HibernateProvider extends Provider{
    
    protected HibernateProvider(){
        super("org.hibernate.ejb.HibernatePersistence");
    }

    public String getDisplayName() {
        return NbBundle.getMessage(HibernateProvider.class, "LBL_Hibernate"); //NOI18N
    }
    
    public String getJdbcUrl() {
        return "hibernate.connection.url";
    }
    
    public String getJdbcDriver() {
        return "hibernate.connection.driver_class";
    }
    
    public String getJdbcUsername() {
        return "hibernate.connection.username";
    }
    
    public String getJdbcPassword() {
        return "hibernate.connection.password";
    }
    
    public String getTableGenerationPropertyName() {
        return "hibernate.hbm2ddl.auto";
    }
    
    public String getTableGenerationDropCreateValue() {
        return "create-drop";
    }
    
    public String getTableGenerationCreateValue() {
        return "update";
    }

    public Map getUnresolvedVendorSpecificProperties() {
        return Collections.EMPTY_MAP;
    }
    
    public Map getDefaultVendorSpecificProperties() {
        return Collections.singletonMap(
                "hibernate.cache.provider_class",
                "org.hibernate.cache.NoCacheProvider");
    }
}

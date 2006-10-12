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
 * This class represents Toplink provider.
 *
 * @author Erno Mononen
 */
class ToplinkProvider extends Provider{
    
    protected ToplinkProvider(){
        super("oracle.toplink.essentials.ejb.cmp3.EntityManagerFactoryProvider");
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(ToplinkProvider.class, "LBL_TopLink"); //NOI18N
    }
    
    public String getJdbcUrl() {
        return "toplink.jdbc.url";
    }

    public String getJdbcDriver() {
        return "toplink.jdbc.driver";
    }

    public String getJdbcUsername() {
        return "toplink.jdbc.user";
    }

    public String getJdbcPassword() {
        return "toplink.jdbc.password";
    }

    public String getTableGenerationPropertyName() {
        return "toplink.ddl-generation";
    }

    public String getTableGenerationDropCreateValue() {
        return "drop-and-create-tables";
    }

    public String getTableGenerationCreateValue() {
        return "create-tables";
    }

    public Map getUnresolvedVendorSpecificProperties() {
        return Collections.EMPTY_MAP;
    }

    public Map getDefaultVendorSpecificProperties() {
        return Collections.EMPTY_MAP;
    }
    
    
}

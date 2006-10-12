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
 * This class represents Kodo provider.
 *
 * @author Erno Mononen
 */
class KodoProvider extends Provider{
    
    protected KodoProvider(){
        super("kodo.persistence.PersistenceProviderImpl");
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(KodoProvider.class, "LBL_Kodo"); //NOI18N
    }
    
    public String getJdbcUrl() {
        return "kodo.ConnectionURL";
    }
    
    public String getJdbcDriver() {
        return "kodo.ConnectionDriverName";
    }
    
    public String getJdbcUsername() {
        return "kodo.ConnectionUserName";
    }
    
    public String getJdbcPassword() {
        return "kodo.ConnectionPassword";
    }

    public String getTableGenerationPropertyName() {
        return "";
    }

    public String getTableGenerationDropCreateValue() {
        return "";
    }

    public String getTableGenerationCreateValue() {
        return "";
    }

    public Map getUnresolvedVendorSpecificProperties() {
        return Collections.EMPTY_MAP;
    }

    public Map getDefaultVendorSpecificProperties() {
        return Collections.EMPTY_MAP;
    }
    
}

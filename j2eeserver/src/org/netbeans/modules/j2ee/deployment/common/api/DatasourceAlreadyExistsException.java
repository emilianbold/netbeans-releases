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

package org.netbeans.modules.j2ee.deployment.common.api;

import java.util.LinkedList;
import java.util.List;
import org.openide.util.NbBundle;

/**
 *
 * Indicates conflict between the data sources being deployed/saved and the existing ones.
 *
 * @author Libor Kotouc
 *
 * @since 1.15
 */
public final class DatasourceAlreadyExistsException extends Exception {
    
    private List<Datasource> datasources;
    
    /**
     * Creates new DatasourceAlreadyExistsException with the list of conflicting data sources
     *
     * @param datasources the list of conflicting data sources
     *
     * @exception NullPointerException if the <code>datasources</code> argument is <code>null</code>
     */
    public DatasourceAlreadyExistsException(List<Datasource> datasources) {
        if (datasources == null) {
            throw new NullPointerException(NbBundle.getMessage(getClass(), "ERR_CannotPassNullDatasources")); // NOI18N
        }
        this.datasources = datasources;
    }
    
    /**
     * Creates new DatasourceAlreadyExistsException with the conflicting data source
     *
     * @param datasource the conflicting data source
     */
    public DatasourceAlreadyExistsException(Datasource datasource) {
        datasources = new LinkedList<Datasource>();
        datasources.add(datasource);
    }
    
    /**
     * Returns list of conflicting data sources
     *
     * @return list of conflicting data sources
     */
    public List<Datasource> getDatasources() {
        return datasources;
    }
    
}

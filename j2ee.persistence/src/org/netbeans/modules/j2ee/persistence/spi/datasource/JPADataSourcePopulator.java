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

package org.netbeans.modules.j2ee.persistence.spi.datasource;

import javax.swing.JComboBox;

/**
 * This interface provides support for populating a combo box that
 * represents available data sources. Itshould be implemented by projects 
 * where it is possible to use data sources.  
 * 
 * @author Erno Mononen
 */
public interface JPADataSourcePopulator {

    /**
     * Populates the given <code>comboBox</code> with <code>JPADataSource</code>s
     * and with items for managing data sources. The items representing the actual
     * data sources must be instances of <code>JPDDataSource</code>. 
     * @param comboBox the combo box to be populated.
     */ 
    void connect(JComboBox comboBox);
    
}

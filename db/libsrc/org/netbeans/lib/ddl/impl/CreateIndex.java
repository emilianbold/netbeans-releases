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

package org.netbeans.lib.ddl.impl;

import java.util.*;
import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.impl.*;

/**
* Interface of database action command. Instances should remember connection
* information of DatabaseSpecification and use it in execute() method. This is a base interface
* used heavily for sub-interfacing (it is not subclassing :)
*/

public class CreateIndex extends ColumnListCommand {
    /** Index name */
    private String indexname;

    /** Index type */
    private String unique;

    static final long serialVersionUID =1899024699690380782L;    
    public String getIndexName()
    {
        return indexname;
    }

    public void setIndexName(String iname)
    {
        indexname = iname;
    }

    public String getIndexType()
    {
        return unique;
    }

    public void setIndexType(String idx_type)
    {
        unique = idx_type;
    }

    public TableColumn specifyColumn(String name)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        return specifyColumn(TableColumn.COLUMN, name, 
            Specification.CREATE_INDEX, false, false);
    }
    
    public TableColumn specifyNewColumn(String name)
            throws ClassNotFoundException, IllegalAccessException, 
            InstantiationException {
        return specifyColumn(TableColumn.COLUMN, name, 
                Specification.CREATE_INDEX, false, true);
    }

    public Map getCommandProperties() throws DDLException {
        Map args = super.getCommandProperties();
        args.put("index.name", indexname); // NOI18N
        args.put("index.unique", unique); // NOI18N
        
        return args;
    }
}

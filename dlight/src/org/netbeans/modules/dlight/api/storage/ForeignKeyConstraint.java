/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.api.storage;

import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;

/**
 *
 * @author masha
 */
public final class ForeignKeyConstraint {
    private final DataTableMetadata primaryTable;
    private  final DataTableMetadata referenceTable;
    private  final Column column;
    private final Column referenceColumn;
    

    ForeignKeyConstraint(DataTableMetadata table, Column column, 
            DataTableMetadata referenceTable, Column referenceColumn) {
        this.primaryTable = table;
        this.column = column;
        this.referenceTable = referenceTable;
        this.referenceColumn = referenceColumn;
    }

    public DataTableMetadata getTable(){
        return primaryTable;
    }

    public Column getColumn(){
        return column;
    }

    public Column getReferenceColumn(){
        return referenceColumn;
    }

    public DataTableMetadata getReferenceTable(){
        return referenceTable;
    }

}

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.ddl.impl;

import java.util.*;
import java.sql.*;
import java.text.ParseException;
import org.netbeans.lib.ddl.*;
import java.io.Serializable;

/**
* Implementation of table column.
*
* @author Slavek Psenicka
*/
public class TableColumn extends AbstractTableColumn
            implements Serializable, TableColumnDescriptor, CheckConstraintDescriptor
{
    /** String constant for column type */
    public static final String COLUMN = "COLUMN"; // NOI18N
    /** String constant for column check */
    public static final String CHECK = "CHECK"; // NOI18N
    /** String constant for unique column type */
    public static final String UNIQUE = "UNIQUE"; // NOI18N
    /** String constant for primary key */
    public static final String PRIMARY_KEY = "PRIMARY_KEY"; // NOI18N
    /** String constant for foreign key */
    public static final String FOREIGN_KEY = "FOREIGN_KEY"; // NOI18N
    /** String constant for check constraint */
    public static final String CHECK_CONSTRAINT = "CHECK_CONSTRAINT"; // NOI18N
    /** String constant for unique constraint */
    public static final String UNIQUE_CONSTRAINT = "UNIQUE_CONSTRAINT"; // NOI18N
    /** String constant for primary key constraint */
    public static final String PRIMARY_KEY_CONSTRAINT = "PRIMARY_KEY_CONSTRAINT"; // NOI18N
    /** String constant for foreign key constraint */
    public static final String FOREIGN_KEY_CONSTRAINT = "FOREIGN_KEY_CONSTRAINT"; // NOI18N

    /** Column type */
    int type;

    /** Column size */
    int size;

    /** Column decimal size */
    int decsize;

    /** Null allowed */
    boolean nullable;

    /** Default value */
    String defval;

    /** Check expression */
    String checke;

    static final long serialVersionUID =4298150043758715392L;
    /** Constructor */
    public TableColumn()
    {
        size = 0;
        decsize = 0;
        nullable = true;
    }

    /** Returns type of column */
    public int getColumnType()
    {
        return type;
    }

    /** Sets type of column */
    public void setColumnType(int columnType)
    {
        type = columnType;
    }

    /** Returns column size */
    public int getColumnSize()
    {
        return size;
    }

    /** Sets size of column */
    public void setColumnSize(int csize)
    {
        size = csize;
    }

    /** Returns decimal digits of column */
    public int getDecimalSize()
    {
        return decsize;
    }

    /** Sets decimal digits of column */
    public void setDecimalSize(int dsize)
    {
        decsize = dsize;
    }

    /** Nulls allowed? */
    public boolean isNullAllowed()
    {
        return nullable;
    }

    /** Sets null property */
    public void setNullAllowed(boolean flag)
    {
        nullable = flag;
    }

    /** Returns default value of column */
    public String getDefaultValue()
    {
        return defval;
    }

    /** Sets default value of column */
    public void setDefaultValue(String val)
    {
        defval = val;
    }

    /** Returns column check condition */
    public String getCheckCondition()
    {
        return checke;
    }

    /** Sets column check condition */
    public void setCheckCondition(String val)
    {
        checke = val;
    }

    /**
    * Returns properties and it's values supported by this object.
    * object.name		Name of the object; use setObjectName() 
    * object.owner		Name of the object; use setObjectOwner() 
    * column.size		Size of column 
    * column.decsize	Deimal size of size 
    * column.type		Type of column 
    * default.value		Condition of column 
    * Throws DDLException if object name is not specified.
    */
    public Map getColumnProperties(AbstractCommand cmd)
    throws DDLException
    {
        DatabaseSpecification spec = cmd.getSpecification();
        Map args = super.getColumnProperties(cmd);
        String stype = spec.getType(type);
        Vector decimaltypes = (Vector)spec.getProperties().get("DecimalTypes"); // NOI18N
        Vector charactertypes = (Vector)spec.getProperties().get("CharacterTypes"); // NOI18N
        String strdelim = (String)spec.getProperties().get("StringDelimiter"); // NOI18N
        Vector sizelesstypes = (Vector)spec.getProperties().get("SizelessTypes"); // NOI18N

        // Decimal size for sizeless type
        if (sizelesstypes != null && size > 0) {
            if (!sizelesstypes.contains(stype)) {
                if (size > 0) args.put("column.size", String.valueOf(size)); // NOI18N
                if (decsize > 0) args.put("column.decsize", String.valueOf(decsize)); // NOI18N
            }
        }

        String qdefval = defval;
        if (charactertypes.contains(spec.getType(type))) qdefval = strdelim+defval+strdelim;
        args.put("column.type", spec.getType(type)); // NOI18N
        if (!nullable) args.put("column.notnull", ""); // NOI18N
        if (defval != null) args.put("default.value", qdefval); // NOI18N
        if (checke != null) args.put("check.condition", checke); // NOI18N
        return args;
    }

    /** Reads object from stream */
    public void readObject(java.io.ObjectInputStream in)
    throws java.io.IOException, ClassNotFoundException
    {
        super.readObject(in);
        type = in.readInt();
        size = in.readInt();
        decsize = in.readInt();
        nullable = in.readBoolean();
        defval = (String)in.readObject();
        checke = (String)in.readObject();
    }

    /** Writes object to stream */
    public void writeObject(java.io.ObjectOutputStream out)
    throws java.io.IOException
    {
        super.writeObject(out);
        out.writeInt(type);
        out.writeInt(size);
        out.writeInt(decsize);
        out.writeBoolean(nullable);
        out.writeObject(defval);
        out.writeObject(checke);
    }
}

/*
* <<Log>>
*  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  5    Gandalf   1.4         9/10/99  Slavek Psenicka 
*  4    Gandalf   1.3         8/17/99  Ian Formanek    Generated serial version 
*       UID
*  3    Gandalf   1.2         5/14/99  Slavek Psenicka new version
*  2    Gandalf   1.1         4/23/99  Slavek Psenicka new version
*  1    Gandalf   1.0         4/6/99   Slavek Psenicka 
* $
*/

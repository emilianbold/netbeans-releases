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

package org.netbeans.modules.db.explorer.infos;

import java.io.InputStream;
import java.io.IOException;
import java.util.*;
import java.sql.*;
import org.netbeans.lib.ddl.*;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import org.openide.nodes.Node;
import org.netbeans.lib.ddl.util.PListReader;
import org.netbeans.lib.ddl.impl.*;
import org.netbeans.modules.db.*;
import org.netbeans.modules.db.explorer.*;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;
import org.netbeans.modules.db.explorer.actions.DatabaseAction;
import org.netbeans.modules.db.explorer.DatabaseDriver;
import org.netbeans.modules.db.explorer.nodes.RootNode;

public class DriverNodeInfo extends DriverListNodeInfo
{
    static final long serialVersionUID =6994829681095273161L;
    public DatabaseDriver getDatabaseDriver()
    {
        return (DatabaseDriver)get(DatabaseNodeInfo.DBDRIVER);
    }

    public void setDatabaseDriver(DatabaseDriver drv)
    {
        put(DatabaseNodeInfo.NAME, drv.getName());
        put(DatabaseNodeInfo.URL, drv.getURL());
        put(DatabaseNodeInfo.PREFIX, drv.getDatabasePrefix());
        put(DatabaseNodeInfo.ADAPTOR_CLASSNAME, drv.getDatabaseAdaptor());
        put(DatabaseNodeInfo.DBDRIVER, drv);
    }

    public void delete()
    throws IOException
    {
        try {
            DatabaseDriver drv = getDatabaseDriver();
            Vector drvs = RootNode.getOption().getAvailableDrivers();
            int idx = drvs.indexOf(drv);
            if (idx != -1) drvs.removeElementAt(idx);
            else throw new DatabaseException("driver "+drv+" was not found");
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
}
/*
 * <<Log>>
 *  8    Gandalf   1.7         11/27/99 Patrik Knakal   
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         9/8/99   Slavek Psenicka adaptor changes
 *  5    Gandalf   1.4         7/21/99  Slavek Psenicka prefix
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         5/21/99  Slavek Psenicka new version
 *  2    Gandalf   1.1         5/14/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         4/23/99  Slavek Psenicka 
 * $
 */

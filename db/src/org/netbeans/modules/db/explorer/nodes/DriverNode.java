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

package org.netbeans.modules.db.explorer.nodes;

import java.beans.*;
import org.openide.nodes.Children;
import org.netbeans.modules.db.*;
import org.netbeans.modules.db.explorer.*;
import org.netbeans.modules.db.explorer.infos.*;

public class DriverNode extends LeafNode implements PropertyChangeListener
{
    public void setInfo(DatabaseNodeInfo info)
    {
        super.setInfo(info);
        DatabaseDriver drv = (DatabaseDriver)info.get(DatabaseNodeInfo.DBDRIVER);
        if (drv != null) {
            info.put(DatabaseNodeInfo.NAME, drv.getName());
            info.put(DatabaseNodeInfo.URL, drv.getURL());
            info.put(DatabaseNodeInfo.ADAPTOR_CLASSNAME, drv.getDatabaseAdaptor());
            info.addDriverListener(this);
        }
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        DatabaseNodeInfo info = getInfo();
        String pname = evt.getPropertyName();
        Object newval = evt.getNewValue();
        DatabaseDriver drv = (DatabaseDriver)info.get(DatabaseNodeInfo.DBDRIVER);
        if (pname.equals(DatabaseNodeInfo.NAME)) drv.setName((String)newval);
        if (pname.equals(DatabaseNodeInfo.URL)) drv.setURL((String)newval);
        if (pname.equals(DatabaseNodeInfo.PREFIX)) drv.setDatabasePrefix((String)newval);
        if (pname.equals(DatabaseNodeInfo.ADAPTOR_CLASSNAME)) drv.setDatabaseAdaptor((String)newval);
    }
}

/*
 * <<Log>>
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         9/8/99   Slavek Psenicka 
 *  6    Gandalf   1.5         7/21/99  Slavek Psenicka prefix
 *  5    Gandalf   1.4         6/15/99  Slavek Psenicka debug prints
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         5/21/99  Slavek Psenicka new version
 *  2    Gandalf   1.1         5/14/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         4/23/99  Slavek Psenicka 
 * $
 */

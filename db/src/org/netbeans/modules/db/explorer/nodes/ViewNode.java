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

import java.util.*;
import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.impl.*;
import org.openide.*;
import org.openide.nodes.Children;
import org.netbeans.modules.db.*;
import org.netbeans.modules.db.explorer.*;
import org.netbeans.modules.db.explorer.infos.*;

// Node for Table/View/Procedure things.

public class ViewNode extends DatabaseNode
{
    public void setName(String newname)
    {
        try {
            DatabaseNodeInfo info = getInfo();
            Specification spec = (Specification)info.getSpecification();
            AbstractCommand cmd = spec.createCommandRenameView(info.getName(), newname);
            cmd.execute();
            super.setName(newname);
            info.put(DatabaseNode.TABLE, newname);
        } catch (CommandNotSupportedException e) {
            TopManager.getDefault().notify(new NotifyDescriptor.Message("Unable to change the name, command "+e.getCommand()+" is not supported by system", NotifyDescriptor.ERROR_MESSAGE));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
/*
 * <<Log>>
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         9/8/99   Slavek Psenicka 
 *  5    Gandalf   1.4         8/19/99  Slavek Psenicka English
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         5/21/99  Slavek Psenicka new version
 *  2    Gandalf   1.1         5/14/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         4/23/99  Slavek Psenicka 
 * $
 */

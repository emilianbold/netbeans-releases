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
import java.text.MessageFormat;

import org.openide.*;
import org.openide.nodes.Children;

import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.impl.*;
import org.netbeans.modules.db.*;
import org.netbeans.modules.db.explorer.*;
import org.netbeans.modules.db.explorer.infos.*;

// Node for Table/View/Procedure things.

public class ViewNode extends DatabaseNode {
    public void setName(String newname)
    {
        try {
            DatabaseNodeInfo info = getInfo();
            Specification spec = (Specification)info.getSpecification();
            AbstractCommand cmd = spec.createCommandRenameView(info.getName(), newname);
            cmd.execute();
            super.setName(newname);
            info.put(DatabaseNode.TABLE, newname);
        } catch (CommandNotSupportedException exc) {
            String message = MessageFormat.format(bundle.getString("EXC_UnableToChangeName"), new String[] {exc.getCommand()}); // NOI18N
            TopManager.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

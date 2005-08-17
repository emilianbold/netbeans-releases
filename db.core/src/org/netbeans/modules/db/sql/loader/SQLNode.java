/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.sql.loader;

import javax.swing.Action;
import org.openide.actions.OpenAction;
import org.openide.loaders.DataNode;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.text.DataEditorSupport;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Andrei Badea
 */
public class SQLNode extends DataNode {
    
    private final static String ICON_BASE = "org/netbeans/modules/db/sql/loader/resources/sql16.gif"; // NOI18N
    
    public SQLNode(SQLDataObject dataObject) {
        super(dataObject, Children.LEAF);
        setIconBaseWithExtension(ICON_BASE);
    }
    
    public Action getPreferredAction() {
        return SystemAction.get(OpenAction.class);
    }
}

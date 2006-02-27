/*
 *                Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.jellytools.modules.db.nodes;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.modules.db.actions.DisableDebugAction;
import org.netbeans.jellytools.modules.db.actions.EnableDebugAction;
import org.netbeans.jellytools.nodes.Node;

/** Node representing "Databases" node in Runtime tab.
 * <p>
 * Usage:<br>
 * <pre>
 *      DatabasesNode databases = DatabasesNode.invoke();
 *      databases.enableDebug();
 *      ....
 * </pre>
 *
 * @author Martin.Schovanek@sun.com
 */
public class DatabasesNode extends Node {
    static final String TREE_PATH = Bundle.getStringTrimmed(
                "org.netbeans.modules.db.resources.Bundle",
                "NDN_Databases");
    private static final Action enableDebugAction = new EnableDebugAction();
    private static final Action disableDebugAction = new DisableDebugAction();
    
    public DatabasesNode() {
        super(new RuntimeTabOperator().getRootNode(), TREE_PATH);
    }
    
    /** Finds "Databases" node in Runtime tab
     */
    public static DatabasesNode invoke() {
        RuntimeTabOperator.invoke();
        return new DatabasesNode();
    }
    
    /** performs EnableDebugAction with this node */
    public void enableDebug() {
        enableDebugAction.perform(this);
    }
    
    /** performs DisableDebugAction with this node */
    public void disableDebug() {
        disableDebugAction.perform(this);
    }
    
    /** tests popup menu items for presence */
    void verifyPopup() {
        verifyPopup(new Action[]{
            enableDebugAction,
            disableDebugAction,
        });
    }
}

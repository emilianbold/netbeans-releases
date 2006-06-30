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

package org.netbeans.jellytools.modules.db.nodes;

import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.CustomizeAction;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.modules.db.actions.ConnectUsingAction;
import org.netbeans.jellytools.nodes.Node;

/** Node representing "Databases > Drivers > ${driver}" node in Runtime tab.
 * <p>
 * Usage:<br>
 * <pre>
 *      DriverNode driver = DriversNode.invoke("Oracle");
 *      driver.connectUsing();
 *      ....
 *      driver.delete();
 * </pre>
 *
 * @author Martin.Schovanek@sun.com
 */
public class DriverNode extends Node {
    private static final Action connectUsingAction = new ConnectUsingAction();
    private static final Action deleteAction = new DeleteAction();
    private static final Action customizeAction = new CustomizeAction();

    /** creates new DriverNode
     * @param name DriverNode display name */
    public DriverNode(String name) {
        super(new RuntimeTabOperator().getRootNode(), DriversNode.TREE_PATH+
                "|"+name);
    }

    /** Finds "Databases > Drivers > ${driver}" node */
    public static DriverNode invoke(String name) {
        RuntimeTabOperator.invoke();
        return new DriverNode(name);
    }
    
    /** performs ConnectUsingAction with this node */
    public void connectUsing() {
        connectUsingAction.perform(this);
    }

    /** performs ConnectUsingAction with this node */
    public void customize() {
        customizeAction.perform(this);
    }

    /** performs DeleteAction with this node */
    public void delete() {
        deleteAction.perform(this);
    }

    /** tests popup menu items for presence */
    void verifyPopup() {
        verifyPopup(new Action[]{
            connectUsingAction,
            customizeAction,
            deleteAction,
        });
    }
}

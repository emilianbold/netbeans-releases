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

import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.modules.db.actions.AddDriverAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.RuntimeTabOperator;

/** Node representing "Databases > Drivers" node in Runtime tab.
 * <p>
 * Usage:<br>
 * <pre>
 *      DriversNode drivers = DriversNode.invoke();
 *      drivers.addDriver();
 *      ....
 * </pre>
 *
 * @author Martin.Schovanek@sun.com
 */
public class DriversNode extends Node {
    static final String TREE_PATH = DatabasesNode.TREE_PATH+"|"+
            Bundle.getStringTrimmed(
                "org.netbeans.modules.db.resources.Bundle",
                "NDN_Drivers");
    private static final Action addDriverAction = new AddDriverAction();

    /** Finds "Databases > Drivers" node */
    public static DriversNode invoke() {
        RuntimeTabOperator.invoke();
        return new DriversNode();
    }
    
    /** Creates new DriversNode */
    public DriversNode() {
        super(new RuntimeTabOperator().getRootNode(), TREE_PATH);
    }
    
    /** performs AddDriverAction with this node */
    public void addDriver() {
        addDriverAction.perform(this);
    }
    
    /** tests popup menu items for presence */
    void verifyPopup() {
        verifyPopup(new Action[]{
            addDriverAction,
        });
    }
}

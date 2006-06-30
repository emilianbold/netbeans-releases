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

package org.netbeans.modules.testtools.generator;

import org.openide.util.actions.NodeAction;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Node Generator action class
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 0.1
 */
public class NodeGeneratorAction extends NodeAction {

    private static final long serialVersionUID = 2491417043823675616L;

    /** method performing the action
     * @param nodes selected nodes
     */
    protected void performAction(Node[] nodes) {
        NodeGeneratorPanel.showDialog(nodes);
    }
    
    /** action is enabled for any selected node
     * @param node selected nodes
     * @return boolean true
     */    
    public boolean enable (Node[] node) {
        try {
            Class.forName("org.netbeans.jemmy.operators.ComponentOperator"); // NOI18N
            Class.forName("org.netbeans.jellytools.actions.Action");  // NOI18N
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    /** method returning name of the action
     * @return String name of the action
     */    
    public String getName() {
        return NbBundle.getMessage(NodeGeneratorAction.class, "ActionName");  // NOI18N
    }

    /** method returning icon for the action
     * @return String path to action icon
     */    
    protected String iconResource() {
       return "org/netbeans/modules/testtools/generator/NodeGeneratorAction.gif";  // NOI18N
    }
    
    /** method returning action Help Context
     * @return action Help Context
     */    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(NodeGeneratorAction.class);
    }
    
    /** Always return false - no need to run asynchronously. */
    protected boolean asynchronous() {
        return false;
    }
}


/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.uml.core.reverseengineering.parsers.parsertestbed;

import java.io.FileReader;

import javax.swing.tree.TreeNode;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IStateListener;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IStatePayload;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenProcessor;

/**
 */
public class StateListener implements IStateListener, ITokenProcessor
{
    private int stateCount, tokenCount;
    private TestbedUI ui;
    
    public StateListener(TestbedUI ui)
    {
        this.ui = ui;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IStateListener#onBeginState(java.lang.String, java.lang.String, org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IStatePayload)
     */
    public void onBeginState(String stateName, String language, IStatePayload payload)
    {
        String curName = "STATE_" + stateCount;
        ui.setLastNode(
                ui.addNode(ui.getLastNode(), curName, stateName));
        stateCount++;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IStateListener#onEndState(java.lang.String)
     */
    public void onEndState(String stateName)
    {
        TreeNode n = ui.getLastNode();
        if (n != null)
            ui.setLastNode(n.getParent());
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenProcessor#processToken(org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor, java.lang.String)
     */
    public void processToken(ITokenDescriptor pToken, String language)
    {
        if (pToken == null) return ;
        String curName = "TOKEN_" + tokenCount;
        if (ui.getLastNode() != null)
        {    
            TreeNode node = ui.addNode(ui.getLastNode(), curName, pToken.getType());
            ui.addToken(node, pToken, language);
        }
        tokenCount++;
    }
}
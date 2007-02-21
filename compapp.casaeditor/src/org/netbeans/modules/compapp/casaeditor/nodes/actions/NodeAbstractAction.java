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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.compapp.casaeditor.nodes.actions;

import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;
import org.openide.nodes.Node;

/**
 *
 * @author Josh Sandusky
 */
public abstract class NodeAbstractAction extends AbstractAction {
    
    private CasaNode mNode;
    
    
    public NodeAbstractAction(String name, CasaNode node) {
        super(name);
        mNode = node;
    }
    
    public NodeAbstractAction(CasaNode node) {
        super();
        mNode = node;
    }
    
    
    public void setName(String name) {
        putValue(Action.NAME, name);
    }
    
    protected Node getNode() {
        return mNode;
    }
    
    protected CasaWrapperModel getModel() {
        return mNode.getModel();
    }
    
    protected Object getData() {
        return mNode.getData();
    }
}

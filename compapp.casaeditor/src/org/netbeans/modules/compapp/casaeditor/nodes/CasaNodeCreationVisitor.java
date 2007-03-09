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

package org.netbeans.modules.compapp.casaeditor.nodes;

import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponentVisitor;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConnection;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConsumes;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPort;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaProvides;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceEngineServiceUnit;

/**
 *
 * @author jsandusky
 */
public class CasaNodeCreationVisitor extends CasaComponentVisitor.Default {
    
    private CasaNode mNode;
    private CasaNodeFactory mNodeFactory;
    
    
    public CasaNodeCreationVisitor(CasaNodeFactory factory) {
        mNodeFactory = factory;
    }
    
    
    public CasaNode getNode() {
        return mNode;
    }
    
    public void visit(CasaServiceEngineServiceUnit data) {
        mNode = new ServiceUnitNode(data, mNodeFactory);
    }

    public void visit(CasaConnection data) {
        mNode = new ConnectionNode(data, mNodeFactory);
    }

    public void visit(CasaConsumes data) {
        mNode = new ConsumesNode(data, mNodeFactory);
    }

    public void visit(CasaProvides data) {
        mNode = new ProvidesNode(data, mNodeFactory);
    }

    public void visit(CasaPort data) {
        mNode = new WSDLEndpointNode(data, mNodeFactory);
    }
    
}


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
/*
 * CompositorOnElementNewType.java
 *
 * Created on July 18, 2006, 3:34 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe.action;

import java.io.IOException;
import java.util.List;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.schema.abe.ABEBaseDropPanel;
import org.netbeans.modules.xml.schema.abe.InstanceUIContext;
import org.netbeans.modules.xml.schema.abe.StartTagPanel;
import org.netbeans.modules.xml.schema.abe.nodes.ABEAbstractNode;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.NewType;

/**
 *
 * @author girix
 */
public class CompositorOnElementNewType extends NewType{
    
    InstanceUIContext context;
    Compositor.CompositorType compType;
    /** Creates a new instance of ElementNewType */
    public CompositorOnElementNewType(InstanceUIContext context,
            Compositor.CompositorType compType) {
        super();
        this.context = context;
        this.compType = compType;
    }
    
    public void create(Compositor.CompositorType compType) throws IOException {
        List<ABEBaseDropPanel> list = context.getComponentSelectionManager().
                getSelectedComponentList();
        if(list.size() <= 0)
            return;
        StartTagPanel startTagPanel = (StartTagPanel) list.get(0);
        context.setUserInducedEventMode(true, startTagPanel);
        startTagPanel.addCompositor(compType);
        context.setUserInducedEventMode(false);
    }
    
    public void create() throws IOException {
        create(compType);
    }
    
    public String getName() {
        String comp = null;
        switch(compType){
            case SEQUENCE:
                comp = NbBundle.getMessage(ABEAbstractNode.class,"LBL_Sequence");
                break;
            case CHOICE:
                comp = NbBundle.getMessage(ABEAbstractNode.class,"LBL_Choice");
                break;
            case ALL:
                comp = NbBundle.getMessage(ABEAbstractNode.class,"LBL_All");
                break;
        }
        return comp;
    }
    
    
    
}

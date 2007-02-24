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
 * ADIncrementalLayoutCommand.java
 *
 * Created on February 25, 2005, 12:32 PM
 */

package org.netbeans.modules.uml.ui.swing.drawingarea;

import com.tomsawyer.editor.command.TSEIncrementalLayoutCommand;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETGraph;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.service.client.TSServiceProxy;
import com.tomsawyer.editor.service.TSEAllOptionsServiceInputData;

/**
 *
 * @author jm142314
 */
public class ADIncrementalLayoutCommand extends TSEIncrementalLayoutCommand {
    
    private TSEGraphWindow m_graphWindow;
    
    /** Creates a new instance of ADIncrementalLayoutCommand */
    public ADIncrementalLayoutCommand(TSEGraphWindow pGraphWindow,
            TSServiceProxy pServiceProxy,
            TSEAllOptionsServiceInputData pInputData,
            int pLayoutStyle) {
        super(pGraphWindow, pServiceProxy, pInputData, pLayoutStyle);
        
        this.m_graphWindow = pGraphWindow;
    }
    
    protected void doAction() throws Throwable {
        ETGraph graph = (ETGraph) this.m_graphWindow.getGraph();
        
        // Send preLayoutEvent to graph
        graph.onGraphEvent(IGraphEventKind.GEK_PRE_LAYOUT,null,null,null);
        
        super.doAction();
        
        // Send postLayoutEvent to graph
        graph.onGraphEvent(IGraphEventKind.GEK_POST_LAYOUT,null,null,null);
    } 
    
}
